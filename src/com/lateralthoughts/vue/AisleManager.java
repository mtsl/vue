package com.lateralthoughts.vue;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.transform.ErrorListener;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lateralthoughts.vue.connectivity.DataBaseManager;
import com.lateralthoughts.vue.connectivity.VueConnectivityManager;
import com.lateralthoughts.vue.domain.Aisle;
import com.lateralthoughts.vue.domain.AisleBookmark;
import com.lateralthoughts.vue.domain.Image;
import com.lateralthoughts.vue.domain.VueImage;
import com.lateralthoughts.vue.user.VueUser;
import com.lateralthoughts.vue.utils.AddImageToAisleBackgroundThread;
import com.lateralthoughts.vue.utils.AisleCreationBackgroundThread;
import com.lateralthoughts.vue.utils.AisleUpdateBackgroundThread;
import com.lateralthoughts.vue.utils.DeleteImageFromAisle;
import com.lateralthoughts.vue.utils.UploadImageBackgroundThread;
import com.lateralthoughts.vue.utils.UrlConstants;
import com.lateralthoughts.vue.utils.Utils;

public class AisleManager {
    
    public interface AisleAddCallback {
        public void onAisleAdded(Aisle aisle, AisleContext aisleContext);
    }
    
    public interface AisleUpdateCallback {
        public void onAisleUpdated();
    }
    
    public interface ImageUploadCallback {
        public void onImageUploaded(String imageUrl, int width, int height);
    }
    
    public interface ImageAddedCallback {
        public void onImageAdded(String aisleId, String imageId,
                String lookingFor, String findAt, String size, String source,
                boolean fromDetailScreen);
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
            AisleUpdateCallback aisleUpdateCallback,
            boolean fromDetailsScreenFlag) {
        Thread t = new Thread(new AisleUpdateBackgroundThread(aisle,
                aisleUpdateCallback, fromDetailsScreenFlag));
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
    public void addImageToAisle(AisleContext aisleContext,
            final boolean fromDetailsScreenFlag, String imageId,
            String lookingfor, VueImage image,
            ImageAddedCallback imageAddedCallback) {
        if (null == image) {
            throw new RuntimeException(
                    "Can't create Aisle without a non null aisle object");
        }
        
        Thread t = new Thread(new AddImageToAisleBackgroundThread(aisleContext,
                image, fromDetailsScreenFlag, imageId, lookingfor,
                imageAddedCallback));
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
        if (VueConnectivityManager.isNetworkConnected(VueApplication
                .getInstance())) {
            new Thread(new Runnable() {
                
                @Override
                public void run() {
                    VueUser storedVueUser = null;
                    try {
                        storedVueUser = Utils.readUserObjectFromFile(
                                VueApplication.getInstance(),
                                VueConstants.VUE_APP_USEROBJECT__FILENAME);
                        if (aisleBookmark.getId() == null) {
                            testCreateAisleBookmark(aisleBookmark,
                                    storedVueUser.getId());
                        } else {
                            
                            testUpdateAisleBookmark(aisleBookmark,
                                    storedVueUser.getId());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    
                }
            }).start();
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
       // updateImageRatingVolley(   imageRating,   likeCount);
        if (VueConnectivityManager.isNetworkConnected(VueApplication
                .getInstance())) {
            ObjectMapper mapper = new ObjectMapper();
            com.lateralthoughts.vue.domain.ImageRating imageRatingRequestObject = new com.lateralthoughts.vue.domain.ImageRating();
            imageRatingRequestObject.setId(imageRating.getId());
            imageRatingRequestObject.setAisleId(imageRating.getAisleId());
            imageRatingRequestObject.setImageId(imageRating.getImageId());
            imageRatingRequestObject.setLiked(imageRating.getLiked());
            imageRatingRequestObject.setLastModifiedTimestamp(imageRating
                    .getLastModifiedTimestamp());
            final String imageRatingString = mapper
                    .writeValueAsString(imageRatingRequestObject);
            try {
                final VueUser storedVueUser = Utils.readUserObjectFromFile(
                        VueApplication.getInstance(),
                        VueConstants.VUE_APP_USEROBJECT__FILENAME);
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        imageRatingPutRequest(imageRating, imageRatingString,
                                storedVueUser.getId(), likeCount);
                    }
                }).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if(imageRating.getId() == null) {
            imageRating.mId = 0001L;
            }
            updateImageRatingToDb(imageRating, likeCount, true);
            Editor editor = mSharedPreferencesObj.edit();
            editor.putBoolean(VueConstants.IS_IMAGE_DIRTY, true);
            editor.commit();
        }
    }
 //TODO: VOLLEY CODE NOT WORKING NEEDS TO BE TESTED.
    private void updateImageRatingVolley(final ImageRating imageRating, final int likeCount)
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
                            Log.i("volley test image", "volley test image sucees: "+imgRating.getId());
                            updateImageRatingToDb(imgRating, likeCount, false);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            };

            Response.ErrorListener errorListener = new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.i("volley test image", "volley test image failure response: "+error.toString());
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
        Log.e("NetworkStateChangeReciver", "VueConstants.IS_IMAGE_DIRTY  updateImageRatingToDb(): imgRating.mId = "
                        + imgRating.mId + ", imgRating.mAisleId = " + imgRating.mAisleId + ", isDirty = " + isDirty);
        DataBaseManager.getInstance(VueApplication.getInstance())
                .addLikeOrDisLike(likeCount, isDirty, imgRating, true, isDirty);
    }
    
    public AisleBookmark testCreateAisleBookmark(AisleBookmark bookmark,
            Long long1) throws Exception {
        AisleBookmark createdAisleBookmark = null;
        ObjectMapper mapper = new ObjectMapper();
        String bookmarkUrl = UrlConstants.CREATE_BOOKMARK_RESTURL;
        if (bookmark.getId() != null) {
            bookmarkUrl = UrlConstants.UPDATE_BOOKMARK_RESTURL;
        }
        URL url = new URL(bookmarkUrl + "/" + long1);
        HttpPut httpPut = new HttpPut(url.toString());
        StringEntity entity = new StringEntity(
                mapper.writeValueAsString(bookmark));
        entity.setContentType("application/json;charset=UTF-8");
        entity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,
                "application/json;charset=UTF-8"));
        httpPut.setEntity(entity);
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpResponse response = httpClient.execute(httpPut);
        if (response.getEntity() != null
                && response.getStatusLine().getStatusCode() == 200) {
            String responseMessage = EntityUtils.toString(response.getEntity());
            System.out.println("Response: " + responseMessage);
            if (responseMessage.length() > 0) {
                createdAisleBookmark = (new ObjectMapper()).readValue(
                        responseMessage, AisleBookmark.class);
                onSuccesfulBookmarkResponse(createdAisleBookmark);
                
            } else {
                onFailureBookmarkResponse(bookmark);
            }
            
        } else {
            onFailureBookmarkResponse(bookmark);
        }
        return createdAisleBookmark;
    }
    
    public AisleBookmark testUpdateAisleBookmark(AisleBookmark bookmark,
            Long long1) throws Exception {
        AisleBookmark updatedAisleBookmark = null;
        ObjectMapper mapper = new ObjectMapper();
        String bookmarkUrl = UrlConstants.CREATE_BOOKMARK_RESTURL;
        if (bookmark.getId() != null) {
            bookmarkUrl = UrlConstants.UPDATE_BOOKMARK_RESTURL;
        }
        URL url = new URL(bookmarkUrl + "/" + long1);
        HttpPut httpPut = new HttpPut(url.toString());
        StringEntity entity = new StringEntity(
                mapper.writeValueAsString(bookmark));
        entity.setContentType("application/json;charset=UTF-8");
        entity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,
                "application/json;charset=UTF-8"));
        httpPut.setEntity(entity);
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpResponse response = httpClient.execute(httpPut);
        if (response.getEntity() != null
                && response.getStatusLine().getStatusCode() == 200) {
            String responseMessage = EntityUtils.toString(response.getEntity());
            if (responseMessage.length() > 0) {
                updatedAisleBookmark = (new ObjectMapper()).readValue(
                        responseMessage, AisleBookmark.class);
                onSuccesfulBookmarkResponse(updatedAisleBookmark);
            } else {
                onFailureBookmarkResponse(bookmark);
            }
        } else {
            onFailureBookmarkResponse(bookmark);
        }
        return updatedAisleBookmark;
    }
    
    private void onSuccesfulBookmarkResponse(AisleBookmark createdAisleBookmark) {
        try {
            mIsDirty = false;
            Editor editor = mSharedPreferencesObj.edit();
            editor.putBoolean(VueConstants.IS_AISLE_DIRTY, false);
            editor.commit();
            ArrayList<AisleWindowContent> windowList;
            if (createdAisleBookmark.getBookmarked()) {
                windowList = DataBaseManager.getInstance(
                        VueApplication.getInstance()).getAisleByAisleId(
                        Long.toString(createdAisleBookmark.getAisleId()));
            } else {
                windowList = DataBaseManager
                        .getInstance(VueApplication.getInstance())
                        .getAisleByAisleIdFromBookmarks(
                                Long.toString(createdAisleBookmark.getAisleId()));
            }
            updateBookmartToDb(windowList, createdAisleBookmark, mIsDirty);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void onFailureBookmarkResponse(AisleBookmark aisleBookmark) {
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
    
    private void imageRatingPutRequest(ImageRating imageRating,
            String ratingString, Long userId, final int likeCount) {
        try {
            String ratingUrl = UrlConstants.CREATE_RATING_RESTURL;
            if (imageRating.getId() != null) {
                ratingUrl = UrlConstants.UPDATE_RATING_RESTURL;
            }
            URL url = new URL(ratingUrl + "/" + userId);
            HttpPut httpPut = new HttpPut(url.toString());
            StringEntity entity = new StringEntity(ratingString);
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
                Log.e("NetworkStateChangeReciver", "VueConstants.IS_IMAGE_DIRTY succes Responce: " + responseMessage);
                if (responseMessage != null && responseMessage.length() > 0) {
                    Log.i("volley test image", "volley test image success normal thread response: ");
                    try {
                        ImageRating imgRating = (new ObjectMapper()).readValue(
                                responseMessage, ImageRating.class);
                        Editor editor = mSharedPreferencesObj.edit();
                        editor.putBoolean(VueConstants.IS_IMAGE_DIRTY, false);
                        editor.commit();
                        AisleImageDetails aisleImageDetails = VueTrendingAislesDataModel
                                .getInstance(VueApplication.getInstance())
                                .getAisleImageForImageId(
                                        String.valueOf(imgRating.mImageId),
                                        String.valueOf(imgRating.mAisleId),
                                        false);
                        if (aisleImageDetails != null) {
                            if (imageRating.mId == null) {
                                aisleImageDetails.mRatingsList.add(imgRating);
                            }
                        }
                        updateImageRatingToDb(imgRating, likeCount, false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    
                } else {
                    imageRating.mId = 0001L;
                    updateImageRatingToDb(imageRating, likeCount, true);
                    Editor editor = mSharedPreferencesObj.edit();
                    editor.putBoolean(VueConstants.IS_IMAGE_DIRTY, true);
                    editor.commit();
                }
            } else {
                imageRating.mId = 0001L;
                updateImageRatingToDb(imageRating, likeCount, true);
                Editor editor = mSharedPreferencesObj.edit();
                editor.putBoolean(VueConstants.IS_IMAGE_DIRTY, true);
                editor.commit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
