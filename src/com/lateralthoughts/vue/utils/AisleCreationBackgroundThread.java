package com.lateralthoughts.vue.utils;

import java.net.URL;
import java.util.ArrayList;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flurry.android.FlurryAgent;
import com.lateralthoughts.vue.AisleWindowContent;
import com.lateralthoughts.vue.R;
import com.lateralthoughts.vue.VueApplication;
import com.lateralthoughts.vue.VueConstants;
import com.lateralthoughts.vue.VueLandingPageActivity;
import com.lateralthoughts.vue.VueTrendingAislesDataModel;
import com.lateralthoughts.vue.AisleManager.AisleUpdateCallback;
import com.lateralthoughts.vue.connectivity.DataBaseManager;
import com.lateralthoughts.vue.domain.Aisle;
import com.lateralthoughts.vue.parser.Parser;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

public class AisleCreationBackgroundThread implements Runnable,
		CountingStringEntity.UploadListener {
	private NotificationManager mNotificationManager;
	private Notification mNotification;
	private int mLastPercent = 0;
	private Aisle mAisle = null;
	private String mResponseMessage = null;
	private AisleUpdateCallback mAisleUpdateCallback = null;

	@SuppressWarnings("static-access")
	public AisleCreationBackgroundThread(Aisle aisle,
			AisleUpdateCallback callback) {
		mNotificationManager = (NotificationManager) VueApplication
				.getInstance().getSystemService(
						VueApplication.getInstance().NOTIFICATION_SERVICE);
		mAisle = aisle;
		mAisleUpdateCallback = callback;
	}

	@Override
	public void onChange(int percent) {
		if (percent > mLastPercent) {
			mNotification.contentView.setProgressBar(R.id.progressBar1, 100,
					percent, false);
			mNotificationManager.notify(
					VueConstants.CREATE_AISLE_NOTIFICATION_ID, mNotification);
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
					"Uploading Aisle to server", System.currentTimeMillis());
			mNotification.flags = mNotification.flags
					| Notification.FLAG_ONGOING_EVENT;
			mNotification.contentView = new RemoteViews(VueApplication
					.getInstance().getPackageName(),
					R.layout.upload_progress_bar);
			mNotification.contentIntent = contentIntent;
			mNotification.contentView.setProgressBar(R.id.progressBar1, 100, 0,
					false);
			mNotificationManager.notify(
					VueConstants.CREATE_AISLE_NOTIFICATION_ID, mNotification);
			ObjectMapper mapper = new ObjectMapper();
			URL url = new URL(UrlConstants.CREATE_AISLE_RESTURL);
			HttpPut httpPut = new HttpPut(url.toString());
			CountingStringEntity entity = new CountingStringEntity(
					mapper.writeValueAsString(mAisle));
			entity.setUploadListener(this);
			System.out.println("Aisle create request: "
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
						"Uploading Completed.", "Aisle is uploaded to server.",
						contentIntent);
				mNotification.flags |= Notification.FLAG_AUTO_CANCEL;
				mNotificationManager.notify(
						VueConstants.CREATE_AISLE_NOTIFICATION_ID,
						mNotification);
				mResponseMessage = EntityUtils.toString(response.getEntity());
				System.out.println("AISLE CREATED Response: "
						+ mResponseMessage);
				Log.i("myailsedebug",
						"myailsedebug: recieved response*******:  "
								+ mResponseMessage);
			} else {
				mNotification.setLatestEventInfo(VueApplication.getInstance(),
						"Uploading Failed.",
						"Aisle is not uploaded to server.", contentIntent);
				mNotification.flags |= Notification.FLAG_AUTO_CANCEL;
				mNotificationManager.notify(
						VueConstants.CREATE_AISLE_NOTIFICATION_ID,
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
								// JSONObject userInfo = new
								// JSONObject(jsonArray);

								AisleWindowContent aileItem = new Parser()
										.getAisleCotent(mResponseMessage);
								VueTrendingAislesDataModel
										.getInstance(
												VueApplication.getInstance())
										.addItemToListAt(
												aileItem.getAisleContext().mAisleId,
												aileItem, 0);
								VueTrendingAislesDataModel.getInstance(
										VueApplication.getInstance())
										.dataObserver();
								ArrayList<AisleWindowContent> list = new ArrayList<AisleWindowContent>();
								list.add(aileItem);
								DataBaseManager.getInstance(
										VueApplication.getInstance())
										.addTrentingAislesFromServerToDB(
												VueApplication.getInstance(),
												list);
								// JSONObject user =
								// userInfo.getJSONObject("user");
								// TODO: GET THE AISLE OBJECT FROM
								// THE PARSER CLASE SEND
								// THE AISLE AND AISLE ID BACK.
								mAisleUpdateCallback.onAisleUpdated(aileItem
										.getAisleContext().mAisleId);
								FlurryAgent.logEvent("Create_Aisle_Success");
								// VueTrendingAislesDataModel.getInstance(VueApplication.getInstance()).getNetworkHandler().requestAislesByUser();
							} catch (Exception ex) {
								Log.e("Profiling",
										"Profiling : onResponse() **************** error");
								ex.printStackTrace();
							}
						} else {
							Toast.makeText(VueApplication.getInstance(),
									"New Aisle Creation in server is failed.",
									Toast.LENGTH_LONG).show();

						}

						// ///////////////////////////////////////////////////////////

					}
				});
	}
}
