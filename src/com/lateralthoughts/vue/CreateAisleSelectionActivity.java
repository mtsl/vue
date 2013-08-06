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
import android.widget.Toast;
import com.lateralthoughts.vue.utils.Utils;

public class CreateAisleSelectionActivity extends Activity {

	private RelativeLayout mTopLeftGreenCircle, mTopRightGreenCircle,
			mBottomLeftGreenCircle, mBottomRightGreenCircle, mTotalBottom,
			mDataentryPopupMainLayout, mBottomTopLeftGreenCircle,
			mBottomTopRightGreenCircle, mBottomBottomLeftGreenCircle,
			mBottomBottomRightGreenCircle, mBottomTotalTop,
			mDataEntryBottomPopupMainSubLayout,
			mDataEntryTopPopupMainSubLayout;
	private LinearLayout mBoxWithCircleLayout, mBottomBoxWithCircleLayout;
	private AnimatorSet mCircleAnimation, mBottomCircleAnimation;
	private Animation mTopToBottomAnimation, mBottomToTopAnimation,
			mBottomBottomToTopAnimation, mBottomTopToBottomAnimation,
			mBounceAnimation = null;
	private boolean mFromCreateAilseScreenFlag = false,
			mFromDetailsScreenFlag = false;
	private String mCameraImageName = null;
	private boolean mGalleryClickedFlag = false, mCameraClickedFlag = false,
			mAmazonClickedFlag = false, mEtsyClickedFlag = false,
			mExtraClickedFlag = false;
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
		Bundle b = getIntent().getExtras();
		if (b != null) {
			mFromCreateAilseScreenFlag = b
					.getBoolean(VueConstants.FROMCREATEAILSESCREENFLAG);
			mFromDetailsScreenFlag = b
					.getBoolean(VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_FLAG);
		}
		mDataentryPopupMainLayout = (RelativeLayout) findViewById(R.id.dataentrypopup_mainlayout);
		mDataEntryBottomPopupMainSubLayout = (RelativeLayout) findViewById(R.id.data_entry_bottom_popup_mainsublayout);
		mDataEntryTopPopupMainSubLayout = (RelativeLayout) findViewById(R.id.data_entry_popup_mainsublayout);
		mBounceAnimation = (ScaleAnimation) AnimationUtils.loadAnimation(this,
				R.anim.buttontoucheffect);
		mBounceAnimation.setInterpolator(new BounceInterpolator());
		if (!mFromDetailsScreenFlag) {
			mDataEntryBottomPopupMainSubLayout.setVisibility(View.GONE);
			mDataEntryTopPopupMainSubLayout.setVisibility(View.VISIBLE);
			mTopLeftGreenCircle = (RelativeLayout) findViewById(R.id.topleftgreencircle);
			mTopRightGreenCircle = (RelativeLayout) findViewById(R.id.toprightgreencircle);
			mBottomLeftGreenCircle = (RelativeLayout) findViewById(R.id.bottomleftgreencircle);
			mBottomRightGreenCircle = (RelativeLayout) findViewById(R.id.bottomrightgreencircle);
			mTotalBottom = (RelativeLayout) findViewById(R.id.totalbottom);
			mBoxWithCircleLayout = (LinearLayout) findViewById(R.id.boxwithcirclelayout);
			mTopToBottomAnimation = (TranslateAnimation) AnimationUtils
					.loadAnimation(this, R.anim.layout_down_animation);
			mBottomToTopAnimation = (TranslateAnimation) AnimationUtils
					.loadAnimation(this, R.anim.layout_up_animation);
			mTopToBottomAnimation.setDuration(BOX_ANIMATION_DURATION);
			mBottomToTopAnimation.setDuration(BOX_ANIMATION_DURATION);
			List<ObjectAnimator> arrayListObjectAnimators = new ArrayList<ObjectAnimator>();
			ObjectAnimator animY = ObjectAnimator.ofFloat(mTopLeftGreenCircle,
					View.SCALE_X, ZOOM_START_POSITION, ZOOM_END_POSITION);
			animY.setInterpolator(new BounceInterpolator());
			arrayListObjectAnimators.add(animY);//
			// ArrayList of ObjectAnimators
			ObjectAnimator animY2 = ObjectAnimator.ofFloat(mTopLeftGreenCircle,
					View.SCALE_Y, ZOOM_START_POSITION, ZOOM_END_POSITION);
			animY2.setInterpolator(new BounceInterpolator());
			arrayListObjectAnimators.add(animY2);
			// ArrayList of ObjectAnimators
			ObjectAnimator animY1 = ObjectAnimator.ofFloat(
					mTopRightGreenCircle, View.SCALE_X, ZOOM_START_POSITION,
					ZOOM_END_POSITION);
			animY1.setInterpolator(new BounceInterpolator());
			arrayListObjectAnimators.add(animY1);
			ObjectAnimator animY3 = ObjectAnimator.ofFloat(
					mTopRightGreenCircle, View.SCALE_Y, ZOOM_START_POSITION,
					ZOOM_END_POSITION);
			animY3.setInterpolator(new BounceInterpolator());
			arrayListObjectAnimators.add(animY3);
			ObjectAnimator animY11 = ObjectAnimator.ofFloat(
					mBottomLeftGreenCircle, View.SCALE_X, ZOOM_START_POSITION,
					ZOOM_END_POSITION);
			animY11.setInterpolator(new BounceInterpolator());
			arrayListObjectAnimators.add(animY11);
			ObjectAnimator animY31 = ObjectAnimator.ofFloat(
					mBottomLeftGreenCircle, View.SCALE_Y, ZOOM_START_POSITION,
					ZOOM_END_POSITION);
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
			ObjectAnimator animY5 = ObjectAnimator.ofFloat(
					mBoxWithCircleLayout, View.Y,
					CIRCLE_SELECTION_START_POSITION,
					CIRCLE_SELECTION_END_POSITION);
			arrayListObjectAnimators1.add(animY5);// ArrayList of
													// ObjectAnimators
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
						galleryFunctionality();
					} else if (mCameraClickedFlag) {
						cameraFunctionality();
					} else if (mAmazonClickedFlag) {
						amazonFunctionality();
					} else if (mEtsyClickedFlag) {
						// Etsy clicked functionality.
						etsyClickFunctionality();
					} else if (mExtraClickedFlag) {
						// Extra clicked functionality
						// finish();
						// fancy
						fancyClickFunctionality();
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
			// Gallery
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
			// Etsy
			mTotalBottom.setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View arg0, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						mEtsyClickedFlag = true;
						mTotalBottom.startAnimation(mBounceAnimation);
						return false;
					}
					return false;
				}
			});
			// Extra
			mBottomRightGreenCircle.setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View arg0, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						mExtraClickedFlag = true;
						mBottomRightGreenCircle
								.startAnimation(mBounceAnimation);
						return false;
					}
					return false;
				}
			});
			// Amazon
			mTopRightGreenCircle.setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View arg0, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						mAmazonClickedFlag = true;
						mTopRightGreenCircle.startAnimation(mBounceAnimation);
						return false;
					}
					return false;
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
			}); //
			mDataentryPopupMainLayout.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					mBoxWithCircleLayout.startAnimation(mBottomToTopAnimation);
				}
			});
		} else {
			mDataEntryBottomPopupMainSubLayout.setVisibility(View.VISIBLE);
			mDataEntryTopPopupMainSubLayout.setVisibility(View.GONE);
			mBottomBoxWithCircleLayout = (LinearLayout) findViewById(R.id.bottomboxwithcirclelayout);
			mBottomTopLeftGreenCircle = (RelativeLayout) findViewById(R.id.bottom_topleftgreencircle);
			mBottomTopRightGreenCircle = (RelativeLayout) findViewById(R.id.bottom_toprightgreencircle);
			mBottomBottomLeftGreenCircle = (RelativeLayout) findViewById(R.id.bottom_bottomleftgreencircle);
			mBottomBottomRightGreenCircle = (RelativeLayout) findViewById(R.id.bottom_bottomrightgreencircle);
			mBottomTotalTop = (RelativeLayout) findViewById(R.id.bottom_total_top);
			mBottomTopToBottomAnimation = (TranslateAnimation) AnimationUtils
					.loadAnimation(this, R.anim.bottom_layout_up_anim);
			mBottomBottomToTopAnimation = (TranslateAnimation) AnimationUtils
					.loadAnimation(this, R.anim.bottom_layout_down_anim);
			mBottomTopToBottomAnimation.setDuration(BOX_ANIMATION_DURATION);
			mBottomBottomToTopAnimation.setDuration(BOX_ANIMATION_DURATION);
			List<ObjectAnimator> bottomArrayListObjectAnimators = new ArrayList<ObjectAnimator>();
			ObjectAnimator bottomAnimY = ObjectAnimator.ofFloat(
					mBottomTopLeftGreenCircle, View.SCALE_X,
					ZOOM_START_POSITION, ZOOM_END_POSITION);
			bottomAnimY.setInterpolator(new BounceInterpolator());
			bottomArrayListObjectAnimators.add(bottomAnimY);// ArrayList of
															// ObjectAnimators

			ObjectAnimator bottomAnimY2 = ObjectAnimator.ofFloat(
					mBottomTopLeftGreenCircle, View.SCALE_Y,
					ZOOM_START_POSITION, ZOOM_END_POSITION);
			bottomAnimY2.setInterpolator(new BounceInterpolator());
			bottomArrayListObjectAnimators.add(bottomAnimY2);// ArrayList of
																// ObjectAnimators

			ObjectAnimator bottomAnimY1 = ObjectAnimator.ofFloat(
					mBottomTopRightGreenCircle, View.SCALE_X,
					ZOOM_START_POSITION, ZOOM_END_POSITION);
			bottomAnimY1.setInterpolator(new BounceInterpolator());
			bottomArrayListObjectAnimators.add(bottomAnimY1);

			ObjectAnimator bottomAnimY3 = ObjectAnimator.ofFloat(
					mBottomTopRightGreenCircle, View.SCALE_Y,
					ZOOM_START_POSITION, ZOOM_END_POSITION);
			bottomAnimY3.setInterpolator(new BounceInterpolator());
			bottomArrayListObjectAnimators.add(bottomAnimY3);

			ObjectAnimator bottomAnimY11 = ObjectAnimator.ofFloat(
					mBottomBottomLeftGreenCircle, View.SCALE_X,
					ZOOM_START_POSITION, ZOOM_END_POSITION);
			bottomAnimY11.setInterpolator(new BounceInterpolator());
			bottomArrayListObjectAnimators.add(bottomAnimY11);

			ObjectAnimator bottomAnimY31 = ObjectAnimator.ofFloat(
					mBottomBottomLeftGreenCircle, View.SCALE_Y,
					ZOOM_START_POSITION, ZOOM_END_POSITION);
			bottomAnimY31.setInterpolator(new BounceInterpolator());
			bottomArrayListObjectAnimators.add(bottomAnimY31);

			ObjectAnimator bottomAnimY12 = ObjectAnimator.ofFloat(
					mBottomBottomRightGreenCircle, View.SCALE_X,
					ZOOM_START_POSITION, ZOOM_END_POSITION);
			bottomAnimY12.setInterpolator(new BounceInterpolator());
			bottomArrayListObjectAnimators.add(bottomAnimY12);

			ObjectAnimator bottomAnimY32 = ObjectAnimator.ofFloat(
					mBottomBottomRightGreenCircle, View.SCALE_Y,
					ZOOM_START_POSITION, ZOOM_END_POSITION);
			bottomAnimY32.setInterpolator(new BounceInterpolator());
			bottomArrayListObjectAnimators.add(bottomAnimY32);

			ObjectAnimator bottomAnimY13 = ObjectAnimator.ofFloat(
					mBottomTotalTop, View.SCALE_X, ZOOM_START_POSITION,
					ZOOM_END_POSITION);
			bottomAnimY13.setInterpolator(new BounceInterpolator());
			bottomArrayListObjectAnimators.add(bottomAnimY13);

			ObjectAnimator bottomAnimY33 = ObjectAnimator.ofFloat(
					mBottomTotalTop, View.SCALE_Y, ZOOM_START_POSITION,
					ZOOM_END_POSITION);
			bottomAnimY33.setInterpolator(new BounceInterpolator());
			bottomArrayListObjectAnimators.add(bottomAnimY33);

			ObjectAnimator[] bottomObjectAnimators = bottomArrayListObjectAnimators
					.toArray(new ObjectAnimator[bottomArrayListObjectAnimators
							.size()]);
			mBottomCircleAnimation = new AnimatorSet();
			mBottomCircleAnimation.playTogether(bottomObjectAnimators);
			mBottomCircleAnimation.setDuration(CIRCLE_ANIMATION_DURATION);// 1sec

			List<ObjectAnimator> bottomArrayListObjectAnimators1 = new ArrayList<ObjectAnimator>();
			ObjectAnimator bottomAnimY5 = ObjectAnimator.ofFloat(
					mBottomBoxWithCircleLayout, View.Y,
					CIRCLE_SELECTION_START_POSITION,
					CIRCLE_SELECTION_END_POSITION);
			bottomArrayListObjectAnimators1.add(bottomAnimY5);// ArrayList of
																// ObjectAnimators
			mBottomTopToBottomAnimation
					.setAnimationListener(new AnimationListener() {
						@Override
						public void onAnimationStart(Animation arg0) {
						}

						@Override
						public void onAnimationRepeat(Animation arg0) {
						}

						@Override
						public void onAnimationEnd(Animation arg0) {
							mBottomTopRightGreenCircle
									.setVisibility(View.VISIBLE);
							mBottomTopLeftGreenCircle
									.setVisibility(View.VISIBLE);
							mBottomBottomLeftGreenCircle
									.setVisibility(View.VISIBLE);
							mBottomBottomRightGreenCircle
									.setVisibility(View.VISIBLE);
							mBottomTotalTop.setVisibility(View.VISIBLE);
							mBottomCircleAnimation.start();
						}
					});
			mBottomBottomToTopAnimation
					.setAnimationListener(new AnimationListener() {

						@Override
						public void onAnimationStart(Animation arg0) {
							mBottomBoxWithCircleLayout
									.setVisibility(View.INVISIBLE);
							mBottomTopRightGreenCircle
									.setVisibility(View.INVISIBLE);
							mBottomTopLeftGreenCircle
									.setVisibility(View.INVISIBLE);
							mBottomBottomLeftGreenCircle
									.setVisibility(View.INVISIBLE);
							mBottomBottomRightGreenCircle
									.setVisibility(View.INVISIBLE);
							mBottomTotalTop.setVisibility(View.INVISIBLE);
						}

						@Override
						public void onAnimationRepeat(Animation arg0) {

						}

						@Override
						public void onAnimationEnd(Animation arg0) {
							if (mGalleryClickedFlag) {
								galleryFunctionality();
							} else if (mCameraClickedFlag) {
								cameraFunctionality();
							} else if (mAmazonClickedFlag) {
								amazonFunctionality();
							} else if (mEtsyClickedFlag) {
								// Etsy clicked functionality.
								// finish();
								etsyClickFunctionality();
							} else if (mExtraClickedFlag) {
								/*
								 * // Extra clicked functionality finish();
								 */
								// fancy
								fancyClickFunctionality();
							} else {
								finish();
							}
						}
					});
			mBottomTopLeftGreenCircle.setVisibility(View.GONE);
			mBottomTopRightGreenCircle.setVisibility(View.GONE);
			mBottomTotalTop.setVisibility(View.GONE);
			mBottomBottomLeftGreenCircle.setVisibility(View.GONE);
			mBottomBottomRightGreenCircle.setVisibility(View.GONE);
			mBottomBoxWithCircleLayout.setVisibility(View.VISIBLE);
			mBottomBoxWithCircleLayout
					.startAnimation(mBottomTopToBottomAnimation);
			// Gallery
			mBottomTopLeftGreenCircle.setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View arg0, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						mGalleryClickedFlag = true;
						mBottomTopLeftGreenCircle
								.startAnimation(mBounceAnimation);
						return false;
					}
					return false;
				}
			});
			// Etsy
			mBottomTotalTop.setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View arg0, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						mEtsyClickedFlag = true;
						mBottomTotalTop.startAnimation(mBounceAnimation);
						return false;
					}
					return false;
				}
			});
			// Extra
			mBottomBottomRightGreenCircle
					.setOnTouchListener(new OnTouchListener() {

						@Override
						public boolean onTouch(View arg0, MotionEvent event) {
							if (event.getAction() == MotionEvent.ACTION_DOWN) {
								mExtraClickedFlag = true;
								mBottomBottomRightGreenCircle
										.startAnimation(mBounceAnimation);
								return false;
							}
							return false;
						}
					});
			// Amazon
			mBottomTopRightGreenCircle
					.setOnTouchListener(new OnTouchListener() {

						@Override
						public boolean onTouch(View arg0, MotionEvent event) {
							if (event.getAction() == MotionEvent.ACTION_DOWN) {
								mAmazonClickedFlag = true;
								mBottomTopRightGreenCircle
										.startAnimation(mBounceAnimation);
								return false;
							}
							return false;
						}
					});
			// Camera...
			mBottomBottomLeftGreenCircle
					.setOnTouchListener(new OnTouchListener() {

						@Override
						public boolean onTouch(View v, MotionEvent event) {
							if (event.getAction() == MotionEvent.ACTION_DOWN) {
								mCameraClickedFlag = true;
								mBottomBottomLeftGreenCircle
										.startAnimation(mBounceAnimation);
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
					mBottomBoxWithCircleLayout
							.startAnimation(mBottomBottomToTopAnimation);
				}
			});
			mDataentryPopupMainLayout.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					mBottomBoxWithCircleLayout
							.startAnimation(mBottomBottomToTopAnimation);
				}
			});
		}
	}

	private void cameraFunctionality() {
		mCameraImageName = Utils
				.vueAppCameraImageFileName(CreateAisleSelectionActivity.this);
		File cameraImageFile = new File(mCameraImageName);
		Intent intent = new Intent(CAMERA_INTENT_NAME);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(cameraImageFile));
		startActivityForResult(intent, VueConstants.CAMERA_REQUEST);
	}

	private void galleryFunctionality() {
		Intent i = new Intent(Intent.ACTION_PICK,
				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(Intent.createChooser(i, GALLERY_ALERT_MESSAGE),
				VueConstants.SELECT_PICTURE);
	}

	private void amazonFunctionality() {
		if (Utils.appInstalledOrNot(VueConstants.AMAZON_APP_PACKAGE_NAME, this)) {

			Intent amazonIntent = new Intent(android.content.Intent.ACTION_VIEW);
			amazonIntent.setClassName(VueConstants.AMAZON_APP_PACKAGE_NAME,
					VueConstants.AMAZON_APP_ACTIVITY_NAME);
			finish();
			startActivity(amazonIntent);

		} else {
			Toast.makeText(this, "This application was not installed.",
					Toast.LENGTH_LONG).show();
			finish();
		}
	}

	private void etsyClickFunctionality() {
		if (Utils.appInstalledOrNot(VueConstants.ETSY_APP_PACKAGE_NAME, this)) {
			Intent etsyIntent = new Intent(android.content.Intent.ACTION_VIEW);
			etsyIntent.setClassName(VueConstants.ETSY_APP_PACKAGE_NAME,
					VueConstants.ETSY_APP_ACTIVITY_NAME);
			finish();
			startActivity(etsyIntent);
		} else {
			Toast.makeText(this, "This application was not installed.",
					Toast.LENGTH_LONG).show();
			finish();
		}
	}

	private void fancyClickFunctionality() {
		if (Utils.appInstalledOrNot(VueConstants.FANCY_APP_PACKAGE_NAME, this)) {
			Intent fancyIntent = new Intent(android.content.Intent.ACTION_VIEW);
			fancyIntent.setClassName(VueConstants.FANCY_APP_PACKAGE_NAME,
					VueConstants.FANCY_APP_ACTIVITY_NAME);
			finish();
			startActivity(fancyIntent);
		} else {
			Toast.makeText(this, "This application was not installed.",
					Toast.LENGTH_LONG).show();
			finish();
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		try {
			Log.e("cs", "1");
			// From Gallery...
			if (requestCode == VueConstants.SELECT_PICTURE) {
				Uri selectedImageUri = data.getData();
				Log.e("cs", "2");
				// MEDIA GALLERY
				String selectedImagePath = Utils
						.getPath(selectedImageUri, this);
				Log.e("cs", "3");
				Log.e("frag", "uri..." + selectedImagePath);
				if (mFromDetailsScreenFlag) {
					Intent intent = new Intent();
					Bundle b = new Bundle();
					b.putString(
							VueConstants.CREATE_AISLE_CAMERA_GALLERY_IMAGE_PATH_BUNDLE_KEY,
							selectedImagePath);
					intent.putExtras(b);
					setResult(
							VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_ACTIVITY_RESULT,
							intent);
					finish();
				} else if (!mFromCreateAilseScreenFlag) {
					Intent intent = new Intent(this, DataEntryActivity.class);
					Bundle b = new Bundle();
					b.putString(
							VueConstants.CREATE_AISLE_CAMERA_GALLERY_IMAGE_PATH_BUNDLE_KEY,
							selectedImagePath);
					intent.putExtras(b);
					Log.e("cs", "7");
					startActivity(intent);
					finish();
				} else {
					Log.e("cs", "4");
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
			mGalleryClickedFlag = false;
			mCameraClickedFlag = false;
			mAmazonClickedFlag = false;
			mEtsyClickedFlag = false;
			mExtraClickedFlag = false;
			if (!mFromDetailsScreenFlag) {
				mBoxWithCircleLayout.startAnimation(mBottomToTopAnimation);
			} else {
				mBottomBoxWithCircleLayout
						.startAnimation(mBottomBottomToTopAnimation);
			}
		}
		return false;
	}

}
