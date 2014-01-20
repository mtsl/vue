package com.lateralthoughts.vue.connectivity;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lateralthoughts.vue.AisleManager;
import com.lateralthoughts.vue.AisleManager.AisleAddCallback;
import com.lateralthoughts.vue.AisleManager.AisleUpdateCallback;
import com.lateralthoughts.vue.AisleManager.ImageAddedCallback;
import com.lateralthoughts.vue.AisleManager.ImageUploadCallback;
import com.lateralthoughts.vue.AisleWindowContent;
import com.lateralthoughts.vue.AisleWindowContentFactory;
import com.lateralthoughts.vue.ImageRating;
import com.lateralthoughts.vue.R;
import com.lateralthoughts.vue.VueApplication;
import com.lateralthoughts.vue.VueConstants;
import com.lateralthoughts.vue.VueContentGateway;
import com.lateralthoughts.vue.VueLandingPageActivity;
import com.lateralthoughts.vue.VueTrendingAislesDataModel;
import com.lateralthoughts.vue.VueUser;
import com.lateralthoughts.vue.domain.Aisle;
import com.lateralthoughts.vue.domain.AisleBookmark;
import com.lateralthoughts.vue.domain.Image;
import com.lateralthoughts.vue.domain.ImageComment;
import com.lateralthoughts.vue.domain.ImageCommentRequest;
import com.lateralthoughts.vue.domain.VueImage;
import com.lateralthoughts.vue.parser.Parser;
import com.lateralthoughts.vue.ui.NotifyProgress;
import com.lateralthoughts.vue.ui.StackViews;
import com.lateralthoughts.vue.utils.UrlConstants;
import com.lateralthoughts.vue.utils.Utils;

public class NetworkHandler {
    
    Context mContext;
    private static final String SEARCH_REQUEST_URL = "http://2-java.vueapi-canary.appspot.com/api/getaisleswithmatchingkeyword/";
    public DataBaseManager dbManager;
    protected VueContentGateway mVueContentGateway;
    protected TrendingAislesContentParser mTrendingAislesParser;
    private static final int NOTIFICATION_THRESHOLD = 4;
    private static final int TRENDING_AISLES_BATCH_SIZE = 20;
    public static final int TRENDING_AISLES_BATCH_INITIAL_SIZE = 10;
    private static final String MY_AISLES = "aislesget/user/";
    protected int mLimit;
    public int offset;
    ArrayList<AisleWindowContent> mAislesList = null;
    private SharedPreferences mSharedPreferencesObj;
    public ArrayList<String> bookmarkedList = new ArrayList<String>();
    private ArrayList<String> ratedImageList = new ArrayList<String>();
    
    public NetworkHandler(Context context) {
        mContext = context;
        mVueContentGateway = VueContentGateway.getInstance();
        mSharedPreferencesObj = VueApplication.getInstance()
                .getSharedPreferences(VueConstants.SHAREDPREFERENCE_NAME, 0);
        try {
            mTrendingAislesParser = new TrendingAislesContentParser(
                    new Handler(), VueConstants.AISLE_TRENDING_LIST_DATA);
        } catch (Exception e) {
        }
        dbManager = DataBaseManager.getInstance(mContext);
        mLimit = TRENDING_AISLES_BATCH_INITIAL_SIZE;
        offset = 0;
    }
    
    // while user scrolls down get next 10 aisles
    public void requestMoreAisle(boolean loadMore, String screenname) {
        
        if (VueTrendingAislesDataModel
                .getInstance(VueApplication.getInstance())
                .isMoreDataAvailable()) {
            
            VueTrendingAislesDataModel
                    .getInstance(VueApplication.getInstance()).loadOnRequest = false;
            
            if (offset < NOTIFICATION_THRESHOLD * TRENDING_AISLES_BATCH_SIZE)
                offset += mLimit;
            else {
                offset += mLimit;
                mLimit = TRENDING_AISLES_BATCH_SIZE;
            }
            mVueContentGateway.getTrendingAisles(mLimit, offset,
                    mTrendingAislesParser, loadMore, screenname);
        } else {
            
        }
    }
    
    // get the aisle based on the category
    public void reqestByCategory(String category, NotifyProgress progress,
            boolean fromServer, boolean loadMore, String screenname) {
        
        VueTrendingAislesDataModel.getInstance(VueApplication.getInstance())
                .setNotificationProgress(progress, fromServer);
        if (fromServer) {
            offset = 0;
            mLimit = TRENDING_AISLES_BATCH_INITIAL_SIZE;
            VueTrendingAislesDataModel
                    .getInstance(VueApplication.getInstance()).clearContent();
            VueTrendingAislesDataModel
                    .getInstance(VueApplication.getInstance()).showProgress();
            VueTrendingAislesDataModel
                    .getInstance(VueApplication.getInstance())
                    .setMoreDataAVailable(true);
            mVueContentGateway.getTrendingAisles(mLimit, offset,
                    mTrendingAislesParser, loadMore, screenname);
            
        } else {
            DataBaseManager.getInstance(VueApplication.getInstance())
                    .resetDbParams();
            ArrayList<AisleWindowContent> aisleContentArray = dbManager
                    .getAislesFromDB(null, false);
            if (aisleContentArray.size() == 0) {
                VueTrendingAislesDataModel.getInstance(VueApplication
                        .getInstance()).mIsFromDb = false;
                return;
            }
            
            Message msg = new Message();
            msg.obj = aisleContentArray;
            VueTrendingAislesDataModel.getInstance(mContext).mHandler
                    .sendMessage(msg);
        }
        
    }
    
    public void requestUpdateAisle(Aisle aisle,
            AisleUpdateCallback aisleUpdateCallback) {
        AisleManager.getAisleManager().updateAisle(aisle, aisleUpdateCallback);
    }
    
    // request the server to create an empty aisle.
    public void requestCreateAisle(Aisle aisle, final AisleAddCallback callback) {
        AisleManager.getAisleManager().createEmptyAisle(aisle, callback);
    }
    
    public void requestForDeleteImage(Image image, String aisleId) {
        AisleManager.getAisleManager().deleteImage(image, aisleId);
    }
    
    public void requestForAddImage(boolean fromDetailsScreenFlag,
            String imageId, VueImage image,
            ImageAddedCallback imageAddedCallback) {
        AisleManager.getAisleManager().addImageToAisle(fromDetailsScreenFlag,
                imageId, image, imageAddedCallback);
    }
    
    public void requestForUploadImage(File imageFile,
            ImageUploadCallback callback) {
        AisleManager.getAisleManager().uploadImage(imageFile, callback);
    }
    
    // get aisles related to search keyword
    public void requestSearch(final String searchString) {
        JsonArrayRequest vueRequest = new JsonArrayRequest(SEARCH_REQUEST_URL
                + searchString, new Response.Listener<JSONArray>() {
            
            @Override
            public void onResponse(JSONArray response) {
                if (null != response) {
                    Bundle responseBundle = new Bundle();
                    responseBundle.putString("Search result",
                            response.toString());
                    responseBundle.putBoolean("loadMore", false);
                    mTrendingAislesParser.send(1, responseBundle);
                }
            }
        }, new Response.ErrorListener() {
            
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        // RETRY POLICY
        vueRequest.setRetryPolicy(new DefaultRetryPolicy(
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, Utils.MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        
        VueApplication.getInstance().getRequestQueue().add(vueRequest);
        
    }
    
    public void requestUserAisles(String userId) {
        
        JsonArrayRequest vueRequest = new JsonArrayRequest(SEARCH_REQUEST_URL
                + MY_AISLES + userId, new Response.Listener<JSONArray>() {
            
            @Override
            public void onResponse(JSONArray response) {
                if (null != response) {
                    Bundle responseBundle = new Bundle();
                    responseBundle.putString("Search result",
                            response.toString());
                    responseBundle.putBoolean("loadMore", false);
                    mTrendingAislesParser.send(1, responseBundle);
                }
            }
        }, new Response.ErrorListener() {
            
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        
        VueApplication.getInstance().getRequestQueue().add(vueRequest);
        
    }
    
    public void loadInitialData(boolean loadMore, Handler mHandler,
            String screenName) {
        getBookmarkAisleByUser();
        getRatedImageList();
        
        offset = 0;
        if (!VueConnectivityManager.isNetworkConnected(mContext)) {
            Toast.makeText(mContext, R.string.no_network, Toast.LENGTH_SHORT)
                    .show();
            ArrayList<AisleWindowContent> aisleContentArray = dbManager
                    .getAislesFromDB(null, false);
            if (aisleContentArray.size() == 0) {
                return;
            }
            Message msg = new Message();
            msg.obj = aisleContentArray;
            mHandler.sendMessage(msg);
            
        } else {
            mLimit = 30;
            mVueContentGateway.getTrendingAisles(mLimit, offset,
                    mTrendingAislesParser, loadMore, screenName);
        }
        
    }
    
    public void loadTrendingAisle(boolean loadMore, boolean fromServer,
            NotifyProgress progress, String screenName) {
        VueTrendingAislesDataModel.getInstance(VueApplication.getInstance())
                .setNotificationProgress(progress, fromServer);
        VueTrendingAislesDataModel.getInstance(VueApplication.getInstance())
                .showProgress();
        mVueContentGateway.getTrendingAisles(mLimit, offset,
                mTrendingAislesParser, loadMore, screenName);
    }
    
    public void requestAislesByUser(boolean fromServer,
            final NotifyProgress progress, final String screenName) {
        
        offset = 0;
        if (!fromServer) {
            String userId = getUserId();
            if (userId != null) {
                ArrayList<AisleWindowContent> windowList = DataBaseManager
                        .getInstance(VueApplication.getInstance())
                        .getAislesByUserId(userId);
                if (windowList != null && windowList.size() > 0) {
                    clearList(progress);
                    for (AisleWindowContent aisleItem : windowList) {
                        if (!aisleItem.getAisleContext().mUserId.equals(userId)) {
                            windowList.remove(aisleItem);
                        }
                    }
                    for (int i = 0; i < windowList.size(); i++) {
                        VueTrendingAislesDataModel.getInstance(
                                VueApplication.getInstance()).addItemToList(
                                windowList.get(i).getAisleContext().mAisleId,
                                windowList.get(i));
                    }
                    Intent i = new Intent(VueConstants.LANDING_SCREEN_RECEIVER);
                    i.putExtra(VueConstants.LANDING_SCREEN_RECEIVER_KEY,
                            screenName);
                    VueApplication.getInstance().sendBroadcast(i);
                    VueTrendingAislesDataModel.getInstance(
                            VueApplication.getInstance()).dataObserver();
                } else {
                    
                    StackViews.getInstance().pull();
                    Toast.makeText(VueLandingPageActivity.landingPageActivity,
                            "There are no Aisles for this User.",
                            Toast.LENGTH_LONG).show();
                }
                
            } else {
                Toast.makeText(VueApplication.getInstance(),
                        "Unable to get user id", Toast.LENGTH_SHORT).show();
                StackViews.getInstance().pull();
            }
        } else {
            VueTrendingAislesDataModel
                    .getInstance(VueApplication.getInstance())
                    .setNotificationProgress(progress, fromServer);
            VueTrendingAislesDataModel
                    .getInstance(VueApplication.getInstance()).showProgress();
            // TODO: CHANGE THIS REQUEST TO VOLLEY
            if (VueConnectivityManager.isNetworkConnected(VueApplication
                    .getInstance())) {
                VueTrendingAislesDataModel.getInstance(VueApplication
                        .getInstance()).loadOnRequest = false;
                new Thread(new Runnable() {
                    
                    @Override
                    public void run() {
                        try {
                            mAislesList = null;
                            String userId = getUserId();
                            mAislesList = getAislesByUser(userId);
                            
                            if (mAislesList != null && mAislesList.size() > 0) {
                                for (AisleWindowContent aisleItem : mAislesList) {
                                    if (!aisleItem.getAisleContext().mUserId
                                            .equals(userId)) {
                                        mAislesList.remove(aisleItem);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (VueLandingPageActivity.landingPageActivity != null) {
                            VueLandingPageActivity.landingPageActivity
                                    .runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            VueTrendingAislesDataModel
                                                    .getInstance(VueApplication
                                                            .getInstance()).loadOnRequest = false;
                                            if (mAislesList != null
                                                    && mAislesList.size() > 0) {
                                                
                                                Intent intent = new Intent(
                                                        VueConstants.LANDING_SCREEN_RECEIVER);
                                                intent.putExtra(
                                                        VueConstants.LANDING_SCREEN_RECEIVER_KEY,
                                                        screenName);
                                                VueApplication.getInstance()
                                                        .sendBroadcast(intent);
                                                clearList(progress);
                                                Collections
                                                        .reverse(mAislesList);
                                                for (int i = 0; i < mAislesList
                                                        .size(); i++) {
                                                    VueTrendingAislesDataModel
                                                            .getInstance(
                                                                    VueApplication
                                                                            .getInstance())
                                                            .addItemToList(
                                                                    mAislesList
                                                                            .get(i)
                                                                            .getAisleContext().mAisleId,
                                                                    mAislesList
                                                                            .get(i));
                                                }
                                                VueTrendingAislesDataModel
                                                        .getInstance(
                                                                VueApplication
                                                                        .getInstance())
                                                        .dataObserver();
                                                // adding my aisle to db.
                                                DataBaseManager
                                                        .getInstance(
                                                                VueApplication
                                                                        .getInstance())
                                                        .addTrentingAislesFromServerToDB(
                                                                VueApplication
                                                                        .getInstance(),
                                                                mAislesList,
                                                                offset,
                                                                DataBaseManager.MY_AISLES);
                                            } else {
                                                StackViews.getInstance().pull();
                                                Toast.makeText(
                                                        VueLandingPageActivity.landingPageActivity,
                                                        "There are no Aisles for this User.",
                                                        Toast.LENGTH_LONG)
                                                        .show();
                                            }
                                            VueTrendingAislesDataModel
                                                    .getInstance(
                                                            VueApplication
                                                                    .getInstance())
                                                    .dismissProgress();
                                            
                                        }
                                        
                                    });
                        }
                    }
                }).start();
            } else {
                
                Toast.makeText(
                        VueApplication.getInstance(),
                        VueApplication.getInstance().getResources()
                                .getString(R.string.no_network),
                        Toast.LENGTH_LONG).show();
                StackViews.getInstance().pull();
            }
        }
    }
    
    public int getmOffset() {
        return offset;
    }
    
    public void setmOffset(int mOffset) {
        this.offset = mOffset;
    }
    
    public ArrayList<AisleWindowContent> getAislesByUser(String userId)
            throws Exception {
        // TODO: change to volley
        
        if (userId == null) {
            return null;
        }
        String requestUrl = UrlConstants.GET_AISLELIST_BYUSER_RESTURL + "/"
                + userId;
        URL url = new URL(requestUrl);
        HttpGet httpGet = new HttpGet(url.toString());
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpResponse response = httpClient.execute(httpGet);
        if (response.getEntity() != null
                && response.getStatusLine().getStatusCode() == 200) {
            String responseMessage = EntityUtils.toString(response.getEntity());
            return new Parser().getUserAilseLIst(responseMessage);
        }
        return null;
        
    }
    
    public void getBookmarkAisleByUser() {
        new Thread(new Runnable() {
            
            @Override
            public void run() {
                try {
                    String userId = getUserId();
                    if (userId == null) {
                        return;
                    }
                    URL url = new URL(UrlConstants.GET_BOOKMARK_Aisles + "/"
                            + userId + "/" + "0");
                    HttpGet httpGet = new HttpGet(url.toString());
                    DefaultHttpClient httpClient = new DefaultHttpClient();
                    HttpResponse response = httpClient.execute(httpGet);
                    if (response.getEntity() != null
                            && response.getStatusLine().getStatusCode() == 200) {
                        String responseMessage = EntityUtils.toString(response
                                .getEntity());
                        if (responseMessage != null) {
                            ArrayList<AisleBookmark> bookmarkedAisles = new Parser()
                                    .parseBookmarkedAisles(responseMessage);
                            bookmarkedList.clear();
                            for (AisleBookmark aB : bookmarkedAisles) {
                                DataBaseManager.getInstance(mContext)
                                        .updateBookmarkAisles(aB.getId(),
                                                Long.toString(aB.getAisleId()),
                                                aB.getBookmarked());
                                
                                if (aB.getBookmarked()) {
                                    bookmarkedList.add(String.valueOf(aB
                                            .getAisleId()));
                                }
                            }
                            VueTrendingAislesDataModel.getInstance(
                                    VueApplication.getInstance())
                                    .updateBookmarkAisleStatus(bookmarkedList);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
            }
        }).start();
        
    }
    
    public boolean isAisleBookmarked(String aisleId) {
        Cursor cursor = mContext.getContentResolver().query(
                VueConstants.BOOKMARKER_AISLES_URI, null,
                VueConstants.AISLE_ID + "=?", new String[] { aisleId }, null);
        if (cursor.moveToFirst()) {
            do {
                if (aisleId.equals(cursor.getString(cursor
                        .getColumnIndex(VueConstants.AISLE_ID)))) {
                    if (cursor
                            .getInt(cursor
                                    .getColumnIndex(VueConstants.IS_LIKED_OR_BOOKMARKED)) == 1) {
                        cursor.close();
                        return true;
                    } else {
                        cursor.close();
                        return false;
                    }
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return false;
    }
    
    public VueUser getUserObj() {
        VueUser storedVueUser = null;
        try {
            storedVueUser = Utils.readUserObjectFromFile(
                    VueApplication.getInstance(),
                    VueConstants.VUE_APP_USEROBJECT__FILENAME);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (storedVueUser != null) {
            storedVueUser.getUserImageURL();
        }
        return storedVueUser;
        
    }
    
    public String getUserId() {
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
            storedVueUser.getUserImageURL();
        }
        return userId;
    }
    
    public ImageComment createImageComment(ImageCommentRequest comment)
            throws Exception {
        ImageComment createdImageComment = null;
        ObjectMapper mapper = new ObjectMapper();
        if (VueConnectivityManager.isNetworkConnected(mContext)) {
            URL url = new URL(UrlConstants.CREATE_IMAGECOMMENT_RESTURL + "/"
                    + Long.valueOf(getUserObj().getId()).toString());
            HttpPut httpPut = new HttpPut(url.toString());
            StringEntity entity = new StringEntity(
                    mapper.writeValueAsString(comment));
            entity.setContentType("application/json;charset=UTF-8");
            entity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,
                    "application/json;charset=UTF-8"));
            httpPut.setEntity(entity);
            
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpResponse response = httpClient.execute(httpPut);
            if (response.getEntity() != null
                    && response.getStatusLine().getStatusCode() == 200) {
                String responseMessage = EntityUtils.toString(response
                        .getEntity());
                if (responseMessage.length() > 0) {
                    createdImageComment = (new ObjectMapper()).readValue(
                            responseMessage, ImageComment.class);
                    Editor editor = mSharedPreferencesObj.edit();
                    editor.putBoolean(VueConstants.IS_COMMENT_DIRTY, false);
                    editor.commit();
                    DataBaseManager.getInstance(mContext).addComments(
                            createdImageComment, false);
                }
            } else {
            }
        } else {
            Editor editor = mSharedPreferencesObj.edit();
            editor.putBoolean(VueConstants.IS_COMMENT_DIRTY, true);
            editor.commit();
            createdImageComment = new ImageComment();
            createdImageComment.setComment(comment.getComment());
            createdImageComment.setLastModifiedTimestamp(comment
                    .getLastModifiedTimestamp());
            createdImageComment.setOwnerImageId(comment.getOwnerImageId());
            createdImageComment.setOwnerUserId(comment.getOwnerUserId());
            DataBaseManager.getInstance(mContext).addComments(
                    createdImageComment, true);
        }
        return createdImageComment;
    }
    
    public void getCommentsFromDb(String aisleId) {
        Map<Long, ArrayList<String>> commentsMap = new HashMap<Long, ArrayList<String>>();
        String imageId = null;
        ArrayList<String> tempComments;
        tempComments = commentsMap.remove(Long.parseLong(imageId));
        if (tempComments == null) {
            tempComments = new ArrayList<String>();
            tempComments.add("add value from cursor");
        } else {
            tempComments.add("add value from cursor");
        }
        commentsMap.put(Long.parseLong(imageId), tempComments);
        
    }
    
    /*
     * to retrieve all the rated images by this user.
     */
    public void getRatedImageList() {
        String userId = getUserId();
        if (userId == null) {
            return;
        }
        JsonArrayRequest vueRequest = new JsonArrayRequest(
                UrlConstants.GET_RATINGS_RESTURL + "/" + userId + "/" + 0L,
                new Response.Listener<JSONArray>() {
                    
                    @Override
                    public void onResponse(JSONArray response) {
                        if (null != response) {
                            ArrayList<ImageRating> retrievedImageRating = null;
                            if (response.length() > 0) {
                                try {
                                    retrievedImageRating = Parser
                                            .parseRatedImages(response
                                                    .toString());
                                    
                                    if (retrievedImageRating != null
                                            && retrievedImageRating.size() > 0) {
                                        ratedImageList.clear();
                                        for (int index = 0; index < retrievedImageRating
                                                .size(); index++) {
                                            ratedImageList.add(String
                                                    .valueOf(retrievedImageRating
                                                            .get(index)
                                                            .getImageId()));
                                        }
                                    }
                                    DataBaseManager.getInstance(mContext)
                                            .insertRatedImages(
                                                    retrievedImageRating, true);
                                    VueTrendingAislesDataModel.getInstance(
                                            VueApplication.getInstance())
                                            .updateImageRatingStatus(
                                                    ratedImageList);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        
                    }
                }, new Response.ErrorListener() {
                    
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                });
        VueApplication.getInstance().getRequestQueue().add(vueRequest);
    }
    
    public void clearList(NotifyProgress progress) {
        if (progress != null) {
            progress.clearBrowsers();
        }
        VueTrendingAislesDataModel.getInstance(VueApplication.getInstance())
                .clearAisles();
        AisleWindowContentFactory.getInstance(VueApplication.getInstance())
                .clearObjectsInUse();
        VueTrendingAislesDataModel.getInstance(VueApplication.getInstance())
                .dataObserver();
    }
    
    public void makeOffseZero() {
        offset = 0;
    }
    
    public boolean checkIsAisleBookmarked(String aisleId) {
        boolean isBookmarked = bookmarkedList.contains(aisleId);
        return isBookmarked;
    }
    
    public void modifyBookmarkList(String aisleId, boolean isAddRequest) {
        if (isAddRequest) {
            bookmarkedList.add(aisleId);
        } else {
            bookmarkedList.remove(aisleId);
        }
    }
    
    public boolean getImageRateStatus(String imageId) {
        boolean isImageRated = false;
        if (ratedImageList.contains(imageId)) {
            isImageRated = true;
        }
        return isImageRated;
    }
}
