package com.lateralthoughts.vue;

public class ImageRating {
    
    public static final int NEW_TIME_STAMP = 2;
    public static final int OLD_TIME_STAMP = 1;
    public static final int SAME_TIME_STAMP = 0;
    Long mId;
    Boolean mLiked;
    Long mAisleId;
    Long mImageId;
    Long lastModifiedTimestamp;
    
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
        return lastModifiedTimestamp;
    }
    
    public void setLastModifiedTimestamp(Long lastModifiedTimestamp) {
        this.lastModifiedTimestamp = lastModifiedTimestamp;
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
        if (this.lastModifiedTimestamp > timeStamp) {
            return NEW_TIME_STAMP;
        } else if (this.lastModifiedTimestamp < timeStamp) {
            return OLD_TIME_STAMP;
        } else {
            return SAME_TIME_STAMP;
        }
    }
}
