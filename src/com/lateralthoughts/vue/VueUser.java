package com.lateralthoughts.vue;

import android.os.Bundle;
import android.util.Log;
import com.android.volley.*;
import org.json.JSONArray;
import com.android.volley.Response.Listener;
import com.android.volley.Response.ErrorListener;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.io.UnsupportedEncodingException;

/**
 * UserCredentials in Vue involves creating a Vue User account on the backend to
 * which we can associate identity layers such as FB id, G+ id, etc. A Vue User
 * may choose to not provide their identity (FB, G+) as they begin exploring the
 * app but may decide to login with a specific id at a later point in time.
 * 
 * 
 */

public class VueUser implements Serializable {

	public VueUser(String facebookId, String googlePlusId, String deviceId,
			String emailId) {
		mEmailId = emailId;
		mFacebookId = facebookId;
		mGooglePlusId = googlePlusId;
		mDeviceId = deviceId;
	}

	public VueUser() {

	}

	public void setUsersName(String firstName, String lastName) {
		mFirstName = firstName;
		mLastName = lastName;
	}

	public void setBirthday(String birthday) {
		mBirthday = birthday;
	}

	public void setUserIdentityMethod(
			VueUserManager.PreferredIdentityLayer identity) {
		mUserIdentifier = identity;
	}

	public VueUserManager.PreferredIdentityLayer getUserIdentity() {
		return mUserIdentifier;
	}

	private String mBirthday;

	public String getmFacebookId() {
		return mFacebookId;
	}

	public void setmFacebookId(String mFacebookId) {
		this.mFacebookId = mFacebookId;
	}

	public String getmGooglePlusId() {
		return mGooglePlusId;
	}

	public void setmGooglePlusId(String mGooglePlusId) {
		this.mGooglePlusId = mGooglePlusId;
	}

	private String mFacebookId;
	private String mGooglePlusId;
	private String mFirstName;
	private String mLastName;
	private Locale mUserLocale;
	private String mEmailId;

	public String getmEmailId() {
		return mEmailId;
	}

	public void setmEmailId(String mEmailId) {
		this.mEmailId = mEmailId;
	}

	public String getmDeviceId() {
		return mDeviceId;
	}

	public void setmDeviceId(String mDeviceId) {
		this.mDeviceId = mDeviceId;
	}

	private String mDeviceId;
	private VueUserManager.PreferredIdentityLayer mUserIdentifier;
}
