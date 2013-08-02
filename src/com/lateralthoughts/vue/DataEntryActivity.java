package com.lateralthoughts.vue;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

public class DataEntryActivity extends BaseActivity {

	public boolean mIsKeyboardShownFlag = false;
	public boolean mIsNewActionBarFlag = false;
	public static Context mDataEntryActivityContext = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.date_entry_main);
		mDataEntryActivityContext = this;
		getSupportActionBar().setTitle(
				getResources().getString(R.string.create_ailse_screen_title));
		Bundle b = getIntent().getExtras();
		if (b != null) {
			Log.e("cs", "30");
			String aisleImagePath = b
					.getString(VueConstants.CREATE_AISLE_CAMERA_GALLERY_IMAGE_PATH_BUNDLE_KEY);
			DataEntryFragment fragment = (DataEntryFragment) getSupportFragmentManager()
					.findFragmentById(R.id.create_aisles_view_fragment);
			fragment.mFromDetailsScreenFlag = b.getBoolean(
					VueConstants.FROM_DETAILS_SCREEN_TO_DATAENTRY_SCREEN_FLAG,
					false);
			if (fragment.mFromDetailsScreenFlag) {
				getSupportActionBar().setTitle(
						getResources().getString(
								R.string.add_imae_to_aisle_screen_title));
				fragment.mIsUserAisleFlag = b
						.getBoolean(VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_IS_USER_AISLE_FLAG);
				if (fragment.mIsUserAisleFlag) {
					fragment.mLookingForPopup.setVisibility(View.GONE);
					fragment.mLookingForBigText
							.setBackgroundColor(Color.TRANSPARENT);
					fragment.mLookingForListviewLayout.setVisibility(View.GONE);
					fragment.mLookingForBigText.setClickable(false);
					fragment.mOccassionBigText.setClickable(false);
					fragment.mCategoryIcon.setClickable(false);
					fragment.mSaySomeThingEditParent.setClickable(false);
				}
				if (b.getString(VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_LOOKINGFOR) != null) {
					fragment.mLookingForBigText
							.setText(b
									.getString(VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_LOOKINGFOR));
					fragment.mLookingForText
							.setText(b
									.getString(VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_LOOKINGFOR));
				}
				if (b.getString(VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_OCCASION) != null) {
					fragment.mOccassionBigText
							.setText(b
									.getString(VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_OCCASION));
					fragment.mOccasionText
							.setText(b
									.getString(VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_OCCASION));
				}
				if (b.getString(VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_SAYSOMETHINGABOUTAISLE) != null) {
					fragment.mSaySomethingAboutAisle
							.setText(b
									.getString(VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_SAYSOMETHINGABOUTAISLE));
				}
				if (b.getString(VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_FINDAT) != null) {
					fragment.mFindAtText
							.setText(b
									.getString(VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_FINDAT));
				}
				if (b.getString(VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_CATEGORY) != null) {
					fragment.mCategoryText
							.setText(b
									.getString(VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_CATEGORY));
				}
			}
			Log.e("cs", "32");
			fragment.setGalleryORCameraImage(aisleImagePath);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (!mIsNewActionBarFlag) {
			getSupportMenuInflater().inflate(R.menu.title_options2, menu);
		} else if (mIsNewActionBarFlag) {
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
				try {
					DataEntryFragment fragment = (DataEntryFragment) getSupportFragmentManager()
							.findFragmentById(R.id.create_aisles_view_fragment);
					if (fragment.mShare.mShareIntentCalled) {
						fragment.mShare.mShareIntentCalled = false;
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
							mIsKeyboardShownFlag = true;
						} else {
							mIsKeyboardShownFlag = false;
						}
					}
				});
		super.onResume();
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		Log.e("fff", "onkeyup");
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (getSlidingMenu().isMenuShowing()) {
				if (!mFrag.listener.onBackPressed()) {
					getSlidingMenu().toggle();
				}
			} else {
				super.onBackPressed();
			}
		}
		return false;

	}

}
