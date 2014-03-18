package com.lateralthoughts.vue.user;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.SharedPreferences;
import android.os.Environment;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.facebook.model.GraphUser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lateralthoughts.vue.VueApplication;
import com.lateralthoughts.vue.VueConstants;
import com.lateralthoughts.vue.VueTrendingAislesDataModel;
import com.lateralthoughts.vue.logging.Logger;
import com.lateralthoughts.vue.parser.Parser;
import com.lateralthoughts.vue.utils.UrlConstants;
import com.lateralthoughts.vue.utils.Utils;

public class VueUserManager {
    
    public interface UserUpdateCallback {
        public void onUserUpdated(VueUser user, boolean loginSuccessFlag);
    }
    
    private static VueUserManager sUserManager = null;
    private VueUser mCurrentUser;
    
    private VueUserManager() {
        mCurrentUser = null;
    }
    
    public static VueUserManager getUserManager() {
        if (null == sUserManager)
            sUserManager = new VueUserManager();
        return sUserManager;
    }
    
    private void setCurrentUser(VueUser user) {
        mCurrentUser = user;
    }
    
    public VueUser getCurrentUser() {
        return mCurrentUser;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void facebookAuthenticationWithServer(
            final String userProfileImageUrl, final GraphUser graphUser,
            final UserUpdateCallback callback) {
        if (null != mCurrentUser)
            throw new RuntimeException(
                    "Cannot call createFBIdentifiedUser when User is "
                            + "already available. Try the update APIs");
        final VueUser user = parseGraphUserIntoVueUser(graphUser,
                userProfileImageUrl);
        final Response.Listener getListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String jsonArray) {
                if (null != jsonArray) {
                    VueUser vueUser = new Parser().parseUserData(jsonArray);
                    if (vueUser != null) {
                        if (VueApplication.getInstance().getmUserInitials() == null) {
                            VueApplication.getInstance().setmUserInitials(
                                    vueUser.getFirstName());
                        }
                        VueApplication.getInstance()
                                .setmUserId(vueUser.getId());
                        VueApplication.getInstance().setmUserEmail(
                                vueUser.getEmail());
                        VueApplication.getInstance().setmUserName(
                                vueUser.getFirstName() + " "
                                        + vueUser.getLastName());
                        VueUserManager.this.setCurrentUser(vueUser);
                        callback.onUserUpdated(vueUser, true);
                        VueApplication
                                .getInstance()
                                .getContentResolver()
                                .delete(VueConstants.RECENTLY_VIEW_AISLES_URI,
                                        null, null);
                        VueApplication
                                .getInstance()
                                .getContentResolver()
                                .delete(VueConstants.BOOKMARKER_AISLES_URI,
                                        null, null);
                        VueApplication
                                .getInstance()
                                .getContentResolver()
                                .delete(VueConstants.RATED_IMAGES_URI, null,
                                        null);
                        VueTrendingAislesDataModel
                                .getInstance(VueApplication.getInstance())
                                .getNetworkHandler().getBookmarkAisleByUser();
                        VueTrendingAislesDataModel
                                .getInstance(VueApplication.getInstance())
                                .getNetworkHandler().getRatedImageList();
                        SharedPreferences sharedPreferencesObj = VueApplication
                                .getInstance().getSharedPreferences(
                                        VueConstants.SHAREDPREFERENCE_NAME, 0);
                        vueUser.setGcmRegistrationId(sharedPreferencesObj
                                .getString(VueConstants.GCM_REGISTRATION_ID,
                                        null));
                        ObjectMapper mapper = new ObjectMapper();
                        String userAsString = null;
                        try {
                            userAsString = mapper.writeValueAsString(vueUser);
                        } catch (JsonProcessingException e) {
                        }
                        callUpdateUserThread(userAsString, callback);
                    } else {
                        try {
                            SharedPreferences sharedPreferencesObj = VueApplication
                                    .getInstance().getSharedPreferences(
                                            VueConstants.SHAREDPREFERENCE_NAME,
                                            0);
                            user.setGcmRegistrationId(sharedPreferencesObj
                                    .getString(
                                            VueConstants.GCM_REGISTRATION_ID,
                                            null));
                            ObjectMapper mapper = new ObjectMapper();
                            String userAsString = mapper
                                    .writeValueAsString(user);
                            callCreateUserThread(userAsString, callback);
                        } catch (Exception e) {
                            
                        }
                    }
                } else {
                    try {
                        SharedPreferences sharedPreferencesObj = VueApplication
                                .getInstance().getSharedPreferences(
                                        VueConstants.SHAREDPREFERENCE_NAME, 0);
                        user.setGcmRegistrationId(sharedPreferencesObj
                                .getString(VueConstants.GCM_REGISTRATION_ID,
                                        null));
                        ObjectMapper mapper = new ObjectMapper();
                        String userAsString = mapper.writeValueAsString(user);
                        callCreateUserThread(userAsString, callback);
                    } catch (Exception e) {
                        
                    }
                }
            }
        };
        Response.ErrorListener fBGetUserErrorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String errorMesg = "";
                if (error != null && error.networkResponse != null) {
                    errorMesg = "" + error.networkResponse.statusCode;
                }
                JSONObject loginprops = new JSONObject();
                try {
                    loginprops.put("Failure Reason",
                            "Unable to Login with Vue Server status code : "
                                    + errorMesg);
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
                writeToSdcard("After server failure login for Facebook get user : "
                        + new Date());
                callback.onUserUpdated(null, false);
            }
        };
        UserGetRequest userGetRequest = new UserGetRequest(
                UrlConstants.GET_USER_FACEBOOK_ID_RESTURL
                        + user.getFacebookId(), getListener,
                fBGetUserErrorListener);
        VueApplication.getInstance().getRequestQueue().add(userGetRequest);
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void googlePlusAuthenticationWithServer(
            final String userProfileImageUrl, final VueUser user,
            final UserUpdateCallback callback) {
        if (null != mCurrentUser)
            throw new RuntimeException(
                    "Cannot call createFBIdentifiedUser when User is "
                            + "already available. Try the update APIs");
        final Response.Listener getListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String jsonArray) {
                if (null != jsonArray) {
                    VueUser vueUser = new Parser().parseUserData(jsonArray);
                    if (vueUser != null) {
                        if (VueApplication.getInstance().getmUserInitials() == null) {
                            VueApplication.getInstance().setmUserInitials(
                                    vueUser.getFirstName());
                        }
                        VueApplication.getInstance()
                                .setmUserId(vueUser.getId());
                        VueApplication.getInstance().setmUserEmail(
                                vueUser.getEmail());
                        VueApplication.getInstance().setmUserName(
                                vueUser.getFirstName() + " "
                                        + vueUser.getLastName());
                        VueUserManager.this.setCurrentUser(vueUser);
                        callback.onUserUpdated(vueUser, true);
                        VueApplication
                                .getInstance()
                                .getContentResolver()
                                .delete(VueConstants.RECENTLY_VIEW_AISLES_URI,
                                        null, null);
                        VueApplication
                                .getInstance()
                                .getContentResolver()
                                .delete(VueConstants.BOOKMARKER_AISLES_URI,
                                        null, null);
                        VueApplication
                                .getInstance()
                                .getContentResolver()
                                .delete(VueConstants.RATED_IMAGES_URI, null,
                                        null);
                        VueTrendingAislesDataModel
                                .getInstance(VueApplication.getInstance())
                                .getNetworkHandler().getBookmarkAisleByUser();
                        VueTrendingAislesDataModel
                                .getInstance(VueApplication.getInstance())
                                .getNetworkHandler().getRatedImageList();
                        SharedPreferences sharedPreferencesObj = VueApplication
                                .getInstance().getSharedPreferences(
                                        VueConstants.SHAREDPREFERENCE_NAME, 0);
                        vueUser.setGcmRegistrationId(sharedPreferencesObj
                                .getString(VueConstants.GCM_REGISTRATION_ID,
                                        null));
                        ObjectMapper mapper = new ObjectMapper();
                        String userAsString = null;
                        try {
                            userAsString = mapper.writeValueAsString(vueUser);
                        } catch (JsonProcessingException e) {
                        }
                        callUpdateUserThread(userAsString, callback);
                    } else {
                        try {
                            SharedPreferences sharedPreferencesObj = VueApplication
                                    .getInstance().getSharedPreferences(
                                            VueConstants.SHAREDPREFERENCE_NAME,
                                            0);
                            user.setGcmRegistrationId(sharedPreferencesObj
                                    .getString(
                                            VueConstants.GCM_REGISTRATION_ID,
                                            null));
                            ObjectMapper mapper = new ObjectMapper();
                            String userAsString = mapper
                                    .writeValueAsString(user);
                            callCreateUserThread(userAsString, callback);
                        } catch (Exception e) {
                            
                        }
                    }
                } else {
                    try {
                        SharedPreferences sharedPreferencesObj = VueApplication
                                .getInstance().getSharedPreferences(
                                        VueConstants.SHAREDPREFERENCE_NAME, 0);
                        user.setGcmRegistrationId(sharedPreferencesObj
                                .getString(VueConstants.GCM_REGISTRATION_ID,
                                        null));
                        ObjectMapper mapper = new ObjectMapper();
                        String userAsString = mapper.writeValueAsString(user);
                        callCreateUserThread(userAsString, callback);
                    } catch (Exception e) {
                        
                    }
                }
            }
        };
        Response.ErrorListener gPlusGetUserErrorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String errorMesg = "";
                if (error != null && error.networkResponse != null) {
                    errorMesg = "" + error.networkResponse.statusCode;
                }
                JSONObject loginprops = new JSONObject();
                try {
                    loginprops.put("Failure Reason",
                            "Unable to Login with Vue Server status code : "
                                    + errorMesg);
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
                writeToSdcard("After server failure login for Google+ get user : "
                        + new Date());
                callback.onUserUpdated(null, false);
            }
        };
        UserGetRequest userGetRequest = new UserGetRequest(
                UrlConstants.GET_USER_GOOGLEPLUS_ID_RESTURL
                        + user.getGooglePlusId(), getListener,
                gPlusGetUserErrorListener);
        VueApplication.getInstance().getRequestQueue().add(userGetRequest);
    }
    
    private VueUser parseGraphUserIntoVueUser(GraphUser graphUser,
            String userProfileImageUrl) {
        if (null == graphUser) {
            throw new NullPointerException(
                    "GraphUser was set to null in parseGraphUserIntoVueUser");
        }
        VueUser vueUser = null;
        try {
            String firstName = graphUser.getFirstName();
            String lastName = graphUser.getLastName();
            JSONObject innerObject = graphUser.getInnerJSONObject();
            String email;
            try {
                email = innerObject
                        .getString(VueConstants.FACEBOOK_GRAPHIC_OBJECT_EMAIL_KEY);
            } catch (Exception e) {
                email = graphUser.getUsername();
                e.printStackTrace();
            }
            String facebookId = null;
            try {
                if (email != null) {
                    facebookId = email;
                    facebookId = facebookId.replace(".", "");
                }
            } catch (Exception e) {
                facebookId = email;
            }
            vueUser = new VueUser(null, email, firstName, lastName,
                    System.currentTimeMillis(), Utils.getDeviceId(),
                    facebookId, VueUser.DEFAULT_GOOGLEPLUS_ID,
                    userProfileImageUrl);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return vueUser;
    }
    
    private void writeToSdcard(String message) {
        if (!Logger.sWrightToSdCard) {
            return;
        }
        String path = Environment.getExternalStorageDirectory().toString();
        File dir = new File(path + "/vueLoginTimes/");
        if (!dir.isDirectory()) {
            dir.mkdir();
        }
        File file = new File(dir, "/" + "vueLoginTimes_"
                + (Calendar.getInstance().get(Calendar.MONTH) + 1) + "-"
                + Calendar.getInstance().get(Calendar.DATE) + "_"
                + Calendar.getInstance().get(Calendar.YEAR) + ".txt");
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(
                    new FileWriter(file, true)));
            out.write("\n" + message + "\n");
            out.flush();
            out.close();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void callUpdateUserThread(final String userAsString,
            final UserUpdateCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final String[] response = testUpdateUser(userAsString);
                    if (response[1].trim().equals("200")) {
                        if (null != response[0]) {
                            VueUser vueUser = new Parser()
                                    .parseUserData(response[0]);
                            if (vueUser != null) {
                                if (VueApplication.getInstance()
                                        .getmUserInitials() == null) {
                                    VueApplication.getInstance()
                                            .setmUserInitials(
                                                    vueUser.getFirstName());
                                }
                                VueApplication.getInstance().setmUserId(
                                        vueUser.getId());
                                VueApplication.getInstance().setmUserEmail(
                                        vueUser.getEmail());
                                VueApplication.getInstance().setmUserName(
                                        vueUser.getFirstName() + " "
                                                + vueUser.getLastName());
                                VueUserManager.this.setCurrentUser(vueUser);
                                callback.onUserUpdated(vueUser, true);
                                VueApplication
                                        .getInstance()
                                        .getContentResolver()
                                        .delete(VueConstants.RECENTLY_VIEW_AISLES_URI,
                                                null, null);
                                VueApplication
                                        .getInstance()
                                        .getContentResolver()
                                        .delete(VueConstants.BOOKMARKER_AISLES_URI,
                                                null, null);
                                VueApplication
                                        .getInstance()
                                        .getContentResolver()
                                        .delete(VueConstants.RATED_IMAGES_URI,
                                                null, null);
                                VueTrendingAislesDataModel
                                        .getInstance(
                                                VueApplication.getInstance())
                                        .getNetworkHandler()
                                        .getBookmarkAisleByUser();
                                VueTrendingAislesDataModel
                                        .getInstance(
                                                VueApplication.getInstance())
                                        .getNetworkHandler()
                                        .getRatedImageList();
                            } else {
                                callback.onUserUpdated(null, false);
                            }
                        } else {
                            callback.onUserUpdated(null, false);
                        }
                    } else {
                        JSONObject loginprops = new JSONObject();
                        try {
                            loginprops.put("Failure Reason",
                                    "Unable to Login with Vue Server status code : "
                                            + response[1]);
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                        callback.onUserUpdated(null, false);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    
    private void callCreateUserThread(final String userAsString,
            final UserUpdateCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final String[] response = testCreateUser(userAsString);
                    if (response[1].trim().equals("200")) {
                        if (null != response[0]) {
                            VueUser vueUser = new Parser()
                                    .parseUserData(response[0]);
                            if (vueUser != null) {
                                if (VueApplication.getInstance()
                                        .getmUserInitials() == null) {
                                    VueApplication.getInstance()
                                            .setmUserInitials(
                                                    vueUser.getFirstName());
                                }
                                VueApplication.getInstance().setmUserId(
                                        vueUser.getId());
                                VueApplication.getInstance().setmUserEmail(
                                        vueUser.getEmail());
                                VueApplication.getInstance().setmUserName(
                                        vueUser.getFirstName() + " "
                                                + vueUser.getLastName());
                                VueUserManager.this.setCurrentUser(vueUser);
                                callback.onUserUpdated(vueUser, true);
                                VueApplication
                                        .getInstance()
                                        .getContentResolver()
                                        .delete(VueConstants.RECENTLY_VIEW_AISLES_URI,
                                                null, null);
                                VueApplication
                                        .getInstance()
                                        .getContentResolver()
                                        .delete(VueConstants.BOOKMARKER_AISLES_URI,
                                                null, null);
                                VueApplication
                                        .getInstance()
                                        .getContentResolver()
                                        .delete(VueConstants.RATED_IMAGES_URI,
                                                null, null);
                                VueTrendingAislesDataModel
                                        .getInstance(
                                                VueApplication.getInstance())
                                        .getNetworkHandler()
                                        .getBookmarkAisleByUser();
                                VueTrendingAislesDataModel
                                        .getInstance(
                                                VueApplication.getInstance())
                                        .getNetworkHandler()
                                        .getRatedImageList();
                            } else {
                                callback.onUserUpdated(null, false);
                            }
                        } else {
                            callback.onUserUpdated(null, false);
                        }
                    } else {
                        JSONObject loginprops = new JSONObject();
                        try {
                            loginprops.put("Failure Reason",
                                    "Unable to Login with Vue Server status code : "
                                            + response[1]);
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                        callback.onUserUpdated(null, false);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    
    public String[] testUpdateUser(final String request) throws Exception {
        String responseArray[] = new String[2];
        responseArray[0] = "";
        responseArray[1] = "status code";
        try {
            URL url = new URL(UrlConstants.UPDATE_USER_RESTURL);
            HttpPut httpPut = new HttpPut(url.toString());
            DefaultHttpClient httpClient = new DefaultHttpClient();
            StringEntity entity = new StringEntity(request);
            entity.setContentType("application/json;charset=UTF-8");
            entity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,
                    "application/json;charset=UTF-8"));
            httpPut.setEntity(entity);
            
            HttpResponse response = httpClient.execute(httpPut);
            if (response.getEntity() != null
                    && response.getStatusLine().getStatusCode() == 200) {
                String responseMessage = EntityUtils.toString(response
                        .getEntity());
                if (responseMessage != null && responseMessage.length() > 0) {
                    responseArray = new String[2];
                    responseArray[0] = responseMessage;
                    responseArray[1] = response.getStatusLine().getStatusCode()
                            + "";
                    return responseArray;
                }
            }
            try {
                responseArray[1] = response.getStatusLine().getStatusCode()
                        + "";
            } catch (Exception e) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return responseArray;
        
    }
    
    public String[] testCreateUser(final String request) throws Exception {
        String responseArray[] = new String[2];
        responseArray[0] = "";
        responseArray[1] = "status code";
        try {
            URL url = new URL(UrlConstants.CREATE_USER_RESTURL);
            HttpPut httpPut = new HttpPut(url.toString());
            DefaultHttpClient httpClient = new DefaultHttpClient();
            StringEntity entity = new StringEntity(request);
            entity.setContentType("application/json;charset=UTF-8");
            entity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,
                    "application/json;charset=UTF-8"));
            httpPut.setEntity(entity);
            
            HttpResponse response = httpClient.execute(httpPut);
            if (response.getEntity() != null
                    && response.getStatusLine().getStatusCode() == 200) {
                String responseMessage = EntityUtils.toString(response
                        .getEntity());
                if (responseMessage != null && responseMessage.length() > 0) {
                    responseArray = new String[2];
                    responseArray[0] = responseMessage;
                    responseArray[1] = response.getStatusLine().getStatusCode()
                            + "";
                    return responseArray;
                }
            }
            try {
                responseArray[1] = response.getStatusLine().getStatusCode()
                        + "";
            } catch (Exception e) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return responseArray;
        
    }
}
