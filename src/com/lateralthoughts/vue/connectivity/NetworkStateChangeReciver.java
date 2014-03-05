package com.lateralthoughts.vue.connectivity;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.Log;

import com.lateralthoughts.vue.AisleManager;
import com.lateralthoughts.vue.ImageRating;
import com.lateralthoughts.vue.VueConstants;
import com.lateralthoughts.vue.domain.AisleBookmark;

public class NetworkStateChangeReciver extends BroadcastReceiver {
    
    private SharedPreferences mSharedPreferencesObj;
    AisleBookmark mCreatedAisleBookmark = null;
    AisleBookmark mAisleBookmark = null;
    boolean mIsDirty = true;
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void onReceive(Context context, Intent inetent) {
        if (VueConnectivityManager.isNetworkConnected(context)) {
            // TODO: if Database is dirty use shared preference here.
            mSharedPreferencesObj = context.getSharedPreferences(
                    VueConstants.SHAREDPREFERENCE_NAME, 0);
            if (mSharedPreferencesObj.getBoolean(VueConstants.IS_AISLE_DIRTY,
                    false)) {/*
                VueUser storedVueUser = null;
                try {
                    storedVueUser = Utils.readUserObjectFromFile(context,
                            VueConstants.VUE_APP_USEROBJECT__FILENAME);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                final ArrayList<AisleWindowContent> aisles = DataBaseManager
                        .getInstance(context).getDirtyAisles("1");
                for (AisleWindowContent content : aisles) {
                    mAisleBookmark = new AisleBookmark(null, true,
                            Long.parseLong(content.getAisleId()));
                    try {
                        storedVueUser = Utils.readUserObjectFromFile(
                                VueApplication.getInstance(),
                                VueConstants.VUE_APP_USEROBJECT__FILENAME);
                        
                        ObjectMapper mapper = new ObjectMapper();
                        String bookmarkAisleAsString = mapper
                                .writeValueAsString(mAisleBookmark);
                        Response.Listener listener = new Response.Listener<String>() {
                            
                            @Override
                            public void onResponse(String jsonArray) {
                                if (jsonArray != null) {
                                    try {
                                        mCreatedAisleBookmark = (new ObjectMapper())
                                                .readValue(jsonArray,
                                                        AisleBookmark.class);
                                        AisleManager.getAisleManager()
                                                .updateBookmartToDb(aisles,
                                                        mAisleBookmark, false);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        };
                        
                        Response.ErrorListener errorListener = new ErrorListener() {
                            
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                mIsDirty = true;
                            }
                        };
                        String bookmarkUrl = UrlConstants.CREATE_BOOKMARK_RESTURL;
                        if (mAisleBookmark.getId() != null) {
                            bookmarkUrl = UrlConstants.UPDATE_BOOKMARK_RESTURL;
                        }
                        BookmarkPutRequest request = new BookmarkPutRequest(
                                bookmarkAisleAsString, listener, errorListener,
                                bookmarkUrl + "/" + storedVueUser.getId());
                        VueApplication.getInstance().getRequestQueue()
                                .add(request);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            */}
            
            if ((mSharedPreferencesObj.getBoolean(VueConstants.IS_IMAGE_DIRTY,
                    false))) {
                ArrayList<ImageRating> imagsRating = DataBaseManager
                        .getInstance(context).getDirtyImages("1");
                for (ImageRating imgRating : imagsRating) {
                    if(imgRating.mId == 1L) {
                        imgRating.mId = null;
                    }
                    try {
                        Cursor c = context.getContentResolver().query(
                                VueConstants.IMAGES_CONTENT_URI, null,
                                VueConstants.IMAGE_ID + "=?",
                                new String[] {String.valueOf(imgRating
                                        .getImageId())}, null);
                        int likesCount = 0;
                        if (c.moveToFirst()) {
                            do {
                                long imgId = c.getLong(c.getColumnIndex(VueConstants.IMAGE_ID));
                               if(imgId == imgRating.getImageId().longValue()) {
                                   likesCount = c.getInt(c.getColumnIndex(VueConstants.LIKES_COUNT));
                                   Log.e("NetworkStateChangeReciver", "VueConstants.IS_IMAGE_DIRTY imgId Matched: " + imgId);
                                   break;
                               }
                            } while (c.moveToNext());
                        }
                        c.close();
                        AisleManager.getAisleManager().updateRating(imgRating,
                                likesCount);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                   
                }
            }
            
            if (mSharedPreferencesObj.getBoolean(VueConstants.IS_COMMENT_DIRTY,
                    false)) {/*
                ArrayList<ImageComment> comments = DataBaseManager.getInstance(
                        context).getDirtyComments("1");
                NetworkHandler networkHandler = new NetworkHandler(context);
                for (ImageComment comment : comments) {
                    try {
                        ImageCommentRequest imageRequest = new ImageCommentRequest();
                        imageRequest.setComment(comment.getComment());
                        imageRequest.setLastModifiedTimestamp(comment
                                .getLastModifiedTimestamp());
                        imageRequest.setOwnerImageId(comment.getOwnerImageId());
                        imageRequest.setOwnerUserId(comment.getOwnerUserId());
                        networkHandler.createImageComment(imageRequest);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            */}
        }
    }
    
}
