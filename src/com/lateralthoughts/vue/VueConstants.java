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
	public static final String INSTAGRAM_LOGIN = "InstagramLoginFlag";
	public static final String GOOGLEPLUS_LOGIN = "GoogleplusLoginFlag";
	public static final String CREATED_AISLE_COUNT_IN_PREFERENCE = "createdAisleCountInPreference";
	public static final String COMMENTS_COUNT_IN_PREFERENCES = "commentsCountInPreferences";
	public static final int CREATE_AISLE_LIMIT_FOR_LOGIN = 5;
	public static final int COMMENTS_LIMIT_FOR_LOGIN = 10;
	public static final int SELECT_PICTURE = 1;
	public static final int CAMERA_REQUEST = 2;
	public static final int CREATE_AISLE_NOTIFICATION_ID = 1;
	public static final int ADD_IMAGE_TO_AISLE_NOTIFICATION_ID = 2;
	public static final int CHANGE_USER_NOTIFICATION_ID = 3;
	public static final int UPLOAD_IMAGE_TO_SERVER_NOTIFICATION_ID = 4;
	public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.sss";
	public static final String FACEBOOK_GETFRIENDS_URL = "https://graph.facebook.com/me/friends?access_token=";
	public static final String FACEBOOK_FRIENDS_DETAILS = "&fields=id,name,picture";
	public static final int SHARE_INTENT_REQUEST_CODE = 1;
	public static final int AMAZON_APP_REQUEST_CODE = 39;
	public static final String GOOGLEPLUS_AUTOMATIC_LOGIN = "googleplusautomaticlogin";
	public static final String FACEBOOK_APP_NAME = "Facebook";
	public static final String TWITTER_APP_NAME = "Twitter";
	public static final String GMAIL_APP_NAME = "Gmail";
	public static final String INSTAGRAM_APP_NAME = "Instagram";
	public static final String GOOGLEPLUS_APP_NAME = "Google+";
	public static final String FACEBOOK_PACKAGE_NAME = "com.facebook.katana";
	public static final String TWITTER_PACKAGE_NAME = "com.twitter.android";
	public static final String GMAIL_PACKAGE_NAME = "com.google.android.gm";
	public static final String GOOGLEPLUS_PACKAGE_NAME = "com.google.android.apps.plus";
	public static final String INSTAGRAM_PACKAGE_NAME = "com.instagram.android";
	public static final String TWITTER_ACTIVITY_NAME = "com.twitter.android.PostActivity";
	public static final String INSTAGRAM_ACTIVITY_NAME = "com.instagram.android.activity.MainTabActivity";
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
	public static final String FROM_OTHER_SOURCES_FLAG = "fromothersourcesflag";
	public static final String FROM_OTHER_SOURCES_URL = "fromothersourcesurl";
	public static final String FROM_OTHER_SOURCES_IMAGE_URIS = "fromothersourcesimageuris";
	public static final String GOOGLEPLUS_FRIEND_INVITE = "GOOGLEPLUS_FRIEND_INVITE";
	public static final String GOOGLEPLUS_FRIEND_INDEX = "GOOGLEPLUS_FRIEND_INDEX";
	public static final String GOOGLEPLUS_FRIEND_IMAGE_PATH_LIST_KEY = "GOOGLEPLUS_FRIEND_INDEX_PATH_LIST_KEY";
	public static final String FROM_DETAILS_SCREEN_TO_DATAENTRY_CREATE_AISLESCREEN_FLAG = "FromDetailsScreenToDataentryCreateAisleScreenFlag";
	public static final String DATAENTRY_ADDIMAGE_AISLE_FLAG = "DataentryAddImageAisleFlag";
	public static final String DATAENTRY_EDIT_AISLE_FLAG = "DataentryEditAisleFlag";
	public static final String LOAD_DATAENTRY_SCREEN_FLAG = "LoadDataentryScreenFlag";
	public static final String AISLE_IMAGE_PATH_LIST_FILE_NAME = "AisleImagePathListFileName";
	public static final String ETSY_PACKAGE_NAME = "com.etsy.android";
	public static final String FANCY_PACKAGE_NAME = "com.thefancy.app";
	public static final String ETSY_ACTIVITY_NAME = "com.etsy.android.ui.HomeActivity";
	public static final String FANCY_ACTIVITY_NAME = "com.thefancy.app.common.Main";
	public static final String DATAENTRY_SCREEN_AISLE_ID = "DataentryScreenAisleId";
	public static final String USER_PROFILE_IMAGE_FILE_NAME = "VueUserProfileImage";
	public static final String AISLE_ORDER = "aisleOrder";
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
	public static final String LIKES_COUNT = "likesCount";
	public static final String DIRTY_FLAG = "isDirtry";
	public static final String AISLE_Id = "aisleId";
	public static final String JSON_OBJ_ID = "id";
	public static final String DELETE_FLAG = "deleteFlag";
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
	public static final String FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_FLAG = "fromDetailsscreentoCreateAilseScreenflag";
	public static final String FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_IS_USER_AISLE_FLAG = "FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_IS_USER_AISLE_FLAG";
	public static final String FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_LOOKINGFOR = "FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_LOOKINGFOR";
	public static final String FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_FINDAT = "FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_FINDAT";
	public static final String FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_IMAGEURL = "FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_IMAGEURL";
	public static final String FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_OCCASION = "FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_OCCASION";
	public static final String FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_CATEGORY = "FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_CATEGORY";
	public static final String FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_SAYSOMETHINGABOUTAISLE = "FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_SAYSOMETHINGABOUTAISLE";
	public static final String FROM_DETAILS_SCREEN_TO_DATAENTRY_SCREEN_FLAG = "fromDetailsscreentoDataentryScreenflag";
	public static final String FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_IMAGE_WIDTH = "FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_IMAGE_WIDTH";
	public static final String FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_IMAGE_HEIGHT = "FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_IMAGE_HEIGHT";
	public static final String FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_IMAGE_DETAILSURL = "FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_IMAGE_DETAILSURL";
	public static final String FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_IMAGE_STORE = "FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_IMAGE_STORE";
	public static final String FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_OFFLINE_IMAGE_ID = "FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_OFFLINE_IMAGE_ID";
	public static final int CREATE_AILSE_ACTIVITY_RESULT = 63;
	public static final int FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_ACTIVITY_RESULT = 64;
	public static final int FROM_DETAILS_SCREEN_TO_DATAENTRY_SCREEN_ACTIVITY_RESULT = 65;
	public static final String FACEBOOK_USER_PROFILE_PICTURE_MAIN_URL = "https://graph.facebook.com/";
	public static final String FACEBOOK_USER_PROFILE_PICTURE_SUB_URL = "/picture";
	public static final String USER_lOCATION = "name";
	public static final String VUE_APP_CAMERAPICTURES_FOLDER = "VueAppCameraPictures";
	public static final String VUE_APP_RESIZED_PICTURES_FOLDER = "VueAppResizedPictures";
	public static final String VUE_APP_USER_PROFILE_PICTURES_FOLDER = "VueAppProfilePicture";
	public static final String VUE_APP_USEROBJECT__FILENAME = "vueuser.ser";
	public static final String VUE_APP_USERPROFILEOBJECT__FILENAME = "vueuserprofile.ser";
	public static final String DATA_ENTRY_FACEBOOK_INVITE_FRIENDS_BUNDLE_FLAG = "DATA_ENTRY_FACEBOOK_INVITE_FRIENDS_BUNDLE_FLAG";

	// Column names for lookingFor, occasion, category table.
	public static final String KEYWORD = "keyWord";
	public static final String LAST_USED_TIME = "lastUsedTime";
	public static final String NUMBER_OF_TIMES_USED = "numberOfTimesUsed";

	// Column names for comments table.
	public static final String COMMENTS = "commentS";

	// Define CONTENT URI
	public static final String AUTHORITY = "com.lateralthoughts.vue.connectivity.VueConstants";
	public static final String AISLES = "aisles";
	public static final String AISLE_IMAGES = "aisleImages";
	public static final String LOOKING_FOR_TABLE = "lookingFor";
	public static final String OCCASION_TABLE = "occasion";
	public static final String CATEGORY_TABLE = "category";
	public static final String COMMENTS_ON_IMAGES_TABLE = "commentsOnImages";
	public static final String RECENTLY_VIEWED_AISLES = "recentlyViewAisles";
	public static final String RATED_IMAGES = "ratedImages";
	public static final String BOOKMARKER_AISLES = "bookmarkedAisles";

	// Define MIME types
	public static final String ARTICLES_MIME_TYPE = "vnd.android.cursor.dir/vnd.vue.articles";
	public static final String ARTICLE_MIME_TYPE = "vnd.android.cursor.item/vnd.vue.article";

	// Uri to the table of database.
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + AISLES);
	public static final Uri IMAGES_CONTENT_URI = Uri.parse("content://"
			+ AUTHORITY + "/" + AISLE_IMAGES);
	public static final Uri LOOKING_FOR_CONTENT_URI = Uri.parse("content://"
			+ AUTHORITY + "/" + LOOKING_FOR_TABLE);
	public static final Uri OCCASION_CONTENT_URI = Uri.parse("content://"
			+ AUTHORITY + "/" + OCCASION_TABLE);
	public static final Uri CATEGORY_CONTENT_URI = Uri.parse("content://"
			+ AUTHORITY + "/" + CATEGORY_TABLE);
	public static final Uri COMMENTS_ON_IMAGE_URI = Uri.parse("content://"
			+ AUTHORITY + "/" + COMMENTS_ON_IMAGES_TABLE);
	public static final Uri RECENTLY_VIEW_AISLES_URI = Uri.parse("content://"
			+ AUTHORITY + "/" + RECENTLY_VIEWED_AISLES);
	public static final Uri RATED_IMAGES_URI = Uri.parse("content://"
			+ AUTHORITY + "/" + RATED_IMAGES);
	public static final Uri BOOKMARKER_AISLES_URI = Uri.parse("content://"
			+ AUTHORITY + "/" + BOOKMARKER_AISLES);

	public static final String GOOGLEPLUS_USER_EMAIL = "GOOGLEPLUS_USER_EMAIL";
	public static final String INVITATION_MESG = "Invitation from Vue application.";
	public static final String FACEBOOK_GRAPHIC_OBJECT_NAME_KEY = "name";
	public static final String FACEBOOK_GRAPHIC_OBJECT_EMAIL_KEY = "email";
	public static final String FACEBOOK_GRAPHIC_OBJECT_GENDER_KEY = "gender";
	public static final String DATA_ENTRY_INVITE_FRIENDS_BUNDLE_FROM_GOOGLEPLUS_FLAG_KEY = "dataentryinvitefriendsbundlefromgoogleplusflagkey";
	public static final String DATA_ENTRY_INVITE_FRIENDS_BUNDLE_FROM_FILE_PATH_ARRAY_KEY = "dataentryinvitefriendsbundlepatharraykey";
	// Aisle Response Keys
	public static final String AISLE_ID = "id";
	public static final String AISLE_CATEGORY = "category";
	public static final String AISLE_LOOKINGFOR = "lookingFor";
	public static final String AISLE_NAME = "name";
	public static final String AISLE_OCCASSION = "occassion";
	public static final String AISLE_OWNER_USER_ID = "ownerUserId";
	public static final String AISLE_DESCRIPTION = "description";
	public static final String AISLE_OWNER_FIRSTNAME = "aisleOwnerFirstName";
	public static final String AISLE_OWNER_LASTNAME = "aisleOwnerLastName";
	public static final String AISLE_BOOKMARK_COUNT = "bookmarkCount";
	// Aisle Images Response Keys
	public static final String AISLE_IMAGE_ID = "id";
	public static final String AISLE_IMAGE_OWNERUSER_ID = "ownerUserId";
	public static final String AISLE_IMAGE_OWNER_AISLE_ID = "ownerAisleId";
	public static final String AISLE_IMAGE_DETAILS_URL = "detailsUrl";
	public static final String AISLE_IMAGE_HEIGHT = "height";
	public static final String AISLE_IMAGE_WIDTH = "width";
	public static final String AISLE_IMAGE_IMAGE_URL = "imageUrl";
	public static final String AISLE_IMAGE_RATING = "likeRatingCount"/* "rating" */;
	public static final String AISLE_IMAGE_STORE = "store";
	public static final String AISLE_IMAGE_TITLE = "title";
	public static final String AISLE_IMAGE_COMMENTS = "comments";
	public static final String AISLE_IMAGE_COMMENTS_ID ="id";
	public static final String AISLE_IMAGE_COMMENTS_IMAGEID ="ownerImageId";
	public static final String AISLE_IMAGE_COMMENTS_USERID = "ownerUserId";
	public static final String AISLE_IMAGE_COMMENTS_FIRST_NAME = "commenterFirstName";
	public static final String AISLE_IMAGE_COMMENTS_LAST_NAME = "commenterLastName";
    public static final String AISLE_IMAGE_COMMENTS_COMMENT = "comment";
    public static final String AISLE_IMAGE_COMMENTS_LASTMODIFIED_TIME = "lastModifiedTimestamp";
    public static final String AISLE_IMAGE_COMMENTS_CREATED_TIME = "createdTimestamp";
	 
	// User response Keys
	public static final String USER_RESPONSE_ID = "id";
	public static final String USER_JOINTIME = "joinTime";
	public static final String USER_DEVICE_ID = "deviceId";
	public static final String USER_EMAIL = "email";
	public static final String USER_FIRST_NAME = "firstName";
	public static final String USER_LAST_NAME = "lastName";
	public static final String USER_FACEBOOK_ID = "facebookId";
	public static final String USER_GOOGLEPLUS_ID = "googlePlusId";
	// Reciver constants
	public static final int AISLE_TRENDING_LIST_DATA = 1;
	public static final int AISLE_TRENDING_PARSED_DATA = 2;

	public static final String RECENTLY_VIEWED_AISLE_ID = "recentlyViewedAisleId";
	public static final String VIEW_TIME = "viewTime";
	public static final String IS_LIKED_OR_BOOKMARKED = "isLikedOrBookmarked";

	// Keys for values stored in shared preference
	public static final String IS_AISLE_DIRTY = "isAisleDirty";
	public static final String IS_IMAGE_DIRTY = "isImageDirty";

	public static final String LIKE_COUNT = "likeCount";
	public static final String LAST_MODIFIED_TIME = "lastModifiedTimestamp";
	public static final String LIKED = "liked";
	public static final String BOOKMARKED = "bookmarked";
}
