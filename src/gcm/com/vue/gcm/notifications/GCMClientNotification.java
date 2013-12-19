package gcm.com.vue.gcm.notifications;

import java.io.Serializable;

public class GCMClientNotification implements Serializable {
    
    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;
    
    public enum NotificationType {
        AISLE_NOTIFICATION_TYPE, IMAGE_NOTIFICATION_TYPE, IMAGE_COMMENT_NOTIFICATION_TYPE, IMAGE_RATING_NOTIFICATION_TYPE
    }
    
    private NotificationType notification;
    
    private RestOperationTypeEnum operation;
    
    private Long userIdOfIntendedNotificationReceipient;
    
    private Long idOfModifiedObject;
    
    private Long userIdOfObjectModifier;
    
    private long modifiedTime;
    
    public NotificationType getNotification() {
        return notification;
    }
    
    public void setNotification(NotificationType notification) {
        this.notification = notification;
    }
    
    public RestOperationTypeEnum getOperation() {
        return operation;
    }
    
    public void setOperation(RestOperationTypeEnum operation) {
        this.operation = operation;
    }
    
    public long getModifiedTime() {
        return modifiedTime;
    }
    
    public void setModifiedTime(long modifiedTime) {
        this.modifiedTime = modifiedTime;
    }
    
    public Long getUserIdOfIntendedNotificationReceipient() {
        return userIdOfIntendedNotificationReceipient;
    }
    
    public void setUserIdOfIntendedNotificationReceipient(
            Long userIdOfIntendedNotificationReceipient) {
        this.userIdOfIntendedNotificationReceipient = userIdOfIntendedNotificationReceipient;
    }
    
    public Long getIdOfModifiedObject() {
        return idOfModifiedObject;
    }
    
    public void setIdOfModifiedObject(Long idOfModifiedObject) {
        this.idOfModifiedObject = idOfModifiedObject;
    }
    
    public Long getUserIdOfObjectModifier() {
        return userIdOfObjectModifier;
    }
    
    public void setUserIdOfObjectModifier(Long userIdOfObjectModifier) {
        this.userIdOfObjectModifier = userIdOfObjectModifier;
    }
}
