package com.lateralthoughts.vue.connectivity;

import java.util.ArrayList;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
 
import com.lateralthoughts.vue.AisleWindowContent;
import com.lateralthoughts.vue.VueApplication;
import com.lateralthoughts.vue.VueTrendingAislesDataModel;
import com.lateralthoughts.vue.parser.Parser;

public class TrendingAislesContentParser extends ResultReceiver{
	private int mState;
	// this variable above is usually set to one of the following values
	private final int AISLE_TRENDING_LIST_DATA = 1;
	public TrendingAislesContentParser(Handler handler) {
		super(handler);
		mState = AISLE_TRENDING_LIST_DATA;
	}

	@Override
	protected void onReceiveResult(int resultCode, Bundle resultData) {
		switch (mState) {
		case AISLE_TRENDING_LIST_DATA:
		 
 
			ArrayList<AisleWindowContent> aislesList =new Parser().parseTrendingAislesResultData(
						resultData.getString("result"),
						resultData.getBoolean("loadMore"));
			if(aislesList != null && aislesList.size()>0){
				int size = aislesList.size();
				Log.i("ailsesize", "ailseListSize: "+size);
				for(int i = 0;i < size;i++){
					AisleWindowContent aisleItem = aislesList.get(i);
					VueTrendingAislesDataModel.getInstance(VueApplication.getInstance()).addItemToList(aisleItem.getAisleId(), aisleItem);
				}
				new DbDataSetter(aislesList).execute();
				aislesList = null;
			}
			
				// if this is the first set of data we are receiving go
				// ahead
				// notify the data set changed
			   VueTrendingAislesDataModel.getInstance(VueApplication.getInstance()).dataObserver();
		 
			   
			break;

		default:
			// we should never have to encounter this!
			break;
		}
	}
	private class DbDataSetter extends AsyncTask<Void, Void, Void> {
		ArrayList<AisleWindowContent> mAislesList;
		public DbDataSetter(ArrayList<AisleWindowContent> aislesList) {
			mAislesList = aislesList;
		}
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
		}
		@Override
		protected Void doInBackground(Void... params) {
			DataBaseManager
			.addTrentingAislesFromServerToDB(VueApplication.getInstance(),mAislesList);
			return null;
		}
		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
		}
	}
}
