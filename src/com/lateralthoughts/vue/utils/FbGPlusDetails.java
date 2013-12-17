package com.lateralthoughts.vue.utils;

import com.google.android.gms.plus.model.people.Person;

/**
 * This is class is used to store the facebook friends information.
 * 
 */
public class FbGPlusDetails {
    
    private Person mGoogleplusFriend;
    
    public Person getGoogleplusFriend() {
        return mGoogleplusFriend;
    }
    
    public void setGoogleplusFriend(Person googleplusFriend) {
        this.mGoogleplusFriend = googleplusFriend;
    }
    
    private String mName = null;
    private String mProfile_image_url = null;
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    private String id = null;
    
    public String getName() {
        return mName;
    }
    
    public void setName(String name) {
        this.mName = name;
    }
    
    public String getProfile_image_url() {
        return mProfile_image_url;
    }
    
    public void setProfile_image_url(String profile_image_url) {
        this.mProfile_image_url = profile_image_url;
    }
    
    public FbGPlusDetails(String id, String name, String profile_image_url,
            Person googleplusFriend) {
        this.mName = name;
        this.mProfile_image_url = profile_image_url;
        this.id = id;
        this.mGoogleplusFriend = googleplusFriend;
    }
    
}
