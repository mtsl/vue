package com.lateralthoughts.vue;

import android.util.Log;
import com.android.volley.*;
import com.android.volley.toolbox.HttpHeaderParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lateralthoughts.vue.domain.Aisle;
import com.lateralthoughts.vue.domain.VueImage;
import com.lateralthoughts.vue.parser.Parser;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
//FB imports
//java util imports

public class AisleManager {

    private ObjectMapper mObjectMapper;
    public interface AisleUpdateCallback {
        public void onAisleUpdated(AisleContext aisleContext,String id);
    }
    public interface ImageAddedCallback {
    	 public void onImageAdded(AisleImageDetails imageDetails);
    }
    private static String VUE_API_BASE_URI = "http://2-java.vueapi-canary.appspot.com/api/";
                                                               
    //private String VUE_API_BASE_URI = "https://vueapi-canary.appspot.com/";
    private static String CREATE_AISLE_ENDPOINT = "aislecreate";
    private String CREATE_IMAGE_ENDPOINT = "imagecreate";
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
    public void createEmptyAisle(final Aisle aisle, final AisleUpdateCallback callback){
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
            	 Log.i("imageurl", "imageurl  aisle creation response ");
                if (null != jsonArray) {
                    
                    try{
                       // JSONObject userInfo = new JSONObject(jsonArray);
                        
                     AisleContext aisleContext =  new Parser().getAisleCotent(jsonArray);
                      //  JSONObject user = userInfo.getJSONObject("user");
                        //TODO: GET THE AISLE OBJECT FROM THE PARSER CLASE SEND THE AISLE AND AISLE ID BACK.
                        callback.onAisleUpdated(aisleContext,aisleContext.mAisleId);
                    }catch(Exception ex){
                    	 Log.e("Profiling", "Profiling : onResponse() **************** error");
                    	ex.printStackTrace();
                    }
                }
            }
        };
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            	 Log.i("imageurl", "imageurl  aisle creation error response ");
                if (null != error.networkResponse
                        && null != error.networkResponse.data) {
                    String errorData = error.networkResponse.data.toString();
                    Log.e("VueUserDebug", "error date = " + errorData);
                }
            }
        };
        Log.i("imageurl", "imageurl  aisle creation request aisleAsString: "+aisleAsString);
        String requestUrl = VUE_API_BASE_URI + CREATE_AISLE_ENDPOINT;
       
       
     
        Log.i("imageurl", "imageurl  aisle creation request url: "+requestUrl);
        
        
        AislePutRequest request = new AislePutRequest(aisleAsString, listener,
                errorListener,requestUrl);
        VueApplication.getInstance().getRequestQueue().add(request);
    }
    
    private AisleContext parseAisleContent(JSONObject user){
    	AisleContext aisle = null;
    	
		return aisle;
    	
    }
//issues a request to add an image to the aisle.
	public void addImageToAisle(VueImage image, final ImageAddedCallback callback) {
		Log.i("addimagetoaisle", "addimagetoaisle1");
		if (null == image) {
			throw new RuntimeException(
					"Can't create Aisle without a non null aisle object");
		}
		String imageAsString = null;
		try {
			imageAsString = mObjectMapper.writeValueAsString(image);
		} catch (JsonProcessingException ex2) {

		}
		Response.Listener listener = new Response.Listener<String>() {

			@Override
			public void onResponse(String jsonArray) {
				Log.i("addimagetoaisle", "addimagetoaisle onresponse jsonArray: "+jsonArray);
				if (null != jsonArray) {
					Log.e("Profiling", "Profiling : onResponse()");
					try {
			 
						callback.onImageAdded(new Parser().getImageDetails(jsonArray));
					} catch (JSONException ex) {
						ex.printStackTrace();
						Log.i("addimagetoaisle", "addimagetoaisle  onresponse exception");
					}
				}
			}
		};
		Response.ErrorListener errorListener = new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Log.i("addimagetoaisle", "addimagetoaisle onerror response");
				if (null != error.networkResponse
						&& null != error.networkResponse.data) {
					String errorData = error.networkResponse.data.toString();
					Log.e("VueUserDebug", "error date = " + errorData);
				}
			}
		};
		Log.i("addimagetoaisle", "addimagetoaisle 2 sending request string: "+imageAsString);
		AislePutRequest request = new AislePutRequest(imageAsString, listener,
				errorListener, VUE_API_BASE_URI + CREATE_IMAGE_ENDPOINT);
		VueApplication.getInstance().getRequestQueue().add(request);
	}
    private class AislePutRequest extends Request<String> {
        // ... other methods go here
        private Map<String, String> mParams;
        Response.Listener<String> mListener;
        private String mAisleAsString;
        private StringEntity mEntity;

        public AislePutRequest(String aisleAsString,
                                 Response.Listener<String> listener,
                                 Response.ErrorListener errorListener,String url) {
            super(Method.PUT,url,
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
