package com.lateralthoughts.vue;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lateralthoughts.vue.VueUserManager.UserUpdateCallback;
import com.lateralthoughts.vue.ui.StackViews;
import com.lateralthoughts.vue.ui.ViewInfo;
import com.lateralthoughts.vue.utils.ExceptionHandler;
import com.lateralthoughts.vue.utils.FbGPlusDetails;
import com.lateralthoughts.vue.utils.FileCache;
import com.lateralthoughts.vue.utils.Utils;

public class VueLandingPageActivity extends BaseActivity {

	private SharedPreferences mSharedPreferencesObj;
	private static final int DELAY_TIME = 500;
	public static List<FbGPlusDetails> mGooglePlusFriendsDetailsList = null;
	VueLandingAislesFragment fragment;
	public TextView mVueLandingActionbarScreenName;
	private LinearLayout mVueLandingActionbarRightLayout;
	private View mVueLandingActionbarView;
	private RelativeLayout mVueLandingActionbarAppIconLayout;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
		setContentView(R.layout.vue_landing_main);
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
						Intent intent = new Intent(VueLandingPageActivity.this,
								CreateAisleSelectionActivity.class);
						VueApplication
								.getInstance()
								.setmFromDetailsScreenToDataentryCreateAisleScreenFlag(
										false);
						intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
						startActivity(intent);
					}
				});
		mVueLandingActionbarAppIconLayout
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						getSlidingMenu().toggle();
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
		fragment = (VueLandingAislesFragment) getSupportFragmentManager()
				.findFragmentById(R.id.aisles_view_fragment);
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
		}
	}

	void handleSendText(Intent intent) {
		String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
		Log.e("VueLandingPageActivity", "Recived Text ::: " + sharedText);
		if (sharedText != null) {
			String sourceUrl = Utils.getUrlFromString(sharedText);
			if (VueApplication.getInstance()
					.ismFromDetailsScreenToDataentryCreateAisleScreenFlag()) {

				VueApplication.getInstance()
						.setmFromDetailsScreenToDataentryCreateAisleScreenFlag(
								false);
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
		}
	}

	void handleSendImage(Intent intent) {
		Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
		if (imageUri != null) {
			Log.e("CretaeAisleSelectionActivity send image", imageUri + "");
			// Update UI to reflect image being shared
			if (VueApplication.getInstance()
					.ismFromDetailsScreenToDataentryCreateAisleScreenFlag()) {

				VueApplication.getInstance()
						.setmFromDetailsScreenToDataentryCreateAisleScreenFlag(
								false);
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
		}
	}

	void handleSendMultipleImages(Intent intent) {
		ArrayList<Uri> imageUris = intent
				.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
		if (imageUris != null) {
			if (VueApplication.getInstance()
					.ismFromDetailsScreenToDataentryCreateAisleScreenFlag()) {

				VueApplication.getInstance()
						.setmFromDetailsScreenToDataentryCreateAisleScreenFlag(
								false);
				Log.e("Land", "vueland 1");
				Intent i = new Intent(this, AisleDetailsViewActivity.class);
				Bundle b = new Bundle();
				b.putParcelableArrayList(
						VueConstants.FROM_OTHER_SOURCES_IMAGE_URIS, imageUris);
				b.putBoolean(VueConstants.FROM_OTHER_SOURCES_FLAG, true);
				i.putExtras(b);
				startActivity(i);

			} else {
				Intent i = new Intent(this, DataEntryActivity.class);
				Bundle b = new Bundle();
				b.putParcelableArrayList(
						VueConstants.FROM_OTHER_SOURCES_IMAGE_URIS, imageUris);
				b.putBoolean(VueConstants.FROM_OTHER_SOURCES_FLAG, true);
				i.putExtras(b);
				startActivity(i);
			}
			// Update UI to reflect multiple images being shared
		}
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (getSlidingMenu().isMenuShowing()) {
				if (!mFrag.listener.onBackPressed()) {
					getSlidingMenu().toggle();
				}
			} else if(StackViews.getInstance().getStackCount() > 0){
				ViewInfo viewInfo = StackViews.getInstance().pull(); 
				mVueLandingActionbarScreenName
				.setText(viewInfo.mVueName);
			 VueTrendingAislesDataModel.getInstance(this).displayCategoryAisles(viewInfo.mVueName);
			} else {
				FileCache fileCache = new FileCache(
						VueApplication.getInstance());
				fileCache.clearVueAppResizedPictures();
				fileCache.clearVueAppCameraPictures();
				fileCache.clearTwoDaysOldPictures();
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
		if (fragment != null) {
			fragment.notifyAdapters();
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
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		// Get intent, action and MIME type
		String action = intent.getAction();
		String type = intent.getType();

		if (Intent.ACTION_SEND.equals(action) && type != null) {
			Log.e("CretaeAisleSelectionActivity send text", type);
			if ("text/plain".equals(type)) {
				handleSendText(intent); // Handle text being sent
				Log.e("CretaeAisleSelectionActivity send text",
						"textplain match");
			} else if (type.startsWith("image/")) {
				handleSendImage(intent); // Handle single image being sent
				Log.e("CretaeAisleSelectionActivity send text", "image match");
			}
		} else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
			if (type.startsWith("image/")) {
				handleSendMultipleImages(intent); // Handle multiple images
													// being sent
				Log.e("CretaeAisleSelectionActivity send text",
						"multiple image match");
			}
		} else {
			// Handle other intents, such as being started from the home screen
		}
	}
	
	public void showCategory(String catName){
	 mVueLandingActionbarScreenName
		.setText(catName);
	 VueTrendingAislesDataModel.getInstance(this).displayCategoryAisles(catName);
	 ViewInfo viewInfo = new ViewInfo();
	 viewInfo.mVueName = catName;
	 viewInfo.position = 0;
	 StackViews.getInstance().push(viewInfo);
		 
	}

}