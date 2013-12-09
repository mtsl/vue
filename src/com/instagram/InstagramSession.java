package com.instagram;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class InstagramSession {

	private SharedPreferences sharedPref;
	private Editor editor;

	private static final String SHARED = "Instagram_Preferences";
	private static final String API_USERNAME = "username";
	private static final String API_ID = "id";
	private static final String API_NAME = "name";
	private static final String API_ACCESS_TOKEN = "access_token";
	private static final String API_BIO = "bio";
	private static final String API_WEBSITE = "website";
	private static final String API_PROFILE_PICTURE = "profile_picture";

	public InstagramSession(Context context) {
		sharedPref = context.getSharedPreferences(SHARED, Context.MODE_PRIVATE);
		editor = sharedPref.edit();
	}

	/**
	 * 
	 * @param accessToken
	 * @param expireToken
	 * @param expiresIn
	 * @param username
	 */
	public void storeAccessToken(String accessToken, String id,
			String username, String name, String bio, String website,
			String profilePicture) {
		editor.putString(API_ID, id);
		editor.putString(API_NAME, name);
		editor.putString(API_ACCESS_TOKEN, accessToken);
		editor.putString(API_USERNAME, username);
		editor.putString(API_BIO, bio);
		editor.putString(API_WEBSITE, website);
		editor.putString(API_PROFILE_PICTURE, profilePicture);
		editor.commit();
	}

	public void storeAccessToken(String accessToken) {
		editor.putString(API_ACCESS_TOKEN, accessToken);
		editor.commit();
	}

	/**
	 * Reset access token and user name
	 */
	public void resetAccessToken() {
		editor.putString(API_ID, null);
		editor.putString(API_NAME, null);
		editor.putString(API_ACCESS_TOKEN, null);
		editor.putString(API_USERNAME, null);
		editor.putString(API_BIO, null);
		editor.putString(API_WEBSITE, null);
		editor.putString(API_PROFILE_PICTURE, null);
		editor.commit();
	}

	/**
	 * Get user name
	 * 
	 * @return User name
	 */
	public String getUsername() {
		return sharedPref.getString(API_USERNAME, null);
	}

	/**
	 * 
	 * @return
	 */
	public String getId() {
		return sharedPref.getString(API_ID, null);
	}

	/**
	 * 
	 * @return
	 */
	public String getName() {
		return sharedPref.getString(API_NAME, null);
	}

	/**
	 * Get access token
	 * 
	 * @return Access token
	 */
	public String getAccessToken() {
		return sharedPref.getString(API_ACCESS_TOKEN, null);
	}

	public String getApiBio() {
		return sharedPref.getString(API_BIO, null);
	}

	public String getApiWebsite() {
		return sharedPref.getString(API_WEBSITE, null);
	}

	public String getApiProfilePicture() {
		return sharedPref.getString(API_PROFILE_PICTURE, null);
	}

	
}