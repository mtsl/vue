package com.lateralthoughts.vue.connectivity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.lateralthoughts.vue.AisleContext;
import com.lateralthoughts.vue.AisleImageDetails;
import com.lateralthoughts.vue.AisleWindowContent;
import com.lateralthoughts.vue.VueConstants;
import com.lateralthoughts.vue.VueTrendingAislesDataModel;

public class DataBaseManager {
  private static final String FORMATE = "%09d";
  private int mStartPosition = 0;
  private int mEndPosition = 0;
  private int mLocalAislesLimit = 10;

  Context mContext;
  
  public DataBaseManager(Context context) {
    mContext = context;
  }
  
  public static void addTrentingAislesFromServerToDB(Context context) {
    for (int i = 0; i < VueTrendingAislesDataModel.getInstance(context).getAisleCount(); i++) {
      Cursor cursor = context.getContentResolver().query(VueConstants.CONTENT_URI,
          new String[] {"COUNT(*)"}, null, null, null);
      String strCount = "";
      int maxId = 0;
      if (cursor.moveToFirst()) {
        strCount = cursor.getString(cursor.getColumnIndex("COUNT(*)"));
      }
      maxId = Integer.valueOf(strCount).intValue();
      cursor.close();
      AisleWindowContent content = VueTrendingAislesDataModel.getInstance(context).getAisleAt(i);
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
      values.put(VueConstants.ID, String.format(FORMATE, maxId + 1));
      context.getContentResolver().insert(VueConstants.CONTENT_URI, values);
      int imgCount = 0;
      for (AisleImageDetails imageDetails : imageItemsArray) {
        ContentValues imgValues = new ContentValues();
        imgValues.put(VueConstants.ID, String.format(FORMATE, imgCount + 1));
        imgValues.put(VueConstants.TITLE, imageDetails.mTitle);
        imgValues.put(VueConstants.IMAGE_URL, imageDetails.mImageUrl);
        imgValues.put(VueConstants.DETAILS_URL, imageDetails.mDetalsUrl);
        imgValues.put(VueConstants.HEIGHT, imageDetails.mAvailableHeight);
        imgValues.put(VueConstants.WIDTH, imageDetails.mAvailableWidth);
        imgValues.put(VueConstants.STORE, imageDetails.mStore);
        imgValues.put(VueConstants.IMAGE_ID, imageDetails.mId);
        imgValues.put(VueConstants.USER_ID, info.mUserId);
        imgValues.put(VueConstants.AISLE_ID, info.mAisleId);
        context.getContentResolver().insert(VueConstants.IMAGES_CONTENT_URI, imgValues);
      }
    }
  }
  
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
    if(aislesIds == null) {
      selection = VueConstants.ID + ">? AND " + VueConstants.ID + "<=?";
      String[]  allAislesArgs = {String.format(FORMATE, mStartPosition),
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
    Cursor aislesCursor = mContext.getContentResolver().query(VueConstants.CONTENT_URI,
        null, selection, args, sortOrder);
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
      Cursor aisleImagesCursor = mContext.getContentResolver().query(VueConstants.IMAGES_CONTENT_URI,
          null, null, null, VueConstants.ID + " ASC");
      Iterator it = map.entrySet().iterator();
      while (it.hasNext()) {
        Map.Entry pairs = (Map.Entry) it.next();
         System.out.println(pairs.getKey() + " = " + pairs.getValue());

        if (aisleImagesCursor.moveToFirst()) {
          do {
            if (aisleImagesCursor.getString(
                aisleImagesCursor.getColumnIndex(VueConstants.AISLE_ID))
                .equals((String) pairs.getKey())) {
              imageItemDetails = new AisleImageDetails();
              imageItemDetails.mTitle = aisleImagesCursor
                  .getString(aisleImagesCursor
                      .getColumnIndex(VueConstants.TITLE));
              imageItemDetails.mImageUrl = aisleImagesCursor
                  .getString(aisleImagesCursor
                      .getColumnIndex(VueConstants.IMAGE_URL));
              imageItemDetails.mDetalsUrl = aisleImagesCursor
                  .getString(aisleImagesCursor
                      .getColumnIndex(VueConstants.DETAILS_URL));
              imageItemDetails.mStore = aisleImagesCursor
                  .getString(aisleImagesCursor
                      .getColumnIndex(VueConstants.STORE));
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
    if(aislesIds == null) {
      mStartPosition = mEndPosition;
    }
    return aisleContentArray;
  }
  
  public void upDateAislesInDB() {
    
  }
  
  public void addComments(String comment, String imageID, String aisleID) {
    ContentValues aisleValues = new ContentValues();
    aisleValues.put(VueConstants.DIRTY_FLAG, true);
    mContext.getContentResolver().update(VueConstants.CONTENT_URI, aisleValues,
        VueConstants.AISLE_ID + "=?", new String[] {aisleID});
    mContext.getContentResolver().update(VueConstants.IMAGES_CONTENT_URI, aisleValues,
        VueConstants.AISLE_ID + "=?", new String[] {aisleID});
    aisleValues.put(VueConstants.COMMENTS, comment);
    aisleValues.put(VueConstants.IMAGE_ID, imageID);
    aisleValues.put(VueConstants.AISLE_ID, aisleID);
    mContext.getContentResolver().insert(VueConstants.COMMENTS_ON_IMAGE_URI, aisleValues);
  }
  
  public void addLikeOrDisLike(boolean isLiked, String imageID, String aisleID) {
    //TODO: We have some confusion on like/dislike and how to maintain user likes
    // and rest of the world likes as we cannot merge these two and sync the db to server.
  }
  
  public void bookMarkOrUnBookmarkAisle(boolean isBookmarked, String aisleID) {
    ContentValues values = new ContentValues();
    values.put(VueConstants.IS_BOOKMARKED, isBookmarked);
    mContext.getContentResolver().update(VueConstants.CONTENT_URI, values,
        VueConstants.AISLE_ID + "=?", new String[] {aisleID});
  }
  
  public void addAisleMetaDataToDB(String tableName, String keyword,
      long time, int count, boolean isNewFlag) {
    Uri uri = null;
    if (tableName.equals(VueConstants.LOOKING_FOR_TABLE) && isNewFlag) {
      uri = VueConstants.LOOKING_FOR_CONTENT_URI;
    } else if (tableName.equals(VueConstants.OCCASION_TABLE)) {
      uri = VueConstants.OCCASION_CONTENT_URI;
    } else if (tableName.equals(VueConstants.CATEGORY_TABLE)) {
      uri = VueConstants.CATEGORY_CONTENT_URI;
    } else {
      return;
    }
    ContentValues values = new ContentValues();
    values.put(VueConstants.KEYWORD, keyword);
    values.put(VueConstants.LAST_USED_TIME, time);
    values.put(VueConstants.NUMBER_OF_TIMES_USED, count);
    if (isNewFlag) {
      mContext.getContentResolver().insert(uri, values);
    } else {
      mContext.getContentResolver().update(uri, values,
          VueConstants.KEYWORD + "=?", new String[] {keyword});
    }
  }
  
}
