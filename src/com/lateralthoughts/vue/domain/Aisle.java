package com.lateralthoughts.vue.domain;

public class Aisle {
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    Long id;
    Long mOwnerUserId;
    String mCategory;
    String mLookingFor;
    String mName;
    String mOccassion;
    String mDescription;
    Long manchorImageId;
    
    public String getDescription() {
        return mDescription;
    }
    
    public void setDescription(String description) {
        this.mDescription = description;
    }
    
    public Aisle() {
    }
    
    public Aisle(Long id, String category, String lookingFor, String name,
            String occassion, Long ownerUserId, String descreption,
            Long anchorImageId, VueImage aisleImage) {
        super();
        this.id = id;
        this.mCategory = category;
        this.mLookingFor = lookingFor;
        this.mName = name;
        this.mOccassion = occassion;
        this.mOwnerUserId = ownerUserId;
        this.mDescription = descreption;
        this.manchorImageId = anchorImageId;
    }
    
    public String getCategory() {
        return mCategory;
    }
    
    public void setCategory(String category) {
        this.mCategory = category;
    }
    
    public String getLookingFor() {
        return mLookingFor;
    }
    
    public void setLookingFor(String lookingFor) {
        this.mLookingFor = lookingFor;
    }
    
    public String getName() {
        return mName;
    }
    
    public void setName(String name) {
        this.mName = name;
    }
    
    public String getOccassion() {
        return mOccassion;
    }
    
    public void setOccassion(String occassion) {
        this.mOccassion = occassion;
    }
    
    public Long getOwnerUserId() {
        return mOwnerUserId;
    }
    
    public void setOwnerUserId(Long ownerUserId) {
        this.mOwnerUserId = ownerUserId;
    }
    
    public Long getAnchorImageId() {
        return manchorImageId;
    }
    
    public void setAnchorImageId(Long anchorImageId) {
        this.manchorImageId = anchorImageId;
    }
}
