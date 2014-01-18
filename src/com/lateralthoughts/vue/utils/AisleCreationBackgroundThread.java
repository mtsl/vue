package com.lateralthoughts.vue.utils;

import java.net.URL;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lateralthoughts.vue.AisleManager.AisleAddCallback;
import com.lateralthoughts.vue.AisleWindowContent;
import com.lateralthoughts.vue.R;
import com.lateralthoughts.vue.VueApplication;
import com.lateralthoughts.vue.VueConstants;
import com.lateralthoughts.vue.VueLandingPageActivity;
import com.lateralthoughts.vue.VueTrendingAislesDataModel;
import com.lateralthoughts.vue.connectivity.DataBaseManager;
import com.lateralthoughts.vue.domain.Aisle;
import com.lateralthoughts.vue.parser.Parser;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

public class AisleCreationBackgroundThread implements Runnable,
        CountingStringEntity.UploadListener {
    private NotificationManager mNotificationManager;
    private Notification mNotification;
    private int mLastPercent = 0;
    private Aisle mAisle = null;
    private String mResponseMessage = null;
    private AisleAddCallback mAisleAddCallback = null;
    private MixpanelAPI mixpanel;
    
    @SuppressWarnings("static-access")
    public AisleCreationBackgroundThread(Aisle aisle, AisleAddCallback callback) {
        mNotificationManager = (NotificationManager) VueApplication
                .getInstance().getSystemService(
                        VueApplication.getInstance().NOTIFICATION_SERVICE);
        mAisle = aisle;
        mAisleAddCallback = callback;
    }
    
    @Override
    public void onChange(int percent) {
        if (percent > mLastPercent) {
            mNotification.contentView.setProgressBar(R.id.progressBar1, 100,
                    percent, false);
            mNotificationManager.notify(
                    VueConstants.AISLE_INFO_UPLOAD_NOTIFICATION_ID,
                    mNotification);
            mLastPercent = percent;
        }
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public void run() {
        mixpanel = MixpanelAPI.getInstance(VueApplication.getInstance(),
                VueApplication.getInstance().MIXPANEL_TOKEN);
        try {
            Intent notificationIntent = new Intent();
            PendingIntent contentIntent = PendingIntent.getActivity(
                    VueApplication.getInstance(), 0, notificationIntent, 0);
            mNotification = new Notification(R.drawable.vue_notification_icon,
                    VueApplication.getInstance().getResources()
                            .getString(R.string.uploading_mesg),
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
                    VueConstants.AISLE_INFO_UPLOAD_NOTIFICATION_ID,
                    mNotification);
            ObjectMapper mapper = new ObjectMapper();
            URL url = new URL(UrlConstants.CREATE_AISLE_RESTURL);
            HttpPut httpPut = new HttpPut(url.toString());
            CountingStringEntity entity = new CountingStringEntity(
                    mapper.writeValueAsString(mAisle));
            entity.setUploadListener(this);
            entity.setContentType("application/json;charset=UTF-8");
            entity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,
                    "application/json;charset=UTF-8"));
            httpPut.setEntity(entity);
            
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpResponse response = httpClient.execute(httpPut);
            if (response.getEntity() != null
                    && response.getStatusLine().getStatusCode() == 200) {
                mNotification.setLatestEventInfo(
                        VueApplication.getInstance(),
                        VueApplication.getInstance().getResources()
                                .getString(R.string.upload_successful_mesg),
                        "", contentIntent);
                mNotification.flags = Notification.FLAG_AUTO_CANCEL;
                mNotificationManager.notify(
                        VueConstants.AISLE_INFO_UPLOAD_NOTIFICATION_ID,
                        mNotification);
                mResponseMessage = EntityUtils.toString(response.getEntity());
            } else {
                mNotification.setLatestEventInfo(
                        VueApplication.getInstance(),
                        VueApplication.getInstance().getResources()
                                .getString(R.string.upload_failed_mesg), "",
                        contentIntent);
                mNotification.flags = Notification.FLAG_AUTO_CANCEL;
                mNotificationManager.notify(
                        VueConstants.AISLE_INFO_UPLOAD_NOTIFICATION_ID,
                        mNotification);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        VueLandingPageActivity.landingPageActivity
                .runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (null != mResponseMessage) {
                            try {
                                AisleWindowContent aileItem = new Parser()
                                        .parseCreateAisleResponse(mResponseMessage);
                                if (VueLandingPageActivity.mLandingScreenName != null
                                        && VueLandingPageActivity.mLandingScreenName
                                                .equalsIgnoreCase("Trending")
                                        || (VueLandingPageActivity.mLandingScreenName != null && VueLandingPageActivity.mLandingScreenName
                                                .equalsIgnoreCase("My Aisles"))) {
                                    VueTrendingAislesDataModel
                                            .getInstance(
                                                    VueApplication
                                                            .getInstance())
                                            .addItemToListAt(
                                                    aileItem.getAisleContext().mAisleId,
                                                    aileItem, 0);
                                }
                                ArrayList<AisleWindowContent> list = new ArrayList<AisleWindowContent>();
                                list.add(aileItem);
                                DataBaseManager
                                        .getInstance(
                                                VueApplication.getInstance())
                                        .addTrentingAislesFromServerToDB(
                                                VueApplication.getInstance(),
                                                list,
                                                VueTrendingAislesDataModel
                                                        .getInstance(
                                                                VueApplication
                                                                        .getInstance())
                                                        .getNetworkHandler().offset,
                                                DataBaseManager.AISLE_CREATED);
                                Aisle aisle = new Aisle();
                                aisle.setCategory(aileItem.getAisleContext().mCategory);
                                aisle.setLookingFor(aileItem.getAisleContext().mLookingForItem);
                                aisle.setName(aileItem.getAisleContext().mName);
                                aisle.setOccassion(aileItem.getAisleContext().mOccasion);
                                aisle.setOwnerUserId(Long.valueOf(aileItem
                                        .getAisleContext().mUserId));
                                aisle.setDescription(aileItem.getAisleContext().mDescription);
                                aisle.setId(Long.valueOf(aileItem
                                        .getAisleContext().mAisleId));
                                mAisleAddCallback.onAisleAdded(aisle);
                                mixpanel.track("Create_Aisle_Success", null);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        } else {
                            Toast.makeText(
                                    VueApplication.getInstance(),
                                    VueApplication
                                            .getInstance()
                                            .getResources()
                                            .getString(
                                                    R.string.upload_failed_mesg),
                                    Toast.LENGTH_LONG).show();
                            
                        }
                    }
                });
    }
}
