package com.lateralthoughts.vue.domain;


public class ImageRating {
    Long id;
    Boolean liked;
    Long aisleId;
    Long imageId;
    Long lastModifiedTimestamp;
    
    public ImageRating() {
        
    }
    
    public ImageRating(Long id, Boolean liked, Long aisleId) {
        super();
        this.id = id;
        this.liked = liked;
        this.aisleId = aisleId;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Boolean getLiked() {
        return liked;
    }
    
    public void setLiked(Boolean liked) {
        this.liked = liked;
    }
    
    public Long getAisleId() {
        return aisleId;
    }
    
    public void setAisleId(Long aisleId) {
        this.aisleId = aisleId;
    }
    
    public Long getImageId() {
        return imageId;
    }
    
    public void setImageId(Long imageId) {
        this.imageId = imageId;
    }
    
    public Long getLastModifiedTimestamp() {
        return lastModifiedTimestamp;
    }
    
    public void setLastModifiedTimestamp(Long lastModifiedTimestamp) {
        this.lastModifiedTimestamp = lastModifiedTimestamp;
    }
    
}
