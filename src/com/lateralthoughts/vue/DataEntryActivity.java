package com.lateralthoughts.vue;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

import com.flurry.android.FlurryAgent;
import com.lateralthoughts.vue.utils.FileCache;
import com.lateralthoughts.vue.utils.OtherSourceImageDetails;
import com.lateralthoughts.vue.utils.Utils;

public class DataEntryActivity extends /*Base*/Activity {

	//public boolean mIsKeyboardShownFlag = false;
	public TextView mVueDataentryActionbarScreenName,
			mActionbarDeleteBtnTextview;
	RelativeLayout mDataentryActionbarMainLayout,
			mVueDataentryActionbarAppIconLayout;
	public LinearLayout mVueDataentryKeyboardLayout, mVueDataentryPostLayout,
			mVueDataentryDeleteLayout, mVueDataentryAddimageSkipLayout;
	public FrameLayout mVueDataentryKeyboardDone, mVueDataentryKeyboardCancel,
			mVueDataentryClose, mVueDataentryPost, mVueDataentryDeleteCancel,
			mVueDataentryDeleteDone;
	private View mVueDataentryActionbarView;
	private DataEntryFragment mDataEntryFragment;
	private static final String CREATE_AISLE_SCREEN_VISITORS = "Create_Aisle_Screen_Visitors";
	public ArrayList<Integer> mDeletedImagesPositionsList = null;
	public int mDeletedImagesCount = 0;
	
	
	private com.lateralthoughts.vue.VueListFragment mSlidListFrag;
	private ProgressDialog mPd;
	private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private FrameLayout content_frame2;
 

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.date_entry_main);
		initialize();
		content_frame2 = (FrameLayout) findViewById(R.id.content_frame2);
		  mSlidListFrag = (VueListFragment) getFragmentManager()
					.findFragmentById(R.id.listfrag);
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
		mVueDataentryAddimageSkipLayout = (LinearLayout) mVueDataentryActionbarView
				.findViewById(R.id.vue_dataentry_addimage_skip_layout);
		getActionBar().setCustomView(mVueDataentryActionbarView);
		getActionBar().setDisplayShowCustomEnabled(true);
		getActionBar().setDisplayShowHomeEnabled(false);


		mVueDataentryAddimageSkipLayout
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						showDiscardOtherAppImageDialog("Do you want to cancel addImage?");
					}
				});

		mVueDataentryDeleteCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDiscardOtherAppImageDialog(null);
			}
		});
		mVueDataentryDeleteDone.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mDeletedImagesPositionsList != null
						&& mDeletedImagesPositionsList.size() > 0) {
				/*	if (mDataEntryFragment == null) {
						mDataEntryFrag = (DataEntryFragment) getSupportFragmentManager()
								.findFragmentById(
										R.id.create_aisles_view_fragment);
					}*/
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
			/*	if (mDataEntryFragment == null) {
					mDataEntryFragment = (DataEntryFragment) getSupportFragmentManager()
							.findFragmentById(R.id.create_aisles_view_fragment);
				}*/
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
			/*	if (mDataEntryFragment == null) {
					mDataEntryFragment = (DataEntryFragment) getSupportFragmentManager()
							.findFragmentById(R.id.create_aisles_view_fragment);
				}*/
				mDataEntryFragment.hideAllEditableTextboxes();
			}
		});

		mVueDataentryClose.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				showDiscardOtherAppImageDialog(null);
			}
		});

		mVueDataentryPost.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				/*if (mDataEntryFragment == null) {
					mDataEntryFragment = (DataEntryFragment) getSupportFragmentManager()
							.findFragmentById(R.id.create_aisles_view_fragment);
				}*/
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
						//getSlidingMenu().toggle();
					}
				});

		Bundle b = getIntent().getExtras();
		if (b != null) {
			Log.e("cs", "30");
			String aisleImagePath = b
					.getString(VueConstants.CREATE_AISLE_CAMERA_GALLERY_IMAGE_PATH_BUNDLE_KEY);
			/*if (mDataEntryFragment == null) {
				mDataEntryFragment = (DataEntryFragment) getSupportFragmentManager()
						.findFragmentById(R.id.create_aisles_view_fragment);
			}*/
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
						false,this);
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
		if (mVueDataentryActionbarScreenName != null
				&& mVueDataentryActionbarScreenName.getText() != null
				&& mVueDataentryActionbarScreenName
						.getText()
						.toString()
						.trim()
						.equals(getResources().getString(
								R.string.add_imae_to_aisle_screen_title))) {
			mVueDataentryAddimageSkipLayout.setVisibility(View.VISIBLE);
		}
	}
	private void initialize(){
	    mTitle = mDrawerTitle = getTitle();
	    mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
	    // set a custom shadow that overlays the main content when the drawer opens
	    mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
	    // set up the drawer's list view with items and click listener


	    // enable ActionBar app icon to behave as action to toggle nav drawer
	    getActionBar().setDisplayHomeAsUpEnabled(true);
	    getActionBar().setHomeButtonEnabled(true);

	    // ActionBarDrawerToggle ties together the the proper interactions
	    // between the sliding drawer and the action bar app icon
	    mDrawerToggle = new ActionBarDrawerToggle(
	            this,                  /* host Activity */
	            mDrawerLayout,         /* DrawerLayout object */
	            R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
	            R.string.drawer_open,  /* "open drawer" description for accessibility */
	            R.string.drawer_close  /* "close drawer" description for accessibility */
	            ) {
	        public void onDrawerClosed(View view) {
	            getActionBar().setTitle(mTitle);
	            invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
	            getActionBar().setCustomView(mVueDataentryActionbarView);
	    		getActionBar().setDisplayShowCustomEnabled(true);
	    		getActionBar().setDisplayShowHomeEnabled(false);
	        }
	       
	        public void onDrawerOpened(View drawerView) {
	            getActionBar().setTitle(mDrawerTitle); 
	            invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
	            
	   		 
			    // add the custom view to the action bar
			 getActionBar().setCustomView(R.layout.actionbar_view); 
			 getActionBar().getCustomView().findViewById(R.id.home).setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					mDrawerLayout.closeDrawer(content_frame2);
					
				}
			});
			 final EditText searchEdit =(EditText)getActionBar().getCustomView().findViewById(R.id.searchfield);
			 searchEdit.setActivated(true);
			 getActionBar().getCustomView().findViewById(R.id.search_cancel).setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						 
						searchEdit.setText("");
					}
				});
			    EditText search = (EditText) getActionBar().getCustomView().findViewById(R.id.searchfield);
			    search.setOnEditorActionListener(new OnEditorActionListener() {

			      @Override
			      public boolean onEditorAction(TextView v, int actionId,
			          KeyEvent event) {
			    /*    Toast.makeText( VueLandingPageActivity.this, "Search triggered",
			            Toast.LENGTH_LONG).show();*/
			        return false;
			      }
			    });
		 
	        }
	    };
	    mDrawerLayout.setDrawerListener(mDrawerToggle);
	    mDataEntryFragment =(DataEntryFragment) getFragmentManager()
				.findFragmentById(
						R.id.create_aisles_view_fragment);
	    
	    		/* new  DataEntryFragment();
		FragmentManager fragmentManager = getFragmentManager();
		fragmentManager.beginTransaction()
				.replace(R.id.content_frame, mDataEntryFragment).commit();*/
		mDrawerLayout.setFocusableInTouchMode(false);
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
				/*	if (mDataEntryFragment == null) {
						mDataEntryFragment = (DataEntryFragment) getSupportFragmentManager()
								.findFragmentById(
										R.id.create_aisles_view_fragment);
					}*/
					if (Utils.getTouchToChangeFlag(DataEntryActivity.this)) {
						Utils.putTouchToChnageImagePosition(
								DataEntryActivity.this,
								Utils.getTouchToChangeTempPosition(DataEntryActivity.this));
					}
					mDataEntryFragment.mFindAtText.setText("");
					mDataEntryFragment.mOtherSourceSelectedImageStore = "UnKnown";
					mDataEntryFragment.mOtherSourceSelectedImageUrl = null;
					mDataEntryFragment
							.setGalleryORCameraImage(imagePath, false,this);
				}
			} else if (requestCode == VueConstants.INVITE_FRIENDS_LOGINACTIVITY_REQUEST_CODE
					&& resultCode == VueConstants.INVITE_FRIENDS_LOGINACTIVITY_REQUEST_CODE) {
				if (data != null) {
					if (data.getStringExtra(VueConstants.INVITE_FRIENDS_LOGINACTIVITY_BUNDLE_STRING_KEY) != null) {
					/*	mFrag.getFriendsList(data
								.getStringExtra(VueConstants.INVITE_FRIENDS_LOGINACTIVITY_BUNDLE_STRING_KEY));*/
					}
				}
			} else {
				try {
				/*	if (mDataEntryFragment == null) {
						mDataEntryFragment = (DataEntryFragment) getSupportFragmentManager()
								.findFragmentById(
										R.id.create_aisles_view_fragment);
					}*/
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
		//getSlidingMenu().toggle();
	}

	@Override
	public void onResume() {
		super.onResume();
		mSlidListFrag.setEditTextVisible(false);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		Log.e("fff", "onkeyup");
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mDrawerLayout.isDrawerOpen(content_frame2)) {

				if (!mSlidListFrag.listener.onBackPressed()) {
					mDrawerLayout.closeDrawer(content_frame2);
				}

			} else {
				showDiscardOtherAppImageDialog(null);
			}
		}
		return false;

	}

	private void showDiscardOtherAppImageDialog(
			final String addImageCancelAlertMesg) {
		final Dialog dialog = new Dialog(this, R.style.Theme_Dialog_Translucent);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.vue_popup);
		final TextView noButton = (TextView) dialog.findViewById(R.id.nobutton);
		TextView yesButton = (TextView) dialog.findViewById(R.id.okbutton);
		TextView messagetext = (TextView) dialog.findViewById(R.id.messagetext);
		if (addImageCancelAlertMesg != null) {
			messagetext.setText(addImageCancelAlertMesg);
		} else {
			messagetext.setText(getResources().getString(
					R.string.discard_dataentry_screen_changes));
		}
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
				if (addImageCancelAlertMesg != null) {
					Utils.putTouchToChnageImagePosition(DataEntryActivity.this,
							-1);
					Utils.putTouchToChnageImageTempPosition(
							DataEntryActivity.this, -1);
					Utils.putTouchToChnageImageFlag(DataEntryActivity.this,
							false);
				/*	if (mDataEntryFragment == null) {
						mDataEntryFragment = (DataEntryFragment) getSupportFragmentManager()
								.findFragmentById(
										R.id.create_aisles_view_fragment);
					}*/
					mDataEntryFragment
							.addImageToAisleButtonClickFunctionality(true);
				}
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