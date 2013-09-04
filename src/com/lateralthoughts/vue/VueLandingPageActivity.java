package com.lateralthoughts.vue;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
 
import com.flurry.android.FlurryAgent;
import com.lateralthoughts.vue.VueUserManager.UserUpdateCallback;
import com.lateralthoughts.vue.ui.NotifyProgress;
import com.lateralthoughts.vue.ui.StackViews;
import com.lateralthoughts.vue.ui.ViewInfo;
import com.lateralthoughts.vue.utils.ExceptionHandler;
import com.lateralthoughts.vue.utils.FbGPlusDetails;
import com.lateralthoughts.vue.utils.FileCache;
import com.lateralthoughts.vue.utils.GetOtherSourceImagesTask;
import com.lateralthoughts.vue.utils.OtherSourceImageDetails;
import com.lateralthoughts.vue.utils.Utils;

public class VueLandingPageActivity extends BaseActivity {

	private SharedPreferences mSharedPreferencesObj;
	private static final int DELAY_TIME = 500;
	public static List<FbGPlusDetails> mGooglePlusFriendsDetailsList = null;
	private VueLandingAislesFragment mFragment;
	public TextView mVueLandingActionbarScreenName;
	private LinearLayout mVueLandingActionbarRightLayout;
	private View mVueLandingActionbarView;
	private RelativeLayout mVueLandingActionbarAppIconLayout;
	private int mCurentScreenPosition;
	private ProgressBar mLoadProgress;
	private ProgressDialog mProgressDialog;
	private OtherSourcesDialog mOtherSourcesDialog = null;
	public static String mOtherSourceImagePath = null;
	public boolean mDisableOutsideClickFlag = false;
	private String mCameraImageName = null;
	private static final String CAMERA_INTENT_NAME = "android.media.action.IMAGE_CAPTURE";
	private static final String TRENDING_SCREEN_VISITORS = "Trending_Screen_Visitors";

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
		setContentView(R.layout.vue_landing_main);
		mLoadProgress = (ProgressBar) findViewById(R.id.adprogress_progressBar);
		mVueLandingActionbarView = LayoutInflater.from(this).inflate(
				R.layout.vue_landing_actionbar, null);
		mVueLandingActionbarScreenName = (TextView) mVueLandingActionbarView
				.findViewById(R.id.vue_landing_actionbar_screen_name);
		mVueLandingActionbarRightLayout = (LinearLayout) mVueLandingActionbarView
				.findViewById(R.id.vue_landing_actionbar_right_layout);
		mVueLandingActionbarAppIconLayout = (RelativeLayout) mVueLandingActionbarView
				.findViewById(R.id.vue_landing_actionbar_app_icon_layout);
		mVueLandingActionbarScreenName.setText(getResources().getString(
				R.string.trending));
		getSupportActionBar().setCustomView(mVueLandingActionbarView);
		getSupportActionBar().setDisplayShowCustomEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		mVueLandingActionbarRightLayout
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						/*
						 * if (mOtherSourceImagePath == null) { Intent intent =
						 * new Intent( VueLandingPageActivity.this,
						 * CreateAisleSelectionActivity.class); Utils.
						 * putFromDetailsScreenToDataentryCreateAisleScreenPreferenceFlag
						 * ( VueLandingPageActivity.this, false);
						 * intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP); if
						 * (!CreateAisleSelectionActivity.isActivityShowing) {
						 * CreateAisleSelectionActivity.isActivityShowing =
						 * true; startActivity(intent); } } else {
						 * showDiscardOtherAppImageDialog(); }
						 */
						showPopUp();
					}
				});
		mVueLandingActionbarAppIconLayout
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						if (!mDisableOutsideClickFlag) {
							getSlidingMenu().toggle();
						} else {
							showPopUp();
						}
					}
				});
		// Checking wheather app is opens for first time or not?
		mSharedPreferencesObj = this.getSharedPreferences(
				VueConstants.SHAREDPREFERENCE_NAME, 0);
		boolean isFirstTimeFlag = mSharedPreferencesObj.getBoolean(
				VueConstants.FIRSTTIME_LOGIN_PREFRENCE_FLAG, true);
		// Application opens first time.
		if (isFirstTimeFlag) {
			VueUserManager userManager = VueUserManager.getUserManager();
			userManager.createUnidentifiedUser(new UserUpdateCallback() {

				@Override
				public void onUserUpdated(VueUser user) {
					try {
						Utils.writeObjectToFile(VueLandingPageActivity.this,
								VueConstants.VUE_APP_USEROBJECT__FILENAME, user);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			SharedPreferences.Editor editor = mSharedPreferencesObj.edit();
			editor.putBoolean(VueConstants.FIRSTTIME_LOGIN_PREFRENCE_FLAG,
					false);
			editor.commit();
			showLogInDialog(false);
		}

		mFragment = (VueLandingAislesFragment) getSupportFragmentManager()
				.findFragmentById(R.id.aisles_view_fragment);
		ViewInfo viewInfo = new ViewInfo();
		viewInfo.mVueName = getResources().getString(R.string.trending);
		viewInfo.mPosition = 0;
		StackViews.getInstance().push(viewInfo);
		Intent intent = getIntent();
		String action = intent.getAction();
		String type = intent.getType();
		if (Intent.ACTION_SEND.equals(action) && type != null) {
			if ("text/plain".equals(type)) {
				handleSendText(intent, true);
			} else if (type.startsWith("image/")) {
				handleSendImage(intent, true);
			}
		} else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
			if (type.startsWith("image/")) {
				handleSendMultipleImages(intent, true);
			}
		}
	}
@Override
protected void onStart() {
	FlurryAgent.onStartSession(this,Utils.FLURRY_APP_KEY);
	FlurryAgent.logEvent(TRENDING_SCREEN_VISITORS);
	VueUser vueUser = null;
	try {
		  vueUser = Utils.readObjectFromFile(this, VueConstants.VUE_APP_USEROBJECT__FILENAME);
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	if(vueUser != null){
		 Map<String, String> articleParams = new HashMap<String, String>();
		 if(vueUser.getUserIdentity().equals(VueUserManager.PreferredIdentityLayer.DEVICE_ID)){
			 articleParams.put("User_Status", "Un_Registered");
		 } else {
			 articleParams.put("User_Status", "Registered");
			 if(vueUser.getUserIdentity().equals(VueUserManager.PreferredIdentityLayer.FB)){
				 articleParams.put("Registered_Source", "Registered with FB");
			 }else if(vueUser.getUserIdentity().equals(VueUserManager.PreferredIdentityLayer.GPLUS)){
				 articleParams.put("Registered_Source", "Registered with GPLUS");
			 } else   if(vueUser.getUserIdentity().equals(VueUserManager.PreferredIdentityLayer.GPLUS_FB)){
				 articleParams.put("Registered_Source", "Registered with FB and GPLUS");
			 }
			 
		 }
		 FlurryAgent.logEvent("Rigestered_Users", articleParams);
 
	}
/*	FlurryAgent.setAge(arg0);
	FlurryAgent.setGender(arg0);
	FlurryAgent.setUserId(arg0);*/
	FlurryAgent.onPageView();
	

	super.onStart();
}
@Override
protected void onStop() {
	super.onStop();
	FlurryAgent.onEndSession(this);
	
}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == VueConstants.INVITE_FRIENDS_LOGINACTIVITY_REQUEST_CODE
				&& resultCode == VueConstants.INVITE_FRIENDS_LOGINACTIVITY_REQUEST_CODE) {
			if (data != null) {
				if (data.getStringExtra(VueConstants.INVITE_FRIENDS_LOGINACTIVITY_BUNDLE_STRING_KEY) != null) {
					mFrag.getFriendsList(data
							.getStringExtra(VueConstants.INVITE_FRIENDS_LOGINACTIVITY_BUNDLE_STRING_KEY));
				}
			}
		} // From Camera...
		else if (requestCode == VueConstants.CAMERA_REQUEST) {
			File cameraImageFile = new File(mCameraImageName);
			if (cameraImageFile.exists()) {
				Intent intent = new Intent(this, DataEntryActivity.class);
				Bundle b = new Bundle();
				b.putString(
						VueConstants.CREATE_AISLE_CAMERA_GALLERY_IMAGE_PATH_BUNDLE_KEY,
						mCameraImageName);
				intent.putExtras(b);
				Log.e("cs", "7");
				startActivity(intent);
			} else {
				//finish();
			}
		}
	}

	void handleSendText(Intent intent, boolean fromOnCreateMethodFlag) {
		String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
		Log.e("VueLandingPageActivity", "Recived Text ::: " + sharedText);
		if (sharedText != null) {
			String sourceUrl = Utils.getUrlFromString(sharedText);
			if (!fromOnCreateMethodFlag) {
				if (Utils
						.getFromDetailsScreenToDataentryCreateAisleScreenPreferenceFlag(VueLandingPageActivity.this)) {

					Utils.putFromDetailsScreenToDataentryCreateAisleScreenPreferenceFlag(
							VueLandingPageActivity.this, false);
					Log.e("Land", "vueland 1");
					Intent i = new Intent(this, AisleDetailsViewActivity.class);
					Bundle b = new Bundle();
					b.putString(VueConstants.FROM_OTHER_SOURCES_URL, sourceUrl);
					b.putBoolean(VueConstants.FROM_OTHER_SOURCES_FLAG, true);
					i.putExtras(b);
					startActivity(i);

				} else {
					Intent i = new Intent(this, DataEntryActivity.class);
					Bundle b = new Bundle();
					b.putString(VueConstants.FROM_OTHER_SOURCES_URL, sourceUrl);
					b.putBoolean(VueConstants.FROM_OTHER_SOURCES_FLAG, true);
					i.putExtras(b);
					startActivity(i);
				}
			} else {
				getImagesFromUrl(sourceUrl);
			}
		}
	}

	void handleSendImage(Intent intent, boolean fromOnCreateMethodFlag) {
		Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
		if (imageUri != null) {
			if (!fromOnCreateMethodFlag) {
				Log.e("CretaeAisleSelectionActivity send image", imageUri + "");
				// Update UI to reflect image being shared
				if (Utils
						.getFromDetailsScreenToDataentryCreateAisleScreenPreferenceFlag(VueLandingPageActivity.this)) {

					Utils.putFromDetailsScreenToDataentryCreateAisleScreenPreferenceFlag(
							VueLandingPageActivity.this, false);
					Log.e("Land", "vueland 1");
					Intent i = new Intent(this, AisleDetailsViewActivity.class);
					Bundle b = new Bundle();
					ArrayList<Uri> imageUrisList = new ArrayList<Uri>();
					imageUrisList.add(imageUri);
					b.putParcelableArrayList(
							VueConstants.FROM_OTHER_SOURCES_IMAGE_URIS,
							imageUrisList);
					b.putBoolean(VueConstants.FROM_OTHER_SOURCES_FLAG, true);
					i.putExtras(b);
					startActivity(i);

				} else {
					Intent i = new Intent(this, DataEntryActivity.class);
					Bundle b = new Bundle();
					ArrayList<Uri> imageUrisList = new ArrayList<Uri>();
					imageUrisList.add(imageUri);
					b.putParcelableArrayList(
							VueConstants.FROM_OTHER_SOURCES_IMAGE_URIS,
							imageUrisList);
					b.putBoolean(VueConstants.FROM_OTHER_SOURCES_FLAG, true);
					i.putExtras(b);
					startActivity(i);
				}
			} else {
				ArrayList<Uri> imageUriList = new ArrayList<Uri>();
				imageUriList.add(imageUri);
				showOtherSourcesGridview(convertImageUrisToOtherSourceImageDetails(imageUriList));
			}
		}
	}

	void handleSendMultipleImages(Intent intent, boolean fromOnCreateMethodFlag) {
		ArrayList<Uri> imageUris = intent
				.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
		if (imageUris != null) {
			if (!fromOnCreateMethodFlag) {
				if (Utils
						.getFromDetailsScreenToDataentryCreateAisleScreenPreferenceFlag(VueLandingPageActivity.this)) {

					Utils.putFromDetailsScreenToDataentryCreateAisleScreenPreferenceFlag(
							VueLandingPageActivity.this, false);
					Log.e("Land", "vueland 1");
					Intent i = new Intent(this, AisleDetailsViewActivity.class);
					Bundle b = new Bundle();
					b.putParcelableArrayList(
							VueConstants.FROM_OTHER_SOURCES_IMAGE_URIS,
							imageUris);
					b.putBoolean(VueConstants.FROM_OTHER_SOURCES_FLAG, true);
					i.putExtras(b);
					startActivity(i);

				} else {
					Intent i = new Intent(this, DataEntryActivity.class);
					Bundle b = new Bundle();
					b.putParcelableArrayList(
							VueConstants.FROM_OTHER_SOURCES_IMAGE_URIS,
							imageUris);
					b.putBoolean(VueConstants.FROM_OTHER_SOURCES_FLAG, true);
					i.putExtras(b);
					startActivity(i);
				}
				// Update UI to reflect multiple images being shared
			} else {
				showOtherSourcesGridview(convertImageUrisToOtherSourceImageDetails(imageUris));
			}
		}
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (getSlidingMenu().isMenuShowing()) {
				if (!mFrag.listener.onBackPressed()) {
					getSlidingMenu().toggle();
				}
			} else if (StackViews.getInstance().getStackCount() > 0) {
				final ViewInfo viewInfo = StackViews.getInstance().pull();
				if (viewInfo != null) {
					if (!mVueLandingActionbarScreenName.getText().toString()
							.equalsIgnoreCase("Trending")) {
						mVueLandingActionbarScreenName
								.setText(viewInfo.mVueName);
						mCurentScreenPosition = viewInfo.mPosition;
						VueTrendingAislesDataModel.getInstance(
								VueLandingPageActivity.this)
								.displayCategoryAisles(viewInfo.mVueName,
										new ProgresStatus(), false, false);

					} else {
						mOtherSourceImagePath = null;
						super.onBackPressed();
					}
				} else {
					mOtherSourceImagePath = null;
					super.onBackPressed();
				}
			} else {
				FileCache fileCache = new FileCache(
						VueApplication.getInstance());
				fileCache.clearVueAppResizedPictures();
				fileCache.clearVueAppCameraPictures();
				fileCache.clearTwoDaysOldPictures();
				mOtherSourceImagePath = null;
				super.onBackPressed();
			}
		}
		return false;
	}

	public void showLogInDialog(boolean hideCancelButton) {
		Intent i = new Intent(this, VueLoginActivity.class);
		Bundle b = new Bundle();
		b.putBoolean(VueConstants.CANCEL_BTN_DISABLE_FLAG, hideCancelButton);
		b.putString(VueConstants.FROM_INVITEFRIENDS, null);
		b.putBoolean(VueConstants.FBLOGIN_FROM_DETAILS_SHARE, false);
		b.putBoolean(VueConstants.FROM_BEZELMENU_LOGIN, false);
		i.putExtras(b);
		startActivity(i);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mFragment != null) {
			mFragment.notifyAdapters();
		}

		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				Rect rect = new Rect();
				Window window = VueLandingPageActivity.this.getWindow();
				window.getDecorView().getWindowVisibleDisplayFrame(rect);
				int statusBarHeight = rect.top;
				VueApplication.getInstance().setmStatusBarHeight(
						statusBarHeight);

			}
		}, DELAY_TIME);
	}

	@Override
	public void onPause() {
		super.onPause();

	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		// Get intent, action and MIME type
		String action = intent.getAction();
		String type = intent.getType();

		if (Intent.ACTION_SEND.equals(action) && type != null) {
			Log.e("CretaeAisleSelectionActivity send text", type);
			if ("text/plain".equals(type)) {
				handleSendText(intent, false); // Handle text being sent
				Log.e("CretaeAisleSelectionActivity send text",
						"textplain match");
			} else if (type.startsWith("image/")) {
				handleSendImage(intent, false); // Handle single image being
												// sent
				Log.e("CretaeAisleSelectionActivity send text", "image match");
			}
		} else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
			if (type.startsWith("image/")) {
				handleSendMultipleImages(intent, false); // Handle multiple
															// images
				// being sent
				Log.e("CretaeAisleSelectionActivity send text",
						"multiple image match");
			}
		} else {
			// Handle other intents, such as being started from the home screen
		}
	}

	public void showCategory(final String catName) {
		if (mFragment == null) {
			mFragment = (VueLandingAislesFragment) getSupportFragmentManager()
					.findFragmentById(R.id.aisles_view_fragment);
		}
		if (mVueLandingActionbarScreenName.getText().toString()
				.equalsIgnoreCase(catName)) {
			return;
		}
		ViewInfo viewInfo = new ViewInfo();
		viewInfo.mVueName = mVueLandingActionbarScreenName.getText().toString();
		viewInfo.mPosition = mFragment.getListPosition();
		viewInfo.mOffset = VueTrendingAislesDataModel.getInstance(VueLandingPageActivity.this).getmOffset();
		StackViews.getInstance().push(viewInfo);
		mVueLandingActionbarScreenName.setText(catName);
		VueTrendingAislesDataModel.getInstance(VueLandingPageActivity.this)
				.displayCategoryAisles(catName, new ProgresStatus(), true,
						false);
		 FlurryAgent.logEvent(catName);

	}

	class ProgresStatus implements NotifyProgress {
		@Override
		public void showProgress() {
			mLoadProgress.setVisibility(View.VISIBLE);
		}

		@Override
		public void dismissProgress(boolean fromWhere) {
			mLoadProgress.setVisibility(View.INVISIBLE);
			if (mFragment == null) {
				mFragment = (VueLandingAislesFragment) getSupportFragmentManager()
						.findFragmentById(R.id.aisles_view_fragment);
			}
			if (fromWhere) {
				mFragment.moveListToPosition(0);
				Log.i("positonmoved", "positonmoved from serverto cur pos 0");
			} else {
				Log.i("positonmoved", "positonmoved moved to position "
						+ mCurentScreenPosition);
				mFragment.moveListToPosition(mCurentScreenPosition);
			}
		}

		@Override
		public boolean isAlreadyDownloaed(String category) {
			boolean isDowoaded = StackViews.getInstance().categoryCheck(
					category);
			Log.i("isAlredeDownloaded", "isAlredeDownloaded: " + isDowoaded);
			return isDowoaded;
		}
	}

	public void showDiscardOtherAppImageDialog() {
		final Dialog dialog = new Dialog(this, R.style.Theme_Dialog_Translucent);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.googleplusappinstallationdialog);
		final TextView noButton = (TextView) dialog.findViewById(R.id.nobutton);
		TextView yesButton = (TextView) dialog.findViewById(R.id.okbutton);
		TextView messagetext = (TextView) dialog.findViewById(R.id.messagetext);
		messagetext.setText(getResources().getString(
				R.string.discard_othersource_image_mesg));
		yesButton.setText("Yes");
		noButton.setText("No");
		yesButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mOtherSourceImagePath = null;
				dialog.dismiss();
			}
		});
		noButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.show();
	}

	public void showScreenSelectionForOtherSource(final String imagePath) {
		mOtherSourceImagePath = imagePath;
		final Dialog dialog = new Dialog(this, R.style.Theme_Dialog_Translucent);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.other_source_landing_screen_selection);
		RelativeLayout addImageToAisleLayout = (RelativeLayout) dialog
				.findViewById(R.id.landingothersourcedialogaddimagetoaisle_buttonlayout);
		RelativeLayout createAisleLayout = (RelativeLayout) dialog
				.findViewById(R.id.landingothersourcedialogcreateaisle_buttonlayout);
		addImageToAisleLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});
		createAisleLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				Intent intent = new Intent(VueLandingPageActivity.this,
						DataEntryActivity.class);
				Bundle b = new Bundle();
				b.putString(
						VueConstants.CREATE_AISLE_CAMERA_GALLERY_IMAGE_PATH_BUNDLE_KEY,
						imagePath);
				mOtherSourceImagePath = null;
				intent.putExtras(b);
				startActivity(intent);
			}
		});
		dialog.show();
	}

	public void showOtherSourcesGridview(
			ArrayList<OtherSourceImageDetails> imagesList) {
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
		if (imagesList != null && imagesList.size() > 0) {
			if (mOtherSourcesDialog == null) {
				mOtherSourcesDialog = new OtherSourcesDialog(this);
			}
			mOtherSourcesDialog.showImageDailog(imagesList, true);
		} else {
			Toast.makeText(this, "Sorry, there are no images.",
					Toast.LENGTH_LONG).show();
		}
	}

	private void getImagesFromUrl(String sourceUrl) {
		if (mProgressDialog == null) {
			mProgressDialog = ProgressDialog.show(this, "", "Please wait...");
		}
		GetOtherSourceImagesTask getImagesTask = new GetOtherSourceImagesTask(
				sourceUrl, this, true);
		getImagesTask.execute();
	}

	private ArrayList<OtherSourceImageDetails> convertImageUrisToOtherSourceImageDetails(
			ArrayList<Uri> imageUriList) {
		ArrayList<OtherSourceImageDetails> otherSourcesImageDetailsList = new ArrayList<OtherSourceImageDetails>();
		for (int i = 0; i < imageUriList.size(); i++) {
			OtherSourceImageDetails otherSourceImageDetails = new OtherSourceImageDetails(
					null, null, null, 0, 0, imageUriList.get(i), 0);
			otherSourcesImageDetailsList.add(otherSourceImageDetails);
		}
		return otherSourcesImageDetailsList;
	}

	public void showPopUp() {
		mDisableOutsideClickFlag = !mDisableOutsideClickFlag;
		if (mFragment == null) {
			mFragment = (VueLandingAislesFragment) getSupportFragmentManager()
					.findFragmentById(R.id.aisles_view_fragment);
		}
		mFragment.arcMenu.mArcLayout.setVisibility(View.VISIBLE);
		mFragment.arcMenu.mArcLayout.switchState(true);
	}

	public void loadCamera() {
		mCameraImageName = Utils.vueAppCameraImageFileName(this);
		File cameraImageFile = new File(mCameraImageName);
		Intent intent = new Intent(CAMERA_INTENT_NAME);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(cameraImageFile));
		startActivityForResult(intent, VueConstants.CAMERA_REQUEST);
	}

}