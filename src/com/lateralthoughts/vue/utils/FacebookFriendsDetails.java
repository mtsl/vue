package com.lateralthoughts.vue.utils;
/**
 * This is class is used to store the facebook friends information.
 * @author krishna
 *
 */
public class FacebookFriendsDetails {

	private String name = null;
	private String profile_image_url = null;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getProfile_image_url() {
		return profile_image_url;
	}
	public void setProfile_image_url(String profile_image_url) {
		this.profile_image_url = profile_image_url;
	}
	public FacebookFriendsDetails(String name, String profile_image_url) {
		this.name = name;
		this.profile_image_url = profile_image_url;
	}
	
}
