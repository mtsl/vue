package com.lateralthoughts.vue.utils;

public class UrlConstants {

	public static final String SERVER_BASE_URL = "http://2dot1-java.vueapi-canary-development1.appspot.com/";

	/**
	 * (C)reate routine URL's
	 */
	public static final String CREATE_USER_RESTURL = SERVER_BASE_URL
			+ "api/usercreate/trial";

	public static final String CREATE_AISLE_RESTURL = SERVER_BASE_URL
			+ "api/aislecreate";
	public static final String CREATE_IMAGE_RESTURL =
			SERVER_BASE_URL+"api/imagecreate";

	public static final String GET_AISLE_RESTURL = SERVER_BASE_URL
			+ "api/aisleget/id";
	public static final String GET_AISLELIST_BYUSER_RESTURL = SERVER_BASE_URL
			+ "api/aislesget/user";
	public static final String GET_IMAGELIST_RESTURL = SERVER_BASE_URL
			+ "api/imagesget/aisle";

	public static final String GET_TRENDINGAISLES_RESTURL = SERVER_BASE_URL
			+ "api/trendingaislesgetorderedbytime";
 
	public static final String SEARCH_BY_KEYWORD_BASE_URL = SERVER_BASE_URL
			+ "api/getaisleswithmatchingoccassion/";
	public static final String SEARCH_BY_USER = SERVER_BASE_URL
			+ "api/getaisleswithmatchingfacebookORGPlus/";
	public static final String CREATE_BOOKMARK_RESTURL = SERVER_BASE_URL
	    + "api/aislebookmarksaveorupdate";
	
	public static final String GET_BOOKMARK_Aisles = SERVER_BASE_URL
		    + "api/aislebookmarksgetall";
	
	   public static final String CREATE_RATING_RESTURL = SERVER_BASE_URL 
	       + "api/imageratingsaveorupdate";

}
