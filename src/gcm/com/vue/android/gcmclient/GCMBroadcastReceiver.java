package gcm.com.vue.android.gcmclient;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;

import gcm.com.vue.gcm.notifications.GCMClientNotification;
import gcm.com.vue.gcm.notifications.GCMClientNotification.NotificationType;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lateralthoughts.vue.R;
import com.lateralthoughts.vue.VueApplication;
import com.lateralthoughts.vue.VueConstants;
import com.lateralthoughts.vue.VueLandingPageActivity;
import com.lateralthoughts.vue.utils.Utils;

public class GCMBroadcastReceiver extends BroadcastReceiver {
    
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
            final String broadcastMessage = intent.getExtras()
                    .getString("data");
            if (broadcastMessage != null
                    && broadcastMessage.trim().length() > 0) {
                writeToSdcard(broadcastMessage);
                final GCMClientNotification lastReceivedNotification = (new ObjectMapper())
                        .readValue(broadcastMessage,
                                GCMClientNotification.class);
                if (lastReceivedNotification != null) {
                    if (!(lastReceivedNotification
                            .getUserIdOfIntendedNotificationReceipient()
                            .equals(lastReceivedNotification
                                    .getUserIdOfObjectModifier()))) {
                        if (lastReceivedNotification
                                .getCorrespondingOwnerAisleId() != null
                                && lastReceivedNotification
                                        .getCorrespondingOwnerImageId() != null
                                && (lastReceivedNotification
                                        .getFirstNameOfObjectModifier() != null || lastReceivedNotification
                                        .getLastNameOfObjectModifier() != null)) {
                            String userName = "";
                            if (lastReceivedNotification
                                    .getFirstNameOfObjectModifier() != null
                                    && !(lastReceivedNotification
                                            .getFirstNameOfObjectModifier()
                                            .equals("null"))) {
                                userName = lastReceivedNotification
                                        .getFirstNameOfObjectModifier();
                            }
                            String notificationMessage = null;
                            // Commented On Image...
                            if (lastReceivedNotification.getNotification() == NotificationType.IMAGE_COMMENT_NOTIFICATION_TYPE) {
                                notificationMessage = userName
                                        + " commented on your image";
                            }
                            // Likes the image...
                            else if (lastReceivedNotification.getNotification() == NotificationType.IMAGE_RATING_NOTIFICATION_TYPE) {
                                notificationMessage = userName
                                        + " liked your image";
                            }
                            // Added new image for an aisle...
                            else if (lastReceivedNotification.getNotification() == NotificationType.IMAGE_NOTIFICATION_TYPE) {
                                notificationMessage = userName
                                        + " added an image to your aisle";
                            }
                            NotificationManager notificationManager = (NotificationManager) VueApplication
                                    .getInstance().getSystemService(
                                            Context.NOTIFICATION_SERVICE);
                            Intent notificationIntent = new Intent(
                                    VueApplication.getInstance(),
                                    VueLandingPageActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString(
                                    VueConstants.NOTIFICATION_IMAGE_ID,
                                    String.valueOf(lastReceivedNotification
                                            .getCorrespondingOwnerImageId()));
                            bundle.putString(
                                    VueConstants.NOTIFICATION_AISLE_ID,
                                    String.valueOf(lastReceivedNotification
                                            .getCorrespondingOwnerAisleId()));
                            notificationIntent.putExtras(bundle);
                            PendingIntent contentIntent = PendingIntent
                                    .getActivity(VueApplication.getInstance(),
                                            (int) System.currentTimeMillis(),
                                            notificationIntent, 0);
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(
                                    VueApplication.getInstance())
                                    .setContentTitle(
                                            VueApplication
                                                    .getInstance()
                                                    .getResources()
                                                    .getString(
                                                            R.string.app_name))
                                    .setSmallIcon(
                                            R.drawable.vue_notification_icon)
                                    .setContentText(notificationMessage);
                            builder.setContentIntent(contentIntent);
                            builder.setAutoCancel(true);
                            notificationManager.notify(
                                    VueConstants.GCM_NOTIFICATION_ID,
                                    builder.build());
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
   private void writeToSdcard(String message) {
        
        String path = Environment.getExternalStorageDirectory().toString();
        File dir = new File(path + "/VueNotificationsOn21jan/");
        if (!dir.isDirectory()) {
            dir.mkdir();
        }
        File file = new File(dir, "/"
                + Calendar.getInstance().get(Calendar.DATE)
                + "-"
                + Utils.getWeekDay(Calendar.getInstance().get(
                        Calendar.DAY_OF_WEEK)) + ".txt");
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(
                    new FileWriter(file, true)));
            out.write("\n" + message + "\n");
            out.flush();
            out.close();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
