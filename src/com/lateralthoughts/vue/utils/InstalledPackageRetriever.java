package com.lateralthoughts.vue.utils;

import java.util.ArrayList;
import java.util.List;

import com.lateralthoughts.vue.VueConstants;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;

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
	public void getInstalledPackages(boolean loadAllApplications) {
		if (!loadAllApplications) {
			String[] sharePackageNames = { VueConstants.TWITTER_PACKAGE_NAME,
					VueConstants.FACEBOOK_PACKAGE_NAME,
					VueConstants.GOOGLEPLUS_PACKAGE_NAME,
					VueConstants.GMAIL_PACKAGE_NAME,
					VueConstants.INSTAGRAM_PACKAGE_NAME };
			String[] shareAppNames = { VueConstants.TWITTER_APP_NAME,
					VueConstants.FACEBOOK_APP_NAME,
					VueConstants.GOOGLEPLUS_APP_NAME,
					VueConstants.GMAIL_APP_NAME,
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
		} else {
			Intent intent = new Intent("android.intent.action.MAIN", null);
			intent.addCategory("android.intent.category.LAUNCHER");
			List<ResolveInfo> activities = mContext.getPackageManager()
					.queryIntentActivities(intent, 0);
			PackageManager pm = mContext.getPackageManager();
			final Object a[] = activities.toArray();
			for (int i = 0; i < activities.size(); i++) {
				boolean isSystemApp = false;
				try {
					isSystemApp = isSystemPackage(pm
							.getPackageInfo(
									((ResolveInfo) a[i]).activityInfo.applicationInfo.packageName,
									PackageManager.GET_ACTIVITIES));
				} catch (NameNotFoundException e) {
					e.printStackTrace();
				}
				String packageName = ((ResolveInfo) a[i]).activityInfo.applicationInfo.packageName;
				if (!isSystemApp
						&& !(packageName.equals("com.lateralthoughts.vue"))) {
					mAppNames
							.add(((ResolveInfo) a[i]).activityInfo.applicationInfo
									.loadLabel(mContext.getPackageManager())
									.toString());
					mActivityNames.add(((ResolveInfo) a[i]).activityInfo.name);
					mPackageNames.add(packageName);
					mAppIcons
							.add(((ResolveInfo) a[i]).activityInfo.applicationInfo
									.loadIcon(mContext.getPackageManager()));
				}
			}
		}
	}

	private boolean isSystemPackage(PackageInfo pkgInfo) {
		return ((pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) ? true
				: false;
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
