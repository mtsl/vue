package com.lateralthoughts.vue;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

public class CreateAisleActivity extends BaseActivity {

	public boolean misKeyboardShown = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_aisle_main);
		getActionBar().hide();
		Bundle b = getIntent().getExtras();
		if (b != null) {
			String imagePath = b
					.getString(VueConstants.CREATE_AISLE_CAMERA_GALLERY_IMAGE_PATH_BUNDLE_KEY);
			CreateAisleFragment fragment = (CreateAisleFragment) getSupportFragmentManager()
					.findFragmentById(R.id.create_aisles_view_fragment);
			fragment.setGalleryORCameraImage(imagePath);
		}
	}

	/*
	 * @Override public boolean onCreateOptionsMenu(Menu menu) {
	 * getSupportMenuInflater().inflate(R.menu.title_options2, menu);
	 * getSupportActionBar().setHomeButtonEnabled(true); // Configure the search
	 * info and add any event listeners return super.onCreateOptionsMenu(menu);
	 * }
	 */
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
					CreateAisleFragment fragment = (CreateAisleFragment) getSupportFragmentManager()
							.findFragmentById(R.id.create_aisles_view_fragment);
					fragment.setGalleryORCameraImage(imagePath);
				}
			} else {
				Log.e("share+", "CreateAisle activity result" + requestCode
						+ resultCode);
				try {
					CreateAisleFragment fragment = (CreateAisleFragment) getSupportFragmentManager()
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

	public void finishActivity() {
		finish();
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
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (getSlidingMenu().isMenuShowing()) {
				if (!mFrag.listener.onBackPressed()) {
					getSlidingMenu().toggle();
				}
			} else {
				if (!misKeyboardShown)
					super.onBackPressed();
			}
		}
		return false;
	}

	/*
	 * @Override public boolean onOptionsItemSelected(MenuItem item) { switch
	 * (item.getItemId()) { case android.R.id.home: getSlidingMenu().toggle();
	 * break; case R.id.menu_create_aisles: CreateAilseFragment fragment =
	 * (CreateAilseFragment) getSupportFragmentManager()
	 * .findFragmentById(R.id.create_aisles_view_fragment);
	 * fragment.addAisleToServer(); break; case R.id.menu_cancel: finish(); }
	 * return super.onOptionsItemSelected(item); }
	 */
}
