package com.lateralthoughts.vue;

import java.util.ArrayList;
import com.flurry.android.FlurryAgent;
import com.lateralthoughts.vue.utils.FileCache;
import com.lateralthoughts.vue.utils.OtherSourceImageDetails;
import com.lateralthoughts.vue.utils.Utils;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DataEntryActivity extends BaseActivity {

	public boolean mIsKeyboardShownFlag = false;
	public TextView mVueDataentryActionbarScreenName;
	private RelativeLayout /* mVueDataentryActionbarCloseIconLayout, */
	mVueDataentryActionbarCreateAisleIconLayout,
			mVueDataentryActionbarShareIconLayout,
			mVueDataentryActionbarEditIconLayout,
			mVueDataentryActionbarAddImageIconLayout,
			mVueDataentryActionbarTopAddImageIconLayout;
	RelativeLayout mDataentryActionbarMainLayout;
	public LinearLayout mVueDataentryActionbarTopLayout,
			mVueDataentryActionbarAppIconLayout,
			mVueDataentryActionbarBottomLayout, mVueDataentryKeyboardLayout,
			mVueDataentryActionbarSaveLayout;
	public FrameLayout mVueDataentryKeyboardDone, mVueDataentryKeyboardCancel;
	private View mVueDataentryActionbarView;
	private DataEntryFragment mDataEntryFragment;
	private static final String CREATE_AISLE_SCREEN_VISITORS = "Create_Aisle_Screen_Visitors";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e("Land", "vueland 64");
		mVueDataentryActionbarView = LayoutInflater.from(this).inflate(
				R.layout.vue_dataentry_actionbar, null);
		mVueDataentryActionbarScreenName = (TextView) mVueDataentryActionbarView
				.findViewById(R.id.vue_dataentry_actionbar_screenname);
		/*
		 * mVueDataentryActionbarCloseIconLayout = (RelativeLayout)
		 * mVueDataentryActionbarView
		 * .findViewById(R.id.vue_dataentry_actionbar_close_icon_layout);
		 */
		mVueDataentryActionbarCreateAisleIconLayout = (RelativeLayout) mVueDataentryActionbarView
				.findViewById(R.id.vue_dataentry_actionbar_createaisle_icon_layout);
		mVueDataentryActionbarShareIconLayout = (RelativeLayout) mVueDataentryActionbarView
				.findViewById(R.id.vue_dataentry_actionbar_share_icon_layout);
		mVueDataentryActionbarSaveLayout = (LinearLayout) mVueDataentryActionbarView
				.findViewById(R.id.vue_dataentry_actionbar_app_save_layout);
		mVueDataentryActionbarEditIconLayout = (RelativeLayout) mVueDataentryActionbarView
				.findViewById(R.id.vue_dataentry_actionbar_edit_icon_layout);
		mVueDataentryActionbarAddImageIconLayout = (RelativeLayout) mVueDataentryActionbarView
				.findViewById(R.id.vue_dataentry_actionbar_addimage_icon_layout);
		mVueDataentryActionbarTopLayout = (LinearLayout) mVueDataentryActionbarView
				.findViewById(R.id.vue_dataentry_actionbar_top_layout);
		mVueDataentryActionbarBottomLayout = (LinearLayout) mVueDataentryActionbarView
				.findViewById(R.id.vue_dataentry_actionbar_bottom_layout);
		mVueDataentryActionbarAppIconLayout = (LinearLayout) mVueDataentryActionbarView
				.findViewById(R.id.vue_dataentry_actionbar_app_icon_layout);
		mVueDataentryActionbarScreenName.setText(getResources().getString(
				R.string.create_aisle_screen_title));
		mVueDataentryActionbarTopAddImageIconLayout = (RelativeLayout) mVueDataentryActionbarView
				.findViewById(R.id.vue_dataentry_actionbar_top_addimage_icon_layout);
		mDataentryActionbarMainLayout = (RelativeLayout) mVueDataentryActionbarView
				.findViewById(R.id.dataentry_actionbar_main_layout);
		mVueDataentryKeyboardLayout = (LinearLayout) mVueDataentryActionbarView
				.findViewById(R.id.vue_dataentry_keyboard_layout);
		mVueDataentryKeyboardDone = (FrameLayout) mVueDataentryActionbarView
				.findViewById(R.id.vue_dataentry_keyboard_done);
		mVueDataentryKeyboardCancel = (FrameLayout) mVueDataentryActionbarView
				.findViewById(R.id.vue_dataentry_keyboard_cancel);

		getSupportActionBar().setCustomView(mVueDataentryActionbarView);
		getSupportActionBar().setDisplayShowCustomEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		setContentView(R.layout.date_entry_main);

		mVueDataentryKeyboardCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (mDataEntryFragment == null) {
					mDataEntryFragment = (DataEntryFragment) getSupportFragmentManager()
							.findFragmentById(R.id.create_aisles_view_fragment);
				}
				if (mDataEntryFragment.mLookingForPopup.getVisibility() == View.VISIBLE) {
					mDataEntryFragment
							.lookingForInterceptListnerFunctionality();
				} else if (mDataEntryFragment.mOccasionPopup.getVisibility() == View.VISIBLE) {
					mDataEntryFragment.occasionInterceptListnerFunctionality();
				} else if (mDataEntryFragment.mCategoryListviewLayout
						.getVisibility() == View.VISIBLE) {
					if (mDataEntryFragment.mFindAtText.getText().toString()
							.trim().length() == 0) {
						mDataEntryFragment.mFindatClose
								.setVisibility(View.VISIBLE);
						/*mDataEntryFragment.mFindAtIconLayout
								.setVisibility(View.VISIBLE);*/
						mDataEntryFragment.mFindAtPopUp
								.setVisibility(View.VISIBLE);
						/*mDataEntryFragment.mFindAtLeftLine
								.setVisibility(View.VISIBLE);*/
						/*mDataEntryFragment.mFindAtRightLine
								.setVisibility(View.VISIBLE);
						mDataEntryFragment.mFindAtBottomLine
								.setVisibility(View.VISIBLE);*/
						mDataEntryFragment.mLookingForPopup
								.setVisibility(View.GONE);
						mDataEntryFragment.mLookingForListviewLayout
								.setVisibility(View.GONE);
						mDataEntryFragment.mOccasionPopup
								.setVisibility(View.GONE);
						mDataEntryFragment.mOccasionListviewLayout
								.setVisibility(View.GONE);
						mDataEntryFragment.mCategoryPopup
								.setVisibility(View.GONE);
						mDataEntryFragment.mCategoryListviewLayout
								.setVisibility(View.GONE);
						mDataEntryFragment.mSelectCategoryLayout
								.setVisibility(View.GONE);
						// mOccassionBigText.setBackgroundColor(Color.TRANSPARENT);
						// mLookingForBigText.setBackgroundColor(Color.TRANSPARENT);
						mDataEntryFragment.mInputMethodManager
								.hideSoftInputFromWindow(
										mDataEntryFragment.mOccasionText
												.getWindowToken(), 0);
						mDataEntryFragment.mInputMethodManager
								.hideSoftInputFromWindow(
										mDataEntryFragment.mLookingForText
												.getWindowToken(), 0);
						mDataEntryFragment.mInputMethodManager
								.hideSoftInputFromWindow(
										mDataEntryFragment.mSaySomethingAboutAisle
												.getWindowToken(), 0);
						mDataEntryFragment.mFindAtText.requestFocus();
						mDataEntryFragment.mFindAtText
								.setSelection(mDataEntryFragment.mFindAtText
										.getText().toString().length());
						mDataEntryFragment.mInputMethodManager.showSoftInput(
								mDataEntryFragment.mFindAtText, 0);
						mDataentryActionbarMainLayout.setVisibility(View.GONE);
						mVueDataentryKeyboardLayout.setVisibility(View.VISIBLE);
						mVueDataentryKeyboardDone.setVisibility(View.VISIBLE);
						mVueDataentryKeyboardCancel.setVisibility(View.VISIBLE);
					} else {
						/*
						 * mDataEntryFragment.mSaySomeThingEditParent
						 * .setVisibility(View.GONE);
						 */
						mDataEntryFragment.mSaySomethingAboutAisle
								.setVisibility(View.VISIBLE);
						mDataEntryFragment.mSaysomethingClose
								.setVisibility(View.VISIBLE);
						/*
						 * mDataEntryFragment.mSaySomeThingEditParent .post(new
						 * Runnable() {
						 * 
						 * @Override public void run() {
						 * mDataEntryFragment.mSaySomethingAboutAisle
						 * .requestFocus();
						 * mDataEntryFragment.mSaySomethingAboutAisle
						 * .setFocusable(true);
						 * mDataEntryFragment.mSaySomethingAboutAisle
						 * .setCursorVisible(true);
						 * mDataEntryFragment.mSaySomethingAboutAisle
						 * .setSelection
						 * (mDataEntryFragment.mSaySomethingAboutAisle
						 * .getText().toString() .length()); } });
						 */
						mDataEntryFragment.mSaySomethingAboutAisleClicked = true;
						mDataEntryFragment.mInputMethodManager
								.hideSoftInputFromWindow(
										mDataEntryFragment.mOccasionText
												.getWindowToken(), 0);
						mDataEntryFragment.mInputMethodManager
								.hideSoftInputFromWindow(
										mDataEntryFragment.mLookingForText
												.getWindowToken(), 0);
						mDataEntryFragment.mInputMethodManager
								.hideSoftInputFromWindow(
										mDataEntryFragment.mFindAtText
												.getWindowToken(), 0);
						mDataEntryFragment.mLookingForPopup
								.setVisibility(View.GONE);
						mDataEntryFragment.mLookingForListviewLayout
								.setVisibility(View.GONE);
						mDataEntryFragment.mOccasionPopup
								.setVisibility(View.GONE);
						mDataEntryFragment.mOccasionListviewLayout
								.setVisibility(View.GONE);
						mDataEntryFragment.mCategoryPopup
								.setVisibility(View.GONE);
						mDataEntryFragment.mFindatClose
								.setVisibility(View.GONE);
						/*mDataEntryFragment.mFindAtIconLayout
								.setVisibility(View.GONE);*/
						mDataEntryFragment.mFindAtPopUp.setVisibility(View.GONE);
						/*mDataEntryFragment.mFindAtLeftLine
								.setVisibility(View.GONE);
						mDataEntryFragment.mFindAtRightLine
								.setVisibility(View.GONE);
						mDataEntryFragment.mFindAtBottomLine
								.setVisibility(View.GONE);*/
						mDataEntryFragment.mCategoryListviewLayout
								.setVisibility(View.GONE);
						mDataEntryFragment.mSelectCategoryLayout
								.setVisibility(View.GONE);
						// mOccassionBigText.setBackgroundColor(Color.TRANSPARENT);
						// mLookingForBigText.setBackgroundColor(Color.TRANSPARENT);
						final InputMethodManager inputMethodManager = (InputMethodManager) DataEntryActivity.this
								.getSystemService(Context.INPUT_METHOD_SERVICE);
						inputMethodManager.toggleSoftInputFromWindow(
								mDataEntryFragment.mSaySomethingAboutAisle
										.getApplicationWindowToken(),
								InputMethodManager.SHOW_FORCED, 0);
						mDataEntryFragment.mSaySomethingAboutAisle
								.requestFocus();
						mDataEntryFragment.mInputMethodManager.showSoftInput(
								mDataEntryFragment.mSaySomethingAboutAisle, 0);
						mDataentryActionbarMainLayout.setVisibility(View.GONE);
						mVueDataentryKeyboardLayout.setVisibility(View.VISIBLE);
						mVueDataentryKeyboardDone.setVisibility(View.VISIBLE);
						mVueDataentryKeyboardCancel.setVisibility(View.VISIBLE);
					}
				} else if (mDataEntryFragment.mFindAtPopUp.getVisibility() == View.VISIBLE) {
					mDataEntryFragment.findAtInterceptListnerFunctionality();
				} else if (mDataEntryFragment.mSaySomethingAboutAisle
						.getVisibility() == View.VISIBLE) {
					mDataEntryFragment
							.saySomethingABoutAisleInterceptListnerFunctionality();
				}
			}
		});
		mVueDataentryKeyboardDone.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mDataEntryFragment == null) {
					mDataEntryFragment = (DataEntryFragment) getSupportFragmentManager()
							.findFragmentById(R.id.create_aisles_view_fragment);
				}
				mDataEntryFragment.hideAllEditableTextboxes();
			}
		});

		mVueDataentryActionbarAppIconLayout
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						// getSlidingMenu().toggle();
						if (mDataEntryFragment == null) {
							mDataEntryFragment = (DataEntryFragment) getSupportFragmentManager()
									.findFragmentById(
											R.id.create_aisles_view_fragment);
						}
						if (mDataEntryFragment.mLookingForPopup.getVisibility() == View.VISIBLE) {
							mDataEntryFragment
									.lookingForInterceptListnerFunctionality();
						} else if (mDataEntryFragment.mOccasionPopup
								.getVisibility() == View.VISIBLE) {
							mDataEntryFragment
									.occasionInterceptListnerFunctionality();
						} else if (mDataEntryFragment.mCategoryListviewLayout
								.getVisibility() == View.VISIBLE) {
							if (mDataEntryFragment.mFindAtText.getText()
									.toString().trim().length() == 0) {
								mDataEntryFragment.mFindatClose
										.setVisibility(View.VISIBLE);
								/*mDataEntryFragment.mFindAtIconLayout
										.setVisibility(View.VISIBLE);*/
								mDataEntryFragment.mFindAtPopUp
										.setVisibility(View.VISIBLE);
								/*mDataEntryFragment.mFindAtLeftLine
										.setVisibility(View.VISIBLE);
								mDataEntryFragment.mFindAtRightLine
										.setVisibility(View.VISIBLE);
								mDataEntryFragment.mFindAtBottomLine
										.setVisibility(View.VISIBLE);*/
								mDataEntryFragment.mLookingForPopup
										.setVisibility(View.GONE);
								mDataEntryFragment.mLookingForListviewLayout
										.setVisibility(View.GONE);
								mDataEntryFragment.mOccasionPopup
										.setVisibility(View.GONE);
								mDataEntryFragment.mOccasionListviewLayout
										.setVisibility(View.GONE);
								mDataEntryFragment.mCategoryPopup
										.setVisibility(View.GONE);
								mDataEntryFragment.mCategoryListviewLayout
										.setVisibility(View.GONE);
								mDataEntryFragment.mSelectCategoryLayout
										.setVisibility(View.GONE);
								// mOccassionBigText.setBackgroundColor(Color.TRANSPARENT);
								// mLookingForBigText.setBackgroundColor(Color.TRANSPARENT);
								mDataEntryFragment.mInputMethodManager
										.hideSoftInputFromWindow(
												mDataEntryFragment.mOccasionText
														.getWindowToken(), 0);
								mDataEntryFragment.mInputMethodManager
										.hideSoftInputFromWindow(
												mDataEntryFragment.mLookingForText
														.getWindowToken(), 0);
								mDataEntryFragment.mInputMethodManager
										.hideSoftInputFromWindow(
												mDataEntryFragment.mSaySomethingAboutAisle
														.getWindowToken(), 0);
								mDataEntryFragment.mFindAtText.requestFocus();
								mDataEntryFragment.mFindAtText
										.setSelection(mDataEntryFragment.mFindAtText
												.getText().toString().length());
								mDataEntryFragment.mInputMethodManager
										.showSoftInput(
												mDataEntryFragment.mFindAtText,
												0);
								mDataentryActionbarMainLayout
										.setVisibility(View.GONE);
								mVueDataentryKeyboardLayout
										.setVisibility(View.VISIBLE);
								mVueDataentryKeyboardDone
										.setVisibility(View.VISIBLE);
								mVueDataentryKeyboardCancel
										.setVisibility(View.VISIBLE);
							} else {
								/*
								 * mDataEntryFragment.mSaySomeThingEditParent
								 * .setVisibility(View.GONE);
								 */
								mDataEntryFragment.mSaySomethingAboutAisle
										.setVisibility(View.VISIBLE);
								mDataEntryFragment.mSaysomethingClose
										.setVisibility(View.VISIBLE);
								/*
								 * mDataEntryFragment.mSaySomeThingEditParent
								 * .post(new Runnable() {
								 * 
								 * @Override public void run() {
								 * mDataEntryFragment.mSaySomethingAboutAisle
								 * .requestFocus();
								 * mDataEntryFragment.mSaySomethingAboutAisle
								 * .setFocusable(true);
								 * mDataEntryFragment.mSaySomethingAboutAisle
								 * .setCursorVisible(true);
								 * mDataEntryFragment.mSaySomethingAboutAisle
								 * .setSelection
								 * (mDataEntryFragment.mSaySomethingAboutAisle
								 * .getText().toString() .length()); } });
								 */
								mDataEntryFragment.mSaySomethingAboutAisleClicked = true;
								mDataEntryFragment.mInputMethodManager
										.hideSoftInputFromWindow(
												mDataEntryFragment.mOccasionText
														.getWindowToken(), 0);
								mDataEntryFragment.mInputMethodManager
										.hideSoftInputFromWindow(
												mDataEntryFragment.mLookingForText
														.getWindowToken(), 0);
								mDataEntryFragment.mInputMethodManager
										.hideSoftInputFromWindow(
												mDataEntryFragment.mFindAtText
														.getWindowToken(), 0);
								mDataEntryFragment.mLookingForPopup
										.setVisibility(View.GONE);
								mDataEntryFragment.mLookingForListviewLayout
										.setVisibility(View.GONE);
								mDataEntryFragment.mOccasionPopup
										.setVisibility(View.GONE);
								mDataEntryFragment.mOccasionListviewLayout
										.setVisibility(View.GONE);
								mDataEntryFragment.mCategoryPopup
										.setVisibility(View.GONE);
								mDataEntryFragment.mFindatClose
										.setVisibility(View.GONE);
								/*mDataEntryFragment.mFindAtIconLayout
										.setVisibility(View.GONE);*/
								mDataEntryFragment.mFindAtPopUp
										.setVisibility(View.GONE);
								/*mDataEntryFragment.mFindAtLeftLine
										.setVisibility(View.GONE);
								mDataEntryFragment.mFindAtRightLine
										.setVisibility(View.GONE);
								mDataEntryFragment.mFindAtBottomLine
										.setVisibility(View.GONE);*/
								mDataEntryFragment.mCategoryListviewLayout
										.setVisibility(View.GONE);
								mDataEntryFragment.mSelectCategoryLayout
										.setVisibility(View.GONE);
								// mOccassionBigText.setBackgroundColor(Color.TRANSPARENT);
								// mLookingForBigText.setBackgroundColor(Color.TRANSPARENT);
								final InputMethodManager inputMethodManager = (InputMethodManager) DataEntryActivity.this
										.getSystemService(Context.INPUT_METHOD_SERVICE);
								inputMethodManager
										.toggleSoftInputFromWindow(
												mDataEntryFragment.mSaySomethingAboutAisle
														.getApplicationWindowToken(),
												InputMethodManager.SHOW_FORCED,
												0);
								mDataEntryFragment.mSaySomethingAboutAisle
										.requestFocus();
								mDataEntryFragment.mInputMethodManager
										.showSoftInput(
												mDataEntryFragment.mSaySomethingAboutAisle,
												0);
								mDataentryActionbarMainLayout
										.setVisibility(View.GONE);
								mVueDataentryKeyboardLayout
										.setVisibility(View.VISIBLE);
								mVueDataentryKeyboardDone
										.setVisibility(View.VISIBLE);
								mVueDataentryKeyboardCancel
										.setVisibility(View.VISIBLE);
							}
						} else if (mDataEntryFragment.mFindAtPopUp
								.getVisibility() == View.VISIBLE) {
							mDataEntryFragment
									.findAtInterceptListnerFunctionality();
						} else if (mDataEntryFragment.mSaySomethingAboutAisle
								.getVisibility() == View.VISIBLE) {
							mDataEntryFragment
									.saySomethingABoutAisleInterceptListnerFunctionality();
							showDiscardOtherAppImageDialog();
						} else {
							showDiscardOtherAppImageDialog();
						}
					}
				});
		mVueDataentryActionbarAddImageIconLayout
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						if (mDataEntryFragment == null) {
							mDataEntryFragment = (DataEntryFragment) getSupportFragmentManager()
									.findFragmentById(
											R.id.create_aisles_view_fragment);
						}
						Utils.putTouchToChnageImagePosition(
								DataEntryActivity.this, -1);
						Utils.putTouchToChnageImageTempPosition(
								DataEntryActivity.this, -1);
						Utils.putTouchToChnageImageFlag(DataEntryActivity.this,
								false);
						mDataEntryFragment
								.addImageToAisleButtonClickFunctionality(false);
					}
				});
		mVueDataentryActionbarTopAddImageIconLayout
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						if (mDataEntryFragment == null) {
							mDataEntryFragment = (DataEntryFragment) getSupportFragmentManager()
									.findFragmentById(
											R.id.create_aisles_view_fragment);
						}
						Utils.putTouchToChnageImagePosition(
								DataEntryActivity.this, -1);
						Utils.putTouchToChnageImageTempPosition(
								DataEntryActivity.this, -1);
						Utils.putTouchToChnageImageFlag(DataEntryActivity.this,
								false);
						mDataEntryFragment
								.addImageToAisleButtonClickFunctionality(true);
					}
				});
		/*
		 * mVueDataentryActionbarCloseIconLayout .setOnClickListener(new
		 * OnClickListener() {
		 * 
		 * @Override public void onClick(View arg0) {
		 * showDiscardOtherAppImageDialog(); } });
		 */
		mVueDataentryActionbarSaveLayout
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						if (mDataEntryFragment == null) {
							mDataEntryFragment = (DataEntryFragment) getSupportFragmentManager()
									.findFragmentById(
											R.id.create_aisles_view_fragment);
						}
						Utils.putTouchToChnageImagePosition(
								DataEntryActivity.this, -1);
						Utils.putTouchToChnageImageTempPosition(
								DataEntryActivity.this, -1);
						Utils.putTouchToChnageImageFlag(DataEntryActivity.this,
								false);
						mDataEntryFragment.createAisleClickFunctionality();
					}
				});

		mVueDataentryActionbarCreateAisleIconLayout
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						if (mDataEntryFragment == null) {
							mDataEntryFragment = (DataEntryFragment) getSupportFragmentManager()
									.findFragmentById(
											R.id.create_aisles_view_fragment);
						}
						Utils.putTouchToChnageImagePosition(
								DataEntryActivity.this, -1);
						Utils.putTouchToChnageImageTempPosition(
								DataEntryActivity.this, -1);
						Utils.putTouchToChnageImageFlag(DataEntryActivity.this,
								false);
						mDataEntryFragment.createAisleClickFunctionality();
					}
				});
		mVueDataentryActionbarShareIconLayout
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						if (mDataEntryFragment == null) {
							mDataEntryFragment = (DataEntryFragment) getSupportFragmentManager()
									.findFragmentById(
											R.id.create_aisles_view_fragment);
						}
						mDataEntryFragment.shareClickFunctionality();
					}
				});
		mVueDataentryActionbarEditIconLayout
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						if (mDataEntryFragment == null) {
							mDataEntryFragment = (DataEntryFragment) getSupportFragmentManager()
									.findFragmentById(
											R.id.create_aisles_view_fragment);
						}
						Utils.putTouchToChnageImagePosition(
								DataEntryActivity.this, -1);
						Utils.putTouchToChnageImageTempPosition(
								DataEntryActivity.this, -1);
						Utils.putTouchToChnageImageFlag(DataEntryActivity.this,
								false);
						mDataEntryFragment.editButtonClickFunctionality();
					}
				});
		Bundle b = getIntent().getExtras();
		if (b != null) {
			Log.e("cs", "30");
			String aisleImagePath = b
					.getString(VueConstants.CREATE_AISLE_CAMERA_GALLERY_IMAGE_PATH_BUNDLE_KEY);
			if (mDataEntryFragment == null) {
				mDataEntryFragment = (DataEntryFragment) getSupportFragmentManager()
						.findFragmentById(R.id.create_aisles_view_fragment);
			}
			mDataEntryFragment.mFromDetailsScreenFlag = b.getBoolean(
					VueConstants.FROM_DETAILS_SCREEN_TO_DATAENTRY_SCREEN_FLAG,
					false);
			try {
				if (b.getString(VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_FINDAT) != null) {
					mDataEntryFragment.mFindAtText
							.setText(b
									.getString(VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_FINDAT));
					/*
					 * mDataEntryFragment.mFindAtheading .setText(b
					 * .getString(VueConstants
					 * .FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_FINDAT));
					 * mDataEntryFragment.mFindatheadinglayout
					 * .setVisibility(View.GONE);
					 */
					mDataEntryFragment.mPreviousFindAtText = mDataEntryFragment.mFindAtText
							.getText().toString();
				}
			} catch (Exception e1) {
			}
			if (b.getString(VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_LOOKINGFOR) != null) {
				/*
				 * mDataEntryFragment.mLookingForBigText .setText(b
				 * .getString(VueConstants
				 * .FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_LOOKINGFOR));
				 */
				mDataEntryFragment.mLookingFor = b
						.getString(VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_LOOKINGFOR);
				mDataEntryFragment.mMainHeadingRow.setVisibility(View.VISIBLE);
				if (mDataEntryFragment.mOccasion != null) {
					mDataEntryFragment.mLookingForOccasionTextview
							.setText(mDataEntryFragment.mLookingFor + " for "
									+ mDataEntryFragment.mOccasion);
				} else {
					mDataEntryFragment.mLookingForOccasionTextview
							.setText("Looking for "
									+ mDataEntryFragment.mLookingFor);
				}
				mDataEntryFragment.mLookingForText
						.setText(b
								.getString(VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_LOOKINGFOR));
			}
			if (b.getString(VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_OCCASION) != null) {
				mDataEntryFragment.mOccasion = b
						.getString(VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_OCCASION);
				if (mDataEntryFragment.mOccasion != null
						&& mDataEntryFragment.mOccasion.length() > 0) {
					// mDataEntryFragment.mOccassionBigText.setText(occasion);
					mDataEntryFragment.mLookingForOccasionTextview
							.setText(mDataEntryFragment.mLookingFor + " for "
									+ mDataEntryFragment.mOccasion);
					mDataEntryFragment.mOccasionText
							.setText(b
									.getString(VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_OCCASION));
				} else {
					/*
					 * mDataEntryFragment.mOccassionBigText
					 * .setText(DataEntryFragment.OCCASION);
					 */
				}
			}
			if (b.getString(VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_CATEGORY) != null) {
				mDataEntryFragment.mCategoryText
						.setText(b
								.getString(VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_CATEGORY));
				mDataEntryFragment.mCategoryheading
						.setText(b
								.getString(VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_CATEGORY));
				mDataEntryFragment.mCategoryheadingLayout
						.setVisibility(View.VISIBLE);
			}
			if (mDataEntryFragment.mFromDetailsScreenFlag) {
				mDataEntryFragment.mIsUserAisleFlag = b
						.getBoolean(VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_IS_USER_AISLE_FLAG);
				boolean firstTimeFlag = false;
				try {
					mDataEntryFragment.mAisleImagePathList = Utils
							.readAisleImagePathListFromFile(
									DataEntryActivity.this,
									VueConstants.AISLE_IMAGE_PATH_LIST_FILE_NAME);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (mDataEntryFragment.mAisleImagePathList == null) {
					firstTimeFlag = true;
					mDataEntryFragment.mAisleImagePathList = new ArrayList<DataentryImage>();
				}
				if (mDataEntryFragment.mAisleImagePathList.size() == 0) {
					firstTimeFlag = true;
				}
				Log.e("DataentryActivity", "firsttimeflag called : "
						+ firstTimeFlag);
				if (firstTimeFlag) {
					Log.e("DataentryActivity", "if firsttimeflag called : ");
					mDataEntryFragment
							.showDetailsScreenImagesInDataentryScreen();
				}
				mVueDataentryActionbarScreenName.setText(getResources()
						.getString(R.string.add_imae_to_aisle_screen_title));
				if (b.getBoolean(VueConstants.EDIT_IMAGE_FROM_DETAILS_SCREEN_FALG)) {
					mVueDataentryActionbarScreenName.setText(getResources()
							.getString(R.string.edit_aisle_screen_title));
				}

				if (!mDataEntryFragment.mIsUserAisleFlag) {
					mDataEntryFragment.mLookingForPopup
							.setVisibility(View.GONE);
					/*
					 * mDataEntryFragment.mLookingForBigText
					 * .setBackgroundColor(Color.TRANSPARENT);
					 */
					mDataEntryFragment.mLookingForListviewLayout
							.setVisibility(View.GONE);
					// mDataEntryFragment.mLookingForBigText.setClickable(false);
					// mDataEntryFragment.mOccassionBigText.setClickable(false);
					// mDataEntryFragment.mCategoryIcon.setClickable(false);
					/*
					 * mDataEntryFragment.mSaySomeThingEditParent
					 * .setClickable(false);
					 */
				}
				if (b.getString(VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_SAYSOMETHINGABOUTAISLE) != null) {
					mDataEntryFragment.mSaySomethingAboutAisle
							.setText(b
									.getString(VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_SAYSOMETHINGABOUTAISLE));
					/*
					 * mDataEntryFragment.mDescriptionheading .setText(b
					 * .getString(VueConstants.
					 * FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_SAYSOMETHINGABOUTAISLE
					 * )); mDataEntryFragment.mDescriptionheadingLayout
					 * .setVisibility(View.VISIBLE);
					 */
					/*
					 * mDataEntryFragment.mHintTextForSaySomeThing .setText(b
					 * .getString(VueConstants.
					 * FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_SAYSOMETHINGABOUTAISLE
					 * ));
					 */
				}
			}
			Log.e("cs", "32");
			mDataEntryFragment.mOtherSourceSelectedImageUrl = b
					.getString(VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_IMAGEURL);
			mDataEntryFragment.mOtherSourceImageOriginalWidth = b
					.getInt(VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_IMAGE_WIDTH);
			mDataEntryFragment.mOtherSourceImageOriginalHeight = b
					.getInt(VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_IMAGE_HEIGHT);
			mDataEntryFragment.mOtherSourceSelectedImageDetailsUrl = b
					.getString(VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_IMAGE_DETAILSURL);
			mDataEntryFragment.mOtherSourceSelectedImageStore = b
					.getString(VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_IMAGE_STORE);
			if (Utils.getTouchToChangeFlag(DataEntryActivity.this)) {
				Utils.putTouchToChnageImagePosition(
						DataEntryActivity.this,
						Utils.getTouchToChangeTempPosition(DataEntryActivity.this));
			}
			if (aisleImagePath != null) {
				mDataEntryFragment.setGalleryORCameraImage(aisleImagePath,
						false);
			}
			if (b.getBoolean(VueConstants.FROM_OTHER_SOURCES_FLAG)) {
				if (!mDataEntryFragment.mFromDetailsScreenFlag
						&& !(Utils
								.getDataentryTopAddImageAisleFlag(DataEntryActivity.this))) {
					ArrayList<DataentryImage> mAisleImagePathList = null;
					try {
						mAisleImagePathList = Utils
								.readAisleImagePathListFromFile(
										DataEntryActivity.this,
										VueConstants.AISLE_IMAGE_PATH_LIST_FILE_NAME);
					} catch (Exception e) {
						e.printStackTrace();
					}
					if (mAisleImagePathList != null
							&& mAisleImagePathList.size() > 0) {
						mVueDataentryActionbarScreenName
								.setText(getResources()
										.getString(
												R.string.add_imae_to_aisle_screen_title));
					}
				}
				if (Utils.getDataentryAddImageAisleFlag(DataEntryActivity.this)) {
					mVueDataentryActionbarScreenName
							.setText(getResources().getString(
									R.string.add_imae_to_aisle_screen_title));
					mVueDataentryActionbarBottomLayout.setVisibility(View.GONE);
					mVueDataentryActionbarTopLayout.setVisibility(View.VISIBLE);
					mVueDataentryActionbarSaveLayout.setVisibility(View.VISIBLE);
					/*
					 * mDataEntryFragment.mDataEntryBottomBottomLayout
					 * .setVisibility(View.VISIBLE);
					 */
					mDataEntryFragment.mDataEntryBottomTopLayout
							.setVisibility(View.GONE);
					mDataEntryFragment.mMainHeadingRow
							.setVisibility(View.VISIBLE);
					mDataEntryFragment.mCategoryheadingLayout
							.setVisibility(View.VISIBLE);
					/*
					 * mDataEntryFragment.mOccassionBigText
					 * .setBackgroundColor(Color.TRANSPARENT);
					 * mDataEntryFragment.mLookingForBigText
					 * .setBackgroundColor(Color.TRANSPARENT);
					 */
				}
				if (Utils.getDataentryEditAisleFlag(DataEntryActivity.this)) {
					mVueDataentryActionbarScreenName.setText(getResources()
							.getString(R.string.edit_aisle_screen_title));
				}
				if (b.getString(VueConstants.FROM_OTHER_SOURCES_URL) != null) {
					mDataEntryFragment.getImagesFromUrl(b
							.getString(VueConstants.FROM_OTHER_SOURCES_URL));
				} else if (b
						.getParcelableArrayList(VueConstants.FROM_OTHER_SOURCES_IMAGE_URIS) != null) {
					ArrayList<Uri> imageUrisList = b
							.getParcelableArrayList(VueConstants.FROM_OTHER_SOURCES_IMAGE_URIS);
					ArrayList<OtherSourceImageDetails> otherSourcesImageDetailsList = new ArrayList<OtherSourceImageDetails>();
					for (int i = 0; i < imageUrisList.size(); i++) {
						OtherSourceImageDetails otherSourceImageDetails = new OtherSourceImageDetails(
								null, null, null, 0, 0, imageUrisList.get(i), 0);
						otherSourcesImageDetailsList
								.add(otherSourceImageDetails);
					}
					mDataEntryFragment.showOtherSourcesGridview(
							otherSourcesImageDetailsList, "");
				}
			}
		}
	}

	@Override
	protected void onStart() {
		FlurryAgent.onStartSession(this, Utils.FLURRY_APP_KEY);
		FlurryAgent.onPageView();
		FlurryAgent.logEvent(CREATE_AISLE_SCREEN_VISITORS);
		super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
		FlurryAgent.onEndSession(this);

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
					if (mDataEntryFragment == null) {
						mDataEntryFragment = (DataEntryFragment) getSupportFragmentManager()
								.findFragmentById(
										R.id.create_aisles_view_fragment);
					}
					if (Utils.getTouchToChangeFlag(DataEntryActivity.this)) {
						Utils.putTouchToChnageImagePosition(
								DataEntryActivity.this,
								Utils.getTouchToChangeTempPosition(DataEntryActivity.this));
					}
					mDataEntryFragment.mFindAtText.setText("");
					mDataEntryFragment.mOtherSourceSelectedImageStore = "UnKnown";
					mDataEntryFragment.mOtherSourceSelectedImageUrl = null;
					mDataEntryFragment
							.setGalleryORCameraImage(imagePath, false);
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
					if (mDataEntryFragment == null) {
						mDataEntryFragment = (DataEntryFragment) getSupportFragmentManager()
								.findFragmentById(
										R.id.create_aisles_view_fragment);
					}
					if (mDataEntryFragment.mShare.mShareIntentCalled) {
						mDataEntryFragment.mShare.mShareIntentCalled = false;
						mDataEntryFragment.mShare.dismisDialog();
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
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				mDataEntryFragment.lookingForTextClickFunctionality();

			}
		}, 200);

		super.onResume();
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		Log.e("fff", "onkeyup");
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mDataEntryFragment.mCategoryListviewLayout.getVisibility() == View.VISIBLE) {
				if (mDataEntryFragment.mFindAtText.getText().toString().trim()
						.length() == 0) {
					mDataEntryFragment.mFindatClose.setVisibility(View.VISIBLE);
					/*mDataEntryFragment.mFindAtIconLayout
							.setVisibility(View.VISIBLE);*/
					mDataEntryFragment.mFindAtPopUp.setVisibility(View.VISIBLE);
					/*mDataEntryFragment.mFindAtLeftLine
							.setVisibility(View.VISIBLE);
					mDataEntryFragment.mFindAtRightLine
							.setVisibility(View.VISIBLE);
					mDataEntryFragment.mFindAtBottomLine
							.setVisibility(View.VISIBLE);*/
					mDataEntryFragment.mLookingForPopup
							.setVisibility(View.GONE);
					mDataEntryFragment.mLookingForListviewLayout
							.setVisibility(View.GONE);
					mDataEntryFragment.mOccasionPopup.setVisibility(View.GONE);
					mDataEntryFragment.mOccasionListviewLayout
							.setVisibility(View.GONE);
					mDataEntryFragment.mCategoryPopup.setVisibility(View.GONE);
					mDataEntryFragment.mCategoryListviewLayout
							.setVisibility(View.GONE);
					mDataEntryFragment.mSelectCategoryLayout
							.setVisibility(View.GONE);
					// mOccassionBigText.setBackgroundColor(Color.TRANSPARENT);
					// mLookingForBigText.setBackgroundColor(Color.TRANSPARENT);
					mDataEntryFragment.mInputMethodManager
							.hideSoftInputFromWindow(
									mDataEntryFragment.mOccasionText
											.getWindowToken(), 0);
					mDataEntryFragment.mInputMethodManager
							.hideSoftInputFromWindow(
									mDataEntryFragment.mLookingForText
											.getWindowToken(), 0);
					mDataEntryFragment.mInputMethodManager
							.hideSoftInputFromWindow(
									mDataEntryFragment.mSaySomethingAboutAisle
											.getWindowToken(), 0);
					mDataEntryFragment.mFindAtText.requestFocus();
					mDataEntryFragment.mFindAtText
							.setSelection(mDataEntryFragment.mFindAtText
									.getText().toString().length());
					mDataEntryFragment.mInputMethodManager.showSoftInput(
							mDataEntryFragment.mFindAtText, 0);
					mDataentryActionbarMainLayout.setVisibility(View.GONE);
					mVueDataentryKeyboardLayout.setVisibility(View.VISIBLE);
					mVueDataentryKeyboardDone.setVisibility(View.VISIBLE);
					mVueDataentryKeyboardCancel.setVisibility(View.VISIBLE);
				} else {
					/*
					 * mDataEntryFragment.mSaySomeThingEditParent
					 * .setVisibility(View.GONE);
					 */
					mDataEntryFragment.mSaySomethingAboutAisle
							.setVisibility(View.VISIBLE);
					mDataEntryFragment.mSaysomethingClose
							.setVisibility(View.VISIBLE);
					/*
					 * mDataEntryFragment.mSaySomeThingEditParent .post(new
					 * Runnable() {
					 * 
					 * @Override public void run() {
					 * mDataEntryFragment.mSaySomethingAboutAisle
					 * .requestFocus();
					 * mDataEntryFragment.mSaySomethingAboutAisle
					 * .setFocusable(true);
					 * mDataEntryFragment.mSaySomethingAboutAisle
					 * .setCursorVisible(true);
					 * mDataEntryFragment.mSaySomethingAboutAisle
					 * .setSelection(mDataEntryFragment.mSaySomethingAboutAisle
					 * .getText().toString() .length()); } });
					 */
					mDataEntryFragment.mSaySomethingAboutAisleClicked = true;
					mDataEntryFragment.mInputMethodManager
							.hideSoftInputFromWindow(
									mDataEntryFragment.mOccasionText
											.getWindowToken(), 0);
					mDataEntryFragment.mInputMethodManager
							.hideSoftInputFromWindow(
									mDataEntryFragment.mLookingForText
											.getWindowToken(), 0);
					mDataEntryFragment.mInputMethodManager
							.hideSoftInputFromWindow(
									mDataEntryFragment.mFindAtText
											.getWindowToken(), 0);
					mDataEntryFragment.mLookingForPopup
							.setVisibility(View.GONE);
					mDataEntryFragment.mLookingForListviewLayout
							.setVisibility(View.GONE);
					mDataEntryFragment.mOccasionPopup.setVisibility(View.GONE);
					mDataEntryFragment.mOccasionListviewLayout
							.setVisibility(View.GONE);
					mDataEntryFragment.mCategoryPopup.setVisibility(View.GONE);
					mDataEntryFragment.mFindatClose.setVisibility(View.GONE);
					/*mDataEntryFragment.mFindAtIconLayout
							.setVisibility(View.GONE);*/
					mDataEntryFragment.mFindAtPopUp.setVisibility(View.GONE);
				/*	mDataEntryFragment.mFindAtLeftLine.setVisibility(View.GONE);
					mDataEntryFragment.mFindAtRightLine
							.setVisibility(View.GONE);
					mDataEntryFragment.mFindAtBottomLine
							.setVisibility(View.GONE);*/
					mDataEntryFragment.mCategoryListviewLayout
							.setVisibility(View.GONE);
					mDataEntryFragment.mSelectCategoryLayout
							.setVisibility(View.GONE);
					// mOccassionBigText.setBackgroundColor(Color.TRANSPARENT);
					// mLookingForBigText.setBackgroundColor(Color.TRANSPARENT);
					final InputMethodManager inputMethodManager = (InputMethodManager) DataEntryActivity.this
							.getSystemService(Context.INPUT_METHOD_SERVICE);
					inputMethodManager.toggleSoftInputFromWindow(
							mDataEntryFragment.mSaySomethingAboutAisle
									.getApplicationWindowToken(),
							InputMethodManager.SHOW_FORCED, 0);
					mDataEntryFragment.mSaySomethingAboutAisle.requestFocus();
					mDataEntryFragment.mInputMethodManager.showSoftInput(
							mDataEntryFragment.mSaySomethingAboutAisle, 0);
					mDataentryActionbarMainLayout.setVisibility(View.GONE);
					mVueDataentryKeyboardLayout.setVisibility(View.VISIBLE);
					mVueDataentryKeyboardDone.setVisibility(View.VISIBLE);
					mVueDataentryKeyboardCancel.setVisibility(View.VISIBLE);
				}
			} else if (getSlidingMenu().isMenuShowing()) {
				if (!mFrag.listener.onBackPressed()) {
					getSlidingMenu().toggle();
				}
			} else {
				if (mVueDataentryActionbarSaveLayout.getVisibility() == View.GONE) {
					Utils.putDataentryAddImageAisleFlag(DataEntryActivity.this,
							false);
					Utils.putDataentryTopAddImageAisleFlag(
							DataEntryActivity.this, false);
					Utils.putDataentryEditAisleFlag(DataEntryActivity.this,
							false);
					Utils.putDataentryTopAddImageAisleLookingFor(
							DataEntryActivity.this, null);
					Utils.putDataentryTopAddImageAisleCategory(
							DataEntryActivity.this, null);
					Utils.putDataentryTopAddImageAisleOccasion(
							DataEntryActivity.this, null);
					Utils.putDataentryTopAddImageAisleDescription(
							DataEntryActivity.this, null);
					Utils.putDataentryScreenAisleId(this, null);
					Utils.putTouchToChnageImagePosition(DataEntryActivity.this,
							-1);
					Utils.putTouchToChnageImageTempPosition(
							DataEntryActivity.this, -1);
					Utils.putTouchToChnageImageFlag(DataEntryActivity.this,
							false);
					FileCache fileCache = new FileCache(
							VueApplication.getInstance());
					fileCache.clearVueAppResizedPictures();
					fileCache.clearVueAppCameraPictures();
					ArrayList<DataentryImage> mAisleImagePathList = null;
					try {
						mAisleImagePathList = Utils
								.readAisleImagePathListFromFile(
										DataEntryActivity.this,
										VueConstants.AISLE_IMAGE_PATH_LIST_FILE_NAME);
						mAisleImagePathList.clear();
						Utils.writeAisleImagePathListToFile(
								DataEntryActivity.this,
								VueConstants.AISLE_IMAGE_PATH_LIST_FILE_NAME,
								mAisleImagePathList);
					} catch (Exception e) {
						e.printStackTrace();
					}
					super.onBackPressed();
				} else {
					showDiscardOtherAppImageDialog();
				}
			}
		}
		return false;

	}

	private void showDiscardOtherAppImageDialog() {
		final Dialog dialog = new Dialog(this, R.style.Theme_Dialog_Translucent);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.vue_popup);
		final TextView noButton = (TextView) dialog.findViewById(R.id.nobutton);
		TextView yesButton = (TextView) dialog.findViewById(R.id.okbutton);
		TextView messagetext = (TextView) dialog.findViewById(R.id.messagetext);
		messagetext.setText(getResources().getString(
				R.string.discard_dataentry_screen_changes));
		yesButton.setText("Yes");
		noButton.setText("No");
		yesButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				dialog.dismiss();
				Utils.putTouchToChnageImagePosition(DataEntryActivity.this, -1);
				Utils.putTouchToChnageImageTempPosition(DataEntryActivity.this,
						-1);
				Utils.putTouchToChnageImageFlag(DataEntryActivity.this, false);
				Utils.putDataentryAddImageAisleFlag(DataEntryActivity.this,
						false);
				Utils.putDataentryTopAddImageAisleFlag(DataEntryActivity.this,
						false);
				Utils.putDataentryTopAddImageAisleLookingFor(
						DataEntryActivity.this, null);
				Utils.putDataentryTopAddImageAisleCategory(
						DataEntryActivity.this, null);
				Utils.putDataentryTopAddImageAisleOccasion(
						DataEntryActivity.this, null);
				Utils.putDataentryTopAddImageAisleDescription(
						DataEntryActivity.this, null);
				Utils.putDataentryEditAisleFlag(DataEntryActivity.this, false);
				Utils.putDataentryScreenAisleId(DataEntryActivity.this, null);
				ArrayList<DataentryImage> mAisleImagePathList = null;
				try {
					mAisleImagePathList = Utils.readAisleImagePathListFromFile(
							DataEntryActivity.this,
							VueConstants.AISLE_IMAGE_PATH_LIST_FILE_NAME);
					mAisleImagePathList.clear();
					Utils.writeAisleImagePathListToFile(DataEntryActivity.this,
							VueConstants.AISLE_IMAGE_PATH_LIST_FILE_NAME,
							mAisleImagePathList);
				} catch (Exception e) {
					e.printStackTrace();
				}
				FileCache fileCache = new FileCache(VueApplication
						.getInstance());
				fileCache.clearVueAppResizedPictures();
				fileCache.clearVueAppCameraPictures();
				finish();
			}
		});
		noButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.show();
	}

	public void shareViaVueClicked() {
		Utils.putDataentryAddImageAisleFlag(DataEntryActivity.this, false);
		Utils.putDataentryTopAddImageAisleFlag(DataEntryActivity.this, false);
		Utils.putDataentryTopAddImageAisleLookingFor(DataEntryActivity.this,
				null);
		Utils.putTouchToChnageImagePosition(DataEntryActivity.this, -1);
		Utils.putTouchToChnageImageTempPosition(DataEntryActivity.this, -1);
		Utils.putTouchToChnageImageFlag(DataEntryActivity.this, false);
		Utils.putDataentryTopAddImageAisleCategory(DataEntryActivity.this, null);
		Utils.putDataentryTopAddImageAisleOccasion(DataEntryActivity.this, null);
		Utils.putDataentryTopAddImageAisleDescription(DataEntryActivity.this,
				null);
		Utils.putDataentryEditAisleFlag(DataEntryActivity.this, false);
		Utils.putDataentryScreenAisleId(DataEntryActivity.this, null);
		ArrayList<DataentryImage> mAisleImagePathList = null;
		try {
			mAisleImagePathList = Utils.readAisleImagePathListFromFile(
					DataEntryActivity.this,
					VueConstants.AISLE_IMAGE_PATH_LIST_FILE_NAME);
			mAisleImagePathList.clear();
			Utils.writeAisleImagePathListToFile(DataEntryActivity.this,
					VueConstants.AISLE_IMAGE_PATH_LIST_FILE_NAME,
					mAisleImagePathList);
		} catch (Exception e) {
			e.printStackTrace();
		}
		FileCache fileCache = new FileCache(VueApplication.getInstance());
		fileCache.clearVueAppResizedPictures();
		fileCache.clearVueAppCameraPictures();
		finish();
	}
}