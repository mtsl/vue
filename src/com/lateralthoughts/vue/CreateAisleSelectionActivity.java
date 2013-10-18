package com.lateralthoughts.vue;

import java.io.File;
import java.util.ArrayList;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
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
	private ArrayList<ShoppingApplicationDetails> mDataEntryShoppingApplicationsList;
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
				if (mDataEntryShoppingApplicationsList == null) {
					mDataEntryShoppingApplicationsList = new ArrayList<ShoppingApplicationDetails>();
				}
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
		/*
		 * FlurryAgent.logEvent("ADD_IMAGE_CAMERA"); mCameraImageName = Utils
		 * .vueAppCameraImageFileName(CreateAisleSelectionActivity.this); File
		 * cameraImageFile = new File(mCameraImageName); Intent intent = new
		 * Intent(CAMERA_INTENT_NAME); intent.putExtra(MediaStore.EXTRA_OUTPUT,
		 * Uri.fromFile(cameraImageFile)); startActivityForResult(intent,
		 * VueConstants.CAMERA_REQUEST);
		 */
		Utils.showAlertMessageForBackendNotIntegrated(this, true);
	}

	public void galleryFunctionality() {
		/*
		 * FlurryAgent.logEvent("ADD_IMAGE_GALLERY"); Intent i = new
		 * Intent(Intent.ACTION_PICK,
		 * android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		 * startActivityForResult(Intent.createChooser(i,
		 * GALLERY_ALERT_MESSAGE), VueConstants.SELECT_PICTURE);
		 */
		Utils.showAlertMessageForBackendNotIntegrated(this, true);
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

	public void loadShoppingApplication(String activityName, String packageName) {
		if (Utils.appInstalledOrNot(packageName, this)) {
			Intent shoppingAppIntent = new Intent(
					android.content.Intent.ACTION_VIEW);
			Utils.setLoadDataentryScreenFlag(this, true);
			shoppingAppIntent.setClassName(packageName, activityName);
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

}
