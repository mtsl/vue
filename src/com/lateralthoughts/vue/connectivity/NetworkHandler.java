package com.lateralthoughts.vue.connectivity;

import java.net.URL;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.lateralthoughts.vue.AisleManager;
import com.lateralthoughts.vue.AisleManager.ImageAddedCallback;
import com.lateralthoughts.vue.AisleWindowContent;
import com.lateralthoughts.vue.R;
import com.lateralthoughts.vue.VueApplication;
import com.lateralthoughts.vue.VueConstants;
import com.lateralthoughts.vue.VueContentGateway;
import com.lateralthoughts.vue.VueTrendingAislesDataModel;
import com.lateralthoughts.vue.VueUser;
import com.lateralthoughts.vue.AisleManager.AisleUpdateCallback;
import com.lateralthoughts.vue.domain.Aisle;
import com.lateralthoughts.vue.domain.VueImage;
import com.lateralthoughts.vue.parser.Parser;
import com.lateralthoughts.vue.ui.NotifyProgress;
import com.lateralthoughts.vue.utils.Utils;
 

public class NetworkHandler {
	Context mContext;
	private static final String SEARCH_REQUEST_URL = "http://2-java.vueapi-canary.appspot.com/api/getaisleswithmatchingkeyword/";
	                                                  //http://2-java.vueapi-canary-development1.appspot.com/api/
	DataBaseManager mDbManager;
	protected VueContentGateway mVueContentGateway;
	protected TrendingAislesContentParser mTrendingAislesParser;
	private static final int NOTIFICATION_THRESHOLD = 4;
	private static final int TRENDING_AISLES_BATCH_SIZE = 10;
	public static final int TRENDING_AISLES_BATCH_INITIAL_SIZE = 10;
	 private static String MY_AISLES = "aislesget/user/";
	protected int mLimit;
	protected int mOffset;
	
	
	

	public NetworkHandler(Context context) {
		mContext = context;
		mVueContentGateway = VueContentGateway.getInstance();
		mTrendingAislesParser = new TrendingAislesContentParser(new Handler(), VueConstants.AISLE_TRENDING_LIST_DATA);
		mDbManager = DataBaseManager.getInstance(mContext);
		mLimit = TRENDING_AISLES_BATCH_INITIAL_SIZE;
		mOffset = 0;
	}
//whle user scrolls down get next 10 aisles
	public void requestMoreAisle(boolean loadMore) {
		Log.i("offeset and limit", "offeset1: load moredata");
		if (VueTrendingAislesDataModel
				.getInstance(VueApplication.getInstance())
				.isMoreDataAvailable()) {
			VueTrendingAislesDataModel
					.getInstance(VueApplication.getInstance()).loadOnRequest = false;
			if (mOffset < NOTIFICATION_THRESHOLD * TRENDING_AISLES_BATCH_SIZE)
				mOffset += mLimit;
			else {
				mOffset += mLimit;
				mLimit = TRENDING_AISLES_BATCH_SIZE;
			}
			Log.i("offeset and limit", "offeset1: " + mOffset + " and limit: "
					+ mLimit);

			mVueContentGateway.getTrendingAisles(mLimit, mOffset,
					mTrendingAislesParser, loadMore);
		} else {
			Log.i("offeset and limit", "offeset1: else part");
		}
	}
//get the aisle based on the category
	public void reqestByCategory(String category, NotifyProgress progress,
			boolean fromServer, boolean loadMore) {

		VueTrendingAislesDataModel.getInstance(VueApplication.getInstance())
				.setNotificationProgress(progress, fromServer);
		String downLoadFromServer = "fromDb";
		if (fromServer == true) {
			downLoadFromServer = "fromServer";
			mOffset = 0;
			mLimit = TRENDING_AISLES_BATCH_INITIAL_SIZE;
			VueTrendingAislesDataModel
					.getInstance(VueApplication.getInstance()).clearContent();
			VueTrendingAislesDataModel
					.getInstance(VueApplication.getInstance()).showProgress();
			VueTrendingAislesDataModel
					.getInstance(VueApplication.getInstance())
					.setMoreDataAVailable(true);
			mVueContentGateway.getTrendingAisles(mLimit, mOffset,
					mTrendingAislesParser, loadMore);
			Log.i("loading from db", "loading from server");

		} else {
			Log.i("loading from db", "loading from db");
			downLoadFromServer = "fromDb";
			DbDataGetter dBgetter = new DbDataGetter(progress);
			dBgetter.execute(category, downLoadFromServer);
		}

	}

	public static void requestTrending() {

	}
//request the server to create an empty aisle.
	public void requestCreateAisle(Aisle aisle,final AisleUpdateCallback callback) {
		AisleManager.getAisleManager().createEmptyAisle(aisle, callback);
		VueTrendingAislesDataModel.getInstance(VueApplication.getInstance())
				.dataObserver();
	}
	public void requestForAddImage(VueImage image,ImageAddedCallback callback){
		AisleManager.getAisleManager().addImageToAisle(image, callback);
	}
//get aisles related to search keyword
	public void requestSearch(final String searchString) {
		JsonArrayRequest vueRequest = new JsonArrayRequest(SEARCH_REQUEST_URL
				+ searchString, new Response.Listener<JSONArray>() {

			@Override
			public void onResponse(JSONArray response) {
				if (null != response) {
					Bundle responseBundle = new Bundle();
					responseBundle.putString("Search result",
							response.toString());
					responseBundle.putBoolean("loadMore", false);
					mTrendingAislesParser.send(1, responseBundle);
				}
				Log.e("Search Resopnse", "SURU Search Resopnse : " + response);
			}
		}, new Response.ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				Log.e("Search Resopnse", "SURU Search Error Resopnse : "
						+ error.getMessage());
			}
		});

		VueApplication.getInstance().getRequestQueue().add(vueRequest);

	}

	public void requestUserAisles(String userId) {

		JsonArrayRequest vueRequest = new JsonArrayRequest(SEARCH_REQUEST_URL
				+ MY_AISLES+userId, new Response.Listener<JSONArray>() {

			@Override
			public void onResponse(JSONArray response) {
				if (null != response) {
					Bundle responseBundle = new Bundle();
					responseBundle.putString("Search result",
							response.toString());
					responseBundle.putBoolean("loadMore", false);
					mTrendingAislesParser.send(1, responseBundle);
				}
				Log.e("Search Resopnse", "SURU Search Resopnse : " + response);
			}
		}, new Response.ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				Log.e("Search Resopnse", "SURU Search Error Resopnse : "
						+ error.getMessage());
			}
		});

		VueApplication.getInstance().getRequestQueue().add(vueRequest);

	
	}

	
	
	public void loadInitialData(boolean loadMore, Handler mHandler) {

		if (!VueConnectivityManager.isNetworkConnected(mContext)) {
			Toast.makeText(mContext, R.string.no_network, Toast.LENGTH_SHORT)
					.show();
			ArrayList<AisleWindowContent> aisleContentArray = mDbManager
					.getAislesFromDB(null);
			if (aisleContentArray.size() == 0) {
				return;
			}
			Message msg = new Message();
			msg.obj = aisleContentArray;
			mHandler.sendMessage(msg);
		} else {
			Log.i("Gateway", "loadInitialData:  " + loadMore);
			mVueContentGateway.getTrendingAisles(mLimit, mOffset,
					mTrendingAislesParser, loadMore);
		}

	}
 public void requestAislesByUser()  {
	 
	 //TODO: CHANGE THIS REQUEST TO VOLLEY
	 new Thread(new Runnable() {
		
		@Override
		public void run() {
			try{
				Log.i("getAisleByuser", "getAisleByuser getailselistmethod " );
				 testGetAisleList();
				 Log.i("getAisleByuser", "getAisleByuser getailselistmethod completed " );
			}catch(Exception e){
				e.printStackTrace();
			}
			
		}
	}).start();
	
	 /*
		VueUser storedVueUser = null;
		try {
			storedVueUser = Utils.readUserObjectFromFile(VueApplication.getInstance(),
					VueConstants.VUE_APP_USEROBJECT__FILENAME);
		} catch (Exception e2) {
			e2.printStackTrace();
		}
		String userId = null;
		if (storedVueUser != null) {
			userId = Long.valueOf(storedVueUser.getVueId()).toString();
		}
	//String requestUrl = "http://2-java.vueapi-canary-development1.appspot.com/"+"api/getaisleswithmatchingfacebookORGPlus/"+userId;
	String requestUrl = "http://2-java.vueapi-canary-development1.appspot.com/"+"api/aislesget/user/"+userId;
	
	http://2-java.vueapi-canary-development1.appspot.com/api/aislesget/user
	
	Log.i("getAisleByuser", "getAisleByuser requestUrl: "+requestUrl );
		JsonArrayRequest vueRequest = new JsonArrayRequest( 
				requestUrl, new Response.Listener<JSONArray>() {

			@Override
			public void onResponse(JSONArray response) {
				if (null != response) {
					Bundle responseBundle = new Bundle();
					responseBundle.putString("Search result",
							response.toString());
					responseBundle.putBoolean("loadMore", false);
					mTrendingAislesParser.send(1, responseBundle);
				}
				Log.e("getAisleByuser", "getAisleByuser response : " + response);
			}
		}, new Response.ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				Log.e("getAisleByuser Resopnse", "getAisleByuser Error Resopnse : "
						+ error.getMessage());
			}
		});

		VueApplication.getInstance().getRequestQueue().add(vueRequest);
	 
	 
 */}
	public int getmOffset() {
		return mOffset;
	}

	public void setmOffset(int mOffset) {
		this.mOffset = mOffset;
	}
	
	public   void testGetAisleList() throws Exception{
		VueUser storedVueUser = null;
		try {
			storedVueUser = Utils.readUserObjectFromFile(VueApplication.getInstance(),
					VueConstants.VUE_APP_USEROBJECT__FILENAME);
		} catch (Exception e2) {
			e2.printStackTrace();
		}
		String userId = null;
		if (storedVueUser != null) {
			userId = Long.valueOf(storedVueUser.getVueId()).toString();
		}
	//String requestUrl = "http://2-java.vueapi-canary-development1.appspot.com/"+"api/getaisleswithmatchingfacebookORGPlus/"+userId;
	String requestUrl = "http://2-java.vueapi-canary-development1.appspot.com/api/aislesget/user/"+userId; 
		
		
		URL url = new URL(requestUrl);
		HttpGet httpGet = new HttpGet(url.toString());
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpResponse response = httpClient.execute(httpGet);
		if(response.getEntity()!=null &&
		response.getStatusLine().getStatusCode() == 200) {
		String responseMessage = EntityUtils.toString(response.getEntity());
		System.out.println("Response: "+responseMessage);
		new Parser().getUserAilseLIst(responseMessage);
		Log.i("getAisleByuser", "getAisleByuser requestUrl java code response: "+responseMessage );
		}

		 
		}

}






 
