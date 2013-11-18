package com.lateralthoughts.vue.utils;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.app.NotificationManager;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lateralthoughts.vue.AisleImageDetails;
import com.lateralthoughts.vue.AisleWindowContent;
import com.lateralthoughts.vue.VueApplication;
import com.lateralthoughts.vue.VueLandingPageActivity;
import com.lateralthoughts.vue.VueTrendingAislesDataModel;
import com.lateralthoughts.vue.connectivity.DataBaseManager;
import com.lateralthoughts.vue.domain.Image;

public class DeleteImageFromAisle implements Runnable,
		CountingStringEntity.UploadListener {
	Image image;
	private NotificationManager mNotificationManager;

	@SuppressWarnings("static-access")
	public DeleteImageFromAisle(Image image) {
		mNotificationManager = (NotificationManager) VueApplication
				.getInstance().getSystemService(
						VueApplication.getInstance().NOTIFICATION_SERVICE);
		this.image = image;

	}

	@Override
	public void onChange(int percent) {
		// TODO Auto-generated method stub

	}

	@Override
	public void run() {
		try {
			ObjectMapper mapper = new ObjectMapper();
			Boolean result = new Boolean(false);
			URL url = new URL(UrlConstants.DELETE_IMAGE_RESTURL);
			HttpPut httpPut = new HttpPut(url.toString());
			String request = mapper.writeValueAsString(image);
			System.out.println("Request: " + request);
			StringEntity entity = new StringEntity(request);
			entity.setContentType("application/json;charset=UTF-8");
			entity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,
					"application/json;charset=UTF-8"));
			httpPut.setEntity(entity);
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpResponse response = httpClient.execute(httpPut);
			if (response.getEntity() != null) {
				String responseMessage = EntityUtils.toString(response
						.getEntity());
				System.out.println("Response: " + responseMessage);
				result = (new ObjectMapper()).readValue(responseMessage,
						Boolean.class);
			}
			if (result) {
				VueLandingPageActivity.landingPageActivity
						.runOnUiThread(new Runnable() {

							@Override
							public void run() {
								System.out.println("success response");
								deleteImageFromAisleList(
										String.valueOf(image.getId()),
										String.valueOf(image.getOwnerAisleId()));
								VueTrendingAislesDataModel.getInstance(
										VueApplication.getInstance())
										.dataObserver();
								deleteImageFromDb(String.valueOf(image.getId()));

							}
						});
			} else {
				Log.e("imageDelete", "imageDeletion failed");
			}
		} catch (MalformedURLException e) {
			Log.e("imageDelete", "imageDelete failed");
			e.printStackTrace();
		} catch (Exception e) {
			Log.e("imageDelete", "imageDelete failed");
			e.printStackTrace();
		}

	}

	/**
	 * 
	 * @param imageId
	 * @param aisleId
	 *            delete the image from the aisle if it is showing in the UI.
	 */
	private void deleteImageFromAisleList(String imageId, String aisleId) {
		AisleWindowContent aisleItem = VueTrendingAislesDataModel.getInstance(
				VueApplication.getInstance()).getAisleAt(aisleId);
		aisleItem = VueTrendingAislesDataModel.getInstance(
				VueApplication.getInstance()).getAisleFromList(aisleItem);
		if (aisleItem != null) {
			AisleImageDetails imageDetails = null;
			for (int i = 0; i < aisleItem.getImageList().size(); i++) {
				imageDetails = aisleItem.getImageList().get(i);
				if (imageId.equalsIgnoreCase(imageDetails.mId)) {
					aisleItem.getImageList().remove(i);
					Log.e("imageDelete",
							"imageDeletion  succesully completes in  UI");
					break;
				}
			}

		} else {
			Log.e("imageDelete",
					"imageDeletion  now this aisle is not showing in UI");
		}
		if (aisleItem.getImageList().size() == 0) {
			int position = VueTrendingAislesDataModel.getInstance(
					VueApplication.getInstance()).getAilsePosition(aisleItem);
			VueTrendingAislesDataModel
					.getInstance(VueApplication.getInstance())
					.removeAisleFromList(position);
			DataBaseManager
					.getInstance(VueApplication.getInstance())
					.deleteOutDatedAisles(VueApplication.getInstance(), aisleId);
		}
	}

	private void deleteImageFromDb(String imageId) {
		DataBaseManager.getInstance(VueApplication.getInstance()).deleteImage(
				imageId);
	}
}
