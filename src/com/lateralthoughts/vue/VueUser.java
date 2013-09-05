package com.lateralthoughts.vue;

import java.io.Serializable;
import java.util.Locale;

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

	public String getFacebookId() {
		return mFacebookId;
	}

	public void setFacebookId(String mFacebookId) {
		this.mFacebookId = mFacebookId;
	}

	public String getGooglePlusId() {
		return mGooglePlusId;
	}

	public void setGooglePlusId(String mGooglePlusId) {
		this.mGooglePlusId = mGooglePlusId;
	}

	private String mFacebookId;
	private String mGooglePlusId;
	private String mFirstName;
	private String mLastName;
	private Locale mUserLocale;
	private String mEmailId;

	public String getEmailId() {
		return mEmailId;
	}

	public void setEmailId(String mEmailId) {
		this.mEmailId = mEmailId;
	}

	public String getDeviceId() {
		return mDeviceId;
	}

	public void setDeviceId(String mDeviceId) {
		this.mDeviceId = mDeviceId;
	}

	private String mDeviceId;
	private VueUserManager.PreferredIdentityLayer mUserIdentifier;
}
