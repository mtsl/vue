package com.lateralthoughts.vue.utils;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.util.Log;

/**
 * 
 * @author raju
 * 
 */
public class InstalledPackageRetriever {
	Intent sendIntent;
	List<ResolveInfo> activities;
	ArrayList<Drawable> drawbles = new ArrayList<Drawable>();
	ArrayList<ResolveInfo> currentDisp = new ArrayList<ResolveInfo>();
	ArrayList<String> listmessages = new ArrayList<String>();
	Context context;
	String fbpackageName;
	Drawable fbdrawble;

	/**
	 * 
	 * @param context
	 *            Context
	 */
	public InstalledPackageRetriever(Context context) {
		this.context = context;
	}

	/**
	 * 
	 */
	public void createIntent() {
		sendIntent = new Intent(android.content.Intent.ACTION_SEND);
		sendIntent.setType("text/plain");
	}

	/**
	 * this method will give all the installed applications
	 */
	public void getInstalledPackages() {
		activities = context.getPackageManager().queryIntentActivities(
				sendIntent, 0);

		final Object a[] = activities.toArray();
		for (int i = 0; i < activities.size(); i++) {
			Drawable appicon;
			String temp = ((ResolveInfo) a[i]).activityInfo.applicationInfo
					.loadLabel(context.getPackageManager()).toString();
			appicon = ((ResolveInfo) a[i]).activityInfo.applicationInfo
					.loadIcon(context.getPackageManager());
			listmessages.add(temp);
			currentDisp.add((ResolveInfo) a[i]);
			drawbles.add(appicon);

		}

	}

	/**
	 * short list the network names. to display in dialogue.
	 */
	public void makeShorList() {
		ArrayList<String> listmessagesDummy = new ArrayList<String>();
		ArrayList<ResolveInfo> currentDispDummy = new ArrayList<ResolveInfo>();
		ArrayList<Drawable> drawblesDummy = new ArrayList<Drawable>();
		String networkName = null;
		for (int i = 0; i < listmessages.size(); i++) {
			networkName = listmessages.get(i);
			if (networkName.equalsIgnoreCase("Facebook")
					|| networkName.equalsIgnoreCase("Gmail")
					|| networkName.equalsIgnoreCase("Email")
					|| networkName.equalsIgnoreCase("Twitter")) {
				listmessagesDummy.add(listmessages.get(i));
				currentDispDummy.add(currentDisp.get(i));
				drawblesDummy.add(drawbles.get(i));
			}
		}
		for (int i = 0; i < listmessages.size(); i++) {
			networkName = listmessages.get(i);
			if (!(networkName.equalsIgnoreCase("Facebook")
					|| networkName.equalsIgnoreCase("Gmail")
					|| networkName.equalsIgnoreCase("Email") || networkName
						.equalsIgnoreCase("Twitter"))) {
				listmessagesDummy.add(listmessages.get(i));
				currentDispDummy.add(currentDisp.get(i));
				drawblesDummy.add(drawbles.get(i));
			}
		}
		listmessages.clear();
		currentDisp.clear();
		drawbles.clear();
		listmessages = listmessagesDummy;
		currentDisp = currentDispDummy;
		drawbles = drawblesDummy;

	}

	/**
	 * 
	 */
	public void setFaceBookIcons() {
		for (int index = 0; index < listmessages.size(); index++) {
			String temp = listmessages.get(index);
			if (temp.equalsIgnoreCase("Facebook")) {
				fbpackageName = temp;
				fbdrawble = drawbles.get(index);
				Log.i("FISH", "REQUIRED PACKAGE IS FOUND :   " + temp);
				break;
			}
		}
	}

	/**
	 * 
	 * @return Drawable
	 */
	public Drawable getFacebookIcon() {
		if (fbdrawble == null) {
			getInstalledPackages();
			setFaceBookIcons();
		}
		return fbdrawble;
	}

	/**
	 * 
	 * @return ArrayList<String>
	 */
	public ArrayList<String> getListMessages() {

		return listmessages;

	}

	/**
	 * 
	 * @return ArrayList<Drawable>
	 */
	public ArrayList<Drawable> getDrawables() {
		return drawbles;

	}

	/**
	 * 
	 * @return ArrayList<ResolveInfo>
	 */
	public ArrayList<ResolveInfo> getDisplayPackages() {
		return currentDisp;
	}

	/**
	 * 
	 * @return Intent
	 */
	public Intent getShareIntent() {
		return sendIntent;

	}

}
