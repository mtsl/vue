package com.lateralthoughts.vue;

import android.net.Uri;

/**
 * Here we will declare all constants related to Vue application.
 * 
 */
public class VueConstants {

	public static final String SHAREDPREFERENCE_NAME = "VuePreferences";
	public static final String FACEBOOK_ACCESSTOKEN = "FacebookAccessToken";
	public static final String VUE_LOGIN = "VueLoginFlag";
	public static final String FACEBOOK = "Facebook";
	public static final String GOOGLEPLUS = "Googleplus";
	public static final String FACEBOOK_LOGIN = "FacebookLoginFlag";
	public static final String GOOGLEPLUS_LOGIN = "GoogleplusLoginFlag";
	public static final String FIRSTTIME_LOGIN_PREFRENCE_FLAG = "firstTimeLoginPrefrenceFlag";
	public static final String CREATED_AISLE_COUNT_IN_PREFERENCE = "createdAisleCountInPreference";
	public static final String COMMENTS_COUNT_IN_PREFERENCES = "commentsCountInPreferences";
	public static final int CREATE_AISLE_LIMIT_FOR_LOGIN = 5;
	public static final int COMMENTS_LIMIT_FOR_LOGIN = 10;
	public static final int SELECT_PICTURE = 1;
	public static final int CAMERA_REQUEST = 2;
	public static final String FACEBOOK_GETFRIENDS_URL = "https://graph.facebook.com/me/friends?access_token=";
	public static final String FACEBOOK_FRIENDS_DETAILS = "&fields=id,name,picture";
	public static final int SHARE_INTENT_REQUEST_CODE = 1;
	public static final int AMAZON_APP_REQUEST_CODE = 39;
	public static final String GOOGLEPLUS_AUTOMATIC_LOGIN = "googleplusautomaticlogin";
	public static final String FACEBOOK_APP_NAME = "Facebook";
	public static final String TWITTER_APP_NAME = "Twitter";
	public static final String GMAIL_APP_NAME = "Gmail";
	public static final String GOOGLEPLUS_APP_NAME = "Google+";
	public static final String FACEBOOK_PACKAGE_NAME = "com.facebook.katana";
	public static final String TWITTER_PACKAGE_NAME = "com.twitter.android";
	public static final String GMAIL_PACKAGE_NAME = "com.google.android.gm";
	public static final String GOOGLEPLUS_PACKAGE_NAME = "com.google.android.apps.plus";
	public static final String TWITTER_ACTIVITY_NAME = "com.twitter.android.PostActivity";
	public static final String GMAIL_ACTIVITY_NAME = "com.google.android.gm.ComposeActivityGmail";
	public static final String GOOGLEPLUS_ACTIVITY_NAME = "com.google.android.apps.plus.phone.SignOnActivity";
	public static final String CANCEL_BTN_DISABLE_FLAG = "cancelbuttondisableflag";
	public static final String FROM_INVITEFRIENDS = "frominvitefriends";
	public static final String FROM_BEZELMENU_LOGIN = "frombezelmenulogin";
	public static final String FBLOGIN_FROM_DETAILS_SHARE = "FBLOGIN_FROM_DETAILS_SHARE";
	public static final String FBPOST_TEXT = "FBPOSTTEXT";
	public static final String FBPOST_IMAGEURLS = "FBPOSTIMAGEURLS";
	public static final String FB_FRIEND_ID = "FBFRIENDID";
	public static final String FB_FRIEND_NAME = "FBFRIENDNAME";
	public static final String GOOGLEPLUS_FRIEND_INVITE = "GOOGLEPLUS_FRIEND_INVITE";
	public static final String GOOGLEPLUS_FRIEND_INDEX = "GOOGLEPLUS_FRIEND_INDEX";
	// Column names for aisles table.
	public static final String ID = "_id";
	public static final String CATEGORY = "category";
	public static final String FIRST_NAME = "firstName";
	public static final String LAST_NAME = "lastName";
	public static final String JOIN_TIME = "joinTime";
	public static final String USER_ID = "userId";
	public static final String LOOKING_FOR = "lookingFor";
	public static final String OCCASION = "occasion";
	public static final String BOOKMARK_COUNT = "bookMarkCount";
	public static final String IS_BOOKMARKED = "isBookmarked";
    public static final String LIKE_OR_DISLIKE = "likeOrDislike";
	public static final String DIRTY_FLAG = "isDirtry";
	public static final String AISLE_ID = "aisleId";
	// Column names for aisles images.
	public static final String TITLE = "title";
	public static final String IMAGE_URL = "imageUrl";
	public static final String HEIGHT = "height";
	public static final String WIDTH = "width";
	public static final String DETAILS_URL = "detailsUrl";
	public static final String IMAGE_ID = "imageId";
	public static final String STORE = "store";
	// Column names for dataToSync table.
	public static final String COMMENT = "comment";
	public static final int INVITE_FRIENDS_LOGINACTIVITY_REQUEST_CODE = 24;
	public static final String INVITE_FRIENDS_LOGINACTIVITY_BUNDLE_STRING_KEY = "invitefriendsloginactivitybundlestringkey";
	public static final String CREATE_AISLE_CAMERA_GALLERY_IMAGE_PATH_BUNDLE_KEY = "CREATE_AISLE_CAMERA_GALLERY_IMAGE_PATH_BUNDLE_KEY";
	public static final String FROMCREATEAILSESCREENFLAG = "fromCreateAilseScreenflag";
	public static final int CREATE_AILSE_ACTIVITY_RESULT = 63;
	public static final String FACEBOOK_USER_PROFILE_PICTURE = "FACEBOOK_USER_PROFILE_PICTURE";
	public static final String FACEBOOK_USER_EMAIL = "FACEBOOK_USER_EMAIL";
	public static final String FACEBOOK_USER_NAME = "FACEBOOK_USER_NAME";
	public static final String FACEBOOK_USER_DOB = "FACEBOOK_USER_DOB";
	public static final String FACEBOOK_USER_GENDER = "FACEBOOK_USER_GENDER";
	public static final String FACEBOOK_USER_LOCATION = "FACEBOOK_USER_LOCATION";
	public static final String FACEBOOK_USER_PROFILE_PICTURE_MAIN_URL = "https://graph.facebook.com/";
	public static final String FACEBOOK_USER_PROFILE_PICTURE_SUB_URL = "/picture";
	public static final String GOOGLEPLUS_USER_PROFILE_PICTURE = "GOOGLEPLUS_USER_PROFILE_PICTURE";
	public static final String GOOGLEPLUS_USER_EMAIL = "GOOGLEPLUS_USER_EMAIL";
	public static final String GOOGLEPLUS_USER_NAME = "GOOGLEPLUS_USER_NAME";
	public static final String GOOGLEPLUS_USER_DOB = "GOOGLEPLUS_USER_DOB";
	public static final String GOOGLEPLUS_USER_GENDER = "GOOGLEPLUS_USER_GENDER";
	public static final String GOOGLEPLUS_USER_LOCATION = "GOOGLEPLUS_USER_LOCATION";
	public static final String USER_lOCATION = "name";
	public static final String VUE_APP_CAMERAPICTURES_FOLDER = "VueAppCameraPictures";
	public static final String VUE_APP_RESIZED_PICTURES_FOLDER = "VueAppResizedPictures";
	public static final String DATA_ENTRY_FACEBOOK_INVITE_FRIENDS_BUNDLE_FLAG = "DATA_ENTRY_FACEBOOK_INVITE_FRIENDS_BUNDLE_FLAG";

	// Column names for lookingFor, occasion, category table.
	public static final String KEYWORD = "keyWord";
	public static final String LAST_USED_TIME = "lastUsedTime";
	public static final String NUMBER_OF_TIMES_USED = "numberOfTimesUsed";
	public static final String AMAZON_APP_PACKAGE_NAME = "com.amazon.mShop.android";
	public static final String AMAZON_APP_ACTIVITY_NAME = "com.amazon.mShop.home.HomeActivity";

	// User Profile details
	public static final String USER_NAME = "userName";
	public static final String USER_PROFILE_PICTURE = "userProfilePicture";
	public static final String USER_EMAIL = "userEmail";
	public static final String USER_DOB = "userDob";
	public static final String USER_GENDER = "userGender";
	public static final String USER_LOCATION = "userLocation";
	
	// Column names for comments table.
	public static final String COMMENTS = "commentS";
	
	// Define CONTENT URI
	public static final String AUTHORITY = 
	    "com.lateralthoughts.vue.connectivity.VueConstants";
	public static final String AISLES = "aisles";
	public static final String AISLE_IMAGES = "aisleImages";
	public static final String LOOKING_FOR_TABLE = "lookingFor";
	public static final String OCCASION_TABLE = "occasion";
	public static final String CATEGORY_TABLE = "category";
	public static final String COMMENTS_ON_IMAGES_TABLE = "commentsOnImages";
	  
	// Define MIME types
	public static final String ARTICLES_MIME_TYPE 
	      = "vnd.android.cursor.dir/vnd.vue.articles";
	public static final String ARTICLE_MIME_TYPE 
	      = "vnd.android.cursor.item/vnd.vue.article";
	  
	// Uri to the table of database.
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
	    + "/" + AISLES);
	public static final Uri IMAGES_CONTENT_URI = Uri.parse("content://" + AUTHORITY
	    + "/" + AISLE_IMAGES);
	public static final Uri LOOKING_FOR_CONTENT_URI = Uri.parse("content://" + AUTHORITY
	    + "/" + LOOKING_FOR_TABLE);
	public static final Uri OCCASION_CONTENT_URI = Uri.parse("content://" + AUTHORITY
        + "/" + OCCASION_TABLE);
	public static final Uri CATEGORY_CONTENT_URI = Uri.parse("content://" + AUTHORITY
        + "/" + CATEGORY_TABLE);
	public static final Uri COMMENTS_ON_IMAGE_URI = Uri.parse("content://" + AUTHORITY
	    + "/" + COMMENTS_ON_IMAGES_TABLE);
}
