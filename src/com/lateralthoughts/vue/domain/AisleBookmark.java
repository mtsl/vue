package com.lateralthoughts.vue.domain;

public class AisleBookmark {
    public static final int NEW_TIME_STAMP = 2;
    public static final int OLD_TIME_STAMP = 1;
    public static final int SAME_TIME_STAMP = 0;
    Long id;
    Boolean bookmarked;
    Long aisleId;
    Long lastModifiedTimestamp;
    
    public AisleBookmark() {
        
    }
    
    public AisleBookmark(Long id, Boolean bookmarked, Long aisleId) {
        super();
        this.id = id;
        this.bookmarked = bookmarked;
        this.aisleId = aisleId;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Boolean getBookmarked() {
        return bookmarked;
    }
    
    public void setBookmarked(Boolean bookmarked) {
        this.bookmarked = bookmarked;
    }
    
    public Long getAisleId() {
        return aisleId;
    }
    
    public void setAisleId(Long aisleId) {
        this.aisleId = aisleId;
    }
    
    public Long getLastModifiedTimestamp() {
        return lastModifiedTimestamp;
    }
    
    public void setLastModifiedTimestamp(Long lastModifiedTimestamp) {
        this.lastModifiedTimestamp = lastModifiedTimestamp;
    }
    
    public boolean compareTo(AisleBookmark other) {
        boolean imgIdMatched = false;
        if (this.aisleId.longValue() == other.aisleId.longValue()) {
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
