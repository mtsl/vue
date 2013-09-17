package com.lateralthoughts.vue;

public class VueUserProfile {

	public String getUserProfilePicture() {
		return userProfilePicture;
	}

	public void setUserProfilePicture(String userProfilePicture) {
		this.userProfilePicture = userProfilePicture;
	}

	public String getUserEmail() {
		return userEmail;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserDOB() {
		return userDOB;
	}

	public VueUserProfile(String userProfilePicture, String userEmail,
			String userName, String userDOB, String userGender,
			String userLocation, boolean isUserDetailsModified) {
		this.userProfilePicture = userProfilePicture;
		this.userEmail = userEmail;
		this.userName = userName;
		this.userDOB = userDOB;
		this.userGender = userGender;
		this.userLocation = userLocation;
		this.isUserDetailsModified = isUserDetailsModified;
	}

	public void setUserDOB(String userDOB) {
		this.userDOB = userDOB;
	}

	public String getUserGender() {
		return userGender;
	}

	public void setUserGender(String userGender) {
		this.userGender = userGender;
	}

	public String getUserLocation() {
		return userLocation;
	}

	public void setUserLocation(String userLocation) {
		this.userLocation = userLocation;
	}

	private String userProfilePicture;
	private String userEmail;
	private String userName;
	private String userDOB;
	private String userGender;
	private String userLocation;

	public boolean isUserDetailsModified() {
		return isUserDetailsModified;
	}

	public void setUserDetailsModified(boolean isUserDetailsModified) {
		this.isUserDetailsModified = isUserDetailsModified;
	}

	private boolean isUserDetailsModified;

}
