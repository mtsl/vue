package com.lateralthoughts.vue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
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
    
    private ObjectMapper mObjectMapper;
    
    public interface AisleUpdateCallback {
        public void onAisleUpdated(String id, String imageId);
    }
    
    public interface ImageUploadCallback {
        public void onImageUploaded(String imageUrl);
    }
    
    public interface ImageAddedCallback {
        public void onImageAdded(String imageId);
    }
    
    // private static String VUE_API_BASE_URI =
    // "http://2-java.vueapi-canary-development1.appspot.com/";
    
    // private String VUE_API_BASE_URI = "https://vueapi-canary.appspot.com/";
    // private static String CREATE_AISLE_ENDPOINT = "api/aislecreate";
    private String CREATE_IMAGE_ENDPOINT = "imagecreate";
    private static AisleManager sAisleManager = null;
    private VueUser mCurrentUser;
    private boolean isDirty;
    private SharedPreferences mSharedPreferencesObj;
    
    private AisleManager() {
        mSharedPreferencesObj = VueApplication.getInstance()
                .getSharedPreferences(VueConstants.SHAREDPREFERENCE_NAME, 0);
        mObjectMapper = new ObjectMapper();
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
            final AisleUpdateCallback callback) {
        Thread t = new Thread(
                new AisleCreationBackgroundThread(aisle, callback));
        t.start();
    }
    
    public void updateAisle(final Aisle aisle) {
        Thread t = new Thread(new AisleUpdateBackgroundThread(aisle));
        t.start();
    }
    
    public void deleteImage(final Image image, String aisleId) {
        Thread t = new Thread(new DeleteImageFromAisle(image, aisleId));
        t.start();
    }
    
    private AisleContext parseAisleContent(JSONObject user) {
        AisleContext aisle = null;
        
        return aisle;
        
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
    
    private class AislePutRequest extends Request<String> {
        // ... other methods go here
        private Map<String, String> mParams;
        Response.Listener<String> mListener;
        private String mAisleAsString;
        private StringEntity mEntity;
        
        public AislePutRequest(String aisleAsString,
                Response.Listener<String> listener,
                Response.ErrorListener errorListener, String url) {
            super(Method.PUT, url, errorListener);
            mListener = listener;
            mAisleAsString = aisleAsString;
            try {
                mEntity = new StringEntity(mAisleAsString);
            } catch (UnsupportedEncodingException ex) {
            }
        }
        
        @Override
        public String getBodyContentType() {
            return mEntity.getContentType().getValue();
        }
        
        @Override
        public byte[] getBody() throws AuthFailureError {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try {
                mEntity.writeTo(bos);
            } catch (IOException e) {
                VolleyLog.e("IOException writing to ByteArrayOutputStream");
            }
            return bos.toByteArray();
        }
        
        @Override
        public Map<String, String> getHeaders() {
            HashMap<String, String> headersMap = new HashMap<String, String>();
            headersMap.put("Content-Type", "application/json");
            return headersMap;
        }
        
        @Override
        protected Response<String> parseNetworkResponse(NetworkResponse response) {
            String parsed;
            try {
                parsed = new String(response.data,
                        HttpHeaderParser.parseCharset(response.headers));
            } catch (UnsupportedEncodingException e) {
                parsed = new String(response.data);
            }
            return Response.success(parsed,
                    HttpHeaderParser.parseCacheHeaders(response));
        }
        
        @Override
        protected void deliverResponse(String s) {
            mListener.onResponse(s);
        }
    }
    
    public String testCreateAisle(Aisle aisle) throws Exception {
        Aisle createdAisle = null;
        ObjectMapper mapper = new ObjectMapper();
        String responseMessage = null;
        URL url = new URL(UrlConstants.CREATE_AISLE_RESTURL);
        HttpPut httpPut = new HttpPut(url.toString());
        StringEntity entity = new StringEntity(mapper.writeValueAsString(aisle));
        System.out.println("Aisle create request: "
                + mapper.writeValueAsString(aisle));
        entity.setContentType("application/json;charset=UTF-8");
        entity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,
                "application/json;charset=UTF-8"));
        httpPut.setEntity(entity);
        
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpResponse response = httpClient.execute(httpPut);
        if (response.getEntity() != null
                && response.getStatusLine().getStatusCode() == 200) {
            responseMessage = EntityUtils.toString(response.getEntity());
        } else {
        }
        return responseMessage;
        
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
        isDirty = true;
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
            
            Response.Listener listener = new Response.Listener<String>() {
                
                @Override
                public void onResponse(String jsonArray) {
                    if (jsonArray != null) {
                        try {
                            AisleBookmark createdAisleBookmark = (new ObjectMapper())
                                    .readValue(jsonArray, AisleBookmark.class);
                            isDirty = false;
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
                                    createdAisleBookmark, isDirty);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                
            };
            
            Response.ErrorListener errorListener = new ErrorListener() {
                
                @Override
                public void onErrorResponse(VolleyError error) {
                    isDirty = true;
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
                    updateBookmartToDb(windowList, aisleBookmark, isDirty);
                }
                
            };
            BookmarkPutRequest request = new BookmarkPutRequest(
                    bookmarkAisleAsString, listener, errorListener, url
                            + storedVueUser.getId());
            VueApplication.getInstance().getRequestQueue().add(request);
        } else {
            isDirty = true;
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
            updateBookmartToDb(windowList, aisleBookmark, isDirty);
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
        if (imageRating.getId() == null) {
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
            String imageRatingString = mapper.writeValueAsString(imageRating);
            
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
                    imageRating.setId(0001L);
                    updateImageRatingToDb(imageRating, likeCount, true);
                    Editor editor = mSharedPreferencesObj.edit();
                    editor.putBoolean(VueConstants.IS_IMAGE_DIRTY, true);
                    editor.commit();
                }
                
            };
            ImageRatingPutRequest request = new ImageRatingPutRequest(
                    imageRatingString, listener, errorListener, url
                            + storedVueUser.getId());
            VueApplication.getInstance().getRequestQueue().add(request);
        } else {
            imageRating.setId(0001L);
            updateImageRatingToDb(imageRating, likeCount, true);
            Editor editor = mSharedPreferencesObj.edit();
            editor.putBoolean(VueConstants.IS_IMAGE_DIRTY, true);
            editor.commit();
        }
    }
    
    private void updateImageRatingToDb(ImageRating imgRating, int likeCount,
            boolean isDirty) {
        DataBaseManager.getInstance(VueApplication.getInstance())
                .addLikeOrDisLike((imgRating.getLiked()) ? 1 : 0, likeCount,
                        imgRating.getId(),
                        Long.toString(imgRating.getImageId()),
                        Long.toString(imgRating.getAisleId()), isDirty);
    }
}
