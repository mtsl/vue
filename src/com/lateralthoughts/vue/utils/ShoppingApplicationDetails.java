package com.lateralthoughts.vue.utils;

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

	public ShoppingApplicationDetails(String appName, String activityName,
			String packageName) {
		this.appName = appName;
		this.activityName = activityName;
		this.packageName = packageName;
	}
}
