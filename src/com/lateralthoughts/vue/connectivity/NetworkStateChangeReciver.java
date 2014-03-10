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
import com.lateralthoughts.vue.VueApplication;
import com.lateralthoughts.vue.VueConstants;
import com.lateralthoughts.vue.VueTrendingAislesDataModel;
import com.lateralthoughts.vue.domain.AisleBookmark;
import com.lateralthoughts.vue.domain.ImageComment;
import com.lateralthoughts.vue.domain.ImageCommentRequest;

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
                    false)) {
                
                ArrayList<AisleBookmark> bookmarkedAisles = DataBaseManager
                        .getInstance(context).getDirtyBookmarkedAisles();
                
                for (AisleBookmark ab : bookmarkedAisles) {
                    try {
                        //in db bookmark id object set to zero if no network so change it to null while syncing  to server.
                        if (ab.getId() != null && ab.getId().longValue() == 0) {
                            ab.setId(null);
                        }
                        AisleManager.getAisleManager().aisleBookmarkUpdate(ab);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                
            }
            if ((mSharedPreferencesObj.getBoolean(VueConstants.IS_IMAGE_DIRTY,
                    false))) {
                ArrayList<ImageRating> imagsRating = DataBaseManager
                        .getInstance(context).getDirtyImages("1");
                if (imagsRating != null) {
                    for (ImageRating imgRating : imagsRating) {
                        if (imgRating.mId == null) {
                            continue;
                        }
                        if (imgRating.mId == 1L) {
                            imgRating.mId = null;
                        }
                        try {
                            Cursor c = context.getContentResolver().query(
                                    VueConstants.IMAGES_CONTENT_URI,
                                    null,
                                    VueConstants.IMAGE_ID + "=?",
                                    new String[] { String.valueOf(imgRating
                                            .getImageId()) }, null);
                            int likesCount = 0;
                            boolean isMathced = false;
                            
                            if (c.moveToFirst()) {
                                do {
                                    long imgId = c
                                            .getLong(c
                                                    .getColumnIndex(VueConstants.IMAGE_ID));
                                    
                                    if (imgId == imgRating.getImageId()
                                            .longValue()) {
                                        likesCount = c
                                                .getInt(c
                                                        .getColumnIndex(VueConstants.LIKES_COUNT));
                                        isMathced = true;
                                        break;
                                    }
                                } while (c.moveToNext());
                            }
                            c.close();
                            if (isMathced) {
                                
                                AisleManager.getAisleManager().updateRating(
                                        imgRating, likesCount);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            
            if (mSharedPreferencesObj.getBoolean(VueConstants.IS_COMMENT_DIRTY,
                    false)) {
                new Thread(new Runnable() {
                    
                    @Override
                    public void run() {
                        ArrayList<ImageComment> comments = DataBaseManager.getInstance(
                                VueApplication.getInstance()).getDirtyComments("1");
                        NetworkHandler networkHandler = VueTrendingAislesDataModel.getInstance(VueApplication.getInstance()).getNetworkHandler();
                       
                        for (ImageComment comment : comments) {
                            try {
                                long id = comment.getId();
                                ImageCommentRequest imageRequest = new ImageCommentRequest();
                                imageRequest.setComment(comment.getComment());
                                imageRequest.setLastModifiedTimestamp(comment
                                        .getLastModifiedTimestamp());
                                imageRequest.setOwnerUserId(comment.getOwnerUserId());
                                imageRequest.setOwnerImageId(comment.getOwnerImageId());
                                ///////////////
                                    syncComment(VueApplication.getInstance(),imageRequest, id);
                               
                                
                            } catch (Exception e) {
                              
                                e.printStackTrace();
                            }
                        }  
                        
                    }
                }).start();
       
            }
        }
    }
    private synchronized void syncComment(Context context, ImageCommentRequest imageRequest, long dirtyTime){
        NetworkHandler networkHandler = VueTrendingAislesDataModel.getInstance(context).getNetworkHandler();
        try {
            Log.i("commentIssue", "commentIssue: receiver syncying: "+dirtyTime);
           // networkHandler.createImageComment(imageRequest, dirtyTime);
        } catch (Exception e) {
            e.printStackTrace();
        }   
    }
}
