package com.lateralthoughts.vue;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
<<<<<<< HEAD
import android.content.Intent;
import android.content.SharedPreferences;
=======
>>>>>>> efd3751e95a10bf84a066d422824bf7ba8554b1b
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
<<<<<<< HEAD
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
=======
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
>>>>>>> efd3751e95a10bf84a066d422824bf7ba8554b1b
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.lateralthoughts.vue.ui.ArcMenu;
import com.lateralthoughts.vue.utils.ShoppingApplicationDetails;
import com.lateralthoughts.vue.utils.Utils;

public class CreateAisleSelectionActivity extends Activity {

	private RelativeLayout mDataentryPopupMainLayout = null;
	private boolean mFromCreateAilseScreenFlag = false,
			mFromDetailsScreenFlag = false;
	private String mCameraImageName = null;
	private static final String GALLERY_ALERT_MESSAGE = "Select Picture";
	private static final String CAMERA_INTENT_NAME = "android.media.action.IMAGE_CAPTURE";
	private ArrayList<ShoppingApplicationDetails> mDataEntryShoppingApplicationsList = new ArrayList<ShoppingApplicationDetails>();
	private static final String CREATE_AISLE_POPUP = "Selection_Popup";
	public static boolean isActivityShowing = false;
	private ArcMenu mDataentryArcMenu = null;
	private static final int ANIM_DELAY = 100;
	private boolean isClickedFlag = false;
	private ShareDialog mShareDialog = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.e("CreateAisleSelectionActivity", "23neem onCreate called");
		isActivityShowing = true;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_asilse_selection);
		Bundle b = getIntent().getExtras();
		if (b != null) {
			mFromCreateAilseScreenFlag = b
					.getBoolean(VueConstants.FROMCREATEAILSESCREENFLAG);
			mFromDetailsScreenFlag = b
					.getBoolean(VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_FLAG);
		}
		mDataentryPopupMainLayout = (RelativeLayout) findViewById(R.id.dataentrypopup_mainlayout);
		mDataentryArcMenu = (ArcMenu) findViewById(R.id.dataentry_arc_menu);
		mDataentryArcMenu.initArcMenu(mDataentryArcMenu,
				VueApplication.POPUP_ITEM_DRAWABLES);
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				showPopUp();
			}
		}, ANIM_DELAY);
		mDataentryPopupMainLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (!isClickedFlag) {
					isClickedFlag = true;
					mDataentryArcMenu.mArcLayout.switchState(true);
				}
			}
		});

		if (VueApplication.getInstance().mShoppingApplicationDetailsList != null) {
			for (int i = 0; i < VueApplication.getInstance().mShoppingApplicationDetailsList
					.size(); i++) {
				ShoppingApplicationDetails shoppingApplicationDetails = new ShoppingApplicationDetails(
						VueApplication.getInstance().mShoppingApplicationDetailsList
								.get(i).getAppName(), VueApplication
								.getInstance().mShoppingApplicationDetailsList
								.get(i).getActivityName(), VueApplication
								.getInstance().mShoppingApplicationDetailsList
								.get(i).getPackageName(), VueApplication
								.getInstance().mShoppingApplicationDetailsList
								.get(i).getAppIcon());
				mDataEntryShoppingApplicationsList
						.add(shoppingApplicationDetails);

			}
		}
		// More... to show the list of installed applications in the separate
		// dialog.
		ShoppingApplicationDetails shoppingApplicationDetails = new ShoppingApplicationDetails(
				getResources().getString(R.string.more), null, null, null);
		mDataEntryShoppingApplicationsList.add(shoppingApplicationDetails);
		// Browser... To load the browser...
		ShoppingApplicationDetails shoppingApplicationDetails1 = new ShoppingApplicationDetails(
				getResources().getString(R.string.browser), null, null, null);
		mDataEntryShoppingApplicationsList.add(shoppingApplicationDetails1);

	}

	@Override
	protected void onStart() {
		FlurryAgent.onStartSession(this, Utils.FLURRY_APP_KEY);
		FlurryAgent.onPageView();
		FlurryAgent.logEvent(CREATE_AISLE_POPUP);
		super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
		FlurryAgent.onEndSession(this);

	}

	public void cameraFunctionality() {
		SharedPreferences sharedPreferences = CreateAisleSelectionActivity.this
				.getSharedPreferences(VueConstants.SHAREDPREFERENCE_NAME, 0);
		boolean flag = sharedPreferences.getBoolean("dontshowpopup", false);
<<<<<<< HEAD
		if (!flag) {
			openHintDialog("Camera", null);
		} else {
			gotoCamera();

		}
=======
		if (flag) {
			cameraIntent();
		} else {
			openHintDialog("Camera", null, null, null);
		}
	}

	private void galleryIntent() {
		FlurryAgent.logEvent("ADD_IMAGE_GALLERY");
		Intent i = new Intent(Intent.ACTION_PICK,
				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(Intent.createChooser(i, GALLERY_ALERT_MESSAGE),
				VueConstants.SELECT_PICTURE);
	}

	private void cameraIntent() {
		FlurryAgent.logEvent("ADD_IMAGE_CAMERA");
		mCameraImageName = Utils
				.vueAppCameraImageFileName(CreateAisleSelectionActivity.this);
		File cameraImageFile = new File(mCameraImageName);
		Intent intent = new Intent(CAMERA_INTENT_NAME);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(cameraImageFile));
		startActivityForResult(intent, VueConstants.CAMERA_REQUEST);
>>>>>>> efd3751e95a10bf84a066d422824bf7ba8554b1b
	}

	public void galleryFunctionality() {
		SharedPreferences sharedPreferences = CreateAisleSelectionActivity.this
				.getSharedPreferences(VueConstants.SHAREDPREFERENCE_NAME, 0);
		boolean flag = sharedPreferences.getBoolean("dontshowpopup", false);
<<<<<<< HEAD
		if (!flag) {
			openHintDialog("Gallery", null);
		} else {
			gotoGallery();

		}

=======
		if (flag) {
			galleryIntent();
		} else {
			openHintDialog("Gallery", null, null, null);
		}
>>>>>>> efd3751e95a10bf84a066d422824bf7ba8554b1b
	}

	public void moreClickFunctionality() {
		FlurryAgent.logEvent("ADD_IMAGE_MORE");
		if (mDataEntryShoppingApplicationsList != null
				&& mDataEntryShoppingApplicationsList.size() > 0) {
			if (mShareDialog == null) {
				mShareDialog = new ShareDialog(this, this);
			}
			mShareDialog
					.loadShareApplications(mDataEntryShoppingApplicationsList);
		} else {
			Toast.makeText(this, "There are no applications.",
					Toast.LENGTH_LONG).show();
			finish();
		}
	}

	@Override
	protected void onDestroy() {
		Log.e("CreateAisleSelectionActivity", "23neem onDestroye called");
		super.onDestroy();
		isActivityShowing = false;
	}

	public void loadShoppingApplication(String activityName,
			String packageName, String appName) {
		SharedPreferences sharedPreferences = CreateAisleSelectionActivity.this
				.getSharedPreferences(VueConstants.SHAREDPREFERENCE_NAME, 0);
		boolean flag = sharedPreferences.getBoolean("dontshowpopup", false);
		if (flag) {
			otherSourceIntent(activityName, packageName);
		} else {
			openHintDialog("OtherSource", appName, activityName, packageName);
		}
	}

	private void otherSourceIntent(String activityName, String packageName) {
		if (Utils.appInstalledOrNot(packageName, this)) {
			Intent shoppingAppIntent = new Intent(
					android.content.Intent.ACTION_VIEW);
			shoppingAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			Utils.setLoadDataentryScreenFlag(this, true);
			shoppingAppIntent.setClassName(packageName, activityName);
			Log.i("apname", "apname1: " + packageName);
			Log.i("apname", "apname2: " + activityName);
			finish();
			startActivity(shoppingAppIntent);
		} else {
			Toast.makeText(this, "Sorry, This application was not installed.",
					Toast.LENGTH_LONG).show();
			finish();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		try {
			Log.e("cs", "1");
			// From Gallery...
			if (requestCode == VueConstants.SELECT_PICTURE) {
				Uri selectedImageUri = data.getData();
				Log.e("cs", "2");
				// MEDIA GALLERY
				String selectedImagePath = Utils
						.getPath(selectedImageUri, this);
				Log.e("cs", "3");
				Log.e("frag", "uri..." + selectedImagePath);
				if (mFromDetailsScreenFlag) {
					Intent intent = new Intent();
					Bundle b = new Bundle();
					b.putString(
							VueConstants.CREATE_AISLE_CAMERA_GALLERY_IMAGE_PATH_BUNDLE_KEY,
							selectedImagePath);
					intent.putExtras(b);
					setResult(
							VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_ACTIVITY_RESULT,
							intent);
					finish();
				} else if (!mFromCreateAilseScreenFlag) {
					Intent intent = new Intent(this, DataEntryActivity.class);
					Bundle b = new Bundle();
					b.putString(
							VueConstants.CREATE_AISLE_CAMERA_GALLERY_IMAGE_PATH_BUNDLE_KEY,
							selectedImagePath);
					intent.putExtras(b);
					Log.e("cs", "7");
					startActivity(intent);
					finish();
				} else {
					Log.e("cs", "4");
					Intent intent = new Intent();
					Bundle b = new Bundle();
					b.putString(
							VueConstants.CREATE_AISLE_CAMERA_GALLERY_IMAGE_PATH_BUNDLE_KEY,
							selectedImagePath);
					intent.putExtras(b);
					setResult(VueConstants.CREATE_AILSE_ACTIVITY_RESULT, intent);
					finish();
				}
			}
			// From Camera...
			else if (requestCode == VueConstants.CAMERA_REQUEST) {
				File cameraImageFile = new File(mCameraImageName);
				if (cameraImageFile.exists()) {
					if (mFromDetailsScreenFlag) {
						Intent intent = new Intent();
						Bundle b = new Bundle();
						b.putString(
								VueConstants.CREATE_AISLE_CAMERA_GALLERY_IMAGE_PATH_BUNDLE_KEY,
								mCameraImageName);
						intent.putExtras(b);
						setResult(
								VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_ACTIVITY_RESULT,
								intent);
						finish();
					} else if (!mFromCreateAilseScreenFlag) {
						Intent intent = new Intent(this,
								DataEntryActivity.class);
						Bundle b = new Bundle();
						b.putString(
								VueConstants.CREATE_AISLE_CAMERA_GALLERY_IMAGE_PATH_BUNDLE_KEY,
								mCameraImageName);
						intent.putExtras(b);
						Log.e("cs", "7");
						startActivity(intent);
						finish();
					} else {
						Log.e("cs", "4");
						Intent intent = new Intent();
						Bundle b = new Bundle();
						b.putString(
								VueConstants.CREATE_AISLE_CAMERA_GALLERY_IMAGE_PATH_BUNDLE_KEY,
								mCameraImageName);
						intent.putExtras(b);
						setResult(VueConstants.CREATE_AILSE_ACTIVITY_RESULT,
								intent);
						finish();
					}
				} else {
					finish();
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			finish();
		}
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (!isClickedFlag) {
				isClickedFlag = true;
				mDataentryArcMenu.mArcLayout.switchState(true);
			}
		}
		return false;
	}

	private void showPopUp() {
		mDataentryArcMenu.mArcLayout.setVisibility(View.VISIBLE);
		mDataentryArcMenu.mArcLayout.switchState(true);
	}

	public void closeScreen() {
		finish();
	}

	public void showAlertMessageForAppInstalation(final String packageName) {
		final Dialog dialog = new Dialog(this, R.style.Theme_Dialog_Translucent);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.vue_popup);
		TextView noButton = (TextView) dialog.findViewById(R.id.nobutton);
		TextView okButton = (TextView) dialog.findViewById(R.id.okbutton);
		TextView messagetext = (TextView) dialog.findViewById(R.id.messagetext);
		messagetext.setText("Install from Play Store");
		okButton.setText("OK");
		noButton.setVisibility(View.GONE);
		okButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				dialog.dismiss();
				Intent goToMarket = new Intent(Intent.ACTION_VIEW).setData(Uri
						.parse("market://details?id=" + packageName));
				startActivity(goToMarket);
			}
		});
		dialog.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface arg0) {
				finish();
			}
		});
		dialog.show();
	}

<<<<<<< HEAD
	private void openHintDialog(final String source, String app) {
		ArrayList<String> your_array_list = new ArrayList<String>();
		if (source.equalsIgnoreCase("Gallery")) {
=======
	private void openHintDialog(final String source, String app,
			final String activityName, final String packageName) {
		final Dialog mDialog;
		mDialog = new Dialog(this, R.style.Theme_Dialog_Translucent);
		mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mDialog.setContentView(R.layout.hintdialog);
		mDialog.setCanceledOnTouchOutside(false);
		mDialog.setCancelable(false);
		TextView dialogtitle = (TextView) mDialog
				.findViewById(R.id.dialogtitle);
		ListView listview = (ListView) mDialog.findViewById(R.id.networklist);
		listview.setDivider(getResources().getDrawable(
				R.drawable.share_dialog_divider));
		TextView dontshow = (TextView) mDialog.findViewById(R.id.dontshow);
		TextView proceed = (TextView) mDialog.findViewById(R.id.proceed);
		ArrayList<String> your_array_list = new ArrayList<String>();
		if (source.equalsIgnoreCase("Gallery")) {
			dialogtitle.setText("Gallery");
>>>>>>> efd3751e95a10bf84a066d422824bf7ba8554b1b
			your_array_list.add("Go to gallery");
			your_array_list.add("Find the right image");
			your_array_list.add("Share(share icon) with vue(vue icon)");
			your_array_list.add("Comeback to vue");
		} else if (source.equalsIgnoreCase("Camera")) {
<<<<<<< HEAD
=======
			dialogtitle.setText("Camera");
>>>>>>> efd3751e95a10bf84a066d422824bf7ba8554b1b
			your_array_list.add("Go to camera");
			your_array_list.add("Take a picture");
			your_array_list.add("Come back to vue");
		} else if (source.equalsIgnoreCase("OtherSource")) {
<<<<<<< HEAD
			String temp = "Proceed to" + app;
=======
			dialogtitle.setText(app);
			String temp = "Proceed to " + app;
>>>>>>> efd3751e95a10bf84a066d422824bf7ba8554b1b
			your_array_list.add(temp);
			your_array_list.add("Select an image");
			your_array_list.add("Share(share icon) with vue(vue icon)");
			your_array_list.add("Come back to vue");
		}
<<<<<<< HEAD
		final Dialog mDialog;
		mDialog = new Dialog(this, R.style.Theme_Dialog_Translucent);
		mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mDialog.setContentView(R.layout.hintdialog);
		mDialog.setCanceledOnTouchOutside(false);
		mDialog.setCancelable(false);
		TextView dialogtitle = (TextView) mDialog
				.findViewById(R.id.dialogtitle);
		dialogtitle.setText("Hint");
		ListView listview = (ListView) mDialog.findViewById(R.id.networklist);
		listview.setDivider(getResources().getDrawable(
				R.drawable.share_dialog_divider));
		TextView dontshow = (TextView) mDialog.findViewById(R.id.dontshow);
		TextView proceed = (TextView) mDialog.findViewById(R.id.proceed);
		mDialog.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface arg0) {
				// /finish();
			}
		});
=======
>>>>>>> efd3751e95a10bf84a066d422824bf7ba8554b1b
		mDialog.show();
		dontshow.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SharedPreferences sharedPreferences = CreateAisleSelectionActivity.this
						.getSharedPreferences(
								VueConstants.SHAREDPREFERENCE_NAME, 0);
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor.putBoolean("dontshowpopup", true);
				editor.commit();
<<<<<<< HEAD

=======
				if (source.equalsIgnoreCase("Gallery")) {
					galleryIntent();
				} else if (source.equalsIgnoreCase("Camera")) {
					cameraIntent();
				} else {
					otherSourceIntent(activityName, packageName);
				}
>>>>>>> efd3751e95a10bf84a066d422824bf7ba8554b1b
			}
		});
		proceed.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (source.equalsIgnoreCase("Gallery")) {
<<<<<<< HEAD
					gotoGallery();
				} else if (source.equalsIgnoreCase("Camera"))
					finish();

			}
		});

		listview.setAdapter(new HintAdapter(your_array_list, app));
=======
					galleryIntent();
				} else if (source.equalsIgnoreCase("Camera")) {
					cameraIntent();
				} else {
					otherSourceIntent(activityName, packageName);
				}
			}
		});

		listview.setAdapter(new HintAdapter(your_array_list, source,
				activityName, packageName));
>>>>>>> efd3751e95a10bf84a066d422824bf7ba8554b1b

	}

	private class HintAdapter extends BaseAdapter {
		ArrayList<String> mHintList;
<<<<<<< HEAD
		String mAppName;

		public HintAdapter(ArrayList<String> hintList, String app) {
			mHintList = hintList;
			mAppName = app;
=======
		String mSource, mActivityName, mPackageName;

		public HintAdapter(ArrayList<String> hintList, String source,
				String activityName, String packageName) {
			mHintList = hintList;
			mSource = source;
			mActivityName = activityName;
			mPackageName = packageName;
>>>>>>> efd3751e95a10bf84a066d422824bf7ba8554b1b
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mHintList.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			Holder holder = null;
			if (convertView == null) {

				holder = new Holder();
				LayoutInflater mLayoutInflater = (LayoutInflater) CreateAisleSelectionActivity.this
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = mLayoutInflater.inflate(R.layout.hintpopup, null);
				holder.textone = (TextView) convertView
						.findViewById(R.id.gmail);
				holder.texttwo = (TextView) convertView.findViewById(R.id.vue);
				holder.imageone = (ImageView) convertView
						.findViewById(R.id.shareicon);
				holder.imagetwo = (ImageView) convertView
						.findViewById(R.id.shareicon2);
				convertView.setTag(holder);
			} else {
				holder = (Holder) convertView.getTag();
			}
			String text = mHintList.get(position);
			if (position == 0) {
				holder.textone.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
<<<<<<< HEAD
						Toast.makeText(CreateAisleSelectionActivity.this,
								"firstclick", Toast.LENGTH_SHORT).show();
=======
						if (mSource.equals("Camera")) {
							cameraIntent();
						} else if (mSource.equals("Gallery")) {
							galleryIntent();
						} else {
							otherSourceIntent(mActivityName, mPackageName);
						}
>>>>>>> efd3751e95a10bf84a066d422824bf7ba8554b1b
					}
				});
			}

			if (text.contains("share") && text.contains("with vue")) {
				holder.textone.setText("Share");
				holder.texttwo.setText("with vue");
				holder.imageone.setVisibility(View.VISIBLE);
				holder.imagetwo.setVisibility(View.VISIBLE);
				holder.texttwo.setVisibility(View.VISIBLE);
			} else {
				holder.imageone.setVisibility(View.GONE);
				holder.imagetwo.setVisibility(View.GONE);
				holder.texttwo.setVisibility(View.GONE);
				holder.textone.setText(text);
			}
			return convertView;
		}
	}

	private class Holder {
		TextView textone, texttwo;
		ImageView imageone, imagetwo;
	}

<<<<<<< HEAD
	private void gotoGallery() {
		FlurryAgent.logEvent("ADD_IMAGE_GALLERY");
		Intent i = new Intent(Intent.ACTION_PICK,
				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(Intent.createChooser(i, GALLERY_ALERT_MESSAGE),
				VueConstants.SELECT_PICTURE);
	}

	private void gotoCamera() {
		FlurryAgent.logEvent("ADD_IMAGE_CAMERA");
		mCameraImageName = Utils
				.vueAppCameraImageFileName(CreateAisleSelectionActivity.this);
		File cameraImageFile = new File(mCameraImageName);
		Intent intent = new Intent(CAMERA_INTENT_NAME);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(cameraImageFile));
		startActivityForResult(intent, VueConstants.CAMERA_REQUEST);
	}
=======
>>>>>>> efd3751e95a10bf84a066d422824bf7ba8554b1b
}
