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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;

import android.content.Context;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lateralthoughts.vue.connectivity.VueConnectivityManager;
import com.lateralthoughts.vue.domain.Aisle;
import com.lateralthoughts.vue.utils.ParcelableNameValuePair;

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

	// private static final String VUE_CONTENT_PROVIDER_BASE_URI =
	// "http://1-python.vueapi-canary.appspot.com/rest/0.1/";
	// private static final String VUE_CONTENT_PROVIDER_BASE_URI =
	// "http://2-java.vueapi-canary.appspot.com/api/";

	private static final String VUE_CONTENT_PROVIDER_BASE_URI = "http://2-java.vueapi-canary-development1.appspot.com/api/trendingaislesgetorderedbytime";

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
			final ResultReceiver receiver, final boolean loadMore) {
		boolean status = true;
		Log.i("datarequest", "datarequest parsing data offset: " + offset
				+ "  limit: " + limit);
		mParams.clear();
/*		StringBuilder baseUri = new StringBuilder();

		addParams(mLimitTag, String.valueOf(limit));
		addParams(mOffsetTag, String.valueOf(offset));
		baseUri.append(VUE_CONTENT_PROVIDER_BASE_URI);
		// String baseUri = VUE_CONTENT_PROVIDER_BASE_URI;
		// we want to get the current trending aisles
		baseUri.append(mTrendingAislesTag);
		if (DEBUG)
			Log.e(TAG, "uri we are sending = " + baseUri.toString());*/

		boolean isConnection = VueConnectivityManager
				.isNetworkConnected(mContext);
		if (!isConnection) {
			Toast.makeText(mContext, R.string.no_network, Toast.LENGTH_LONG)
					.show();
			Log.e(TAG, "network connection No");
			return status;
		} else if (isConnection) {
			/*
			 * Intent intent = new Intent(mContext,
			 * VueContentRestService.class);
			 * intent.putExtra("url",baseUri.toString());
			 * intent.putParcelableArrayListExtra("headers", mHeaders);
			 * intent.putParcelableArrayListExtra("params",mParams);
			 * intent.putExtra("receiver", receiver);
			 */

			// String requestUrlBase = VUE_CONTENT_PROVIDER_BASE_URI +
			// "aisle/trending?limit=%s&offset=%s";
			final String requestUrl = VUE_CONTENT_PROVIDER_BASE_URI + "/" + limit
					+ "/" + offset; /*
									 * String.format(requestUrlBase, limit,
									 * offset);
									 */
			Log.i("Gateway", "jsonresponse trendig requestUrl:  " + requestUrl);
			Response.Listener listener = new Response.Listener<JSONArray>() {
				@Override
				public void onResponse(JSONArray jsonArray) {
					if (null != jsonArray) {
						Log.i("Gateway", "jsonresponse trendig:  " + jsonArray);
						Bundle responseBundle = new Bundle();
						responseBundle
								.putString("result", jsonArray.toString());
						responseBundle.putBoolean("loadMore", loadMore);
						responseBundle.putInt("offset", offset);
						receiver.send(1, responseBundle);
					}
				}
			};
			Response.ErrorListener errorListener = new Response.ErrorListener() {
				@Override
				public void onErrorResponse(VolleyError error) {
					Bundle responseBundle = new Bundle();
					responseBundle.putString("result", "error");
					receiver.send(1, responseBundle);
					Log.i("Gateway", "jsonresponse trendig error response:  "   );
					Log.e("VueNetworkError",
							"Vue encountered network operations error. Error = "
									+ error.networkResponse);
				}
			};
			JsonArrayRequest vueRequest = new JsonArrayRequest(requestUrl,
					listener, errorListener)/*
											 * {
											 * 
											 * @Override public Map<String,
											 * String> getHeaders() throws
											 * AuthFailureError{ HashMap<String,
											 * String> headersMap = new
											 * HashMap<String, String>();
											 * headersMap.put("Accept-Encoding",
											 * "gzip");
											 * headersMap.put("Content-Type"
											 * ,"application/json"); return
											 * headersMap; } }
											 */;
	 
			
			
			VueApplication.getInstance().getRequestQueue().add(vueRequest);
 
		}
		return status;
	}

	/*
	 * public static List<Aisle> testGetTrendingAislesSortedByCreationTime( int
	 * limit, int offset) throws Exception { List<Aisle> retrievedAisles = null;
	 * 
	 * URL url = new URL(VUE_CONTENT_PROVIDER_BASE_URI + "/" + limit + "/" +
	 * offset); HttpGet httpGet = new HttpGet(url.toString()); DefaultHttpClient
	 * httpClient = new DefaultHttpClient(); HttpResponse response =
	 * httpClient.execute(httpGet); if (response.getEntity() != null &&
	 * response.getStatusLine().getStatusCode() == 200) { String responseMessage
	 * = EntityUtils.toString(response.getEntity());
	 * System.out.println("GET Trending Aisles Response: " + responseMessage);
	 * if (responseMessage.length() > 0) { retrievedAisles = (new
	 * ObjectMapper()).readValue( responseMessage, new
	 * TypeReference<List<Aisle>>() { }); } } return retrievedAisles; }
	 */

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
}
