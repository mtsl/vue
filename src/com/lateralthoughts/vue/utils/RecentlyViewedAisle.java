package com.lateralthoughts.vue.utils;

public class RecentlyViewedAisle {
    public String getmAisleId() {
        return mAisleId;
    }
    
    public void setmAisleId(String mAisleId) {
        this.mAisleId = mAisleId;
    }
    
    public String getmTime() {
        return mTime;
    }
    
    public void setmTime(String mTime) {
        this.mTime = mTime;
    }
    
    public RecentlyViewedAisle(String mAisleId, String mTime) {
        this.mAisleId = mAisleId;
        this.mTime = mTime;
    }
    
    private String mAisleId = null;
    private String mTime = null;
}
