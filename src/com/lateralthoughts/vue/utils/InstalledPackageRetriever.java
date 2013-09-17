package com.lateralthoughts.vue.utils;

import java.util.ArrayList;
import com.lateralthoughts.vue.VueConstants;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

public class InstalledPackageRetriever {
	ArrayList<Drawable> mAppIcons = new ArrayList<Drawable>();
	ArrayList<String> mPackageNames = new ArrayList<String>();
	ArrayList<String> mAppNames = new ArrayList<String>();
	Context context;

	/**
	 * 
	 * @param context
	 *            Context
	 */
	public InstalledPackageRetriever(Context context) {
		this.context = context;
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
		PackageManager pm = context.getPackageManager();
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
	}

	/**
	 * 
	 * @return ArrayList<String>
	 */
	public ArrayList<String> getAppNames() {
		return mAppNames;
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
