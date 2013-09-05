package com.lateralthoughts.vue;

import android.util.Log;
import com.android.volley.*;
import com.android.volley.toolbox.HttpHeaderParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lateralthoughts.vue.domain.Aisle;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
//FB imports
//java util imports

public class AisleManager {

    private ObjectMapper mObjectMapper;
    public interface AisleUpdateCallback {
        public void onAisleUpdated();
    }

    private String VUE_API_BASE_URI = "http://2-java.vueapi-canary-development1.appspot.com/";
    //private String VUE_API_BASE_URI = "https://vueapi-canary.appspot.com/";
    private String CREATE_AISLE_ENDPOINT = "api/aislecreate";

    private static AisleManager sAisleManager = null;
    private VueUser mCurrentUser;

    private AisleManager() {
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
    public void createEmptyAisle(Aisle aisle, final AisleUpdateCallback callback){
        if(null == aisle)
            throw new RuntimeException("Can't create Aisle without a non null aisle object");
        String aisleAsString = null;
        try{
            aisleAsString = mObjectMapper.writeValueAsString(aisle);
        }catch(JsonProcessingException ex2){

        }
        Response.Listener listener = new Response.Listener<String>() {

            @Override
            public void onResponse(String jsonArray) {
                if (null != jsonArray) {
                    Log.e("Profiling", "Profiling : onResponse()");
                    try{
                        JSONObject userInfo = new JSONObject(jsonArray);
                        JSONObject user = userInfo.getJSONObject("user");
                        callback.onAisleUpdated();
                    }catch(JSONException ex){

                    }
                }
            }
        };
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (null != error.networkResponse
                        && null != error.networkResponse.data) {
                    String errorData = error.networkResponse.data.toString();
                    Log.e("VueUserDebug", "error date = " + errorData);
                }
            }
        };
        String requestUrl = VUE_API_BASE_URI + CREATE_AISLE_ENDPOINT;
        AisleCreateRequest request = new AisleCreateRequest(aisleAsString, listener,
                errorListener);
        VueApplication.getInstance().getRequestQueue().add(request);
    }

    private class AisleCreateRequest extends Request<String> {
        // ... other methods go here
        private Map<String, String> mParams;
        Response.Listener<String> mListener;
        private String mAisleAsString;
        private StringEntity mEntity;

        public AisleCreateRequest(String aisleAsString,
                                 Response.Listener<String> listener,
                                 Response.ErrorListener errorListener) {
            super(Method.PUT, VUE_API_BASE_URI + CREATE_AISLE_ENDPOINT,
                    errorListener);
            mListener = listener;
            mAisleAsString = aisleAsString;
            try{
                mEntity = new StringEntity(mAisleAsString);
            }catch(UnsupportedEncodingException ex){
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
}
