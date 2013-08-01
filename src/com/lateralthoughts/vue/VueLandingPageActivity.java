package com.lateralthoughts.vue;

import java.util.List;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
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
		boolean isLoggedInFlag = mSharedPreferencesObj.getBoolean(
				VueConstants.VUE_LOGIN, false);
		// Application opens first time.
		if (isFirstTimeFlag) {
			SharedPreferences.Editor editor = mSharedPreferencesObj.edit();
			editor.putBoolean(VueConstants.FIRSTTIME_LOGIN_PREFRENCE_FLAG,
					false);
			editor.commit();
			showLogInDialog(false);
		}
		// Check the CreatedAisleCount and Comments count
		else {
			int createdAisleCount = mSharedPreferencesObj.getInt(
					VueConstants.CREATED_AISLE_COUNT_IN_PREFERENCE, 0);
			int commentsCount = mSharedPreferencesObj.getInt(
					VueConstants.COMMENTS_COUNT_IN_PREFERENCES, 0);

			if (createdAisleCount == VueConstants.CREATE_AISLE_LIMIT_FOR_LOGIN
					|| commentsCount == VueConstants.COMMENTS_LIMIT_FOR_LOGIN) {
				if (!isLoggedInFlag) {
					showLogInDialog(true);
				}
			}

		}
		fragment = (VueLandingAislesFragment) getSupportFragmentManager()
				.findFragmentById(
						R.id.aisles_view_fragment); 
				
			 
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
		if(fragment != null) {
			fragment.notifyAdapters();
		}
		
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				Rect rect = new Rect();
				Window window = VueLandingPageActivity.this.getWindow();
				window.getDecorView().getWindowVisibleDisplayFrame(rect);
				int statusBarHeight = rect.top;

				/*
				 * int contentViewTop=
				 * window.findViewById(Window.ID_ANDROID_CONTENT).getTop(); int
				 * titleBarHeight= contentViewTop - statusBarHeight;
				 * 
				 * int contentViewTop=
				 * window.findViewById(Window.ID_ANDROID_CONTENT).getTop(); int
				 * titleBarHeight= contentViewTop - statusBarHeight;
				 */

				VueApplication.getInstance().setmStatusBarHeight(
						statusBarHeight);

			}
		}, DELAY_TIME);
	}

	@Override
	public void onPause() {
		super.onPause();

	}

}