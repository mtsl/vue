package com.lateralthoughts.vue;

import android.os.Bundle;
import android.util.Log;
import com.android.volley.*;
import org.json.JSONArray;
import com.android.volley.Response.Listener;
import com.android.volley.Response.ErrorListener;
import com.android.volley.toolbox.HttpHeaderParser;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.io.UnsupportedEncodingException;

/**
 * UserCredentials in Vue involves creating a Vue User account on the backend to which we can associate identity
 * layers such as FB id, G+ id, etc.
 * A Vue User may choose to not provide their identity (FB, G+) as they begin exploring the app but may decide to
 * login with a specific id at a later point in time.
 *
 *
 */

public class VueUser {
    public enum PreferredIdentityLayer{
        GPLUS,
        FB,
        DEVICE_ID,
        GPLUS_FB,
        ALL_IDS_AVAILABLE //always keep this enum the last. If you don't know what you are doing don't muck around in here
    }

    private String VUE_API_BASE_URI = " https://1-java.vueapi-canary.appspot.com/";
    private String USER_CREATE_ENDPOINT = "api/usercreate/trial";
    private String GPLUS_USER_CREATE_ENDPOINT = "api/usercreate/googleplus";
    private String FB_USER_CREATE_ENDPOINT = "api/usercreate/facebook";
    private String USER_UPDATE_FB = "api/userupdate/facebook";

    public VueUser(String facebookId, String googlePlusId, String emailId){
        mEmailId = emailId;
    }

    public void setUsersName(String firstName, String lastName){

    }

    public void setBirthday(Date birthday){

    }

    public void constructUnidentifiedUser(){
        Response.Listener listener = new Response.Listener<String>(){
            @Override
            public void onResponse(String jsonArray){
                if(null != jsonArray){
                    Log.e("Profiling", "Profiling : onResponse()");
                    Bundle responseBundle = new Bundle();
                    responseBundle.putString("result",jsonArray);
                }
            }
        };
        Response.ErrorListener errorListener = new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                if(null != error.networkResponse && null != error.networkResponse.data){
                    String errorData = error.networkResponse.data.toString();
                    Log.e("VueUserDebug","error date = " + errorData);
                }
            }
        };
        String requestUrl = VUE_API_BASE_URI + USER_CREATE_ENDPOINT;
        UserCreateRequest request = new UserCreateRequest(null, null, listener, errorListener);
        VueApplication.getInstance().getRequestQueue().add(request);
    }

    public interface UserUpdateCallback{
        public void onUserUpdated();
    }
    public void createUnidentifiedUser(UserUpdateCallback callback){

    }

    public void createFBIdentifiedUser(String idValue, UserUpdateCallback callback){

    }

    public void createGPlusIdentifiedUser(String idValue, UserUpdateCallback callback){

    }

    public void updateUnidentifiedUser(PreferredIdentityLayer newIdentity, String idValue, UserUpdateCallback callback){

    }

    private class UserCreateRequest extends Request<String>{
        // ... other methods go here
        private Map<String, String> mParams;
        Listener<String> mListener;

        public UserCreateRequest(String param1, String param2, Listener<String> listener, ErrorListener errorListener) {
            super(Request.Method.PUT, VUE_API_BASE_URI+USER_CREATE_ENDPOINT, errorListener);
            mListener = listener;
        }

        @Override
        public Map<String, String> getHeaders() {
            HashMap<String, String> headersMap = new HashMap<String, String>();
            headersMap.put("Content-Type","application/json");
            headersMap.put("Content-length","0");
            return headersMap;
        }

        @Override
        protected Response<String> parseNetworkResponse(NetworkResponse response) {
            String parsed;
            try {
                parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            } catch (UnsupportedEncodingException e) {
                parsed = new String(response.data);
            }
            return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response));
        }

        @Override
        protected void deliverResponse(String s) {
            mListener.onResponse(s);
            Log.e("VueUser","error = " + s);
        }
    }

    private Date mBirthday;
    private String mFacebookId;
    private String mGooglePlusId;
    private String mFirstName;
    private String mLastName;
    private Locale mUserLocale;
    private String mEmailId;

}
