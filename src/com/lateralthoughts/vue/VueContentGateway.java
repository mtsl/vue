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

import org.json.JSONArray;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.lateralthoughts.vue.connectivity.VueConnectivityManager;
import com.lateralthoughts.vue.utils.ParcelableNameValuePair;
import com.lateralthoughts.vue.utils.UrlConstants;
import com.lateralthoughts.vue.utils.Utils;

public class VueContentGateway {
    private final String TAG = "VueContentGateway";
    private static VueContentGateway sInstance;
    private Context mContext; // application context;
    
    private ArrayList<ParcelableNameValuePair> mHeaders;
    private ArrayList<ParcelableNameValuePair> mParams;
    
    private String mTrendingAislesTag;
    private String mLimitTag;
    private String mOffsetTag;
    
    public static VueContentGateway getInstance() {
        if (null == sInstance) {
            sInstance = new VueContentGateway();
        }
        return sInstance;
    }
    
    /*
     * hiding the constructor. This is going to be singleton and shared by all
     * activities in the application.
     */
    private VueContentGateway() {
        mContext = (Context) VueApplication.getInstance();
        initializeHttpFields();
        mTrendingAislesTag = mContext.getResources().getString(
                R.string.trending_aisles_tag);
        mLimitTag = mContext.getResources().getString(R.string.limit_tag);
        mOffsetTag = mContext.getResources().getString(R.string.offset_tag);
    }
    
    /*
     * getTrendingAisles - This API is used to get a list of the current
     * Trending Aisles. The ResultReceiver object will be notified when the list
     * is available
     */
    public boolean getTrendingAisles(int limit, final int offset,
            final ResultReceiver receiver, final boolean loadMore,
            final String screenName) {
        boolean status = true;
        mParams.clear();
        boolean isConnection = VueConnectivityManager
                .isNetworkConnected(mContext);
        if (!isConnection) {
            Toast.makeText(mContext, R.string.no_network, Toast.LENGTH_LONG)
                    .show();
            VueTrendingAislesDataModel
                    .getInstance(VueApplication.getInstance())
                    .dismissProgress();
            return status;
        } else if (isConnection) {
            final String requestUrl = UrlConstants.GET_TRENDINGAISLES_RESTURL
                    + "/" + limit + "/" + offset;
            @SuppressWarnings("rawtypes")
            Response.Listener listener = new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray jsonArray) {
                    if (null != jsonArray) {
                        Bundle responseBundle = new Bundle();
                        responseBundle
                                .putString("result", jsonArray.toString());
                        responseBundle.putBoolean("loadMore", loadMore);
                        responseBundle.putInt("offset", offset);
                        receiver.send(1, responseBundle);
                        Intent intent = new Intent(
                                VueConstants.LANDING_SCREEN_RECEIVER);
                        intent.putExtra(
                                VueConstants.LANDING_SCREEN_RECEIVER_KEY,
                                screenName);
                        VueApplication.getInstance().sendBroadcast(intent);
                    }
                }
            };
            Response.ErrorListener errorListener = new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Bundle responseBundle = new Bundle();
                    responseBundle.putString("result", "error");
                    receiver.send(1, responseBundle);
                    VueTrendingAislesDataModel.getInstance(
                            VueApplication.getInstance()).dismissProgress();
                    
                }
            };
            @SuppressWarnings("unchecked")
            VueAislesRequest vueRequest = new VueAislesRequest(requestUrl,
                    listener, errorListener) {
            };
            vueRequest.setRetryPolicy(new DefaultRetryPolicy(
                    DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, Utils.MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VueApplication.getInstance().getRequestQueue().add(vueRequest);
        }
        return status;
    }
    
    private void initializeHttpFields() {
        mHeaders = new ArrayList<ParcelableNameValuePair>();
        mParams = new ArrayList<ParcelableNameValuePair>();
        addHeaders("Accept-Encoding", "gzip");
        addHeaders("Content-Type", "application/json");
    }
    
    private void addHeaders(String name, String value) {
        mHeaders.add(new ParcelableNameValuePair(name, value));
    }
    
    @SuppressWarnings("unused")
    private void addParams(String name, String value) {
        mParams.add(new ParcelableNameValuePair(name, value));
    }
    
    private class VueAislesRequest extends JsonArrayRequest {
        
        /**
         * Creates a new request.
         * 
         * @param url
         *            URL to fetch the JSON from
         * @param listener
         *            Listener to receive the JSON response
         * @param errorListener
         *            Error listener, or null to ignore errors.
         */
        private Priority mPriority = Priority.HIGH;
        
        @Override
        public Priority getPriority() {
            return mPriority;
        }
        
        public VueAislesRequest(String url, Listener<JSONArray> listener,
                ErrorListener errorListener) {
            super(url, listener, errorListener);
        }
    }
}
