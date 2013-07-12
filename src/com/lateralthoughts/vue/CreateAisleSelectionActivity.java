package com.lateralthoughts.vue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import com.lateralthoughts.vue.utils.Utils;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class CreateAisleSelectionActivity extends Activity {

	RelativeLayout topLeftGreenCircle, topRightGreenCircle,
			bottomLeftGreenCircle, bottomRightGreenCircle, totalBottom;
	LinearLayout boxWithCircleLayout;
	AnimatorSet animSetXY;
	Animation anim;
	boolean fromCreateAilseScreenFlag = false;
	float screenHeight = 0, screenWidth = 0;
	String cameraImageName = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_asilse_selection);
		DisplayMetrics dm = getResources().getDisplayMetrics();
		screenHeight = dm.heightPixels;
		screenWidth = dm.widthPixels;
		Bundle b = getIntent().getExtras();
		if (b != null) {
			fromCreateAilseScreenFlag = b
					.getBoolean(VueConstants.FROMCREATEAILSESCREENFLAG);
		}
		topLeftGreenCircle = (RelativeLayout) findViewById(R.id.topleftgreencircle);
		topRightGreenCircle = (RelativeLayout) findViewById(R.id.toprightgreencircle);
		bottomLeftGreenCircle = (RelativeLayout) findViewById(R.id.bottomleftgreencircle);
		bottomRightGreenCircle = (RelativeLayout) findViewById(R.id.bottomrightgreencircle);
		totalBottom = (RelativeLayout) findViewById(R.id.totalbottom);
		boxWithCircleLayout = (LinearLayout) findViewById(R.id.boxwithcirclelayout);
		anim = (TranslateAnimation) AnimationUtils.loadAnimation(this,
				R.anim.layout_down_animation);
		anim.setDuration(600);
		List<ObjectAnimator> arrayListObjectAnimators = new ArrayList<ObjectAnimator>();
		ObjectAnimator animY = ObjectAnimator.ofFloat(topLeftGreenCircle,
				View.SCALE_X, 0, 1f);
		animY.setInterpolator(new BounceInterpolator());
		arrayListObjectAnimators.add(animY);// ArrayList of ObjectAnimators
		ObjectAnimator animY2 = ObjectAnimator.ofFloat(topLeftGreenCircle,
				View.SCALE_Y, 0, 1f);
		animY2.setInterpolator(new BounceInterpolator());
		arrayListObjectAnimators.add(animY2);// ArrayList of ObjectAnimators
		ObjectAnimator animY1 = ObjectAnimator.ofFloat(topRightGreenCircle,
				View.SCALE_X, 0, 1f);
		animY1.setInterpolator(new BounceInterpolator());
		arrayListObjectAnimators.add(animY1);
		ObjectAnimator animY3 = ObjectAnimator.ofFloat(topRightGreenCircle,
				View.SCALE_Y, 0, 1f);
		animY3.setInterpolator(new BounceInterpolator());
		arrayListObjectAnimators.add(animY3);
		ObjectAnimator animY11 = ObjectAnimator.ofFloat(bottomLeftGreenCircle,
				View.SCALE_X, 0, 1f);
		animY11.setInterpolator(new BounceInterpolator());
		arrayListObjectAnimators.add(animY11);
		ObjectAnimator animY31 = ObjectAnimator.ofFloat(bottomLeftGreenCircle,
				View.SCALE_Y, 0, 1f);
		animY31.setInterpolator(new BounceInterpolator());
		arrayListObjectAnimators.add(animY31);
		ObjectAnimator animY12 = ObjectAnimator.ofFloat(bottomRightGreenCircle,
				View.SCALE_X, 0, 1f);
		animY12.setInterpolator(new BounceInterpolator());
		arrayListObjectAnimators.add(animY12);
		ObjectAnimator animY32 = ObjectAnimator.ofFloat(bottomRightGreenCircle,
				View.SCALE_Y, 0, 1f);
		animY32.setInterpolator(new BounceInterpolator());
		arrayListObjectAnimators.add(animY32);
		ObjectAnimator animY13 = ObjectAnimator.ofFloat(totalBottom,
				View.SCALE_X, 0, 1f);
		animY13.setInterpolator(new BounceInterpolator());
		arrayListObjectAnimators.add(animY13);
		ObjectAnimator animY33 = ObjectAnimator.ofFloat(totalBottom,
				View.SCALE_Y, 0, 1f);
		animY33.setInterpolator(new BounceInterpolator());
		arrayListObjectAnimators.add(animY33);
		ObjectAnimator[] objectAnimators = arrayListObjectAnimators
				.toArray(new ObjectAnimator[arrayListObjectAnimators.size()]);
		animSetXY = new AnimatorSet();
		animSetXY.playTogether(objectAnimators);
		animSetXY.setDuration(1000);// 1sec
		List<ObjectAnimator> arrayListObjectAnimators1 = new ArrayList<ObjectAnimator>();
		ObjectAnimator animY5 = ObjectAnimator.ofFloat(boxWithCircleLayout,
				View.Y, 20, 0f);
		arrayListObjectAnimators1.add(animY5);// ArrayList of ObjectAnimators
		anim.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation arg0) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onAnimationRepeat(Animation arg0) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onAnimationEnd(Animation arg0) {
				// TODO Auto-generated method stub
				topRightGreenCircle.setVisibility(View.VISIBLE);
				topLeftGreenCircle.setVisibility(View.VISIBLE);
				bottomLeftGreenCircle.setVisibility(View.VISIBLE);
				bottomRightGreenCircle.setVisibility(View.VISIBLE);
				totalBottom.setVisibility(View.VISIBLE);
				animSetXY.start();
			}
		});
		topLeftGreenCircle.setVisibility(View.GONE);
		topRightGreenCircle.setVisibility(View.GONE);
		totalBottom.setVisibility(View.GONE);
		bottomLeftGreenCircle.setVisibility(View.GONE);
		bottomRightGreenCircle.setVisibility(View.GONE);
		boxWithCircleLayout.setVisibility(View.GONE);
		boxWithCircleLayout.setVisibility(View.VISIBLE);
		boxWithCircleLayout.startAnimation(anim);
		// Gallery....
		topLeftGreenCircle.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent i = new Intent(
						Intent.ACTION_PICK,
						android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(
						Intent.createChooser(i, "Select Picture"),
						VueConstants.SELECT_PICTURE);
			}
		});
		// Camera...
		bottomLeftGreenCircle.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				cameraImageName = Utils
						.vueAppCameraImageFileName(CreateAisleSelectionActivity.this);
				File cameraImageFile = new File(cameraImageName);
				Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
				intent.putExtra(MediaStore.EXTRA_OUTPUT,
						Uri.fromFile(cameraImageFile));
				startActivityForResult(intent, VueConstants.CAMERA_REQUEST);
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		try {
			// From Gallery...
			if (requestCode == VueConstants.SELECT_PICTURE) {
				Uri selectedImageUri = data.getData();
				// MEDIA GALLERY
				String selectedImagePath = Utils
						.getPath(selectedImageUri, this);
				Utils.saveImage(new File(selectedImagePath), screenHeight,
						screenWidth);
				if (!fromCreateAilseScreenFlag) {
					Intent intent = new Intent(this, CreateAisleActivity.class);
					Bundle b = new Bundle();
					b.putString(
							VueConstants.CREATE_AISLE_GALLERY_IMAGE_PATH_BUNDLE_KEY,
							selectedImagePath);
					intent.putExtras(b);
					startActivity(intent);
					finish();
				} else {
					Intent intent = new Intent();
					Bundle b = new Bundle();
					b.putString(
							VueConstants.CREATE_AISLE_GALLERY_IMAGE_PATH_BUNDLE_KEY,
							selectedImagePath);
					intent.putExtras(b);
					setResult(VueConstants.CREATE_AILSE_ACTIVITY_RESULT, intent);
					finish();
				}
			}
			// From Camera...
			else if (requestCode == VueConstants.CAMERA_REQUEST) {
				File cameraImageFile = new File(cameraImageName);
				if (cameraImageFile.exists()) {
					Utils.saveImage(cameraImageFile, screenHeight, screenWidth);
					if (!fromCreateAilseScreenFlag) {
						Intent intent = new Intent(this,
								CreateAisleActivity.class);
						Bundle b = new Bundle();
						b.putString(
								VueConstants.CREATE_AISLE_GALLERY_IMAGE_PATH_BUNDLE_KEY,
								cameraImageName);
						intent.putExtras(b);
						startActivity(intent);
						finish();
					} else {
						Intent intent = new Intent();
						Bundle b = new Bundle();
						b.putString(
								VueConstants.CREATE_AISLE_GALLERY_IMAGE_PATH_BUNDLE_KEY,
								cameraImageName);
						intent.putExtras(b);
						setResult(VueConstants.CREATE_AILSE_ACTIVITY_RESULT,
								intent);
						finish();
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}