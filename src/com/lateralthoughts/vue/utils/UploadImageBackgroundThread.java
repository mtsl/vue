package com.lateralthoughts.vue.utils;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.widget.RemoteViews;

import com.lateralthoughts.vue.AisleManager.ImageUploadCallback;
import com.lateralthoughts.vue.R;
import com.lateralthoughts.vue.VueApplication;
import com.lateralthoughts.vue.VueConstants;

public class UploadImageBackgroundThread implements Runnable,
        CountingStringEntity.UploadListener {
    private NotificationManager mNotificationManager;
    private Notification mNotification;
    private int mLastPercent = 0;
    private File mImageFile;
    private ImageUploadCallback mImageUploadCallback;
    private String mImageUrl;
    private static final int IMAGE_MAX_SIZE = 600;
    private int mImageWidth, mImageHeight;
    
    @SuppressWarnings("static-access")
    public UploadImageBackgroundThread(File imageFile,
            ImageUploadCallback imageUploadCallback) {
        mNotificationManager = (NotificationManager) VueApplication
                .getInstance().getSystemService(
                        VueApplication.getInstance().NOTIFICATION_SERVICE);
        mImageFile = imageFile;
        mImageUploadCallback = imageUploadCallback;
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
            resizeImageToUpload(mImageFile);
            mImageUrl = uploadImage(mImageFile, getImageUploadURL());
            if (mImageUrl != null) {
                mImageUrl = UrlConstants.GET_IMAGE_FILE_RESTURL + "?blob_key="
                        + mImageUrl;
            }
            if (mImageUrl != null) {
                mNotification.setLatestEventInfo(
                        VueApplication.getInstance(),
                        VueApplication.getInstance().getResources()
                                .getString(R.string.upload_successful_mesg),
                        "", contentIntent);
                mNotification.flags |= Notification.FLAG_AUTO_CANCEL;
                mNotificationManager.notify(
                        VueConstants.AISLE_INFO_UPLOAD_NOTIFICATION_ID,
                        mNotification);
                
            } else {
                mNotification.setLatestEventInfo(
                        VueApplication.getInstance(),
                        VueApplication.getInstance().getResources()
                                .getString(R.string.upload_failed_mesg), "",
                        contentIntent);
                mNotification.flags |= Notification.FLAG_AUTO_CANCEL;
                mNotificationManager.notify(
                        VueConstants.AISLE_INFO_UPLOAD_NOTIFICATION_ID,
                        mNotification);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        if (null != mImageUrl) {
            mImageUploadCallback.onImageUploaded(mImageUrl, mImageWidth,
                    mImageHeight);
        }
    }
    
    public String getImageUploadURL() throws Exception {
        URL url = new URL(UrlConstants.GET_UNIQUE_ONETIME_IMAGE_UPLOAD_RESTURL);
        HttpGet httpGet = new HttpGet(url.toString());
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpResponse response = httpClient.execute(httpGet);
        if (response.getEntity() != null
                && response.getStatusLine().getStatusCode() == 200) {
            String responseMessage = EntityUtils.toString(response.getEntity());
            return responseMessage;
        }
        return null;
    }
    
    public String uploadImage(File image, String uniqueUrl) throws Exception {
        
        HttpClient httpclient = new DefaultHttpClient();
        httpclient.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS,
                false);
        HttpPost httppost = new HttpPost(uniqueUrl);
        
        /** Request parameters and other properties. */
        MultipartEntity reqEntity = new MultipartEntity();
        reqEntity.addPart("myFile", new FileBody(image));
        httppost.setEntity(reqEntity);
        
        /** Execute and get the response. */
        HttpResponse response = httpclient.execute(httppost);
        if (response.getEntity() != null
                && response.getStatusLine().getStatusCode() == 200) {
            String responseMessage = EntityUtils.toString(response.getEntity());
            return responseMessage;
        } else
            return null;
    }
    
    private void resizeImageToUpload(File file) {
        try {
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            FileInputStream stream1 = new FileInputStream(file);
            BitmapFactory.decodeStream(stream1, null, o);
            stream1.close();
            int height = o.outHeight;
            int width = o.outWidth;
            int scale = 1;
            if (height > IMAGE_MAX_SIZE || width > IMAGE_MAX_SIZE) {
                scale = (int) Math.pow(
                        2,
                        (int) Math.round(Math.log(IMAGE_MAX_SIZE
                                / (double) Math.max(o.outHeight, o.outWidth))
                                / Math.log(0.5)));
            } else {
                mImageWidth = width;
                mImageHeight = height;
                return;
            }
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            FileInputStream stream2 = new FileInputStream(file);
            Bitmap bitmap = BitmapFactory.decodeStream(stream2, null, o2);
            stream2.close();
            
            int resizedBitmapHeight = bitmap.getHeight();
            int resizedBitmapWidth = bitmap.getWidth();
            if (resizedBitmapHeight > resizedBitmapWidth) {
                if (resizedBitmapHeight > IMAGE_MAX_SIZE) {
                    height = IMAGE_MAX_SIZE;
                    width = (resizedBitmapWidth * height) / resizedBitmapHeight;
                    mImageWidth = width;
                    mImageHeight = height;
                    Utils.saveBitmap(getModifiedBitmap(bitmap, width, height),
                            file);
                } else {
                    Utils.saveBitmap(bitmap, file);
                    mImageWidth = resizedBitmapWidth;
                    mImageHeight = resizedBitmapHeight;
                }
            } else {
                if (resizedBitmapWidth > IMAGE_MAX_SIZE) {
                    width = IMAGE_MAX_SIZE;
                    height = (resizedBitmapHeight * width) / resizedBitmapWidth;
                    mImageWidth = width;
                    mImageHeight = height;
                    Utils.saveBitmap(getModifiedBitmap(bitmap, width, height),
                            file);
                } else {
                    Utils.saveBitmap(bitmap, file);
                    mImageWidth = resizedBitmapWidth;
                    mImageHeight = resizedBitmapHeight;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private Bitmap getModifiedBitmap(Bitmap originalImage, int width, int height) {
        Bitmap newBitmap = Bitmap.createBitmap((int) width, (int) height,
                Config.ARGB_8888);
        float originalWidth = originalImage.getWidth(), originalHeight = originalImage
                .getHeight();
        Canvas canvas = new Canvas(newBitmap);
        float scale = width / originalWidth;
        float xTranslation = 0.0f, yTranslation = (height - originalHeight
                * scale) / 2.0f;
        Matrix transformation = new Matrix();
        transformation.postTranslate(xTranslation, yTranslation);
        transformation.preScale(scale, scale);
        Paint paint = new Paint();
        paint.setFilterBitmap(true);
        canvas.drawBitmap(originalImage, transformation, paint);
        originalImage.recycle();
        return newBitmap;
    }
}
