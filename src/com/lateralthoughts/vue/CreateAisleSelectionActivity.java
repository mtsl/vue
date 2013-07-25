package com.lateralthoughts.vue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.lateralthoughts.vue.utils.Utils;

public class CreateAisleSelectionActivity extends Activity {

	private RelativeLayout mTopLeftGreenCircle, mTopRightGreenCircle,
			mBottomLeftGreenCircle, mBottomRightGreenCircle, mTotalBottom,
			mDataentryPopupMainLayout;
	private LinearLayout mBoxWithCircleLayout;
	private AnimatorSet mCircleAnimation;
	private Animation mTopToBottomAnimation, mBottomToTopAnimation,
			mBounceAnimation = null;
	private boolean mFromCreateAilseScreenFlag = false;
	private String mCameraImageName = null;
	private boolean mGalleryClickedFlag = false, mCameraClickedFlag = false;
	private static final int BOX_ANIMATION_DURATION = 600;
	private static final int CIRCLE_ANIMATION_DURATION = 1000;
	private static final float ZOOM_START_POSITION = 0f;
	private static final float ZOOM_END_POSITION = 1f;
	private static final float CIRCLE_SELECTION_START_POSITION = 20f;
	private static final float CIRCLE_SELECTION_END_POSITION = 0f;
	private static final String GALLERY_ALERT_MESSAGE = "Select Picture";
	private static final String CAMERA_INTENT_NAME = "android.media.action.IMAGE_CAPTURE";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_asilse_selection);
		Utils.refreshGallery(this);
		Bundle b = getIntent().getExtras();
		if (b != null) {
			mFromCreateAilseScreenFlag = b
					.getBoolean(VueConstants.FROMCREATEAILSESCREENFLAG);
		}
		mTopLeftGreenCircle = (RelativeLayout) findViewById(R.id.topleftgreencircle);
		mTopRightGreenCircle = (RelativeLayout) findViewById(R.id.toprightgreencircle);
		mBottomLeftGreenCircle = (RelativeLayout) findViewById(R.id.bottomleftgreencircle);
		mBottomRightGreenCircle = (RelativeLayout) findViewById(R.id.bottomrightgreencircle);
		mTotalBottom = (RelativeLayout) findViewById(R.id.totalbottom);
		mDataentryPopupMainLayout = (RelativeLayout) findViewById(R.id.dataentrypopup_mainlayout);
		mBoxWithCircleLayout = (LinearLayout) findViewById(R.id.boxwithcirclelayout);
		mTopToBottomAnimation = (TranslateAnimation) AnimationUtils
				.loadAnimation(this, R.anim.layout_down_animation);
		mBottomToTopAnimation = (TranslateAnimation) AnimationUtils
				.loadAnimation(this, R.anim.layout_up_animation);
		mBounceAnimation = (ScaleAnimation) AnimationUtils.loadAnimation(this,
				R.anim.buttontoucheffect);
		mBounceAnimation.setInterpolator(new BounceInterpolator());
		mTopToBottomAnimation.setDuration(BOX_ANIMATION_DURATION);
		mBottomToTopAnimation.setDuration(BOX_ANIMATION_DURATION);
		List<ObjectAnimator> arrayListObjectAnimators = new ArrayList<ObjectAnimator>();
		ObjectAnimator animY = ObjectAnimator.ofFloat(mTopLeftGreenCircle,
				View.SCALE_X, ZOOM_START_POSITION, ZOOM_END_POSITION);
		animY.setInterpolator(new BounceInterpolator());
		arrayListObjectAnimators.add(animY);// ArrayList of ObjectAnimators
		ObjectAnimator animY2 = ObjectAnimator.ofFloat(mTopLeftGreenCircle,
				View.SCALE_Y, ZOOM_START_POSITION, ZOOM_END_POSITION);
		animY2.setInterpolator(new BounceInterpolator());
		arrayListObjectAnimators.add(animY2);// ArrayList of ObjectAnimators
		ObjectAnimator animY1 = ObjectAnimator.ofFloat(mTopRightGreenCircle,
				View.SCALE_X, ZOOM_START_POSITION, ZOOM_END_POSITION);
		animY1.setInterpolator(new BounceInterpolator());
		arrayListObjectAnimators.add(animY1);
		ObjectAnimator animY3 = ObjectAnimator.ofFloat(mTopRightGreenCircle,
				View.SCALE_Y, ZOOM_START_POSITION, ZOOM_END_POSITION);
		animY3.setInterpolator(new BounceInterpolator());
		arrayListObjectAnimators.add(animY3);
		ObjectAnimator animY11 = ObjectAnimator.ofFloat(mBottomLeftGreenCircle,
				View.SCALE_X, ZOOM_START_POSITION, ZOOM_END_POSITION);
		animY11.setInterpolator(new BounceInterpolator());
		arrayListObjectAnimators.add(animY11);
		ObjectAnimator animY31 = ObjectAnimator.ofFloat(mBottomLeftGreenCircle,
				View.SCALE_Y, ZOOM_START_POSITION, ZOOM_END_POSITION);
		animY31.setInterpolator(new BounceInterpolator());
		arrayListObjectAnimators.add(animY31);
		ObjectAnimator animY12 = ObjectAnimator.ofFloat(
				mBottomRightGreenCircle, View.SCALE_X, ZOOM_START_POSITION,
				ZOOM_END_POSITION);
		animY12.setInterpolator(new BounceInterpolator());
		arrayListObjectAnimators.add(animY12);
		ObjectAnimator animY32 = ObjectAnimator.ofFloat(
				mBottomRightGreenCircle, View.SCALE_Y, ZOOM_START_POSITION,
				ZOOM_END_POSITION);
		animY32.setInterpolator(new BounceInterpolator());
		arrayListObjectAnimators.add(animY32);
		ObjectAnimator animY13 = ObjectAnimator.ofFloat(mTotalBottom,
				View.SCALE_X, ZOOM_START_POSITION, ZOOM_END_POSITION);
		animY13.setInterpolator(new BounceInterpolator());
		arrayListObjectAnimators.add(animY13);
		ObjectAnimator animY33 = ObjectAnimator.ofFloat(mTotalBottom,
				View.SCALE_Y, ZOOM_START_POSITION, ZOOM_END_POSITION);
		animY33.setInterpolator(new BounceInterpolator());
		arrayListObjectAnimators.add(animY33);
		ObjectAnimator[] objectAnimators = arrayListObjectAnimators
				.toArray(new ObjectAnimator[arrayListObjectAnimators.size()]);
		mCircleAnimation = new AnimatorSet();
		mCircleAnimation.playTogether(objectAnimators);
		mCircleAnimation.setDuration(CIRCLE_ANIMATION_DURATION);// 1sec
		List<ObjectAnimator> arrayListObjectAnimators1 = new ArrayList<ObjectAnimator>();
		ObjectAnimator animY5 = ObjectAnimator.ofFloat(mBoxWithCircleLayout,
				View.Y, CIRCLE_SELECTION_START_POSITION,
				CIRCLE_SELECTION_END_POSITION);
		arrayListObjectAnimators1.add(animY5);// ArrayList of ObjectAnimators
		mTopToBottomAnimation.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation arg0) {
			}

			@Override
			public void onAnimationRepeat(Animation arg0) {
			}

			@Override
			public void onAnimationEnd(Animation arg0) {
				mTopRightGreenCircle.setVisibility(View.VISIBLE);
				mTopLeftGreenCircle.setVisibility(View.VISIBLE);
				mBottomLeftGreenCircle.setVisibility(View.VISIBLE);
				mBottomRightGreenCircle.setVisibility(View.VISIBLE);
				mTotalBottom.setVisibility(View.VISIBLE);
				mCircleAnimation.start();
			}
		});
		mBottomToTopAnimation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation arg0) {
				mBoxWithCircleLayout.setVisibility(View.INVISIBLE);
				mTopRightGreenCircle.setVisibility(View.INVISIBLE);
				mTopLeftGreenCircle.setVisibility(View.INVISIBLE);
				mBottomLeftGreenCircle.setVisibility(View.INVISIBLE);
				mBottomRightGreenCircle.setVisibility(View.INVISIBLE);
				mTotalBottom.setVisibility(View.INVISIBLE);
			}

			@Override
			public void onAnimationRepeat(Animation arg0) {

			}

			@Override
			public void onAnimationEnd(Animation arg0) {
				if (mGalleryClickedFlag) {
					Utils.refreshGallery(CreateAisleSelectionActivity.this);
					Intent i = new Intent(
							Intent.ACTION_PICK,
							android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
					startActivityForResult(
							Intent.createChooser(i, GALLERY_ALERT_MESSAGE),
							VueConstants.SELECT_PICTURE);
				} else if (mCameraClickedFlag) {
					mCameraImageName = Utils
							.vueAppCameraImageFileName(CreateAisleSelectionActivity.this);
					File cameraImageFile = new File(mCameraImageName);
					Intent intent = new Intent(CAMERA_INTENT_NAME);
					intent.putExtra(MediaStore.EXTRA_OUTPUT,
							Uri.fromFile(cameraImageFile));
					startActivityForResult(intent, VueConstants.CAMERA_REQUEST);
				} else {
					finish();
				}
			}
		});
		mTopLeftGreenCircle.setVisibility(View.GONE);
		mTopRightGreenCircle.setVisibility(View.GONE);
		mTotalBottom.setVisibility(View.GONE);
		mBottomLeftGreenCircle.setVisibility(View.GONE);
		mBottomRightGreenCircle.setVisibility(View.GONE);
		mBoxWithCircleLayout.setVisibility(View.GONE);
		mBoxWithCircleLayout.setVisibility(View.VISIBLE);
		mBoxWithCircleLayout.startAnimation(mTopToBottomAnimation);
		mTopLeftGreenCircle.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View arg0, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					mGalleryClickedFlag = true;
					mTopLeftGreenCircle.startAnimation(mBounceAnimation);
					return false;
				}
				return false;
			}
		});
		mBounceAnimation.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation arg0) {
			}

			@Override
			public void onAnimationRepeat(Animation arg0) {
			}

			@Override
			public void onAnimationEnd(Animation arg0) {
				mBoxWithCircleLayout.startAnimation(mBottomToTopAnimation);
			}
		});
		// Camera...
		mBottomLeftGreenCircle.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					mCameraClickedFlag = true;
					mBottomLeftGreenCircle.startAnimation(mBounceAnimation);
					return false;
				}
				return false;
			}
		});
		mDataentryPopupMainLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				mBoxWithCircleLayout.startAnimation(mBottomToTopAnimation);
			}
		});
		mTopRightGreenCircle.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		try {
			// From Gallery...
			if (requestCode == VueConstants.SELECT_PICTURE) {
				Uri selectedImageUri = data.getData();
				// MEDIA GALLERY
				String selectedImagePath = Utils
						.getPath(selectedImageUri, this);
				Log.e("frag", "uri..." + selectedImagePath);
				if (!mFromCreateAilseScreenFlag) {
					Intent intent = new Intent(this, DataEntryActivity.class);
					Bundle b = new Bundle();
					b.putString(
							VueConstants.CREATE_AISLE_CAMERA_GALLERY_IMAGE_PATH_BUNDLE_KEY,
							selectedImagePath);
					intent.putExtras(b);
					startActivity(intent);
					finish();
				} else {
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
					if (!mFromCreateAilseScreenFlag) {
						Intent intent = new Intent(this,
								DataEntryActivity.class);
						Bundle b = new Bundle();
						b.putString(
								VueConstants.CREATE_AISLE_CAMERA_GALLERY_IMAGE_PATH_BUNDLE_KEY,
								mCameraImageName);
						intent.putExtras(b);
						startActivity(intent);
						finish();
					} else {
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
			mBoxWithCircleLayout.startAnimation(mBottomToTopAnimation);
		}
		return false;
	}
}
