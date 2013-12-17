package com.lateralthoughts.vue.utils;

import android.graphics.drawable.Drawable;

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
    
    public Drawable getAppIcon() {
        return mAppIcon;
    }
    
    public void setAppIcon(Drawable appIcon) {
        this.mAppIcon = appIcon;
    }
    
    private Drawable mAppIcon;
    
    public ShoppingApplicationDetails(String appName, String activityName,
            String packageName, Drawable appIcon) {
        this.mAppName = appName;
        this.mActivityName = activityName;
        this.mPackageName = packageName;
        this.mAppIcon = appIcon;
    }
}
