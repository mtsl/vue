package com.lateralthoughts.vue.utils;

import java.util.ArrayList;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import com.lateralthoughts.vue.VueConstants;

public class InstalledPackageRetriever {
    private ArrayList<Drawable> mAppIcons = new ArrayList<Drawable>();
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
        for (int i = 0; i < sharePackageNames.length; i++) {
            try {
                PackageInfo pmo = pm.getPackageInfo(sharePackageNames[i],
                        PackageManager.GET_ACTIVITIES);
                mAppNames.add(shareAppNames[i]);
                mPackageNames.add(sharePackageNames[i]);
                mAppIcons.add(pmo.applicationInfo.loadIcon(pm));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        mPackageNames.add(mContext.getApplicationContext().getPackageName());
        mAppNames.add(mContext.getApplicationContext().getApplicationInfo()
                .loadLabel(mContext.getPackageManager()).toString());
        mAppIcons.add(mContext.getApplicationContext().getApplicationInfo()
                .loadIcon(mContext.getPackageManager()));
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
    public ArrayList<Drawable> getDrawables() {
        return mAppIcons;
    }
    
    /**
     * 
     * @return ArrayList<ResolveInfo>
     */
    public ArrayList<String> getpackageNames() {
        return mPackageNames;
    }
}
