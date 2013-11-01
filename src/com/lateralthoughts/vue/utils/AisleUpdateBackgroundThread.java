package com.lateralthoughts.vue.utils;

import java.net.URL;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flurry.android.FlurryAgent;
import com.lateralthoughts.vue.AisleContext;
import com.lateralthoughts.vue.AisleWindowContent;
import com.lateralthoughts.vue.R;
import com.lateralthoughts.vue.VueApplication;
import com.lateralthoughts.vue.VueConstants;
import com.lateralthoughts.vue.VueLandingPageActivity;
import com.lateralthoughts.vue.VueTrendingAislesDataModel;
import com.lateralthoughts.vue.connectivity.DataBaseManager;
import com.lateralthoughts.vue.domain.Aisle;
import com.lateralthoughts.vue.parser.Parser;

public class AisleUpdateBackgroundThread implements Runnable,
		CountingStringEntity.UploadListener {
	private NotificationManager mNotificationManager;
	private Notification mNotification;
	private int mLastPercent = 0;
	private Aisle mAisle = null;
	private String mResponseMessage = null;

	@SuppressWarnings("static-access")
	public AisleUpdateBackgroundThread(Aisle aisle) {
		mNotificationManager = (NotificationManager) VueApplication
				.getInstance().getSystemService(
						VueApplication.getInstance().NOTIFICATION_SERVICE);
		mAisle = aisle;
	}

	@Override
	public void onChange(int percent) {
		if (percent > mLastPercent) {
			mNotification.contentView.setProgressBar(R.id.progressBar1, 100,
					percent, false);
			mNotificationManager.notify(
					VueConstants.AISLE_INFO_UPLOAD_NOTIFICATION_ID,
					mNotification);
			mLastPercent = percent;
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void run() {
		try {
			Intent notificationIntent = new Intent();
			PendingIntent contentIntent = PendingIntent.getActivity(
					VueApplication.getInstance(), 0, notificationIntent, 0);
			mNotification = new Notification(R.drawable.vue_notification_icon,
					VueApplication.getInstance().getResources()
							.getString(R.string.uploading_mesg),
					System.currentTimeMillis());
			mNotification.flags = mNotification.flags
					| Notification.FLAG_ONGOING_EVENT;
			mNotification.contentView = new RemoteViews(VueApplication
					.getInstance().getPackageName(),
					R.layout.upload_progress_bar);
			mNotification.contentIntent = contentIntent;
			mNotification.contentView.setProgressBar(R.id.progressBar1, 100, 0,
					false);
			mNotificationManager.notify(
					VueConstants.AISLE_INFO_UPLOAD_NOTIFICATION_ID,
					mNotification);
			ObjectMapper mapper = new ObjectMapper();
			URL url = new URL(UrlConstants.UPDATE_AISLE_RESTURL);
			HttpPut httpPut = new HttpPut(url.toString());
			CountingStringEntity entity = new CountingStringEntity(
					mapper.writeValueAsString(mAisle));
			entity.setUploadListener(this);
			System.out.println("Aisle Update request: "
					+ mapper.writeValueAsString(mAisle));
			entity.setContentType("application/json;charset=UTF-8");
			entity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,
					"application/json;charset=UTF-8"));
			httpPut.setEntity(entity);

			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpResponse response = httpClient.execute(httpPut);
			if (response.getEntity() != null
					&& response.getStatusLine().getStatusCode() == 200) {
				mNotification.setLatestEventInfo(VueApplication.getInstance(),
						VueApplication.getInstance().getResources()
						.getString(R.string.upload_successful_mesg), "",
						contentIntent);
				mNotification.flags = Notification.FLAG_AUTO_CANCEL;
				mNotificationManager.notify(
						VueConstants.AISLE_INFO_UPLOAD_NOTIFICATION_ID,
						mNotification);
				mResponseMessage = EntityUtils.toString(response.getEntity());
				System.out
						.println("AISLE UPDATE Response: " + mResponseMessage);
				Log.i("myailsedebug",
						"myailsedebug: recieved response*******:  "
								+ mResponseMessage);
			} else {
				mNotification.setLatestEventInfo(VueApplication.getInstance(),
						VueApplication.getInstance().getResources()
						.getString(R.string.upload_failed_mesg), "",
						contentIntent);
				mNotification.flags = Notification.FLAG_AUTO_CANCEL;
				mNotificationManager.notify(
						VueConstants.AISLE_INFO_UPLOAD_NOTIFICATION_ID,
						mNotification);
				Log.i("myailsedebug",
						"myailsedebug: recieved response******* response code :  "
								+ response.getStatusLine().getStatusCode());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		VueLandingPageActivity.landingPageActivity
				.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (null != mResponseMessage) {

							Log.i("myailsedebug",
									"myailsedebug: recieved response:  "
											+ mResponseMessage);
							try {
								JSONObject jsonObject = new JSONObject(
										mResponseMessage);
								AisleContext aisleContext = new Parser()
										.parseAisleData(jsonObject);
								if (aisleContext != null) {
									if (VueLandingPageActivity.getScreenName()
											.equalsIgnoreCase("Trending")
											|| VueLandingPageActivity
													.getScreenName()
													.equalsIgnoreCase(
															"My Aisles")) {
										AisleWindowContent existedAisle = VueTrendingAislesDataModel
												.getInstance(
														VueApplication
																.getInstance())
												.getAisleAt(
														aisleContext.mAisleId);
										if (existedAisle != null) {
											existedAisle.getAisleContext().mCategory = aisleContext.mCategory;
											existedAisle.getAisleContext().mDescription = aisleContext.mDescription;
											existedAisle.getAisleContext().mName = aisleContext.mName;
											existedAisle.getAisleContext().mLookingForItem = aisleContext.mLookingForItem;
											existedAisle.getAisleContext().mOccasion = aisleContext.mOccasion;
										}
										VueTrendingAislesDataModel.getInstance(
												VueApplication.getInstance())
												.dataObserver();
									}
									DataBaseManager.getInstance(
											VueApplication.getInstance())
											.aisleUpdateToDB(aisleContext);
									FlurryAgent
											.logEvent("Update_Aisle_Success");
								}
							} catch (Exception ex) {
								Log.e("Profiling",
										"Profiling : onResponse() **************** error");
								ex.printStackTrace();
							}
						} else {
							Toast.makeText(
									VueApplication.getInstance(),
									VueApplication
											.getInstance()
											.getResources()
											.getString(
													R.string.upload_failed_mesg),
									Toast.LENGTH_LONG).show();

						}
					}
				});
	}
}
