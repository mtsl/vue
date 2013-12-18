package com.lateralthoughts.vue.domain;

public class ImageComment {
    Long id;
    Long ownerImageId;
    Long ownerUserId;
    String comment;
    String commenterFirstName;
    String commenterLastName;
    Long lastModifiedTimestamp;
    Long createdTimestamp;
    String imageCommentOwnerImageURL;
    
    public ImageComment() {
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getOwnerUserId() {
        return ownerUserId;
    }
    
    public void setOwnerUserId(Long ownerUserId) {
        this.ownerUserId = ownerUserId;
    }
    
    public String getComment() {
        return comment;
    }
    
    public void setComment(String comment) {
        this.comment = comment;
    }
    
    public Long getLastModifiedTimestamp() {
        return lastModifiedTimestamp;
    }
    
    public void setLastModifiedTimestamp(Long lastModifiedTimestamp) {
        this.lastModifiedTimestamp = lastModifiedTimestamp;
    }
    
    public Long getCreatedTimestamp() {
        return createdTimestamp;
    }
    
    public void setCreatedTimestamp(Long createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }
    
    public Long getOwnerImageId() {
        return ownerImageId;
    }
    
    public void setOwnerImageId(Long ownerImageId) {
        this.ownerImageId = ownerImageId;
    }
    
    public String getCommenterFirstName() {
        return commenterFirstName;
    }
    
    public void setCommenterFirstName(String commenterFirstName) {
        this.commenterFirstName = commenterFirstName;
    }
    
    public String getCommenterLastName() {
        return commenterLastName;
    }
    
    public void setCommenterLastName(String commenterLastName) {
        this.commenterLastName = commenterLastName;
    }
    
    public String getImageCommentOwnerImageURL() {
        return imageCommentOwnerImageURL;
    }
    
    public void setImageCommentOwnerImageURL(String imageCommentOwnerImageURL) {
        this.imageCommentOwnerImageURL = imageCommentOwnerImageURL;
    }
}
