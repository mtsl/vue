package com.lateralthoughts.vue.parser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Environment;

import com.lateralthoughts.vue.AisleContext;
import com.lateralthoughts.vue.AisleImageDetails;
import com.lateralthoughts.vue.AisleWindowContent;
import com.lateralthoughts.vue.ImageRating;
import com.lateralthoughts.vue.VueApplication;
import com.lateralthoughts.vue.VueConstants;
import com.lateralthoughts.vue.VueTrendingAislesDataModel;
import com.lateralthoughts.vue.VueUser;
import com.lateralthoughts.vue.domain.AisleBookmark;
import com.lateralthoughts.vue.utils.UrlConstants;
import com.lateralthoughts.vue.utils.Utils;

public class Parser {
    // ========================= START OF PARSING TAGS
    // ========================================================
    // the following strings are pre-defined to help with JSON parsing
    // the tags defined here should be in sync the API documentation for the
    // backend
    
    // AisleImageObjects but re-uses the
    // imageItemsArray. Instead the called function clones and keeps a copy.
    // This is pretty inconsistent.
    // Let the allocation happen in one place for both items. Fix this!
    private boolean isEmptyAilseCached = false;
    
    public ArrayList<AisleWindowContent> parseTrendingAislesResultData(
            String resultString, boolean loadMore) {
        
        ArrayList<AisleWindowContent> aisleWindowContentList = new ArrayList<AisleWindowContent>();
        
        JSONArray contentArray = null;
        contentArray = handleResponse(resultString, loadMore);
        if (contentArray == null) {
            return aisleWindowContentList;
        }
        try {
            writeToSdcard("\n\n\nRESPONSE: "+contentArray.toString());
            isEmptyAilseCached = true;
            aisleWindowContentList = parseAisleInformation(contentArray,
                    isEmptyAilseCached);
            
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
    
    public AisleImageDetails parseAisleImageData(JSONObject jsonObject)
            throws JSONException {
        // get the image data from image object
        AisleImageDetails aisleImageDetails = new AisleImageDetails();
        aisleImageDetails.mId = jsonObject
                .getString(VueConstants.AISLE_IMAGE_ID);
        aisleImageDetails.mTitle = jsonObject
                .getString(VueConstants.AISLE_IMAGE_TITLE);
        aisleImageDetails.mAvailableHeight = jsonObject
                .getInt(VueConstants.AISLE_IMAGE_HEIGHT);
        aisleImageDetails.mStore = jsonObject
                .getString(VueConstants.AISLE_IMAGE_STORE);
        aisleImageDetails.mImageUrl = jsonObject
                .getString(VueConstants.AISLE_IMAGE_IMAGE_URL);
        aisleImageDetails.mAvailableWidth = jsonObject
                .getInt(VueConstants.AISLE_IMAGE_WIDTH);
        aisleImageDetails.mDetailsUrl = jsonObject
                .getString(VueConstants.AISLE_IMAGE_DETAILS_URL);
        aisleImageDetails.mOwnerUserId = jsonObject
                .getString(VueConstants.AISLE_IMAGE_OWNERUSER_ID);
        aisleImageDetails.mOwnerAisleId = jsonObject
                .getString(VueConstants.AISLE_IMAGE_OWNER_AISLE_ID);
        
        // get the image rating list
        JSONArray ratingJsonArray = jsonObject
                .getJSONArray(VueConstants.AISLE_IMAGE_RATINGS);
        ArrayList<ImageRating> ratingList = new ArrayList<ImageRating>();
        int ratingLikeCount = 0;
        if (ratingJsonArray != null) {
            ImageRating imgRatings;
            for (int i = 0; i < ratingJsonArray.length(); i++) {
                JSONObject ratingObj = ratingJsonArray.getJSONObject(i);
                imgRatings = new ImageRating();
                imgRatings.mId = ratingObj
                        .getLong(VueConstants.AISLE_IMAGE_RATING_ID);
                imgRatings.mLastModifiedTimestamp = ratingObj
                        .getLong(VueConstants.AISLE_IMAGE_RATING_LASTMODIFIED_TIME);
                imgRatings.mImageRatingOwnerFirstName = ratingObj
                        .getString(VueConstants.AISLE_IMAGE_RATING_OWNER_FIRST_NAME);
                imgRatings.mImageId = ratingObj
                        .getLong(VueConstants.AISLE_IMAGE_RATING_IMAGEID);
                imgRatings.mImageRatingOwnerLastName = ratingObj
                        .getString(VueConstants.AISLE_IMAGE_RATING_OWNER_LAST_NAME);
                imgRatings.mAisleId = ratingObj
                        .getLong(VueConstants.AISLE_IMAGE_RATING_AISLEID);
                imgRatings.mLiked = ratingObj
                        .getBoolean(VueConstants.AISLE_IMAGE_RATING_LIKED);
                if (imgRatings.mLiked) {
                    ratingLikeCount++;
                }
                ratingList.add(imgRatings);
            }
        }
        aisleImageDetails.mRatingsList = ratingList;
        aisleImageDetails.mLikesCount = ratingLikeCount;
        
        // get the image comment data.
        JSONArray jsonArray = jsonObject
                .getJSONArray(VueConstants.AISLE_IMAGE_COMMENTS);
        ArrayList<ImageComments> commentList = new ArrayList<ImageComments>();
        if (jsonArray != null) {
            ImageComments imgComments;
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject commnetObj = jsonArray.getJSONObject(i);
                imgComments = new ImageComments();
                imgComments.mId = commnetObj
                        .getLong(VueConstants.AISLE_IMAGE_COMMENTS_ID);
                if (commnetObj.getString(
                        VueConstants.AISLE_IMAGE_COMMENTS_LASTMODIFIED_TIME)
                        .equals("null")) {
                    imgComments.mLastModifiedTimestamp = commnetObj
                            .getLong(VueConstants.AISLE_IMAGE_COMMENTS_CREATED_TIME);
                } else {
                    imgComments.mLastModifiedTimestamp = commnetObj
                            .getLong(VueConstants.AISLE_IMAGE_COMMENTS_LASTMODIFIED_TIME);
                }
                imgComments.mCommenterLastName = commnetObj
                        .getString(VueConstants.AISLE_IMAGE_COMMENTER_LAST_NAME);
                imgComments.mCommenterUrl = commnetObj
                        .getString(VueConstants.IMAGE_COMMENT_OWNER_IMAGE_URL);
                imgComments.mImageId = commnetObj
                        .getLong(VueConstants.AISLE_IMAGE_COMMENTS_IMAGEID);
                
                imgComments.mCommenterFirstName = commnetObj
                        .getString(VueConstants.AISLE_IMAGE_COMMENTER_FIRST_NAME);
                imgComments.mOwnerUserId = commnetObj
                        .getString(VueConstants.AISLE_IMAGE_COMMENTS_USERID);
                
                imgComments.mComment = commnetObj
                        .getString(VueConstants.COMMENT);
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
                isEmptyAilseCached = true;
                aisleWindowContentList = parseAisleInformation(aisleArray,
                        isEmptyAilseCached);
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
                try {
                    JSONObject mainJsonObject = new JSONObject(responseMessage);
                    JSONArray jsonArray = mainJsonObject.getJSONArray("images");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        AisleImageDetails aisleImageDetails = parseAisleImageData(jsonObject);
                        if (aisleImageDetails.mImageUrl != null
                                && (!aisleImageDetails.mImageUrl
                                        .contains("randomurl.com"))
                                && aisleImageDetails.mImageUrl.trim().length() > 0
                                && aisleImageDetails.mAvailableHeight != 0
                                && aisleImageDetails.mAvailableWidth != 0) {
                            imageList.add(aisleImageDetails);
                        }
                    }
                } catch (Exception ex) {
                }
            }
        }
        return imageList;
    }
    
    public AisleWindowContent getAisleForAisleId(String aisleId) {
        try {
            URL url = new URL(UrlConstants.GET_AISLE_RESTURL + aisleId);
            HttpGet httpGet = new HttpGet(url.toString());
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpResponse response = httpClient.execute(httpGet);
            if (response.getEntity() != null
                    && response.getStatusLine().getStatusCode() == 200) {
                String responseMessage = EntityUtils.toString(response
                        .getEntity());
                if (responseMessage.length() > 0) {
                    AisleContext aisleContext = parseAisleData(new JSONObject(
                            responseMessage));
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
                        VueTrendingAislesDataModel.getInstance(
                                VueApplication.getInstance()).addItemToList(
                                aisleWindowContent.getAisleContext().mAisleId,
                                aisleWindowContent);
                        
                        return aisleWindowContent;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private ArrayList<AisleWindowContent> parseAisleInformation(
            JSONArray jsonArray, boolean isEmptyAilseCached)
            throws JSONException {
        ArrayList<AisleWindowContent> aisleWindowContentList = new ArrayList<AisleWindowContent>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject ailseItem = jsonArray.getJSONObject(i);
            AisleContext aisleContext = parseAisleData(ailseItem);
            ArrayList<AisleImageDetails> aisleImageDetailsList = new ArrayList<AisleImageDetails>();
            try {
                JSONObject imageObject = ailseItem
                        .getJSONObject(VueConstants.AISLE_IMAGE_OBJECT);
                JSONArray ImageListJson = imageObject
                        .getJSONArray(VueConstants.AISLE_IMAGE_LIST);
                for (int index = 0; index < ImageListJson.length(); index++) {
                    AisleImageDetails aisleImageDetails = parseAisleImageData(ImageListJson
                            .getJSONObject(index));
                    if (aisleImageDetails.mImageUrl != null
                            && (!aisleImageDetails.mImageUrl
                                    .contains("randomurl.com"))
                            && aisleImageDetails.mImageUrl.trim().length() > 0
                            && aisleImageDetails.mAvailableHeight != 0
                            && aisleImageDetails.mAvailableWidth != 0) {
                        aisleImageDetailsList.add(aisleImageDetails);
                    }
                }
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
                aisleWindowContentList.add(aisleWindowContent);
            } else if (isEmptyAilseCached) {
                // TODO: UNCOMMENT THIS CODE WHEN NO IMAGE AISLE FEATURE
                // ENABLED.
                /*
                 * AisleWindowContent aisleWindowContent = new
                 * AisleWindowContent( aisleContext.mAisleId);
                 * aisleWindowContent.addAisleContent(aisleContext, null);
                 * aisleWindowContentList.add(aisleWindowContent);
                 */
            }
        }
        return aisleWindowContentList;
    }
    
    public AisleContext parseAisleData(JSONObject josnObject) {
        try {
            writeToSdcard(josnObject + "??"
                    + josnObject.getString(VueConstants.AISLE_LOOKINGFOR));
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
        AisleContext aisleContext = new AisleContext();
        try {
            aisleContext.mAisleId = josnObject.getString(VueConstants.AISLE_ID);
            String aisleOwnerImageUrl = josnObject
                    .optString(VueConstants.AISLE_OWNER_IMAGE_URL);
            if (aisleOwnerImageUrl == null || aisleOwnerImageUrl.equals("null")) {
                aisleContext.mAisleOwnerImageURL = null;
            } else {
                aisleContext.mAisleOwnerImageURL = aisleOwnerImageUrl;
            }
            String lastName = josnObject
                    .getString(VueConstants.AISLE_OWNER_LASTNAME);
            aisleContext.mCategory = josnObject
                    .getString(VueConstants.AISLE_CATEGORY);
            aisleContext.mBookmarkCount = josnObject
                    .getInt(VueConstants.AISLE_BOOKMARK_COUNT);
            String description = josnObject
                    .getString(VueConstants.AISLE_DESCRIPTION);
            if (description == null || description.equals("null")) {
                aisleContext.mDescription = "";
            } else {
                aisleContext.mDescription = description;
            }
            String occasion = josnObject
                    .getString(VueConstants.AISLE_OCCASSION);
            if (occasion == null || occasion.equals("null")) {
                aisleContext.mOccasion = "";
            } else {
                aisleContext.mOccasion = occasion;
            }
            aisleContext.mName = josnObject.getString(VueConstants.AISLE_NAME);
            String firstName = josnObject
                    .getString(VueConstants.AISLE_OWNER_FIRSTNAME);
            aisleContext.mUserId = josnObject
                    .getString(VueConstants.AISLE_OWNER_USER_ID);
            aisleContext.mLookingForItem = josnObject
                    .getString(VueConstants.AISLE_LOOKINGFOR);
            
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
            
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return aisleContext;
    }
    
    private void writeToSdcard(String message) {
        
        String path = Environment.getExternalStorageDirectory().toString();
        File dir = new File(path + "/vueParseAisleData/");
        if (!dir.isDirectory()) {
            dir.mkdir();
        }
        File file = new File(dir, "/"
                + Calendar.getInstance().get(Calendar.DATE)
                + "-"
                + Utils.getWeekDay(Calendar.getInstance().get(
                        Calendar.DAY_OF_WEEK)) + ".txt");
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(
                    new FileWriter(file, true)));
            out.write("\n" + message + "\n");
            out.flush();
            out.close();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public ArrayList<AisleBookmark> parseBookmarkedAisles(String response) {
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
                    jsonObject.getString(VueConstants.USER_GOOGLEPLUS_ID),
                    jsonObject.getString(VueConstants.USER_PROFILE_IMAGE_URL));
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
                    imgRating.mId = jsonArray.getJSONObject(i).getLong(
                            VueConstants.AISLE_IMAGE_RATING_ID);
                    imgRating.mImageId = jsonArray.getJSONObject(i).getLong(
                            VueConstants.AISLE_IMAGE_RATING_IMAGEID);
                    imgRating.mAisleId = jsonArray.getJSONObject(i).getLong(
                            VueConstants.AISLE_IMAGE_RATING_AISLEID);
                    imgRating.mLastModifiedTimestamp = jsonArray.getJSONObject(
                            i).getLong(
                            VueConstants.AISLE_IMAGE_RATING_LASTMODIFIED_TIME);
                    imgRating.mImageRatingOwnerFirstName = jsonArray
                            .getJSONObject(i)
                            .getString(
                                    VueConstants.AISLE_IMAGE_RATING_OWNER_FIRST_NAME);
                    imgRating.mImageRatingOwnerLastName = jsonArray
                            .getJSONObject(i)
                            .getString(
                                    VueConstants.AISLE_IMAGE_RATING_OWNER_LAST_NAME);
                    imgRating.mLiked = jsonArray.getJSONObject(i).getBoolean(
                            VueConstants.AISLE_IMAGE_RATING_LIKED);
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
                    int isGrater = previous
                            .compareTime(current.mLastModifiedTimestamp);
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
            
            for (int j = 0; j < i; j++) {
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
                        
                    }
                }
            }
        }
        return bookmarkedAisles;
    }
    
    private void writeToSdcard(String message) {
        
        String path = Environment.getExternalStorageDirectory().toString();
        File dir = new File(path + "/vueAisleseResponse/");
        if (!dir.isDirectory()) {
            dir.mkdir();
        }
        File file = new File(dir, "/"
                + Calendar.getInstance().get(Calendar.DATE)
                + "-"
                + Utils.getWeekDay(Calendar.getInstance().get(
                        Calendar.DAY_OF_WEEK)) + ".txt");
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(
                    new FileWriter(file, true)));
            out.write("\n" + message + "\n");
            out.flush();
            out.close();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
