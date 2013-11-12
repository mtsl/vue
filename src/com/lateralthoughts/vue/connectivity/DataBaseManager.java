package com.lateralthoughts.vue.connectivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lateralthoughts.vue.AisleContext;
import com.lateralthoughts.vue.AisleImageDetails;
import com.lateralthoughts.vue.AisleWindowContent;
import com.lateralthoughts.vue.ImageRating;
import com.lateralthoughts.vue.VueApplication;
import com.lateralthoughts.vue.VueConstants;
import com.lateralthoughts.vue.VueUser;
import com.lateralthoughts.vue.domain.Aisle;
import com.lateralthoughts.vue.domain.AisleBookmark;
import com.lateralthoughts.vue.domain.ImageComment;
import com.lateralthoughts.vue.parser.ImageComments;
import com.lateralthoughts.vue.parser.Parser;
import com.lateralthoughts.vue.utils.UrlConstants;
import com.lateralthoughts.vue.utils.Utils;

public class DataBaseManager {

  public static final int TRENDING = 1;
  public static final int MY_AISLES = 2;
  public static final int AISLE_CREATED = 3;
  public static final int BOOKMARK = 4;
  private static final String FORMATE = "%05d";
  private static final String DELETE_ROW = "1";
  private static final int THOUSAND = 1000;
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
  private HashMap<String, Integer> aislesOrderMap;
  private HashMap<String, Integer> bookmarkedAislesOrderMap = new HashMap<String, Integer>();
 // private HashMap<String, HashMap<String, Integer>> imagesOrderMap = new HashMap<String, HashMap<String,Integer>>();


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

  public void addTrentingAislesFromServerToDB(final Context context,
      final List<AisleWindowContent> contentList, final int offsetValue, final int whichScreen) {
    runTask(new Runnable() {

      @Override
      public void run() {
        addAislesToDB(context, contentList, offsetValue, whichScreen, false);
      }
    });
  }

  public void addOrUpdateAisles(final Context context,
      final List<AisleWindowContent> contentList, final int offsetValue, final int whichScreen) {
    runTask(new Runnable() {

      @Override
      public void run() {
        addAislesToDB(context, contentList, offsetValue, whichScreen, false);
      }
    });
  }
  
  /**
   * update the aisle information in local DB, sends this task to thread pool.
   * @param AisleContext context.
   * */
  public void aisleUpdateToDB(final AisleContext context) {
    runTask(new Runnable() {
      
      @Override
      public void run() {
        aisleUpdate(context);
      }
    });
  }

  public void addBookmarkedAisles(final Context context,
      final List<AisleWindowContent> contentList, final int offsetValue, final int whichScreen) {
    Log.i("bookmark response", "bookmark response: SURUSURU windowList.size()sdsdsd 17:");
    runTask(new Runnable() {

      @Override
      public void run() {
        Log.i("bookmark response", "bookmark response: SURUSURU windowList.size()sdsdsd 18:");
        addAislesToDB(context, contentList, offsetValue, whichScreen, true);
      }
    });
  }
  
  /**
   * add all the aisles pulled from server to sqlite, if the aisle is already
   * there in sqlite then it will delete and insert the new data for that aisle.
   * 
   * @param Context context.
   * */
  private void addAislesToDB(Context context,
      List<AisleWindowContent> contentList, int offsetValue, int whichScreen, boolean isBookmarkedAisle) {
	  
    if(contentList.size() == 0) {
      return;
    }
    aislesOrderMap = new HashMap<String, Integer>();
    if(offsetValue == 0 && whichScreen == TRENDING && !isBookmarkedAisle) {
      Log.e("DataBaseManager", "SURU updated aisle Order: whichScreen == TRENDING, offsetValue == " + offsetValue);
      ArrayList<String> bookmarkaisleIds = new ArrayList<String>();
      String[] iDs = bookmarkaisleIds.toArray(new String[bookmarkaisleIds.size()]);
      int aislesDeleted = context.getContentResolver().delete(VueConstants.CONTENT_URI, null, null);
      int imagesDeleted = context.getContentResolver().delete(VueConstants.IMAGES_CONTENT_URI, VueConstants.AISLE_Id + "!=?", iDs);
      int commentsDeleted = context.getContentResolver().delete(VueConstants.COMMENTS_ON_IMAGE_URI, VueConstants.AISLE_Id + "!=?", iDs);
      Log.e("DataBaseManager", "SURU updated aisle Order: aislesDeleted: "
          + aislesDeleted + ", imagesDeleted: " + imagesDeleted
          + ", commentsDeleted: " + commentsDeleted);
      //imagesOrderMap.clear();
    } else if(isBookmarkedAisle) {
      bookmarkedAislesOrderMap.clear();
    }
    
    Cursor aisleIdCursor = context.getContentResolver().query(
        VueConstants.CONTENT_URI, new String[] {VueConstants.AISLE_Id, VueConstants.ID}, null,
        null, null);
    Cursor imageIdCursor = context.getContentResolver().query(
        VueConstants.IMAGES_CONTENT_URI, new String[] {VueConstants.IMAGE_ID},
        null, null, null);
    Cursor commntsCursor = context.getContentResolver().query(
        VueConstants.COMMENTS_ON_IMAGE_URI, new String[] {VueConstants.ID},
        null, null, null);

    ArrayList<String> aisleIds = new ArrayList<String>();
    ArrayList<String> imageIds = new ArrayList<String>();
    ArrayList<Long> commentsImgId = new ArrayList<Long>();
    if (aisleIdCursor.moveToFirst()) {
      do {
        String aisleId = aisleIdCursor.getString(aisleIdCursor
            .getColumnIndex(VueConstants.AISLE_Id));
        aisleIds.add(aisleId);
        if (whichScreen == MY_AISLES || whichScreen == AISLE_CREATED) {
          String order = aisleIdCursor.getString(aisleIdCursor
              .getColumnIndex(VueConstants.ID));
          aislesOrderMap.put(aisleId, Integer.parseInt(order));
        }
      } while (aisleIdCursor.moveToNext());
    }
    aisleIdCursor.close();
    if (imageIdCursor.moveToFirst()) {
      do {
        imageIds.add(imageIdCursor.getString(imageIdCursor
            .getColumnIndex(VueConstants.IMAGE_ID)));
      } while (imageIdCursor.moveToNext());
    }
    imageIdCursor.close();
    if (commntsCursor.moveToFirst()) {
      do {
        long commentId = commntsCursor.getLong(commntsCursor
            .getColumnIndex(VueConstants.ID));
        Log.e("DataBaseManage", "SURU Comments ID: " + commentId);
        commentsImgId.add(commentId);
      } while (commntsCursor.moveToNext());
    }
    commntsCursor.close();
    int aislesCount = contentList.size();
   for (int i = 0; i < aislesCount; i++) {
      AisleWindowContent content = contentList.get(i);
      AisleContext info = content.getAisleContext();
      if(whichScreen == TRENDING && aislesOrderMap.isEmpty()) {
       aislesOrderMap.put(info.mAisleId, THOUSAND); 
      } else if(whichScreen == TRENDING && !aislesOrderMap.isEmpty()) {
        aislesOrderMap.put(info.mAisleId, getMaxAisleValue(aislesOrderMap) + 1);
      } else if(whichScreen == MY_AISLES && !aislesOrderMap.containsKey(info.mAisleId)) {
        int newOrder = getMaxAisleValue(aislesOrderMap) + 1;
        aislesOrderMap.put(info.mAisleId, newOrder);
      } else if(whichScreen == AISLE_CREATED) {
        aislesOrderMap.put(info.mAisleId, getMinAisleValue(aislesOrderMap) - 1);
      } else if(isBookmarkedAisle) {
        Log.e("DataBaseManage", "isBookmarkedAisle: inn 1");
        Cursor cur = mContext.getContentResolver().query(VueConstants.MY_BOOKMARKED_AISLES_URI, null, null, null, null);
        int count = cur.getCount() + 1;
        cur.close();
        bookmarkedAislesOrderMap.put(info.mAisleId, count);
      }
      ArrayList<AisleImageDetails> imageItemsArray = content.getImageList();
      ContentValues values = new ContentValues();
      values.put(VueConstants.FIRST_NAME, info.mFirstName);
      values.put(VueConstants.LAST_NAME, info.mLastName);
      values.put(VueConstants.JOIN_TIME, info.mJoinTime);
      values.put(VueConstants.LOOKING_FOR, info.mLookingForItem);
      values.put(VueConstants.OCCASION, info.mOccasion);
      values.put(VueConstants.USER_ID, info.mUserId);
      values.put(VueConstants.BOOKMARK_COUNT, info.mBookmarkCount);
      values.put(VueConstants.CATEGORY, info.mCategory);
      values.put(VueConstants.AISLE_DESCRIPTION, info.mDescription);
      values.put(VueConstants.DELETE_FLAG, 0);
     
      if (aisleIds.contains(info.mAisleId)) {
        if(!isBookmarkedAisle) {
          int order = aislesOrderMap.get(info.mAisleId);
          values.put(VueConstants.ID, String.format(FORMATE, order));
          context.getContentResolver().update(VueConstants.CONTENT_URI, values,
              VueConstants.AISLE_Id + "=?", new String[] {info.mAisleId});
        } else {
          int order = bookmarkedAislesOrderMap.get(info.mAisleId);
          values.put(VueConstants.ID, String.format(FORMATE, order));
        int rows = context.getContentResolver().update(VueConstants.MY_BOOKMARKED_AISLES_URI, values,
            VueConstants.AISLE_Id + "=?", new String[] {info.mAisleId});
        Log.e("DataBaseManage", "bookmark response: SURUSURU rows updated:" + rows);
        if(rows == 0) {
          order = bookmarkedAislesOrderMap.get(info.mAisleId);
          values.put(VueConstants.ID, String.format(FORMATE, order));
          values.put(VueConstants.AISLE_Id, info.mAisleId);
          Uri uri = context.getContentResolver().insert(VueConstants.MY_BOOKMARKED_AISLES_URI, values);
          Log.e("DataBaseManage", "bookmark response: SURUSURU new ROW: !isBookmarkedAisle" + uri);
        }
       
        }
      } else {
        if(!isBookmarkedAisle) {
          int order = aislesOrderMap.get(info.mAisleId);
          values.put(VueConstants.ID, String.format(FORMATE, order));
          values.put(VueConstants.AISLE_Id, info.mAisleId);
          context.getContentResolver().insert(VueConstants.CONTENT_URI, values);
        } else {
          int order = bookmarkedAislesOrderMap.get(info.mAisleId);
          values.put(VueConstants.ID, String.format(FORMATE, order));
          int rows = context.getContentResolver().update(VueConstants.MY_BOOKMARKED_AISLES_URI, values,
              VueConstants.AISLE_Id + "=?", new String[] {info.mAisleId});
          if(rows == 0) {
            order = bookmarkedAislesOrderMap.get(info.mAisleId);
            values.put(VueConstants.ID, String.format(FORMATE, order));
            values.put(VueConstants.AISLE_Id, info.mAisleId);
            Uri uri = context.getContentResolver().insert(VueConstants.MY_BOOKMARKED_AISLES_URI, values);
            Log.e("DataBaseManage", "bookmark response: SURUSURU 3 new ROW: " + uri);
          }
        }
      }
      if (imageItemsArray != null) {
        for (AisleImageDetails imageDetails : imageItemsArray) {
          Cursor imgCountCursor = context.getContentResolver().query(
              VueConstants.IMAGES_CONTENT_URI, new String[] {"COUNT(*)"}, null,
              null, null);
          String strImgCount = "";
          int imgCount = 0;
          if (imgCountCursor.moveToFirst()) {
            strImgCount = imgCountCursor.getString(imgCountCursor
                .getColumnIndex("COUNT(*)"));
          }
          imgCount = Integer.valueOf(strImgCount).intValue();
          imgCountCursor.close();
          ContentValues imgValues = new ContentValues();
          imgValues.put(VueConstants.TITLE, imageDetails.mTitle);
          imgValues.put(VueConstants.IMAGE_URL, imageDetails.mImageUrl);
          imgValues.put(VueConstants.DETAILS_URL, imageDetails.mDetalsUrl);
          imgValues.put(VueConstants.HEIGHT, imageDetails.mAvailableHeight);
          imgValues.put(VueConstants.WIDTH, imageDetails.mAvailableWidth);
          imgValues.put(VueConstants.STORE, imageDetails.mStore);
          imgValues.put(VueConstants.USER_ID, info.mUserId);
          imgValues.put(VueConstants.AISLE_Id, info.mAisleId);
          imgValues.put(VueConstants.LIKES_COUNT, imageDetails.mLikesCount);

          if (imageIds.contains(imageDetails.mId)) {
            context.getContentResolver().update(
                VueConstants.IMAGES_CONTENT_URI, imgValues,
                VueConstants.IMAGE_ID + "=?", new String[] {imageDetails.mId});
          } else {
            imgValues.put(VueConstants.IMAGE_ID, imageDetails.mId);
            imgValues.put(VueConstants.ID, String.format(FORMATE, ++imgCount));
            try {
              Uri x = context.getContentResolver().insert(
                  VueConstants.IMAGES_CONTENT_URI, imgValues);
            } catch (Exception e) {
              e.printStackTrace();
            }
          }

          // image comments content values.
          ContentValues commentValues = new ContentValues();
          commentValues.put(VueConstants.IMAGE_ID, imageDetails.mId);
          commentValues.put(VueConstants.DIRTY_FLAG, false);
          commentValues.put(VueConstants.DELETE_FLAG, false);
          commentValues.put(VueConstants.AISLE_Id, info.mAisleId);
          for (ImageComments commnts : imageDetails.mCommentsList) {
            commentValues.put(VueConstants.COMMENTS, commnts.comment);
            commentValues
                .put(
                    VueConstants.LAST_MODIFIED_TIME,
                    (commnts.lastModifiedTimestamp != null) ? commnts.lastModifiedTimestamp
                        : System.currentTimeMillis());
            if (commentsImgId.contains(commnts.Id.longValue())) {
              Uri uri = Uri.parse(VueConstants.COMMENTS_ON_IMAGE_URI + "/"
                  + commnts.Id);
              context.getContentResolver().update(uri, commentValues, null,
                  null);
            } else {
              commentValues.put(VueConstants.ID, commnts.Id);
              context.getContentResolver().insert(
                  VueConstants.COMMENTS_ON_IMAGE_URI, commentValues);
            }
          }
        }
      }
    }
    // updateAisleOrder();
    copydbToSdcard();
  }

  /**
   * update the aisle information in local DB.
   * @param AisleContext context.
   * */
  private void aisleUpdate(AisleContext context) {
    boolean isAisle = false;
    if(aislesOrderMap != null && aislesOrderMap.isEmpty()) {
      Cursor aisleIdCursor = VueApplication.getInstance().getContentResolver().query(
          VueConstants.CONTENT_URI, new String[] {VueConstants.AISLE_Id, VueConstants.ID}, VueConstants.AISLE_Id + "=?",
          new String[]{context.mAisleId}, null);
      if(aisleIdCursor.moveToFirst()) {
        do {
          String aisleId = aisleIdCursor.getString(aisleIdCursor.getColumnIndex(VueConstants.AISLE_Id));
          if(aisleId.equals(context.mAisleId)) {
            aislesOrderMap.put(context.mAisleId, Integer.parseInt(aisleIdCursor
                .getString(aisleIdCursor.getColumnIndex(VueConstants.ID))));
            isAisle = true;
            break;
          }
        } while(aisleIdCursor.moveToNext());
        if(!isAisle) {
          aislesOrderMap.put(context.mAisleId, THOUSAND + (aisleIdCursor.getCount() + 1));
        }
      }
      aisleIdCursor.close();
    } else {
      if(aislesOrderMap == null) {
      aislesOrderMap = new HashMap<String, Integer>();
      }
      aislesOrderMap.put(context.mAisleId, getMaxAisleValue(aislesOrderMap) + 1);
    }
    ContentValues values = new ContentValues();
    values.put(VueConstants.FIRST_NAME, context.mFirstName);
    values.put(VueConstants.LAST_NAME, context.mLastName);
    values.put(VueConstants.JOIN_TIME, context.mJoinTime);
    values.put(VueConstants.LOOKING_FOR, context.mLookingForItem);
    values.put(VueConstants.OCCASION, context.mOccasion);
    values.put(VueConstants.USER_ID, context.mUserId);
    values.put(VueConstants.AISLE_Id, context.mAisleId);
    values.put(VueConstants.BOOKMARK_COUNT, context.mBookmarkCount);
    values.put(VueConstants.CATEGORY, context.mCategory);
    values.put(VueConstants.AISLE_DESCRIPTION, context.mDescription);
    values.put(VueConstants.DELETE_FLAG, 0);
    int order = aislesOrderMap.get(context.mAisleId);
    values.put(VueConstants.ID, String.format(FORMATE, order));
    if (isAisle) {
      VueApplication.getInstance().getContentResolver().update(VueConstants.CONTENT_URI, values,
          VueConstants.AISLE_Id + "=?", new String[] {context.mAisleId});
    } else {
      VueApplication.getInstance().getContentResolver().insert(VueConstants.CONTENT_URI, values);
    }
  }
  
  /**
   * This method will return the aisles with aislesIds we give in parameter, if
   * the parameter is null then it will return aisles in installment of 10.
   * 
   * @param String[] aislesIds
   * @return ArrayList<AisleWindowContent>
   * */
  public ArrayList<AisleWindowContent> getAislesFromDB(String[] aislesIds, boolean isBookmarked) {
    Cursor cursor = mContext.getContentResolver().query(
        VueConstants.CONTENT_URI, new String[] {VueConstants.ID}, null, null,
        VueConstants.ID + " ASC");
    if(mStartPosition == 0) {
      if(cursor.moveToFirst()) {
        mStartPosition = Integer.parseInt(cursor.getString(cursor.getColumnIndex(VueConstants.ID)));
      } else {
        mStartPosition = 1000;
      }
    }
    if(mEndPosition == 0) {
      if(cursor.moveToFirst()) {
    	  if(cursor.getString(cursor.getColumnIndex(VueConstants.ID)) != null) {
        mEndPosition = Integer.parseInt(cursor.getString(cursor.getColumnIndex(VueConstants.ID)));
    	  } else {
    		  mStartPosition = 1000;
    	  }
      } else {
        mEndPosition = 1000;
      }
    }
	
    cursor.close();
    mEndPosition = mEndPosition + mLocalAislesLimit;
    Log.e("DataBaseManager",
			"SURU updated aisle Order: DATABASE LODING 1 mEndPosition: "+mEndPosition+" mStartPosition: "+mStartPosition);
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
      selection = VueConstants.ID + " >=? AND " + VueConstants.ID + " <=? ";
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
    // isBookmarked
    Cursor aislesCursor = null; 
    if (isBookmarked) {
      aislesCursor = mContext.getContentResolver().query(
          VueConstants.MY_BOOKMARKED_AISLES_URI, null, selection, args,
          VueConstants.ID + " ASC");
    } else {
      aislesCursor = mContext.getContentResolver().query(
          VueConstants.CONTENT_URI, null, selection, args,
          VueConstants.ID + " ASC");
    }
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
        userInfo.mBookmarkCount = Integer
            .parseInt(aislesCursor.getString(aislesCursor
                .getColumnIndex(VueConstants.BOOKMARK_COUNT)));
        userInfo.mCategory = aislesCursor.getString(aislesCursor
            .getColumnIndex(VueConstants.CATEGORY));
        userInfo.mDescription = aislesCursor.getString(aislesCursor
            .getColumnIndex(VueConstants.AISLE_DESCRIPTION));
        map.put(userInfo.mAisleId, userInfo);
      } while (aislesCursor.moveToNext());
    }
    Cursor aisleImagesCursor = mContext.getContentResolver().query(
        VueConstants.IMAGES_CONTENT_URI, null, null, null,
        VueConstants.ID + " ASC");
    Cursor imgCommentCursor = mContext.getContentResolver().query(
        VueConstants.COMMENTS_ON_IMAGE_URI, null, null, null,
        VueConstants.LAST_MODIFIED_TIME + " DESC");

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
            imageItemDetails.mLikesCount = aisleImagesCursor
                .getInt(aisleImagesCursor
                    .getColumnIndex(VueConstants.LIKES_COUNT));
            imageItemDetails.mOwnerUserId = aisleImagesCursor
                    .getString(aisleImagesCursor
                        .getColumnIndex(VueConstants.USER_ID));

            if (imgCommentCursor.moveToFirst()) {
              ImageComments comments;
              do {
                if (imgCommentCursor.getLong(imgCommentCursor
                    .getColumnIndex(VueConstants.IMAGE_ID)) == Long
                    .parseLong(imageItemDetails.mId)) {
                  comments = new ImageComments();
                  comments.Id = imgCommentCursor.getLong(imgCommentCursor
                      .getColumnIndex(VueConstants.ID));
                  comments.imageId = imgCommentCursor.getLong(imgCommentCursor
                      .getColumnIndex(VueConstants.IMAGE_ID));
                  comments.comment = imgCommentCursor
                      .getString(imgCommentCursor
                          .getColumnIndex(VueConstants.COMMENTS));
                  comments.lastModifiedTimestamp = Long.parseLong(imgCommentCursor
                      .getString(imgCommentCursor
                          .getColumnIndex(VueConstants.LAST_MODIFIED_TIME)));
                  imageItemDetails.mCommentsList.add(comments);
                }

              } while (imgCommentCursor.moveToNext());
            }
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
    imgCommentCursor.close();
    if (aislesIds == null) {
      mStartPosition = mEndPosition;
    }
    return aisleContentArray;
  }


  public void addComments(final ImageComment createdImageComment,
      final boolean isCommentDirty) {
    runTask(new Runnable() {

      @Override
      public void run() {
        addCommentsToDb(createdImageComment, isCommentDirty);
      }
    });
  }

  public void addLikeOrDisLike(final int likeStatus, final int likeCount,
      final Long id, final String imageID, final String aisleID,
      final boolean dirtyFlag) {
    runTask(new Runnable() {

      @Override
      public void run() {
        addLikeOrDisLikeToDb(likeStatus, likeCount, id, imageID, aisleID,
            dirtyFlag);
      }
    });
  }

  public void bookMarkOrUnBookmarkAisle(final boolean isBookmarked,
      final int bookmarkCount, final Long bookmarkID, final String aisleID,
      final boolean isDirty) {
    Log.i("bookmark response", "bookmark response: SURUSURU windowList.size()sdsdsd 12:");
    Log.e("DataBaseManage", "isBookmarkedAisle: inn isBookmarked -88 " + isBookmarked);
    runTask(new Runnable() {

      @Override
      public void run() {
        Log.i("bookmark response", "bookmark response: SURUSURU windowList.size()sdsdsd 13:");
        Log.e("DataBaseManage", "isBookmarkedAisle: inn isBookmarked -89 " + isBookmarked);
        bookMarkOrUnBookmarkAisleToDb(isBookmarked, bookmarkCount, bookmarkID,
            aisleID, isDirty);
        Log.e("DataBaseManage", "isBookmarkedAisle: inn isBookmarked -89 LAST " + isBookmarked);
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

  public void insertRatedImages(
      final ArrayList<ImageRating> retrievedImageRating) {
    runTask(new Runnable() {

      @Override
      public void run() {
        insertRatedImagesToDB(retrievedImageRating);
      }
    });
  }

  public void updateBookmarkAisles(final Long bookmarkId,
      final String bookmarkedAisleId, final boolean isBookarked) {
    runTask(new Runnable() {

      @Override
      public void run() {
        updateBookmarkAislesToBDb(bookmarkId, bookmarkedAisleId, isBookarked);
      }
    });
  }
  
  public void deleteImage(final String imgId) {
    runTask(new Runnable() {
      
      @Override
      public void run() {
          deleteImagesfromDB(imgId);
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
  private void addCommentsToDb(ImageComment createdImageComment,
      boolean isCommentDirty) {
    ContentValues commentValues = new ContentValues();
    commentValues.put(VueConstants.ID, createdImageComment.getId().longValue());
    commentValues.put(VueConstants.IMAGE_ID, createdImageComment
        .getOwnerImageId().longValue());
    commentValues.put(VueConstants.LAST_MODIFIED_TIME, (createdImageComment
        .getLastModifiedTimestamp() != null) ? createdImageComment
        .getLastModifiedTimestamp().longValue() : System.currentTimeMillis());
    commentValues.put(VueConstants.COMMENTS, createdImageComment.getComment());
    commentValues.put(VueConstants.DIRTY_FLAG, isCommentDirty);
    commentValues.put(VueConstants.DELETE_FLAG, false);
    Uri uri = mContext.getContentResolver().insert(
        VueConstants.COMMENTS_ON_IMAGE_URI, commentValues);
    Log.e("NetworkHandler",
        "Comments Issue: addCommentsToDb() Inserted succes Uri: " + uri);
  }

  private void addLikeOrDisLikeToDb(int likeStatus, int likeCount, Long id,
      String imageID, String aisleID, boolean dirtyFlag) {
    ContentValues aisleValues = new ContentValues();
    aisleValues.put(VueConstants.LIKE_OR_DISLIKE, likeStatus);
    aisleValues.put(VueConstants.LIKES_COUNT, likeCount);
    aisleValues.put(VueConstants.DIRTY_FLAG, (dirtyFlag == true) ? 1 : 0);
    int rowsUpdated = mContext.getContentResolver().update(
        VueConstants.IMAGES_CONTENT_URI, aisleValues,
        VueConstants.AISLE_Id + "=? AND " + VueConstants.IMAGE_ID + "=?",
        new String[] {aisleID, imageID});
    Log.e("DatabaseManager", "likecountissue: imageTable rowsUpdated: "
        + rowsUpdated);
    updateRatedImages(id, imageID, aisleID, likeStatus);
  }

  /**
   * if user bookmarks of unbookmarks any aisle then this method will mark that
   * aisle as the case.
   * 
   * @param boolean isBookmarked
   * @param String aisleID
   * */
  private void bookMarkOrUnBookmarkAisleToDb(boolean isBookmarked,
      int bookmarkCount, Long bookmarkId, String aisleID, boolean isDirty) {
    Log.i("bookmark response", "bookmark response: SURUSURU windowList.size()sdsdsd 14:");
    Log.e("DataBaseManage", "isBookmarkedAisle: inn -4");
    ContentValues values = new ContentValues();
    values.put(VueConstants.IS_BOOKMARKED, isBookmarked);
    values.put(VueConstants.BOOKMARK_COUNT, bookmarkCount);
    values.put(VueConstants.DIRTY_FLAG, isDirty);
    Log.e("DataBaseManage", "isBookmarkedAisle: inn -5");
    mContext.getContentResolver().update(VueConstants.CONTENT_URI, values,
        VueConstants.AISLE_Id + "=?", new String[] {aisleID});
    Log.e("DataBaseManage", "isBookmarkedAisle: inn -6");
    updateBookmarkAislesToBDb(bookmarkId, aisleID, isBookmarked);
  }

  // TODO: get aisles from server by aisle Id in a loop and insert it in db.
  public void updateBookmarkAislesToBDb(Long bookmarkId,
      String bookmarkedAisleId, boolean isBookmarked) {
    boolean isMatched = false;
    ContentValues values = new ContentValues();
    values.put(VueConstants.IS_LIKED_OR_BOOKMARKED, isBookmarked);
    values.put(VueConstants.AISLE_ID, bookmarkedAisleId);
    Log.i("bookmark response", "bookmark response: SURUSURU windowList.size()sdsdsd 15:");
    Log.e("DataBaseManage", "isBookmarkedAisle: inn isBookmarked -7 " + isBookmarked);
    if (isBookmarked) {
      String url = UrlConstants.GET_AISLE_RESTURL + "/" + bookmarkedAisleId;
      
        Response.Listener listener = new Response.Listener<JSONObject>() {

          @Override
          public void onResponse(final JSONObject jsonObject) {
            if (jsonObject != null) {
              Thread t = new Thread(new Runnable() {
                  
                  @Override
                  public void run() {
                    try {
                    Parser parser = new Parser();
                    AisleContext retrievedAisle = parser.parseAisleData(jsonObject);
                    ArrayList<AisleImageDetails> imageDetails = parser.getImagesForAisleId(retrievedAisle.mAisleId);
                    AisleWindowContent aisleItem = new AisleWindowContent(retrievedAisle.mAisleId);
                    aisleItem.addAisleContent(retrievedAisle, imageDetails);
                    ArrayList<AisleWindowContent> aisleContentArray = new ArrayList<AisleWindowContent>();
                    aisleContentArray.add(aisleItem);
                    
                    addBookmarkedAisles(mContext, aisleContentArray, 0, BOOKMARK);
                    } catch (Exception e) {
                      e.printStackTrace();
                    }
                    
                  }
                });
               t.start();
            }
          }};
      Response.ErrorListener errorListener = new ErrorListener() {

        @Override
        public void onErrorResponse(VolleyError arg0) {
          Log.e("DataBaseManager", "My Bookmarked Server Error Responce");
        }};
      
      JsonObjectRequest request = new JsonObjectRequest(url, null, listener, errorListener);
      request.setRetryPolicy(new DefaultRetryPolicy(
          DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 
            Utils.MAX_RETRIES, 
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
     VueApplication.getInstance().getRequestQueue().add(request);
    } else {
     int rowdeleted = mContext.getContentResolver().delete(
          VueConstants.MY_BOOKMARKED_AISLES_URI, VueConstants.AISLE_Id + "=?",
          new String[] {bookmarkedAisleId});
      Log.e("DataBaseManage", "isBookmarkedAisle: inn DELETED -2 " + rowdeleted);
    }
    Cursor cursor = mContext.getContentResolver().query(
        VueConstants.BOOKMARKER_AISLES_URI, null, null, null, null);
    if (cursor.moveToFirst()) {
      do {
        String aisleId = cursor.getString(cursor
            .getColumnIndex(VueConstants.AISLE_ID));
        if (bookmarkedAisleId.equals(aisleId)) {
          mContext.getContentResolver().update(
              VueConstants.BOOKMARKER_AISLES_URI, values,
              VueConstants.AISLE_ID + "=?", new String[] {bookmarkedAisleId});
          isMatched = true;
          break;
        }
      } while (cursor.moveToNext());
    }
    cursor.close();
    if (!isMatched) {
      values.put(VueConstants.ID, bookmarkId);
      Uri uri = mContext.getContentResolver().insert(
          VueConstants.BOOKMARKER_AISLES_URI, values);
      Log.e("bookmarkissue", "bookmarkissue new aisle inserted Uri: " + uri);
    }

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
    int updatedimagesRows = context.getContentResolver().update(
        VueConstants.IMAGES_CONTENT_URI, values, null, null);
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
        userInfo.mBookmarkCount = Integer
            .parseInt(aislesCursor.getString(aislesCursor
                .getColumnIndex(VueConstants.BOOKMARK_COUNT)));
        map.put(userInfo.mAisleId, userInfo);
      } while (aislesCursor.moveToNext());
    }
    Cursor aisleImagesCursor = mContext.getContentResolver().query(
        VueConstants.IMAGES_CONTENT_URI, null, null, null,
        VueConstants.ID + " ASC");
    
    Cursor imgCommentCursor = mContext.getContentResolver().query(
        VueConstants.COMMENTS_ON_IMAGE_URI, null, null, null,
        VueConstants.LAST_MODIFIED_TIME + " DESC");
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
            imageItemDetails.mLikesCount = aisleImagesCursor
                .getInt(aisleImagesCursor
                    .getColumnIndex(VueConstants.LIKES_COUNT));
            imageItemDetails.mOwnerUserId = aisleImagesCursor
                    .getString(aisleImagesCursor
                        .getColumnIndex(VueConstants.USER_ID));
            if (imgCommentCursor.moveToFirst()) {
              ImageComments comments;
              do {
                if (imgCommentCursor.getLong(imgCommentCursor
                    .getColumnIndex(VueConstants.IMAGE_ID)) == Long
                    .parseLong(imageItemDetails.mId)) {
                  comments = new ImageComments();
                  comments.Id = imgCommentCursor.getLong(imgCommentCursor
                      .getColumnIndex(VueConstants.ID));
                  comments.imageId = imgCommentCursor.getLong(imgCommentCursor
                      .getColumnIndex(VueConstants.IMAGE_ID));
                  comments.comment = imgCommentCursor
                      .getString(imgCommentCursor
                          .getColumnIndex(VueConstants.COMMENTS));
                  comments.lastModifiedTimestamp = Long.parseLong(imgCommentCursor
                      .getString(imgCommentCursor
                          .getColumnIndex(VueConstants.LAST_MODIFIED_TIME)));
                  imageItemDetails.mCommentsList.add(comments);
                }

              } while (imgCommentCursor.moveToNext());
            }
            
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
    imgCommentCursor.close();

    return aisleContentArray;

  }


  public ArrayList<AisleWindowContent> getAislesByUserId(String userId) {
    return getAisles(getAislesCursor(userId, VueConstants.USER_ID, false));
  }

  public ArrayList<AisleWindowContent> getAislesByCategory(String category) {
    return getAisles(getAislesCursor(category, VueConstants.CATEGORY, false));
  }

  public ArrayList<AisleWindowContent> getAisleByAisleId(String aisleId) {
    return getAisles(getAislesCursor(aisleId, VueConstants.AISLE_Id, false));
  }

  public ArrayList<AisleWindowContent> getAisleByAisleIdFromBookmarks(String aisleId) {
    return getAisles(getAislesCursor(aisleId, VueConstants.AISLE_Id, true));
  }
  public ArrayList<AisleWindowContent> getDirtyAisles(String dirtyFlag) {
    return getAisles(getAislesCursor(dirtyFlag, VueConstants.DIRTY_FLAG, false));
  }

  public ArrayList<ImageRating> getDirtyImages(String dirtyFlag) {
    ArrayList<ImageRating> images = new ArrayList<ImageRating>();
    Cursor cursor = mContext.getContentResolver().query(
        VueConstants.IMAGES_CONTENT_URI, null, VueConstants.DIRTY_FLAG + "=?",
        new String[] {dirtyFlag}, null);
    if (cursor.moveToFirst()) {
      do {
        if (cursor.getInt(cursor.getColumnIndex(VueConstants.DIRTY_FLAG)) == 1) {
          ImageRating imgRating = new ImageRating();
          imgRating.setImageId((long) cursor.getInt(cursor
              .getColumnIndex(VueConstants.IMAGE_ID)));
          imgRating.setAisleId((long) cursor.getInt(cursor
              .getColumnIndex(VueConstants.AISLE_Id)));
          imgRating.setLiked((cursor.getInt(cursor
              .getColumnIndex(VueConstants.LIKE_OR_DISLIKE)) == 1) ? true
              : false);
          images.add(imgRating);
        }
      } while (cursor.moveToNext());
    }
    return images;
  }

  private Cursor getAislesCursor(String searchString, String searchBy, boolean isBookmark) {
    Cursor aislesCursor = null;
    if(isBookmark) {
      aislesCursor = mContext.getContentResolver().query(
          VueConstants.MY_BOOKMARKED_AISLES_URI, null, searchBy + "=?",
          new String[] {searchString}, VueConstants.ID + " ASC");
    } else {
    aislesCursor = mContext.getContentResolver().query(
        VueConstants.CONTENT_URI, null, searchBy + "=?",
        new String[] {searchString}, VueConstants.ID + " ASC");
    }
    return aislesCursor;
  }

  public void resetDbParams() {
    mStartPosition = 0;
    mEndPosition = 0;
  }

  public ArrayList<ImageComment> getDirtyComments(String dirtyFlag) {
    ArrayList<ImageComment> comments = new ArrayList<ImageComment>();
    Cursor cursor = mContext.getContentResolver().query(
        VueConstants.CATEGORY_CONTENT_URI, null,
        VueConstants.DIRTY_FLAG + "=?", new String[] {dirtyFlag}, null);
    if (cursor.moveToFirst()) {
      do {
        ImageComment comment = new ImageComment();
        // comment.setId(cursor.getLong(cursor.getColumnIndex(VueConstants.ID)));
        comment.setOwnerImageId(cursor.getLong(cursor
            .getColumnIndex(VueConstants.IMAGE_ID)));
        comment.setComment(cursor.getString(cursor
            .getColumnIndex(VueConstants.COMMENTS)));
        comment.setOwnerUserId(Long.parseLong(getUserId()));
        comment.setCommenterFirstName("");
        comment.setCommenterLastName("");
        comments.add(comment);
      } while (cursor.moveToNext());
    }

    return comments;
  }


  private String getUserId() {
    VueUser storedVueUser = null;
    try {
      storedVueUser = Utils.readUserObjectFromFile(
          VueApplication.getInstance(),
          VueConstants.VUE_APP_USEROBJECT__FILENAME);
    } catch (Exception e) {
      e.printStackTrace();
    }
    String userId = null;
    if (storedVueUser != null) {
      userId = Long.valueOf(storedVueUser.getId()).toString();
    }
    return userId;

  }


  private void changeDeleteFlag() {
    ContentValues values = new ContentValues();
    values.put(VueConstants.DELETE_FLAG, 1);
    int rowsUpdated = mContext.getContentResolver().update(
        VueConstants.CONTENT_URI, values, null, null);
  }

  public ArrayList<AisleWindowContent> getRecentlyViewedAisles() {
    ArrayList<AisleWindowContent> aisles = new ArrayList<AisleWindowContent>();
    for (String aisleId : getRecentlyViewedAislesId()) {
      Log.e("DataBaseManager", "Suru recently viewed aisleId: " + aisleId);
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
        if (id.equals(aisleId)) {
          isAisleViewed = true;
          viewedId = cursor.getString(cursor.getColumnIndex(VueConstants.ID));
          break;
        }
      } while (cursor.moveToNext());
    }
    cursor.close();
    ContentValues values = new ContentValues();
    if (isAisleViewed) {
      values.put(VueConstants.VIEW_TIME, System.currentTimeMillis());
      Uri url = Uri.parse(VueConstants.RECENTLY_VIEW_AISLES_URI + "/"
          + viewedId);
      mContext.getContentResolver().update(url, values, null, null);
    } else {
      values.put(VueConstants.RECENTLY_VIEWED_AISLE_ID, aisleId);
      values.put(VueConstants.VIEW_TIME, System.currentTimeMillis());
      mContext.getContentResolver().insert(
          VueConstants.RECENTLY_VIEW_AISLES_URI, values);
    }
  }

  private void insertRatedImagesToDB(ArrayList<ImageRating> retrievedImageRating) {
    boolean isMatched = false;
    Cursor cursor = mContext.getContentResolver().query(
        VueConstants.RATED_IMAGES_URI, null, null, null, null);

    for (ImageRating imageRating : retrievedImageRating) {
      isMatched = false;
      ContentValues values = new ContentValues();
      values.put(VueConstants.AISLE_ID, imageRating.getAisleId());
      values.put(VueConstants.IMAGE_ID, imageRating.getImageId());
      values.put(VueConstants.IS_LIKED_OR_BOOKMARKED, imageRating.getLiked());
      String imId = Long.toString(imageRating.getImageId().longValue());

      if (cursor.moveToFirst()) {
        do {
          String imgId = cursor.getString(cursor
              .getColumnIndex(VueConstants.IMAGE_ID));
          if (imgId.equals(imId)) {
            int updatedrows = mContext.getContentResolver().update(
                VueConstants.RATED_IMAGES_URI,
                values,
                VueConstants.IMAGE_ID + "=? AND " + VueConstants.AISLE_ID
                    + "=?",
                new String[] {Long.toString(imageRating.getImageId()),
                    Long.toString(imageRating.getAisleId())});
            isMatched = true;
            break;
          }

        } while (cursor.moveToNext());
      }
      if (!isMatched) {
        values.put(VueConstants.ID, imageRating.getId());
        mContext.getContentResolver().insert(VueConstants.RATED_IMAGES_URI,
            values);
      }
    }
    cursor.close();
  }

  private void updateRatedImages(Long id, String imageID, String aisleID,
      int likeStatus) {
    if (id == null) {
      return;
    }
    boolean isMatched = false;
    ContentValues values = new ContentValues();
    values.put(VueConstants.ID, id.longValue());
    values.put(VueConstants.IMAGE_ID, imageID);
    values.put(VueConstants.AISLE_ID, aisleID);
    values.put(VueConstants.IS_LIKED_OR_BOOKMARKED, likeStatus);
    Cursor c = mContext.getContentResolver().query(
        VueConstants.RATED_IMAGES_URI, null, null, null, null);
    if (c.moveToFirst()) {
      do {
        String imgId = c.getString(c.getColumnIndex(VueConstants.IMAGE_ID));
        if (imgId.equals(imageID)) {
          int updatedrows = mContext.getContentResolver().update(
              VueConstants.RATED_IMAGES_URI, values,
              VueConstants.IMAGE_ID + "=? AND " + VueConstants.AISLE_ID + "=?",
              new String[] {imageID, aisleID});
          isMatched = true;
          break;
        }
      } while (c.moveToNext());
    }
    c.close();
    if (!isMatched) {
      Uri uri = mContext.getContentResolver().insert(
          VueConstants.RATED_IMAGES_URI, values);
    }
  }

  public ArrayList<ImageRating> getRatedImagesList(String aisleId) {
    ArrayList<ImageRating> imgRatingList = new ArrayList<ImageRating>();
    Cursor cursor = mContext.getContentResolver().query(
        VueConstants.RATED_IMAGES_URI,
        null,
        VueConstants.AISLE_ID + "=? AND " + VueConstants.IS_LIKED_OR_BOOKMARKED
            + "=?", new String[] {aisleId, "1"}, null);
    if (cursor.moveToFirst()) {
      do {
        ImageRating imgRating = new ImageRating();
        imgRating.setId(Long.parseLong(cursor.getString(cursor
            .getColumnIndex(VueConstants.ID))));
        imgRating.setImageId(Long.parseLong(cursor.getString(cursor
            .getColumnIndex(VueConstants.IMAGE_ID))));
        imgRating.setAisleId(Long.parseLong(cursor.getString(cursor
            .getColumnIndex(VueConstants.AISLE_ID))));
        long bookmark = Long.parseLong(cursor.getString(cursor
            .getColumnIndex(VueConstants.IS_LIKED_OR_BOOKMARKED)));
        imgRating.setLiked((bookmark == 1) ? true : false);
        imgRatingList.add(imgRating);
      } while (cursor.moveToNext());
    }
    cursor.close();
    copydbToSdcard();
    return imgRatingList;
  }

  public ArrayList<AisleBookmark> getBookmarkAisleIdsList() {
    AisleBookmark aisleBookmark;
    ArrayList<AisleBookmark> bookmarkAisles = new ArrayList<AisleBookmark>();
    Cursor cursor = mContext.getContentResolver().query(
        VueConstants.BOOKMARKER_AISLES_URI, null,
        VueConstants.IS_LIKED_OR_BOOKMARKED + "=?", new String[] {"1"}, null);
    if (cursor.moveToFirst()) {
      do {
        aisleBookmark = new AisleBookmark();
        aisleBookmark.setAisleId(Long.parseLong(cursor.getString(cursor
            .getColumnIndex(VueConstants.AISLE_ID))));
        aisleBookmark.setId(cursor.getLong(cursor
            .getColumnIndex(VueConstants.ID)));
        bookmarkAisles.add(aisleBookmark);
      } while (cursor.moveToNext());
    }
    cursor.close();
    return bookmarkAisles;
  }

 private int getMaxAisleValue(HashMap<String, Integer> mapOrder) {
    Entry<String,Integer> maxEntry = null;
    for(Entry<String,Integer> entry : mapOrder.entrySet()) {
        if (maxEntry == null || entry.getValue() > maxEntry.getValue()) {
            maxEntry = entry;
        }
    }
    return maxEntry.getValue();
  }
 
 private int getMinAisleValue(HashMap<String, Integer> mapOrder) {
   Entry<String,Integer> minEntry = null;
   for(Entry<String,Integer> entry : mapOrder.entrySet()) {
     if(minEntry == null) {
       minEntry = entry;
     } else if (entry.getValue() < minEntry.getValue()) {
         minEntry = entry;
       }
   }
   return (minEntry == null) ? 0 : minEntry.getValue();
 }
 
 @Deprecated // not in use by Surendra
 private void updateAisleOrder() {
   ContentValues values = new ContentValues();
   Cursor aisleIdCursor = mContext.getContentResolver().query(
       VueConstants.CONTENT_URI, new String[] {VueConstants.AISLE_Id}, null,
       null, null);
   Iterator<Entry<String, Integer>> entries = aislesOrderMap.entrySet().iterator();
   while(entries.hasNext()) {
     Map.Entry entry = (Map.Entry) entries.next();
     String key = (String)entry.getKey();
     Integer value = (Integer)entry.getValue();
     values.put(VueConstants.ID, String.format(FORMATE, value));
     Uri uri = Uri.parse( VueConstants.CONTENT_URI + "/" + key);
     int rowsUpdated = mContext.getContentResolver().update(uri, values, null, null);
   }
 }
 
 private boolean deleteImagesfromDB(String imgId) {
    int rowDeleted = mContext.getContentResolver().delete(VueConstants.IMAGES_CONTENT_URI,
        VueConstants.IMAGE_ID + "=?", new String[] {imgId});
    return (rowDeleted == 1) ? true : false;
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
