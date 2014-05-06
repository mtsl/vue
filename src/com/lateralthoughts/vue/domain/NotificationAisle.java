package com.lateralthoughts.vue.domain;

import java.util.ArrayList;

public class NotificationAisle {
    private String aisleId;
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
    
    public boolean isDontShowAisleOnList() {
        return dontShowAisleOnList;
    }
    
    public void setDontShowAisleOnList(boolean dontShowAisleOnList) {
        this.dontShowAisleOnList = dontShowAisleOnList;
    }
    
    private boolean dontShowAisleOnList; // true for dontshow, false for show
    
    public NotificationAisle(String aisleId, boolean readStatus,
            boolean dontShowAisleOnList) {
        this.aisleId = aisleId;
        this.readStatus = readStatus;
        this.dontShowAisleOnList = dontShowAisleOnList;
    }
}
