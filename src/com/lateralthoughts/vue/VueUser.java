package com.lateralthoughts.vue;

import java.io.Serializable;
import java.util.Locale;

import android.util.Log;

/**
 * UserCredentials in Vue involves creating a Vue User account on the backend to
 * which we can associate identity layers such as FB id, G+ id, etc. A Vue User
 * may choose to not provide their identity (FB, G+) as they begin exploring the
 * app but may decide to login with a specific id at a later point in time.
 * 
 * 
 */

public class VueUser implements Serializable {

	private String vueId;

	public String getInstagramId() {
		return instagramId;
	}

	public void setInstagramId(String mInstagramId) {
		this.instagramId = mInstagramId;
	}

	private String instagramId;

	public VueUser(String facebookId, String googlePlusId, String instagramId,
			String deviceId, String emailId) {
		this.emailId = emailId;
		this.facebookId = facebookId;
		this.googlePlusId = googlePlusId;
		this.instagramId = instagramId;
		this.deviceId = deviceId;
	}

	public VueUser(String deviceId, String facebookId, String googlePlusId,
			String instagramId, String firstName, String lastName) {
		this.deviceId = deviceId;
		this.facebookId = facebookId;
		this.googlePlusId = googlePlusId;
		this.firstName = firstName;
		this.lastName = lastName;
	}

	public VueUser() {

	}

	public void setVueUserId(String vueId) {
		this.vueId = vueId;
	}

	public String getVueId() {
		Log.i("userid", "userid123456: " + vueId);
		return vueId;
	}

	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}

	public void setUserIdentityMethod(
			VueUserManager.PreferredIdentityLayer identity) {
		userIdentifier = identity;
	}

	public VueUserManager.PreferredIdentityLayer getUserIdentity() {
		return userIdentifier;
	}

	private String birthday;

	public String getFacebookId() {
		return facebookId;
	}

	public void setFacebookId(String mFacebookId) {
		this.facebookId = mFacebookId;
	}

	public String getGooglePlusId() {
		return googlePlusId;
	}

	public void setGooglePlusId(String mGooglePlusId) {
		this.googlePlusId = mGooglePlusId;
	}

	private String facebookId;
	private String googlePlusId;

	public String getmFirstName() {
		return firstName;
	}

	public String getmLastName() {
		return lastName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	private String firstName;
	private String lastName;
	private Locale userLocale;
	private String emailId;

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String mEmailId) {
		this.emailId = mEmailId;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String mDeviceId) {
		this.deviceId = mDeviceId;
	}

	private String deviceId;
	private VueUserManager.PreferredIdentityLayer userIdentifier;
}
