package com.lateralthoughts.vue.utils;
/**
 * This is class is used to store the facebook friends information.
 * @author krishna
 *
 */
public class FbGPlusDetails {

	private String name = null;
	private String profile_image_url = null;

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	private String id = null;
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
	public FbGPlusDetails(String id, String name, String profile_image_url) {
		this.name = name;
		this.profile_image_url = profile_image_url;
		this.id = id;
	}

}
