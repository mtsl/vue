package com.lateralthoughts.vue.connectivity;

import java.util.ArrayList;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;

import com.lateralthoughts.vue.AisleWindowContent;
import com.lateralthoughts.vue.R;
import com.lateralthoughts.vue.VueApplication;
import com.lateralthoughts.vue.VueConstants;
import com.lateralthoughts.vue.VueLandingPageActivity;
import com.lateralthoughts.vue.VueTrendingAislesDataModel;
import com.lateralthoughts.vue.parser.Parser;

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
			VueApplication.getInstance().mLastRecordedTime = System
					.currentTimeMillis();

			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {

					final ArrayList<AisleWindowContent> aislesList = new Parser()
							.parseTrendingAislesResultData(
									resultData.getString("result"),
									resultData.getBoolean("loadMore"));
					int offset = resultData.getInt("offset");

					DataBaseManager.getInstance(VueApplication.getInstance())
							.addTrentingAislesFromServerToDB(
									VueApplication.getInstance(), aislesList,
									offset, DataBaseManager.TRENDING);

					boolean refreshListFlag = true;
					/*
					 * if(!VueTrendingAislesDataModel.getInstance(VueApplication.
					 * getInstance()).isFromDb) {
					 */
					if (VueLandingPageActivity.landingPageActivity != null
							&& VueLandingPageActivity.mLandingScreenName != null
							&& (VueLandingPageActivity.mLandingScreenName
									.equals(VueApplication
											.getInstance()
											.getString(
													R.string.sidemenu_option_Trending_Aisles)))) {
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
											VueApplication.getInstance())
											.dismissProgress();

								}
							});
					if (VueLandingPageActivity.landingPageActivity != null
							&& VueLandingPageActivity.mLandingScreenName != null
							&& (VueLandingPageActivity.mLandingScreenName
									.equals(VueApplication
											.getInstance()
											.getString(
													R.string.sidemenu_sub_option_My_Aisles)))) {
						refreshListFlag = false;
					}

					if (refreshListFlag) {
						VueLandingPageActivity.landingPageActivity
								.runOnUiThread(new Runnable() {
									@Override
									public void run() {

										if (VueLandingPageActivity.mLandingScreenName != null
												&& VueLandingPageActivity.mLandingScreenName
														.equals(VueApplication
																.getInstance()
																.getString(
																		R.string.sidemenu_sub_option_Bookmarks))) {
											VueTrendingAislesDataModel
													.getInstance(VueApplication
															.getInstance()).loadOnRequest = false;
										} else {
											VueTrendingAislesDataModel
													.getInstance(VueApplication
															.getInstance()).loadOnRequest = true;
											if (aislesList != null
													&& aislesList.size() > 0) {
												for (int i = 0; i < aislesList
														.size(); i++) {
													VueTrendingAislesDataModel model = VueTrendingAislesDataModel
															.getInstance(VueApplication
																	.getInstance());
													model.addItemToList(
															aislesList
																	.get(i)
																	.getAisleContext().mAisleId,
															aislesList.get(i));
												}

												VueTrendingAislesDataModel
														.getInstance(
																VueApplication
																		.getInstance())
														.dismissProgress();
												// if this is the first set of
												// data we are receiving
												// go ahead
												// notify the data set changed
												VueTrendingAislesDataModel
														.getInstance(
																VueApplication
																		.getInstance())
														.dataObserver();
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
