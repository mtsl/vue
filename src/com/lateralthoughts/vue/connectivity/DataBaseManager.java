package com.lateralthoughts.vue.connectivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.lateralthoughts.vue.AisleContext;
import com.lateralthoughts.vue.AisleImageDetails;
import com.lateralthoughts.vue.AisleWindowContent;
import com.lateralthoughts.vue.ImageRating;
import com.lateralthoughts.vue.VueConstants;
import com.lateralthoughts.vue.utils.Utils;

public class DataBaseManager {
  private static final String FORMATE = "%09d";
  private static final String DELETE_ROW = "1";
  private int mStartPosition = 0;
  public int mEndPosition = 0;
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

 public void addTrentingAislesFromServerToDB(final Context context, final List<AisleWindowContent> contentList) {
   runTask(new Runnable() {
    
    @Override
    public void run() {
    	Log.i("dbInsert", "dbInsert1 calling threadpool: " );
      addAislesToDB(context, contentList);
    }
  });
 }
  
  /**
   * add all the aisles pulled from server to sqlite, if the aisle is already
   * there in sqlite then it will delete and insert the new data for that aisle.
   * 
   * @param Context context.
   * */
  private void addAislesToDB(Context context, List<AisleWindowContent> contentList) {
		Log.i("dbInsert", "dbInsert1 calling threadpool execute to db contentList.size: "+contentList.size() );
	  int imgCount = 0;
    Cursor aisleIdCursor = context.getContentResolver().query(
        VueConstants.CONTENT_URI, new String[] {VueConstants.AISLE_Id}, null,
        null, null);
    ArrayList<String> aisleIds = new ArrayList<String>();
    if (aisleIdCursor.moveToFirst()) {
      do {
        aisleIds.add(aisleIdCursor.getString(aisleIdCursor
            .getColumnIndex(VueConstants.AISLE_Id)));
      } while (aisleIdCursor.moveToNext());
    }
    int aislesCount = contentList.size();
    aisleIdCursor.close();
    for (int i = 0; i < aislesCount; i++) {
      Cursor cursor = context.getContentResolver()
          .query(VueConstants.CONTENT_URI, new String[] {"COUNT(*)"}, null,
              null, null);
      String strCount = "";
      int maxId = 0;
      
      if (cursor.moveToFirst()) {
        strCount = cursor.getString(cursor.getColumnIndex("COUNT(*)"));
      }
      maxId = Integer.valueOf(strCount).intValue();
      cursor.close();
      /*AisleWindowContent content = VueTrendingAislesDataModel.getInstance(
          context).getAisleAt(i);*/
      AisleWindowContent content = contentList.get(i);
      AisleContext info = content.getAisleContext();
      ArrayList<AisleImageDetails> imageItemsArray = content.getImageList();
      ContentValues values = new ContentValues();
      values.put(VueConstants.FIRST_NAME, info.mFirstName);
      values.put(VueConstants.LAST_NAME, info.mLastName);
      values.put(VueConstants.JOIN_TIME, info.mJoinTime);
      values.put(VueConstants.LOOKING_FOR, info.mLookingForItem);
      values.put(VueConstants.OCCASION, info.mOccasion);
      values.put(VueConstants.USER_ID, info.mUserId);
      values.put(VueConstants.AISLE_Id, info.mAisleId);
      values.put(VueConstants.BOOKMARK_COUNT, info.mBookmarkCount);
      values.put(VueConstants.DELETE_FLAG, 0);
      if (aisleIds.contains(info.mAisleId)) {
        context.getContentResolver().update(VueConstants.CONTENT_URI, values,
            VueConstants.AISLE_Id + "=?", new String[] {info.mAisleId});
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
        imgValues.put(VueConstants.AISLE_Id, info.mAisleId);
        if (aisleIds.contains(info.mAisleId)) {
          context.getContentResolver().update(VueConstants.IMAGES_CONTENT_URI,
              imgValues, VueConstants.AISLE_Id + "=?",
              new String[] {info.mAisleId});
        } else {
          imgValues.put(VueConstants.IMAGE_ID, imageDetails.mId);
          imgValues.put(VueConstants.ID, String.format(FORMATE, ++imgCount));
          try{
        	  Log.i("dbInsert", "dbInsert1 calling threadpool execute to db before insert: "  );
          Uri x = context.getContentResolver().insert(VueConstants.IMAGES_CONTENT_URI,
                      imgValues);
        	  Log.i("dbInsert", "dbInsert1 calling threadpool execute to db after insert:uri " +x );
          }catch(Exception e) {
        	  e.printStackTrace();
          }
        
        }
      }
      copydbToSdcard();
      Log.e("Profiling", "Profiling inserting new aisles to db");
    }
   
  //  copydbToSdcard();
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
      selection = VueConstants.ID + " >? AND " + VueConstants.ID + " <=? ";
      String[] allAislesArgs = {String.format(FORMATE, mStartPosition),
          String.format(FORMATE, mEndPosition)};
      args = allAislesArgs;
      sortOrder = VueConstants.ID + " ASC";
    } else {
      String questionSymbols = "?";
      for (int i = 1; i < aislesIds.length; i++) {
        questionSymbols = questionSymbols + ",?";
      }
      selection = VueConstants.AISLE_Id + " IN (" + questionSymbols + ") ";
      args = aislesIds;
    }
      Log.i("arrayList", "arrayList from db ***: "+selection);
     
    Cursor aislesCursor = mContext.getContentResolver().query(
        VueConstants.CONTENT_URI, null, selection, args, null);
    Log.i("arrayList", "arrayList from db aislesCursor count ***: "+aislesCursor.getCount());
    
    if (aislesCursor.moveToFirst()) {
      do {
        userInfo = new AisleContext();
        userInfo.mAisleId = aislesCursor.getString(aislesCursor
            .getColumnIndex(VueConstants.AISLE_Id));
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
              aisleImagesCursor.getColumnIndex(VueConstants.AISLE_Id)).equals(
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
      final String imageID, final String aisleID, final boolean dirtyFlag) {
    runTask(new Runnable() {

      @Override
      public void run() {
        addLikeOrDisLikeToDb(likeStatus, likeCount, imageID, aisleID, dirtyFlag);
      }
    });
  }

  public void bookMarkOrUnBookmarkAisle(final boolean isBookmarked,
      final int bookmarkCount, final String aisleID, final boolean isDirty) {
    runTask(new Runnable() {

      @Override
      public void run() {
        bookMarkOrUnBookmarkAisleToDb(isBookmarked, bookmarkCount, aisleID, isDirty);
      }
    });
  }

  public void updateOrAddRecentlyViewedAisles(final String aisleId) {
    runTask(new Runnable() {
      
      @Override
      public void run() {
        updateOrAddRecentlyViewedAislesList(aisleId);
        
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
        VueConstants.AISLE_Id + "=?", new String[] {aisleID});
    mContext.getContentResolver().update(VueConstants.IMAGES_CONTENT_URI,
        aisleValues, VueConstants.AISLE_Id + "=?", new String[] {aisleID});
    aisleValues.put(VueConstants.COMMENTS, comment);
    aisleValues.put(VueConstants.IMAGE_ID, imageID);
    aisleValues.put(VueConstants.AISLE_Id, aisleID);
    mContext.getContentResolver().insert(VueConstants.COMMENTS_ON_IMAGE_URI,
        aisleValues);
  }

  private void addLikeOrDisLikeToDb(int likeStatus, int likeCount,
      String imageID, String aisleID, boolean dirtyFlag) {
    ContentValues aisleValues = new ContentValues();
    aisleValues.put(VueConstants.DIRTY_FLAG, true);
    mContext.getContentResolver().update(VueConstants.IMAGES_CONTENT_URI, aisleValues,
        VueConstants.AISLE_Id + "=?", new String[] {aisleID});
    aisleValues.put(VueConstants.LIKE_OR_DISLIKE, likeStatus);
    if(likeCount != 0)
    aisleValues.put(VueConstants.LIKES_COUNT, likeCount);
    aisleValues.put(VueConstants.DIRTY_FLAG, (dirtyFlag == true) ? 1 : 0);
    mContext.getContentResolver().update(VueConstants.IMAGES_CONTENT_URI,
        aisleValues,
        VueConstants.AISLE_Id + "=? AND " + VueConstants.IMAGE_ID + "=?",
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
      int bookmarkCount, String aisleID, boolean isDirty) {
    ContentValues values = new ContentValues();
    values.put(VueConstants.IS_BOOKMARKED, isBookmarked);
    values.put(VueConstants.BOOKMARK_COUNT, bookmarkCount);
    values.put(VueConstants.DIRTY_FLAG, isDirty);
    mContext.getContentResolver().update(VueConstants.CONTENT_URI, values,
        VueConstants.AISLE_Id + "=?", new String[] {aisleID});
    copydbToSdcard();
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
  
  private ArrayList<AisleWindowContent> getAisles(Cursor aislesCursor) {
    AisleContext userInfo;
    AisleImageDetails imageItemDetails;
    AisleWindowContent aisleItem = null;
    String selection;
    String[] args = null;
    String sortOrder = null;
    
    LinkedHashMap<String, AisleContext> map = new LinkedHashMap<String, AisleContext>();
    ArrayList<AisleWindowContent> aisleContentArray = new ArrayList<AisleWindowContent>();
    ArrayList<AisleImageDetails> imageItemsArray = new ArrayList<AisleImageDetails>();
    
    /*Cursor aislesCursor = mContext.getContentResolver().query(
        VueConstants.CONTENT_URI, null, VueConstants.CATEGORY + "=?"null,
        new String[] {category}null, VueConstants.ID + " ASC");*/
    Log.i("cursize", "cursize: "+aislesCursor.getCount());
    
    if (aislesCursor.moveToFirst()) {
      do {
        userInfo = new AisleContext();
        userInfo.mAisleId = aislesCursor.getString(aislesCursor
            .getColumnIndex(VueConstants.AISLE_Id));
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
              aisleImagesCursor.getColumnIndex(VueConstants.AISLE_Id)).equals(
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
 
    return aisleContentArray;
    
  }
  
  
  public ArrayList<AisleWindowContent> getAislesByUserId(String userId) {
    return getAisles(getAislesCursor(userId, VueConstants.USER_ID));
  }
  
  public ArrayList<AisleWindowContent> getAislesByCategory(String category) {
    return getAisles(getAislesCursor(category, VueConstants.CATEGORY));
  }
  
  public ArrayList<AisleWindowContent> getAisleByAisleId(String aisleId) {
    return getAisles(getAislesCursor(aisleId, VueConstants.AISLE_Id));
  }
  
  public ArrayList<AisleWindowContent> getDirtyAisles(String dirtyFlag) {
    return getAisles(getAislesCursor(dirtyFlag, VueConstants.DIRTY_FLAG));
  }
  
  public ArrayList<ImageRating> getDirtyImages(String dirtyFlag) {
    ArrayList<ImageRating> images = new ArrayList<ImageRating>();
    Cursor cursor = mContext.getContentResolver().query(
        VueConstants.IMAGES_CONTENT_URI, null, VueConstants.DIRTY_FLAG + "=?",
        new String[] {dirtyFlag}, null);
    if(cursor.moveToFirst()) {
      do {
        if(cursor.getInt(cursor.getColumnIndex(VueConstants.DIRTY_FLAG)) == 1) {
        ImageRating imgRating = new ImageRating();
        imgRating.setImageId((long)cursor.getInt(cursor.getColumnIndex(VueConstants.IMAGE_ID)));
        imgRating.setAisleId((long)cursor.getInt(cursor.getColumnIndex(VueConstants.AISLE_Id)));
        imgRating.setLiked((cursor.getInt(cursor.getColumnIndex(VueConstants.LIKE_OR_DISLIKE)) == 1) ? true : false);
        images.add(imgRating);
        }
      } while(cursor.moveToNext());
    }
    return images;
  }
  
  private Cursor getAislesCursor(String searchString, String searchBy) {
    Cursor aislesCursor = mContext.getContentResolver().query(
        VueConstants.CONTENT_URI, null, searchBy + "=?",
        new String[] {searchString}, VueConstants.ID + " ASC");
    Log.e("DataBaseManager", "aislesCursor.getCount() aisleId: " + searchString);
    Log.e("DataBaseManager", "aislesCursor.getCount(): " + aislesCursor.getCount());
    return aislesCursor;
  }

  public void resetDbParams(){
	 mStartPosition = 0;
	  mEndPosition = 0;
  }
  

  
  private void changeDeleteFlag() {
    ContentValues values = new ContentValues();
    values.put(VueConstants.DELETE_FLAG, 1);
    int rowsUpdated = mContext.getContentResolver().update(
        VueConstants.CONTENT_URI, values, null, null);
    Log.e("DataBaseManager", "rowsUpdated: " + rowsUpdated);
  }
  
  public ArrayList<AisleWindowContent> getRecentlyViewedAisles() {
    ArrayList<AisleWindowContent> aisles = new ArrayList<AisleWindowContent>();
    for (String aisleId : getRecentlyViewedAislesId()) {
      aisles.addAll(getAisleByAisleId(aisleId));
    }
    return aisles;
  }
  
  private ArrayList<String> getRecentlyViewedAislesId() {
    Cursor cursor = mContext.getContentResolver().query(
        VueConstants.RECENTLY_VIEW_AISLES_URI, null, null, null,
        VueConstants.VIEW_TIME + " DESC");
    ArrayList<String> aisleIds = new ArrayList<String>();
    if (cursor.moveToFirst()) {
      do {
        aisleIds.add(cursor.getString(cursor
            .getColumnIndex(VueConstants.RECENTLY_VIEWED_AISLE_ID)));
      } while (cursor.moveToNext());
    }
    return aisleIds;
  }  
  

  private void updateOrAddRecentlyViewedAislesList(String aisleId) {
    Cursor cursor = mContext.getContentResolver().query(
        VueConstants.RECENTLY_VIEW_AISLES_URI, null, null, null,
        VueConstants.VIEW_TIME + " DESC");
    boolean isAisleViewed = false;
    String viewedId = null;
    if (cursor.moveToFirst()) {
      do {
        String id = cursor.getString(cursor
            .getColumnIndex(VueConstants.RECENTLY_VIEWED_AISLE_ID));
        if(id.equals(aisleId)) {
          isAisleViewed = true;
          viewedId = cursor.getString(cursor
              .getColumnIndex(VueConstants.RECENTLY_VIEWED_AISLE_ID));
          break;
        }
      } while (cursor.moveToNext());
    }
    ContentValues values = null;
    if(isAisleViewed) {
      Log.e("DataBaseManager", "Suru updateOrAddRecentlyViewedAislesList isAisleViewed: " + isAisleViewed);
      values = new ContentValues();
      values.put(VueConstants.VIEW_TIME, System.currentTimeMillis());
      Uri url = Uri.parse(VueConstants.RECENTLY_VIEW_AISLES_URI + "/" + viewedId);
      mContext.getContentResolver().update(url, values, null, null);
    } else {
      Log.e("DataBaseManager", "Suru updateOrAddRecentlyViewedAislesList isAisleViewed: " + isAisleViewed);
      values = new ContentValues();
      values.put(VueConstants.RECENTLY_VIEWED_AISLE_ID, aisleId);
      values.put(VueConstants.VIEW_TIME, System.currentTimeMillis());
      mContext.getContentResolver().insert(VueConstants.RECENTLY_VIEW_AISLES_URI, values);
    }
  }
  
  
  /**
   * FOR TESTING PERPOES ONLY, SHOULD BE REMOVED OR COMMENTED FROM WHERE IT IS
   * CALLING AFTER TESTING, to copy FishWrap.db to sdCard.
   */
  private void copydbToSdcard() {
    try {
      File sd = Environment.getExternalStorageDirectory();
      File data = Environment.getDataDirectory();

      if (sd.canWrite()) {
        String currentDBPath = "//data//com.lateralthoughts.vue//databases//Vue.db";
        String backupDBPath = "Vue.db";
        File currentDB = new File(data, currentDBPath);
        File backupDB = new File(sd, backupDBPath);

        if (currentDB.exists()) {
          FileChannel src = new FileInputStream(currentDB).getChannel();
          FileChannel dst = new FileOutputStream(backupDB).getChannel();
          dst.transferFrom(src, 0, src.size());
          src.close();
          dst.close();
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
