package com.lateralthoughts.vue;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ImageRating {
    
    public static final int NEW_TIME_STAMP = 2;
    public static final int OLD_TIME_STAMP = 1;
    public static final int SAME_TIME_STAMP = 0;
    public Long mId;
    public Boolean mLiked;
    public Long mAisleId;
    public Long mImageId;
    public Long mLastModifiedTimestamp;
    public String mImageRatingOwnerFirstName;
    public String mImageRatingOwnerLastName;
    
    public ImageRating() {
        
    }
    
    public ImageRating(Long id, Boolean liked, Long aisleId) {
        super();
        this.mId = id;
        this.mLiked = liked;
        this.mAisleId = aisleId;
    }
    
    public Long getId() {
        return mId;
    }
    
    public void setId(Long id) {
        this.mId = id;
    }
    
    public Boolean getLiked() {
        return mLiked;
    }
    
    public void setLiked(Boolean liked) {
        this.mLiked = liked;
    }
    
    public Long getAisleId() {
        return mAisleId;
    }
    
    public void setAisleId(Long aisleId) {
        this.mAisleId = aisleId;
    }
    
    public Long getImageId() {
        return mImageId;
    }
    
    public void setImageId(Long imageId) {
        this.mImageId = imageId;
    }
    
    public Long getLastModifiedTimestamp() {
        return mLastModifiedTimestamp;
    }
    
    public void setLastModifiedTimestamp(Long lastModifiedTimestamp) {
        this.mLastModifiedTimestamp = lastModifiedTimestamp;
    }
    
    public boolean compareTo(ImageRating other) {
        boolean imgIdMatched = false;
        if (this.mImageId.longValue() == other.mImageId.longValue()) {
            imgIdMatched = true;
        }
        if (imgIdMatched) {
            return true;
        }
        
        return false;
    }
    
    public int compareTime(long timeStamp) {
        if (this.mLastModifiedTimestamp > timeStamp) {
            return NEW_TIME_STAMP;
        } else if (this.mLastModifiedTimestamp < timeStamp) {
            return OLD_TIME_STAMP;
        } else {
            return SAME_TIME_STAMP;
        }
    }
    
    /**
     * ImageRatingOwnerFirstName count is a read-only field for the client. So
     * getter should be disabled. This will disable serialization of the field.
     */
    @JsonIgnore
    public String getRatingOwnerFirstName() {
        return mImageRatingOwnerFirstName;
    }
    
    @JsonProperty
    public void setRatingOwnerFirstName(String imageRatingOwnerFirstName) {
        this.mImageRatingOwnerFirstName = imageRatingOwnerFirstName;
    }
    
    /**
     * ImageRatingOwnerLastName count is a read-only field for the client. So
     * getter should be disabled. This will disable serialization of the field.
     */
    @JsonIgnore
    public String getRatingOwnerLastName() {
        return mImageRatingOwnerLastName;
    }
    
    @JsonProperty
    public void setRatingOwnerLastName(String imageRatingOwnerLastName) {
        this.mImageRatingOwnerLastName = imageRatingOwnerLastName;
    }
}
