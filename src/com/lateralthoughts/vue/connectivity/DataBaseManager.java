package com.lateralthoughts.vue.connectivity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.lateralthoughts.vue.AisleContext;
import com.lateralthoughts.vue.AisleImageDetails;
import com.lateralthoughts.vue.AisleWindowContent;
import com.lateralthoughts.vue.VueConstants;
import com.lateralthoughts.vue.VueTrendingAislesDataModel;
import com.lateralthoughts.vue.utils.Utils;

public class DataBaseManager {
  private static final String FORMATE = "%09d";
  private static final String DELETE_ROW = "1";
  private int mStartPosition = 0;
  private int mEndPosition = 0;
  private int mLocalAislesLimit = 10;
  private Context mContext;
  private int mPoolSize = 1;
  private int mMaxPoolSize = 1;
  private long mKeepAliveTime = 10;
  private ThreadPoolExecutor threadPool;
  private final LinkedBlockingQueue<Runnable> threadsQueue = new LinkedBlockingQueue<Runnable>();
  private static DataBaseManager manager;


  private DataBaseManager(Context context) {
    mContext = context;
    threadPool = new ThreadPoolExecutor(mPoolSize, mMaxPoolSize,
        mKeepAliveTime, TimeUnit.SECONDS, threadsQueue);
  }

  public static DataBaseManager getInstance(Context context) {
    if (manager == null) {
      manager = new DataBaseManager(context);
    }
    return manager;
  }

  /**
   * to start thread pool.
   * 
   * @param task Runnable
   */
  private void runTask(Runnable task) {
    threadPool.execute(task);
  }


  /**
   * add all the aisles pulled from server to sqlite, if the aisle is already
   * there in sqlite then it will delete and insert the new data for that aisle.
   * 
   * @param Context context.
   * */
  public static void addTrentingAislesFromServerToDB(Context context) {
    Cursor aisleIdCursor = context.getContentResolver().query(
        VueConstants.CONTENT_URI, new String[] {VueConstants.AISLE_ID}, null,
        null, null);
    ArrayList<String> aisleIds = new ArrayList<String>();
    if (aisleIdCursor.moveToFirst()) {
      do {
        aisleIds.add(aisleIdCursor.getString(aisleIdCursor
            .getColumnIndex(VueConstants.AISLE_ID)));
      } while (aisleIdCursor.moveToNext());
    }
    aisleIdCursor.close();
    for (int i = 0; i < VueTrendingAislesDataModel.getInstance(context)
        .getAisleCount(); i++) {
      Cursor cursor = context.getContentResolver()
          .query(VueConstants.CONTENT_URI, new String[] {"COUNT(*)"}, null,
              null, null);
      String strCount = "";
      int maxId = 0;
      int imgCount = 0;
      if (cursor.moveToFirst()) {
        strCount = cursor.getString(cursor.getColumnIndex("COUNT(*)"));
      }
      maxId = Integer.valueOf(strCount).intValue();
      cursor.close();
      AisleWindowContent content = VueTrendingAislesDataModel.getInstance(
          context).getAisleAt(i);
      AisleContext info = content.getAisleContext();
      ArrayList<AisleImageDetails> imageItemsArray = content.getImageList();
      ContentValues values = new ContentValues();
      values.put(VueConstants.FIRST_NAME, info.mFirstName);
      values.put(VueConstants.LAST_NAME, info.mLastName);
      values.put(VueConstants.JOIN_TIME, info.mJoinTime);
      values.put(VueConstants.LOOKING_FOR, info.mLookingForItem);
      values.put(VueConstants.OCCASION, info.mOccasion);
      values.put(VueConstants.USER_ID, info.mUserId);
      values.put(VueConstants.AISLE_ID, info.mAisleId);
      values.put(VueConstants.DELETE_FLAG, 0);
      if (aisleIds.contains(info.mAisleId)) {
        context.getContentResolver().update(VueConstants.CONTENT_URI, values,
            VueConstants.AISLE_ID + "=?", new String[] {info.mAisleId});
      } else {
        values.put(VueConstants.ID, String.format(FORMATE, maxId + 1));
        context.getContentResolver().insert(VueConstants.CONTENT_URI, values);
      }
      imgCount = 0;
      for (AisleImageDetails imageDetails : imageItemsArray) {
        ContentValues imgValues = new ContentValues();
        imgValues.put(VueConstants.TITLE, imageDetails.mTitle);
        imgValues.put(VueConstants.IMAGE_URL, imageDetails.mImageUrl);
        imgValues.put(VueConstants.DETAILS_URL, imageDetails.mDetalsUrl);
        imgValues.put(VueConstants.HEIGHT, imageDetails.mAvailableHeight);
        imgValues.put(VueConstants.WIDTH, imageDetails.mAvailableWidth);
        imgValues.put(VueConstants.STORE, imageDetails.mStore);
        imgValues.put(VueConstants.USER_ID, info.mUserId);
        imgValues.put(VueConstants.AISLE_ID, info.mAisleId);
        if (aisleIds.contains(info.mAisleId)) {
          context.getContentResolver().update(VueConstants.IMAGES_CONTENT_URI,
              imgValues, VueConstants.AISLE_ID + "=?",
              new String[] {info.mAisleId});
        } else {
          imgValues.put(VueConstants.IMAGE_ID, imageDetails.mId);
          imgValues.put(VueConstants.ID, String.format(FORMATE, ++imgCount));
          context.getContentResolver().insert(VueConstants.IMAGES_CONTENT_URI,
              imgValues);
        }
      }
    }
    deleteOutDatedAisles(context);
  }

  /**
   * This method will return the aisles with aislesIds we give in parameter, if
   * the parameter is null then it will return aisles in installment of 10.
   * 
   * @param String[] aislesIds
   * @return ArrayList<AisleWindowContent>
   * */
  public ArrayList<AisleWindowContent> getAislesFromDB(String[] aislesIds) {
    mEndPosition = mEndPosition + mLocalAislesLimit;
    AisleContext userInfo;
    AisleImageDetails imageItemDetails;
    AisleWindowContent aisleItem = null;
    String selection;
    String[] args = null;
    String sortOrder = null;
    LinkedHashMap<String, AisleContext> map = new LinkedHashMap<String, AisleContext>();
    ArrayList<AisleWindowContent> aisleContentArray = new ArrayList<AisleWindowContent>();
    ArrayList<AisleImageDetails> imageItemsArray = new ArrayList<AisleImageDetails>();
    if (aislesIds == null) {
      selection = VueConstants.ID + ">? AND " + VueConstants.ID + "<=?";
      String[] allAislesArgs = {String.format(FORMATE, mStartPosition),
          String.format(FORMATE, mEndPosition)};
      args = allAislesArgs;
      sortOrder = VueConstants.ID + " ASC";
    } else {
      String questionSymbols = "?";
      for (int i = 1; i < aislesIds.length; i++) {
        questionSymbols = questionSymbols + ",?";
      }
      selection = VueConstants.AISLE_ID + " IN (" + questionSymbols + ") ";
      args = aislesIds;
    }
    Cursor aislesCursor = mContext.getContentResolver().query(
        VueConstants.CONTENT_URI, null, selection, args, sortOrder);
    if (aislesCursor.moveToFirst()) {
      do {
        userInfo = new AisleContext();
        userInfo.mAisleId = aislesCursor.getString(aislesCursor
            .getColumnIndex(VueConstants.AISLE_ID));
        userInfo.mUserId = aislesCursor.getString(aislesCursor
            .getColumnIndex(VueConstants.USER_ID));
        userInfo.mFirstName = aislesCursor.getString(aislesCursor
            .getColumnIndex(VueConstants.FIRST_NAME));
        userInfo.mLastName = aislesCursor.getString(aislesCursor
            .getColumnIndex(VueConstants.LAST_NAME));
        userInfo.mJoinTime = Long.parseLong(aislesCursor.getString(aislesCursor
            .getColumnIndex(VueConstants.JOIN_TIME)));
        userInfo.mLookingForItem = aislesCursor.getString(aislesCursor
            .getColumnIndex(VueConstants.LOOKING_FOR));
        userInfo.mOccasion = aislesCursor.getString(aislesCursor
            .getColumnIndex(VueConstants.OCCASION));
        map.put(userInfo.mAisleId, userInfo);
      } while (aislesCursor.moveToNext());
    }
    Cursor aisleImagesCursor = mContext.getContentResolver().query(
        VueConstants.IMAGES_CONTENT_URI, null, null, null,
        VueConstants.ID + " ASC");
    Iterator it = map.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry pairs = (Map.Entry) it.next();
      System.out.println(pairs.getKey() + " = " + pairs.getValue());

      if (aisleImagesCursor.moveToFirst()) {
        do {
          if (aisleImagesCursor.getString(
              aisleImagesCursor.getColumnIndex(VueConstants.AISLE_ID)).equals(
              (String) pairs.getKey())) {
            imageItemDetails = new AisleImageDetails();
            imageItemDetails.mTitle = aisleImagesCursor
                .getString(aisleImagesCursor.getColumnIndex(VueConstants.TITLE));
            imageItemDetails.mImageUrl = aisleImagesCursor
                .getString(aisleImagesCursor
                    .getColumnIndex(VueConstants.IMAGE_URL));
            imageItemDetails.mDetalsUrl = aisleImagesCursor
                .getString(aisleImagesCursor
                    .getColumnIndex(VueConstants.DETAILS_URL));
            imageItemDetails.mStore = aisleImagesCursor
                .getString(aisleImagesCursor.getColumnIndex(VueConstants.STORE));
            imageItemDetails.mId = aisleImagesCursor
                .getString(aisleImagesCursor
                    .getColumnIndex(VueConstants.IMAGE_ID));
            imageItemDetails.mAvailableHeight = Integer
                .parseInt(aisleImagesCursor.getString(aisleImagesCursor
                    .getColumnIndex(VueConstants.HEIGHT)));
            imageItemDetails.mAvailableWidth = Integer
                .parseInt(aisleImagesCursor.getString(aisleImagesCursor
                    .getColumnIndex(VueConstants.WIDTH)));
            imageItemsArray.add(imageItemDetails);
          }
        } while (aisleImagesCursor.moveToNext());
      }
      userInfo = (AisleContext) pairs.getValue();
      aisleItem = new AisleWindowContent(userInfo.mAisleId);
      aisleItem.addAisleContent(userInfo, imageItemsArray);
      aisleContentArray.add(aisleItem);
      imageItemsArray.clear();
      it.remove(); // avoids a ConcurrentModificationException
    }
    aislesCursor.close();
    aisleImagesCursor.close();
    if (aislesIds == null) {
      mStartPosition = mEndPosition;
    }
    return aisleContentArray;
  }

  public void upDateAislesInDB() {

  }

  public void addComments(final String comment, final String imageID,
      final String aisleID) {
    runTask(new Runnable() {

      @Override
      public void run() {
        addCommentsToDb(comment, imageID, aisleID);
      }
    });
  }

  public void addLikeOrDisLike(final int likeStatus, final int likeCount,
      final String imageID, final String aisleID) {
    runTask(new Runnable() {

      @Override
      public void run() {
        addLikeOrDisLikeToDb(likeStatus, likeCount, imageID, aisleID);
      }
    });
  }

  public void bookMarkOrUnBookmarkAisle(final boolean isBookmarked,
      final int bookmarkCount, final String aisleID) {
    runTask(new Runnable() {

      @Override
      public void run() {
        bookMarkOrUnBookmarkAisleToDb(isBookmarked, bookmarkCount, aisleID);
      }
    });
  }

  /**
   * add new Comment on images and sets the DIRTY_FLAG true to indicate that
   * there is some new data to be uploaded to server.
   * 
   * @param String comment
   * @param String imageID
   * @param String aisleID
   * */
  private void addCommentsToDb(String comment, String imageID, String aisleID) {
    ContentValues aisleValues = new ContentValues();
    aisleValues.put(VueConstants.DIRTY_FLAG, true);
    mContext.getContentResolver().update(VueConstants.CONTENT_URI, aisleValues,
        VueConstants.AISLE_ID + "=?", new String[] {aisleID});
    mContext.getContentResolver().update(VueConstants.IMAGES_CONTENT_URI,
        aisleValues, VueConstants.AISLE_ID + "=?", new String[] {aisleID});
    aisleValues.put(VueConstants.COMMENTS, comment);
    aisleValues.put(VueConstants.IMAGE_ID, imageID);
    aisleValues.put(VueConstants.AISLE_ID, aisleID);
    mContext.getContentResolver().insert(VueConstants.COMMENTS_ON_IMAGE_URI,
        aisleValues);
  }

  private void addLikeOrDisLikeToDb(int likeStatus, int likeCount,
      String imageID, String aisleID) {
    ContentValues aisleValues = new ContentValues();
    aisleValues.put(VueConstants.DIRTY_FLAG, true);
    mContext.getContentResolver().update(VueConstants.CONTENT_URI, aisleValues,
        VueConstants.AISLE_ID + "=?", new String[] {aisleID});
    aisleValues.put(VueConstants.LIKE_OR_DISLIKE, likeStatus);
    aisleValues.put(VueConstants.LIKES_COUNT, likeCount);
    mContext.getContentResolver().update(VueConstants.IMAGES_CONTENT_URI,
        aisleValues,
        VueConstants.AISLE_ID + "=? AND " + VueConstants.IMAGE_ID + "=?",
        new String[] {aisleID, imageID});
  }

  /**
   * if user bookmarks of unbookmarks any aisle then this method will mark that
   * aisle as the case.
   * 
   * @param boolean isBookmarked
   * @param String aisleID
   * */
  private void bookMarkOrUnBookmarkAisleToDb(boolean isBookmarked,
      int bookmarkCount, String aisleID) {
    ContentValues values = new ContentValues();
    values.put(VueConstants.IS_BOOKMARKED, isBookmarked);
    values.put(VueConstants.BOOKMARK_COUNT, bookmarkCount);
    mContext.getContentResolver().update(VueConstants.CONTENT_URI, values,
        VueConstants.AISLE_ID + "=?", new String[] {aisleID});
  }


  public void addAisleMetaDataToDB(String tableName, AisleData aisleData) {
    Uri uri = null;
    if (tableName.equals(VueConstants.LOOKING_FOR_TABLE)) {
      uri = VueConstants.LOOKING_FOR_CONTENT_URI;
    } else if (tableName.equals(VueConstants.OCCASION_TABLE)) {
      uri = VueConstants.OCCASION_CONTENT_URI;
    } else if (tableName.equals(VueConstants.CATEGORY_TABLE)) {
      uri = VueConstants.CATEGORY_CONTENT_URI;
    } else {
      return;
    }
    ContentValues values = new ContentValues();
    values.put(VueConstants.KEYWORD, aisleData.keyword);
    values.put(VueConstants.LAST_USED_TIME, Utils.date());
    values.put(VueConstants.NUMBER_OF_TIMES_USED, aisleData.count);
    if (aisleData.isNew) {
      mContext.getContentResolver().insert(uri, values);
    } else {
      mContext.getContentResolver().update(uri, values,
          VueConstants.KEYWORD + "=?", new String[] {aisleData.keyword});
    }
  }


  public ArrayList<String> getAisleKeywords(String tableName) {
    ArrayList<String> aisleKeywordsList = null;
    Uri uri = null;
    if (tableName.equals(VueConstants.LOOKING_FOR_TABLE)) {
      uri = VueConstants.LOOKING_FOR_CONTENT_URI;
    } else if (tableName.equals(VueConstants.OCCASION_TABLE)) {
      uri = VueConstants.OCCASION_CONTENT_URI;
    } else if (tableName.equals(VueConstants.CATEGORY_TABLE)) {
      uri = VueConstants.CATEGORY_CONTENT_URI;
    } else {
      return null;
    }
    String twoWeeksBeforeTime = Utils.twoWeeksBeforeTime();
    Cursor c = mContext.getContentResolver().query(uri, null,
        VueConstants.LAST_USED_TIME + " >?", new String[] {twoWeeksBeforeTime},
        VueConstants.NUMBER_OF_TIMES_USED + " DESC");
    if (c.moveToFirst()) {
      aisleKeywordsList = new ArrayList<String>();
      do {
        aisleKeywordsList.add(c.getString(c
            .getColumnIndex(VueConstants.KEYWORD)));
      } while (c.moveToNext());
    }
    c.close();
    Cursor c1 = mContext.getContentResolver().query(uri, null,
        VueConstants.LAST_USED_TIME + " <=?",
        new String[] {twoWeeksBeforeTime},
        VueConstants.NUMBER_OF_TIMES_USED + " DESC");
    if (c1.moveToFirst()) {
      if (aisleKeywordsList == null)
        aisleKeywordsList = new ArrayList<String>();
      do {
        aisleKeywordsList.add(c1.getString(c1
            .getColumnIndex(VueConstants.KEYWORD)));
      } while (c1.moveToNext());
    }
    c1.close();
    return aisleKeywordsList;
  }

  public AisleData getAisleMetaDataForKeyword(String keyWord, String tableName) {
    AisleData aisleDataObj = null;
    Uri uri = null;
    if (tableName.equals(VueConstants.LOOKING_FOR_TABLE)) {
      uri = VueConstants.LOOKING_FOR_CONTENT_URI;
    } else if (tableName.equals(VueConstants.OCCASION_TABLE)) {
      uri = VueConstants.OCCASION_CONTENT_URI;
    } else if (tableName.equals(VueConstants.CATEGORY_TABLE)) {
      uri = VueConstants.CATEGORY_CONTENT_URI;
    } else {
      return null;
    }
    Cursor c = mContext.getContentResolver().query(uri, null,
        VueConstants.KEYWORD + "=?", new String[] {keyWord}, null);
    if (c.moveToFirst()) {
      aisleDataObj = new AisleData();
      aisleDataObj.keyword = c
          .getString(c.getColumnIndex(VueConstants.KEYWORD));
      aisleDataObj.count = c.getInt(c
          .getColumnIndex(VueConstants.NUMBER_OF_TIMES_USED));
      aisleDataObj.time = c.getString(c
          .getColumnIndex(VueConstants.LAST_USED_TIME));
    }
    c.close();
    return aisleDataObj;
  }

  public static boolean markOldAislesToDelete(Context context) {
    ContentValues values = new ContentValues();
    values.put(VueConstants.DELETE_FLAG, DELETE_ROW);
    int updatedaisleRows = context.getContentResolver().update(
        VueConstants.CONTENT_URI, values, null, null);
    Log.e("DataBaseManager", "Total Aisles marked to delete in aisle table : "
        + updatedaisleRows);
    int updatedimagesRows = context.getContentResolver().update(
        VueConstants.IMAGES_CONTENT_URI, values, null, null);
    Log.e("DataBaseManager", "Total Aisles marked to delete in images table : "
        + updatedimagesRows);
    // int updatedCommentsRows =
    // context.getContentResolver().update(VueConstants
    // .COMMENTS_ON_IMAGE_URI, values, null, null);
    // Log.e("DataBaseManager",
    // "Total Aisles marked to delete in images table : "
    // + updatedCommentsRows);
    return true;
  }

  private static void deleteOutDatedAisles(Context context) {
    int deletedAisles = context.getContentResolver().delete(
        VueConstants.CONTENT_URI, VueConstants.DELETE_FLAG + "=?",
        new String[] {DELETE_ROW});
    int deletedImages = context.getContentResolver().delete(
        VueConstants.IMAGES_CONTENT_URI, VueConstants.DELETE_FLAG + "=?",
        new String[] {DELETE_ROW});
    int deletedComments = context.getContentResolver().delete(
        VueConstants.COMMENTS_ON_IMAGE_URI, VueConstants.DELETE_FLAG + "=?",
        new String[] {DELETE_ROW});
    Log.e("DataBaseManager", "Total Aisles deleted : " + deletedAisles
        + ", Total Images deleted : " + deletedImages
        + ", Total Comments deleted : " + deletedComments);
  }
}
