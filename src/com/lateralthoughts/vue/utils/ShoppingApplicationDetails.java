package com.lateralthoughts.vue.utils;

public class ShoppingApplicationDetails {
    private String mAppName;
    
    public String getAppName() {
        return mAppName;
    }
    
    public void setAppName(String appName) {
        this.mAppName = appName;
    }
    
    public String getActivityName() {
        return mActivityName;
    }
    
    public void setActivityName(String activityName) {
        this.mActivityName = activityName;
    }
    
    public String getPackageName() {
        return mPackageName;
    }
    
    public void setPackageName(String packageName) {
        this.mPackageName = packageName;
    }
    
    private String mActivityName;
    private String mPackageName;
    
    public String getAppIconPath() {
        return mAppIconPath;
    }
    
    public void setAppIconPath(String appIconPath) {
        this.mAppIconPath = appIconPath;
    }
    
    private String mAppIconPath;
    
    public ShoppingApplicationDetails(String appName, String activityName,
            String packageName, String appIconPath) {
        this.mAppName = appName;
        this.mActivityName = activityName;
        this.mPackageName = packageName;
        this.mAppIconPath = appIconPath;
    }
}
