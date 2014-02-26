package com.lateralthoughts.vue.utils;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.widget.RemoteViews;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lateralthoughts.vue.AisleImageDetails;
import com.lateralthoughts.vue.AisleWindowContent;
import com.lateralthoughts.vue.R;
import com.lateralthoughts.vue.VueApplication;
import com.lateralthoughts.vue.VueConstants;
import com.lateralthoughts.vue.VueLandingPageActivity;
import com.lateralthoughts.vue.VueTrendingAislesDataModel;
import com.lateralthoughts.vue.connectivity.DataBaseManager;
import com.lateralthoughts.vue.domain.Image;

public class DeleteImageFromAisle implements Runnable,
        CountingStringEntity.UploadListener {
    Image mImage;
    private NotificationManager mNotificationManager;
    private Notification mNotification;
    private int mLastPercent = 0;
    private String mAisleId = null;
    
    @SuppressWarnings("static-access")
    public DeleteImageFromAisle(Image image, String aisleId) {
        mNotificationManager = (NotificationManager) VueApplication
                .getInstance().getSystemService(
                        VueApplication.getInstance().NOTIFICATION_SERVICE);
        this.mImage = image;
        mAisleId = aisleId;
        
    }
    
    @Override
    public void onChange(int percent) {
        if (percent > mLastPercent) {
            mNotification.contentView.setProgressBar(R.id.progressBar1, 100,
                    percent, false);
            mNotificationManager.notify(
                    VueConstants.IMAGE_DELETE_NOTIFICATION_ID, mNotification);
            mLastPercent = percent;
        }
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public void run() {
        try {
            VueLandingPageActivity.landingPageActivity
                    .runOnUiThread(new Runnable() {
                        
                        @Override
                        public void run() {
                            Utils.sIsAisleChanged = true;
                            Utils.mChangeAilseId = mAisleId;
                            deleteImageFromAisleList(
                                    String.valueOf(mImage.getId()),
                                    String.valueOf(mImage.getOwnerAisleId()));
                            VueTrendingAislesDataModel.getInstance(
                                    VueApplication.getInstance())
                                    .dataObserver();
                            deleteImageFromDb(String.valueOf(mImage.getId()));
                            
                        }
                    });
            Intent notificationIntent = new Intent();
            PendingIntent contentIntent = PendingIntent.getActivity(
                    VueApplication.getInstance(), 0, notificationIntent, 0);
            mNotification = new Notification(R.drawable.vue_notification_icon,
                    VueApplication.getInstance().getResources()
                            .getString(R.string.deleting_mesg),
                    System.currentTimeMillis());
            mNotification.flags = mNotification.flags
                    | Notification.FLAG_ONGOING_EVENT;
            mNotification.contentView = new RemoteViews(VueApplication
                    .getInstance().getPackageName(),
                    R.layout.upload_progress_bar);
            mNotification.contentIntent = contentIntent;
            mNotification.contentView.setProgressBar(R.id.progressBar1, 100, 0,
                    false);
            mNotificationManager.notify(
                    VueConstants.IMAGE_DELETE_NOTIFICATION_ID, mNotification);
            URL url = new URL(UrlConstants.DELETE_IMAGE_RESTURL + "/"
                    + mImage.getId());
            HttpDelete httpDelete = new HttpDelete(url.toString());
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpResponse response = httpClient.execute(httpDelete);
            boolean result = false;
            if (response.getEntity() != null) {
                String responseMessage = EntityUtils.toString(response
                        .getEntity());
                result = (new ObjectMapper()).readValue(responseMessage,
                        Boolean.class);
            }
            if (result) {
                mNotification.setLatestEventInfo(
                        VueApplication.getInstance(),
                        VueApplication.getInstance().getResources()
                                .getString(R.string.delete_successful_mesg),
                        "", contentIntent);
                mNotification.flags = Notification.FLAG_AUTO_CANCEL;
                mNotificationManager.notify(
                        VueConstants.IMAGE_DELETE_NOTIFICATION_ID,
                        mNotification);
            } else {
                mNotification.setLatestEventInfo(
                        VueApplication.getInstance(),
                        VueApplication.getInstance().getResources()
                                .getString(R.string.delete_failed_mesg), "",
                        contentIntent);
                mNotification.flags = Notification.FLAG_AUTO_CANCEL;
                mNotificationManager.notify(
                        VueConstants.IMAGE_DELETE_NOTIFICATION_ID,
                        mNotification);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
    /**
     * 
     * @param imageId
     * @param aisleId
     *            delete the image from the aisle if it is showing in the UI.
     */
    private void deleteImageFromAisleList(String imageId, String aisleId) {
        AisleWindowContent aisleItem = VueTrendingAislesDataModel.getInstance(
                VueApplication.getInstance()).getAisleAt(aisleId);
        aisleItem = VueTrendingAislesDataModel.getInstance(
                VueApplication.getInstance()).getAisleFromList(aisleItem);
        if (aisleItem != null) {
            AisleImageDetails imageDetails = null;
            for (int i = 0; i < aisleItem.getImageList().size(); i++) {
                imageDetails = aisleItem.getImageList().get(i);
                if (imageId.equalsIgnoreCase(imageDetails.mId)) {
                    aisleItem.getImageList().remove(i);
                    break;
                }
            }
        }
        if (aisleItem != null && aisleItem.getImageList() != null
                && aisleItem.getImageList().size() == 0) {
            int position = VueTrendingAislesDataModel.getInstance(
                    VueApplication.getInstance()).getAilsePosition(aisleItem);
            VueTrendingAislesDataModel
                    .getInstance(VueApplication.getInstance())
                    .removeAisleFromList(position);
            DataBaseManager
                    .getInstance(VueApplication.getInstance())
                    .deleteOutDatedAisles(VueApplication.getInstance(), aisleId);
        }
    }
    
    private void deleteImageFromDb(String imageId) {
        DataBaseManager.getInstance(VueApplication.getInstance()).deleteImage(
                imageId);
    }
}
