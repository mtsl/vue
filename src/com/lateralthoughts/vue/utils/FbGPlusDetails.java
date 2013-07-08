package com.lateralthoughts.vue.utils;

import com.google.android.gms.plus.model.people.Person;

/**
 * This is class is used to store the facebook friends information.
 * @author krishna
 *
 */
public class FbGPlusDetails {

	private Person googleplusFriend;
	
	public Person getGoogleplusFriend() {
		return googleplusFriend;
	}
	public void setGoogleplusFriend(Person googleplusFriend) {
		this.googleplusFriend = googleplusFriend;
	}
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
	public FbGPlusDetails(String id, String name, String profile_image_url, Person googleplusFriend) {
		this.name = name;
		this.profile_image_url = profile_image_url;
		this.id = id;
		this.googleplusFriend = googleplusFriend;
	}

}
