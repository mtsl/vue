package com.lateralthoughts.vue.utils;

import android.graphics.drawable.Drawable;

public class ShoppingApplicationDetails {
	private String appName;

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getActivityName() {
		return activityName;
	}

	public void setActivityName(String activityName) {
		this.activityName = activityName;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	private String activityName;
	private String packageName;

	public Drawable getAppIcon() {
		return appIcon;
	}

	public void setAppIcon(Drawable appIcon) {
		this.appIcon = appIcon;
	}

	private Drawable appIcon;

	public ShoppingApplicationDetails(String appName, String activityName,
			String packageName, Drawable appIcon) {
		this.appName = appName;
		this.activityName = activityName;
		this.packageName = packageName;
		this.appIcon = appIcon;
	}
}
