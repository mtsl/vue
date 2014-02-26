package com.lateralthoughts.vue.utils;

public class UrlConstants {
    // Uncomment this to point to dev content server
    // public static final String SERVER_BASE_URL =
    // "http://2dot10-java.vueapi-canary.appspot.com/";
    
    // Uncomment this to point to production content server
    public static final String SERVER_BASE_URL = "http://2dot12-restlet.vueapi-canary.appspot.com/";
    private static final String CANARY_SERVER_PROJECT_ID = "876955216873";
    private static final String DEVELOPMENT_SERVER_PROJECT_ID = "477960328185";
    private static final String DEVELOPMENT1_SERVER_PROJECT_ID = "341676083313";
    public static final String CURRENT_SERVER_PROJECT_ID = CANARY_SERVER_PROJECT_ID;
    
    /**
     * (C)reate routine URL's
     */
    public static final String USER_PUT_RESTURL = SERVER_BASE_URL
            + "api/userput";
    public static final String AISLE_PUT_RESTURL = SERVER_BASE_URL
            + "api/aisleput";
    public static final String IMAGE_PUT_RESTURL = SERVER_BASE_URL
            + "api/imageput";
    public static final String BOOKMARK_PUT_RESTURL = SERVER_BASE_URL
            + "api/aislebookmarkput";
    public static final String IMAGE_RATING_PUT_RESTURL = SERVER_BASE_URL
            + "api/imageratingput";
    public static final String CREATE_AISLECOMMENT_RESTURL = SERVER_BASE_URL
            + "api/aislecommentcreate";
    public static final String IMAGECOMMENT_PUT_RESTURL = SERVER_BASE_URL
            + "api/imagecommentput";
    
    public static final String GET_USER_RESTURL = SERVER_BASE_URL
            + "api/userget/id/";
    public static final String GET_USER_FACEBOOK_ID_RESTURL = SERVER_BASE_URL
            + "api/userget/facebook/";
    public static final String GET_USER_GOOGLEPLUS_ID_RESTURL = SERVER_BASE_URL
            + "api/userget/googleplus/";
    public static final String GET_AISLE_RESTURL = SERVER_BASE_URL
            + "api/aisleget/id/";
    public static final String GET_AISLELIST_BYUSER_RESTURL = SERVER_BASE_URL
            + "api/aislesget/user";
    public static final String GET_IMAGELIST_RESTURL = SERVER_BASE_URL
            + "api/imagesget/aisle";
    public static final String GET_TRENDINGAISLES_RESTURL = SERVER_BASE_URL
            + "api/trendingaislesgetorderedbytime";
    public static final String GET_BOOKMARK_Aisles = SERVER_BASE_URL
            + "api/aislebookmarksgetall";
    public static final String GET_RATINGS_RESTURL = SERVER_BASE_URL
            + "api/imageratingsgetall";
    public static final String GET_IMAGES_FOR_AISLE = SERVER_BASE_URL
            + "api/imagesget/aisle/";
    public static final String GET_UNIQUE_ONETIME_IMAGE_UPLOAD_RESTURL = SERVER_BASE_URL
            + "api/getUrlToUploadImage";
    public static final String GET_IMAGE_FILE_RESTURL = SERVER_BASE_URL
            + "upload";
    
    public static final String SEARCH_BY_KEYWORD_BASE_URL = SERVER_BASE_URL
            + "api/getaisleswithmatchingoccassion/";
    public static final String SEARCH_BY_USER = SERVER_BASE_URL
            + "api/getaisleswithmatchingfacebookORGPlus/";
    
    public static final String DELETE_IMAGE_RESTURL = SERVER_BASE_URL
            + "api/imagedelete";
    public static final String GET_IMAGE_RESTURL = SERVER_BASE_URL
            + "api/imageget/id/";
}
