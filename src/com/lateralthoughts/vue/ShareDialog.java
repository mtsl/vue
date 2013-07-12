package com.lateralthoughts.vue;

import java.io.File;
import java.util.ArrayList;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.lateralthoughts.vue.utils.InstalledPackageRetriever;
import com.lateralthoughts.vue.utils.Utils;
import com.lateralthoughts.vue.utils.clsShare;

/**
 * 
 * common class for share functionality capable of handling
 * email,gmail,twitter,face book and used action send intent to call the other
 * applications
 * 
 */
public class ShareDialog {

	LayoutInflater layoutInflater;
	AlertDialog.Builder screenDialog;
	ArrayList<String> appNames = new ArrayList<String>();
	ArrayList<String> packageNames = new ArrayList<String>();
	ArrayList<Drawable> appIcons = new ArrayList<Drawable>();
	Intent sendIntent;
	Context context;
	Activity activity;
	String shareOption;
	Resources resouces;
	InstalledPackageRetriever shareIntentObj;
	public final static int TWITER_TITLE_BUDGET = 76;
	Bitmap articleBitmap;
	boolean shareIntentCalled = false;
	public Dialog dialog;
	String aisleTitle, name;
	ArrayList<clsShare> imagePathArray;
	ProgressDialog shareDialog;
	private static final String TAG = "ShareDialog";

	public void dismisDialog() {
		shareDialog.dismiss();
	}

	/**
	 * 
	 * @param context
	 *            Context
	 */
	public ShareDialog(Context context, Activity activity) {
		this.context = context;
		this.activity = activity;
		layoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		resouces = context.getResources();
	}

	public void share(ArrayList<clsShare> imagePathArray, String aisleTitle,
			String name) {
		shareIntentCalled = false;
		this.imagePathArray = imagePathArray;
		this.aisleTitle = aisleTitle;
		this.name = name;
		prepareShareIntentData();
		openScreenDialog();
	}

	/** to show pop-up */
	private void openScreenDialog() {
		shareDialog = ProgressDialog.show(context,
				context.getString(R.string.app_name), "Sharing....", true);
		shareDialog.dismiss();
		dialog = new Dialog(context, R.style.Theme_Dialog_Translucent);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.sharedialogue);
		ListView listview = (ListView) dialog.findViewById(R.id.networklist);
		TextView okbuton = (TextView) dialog.findViewById(R.id.shownetworkok);
		listview.setAdapter(new CustomAdapter());
		listview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> adapter, View v,
					int position, long id) {
				shareIntent(position);
			}
		});
		dialog.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface arg0) {
				shareIntentObj = null;
			}
		});
		dialog.show();
		okbuton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				InputMethodManager i1pm = (InputMethodManager) context
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				i1pm.hideSoftInputFromWindow(null, 0);
			}
		});
		screenDialog = new AlertDialog.Builder(context);
		screenDialog.setTitle("SHARE VIA");
	}

	/**
	 * Adapter to set on popup dialogue
	 * 
	 * */
	public class CustomAdapter extends BaseAdapter {
		/**
		 * returns the count
		 * 
		 * @return int
		 */
		public int getCount() {
			return appNames.size();
		}

		/**
		 * @param arg0
		 *            int
		 * @return Object
		 */
		public Object getItem(int arg0) {
			return arg0;
		}

		/**
		 * @param arg0
		 *            int
		 * @return long
		 */
		public long getItemId(int arg0) {
			return arg0;
		}

		/**
		 * @param position
		 *            int
		 * 
		 * @param convertView
		 *            View
		 * @param parent
		 *            ViewGroup
		 * @return View
		 */
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			Holder holderView = null;
			if (convertView == null) {
				holderView = new Holder();
				convertView = layoutInflater.inflate(R.layout.sharedialog_row, null);
				holderView.network = (TextView) convertView
						.findViewById(R.id.gmail);
				holderView.launcheicon = (ImageView) convertView
						.findViewById(R.id.launchericon);
				convertView.setTag(holderView);
			} else {
				holderView = (Holder) convertView.getTag();
			}
			holderView.launcheicon.setImageDrawable(appIcons.get(position));
			holderView.network.setText(appNames.get(position));
			return convertView;
		}
	}

	private void shareIntent(final int position) {
		shareDialog.show();
		try {
			if (appNames.get(position).equalsIgnoreCase(
					VueConstants.FACEBOOK_APP_NAME)) {
				dialog.dismiss();
				shareDialog.dismiss();
				String shareText = "Your friend "
						+ name
						+ " wants your opinion - get Vue to see the full details and help "
						+ name + " out.";
				Intent i = new Intent(context, VueLoginActivity.class);
				Bundle b = new Bundle();
				b.putBoolean(VueConstants.CANCEL_BTN_DISABLE_FLAG, false);
				b.putString(VueConstants.FROM_INVITEFRIENDS, null);
				b.putBoolean(VueConstants.FBLOGIN_FROM_DETAILS_SHARE, true);
				b.putBoolean(VueConstants.FROM_BEZELMENU_LOGIN, false);
				b.putString(VueConstants.FBPOST_TEXT, shareText);
				b.putParcelableArrayList(VueConstants.FBPOST_IMAGEURLS,
						imagePathArray);
				i.putExtras(b);
				context.startActivity(i);
			} else if (appNames.get(position).equalsIgnoreCase(
					VueConstants.GOOGLEPLUS_APP_NAME)) {
				shareImageAndText(position);
			} else if (appNames.get(position).equalsIgnoreCase(
					VueConstants.GMAIL_APP_NAME)) {
				shareImageAndText(position);
			} else if (appNames.get(position).equalsIgnoreCase(
					VueConstants.TWITTER_APP_NAME)) {
				shareText(position);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			dialog.dismiss();
			shareDialog.dismiss();
			showAlertMessageShareError(appNames.get(position), false);
		}
	}

	private void showAlertMessageShareError(String appName, boolean fberror) {
		final Dialog gplusdialog = new Dialog(context,
				R.style.Theme_Dialog_Translucent);
		gplusdialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		gplusdialog.setContentView(R.layout.googleplusappinstallationdialog);
		TextView messagetext = (TextView) gplusdialog
				.findViewById(R.id.messagetext);
		if (!fberror)
			messagetext.setText("Unable to Share content to " + appName);
		else
			messagetext.setText(appName);
		TextView noButton = (TextView) gplusdialog.findViewById(R.id.nobutton);
		noButton.setVisibility(View.GONE);
		TextView okButton = (TextView) gplusdialog.findViewById(R.id.okbutton);
		okButton.setText("OK");
		okButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				gplusdialog.dismiss();
			}
		});
		gplusdialog.show();
	}

	private void prepareShareIntentData() {
		if (shareIntentObj == null) {
			shareIntentObj = new InstalledPackageRetriever(context);
			shareIntentObj.getInstalledPackages();
			appNames = shareIntentObj.getAppNames();
			packageNames = shareIntentObj.getpackageNames();
			appIcons = shareIntentObj.getDrawables();
			sendIntent = new Intent(android.content.Intent.ACTION_SEND);
			sendIntent.setType("text/plain");
		}
	}

	private class Holder {
		TextView network;
		ImageView launcheicon;
	}

	private void shareImageAndText(final int position) {
		dialog.dismiss();
		shareIntentCalled = true;
		shareDialog.show();
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				ArrayList<Uri> imageUris = new ArrayList<Uri>();
				if (imagePathArray != null && imagePathArray.size() > 0) {
					for (int i = 0; i < imagePathArray.size(); i++) {
						final File f = new File(imagePathArray.get(i)
								.getFilepath());
						if (!f.exists()) {
							Response.Listener listener = new Response.Listener<Bitmap>() {
								@Override
								public void onResponse(Bitmap bmp) {
									Utils.saveBitmap(bmp, f);
								}
							};
							Response.ErrorListener errorListener = new Response.ErrorListener() {
								@Override
								public void onErrorResponse(VolleyError arg0) {
									Log.e(TAG, arg0.getMessage());
								}
							};
							ImageRequest imagerequestObj = new ImageRequest(
									imagePathArray.get(i).getImageUrl(),
									listener, 0, 0, null, errorListener);
							VueApplication.getInstance().getRequestQueue()
									.add(imagerequestObj);
						}
						Uri screenshotUri = Uri.fromFile(f);
						imageUris.add(screenshotUri);
					}
				}
				String shareText = "Your friend "
						+ name
						+ " wants your opinion - get Vue to see the full details and help "
						+ name + " out.";
				sendIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
				sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("mailto:"));
				sendIntent.putExtra(android.content.Intent.EXTRA_TEXT,
						shareText);
				sendIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM,
						imageUris);
				String activityname = null;
				if (appNames.get(position).equals(VueConstants.GMAIL_APP_NAME)) {
					activityname = VueConstants.GMAIL_ACTIVITY_NAME;
				} else if (appNames.get(position).equals(
						VueConstants.GOOGLEPLUS_APP_NAME)) {
					activityname = VueConstants.GOOGLEPLUS_ACTIVITY_NAME;
				} else if (appNames.get(position).equals(
						VueConstants.TWITTER_APP_NAME)) {
					activityname = VueConstants.TWITTER_ACTIVITY_NAME;
				}
				sendIntent.setClassName(packageNames.get(position),
						activityname);
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						screenDialog.setCancelable(true);
						activity.startActivityForResult(sendIntent,
								VueConstants.SHARE_INTENT_REQUEST_CODE);
					}
				});
			}
		});
		t.start();
	}

	private void shareText(int position) {
		dialog.dismiss();
		shareIntentCalled = true;
		shareDialog.show();
		String shareText = "Your friend "
				+ name
				+ " wants your opinion - get Vue to see the full details and help "
				+ name + " out.";
		sendIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareText);
		String activityname = null;
		if (appNames.get(position).equals(VueConstants.TWITTER_APP_NAME)) {
			activityname = VueConstants.TWITTER_ACTIVITY_NAME;
		} else if (appNames.get(position).equals(VueConstants.GMAIL_APP_NAME)) {
			activityname = VueConstants.GMAIL_ACTIVITY_NAME;
		} else if (appNames.get(position).equals(
				VueConstants.GOOGLEPLUS_APP_NAME)) {
			activityname = VueConstants.GOOGLEPLUS_ACTIVITY_NAME;
		}
		sendIntent.setClassName(packageNames.get(position), activityname);
		screenDialog.setCancelable(true);
		activity.startActivityForResult(sendIntent,
				VueConstants.SHARE_INTENT_REQUEST_CODE);
	}

}