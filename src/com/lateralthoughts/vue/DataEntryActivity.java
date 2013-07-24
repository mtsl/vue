package com.lateralthoughts.vue;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

public class DataEntryActivity extends BaseActivity {

	public boolean misKeyboardShown = false;
	public boolean isNewActionBar = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.date_entry_main);
		getSupportActionBar().setTitle(
				getResources().getString(R.string.create_ailse_screen_title));
		Bundle b = getIntent().getExtras();
		if (b != null) {
			String imagePath = b
					.getString(VueConstants.CREATE_AISLE_CAMERA_GALLERY_IMAGE_PATH_BUNDLE_KEY);
			DataEntryFragment fragment = (DataEntryFragment) getSupportFragmentManager()
					.findFragmentById(R.id.create_aisles_view_fragment);
			fragment.setGalleryORCameraImage(imagePath);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (!isNewActionBar) {
			getSupportMenuInflater().inflate(R.menu.title_options2, menu);
		} else if (isNewActionBar) {
			getSupportMenuInflater()
					.inflate(R.menu.create_aisle_options2, menu);

		}
		getSupportActionBar().setHomeButtonEnabled(true); // Configure the
															// search
		// info and add any event listeners
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		DataEntryFragment fragment = null;
		switch (item.getItemId()) {
		case android.R.id.home:
			getSlidingMenu().toggle();
			break;
		case R.id.menu_create_aisles:
			fragment = (DataEntryFragment) getSupportFragmentManager()
					.findFragmentById(R.id.create_aisles_view_fragment);
			fragment.createAisleClickFunctionality();
			break;
		case R.id.menu_cancel:
			finish();
			break;
		case R.id.menu_share:
			fragment = (DataEntryFragment) getSupportFragmentManager()
					.findFragmentById(R.id.create_aisles_view_fragment);
			fragment.shareClickFunctionality();
			break;
		case R.id.menu_create_aisles_edit:
			fragment = (DataEntryFragment) getSupportFragmentManager()
					.findFragmentById(R.id.create_aisles_view_fragment);
			fragment.editButtonClickFunctionality();
			break;
		case R.id.menu_add_image:
			fragment = (DataEntryFragment) getSupportFragmentManager()
					.findFragmentById(R.id.create_aisles_view_fragment);
			fragment.addImageToAisleButtonClickFunctionality();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		try {
			if (requestCode == VueConstants.CREATE_AILSE_ACTIVITY_RESULT) {
				Bundle b = data.getExtras();
				if (b != null) {
					String imagePath = b
							.getString(VueConstants.CREATE_AISLE_CAMERA_GALLERY_IMAGE_PATH_BUNDLE_KEY);
					DataEntryFragment fragment = (DataEntryFragment) getSupportFragmentManager()
							.findFragmentById(R.id.create_aisles_view_fragment);
					fragment.setGalleryORCameraImage(imagePath);
				}
			} else if (requestCode == VueConstants.INVITE_FRIENDS_LOGINACTIVITY_REQUEST_CODE
					&& resultCode == VueConstants.INVITE_FRIENDS_LOGINACTIVITY_REQUEST_CODE) {
				if (data != null) {
					if (data.getStringExtra(VueConstants.INVITE_FRIENDS_LOGINACTIVITY_BUNDLE_STRING_KEY) != null) {
						mFrag.getFriendsList(data
								.getStringExtra(VueConstants.INVITE_FRIENDS_LOGINACTIVITY_BUNDLE_STRING_KEY));
					}
				}
			} else {
				Log.e("share+", "CreateAisle activity result" + requestCode
						+ resultCode);
				try {
					DataEntryFragment fragment = (DataEntryFragment) getSupportFragmentManager()
							.findFragmentById(R.id.create_aisles_view_fragment);
					if (fragment.mShare.shareIntentCalled) {
						fragment.mShare.shareIntentCalled = false;
						fragment.mShare.dismisDialog();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void showBezelMenu() {
		getSlidingMenu().toggle();
	}

	@Override
	public void onResume() {
		final View createAisleActivityRootLayout = findViewById(R.id.create_aisle_activity_root_layout);
		createAisleActivityRootLayout.getViewTreeObserver()
				.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						int heightDiff = createAisleActivityRootLayout
								.getRootView().getHeight()
								- createAisleActivityRootLayout.getHeight();
						if (heightDiff > 100) { // if more than 100 pixels, its
							// probably a keyboard...
							misKeyboardShown = true;
						} else {
							misKeyboardShown = false;
						}
					}
				});
		super.onResume();
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (getSlidingMenu().isMenuShowing()) {
				if (!mFrag.listener.onBackPressed()) {
					getSlidingMenu().toggle();
				}
			} else {
				if (!VueApplication.getInstance().mSoftKeboardIndicator) {
					super.onBackPressed();
				} else {
					VueApplication.getInstance().mSoftKeboardIndicator = false;
				}

			}
		}
		return false;

	}

}
