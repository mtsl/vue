package com.lateralthoughts.vue;

import android.content.Intent;
import android.os.Bundle;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class CreateAisleActivity extends BaseActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_aisle_main);
		Bundle b = getIntent().getExtras();
		if (b != null) {
			String imagePath = b
					.getString(VueConstants.CREATE_AISLE_CAMERA_GALLERY_IMAGE_PATH_BUNDLE_KEY);
			CreateAilseFragment fragment = (CreateAilseFragment) getSupportFragmentManager()
					.findFragmentById(R.id.create_aisles_view_fragment);
			fragment.setGalleryORCameraImage(imagePath);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.title_options2, menu);
		getSupportActionBar().setHomeButtonEnabled(true);
		// Configure the search info and add any event listeners
		return super.onCreateOptionsMenu(menu);
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
					CreateAilseFragment fragment = (CreateAilseFragment) getSupportFragmentManager()
							.findFragmentById(R.id.create_aisles_view_fragment);
					fragment.setGalleryORCameraImage(imagePath);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			getSlidingMenu().toggle();
			break;
		case R.id.menu_create_aisles:
			CreateAilseFragment fragment = (CreateAilseFragment) getSupportFragmentManager()
					.findFragmentById(R.id.create_aisles_view_fragment);
			fragment.addAisleToServer();
			break;
		case R.id.menu_cancel:
			finish();
		}
		return super.onOptionsItemSelected(item);
	}
}
