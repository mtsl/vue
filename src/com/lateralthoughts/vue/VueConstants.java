package com.lateralthoughts.vue;

import android.net.Uri;

/**
 * Here we will declare all constants related to Vue application.
 * 
 */
public class VueConstants {
    public static final String USER_FINDFRIENDS_OPEN_COUNT = "openCount";
    public static final String USER_FINDFRIENDS_OPEN_TIME = "opendTime";
    public static final String DETAILS_USER_FINDFRIENDS_OPEN_COUNT = "detailsopenCount";
    public static final String IS_ALREADY_VIEWED_DETAILS_SCREEN = "isAlreadyViewedDetailsScreen";
    public static final String DETAILS_USER_FINDFRIENDS_OPEN_TIME = "detailsopendTime";
    public static final String USER_REEDM_POINTS = "redeemPoints";
    public static final String USER_LIKES_POINTS = "userLikes";
    public static final String USER_POINTS_DIALOG_SHOWN = "pointsDialog";
    public static final String USER_COMMENTS_POINTS = "userComments";
    public static final String USER_ADD_IAMGE_POINTS = "addImage";
    public static final String USER_INVITE_FRIEND_POINTS = "inviteFriends";
    public static final String DETAIALS_HELP_SHOWN = "DetailsHelp";
    public static final String DETAIALS_HELP_SHOWN_TIME = "DetailsHelpTime";
    public static final String TRENDING_SWIPE_COUNT = "TrendingSwipeCount";
    public static final String TRENDING_SWIPE_BLOCK = "TrendigSwipBlock";
    
    public static final String DETAIALS_HELP_BLOCK = "DetailsHelpBlock";
    public static final String NO_IMAGE_URL = "noimageurl";
    public static final int NO_IMAGE_WIDTH = 200;
    public static final int NO_IMAGE_HEIGHT = 200;
    public static final int IMG_LIKE_STATUS = 1;
    public static final int IMG_NONE_STATUS = 0;
    public static final String AISLE_STATGE_ONE = "frist_staage";
    public static final String AISLE_STAGE_TWO = "second_stage";
    public static final String AISLE_STAGE_THREE = "third_stage";
    public static final String SHAREDPREFERENCE_NAME = "VuePreferences";
    public static final String HELP_SCREEN_ACCES = "HelpScreenAccess";
    public static final String DETAILS_HELP_SCREEN_ACCES = "DetailsHelpScreenAccess";
    public static final String AISLE_SWIPE = "AISLESWIPE";
    public static final String APP_FIRST_TIME_OPENED_TIME = "AppFirstTimeOpenedTime";
    public static final String SCREEN_REFRESH_TIME = "TrendingRefreshTimeInMinutes";
    public static final String VERSION_CODE_CHANGE = "VersionCode";
    public static final String HELP_KEY = "helpScreen";
    public static final String FACEBOOK_ACCESSTOKEN = "FacebookAccessToken";
    public static final String VUE_LOGIN = "VueLoginFlag";
    public static final String GCM_REGISTRATION_ID = "GCMRegistrationId";
    public static final String MIXPANEL_REGISTRATION_ID = "MixpanelRegistrationId";
    public static final String LANDING_SCREEN_RECEIVER = "LandingscreenReceiver";
    public static final String LANDING_SCREEN_RECEIVER_KEY = "LandingScreenReceiverKey";
    public static final String NOTIFICATION_IMAGE_ID = "notficationImageId";
    public static final String NOTIFICATION_AISLE_ID = "notficationAisleId";
    public static final String FACEBOOK = "Facebook";
    public static final String GOOGLEPLUS = "Googleplus";
    public static final String HelpSCREEN_FROM_LANDING = "HelpScreenFromLanding";
    public static final String FACEBOOK_LOGIN = "FacebookLoginFlag";
    public static final String INSTAGRAM_LOGIN = "InstagramLoginFlag";
    public static final String GOOGLEPLUS_LOGIN = "GoogleplusLoginFlag";
    public static final String CREATED_AISLE_COUNT_IN_PREFERENCE = "createdAisleCountInPreference";
    public static final String COMMENTS_COUNT_IN_PREFERENCES = "commentsCountInPreferences";
    public static final int CREATE_AISLE_LIMIT_FOR_LOGIN = 5;
    public static final int COMMENTS_LIMIT_FOR_LOGIN = 10;
    public static final int SELECT_PICTURE = 1;
    public static final int CAMERA_REQUEST = 2;
    public static final int AISLE_INFO_UPLOAD_NOTIFICATION_ID = 1;
    public static final int IMAGE_DELETE_NOTIFICATION_ID = 2;
    public static final int CHANGE_USER_NOTIFICATION_ID = 3;
    public static final int GCM_NOTIFICATION_ID = 4;
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.sss";
    public static final String FACEBOOK_GETFRIENDS_URL = "https://graph.facebook.com/me/friends?access_token=";
    public static final String FACEBOOK_FRIENDS_DETAILS = "&fields=id,name,picture";
    public static final int SHARE_INTENT_REQUEST_CODE = 1;
    public static final int AMAZON_APP_REQUEST_CODE = 39;
    public static final String FIRST_TIME_OPENS_THEAPP = "FirstTimeOpenstheApp";
    public static final String GOOGLEPLUS_AUTOMATIC_LOGIN = "googleplusautomaticlogin";
    public static final String FACEBOOK_APP_NAME = "Facebook";
    public static final String TWITTER_APP_NAME = "Twitter";
    public static final String GMAIL_APP_NAME = "Gmail";
    public static final String INSTAGRAM_APP_NAME = "Instagram";
    public static final String GOOGLEPLUS_APP_NAME = "Google+";
    public static final String FACEBOOK_PACKAGE_NAME = "com.facebook.katana";
    public static final String TWITTER_PACKAGE_NAME = "com.twitter.android";
    public static final String GOOGLE_PLAY_SERVICES_PACKAGE_NAME = "com.google.android.gms";
    public static final String GMAIL_PACKAGE_NAME = "com.google.android.gm";
    public static final String GOOGLEPLUS_PACKAGE_NAME = "com.google.android.apps.plus";
    public static final String INSTAGRAM_PACKAGE_NAME = "com.instagram.android";
    public static final String INSTAGRAM_ACTIVITY_NAME = "com.instagram.android.activity.MainTabActivity";
    public static final String VUE_ACTIVITY_NAME = "com.lateralthoughts.vue.VueLandingPageActivity";
    public static final String GMAIL_ACTIVITY_NAME = "com.google.android.gm.ComposeActivityGmail";
    public static final String GOOGLEPLUS_ACTIVITY_NAME = "com.google.android.apps.plus.phone.SignOnActivity";
    public static final String CANCEL_BTN_DISABLE_FLAG = "cancelbuttondisableflag";
    public static final String GUEST_LOGIN_MESSAGE = "guestLoginMesage";
    public static final String SHOW_AISLE_SWIPE_HELP_LAYOUT_FLAG = "ShowAisleSwipeHelpLayoutFlag";
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
    public static final String TOUCH_TO_CHANGE_IMAGE_FLAG = "TouchToChangeImageFlag";
    public static final String TOUCH_TO_CHANGE_IMAGE_POSITION = "TouchToChangeImagePosition";
    public static final String TOUCH_TO_CHANGE_IMAGE_TEMP_POSITION = "TouchToChangeImageTempPosition";
    public static final String DATAENTRY_ADDIMAGE_AISLE_FLAG = "DataentrdyAddImageAisleFlag";
    public static final String DATAENTRY_TOP_ADDIMAGE_AISLE_FLAG = "DataentryTopAddImageAisleFlag";
    public static final String DATAENTRY_TOP_ADDIMAGE_AISLE_LOOKINGFOR = "DataentryTopAddImageAisleLookingFor";
    public static final String DATAENTRY_TOP_ADDIMAGE_AISLE_OCCASION = "DataentryTopAddImageAisleOccasion";
    public static final String INCREAMENT_MIXPANEL_POSTCOUNT = "IncreamentMixpanelPostCount";
    public static final String DATAENTRY_TOP_ADDIMAGE_AISLE_CATEGORY = "DataentryTopAddImageAisleCategory";
    public static final String DATAENTRY_TOP_ADDIMAGE_AISLE_DESCRIPTION = "DataentryTopAddImageAisleDescription";
    public static final String LOAD_DATAENTRY_SCREEN_FLAG = "LoadDataentryScreenFlag";
    public static final String AISLE_IMAGE_PATH_LIST_FILE_NAME = "AisleImagePathListFileName";
    public static final String USER_PROFILE_IMAGE_FILE_NAME = "VueUserProfileImage";
    public static final String AISLE_ORDER = "aisleOrder";
    // Column names for aisles table.
    public static final String SERIAL_NO = "serialNo";
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
    
    public static final String IMAGE_FROM = "Image From";
    public static final String CAMERA_IMAGE = "Camera";
    public static final String GALLERY_IMAGE = "Gallery";
    
    public static final String EDIT_IMAGE_FROM_DETAILS_SCREEN_FALG = "EDIT_IMAGE_FROM_DETAILS_SCREEN_FALG";
    public static final String FROMCREATEAILSESCREENFLAG = "fromCreateAilseScreenflag";
    public static final String FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_FLAG = "fromDetailsscreentoCreateAilseScreenflag";
    public static final String FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_IS_USER_AISLE_FLAG = "FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_IS_USER_AISLE_FLAG";
    public static final String FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_LOOKINGFOR = "FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_LOOKINGFOR";
    public static final String FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_FINDAT = "FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_FINDAT";
    public static final String FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_IMAGEURL = "FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_IMAGEURL";
    public static final String FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_OCCASION = "FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_OCCASION";
    public static final String FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_CATEGORY = "FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_CATEGORY";
    public static final String FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_IMAGE_POSITION = "FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_IMAGE_POSITION";
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
    public static final String VUE_APP_HELP_FOLDER = "VueHelpPictures";
    public static final String VUE_APP_RESIZED_PICTURES_FOLDER = "VueAppResizedPictures";
    public static final String VUE_APP_USER_PROFILE_PICTURES_FOLDER = "VueAppProfilePicture";
    public static final String VUE_INSTALLED_APP_ICONS_FILENAME = "VueInstalledAppIcons";
    public static final String VUE_APP_USEROBJECT__FILENAME = "vueuser.ser";
    public static final String VUE_APP_USERPROFILEOBJECT__FILENAME = "vueuserprofile.ser";
    public static final String DATA_ENTRY_FACEBOOK_INVITE_FRIENDS_BUNDLE_FLAG = "DATA_ENTRY_FACEBOOK_INVITE_FRIENDS_BUNDLE_FLAG";
    public static final String IMAGE_COMMENT_OWNER_IMAGE_URL = "imageCommentOwnerImageURL";
    
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
    public static final String ALL_RATED_IMAGES = "allRatedImages";
    public static final String BOOKMARKER_AISLES = "bookmarkedAisles";
    public static final String MY_BOOKMARKED_AISLES = "myBookmarkedAisles";
    
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
    public static final Uri MY_BOOKMARKED_AISLES_URI = Uri.parse("content://"
            + AUTHORITY + "/" + MY_BOOKMARKED_AISLES);
    public static final Uri ALL_RATED_IMAGES_URI = Uri.parse("content://"
            + AUTHORITY + "/" + ALL_RATED_IMAGES);
    
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
    public static final String AISLE_OWNER_IMAGE_URL = "aisleOwnerImageURL";
    public static final String AISLE_BOOKMARK_COUNT = "bookmarkCount";
    // Aisle Images Response Keys
    public static final String AISLE_IMAGE_OBJECT = "imageList";
    public static final String AISLE_IMAGE_LIST = "images";
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
    public static final String AISLE_IMAGE_RATINGS = "ratings";
    public static final String AISLE_IMAGE_COMMENTS_ID = "id";
    public static final String AISLE_IMAGE_COMMENTS_IMAGEID = "ownerImageId";
    public static final String AISLE_IMAGE_COMMENTS_USERID = "ownerUserId";
    public static final String AISLE_IMAGE_COMMENTS_FIRST_NAME = "commenterFirstName";
    public static final String AISLE_IMAGE_COMMENTS_LAST_NAME = "commenterLastName";
    public static final String AISLE_IMAGE_COMMENTS_COMMENT = "comment";
    public static final String AISLE_IMAGE_COMMENTS_LASTMODIFIED_TIME = "lastModifiedTimestamp";
    public static final String AISLE_IMAGE_COMMENTS_CREATED_TIME = "createdTimestamp";
    public static final String AISLE_IMAGE_COMMENTER_LAST_NAME = "imageCommentOwnerLastName";
    public static final String AISLE_IMAGE_COMMENTER_FIRST_NAME = "imageCommentOwnerFirstName";
    public static final String AISLE_IMAGE_RATING_ID = "id";
    public static final String AISLE_IMAGE_RATING_LIKED = "liked";
    public static final String AISLE_IMAGE_RATING_AISLEID = "aisleId";
    public static final String AISLE_IMAGE_RATING_IMAGEID = "imageId";
    public static final String AISLE_IMAGE_RATING_OWNER_FIRST_NAME = "ratingOwnerFirstName";
    public static final String AISLE_IMAGE_RATING_OWNER_LAST_NAME = "ratingOwnerLastName";
    public static final String AISLE_IMAGE_RATING_LASTMODIFIED_TIME = "lastModifiedTimestamp";
    // public static final String AISLE_IMAGE_USER_RATING = "userRating";
    // User response Keys
    public static final String USER_RESPONSE_ID = "id";
    public static final String USER_JOINTIME = "joinTime";
    public static final String USER_DEVICE_ID = "deviceId";
    public static final String USER_EMAIL = "email";
    public static final String USER_FIRST_NAME = "firstName";
    public static final String USER_LAST_NAME = "lastName";
    public static final String USER_FACEBOOK_ID = "facebookId";
    public static final String USER_GOOGLEPLUS_ID = "googlePlusId";
    public static final String USER_PROFILE_IMAGE_URL = "userImageURL";
    // Reciver constants
    public static final int AISLE_TRENDING_LIST_DATA = 1;
    public static final int AISLE_TRENDING_PARSED_DATA = 2;
    
    public static final String RECENTLY_VIEWED_AISLE_ID = "recentlyViewedAisleId";
    public static final String VIEW_TIME = "viewTime";
    public static final String IS_LIKED_OR_BOOKMARKED = "isLikedOrBookmarked";
    
    // Keys for values stored in shared preference
    public static final String IS_AISLE_DIRTY = "isAisleDirty";
    public static final String IS_IMAGE_DIRTY = "isImageDirty";
    public static final String IS_COMMENT_DIRTY = "isCommentDirty";
    public static final String IS_BOOKMARK_DIRTY = "isBookmarkDirty";
    
    public static final String LIKE_COUNT = "likeCount";
    public static final String LAST_MODIFIED_TIME = "lastModifiedTimestamp";
    public static final String LIKED = "liked";
    public static final String BOOKMARKED = "bookmarked";
    public static final String COMMENTER_URL = "commenterUrl";
    public static final String ADMIN_MAIL_ADDRESS = "admin@thesilverlabs.com";
}
