package com.lateralthoughts.vue;

import java.util.ArrayList;

import com.flurry.android.FlurryAgent;
import com.lateralthoughts.vue.utils.OtherSourceImageDetails;
import com.lateralthoughts.vue.utils.Utils;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DataEntryActivity extends BaseActivity {

	public boolean mIsKeyboardShownFlag = false;
	public TextView mVueDataentryActionbarScreenName;
	private RelativeLayout mVueDataentryActionbarCloseIconLayout,
			mVueDataentryActionbarCreateAisleIconLayout,
			mVueDataentryActionbarShareIconLayout,
			mVueDataentryActionbarEditIconLayout,
			mVueDataentryActionbarAddImageIconLayout,
			mVueDataentryActionbarAppIconLayout;
	public LinearLayout mVueDataentryActionbarTopLayout,
			mVueDataentryActionbarBottomLayout;
	private View mVueDataentryActionbarView;
	private DataEntryFragment mDataEntryFragment;
	private static final String CREATE_AISLE_SCREEN_VISITORS = "Create_Aisle_Screen_Visitors";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e("Land", "vueland 64");
		setContentView(R.layout.date_entry_main);
		mVueDataentryActionbarView = LayoutInflater.from(this).inflate(
				R.layout.vue_dataentry_actionbar, null);
		mVueDataentryActionbarScreenName = (TextView) mVueDataentryActionbarView
				.findViewById(R.id.vue_dataentry_actionbar_screenname);
		mVueDataentryActionbarCloseIconLayout = (RelativeLayout) mVueDataentryActionbarView
				.findViewById(R.id.vue_dataentry_actionbar_close_icon_layout);
		mVueDataentryActionbarCreateAisleIconLayout = (RelativeLayout) mVueDataentryActionbarView
				.findViewById(R.id.vue_dataentry_actionbar_createaisle_icon_layout);
		mVueDataentryActionbarShareIconLayout = (RelativeLayout) mVueDataentryActionbarView
				.findViewById(R.id.vue_dataentry_actionbar_share_icon_layout);
		mVueDataentryActionbarEditIconLayout = (RelativeLayout) mVueDataentryActionbarView
				.findViewById(R.id.vue_dataentry_actionbar_edit_icon_layout);
		mVueDataentryActionbarAddImageIconLayout = (RelativeLayout) mVueDataentryActionbarView
				.findViewById(R.id.vue_dataentry_actionbar_addimage_icon_layout);
		mVueDataentryActionbarTopLayout = (LinearLayout) mVueDataentryActionbarView
				.findViewById(R.id.vue_dataentry_actionbar_top_layout);
		mVueDataentryActionbarBottomLayout = (LinearLayout) mVueDataentryActionbarView
				.findViewById(R.id.vue_dataentry_actionbar_bottom_layout);
		mVueDataentryActionbarAppIconLayout = (RelativeLayout) mVueDataentryActionbarView
				.findViewById(R.id.vue_dataentry_actionbar_app_icon_layout);
		mVueDataentryActionbarScreenName.setText(getResources().getString(
				R.string.create_aisle_screen_title));
		getSupportActionBar().setCustomView(mVueDataentryActionbarView);
		getSupportActionBar().setDisplayShowCustomEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		mVueDataentryActionbarAppIconLayout
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						getSlidingMenu().toggle();
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
						mDataEntryFragment
								.addImageToAisleButtonClickFunctionality();
					}
				});
		mVueDataentryActionbarCloseIconLayout
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						Utils.putDataentryScreenAisleId(DataEntryActivity.this,
								null);
						Utils.putDataentryAddImageAisleFlag(
								DataEntryActivity.this, false);
						Utils.putDataentryEditAisleFlag(DataEntryActivity.this,
								false);
						VueApplication.getInstance().mAisleImagePathList
								.clear();
						finish();
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
			if (mDataEntryFragment.mFromDetailsScreenFlag) {
				mVueDataentryActionbarScreenName.setText(getResources()
						.getString(R.string.add_imae_to_aisle_screen_title));
				mDataEntryFragment.mIsUserAisleFlag = b
						.getBoolean(VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_IS_USER_AISLE_FLAG);
				if (mDataEntryFragment.mIsUserAisleFlag) {
					mDataEntryFragment.mLookingForPopup
							.setVisibility(View.GONE);
					mDataEntryFragment.mLookingForBigText
							.setBackgroundColor(Color.TRANSPARENT);
					mDataEntryFragment.mLookingForListviewLayout
							.setVisibility(View.GONE);
					mDataEntryFragment.mLookingForBigText.setClickable(false);
					mDataEntryFragment.mOccassionBigText.setClickable(false);
					mDataEntryFragment.mCategoryIcon.setClickable(false);
					mDataEntryFragment.mSaySomeThingEditParent
							.setClickable(false);
				}
				if (b.getString(VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_LOOKINGFOR) != null) {
					mDataEntryFragment.mLookingForBigText
							.setText(b
									.getString(VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_LOOKINGFOR));
					mDataEntryFragment.mLookingForText
							.setText(b
									.getString(VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_LOOKINGFOR));
				}
				if (b.getString(VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_OCCASION) != null) {
					mDataEntryFragment.mOccassionBigText
							.setText(b
									.getString(VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_OCCASION));
					mDataEntryFragment.mOccasionText
							.setText(b
									.getString(VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_OCCASION));
				}
				if (b.getString(VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_SAYSOMETHINGABOUTAISLE) != null) {
					mDataEntryFragment.mSaySomethingAboutAisle
							.setText(b
									.getString(VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_SAYSOMETHINGABOUTAISLE));
				}
				if (b.getString(VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_FINDAT) != null) {
					mDataEntryFragment.mFindAtText
							.setText(b
									.getString(VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_FINDAT));
				}
				if (b.getString(VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_CATEGORY) != null) {
					mDataEntryFragment.mCategoryText
							.setText(b
									.getString(VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_CATEGORY));
				}
			}
			Log.e("cs", "32");
			if (aisleImagePath != null)
				mDataEntryFragment.setGalleryORCameraImage(aisleImagePath,
						false);
			if (b.getBoolean(VueConstants.FROM_OTHER_SOURCES_FLAG)) {
				mDataEntryFragment.mCreateAisleBg.setVisibility(View.GONE);
				mDataEntryFragment.mAisleBgProgressbar.setVisibility(View.GONE);
				if (!mDataEntryFragment.mFromDetailsScreenFlag) {
					if (VueApplication.getInstance().mAisleImagePathList != null
							&& VueApplication.getInstance().mAisleImagePathList
									.size() > 0) {
						mVueDataentryActionbarScreenName
								.setText(getResources()
										.getString(
												R.string.add_imae_to_aisle_screen_title));
					}
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
					mDataEntryFragment
							.showOtherSourcesGridview(otherSourcesImageDetailsList);
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
			if (getSlidingMenu().isMenuShowing()) {
				if (!mFrag.listener.onBackPressed()) {
					getSlidingMenu().toggle();
				}
			} else {
				Utils.putDataentryScreenAisleId(this, null);
				VueApplication.getInstance().mAisleImagePathList.clear();
				super.onBackPressed();
			}
		}
		return false;

	}

}
