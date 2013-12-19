package gcm.com.vue.android.gcmclient;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;

import com.lateralthoughts.vue.R;
import com.lateralthoughts.vue.VueApplication;
import com.lateralthoughts.vue.VueConstants;

public class GCMBroadcastReceiver extends BroadcastReceiver {
    
    public void registerCallback(IGCMBroadcastReceiverCallback callback) {
    }
    
    public void registerReceiver(Context context) {
        /**
         * Create our IntentFilter, which will be used in conjunction with a
         * broadcast receiver.
         */
        IntentFilter gcmFilter = new IntentFilter();
        gcmFilter.addAction("GCM_RECEIVED_ACTION");
        context.registerReceiver(gcmReceiver, gcmFilter);
    }
    
    public void unregisterReciever(Context context) {
        context.unregisterReceiver(gcmReceiver);
    }
    
    private BroadcastReceiver gcmReceiver = new BroadcastReceiver() {
        @SuppressWarnings("deprecation")
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String broadcastMessage = intent.getExtras().getString("gcm");
                NotificationManager notificationManager = (NotificationManager) VueApplication
                        .getInstance().getSystemService(
                                Context.NOTIFICATION_SERVICE);
                Notification mNotification = new Notification(
                        R.drawable.vue_notification_icon, "",
                        System.currentTimeMillis());
                Intent MyIntent = new Intent(Intent.ACTION_VIEW);
                PendingIntent StartIntent = PendingIntent.getActivity(
                        VueApplication.getInstance().getApplicationContext(),
                        0, MyIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                mNotification.flags = Notification.FLAG_AUTO_CANCEL;
                mNotification.setLatestEventInfo(VueApplication.getInstance()
                        .getApplicationContext(), "", broadcastMessage,
                        StartIntent);
                notificationManager.notify(VueConstants.GCM_NOTIFICATION_ID,
                        mNotification);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    
    @SuppressWarnings("deprecation")
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            String registrationId = intent.getExtras().getString(
                    "registration_id");
            SharedPreferences sharedPreferencesObj = VueApplication
                    .getInstance().getSharedPreferences(
                            VueConstants.SHAREDPREFERENCE_NAME, 0);
            SharedPreferences.Editor editor = sharedPreferencesObj.edit();
            editor.putString(VueConstants.GCM_REGISTRATION_ID, registrationId);
            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            String broadcastMessage = intent.getExtras().getString("gcm");
            NotificationManager notificationManager = (NotificationManager) VueApplication
                    .getInstance().getSystemService(
                            Context.NOTIFICATION_SERVICE);
            Notification mNotification = new Notification(
                    R.drawable.vue_notification_icon, "",
                    System.currentTimeMillis());
            Intent MyIntent = new Intent(Intent.ACTION_VIEW);
            PendingIntent StartIntent = PendingIntent.getActivity(
                    VueApplication.getInstance().getApplicationContext(), 0,
                    MyIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            mNotification.flags = Notification.FLAG_AUTO_CANCEL;
            mNotification
                    .setLatestEventInfo(VueApplication.getInstance()
                            .getApplicationContext(), "", broadcastMessage,
                            StartIntent);
            notificationManager.notify(VueConstants.GCM_NOTIFICATION_ID,
                    mNotification);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
}
