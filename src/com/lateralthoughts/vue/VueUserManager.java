package com.lateralthoughts.vue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.facebook.model.GraphUser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lateralthoughts.vue.logging.Logger;
import com.lateralthoughts.vue.parser.Parser;
import com.lateralthoughts.vue.utils.UrlConstants;
import com.lateralthoughts.vue.utils.UserCreateOrUpdateRequest;
import com.lateralthoughts.vue.utils.UserGetRequest;
import com.lateralthoughts.vue.utils.Utils;

public class VueUserManager {
    
    private Response.ErrorListener mCreateFBGetUserErrorListener,
            mCreateFBCreateUserErrorListener, mCreateFBUpdateUserErrorListener,
            mUpdateFBGetUserErrorListener, mUpdateFBUpdateUserErrorListener,
            mCreateGPGetUserErrorListener, mCreateGPCreateUserErrorListener,
            mCreateGPUpdateUserErrorListener, mUpdateGPGetUserErrorListener,
            mUpdateGPUpdateUserErrorListener;
    private int mRetryCountForCreateFbGetUser,
            mRetryCountForCreateFbCreateUser, mRetryCountForCreateFbUpdateUser,
            mRetryCountForUpdateFbGetUser, mRetryCountForUpdateFbUpdateUser,
            mRetryCountForCreateGPGetUser, mRetryCountForCreateGPCreateUser,
            mRetryCountForCreateGPUpdateUser, mRetryCountForUpdateGPGetUser,
            mRetryCountForUpdateGPUpdateUser;
    private String mCreateFbCreateUserReq, mCreateFbUpdateUserReq,
            mUpdateFbUpdateUserReq, mCreateGPCreateUserReq,
            mCreateGPUpdateUserReq, mUpdateGPUpdateUserReq;
    private static final int MAX_LOGIN_RETRY_COUNT = 5;
    private static final String SERVER_ERROR_MESG_FOR_MAX_RETRY = "We seem to be having problems reaching the server. Please try again later.";
    private static final int DELAY_TIME = 60000;
    
    public interface UserUpdateCallback {
        public void onUserUpdated(VueUser user);
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
    
    // get the currently logged in VueUser object. Even if the user chose not to
    // identify themselves we would have
    // created an empty VueUser object. In this case, the getIdentity() API will
    // return DEVICE_ID to indicate that
    // this user is identified only by this device id.
    public VueUser getCurrentUser() {
        return mCurrentUser;
    }
    
    // create an unidentified VueUser object. This is an asynchronous API and
    // needs to make a round trip
    // network call.
    // Usually this call cannot be invoked when mCurrentUser is set to a valid
    // value. This is because we can only
    // have only current user at a time. When this call returns the
    // UserUpdateCallback's onUserUpdated API will
    // be invoked and the VueUser object is created and set at that point.
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void createUnidentifiedUser(final String userInitals,
            final String deviceId, final UserUpdateCallback callback) {
        if (null != mCurrentUser || null == callback)
            throw new RuntimeException(
                    "Cannot call createFBIdentifiedUser when User is "
                            + "already available. Try the update APIs");
        Response.Listener listener = new Response.Listener<String>() {
            
            @Override
            public void onResponse(String jsonArray) {
                
                if (null != jsonArray) {
                    VueUser vueUser = new Parser().parseUserData(jsonArray);
                    if (vueUser != null) {
                        VueApplication.getInstance().setmUserInitials(
                                userInitals);
                        VueUserManager.this.setCurrentUser(vueUser);
                        VueApplication.getInstance()
                                .setmUserId(vueUser.getId());
                        VueApplication.getInstance().setmUserEmail(
                                vueUser.getEmail());
                        callback.onUserUpdated(vueUser);
                        try {
                            VueTrendingAislesDataModel.getInstance(
                                    VueApplication.getInstance())
                                    .dataObserver();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
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
                    }
                }
            }
            
        };
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                
            }
        };
        
        try {
            ObjectMapper mapper = new ObjectMapper();
            VueUser newUser = new VueUser();
            newUser.setFirstName(userInitals);
            newUser.setLastName("");
            newUser.setDeviceId(deviceId);
            SharedPreferences sharedPreferencesObj = VueApplication
                    .getInstance().getSharedPreferences(
                            VueConstants.SHAREDPREFERENCE_NAME, 0);
            newUser.setGcmRegistrationId(sharedPreferencesObj.getString(
                    VueConstants.GCM_REGISTRATION_ID, null));
            String userAsString = mapper.writeValueAsString(newUser);
            UserCreateOrUpdateRequest request = new UserCreateOrUpdateRequest(
                    userAsString, UrlConstants.USER_PUT_RESTURL, listener,
                    errorListener);
            VueApplication.getInstance().getRequestQueue().add(request);
        } catch (Exception e) {
            
        }
        
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void createFBIdentifiedUser(final String userProfileImageUrl,
            final GraphUser graphUser, final UserUpdateCallback callback) {
        // lets throw an exception if the current user is not NULL.
        if (null != mCurrentUser)
            throw new RuntimeException(
                    "Cannot call createFBIdentifiedUser when User is "
                            + "already available. Try the update APIs");
        final VueUser user = parseGraphUserIntoVueUser(graphUser, null);
        final Response.Listener listener = new Response.Listener<String>() {
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
                        callback.onUserUpdated(vueUser);
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
                    }
                }
            }
        };
        mCreateFBCreateUserErrorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mRetryCountForCreateFbCreateUser++;
                String errorMesg = "";
                if (error != null && error.networkResponse != null) {
                    errorMesg = "Status code : "
                            + error.networkResponse.statusCode;
                }
                writeToSdcard("After server login failure for create facebook create user: "
                        + new Date() + "???" + errorMesg + "???");
                if (mRetryCountForCreateFbCreateUser < MAX_LOGIN_RETRY_COUNT) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            UserCreateOrUpdateRequest request = new UserCreateOrUpdateRequest(
                                    mCreateFbCreateUserReq,
                                    UrlConstants.USER_PUT_RESTURL, listener,
                                    mCreateFBCreateUserErrorListener);
                            VueApplication.getInstance().getRequestQueue()
                                    .add(request);
                        }
                    }, DELAY_TIME);
                } else {
                    mRetryCountForCreateFbCreateUser = 0;
                    showServerMesgForMaxTries();
                }
            }
        };
        mCreateFBUpdateUserErrorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mRetryCountForCreateFbUpdateUser++;
                String errorMesg = "";
                if (error != null && error.networkResponse != null) {
                    errorMesg = "Status code : "
                            + error.networkResponse.statusCode;
                }
                writeToSdcard("After server login failure for create facebook update user: "
                        + new Date() + "???" + errorMesg);
                if (mRetryCountForCreateFbUpdateUser < MAX_LOGIN_RETRY_COUNT) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            UserCreateOrUpdateRequest request = new UserCreateOrUpdateRequest(
                                    mCreateFbUpdateUserReq,
                                    UrlConstants.USER_PUT_RESTURL, listener,
                                    mCreateFBUpdateUserErrorListener);
                            VueApplication.getInstance().getRequestQueue()
                                    .add(request);
                        }
                    }, DELAY_TIME);
                } else {
                    mRetryCountForCreateFbUpdateUser = 0;
                    showServerMesgForMaxTries();
                }
            }
        };
        final Response.Listener getListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String jsonArray) {
                if (null != jsonArray) {
                    VueUser vueUser = new Parser().parseUserData(jsonArray);
                    if (vueUser != null) {
                        vueUser.setUserImageURL(userProfileImageUrl);
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
                        mCreateFbUpdateUserReq = userAsString;
                        UserCreateOrUpdateRequest request = new UserCreateOrUpdateRequest(
                                userAsString, UrlConstants.USER_PUT_RESTURL,
                                listener, mCreateFBUpdateUserErrorListener);
                        VueApplication.getInstance().getRequestQueue()
                                .add(request);
                        
                    } else {
                        try {
                            user.setUserImageURL(userProfileImageUrl);
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
                            mCreateFbCreateUserReq = userAsString;
                            UserCreateOrUpdateRequest request = new UserCreateOrUpdateRequest(
                                    userAsString,
                                    UrlConstants.USER_PUT_RESTURL, listener,
                                    mCreateFBCreateUserErrorListener);
                            VueApplication.getInstance().getRequestQueue()
                                    .add(request);
                        } catch (Exception e) {
                            
                        }
                        
                    }
                } else {
                    try {
                        user.setUserImageURL(userProfileImageUrl);
                        SharedPreferences sharedPreferencesObj = VueApplication
                                .getInstance().getSharedPreferences(
                                        VueConstants.SHAREDPREFERENCE_NAME, 0);
                        user.setGcmRegistrationId(sharedPreferencesObj
                                .getString(VueConstants.GCM_REGISTRATION_ID,
                                        null));
                        ObjectMapper mapper = new ObjectMapper();
                        String userAsString = mapper.writeValueAsString(user);
                        mCreateFbCreateUserReq = userAsString;
                        UserCreateOrUpdateRequest request = new UserCreateOrUpdateRequest(
                                userAsString, UrlConstants.USER_PUT_RESTURL,
                                listener, mCreateFBCreateUserErrorListener);
                        VueApplication.getInstance().getRequestQueue()
                                .add(request);
                    } catch (Exception e) {
                        
                    }
                }
            }
        };
        mCreateFBGetUserErrorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mRetryCountForCreateFbGetUser++;
                String errorMesg = "";
                if (error != null && error.networkResponse != null) {
                    errorMesg = "Status code : "
                            + error.networkResponse.statusCode;
                }
                writeToSdcard("After server login failure for create facebook get user : "
                        + new Date() + "???" + errorMesg);
                if (mRetryCountForCreateFbGetUser < MAX_LOGIN_RETRY_COUNT) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            UserGetRequest userGetRequest = new UserGetRequest(
                                    UrlConstants.GET_USER_FACEBOOK_ID_RESTURL
                                            + user.getFacebookId(),
                                    getListener, mCreateFBGetUserErrorListener);
                            VueApplication.getInstance().getRequestQueue()
                                    .add(userGetRequest);
                        }
                    }, DELAY_TIME);
                } else {
                    mRetryCountForCreateFbGetUser = 0;
                    showServerMesgForMaxTries();
                }
            }
        };
        UserGetRequest userGetRequest = new UserGetRequest(
                UrlConstants.GET_USER_FACEBOOK_ID_RESTURL
                        + user.getFacebookId(), getListener,
                mCreateFBGetUserErrorListener);
        VueApplication.getInstance().getRequestQueue().add(userGetRequest);
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void createGooglePlusIdentifiedUser(
            final String userProfileImageUrl, final VueUser vueUser,
            final UserUpdateCallback callback) {
        // lets throw an exception if the current user is not NULL.
        if (null != mCurrentUser)
            throw new RuntimeException(
                    "Cannot call createFBIdentifiedUser when User is "
                            + "already available. Try the update APIs");
        
        final Response.Listener listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String jsonArray) {
                if (null != jsonArray) {
                    Logger.log("INFo", "GCMonGoogle+UserUpdate Response",
                            jsonArray);
                    VueUser vueUser2 = new Parser().parseUserData(jsonArray);
                    if (vueUser2 != null) {
                        if (VueApplication.getInstance().getmUserInitials() == null) {
                            VueApplication.getInstance().setmUserInitials(
                                    vueUser2.getFirstName());
                        }
                        VueApplication.getInstance().setmUserId(
                                vueUser2.getId());
                        VueApplication.getInstance().setmUserEmail(
                                vueUser2.getEmail());
                        VueApplication.getInstance().setmUserName(
                                vueUser2.getFirstName() + " "
                                        + vueUser2.getLastName());
                        VueUserManager.this.setCurrentUser(vueUser2);
                        callback.onUserUpdated(vueUser2);
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
                    }
                }
            }
        };
        mCreateGPCreateUserErrorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mRetryCountForCreateGPCreateUser++;
                String errorMesg = "";
                if (error != null && error.networkResponse != null) {
                    errorMesg = "Status code : "
                            + error.networkResponse.statusCode;
                }
                writeToSdcard("After server login failure for create google+ create user: "
                        + new Date() + "???" + errorMesg);
                if (mRetryCountForCreateGPCreateUser < MAX_LOGIN_RETRY_COUNT) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            UserCreateOrUpdateRequest request = new UserCreateOrUpdateRequest(
                                    mCreateGPCreateUserReq,
                                    UrlConstants.USER_PUT_RESTURL, listener,
                                    mCreateGPCreateUserErrorListener);
                            VueApplication.getInstance().getRequestQueue()
                                    .add(request);
                        }
                    }, DELAY_TIME);
                } else {
                    mRetryCountForCreateGPCreateUser = 0;
                    showServerMesgForMaxTries();
                }
            }
        };
        mCreateGPUpdateUserErrorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mRetryCountForCreateGPUpdateUser++;
                String errorMesg = "";
                if (error != null && error.networkResponse != null) {
                    errorMesg = "Status code : "
                            + error.networkResponse.statusCode;
                }
                writeToSdcard("After server login failure for create google+ update user: "
                        + new Date() + "???" + errorMesg);
                if (mRetryCountForCreateGPUpdateUser < MAX_LOGIN_RETRY_COUNT) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            UserCreateOrUpdateRequest request = new UserCreateOrUpdateRequest(
                                    mCreateGPUpdateUserReq,
                                    UrlConstants.USER_PUT_RESTURL, listener,
                                    mCreateGPUpdateUserErrorListener);
                            VueApplication.getInstance().getRequestQueue()
                                    .add(request);
                        }
                    }, DELAY_TIME);
                } else {
                    mRetryCountForCreateGPUpdateUser = 0;
                    showServerMesgForMaxTries();
                }
            }
        };
        final Response.Listener getListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String jsonArray) {
                if (null != jsonArray) {
                    VueUser vueUser1 = new Parser().parseUserData(jsonArray);
                    if (vueUser1 != null) {
                        vueUser1.setUserImageURL(userProfileImageUrl);
                        SharedPreferences sharedPreferencesObj = VueApplication
                                .getInstance().getSharedPreferences(
                                        VueConstants.SHAREDPREFERENCE_NAME, 0);
                        vueUser1.setGcmRegistrationId(sharedPreferencesObj
                                .getString(VueConstants.GCM_REGISTRATION_ID,
                                        null));
                        ObjectMapper mapper = new ObjectMapper();
                        String userAsString = null;
                        try {
                            userAsString = mapper.writeValueAsString(vueUser1);
                            Logger.log("INFo",
                                    "GCMonGoogle+UserUpdate Request",
                                    userAsString);
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }
                        mCreateGPUpdateUserReq = userAsString;
                        UserCreateOrUpdateRequest request = new UserCreateOrUpdateRequest(
                                userAsString, UrlConstants.USER_PUT_RESTURL,
                                listener, mCreateGPUpdateUserErrorListener);
                        VueApplication.getInstance().getRequestQueue()
                                .add(request);
                    } else {
                        try {
                            vueUser.setUserImageURL(userProfileImageUrl);
                            SharedPreferences sharedPreferencesObj = VueApplication
                                    .getInstance().getSharedPreferences(
                                            VueConstants.SHAREDPREFERENCE_NAME,
                                            0);
                            vueUser.setGcmRegistrationId(sharedPreferencesObj
                                    .getString(
                                            VueConstants.GCM_REGISTRATION_ID,
                                            null));
                            ObjectMapper mapper = new ObjectMapper();
                            String userAsString = mapper
                                    .writeValueAsString(vueUser);
                            mCreateGPCreateUserReq = userAsString;
                            UserCreateOrUpdateRequest request = new UserCreateOrUpdateRequest(
                                    userAsString,
                                    UrlConstants.USER_PUT_RESTURL, listener,
                                    mCreateGPCreateUserErrorListener);
                            VueApplication.getInstance().getRequestQueue()
                                    .add(request);
                        } catch (Exception e) {
                            
                        }
                    }
                } else {
                    try {
                        vueUser.setUserImageURL(userProfileImageUrl);
                        SharedPreferences sharedPreferencesObj = VueApplication
                                .getInstance().getSharedPreferences(
                                        VueConstants.SHAREDPREFERENCE_NAME, 0);
                        vueUser.setGcmRegistrationId(sharedPreferencesObj
                                .getString(VueConstants.GCM_REGISTRATION_ID,
                                        null));
                        ObjectMapper mapper = new ObjectMapper();
                        String userAsString = mapper
                                .writeValueAsString(vueUser);
                        mCreateGPCreateUserReq = userAsString;
                        UserCreateOrUpdateRequest request = new UserCreateOrUpdateRequest(
                                userAsString, UrlConstants.USER_PUT_RESTURL,
                                listener, mCreateGPCreateUserErrorListener);
                        VueApplication.getInstance().getRequestQueue()
                                .add(request);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                
            }
        };
        
        mCreateGPGetUserErrorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mRetryCountForCreateGPGetUser++;
                String errorMesg = "";
                if (error != null && error.networkResponse != null) {
                    errorMesg = "Status code : "
                            + error.networkResponse.statusCode;
                }
                writeToSdcard("After server login failure for create google+ get user : "
                        + new Date() + "???" + errorMesg);
                if (mRetryCountForCreateGPGetUser < MAX_LOGIN_RETRY_COUNT) {
                    new Handler().postDelayed(new Runnable() {
                        
                        @Override
                        public void run() {
                            UserGetRequest userGetRequest = new UserGetRequest(
                                    UrlConstants.GET_USER_GOOGLEPLUS_ID_RESTURL
                                            + vueUser.getGooglePlusId(),
                                    getListener, mCreateGPGetUserErrorListener);
                            VueApplication.getInstance().getRequestQueue()
                                    .add(userGetRequest);
                        }
                    }, DELAY_TIME);
                } else {
                    mRetryCountForCreateGPGetUser = 0;
                    showServerMesgForMaxTries();
                }
            }
        };
        UserGetRequest userGetRequest = new UserGetRequest(
                UrlConstants.GET_USER_GOOGLEPLUS_ID_RESTURL
                        + vueUser.getGooglePlusId(), getListener,
                mCreateGPGetUserErrorListener);
        VueApplication.getInstance().getRequestQueue().add(userGetRequest);
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void updateFBIdentifiedUser(final String userProfileImageUrl,
            final GraphUser graphUser, final VueUser vueUser,
            final UserUpdateCallback callback) {
        final VueUser user = parseGraphUserIntoVueUser(graphUser, vueUser);
        final Response.Listener listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String jsonArray) {
                if (null != jsonArray) {
                    VueUser vueUser1 = new Parser().parseUserData(jsonArray);
                    if (vueUser1 != null) {
                        if (VueApplication.getInstance().getmUserInitials() == null) {
                            VueApplication.getInstance().setmUserInitials(
                                    vueUser1.getFirstName());
                        }
                        VueApplication.getInstance().setmUserId(
                                vueUser1.getId());
                        VueApplication.getInstance().setmUserEmail(
                                vueUser1.getEmail());
                        VueApplication.getInstance().setmUserName(
                                vueUser1.getFirstName() + " "
                                        + vueUser1.getLastName());
                        VueUserManager.this.setCurrentUser(vueUser1);
                        callback.onUserUpdated(vueUser1);
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
                    }
                }
            }
        };
        
        mUpdateFBUpdateUserErrorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mRetryCountForUpdateFbUpdateUser++;
                String errorMesg = "";
                if (error != null && error.networkResponse != null) {
                    errorMesg = "Status code : "
                            + error.networkResponse.statusCode;
                }
                writeToSdcard("After server login failure for Update facebook update user: "
                        + new Date() + "???" + errorMesg);
                if (mRetryCountForUpdateFbUpdateUser < MAX_LOGIN_RETRY_COUNT) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            UserCreateOrUpdateRequest request = new UserCreateOrUpdateRequest(
                                    mUpdateFbUpdateUserReq,
                                    UrlConstants.USER_PUT_RESTURL, listener,
                                    mCreateFBUpdateUserErrorListener);
                            VueApplication.getInstance().getRequestQueue()
                                    .add(request);
                        }
                    }, DELAY_TIME);
                } else {
                    mRetryCountForUpdateFbUpdateUser = 0;
                    showServerMesgForMaxTries();
                }
            }
        };
        
        final Response.Listener getListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String jsonArray) {
                if (null != jsonArray) {
                    VueUser vueUser2 = new Parser().parseUserData(jsonArray);
                    if (vueUser2 != null) {
                        showToastForSwitchingUser("Facebook");
                        vueUser2.setUserImageURL(userProfileImageUrl);
                        SharedPreferences sharedPreferencesObj = VueApplication
                                .getInstance().getSharedPreferences(
                                        VueConstants.SHAREDPREFERENCE_NAME, 0);
                        vueUser2.setGcmRegistrationId(sharedPreferencesObj
                                .getString(VueConstants.GCM_REGISTRATION_ID,
                                        null));
                        ObjectMapper mapper = new ObjectMapper();
                        String userAsString = null;
                        try {
                            userAsString = mapper.writeValueAsString(vueUser2);
                        } catch (JsonProcessingException e) {
                        }
                        mUpdateFbUpdateUserReq = userAsString;
                        UserCreateOrUpdateRequest request = new UserCreateOrUpdateRequest(
                                userAsString, UrlConstants.USER_PUT_RESTURL,
                                listener, mUpdateFBUpdateUserErrorListener);
                        VueApplication.getInstance().getRequestQueue()
                                .add(request);
                        
                    } else {
                        try {
                            user.setUserImageURL(userProfileImageUrl);
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
                            mUpdateFbUpdateUserReq = userAsString;
                            UserCreateOrUpdateRequest request = new UserCreateOrUpdateRequest(
                                    userAsString,
                                    UrlConstants.USER_PUT_RESTURL, listener,
                                    mUpdateFBUpdateUserErrorListener);
                            VueApplication.getInstance().getRequestQueue()
                                    .add(request);
                        } catch (Exception e) {
                            
                        }
                        
                    }
                } else {
                    try {
                        user.setUserImageURL(userProfileImageUrl);
                        SharedPreferences sharedPreferencesObj = VueApplication
                                .getInstance().getSharedPreferences(
                                        VueConstants.SHAREDPREFERENCE_NAME, 0);
                        user.setGcmRegistrationId(sharedPreferencesObj
                                .getString(VueConstants.GCM_REGISTRATION_ID,
                                        null));
                        ObjectMapper mapper = new ObjectMapper();
                        String userAsString = mapper.writeValueAsString(user);
                        mUpdateFbUpdateUserReq = userAsString;
                        UserCreateOrUpdateRequest request = new UserCreateOrUpdateRequest(
                                userAsString, UrlConstants.USER_PUT_RESTURL,
                                listener, mUpdateFBUpdateUserErrorListener);
                        VueApplication.getInstance().getRequestQueue()
                                .add(request);
                    } catch (Exception e) {
                        
                    }
                    
                }
            }
        };
        mUpdateFBGetUserErrorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mRetryCountForUpdateFbGetUser++;
                String errorMesg = "";
                if (error != null && error.networkResponse != null) {
                    errorMesg = "Status code : "
                            + error.networkResponse.statusCode;
                }
                writeToSdcard("After server login failure for update facebook get user : "
                        + new Date() + "???" + errorMesg);
                if (mRetryCountForUpdateFbGetUser < MAX_LOGIN_RETRY_COUNT) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            UserGetRequest userGetRequest = new UserGetRequest(
                                    UrlConstants.GET_USER_FACEBOOK_ID_RESTURL
                                            + user.getFacebookId(),
                                    getListener, mUpdateFBGetUserErrorListener);
                            VueApplication.getInstance().getRequestQueue()
                                    .add(userGetRequest);
                        }
                    }, DELAY_TIME);
                } else {
                    mRetryCountForUpdateFbGetUser = 0;
                    showServerMesgForMaxTries();
                }
            }
        };
        UserGetRequest userGetRequest = new UserGetRequest(
                UrlConstants.GET_USER_FACEBOOK_ID_RESTURL
                        + user.getFacebookId(), getListener,
                mUpdateFBGetUserErrorListener);
        VueApplication.getInstance().getRequestQueue().add(userGetRequest);
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void updateGooglePlusIdentifiedUser(
            final String userProfileImageUrl, final VueUser vueUser,
            final UserUpdateCallback callback) {
        
        final Response.Listener listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String jsonArray) {
                if (null != jsonArray) {
                    VueUser vueUser1 = new Parser().parseUserData(jsonArray);
                    if (vueUser1 != null) {
                        if (VueApplication.getInstance().getmUserInitials() == null) {
                            VueApplication.getInstance().setmUserInitials(
                                    vueUser1.getFirstName());
                        }
                        VueApplication.getInstance().setmUserId(
                                vueUser1.getId());
                        VueApplication.getInstance().setmUserEmail(
                                vueUser1.getEmail());
                        VueApplication.getInstance().setmUserName(
                                vueUser1.getFirstName() + " "
                                        + vueUser1.getLastName());
                        VueUserManager.this.setCurrentUser(vueUser1);
                        VueUserManager.this.setCurrentUser(vueUser1);
                        callback.onUserUpdated(vueUser1);
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
                    }
                }
            }
        };
        
        mUpdateGPUpdateUserErrorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mRetryCountForUpdateGPUpdateUser++;
                String errorMesg = "";
                if (error != null && error.networkResponse != null) {
                    errorMesg = "Status code : "
                            + error.networkResponse.statusCode;
                }
                writeToSdcard("After server login failure for Update google+ update user: "
                        + new Date() + "???" + errorMesg);
                if (mRetryCountForUpdateGPUpdateUser < MAX_LOGIN_RETRY_COUNT) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            UserCreateOrUpdateRequest request = new UserCreateOrUpdateRequest(
                                    mUpdateGPUpdateUserReq,
                                    UrlConstants.USER_PUT_RESTURL, listener,
                                    mUpdateGPUpdateUserErrorListener);
                            VueApplication.getInstance().getRequestQueue()
                                    .add(request);
                        }
                    }, DELAY_TIME);
                } else {
                    mRetryCountForUpdateGPUpdateUser = 0;
                    showServerMesgForMaxTries();
                }
            }
        };
        
        final Response.Listener getListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String jsonArray) {
                if (null != jsonArray) {
                    VueUser vueUser2 = new Parser().parseUserData(jsonArray);
                    if (vueUser2 != null) {
                        showToastForSwitchingUser("G+");
                        vueUser2.setUserImageURL(userProfileImageUrl);
                        SharedPreferences sharedPreferencesObj = VueApplication
                                .getInstance().getSharedPreferences(
                                        VueConstants.SHAREDPREFERENCE_NAME, 0);
                        vueUser2.setGcmRegistrationId(sharedPreferencesObj
                                .getString(VueConstants.GCM_REGISTRATION_ID,
                                        null));
                        ObjectMapper mapper = new ObjectMapper();
                        String userAsString = null;
                        try {
                            userAsString = mapper.writeValueAsString(vueUser2);
                        } catch (JsonProcessingException e) {
                        }
                        mUpdateGPUpdateUserReq = userAsString;
                        UserCreateOrUpdateRequest request = new UserCreateOrUpdateRequest(
                                userAsString, UrlConstants.USER_PUT_RESTURL,
                                listener, mUpdateGPUpdateUserErrorListener);
                        VueApplication.getInstance().getRequestQueue()
                                .add(request);
                        
                    } else {
                        try {
                            vueUser.setUserImageURL(userProfileImageUrl);
                            SharedPreferences sharedPreferencesObj = VueApplication
                                    .getInstance().getSharedPreferences(
                                            VueConstants.SHAREDPREFERENCE_NAME,
                                            0);
                            vueUser.setGcmRegistrationId(sharedPreferencesObj
                                    .getString(
                                            VueConstants.GCM_REGISTRATION_ID,
                                            null));
                            ObjectMapper mapper = new ObjectMapper();
                            String userAsString = mapper
                                    .writeValueAsString(vueUser);
                            mUpdateGPUpdateUserReq = userAsString;
                            UserCreateOrUpdateRequest request = new UserCreateOrUpdateRequest(
                                    userAsString,
                                    UrlConstants.USER_PUT_RESTURL, listener,
                                    mUpdateGPUpdateUserErrorListener);
                            VueApplication.getInstance().getRequestQueue()
                                    .add(request);
                        } catch (Exception e) {
                            
                        }
                    }
                } else {
                    try {
                        vueUser.setUserImageURL(userProfileImageUrl);
                        SharedPreferences sharedPreferencesObj = VueApplication
                                .getInstance().getSharedPreferences(
                                        VueConstants.SHAREDPREFERENCE_NAME, 0);
                        vueUser.setGcmRegistrationId(sharedPreferencesObj
                                .getString(VueConstants.GCM_REGISTRATION_ID,
                                        null));
                        ObjectMapper mapper = new ObjectMapper();
                        String userAsString = mapper
                                .writeValueAsString(vueUser);
                        mUpdateGPUpdateUserReq = userAsString;
                        UserCreateOrUpdateRequest request = new UserCreateOrUpdateRequest(
                                userAsString, UrlConstants.USER_PUT_RESTURL,
                                listener, mUpdateGPUpdateUserErrorListener);
                        VueApplication.getInstance().getRequestQueue()
                                .add(request);
                    } catch (Exception e) {
                        
                    }
                }
            }
        };
        mUpdateGPGetUserErrorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mRetryCountForUpdateGPGetUser++;
                String errorMesg = "";
                if (error != null && error.networkResponse != null) {
                    errorMesg = "Status code : "
                            + error.networkResponse.statusCode;
                }
                writeToSdcard("After server login failure for update google+ get user : "
                        + new Date() + "???" + errorMesg);
                if (mRetryCountForUpdateGPGetUser < MAX_LOGIN_RETRY_COUNT) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            UserGetRequest userGetRequest = new UserGetRequest(
                                    UrlConstants.GET_USER_GOOGLEPLUS_ID_RESTURL
                                            + vueUser.getGooglePlusId(),
                                    getListener, mUpdateGPGetUserErrorListener);
                            VueApplication.getInstance().getRequestQueue()
                                    .add(userGetRequest);
                        }
                    }, DELAY_TIME);
                } else {
                    mRetryCountForUpdateGPGetUser = 0;
                    showServerMesgForMaxTries();
                }
            }
        };
        UserGetRequest userGetRequest = new UserGetRequest(
                UrlConstants.GET_USER_GOOGLEPLUS_ID_RESTURL
                        + vueUser.getGooglePlusId(), getListener,
                mUpdateGPGetUserErrorListener);
        VueApplication.getInstance().getRequestQueue().add(userGetRequest);
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void fetchUserDataFromLocalId(String id,
            final UserUpdateCallback callback) {
        String requestUrl = UrlConstants.GET_USER_RESTURL + id;
        Response.Listener listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String jsonString) {
                if (null != jsonString) {
                    VueUser user = new Parser().parseUserData(jsonString);
                    callback.onUserUpdated(user);
                }
            }
        };
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        };
        StringRequest vueRequest = new StringRequest(requestUrl, listener,
                errorListener) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headersMap = new HashMap<String, String>();
                headersMap.put("Accept-Encoding", "gzip");
                headersMap.put("Content-Type", "application/json");
                return headersMap;
            }
        };
        
        VueApplication.getInstance().getRequestQueue().add(vueRequest);
    }
    
    private VueUser parseGraphUserIntoVueUser(GraphUser graphUser,
            VueUser storedVueUser) {
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
            vueUser = new VueUser(null, email, firstName, lastName, null,
                    Utils.getDeviceId(), facebookId,
                    VueUser.DEFAULT_GOOGLEPLUS_ID, null);
            if (storedVueUser != null) {
                vueUser.setDeviceId(storedVueUser.getDeviceId());
                vueUser.setGooglePlusId(storedVueUser.getGooglePlusId());
                vueUser.setId(storedVueUser.getId());
                vueUser.setJoinTime(storedVueUser.getJoinTime());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return vueUser;
    }
    
    private void showToastForSwitchingUser(String accountName) {
        Toast.makeText(VueApplication.getInstance(),
                "You are now logged in using " + accountName, Toast.LENGTH_LONG)
                .show();
    }
    
    private void writeToSdcard(String message) {
        
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
    
    private void showServerMesgForMaxTries() {
        SharedPreferences sharedPreferencesObj = VueApplication.getInstance()
                .getSharedPreferences(VueConstants.SHAREDPREFERENCE_NAME, 0);
        Editor editor = sharedPreferencesObj.edit();
        editor.putBoolean(VueConstants.VUE_LOGIN, false);
        editor.putBoolean(VueConstants.FACEBOOK_LOGIN, false);
        editor.putBoolean(VueConstants.GOOGLEPLUS_LOGIN, false);
        editor.commit();
        Toast.makeText(VueApplication.getInstance(),
                SERVER_ERROR_MESG_FOR_MAX_RETRY, Toast.LENGTH_LONG).show();
        Intent i = new Intent(VueApplication.getInstance(),
                VueLoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle b = new Bundle();
        b.putBoolean(VueConstants.CANCEL_BTN_DISABLE_FLAG, false);
        b.putString(VueConstants.FROM_INVITEFRIENDS, null);
        b.putBoolean(VueConstants.FBLOGIN_FROM_DETAILS_SHARE, false);
        b.putBoolean(VueConstants.FROM_BEZELMENU_LOGIN, false);
        i.putExtras(b);
        VueApplication.getInstance().startActivity(i);
    }
}
