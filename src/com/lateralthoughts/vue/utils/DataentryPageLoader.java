package com.lateralthoughts.vue.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Stack;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.lateralthoughts.vue.DataEntryFragment;
import com.lateralthoughts.vue.R;
import com.lateralthoughts.vue.VueApplication;

public class DataentryPageLoader {
    
    PhotosQueue mPhotosQueue;
    PhotosLoader mPhotoLoaderThread;
    public static DataentryPageLoader mOtherSourceImageLoader;
    
    public DataentryPageLoader() {
        mPhotosQueue = new PhotosQueue();
        mPhotoLoaderThread = new PhotosLoader();
        mPhotoLoaderThread.setPriority(Thread.NORM_PRIORITY - 1);
    }
    
    public static DataentryPageLoader getInstatnce() {
        if (mOtherSourceImageLoader == null) {
            mOtherSourceImageLoader = new DataentryPageLoader();
        }
        return mOtherSourceImageLoader;
    }
    
    public void DisplayImage(String originalImagePath, String imageUrl,
            String imagePath, ImageView imageView,
            ProgressBar dataEntryRowAisleImage, LinearLayout imageDeleteBtn,
            boolean isAddedToServer, boolean hideDeleteButton) {
        queuePhoto(originalImagePath, imageUrl, imagePath, imageView,
                dataEntryRowAisleImage, imageDeleteBtn, isAddedToServer,
                hideDeleteButton);
    }
    
    private void queuePhoto(String originalImagePath, String imageUrl,
            String imagePath, ImageView imageView,
            ProgressBar dataEntryRowAisleImage, LinearLayout imageDeleteBtn,
            boolean isAddedToServer, boolean hideDeleteButton) {
        mPhotosQueue.Clean(imageView);
        PhotoToLoad p = new PhotoToLoad(originalImagePath, imageUrl, imagePath,
                imageView, dataEntryRowAisleImage, imageDeleteBtn,
                isAddedToServer, hideDeleteButton);
        synchronized (mPhotosQueue.photosToLoad) {
            mPhotosQueue.photosToLoad.push(p);
            mPhotosQueue.photosToLoad.notifyAll();
        }
        
        if (mPhotoLoaderThread.getState() == Thread.State.NEW)
            mPhotoLoaderThread.start();
    }
    
    private class PhotoToLoad {
        public String imagePath;
        public ImageView imageView;
        public ProgressBar dataEntryRowAisleImage;
        String originalImagePath;
        String imageUrl;
        LinearLayout imageDeleteBtn;
        boolean isAddedToServer, hideDeleteButton;
        
        public PhotoToLoad(String originalImagePath, String imageUrl, String u,
                ImageView i, ProgressBar dataEntryRowAisleImage,
                LinearLayout imageDeleteBtn, boolean isAddedToServer,
                boolean hideDeleteButton) {
            imagePath = u;
            imageView = i;
            this.dataEntryRowAisleImage = dataEntryRowAisleImage;
            this.imageUrl = imageUrl;
            this.originalImagePath = originalImagePath;
            this.imageDeleteBtn = imageDeleteBtn;
            this.isAddedToServer = isAddedToServer;
            this.hideDeleteButton = hideDeleteButton;
        }
    }
    
    public void stopThread() {
        mPhotoLoaderThread.interrupt();
    }
    
    class PhotosQueue {
        private Stack<PhotoToLoad> photosToLoad = new Stack<PhotoToLoad>();
        
        public void Clean(ImageView image) {
            for (int j = 0; j < photosToLoad.size();) {
                if (photosToLoad.get(j).imageView == image)
                    photosToLoad.remove(j);
                else
                    ++j;
            }
        }
    }
    
    class PhotosLoader extends Thread {
        public void run() {
            try {
                while (true) {
                    if (mPhotosQueue.photosToLoad.size() == 0)
                        synchronized (mPhotosQueue.photosToLoad) {
                            mPhotosQueue.photosToLoad.wait();
                        }
                    if (mPhotosQueue.photosToLoad.size() != 0) {
                        PhotoToLoad photoToLoad;
                        synchronized (mPhotosQueue.photosToLoad) {
                            photoToLoad = mPhotosQueue.photosToLoad.pop();
                        }
                        File f = new File(photoToLoad.imagePath);
                        if (f != null && f.exists()) {
                            if (((String) photoToLoad.imageView.getTag())
                                    .equals(photoToLoad.imagePath)) {
                                Bitmap bmp = BitmapFactory.decodeFile(f
                                        .getPath());
                                BitmapDisplayer bd = new BitmapDisplayer(bmp,
                                        photoToLoad.imageView,
                                        photoToLoad.dataEntryRowAisleImage,
                                        photoToLoad.imageDeleteBtn,
                                        photoToLoad.isAddedToServer,
                                        photoToLoad.hideDeleteButton);
                                Activity a = (Activity) photoToLoad.imageView
                                        .getContext();
                                a.runOnUiThread(bd);
                            }
                        } else {
                            if (photoToLoad.originalImagePath != null) {
                                DisplayMetrics dm = VueApplication
                                        .getInstance().getResources()
                                        .getDisplayMetrics();
                                float screenHeight = dm.heightPixels;
                                screenHeight = screenHeight
                                        - Utils.dipToPixels(
                                                VueApplication.getInstance(),
                                                DataEntryFragment.AISLE_IMAGE_MARGIN);
                                float screenWidth = dm.widthPixels;
                                getResizedImage(new File(
                                        photoToLoad.originalImagePath),
                                        new File(photoToLoad.imagePath),
                                        screenHeight, screenWidth,
                                        VueApplication.getInstance());
                                if (((String) photoToLoad.imageView.getTag())
                                        .equals(photoToLoad.imagePath)) {
                                    Bitmap bmp = BitmapFactory.decodeFile(f
                                            .getPath());
                                    BitmapDisplayer bd = new BitmapDisplayer(
                                            bmp, photoToLoad.imageView,
                                            photoToLoad.dataEntryRowAisleImage,
                                            photoToLoad.imageDeleteBtn,
                                            photoToLoad.isAddedToServer,
                                            photoToLoad.hideDeleteButton);
                                    Activity a = (Activity) photoToLoad.imageView
                                            .getContext();
                                    a.runOnUiThread(bd);
                                }
                            } else if (photoToLoad.imageUrl != null) {
                                try {
                                    InputStream is = new URL(
                                            photoToLoad.imageUrl).openStream();
                                    OutputStream os = new FileOutputStream(
                                            new FileCache(VueApplication
                                                    .getInstance())
                                                    .getFile(photoToLoad.imageUrl));
                                    Utils.CopyStream(is, os);
                                    os.close();
                                    DisplayMetrics dm = VueApplication
                                            .getInstance().getResources()
                                            .getDisplayMetrics();
                                    float screenHeight = dm.heightPixels;
                                    screenHeight = screenHeight
                                            - Utils.dipToPixels(
                                                    VueApplication
                                                            .getInstance(),
                                                    DataEntryFragment.AISLE_IMAGE_MARGIN);
                                    float screenWidth = dm.widthPixels;
                                    getResizedImage(
                                            new FileCache(VueApplication
                                                    .getInstance())
                                                    .getFile(photoToLoad.imageUrl),
                                            new File(photoToLoad.imagePath),
                                            screenHeight, screenWidth,
                                            VueApplication.getInstance());
                                    if (((String) photoToLoad.imageView
                                            .getTag())
                                            .equals(photoToLoad.imagePath)) {
                                        Bitmap bmp = BitmapFactory.decodeFile(f
                                                .getPath());
                                        BitmapDisplayer bd = new BitmapDisplayer(
                                                bmp,
                                                photoToLoad.imageView,
                                                photoToLoad.dataEntryRowAisleImage,
                                                photoToLoad.imageDeleteBtn,
                                                photoToLoad.isAddedToServer,
                                                photoToLoad.hideDeleteButton);
                                        Activity a = (Activity) photoToLoad.imageView
                                                .getContext();
                                        a.runOnUiThread(bd);
                                    }
                                } catch (MalformedURLException e) {
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }
                    }
                    if (Thread.interrupted())
                        break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    class BitmapDisplayer implements Runnable {
        Bitmap bmp;
        ImageView imageView;
        ProgressBar dataEntryRowAisleImage;
        LinearLayout imageDeleteBtn;
        boolean isAddedToServer, hideDeleteButton;
        
        public BitmapDisplayer(Bitmap bmp, ImageView i,
                ProgressBar dataEntryRowAisleImage,
                LinearLayout imageDeleteBtn, boolean isAddedToServer,
                boolean hideDeleteButton) {
            this.bmp = bmp;
            imageView = i;
            this.dataEntryRowAisleImage = dataEntryRowAisleImage;
            this.imageDeleteBtn = imageDeleteBtn;
            this.isAddedToServer = isAddedToServer;
            this.hideDeleteButton = hideDeleteButton;
        }
        
        public void run() {
            dataEntryRowAisleImage.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
            if (isAddedToServer) {
                if (hideDeleteButton) {
                    imageDeleteBtn.setVisibility(View.GONE);
                } else {
                    imageDeleteBtn.setVisibility(View.VISIBLE);
                }
            } else {
                imageDeleteBtn.setVisibility(View.GONE);
            }
            if (bmp != null) {
                imageView.setImageBitmap(bmp);
            } else {
                imageView.setImageResource(R.drawable.no_image);
            }
        }
    }
    
    private void getResizedImage(File f, File resizedFileName,
            float screenHeight, float screenWidth, Context mContext) {
        try {
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            FileInputStream stream1 = new FileInputStream(f);
            BitmapFactory.decodeStream(stream1, null, o);
            stream1.close();
            int height = o.outHeight;
            int width = o.outWidth;
            int scale = 1;
            int heightRatio = 0;
            int widthRatio = 0;
            if (height > screenHeight) {
                heightRatio = Math.round((float) height / (float) screenHeight);
            }
            if (width > screenWidth) {
                widthRatio = Math.round((float) width / (float) screenWidth);
            }
            scale = heightRatio < widthRatio ? heightRatio : widthRatio;
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = (int) scale;
            FileInputStream stream2 = new FileInputStream(f);
            Bitmap resizedBitmap = BitmapFactory
                    .decodeStream(stream2, null, o2);
            stream2.close();
            Utils.saveBitmap(resizedBitmap, resizedFileName);
            BitmapFactory.Options o3 = new BitmapFactory.Options();
            o3.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(resizedFileName),
                    null, o3);
            if (o3.outWidth > VueApplication.getInstance().mScreenWidth
                    || o3.outHeight > VueApplication.getInstance().mScreenHeight) {
                resizedBitmap = Utils.getBestDementions(resizedBitmap,
                        o3.outWidth, o3.outHeight,
                        VueApplication.getInstance().mScreenWidth,
                        VueApplication.getInstance().mScreenHeight);
                Utils.saveBitmap(resizedBitmap, resizedFileName);
            }
            resizedBitmap.recycle();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}