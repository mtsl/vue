package com.lateralthoughts.vue;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.lateralthoughts.vue.utils.FileCache;
import com.lateralthoughts.vue.utils.OtherSourceImageDetails;
import com.lateralthoughts.vue.utils.Utils;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

public class DataEntryActivity extends Activity {
    
    public TextView mActionbarDeleteBtnTextview;
    public LinearLayout mVueDataentryKeyboardLayout, mVueDataentryPostLayout,
            mVueDataentryDeleteLayout;
    public FrameLayout mVueDataentryKeyboardDone, mVueDataentryKeyboardCancel,
            mVueDataentryClose, mVueDataentryPost, mVueDataentryDeleteCancel,
            mVueDataentryDeleteDone, mActionbarNext;
    private View mVueDataentryActionbarView;
    private DataEntryFragment mDataEntryFragment;
    private static final String CREATE_AISLE_SCREEN_VISITORS = "Create_Aisle_Screen_Visitors";
    public ArrayList<Integer> mDeletedImagesPositionsList = null;
    public int mDeletedImagesCount = 0;
    private com.lateralthoughts.vue.VueListFragment mSlidListFrag;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private FrameLayout mContentFrame;
    private boolean mHideDefaultActionbar = false;
    private boolean mShowSkipButton = false;
    private MixpanelAPI mixpanel;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.date_entry_main);
        mixpanel = MixpanelAPI.getInstance(this,
                VueApplication.getInstance().MIXPANEL_TOKEN);
        initialize();
        mContentFrame = (FrameLayout) findViewById(R.id.content_frame2);
        mSlidListFrag = (VueListFragment) getFragmentManager()
                .findFragmentById(R.id.listfrag);
        mVueDataentryActionbarView = LayoutInflater.from(this).inflate(
                R.layout.vue_dataentry_custom_actionbar, null);
        getActionBar().setTitle(
                getResources().getString(R.string.create_aisle_screen_title));
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
        mActionbarNext = (FrameLayout) mVueDataentryActionbarView
                .findViewById(R.id.actionbar_next);
        invalidateOptionsMenu();
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
                        mVueDataentryKeyboardLayout.setVisibility(View.VISIBLE);
                        mVueDataentryKeyboardDone.setVisibility(View.VISIBLE);
                        mVueDataentryKeyboardCancel.setVisibility(View.VISIBLE);
                        mHideDefaultActionbar = true;
                        invalidateOptionsMenu();
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
                        mVueDataentryKeyboardLayout.setVisibility(View.VISIBLE);
                        mVueDataentryKeyboardDone.setVisibility(View.VISIBLE);
                        mVueDataentryKeyboardCancel.setVisibility(View.VISIBLE);
                        mHideDefaultActionbar = true;
                        invalidateOptionsMenu();
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
                Utils.putTouchToChnageImagePosition(DataEntryActivity.this, -1);
                Utils.putTouchToChnageImageTempPosition(DataEntryActivity.this,
                        -1);
                Utils.putTouchToChnageImageFlag(DataEntryActivity.this, false);
                mDataEntryFragment.createAisleClickFunctionality();
            }
        });
        mActionbarNext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mDataEntryFragment.showAddMoreImagesDialog();
            }
        });
        Bundle b = getIntent().getExtras();
        if (b != null) {
            String aisleImagePath = b
                    .getString(VueConstants.CREATE_AISLE_CAMERA_GALLERY_IMAGE_PATH_BUNDLE_KEY);
            mDataEntryFragment.mFromDetailsScreenFlag = b.getBoolean(
                    VueConstants.FROM_DETAILS_SCREEN_TO_DATAENTRY_SCREEN_FLAG,
                    false);
            try {
                if (b.getString(VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_FINDAT) != null) {
                    mDataEntryFragment.mFindAtText
                            .setText(b
                                    .getString(VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_FINDAT));
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
                mDataEntryFragment.mOccasionLayout.setVisibility(View.GONE);
                mDataEntryFragment.mFindatLayout.setVisibility(View.GONE);
                mDataEntryFragment.mSaySomethingAboutAisle
                        .setVisibility(View.GONE);
                mDataEntryFragment.mSaysomethingClose.setVisibility(View.GONE);
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
                if (firstTimeFlag) {
                    mDataEntryFragment
                            .showDetailsScreenImagesInDataentryScreen();
                }
                getActionBar().setTitle(
                        getResources().getString(
                                R.string.add_imae_to_aisle_screen_title));
                if (b.getBoolean(VueConstants.EDIT_IMAGE_FROM_DETAILS_SCREEN_FALG)) {
                    getActionBar().setTitle("Delete Images");
                    mDataEntryFragment.mOccasionLayout.setVisibility(View.GONE);
                    mDataEntryFragment.mFindatLayout.setVisibility(View.GONE);
                    mDataEntryFragment.mSaySomethingAboutAisle
                            .setVisibility(View.GONE);
                    mDataEntryFragment.mSaysomethingClose
                            .setVisibility(View.GONE);
                    mDataEntryFragment.mLookingForPopup
                            .setVisibility(View.GONE);
                    mDataEntryFragment.mLookingForListviewLayout
                            .setVisibility(View.GONE);
                    mDataEntryFragment.mMainHeadingRow.setClickable(false);
                    mDataEntryFragment.mMainHeadingRow.setEnabled(false);
                    mVueDataentryKeyboardLayout.setVisibility(View.GONE);
                    mVueDataentryKeyboardDone.setVisibility(View.GONE);
                    mVueDataentryKeyboardCancel.setVisibility(View.GONE);
                    mVueDataentryPostLayout.setVisibility(View.GONE);
                    mVueDataentryDeleteLayout.setVisibility(View.GONE);
                    showDefaultActionbar();
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
                        false, this);
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
                        getActionBar()
                                .setTitle(
                                        getResources()
                                                .getString(
                                                        R.string.add_imae_to_aisle_screen_title));
                    }
                }
                if (Utils
                        .getDataentryTopAddImageAisleFlag(DataEntryActivity.this)) {
                    mVueDataentryKeyboardLayout.setVisibility(View.GONE);
                    mVueDataentryKeyboardDone.setVisibility(View.GONE);
                    mVueDataentryKeyboardCancel.setVisibility(View.GONE);
                    mVueDataentryPostLayout.setVisibility(View.VISIBLE);
                    hideDefaultActionbar();
                }
                if (Utils.getDataentryAddImageAisleFlag(DataEntryActivity.this)) {
                    getActionBar().setTitle(
                            getResources().getString(
                                    R.string.add_imae_to_aisle_screen_title));
                    mDataEntryFragment.mMainHeadingRow
                            .setVisibility(View.VISIBLE);
                    mDataEntryFragment.mCategoryheadingLayout
                            .setVisibility(View.VISIBLE);
                }
                if (b.getParcelableArrayList(VueConstants.FROM_OTHER_SOURCES_IMAGE_URIS) != null) {
                    ArrayList<Uri> imageUrisList = b
                            .getParcelableArrayList(VueConstants.FROM_OTHER_SOURCES_IMAGE_URIS);
                    ArrayList<OtherSourceImageDetails> otherSourcesImageDetailsList = new ArrayList<OtherSourceImageDetails>();
                    for (int i = 0; i < imageUrisList.size(); i++) {
                        OtherSourceImageDetails otherSourceImageDetails = new OtherSourceImageDetails(
                                null, null, null, 0, 0, imageUrisList.get(i), 0);
                        otherSourcesImageDetailsList
                                .add(otherSourceImageDetails);
                    }
                    String sourceUrl = "";
                    if (b.getString(VueConstants.FROM_OTHER_SOURCES_URL) != null) {
                        sourceUrl = b
                                .getString(VueConstants.FROM_OTHER_SOURCES_URL);
                    }
                    mDataEntryFragment.showOtherSourcesGridview(
                            otherSourcesImageDetailsList, sourceUrl);
                } else if (b.getString(VueConstants.FROM_OTHER_SOURCES_URL) != null) {
                    mDataEntryFragment.getImagesFromUrl(b
                            .getString(VueConstants.FROM_OTHER_SOURCES_URL));
                }
            }
            if (b.getString(VueConstants.IMAGE_FROM) != null
                    && b.getString(VueConstants.IMAGE_FROM).equals(
                            VueConstants.GALLERY_IMAGE)) {
                try {
                    createAisleProps.put(VueConstants.IMAGE_FROM,
                            VueConstants.GALLERY_IMAGE);
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            } else if (b.getString(VueConstants.IMAGE_FROM) != null
                    && b.getString(VueConstants.IMAGE_FROM).equals(
                            VueConstants.CAMERA_IMAGE)) {
                try {
                    createAisleProps.put(VueConstants.IMAGE_FROM,
                            VueConstants.CAMERA_IMAGE);
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        } else {
            if (mDataEntryFragment.mAisleImagePathList.size() == 0) {
                mDataEntryFragment.mAddAnItemToAisle
                        .setVisibility(View.VISIBLE);
                mDataEntryFragment.lookingForTextClickFunctionality();
            }
        }
    }
    
    private void initialize() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setBackgroundDrawable(
                getResources().getDrawable(R.drawable.actionbar_bg));
        mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
        mDrawerLayout, /* DrawerLayout object */
        R.drawable.ic_navigation_drawer, R.string.drawer_open, /*
                                                                * "open drawer"
                                                                * description
                                                                * for
                                                                * accessibility
                                                                */
        R.string.drawer_close /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                invalidateOptionsMenu();
            }
            
            @SuppressLint("CutPasteId")
            public void onDrawerOpened(View drawerView) {
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDataEntryFragment = (DataEntryFragment) getFragmentManager()
                .findFragmentById(R.id.create_aisles_view_fragment);
        mDrawerLayout.setFocusableInTouchMode(false);
    }
    
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        } else if (item.getItemId() == R.id.menu_close_dataentry) {
            showDiscardOtherAppImageDialog("Do you want to cancel addImage?");
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dataentry_actionbar, menu);
        getActionBar().setHomeButtonEnabled(true);
        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean isdrawOpen = mDrawerLayout.isDrawerOpen(mContentFrame);
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setDisplayShowCustomEnabled(false);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setDisplayShowHomeEnabled(true);
        getActionBar().setCustomView(null);
        getActionBar().setDisplayShowTitleEnabled(true);
        if (isdrawOpen) {
            // set menu search visibility to true when backend functionality is
            // ready
            menu.findItem(R.id.menu_search).setVisible(false);
            menu.findItem(R.id.menu_close_dataentry).setVisible(false);
        } else {
            if (mHideDefaultActionbar) {
                mShowSkipButton = false;
                getActionBar().setDisplayShowTitleEnabled(false);
                getActionBar().setDisplayHomeAsUpEnabled(false);
                getActionBar().setDisplayShowCustomEnabled(true);
                getActionBar().setDisplayShowHomeEnabled(false);
                getActionBar().setCustomView(mVueDataentryActionbarView);
                menu.findItem(R.id.menu_search).setVisible(false);
                menu.findItem(R.id.menu_close_dataentry).setVisible(false);
            } else if (mShowSkipButton) {
                menu.findItem(R.id.menu_search).collapseActionView();
                menu.findItem(R.id.menu_search).setVisible(false);
                menu.findItem(R.id.menu_close_dataentry).setVisible(true);
            } else {
                menu.findItem(R.id.menu_search).collapseActionView();
                menu.findItem(R.id.menu_search).setVisible(false);
                menu.findItem(R.id.menu_close_dataentry).setVisible(false);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }
    
    @Override
    protected void onStart() {
        mixpanel.track(CREATE_AISLE_SCREEN_VISITORS, null);
        FlurryAgent.onStartSession(this, Utils.FLURRY_APP_KEY);
        FlurryAgent.onPageView();
        FlurryAgent.logEvent(CREATE_AISLE_SCREEN_VISITORS);
        super.onStart();
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        FlurryAgent.onEndSession(this);
        mixpanel.flush();
        
    }
    
    JSONObject createAisleProps = new JSONObject();
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Iterator<?> keys = createAisleProps.keys();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            createAisleProps.remove(key);
        }
        
        try {
            if (requestCode == VueConstants.CREATE_AILSE_ACTIVITY_RESULT) {
                Bundle b = data.getExtras();
                if (b != null) {
                    String imagePath = b
                            .getString(VueConstants.CREATE_AISLE_CAMERA_GALLERY_IMAGE_PATH_BUNDLE_KEY);
                    if (Utils.getTouchToChangeFlag(DataEntryActivity.this)) {
                        Utils.putTouchToChnageImagePosition(
                                DataEntryActivity.this,
                                Utils.getTouchToChangeTempPosition(DataEntryActivity.this));
                    }
                    mDataEntryFragment.mFindAtText.setText("");
                    mDataEntryFragment.mOtherSourceSelectedImageStore = "UnKnown";
                    mDataEntryFragment.mOtherSourceSelectedImageUrl = null;
                    mDataEntryFragment.mAddAnItemToAisle
                            .setVisibility(View.GONE);
                    mDataEntryFragment.setGalleryORCameraImage(imagePath,
                            false, this);
                }
            } else if (requestCode == VueConstants.INVITE_FRIENDS_LOGINACTIVITY_REQUEST_CODE
                    && resultCode == VueConstants.INVITE_FRIENDS_LOGINACTIVITY_REQUEST_CODE) {
                if (data != null) {
                    if (data.getStringExtra(VueConstants.INVITE_FRIENDS_LOGINACTIVITY_BUNDLE_STRING_KEY) != null) {
                        mSlidListFrag
                                .getFriendsList(data
                                        .getStringExtra(VueConstants.INVITE_FRIENDS_LOGINACTIVITY_BUNDLE_STRING_KEY));
                    }
                }
            } else {
                try {
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
    
    @Override
    public void onResume() {
        super.onResume();
        mSlidListFrag.setEditTextVisible(false);
    }
    
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mDrawerLayout.isDrawerOpen(mContentFrame)) {
                
                if (!mSlidListFrag.listener.onBackPressed()) {
                    mDrawerLayout.closeDrawer(mContentFrame);
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
        finish();
    }
    
    public void showDefaultActionbar() {
        mHideDefaultActionbar = false;
        invalidateOptionsMenu();
    }
    
    public void hideDefaultActionbar() {
        mHideDefaultActionbar = true;
        invalidateOptionsMenu();
    }
    
    public void showDefaultActionbarWithSkipButton() {
        mShowSkipButton = true;
        mHideDefaultActionbar = false;
        invalidateOptionsMenu();
    }
}