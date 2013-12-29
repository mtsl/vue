package com.lateralthoughts.vue.utils;

import java.io.File;

import android.content.Context;

import com.lateralthoughts.vue.VueConstants;

public class FileCache {
    
    private File mCacheDir;
    private long mTwoDaysOldTime = 2 * 24 * 60 * 60 * 1000;
    private File mVueAppCameraPicsDir;
    private File mHelpPicDir;
    
    public File getmVueAppCameraPicsDir() {
        return mVueAppCameraPicsDir;
    }
    
    public void setmVueAppCameraPicsDir(File mVueAppCameraPicsDir) {
        this.mVueAppCameraPicsDir = mVueAppCameraPicsDir;
    }
    
    public File getmVueAppResizedImagesDir() {
        return mVueAppResizedImagesDir;
    }
    
    public void setmVueAppResizedImagesDir(File mVueAppResizedImagesDir) {
        this.mVueAppResizedImagesDir = mVueAppResizedImagesDir;
    }
    
    private File mVueAppResizedImagesDir;
    
    private File mVueUserProfileImageDir;
    
    public File getmVueUserProfileImageDir() {
        return mVueUserProfileImageDir;
    }
    
    public void setmVueUserProfileImageDir(File mVueUserProfileImageDir) {
        this.mVueUserProfileImageDir = mVueUserProfileImageDir;
    }
    
    public FileCache(Context context) {
        // Find the dir to save cached images
        if (android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED)) {
            mCacheDir = new File(context.getExternalCacheDir(), "LazyList");
            mVueAppCameraPicsDir = new File(context.getExternalFilesDir(null),
                    VueConstants.VUE_APP_CAMERAPICTURES_FOLDER);
            mHelpPicDir = new File(context.getExternalFilesDir(null),
                    VueConstants.VUE_APP_HELP_FOLDER);
            
            mVueAppResizedImagesDir = new File(
                    context.getExternalFilesDir(null),
                    VueConstants.VUE_APP_RESIZED_PICTURES_FOLDER);
            mVueUserProfileImageDir = new File(
                    context.getExternalFilesDir(null),
                    VueConstants.VUE_APP_USER_PROFILE_PICTURES_FOLDER);
        } else {
            mCacheDir = context.getCacheDir();
            mVueAppCameraPicsDir = new File(context.getFilesDir(),
                    VueConstants.VUE_APP_CAMERAPICTURES_FOLDER);
            mHelpPicDir = new File(context.getFilesDir(),
                    VueConstants.VUE_APP_HELP_FOLDER);
            mVueAppResizedImagesDir = new File(context.getFilesDir(),
                    VueConstants.VUE_APP_RESIZED_PICTURES_FOLDER);
            mVueUserProfileImageDir = new File(context.getFilesDir(),
                    VueConstants.VUE_APP_USER_PROFILE_PICTURES_FOLDER);
        }
        
        if (!mCacheDir.exists())
            mCacheDir.mkdirs();
        if (!mVueAppCameraPicsDir.exists())
            mVueAppCameraPicsDir.mkdirs();
        if (!mVueAppResizedImagesDir.exists())
            mVueAppResizedImagesDir.mkdirs();
        if (!mVueUserProfileImageDir.exists())
            mVueUserProfileImageDir.mkdirs();
        if (!mHelpPicDir.exists())
            mHelpPicDir.mkdirs();
    }
    
    public File getFile(String url) {
        int hashCode = url.hashCode();
        String filename = String.valueOf(hashCode);
        File f = new File(mCacheDir, filename + ".jpg");
        return f;
        
    }
    
    public File getHelpFile(String imageName) {
        File f = new File(mHelpPicDir, imageName + ".jpg");
        return f;
    }
    
    public File getVueAppCameraPictureFile(String cameraImageName) {
        File f = new File(mVueAppCameraPicsDir, cameraImageName + ".jpg");
        return f;
    }
    
    public File getVueAppResizedPictureFile(String resizedImageName) {
        File f = new File(mVueAppResizedImagesDir, resizedImageName + ".jpg");
        return f;
    }
    
    public File getVueAppUserProfilePictureFile(String imageName) {
        File f = new File(mVueUserProfileImageDir, imageName + ".jpg");
        return f;
    }
    
    @Deprecated
    public void clear() {
        File[] files = mCacheDir.listFiles();
        if (files == null)
            return;
        for (File f : files)
            f.delete();
    }
    
    public void clearVueAppResizedPictures() {
        File[] files = mVueAppResizedImagesDir.listFiles();
        if (files == null)
            return;
        for (File f : files)
            f.delete();
    }
    
    public void clearTwoDaysOldPictures() {
        File[] files = mCacheDir.listFiles();
        if (files == null) {
            return;
        }
        for (File f : files) {
            long lastModifidedDate = f.lastModified();
            if (System.currentTimeMillis() - lastModifidedDate >= mTwoDaysOldTime) {
                if (f.delete()) {
                }
            }
        }
    }
}