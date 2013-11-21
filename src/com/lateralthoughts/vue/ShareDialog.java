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
import com.flurry.android.FlurryAgent;
import com.lateralthoughts.vue.utils.InstalledPackageRetriever;
import com.lateralthoughts.vue.utils.ShoppingApplicationDetails;
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
	private ArrayList<String> mActivityNames = new ArrayList<String>();
	private ArrayList<String> mPackageNames = new ArrayList<String>();
	private ArrayList<Drawable> mAppIcons = new ArrayList<Drawable>();
	private Intent mSendIntent;
	private Context mContext;
	private Activity mActivity;
	private InstalledPackageRetriever mShareIntentObj;
	public boolean mShareIntentCalled = false;
	private Dialog mDialog;
	private int mCurrentAislePosition;
	private ArrayList<clsShare> mImagePathArray;
	private ProgressDialog mShareDialog;
	private boolean mFromCreateAislePopupFlag = false;
	private boolean mLoadAllApplications = false;
	private VueAisleDetailsViewFragment.ShareViaVueListner mDetailsScreenShareViaVueListner;
	private DataEntryFragment.ShareViaVueListner mDataentryScreenShareViaVueListner;

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

	public void share(
			ArrayList<clsShare> imagePathArray,
			String aisleTitle,
			String name,
			int currentAislePosition,
			VueAisleDetailsViewFragment.ShareViaVueListner detailsScreenShareViaVueListner,
			DataEntryFragment.ShareViaVueListner dataentryScreenShareViaVueListner) {
		mShareIntentCalled = false;
		mDetailsScreenShareViaVueListner = detailsScreenShareViaVueListner;
		mDataentryScreenShareViaVueListner = dataentryScreenShareViaVueListner;
		this.mImagePathArray = imagePathArray;
		mCurrentAislePosition = currentAislePosition;
		prepareShareIntentData();
		openScreenDialog();
	}

	public void showAllInstalledApplications() {
		mLoadAllApplications = true;
		if (mAppNames.size() == 0) {
			prepareDisplayData(VueApplication.getInstance().mMoreInstalledApplicationDetailsList);
		}
		openScreenDialog();
	}

	public void loadShareApplications(
			ArrayList<ShoppingApplicationDetails> dataEntryShoppingApplicationsList) {
		mFromCreateAislePopupFlag = true;
		if (mAppNames.size() == 0) {
			prepareDisplayData(dataEntryShoppingApplicationsList);
		}
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
		TextView dialogtitle = (TextView) mDialog
				.findViewById(R.id.dialogtitle);
		ListView listview = (ListView) mDialog.findViewById(R.id.networklist);
		TextView okbuton = (TextView) mDialog.findViewById(R.id.shownetworkok);
		if (mFromCreateAislePopupFlag || mLoadAllApplications) {
			dialogtitle.setText("Open ...");
		}
		listview.setAdapter(new CustomAdapter());
		listview.setDivider(mContext.getResources().getDrawable(
				R.drawable.share_dialog_divider));
		listview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> adapter, View v,
					int position, long id) {
				FlurryAgent.logEvent("Ailse_share");
				if (mFromCreateAislePopupFlag) {
					CreateAisleSelectionActivity createAisleSelectionActivity = (CreateAisleSelectionActivity) mContext;
					if (createAisleSelectionActivity != null) {
						mDialog.dismiss();
						if (mPackageNames.get(position) == null) {
							if (mAppNames.get(position).equals(
									mContext.getResources().getString(
											R.string.more))) {
								// show another dialog for displaying installed
								// apps...
								ShareDialog shareDialog = new ShareDialog(
										mContext, mActivity);
								shareDialog.showAllInstalledApplications();
							} else if (mAppNames.get(position).equals(
									mContext.getResources().getString(
											R.string.browser))) {
								Utils.setLoadDataentryScreenFlag(mContext, true);
								// Load Browser...
								mContext.startActivity(new Intent(
										Intent.ACTION_VIEW, Uri
												.parse("http://www.google.com")));
							}
						} else {
							createAisleSelectionActivity
									.loadShoppingApplication(
											mActivityNames.get(position),
											mPackageNames.get(position),
											mAppNames.get(position));
						}
					}
				} else if (mLoadAllApplications) {
					mDialog.dismiss();
					CreateAisleSelectionActivity createAisleSelectionActivity = (CreateAisleSelectionActivity) mContext;
					createAisleSelectionActivity.loadShoppingApplication(
							mActivityNames.get(position),
							mPackageNames.get(position),
							mAppNames.get(position));
				} else {
					shareIntent(position);
				}
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
			if (mAppIcons.get(position) != null) {
				holderView.launcheicon
						.setImageDrawable(mAppIcons.get(position));
			} else {
				if (mAppNames.get(position).equals(
						mContext.getResources().getString(R.string.more))) {
					holderView.launcheicon
							.setImageResource(R.drawable.more_icon);
				} else if (mAppNames.get(position).equals(
						mContext.getResources().getString(R.string.browser))) {
					holderView.launcheicon
							.setImageResource(R.drawable.browser_icon);
				} else {
					holderView.launcheicon
							.setImageResource(R.drawable.vue_launcher_icon);
				}
			}
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
				String shareText = "";
				// User Aisle...
				if (mImagePathArray.get(0).isUserAisle().equals("1")) {
					shareText = mImagePathArray.get(0).getAisleOwnerName()
							+ " would like your opinion in finding "
							+ mImagePathArray.get(0).getLookingFor()
							+ ". Please help out by liking the picture you choose. Get Vue to create your own aisles and help more.";
				} else {
					shareText = VueApplication.getInstance().getmUserName()
							+ " would like you to check this aisle out on Vue - "
							+ mImagePathArray.get(0).getLookingFor() + " by "
							+ mImagePathArray.get(0).getAisleOwnerName()
							+ ". Get Vue to create your own aisles!";
				}
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
					VueConstants.INSTAGRAM_APP_NAME)) {
				shareSingleImage(position, mCurrentAislePosition);
			} else if (mAppNames.get(position).equalsIgnoreCase(
					VueConstants.TWITTER_APP_NAME)) {
				shareText(position);
			} else if (mAppNames.get(position)
					.equalsIgnoreCase(
							mContext.getApplicationContext()
									.getApplicationInfo()
									.loadLabel(mContext.getPackageManager())
									.toString())) {
				if (mImagePathArray.get(mCurrentAislePosition).getAisleId() != null
						&& mImagePathArray.get(mCurrentAislePosition)
								.getImageId() != null) {
					shareToVue(mImagePathArray.get(mCurrentAislePosition)
							.getAisleId(),
							mImagePathArray.get(mCurrentAislePosition)
									.getImageId());
				} else {
					shareSingleImage(position, mCurrentAislePosition);
				}
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
		gplusdialog.setContentView(R.layout.vue_popup);
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
			mSendIntent = new Intent(android.content.Intent.ACTION_SEND);
			mSendIntent.setType("text/plain");
			mShareIntentObj = new InstalledPackageRetriever(mContext);
			mShareIntentObj.getInstalledPackages();
			mAppNames = mShareIntentObj.getAppNames();
			mPackageNames = mShareIntentObj.getpackageNames();
			mAppIcons = mShareIntentObj.getDrawables();
		}
	}

	private void prepareDisplayData(
			ArrayList<ShoppingApplicationDetails> dataEntryShoppingApplicationsList) {
		if (dataEntryShoppingApplicationsList != null
				&& dataEntryShoppingApplicationsList.size() > 0) {
			for (int i = 0; i < dataEntryShoppingApplicationsList.size(); i++) {
				mAppNames.add(dataEntryShoppingApplicationsList.get(i)
						.getAppName());
				mPackageNames.add(dataEntryShoppingApplicationsList.get(i)
						.getPackageName());
				mAppIcons.add(dataEntryShoppingApplicationsList.get(i)
						.getAppIcon());
				mActivityNames.add(dataEntryShoppingApplicationsList.get(i)
						.getActivityName());
			}
		}
	}

	private class Holder {
		TextView network;
		ImageView launcheicon;
	}

	private void shareToVue(String aisleId, String imageId) {
		VueApplication.getInstance().mShareViaVueClickedFlag = true;
		VueApplication.getInstance().mShareViaVueClickedAisleId = aisleId;
		VueApplication.getInstance().mShareViaVueClickedImageId = imageId;
		mDialog.dismiss();
		if (mDataentryScreenShareViaVueListner != null) {
			mDataentryScreenShareViaVueListner.onAisleShareToVue();
		} else if (mDetailsScreenShareViaVueListner != null) {
			mDetailsScreenShareViaVueListner.onAisleShareToVue();
		}
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
				String shareText = "";
				// User Aisle...
				if (mImagePathArray.get(0).isUserAisle().equals("1")) {
					shareText = mImagePathArray.get(0).getAisleOwnerName()
							+ " would like your opinion in finding "
							+ mImagePathArray.get(0).getLookingFor()
							+ ". Please help out by liking the picture you choose. Get Vue to create your own aisles and help more.";
				} else {
					shareText = VueApplication.getInstance().getmUserName()
							+ " would like you to check this aisle out on Vue - "
							+ mImagePathArray.get(0).getLookingFor() + " by "
							+ mImagePathArray.get(0).getAisleOwnerName()
							+ ". Get Vue to create your own aisles!";
				}
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
				} else if (mAppNames.get(position).equals(
						VueConstants.INSTAGRAM_APP_NAME)) {
					activityname = VueConstants.INSTAGRAM_ACTIVITY_NAME;
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

	private void shareSingleImage(final int position,
			final int currentAislePosition) {
		mDialog.dismiss();
		mShareIntentCalled = true;
		mShareDialog.show();
		Thread t = new Thread(new Runnable() {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public void run() {
				Uri imageUri = null;
				if (mImagePathArray != null && mImagePathArray.size() > 0) {
					final File f = new File(mImagePathArray.get(
							currentAislePosition).getFilepath());
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
							}
						};
						if (mImagePathArray.get(currentAislePosition)
								.getImageUrl() != null) {
							ImageRequest imagerequestObj = new ImageRequest(
									mImagePathArray.get(currentAislePosition)
											.getImageUrl(), listener, 0, 0,
									null, errorListener);
							VueApplication.getInstance().getRequestQueue()
									.add(imagerequestObj);
						}
					}
					imageUri = Uri.fromFile(f);
				}
				mSendIntent.setType("image/*");
				mSendIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
				String activityname = null;
				if (mAppNames.get(position).equals(VueConstants.GMAIL_APP_NAME)) {
					activityname = VueConstants.GMAIL_ACTIVITY_NAME;
				} else if (mAppNames.get(position).equals(
						VueConstants.GOOGLEPLUS_APP_NAME)) {
					activityname = VueConstants.GOOGLEPLUS_ACTIVITY_NAME;
				} else if (mAppNames.get(position).equals(
						VueConstants.TWITTER_APP_NAME)) {
					activityname = VueConstants.TWITTER_ACTIVITY_NAME;
				} else if (mAppNames.get(position).equals(
						VueConstants.INSTAGRAM_APP_NAME)) {
					activityname = VueConstants.INSTAGRAM_ACTIVITY_NAME;
				} else if (mAppNames.get(position).equalsIgnoreCase(
						mContext.getApplicationContext().getApplicationInfo()
								.loadLabel(mContext.getPackageManager())
								.toString())) {
					activityname = VueConstants.VUE_ACTIVITY_NAME;
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
		String shareText = "";
		// User Aisle...
		if (mImagePathArray.get(0).isUserAisle().equals("1")) {
			shareText = mImagePathArray.get(0).getAisleOwnerName()
					+ " would like your opinion in finding "
					+ mImagePathArray.get(0).getLookingFor()
					+ ". Please help out by liking the picture you choose. Get Vue to create your own aisles and help more.";
		} else {
			shareText = VueApplication.getInstance().getmUserName()
					+ " would like you to check this aisle out on Vue - "
					+ mImagePathArray.get(0).getLookingFor() + " by "
					+ mImagePathArray.get(0).getAisleOwnerName()
					+ ". Get Vue to create your own aisles!";
		}
		mSendIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareText);
		String activityname = null;
		if (mAppNames.get(position).equals(VueConstants.TWITTER_APP_NAME)) {
			activityname = VueConstants.TWITTER_ACTIVITY_NAME;
		} else if (mAppNames.get(position).equals(VueConstants.GMAIL_APP_NAME)) {
			activityname = VueConstants.GMAIL_ACTIVITY_NAME;
		} else if (mAppNames.get(position).equals(
				VueConstants.GOOGLEPLUS_APP_NAME)) {
			activityname = VueConstants.GOOGLEPLUS_ACTIVITY_NAME;
		} else if (mAppNames.get(position).equals(
				VueConstants.INSTAGRAM_APP_NAME)) {
			activityname = VueConstants.INSTAGRAM_ACTIVITY_NAME;
		}
		mSendIntent.setClassName(mPackageNames.get(position), activityname);
		mScreenDialog.setCancelable(true);
		mActivity.startActivityForResult(mSendIntent,
				VueConstants.SHARE_INTENT_REQUEST_CODE);
	}

	public interface ShareViaVueClickedListner {
		public void onAisleShareToVue();
	}
}