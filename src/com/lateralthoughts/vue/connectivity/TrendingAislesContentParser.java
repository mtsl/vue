package com.lateralthoughts.vue.connectivity;

import java.util.ArrayList;

import android.os.AsyncTask;
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

			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					final ArrayList<AisleWindowContent> aislesList = new Parser()
							.parseTrendingAislesResultData(
									resultData.getString("result"),
									resultData.getBoolean("loadMore"));

					/*
					 * if (aislesList != null && aislesList.size() > 0) { int
					 * size = aislesList.size(); Log.i("ailsesize",
					 * "ailseListSize: " + size); new
					 * DbDataSetter(aislesList).execute(); }
					 */

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

					/*
					 * if (aislesList.size() == 0) {
					 * VueTrendingAislesDataModel.getInstance(
					 * VueApplication.getInstance())
					 * .setMoreDataAVailable(false); // TODO }
					 */

					if (refreshListFlag) {
						VueLandingPageActivity.landingPageActivity
								.runOnUiThread(new Runnable() {

									@Override
									public void run() {
										VueTrendingAislesDataModel
												.getInstance(VueApplication
														.getInstance()).loadOnRequest = true;
										if (aislesList != null
												&& aislesList.size() > 0) {
											for (int i = 0; i < aislesList
													.size(); i++) {
												VueTrendingAislesDataModel
														.getInstance(
																VueApplication
																		.getInstance())
														.addItemToList(
																aislesList
																		.get(i)
																		.getAisleContext().mAisleId,
																aislesList
																		.get(i));
											}

											VueTrendingAislesDataModel
													.getInstance(
															VueApplication
																	.getInstance())
													.dismissProgress();
											// if this is the first set of data
											// we
											// are receiving
											// go
											// ahead
											// notify the data set changed
											VueTrendingAislesDataModel
													.getInstance(
															VueApplication
																	.getInstance())
													.dataObserver();
										}
									}
								});
					}
					/*
					 * TrendingAislesContentParser mTrendingAislesParser = new
					 * TrendingAislesContentParser( new Handler(), 1);
					 * mTrendingAislesParser.send(1, null);
					 */
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
			DataBaseManager.addTrentingAislesFromServerToDB(
					VueApplication.getInstance(), mAislesList);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			mAislesList.clear();
			super.onPostExecute(result);
		}
	}
}
