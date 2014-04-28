package com.lateralthoughts.vue.domain;

public class NotificationAisle {
    private String aisleId;
    
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
    
    public NotificationAisle(String aisleId, boolean readStatus) {
        this.aisleId = aisleId;
        this.readStatus = readStatus;
    }
}
