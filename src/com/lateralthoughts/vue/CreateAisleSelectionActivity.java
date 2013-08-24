package com.lateralthoughts.vue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.lateralthoughts.vue.utils.ShoppingApplicationDetails;
import com.lateralthoughts.vue.utils.Utils;

public class CreateAisleSelectionActivity extends Activity {

	private RelativeLayout mTopLeftGreenCircle, mTopRightGreenCircle,
			mBottomLeftGreenCircle, mBottomRightGreenCircle, mTotalBottom,
			mDataentryPopupMainLayout, mBottomTopLeftGreenCircle,
			mBottomTopRightGreenCircle, mBottomBottomLeftGreenCircle,
			mBottomBottomRightGreenCircle, mBottomTotalTop,
			mDataEntryBottomPopupMainSubLayout,
			mDataEntryTopPopupMainSubLayout;
	private LinearLayout mBoxWithCircleLayout, mBottomBoxWithCircleLayout,
			mDataEntryMoreTopListLayout, mDataEntryMoreBottomListLayout;
	private AnimatorSet mCircleAnimation, mBottomCircleAnimation;
	private Animation mTopToBottomAnimation, mBottomToTopAnimation,
			mBottomBottomToTopAnimation, mBottomTopToBottomAnimation,
			mBounceAnimation = null;
	private ListView mDataEntryMoreTopListview, mDataEntryMoreBottomListview;
	private TextView mTopRightText, mBottomRightText, mBottomTopRightText,
			mBottomBottomRightText;
	private boolean mFromCreateAilseScreenFlag = false,
			mFromDetailsScreenFlag = false;
	private String mCameraImageName = null;
	private boolean mGalleryClickedFlag = false, mCameraClickedFlag = false,
			mTopRightClickedFlag = false, mBottomRightClickedFlag = false,
			mMoreClickedFlag = false,
			mIsTopToBottomAnimationStartedFlag = false,
			mIsBottomToTopAnimationStartedFlag = false,
			mIsBottomTopToBottomAnimationStartedFlag = false,
			mIsBottomBottomToTopAnimationStartedFlag = false;
	private final int BOX_ANIMATION_DURATION = 600;
	private final int CIRCLE_ANIMATION_DURATION = 1000;
	private final float ZOOM_START_POSITION = 0f;
	private final float ZOOM_END_POSITION = 1f;
	private final float CIRCLE_SELECTION_START_POSITION = 20f;
	private final float CIRCLE_SELECTION_END_POSITION = 0f;
	private static final String GALLERY_ALERT_MESSAGE = "Select Picture";
	private static final String CAMERA_INTENT_NAME = "android.media.action.IMAGE_CAPTURE";
	private ArrayList<ShoppingApplicationDetails> mDataEntryShoppingApplicationsList;

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
			mDataEntryMoreTopListLayout = (LinearLayout) findViewById(R.id.data_entry_more_top_list_layout);
			mDataEntryMoreTopListLayout.setVisibility(View.GONE);
			mDataEntryBottomPopupMainSubLayout.setVisibility(View.GONE);
			mDataEntryTopPopupMainSubLayout.setVisibility(View.VISIBLE);
			mDataEntryMoreTopListview = (ListView) findViewById(R.id.data_entry_more_top_listview);
			mTopLeftGreenCircle = (RelativeLayout) findViewById(R.id.topleftgreencircle);
			mTopRightGreenCircle = (RelativeLayout) findViewById(R.id.toprightgreencircle);
			mBottomLeftGreenCircle = (RelativeLayout) findViewById(R.id.bottomleftgreencircle);
			mBottomRightGreenCircle = (RelativeLayout) findViewById(R.id.bottomrightgreencircle);
			mTotalBottom = (RelativeLayout) findViewById(R.id.totalbottom);
			mTopRightText = (TextView) findViewById(R.id.top_right_text);
			mBottomRightText = (TextView) findViewById(R.id.bottom_right_text);
			if (VueApplication.getInstance().mShoppingApplicationDetailsList != null) {
				if (VueApplication.getInstance().mShoppingApplicationDetailsList
						.size() > 0
						&& VueApplication.getInstance().mShoppingApplicationDetailsList
								.get(0).getAppName() != null) {
					mTopRightText
							.setText(VueApplication.getInstance().mShoppingApplicationDetailsList
									.get(0).getAppName());
				} else {
					mTopRightText.setText("Not found.");
					mTopRightGreenCircle.setClickable(false);
				}
				if (VueApplication.getInstance().mShoppingApplicationDetailsList
						.size() > 1
						&& VueApplication.getInstance().mShoppingApplicationDetailsList
								.get(1).getAppName() != null) {
					mBottomRightText
							.setText(VueApplication.getInstance().mShoppingApplicationDetailsList
									.get(1).getAppName());
				} else {
					mBottomRightText.setText("Not found.");
					mBottomRightGreenCircle.setClickable(false);
				}
				for (int i = 2; i < VueApplication.getInstance().mShoppingApplicationDetailsList
						.size(); i++) {
					if (mDataEntryShoppingApplicationsList == null) {
						mDataEntryShoppingApplicationsList = new ArrayList<ShoppingApplicationDetails>();
					}
					ShoppingApplicationDetails shoppingApplicationDetails = new ShoppingApplicationDetails(
							VueApplication.getInstance().mShoppingApplicationDetailsList
									.get(i).getAppName(),
							VueApplication.getInstance().mShoppingApplicationDetailsList
									.get(i).getActivityName(),
							VueApplication.getInstance().mShoppingApplicationDetailsList
									.get(i).getPackageName());
					mDataEntryShoppingApplicationsList
							.add(shoppingApplicationDetails);
				}
				if (mDataEntryShoppingApplicationsList != null
						&& mDataEntryShoppingApplicationsList.size() > 0) {
					mDataEntryMoreTopListview
							.setAdapter(new ShoppingApplicationsAdapter(this));
				}
			}
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
			mCircleAnimation.addListener(new AnimatorListener() {

				@Override
				public void onAnimationStart(Animator animation) {

				}

				@Override
				public void onAnimationRepeat(Animator animation) {

				}

				@Override
				public void onAnimationEnd(Animator animation) {
					mIsTopToBottomAnimationStartedFlag = false;
				}

				@Override
				public void onAnimationCancel(Animator animation) {

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
					try {
						if (mGalleryClickedFlag) {
							galleryFunctionality();
						} else if (mCameraClickedFlag) {
							cameraFunctionality();
						} else if (mTopRightClickedFlag) {
							loadShoppingApplication(
									VueApplication.getInstance().mShoppingApplicationDetailsList
											.get(0).getActivityName(),
									VueApplication.getInstance().mShoppingApplicationDetailsList
											.get(0).getPackageName());
						} else if (mBottomRightClickedFlag) {
							loadShoppingApplication(
									VueApplication.getInstance().mShoppingApplicationDetailsList
											.get(1).getActivityName(),
									VueApplication.getInstance().mShoppingApplicationDetailsList
											.get(1).getPackageName());
						} else {
							finish();
						}
					} catch (Exception e) {
						e.printStackTrace();
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
			if (!mIsTopToBottomAnimationStartedFlag) {
				mIsTopToBottomAnimationStartedFlag = true;
				mBoxWithCircleLayout.startAnimation(mTopToBottomAnimation);
			}
			// Gallery
			mTopLeftGreenCircle.setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View arg0, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						mDataEntryMoreTopListLayout.setVisibility(View.GONE);
						mGalleryClickedFlag = true;
						mTopLeftGreenCircle.startAnimation(mBounceAnimation);
						return false;
					}
					return false;
				}
			});
			// More Shopping Category Applications....
			mTotalBottom.setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View arg0, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						mMoreClickedFlag = true;
						return false;
					}
					return false;
				}
			});
			// Bottom Right
			mBottomRightGreenCircle.setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View arg0, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						mDataEntryMoreTopListLayout.setVisibility(View.GONE);
						mBottomRightClickedFlag = true;
						mBottomRightGreenCircle
								.startAnimation(mBounceAnimation);
						return false;
					}
					return false;
				}
			});
			// TopRight App
			mTopRightGreenCircle.setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View arg0, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						mDataEntryMoreTopListLayout.setVisibility(View.GONE);
						mTopRightClickedFlag = true;
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
						mDataEntryMoreTopListLayout.setVisibility(View.GONE);
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
					if (mGalleryClickedFlag || mCameraClickedFlag
							|| mTopRightClickedFlag || mBottomRightClickedFlag) {
						// don't do anything...
					} else if (mMoreClickedFlag) {
						moreClickFunctionality();
					} else {
						if (!mIsTopToBottomAnimationStartedFlag
								&& !mIsBottomToTopAnimationStartedFlag) {
							mDataEntryMoreTopListLayout
									.setVisibility(View.GONE);
							mIsBottomToTopAnimationStartedFlag = true;
							mBoxWithCircleLayout
									.startAnimation(mBottomToTopAnimation);
						}
					}
				}
			});

		} else {
			mDataEntryMoreBottomListLayout = (LinearLayout) findViewById(R.id.data_entry_more_bottom_list_layout);
			mDataEntryMoreBottomListLayout.setVisibility(View.GONE);
			mDataEntryBottomPopupMainSubLayout.setVisibility(View.VISIBLE);
			mDataEntryTopPopupMainSubLayout.setVisibility(View.GONE);
			mBottomBoxWithCircleLayout = (LinearLayout) findViewById(R.id.bottomboxwithcirclelayout);
			mBottomTopLeftGreenCircle = (RelativeLayout) findViewById(R.id.bottom_topleftgreencircle);
			mBottomTopRightGreenCircle = (RelativeLayout) findViewById(R.id.bottom_toprightgreencircle);
			mBottomBottomLeftGreenCircle = (RelativeLayout) findViewById(R.id.bottom_bottomleftgreencircle);
			mBottomBottomRightGreenCircle = (RelativeLayout) findViewById(R.id.bottom_bottomrightgreencircle);
			mBottomTotalTop = (RelativeLayout) findViewById(R.id.bottom_total_top);
			mBottomTopRightText = (TextView) findViewById(R.id.bottom_topright_text);
			mBottomBottomRightText = (TextView) findViewById(R.id.bottom_bottom_right_text);
			mDataEntryMoreBottomListview = (ListView) findViewById(R.id.data_entry_more_bottom_listview);
			if (VueApplication.getInstance().mShoppingApplicationDetailsList != null) {
				if (VueApplication.getInstance().mShoppingApplicationDetailsList
						.size() > 0
						&& VueApplication.getInstance().mShoppingApplicationDetailsList
								.get(0).getAppName() != null) {
					mBottomTopRightText
							.setText(VueApplication.getInstance().mShoppingApplicationDetailsList
									.get(0).getAppName());
				} else {
					mBottomTopRightText.setText("Not found.");
					mBottomTopRightGreenCircle.setClickable(false);
				}
				if (VueApplication.getInstance().mShoppingApplicationDetailsList
						.size() > 1
						&& VueApplication.getInstance().mShoppingApplicationDetailsList
								.get(1).getAppName() != null) {
					mBottomBottomRightText
							.setText(VueApplication.getInstance().mShoppingApplicationDetailsList
									.get(1).getAppName());
				} else {
					mBottomBottomRightText.setText("Not found.");
					mBottomBottomRightGreenCircle.setClickable(false);
				}
				for (int i = 2; i < VueApplication.getInstance().mShoppingApplicationDetailsList
						.size(); i++) {
					if (mDataEntryShoppingApplicationsList == null) {
						mDataEntryShoppingApplicationsList = new ArrayList<ShoppingApplicationDetails>();
					}
					ShoppingApplicationDetails shoppingApplicationDetails = new ShoppingApplicationDetails(
							VueApplication.getInstance().mShoppingApplicationDetailsList
									.get(i).getAppName(),
							VueApplication.getInstance().mShoppingApplicationDetailsList
									.get(i).getActivityName(),
							VueApplication.getInstance().mShoppingApplicationDetailsList
									.get(i).getPackageName());
					mDataEntryShoppingApplicationsList
							.add(shoppingApplicationDetails);
				}
				if (mDataEntryShoppingApplicationsList != null
						&& mDataEntryShoppingApplicationsList.size() > 0) {
					mDataEntryMoreBottomListview
							.setAdapter(new ShoppingApplicationsAdapter(this));
				}
			}
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
			mBottomCircleAnimation.addListener(new AnimatorListener() {

				@Override
				public void onAnimationStart(Animator animation) {

				}

				@Override
				public void onAnimationRepeat(Animator animation) {

				}

				@Override
				public void onAnimationEnd(Animator animation) {
					mIsBottomTopToBottomAnimationStartedFlag = false;
				}

				@Override
				public void onAnimationCancel(Animator animation) {

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
							try {
								if (mGalleryClickedFlag) {
									galleryFunctionality();
								} else if (mCameraClickedFlag) {
									cameraFunctionality();
								} else if (mTopRightClickedFlag) {
									loadShoppingApplication(
											VueApplication.getInstance().mShoppingApplicationDetailsList
													.get(0).getActivityName(),
											VueApplication.getInstance().mShoppingApplicationDetailsList
													.get(0).getPackageName());
								} else if (mBottomRightClickedFlag) {
									loadShoppingApplication(
											VueApplication.getInstance().mShoppingApplicationDetailsList
													.get(1).getActivityName(),
											VueApplication.getInstance().mShoppingApplicationDetailsList
													.get(1).getPackageName());
								} else {
									finish();
								}
							} catch (Exception e) {
								e.printStackTrace();
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
			if (!mIsBottomTopToBottomAnimationStartedFlag) {
				mIsBottomTopToBottomAnimationStartedFlag = true;
				mBottomBoxWithCircleLayout
						.startAnimation(mBottomTopToBottomAnimation);
			}
			// Gallery
			mBottomTopLeftGreenCircle.setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View arg0, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						mDataEntryMoreBottomListLayout.setVisibility(View.GONE);
						mGalleryClickedFlag = true;
						mBottomTopLeftGreenCircle
								.startAnimation(mBounceAnimation);
						return false;
					}
					return false;
				}
			});
			// More...
			mBottomTotalTop.setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View arg0, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						mMoreClickedFlag = true;
						return false;
					}
					return false;
				}
			});
			// Bottom Right
			mBottomBottomRightGreenCircle
					.setOnTouchListener(new OnTouchListener() {

						@Override
						public boolean onTouch(View arg0, MotionEvent event) {
							if (event.getAction() == MotionEvent.ACTION_DOWN) {
								mDataEntryMoreBottomListLayout
										.setVisibility(View.GONE);
								mBottomRightClickedFlag = true;
								mBottomBottomRightGreenCircle
										.startAnimation(mBounceAnimation);
								return false;
							}
							return false;
						}
					});
			// Top Right
			mBottomTopRightGreenCircle
					.setOnTouchListener(new OnTouchListener() {

						@Override
						public boolean onTouch(View arg0, MotionEvent event) {
							if (event.getAction() == MotionEvent.ACTION_DOWN) {
								mDataEntryMoreBottomListLayout
										.setVisibility(View.GONE);
								mTopRightClickedFlag = true;
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
								mDataEntryMoreBottomListLayout
										.setVisibility(View.GONE);
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
					if (mGalleryClickedFlag || mCameraClickedFlag
							|| mTopRightClickedFlag || mBottomRightClickedFlag
							) {
						// don't do anything...
					}  else if (mMoreClickedFlag) {
						moreClickFunctionality();
					} else {
						if (!mIsBottomTopToBottomAnimationStartedFlag
								&& !mIsBottomBottomToTopAnimationStartedFlag) {
							mDataEntryMoreBottomListLayout
									.setVisibility(View.GONE);
							mIsBottomBottomToTopAnimationStartedFlag = true;
							mBottomBoxWithCircleLayout
									.startAnimation(mBottomBottomToTopAnimation);
						}
					}
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

	private void moreClickFunctionality() {
		if (mDataEntryShoppingApplicationsList != null
				&& mDataEntryShoppingApplicationsList.size() > 0) {
			if (mDataEntryMoreTopListLayout != null) {
				mDataEntryMoreTopListLayout.setVisibility(View.VISIBLE);
			} else if (mDataEntryMoreBottomListLayout != null) {
				mDataEntryMoreBottomListLayout.setVisibility(View.VISIBLE);
			}
		} else {
			Toast.makeText(this, "There are no applications.",
					Toast.LENGTH_LONG).show();
			finish();
		}
		mMoreClickedFlag = false;
	}

	private void loadShoppingApplication(String activityName, String packageName) {
		if (Utils.appInstalledOrNot(packageName, this)) {
			Intent shoppingAppIntent = new Intent(
					android.content.Intent.ACTION_VIEW);
			shoppingAppIntent.setClassName(packageName, activityName);
			finish();
			startActivity(shoppingAppIntent);
		} else {
			Toast.makeText(this, "Sorry, This application was not installed.",
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
					if (mFromDetailsScreenFlag) {
						Intent intent = new Intent();
						Bundle b = new Bundle();
						b.putString(
								VueConstants.CREATE_AISLE_CAMERA_GALLERY_IMAGE_PATH_BUNDLE_KEY,
								mCameraImageName);
						intent.putExtras(b);
						setResult(
								VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_ACTIVITY_RESULT,
								intent);
						finish();
					} else if (!mFromCreateAilseScreenFlag) {
						Intent intent = new Intent(this,
								DataEntryActivity.class);
						Bundle b = new Bundle();
						b.putString(
								VueConstants.CREATE_AISLE_CAMERA_GALLERY_IMAGE_PATH_BUNDLE_KEY,
								mCameraImageName);
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

	// Shopping Category Applications List ....
	private class ShoppingApplicationsAdapter extends BaseAdapter {
		Activity context;

		public ShoppingApplicationsAdapter(Activity context) {
			super();
			this.context = context;
		}

		class ViewHolder {
			TextView dataentryitemname;
		}

		public View getView(final int position, View convertView,
				ViewGroup parent) {
			ViewHolder holder = null;
			View rowView = convertView;
			if (rowView == null) {
				LayoutInflater inflater = context.getLayoutInflater();
				rowView = inflater.inflate(R.layout.dataentry_row, null, true);
				holder = new ViewHolder();
				holder.dataentryitemname = (TextView) rowView
						.findViewById(R.id.dataentryitemname);
				rowView.setTag(holder);
			} else {
				holder = (ViewHolder) rowView.getTag();
			}
			holder.dataentryitemname.setText(mDataEntryShoppingApplicationsList
					.get(position).getAppName());
			rowView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					loadShoppingApplication(mDataEntryShoppingApplicationsList
							.get(position).getActivityName(),
							mDataEntryShoppingApplicationsList.get(position)
									.getPackageName());
				}
			});
			return rowView;
		}

		@Override
		public int getCount() {
			return mDataEntryShoppingApplicationsList.size();
		}

		@Override
		public Object getItem(int arg0) {
			return arg0;
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			mGalleryClickedFlag = false;
			mCameraClickedFlag = false;
			mTopRightClickedFlag = false;
			mBottomRightClickedFlag = false;
			if (!mFromDetailsScreenFlag) {
				if (!mIsTopToBottomAnimationStartedFlag
						&& !mIsBottomToTopAnimationStartedFlag) {
					mDataEntryMoreTopListLayout.setVisibility(View.GONE);
					mIsBottomToTopAnimationStartedFlag = true;
					mBoxWithCircleLayout.startAnimation(mBottomToTopAnimation);
				}
			} else {
				if (!mIsBottomTopToBottomAnimationStartedFlag
						&& !mIsBottomBottomToTopAnimationStartedFlag) {
					mDataEntryMoreBottomListLayout.setVisibility(View.GONE);
					mIsBottomBottomToTopAnimationStartedFlag = true;
					mBottomBoxWithCircleLayout
							.startAnimation(mBottomBottomToTopAnimation);
				}
			}
		}
		return false;
	}

}
