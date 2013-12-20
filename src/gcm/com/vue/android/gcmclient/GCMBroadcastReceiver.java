package gcm.com.vue.android.gcmclient;

import gcm.com.vue.gcm.notifications.GCMClientNotification;
import gcm.com.vue.gcm.notifications.GCMClientNotification.NotificationType;
import gcm.com.vue.gcm.notifications.RestOperationTypeEnum;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lateralthoughts.vue.R;
import com.lateralthoughts.vue.VueApplication;
import com.lateralthoughts.vue.VueConstants;
import com.lateralthoughts.vue.VueLandingPageActivity;
import com.lateralthoughts.vue.VueUser;
import com.lateralthoughts.vue.parser.Parser;
import com.lateralthoughts.vue.utils.UrlConstants;
import com.lateralthoughts.vue.utils.UserGetRequest;

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
                final GCMClientNotification lastReceivedNotification = (new ObjectMapper())
                        .readValue(broadcastMessage,
                                GCMClientNotification.class);
                if (lastReceivedNotification != null) {
                    if (!(lastReceivedNotification
                            .getUserIdOfIntendedNotificationReceipient()
                            .equals(lastReceivedNotification
                                    .getUserIdOfObjectModifier()))) {
                        if (lastReceivedNotification.getOperation() == RestOperationTypeEnum.CREATED) {
                            Response.ErrorListener errorListener = new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                }
                            };
                            @SuppressWarnings("rawtypes")
                            final Response.Listener responseListener = new Response.Listener<String>() {
                                @Override
                                public void onResponse(String jsonArray) {
                                    if (jsonArray != null) {
                                        VueUser vueUser = new Parser()
                                                .parseUserData(jsonArray);
                                        if (vueUser != null) {
                                            String userName = vueUser
                                                    .getFirstName()
                                                    + " "
                                                    + vueUser.getLastName();
                                            String notificationMessage = null;
                                            // Commented On Image...
                                            if (lastReceivedNotification
                                                    .getNotification() == NotificationType.IMAGE_COMMENT_NOTIFICATION_TYPE) {
                                                notificationMessage = userName
                                                        + " commented on your image";
                                            }
                                            // Likes the image...
                                            else if (lastReceivedNotification
                                                    .getNotification() == NotificationType.IMAGE_RATING_NOTIFICATION_TYPE) {
                                                notificationMessage = userName
                                                        + " likes your image";
                                            }
                                            // Added new image for an aisle...
                                            else if (lastReceivedNotification
                                                    .getNotification() == NotificationType.IMAGE_NOTIFICATION_TYPE) {
                                                notificationMessage = userName
                                                        + " added image to your aisle";
                                            }
                                            NotificationManager notificationManager = (NotificationManager) VueApplication
                                                    .getInstance()
                                                    .getSystemService(
                                                            Context.NOTIFICATION_SERVICE);
                                            Intent notificationIntent = new Intent(
                                                    VueApplication
                                                            .getInstance(),
                                                    VueLandingPageActivity.class);
                                            notificationIntent.putExtra(
                                                    "notfication", "MyAisles");
                                            PendingIntent contentIntent = PendingIntent
                                                    .getActivity(VueApplication
                                                            .getInstance(), 0,
                                                            notificationIntent,
                                                            0);
                                            NotificationCompat.Builder builder = new NotificationCompat.Builder(
                                                    VueApplication
                                                            .getInstance())
                                                    .setContentTitle(
                                                            VueApplication
                                                                    .getInstance()
                                                                    .getResources()
                                                                    .getString(
                                                                            R.string.app_name))
                                                    .setSmallIcon(
                                                            R.drawable.vue_notification_icon)
                                                    .setStyle(
                                                            new NotificationCompat.BigTextStyle())
                                                    .setContentText(
                                                            notificationMessage);
                                            builder.setContentIntent(contentIntent);
                                            notificationManager
                                                    .notify(VueConstants.GCM_NOTIFICATION_ID,
                                                            builder.build());
                                        }
                                    }
                                }
                            };
                            @SuppressWarnings("unchecked")
                            UserGetRequest userGetRequest = new UserGetRequest(
                                    UrlConstants.GET_USER_RESTURL
                                            + lastReceivedNotification
                                                    .getUserIdOfObjectModifier(),
                                    responseListener, errorListener);
                            VueApplication.getInstance().getRequestQueue()
                                    .add(userGetRequest);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
}
