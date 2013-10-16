package com.lateralthoughts.vue.utils;

import java.net.URL;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lateralthoughts.vue.AisleImageDetails;
import com.lateralthoughts.vue.AisleManager.ImageAddedCallback;
import com.lateralthoughts.vue.AisleWindowContent;
import com.lateralthoughts.vue.R;
import com.lateralthoughts.vue.VueApplication;
import com.lateralthoughts.vue.VueConstants;
import com.lateralthoughts.vue.VueLandingPageActivity;
import com.lateralthoughts.vue.VueTrendingAislesDataModel;
import com.lateralthoughts.vue.connectivity.DataBaseManager;
import com.lateralthoughts.vue.domain.VueImage;
import com.lateralthoughts.vue.parser.Parser;

public class AddImageToAisleBackgroundThread implements Runnable,
		CountingStringEntity.UploadListener {
	private NotificationManager mNotificationManager;
	private Notification mNotification;
	private int mLastPercent = 0;
	private VueImage mVueImage = null;
	private String mResponseMessage = null;
	private ImageAddedCallback mImageAddedCallback = null;
	private boolean mFromDetailsScreenFlag;

	@SuppressWarnings("static-access")
	public AddImageToAisleBackgroundThread(VueImage vueImage,
			ImageAddedCallback callback, boolean fromDetailsScreenFlag) {
		mNotificationManager = (NotificationManager) VueApplication
				.getInstance().getSystemService(
						VueApplication.getInstance().NOTIFICATION_SERVICE);
		mVueImage = vueImage;
		mImageAddedCallback = callback;
		mFromDetailsScreenFlag = fromDetailsScreenFlag;
	}

	@Override
	public void onChange(int percent) {
		if (percent > mLastPercent) {
			mNotification.contentView.setProgressBar(R.id.progressBar1, 100,
					percent, false);
			mNotificationManager.notify(
					VueConstants.ADD_IMAGE_TO_AISLE_NOTIFICATION_ID,
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
					"Adding Image to Aisle to server",
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
					VueConstants.ADD_IMAGE_TO_AISLE_NOTIFICATION_ID,
					mNotification);
			ObjectMapper mapper = new ObjectMapper();
			URL url = new URL(UrlConstants.CREATE_IMAGE_RESTURL);
			HttpPut httpPut = new HttpPut(url.toString());
			CountingStringEntity entity = new CountingStringEntity(
					mapper.writeValueAsString(mVueImage));
			entity.setUploadListener(this);
			System.out.println("Aisle create request: "
					+ mapper.writeValueAsString(mVueImage));
			entity.setContentType("application/json;charset=UTF-8");
			entity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,
					"application/json;charset=UTF-8"));
			httpPut.setEntity(entity);

			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpResponse response = httpClient.execute(httpPut);
			if (response.getEntity() != null
					&& response.getStatusLine().getStatusCode() == 200) {
				mNotification.setLatestEventInfo(VueApplication.getInstance(),
						"Image is Added.", "Image is Added to Aisle.",
						contentIntent);
				mNotification.flags |= Notification.FLAG_AUTO_CANCEL;
				mNotificationManager.notify(
						VueConstants.ADD_IMAGE_TO_AISLE_NOTIFICATION_ID,
						mNotification);
				mResponseMessage = EntityUtils.toString(response.getEntity());
				System.out.println("AISLE CREATED Response: "
						+ mResponseMessage);
				Log.i("myailsedebug",
						"myailsedebug: recieved response*******:  "
								+ mResponseMessage);
			} else {
				mNotification.setLatestEventInfo(VueApplication.getInstance(),
						"Uploading Failed.", "Image adding is failed.",
						contentIntent);
				mNotification.flags |= Notification.FLAG_AUTO_CANCEL;
				mNotificationManager.notify(
						VueConstants.ADD_IMAGE_TO_AISLE_NOTIFICATION_ID,
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

							if (!mFromDetailsScreenFlag) {
								Log.i("addimagefuncitonality",
										"addimagefuncitonality jsonArray response: "
												+ mFromDetailsScreenFlag);
								try {
									AisleImageDetails aisleImageDetails = new Parser()
											.parseAisleImageData(new JSONObject(
													mResponseMessage));
									if (aisleImageDetails != null) {
										AisleWindowContent aisleWindowContent = VueTrendingAislesDataModel
												.getInstance(
														VueApplication
																.getInstance())
												.getAisleAt(
														aisleImageDetails.mOwnerAisleId);
										aisleWindowContent
												.prepareCustomUrl(aisleImageDetails);
										Log.i("Ailse Manager",
												"customimageurl add image to aisle: "
														+ aisleImageDetails.mCustomImageUrl);

										int returnValue = aisleWindowContent
												.getAisleImageForImageUrl(aisleImageDetails.mImageUrl);
										if (returnValue == -1) {
											aisleWindowContent.getImageList()
													.add(aisleImageDetails);
										} else {
											aisleWindowContent.getImageList()
													.get(returnValue).mId = aisleImageDetails.mId;
										}
										VueTrendingAislesDataModel.getInstance(
												VueApplication.getInstance())
												.dataObserver();

										/*
										 * String s[] = {
										 * aisleImageDetails.mOwnerAisleId };
										 * ArrayList<AisleWindowContent> list =
										 * DataBaseManager .getInstance(
										 * VueApplication .getInstance())
										 * .getAislesFromDB(s); if (list !=
										 * null) { list.get(0).getImageList()
										 * .add(aisleImageDetails);
										 * DataBaseManager .getInstance(
										 * VueApplication .getInstance())
										 * .addTrentingAislesFromServerToDB(
										 * VueApplication .getInstance(), list);
										 * }
										 */

									}
								} catch (JSONException e) {
									e.printStackTrace();
								}
							}
						} else {
							Toast.makeText(VueApplication.getInstance(),
									"Add Image To Aisle in server is failed.",
									Toast.LENGTH_LONG).show();

						}
					}
				});
	}
}
