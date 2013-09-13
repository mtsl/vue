package com.lateralthoughts.vue.connectivity;

import java.util.ArrayList;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.lateralthoughts.vue.AisleWindowContent;
import com.lateralthoughts.vue.R;
import com.lateralthoughts.vue.VueApplication;
import com.lateralthoughts.vue.VueContentGateway;
import com.lateralthoughts.vue.VueTrendingAislesDataModel;
import com.lateralthoughts.vue.ui.NotifyProgress;
 

public class NetworkHandler {
	 Context mContext;
	 
	  
	 DataBaseManager mDbManager;
	 protected VueContentGateway mVueContentGateway;
	 protected TrendingAislesContentParser mTrendingAislesParser;
	 private static final int NOTIFICATION_THRESHOLD = 4;
	 private static final int TRENDING_AISLES_BATCH_SIZE = 10;
	 public static final int TRENDING_AISLES_BATCH_INITIAL_SIZE = 10;
		protected int mLimit;
		protected int mOffset;
	public NetworkHandler(Context context){
		 mContext = context;
		 mVueContentGateway = VueContentGateway.getInstance();
		mTrendingAislesParser =new TrendingAislesContentParser(new Handler());
		mDbManager = DataBaseManager.getInstance(mContext);
		mLimit = TRENDING_AISLES_BATCH_INITIAL_SIZE;
		mOffset = 0;
	}
	
 public void requestMoreAisle(boolean loadMore){
		Log.i("offeset and limit", "offeset1: load moredata");
		if(VueTrendingAislesDataModel.getInstance(VueApplication.getInstance()).isMoreDataAvailable()){
			VueTrendingAislesDataModel.getInstance(VueApplication.getInstance()).loadOnRequest = false;
		if (mOffset < NOTIFICATION_THRESHOLD * TRENDING_AISLES_BATCH_SIZE)
			mOffset += mLimit;
		else {
			mOffset += mLimit;
			mLimit = TRENDING_AISLES_BATCH_SIZE;
		}
		Log.i("offeset and limit", "offeset1: " + mOffset + " and limit: "+ mLimit);
		
		mVueContentGateway.getTrendingAisles(mLimit, mOffset,
				mTrendingAislesParser, loadMore);
		} else {
			Log.i("offeset and limit", "offeset1: else part");
		}
 }
 public void reqestByCategory(String category, NotifyProgress progress,
			boolean fromServer, boolean loadMore){
 
		VueTrendingAislesDataModel.getInstance(VueApplication.getInstance()).setNotificationProgress(progress, fromServer);
		String downLoadFromServer = "fromDb";
		if (fromServer == true) {
			downLoadFromServer = "fromServer";
			mOffset = 0;
			mLimit = TRENDING_AISLES_BATCH_INITIAL_SIZE;
			VueTrendingAislesDataModel.getInstance(VueApplication.getInstance()).clearContent();
			VueTrendingAislesDataModel.getInstance(VueApplication.getInstance()).showProgress();
			VueTrendingAislesDataModel.getInstance(VueApplication.getInstance()).setMoreDataAVailable(true);
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
 public static void requestTrending(){
	 
 }
 public void requestCreateAisle(){
	 VueTrendingAislesDataModel.getInstance(VueApplication.getInstance()).dataObserver();
 }
 public void requestSearch(String searchString){
	 
 }
 public void requestUserAisles(){
	 
 }
 public void loadInitialData(boolean loadMore,Handler mHandler){
	
			if (!VueConnectivityManager.isNetworkConnected(mContext)) {
				Toast.makeText(mContext, R.string.no_network, Toast.LENGTH_SHORT)
						.show();
				//VueTrendingAislesDataModel.getInstance(mContext).isDownloadFail = true;
				ArrayList<AisleWindowContent> aisleContentArray = mDbManager
						.getAislesFromDB(null);
				if (aisleContentArray.size() == 0) {
					return;
				}
				Message msg = new Message();
				msg.obj = aisleContentArray;
				mHandler.sendMessage(msg);
			} else {
				// initializeTrendingAisleContent();
				//VueTrendingAislesDataModel.getInstance(mContext).setMoreDataAVailable(true);
				mVueContentGateway.getTrendingAisles(mLimit, mOffset,
						mTrendingAislesParser, loadMore);
			}
	 
 }
 	public int getmOffset() {
	return mOffset;
}

public void setmOffset(int mOffset) {
	this.mOffset = mOffset;
}
}
