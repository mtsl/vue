package com.lateralthoughts.vue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.client.ClientProtocolException;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lateralthoughts.vue.connectivity.DataBaseManager;
import com.lateralthoughts.vue.connectivity.VueConnectivityManager;
import com.lateralthoughts.vue.domain.Aisle;
import com.lateralthoughts.vue.domain.AisleBookmark;
import com.lateralthoughts.vue.domain.Image;
import com.lateralthoughts.vue.domain.VueImage;
import com.lateralthoughts.vue.utils.AddImageToAisleBackgroundThread;
import com.lateralthoughts.vue.utils.AisleCreationBackgroundThread;
import com.lateralthoughts.vue.utils.AisleUpdateBackgroundThread;
import com.lateralthoughts.vue.utils.DeleteImageFromAisle;
import com.lateralthoughts.vue.utils.UploadImageBackgroundThread;
import com.lateralthoughts.vue.utils.UrlConstants;
import com.lateralthoughts.vue.utils.Utils;

public class AisleManager {
    
    public interface AisleAddCallback {
        public void onAisleAdded(Aisle aisle);
    }
    
    public interface AisleUpdateCallback {
        public void onAisleUpdated();
    }
    
    public interface ImageUploadCallback {
        public void onImageUploaded(String imageUrl);
    }
    
    public interface ImageAddedCallback {
        public void onImageAdded(String imageId);
    }
    
    private static AisleManager sAisleManager = null;
    private boolean mIsDirty;
    private SharedPreferences mSharedPreferencesObj;
    
    private AisleManager() {
        mSharedPreferencesObj = VueApplication.getInstance()
                .getSharedPreferences(VueConstants.SHAREDPREFERENCE_NAME, 0);
    }
    
    public static AisleManager getAisleManager() {
        if (null == sAisleManager)
            sAisleManager = new AisleManager();
        return sAisleManager;
    }
    
    // create an unidentified VueUser object. This is an asynchronous API and
    // needs to make a round trip
    // network call.
    // Usually this call cannot be invoked when mCurrentUser is set to a valid
    // value. This is because we can only
    // have only current user at a time. When this call returns the
    // UserUpdateCallback's onUserUpdated API will
    // be invoked and the VueUser object is created and set at that point.
    public void createEmptyAisle(final Aisle aisle,
            final AisleAddCallback callback) {
        Thread t = new Thread(
                new AisleCreationBackgroundThread(aisle, callback));
        t.start();
    }
    
    public void updateAisle(final Aisle aisle,
            AisleUpdateCallback aisleUpdateCallback) {
        Thread t = new Thread(new AisleUpdateBackgroundThread(aisle,
                aisleUpdateCallback));
        t.start();
    }
    
    public void deleteImage(final Image image, String aisleId) {
        Thread t = new Thread(new DeleteImageFromAisle(image, aisleId));
        t.start();
    }
    
    public void uploadImage(File imageName,
            ImageUploadCallback imageUploadCallback) {
        if (null == imageName) {
            throw new RuntimeException(
                    "Can't create Aisle without a non null aisle object");
        }
        
        Thread t = new Thread(new UploadImageBackgroundThread(imageName,
                imageUploadCallback));
        t.start();
    }
    
    // issues a request to add an image to the aisle.
    public void addImageToAisle(final boolean fromDetailsScreenFlag,
            String imageId, VueImage image,
            ImageAddedCallback imageAddedCallback) {
        if (null == image) {
            throw new RuntimeException(
                    "Can't create Aisle without a non null aisle object");
        }
        
        Thread t = new Thread(new AddImageToAisleBackgroundThread(image,
                fromDetailsScreenFlag, imageId, imageAddedCallback));
        t.start();
    }
    
    /**
     * send the book mark information to server and writes the response to db if
     * network is not available then it will write the book mark info to db and
     * automatically sync to the server later, when ever the network is
     * available.
     * 
     * @param AisleBookmark
     *            aisleBookmark
     * @param String
     *            userId
     * @throws ClientProtocolException
     *             , IOException
     * */
    public void aisleBookmarkUpdate(final AisleBookmark aisleBookmark,
    
    String userId) throws ClientProtocolException, IOException {
        mIsDirty = true;
        String url;
        if (aisleBookmark.getId() == null) {
            url = UrlConstants.CREATE_BOOKMARK_RESTURL + "/";
        } else {
            url = UrlConstants.UPDATE_BOOKMARK_RESTURL + "/";
        }
        if (VueConnectivityManager.isNetworkConnected(VueApplication
                .getInstance())) {
            VueUser storedVueUser = null;
            try {
                storedVueUser = Utils.readUserObjectFromFile(
                        VueApplication.getInstance(),
                        VueConstants.VUE_APP_USEROBJECT__FILENAME);
            } catch (Exception e) {
                e.printStackTrace();
            }
            ObjectMapper mapper = new ObjectMapper();
            String bookmarkAisleAsString = mapper
                    .writeValueAsString(aisleBookmark);
            
            @SuppressWarnings("rawtypes")
            Response.Listener listener = new Response.Listener<String>() {
                
                @Override
                public void onResponse(String jsonArray) {
                    if (jsonArray != null) {
                        try {
                            AisleBookmark createdAisleBookmark = (new ObjectMapper())
                                    .readValue(jsonArray, AisleBookmark.class);
                            mIsDirty = false;
                            Editor editor = mSharedPreferencesObj.edit();
                            editor.putBoolean(VueConstants.IS_AISLE_DIRTY,
                                    false);
                            editor.commit();
                            ArrayList<AisleWindowContent> windowList;
                            if (aisleBookmark.getBookmarked()) {
                                windowList = DataBaseManager.getInstance(
                                        VueApplication.getInstance())
                                        .getAisleByAisleId(
                                                Long.toString(aisleBookmark
                                                        .getAisleId()));
                            } else {
                                windowList = DataBaseManager.getInstance(
                                        VueApplication.getInstance())
                                        .getAisleByAisleIdFromBookmarks(
                                                Long.toString(aisleBookmark
                                                        .getAisleId()));
                            }
                            updateBookmartToDb(windowList,
                                    createdAisleBookmark, mIsDirty);
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
                    Editor editor = mSharedPreferencesObj.edit();
                    editor.putBoolean(VueConstants.IS_AISLE_DIRTY, true);
                    editor.commit();
                    ArrayList<AisleWindowContent> windowList;
                    if (aisleBookmark.getBookmarked()) {
                        windowList = DataBaseManager.getInstance(
                                VueApplication.getInstance())
                                .getAisleByAisleId(
                                        Long.toString(aisleBookmark
                                                .getAisleId()));
                    } else {
                        windowList = DataBaseManager.getInstance(
                                VueApplication.getInstance())
                                .getAisleByAisleIdFromBookmarks(
                                        Long.toString(aisleBookmark
                                                .getAisleId()));
                    }
                    updateBookmartToDb(windowList, aisleBookmark, mIsDirty);
                }
                
            };
            @SuppressWarnings("unchecked")
            BookmarkPutRequest request = new BookmarkPutRequest(
                    bookmarkAisleAsString, listener, errorListener, url
                            + storedVueUser.getId());
            VueApplication.getInstance().getRequestQueue().add(request);
        } else {
            mIsDirty = true;
            Editor editor = mSharedPreferencesObj.edit();
            editor.putBoolean(VueConstants.IS_AISLE_DIRTY, true);
            editor.commit();
            ArrayList<AisleWindowContent> windowList;
            if (aisleBookmark.getBookmarked()) {
                windowList = DataBaseManager.getInstance(
                        VueApplication.getInstance()).getAisleByAisleId(
                        Long.toString(aisleBookmark.getAisleId()));
            } else {
                windowList = DataBaseManager.getInstance(
                        VueApplication.getInstance())
                        .getAisleByAisleIdFromBookmarks(
                                Long.toString(aisleBookmark.getAisleId()));
            }
            updateBookmartToDb(windowList, aisleBookmark, mIsDirty);
        }
        
    }
    
    /**
     * update book mark info to db if the aisle is bookmarked by the user
     * 
     * @param ArrayList
     *            <AisleWindowContent> windowList
     * @param AisleBookmark
     *            aisleBookmark
     * @param boolean isDirty if bookmark info is writing to db when there is no
     *        network then it should be true so that when network comes app
     *        should identify that this info needs to send to the server.
     * */
    public void updateBookmartToDb(ArrayList<AisleWindowContent> windowList,
            AisleBookmark aisleBookmark, boolean isDirty) {
        for (AisleWindowContent aisleWindow : windowList) {
            AisleContext context = aisleWindow.getAisleContext();
            DataBaseManager
                    .getInstance(VueApplication.getInstance())
                    .bookMarkOrUnBookmarkAisle(
                            aisleBookmark.getBookmarked(),
                            (aisleBookmark.getBookmarked()) ? context.mBookmarkCount + 1
                                    : context.mBookmarkCount - 1,
                            aisleBookmark.getId(),
                            Long.toString(aisleBookmark.getAisleId()), isDirty);
        }
    }
    
    public void updateRating(final ImageRating imageRating, final int likeCount)
            throws ClientProtocolException, IOException {
        String url;
        if (imageRating.mId == null) {
            url = UrlConstants.CREATE_RATING_RESTURL + "/";
        } else {
            url = UrlConstants.UPDATE_RATING_RESTURL + "/";
        }
        
        if (VueConnectivityManager.isNetworkConnected(VueApplication
                .getInstance())) {
            VueUser storedVueUser = null;
            try {
                storedVueUser = Utils.readUserObjectFromFile(
                        VueApplication.getInstance(),
                        VueConstants.VUE_APP_USEROBJECT__FILENAME);
            } catch (Exception e) {
                e.printStackTrace();
            }
            ObjectMapper mapper = new ObjectMapper();
            com.lateralthoughts.vue.domain.ImageRating imageRatingRequestObject = new com.lateralthoughts.vue.domain.ImageRating();
            imageRatingRequestObject.setId(imageRating.getId());
            imageRatingRequestObject.setAisleId(imageRating.getAisleId());
            imageRatingRequestObject.setImageId(imageRating.getImageId());
            imageRatingRequestObject.setLiked(imageRating.getLiked());
            imageRatingRequestObject.setLastModifiedTimestamp(imageRating
                    .getLastModifiedTimestamp());
            String imageRatingString = mapper
                    .writeValueAsString(imageRatingRequestObject);
            
            @SuppressWarnings("rawtypes")
            Response.Listener listener = new Response.Listener<String>() {
                
                @Override
                public void onResponse(String jsonArray) {
                    if (jsonArray != null) {
                        try {
                            ImageRating imgRating = (new ObjectMapper())
                                    .readValue(jsonArray, ImageRating.class);
                            Editor editor = mSharedPreferencesObj.edit();
                            editor.putBoolean(VueConstants.IS_IMAGE_DIRTY,
                                    false);
                            editor.commit();
                            AisleImageDetails aisleImageDetails = VueTrendingAislesDataModel
                                    .getInstance(VueApplication.getInstance())
                                    .getAisleImageForImageId(
                                            String.valueOf(imgRating.mImageId),
                                            String.valueOf(imgRating.mAisleId),
                                            false);
                            if (aisleImageDetails != null) {
                                if (imageRating.mId == null) {
                                    aisleImageDetails.mRatingsList
                                            .add(imgRating);
                                }
                            }
                            updateImageRatingToDb(imgRating, likeCount, false);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            
            Response.ErrorListener errorListener = new ErrorListener() {
                
                @Override
                public void onErrorResponse(VolleyError error) {
                    imageRating.mId = 0001L;
                    updateImageRatingToDb(imageRating, likeCount, true);
                    Editor editor = mSharedPreferencesObj.edit();
                    editor.putBoolean(VueConstants.IS_IMAGE_DIRTY, true);
                    editor.commit();
                }
                
            };
            @SuppressWarnings("unchecked")
            ImageRatingPutRequest request = new ImageRatingPutRequest(
                    imageRatingString, listener, errorListener, url
                            + storedVueUser.getId());
            VueApplication.getInstance().getRequestQueue().add(request);
        } else {
            imageRating.mId = 0001L;
            updateImageRatingToDb(imageRating, likeCount, true);
            Editor editor = mSharedPreferencesObj.edit();
            editor.putBoolean(VueConstants.IS_IMAGE_DIRTY, true);
            editor.commit();
        }
    }
    
    private void updateImageRatingToDb(ImageRating imgRating, int likeCount,
            boolean isDirty) {
        DataBaseManager.getInstance(VueApplication.getInstance())
                .addLikeOrDisLike(likeCount, isDirty, imgRating, true);
    }
}
