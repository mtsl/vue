/**
 * VueContentGateway is an API layer on top of the REST client service. From the
 * application's point of view, calls to get data is streamlined through this
 * simplified API interface. No need to bother with service specific details or
 * background tasks and such.
 * The general format for most APIs in this class involves sending in a ResultReceiver
 * object for callback. Invoke an API, provide a ResultReceiver and handle the callback
 * implemented by ResultReceiver. State management is up to the application. 
 * 
 */
package com.lateralthoughts.vue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.lateralthoughts.vue.connectivity.VueBatteryManager;
import com.lateralthoughts.vue.connectivity.VueConnectivityManager;
import com.lateralthoughts.vue.service.VueContentRestService;
import com.lateralthoughts.vue.utils.ParcelableNameValuePair;
//internal imports
//volley imports
//java lang utils

public class VueContentGateway {
	private final String TAG = "VueContentGateway";
	private final boolean DEBUG = false;
	private static VueContentGateway sInstance;
	private Context mContext; //application context;
	
    private ArrayList<ParcelableNameValuePair> mHeaders;
    private ArrayList<ParcelableNameValuePair> mParams;
    
    private String mTrendingAislesTag;
    private String mLimitTag;
    private String mOffsetTag;
    
    private static final String VUE_CONTENT_PROVIDER_BASE_URI = "http://vueapi-canary.appspot.com/rest/0.1/";
	
	public static VueContentGateway getInstance(){
		if(null == sInstance){
			sInstance = new VueContentGateway();
		}
		return sInstance;
	}
	/*
	 * hiding the constructor. This is going to be singleton and shared
	 * by all activities in the application.
	 */
	private VueContentGateway(){
		mContext = (Context)VueApplication.getInstance();
		initializeHttpFields();
		mTrendingAislesTag = mContext.getResources().getString(R.string.trending_aisles_tag);
		mLimitTag = mContext.getResources().getString(R.string.limit_tag);
		mOffsetTag = mContext.getResources().getString(R.string.offset_tag);}

	/*
	 * getTrendingAisles - This API is used to get a list of the current Trending Aisles.
	 * The ResultReceiver object will be notified when the list is available
	 */
	public boolean getTrendingAisles(int limit, int offset, final ResultReceiver receiver){
		boolean status = true;
        mParams.clear();
        StringBuilder baseUri = new StringBuilder();

        addParams(mLimitTag, String.valueOf(limit));
        addParams(mOffsetTag, String.valueOf(offset));
        baseUri.append(VUE_CONTENT_PROVIDER_BASE_URI);
        //String baseUri = VUE_CONTENT_PROVIDER_BASE_URI;
        //we want to get the current trending aisles
        baseUri.append(mTrendingAislesTag);
        if(DEBUG) Log.e(TAG,"uri we are sending = " + baseUri.toString());

        boolean isConnection = VueConnectivityManager.isNetworkConnected(mContext);
        if(!isConnection) {
            Log.e(TAG, "network connection No");
            return status;
          } else if(isConnection && (VueBatteryManager.isConnected(mContext) || VueBatteryManager.batteryLevel(mContext) > VueBatteryManager.MINIMUM_BATTERY_LEVEL)) {
              Log.e("VueTrendingAislesDataModel", "JSONArray size(): TEST 1");
        	 /* Intent intent = new Intent(mContext, VueContentRestService.class);
              intent.putExtra("url",baseUri.toString());
              intent.putParcelableArrayListExtra("headers", mHeaders);
              intent.putParcelableArrayListExtra("params",mParams);
              intent.putExtra("receiver", receiver);*/

              String requestUrlBase = VUE_CONTENT_PROVIDER_BASE_URI + "aisle/trending?limit=%s&offset=%s";
              String requestUrl = String.format(requestUrlBase, limit, offset);
              Response.Listener listener = new Response.Listener<JSONArray>(){
                  @Override
                  public void onResponse(JSONArray jsonArray){
                    Log.e("VueTrendingAislesDataModel", "JSONArray size(): TEST 2");
                      if(null != jsonArray){
                        Log.e("VueTrendingAislesDataModel", "JSONArray size(): TEST 3");
                          Bundle responseBundle = new Bundle();
                          responseBundle.putString("result",jsonArray.toString());
                          receiver.send(1,responseBundle);
                      }
                  }
              };
              Response.ErrorListener errorListener = new Response.ErrorListener(){
                  @Override
                  public void onErrorResponse(VolleyError error){
                      Log.e("VueNetworkError","Vue encountered network operations error. Error = " + error.networkResponse);
                  }
              };
              JsonArrayRequest vueRequest =
                      new JsonArrayRequest(requestUrl, listener, errorListener ){
                  @Override
                  public Map<String, String> getHeaders() throws AuthFailureError{
                      HashMap<String, String> headersMap = new HashMap<String, String>();
                      headersMap.put("Accept-Encoding", "gzip");
                      headersMap.put("Content-Type","application/json");
                      return headersMap;
                  }
              };

              VueApplication.getInstance().getRequestQueue().add(vueRequest);

              //mContext.startService(intent);
          }
        return status;		
	}

    private void initializeHttpFields(){
        mHeaders = new ArrayList<ParcelableNameValuePair>();
        mParams = new ArrayList<ParcelableNameValuePair>();
        addHeaders("Accept-Encoding", "gzip");
        addHeaders("Content-Type","application/json");
    }
    
    private void addHeaders(String name, String value){
        mHeaders.add(new ParcelableNameValuePair(name, value));
    }
    
    private void addParams(String name, String value){
        mParams.add(new ParcelableNameValuePair(name,value));
    }
}
