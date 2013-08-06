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
import android.view.Window;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.lateralthoughts.vue.utils.ExceptionHandler;
import com.lateralthoughts.vue.utils.FbGPlusDetails;
import com.lateralthoughts.vue.utils.FileCache;
import com.lateralthoughts.vue.utils.Utils;

public class VueLandingPageActivity extends BaseActivity {

	private SharedPreferences mSharedPreferencesObj;
	private static final int DELAY_TIME = 500;
	public static List<FbGPlusDetails> mGooglePlusFriendsDetailsList = null;
	VueLandingAislesFragment fragment;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
		setContentView(R.layout.vue_landing_main);
		getSupportActionBar().setTitle(getString(R.string.trending));
		// Checking wheather app is opens for first time or not?
		mSharedPreferencesObj = this.getSharedPreferences(
				VueConstants.SHAREDPREFERENCE_NAME, 0);
		boolean isFirstTimeFlag = mSharedPreferencesObj.getBoolean(
				VueConstants.FIRSTTIME_LOGIN_PREFRENCE_FLAG, true);
		// Application opens first time.
		if (isFirstTimeFlag) {
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
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.title_options, menu);
		getSupportActionBar().setHomeButtonEnabled(true);
		// Configure the search info and add any event listeners
		return super.onCreateOptionsMenu(menu); // true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.menu_create_aisles:
			Intent intent = new Intent(VueLandingPageActivity.this,
					CreateAisleSelectionActivity.class);
			VueApplication.getInstance()
					.setmFromDetailsScreenToDataentryCreateAisleScreenFlag(
							false);
			intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
			startActivity(intent);
			return true;
		case android.R.id.home:
			getSlidingMenu().toggle();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (getSlidingMenu().isMenuShowing()) {
				if (!mFrag.listener.onBackPressed()) {
					getSlidingMenu().toggle();
				}
			} else {
				Log.e("Profiling",
						"Profiling Landing Activity: call to delete old images");
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

}