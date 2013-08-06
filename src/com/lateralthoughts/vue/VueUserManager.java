package com.lateralthoughts.vue;

import android.os.Bundle;
import android.util.Log;
import com.android.volley.*;
import com.android.volley.toolbox.HttpHeaderParser;

//FB imports
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//java util imports
import java.io.UnsupportedEncodingException;
import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

public class VueUserManager {
    public interface UserUpdateCallback{
        public void onUserUpdated(VueUser user);
    }

    private String VUE_API_BASE_URI = "https://1-java.vueapi-canary.appspot.com/";
    private String USER_CREATE_ENDPOINT = "api/usercreate/trial";
    private String GPLUS_USER_CREATE_ENDPOINT = "api/usercreate/googleplus";
    private String FB_USER_CREATE_ENDPOINT = "api/usercreate/facebook/";
    private String USER_UPDATE_FB = "api/userupdate/facebook/";

    public enum PreferredIdentityLayer{
        GPLUS,
        FB,
        DEVICE_ID,
        GPLUS_FB,
        ALL_IDS_AVAILABLE //always keep this enum the last. If you don't know what you are doing don't muck around in here
    }

    private static VueUserManager sUserManager = null;
    private VueUser mCurrentUser;

    private VueUserManager(){
        mCurrentUser = null;
    }

    public static VueUserManager getUserManager(){
        if(null == sUserManager)
            sUserManager = new VueUserManager();
        return sUserManager;
    }

    private void setCurrentUser(VueUser user){
        mCurrentUser = user;
    }

    //get the currently logged in VueUser object. Even if the user chose not to identify themselves we would have
    //created an empty VueUser object. In this case, the getIdentity() API will return DEVICE_ID to indicate that
    //this user is identified only by this device id.
    public VueUser getCurrentUser(){
        return mCurrentUser;
    }
    //create an unidentified VueUser object. This is an asynchronous API and needs to make a round trip
    //network call.
    //Usually this call cannot be invoked when mCurrentUser is set to a valid value. This is because we can only
    //have only current user at a time. When this call returns the UserUpdateCallback's onUserUpdated API will
    //be invoked and the VueUser object is created and set at that point.
    public void createUnidentifiedUser(final UserUpdateCallback callback){
        if(null != mCurrentUser || null == callback)
            throw new RuntimeException("Cannot call createFBIdentifiedUser when User is " +
                    "already available. Try the update APIs");

        Response.Listener listener = new Response.Listener<String>(){

            @Override
            public void onResponse(String jsonArray){
                if(null != jsonArray){
                    Log.e("Profiling", "Profiling : onResponse()");
                    VueUser user = new VueUser(null, null, null);
                    user.setUserIdentityMethod(PreferredIdentityLayer.DEVICE_ID);
                    VueUserManager.this.setCurrentUser(user);
                    callback.onUserUpdated(user);
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

    public void createFBIdentifiedUser(final GraphUser graphUser, final UserUpdateCallback callback){
        //lets throw an exception if the current user is not NULL.
        if(null != mCurrentUser)
            throw new RuntimeException("Cannot call createFBIdentifiedUser when User is " +
                                                    "already available. Try the update APIs");

        final VueUser user = parseGraphUserIntoVueUser(graphUser);
        Response.Listener listener = new Response.Listener<String>(){
            GraphUser graphUser;

            @Override
            public void onResponse(String jsonArray){
                if(null != jsonArray){
                    VueUserManager.this.setCurrentUser(user);
                    user.setUserIdentityMethod(PreferredIdentityLayer.FB);
                    callback.onUserUpdated(user);
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
        String requestUrl = VUE_API_BASE_URI + FB_USER_CREATE_ENDPOINT + user.getFBUserId();
        UserCreateRequest request = new UserCreateRequest(null, null, listener, errorListener);
        VueApplication.getInstance().getRequestQueue().add(request);

    }

    public void createGPlusIdentifiedUser(String idValue, UserUpdateCallback callback){

    }

    public void updateUnidentifiedUser(PreferredIdentityLayer newIdentity, String idValue, UserUpdateCallback callback){

    }

    private class UserCreateRequest extends Request<String> {
        // ... other methods go here
        private Map<String, String> mParams;
        Response.Listener<String> mListener;

        public UserCreateRequest(String param1, String param2, Response.Listener<String> listener, Response.ErrorListener errorListener) {
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
            Log.e("VueUser", "error = " + s);
        }
    }

    public void fetchUserDataFromLocalId(String id, final UserUpdateCallback callback){
        //https://1-java.vueapi-canary-development1.appspot.com/api/userget/id/5707702298738688

        String requestUrlBase = VUE_API_BASE_URI + "api/userget/id/" + id;
        String requestUrl = requestUrlBase;
        Response.Listener listener = new Response.Listener<String>(){
            @Override
            public void onResponse(String jsonString){
                if(null != jsonString){
                    Log.e("Vue App", "jsonString = " + jsonString);
                    VueUser user = parseVueUserInfo(jsonString);
                    callback.onUserUpdated(user);
                }
            }
        };
        Response.ErrorListener errorListener = new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("VueNetworkError","Vue encountered network operations error. Error = " + error.networkResponse);
            }
        };
        StringRequest vueRequest =
                new StringRequest(requestUrl, listener, errorListener ){
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        HashMap<String, String> headersMap = new HashMap<String, String>();
                        headersMap.put("Accept-Encoding", "gzip");
                        headersMap.put("Content-Type","application/json");
                        return headersMap;
                    }
                };

        VueApplication.getInstance().getRequestQueue().add(vueRequest);
    }

    private VueUser parseGraphUserIntoVueUser(GraphUser graphUser){
        if(null == graphUser){
            throw new NullPointerException("GraphUser was set to null in parseGraphUserIntoVueUser");
        }
        VueUser vueUser = null;
        try {
            String firstName = graphUser.getFirstName();
            String lastName = graphUser.getLastName();
            String birthday = graphUser.getBirthday();
            JSONObject innerObject = graphUser.getInnerJSONObject();
            String email = innerObject.getString("email");
            String username = innerObject.getString("username");
            vueUser = new VueUser(username, null, email);
            vueUser.setUsersName(firstName, lastName);
            vueUser.setBirthday(birthday);
        } catch (JSONException ex) {

        }
        return vueUser;
    }

    private VueUser parseVueUserInfo(String jsonString){
        VueUser user = null;
        //{"user":{"id":5707702298738688,"email":null,"firstName":null,"lastName":null,
        // "joinTime":1375333802621,"deviceId":null,"facebookId":"FACEBOOK_ID_UNKNOWN","
        // googlePlusId":"GOOGLE_PLUS_ID_UNKNOWN"}}

        if(null == jsonString)
            throw new NullPointerException("parseVueUserInfo invoked with null object");

        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            String userString = jsonObject.optString("user");
            JSONObject userObject = jsonObject.getJSONObject("user");
            String id = userObject.optString("id");
            String email = userObject.optString("email");
            String firstName = userObject.optString("firstName");
            String lastName = userObject.optString("lastName");
            String deviceId = userObject.optString("deviceId");
            user = new VueUser(null, null, email);
            user.setUserIdentityMethod(PreferredIdentityLayer.DEVICE_ID);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return user;
    }
}
