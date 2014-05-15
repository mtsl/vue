package gcm.com.vue.android.gcmclient;

import gcm.com.vue.gcm.notifications.GCMClientNotification;
import gcm.com.vue.gcm.notifications.GCMClientNotification.NotificationType;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lateralthoughts.vue.AisleImageDetails;
import com.lateralthoughts.vue.AisleWindowContent;
import com.lateralthoughts.vue.R;
import com.lateralthoughts.vue.VueApplication;
import com.lateralthoughts.vue.VueConstants;
import com.lateralthoughts.vue.VueLandingPageActivity;
import com.lateralthoughts.vue.VueTrendingAislesDataModel;
import com.lateralthoughts.vue.connectivity.DataBaseManager;
import com.lateralthoughts.vue.domain.NotificationAisle;
import com.lateralthoughts.vue.parser.Parser;
import com.lateralthoughts.vue.user.VueUser;

public class GCMBroadcastReceiver extends BroadcastReceiver {
    String aisleId = null;
    
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
                            final String mesg = notificationMessage;
                            final String reciviedNotificationImageId = String
                                    .valueOf(lastReceivedNotification
                                            .getCorrespondingOwnerImageId());
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    AisleWindowContent aisleWindowContent = new Parser()
                                            .getAisleForAisleIdFromServerORDatabase(
                                                    String.valueOf(lastReceivedNotification
                                                            .getCorrespondingOwnerAisleId()),
                                                    true);
                                    if (aisleWindowContent != null) {
                                        List<AisleWindowContent> aisleList = new ArrayList<AisleWindowContent>();
                                        aisleList.add(aisleWindowContent);
                                        DataBaseManager
                                                .getInstance(
                                                        VueApplication
                                                                .getInstance())
                                                .addTrentingAislesFromServerToDB(
                                                        VueApplication
                                                                .getInstance(),
                                                        aisleList,
                                                        VueTrendingAislesDataModel
                                                                .getInstance(
                                                                        VueApplication
                                                                                .getInstance())
                                                                .getNetworkHandler().offset,
                                                        DataBaseManager.AISLE_CREATED);
                                        VueUser vueUser = null;
                                        try {
                                            vueUser = VueTrendingAislesDataModel
                                                    .getInstance(
                                                            VueApplication
                                                                    .getInstance())
                                                    .getNetworkHandler()
                                                    .getUserFromServerByUserId(
                                                            lastReceivedNotification
                                                                    .getUserIdOfObjectModifier());
                                        } catch (Exception e1) {
                                            e1.printStackTrace();
                                        }
                                        String userProfileImageUrl = null;
                                        if (vueUser != null) {
                                            userProfileImageUrl = vueUser
                                                    .getUserImageURL();
                                        }
                                        // Title (lookingfor and occasion)
                                        String occasion = null;
                                        String lookingFor = null;
                                        if (aisleWindowContent
                                                .getAisleContext().mOccasion != null
                                                && aisleWindowContent
                                                        .getAisleContext().mOccasion
                                                        .length() > 1) {
                                            occasion = aisleWindowContent
                                                    .getAisleContext().mOccasion;
                                        }
                                        if (aisleWindowContent
                                                .getAisleContext().mLookingForItem != null
                                                && aisleWindowContent
                                                        .getAisleContext().mLookingForItem
                                                        .length() > 1) {
                                            lookingFor = aisleWindowContent
                                                    .getAisleContext().mLookingForItem;
                                        }
                                        if (occasion != null
                                                && occasion.length() > 1) {
                                            occasion = occasion.toLowerCase();
                                            occasion = Character.toString(
                                                    occasion.charAt(0))
                                                    .toUpperCase()
                                                    + occasion.substring(1);
                                        }
                                        if (lookingFor != null
                                                && lookingFor.length() > 1) {
                                            lookingFor = lookingFor
                                                    .toLowerCase();
                                            lookingFor = Character.toString(
                                                    lookingFor.charAt(0))
                                                    .toUpperCase()
                                                    + lookingFor.substring(1);
                                        }
                                        String title = "";
                                        if (occasion != null) {
                                            title = occasion + " : ";
                                        }
                                        if (lookingFor != null) {
                                            title = title + lookingFor;
                                        }
                                        // Like, Bookmark and comments Counts
                                        // for an Aisle...
                                        int likeCount = 0, bookmarkCount = aisleWindowContent
                                                .getAisleContext().mBookmarkCount, commentsCount = 0;
                                        if (aisleWindowContent.getImageList() != null) {
                                            for (AisleImageDetails aisleImageDetails : aisleWindowContent
                                                    .getImageList()) {
                                                likeCount += aisleImageDetails.mLikesCount;
                                                try {
                                                    commentsCount += aisleImageDetails.mCommentsList
                                                            .size();
                                                } catch (Exception e) {
                                                }
                                            }
                                        }
                                        NotificationAisle notificationAisle = new NotificationAisle(
                                                0, aisleWindowContent
                                                        .getAisleId(),
                                                reciviedNotificationImageId,
                                                userProfileImageUrl, title,
                                                likeCount, bookmarkCount,
                                                commentsCount, null, false,
                                                mesg);
                                        DataBaseManager.getInstance(
                                                VueApplication.getInstance())
                                                .insertNotificationAisleId(
                                                        notificationAisle);
                                    }
                                }
                            }).start();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            final String broadcastMessage = intent.getExtras().getString(
                    "mp_message");
            String aisleId = null;
            try {
                JSONObject jsonObject = new JSONObject(intent.getExtras()
                        .getString("apps"));
                aisleId = jsonObject.getString("aisleId");
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (broadcastMessage != null && broadcastMessage.length() > 0) {
                handleNotificationIntent(broadcastMessage, aisleId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
    private void handleNotificationIntent(String message, final String aisleId) {
        if (message == null)
            return;
        NotificationManager notificationManager = (NotificationManager) VueApplication
                .getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
        Intent notificationIntent = new Intent(VueApplication.getInstance(),
                VueLandingPageActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(VueConstants.NOTIFICATION_IMAGE_ID, "0");
        bundle.putString(VueConstants.NOTIFICATION_AISLE_ID, aisleId);
        notificationIntent.putExtras(bundle);
        PendingIntent contentIntent = PendingIntent.getActivity(
                VueApplication.getInstance(), (int) System.currentTimeMillis(),
                notificationIntent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                VueApplication.getInstance())
                .setContentTitle(
                        VueApplication.getInstance().getResources()
                                .getString(R.string.app_name))
                .setSmallIcon(R.drawable.vue_notification_icon)
                .setContentText(message);
        builder.setContentIntent(contentIntent);
        builder.setAutoCancel(true);
        notificationManager.notify(VueConstants.GCM_NOTIFICATION_ID,
                builder.build());
        if (aisleId != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    AisleWindowContent aisleWindowContent = new Parser()
                            .getAisleForAisleIdFromServerORDatabase(aisleId,
                                    true);
                    if (aisleWindowContent != null) {
                        List<AisleWindowContent> aisleList = new ArrayList<AisleWindowContent>();
                        aisleList.add(aisleWindowContent);
                        DataBaseManager.getInstance(
                                VueApplication.getInstance())
                                .addTrentingAislesFromServerToDB(
                                        VueApplication.getInstance(),
                                        aisleList,
                                        VueTrendingAislesDataModel.getInstance(
                                                VueApplication.getInstance())
                                                .getNetworkHandler().offset,
                                        DataBaseManager.AISLE_CREATED);
                    }
                }
            }).start();
        }
    }
    
}
