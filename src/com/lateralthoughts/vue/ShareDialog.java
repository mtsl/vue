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
 * email,gmail,twitter,facebook and used action send intent to call the other
 * applications
 * 
 */
public class ShareDialog {

	private LayoutInflater mLayoutInflater;
	private AlertDialog.Builder mScreenDialog;
	private ArrayList<String> mAppNames = new ArrayList<String>();
	private ArrayList<String> mPackageNames = new ArrayList<String>();
	private ArrayList<Drawable> mAppIcons = new ArrayList<Drawable>();
	private Intent mSendIntent;
	private Context mContext;
	private Activity mActivity;
	private InstalledPackageRetriever mShareIntentObj;
	public boolean mShareIntentCalled = false;
	private Dialog mDialog;
	private String mName;
	private ArrayList<clsShare> mImagePathArray;
	private ProgressDialog mShareDialog;
	private static final String TAG = "ShareDialog";

	public void dismisDialog() {
		mShareDialog.dismiss();
	}

	/**
	 * 
	 * @param context
	 *            Context
	 */
	public ShareDialog(Context context, Activity activity) {
		this.mContext = context;
		this.mActivity = activity;
		mLayoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void share(ArrayList<clsShare> imagePathArray, String aisleTitle,
			String name) {
		mShareIntentCalled = false;
		this.mImagePathArray = imagePathArray;
		this.mName = name;
		prepareShareIntentData();
		openScreenDialog();
	}

	/** to show pop-up */
	private void openScreenDialog() {
		mShareDialog = ProgressDialog.show(mContext, mContext
				.getString(R.string.app_name), mContext.getResources()
				.getString(R.string.sharing_mesg), true);
		mShareDialog.dismiss();
		mDialog = new Dialog(mContext, R.style.Theme_Dialog_Translucent);
		mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mDialog.setContentView(R.layout.sharedialogue);
		ListView listview = (ListView) mDialog.findViewById(R.id.networklist);
		TextView okbuton = (TextView) mDialog.findViewById(R.id.shownetworkok);
		listview.setAdapter(new CustomAdapter());
		listview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> adapter, View v,
					int position, long id) {
				shareIntent(position);
			}
		});
		mDialog.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface arg0) {
				mShareIntentObj = null;
			}
		});
		mDialog.show();
		okbuton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mDialog.dismiss();
				InputMethodManager i1pm = (InputMethodManager) mContext
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				i1pm.hideSoftInputFromWindow(null, 0);
			}
		});
		mScreenDialog = new AlertDialog.Builder(mContext);
		mScreenDialog.setTitle(mContext.getResources().getString(
				R.string.share_via_mesg));
	}

	/**
	 * Adapter to set on popup dialogue
	 * 
	 * */
	private class CustomAdapter extends BaseAdapter {
		/**
		 * returns the count
		 * 
		 * @return int
		 */
		public int getCount() {
			return mAppNames.size();
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
				convertView = mLayoutInflater.inflate(R.layout.sharedialog_row,
						null);
				holderView.network = (TextView) convertView
						.findViewById(R.id.gmail);
				holderView.launcheicon = (ImageView) convertView
						.findViewById(R.id.launchericon);
				convertView.setTag(holderView);
			} else {
				holderView = (Holder) convertView.getTag();
			}
			holderView.launcheicon.setImageDrawable(mAppIcons.get(position));
			holderView.network.setText(mAppNames.get(position));
			return convertView;
		}
	}

	private void shareIntent(final int position) {
		mShareDialog.show();
		try {
			if (mAppNames.get(position).equalsIgnoreCase(
					VueConstants.FACEBOOK_APP_NAME)) {
				mDialog.dismiss();
				mShareDialog.dismiss();
				String shareText = "Your friend "
						+ mName
						+ " wants your opinion - get Vue to see the full details and help "
						+ mName + " out.";
				Intent i = new Intent(mContext, VueLoginActivity.class);
				Bundle b = new Bundle();
				b.putBoolean(VueConstants.CANCEL_BTN_DISABLE_FLAG, false);
				b.putString(VueConstants.FROM_INVITEFRIENDS, null);
				b.putBoolean(VueConstants.FBLOGIN_FROM_DETAILS_SHARE, true);
				b.putBoolean(VueConstants.FROM_BEZELMENU_LOGIN, false);
				b.putString(VueConstants.FBPOST_TEXT, shareText);
				b.putParcelableArrayList(VueConstants.FBPOST_IMAGEURLS,
						mImagePathArray);
				i.putExtras(b);
				mContext.startActivity(i);
			} else if (mAppNames.get(position).equalsIgnoreCase(
					VueConstants.GOOGLEPLUS_APP_NAME)) {
				shareImageAndText(position);
			} else if (mAppNames.get(position).equalsIgnoreCase(
					VueConstants.GMAIL_APP_NAME)) {
				shareImageAndText(position);
			} else if (mAppNames.get(position).equalsIgnoreCase(
					VueConstants.TWITTER_APP_NAME)) {
				shareText(position);
			}
		} catch (Exception e) {
			e.printStackTrace();
			mDialog.dismiss();
			mShareDialog.dismiss();
			showAlertMessageShareError(mAppNames.get(position), false);
		}
	}

	private void showAlertMessageShareError(String appName, boolean fberror) {
		final Dialog gplusdialog = new Dialog(mContext,
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
		if (mShareIntentObj == null) {
			mShareIntentObj = new InstalledPackageRetriever(mContext);
			mShareIntentObj.getInstalledPackages();
			mAppNames = mShareIntentObj.getAppNames();
			mPackageNames = mShareIntentObj.getpackageNames();
			mAppIcons = mShareIntentObj.getDrawables();
			mSendIntent = new Intent(android.content.Intent.ACTION_SEND);
			mSendIntent.setType("text/plain");
		}
	}

	private class Holder {
		TextView network;
		ImageView launcheicon;
	}

	private void shareImageAndText(final int position) {
		mDialog.dismiss();
		mShareIntentCalled = true;
		mShareDialog.show();
		Thread t = new Thread(new Runnable() {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public void run() {
				ArrayList<Uri> imageUris = new ArrayList<Uri>();
				if (mImagePathArray != null && mImagePathArray.size() > 0) {
					for (int i = 0; i < mImagePathArray.size(); i++) {
						final File f = new File(mImagePathArray.get(i)
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
							if (mImagePathArray.get(i).getImageUrl() != null) {
								ImageRequest imagerequestObj = new ImageRequest(
										mImagePathArray.get(i).getImageUrl(),
										listener, 0, 0, null, errorListener);
								VueApplication.getInstance().getRequestQueue()
										.add(imagerequestObj);
							}
						}
						Uri screenshotUri = Uri.fromFile(f);
						imageUris.add(screenshotUri);
					}
				}
				String shareText = "Your friend "
						+ mName
						+ " wants your opinion - get Vue to see the full details and help "
						+ mName + " out.";
				mSendIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
				mSendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("mailto:"));
				mSendIntent.putExtra(android.content.Intent.EXTRA_TEXT,
						shareText);
				mSendIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM,
						imageUris);
				String activityname = null;
				if (mAppNames.get(position).equals(VueConstants.GMAIL_APP_NAME)) {
					activityname = VueConstants.GMAIL_ACTIVITY_NAME;
				} else if (mAppNames.get(position).equals(
						VueConstants.GOOGLEPLUS_APP_NAME)) {
					activityname = VueConstants.GOOGLEPLUS_ACTIVITY_NAME;
				} else if (mAppNames.get(position).equals(
						VueConstants.TWITTER_APP_NAME)) {
					activityname = VueConstants.TWITTER_ACTIVITY_NAME;
				}
				mSendIntent.setClassName(mPackageNames.get(position),
						activityname);
				mActivity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mScreenDialog.setCancelable(true);
						mActivity.startActivityForResult(mSendIntent,
								VueConstants.SHARE_INTENT_REQUEST_CODE);
					}
				});
			}
		});
		t.start();
	}

	private void shareText(int position) {
		mDialog.dismiss();
		mShareIntentCalled = true;
		mShareDialog.show();
		String shareText = "Your friend "
				+ mName
				+ " wants your opinion - get Vue to see the full details and help "
				+ mName + " out.";
		mSendIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareText);
		String activityname = null;
		if (mAppNames.get(position).equals(VueConstants.TWITTER_APP_NAME)) {
			activityname = VueConstants.TWITTER_ACTIVITY_NAME;
		} else if (mAppNames.get(position).equals(VueConstants.GMAIL_APP_NAME)) {
			activityname = VueConstants.GMAIL_ACTIVITY_NAME;
		} else if (mAppNames.get(position).equals(
				VueConstants.GOOGLEPLUS_APP_NAME)) {
			activityname = VueConstants.GOOGLEPLUS_ACTIVITY_NAME;
		}
		mSendIntent.setClassName(mPackageNames.get(position), activityname);
		mScreenDialog.setCancelable(true);
		mActivity.startActivityForResult(mSendIntent,
				VueConstants.SHARE_INTENT_REQUEST_CODE);
	}

}