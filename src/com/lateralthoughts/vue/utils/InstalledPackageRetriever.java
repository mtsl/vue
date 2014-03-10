package com.lateralthoughts.vue.utils;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.lateralthoughts.vue.VueApplication;
import com.lateralthoughts.vue.VueConstants;

public class InstalledPackageRetriever {
    private ArrayList<String> mAppIconsPath = new ArrayList<String>();
    private ArrayList<String> mPackageNames = new ArrayList<String>();
    private ArrayList<String> mAppNames = new ArrayList<String>();
    private ArrayList<String> mActivityNames = new ArrayList<String>();
    private Context mContext;
    
    /**
     * 
     * @param context
     *            Context
     */
    public InstalledPackageRetriever(Context context) {
        this.mContext = context;
    }
    
    /**
     * this method will give all the installed applications
     */
    public void getInstalledPackages() {
        String[] sharePackageNames = { VueConstants.TWITTER_PACKAGE_NAME,
                VueConstants.FACEBOOK_PACKAGE_NAME,
                VueConstants.GOOGLEPLUS_PACKAGE_NAME,
                VueConstants.GMAIL_PACKAGE_NAME,
                VueConstants.INSTAGRAM_PACKAGE_NAME };
        String[] shareAppNames = { VueConstants.TWITTER_APP_NAME,
                VueConstants.FACEBOOK_APP_NAME,
                VueConstants.GOOGLEPLUS_APP_NAME, VueConstants.GMAIL_APP_NAME,
                VueConstants.INSTAGRAM_APP_NAME };
        PackageManager pm = mContext.getPackageManager();
        FileCache fileCache = new FileCache(VueApplication.getInstance());
        for (int i = 0; i < sharePackageNames.length; i++) {
            try {
                PackageInfo pmo = pm.getPackageInfo(sharePackageNames[i],
                        PackageManager.GET_ACTIVITIES);
                mAppNames.add(shareAppNames[i]);
                mPackageNames.add(sharePackageNames[i]);
                String fileName = null;
                Drawable appIcon = null;
                try {
                    File file = fileCache
                            .getVueInstalledAppIconFile(sharePackageNames[i]
                                    .replace(".", ""));
                    fileName = null /* file.getPath() */;
                    if (!file.exists()) {
                        /*
                         * appIcon = pmo.applicationInfo.loadIcon(pm);
                         * Utils.saveBitmap( ((BitmapDrawable)
                         * appIcon).getBitmap(), file);
                         */
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                mAppIconsPath.add(fileName);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        mPackageNames.add(mContext.getApplicationContext().getPackageName());
        mAppNames.add(mContext.getApplicationContext().getApplicationInfo()
                .loadLabel(mContext.getPackageManager()).toString());
        String fileName = null;
        Drawable appIcon = null;
        try {
            File file = fileCache.getVueInstalledAppIconFile(mContext
                    .getApplicationContext().getPackageName().replace(".", ""));
            fileName = null/* file.getPath() */;
            if (!file.exists()) {
                /*
                 * appIcon =
                 * mContext.getApplicationContext().getApplicationInfo()
                 * .loadIcon(mContext.getPackageManager());
                 * Utils.saveBitmap(((BitmapDrawable) appIcon).getBitmap(),
                 * file);
                 */
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        mAppIconsPath.add(fileName);
    }
    
    /**
     * 
     * @return ArrayList<String>
     */
    public ArrayList<String> getAppNames() {
        return mAppNames;
    }
    
    public ArrayList<String> getActivityNames() {
        return mActivityNames;
    }
    
    /**
     * 
     * @return ArrayList<Drawable>
     */
    public ArrayList<String> getDrawables() {
        return mAppIconsPath;
    }
    
    /**
     * 
     * @return ArrayList<ResolveInfo>
     */
    public ArrayList<String> getpackageNames() {
        return mPackageNames;
    }
}
