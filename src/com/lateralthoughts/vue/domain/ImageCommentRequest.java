package com.lateralthoughts.vue.domain;

public class ImageCommentRequest {
    Long id;
    String imageCommentOwnerFirstName;
    String imageCommentOwnerLastName;
    Long createdTimestamp;
    String imageCommentOwnerImageURL;
    
    Long ownerImageId;
    
    public Long getOwnerImageId() {
        return ownerImageId;
    }
    
    public void setOwnerImageId(Long ownerImageId) {
        this.ownerImageId = ownerImageId;
    }
    
    Long ownerUserId;
    
    public Long getOwnerUserId() {
        return ownerUserId;
    }
    
    public void setOwnerUserId(Long ownerUserId) {
        this.ownerUserId = ownerUserId;
    }
    
    String comment;
    
    public String getComment() {
        return comment;
    }
    
    public void setComment(String comment) {
        this.comment = comment;
    }
    
    Long lastModifiedTimestamp;
    
    public Long getLastModifiedTimestamp() {
        return lastModifiedTimestamp;
    }
    
    public void setLastModifiedTimestamp(Long lastModifiedTimestamp) {
        this.lastModifiedTimestamp = lastModifiedTimestamp;
    }
    
}
