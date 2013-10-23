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
	public boolean logStatus = false;

	public ArrayList<AisleWindowContent> parseTrendingAislesResultData(
			String resultString, boolean loadMore) {

		ArrayList<AisleWindowContent> aisleWindowContentList = new ArrayList<AisleWindowContent>();

		JSONArray contentArray = null;

		contentArray = handleResponse(resultString, loadMore);
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
		if (logStatus) {
			Log.e("Parser",
					"abcparserAisleImageData: Response " + jsonObject.toString());
			logStatus = false;
		}
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
		Log.i("bookmarkfeaturetest", "bookmarkfeaturetest:url "
				+ aisleImageDetails.mImageUrl);

		Log.i("urlserver", "urlserver: " + aisleImageDetails.mImageUrl);
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
		JSONArray jsonArray = jsonObject.getJSONArray(VueConstants.AISLE_IMAGE_COMMENTS);
		Log.e("DataBaseManager", "Suru comment show: PARSER COMMENT: jsonArray.length() " + jsonArray.toString());
		ArrayList<ImageComments> commentList = new ArrayList<ImageComments>();
		 if(jsonArray != null){
		   Log.e("DataBaseManager", "Suru comment show: PARSER COMMENT: jsonArray not null ");
			 ImageComments imgComments;
			 for(int i = 0;i < jsonArray.length();i++){
			   Log.e("DataBaseManager", "Suru comment show: PARSER COMMENT: jsonArray 1 ");
				 JSONObject commnetObj = jsonArray.getJSONObject(i);
				 Log.e("DataBaseManager", "Suru comment show: PARSER COMMENT: jsonArray 2 ");
				 imgComments = new ImageComments();
				 Log.e("DataBaseManager", "Suru comment show: PARSER COMMENT: jsonArray 3 ");
				 imgComments.Id = commnetObj.getLong(VueConstants.AISLE_IMAGE_COMMENTS_ID);
				 Log.e("DataBaseManager", "Suru comment show: PARSER COMMENT: jsonArray 4 ");
				 imgComments.imageId = commnetObj.getLong(VueConstants.AISLE_IMAGE_COMMENTS_IMAGEID);
				 Log.e("DataBaseManager", "Suru comment show: PARSER COMMENT: jsonArray 5 ");
				 imgComments.comment = commnetObj.getString(VueConstants.COMMENT);
				 Log.e("DataBaseManager", "Suru comment show: PARSER COMMENT: jsonArray 6 ");
				 if(commnetObj.getString(VueConstants.AISLE_IMAGE_COMMENTS_LASTMODIFIED_TIME).equals("null")) {
				   Log.e("DataBaseManager", "Suru comment show: PARSER COMMENT: jsonArray 7 ");
				   imgComments.lastModifiedTimestamp = commnetObj.getLong(VueConstants.AISLE_IMAGE_COMMENTS_CREATED_TIME);
				   Log.e("DataBaseManager", "Suru comment show: PARSER COMMENT: jsonArray 8 ");
				 } else {
				   Log.e("DataBaseManager", "Suru comment show: PARSER COMMENT: jsonArray 9 ");
				 imgComments.lastModifiedTimestamp = commnetObj.getLong(VueConstants.AISLE_IMAGE_COMMENTS_LASTMODIFIED_TIME);
				 Log.e("DataBaseManager", "Suru comment show: PARSER COMMENT: jsonArray 10 ");
				 }
				 Log.e("DataBaseManager", "Suru comment show: PARSER COMMENT: jsonArray 11 ");
				 commentList.add(imgComments);
				 Log.e("DataBaseManager", "Suru comment show: PARSER COMMENT: " + imgComments.comment);
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
				Log.i("Parser", responseMessage);
				JSONObject mainJsonObject = new JSONObject(responseMessage);
				if(aisleId.equals("5567688512372736"))
				Log.i ("Comment Response: ","Comment Response: " + responseMessage);
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
			Log.i("aisleItemId", "aisleItemId: " + ailseItem);
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

	private AisleContext parseAisleData(JSONObject josnObject) {
		// TODO:

		AisleContext aisleContext = new AisleContext();
		try {
			aisleContext.mAisleId = josnObject.getString(VueConstants.AISLE_ID);
			if ("4823662737752064".equalsIgnoreCase(aisleContext.mAisleId)) {
				logStatus = true;
				Log.e("Parser",
						"abcparserAisleAisleData: Response  "+josnObject.toString());
			}
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
			String description = josnObject
					.getString(VueConstants.AISLE_DESCRIPTION);
			if (description == null || description.equals("null")) {
				aisleContext.mDescription = "";
			} else {
				aisleContext.mDescription = description;
			}
			aisleContext.mBookmarkCount = josnObject
					.getInt(VueConstants.AISLE_BOOKMARK_COUNT);
			Log.i("bookmarkfeaturetest", "bookmarkfeaturetest: count parser"
					+ aisleContext.mBookmarkCount);
			Log.i("bookmarkfeaturetest", "bookmarkfeaturetest parser id: "
					+ aisleContext.mAisleId);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return aisleContext;
	}

  public ArrayList<AisleBookmark> parseBookmarkedAisles(String response) {
    Log.i("bookmarked aisle", "bookmarked aisle: " + response);
    ArrayList<AisleBookmark> aisleIdList = new ArrayList<AisleBookmark>();
    AisleBookmark bookmarkAisle = null;
    try {
      JSONArray jsonArray = new JSONArray(response);
      if (jsonArray != null && jsonArray.length() > 0) {
        for (int i = 0; i < jsonArray.length(); i++) {
          bookmarkAisle = new AisleBookmark();
          bookmarkAisle.setAisleId(Long.parseLong(jsonArray.getJSONObject(i)
              .getString(VueConstants.AISLE_Id)));
          bookmarkAisle.setId(jsonArray.getJSONObject(i).getLong(
              VueConstants.JSON_OBJ_ID));
          bookmarkAisle.setId(jsonArray.getJSONObject(i).getLong(
              VueConstants.JSON_OBJ_ID));
              bookmarkAisle.setBookmarked((jsonArray.getJSONObject(i).getString(
                  VueConstants.BOOKMARKED).equals("true")) ? true : false);
          aisleIdList.add(bookmarkAisle);
        }
        for(AisleBookmark m : aisleIdList) {
          Log.i("bookmarked aisle", "bookmarked aisle: with duplicates: ID; " + m.getId());
          Log.i("bookmarked aisle", "bookmarked aisle: with duplicates: AisleID; " + m.getAisleId());
          Log.i("bookmarked aisle", "bookmarked aisle: with duplicates: Bookmarked; " + m.getBookmarked());
        }
        aisleIdList = removeDuplicateBookmarkedAisles(aisleIdList);
        for(AisleBookmark m : aisleIdList) {
          Log.i("bookmarked aisle", "bookmarked aisle: after duplicates: ID; " + m.getId());
          Log.i("bookmarked aisle", "bookmarked aisle: after duplicates: AisleID; " + m.getAisleId());
          Log.i("bookmarked aisle", "bookmarked aisle: after duplicates: Bookmarked; " + m.getBookmarked());
        }
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
/*   public ArrayList<String> parseComments(JSONArray jsonArray) throws JSONException{
	   ArrayList<String> commentList = new ArrayList<String>();
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject jsonObject = jsonArray.getJSONObject(i);
		     String userComment = jsonObject.getString("comment"); 
				commentList.add(userComment);
		}
		return commentList;
   }*/
   
	public VueUser parseUserData(String response) {
		try {
			JSONObject jsonObject = new JSONObject(response);
			VueUser vueUser = new VueUser(
					jsonObject.getLong(VueConstants.USER_RESPONSE_ID),
					jsonObject.getString(VueConstants.USER_EMAIL),
					jsonObject.getString(VueConstants.USER_FIRST_NAME),
					jsonObject.getString(VueConstants.USER_LAST_NAME),
					jsonObject.getLong(VueConstants.USER_JOINTIME),
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
	  Log.e("Parser", "parserRatedImages response " + response);
	  ArrayList<ImageRating> imgRatingList = new ArrayList<ImageRating>();
	  ImageRating imgRating = null;
	  try {
	    JSONArray jsonArray = new JSONArray(response);
	    if (jsonArray != null && jsonArray.length() > 0) {
	      for (int i = 0; i < jsonArray.length(); i++) {
	        imgRating = new ImageRating();
	        imgRating.setId(Long.parseLong(jsonArray.getJSONObject(i)
	              .getString(VueConstants.JSON_OBJ_ID)));
	        imgRating.setImageId(Long.parseLong(jsonArray.getJSONObject(i)
                .getString(VueConstants.IMAGE_ID)));
	        imgRating.setAisleId(Long.parseLong(jsonArray.getJSONObject(i)
                .getString(VueConstants.AISLE_Id)));
            imgRating.setLastModifiedTimestamp(Long.parseLong(jsonArray.getJSONObject(i)
                .getString(VueConstants.LAST_MODIFIED_TIME)));
            imgRating.setLiked((jsonArray.getJSONObject(i)
                .getString(VueConstants.LIKED).equals("true")) ? true : false);
            imgRatingList.add(imgRating);
	      }
	      for(ImageRating r : imgRatingList) {
	        Log.e("Parser", "parserRatedImages imgRatingList with duplicate: Id: " + r.getId());
	        Log.e("Parser", "parserRatedImages imgRatingList with duplicate: ImageId: " + r.getImageId());
	        Log.e("Parser", "parserRatedImages imgRatingList with duplicate: trueOrfalse: " + r.getLiked());
	      }
	      
	      imgRatingList = removeDuplicateImageRating(imgRatingList);
	      for(ImageRating r : imgRatingList) {
            Log.e("Parser", "parserRatedImages imgRatingList after duplicate: Id: " + r.getId());
            Log.e("Parser", "parserRatedImages imgRatingList after duplicate: ImageId: " + r.getImageId());
            Log.e("Parser", "parserRatedImages imgRatingList after duplicate: trueOrfalse: " + r.getLiked());
          }
	    }
      } catch (Exception e) {
         e.printStackTrace();
      }
	  
	  return imgRatingList;
	}
	
	
	private static ArrayList<ImageRating> removeDuplicateImageRating(ArrayList<ImageRating> imgRatingList) {
	  int size = imgRatingList.size();
	  for (int i = 0; i < size; ++i) {
	    final ImageRating current = imgRatingList.get(i);
	    for (int j = 0; j < i; ++j) {
	      final ImageRating previous = imgRatingList.get(j);
	      final boolean relation = previous.compareTo(current);
	      if (relation) {
	        int isGrater = previous.compareTime(current.getLastModifiedTimestamp().longValue());
	        if(isGrater == ImageRating.NEW_TIME_STAMP) {
	          imgRatingList.remove(i);
	        } else {
	          imgRatingList.remove(j);
	        }
	      }
	    }
	  }
	  return imgRatingList;
	}
	
	private static ArrayList<AisleBookmark> removeDuplicateBookmarkedAisles(ArrayList<AisleBookmark> bookmarkedAisles) {
      int size = bookmarkedAisles.size();
      for (int i = 0; i < size; ++i) {
        final AisleBookmark current = bookmarkedAisles.get(i);
        for (int j = 0; j < i; ++j) {
          final AisleBookmark previous = bookmarkedAisles.get(j);
          final boolean relation = previous.compareTo(current);
          if (relation) {
            int isGrater = previous.compareTime(current.getLastModifiedTimestamp().longValue());
            if(isGrater == AisleBookmark.NEW_TIME_STAMP) {
              bookmarkedAisles.remove(i);
            } else {
              bookmarkedAisles.remove(j);
            }
          }
        }
      }
      return bookmarkedAisles;
    }
}
