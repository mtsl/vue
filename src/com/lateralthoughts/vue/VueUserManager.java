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

import android.content.SharedPreferences;
import android.os.Environment;
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
                    userAsString, UrlConstants.CREATE_USER_RESTURL, listener,
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
        final Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String errorMesg = "";
                if (error != null && error.getMessage() != null) {
                    errorMesg = error.getMessage();
                }
                writeToSdcard("After server login failure for facebook : "
                        + new Date() + "???" + errorMesg);
            }
        };
        Response.Listener getListener = new Response.Listener<String>() {
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
                        UserCreateOrUpdateRequest request = new UserCreateOrUpdateRequest(
                                userAsString, UrlConstants.UPDATE_USER_RESTURL,
                                listener, errorListener);
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
                            UserCreateOrUpdateRequest request = new UserCreateOrUpdateRequest(
                                    userAsString,
                                    UrlConstants.CREATE_USER_RESTURL, listener,
                                    errorListener);
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
                        UserCreateOrUpdateRequest request = new UserCreateOrUpdateRequest(
                                userAsString, UrlConstants.CREATE_USER_RESTURL,
                                listener, errorListener);
                        VueApplication.getInstance().getRequestQueue()
                                .add(request);
                    } catch (Exception e) {
                        
                    }
                    
                }
            }
        };
        Response.ErrorListener getErrorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String errorMesg = "";
                if (error != null && error.getMessage() != null) {
                    errorMesg = error.getMessage();
                }
                writeToSdcard("After server login failure for facebook: "
                        + new Date() + "???" + errorMesg);
            }
        };
        UserGetRequest userGetRequest = new UserGetRequest(
                UrlConstants.GET_USER_FACEBOOK_ID_RESTURL
                        + user.getFacebookId(), getListener, getErrorListener);
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
        
        final Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String errorMesg = "";
                if (error != null && error.getMessage() != null) {
                    errorMesg = error.getMessage();
                }
                writeToSdcard("After server login failure for google+: "
                        + new Date() + "????" + errorMesg);
            }
        };
        
        Response.Listener getListener = new Response.Listener<String>() {
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
                        UserCreateOrUpdateRequest request = new UserCreateOrUpdateRequest(
                                userAsString, UrlConstants.UPDATE_USER_RESTURL,
                                listener, errorListener);
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
                            UserCreateOrUpdateRequest request = new UserCreateOrUpdateRequest(
                                    userAsString,
                                    UrlConstants.CREATE_USER_RESTURL, listener,
                                    errorListener);
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
                        UserCreateOrUpdateRequest request = new UserCreateOrUpdateRequest(
                                userAsString, UrlConstants.CREATE_USER_RESTURL,
                                listener, errorListener);
                        VueApplication.getInstance().getRequestQueue()
                                .add(request);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                
            }
        };
        Response.ErrorListener getErrorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String errorMesg = "";
                if (error != null && error.getMessage() != null) {
                    errorMesg = error.getMessage();
                }
                writeToSdcard("After server login failure for google+: "
                        + new Date() + "????" + errorMesg);
            }
        };
        UserGetRequest userGetRequest = new UserGetRequest(
                UrlConstants.GET_USER_GOOGLEPLUS_ID_RESTURL
                        + vueUser.getGooglePlusId(), getListener,
                getErrorListener);
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
        final Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String errorMesg = "";
                if (error != null && error.getMessage() != null) {
                    errorMesg = error.getMessage();
                }
                writeToSdcard("After server login failure for facebook: "
                        + new Date() + "????" + errorMesg);
            }
        };
        
        Response.Listener getListener = new Response.Listener<String>() {
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
                        UserCreateOrUpdateRequest request = new UserCreateOrUpdateRequest(
                                userAsString, UrlConstants.UPDATE_USER_RESTURL,
                                listener, errorListener);
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
                            UserCreateOrUpdateRequest request = new UserCreateOrUpdateRequest(
                                    userAsString,
                                    UrlConstants.UPDATE_USER_RESTURL, listener,
                                    errorListener);
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
                        UserCreateOrUpdateRequest request = new UserCreateOrUpdateRequest(
                                userAsString, UrlConstants.UPDATE_USER_RESTURL,
                                listener, errorListener);
                        VueApplication.getInstance().getRequestQueue()
                                .add(request);
                    } catch (Exception e) {
                        
                    }
                    
                }
            }
        };
        Response.ErrorListener getErrorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String errorMesg = "";
                if (error != null && error.getMessage() != null) {
                    errorMesg = error.getMessage();
                }
                writeToSdcard("After server login failure for facebook: "
                        + new Date() + "???" + errorMesg);
            }
        };
        UserGetRequest userGetRequest = new UserGetRequest(
                UrlConstants.GET_USER_FACEBOOK_ID_RESTURL
                        + user.getFacebookId(), getListener, getErrorListener);
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
        
        final Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String errorMesg = "";
                if (error != null && error.getMessage() != null) {
                    errorMesg = error.getMessage();
                }
                writeToSdcard("After server login failure for google+: "
                        + new Date() + "???" + errorMesg);
            }
        };
        
        Response.Listener getListener = new Response.Listener<String>() {
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
                        UserCreateOrUpdateRequest request = new UserCreateOrUpdateRequest(
                                userAsString, UrlConstants.UPDATE_USER_RESTURL,
                                listener, errorListener);
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
                            UserCreateOrUpdateRequest request = new UserCreateOrUpdateRequest(
                                    userAsString,
                                    UrlConstants.UPDATE_USER_RESTURL, listener,
                                    errorListener);
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
                        UserCreateOrUpdateRequest request = new UserCreateOrUpdateRequest(
                                userAsString, UrlConstants.UPDATE_USER_RESTURL,
                                listener, errorListener);
                        VueApplication.getInstance().getRequestQueue()
                                .add(request);
                    } catch (Exception e) {
                        
                    }
                }
            }
        };
        
        Response.ErrorListener getErrorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String errorMesg = "";
                if (error != null && error.getMessage() != null) {
                    errorMesg = error.getMessage();
                }
                writeToSdcard("After server login failure for google+: "
                        + new Date() + "???" + errorMesg);
            }
        };
        UserGetRequest userGetRequest = new UserGetRequest(
                UrlConstants.GET_USER_GOOGLEPLUS_ID_RESTURL
                        + vueUser.getGooglePlusId(), getListener,
                getErrorListener);
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
        File file = new File(dir, "/"
                + Calendar.getInstance().get(Calendar.DATE)
                + "-"
                + Utils.getWeekDay(Calendar.getInstance().get(
                        Calendar.DAY_OF_WEEK)) + ".txt");
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
    
}
