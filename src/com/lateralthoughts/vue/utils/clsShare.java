package com.lateralthoughts.vue.utils;

import android.os.Parcel;
import android.os.Parcelable;

public class clsShare implements Parcelable {
    
    @SuppressWarnings("rawtypes")
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        @Override
        public clsShare createFromParcel(Parcel in) {
            return new clsShare(in);
        }
        
        @Override
        public clsShare[] newArray(int size) {
            return new clsShare[size];
        }
    };
    
    public clsShare(Parcel in) {
        readFromParcel(in);
    }
    
    private void readFromParcel(Parcel in) {
        imageUrl = in.readString();
        filepath = in.readString();
        lookingFor = in.readString();
        aisleOwnerName = in.readString();
        isUserAisle = in.readString();
        aisleId = in.readString();
        imageId = in.readString();
    }
    
    private String imageUrl;
    
    public clsShare(String imageUrl, String filepath, String lookingFor,
            String aisleOwnerName, String isUserAisle, String aisleId,
            String imageId) {
        this.imageUrl = imageUrl;
        this.filepath = filepath;
        this.lookingFor = lookingFor;
        this.aisleOwnerName = aisleOwnerName;
        this.isUserAisle = isUserAisle;
        this.aisleId = aisleId;
        this.imageId = imageId;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public String getLookingFor() {
        return lookingFor;
    }
    
    public void setLookingFor(String lookingFor) {
        this.lookingFor = lookingFor;
    }
    
    public String getAisleOwnerName() {
        return aisleOwnerName;
    }
    
    public void setAisleOwnerName(String aisleOwnerName) {
        this.aisleOwnerName = aisleOwnerName;
    }
    
    public String isUserAisle() {
        return isUserAisle;
    }
    
    public void setUserAisle(String isUserAisle) {
        this.isUserAisle = isUserAisle;
    }
    
    private String filepath;
    private String aisleId;
    
    public String getImageId() {
        return imageId;
    }
    
    public void setImageId(String imageId) {
        this.imageId = imageId;
    }
    
    private String imageId;
    
    public String getAisleId() {
        return aisleId;
    }
    
    public void setAisleId(String aisleId) {
        this.aisleId = aisleId;
    }
    
    private String lookingFor;
    private String aisleOwnerName;
    private String isUserAisle; // 1 userAisle and 0 otherAisle
    
    public String getFilepath() {
        return filepath;
    }
    
    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(imageUrl);
        dest.writeString(filepath);
        dest.writeString(lookingFor);
        dest.writeString(aisleOwnerName);
        dest.writeString(isUserAisle);
        dest.writeString(aisleId);
        dest.writeString(imageId);
    }
}
