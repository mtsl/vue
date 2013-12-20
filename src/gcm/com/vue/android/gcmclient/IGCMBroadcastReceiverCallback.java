package gcm.com.vue.android.gcmclient;

import gcm.com.vue.gcm.notifications.GCMClientNotification;

public interface IGCMBroadcastReceiverCallback {
    
    public void onNotificationReceive(GCMClientNotification serverMessage);
}
