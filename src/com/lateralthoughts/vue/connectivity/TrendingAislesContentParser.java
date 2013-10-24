package com.lateralthoughts.vue.connectivity;

import java.util.ArrayList;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;

import com.lateralthoughts.vue.*;

import com.lateralthoughts.vue.AisleWindowContent;
import com.lateralthoughts.vue.R;
import com.lateralthoughts.vue.VueApplication;
import com.lateralthoughts.vue.VueConstants;
import com.lateralthoughts.vue.VueLandingPageActivity;
import com.lateralthoughts.vue.VueTrendingAislesDataModel;
import com.lateralthoughts.vue.parser.Parser;

import java.util.ArrayList;

public class TrendingAislesContentParser extends ResultReceiver {
	private int mState;

	// this variable above is usually set to one of the following values

	public TrendingAislesContentParser(Handler handler, int reciverSource) {
		super(handler);
		mState = reciverSource;
	}

	@Override
  protected void onReceiveResult(int resultCode, final Bundle resultData) {
    switch (mState) {
      case VueConstants.AISLE_TRENDING_LIST_DATA:
        long elapsedTime = System.currentTimeMillis()
            - VueApplication.getInstance().mLastRecordedTime;
        Log.e("PERF_VUE",
            "AISLE_TRENDING_LIST_DATA is the state. Received content. Time elapsed = "
                + elapsedTime);
        VueApplication.getInstance().mLastRecordedTime = System
            .currentTimeMillis();

        Thread t = new Thread(new Runnable() {
          @Override
          public void run() {
        	
            final ArrayList<AisleWindowContent> aislesList = new Parser()
                .parseTrendingAislesResultData(resultData.getString("result"),
                    resultData.getBoolean("loadMore"));
            
         
            DataBaseManager
                .getInstance(VueApplication.getInstance())
                .addTrentingAislesFromServerToDB(VueApplication.getInstance(),
                    aislesList, VueTrendingAislesDataModel.getInstance(
                        VueApplication.getInstance()).getNetworkHandler().mOffset,
                    DataBaseManager.TRENDING);

            Log.i("ailsesize",
                "Suru comment show: " + aislesList.size());
             

            boolean refreshListFlag = true;
          /*  if(!VueTrendingAislesDataModel.getInstance(VueApplication.getInstance()).isFromDb) {*/
            if (VueLandingPageActivity.landingPageActivity != null
                && (VueLandingPageActivity.mVueLandingActionbarScreenName
                    .getText().toString().equals(VueApplication.getInstance()
                    .getString(R.string.sidemenu_option_Trending_Aisles)))) {
              if (VueApplication.getInstance().mIsTrendingSelectedFromBezelMenuFlag) {
                VueApplication.getInstance().mIsTrendingSelectedFromBezelMenuFlag = false;
                if (resultData.getInt("offset") == 0) {
                  refreshListFlag = true;
                }
              } else {
                refreshListFlag = true;
              }
            }
            VueLandingPageActivity.landingPageActivity
                .runOnUiThread(new Runnable() {
                  @Override
                  public void run() {
                    VueTrendingAislesDataModel.getInstance(
                        VueApplication.getInstance()).dismissProgress();

                  }
                });
         /* } else {
         	  VueTrendingAislesDataModel.getInstance(VueApplication.getInstance()).loadOnRequest = true;
         	  Log.i("listmovingissue", "listmovingissue***: dbcase");
           }*/
            
            if (refreshListFlag) {
              VueLandingPageActivity.landingPageActivity
                  .runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                      Log.e(
                          "TrendingAislesContentParser",
                          "Surendra check check Screen Name: "
                              + VueLandingPageActivity.mVueLandingActionbarScreenName);
                      if (VueLandingPageActivity.getScreenName().equals(
                          VueApplication.getInstance().getString(
                              R.string.sidemenu_sub_option_Bookmarks))) {
                        VueTrendingAislesDataModel.getInstance(VueApplication
                            .getInstance()).loadOnRequest = false;
                      } else {
                        VueTrendingAislesDataModel.getInstance(VueApplication
                            .getInstance()).loadOnRequest = true;
                        if (aislesList != null && aislesList.size() > 0) {
                          for (int i = 0; i < aislesList.size(); i++) {
                            VueTrendingAislesDataModel model = VueTrendingAislesDataModel
                                .getInstance(VueApplication.getInstance());
                            model.addItemToList(aislesList.get(i)
                                .getAisleContext().mAisleId, aislesList.get(i));
                          }

                          VueTrendingAislesDataModel.getInstance(
                              VueApplication.getInstance()).dismissProgress();
                          // if this is the first set of data we are receiving
                          // go ahead
                          // notify the data set changed
                          VueTrendingAislesDataModel.getInstance(
                              VueApplication.getInstance()).dataObserver();
                        }
                      }
                    }
                  });
            }
            
        }
        });
        t.start();
        break;
      case VueConstants.AISLE_TRENDING_PARSED_DATA:
        VueTrendingAislesDataModel.getInstance(VueApplication.getInstance())
            .dismissProgress();
        // if this is the first set of data we are receiving
        // go
        // ahead
        // notify the data set changed
        VueTrendingAislesDataModel.getInstance(VueApplication.getInstance())
            .dataObserver();
        break;
      default:
        // we should never have to encounter this!
        break;
    }
  }

	/*
	 * private class DbDataSetter extends AsyncTask<Void, Void, Void> {
	 * ArrayList<AisleWindowContent> mAislesList;
	 * 
	 * public DbDataSetter(ArrayList<AisleWindowContent> aislesList) {
	 * mAislesList = aislesList; }
	 * 
	 * @Override protected void onPreExecute() { // TODO Auto-generated method
	 * stub super.onPreExecute(); }
	 * 
	 * @Override protected Void doInBackground(Void... params) {
	 * DataBaseManager.
	 * getInstance(VueApplication.getInstance()).addTrentingAislesFromServerToDB
	 * ( VueApplication.getInstance(), mAislesList); return null; }
	 * 
	 * @Override protected void onPostExecute(Void result) {
	 * mAislesList.clear(); super.onPostExecute(result); } }
	 */

}
