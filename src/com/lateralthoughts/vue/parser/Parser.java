package com.lateralthoughts.vue.parser;

import android.util.Log;
import com.lateralthoughts.vue.*;
import com.lateralthoughts.vue.domain.AisleBookmark;
import com.lateralthoughts.vue.domain.ImageComment;
import com.lateralthoughts.vue.utils.UrlConstants;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;

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

		contentArray = handleResponse(resultString, loadMore);
		if (contentArray == null) {
			return aisleWindowContentList;
		}
		try {
			aisleWindowContentList = parseAisleInformation(contentArray);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return aisleWindowContentList;
	}

	private JSONArray handleResponse(String resultString, boolean loadMore) {
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
				AisleImageDetails aisleImageDetails = null;
				try {
					aisleImageDetails = parseAisleImageData(jsonObject
							.getJSONObject("aisleImage"));
				} catch (Exception e) {
					e.printStackTrace();
				}

				if (aisleImageDetails != null
						&& aisleImageDetails.mImageUrl != null
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
		Log.e("Parser", "Image Response : " + jsonObject);
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

		if (jsonObject.getString(VueConstants.AISLE_IMAGE_RATING) == null
				|| jsonObject.getString(VueConstants.AISLE_IMAGE_RATING)
						.equalsIgnoreCase("null")) {
			aisleImageDetails.mLikesCount = 0;
		} else {
			aisleImageDetails.mLikesCount = Integer.parseInt(jsonObject
					.getString(VueConstants.AISLE_IMAGE_RATING));
		}
		aisleImageDetails.mStore = jsonObject
				.getString(VueConstants.AISLE_IMAGE_STORE);
		aisleImageDetails.mTitle = jsonObject
				.getString(VueConstants.AISLE_IMAGE_TITLE);
		JSONArray jsonArray = jsonObject
				.getJSONArray(VueConstants.AISLE_IMAGE_COMMENTS);

		ArrayList<ImageComments> commentList = new ArrayList<ImageComments>();
		if (jsonArray != null) {
			ImageComments imgComments;
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject commnetObj = jsonArray.getJSONObject(i);
				imgComments = new ImageComments();
				imgComments.Id = commnetObj
						.getLong(VueConstants.AISLE_IMAGE_COMMENTS_ID);
				imgComments.imageId = commnetObj
						.getLong(VueConstants.AISLE_IMAGE_COMMENTS_IMAGEID);
				imgComments.comment = commnetObj
						.getString(VueConstants.COMMENT);
				if (commnetObj.getString(
						VueConstants.AISLE_IMAGE_COMMENTS_LASTMODIFIED_TIME)
						.equals("null")) {
					imgComments.lastModifiedTimestamp = commnetObj
							.getLong(VueConstants.AISLE_IMAGE_COMMENTS_CREATED_TIME);
				} else {
					imgComments.lastModifiedTimestamp = commnetObj
							.getLong(VueConstants.AISLE_IMAGE_COMMENTS_LASTMODIFIED_TIME);
				}
				commentList.add(imgComments);
			}
		}
		aisleImageDetails.mCommentsList = commentList;
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
		String imageRequestUrl = UrlConstants.GET_IMAGES_FOR_AISLE + aisleId;
		URL url = new URL(imageRequestUrl);
		HttpGet httpGet = new HttpGet(url.toString());
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpResponse response = httpClient.execute(httpGet);
		if (response.getEntity() != null
				&& response.getStatusLine().getStatusCode() == 200) {
			String responseMessage = EntityUtils.toString(response.getEntity());
			if (responseMessage != null) {
				JSONObject mainJsonObject = new JSONObject(responseMessage);
				JSONArray jsonArray = mainJsonObject.getJSONArray("images");
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject jsonObject = jsonArray.getJSONObject(i);
					AisleImageDetails aisleImageDetails = parseAisleImageData(jsonObject); // /randomimage.jpg

					if (aisleImageDetails.mImageUrl != null
							&& (!aisleImageDetails.mImageUrl
									.contains("randomurl.com"))
							&& aisleImageDetails.mImageUrl.trim().length() > 0
							&& aisleImageDetails.mAvailableHeight != 0
							&& aisleImageDetails.mAvailableWidth != 0) {
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
						.getInstance(VueApplication.getInstance())
						.getAisleItem(aisleContext.mAisleId);
				aisleWindowContent.addAisleContent(aisleContext,
						aisleImageDetailsList);
				aisleWindowContent
						.setmAisleBookmarksCount(aisleContext.mBookmarkCount);
				// Log.i("aisleItemId", "aisleItemId: "+aisleContext.mAisleId);
				aisleWindowContentList.add(aisleWindowContent);
			}
		}
		return aisleWindowContentList;
	}

	public AisleContext parseAisleData(JSONObject josnObject) {
		// TODO:

		AisleContext aisleContext = new AisleContext();
		try {
			aisleContext.mAisleId = josnObject.getString(VueConstants.AISLE_ID);
			aisleContext.mCategory = josnObject
					.getString(VueConstants.AISLE_CATEGORY);
			aisleContext.mLookingForItem = josnObject
					.getString(VueConstants.AISLE_LOOKINGFOR);
			aisleContext.mName = josnObject.getString(VueConstants.AISLE_NAME);
			String occasion = josnObject
					.getString(VueConstants.AISLE_OCCASSION);
			if (occasion == null || occasion.equals("null")) {
				aisleContext.mOccasion = "";
			} else {
				aisleContext.mOccasion = occasion;
			}
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
			String description = josnObject
					.getString(VueConstants.AISLE_DESCRIPTION);
			if (description == null || description.equals("null")) {
				aisleContext.mDescription = "";
			} else {
				aisleContext.mDescription = description;
			}
			aisleContext.mBookmarkCount = josnObject
					.getInt(VueConstants.AISLE_BOOKMARK_COUNT);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return aisleContext;
	}

	public ArrayList<AisleBookmark> parseBookmarkedAisles(String response) {
		Log.i("bookmarked aisle", "bookmarked aisle response SURU: " + response);
		ArrayList<AisleBookmark> aisleIdList = new ArrayList<AisleBookmark>();
		AisleBookmark bookmarkAisle = null;
		try {
			JSONArray jsonArray = new JSONArray(response);
			if (jsonArray != null && jsonArray.length() > 0) {
				for (int i = 0; i < jsonArray.length(); i++) {
					bookmarkAisle = new AisleBookmark();
					bookmarkAisle
							.setAisleId(Long.parseLong(jsonArray.getJSONObject(
									i).getString(VueConstants.AISLE_Id)));
					bookmarkAisle.setId(jsonArray.getJSONObject(i).getLong(
							VueConstants.JSON_OBJ_ID));
					bookmarkAisle.setId(jsonArray.getJSONObject(i).getLong(
							VueConstants.JSON_OBJ_ID));
					bookmarkAisle.setLastModifiedTimestamp(jsonArray
							.getJSONObject(i).getLong(
									VueConstants.LAST_MODIFIED_TIME));
					bookmarkAisle
							.setBookmarked((jsonArray.getJSONObject(i)
									.getString(VueConstants.BOOKMARKED)
									.equals("true")) ? true : false);
					aisleIdList.add(bookmarkAisle);
				}
				aisleIdList = removeDuplicateBookmarkedAisles(aisleIdList);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (aisleIdList != null && aisleIdList.size() > 0) {
			Log.i("bookmarked aisle", "bookmarked aisle aisleIdList.size(): "
					+ aisleIdList.size());
		} else {
			Log.i("bookmarked aisle", "bookmarked aisle not found: ");

		}
		return aisleIdList;
	}

	public VueUser parseUserData(String response) {
		try {
			JSONObject jsonObject = new JSONObject(response);
			String firstName = null, lastName = null;
			if (jsonObject.getString(VueConstants.USER_FIRST_NAME) == null
					|| jsonObject.getString(VueConstants.USER_FIRST_NAME)
							.equals("null")) {
				firstName = "";
			} else {
				firstName = jsonObject.getString(VueConstants.USER_FIRST_NAME);
			}
			if (jsonObject.getString(VueConstants.USER_LAST_NAME) == null
					|| jsonObject.getString(VueConstants.USER_LAST_NAME)
							.equals("null")) {
				lastName = "";
			} else {
				lastName = jsonObject.getString(VueConstants.USER_LAST_NAME);
			}
			VueUser vueUser = new VueUser(
					jsonObject.getLong(VueConstants.USER_RESPONSE_ID),
					jsonObject.getString(VueConstants.USER_EMAIL), firstName,
					lastName, jsonObject.getLong(VueConstants.USER_JOINTIME),
					jsonObject.getString(VueConstants.USER_DEVICE_ID),
					jsonObject.getString(VueConstants.USER_FACEBOOK_ID),
					jsonObject.getString(VueConstants.USER_GOOGLEPLUS_ID));
			return vueUser;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static ArrayList<ImageRating> parseRatedImages(String response) {
		ArrayList<ImageRating> imgRatingList = new ArrayList<ImageRating>();
		ImageRating imgRating = null;
		try {
			JSONArray jsonArray = new JSONArray(response);
			if (jsonArray != null && jsonArray.length() > 0) {
				for (int i = 0; i < jsonArray.length(); i++) {
					imgRating = new ImageRating();
					imgRating.setId(Long.parseLong(jsonArray.getJSONObject(i)
							.getString(VueConstants.JSON_OBJ_ID)));
					imgRating
							.setImageId(Long.parseLong(jsonArray.getJSONObject(
									i).getString(VueConstants.IMAGE_ID)));
					imgRating
							.setAisleId(Long.parseLong(jsonArray.getJSONObject(
									i).getString(VueConstants.AISLE_Id)));
					imgRating.setLastModifiedTimestamp(jsonArray.getJSONObject(
							i).getLong(VueConstants.LAST_MODIFIED_TIME));
					imgRating.setLiked((jsonArray.getJSONObject(i).getString(
							VueConstants.LIKED).equals("true")) ? true : false);
					imgRatingList.add(imgRating);
				}
				imgRatingList = removeDuplicateImageRating(imgRatingList);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return imgRatingList;
	}

	private static ArrayList<ImageRating> removeDuplicateImageRating(
			ArrayList<ImageRating> imgRatingList) {
		int size = imgRatingList.size();
		for (int i = 0; i < size; i++) {
			ImageRating current;
			if (imgRatingList.size() > i) {
				current = imgRatingList.get(i);
			} else {
				break;
			}
			for (int j = 0; j < i; ++j) {
				ImageRating previous;
				if (imgRatingList.size() > j) {
					previous = imgRatingList.get(j);
				} else {
					break;
				}
				final boolean relation = previous.compareTo(current);
				if (relation) {
					int isGrater = previous.compareTime(current
							.getLastModifiedTimestamp().longValue());
					if (isGrater == ImageRating.NEW_TIME_STAMP) {
						imgRatingList.remove(i);
					} else {
						imgRatingList.remove(j);
					}
				}
			}
		}
		return imgRatingList;
	}

	private static ArrayList<AisleBookmark> removeDuplicateBookmarkedAisles(
			ArrayList<AisleBookmark> bookmarkedAisles) {
		int size = bookmarkedAisles.size();
		for (int i = 0; i < size; i++) {
			AisleBookmark current;
			if (bookmarkedAisles.size() > i) {
				current = bookmarkedAisles.get(i);
			} else {
				break;
			}

			for (int j = 0; j < i; ++j) {
				AisleBookmark previous;
				if (bookmarkedAisles.size() > j) {
					previous = bookmarkedAisles.get(j);
				} else {
					break;
				}

				final boolean relation = previous.compareTo(current);
				if (relation) {
					if (current.getLastModifiedTimestamp() != null) {
						long currentTime = current.getLastModifiedTimestamp()
								.longValue();
						int isGrater = previous.compareTime(currentTime);
						if (isGrater == AisleBookmark.NEW_TIME_STAMP) {
							bookmarkedAisles.remove(i);
						} else {
							bookmarkedAisles.remove(j);
						}
					} else {
						Log.i("bookmarked aisle",
								"bookmarked aisle current time is null: ");
					}
				}
			}
		}
		return bookmarkedAisles;
	}
}
