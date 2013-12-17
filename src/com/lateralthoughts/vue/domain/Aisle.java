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
    VueImage mAisleImage;
    String mDescription;
    
    public String getDescription() {
        return mDescription;
    }
    
    public void setDescription(String description) {
        this.mDescription = description;
    }
    
    public VueImage getAisleImage() {
        return mAisleImage;
    }
    
    public void setAisleImage(VueImage mAisleImage) {
        this.mAisleImage = mAisleImage;
    }
    
    public Aisle() {
    }
    
    public Aisle(Long id, String category, String lookingFor, String name,
            String occassion, Long ownerUserId, String descreption,
            VueImage aisleImage) {
        super();
        this.id = id;
        this.mCategory = category;
        this.mLookingFor = lookingFor;
        this.mName = name;
        this.mOccassion = occassion;
        this.mOwnerUserId = ownerUserId;
        this.mAisleImage = aisleImage;
        this.mDescription = descreption;
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
}
