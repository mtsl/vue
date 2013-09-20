package com.lateralthoughts.vue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lateralthoughts.vue.domain.Aisle;
import com.lateralthoughts.vue.domain.VueImage;
import com.lateralthoughts.vue.parser.Parser;
import com.lateralthoughts.vue.utils.UrlConstants;

public class AisleManager {

    private ObjectMapper mObjectMapper;
    public interface AisleUpdateCallback {
        public void onAisleUpdated(AisleContext aisleContext,String id);
    }
    public interface ImageAddedCallback {
    	 public void onImageAdded(AisleImageDetails imageDetails);
    }
   // private static String VUE_API_BASE_URI = "http://2-java.vueapi-canary-development1.appspot.com/";
    
    //private String VUE_API_BASE_URI = "https://vueapi-canary.appspot.com/";
    //private static String CREATE_AISLE_ENDPOINT = "api/aislecreate";
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
                if (null != jsonArray) {
               	 
               	Log.i("myailsedebug", "myailsedebug: recieved response:  "+jsonArray );
                    try{
                       // JSONObject userInfo = new JSONObject(jsonArray);
                        
                  AisleWindowContent aileItem =  new Parser().getAisleCotent(jsonArray);
                      VueTrendingAislesDataModel.getInstance(VueApplication.getInstance()).addItemToListAt(aileItem.getAisleContext().mAisleId, aileItem, 0);
                  	VueTrendingAislesDataModel.getInstance(
							VueApplication.getInstance())
							.dataObserver();

                      //  JSONObject user = userInfo.getJSONObject("user");
                        //TODO: GET THE AISLE OBJECT FROM THE PARSER CLASE SEND THE AISLE AND AISLE ID BACK.
                        //callback.onAisleUpdated(aisleContext,aisleContext.mAisleId);
                        //VueTrendingAislesDataModel.getInstance(VueApplication.getInstance()).getNetworkHandler().requestAislesByUser();
                    }catch(Exception ex){
                    	 Log.e("Profiling", "Profiling : onResponse() **************** error");
                    	ex.printStackTrace();
                    }
                } else {
                	Log.i("myailsedebug", "myailsedebug: recieved response: is null ");
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
       // String requestUrl = VUE_API_BASE_URI + CREATE_AISLE_ENDPOINT;
        String requestUrl = UrlConstants.CREATE_AISLE_RESTURL;
       
     
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
	public void addImageToAisle(VueImage image, final ImageAddedCallback callback) {/*
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
	*/}
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
