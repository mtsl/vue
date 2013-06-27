package com.lateralthoughts.vue;

/**
 * Here we will declare all constants related to Vue application.
 * @author krishna
 *
 */
public class VueConstants {

        public static final String SHAREDPREFERENCE_NAME = "VuePreferences";
        public static final String FACEBOOK_ACCESSTOKEN = "FacebookAccessToken";
        public static final String VUELOGIN = "VueLogin";
        public static final String FACEBOOK = "Facebook";
        public static final String GOOGLEPLUS = "Googleplus";
        public static final String FIRSTTIME_LOGIN_PREFRENCE_FLAG = "firstTimeLoginPrefrenceFlag";
        public static final String CREATED_AISLE_COUNT_IN_PREFERENCE = "createdAisleCountInPreference";
        public static final String COMMENTS_COUNT_IN_PREFERENCES = "commentsCountInPreferences";
        public static final int CREATE_AISLE_LIMIT_FOR_LOGIN = 5;
        public static final int COMMENTS_LIMIT_FOR_LOGIN = 10;
        
        public static final String FACEBOOK_GETFRIENDS_URL = "https://graph.facebook.com/me/friends?access_token=";
        public static final String FACEBOOK_FRIENDS_DETAILS = "&fields=name,picture"; 

}