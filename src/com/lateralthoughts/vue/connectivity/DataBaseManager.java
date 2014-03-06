package com.lateralthoughts.vue.connectivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.lateralthoughts.vue.AisleContext;
import com.lateralthoughts.vue.AisleImageDetails;
import com.lateralthoughts.vue.AisleWindowContent;
import com.lateralthoughts.vue.ImageRating;
import com.lateralthoughts.vue.VueApplication;
import com.lateralthoughts.vue.VueConstants;
import com.lateralthoughts.vue.domain.AisleBookmark;
import com.lateralthoughts.vue.domain.ImageComment;
import com.lateralthoughts.vue.parser.ImageComments;
import com.lateralthoughts.vue.parser.Parser;
import com.lateralthoughts.vue.user.VueUser;
import com.lateralthoughts.vue.utils.RecentlyViewedAisle;
import com.lateralthoughts.vue.utils.UrlConstants;
import com.lateralthoughts.vue.utils.UsedKeywordsOnUpgrade;
import com.lateralthoughts.vue.utils.Utils;

public class DataBaseManager {
    public static final int TRENDING = 1;
    public static final int MY_AISLES = 2;
    public static final int AISLE_CREATED = 3;
    public static final int BOOKMARK = 4;
    private static final String FORMATE = "%05d";
    private static final String DELETE_ROW = "1";
    private static final int THOUSAND = 1000;
    private static final int MORE_TIMES_USED = 1;
    private static final int EQUAL_TIMES_USED = 0;
    private static final int LESS_TIMES_USED = -1;
    private int mStartPosition = 0;
    public int mEndPosition = 0;
    private int mLocalAislesLimit = 10;
    private Context mContext;
    private int mPoolSize = 1;
    private int mMaxPoolSize = 1;
    private long mKeepAliveTime = 10;
    private ThreadPoolExecutor threadPool;
    
    private final LinkedBlockingQueue<Runnable> mThreadsQueue = new LinkedBlockingQueue<Runnable>();
    private static DataBaseManager sManager;
    private HashMap<String, Integer> mAislesOrderMap;
    private HashMap<String, Integer> mBookmarkedAislesOrderMap = new HashMap<String, Integer>();
    
    private DataBaseManager(Context context) {
        mContext = context;
        threadPool = new ThreadPoolExecutor(mPoolSize, mMaxPoolSize,
                mKeepAliveTime, TimeUnit.SECONDS, mThreadsQueue);
    }
    
    public static DataBaseManager getInstance(Context context) {
        if (sManager == null) {
            sManager = new DataBaseManager(context);
        }
        return sManager;
    }
    
    /**
     * to start thread pool.
     * 
     * @param task
     *            Runnable
     */
    private void runTask(Runnable task) {
        threadPool.execute(task);
    }
    
    public void addTrentingAislesFromServerToDB(final Context context,
            final List<AisleWindowContent> contentList, final int offsetValue,
            final int whichScreen) {
        runTask(new Runnable() {
            
            @Override
            public void run() {
                addAislesToDB(context, contentList, offsetValue, whichScreen,
                        false);
            }
        });
    }
    
    public void addOrUpdateAisles(final Context context,
            final List<AisleWindowContent> contentList, final int offsetValue,
            final int whichScreen) {
        runTask(new Runnable() {
            
            @Override
            public void run() {
                addAislesToDB(context, contentList, offsetValue, whichScreen,
                        false);
            }
        });
    }
    
    public void deleteOutDatedAisles(Context context, String aisleID) {
        context.getContentResolver().delete(VueConstants.CONTENT_URI,
                VueConstants.AISLE_Id + "=?", new String[] { aisleID });
        context.getContentResolver().delete(VueConstants.IMAGES_CONTENT_URI,
                VueConstants.AISLE_Id + "=?", new String[] { aisleID });
        context.getContentResolver().delete(VueConstants.COMMENTS_ON_IMAGE_URI,
                VueConstants.AISLE_Id + "=?", new String[] { aisleID });
    }
    
    /**
     * update the aisle information in local DB, sends this task to thread pool.
     * 
     * @param AisleContext
     *            context.
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
            final List<AisleWindowContent> contentList, final int offsetValue,
            final int whichScreen) {
        
        runTask(new Runnable() {
            
            @Override
            public void run() {
                addAislesToDB(context, contentList, offsetValue, whichScreen,
                        true);
            }
        });
    }
    
    /**
     * add all the aisles pulled from server to sqlite, if the aisle is already
     * there in sqlite then it will delete and insert the new data for that
     * aisle.
     * 
     * @param Context
     *            context.
     * */
    private void addAislesToDB(Context context,
            List<AisleWindowContent> contentList, int offsetValue,
            int whichScreen, boolean isBookmarkedAisle) {
        Log.i("IdsFromSdCArd", "IdsFromSdCArd addAislesToDB method called ");
        if (contentList.size() == 0) {
            return;
        }
        mAislesOrderMap = new HashMap<String, Integer>();
        if (offsetValue == 0 && whichScreen == TRENDING && !isBookmarkedAisle) {
            context.getContentResolver().delete(VueConstants.CONTENT_URI, null,
                    null);
            context.getContentResolver().delete(
                    VueConstants.IMAGES_CONTENT_URI, null, null);
            context.getContentResolver().delete(
                    VueConstants.COMMENTS_ON_IMAGE_URI, null, null);
            context.getContentResolver().delete(
                    VueConstants.ALL_RATED_IMAGES_URI, null, null);
        } else if (isBookmarkedAisle) {
            mBookmarkedAislesOrderMap.clear();
        }
        
        Cursor aisleIdCursor = context.getContentResolver().query(
                VueConstants.CONTENT_URI,
                new String[] { VueConstants.AISLE_Id, VueConstants.ID }, null,
                null, null);
        Cursor imageIdCursor = context.getContentResolver().query(
                VueConstants.IMAGES_CONTENT_URI,
                new String[] { VueConstants.IMAGE_ID }, null, null, null);
        Cursor commntsCursor = context.getContentResolver().query(
                VueConstants.COMMENTS_ON_IMAGE_URI,
                new String[] { VueConstants.ID }, null, null, null);
        Cursor allRatedImages = mContext.getContentResolver()
                .query(VueConstants.ALL_RATED_IMAGES_URI,
                 new String[] {VueConstants.ID}, null, null, null);
        
        ArrayList<String> aisleIds = new ArrayList<String>();
        ArrayList<String> imageIds = new ArrayList<String>();
        ArrayList<Long> commentsImgId = new ArrayList<Long>();
        ArrayList<Long> imgRatedIds = new ArrayList<Long>();
        if (aisleIdCursor.moveToFirst()) {
            do {
                String aisleId = aisleIdCursor.getString(aisleIdCursor
                        .getColumnIndex(VueConstants.AISLE_Id));
                aisleIds.add(aisleId);
                if (whichScreen == MY_AISLES || whichScreen == AISLE_CREATED) {
                    String order = aisleIdCursor.getString(aisleIdCursor
                            .getColumnIndex(VueConstants.ID));
                    mAislesOrderMap.put(aisleId, Integer.parseInt(order));
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
                commentsImgId.add(commentId);
            } while (commntsCursor.moveToNext());
        }
        commntsCursor.close();
        if (allRatedImages.moveToFirst()) {
            do {
                long ratedId = allRatedImages.getLong(allRatedImages
                        .getColumnIndex(VueConstants.ID));
                imgRatedIds.add(ratedId);
                Log.i("IdsFromSdCArd", "IdsFromSdCArd from Db: "+ratedId);
            } while (allRatedImages.moveToNext());
        }
        allRatedImages.close();
        
        int aislesCount = contentList.size();
        for (int i = 0; i < aislesCount; i++) {
            AisleWindowContent content = contentList.get(i);
            AisleContext info = content.getAisleContext();
            ArrayList<AisleImageDetails> imageItemsArray = content
                    .getImageList();
            
            if (whichScreen == TRENDING && mAislesOrderMap.isEmpty()) {
                mAislesOrderMap.put(info.mAisleId, THOUSAND);
            } else if (whichScreen == TRENDING && !mAislesOrderMap.isEmpty()) {
                mAislesOrderMap.put(info.mAisleId,
                        getMaxAisleValue(mAislesOrderMap) + 1);
            } else if (whichScreen == MY_AISLES
                    && !mAislesOrderMap.containsKey(info.mAisleId)) {
                int newOrder = getMaxAisleValue(mAislesOrderMap) + 1;
                mAislesOrderMap.put(info.mAisleId, newOrder);
            } else if (whichScreen == AISLE_CREATED) {
                mAislesOrderMap.put(info.mAisleId,
                        getMinAisleValue(mAislesOrderMap) - 1);
            } else if (isBookmarkedAisle) {
                Cursor cur = mContext.getContentResolver().query(
                        VueConstants.MY_BOOKMARKED_AISLES_URI, null, null,
                        null, null);
                int count = cur.getCount() + 1;
                cur.close();
                mBookmarkedAislesOrderMap.put(info.mAisleId, count);
            }
            
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
            values.put(VueConstants.AISLE_OWNER_IMAGE_URL,
                    info.mAisleOwnerImageURL);
            values.put(VueConstants.DELETE_FLAG, 0);
            
            if (aisleIds.contains(info.mAisleId)) {
                if (!isBookmarkedAisle) {
                    int order = mAislesOrderMap.get(info.mAisleId);
                    values.put(VueConstants.ID, String.format(FORMATE, order));
                    context.getContentResolver().update(
                            VueConstants.CONTENT_URI, values,
                            VueConstants.AISLE_Id + "=?",
                            new String[] { info.mAisleId });
                } else {
                    int order = mBookmarkedAislesOrderMap.get(info.mAisleId);
                    values.put(VueConstants.ID, String.format(FORMATE, order));
                    int rows = context.getContentResolver().update(
                            VueConstants.MY_BOOKMARKED_AISLES_URI, values,
                            VueConstants.AISLE_Id + "=?",
                            new String[] { info.mAisleId });
                    if (rows == 0) {
                        order = mBookmarkedAislesOrderMap.get(info.mAisleId);
                        values.put(VueConstants.ID,
                                String.format(FORMATE, order));
                        values.put(VueConstants.AISLE_Id, info.mAisleId);
                        context.getContentResolver().insert(
                                VueConstants.MY_BOOKMARKED_AISLES_URI, values);
                    }
                }
            } else {
                if (!isBookmarkedAisle) {
                    int order = mAislesOrderMap.get(info.mAisleId);
                    values.put(VueConstants.ID, String.format(FORMATE, order));
                    values.put(VueConstants.AISLE_Id, info.mAisleId);
                    Uri uri = context.getContentResolver().insert(
                            VueConstants.CONTENT_URI, values);
                } else {
                    int order = mBookmarkedAislesOrderMap.get(info.mAisleId);
                    values.put(VueConstants.ID, String.format(FORMATE, order));
                    int rows = context.getContentResolver().update(
                            VueConstants.MY_BOOKMARKED_AISLES_URI, values,
                            VueConstants.AISLE_Id + "=?",
                            new String[] { info.mAisleId });
                    if (rows == 0) {
                        order = mBookmarkedAislesOrderMap.get(info.mAisleId);
                        values.put(VueConstants.ID,
                                String.format(FORMATE, order));
                        values.put(VueConstants.AISLE_Id, info.mAisleId);
                        context.getContentResolver().insert(
                                VueConstants.MY_BOOKMARKED_AISLES_URI, values);
                        
                    }
                }
            }
            /*
             * if (imageItemsArray == null || imageItemsArray.size() == 0) {
             * continue; }
             */
            if (imageItemsArray != null) {
                for (AisleImageDetails imageDetails : imageItemsArray) {
                    Log.i("IdsFromSdCArd", "IdsFromSdCArd from Server image id: "+imageDetails.mId);
                    Cursor imgCountCursor = context.getContentResolver().query(
                            VueConstants.IMAGES_CONTENT_URI,
                            new String[] { "COUNT(*)" }, null, null, null);
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
                    imgValues.put(VueConstants.IMAGE_URL,
                            imageDetails.mImageUrl);
                    imgValues.put(VueConstants.DETAILS_URL,
                            imageDetails.mDetailsUrl);
                    imgValues.put(VueConstants.HEIGHT,
                            imageDetails.mAvailableHeight);
                    imgValues.put(VueConstants.WIDTH,
                            imageDetails.mAvailableWidth);
                    imgValues.put(VueConstants.STORE, imageDetails.mStore);
                    imgValues.put(VueConstants.USER_ID, info.mUserId);
                    imgValues.put(VueConstants.AISLE_Id, info.mAisleId);
                    imgValues.put(VueConstants.LIKES_COUNT,
                            imageDetails.mLikesCount);
                    
                    if (imageIds.contains(imageDetails.mId)) {
                        context.getContentResolver().update(
                                VueConstants.IMAGES_CONTENT_URI, imgValues,
                                VueConstants.IMAGE_ID + "=?",
                                new String[] { imageDetails.mId });
                    } else {
                        imgValues.put(VueConstants.IMAGE_ID, imageDetails.mId);
                        imgValues.put(VueConstants.ID,
                                String.format(FORMATE, ++imgCount));
                        try {
                            context.getContentResolver().insert(
                                    VueConstants.IMAGES_CONTENT_URI, imgValues);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if(imageDetails.mRatingsList != null) {
                        ContentValues imgRatedValues;
                        for(ImageRating imgRating : imageDetails.mRatingsList) {
                            imgRatedValues = new ContentValues();
                            Log.i("IdsFromSdCArd", "IdsFromSdCArd from Server: "+imgRating.mId);
                            imgRatedValues.put(VueConstants.IMAGE_ID, imgRating.mImageId.longValue());
                            imgRatedValues.put(VueConstants.AISLE_ID, imgRating.mAisleId.longValue());
                            imgRatedValues.put(VueConstants.LIKE_OR_DISLIKE, imgRating.mLiked ? 1 : 0);
                            imgRatedValues.put(VueConstants.AISLE_IMAGE_RATING_OWNER_FIRST_NAME,
                                    imgRating.mImageRatingOwnerFirstName);
                            imgRatedValues.put(VueConstants.AISLE_IMAGE_RATING_OWNER_LAST_NAME,
                                    imgRating.mImageRatingOwnerLastName);
                            imgRatedValues.put(VueConstants.AISLE_IMAGE_RATING_LASTMODIFIED_TIME,
                                    imgRating.mLastModifiedTimestamp.longValue());
                            if(imgRatedIds.contains(imgRating.mId)) {
                                mContext.getContentResolver().update(
                                        VueConstants.ALL_RATED_IMAGES_URI,
                                        imgRatedValues, VueConstants.ID + "=?",
                                        new String[] {String.valueOf(imgRating.mId)});    
                            } else {
                               imgRatedValues.put(VueConstants.ID, imgRating.mId.longValue());
                               try{ 
                               mContext.getContentResolver().insert(
                                    VueConstants.ALL_RATED_IMAGES_URI, imgRatedValues);
                               
                               
                               } catch(Exception e){
                                   Log.i("IdsFromSdCArd", "IdsFromSdCArd &&&&&&&&: "+imgRating.mId.longValue());
                               }
                            }
                        }
                    }
                    
                    // image comments content values.
                    ContentValues commentValues = new ContentValues();
                    commentValues.put(VueConstants.IMAGE_ID, imageDetails.mId);
                    commentValues.put(VueConstants.DIRTY_FLAG, false);
                    commentValues.put(VueConstants.DELETE_FLAG, false);
                    commentValues.put(VueConstants.AISLE_Id, info.mAisleId);
                    for (ImageComments commnts : imageDetails.mCommentsList) {
                        commentValues.put(VueConstants.COMMENTS,
                                commnts.mComment);
                        commentValues.put(VueConstants.COMMENTER_URL,
                                commnts.mCommenterUrl);
                        commentValues.put(
                                VueConstants.AISLE_IMAGE_COMMENTER_FIRST_NAME,
                                commnts.mCommenterFirstName);
                        commentValues.put(
                                VueConstants.AISLE_IMAGE_COMMENTER_LAST_NAME,
                                commnts.mCommenterLastName);
                        commentValues
                                .put(VueConstants.LAST_MODIFIED_TIME,
                                        (commnts.mLastModifiedTimestamp != null) ? commnts.mLastModifiedTimestamp
                                                : System.currentTimeMillis());
                        if (commentsImgId.contains(commnts.mId.longValue())) {
                            Uri uri = Uri
                                    .parse(VueConstants.COMMENTS_ON_IMAGE_URI
                                            + "/" + commnts.mId);
                            context.getContentResolver().update(uri,
                                    commentValues, null, null);
                        } else {
                            commentValues.put(VueConstants.ID, commnts.mId);
                            context.getContentResolver().insert(
                                    VueConstants.COMMENTS_ON_IMAGE_URI,
                                    commentValues);
                        }
                    }
                }
            }
        }
        copydbToSdcard();
    }
    
    /**
     * update the aisle information in local DB.
     * 
     * @param AisleContext
     *            context.
     * */
    private void aisleUpdate(AisleContext context) {
        boolean isAisle = false;
        if (mAislesOrderMap != null && mAislesOrderMap.isEmpty()) {
            Cursor aisleIdCursor = VueApplication
                    .getInstance()
                    .getContentResolver()
                    .query(VueConstants.CONTENT_URI,
                            new String[] { VueConstants.AISLE_Id,
                                    VueConstants.ID },
                            VueConstants.AISLE_Id + "=?",
                            new String[] { context.mAisleId }, null);
            if (aisleIdCursor.moveToFirst()) {
                do {
                    String aisleId = aisleIdCursor.getString(aisleIdCursor
                            .getColumnIndex(VueConstants.AISLE_Id));
                    if (aisleId.equals(context.mAisleId)) {
                        mAislesOrderMap.put(context.mAisleId, Integer
                                .parseInt(aisleIdCursor.getString(aisleIdCursor
                                        .getColumnIndex(VueConstants.ID))));
                        isAisle = true;
                        break;
                    }
                } while (aisleIdCursor.moveToNext());
                if (!isAisle) {
                    mAislesOrderMap.put(context.mAisleId, THOUSAND
                            + (aisleIdCursor.getCount() + 1));
                }
            }
            aisleIdCursor.close();
        } else {
            if (mAislesOrderMap == null) {
                mAislesOrderMap = new HashMap<String, Integer>();
            }
            mAislesOrderMap.put(context.mAisleId,
                    getMaxAisleValue(mAislesOrderMap) + 1);
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
        int order = mAislesOrderMap.get(context.mAisleId);
        values.put(VueConstants.ID, String.format(FORMATE, order));
        if (isAisle) {
            VueApplication
                    .getInstance()
                    .getContentResolver()
                    .update(VueConstants.CONTENT_URI, values,
                            VueConstants.AISLE_Id + "=?",
                            new String[] { context.mAisleId });
        } else {
            VueApplication.getInstance().getContentResolver()
                    .insert(VueConstants.CONTENT_URI, values);
        }
    }
    
    /**
     * This method will return the aisles with aislesIds we give in parameter,
     * if the parameter is null then it will return aisles in installment of 10.
     * 
     * @param String
     *            [] aislesIds
     * @return ArrayList<AisleWindowContent>
     * */
    @SuppressWarnings("rawtypes")
    public ArrayList<AisleWindowContent> getAislesFromDB(String[] aislesIds,
            boolean isBookmarked) {
        Cursor cursor = mContext.getContentResolver().query(
                VueConstants.CONTENT_URI, new String[] { VueConstants.ID },
                null, null, VueConstants.ID + " ASC");
        if (!isBookmarked) {
            if (mStartPosition == 0) {
                if (cursor.moveToFirst()) {
                    mStartPosition = Integer.parseInt(cursor.getString(cursor
                            .getColumnIndex(VueConstants.ID)));
                } else {
                    mStartPosition = 1000;
                }
            }
            
            if (mEndPosition == 0) {
                if (cursor.moveToFirst()) {
                    if (cursor
                            .getString(cursor.getColumnIndex(VueConstants.ID)) != null) {
                        mEndPosition = Integer.parseInt(cursor.getString(cursor
                                .getColumnIndex(VueConstants.ID)));
                    } else {
                        mStartPosition = 1000;
                    }
                } else {
                    mEndPosition = 1000;
                }
            }
        }
        cursor.close();
        mEndPosition = mEndPosition + mLocalAislesLimit;
        AisleContext userInfo;
        AisleImageDetails imageItemDetails;
        AisleWindowContent aisleItem = null;
        String selection;
        String[] args = null;
        LinkedHashMap<String, AisleContext> map = new LinkedHashMap<String, AisleContext>();
        ArrayList<AisleWindowContent> aisleContentArray = new ArrayList<AisleWindowContent>();
        ArrayList<AisleImageDetails> imageItemsArray = new ArrayList<AisleImageDetails>();
        
        if (aislesIds == null) {
            selection = VueConstants.ID + " >=? AND " + VueConstants.ID
                    + " <=? ";
            String[] allAislesArgs = { String.format(FORMATE, mStartPosition),
                    String.format(FORMATE, mEndPosition) };
            args = allAislesArgs;
        } else {
            String questionSymbols = "?";
            for (int i = 1; i < aislesIds.length; i++) {
                questionSymbols = questionSymbols + ",?";
            }
            selection = VueConstants.AISLE_Id + " IN (" + questionSymbols
                    + ") ";
            args = aislesIds;
        }
        // isBookmarked
        Cursor aislesCursor = null;
        if (isBookmarked) {
            aislesCursor = mContext.getContentResolver().query(
                    VueConstants.MY_BOOKMARKED_AISLES_URI, null, null, null,
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
                userInfo.mJoinTime = Long.parseLong(aislesCursor
                        .getString(aislesCursor
                                .getColumnIndex(VueConstants.JOIN_TIME)));
                userInfo.mLookingForItem = aislesCursor.getString(aislesCursor
                        .getColumnIndex(VueConstants.LOOKING_FOR));
                userInfo.mOccasion = aislesCursor.getString(aislesCursor
                        .getColumnIndex(VueConstants.OCCASION));
                userInfo.mBookmarkCount = Integer.parseInt(aislesCursor
                        .getString(aislesCursor
                                .getColumnIndex(VueConstants.BOOKMARK_COUNT)));
                userInfo.mCategory = aislesCursor.getString(aislesCursor
                        .getColumnIndex(VueConstants.CATEGORY));
                userInfo.mDescription = aislesCursor.getString(aislesCursor
                        .getColumnIndex(VueConstants.AISLE_DESCRIPTION));
                userInfo.mAisleOwnerImageURL = aislesCursor
                        .getString(aislesCursor
                                .getColumnIndex(VueConstants.AISLE_OWNER_IMAGE_URL));
                map.put(userInfo.mAisleId, userInfo);
            } while (aislesCursor.moveToNext());
        }
        Cursor aisleImagesCursor = mContext.getContentResolver().query(
                VueConstants.IMAGES_CONTENT_URI, null, null, null,
                VueConstants.ID + " ASC");
        Cursor imgCommentCursor = mContext.getContentResolver().query(
                VueConstants.COMMENTS_ON_IMAGE_URI, null, null, null,
                VueConstants.LAST_MODIFIED_TIME + " DESC");
        Cursor imageRatedCursor = mContext.getContentResolver().query(
                VueConstants.ALL_RATED_IMAGES_URI,
                null, null, null, null);
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            if (aisleImagesCursor.moveToFirst()) {
                do {
                    if (aisleImagesCursor.getString(
                            aisleImagesCursor
                                    .getColumnIndex(VueConstants.AISLE_Id))
                            .equals((String) pairs.getKey())) {
                        imageItemDetails = new AisleImageDetails();
                        imageItemDetails.mTitle = aisleImagesCursor
                                .getString(aisleImagesCursor
                                        .getColumnIndex(VueConstants.TITLE));
                        imageItemDetails.mImageUrl = aisleImagesCursor
                                .getString(aisleImagesCursor
                                        .getColumnIndex(VueConstants.IMAGE_URL));
                        imageItemDetails.mDetailsUrl = aisleImagesCursor
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
                        imageItemDetails.mLikesCount = aisleImagesCursor
                                .getInt(aisleImagesCursor
                                        .getColumnIndex(VueConstants.LIKES_COUNT));
                        imageItemDetails.mOwnerUserId = aisleImagesCursor
                                .getString(aisleImagesCursor
                                        .getColumnIndex(VueConstants.USER_ID));
                        
                        if(imageRatedCursor.moveToFirst()) {
                            ImageRating imgRating;
                            do {
                                if (imageRatedCursor.getLong(imageRatedCursor
                                        .getColumnIndex(VueConstants.IMAGE_ID)) == Long
                                        .parseLong(imageItemDetails.mId)) {
                                    imgRating = new ImageRating();
                                    imgRating.setId(imageRatedCursor.getLong(imageRatedCursor.getColumnIndex(VueConstants.ID)));
                                    imgRating.setImageId(imageRatedCursor.getLong(imageRatedCursor.getColumnIndex(VueConstants.IMAGE_ID)));
                                    imgRating.setAisleId(imageRatedCursor.getLong(imageRatedCursor.getColumnIndex(VueConstants.AISLE_ID)));
                                    imgRating.setLiked((imageRatedCursor.getInt(imageRatedCursor.getColumnIndex(
                                            VueConstants.LIKE_OR_DISLIKE)) == 1) ? true : false);
                                    imgRating.setRatingOwnerFirstName(imageRatedCursor.getString(imageRatedCursor.getColumnIndex(
                                            VueConstants.AISLE_IMAGE_RATING_OWNER_FIRST_NAME)));
                                    imgRating.setRatingOwnerLastName(imageRatedCursor.getString(imageRatedCursor.getColumnIndex(
                                            VueConstants.AISLE_IMAGE_RATING_OWNER_LAST_NAME)));
                                    imgRating.setLastModifiedTimestamp(imageRatedCursor.getLong(imageRatedCursor.getColumnIndex(
                                            VueConstants.AISLE_IMAGE_RATING_LASTMODIFIED_TIME)));
                                    imageItemDetails.mRatingsList.add(imgRating);
                                }
                            } while(imageRatedCursor.moveToNext());
                        }
                        
                        if (imgCommentCursor.moveToFirst()) {
                            ImageComments comments;
                            do {
                                if (imgCommentCursor.getLong(imgCommentCursor
                                        .getColumnIndex(VueConstants.IMAGE_ID)) == Long
                                        .parseLong(imageItemDetails.mId)) {
                                    comments = new ImageComments();
                                    comments.mId = imgCommentCursor
                                            .getLong(imgCommentCursor
                                                    .getColumnIndex(VueConstants.ID));
                                    comments.mImageId = imgCommentCursor
                                            .getLong(imgCommentCursor
                                                    .getColumnIndex(VueConstants.IMAGE_ID));
                                    comments.mComment = imgCommentCursor
                                            .getString(imgCommentCursor
                                                    .getColumnIndex(VueConstants.COMMENTS));
                                    comments.mLastModifiedTimestamp = Long
                                            .parseLong(imgCommentCursor.getString(imgCommentCursor
                                                    .getColumnIndex(VueConstants.LAST_MODIFIED_TIME)));
                                    comments.mCommenterUrl = imgCommentCursor
                                            .getString(imgCommentCursor
                                                    .getColumnIndex(VueConstants.COMMENTER_URL));
                                    comments.mCommenterFirstName = imgCommentCursor
                                            .getString(imgCommentCursor
                                                    .getColumnIndex(VueConstants.AISLE_IMAGE_COMMENTER_FIRST_NAME));
                                    comments.mCommenterLastName = imgCommentCursor
                                            .getString(imgCommentCursor
                                                    .getColumnIndex(VueConstants.AISLE_IMAGE_COMMENTER_LAST_NAME));
                                    imageItemDetails.mCommentsList
                                            .add(comments);
                                }
                                
                            } while (imgCommentCursor.moveToNext());
                            if (imageItemDetails.mCommentsList != null)
                                Collections
                                        .reverse(imageItemDetails.mCommentsList);
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
        imageRatedCursor.close();
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
    
    public void addLikeOrDisLike(final int likeCount, final boolean dirtyFlag,
            final ImageRating imageRating, final boolean isUserRating,
            final boolean isDirty) {
        runTask(new Runnable() {
            
            @Override
            public void run() {
                addLikeOrDisLikeToDb(likeCount, dirtyFlag, imageRating,
                        isUserRating, isDirty);
            }
        });
    }
    
    public void bookMarkOrUnBookmarkAisle(final boolean isBookmarked,
            final int bookmarkCount, final Long bookmarkID,
            final String aisleID, final boolean isDirty) {
        runTask(new Runnable() {
            
            @Override
            public void run() {
                bookMarkOrUnBookmarkAisleToDb(isBookmarked, bookmarkCount,
                        bookmarkID, aisleID, isDirty);
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
            final ArrayList<ImageRating> retrievedImageRating,
            final boolean isUserRating, final boolean isDirty) {
        runTask(new Runnable() {
            
            @Override
            public void run() {
                insertRatedImagesToDB(retrievedImageRating, isUserRating,
                        isDirty);
            }
        });
    }
    
    public void updateBookmarkAisles(final Long bookmarkId,
            final String bookmarkedAisleId, final boolean isBookarked) {
        runTask(new Runnable() {
            
            @Override
            public void run() {
                updateBookmarkAislesToBDb(bookmarkId, bookmarkedAisleId,
                        isBookarked);
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
     * @param String
     *            comment
     * @param String
     *            imageID
     * @param String
     *            aisleID
     * */
    private void addCommentsToDb(ImageComment createdImageComment,
            boolean isCommentDirty) {
        ContentValues commentValues = new ContentValues();
        commentValues.put(VueConstants.ID, createdImageComment.getId()
                .longValue());
        commentValues.put(VueConstants.IMAGE_ID, createdImageComment
                .getOwnerImageId().longValue());
        commentValues
                .put(VueConstants.LAST_MODIFIED_TIME,
                        (createdImageComment.getLastModifiedTimestamp() != null) ? createdImageComment
                                .getLastModifiedTimestamp().longValue()
                                : System.currentTimeMillis());
        commentValues.put(VueConstants.COMMENTS,
                createdImageComment.getComment());
        commentValues.put(VueConstants.COMMENTER_URL,
                createdImageComment.getImageCommentOwnerImageURL());
        commentValues.put(VueConstants.DIRTY_FLAG, isCommentDirty);
        commentValues.put(VueConstants.DELETE_FLAG, false);
        mContext.getContentResolver().insert(
                VueConstants.COMMENTS_ON_IMAGE_URI, commentValues);
    }
    
    private void addLikeOrDisLikeToDb(int likeCount, boolean dirtyFlag,
            ImageRating imageRating, boolean isUserRating, final boolean isDirty) {
        ContentValues aisleValues = new ContentValues();
        aisleValues.put(VueConstants.LIKE_OR_DISLIKE, imageRating.mLiked ? 1
                : 0);
        aisleValues.put(VueConstants.LIKES_COUNT, likeCount);
        aisleValues.put(VueConstants.DIRTY_FLAG, (dirtyFlag == true) ? 1 : 0);
        mContext.getContentResolver().update(
                VueConstants.IMAGES_CONTENT_URI,
                aisleValues,
                VueConstants.AISLE_Id + "=? AND " + VueConstants.IMAGE_ID
                        + "=?",
                new String[] { String.valueOf(imageRating.getAisleId()),
                        String.valueOf(imageRating.getImageId()) });
        String imgId = String.valueOf(imageRating.getImageId());
        Log.e("NetworkStateChangeReciver",
                "VueConstants.IS_IMAGE_DIRTY succes Responce update success for imageID: "
                        + imgId);
        Log.e("NetworkStateChangeReciver",
                "VueConstants.IS_IMAGE_DIRTY succes Responce update success for image");
        ArrayList<ImageRating> imageRateList = new ArrayList<ImageRating>();
        imageRateList.add(imageRating);
        insertRatedImagesToDB(imageRateList, isUserRating, isDirty);
    }
    
    /**
     * if user bookmarks of unbookmarks any aisle then this method will mark
     * that aisle as the case.
     * 
     * @param boolean isBookmarked
     * @param String
     *            aisleID
     * */
    public void bookMarkOrUnBookmarkAisleToDb(boolean isBookmarked,
            int bookmarkCount, Long bookmarkId, String aisleID, boolean isDirty) {
        ContentValues values = new ContentValues();
        values.put(VueConstants.IS_BOOKMARKED, isBookmarked);
        values.put(VueConstants.BOOKMARK_COUNT, bookmarkCount);
        values.put(VueConstants.DIRTY_FLAG, isDirty);
        mContext.getContentResolver().update(VueConstants.CONTENT_URI, values,
                VueConstants.AISLE_Id + "=?", new String[] { aisleID });
        updateBookmarkAislesToBDb(bookmarkId, aisleID, isBookmarked);
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void updateBookmarkAislesToBDb(Long bookmarkId,
            String bookmarkedAisleId, boolean isBookmarked) {
        boolean isMatched = false;
        ContentValues values = new ContentValues();
        values.put(VueConstants.IS_LIKED_OR_BOOKMARKED, isBookmarked);
        values.put(VueConstants.AISLE_ID, bookmarkedAisleId);
        Cursor cursor = mContext.getContentResolver().query(
                VueConstants.BOOKMARKER_AISLES_URI, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                String aisleId = cursor.getString(cursor
                        .getColumnIndex(VueConstants.AISLE_ID));
                if (bookmarkedAisleId.equals(aisleId)) {
                    mContext.getContentResolver().update(
                            VueConstants.BOOKMARKER_AISLES_URI, values,
                            VueConstants.ID + "=?",
                            new String[] { Long.toString(bookmarkId) });
                    isMatched = true;
                    break;
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        if (!isMatched) {
            values.put(VueConstants.ID, bookmarkId);
            mContext.getContentResolver().insert(
                    VueConstants.BOOKMARKER_AISLES_URI, values);
        }
        if (isBookmarked) {
            String url = UrlConstants.GET_AISLE_RESTURL + bookmarkedAisleId;
            
            Response.Listener listener = new Response.Listener<JSONObject>() {
                
                @Override
                public void onResponse(final JSONObject jsonObject) {
                    if (jsonObject != null) {
                        Thread t = new Thread(new Runnable() {
                            
                            @Override
                            public void run() {
                                try {
                                    
                                    Parser parser = new Parser();
                                    AisleWindowContent aisleItem = parser
                                            .getBookmarkedAisle(jsonObject);
                                    ArrayList<AisleImageDetails> imageDetails = aisleItem
                                            .getImageList();
                                    if (imageDetails.size() > 0) {
                                        ArrayList<AisleWindowContent> aisleContentArray = new ArrayList<AisleWindowContent>();
                                        aisleContentArray.add(aisleItem);
                                        addBookmarkedAisles(mContext,
                                                aisleContentArray, 0, BOOKMARK);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                
                            }
                        });
                        t.start();
                    }
                }
            };
            Response.ErrorListener errorListener = new ErrorListener() {
                
                @Override
                public void onErrorResponse(VolleyError arg0) {
                }
            };
            
            JsonObjectRequest request = new JsonObjectRequest(url, null,
                    listener, errorListener);
            request.setRetryPolicy(new DefaultRetryPolicy(
                    DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, Utils.MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VueApplication.getInstance().getRequestQueue().add(request);
        } else {
            mContext.getContentResolver().delete(
                    VueConstants.MY_BOOKMARKED_AISLES_URI,
                    VueConstants.AISLE_Id + "=?",
                    new String[] { bookmarkedAisleId });
            
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
        values.put(VueConstants.LAST_USED_TIME, System.currentTimeMillis());
        values.put(VueConstants.NUMBER_OF_TIMES_USED, aisleData.count);
        if (aisleData.isNew) {
            mContext.getContentResolver().insert(uri, values);
        } else {
            mContext.getContentResolver().update(uri, values,
                    VueConstants.KEYWORD + "=?",
                    new String[] { aisleData.keyword });
        }
    }
    
    public void addAisleMetaDataToDBOnUpgrade(String tableName,
            ArrayList<UsedKeywordsOnUpgrade> usedKeywords, SQLiteDatabase db) {
        if (usedKeywords != null && usedKeywords.size() > 0) {
            for (UsedKeywordsOnUpgrade usedKeywordsOnUpgrade : usedKeywords) {
                ContentValues values = new ContentValues();
                values.put(VueConstants.KEYWORD, usedKeywordsOnUpgrade.keyWord);
                values.put(VueConstants.LAST_USED_TIME,
                        usedKeywordsOnUpgrade.lastUsedTime);
                values.put(VueConstants.NUMBER_OF_TIMES_USED,
                        usedKeywordsOnUpgrade.numberOfTimesUsed);
                db.insert(tableName, null, values);
            }
        }
    }
    
    public ArrayList<String> getAisleKeywords(String tableName) {
        ArrayList<UsedKeywords> aisleKeywordsList = null;
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
                VueConstants.LAST_USED_TIME + ">?",
                new String[] { twoWeeksBeforeTime },
                VueConstants.NUMBER_OF_TIMES_USED + " DESC");
        if (c.moveToFirst()) {
            aisleKeywordsList = new ArrayList<UsedKeywords>();
            UsedKeywords keywords;
            do {
                keywords = new UsedKeywords(
                        c.getString(c.getColumnIndex(VueConstants.KEYWORD)),
                        c.getString(c
                                .getColumnIndex(VueConstants.LAST_USED_TIME)),
                        c.getString(c
                                .getColumnIndex(VueConstants.NUMBER_OF_TIMES_USED)));
                aisleKeywordsList.add(keywords);
            } while (c.moveToNext());
        }
        c.close();
        if (aisleKeywordsList == null) {
            return null;
        }
        for (int i = 0; i < aisleKeywordsList.size(); i++) {
            UsedKeywords current = aisleKeywordsList.get(i);
            for (int j = 0; j < i; ++j) {
                UsedKeywords previous = aisleKeywordsList.get(j);
                if (current.compareNumberOfTimesUsed(previous) == EQUAL_TIMES_USED) {
                    if (current.compareTime(previous)) {
                        Collections.swap(aisleKeywordsList, i, j);
                    }
                } else if (current.compareNumberOfTimesUsed(previous) == LESS_TIMES_USED) {
                    Collections.swap(aisleKeywordsList, i, j);
                }
            }
        }
        ArrayList<String> finalList = new ArrayList<String>();
        for (UsedKeywords key : aisleKeywordsList) {
            finalList.add(key.keyWord);
        }
        return finalList;
    }
    
    public ArrayList<UsedKeywordsOnUpgrade> getAisleKeywordsOnUpgrade(
            String tableName, SQLiteDatabase db) {
        ArrayList<UsedKeywordsOnUpgrade> aisleKeywordsList = null;
        Cursor cursor = db.query(tableName, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            aisleKeywordsList = new ArrayList<UsedKeywordsOnUpgrade>();
            UsedKeywordsOnUpgrade keywords;
            do {
                keywords = new UsedKeywordsOnUpgrade(
                        cursor.getString(cursor
                                .getColumnIndex(VueConstants.KEYWORD)),
                        cursor.getString(cursor
                                .getColumnIndex(VueConstants.LAST_USED_TIME)),
                        cursor.getString(cursor
                                .getColumnIndex(VueConstants.NUMBER_OF_TIMES_USED)));
                aisleKeywordsList.add(keywords);
            } while (cursor.moveToNext());
        }
        cursor.close();
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
                VueConstants.KEYWORD + "=?", new String[] { keyWord }, null);
        if (c.moveToFirst()) {
            aisleDataObj = new AisleData();
            aisleDataObj.keyword = c.getString(c
                    .getColumnIndex(VueConstants.KEYWORD));
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
        context.getContentResolver().update(VueConstants.CONTENT_URI, values,
                null, null);
        context.getContentResolver().update(VueConstants.IMAGES_CONTENT_URI,
                values, null, null);
        return true;
    }
    
    @SuppressWarnings("rawtypes")
    private ArrayList<AisleWindowContent> getAisles(Cursor aislesCursor) {
        AisleContext userInfo;
        AisleImageDetails imageItemDetails;
        AisleWindowContent aisleItem = null;
        
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
                userInfo.mJoinTime = Long.parseLong(aislesCursor
                        .getString(aislesCursor
                                .getColumnIndex(VueConstants.JOIN_TIME)));
                userInfo.mLookingForItem = aislesCursor.getString(aislesCursor
                        .getColumnIndex(VueConstants.LOOKING_FOR));
                userInfo.mOccasion = aislesCursor.getString(aislesCursor
                        .getColumnIndex(VueConstants.OCCASION));
                userInfo.mBookmarkCount = Integer.parseInt(aislesCursor
                        .getString(aislesCursor
                                .getColumnIndex(VueConstants.BOOKMARK_COUNT)));
                userInfo.mAisleOwnerImageURL = aislesCursor
                        .getString(aislesCursor
                                .getColumnIndex(VueConstants.AISLE_OWNER_IMAGE_URL));
                map.put(userInfo.mAisleId, userInfo);
            } while (aislesCursor.moveToNext());
        }
        Cursor aisleImagesCursor = mContext.getContentResolver().query(
                VueConstants.IMAGES_CONTENT_URI, null, null, null,
                VueConstants.ID + " ASC");
        
        Cursor imgCommentCursor = mContext.getContentResolver().query(
                VueConstants.COMMENTS_ON_IMAGE_URI, null, null, null,
                VueConstants.LAST_MODIFIED_TIME + " DESC");
        Cursor imageRatedCursor = mContext.getContentResolver().query(
                VueConstants.ALL_RATED_IMAGES_URI,
                null, null, null, null);
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            
            if (aisleImagesCursor.moveToFirst()) {
                do {
                    if (aisleImagesCursor.getString(
                            aisleImagesCursor
                                    .getColumnIndex(VueConstants.AISLE_Id))
                            .equals((String) pairs.getKey())) {
                        imageItemDetails = new AisleImageDetails();
                        imageItemDetails.mTitle = aisleImagesCursor
                                .getString(aisleImagesCursor
                                        .getColumnIndex(VueConstants.TITLE));
                        imageItemDetails.mImageUrl = aisleImagesCursor
                                .getString(aisleImagesCursor
                                        .getColumnIndex(VueConstants.IMAGE_URL));
                        imageItemDetails.mDetailsUrl = aisleImagesCursor
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
                        imageItemDetails.mLikesCount = aisleImagesCursor
                                .getInt(aisleImagesCursor
                                        .getColumnIndex(VueConstants.LIKES_COUNT));
                        imageItemDetails.mOwnerUserId = aisleImagesCursor
                                .getString(aisleImagesCursor
                                        .getColumnIndex(VueConstants.USER_ID));
                        
                        
                        if(imageRatedCursor.moveToFirst()) {
                            ImageRating imgRating;
                            do {
                                if (imageRatedCursor.getLong(imageRatedCursor
                                        .getColumnIndex(VueConstants.IMAGE_ID)) == Long
                                        .parseLong(imageItemDetails.mId)) {
                                    imgRating = new ImageRating();
                                    imgRating.setId(imageRatedCursor.getLong(imageRatedCursor.getColumnIndex(VueConstants.ID)));
                                    imgRating.setImageId(imageRatedCursor.getLong(imageRatedCursor.getColumnIndex(VueConstants.IMAGE_ID)));
                                    imgRating.setAisleId(imageRatedCursor.getLong(imageRatedCursor.getColumnIndex(VueConstants.AISLE_ID)));
                                    imgRating.setLiked((imageRatedCursor.getInt(imageRatedCursor.getColumnIndex(
                                            VueConstants.LIKE_OR_DISLIKE)) == 1) ? true : false);
                                    imgRating.setRatingOwnerFirstName(imageRatedCursor.getString(imageRatedCursor.getColumnIndex(
                                            VueConstants.AISLE_IMAGE_RATING_OWNER_FIRST_NAME)));
                                    imgRating.setRatingOwnerLastName(imageRatedCursor.getString(imageRatedCursor.getColumnIndex(
                                            VueConstants.AISLE_IMAGE_RATING_OWNER_LAST_NAME)));
                                    imgRating.setLastModifiedTimestamp(imageRatedCursor.getLong(imageRatedCursor.getColumnIndex(
                                            VueConstants.AISLE_IMAGE_RATING_LASTMODIFIED_TIME)));
                                    imageItemDetails.mRatingsList.add(imgRating);
                                }
                            } while(imageRatedCursor.moveToNext());
                        }
                        
                        if (imgCommentCursor.moveToFirst()) {
                            ImageComments comments;
                            do {
                                if (imgCommentCursor.getLong(imgCommentCursor
                                        .getColumnIndex(VueConstants.IMAGE_ID)) == Long
                                        .parseLong(imageItemDetails.mId)) {
                                    comments = new ImageComments();
                                    comments.mId = imgCommentCursor
                                            .getLong(imgCommentCursor
                                                    .getColumnIndex(VueConstants.ID));
                                    comments.mImageId = imgCommentCursor
                                            .getLong(imgCommentCursor
                                                    .getColumnIndex(VueConstants.IMAGE_ID));
                                    comments.mComment = imgCommentCursor
                                            .getString(imgCommentCursor
                                                    .getColumnIndex(VueConstants.COMMENTS));
                                    comments.mCommenterUrl = imgCommentCursor
                                            .getString(imgCommentCursor
                                                    .getColumnIndex(VueConstants.COMMENTER_URL));
                                    comments.mLastModifiedTimestamp = Long
                                            .parseLong(imgCommentCursor.getString(imgCommentCursor
                                                    .getColumnIndex(VueConstants.LAST_MODIFIED_TIME)));
                                    comments.mCommenterFirstName = imgCommentCursor
                                            .getString(imgCommentCursor
                                                    .getColumnIndex(VueConstants.AISLE_IMAGE_COMMENTER_FIRST_NAME));
                                    comments.mCommenterLastName = imgCommentCursor
                                            .getString(imgCommentCursor
                                                    .getColumnIndex(VueConstants.AISLE_IMAGE_COMMENTER_LAST_NAME));
                                    imageItemDetails.mCommentsList
                                            .add(comments);
                                }
                                
                            } while (imgCommentCursor.moveToNext());
                            if (imageItemDetails.mCommentsList != null)
                                Collections
                                        .reverse(imageItemDetails.mCommentsList);
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
        imageRatedCursor.close();
        
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
    
    public ArrayList<AisleWindowContent> getAisleByAisleIdFromBookmarks(
            String aisleId) {
        return getAisles(getAislesCursor(aisleId, VueConstants.AISLE_Id, true));
    }
    
    public ArrayList<AisleWindowContent> getDirtyAisles(String dirtyFlag) {
        return getAisles(getAislesCursor(dirtyFlag, VueConstants.DIRTY_FLAG,
                false));
    }
    
    public ArrayList<ImageRating> getDirtyImages(String dirtyFlag) {
        ArrayList<ImageRating> images = new ArrayList<ImageRating>();
        Cursor cursor = mContext.getContentResolver().query(
                VueConstants.RATED_IMAGES_URI, null,
                VueConstants.DIRTY_FLAG + "=?", new String[] { dirtyFlag },
                null);
        if (cursor.moveToFirst()) {
            do {
                if (cursor.getInt(cursor
                        .getColumnIndex(VueConstants.DIRTY_FLAG)) == 1) {
                    ImageRating imgRating = new ImageRating();
                    imgRating.mImageId = cursor.getLong(cursor
                            .getColumnIndex(VueConstants.IMAGE_ID));
                    imgRating.mId = cursor.getLong(cursor
                            .getColumnIndex(VueConstants.ID));
                    imgRating.mAisleId = cursor.getLong(cursor
                            .getColumnIndex(VueConstants.AISLE_ID));
                    imgRating.mLiked = (cursor.getInt(cursor
                            .getColumnIndex(VueConstants.LIKE_OR_DISLIKE)) == 1) ? true
                            : false;
                    images.add(imgRating);
                }
            } while (cursor.moveToNext());
        }
        return images;
    }
    
    private Cursor getAislesCursor(String searchString, String searchBy,
            boolean isBookmark) {
        Cursor aislesCursor = null;
        if (isBookmark) {
            aislesCursor = mContext.getContentResolver().query(
                    VueConstants.MY_BOOKMARKED_AISLES_URI, null,
                    searchBy + "=?", new String[] { searchString },
                    VueConstants.ID + " ASC");
        } else {
            aislesCursor = mContext.getContentResolver().query(
                    VueConstants.CONTENT_URI, null, searchBy + "=?",
                    new String[] { searchString }, VueConstants.ID + " ASC");
            if (aislesCursor.getCount() == 0) {
                aislesCursor = mContext.getContentResolver().query(
                        VueConstants.MY_BOOKMARKED_AISLES_URI, null,
                        searchBy + "=?", new String[] { searchString },
                        VueConstants.ID + " ASC");
            }
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
                VueConstants.DIRTY_FLAG + "=?", new String[] { dirtyFlag },
                null);
        if (cursor.moveToFirst()) {
            do {
                ImageComment comment = new ImageComment();
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
    
    @SuppressWarnings("unused")
    private void changeDeleteFlag() {
        ContentValues values = new ContentValues();
        values.put(VueConstants.DELETE_FLAG, 1);
        mContext.getContentResolver().update(VueConstants.CONTENT_URI, values,
                null, null);
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
        cursor.close();
        return aisleIds;
    }
    
    public ArrayList<RecentlyViewedAisle> getRecentlyViewedAislesOnUpgrade(
            SQLiteDatabase db) {
        ArrayList<RecentlyViewedAisle> recentlyViewedAisles = null;
        Cursor cursor = db.query(
                DbHelper.DATABASE_TABLE_RECENTLY_VIEWED_AISLES, null, null,
                null, null, null, null);
        if (cursor.moveToFirst()) {
            recentlyViewedAisles = new ArrayList<RecentlyViewedAisle>();
            do {
                RecentlyViewedAisle recentlyViewedAisle = new RecentlyViewedAisle(
                        cursor.getString(cursor
                                .getColumnIndex(VueConstants.RECENTLY_VIEWED_AISLE_ID)),
                        cursor.getString(cursor
                                .getColumnIndex(VueConstants.VIEW_TIME)));
                recentlyViewedAisles.add(recentlyViewedAisle);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return recentlyViewedAisles;
    }
    
    private void updateOrAddRecentlyViewedAislesList(String aisleId) {
        boolean isAisleViewed = false;
        String viewedId = null;
        Cursor cursor = mContext.getContentResolver().query(
                VueConstants.RECENTLY_VIEW_AISLES_URI, null, null, null,
                VueConstants.VIEW_TIME + " DESC");
        if (cursor.moveToFirst()) {
            do {
                String id = cursor.getString(cursor
                        .getColumnIndex(VueConstants.RECENTLY_VIEWED_AISLE_ID));
                if (id.equals(aisleId)) {
                    isAisleViewed = true;
                    viewedId = cursor.getString(cursor
                            .getColumnIndex(VueConstants.ID));
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
    
    public void insertRecentlyViewedAislesOnUpgrade(
            ArrayList<RecentlyViewedAisle> recentlyViewedAisles,
            SQLiteDatabase db) {
        if (recentlyViewedAisles != null && recentlyViewedAisles.size() > 0) {
            for (RecentlyViewedAisle recentlyViewedAisle : recentlyViewedAisles) {
                ContentValues values = new ContentValues();
                values.put(VueConstants.RECENTLY_VIEWED_AISLE_ID,
                        recentlyViewedAisle.getmAisleId());
                values.put(VueConstants.VIEW_TIME,
                        recentlyViewedAisle.getmTime());
                db.insert(DbHelper.DATABASE_TABLE_RECENTLY_VIEWED_AISLES, null,
                        values);
            }
        }
    }
    
    private void insertRatedImagesToDB(
            ArrayList<ImageRating> retrievedImageRating, boolean isUserRating,
            boolean isDirty) {
        boolean isMatched = false;
        Cursor cursor = mContext.getContentResolver().query(
                VueConstants.RATED_IMAGES_URI, null, null, null, null);
        
        for (ImageRating imageRating : retrievedImageRating) {
            isMatched = false;
            ContentValues values = new ContentValues();
            values.put(VueConstants.AISLE_ID, imageRating.mAisleId);
            values.put(VueConstants.ID, imageRating.mId);
            values.put(VueConstants.LIKE_OR_DISLIKE, imageRating.mLiked ? 1 : 0);
            values.put(VueConstants.AISLE_IMAGE_RATING_OWNER_FIRST_NAME,
                    imageRating.mImageRatingOwnerFirstName);
            values.put(VueConstants.AISLE_IMAGE_RATING_OWNER_LAST_NAME,
                    imageRating.mImageRatingOwnerLastName);
            values.put(VueConstants.AISLE_IMAGE_RATING_LASTMODIFIED_TIME,
                    imageRating.mLastModifiedTimestamp);
            values.put(VueConstants.DIRTY_FLAG, isDirty ? 1 : 0);
            if (cursor.moveToFirst()) {
                do {
                    
                    long id = cursor.getLong(cursor
                            .getColumnIndex(VueConstants.IMAGE_ID));
                    if (id == imageRating.mImageId.longValue()) {
                        mContext.getContentResolver().update(
                                VueConstants.RATED_IMAGES_URI,
                                values,
                                VueConstants.IMAGE_ID + "=? ",
                                new String[] { Long
                                        .toString(imageRating.mImageId) });
                        
                        isMatched = true;
                        break;
                    }
                    
                } while (cursor.moveToNext());
            }
            if (!isMatched) {
                values.put(VueConstants.IMAGE_ID, imageRating.getImageId()
                        .longValue());
                mContext.getContentResolver().insert(
                        VueConstants.RATED_IMAGES_URI, values);
            }
        }
        cursor.close();
    }
    
    public ArrayList<ImageRating> getRatedImagesList(String aisleId) {
        ArrayList<ImageRating> imgRatingList = new ArrayList<ImageRating>();
        Cursor cursor = mContext.getContentResolver().query(
                VueConstants.RATED_IMAGES_URI, null,
                VueConstants.AISLE_ID + "=? ", new String[] { aisleId }, null);
        if (cursor.moveToFirst()) {
            do {
                ImageRating imgRating = new ImageRating();
                imgRating.mId = cursor.getLong(cursor
                        .getColumnIndex(VueConstants.ID));
                imgRating.mImageId = cursor.getLong(cursor
                        .getColumnIndex(VueConstants.IMAGE_ID));
                imgRating.mAisleId = cursor.getLong(cursor
                        .getColumnIndex(VueConstants.AISLE_ID));
                int like = cursor.getInt(cursor
                        .getColumnIndex(VueConstants.LIKE_OR_DISLIKE));
                imgRating.mLiked = (like == 1) ? true : false;
                imgRating.mImageRatingOwnerFirstName = cursor
                        .getString(cursor
                                .getColumnIndex(VueConstants.AISLE_IMAGE_RATING_OWNER_FIRST_NAME));
                imgRating.mImageRatingOwnerLastName = cursor
                        .getString(cursor
                                .getColumnIndex(VueConstants.AISLE_IMAGE_RATING_OWNER_LAST_NAME));
                try {
                    imgRating.mLastModifiedTimestamp = cursor
                            .getLong(cursor
                                    .getColumnIndex(VueConstants.AISLE_IMAGE_RATING_LASTMODIFIED_TIME));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                imgRatingList.add(imgRating);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return imgRatingList;
    }
    
    public ArrayList<AisleBookmark> getBookmarkAisleIdsList() {
        AisleBookmark aisleBookmark;
        ArrayList<AisleBookmark> bookmarkAisles = new ArrayList<AisleBookmark>();
        Cursor cursor = mContext.getContentResolver().query(
                VueConstants.BOOKMARKER_AISLES_URI, null,
                VueConstants.IS_LIKED_OR_BOOKMARKED + "=?",
                new String[] { "1" }, null);
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
    
    public ArrayList<AisleBookmark> getAllBookmarkAisleIdsList() {
        AisleBookmark aisleBookmark;
        ArrayList<AisleBookmark> bookmarkAisles = new ArrayList<AisleBookmark>();
        Cursor cursor = mContext.getContentResolver().query(
                VueConstants.BOOKMARKER_AISLES_URI, null, null, null, null);
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
        Entry<String, Integer> maxEntry = null;
        for (Entry<String, Integer> entry : mapOrder.entrySet()) {
            if (maxEntry == null || entry.getValue() > maxEntry.getValue()) {
                maxEntry = entry;
            }
        }
        return maxEntry.getValue();
    }
    
    private int getMinAisleValue(HashMap<String, Integer> mapOrder) {
        Entry<String, Integer> minEntry = null;
        for (Entry<String, Integer> entry : mapOrder.entrySet()) {
            if (minEntry == null) {
                minEntry = entry;
            } else if (entry.getValue() < minEntry.getValue()) {
                minEntry = entry;
            }
        }
        return (minEntry == null) ? 0 : minEntry.getValue();
    }
    
    @SuppressWarnings("unused")
    private void upDateCommenterUrl(String userId, String userImgUrl) {
        Cursor c = mContext.getContentResolver().query(
                VueConstants.CONTENT_URI,
                new String[] { VueConstants.AISLE_OWNER_IMAGE_URL },
                VueConstants.USER_ID + "=?", new String[] { userId }, null);
        if (c.moveToFirst()) {
            do {
            } while (c.moveToNext());
        }
    }
    
    // not in use by Surendra
    @SuppressWarnings({ "unused", "rawtypes" })
    @Deprecated
    private void updateAisleOrder() {
        ContentValues values = new ContentValues();
        Cursor aisleIdCursor = mContext.getContentResolver().query(
                VueConstants.CONTENT_URI,
                new String[] { VueConstants.AISLE_Id }, null, null, null);
        Iterator<Entry<String, Integer>> entries = mAislesOrderMap.entrySet()
                .iterator();
        while (entries.hasNext()) {
            Map.Entry entry = (Map.Entry) entries.next();
            String key = (String) entry.getKey();
            Integer value = (Integer) entry.getValue();
            values.put(VueConstants.ID, String.format(FORMATE, value));
            Uri uri = Uri.parse(VueConstants.CONTENT_URI + "/" + key);
            mContext.getContentResolver().update(uri, values, null, null);
        }
    }
    
    private boolean deleteImagesfromDB(String imgId) {
        int rowDeleted = mContext.getContentResolver().delete(
                VueConstants.IMAGES_CONTENT_URI, VueConstants.IMAGE_ID + "=?",
                new String[] { imgId });
        return (rowDeleted == 1) ? true : false;
    }
    
    private class UsedKeywords {
        String lastUsedTime;
        String numberOfTimesUsed;
        String keyWord;
        
        public UsedKeywords(String keyWord, String lastUsedTime,
                String numberOfTimesUsed) {
            this.keyWord = keyWord;
            this.lastUsedTime = lastUsedTime;
            this.numberOfTimesUsed = numberOfTimesUsed;
        }
        
        @SuppressWarnings("unused")
        public boolean compareKeyword(UsedKeywords other) {
            if (this.keyWord.equals(other.keyWord)) {
                return true;
            }
            return false;
        }
        
        public boolean compareTime(UsedKeywords other) {
            if (this.lastUsedTime.compareTo(other.lastUsedTime) > 0) {
                return true;
            }
            return false;
        }
        
        public int compareNumberOfTimesUsed(UsedKeywords other) {
            int result = this.numberOfTimesUsed
                    .compareTo(other.numberOfTimesUsed);
            if (result > 0) {
                return MORE_TIMES_USED;
            } else if (result == 0) {
                return EQUAL_TIMES_USED;
            }
            return LESS_TIMES_USED;
        }
    }
    
    public ArrayList<AisleWindowContent> getPendingAisles() {
        
        AisleContext userInfo;
        // AisleImageDetails imageItemDetails;
        AisleWindowContent aisleItem = null;
        
        LinkedHashMap<String, AisleContext> map = new LinkedHashMap<String, AisleContext>();
        ArrayList<AisleWindowContent> aisleContentArray = new ArrayList<AisleWindowContent>();
        ArrayList<AisleImageDetails> imageItemsArray = new ArrayList<AisleImageDetails>();
        AisleImageDetails imageDetails = new AisleImageDetails();
        imageItemsArray.add(imageDetails);
        imageDetails.mImageUrl = VueConstants.NO_IMAGE_URL;
        imageDetails.mAvailableWidth = VueApplication.getInstance().getPixel(
                VueConstants.NO_IMAGE_WIDTH);
        imageDetails.mAvailableHeight = VueApplication.getInstance().getPixel(
                VueConstants.NO_IMAGE_HEIGHT);
        Cursor aislesCursor = mContext.getContentResolver().query(
                VueConstants.CONTENT_URI, null, null, null, null);
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
                userInfo.mJoinTime = Long.parseLong(aislesCursor
                        .getString(aislesCursor
                                .getColumnIndex(VueConstants.JOIN_TIME)));
                userInfo.mLookingForItem = aislesCursor.getString(aislesCursor
                        .getColumnIndex(VueConstants.LOOKING_FOR));
                userInfo.mOccasion = aislesCursor.getString(aislesCursor
                        .getColumnIndex(VueConstants.OCCASION));
                userInfo.mBookmarkCount = Integer.parseInt(aislesCursor
                        .getString(aislesCursor
                                .getColumnIndex(VueConstants.BOOKMARK_COUNT)));
                userInfo.mAisleOwnerImageURL = aislesCursor
                        .getString(aislesCursor
                                .getColumnIndex(VueConstants.AISLE_OWNER_IMAGE_URL));
                userInfo.mDescription = aislesCursor.getString(aislesCursor
                        .getColumnIndex(VueConstants.AISLE_DESCRIPTION));
                userInfo.mCategory = aislesCursor.getString(aislesCursor
                        .getColumnIndex(VueConstants.AISLE_CATEGORY));
                map.put(userInfo.mAisleId, userInfo);
            } while (aislesCursor.moveToNext());
        }
        Cursor aisleImagesCursor = mContext.getContentResolver().query(
                VueConstants.IMAGES_CONTENT_URI, null, null, null,
                VueConstants.ID + " ASC");
        
        Set<String> imgAisleId = new HashSet<String>();
        if (aisleImagesCursor.moveToFirst()) {
            do {
                imgAisleId.add(aisleImagesCursor.getString(aisleImagesCursor
                        .getColumnIndex(VueConstants.AISLE_Id)));
                
            } while (aisleImagesCursor.moveToNext());
            Iterator it = map.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pairs = (Map.Entry) it.next();
                String tempAisleId = (String) pairs.getKey();
                if (!imgAisleId.contains(tempAisleId)) {
                    userInfo = (AisleContext) pairs.getValue();
                    aisleItem = new AisleWindowContent(userInfo.mAisleId);
                    aisleItem.addAisleContent(userInfo, imageItemsArray);
                    aisleContentArray.add(aisleItem);
                }
                it.remove();
            }
        }
        aislesCursor.close();
        aisleImagesCursor.close();
        return aisleContentArray;
    }
    
    public void updateAllRatingAisles(ImageRating imgRating, boolean isDirty) {
        boolean isMatched = false;
        Cursor cursor = mContext.getContentResolver().query(
                VueConstants.ALL_RATED_IMAGES_URI, null, null, null, null);
            isMatched = false;
            ContentValues values = new ContentValues();
            values.put(VueConstants.AISLE_ID, imgRating.getAisleId().longValue());
            values.put(VueConstants.IMAGE_ID, imgRating.getImageId().longValue());
            values.put(VueConstants.LIKE_OR_DISLIKE, imgRating.mLiked ? 1 : 0);
            values.put(VueConstants.AISLE_IMAGE_RATING_OWNER_FIRST_NAME,
                    imgRating.mImageRatingOwnerFirstName);
            values.put(VueConstants.AISLE_IMAGE_RATING_OWNER_LAST_NAME,
                    imgRating.mImageRatingOwnerLastName);
            values.put(VueConstants.AISLE_IMAGE_RATING_LASTMODIFIED_TIME,
                    imgRating.mLastModifiedTimestamp.longValue());
            values.put(VueConstants.DIRTY_FLAG, isDirty ? 1 : 0);
            if (cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(cursor
                            .getColumnIndex(VueConstants.ID));
                    if (id == imgRating.mId.longValue()) {
                        mContext.getContentResolver()
                                .update(VueConstants.ALL_RATED_IMAGES_URI,
                                        values,
                                        VueConstants.ID + "=? ",
                                        new String[] { Long
                                                .toString(imgRating.mId) });
                        isMatched = true;
                        break;
                    }
                    
                } while (cursor.moveToNext());
            }
            if (!isMatched) {
                values.put(VueConstants.ID, imgRating.getId().longValue());
                mContext.getContentResolver().insert(
                        VueConstants.ALL_RATED_IMAGES_URI, values);
            }
        
        cursor.close();
    }
    
    
    /**
     * FOR TESTING PURPOSE ONLY, SHOULD BE REMOVED OR COMMENTED FROM WHERE IT IS
     * CALLING AFTER TESTING, to copy FishWrap.db to sdCard.
     */
    public void copydbToSdcard() {
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();
            
            if (sd.canWrite()) {
                String currentDBPath = "//data//com.lateralthoughts.vue//databases//Vue.db";
                String backupDBPath = "Vue.db";
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);
                
                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB)
                            .getChannel();
                    FileChannel dst = new FileOutputStream(backupDB)
                            .getChannel();
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
