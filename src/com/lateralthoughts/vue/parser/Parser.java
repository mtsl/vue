package com.lateralthoughts.vue.parser;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.util.Log;
import com.lateralthoughts.vue.AisleContext;
import com.lateralthoughts.vue.AisleImageDetails;
import com.lateralthoughts.vue.AisleWindowContent;
import com.lateralthoughts.vue.VueApplication;
import com.lateralthoughts.vue.VueConstants;
import com.lateralthoughts.vue.VueTrendingAislesDataModel;
import com.lateralthoughts.vue.connectivity.DataBaseManager;

public class Parser {
	// ========================= START OF PARSING TAGS
	// ========================================================
	// the following strings are pre-defined to help with JSON parsing
	// the tags defined here should be in sync the API documentation for the
	// backend

	// TODO: code cleanup. This function here allocated the new
	// AisleImageObjects but re-uses the
	// imageItemsArray. Instead the called function clones and keeps a copy.
	// This is pretty inconsistent.
	// Let the allocation happen in one place for both items. Fix this!
	public ArrayList<AisleWindowContent> parseTrendingAislesResultData(
			String resultString, boolean loadMore) {

		ArrayList<AisleWindowContent> aisleWindowContentList = new ArrayList<AisleWindowContent>();

		JSONArray contentArray = null;

		contentArray = handleResponce(resultString, loadMore);
		if (contentArray == null) {
			return aisleWindowContentList;
		}
		Log.i("useraisleparsing",
				"imageListresponse imageListResponse response: " + contentArray);
		try {
			aisleWindowContentList = parseAisleInformation(contentArray);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return aisleWindowContentList;
	}

	private JSONArray handleResponce(String resultString, boolean loadMore) {
		JSONArray contentArray = null;
		try {
			contentArray = new JSONArray(resultString);
			if (!loadMore) {
				VueTrendingAislesDataModel.getInstance(
						VueApplication.getInstance()).clearContent();
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return contentArray;
	}

	public AisleWindowContent getAisleCotent(String response) {
		AisleWindowContent aisleWindowContent = null;
		AisleContext aisleContext = null;
		ArrayList<AisleImageDetails> arrayList = new ArrayList<AisleImageDetails>();

		if (null != response) {

			try {
				JSONObject jsonObject = new JSONObject(response);
				aisleContext = parseAisleData(jsonObject);
				AisleImageDetails aisleImageDetails = parseAisleImageData(jsonObject
						.getJSONObject("aisleImage"));
				if (aisleImageDetails.mImageUrl != null
						&& aisleImageDetails.mImageUrl.trim().length() > 0) {
					arrayList.add(aisleImageDetails);
					aisleWindowContent = VueTrendingAislesDataModel
							.getInstance(VueApplication.getInstance())
							.getAisle(aisleContext.mUserId);
					aisleWindowContent.addAisleContent(aisleContext, arrayList);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return aisleWindowContent;
	}

	public AisleImageDetails parseAisleImageData(JSONObject jsonObject)
			throws JSONException {
		AisleImageDetails aisleImageDetails = new AisleImageDetails();
		aisleImageDetails.mId = jsonObject
				.getString(VueConstants.AISLE_IMAGE_ID);
		aisleImageDetails.mOwnerUserId = jsonObject
				.getString(VueConstants.AISLE_IMAGE_OWNERUSER_ID);
		aisleImageDetails.mOwnerAisleId = jsonObject
				.getString(VueConstants.AISLE_IMAGE_OWNER_AISLE_ID);
		aisleImageDetails.mDetalsUrl = jsonObject
				.getString(VueConstants.AISLE_IMAGE_DETAILS_URL);
		aisleImageDetails.mAvailableHeight = jsonObject
				.getInt(VueConstants.AISLE_IMAGE_HEIGHT);
		aisleImageDetails.mAvailableWidth = jsonObject
				.getInt(VueConstants.AISLE_IMAGE_WIDTH);
		aisleImageDetails.mImageUrl = jsonObject
				.getString(VueConstants.AISLE_IMAGE_IMAGE_URL);
		aisleImageDetails.mRating = jsonObject
				.getString(VueConstants.AISLE_IMAGE_RATING);
		aisleImageDetails.mStore = jsonObject
				.getString(VueConstants.AISLE_IMAGE_STORE);
		aisleImageDetails.mTitle = jsonObject
				.getString(VueConstants.AISLE_IMAGE_TITLE);
		return aisleImageDetails;
	}

	public ArrayList<AisleWindowContent> getUserAilseLIst(String jsonArray) {
		ArrayList<AisleWindowContent> aisleWindowContentList = new ArrayList<AisleWindowContent>();
		try {

			JSONObject jsonResponse = new JSONObject(new String(jsonArray));
			JSONArray aisleArray = jsonResponse.getJSONArray("aisles");

			if (aisleArray != null) {
				aisleWindowContentList = parseAisleInformation(aisleArray);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return aisleWindowContentList;
	}

	public ArrayList<AisleImageDetails> getImagesForAisleId(String aisleId)
			throws Exception {
		ArrayList<AisleImageDetails> imageList = new ArrayList<AisleImageDetails>();
		String imageRequestUrl = VueConstants.GET_IMAGES_FOR_AISLE + aisleId;
		URL url = new URL(imageRequestUrl);
		HttpGet httpGet = new HttpGet(url.toString());
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpResponse response = httpClient.execute(httpGet);
		if (response.getEntity() != null
				&& response.getStatusLine().getStatusCode() == 200) {
			String responseMessage = EntityUtils.toString(response.getEntity());
			if (responseMessage != null) {
				Log.i("Parser", responseMessage);
				JSONObject mainJsonObject = new JSONObject(responseMessage);
				JSONArray jsonArray = mainJsonObject.getJSONArray("images");
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject jsonObject = jsonArray.getJSONObject(i);
					AisleImageDetails aisleImageDetails = parseAisleImageData(jsonObject);
					if (aisleImageDetails.mImageUrl != null
							&& aisleImageDetails.mImageUrl.trim().length() > 0) {
						imageList.add(aisleImageDetails);
					}
				}
			}
		}
		return imageList;
	}

	private ArrayList<AisleWindowContent> parseAisleInformation(
			JSONArray jsonArray) throws JSONException {
		ArrayList<AisleWindowContent> aisleWindowContentList = new ArrayList<AisleWindowContent>();
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject ailseItem = jsonArray.getJSONObject(i);
			AisleContext aisleContext = parseAisleData(ailseItem);
			ArrayList<AisleImageDetails> aisleImageDetailsList = null;
			try {
				aisleImageDetailsList = getImagesForAisleId(aisleContext.mAisleId);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (aisleImageDetailsList != null
					&& aisleImageDetailsList.size() > 0) {
				AisleWindowContent aisleWindowContent = VueTrendingAislesDataModel
						.getInstance(VueApplication.getInstance()).getAisle(
								aisleContext.mAisleId);
				aisleWindowContent.addAisleContent(aisleContext,
						aisleImageDetailsList);
				aisleWindowContentList.add(aisleWindowContent);
			}
		}
		return aisleWindowContentList;
	}

	private AisleContext parseAisleData(JSONObject josnObject) {
		AisleContext aisleContext = new AisleContext();
		try {
			aisleContext.mAisleId = josnObject.getString(VueConstants.AISLE_ID);
			aisleContext.mCategory = josnObject
					.getString(VueConstants.AISLE_CATEGORY);
			aisleContext.mLookingForItem = josnObject
					.getString(VueConstants.AISLE_LOOKINGFOR);
			aisleContext.mName = josnObject.getString(VueConstants.AISLE_NAME);
			aisleContext.mOccasion = josnObject
					.getString(VueConstants.AISLE_OCCASSION);
			aisleContext.mUserId = josnObject
					.getString(VueConstants.AISLE_OWNER_USER_ID);
			String firstName = josnObject
					.getString(VueConstants.AISLE_OWNER_FIRSTNAME);
			String lastName = josnObject
					.getString(VueConstants.AISLE_OWNER_LASTNAME);
			if (firstName == null || firstName.equals("null")) {
				aisleContext.mFirstName = " ";
				firstName = null;
			} else {
				aisleContext.mFirstName = firstName;
			}
			if (lastName == null || lastName.equals("null")) {
				aisleContext.mLastName = " ";
				lastName = null;
			} else {
				aisleContext.mLastName = lastName;
			}
			if (firstName == null && lastName == null) {
				aisleContext.mFirstName = "Anonymous";
			}
			aisleContext.mBookmarkCount = josnObject
					.getInt(VueConstants.AISLE_BOOKMARK_COUNT);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return aisleContext;
	}
}
