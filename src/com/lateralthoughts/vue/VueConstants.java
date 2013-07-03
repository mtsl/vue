package com.lateralthoughts.vue;

/**
 * Here we will declare all constants related to Vue application.
 * @author krishna
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
        
        public static final String FACEBOOK_GETFRIENDS_URL = "https://graph.facebook.com/me/friends?access_token=";
        public static final String FACEBOOK_FRIENDS_DETAILS = "&fields=id,name,picture"; 
        
        public static final String FACEBOOK_APP_NAME = "Facebook";
        public static final String TWITTER_APP_NAME = "Twitter";
        public static final String GMAIL_APP_NAME = "Gmail";
        public static final String GOOGLEPLUS_APP_NAME = "Google+";
        
        public static final String FACEBOOK_PACKAGE_NAME = "com.facebook.android";
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

        
        // Column names for aisles table.
        public static final String ID = "_id";
        public static final String CATEGORY = "category";
        public static final String FIRST_NAME = "firstName";
        public static final String LAST_NAME = "lastName";
        public static final String JOIN_TIME = "joinTime";
        public static final String USER_ID = "userId";
        public static final String LOOKING_FOR = "lookingFor";
        public static final String OCCASION = "occasion";
        public static final String AISLE_ID = "aisleId";
        
        // Column names for aisles images.
        public static final String TITLE = "title";
        public static final String IMAGE_URL = "imageUrl";
        public static final String HEIGHT = "height";
        public static final String WIDTH = "width";
        public static final String DETAILS_URL = "detailsUrl";
        public static final String IMAGE_ID = "id";
        public static final String STORE = "store";

}