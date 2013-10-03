package com.lateralthoughts.vue.connectivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import com.lateralthoughts.vue.*;
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
            long elapsedTime = System.currentTimeMillis() - VueApplication.getInstance().mLastRecordedTime;
            Log.e("PERF_VUE","AISLE_TRENDING_LIST_DATA is the state. Received content. Time elapsed = " + elapsedTime);
            VueApplication.getInstance().mLastRecordedTime = System.currentTimeMillis();

			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					final ArrayList<AisleWindowContent> aislesList = new Parser()
							.parseTrendingAislesResultData(
									resultData.getString("result"),
									resultData.getBoolean("loadMore"));
					Log.i("dbInsert", "dbInsert1 aislesListSize: "+aislesList.size());
					DataBaseManager.getInstance(VueApplication.getInstance()).addTrentingAislesFromServerToDB(
							VueApplication.getInstance(), aislesList);

					Log.i("ailsesize", "ailseListSizemaintrending: "
							+ aislesList.size());

					boolean refreshListFlag = false;

					if (VueLandingPageActivity.landingPageActivity != null
							&& !(VueLandingPageActivity.mVueLandingActionbarScreenName
									.getText().toString()
									.equals(VueApplication
											.getInstance()
											.getString(
													R.string.sidemenu_option_My_Aisles)))) {
						if (VueApplication.getInstance().mIsTrendingSelectedFromBezelMenuFlag) {
							VueApplication.getInstance().mIsTrendingSelectedFromBezelMenuFlag = false;
							if (resultData.getInt("offset") == 0) {
								refreshListFlag = true;
							}
						} else {
							refreshListFlag = true;
						}
					}
					VueLandingPageActivity.landingPageActivity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							VueTrendingAislesDataModel.getInstance(VueApplication.getInstance()).dismissProgress();
							
						}
					});
					if (refreshListFlag) {
						VueLandingPageActivity.landingPageActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                VueTrendingAislesDataModel.getInstance(VueApplication.getInstance()).loadOnRequest = true;
                                if (aislesList != null && aislesList.size() > 0) {
                                    for (int i = 0; i < aislesList.size(); i++) {
                                        VueTrendingAislesDataModel model = VueTrendingAislesDataModel.getInstance(VueApplication.getInstance());
                                        model.addItemToList(aislesList.get(i).getAisleContext().mAisleId,aislesList.get(i));
                                    }

                                    VueTrendingAislesDataModel.getInstance(VueApplication.getInstance()).dismissProgress();
                                    //if this is the first set of data we are receiving go ahead
                                    //notify the data set changed
                                    VueTrendingAislesDataModel.getInstance(VueApplication.getInstance()).dataObserver();
                                }
                            }
                        });
					}
				}
			});
			t.start();
			break;
		case VueConstants.AISLE_TRENDING_PARSED_DATA:
			VueTrendingAislesDataModel
					.getInstance(VueApplication.getInstance())
					.dismissProgress();
			// if this is the first set of data we are receiving
			// go
			// ahead
			// notify the data set changed
			VueTrendingAislesDataModel
					.getInstance(VueApplication.getInstance()).dataObserver();
			break;
		default:
			// we should never have to encounter this!
			break;
		}
	}
}
