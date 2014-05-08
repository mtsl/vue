package com.lateralthoughts.vue.domain;

import java.util.ArrayList;

public class NotificationAisle {
    private String aisleId;
    
    public String getNotificationText() {
        return notificationText;
    }
    
    public void setNotificationText(String notificationText) {
        this.notificationText = notificationText;
    }
    
    private String notificationText;
    
    public String getUserProfileImageUrl() {
        return userProfileImageUrl;
    }
    
    public void setUserProfileImageUrl(String userProfileImageUrl) {
        this.userProfileImageUrl = userProfileImageUrl;
    }
    
    public String getAisleTitle() {
        return aisleTitle;
    }
    
    public void setAisleTitle(String aisleTitle) {
        this.aisleTitle = aisleTitle;
    }
    
    public int getLikeCount() {
        return likeCount;
    }
    
    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }
    
    public int getBookmarkCount() {
        return bookmarkCount;
    }
    
    public void setBookmarkCount(int bookmarkCount) {
        this.bookmarkCount = bookmarkCount;
    }
    
    public int getCommentsCount() {
        return commentsCount;
    }
    
    public void setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
    }
    
    public ArrayList<NotificationAisle> getAggregatedAisles() {
        return aggregatedAisles;
    }
    
    public void setAggregatedAisles(
            ArrayList<NotificationAisle> aggregatedAisles) {
        this.aggregatedAisles = aggregatedAisles;
    }
    
    private String userProfileImageUrl;
    private String aisleTitle; // aisle title is combination of lokkingfor and
                               // occasion
    private int likeCount;
    private int bookmarkCount;
    private int commentsCount;
    private ArrayList<NotificationAisle> aggregatedAisles;
    
    public String getAisleId() {
        return aisleId;
    }
    
    public void setAisleId(String aisleId) {
        this.aisleId = aisleId;
    }
    
    public boolean isReadStatus() {
        return readStatus;
    }
    
    public void setReadStatus(boolean readStatus) {
        this.readStatus = readStatus;
    }
    
    private boolean readStatus; // true for read, false for unread
    
    public NotificationAisle(String aisleId, String userProfileImageUrl,
            String aisleTitle, int likeCount, int bookmarkCount,
            int commentsCount, ArrayList<NotificationAisle> aggregatedAisles,
            boolean readStatus, String notificationText) {
        this.aisleId = aisleId;
        this.userProfileImageUrl = userProfileImageUrl;
        this.aisleTitle = aisleTitle;
        this.likeCount = likeCount;
        this.bookmarkCount = bookmarkCount;
        this.commentsCount = commentsCount;
        this.aggregatedAisles = aggregatedAisles;
        this.readStatus = readStatus;
        this.notificationText = notificationText;
    }
    
}
