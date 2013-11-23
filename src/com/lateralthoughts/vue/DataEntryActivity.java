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
import android.widget.Toast;

public class DataEntryActivity extends BaseActivity {

	public boolean mIsKeyboardShownFlag = false;
	public TextView mVueDataentryActionbarScreenName,
			mActionbarDeleteBtnTextview;
	RelativeLayout mDataentryActionbarMainLayout,
			mVueDataentryActionbarAppIconLayout;
	public LinearLayout mVueDataentryKeyboardLayout, mVueDataentryPostLayout,
			mVueDataentryDeleteLayout;
	public FrameLayout mVueDataentryKeyboardDone, mVueDataentryKeyboardCancel,
			mVueDataentryClose, mVueDataentryPost, mVueDataentryDeleteCancel,
			mVueDataentryDeleteDone;
	private View mVueDataentryActionbarView;
	private DataEntryFragment mDataEntryFragment;
	private static final String CREATE_AISLE_SCREEN_VISITORS = "Create_Aisle_Screen_Visitors";
	public ArrayList<Integer> mDeletedImagesPositionsList = null;
	public int mDeletedImagesCount = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mVueDataentryActionbarView = LayoutInflater.from(this).inflate(
				R.layout.vue_dataentry_actionbar, null);
		mVueDataentryActionbarScreenName = (TextView) mVueDataentryActionbarView
				.findViewById(R.id.vue_dataentry_actionbar_screenname);
		mVueDataentryActionbarAppIconLayout = (RelativeLayout) mVueDataentryActionbarView
				.findViewById(R.id.vue_dataentry_actionbar_app_icon_layout);
		mVueDataentryActionbarScreenName.setText(getResources().getString(
				R.string.create_aisle_screen_title));
		mDataentryActionbarMainLayout = (RelativeLayout) mVueDataentryActionbarView
				.findViewById(R.id.dataentry_actionbar_main_layout);
		mVueDataentryKeyboardLayout = (LinearLayout) mVueDataentryActionbarView
				.findViewById(R.id.vue_dataentry_keyboard_layout);
		mVueDataentryKeyboardDone = (FrameLayout) mVueDataentryActionbarView
				.findViewById(R.id.vue_dataentry_keyboard_done);
		mVueDataentryKeyboardCancel = (FrameLayout) mVueDataentryActionbarView
				.findViewById(R.id.vue_dataentry_keyboard_cancel);
		mVueDataentryPostLayout = (LinearLayout) mVueDataentryActionbarView
				.findViewById(R.id.vue_dataentry_post_layout);
		mVueDataentryClose = (FrameLayout) mVueDataentryActionbarView
				.findViewById(R.id.actionbar_close);
		mVueDataentryPost = (FrameLayout) mVueDataentryActionbarView
				.findViewById(R.id.actionbar_post);
		mVueDataentryDeleteLayout = (LinearLayout) mVueDataentryActionbarView
				.findViewById(R.id.vue_dataentry_delete_layout);
		mVueDataentryDeleteCancel = (FrameLayout) mVueDataentryActionbarView
				.findViewById(R.id.vue_dataentry_delete_cancel);
		mVueDataentryDeleteDone = (FrameLayout) mVueDataentryActionbarView
				.findViewById(R.id.vue_dataentry_delete_done);
		mActionbarDeleteBtnTextview = (TextView) mVueDataentryActionbarView
				.findViewById(R.id.actionbar_delete_btn_textview);
		getSupportActionBar().setCustomView(mVueDataentryActionbarView);
		getSupportActionBar().setDisplayShowCustomEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(false);

		setContentView(R.layout.date_entry_main);

		mVueDataentryDeleteCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDiscardOtherAppImageDialog();
			}
		});
		mVueDataentryDeleteDone.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mDeletedImagesPositionsList != null
						&& mDeletedImagesPositionsList.size() > 0) {
					if (mDataEntryFragment == null) {
						mDataEntryFragment = (DataEntryFragment) getSupportFragmentManager()
								.findFragmentById(
										R.id.create_aisles_view_fragment);
					}
					mDataEntryFragment.deleteImage(mDeletedImagesPositionsList);
				} else {
					Toast.makeText(DataEntryActivity.this,
							"Please select atleast one image to delete.",
							Toast.LENGTH_LONG).show();
				}
			}
		});
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
						mDataEntryFragment.mFindAtPopUp
								.setVisibility(View.VISIBLE);
						mDataEntryFragment.mLookingForPopup
								.setVisibility(View.GONE);
						mDataEntryFragment.mLookingForListviewLayout
								.setVisibility(View.GONE);
						mDataEntryFragment.mOccasionPopup
								.setVisibility(View.GONE);
						mDataEntryFragment.mOccasionListviewLayout
								.setVisibility(View.GONE);
						mDataEntryFragment.mCategoryListviewLayout
								.setVisibility(View.GONE);
						mDataEntryFragment.mSelectCategoryLayout
								.setVisibility(View.GONE);
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
						mDataEntryFragment.mSaySomethingAboutAisle
								.setVisibility(View.VISIBLE);
						mDataEntryFragment.mSaysomethingClose
								.setVisibility(View.VISIBLE);
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
						mDataEntryFragment.mFindatClose
								.setVisibility(View.GONE);
						mDataEntryFragment.mFindAtPopUp
								.setVisibility(View.GONE);
						mDataEntryFragment.mCategoryListviewLayout
								.setVisibility(View.GONE);
						mDataEntryFragment.mSelectCategoryLayout
								.setVisibility(View.GONE);
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

		mVueDataentryClose.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				showDiscardOtherAppImageDialog();
			}
		});

		mVueDataentryPost.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mDataEntryFragment == null) {
					mDataEntryFragment = (DataEntryFragment) getSupportFragmentManager()
							.findFragmentById(R.id.create_aisles_view_fragment);
				}
				Utils.putTouchToChnageImagePosition(DataEntryActivity.this, -1);
				Utils.putTouchToChnageImageTempPosition(DataEntryActivity.this,
						-1);
				Utils.putTouchToChnageImageFlag(DataEntryActivity.this, false);
				mDataEntryFragment.createAisleClickFunctionality();
			}
		});

		mVueDataentryActionbarAppIconLayout
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						getSlidingMenu().toggle();
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
					mDataEntryFragment.mPreviousFindAtText = mDataEntryFragment.mFindAtText
							.getText().toString();
				}
			} catch (Exception e1) {
			}
			if (b.getString(VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_LOOKINGFOR) != null) {
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
					mDataEntryFragment.mLookingForOccasionTextview
							.setText(mDataEntryFragment.mLookingFor + " for "
									+ mDataEntryFragment.mOccasion);
					mDataEntryFragment.mOccasionText
							.setText(b
									.getString(VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_OCCASION));
				}
			}
			if (b.getString(VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_CATEGORY) != null) {
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
					mVueDataentryActionbarScreenName.setText("Delete Images");
					mDataEntryFragment.mDataEntryAislesViewpager
							.setVisibility(View.VISIBLE);
					try {
						mDataEntryFragment.mDataEntryAislesViewpager
								.setAdapter(new DataEntryAilsePagerAdapter(
										DataEntryActivity.this,
										mDataEntryFragment.mAisleImagePathList,
										false));
					} catch (Throwable e) {
						e.printStackTrace();
					}
				}

				if (!mDataEntryFragment.mIsUserAisleFlag) {
					mDataEntryFragment.mLookingForPopup
							.setVisibility(View.GONE);
					mDataEntryFragment.mLookingForListviewLayout
							.setVisibility(View.GONE);

				}
				if (b.getString(VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_SAYSOMETHINGABOUTAISLE) != null) {
					mDataEntryFragment.mSaySomethingAboutAisle
							.setText(b
									.getString(VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_SAYSOMETHINGABOUTAISLE));
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
						&& (Utils
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
					mDataEntryFragment.mMainHeadingRow
							.setVisibility(View.VISIBLE);
					mDataEntryFragment.mCategoryheadingLayout
							.setVisibility(View.VISIBLE);
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
		super.onResume();
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		Log.e("fff", "onkeyup");
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mDataEntryFragment.mCategoryListviewLayout.getVisibility() == View.VISIBLE) {
				Toast.makeText(DataEntryActivity.this,
						"Category is mandotory.", Toast.LENGTH_LONG).show();
			} else if (getSlidingMenu().isMenuShowing()) {
				if (!mFrag.listener.onBackPressed()) {
					getSlidingMenu().toggle();
				}
			} else {
				showDiscardOtherAppImageDialog();
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