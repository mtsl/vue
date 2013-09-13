package com.lateralthoughts.vue;

import android.util.Log;
import com.android.volley.*;
import com.android.volley.toolbox.HttpHeaderParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lateralthoughts.vue.domain.Aisle;
import com.lateralthoughts.vue.domain.VueImage;

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
    private static String VUE_API_BASE_URI = "http://2-java.vueapi-canary-development1.appspot.com/api/";
    //private String VUE_API_BASE_URI = "https://vueapi-canary.appspot.com/";
    private static String CREATE_AISLE_ENDPOINT = "aislecreate";
    private String CREATE_IMAGE_ENDPOINT = "imageecreate";
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
        Thread t = new Thread(new Runnable() {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public void run() {
        try {
			Aisle aisleresult = testCreateAisle(aisle);
			
			Log.i("aisleinfo", "aisle id: "+aisleresult.getId());
			Log.i("aisleinfo", "aisle id: "+aisleresult.getOwnerUserId());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			}});
		t.start();
        Response.Listener listener = new Response.Listener<String>() {

            @Override
            public void onResponse(String jsonArray) {
            	 
                if (null != jsonArray) {
                    Log.e("Profiling", "Profiling : onResponse(): "+jsonArray);
                    try{
                        JSONObject userInfo = new JSONObject(jsonArray);
                        Log.e("Profiling", "Profiling : onResponse(): "+jsonArray);
                        //JSONObject user = userInfo.getJSONObject("user");
                        callback.onAisleUpdated(null,null);
                    }catch(Exception ex){
                    	 Log.e("Profiling", "Profiling : onResponse() error");
                    	ex.printStackTrace();
                    }
                }
            }
        };
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            	 Log.e("AisleCreationTest","Aisle created2 failure response");
                if (null != error.networkResponse
                        && null != error.networkResponse.data) {
                    String errorData = error.networkResponse.data.toString();
                    Log.e("VueUserDebug", "error date = " + errorData);
                }
            }
        };
        Log.e("AisleCreationTest","Aisle created2 requestsend!");
        String requestUrl = VUE_API_BASE_URI + CREATE_AISLE_ENDPOINT;
        AislePutRequest request = new AislePutRequest(aisleAsString, listener,
                errorListener,requestUrl);
        VueApplication.getInstance().getRequestQueue().add(request);
    }
    
    
    //test code for url put request checking.
    public static Aisle testCreateAisle(Aisle aisle) throws Exception
 {
		Aisle createdAisle = null;
		ObjectMapper mapper = new ObjectMapper();
		String s = VUE_API_BASE_URI + CREATE_AISLE_ENDPOINT;
		URL url = new URL(s);
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
			String responseMessage = EntityUtils.toString(response.getEntity());
			System.out.println("AISLE CREATED SUCCESS Response: "
					+ responseMessage);
			if (responseMessage.length() > 0) {
				createdAisle = (new ObjectMapper()).readValue(responseMessage,
						Aisle.class);
			}
		}

		return createdAisle;
	}
 

    
    
    
//issues a request to add an image to the aisle.
	public void addImageToAisle(VueImage image, final ImageAddedCallback callback) {
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
				if (null != jsonArray) {
					Log.e("Profiling", "Profiling : onResponse()");
					try {
						JSONObject userInfo = new JSONObject(jsonArray);
						JSONObject user = userInfo.getJSONObject("user");
						callback.onImageAdded(null);
					} catch (JSONException ex) {
						ex.printStackTrace();
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
