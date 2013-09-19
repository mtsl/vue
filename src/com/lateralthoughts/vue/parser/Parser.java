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
import com.lateralthoughts.vue.VueTrendingAislesDataModel;
import com.lateralthoughts.vue.connectivity.DataBaseManager;
import com.lateralthoughts.vue.domain.ReceivedAisle;

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
	private static final String USER_IMAGES_TAG = "aisleImage";
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
	public ArrayList<AisleWindowContent> parseTrendingAislesResultData(
			String resultString, boolean loadMore) {
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
			Log.i("useraisleparsing", "imageListresponse imageListResponse response: "+contentArray);
			ReceivedAisle aisle  = null;
			JSONObject ailseItem;
			final ArrayList<ReceivedAisle> listAisles = new ArrayList<ReceivedAisle>();
			
			for (int i = 0; i < contentArray.length(); i++) {
				aisle = new ReceivedAisle();
				//Log.i("useraisleparsing", "imageListresponse imageListResponse2: ");
				ailseItem = contentArray.getJSONObject(i);
				//Log.i("useraisleparsing", "imageListresponse imageListResponse3: ");
				aisle.setmAisleid(ailseItem.getString("id"));
				if(aisle.getmAisleid().equalsIgnoreCase("5607715460087808")){
					Log.i("useraisleparsing", "imageSuccesfullyresponse id : "+aisle.getmAisleid());	
				}
				//Log.i("useraisleparsing", "imageListresponse imageListResponse4 aisleId: "+aisle.getmAisleid());
				aisle.setCategory(ailseItem.getString("category"));
				aisle.setLookingFor(ailseItem.getString("lookingFor"));
				aisle.setName(ailseItem.getString("name"));
				aisle.setOccassion(ailseItem.getString("occassion"));
				//Log.i("useraisleparsing", "imageListresponse imageListResponse4: "+aisle.getOccassion());
				listAisles.add(aisle);
				
			}
			
			 
					new Thread(new Runnable() {
						
						@Override
						public void run() {
							for(int j = 0;j<listAisles.size();j++){
							try {
								testGetImageList(listAisles.get(j).getmAisleid());
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							}
							
						}
					}).start();
					
			 
		
/*			for (int i = 0; i < contentArray.length(); i++) {
				userInfo = new AisleContext();
				JSONObject contentItem = contentArray.getJSONObject(i);
				category = contentItem.getString(ITEM_CATEGORY_TAG);
				aisleId = contentItem.getString(CONTENT_ID_TAG);
				userInfo.mAisleId = contentItem.getString(CONTENT_ID_TAG);
				Log.i("aisleId", "aisleId: " + userInfo.mAisleId);
				if (userInfo.mAisleId.equalsIgnoreCase("5741031244955648")) {
					Log.i("aisleId",
							"aisleId: this ailse retrieved successfully*******************************"
									+ userInfo.mAisleId);
				}
				JSONArray imagesArray = null;
				try {
					imagesArray = contentItem.getJSONArray(USER_IMAGES_TAG);
				} catch (Exception e) {
					e.printStackTrace();
				}

				// within the content item we have a user object
				JSONObject user = contentItem.getJSONObject(USER_OBJECT_TAG);
				userInfo.mFirstName = user.getString(USER_FIRST_NAME_TAG);
				userInfo.mLastName = user.getString(USER_LAST_NAME_TAG);
				userInfo.mUserId = user.getString(CONTENT_ID_TAG);
				userInfo.mLookingForItem = contentItem
						.getString(LOOKING_FOR_TAG);
				userInfo.mOccasion = contentItem.getString(OCCASION_TAG);
				userInfo.mCategory = category;
				userInfo.mJoinTime = Long.parseLong(user
						.getString(USER_JOIN_TIME_TAG));
				if (imagesArray != null) {
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
				}
				aisleItem = VueTrendingAislesDataModel.getInstance(
						VueApplication.getInstance()).getAisleItem(aisleId);
				aisleItem.addAisleContent(userInfo, imageItemsArray);
				aisleList.add(aisleItem);
				imageItemsArray.clear();
			}*/
			VueTrendingAislesDataModel
					.getInstance(VueApplication.getInstance())
					.dismissProgress();
		} catch (JSONException ex1) {
			ex1.printStackTrace();
			VueTrendingAislesDataModel
					.getInstance(VueApplication.getInstance())
					.dismissProgress();
			Log.e("Profiling",
					"Profiling : response json array444444444444: exception "
							+ aisleList.size());
			return aisleList;
		}
		Log.e("Profiling", "Profiling : response json array444444444444444: "
				+ aisleList.size());
		return aisleList;
	}

	private JSONArray handleResponce(String resultString, boolean loadMore) {
		JSONArray contentArray = null;
		try {
			contentArray = new JSONArray(resultString);
			if (!loadMore) {
				Log.i("datarequest",
						"datarequest new request clear data in list");
				VueTrendingAislesDataModel.getInstance(
						VueApplication.getInstance()).clearContent();
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return contentArray;
	}

	public AisleContext getAisleCotent(String jsonArray) {
		AisleContext aisleContext = new AisleContext();

		if (null != jsonArray) {

			try {
				JSONObject userInfo = new JSONObject(jsonArray);
				aisleContext.mAisleId = userInfo.getString(CONTENT_ID_TAG);
				aisleContext.mCategory = userInfo.getString(ITEM_CATEGORY_TAG);

				aisleContext.mLookingForItem = userInfo
						.getString(LOOKING_FOR_TAG);
				aisleContext.mUserId = userInfo.getString(CONTENT_ID_TAG);
				Log.e("Profiling", "Profiling : onResponse() mUserId: "
						+ aisleContext.mUserId);
				Log.e("Profiling", "Profiling : onResponse() mAisleId: "
						+ aisleContext.mAisleId);
				aisleContext.mOccasion = userInfo.getString(OCCASION_TAG);

			} catch (Exception ex) {
				Log.e("Profiling",
						"Profiling : onResponse()################### error");
				ex.printStackTrace();
			}
		}
		return aisleContext;
	}

	public AisleImageDetails getImageDetails(String jsonArray)
			throws JSONException {

		AisleImageDetails imageItemDetails = new AisleImageDetails();
		JSONObject userInfo = new JSONObject(jsonArray);
		imageItemDetails.mDetalsUrl = userInfo.getString(USER_IMAGE_DETALS_TAG);
		imageItemDetails.mId = userInfo.getString(USER_IMAGE_ID_TAG);
		imageItemDetails.mStore = userInfo.getString(USER_IMAGE_STORE_TAG);
		imageItemDetails.mTitle = userInfo.getString(USER_IMAGE_TITLE_TAG);
		imageItemDetails.mImageUrl = userInfo.getString(USER_IMAGE_URL_TAG);
		Log.i("imageurl", "imageurl is: " + imageItemDetails.mImageUrl);
		imageItemDetails.mAvailableHeight = userInfo.getInt(IMAGE_HEIGHT_TAG);
		imageItemDetails.mAvailableWidth = userInfo.getInt(IMAGE_WIDTH_TAG);
		return imageItemDetails;

	}
	public List getUserAilseLIst(String jsonArray){
		String userId;
		String aisleId,ownerUserId,name,category,lookingFor,occassion,aisleOwnerFirstName,aisleOwnerLastName;
		int bookmarkCount;
		try{
			Log.i("useraisleparsing", "useraisleparsing started");
			
			JSONObject jsonResponse = new JSONObject(new String(jsonArray));
			JSONArray aisleArray = jsonResponse.getJSONArray("aisles");
			
/*			
		    JSONArray contentArray = new JSONArray(jsonArray);
       		JSONObject contentItem = contentArray.getJSONObject(0);
       		userId = contentItem.getString("id");
       		Log.i("useraisleparsing", "useraisleparsing userId: "+userId);*/
       		 
       		
       	/*	JSONArray imagesArray = null;
				imagesArray = contentItem.getJSONArray("aisles");*/
				JSONObject ailseItem;
				for(int i = 0;i<aisleArray.length();i++){
					AisleContext aisleContext = new AisleContext();
					ailseItem = aisleArray.getJSONObject(i);
					aisleId = ailseItem.getString("id");
					aisleContext.mAisleId = aisleId;
					Log.i("useraisleparsing", "useraisleparsing aisleId: "+aisleId);
					ownerUserId = ailseItem.getString("ownerUserId");
					aisleContext.mUserId = ownerUserId;
					Log.i("useraisleparsing", "useraisleparsing ownerUserId: "+ownerUserId);
					name = ailseItem.getString("name");
					Log.i("useraisleparsing", "useraisleparsing name: "+name);
					category = ailseItem.getString("category");
					aisleContext.mCategory = category;
					Log.i("useraisleparsing", "useraisleparsing category: "+category);
					lookingFor = ailseItem.getString("lookingFor");
					aisleContext.mLookingForItem = lookingFor;
					Log.i("useraisleparsing", "useraisleparsing lookingFor: "+lookingFor);
					occassion = ailseItem.getString("occassion");
					aisleContext.mOccasion = occassion;
					Log.i("useraisleparsing", "useraisleparsing occassion: "+occassion);
					aisleOwnerFirstName = ailseItem.getString("aisleOwnerFirstName");
					if(aisleOwnerFirstName == null){
					aisleContext.mFirstName = "";
					} else {
						aisleContext.mFirstName = aisleOwnerFirstName;
					}
					Log.i("useraisleparsing", "useraisleparsing aisleOwnerFirstName: "+aisleOwnerFirstName);
					aisleOwnerLastName = ailseItem.getString("aisleOwnerLastName");
					if(aisleOwnerLastName == null){
						aisleContext.mLastName = "";
						} else {
							aisleContext.mLastName = aisleOwnerLastName;
						}
					Log.i("useraisleparsing", "useraisleparsing aisleOwnerLastName: "+aisleOwnerLastName);
					
				}

		
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
		
	}
	public static ArrayList<AisleImageDetails> testGetImageList(String aisleId) throws Exception {
		ArrayList<AisleImageDetails> imageList = new ArrayList<AisleImageDetails>();
 
         String imageRequestUrl = "http://2-java.vueapi-canary-development1.appspot.com/api/imagesget/aisle/" +aisleId;
		URL url = new URL(imageRequestUrl);
		HttpGet httpGet = new HttpGet(url.toString());
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpResponse response = httpClient.execute(httpGet);
		if(response.getEntity()!=null &&
		response.getStatusLine().getStatusCode() == 200) {
		String responseMessage = EntityUtils.toString(response.getEntity());
		//Log.i("useraisleparsing", "imageListresponse imageListResponse: "+responseMessage);
		
		if(aisleId.equalsIgnoreCase("5607715460087808")){
			Log.i("useraisleparsing", "imageSuccesfullyresponse id response : "+responseMessage);	
		}
		
		 
		
		}

		return imageList;
		}
}
