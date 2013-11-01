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

import android.content.Context;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.Request.Priority;
import com.lateralthoughts.vue.connectivity.VueConnectivityManager;
import com.lateralthoughts.vue.utils.ParcelableNameValuePair;
import com.lateralthoughts.vue.utils.UrlConstants;
import com.lateralthoughts.vue.utils.Utils;
import com.lateralthoughts.vue.utils.Logging;

import org.json.JSONArray;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class VueContentGateway {
	private final String TAG = "VueContentGateway";
	private final boolean DEBUG = false;
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
			final ResultReceiver receiver, final boolean loadMore, final String screenName) {
		boolean status = true;
		Logging.i("datarequest", "datarequest parsing data offset: " + offset
				+ "  limit: " + limit);
		mParams.clear();
		boolean isConnection = VueConnectivityManager
				.isNetworkConnected(mContext);
		if (!isConnection) {
			Toast.makeText(mContext, R.string.no_network, Toast.LENGTH_LONG)
					.show();
			VueTrendingAislesDataModel.getInstance(VueApplication.getInstance()).dismissProgress();
			Logging.d(TAG, "network connection No");
			return status;
		} else if (isConnection) {
			final String requestUrl = UrlConstants.GET_TRENDINGAISLES_RESTURL + "/" + limit
					+ "/" + offset;  
			Logging.i("Gateway", "jsonresponse trendig requestUrl:  " + requestUrl);
			Response.Listener listener = new SuccessListener(offset, limit, receiver, screenName, loadMore);
			Response.ErrorListener errorListener = new Response.ErrorListener() {
				@Override
				public void onErrorResponse(VolleyError error) {
					Bundle responseBundle = new Bundle();
					responseBundle.putString("result", "error");
					receiver.send(1, responseBundle);
					Logging.i("Gateway", "jsonresponse trendig error response:  "   );
					Logging.d("VueNetworkError",
							"Vue encountered network operations error. Error = "
									+ error.networkResponse);
 
							VueTrendingAislesDataModel.getInstance(VueApplication.getInstance()).dismissProgress();
					 
				}
			};
			JsonArrayRequest vueRequest = new JsonArrayRequest(requestUrl,
					listener, errorListener) {
      };
      
      vueRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, Utils.MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
      vueRequest.setTag(new String("MoreAislesTag"));
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

	private void addParams(String name, String value) {
		mParams.add(new ParcelableNameValuePair(name, value));
	}

    private class SuccessListener implements Response.Listener<JSONArray> {
        private boolean _loadMore;
        private int _offset;
        private int _limit;
        WeakReference<ResultReceiver> _receiver;
        String _screenName;

        private long _requestStartTime;
        public SuccessListener(int offset, int limit, ResultReceiver receiver, String screenName, boolean loadMore){
            _loadMore = loadMore;
            _offset = offset;
            _limit = limit;
            _receiver = new WeakReference<ResultReceiver>(receiver);
            _screenName = screenName;
            _requestStartTime = System.currentTimeMillis();

        }
        @Override
        public void onResponse(JSONArray jsonArray) {
            if (null != jsonArray) {
                Logging.i("Gateway", "jsonresponse trendig:  " + jsonArray);
                Bundle responseBundle = new Bundle();
                responseBundle
                        .putString("result", jsonArray.toString());
                responseBundle.putBoolean("loadMore", _loadMore);
                responseBundle.putInt("offset", _offset);
                _receiver.get().send(1, responseBundle);
                VueLandingPageActivity.changeScreenName(_screenName);
                int elapsed = (int) (System.currentTimeMillis()-_requestStartTime);
                //Logging.e("**** LoadAisleData ****","elapsed time to load aisle meta data = "  + elapsed + " offset = " + _offset
                //                        + " limit = " + _limit);
            }
        }
    };
}
