package com.lateralthoughts.vue.utils;

import android.net.Uri;

public class OtherSourceImageDetails {
    public String getThumbUrl() {
        return mTthumbUrl;
    }
    
    public void setThumbUrl(String thumbUrl) {
        this.mTthumbUrl = thumbUrl;
    }
    
    public String getTitle() {
        return mTitle;
    }
    
    public void setTitle(String title) {
        this.mTitle = title;
    }
    
    public String getOriginUrl() {
        return mOriginUrl;
    }
    
    public void setOriginUrl(String originUrl) {
        this.mOriginUrl = originUrl;
    }
    
    public int getWidth() {
        return mWidth;
    }
    
    public void setWidth(int width) {
        this.mWidth = width;
    }
    
    public int getHeight() {
        return mHeight;
    }
    
    public void setHeight(int height) {
        this.mHeight = height;
    }
    
    public OtherSourceImageDetails(String thumbUrl, String title,
            String originUrl, int width, int height, Uri imageUri,
            int widthHeightMultipliedValue) {
        this.mTthumbUrl = thumbUrl;
        this.mTitle = title;
        this.mOriginUrl = originUrl;
        this.mWidth = width;
        this.mHeight = height;
        this.mImageUri = imageUri;
        this.mWidthHeightMultipliedValue = widthHeightMultipliedValue;
    }
    
    public OtherSourceImageDetails() {
        
    }
    
    private String mTthumbUrl;
    private String mTitle;
    private String mOriginUrl;
    private int mWidth;
    private int mHeight;
    private Uri mImageUri;
    private int mWidthHeightMultipliedValue;
    
    public int getWidthHeightMultipliedValue() {
        return mWidthHeightMultipliedValue;
    }
    
    public void setWidthHeightMultipliedValue(int widthHeightMultipliedValue) {
        this.mWidthHeightMultipliedValue = widthHeightMultipliedValue;
    }
    
    public Uri getImageUri() {
        return mImageUri;
    }
    
    public void setImageUri(Uri imageUri) {
        this.mImageUri = imageUri;
    }
    
}