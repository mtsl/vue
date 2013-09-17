package com.lateralthoughts.vue.parser;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.lateralthoughts.vue.AisleContext;
import com.lateralthoughts.vue.AisleImageDetails;
import com.lateralthoughts.vue.AisleWindowContent;
import com.lateralthoughts.vue.VueApplication;
import com.lateralthoughts.vue.VueTrendingAislesDataModel;
import com.lateralthoughts.vue.connectivity.DataBaseManager;




public class Parser {
	// ========================= START OF PARSING TAGS
	// ========================================================
	// the following strings are pre-defined to help with JSON parsing
	// the tags defined here should be in sync the API documentation for the
	// backend
	private static final String ITEM_CATEGORY_TAG = "category";
	private static final String LOOKING_FOR_TAG = "lookingFor";
	private static final String OCCASION_TAG = "occasion";
	private static final String CONTENT_ID_TAG = "id";
	private static final String USER_OBJECT_TAG = "user";
	private static final String USER_FIRST_NAME_TAG = "firstName";
	private static final String USER_LAST_NAME_TAG = "lastName";
	private static final String USER_JOIN_TIME_TAG = "joinTime";
	private static final String USER_IMAGES_TAG = "images";
	private static final String USER_IMAGE_ID_TAG = "id";
	private static final String USER_IMAGE_DETALS_TAG = "detailsUrl";
	private static final String USER_IMAGE_URL_TAG = "imageUrl";
	private static final String USER_IMAGE_STORE_TAG = "store";
	private static final String USER_IMAGE_TITLE_TAG = "title";
	private static final String IMAGE_HEIGHT_TAG = "height";
	private static final String IMAGE_WIDTH_TAG = "width";
	// TODO: code cleanup. This function here allocated the new
	// AisleImageObjects but re-uses the
	// imageItemsArray. Instead the called function clones and keeps a copy.
	// This is pretty inconsistent.
	// Let the allocation happen in one place for both items. Fix this!
	@SuppressWarnings("unused")
	public ArrayList<AisleWindowContent> parseTrendingAislesResultData(String resultString,
			boolean loadMore) {
		String category;
		String aisleId;
		String context;
		String occasion;
		String ownerFirstName;
		String ownerLastName;
		long joinTime;
		AisleContext userInfo;

		AisleWindowContent aisleItem = null;
		ArrayList<AisleImageDetails> imageItemsArray = new ArrayList<AisleImageDetails>();
		ArrayList<AisleWindowContent> aisleList = new ArrayList<AisleWindowContent>();
		JSONObject imageItem;
		String imageUrl;
		String imageDetalsUrl;
		String imageId;
		String imageStore;
		String imageTitle;
		AisleImageDetails imageItemDetails;
		JSONArray contentArray = null;
		try {
			contentArray = handleResponce(resultString, loadMore);
			if (contentArray == null) {
				return aisleList;
			}
			for (int i = 0; i < contentArray.length(); i++) {
				userInfo = new AisleContext();
				JSONObject contentItem = contentArray.getJSONObject(i);
				category = contentItem.getString(ITEM_CATEGORY_TAG);
				aisleId = contentItem.getString(CONTENT_ID_TAG);
				userInfo.mAisleId = contentItem.getString(CONTENT_ID_TAG);
				Log.i("aisleId", "aisleId: "+userInfo.mAisleId);
				if(userInfo.mAisleId.equalsIgnoreCase("5741031244955648")){
					Log.i("aisleId", "aisleId: this ailse retrieved successfully*******************************"+userInfo.mAisleId);
				}
				JSONArray imagesArray = contentItem
						.getJSONArray(USER_IMAGES_TAG);

				// within the content item we have a user object
				JSONObject user = contentItem
						.getJSONObject(USER_OBJECT_TAG);
				userInfo.mFirstName = user.getString(USER_FIRST_NAME_TAG);
				userInfo.mLastName = user.getString(USER_LAST_NAME_TAG);
				userInfo.mUserId = user.getString(CONTENT_ID_TAG);
				userInfo.mLookingForItem = contentItem
						.getString(LOOKING_FOR_TAG);
				userInfo.mOccasion = contentItem.getString(OCCASION_TAG);
				userInfo.mCategory = category;
				userInfo.mJoinTime = Long.parseLong(user
						.getString(USER_JOIN_TIME_TAG));
				for (int j = 0; j < imagesArray.length(); j++) {
					imageItemDetails = new AisleImageDetails();
					imageItem = imagesArray.getJSONObject(j);
					imageItemDetails.mDetalsUrl = imageItem
							.getString(USER_IMAGE_DETALS_TAG);
					imageItemDetails.mId = imageItem
							.getString(USER_IMAGE_ID_TAG);
					imageItemDetails.mStore = imageItem
							.getString(USER_IMAGE_STORE_TAG);
					imageItemDetails.mTitle = imageItem
							.getString(USER_IMAGE_TITLE_TAG);
					imageItemDetails.mImageUrl = imageItem
							.getString(USER_IMAGE_URL_TAG);
					imageItemDetails.mAvailableHeight = imageItem
							.getInt(IMAGE_HEIGHT_TAG);
					imageItemDetails.mAvailableWidth = imageItem
							.getInt(IMAGE_WIDTH_TAG);
					imageItemsArray.add(imageItemDetails);
				}
				
				aisleItem = VueTrendingAislesDataModel.getInstance(VueApplication.getInstance()).getAisleItem(aisleId);
				aisleItem.addAisleContent(userInfo, imageItemsArray);
				aisleList.add(aisleItem);
				imageItemsArray.clear();
			}
			VueTrendingAislesDataModel.getInstance(VueApplication.getInstance()).dismissProgress();
		} catch (JSONException ex1) {
			VueTrendingAislesDataModel.getInstance(VueApplication.getInstance()).dismissProgress();
		return aisleList;
	}
		return aisleList;
	}
	private JSONArray handleResponce(String resultString, boolean loadMore) {
		JSONArray contentArray = null;
		try {
			contentArray = new JSONArray(resultString);
			if (!loadMore) {
				Log.i("datarequest", "datarequest new request clear data in list");
				VueTrendingAislesDataModel.getInstance(VueApplication.getInstance()).clearContent();
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return contentArray;
	}
	
public AisleContext getAisleCotent(String jsonArray){
	AisleContext aisleContext = new AisleContext();
    if (null != jsonArray) {
        
        try{
            JSONObject userInfo = new JSONObject(jsonArray);
            aisleContext.mAisleId = userInfo.getString(CONTENT_ID_TAG);
            aisleContext.mCategory = userInfo.getString(ITEM_CATEGORY_TAG);
            aisleContext.mOccasion = userInfo.getString(OCCASION_TAG);
            aisleContext.mLookingForItem = userInfo
					.getString(LOOKING_FOR_TAG);
            aisleContext.mUserId = userInfo.getString(CONTENT_ID_TAG);
        }catch(Exception ex){
        	 Log.e("Profiling", "Profiling : onResponse()################### error");
        	ex.printStackTrace();
        }
    }
	return aisleContext;
}
public AisleImageDetails getImageDetails(String jsonArray) throws JSONException {
	AisleImageDetails imageItemDetails = new AisleImageDetails();
	JSONObject userInfo = new JSONObject(jsonArray);
	imageItemDetails.mDetalsUrl = userInfo
			.getString(USER_IMAGE_DETALS_TAG);
	imageItemDetails.mId = userInfo
			.getString(USER_IMAGE_ID_TAG);
	imageItemDetails.mStore = userInfo
			.getString(USER_IMAGE_STORE_TAG);
	imageItemDetails.mTitle = userInfo
			.getString(USER_IMAGE_TITLE_TAG);
	imageItemDetails.mImageUrl = userInfo
			.getString(USER_IMAGE_URL_TAG);
	Log.i("imageurl", "imageurl is: "+imageItemDetails.mImageUrl);
	imageItemDetails.mAvailableHeight = userInfo
			.getInt(IMAGE_HEIGHT_TAG);
	imageItemDetails.mAvailableWidth = userInfo
			.getInt(IMAGE_WIDTH_TAG);
	return imageItemDetails;
	 
}
}
 
