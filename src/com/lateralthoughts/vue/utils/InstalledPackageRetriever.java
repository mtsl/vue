package com.lateralthoughts.vue.utils;

import java.util.ArrayList;
import com.lateralthoughts.vue.VueConstants;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;

/**
 * 
 * @author raju
 * 
 */
public class InstalledPackageRetriever {
	ArrayList<Drawable> drawbles = new ArrayList<Drawable>();
	ArrayList<String> packageNames = new ArrayList<String>();
	ArrayList<String> appNames = new ArrayList<String>();
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
		Log.e("share click", "30");
		/*activities = context.getPackageManager().queryIntentActivities(
				sendIntent, 0);
		Log.e("share click", "31");
		final Object a[] = activities.toArray();
		Log.e("share click", "32");*/
		
		String[] sharePackageNames = {VueConstants.TWITTER_PACKAGE_NAME, VueConstants.FACEBOOK_PACKAGE_NAME, VueConstants.GOOGLEPLUS_PACKAGE_NAME, VueConstants.GMAIL_PACKAGE_NAME };
		String[] shareAppNames = {VueConstants.TWITTER_APP_NAME, VueConstants.FACEBOOK_APP_NAME, VueConstants.GOOGLEPLUS_APP_NAME, VueConstants.GMAIL_APP_NAME };

		
	    PackageManager pm = context.getPackageManager();
		
	    Log.e("share click", "31");
	    
		for (int i = 0; i < sharePackageNames.length; i++) {
			
			Log.e("share click", "32..."+i);
			
			try {
			     PackageInfo pmo = pm.getPackageInfo(sharePackageNames[i], PackageManager.GET_ACTIVITIES);
				appNames.add(shareAppNames[i]);
				packageNames.add(sharePackageNames[i]);
				drawbles.add(pmo.applicationInfo.loadIcon(pm));
			    } catch (PackageManager.NameNotFoundException e) {
			    e.printStackTrace();
			    }
		}
		
		
		
		
		
		/*int  count = 0;
		for (int i = 0; i < activities.size(); i++) {
			Log.e("share click", "33..."+i);
			String networkName = ((ResolveInfo) a[i]).activityInfo.applicationInfo
					.loadLabel(context.getPackageManager()).toString();
			if (networkName.equalsIgnoreCase(VueConstants.FACEBOOK_APP_NAME)
					|| networkName.equalsIgnoreCase(VueConstants.GMAIL_APP_NAME)
					|| networkName.equalsIgnoreCase(VueConstants.GOOGLEPLUS_APP_NAME)
					|| networkName.equalsIgnoreCase(VueConstants.TWITTER_APP_NAME)) {
				count ++;
				//Drawable appicon;
			
				appicon = ((ResolveInfo) a[i]).activityInfo.applicationInfo
						.loadIcon(context.getPackageManager());
				listmessages.add(networkName);
				currentDisp.add((ResolveInfo) a[i]);
				//drawbles.add(appicon);
			}
			
			if(count == 4) break;
		}*/

	}

/*	*//**
	 * short list the network names. to display in dialogue.
	 *//*
	public void makeShorList() {
		ArrayList<String> listmessagesDummy = new ArrayList<String>();
		ArrayList<ResolveInfo> currentDispDummy = new ArrayList<ResolveInfo>();
		ArrayList<Drawable> drawblesDummy = new ArrayList<Drawable>();
		String networkName = null;
		for (int i = 0; i < listmessages.size(); i++) {
			networkName = listmessages.get(i);
			if (networkName.equalsIgnoreCase(VueConstants.FACEBOOK_APP_NAME)
					|| networkName.equalsIgnoreCase(VueConstants.GMAIL_APP_NAME)
					|| networkName.equalsIgnoreCase(VueConstants.GOOGLEPLUS_APP_NAME)
					|| networkName.equalsIgnoreCase(VueConstants.TWITTER_APP_NAME)) {
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

	}*/

/*	*//**
	 * 
	 *//*
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
*/
	/**
	 * 
	 * @return Drawable
	 *//*
	public Drawable getFacebookIcon() {
		if (fbdrawble == null) {
			getInstalledPackages();
			setFaceBookIcons();
		}
		return fbdrawble;
	}
*/
	/**
	 * 
	 * @return ArrayList<String>
	 */
	public ArrayList<String> getAppNames() {
		return appNames;
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
	public ArrayList<String> getpackageNames() {
		return packageNames;
	}

}
