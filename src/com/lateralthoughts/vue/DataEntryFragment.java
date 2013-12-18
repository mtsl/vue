package com.lateralthoughts.vue;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.lateralthoughts.vue.AisleManager.ImageAddedCallback;
import com.lateralthoughts.vue.AisleManager.ImageUploadCallback;
import com.lateralthoughts.vue.ShareDialog.ShareViaVueClickedListner;
import com.lateralthoughts.vue.connectivity.AisleData;
import com.lateralthoughts.vue.connectivity.DataBaseManager;
import com.lateralthoughts.vue.connectivity.VueConnectivityManager;
import com.lateralthoughts.vue.domain.Aisle;
import com.lateralthoughts.vue.domain.Image;
import com.lateralthoughts.vue.domain.VueImage;
import com.lateralthoughts.vue.utils.BitmapLoaderUtils;
import com.lateralthoughts.vue.utils.FileCache;
import com.lateralthoughts.vue.utils.GetOtherSourceImagesTask;
import com.lateralthoughts.vue.utils.OtherSourceImageDetails;
import com.lateralthoughts.vue.utils.Utils;

/**
 * Fragment for creating Aisle, Updating Ailse and AddingImageToAisle
 * 
 */
public class DataEntryFragment extends Fragment {
    private ListView mCategoryListview = null, mLookingForListview = null,
            mOccasionListview = null;
    public TextView mCategoryheading;
    public LinearLayout mLookingForListviewLayout = null,
            mOccasionPopup = null, mFindAtPopUp,
            mCategoryListviewLayout = null, mOccasionListviewLayout = null;
    public LinearLayout mLookingForPopup = null, mCategoryheadingLayout;
    public TextView mLookingForOccasionTextview;
    public EditText mLookingForText = null, mOccasionText = null,
            mSaySomethingAboutAisle = null, mFindAtText = null;
    private String mCategoryitemsArray[] = null;
    private int mCategoryitemsDrawablesArray[] = { R.drawable.new_apparel,
            R.drawable.new_beauty, R.drawable.new_electronics,
            R.drawable.new_entertainment, R.drawable.new_events,
            R.drawable.new_food, R.drawable.new_home };
    InputMethodManager mInputMethodManager;
    private String mImagePath = null, mResizedImagePath = null;
    LinearLayout mMainHeadingRow = null;
    public ShareDialog mShare = null;
    private float mScreenHeight = 0, mScreenWidth = 0;
    ViewPager mDataEntryAislesViewpager = null;
    public static final int AISLE_IMAGE_MARGIN = 96;
    public static final String LOOKING_FOR = "Looking";
    public static final String OCCASION = "Occasion";
    public static final String CATEGORY = "Category";
    public static final String FINDAT = "findat";
    public static final String SAY_SOMETHING_ABOUT_AISLE = "SaysomethingAboutAisle";
    private ArrayList<String> mLookingForAisleKeywordsList = null,
            mOccassionAisleKeywordsList = null;
    private DataBaseManager mDbManager;
    private View mDataEntryFragmentView;
    private ImageResizeAsynTask mImageResizeAsynTask = null;
    private ImageView mOccasionClose;
    ImageView mFindatClose;
    private ImageView mLookingforClose;
    ImageView mSaysomethingClose;
    public boolean mFromDetailsScreenFlag = false;
    public boolean mIsUserAisleFlag = false;
    private LoginWarningMessage mLoginWarningMessage = null;
    private OtherSourcesDialog mOtherSourcesDialog = null;
    private ProgressDialog mProgressDialog;
    private DataEntryActivity mDataEntryActivity;
    public int mOtherSourceImageOriginalHeight, mOtherSourceImageOriginalWidth;
    public String mOtherSourceSelectedImageUrl = null,
            mOtherSourceSelectedImageDetailsUrl = null,
            mOtherSourceSelectedImageStore = null;
    ArrayList<DataentryImage> mAisleImagePathList = null;
    String mLookingFor = null;
    String mOccasion = null;
    LinearLayout mSelectCategoryLayout;
    private Context mContext;
    RelativeLayout mOccasionLayout = null;
    RelativeLayout mFindatLayout = null;
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mCategoryitemsArray = getResources().getStringArray(
                R.array.category_items_array);
        
        mDbManager = DataBaseManager.getInstance(getActivity());
        DisplayMetrics dm = getResources().getDisplayMetrics();
        mScreenHeight = dm.heightPixels;
        mScreenHeight = mScreenHeight
                - Utils.dipToPixels(getActivity(), AISLE_IMAGE_MARGIN);
        mScreenWidth = dm.widthPixels;
        mDataEntryFragmentView = inflater.inflate(R.layout.data_entry_fragment,
                container, false);
        mInputMethodManager = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        mLookingForText = (EditText) mDataEntryFragmentView
                .findViewById(R.id.lookingfortext);
        mFindAtPopUp = (LinearLayout) mDataEntryFragmentView
                .findViewById(R.id.find_at_popup);
        mOccasionLayout = (RelativeLayout) mDataEntryFragmentView
                .findViewById(R.id.occasion_layout);
        mFindatLayout = (RelativeLayout) mDataEntryFragmentView
                .findViewById(R.id.findat_layout);
        mCategoryheadingLayout = (LinearLayout) mDataEntryFragmentView
                .findViewById(R.id.categoryheading);
        mCategoryheading = (TextView) mDataEntryFragmentView
                .findViewById(R.id.category_heading_textview);
        mOccasionClose = (ImageView) mDataEntryFragmentView
                .findViewById(R.id.occasionclose);
        mLookingforClose = (ImageView) mDataEntryFragmentView
                .findViewById(R.id.lookingforclose);
        mFindatClose = (ImageView) mDataEntryFragmentView
                .findViewById(R.id.findatclose);
        mDataEntryAislesViewpager = (ViewPager) mDataEntryFragmentView
                .findViewById(R.id.dataentry_aisles_viewpager);
        mOccasionListviewLayout = (LinearLayout) mDataEntryFragmentView
                .findViewById(R.id.ocassionlistviewlayout);
        mSaysomethingClose = (ImageView) mDataEntryFragmentView
                .findViewById(R.id.saysomethingclose);
        mSelectCategoryLayout = (LinearLayout) mDataEntryFragmentView
                .findViewById(R.id.selectcategorylayout);
        mOccasionListview = (ListView) mDataEntryFragmentView
                .findViewById(R.id.ocassionlistview);
        mLookingForOccasionTextview = (TextView) mDataEntryFragmentView
                .findViewById(R.id.looking_for_occasion_textview);
        mLookingForListview = (ListView) mDataEntryFragmentView
                .findViewById(R.id.lookingforlistview);
        mFindAtText = (EditText) mDataEntryFragmentView
                .findViewById(R.id.find_at_text);
        mOccasionPopup = (LinearLayout) mDataEntryFragmentView
                .findViewById(R.id.ocassionpopup);
        mOccasionText = (EditText) mDataEntryFragmentView
                .findViewById(R.id.occasiontext);
        mLookingForListviewLayout = (LinearLayout) mDataEntryFragmentView
                .findViewById(R.id.lookingforlistviewlayout);
        mLookingForPopup = (LinearLayout) mDataEntryFragmentView
                .findViewById(R.id.lookingforpopup);
        mSaySomethingAboutAisle = (EditText) mDataEntryFragmentView
                .findViewById(R.id.saysomethingaboutaisle);
        mCategoryListview = (ListView) mDataEntryFragmentView
                .findViewById(R.id.categorylistview);
        mCategoryListviewLayout = (LinearLayout) mDataEntryFragmentView
                .findViewById(R.id.categorylistviewlayout);
        mMainHeadingRow = (LinearLayout) mDataEntryFragmentView
                .findViewById(R.id.mainheadingrow);
        mLookingForListviewLayout.setVisibility(View.GONE);
        mLookingForAisleKeywordsList = mDbManager
                .getAisleKeywords(VueConstants.LOOKING_FOR_TABLE);
        String savedLookingFor = Utils
                .getDataentryTopAddImageAisleLookingFor(getActivity());
        String savedCategory = Utils
                .getDataentryTopAddImageAisleCategory(getActivity());
        String savedOccasion = Utils
                .getDataentryTopAddImageAisleOccasion(getActivity());
        String savedDescription = Utils
                .getDataentryTopAddImageAisleDescription(getActivity());
        if (Utils.getDataentryTopAddImageAisleFlag(mContext)
                && savedLookingFor != null
                && savedLookingFor.trim().length() > 0) {
            mLookingForText.setText(savedLookingFor);
            mLookingFor = savedLookingFor;
        } else if (mLookingForAisleKeywordsList != null) {
            mLookingForText.setText(mLookingForAisleKeywordsList.get(0));
            mLookingFor = mLookingForAisleKeywordsList.get(0);
        } else {
            mLookingForListviewLayout.setVisibility(View.GONE);
            mLookingForText.requestFocus();
        }
        mOccassionAisleKeywordsList = mDbManager
                .getAisleKeywords(VueConstants.OCCASION_TABLE);
        if (Utils.getDataentryTopAddImageAisleFlag(mContext)
                && savedOccasion != null && savedOccasion.trim().length() > 0) {
            mOccasionText.setText(savedOccasion);
            mOccasion = savedOccasion;
        } else if (mOccassionAisleKeywordsList != null) {
            mOccasionText.setText(mOccassionAisleKeywordsList.get(0));
            mOccasion = mOccassionAisleKeywordsList.get(0);
        }
        if (mLookingFor != null) {
            mMainHeadingRow.setVisibility(View.VISIBLE);
            if (mOccasion != null) {
                mLookingForOccasionTextview.setText(mLookingFor + " for "
                        + mOccasion);
            } else {
                mLookingForOccasionTextview.setText("Looking for "
                        + mLookingFor);
            }
        }
        if (Utils.getDataentryTopAddImageAisleFlag(mContext)
                && savedCategory != null && savedCategory.trim().length() > 0) {
            mCategoryheading.setText(savedCategory);
            mCategoryheadingLayout.setVisibility(View.VISIBLE);
        }
        if (Utils.getDataentryTopAddImageAisleFlag(mContext)
                && savedDescription != null
                && savedDescription.trim().length() > 0) {
            mSaySomethingAboutAisle.setText(savedDescription);
        }
        mSaySomethingAboutAisle
                .setOnEditorActionListener(new OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView arg0, int arg1,
                            KeyEvent arg2) {
                        mSaysomethingClose.setVisibility(View.GONE);
                        mSaySomethingAboutAisle.setCursorVisible(false);
                        mInputMethodManager.hideSoftInputFromWindow(
                                mSaySomethingAboutAisle.getWindowToken(), 0);
                        mInputMethodManager.hideSoftInputFromWindow(
                                mOccasionText.getWindowToken(), 0);
                        mInputMethodManager.hideSoftInputFromWindow(
                                mLookingForText.getWindowToken(), 0);
                        if (mDataEntryActivity == null) {
                            mDataEntryActivity = (DataEntryActivity) getActivity();
                        }
                        mDataEntryActivity.mVueDataentryKeyboardLayout
                                .setVisibility(View.GONE);
                        mDataEntryActivity.mVueDataentryKeyboardDone
                                .setVisibility(View.GONE);
                        mDataEntryActivity.mVueDataentryKeyboardCancel
                                .setVisibility(View.GONE);
                        mDataEntryActivity.mVueDataentryPostLayout
                                .setVisibility(View.VISIBLE);
                        mDataEntryActivity.hideDefaultActionbar();
                        return true;
                    }
                });
        mSaySomethingAboutAisle.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                mSaysomethingClose.setVisibility(View.VISIBLE);
                mDataEntryActivity.mVueDataentryKeyboardLayout
                        .setVisibility(View.VISIBLE);
                mDataEntryActivity.mVueDataentryKeyboardDone
                        .setVisibility(View.VISIBLE);
                mDataEntryActivity.mVueDataentryKeyboardCancel
                        .setVisibility(View.VISIBLE);
                mDataEntryActivity.mVueDataentryPostLayout
                        .setVisibility(View.GONE);
                mDataEntryActivity.hideDefaultActionbar();
            }
        });
        mLookingForText.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                mLookingForText.post(new Runnable() {
                    public void run() {
                        mLookingForText.setFocusable(true);
                        mLookingForText.requestFocus();
                    }
                    
                });
                
            }
        });
        
        mLookingForText
                .setOnEditorActionListener(new EditText.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId,
                            KeyEvent event) {
                        if (mLookingForText.getText().toString().trim()
                                .length() > 0) {
                            mLookingFor = mLookingForText.getText().toString();
                            mLookingForPopup.setVisibility(View.GONE);
                            mLookingForListviewLayout.setVisibility(View.GONE);
                            mInputMethodManager.hideSoftInputFromWindow(
                                    mLookingForText.getWindowToken(), 0);
                            mMainHeadingRow.setVisibility(View.VISIBLE);
                            if (mOccasion != null) {
                                mLookingForOccasionTextview.setText(mLookingFor
                                        + " for " + mOccasion);
                            } else {
                                mLookingForOccasionTextview
                                        .setText("Looking for " + mLookingFor);
                            }
                            mDataEntryActivity.mVueDataentryKeyboardLayout
                                    .setVisibility(View.GONE);
                            mDataEntryActivity.mVueDataentryKeyboardDone
                                    .setVisibility(View.GONE);
                            mDataEntryActivity.mVueDataentryKeyboardCancel
                                    .setVisibility(View.GONE);
                            mDataEntryActivity.showDefaultActionbar();
                            categoryIconClickFunctionality();
                        } else {
                            Toast.makeText(getActivity(),
                                    "LookingFor is mandatory.",
                                    Toast.LENGTH_LONG).show();
                        }
                        
                        return true;
                    }
                });
        mOccasionText.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView arg0, int actionId,
                    KeyEvent arg2) {
                mInputMethodManager.hideSoftInputFromWindow(
                        mOccasionText.getWindowToken(), 0);
                if (mDataEntryActivity == null) {
                    mDataEntryActivity = (DataEntryActivity) getActivity();
                }
                mDataEntryActivity.mVueDataentryKeyboardLayout
                        .setVisibility(View.GONE);
                mDataEntryActivity.mVueDataentryKeyboardDone
                        .setVisibility(View.GONE);
                mDataEntryActivity.mVueDataentryKeyboardCancel
                        .setVisibility(View.GONE);
                mDataEntryActivity.showDefaultActionbar();
                if (mOccasionText.getText().toString().trim().length() > 0) {
                    mOccasion = mOccasionText.getText().toString();
                    mLookingForOccasionTextview.setText(mLookingFor + " for "
                            + mOccasion);
                } else {
                    mOccasion = null;
                    mLookingForOccasionTextview.setText("Looking for "
                            + mLookingFor);
                }
                mOccasionPopup.setVisibility(View.GONE);
                mOccasionListviewLayout.setVisibility(View.GONE);
                mSaySomethingAboutAisle.setVisibility(View.VISIBLE);
                mSaysomethingClose.setVisibility(View.GONE);
                mDataEntryActivity.mVueDataentryKeyboardLayout
                        .setVisibility(View.GONE);
                mDataEntryActivity.mVueDataentryKeyboardDone
                        .setVisibility(View.GONE);
                mDataEntryActivity.mVueDataentryKeyboardCancel
                        .setVisibility(View.GONE);
                mDataEntryActivity.mVueDataentryPostLayout
                        .setVisibility(View.VISIBLE);
                mDataEntryActivity.hideDefaultActionbar();
                return true;
            };
        });
        mFindAtText.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView arg0, int actionId,
                    KeyEvent arg2) {
                mInputMethodManager.hideSoftInputFromWindow(
                        mFindAtText.getWindowToken(), 0);
                mOtherSourceSelectedImageDetailsUrl = mFindAtText.getText()
                        .toString();
                mFindatClose.setVisibility(View.GONE);
                mFindAtPopUp.setVisibility(View.GONE);
                mSaySomethingAboutAisle.setVisibility(View.VISIBLE);
                mSaysomethingClose.setVisibility(View.GONE);
                mInputMethodManager.hideSoftInputFromWindow(
                        mOccasionText.getWindowToken(), 0);
                mInputMethodManager.hideSoftInputFromWindow(
                        mLookingForText.getWindowToken(), 0);
                mInputMethodManager.hideSoftInputFromWindow(
                        mFindAtText.getWindowToken(), 0);
                mLookingForPopup.setVisibility(View.GONE);
                mLookingForListviewLayout.setVisibility(View.GONE);
                mOccasionPopup.setVisibility(View.GONE);
                mOccasionListviewLayout.setVisibility(View.GONE);
                mFindatClose.setVisibility(View.GONE);
                mFindAtPopUp.setVisibility(View.GONE);
                mCategoryListviewLayout.setVisibility(View.GONE);
                mSelectCategoryLayout.setVisibility(View.GONE);
                mDataEntryActivity.mVueDataentryKeyboardLayout
                        .setVisibility(View.GONE);
                mDataEntryActivity.mVueDataentryKeyboardDone
                        .setVisibility(View.GONE);
                mDataEntryActivity.mVueDataentryKeyboardCancel
                        .setVisibility(View.GONE);
                mDataEntryActivity.mVueDataentryPostLayout
                        .setVisibility(View.VISIBLE);
                mDataEntryActivity.hideDefaultActionbar();
                return true;
            };
        });
        mLookingForText.addTextChangedListener(new TextWatcher() {
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                    int count) {
                ArrayList<String> tempLookingForKeywordsList = null;
                if (mLookingForAisleKeywordsList != null
                        && mLookingForAisleKeywordsList.size() > 0) {
                    tempLookingForKeywordsList = new ArrayList<String>();
                    for (int i = 0; i < mLookingForAisleKeywordsList.size(); i++) {
                        String temp = mLookingForAisleKeywordsList.get(i);
                        if (temp.length() >= s.length()) {
                            String tempString = temp.substring(0, s.length());
                            if (tempString.equalsIgnoreCase(s.toString())) {
                                tempLookingForKeywordsList
                                        .add(mLookingForAisleKeywordsList
                                                .get(i));
                            }
                        }
                    }
                    mLookingForListview.setAdapter(new LookingForAdapter(
                            getActivity(), tempLookingForKeywordsList));
                    if (tempLookingForKeywordsList.size() > 0) {
                        if (!mFromDetailsScreenFlag) {
                            mLookingForListviewLayout
                                    .setVisibility(View.VISIBLE);
                        }
                    } else {
                        mLookingForListviewLayout.setVisibility(View.GONE);
                    }
                }
            }
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                    int after) {
            }
            
            @Override
            public void afterTextChanged(Editable arg0) {
            }
        });
        mOccasionText.addTextChangedListener(new TextWatcher() {
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                    int count) {
                ArrayList<String> tempOccassionKeywordsList = null;
                if (mOccassionAisleKeywordsList != null
                        && mOccassionAisleKeywordsList.size() > 0) {
                    tempOccassionKeywordsList = new ArrayList<String>();
                    for (int i = 0; i < mOccassionAisleKeywordsList.size(); i++) {
                        String temp = mOccassionAisleKeywordsList.get(i);
                        if (temp.length() > s.length()) {
                            String tempString = temp.substring(0, s.length());
                            if (tempString.equalsIgnoreCase(s.toString())) {
                                tempOccassionKeywordsList
                                        .add(mOccassionAisleKeywordsList.get(i));
                            }
                        }
                    }
                    if (tempOccassionKeywordsList.size() > 0) {
                        mOccasionListviewLayout.setVisibility(View.VISIBLE);
                    } else {
                        mOccasionListviewLayout.setVisibility(View.GONE);
                    }
                    mOccasionListview.setAdapter(new OccassionAdapter(
                            getActivity(), tempOccassionKeywordsList));
                }
            }
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                    int after) {
            }
            
            @Override
            public void afterTextChanged(Editable arg0) {
            }
        });
        mDataEntryAislesViewpager
                .setOnPageChangeListener(new OnPageChangeListener() {
                    
                    @Override
                    public void onPageSelected(int position) {
                        if (mAisleImagePathList != null
                                && mAisleImagePathList.size() > 0) {
                            mFindAtText.setText(mAisleImagePathList.get(
                                    position).getDetailsUrl());
                        }
                    }
                    
                    @Override
                    public void onPageScrolled(int arg0, float arg1, int arg2) {
                    }
                    
                    @Override
                    public void onPageScrollStateChanged(int arg0) {
                    }
                });
        mLookingforClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mLookingForText.setText("");
            }
        });
        mFindatClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mFindAtText.setText("");
            }
        });
        mOccasionClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mOccasionText.setText("");
            }
        });
        mSaysomethingClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mSaySomethingAboutAisle.setText("");
            }
        });
        mOccasionLayout.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View arg0) {
                if (mCategoryListviewLayout.getVisibility() == View.GONE
                        && mLookingForPopup.getVisibility() == View.GONE
                        && mFindAtPopUp.getVisibility() == View.GONE) {
                    mSaySomethingAboutAisle.setVisibility(View.GONE);
                    mSaysomethingClose.setVisibility(View.GONE);
                    mOccasionPopup.setVisibility(View.VISIBLE);
                    if (mOccassionAisleKeywordsList != null
                            && mOccassionAisleKeywordsList.size() > 0) {
                        mOccasionListviewLayout.setVisibility(View.VISIBLE);
                        mOccasionListview.setAdapter(new OccassionAdapter(
                                getActivity(), mOccassionAisleKeywordsList));
                    }
                    try {
                        mOccasionText.setSelection(mOccasionText.getText()
                                .toString().length());
                    } catch (Exception e) {
                    }
                    mOccasionText.requestFocus();
                    mInputMethodManager.showSoftInput(mOccasionText, 0);
                    if (mDataEntryActivity == null) {
                        mDataEntryActivity = (DataEntryActivity) getActivity();
                    }
                    mDataEntryActivity.mVueDataentryKeyboardLayout
                            .setVisibility(View.VISIBLE);
                    mDataEntryActivity.mVueDataentryKeyboardDone
                            .setVisibility(View.VISIBLE);
                    mDataEntryActivity.mVueDataentryKeyboardCancel
                            .setVisibility(View.VISIBLE);
                    mDataEntryActivity.mVueDataentryPostLayout
                            .setVisibility(View.GONE);
                    mDataEntryActivity.hideDefaultActionbar();
                }
            }
        });
        mFindatLayout.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                if (mCategoryListviewLayout.getVisibility() == View.GONE
                        && mLookingForPopup.getVisibility() == View.GONE
                        && mOccasionPopup.getVisibility() == View.GONE) {
                    mSaySomethingAboutAisle.setVisibility(View.GONE);
                    mSaysomethingClose.setVisibility(View.GONE);
                    mFindatClose.setVisibility(View.VISIBLE);
                    mFindAtPopUp.setVisibility(View.VISIBLE);
                    mLookingForPopup.setVisibility(View.GONE);
                    mLookingForListviewLayout.setVisibility(View.GONE);
                    mOccasionPopup.setVisibility(View.GONE);
                    mOccasionListviewLayout.setVisibility(View.GONE);
                    mCategoryListviewLayout.setVisibility(View.GONE);
                    mSelectCategoryLayout.setVisibility(View.GONE);
                    mInputMethodManager.hideSoftInputFromWindow(
                            mOccasionText.getWindowToken(), 0);
                    mInputMethodManager.hideSoftInputFromWindow(
                            mLookingForText.getWindowToken(), 0);
                    mInputMethodManager.hideSoftInputFromWindow(
                            mSaySomethingAboutAisle.getWindowToken(), 0);
                    mFindAtText.requestFocus();
                    mFindAtText.setSelection(mFindAtText.getText().toString()
                            .length());
                    mInputMethodManager.showSoftInput(mFindAtText, 0);
                    if (mDataEntryActivity == null) {
                        mDataEntryActivity = (DataEntryActivity) getActivity();
                    }
                    mDataEntryActivity.mVueDataentryKeyboardLayout
                            .setVisibility(View.VISIBLE);
                    mDataEntryActivity.mVueDataentryKeyboardDone
                            .setVisibility(View.VISIBLE);
                    mDataEntryActivity.mVueDataentryKeyboardCancel
                            .setVisibility(View.VISIBLE);
                    mDataEntryActivity.mVueDataentryPostLayout
                            .setVisibility(View.GONE);
                    mDataEntryActivity.hideDefaultActionbar();
                }
            }
        });
        addImageToViewPager(true);
        return mDataEntryFragmentView;
    }
    
    public void hideAllEditableTextboxes() {
        mSaySomethingAboutAisle.setCursorVisible(false);
        // hiding keyboard
        mInputMethodManager.hideSoftInputFromWindow(
                mSaySomethingAboutAisle.getWindowToken(), 0);
        mInputMethodManager.hideSoftInputFromWindow(
                mOccasionText.getWindowToken(), 0);
        mInputMethodManager.hideSoftInputFromWindow(
                mLookingForText.getWindowToken(), 0);
        mInputMethodManager.hideSoftInputFromWindow(
                mFindAtText.getWindowToken(), 0);
        if (mDataEntryActivity == null) {
            mDataEntryActivity = (DataEntryActivity) getActivity();
        }
        if (mLookingForPopup.getVisibility() == View.VISIBLE) {
            if (mLookingForText.getText().toString().trim().length() > 0) {
                mLookingFor = mLookingForText.getText().toString();
                mLookingForPopup.setVisibility(View.GONE);
                mLookingForListviewLayout.setVisibility(View.GONE);
                mInputMethodManager.hideSoftInputFromWindow(
                        mLookingForText.getWindowToken(), 0);
                mMainHeadingRow.setVisibility(View.VISIBLE);
                if (mOccasion != null) {
                    mLookingForOccasionTextview.setText(mLookingFor + " for "
                            + mOccasion);
                } else {
                    mLookingForOccasionTextview.setText("Looking for "
                            + mLookingFor);
                }
                mDataEntryActivity.mVueDataentryKeyboardLayout
                        .setVisibility(View.GONE);
                mDataEntryActivity.mVueDataentryKeyboardDone
                        .setVisibility(View.GONE);
                mDataEntryActivity.mVueDataentryKeyboardCancel
                        .setVisibility(View.GONE);
                mDataEntryActivity.showDefaultActionbar();
                categoryIconClickFunctionality();
            } else {
                mLookingForText.requestFocus();
                mLookingForText.setSelection(mLookingForText.getText()
                        .toString().length());
                mInputMethodManager.showSoftInput(mLookingForText, 0);
                if (mDataEntryActivity == null) {
                    mDataEntryActivity = (DataEntryActivity) getActivity();
                }
                mDataEntryActivity.mVueDataentryKeyboardLayout
                        .setVisibility(View.VISIBLE);
                mDataEntryActivity.mVueDataentryKeyboardDone
                        .setVisibility(View.VISIBLE);
                mDataEntryActivity.mVueDataentryKeyboardCancel
                        .setVisibility(View.GONE);
                mDataEntryActivity.hideDefaultActionbar();
                Toast.makeText(getActivity(), "LookingFor is mandatory.",
                        Toast.LENGTH_LONG).show();
            }
        } else if (mOccasionPopup.getVisibility() == View.VISIBLE) {
            mInputMethodManager.hideSoftInputFromWindow(
                    mOccasionText.getWindowToken(), 0);
            if (mDataEntryActivity == null) {
                mDataEntryActivity = (DataEntryActivity) getActivity();
            }
            mDataEntryActivity.mVueDataentryKeyboardLayout
                    .setVisibility(View.GONE);
            mDataEntryActivity.mVueDataentryKeyboardDone
                    .setVisibility(View.GONE);
            mDataEntryActivity.mVueDataentryKeyboardCancel
                    .setVisibility(View.GONE);
            mDataEntryActivity.showDefaultActionbar();
            if (mOccasionText.getText().toString().trim().length() > 0) {
                mOccasion = mOccasionText.getText().toString();
                mLookingForOccasionTextview.setText(mLookingFor + " for "
                        + mOccasion);
            } else {
                mOccasion = null;
                mLookingForOccasionTextview.setText("Looking for "
                        + mLookingFor);
            }
            mOccasionPopup.setVisibility(View.GONE);
            mOccasionListviewLayout.setVisibility(View.GONE);
            mSaySomethingAboutAisle.setVisibility(View.VISIBLE);
            mSaysomethingClose.setVisibility(View.GONE);
            mDataEntryActivity.mVueDataentryKeyboardLayout
                    .setVisibility(View.GONE);
            mDataEntryActivity.mVueDataentryKeyboardDone
                    .setVisibility(View.GONE);
            mDataEntryActivity.mVueDataentryKeyboardCancel
                    .setVisibility(View.GONE);
            mDataEntryActivity.mVueDataentryPostLayout
                    .setVisibility(View.VISIBLE);
            mDataEntryActivity.hideDefaultActionbar();
        } else if (mCategoryListviewLayout.getVisibility() == View.VISIBLE) {
            Toast.makeText(getActivity(), "Category is mandatory.",
                    Toast.LENGTH_LONG).show();
        } else if (mFindAtPopUp.getVisibility() == View.VISIBLE) {
            mSaySomethingAboutAisle.setVisibility(View.VISIBLE);
            mSaysomethingClose.setVisibility(View.GONE);
            mInputMethodManager.hideSoftInputFromWindow(
                    mOccasionText.getWindowToken(), 0);
            mInputMethodManager.hideSoftInputFromWindow(
                    mLookingForText.getWindowToken(), 0);
            mInputMethodManager.hideSoftInputFromWindow(
                    mFindAtText.getWindowToken(), 0);
            mLookingForPopup.setVisibility(View.GONE);
            mLookingForListviewLayout.setVisibility(View.GONE);
            mOccasionPopup.setVisibility(View.GONE);
            mOccasionListviewLayout.setVisibility(View.GONE);
            mFindatClose.setVisibility(View.GONE);
            mFindAtPopUp.setVisibility(View.GONE);
            mCategoryListviewLayout.setVisibility(View.GONE);
            mSelectCategoryLayout.setVisibility(View.GONE);
            mDataEntryActivity.mVueDataentryKeyboardLayout
                    .setVisibility(View.GONE);
            mDataEntryActivity.mVueDataentryKeyboardDone
                    .setVisibility(View.GONE);
            mDataEntryActivity.mVueDataentryKeyboardCancel
                    .setVisibility(View.GONE);
            mDataEntryActivity.mVueDataentryPostLayout
                    .setVisibility(View.VISIBLE);
            mDataEntryActivity.hideDefaultActionbar();
        } else if (mSaySomethingAboutAisle.getVisibility() == View.VISIBLE) {
            mSaysomethingClose.setVisibility(View.GONE);
            mDataEntryActivity.mVueDataentryKeyboardLayout
                    .setVisibility(View.GONE);
            mDataEntryActivity.mVueDataentryKeyboardDone
                    .setVisibility(View.GONE);
            mDataEntryActivity.mVueDataentryKeyboardCancel
                    .setVisibility(View.GONE);
            mDataEntryActivity.mVueDataentryPostLayout
                    .setVisibility(View.VISIBLE);
            mDataEntryActivity.hideDefaultActionbar();
        }
    }
    
    public void lookingForInterceptListnerFunctionality() {
        if (mLookingFor != null && mLookingFor.trim().length() > 0) {
            mLookingForPopup.setVisibility(View.GONE);
            mLookingForListviewLayout.setVisibility(View.GONE);
            mInputMethodManager.hideSoftInputFromWindow(
                    mSaySomethingAboutAisle.getWindowToken(), 0);
            mInputMethodManager.hideSoftInputFromWindow(
                    mOccasionText.getWindowToken(), 0);
            mInputMethodManager.hideSoftInputFromWindow(
                    mLookingForText.getWindowToken(), 0);
            mDataEntryActivity.mVueDataentryKeyboardLayout
                    .setVisibility(View.GONE);
            mDataEntryActivity.mVueDataentryKeyboardDone
                    .setVisibility(View.GONE);
            mDataEntryActivity.mVueDataentryKeyboardCancel
                    .setVisibility(View.GONE);
            mDataEntryActivity.showDefaultActionbar();
            categoryIconClickFunctionality();
        } else {
            Toast.makeText(getActivity(), "Lookingfor is mandatory.",
                    Toast.LENGTH_LONG).show();
            lookingForTextClickFunctionality();
        }
    }
    
    public void occasionInterceptListnerFunctionality() {
        mLookingForPopup.setVisibility(View.GONE);
        mLookingForListviewLayout.setVisibility(View.GONE);
        mOccasionPopup.setVisibility(View.GONE);
        mOccasionListviewLayout.setVisibility(View.GONE);
        mInputMethodManager.hideSoftInputFromWindow(
                mSaySomethingAboutAisle.getWindowToken(), 0);
        mInputMethodManager.hideSoftInputFromWindow(
                mOccasionText.getWindowToken(), 0);
        mInputMethodManager.hideSoftInputFromWindow(
                mLookingForText.getWindowToken(), 0);
        if (mDataEntryActivity == null) {
            mDataEntryActivity = (DataEntryActivity) getActivity();
        }
        mDataEntryActivity.mVueDataentryKeyboardLayout.setVisibility(View.GONE);
        mDataEntryActivity.mVueDataentryKeyboardDone.setVisibility(View.GONE);
        mDataEntryActivity.mVueDataentryKeyboardCancel.setVisibility(View.GONE);
        mDataEntryActivity.showDefaultActionbar();
        mSaySomethingAboutAisle.setVisibility(View.VISIBLE);
        mSaysomethingClose.setVisibility(View.GONE);
        mDataEntryActivity.mVueDataentryKeyboardLayout.setVisibility(View.GONE);
        mDataEntryActivity.mVueDataentryKeyboardDone.setVisibility(View.GONE);
        mDataEntryActivity.mVueDataentryKeyboardCancel.setVisibility(View.GONE);
        mDataEntryActivity.mVueDataentryPostLayout.setVisibility(View.VISIBLE);
        mDataEntryActivity.hideDefaultActionbar();
    }
    
    public void findAtInterceptListnerFunctionality() {
        mFindatClose.setVisibility(View.GONE);
        mFindAtPopUp.setVisibility(View.GONE);
        mInputMethodManager.hideSoftInputFromWindow(
                mSaySomethingAboutAisle.getWindowToken(), 0);
        mInputMethodManager.hideSoftInputFromWindow(
                mOccasionText.getWindowToken(), 0);
        mInputMethodManager.hideSoftInputFromWindow(
                mLookingForText.getWindowToken(), 0);
        mInputMethodManager.hideSoftInputFromWindow(
                mFindAtText.getWindowToken(), 0);
        mSaySomethingAboutAisle.setVisibility(View.VISIBLE);
        mSaysomethingClose.setVisibility(View.GONE);
        mInputMethodManager.hideSoftInputFromWindow(
                mOccasionText.getWindowToken(), 0);
        mInputMethodManager.hideSoftInputFromWindow(
                mLookingForText.getWindowToken(), 0);
        mInputMethodManager.hideSoftInputFromWindow(
                mFindAtText.getWindowToken(), 0);
        mLookingForPopup.setVisibility(View.GONE);
        mLookingForListviewLayout.setVisibility(View.GONE);
        mOccasionPopup.setVisibility(View.GONE);
        mOccasionListviewLayout.setVisibility(View.GONE);
        mFindatClose.setVisibility(View.GONE);
        mFindAtPopUp.setVisibility(View.GONE);
        mCategoryListviewLayout.setVisibility(View.GONE);
        mSelectCategoryLayout.setVisibility(View.GONE);
        mDataEntryActivity.mVueDataentryKeyboardLayout.setVisibility(View.GONE);
        mDataEntryActivity.mVueDataentryKeyboardDone.setVisibility(View.GONE);
        mDataEntryActivity.mVueDataentryKeyboardCancel.setVisibility(View.GONE);
        mDataEntryActivity.mVueDataentryPostLayout.setVisibility(View.VISIBLE);
        mDataEntryActivity.hideDefaultActionbar();
    }
    
    public void saySomethingABoutAisleInterceptListnerFunctionality() {
        mSaysomethingClose.setVisibility(View.GONE);
        mSaySomethingAboutAisle.setCursorVisible(false);
        mInputMethodManager.hideSoftInputFromWindow(
                mSaySomethingAboutAisle.getWindowToken(), 0);
        if (mDataEntryActivity == null) {
            mDataEntryActivity = (DataEntryActivity) getActivity();
        }
        mDataEntryActivity.mVueDataentryKeyboardLayout.setVisibility(View.GONE);
        mDataEntryActivity.mVueDataentryKeyboardDone.setVisibility(View.GONE);
        mDataEntryActivity.mVueDataentryKeyboardCancel.setVisibility(View.GONE);
        mDataEntryActivity.mVueDataentryPostLayout.setVisibility(View.VISIBLE);
        mDataEntryActivity.hideDefaultActionbar();
    }
    
    public void createAisleClickFunctionality() {
        hideAllEditableTextboxes();
        if (mLookingFor != null
                && (mCategoryheading.getText().toString().trim().length() > 0)) {
            if (mFromDetailsScreenFlag) {
                addImageToAisle();
            } else {
                addAisle();
            }
        } else {
            if (mCategoryheading.getText().toString().trim().length() == 0) {
                categoryIconClickFunctionality();
                showAlertForMandatoryFields("choose a category");
            } else {
                showAlertForMandatoryFields(getResources().getString(
                        R.string.dataentry_mandtory_field_mesg));
            }
        }
    }
    
    public void addImageToAisleButtonClickFunctionality(
            boolean topAddImageToAisleFlag) {
        if (topAddImageToAisleFlag) {
            Utils.putDataentryTopAddImageAisleFlag(getActivity(), true);
            Utils.putDataentryTopAddImageAisleLookingFor(getActivity(),
                    mLookingFor);
            Utils.putDataentryTopAddImageAisleOccasion(getActivity(), mOccasion);
            Utils.putDataentryTopAddImageAisleCategory(getActivity(),
                    mCategoryheading.getText().toString());
            Utils.putDataentryTopAddImageAisleDescription(getActivity(),
                    mSaySomethingAboutAisle.getText().toString());
            if (mFromDetailsScreenFlag) {
                Utils.putFromDetailsScreenToDataentryCreateAisleScreenPreferenceFlag(
                        getActivity(), true);
            } else {
                Utils.putFromDetailsScreenToDataentryCreateAisleScreenPreferenceFlag(
                        getActivity(), false);
            }
        } else {
            Utils.putDataentryAddImageAisleFlag(getActivity(), true);
            Utils.putFromDetailsScreenToDataentryCreateAisleScreenPreferenceFlag(
                    getActivity(), false);
        }
        Intent intent = new Intent(getActivity(),
                CreateAisleSelectionActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        Bundle b = new Bundle();
        b.putBoolean(VueConstants.FROMCREATEAILSESCREENFLAG, true);
        intent.putExtras(b);
        if (!CreateAisleSelectionActivity.isActivityShowing) {
            CreateAisleSelectionActivity.isActivityShowing = true;
            getActivity().startActivityForResult(intent,
                    VueConstants.CREATE_AILSE_ACTIVITY_RESULT);
        }
    }
    
    private void showAlertForMandatoryFields(String message) {
        final Dialog dialog = new Dialog(getActivity(),
                R.style.Theme_Dialog_Translucent);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.vue_popup);
        TextView noButton = (TextView) dialog.findViewById(R.id.nobutton);
        TextView okButton = (TextView) dialog.findViewById(R.id.okbutton);
        TextView messagetext = (TextView) dialog.findViewById(R.id.messagetext);
        messagetext.setText(message);
        okButton.setVisibility(View.GONE);
        noButton.setText(getResources().getString(R.string.ok));
        noButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
    
    public void lookingForTextClickFunctionality() {
        mLookingForPopup.setVisibility(View.VISIBLE);
        if (mLookingForAisleKeywordsList != null
                && mLookingForAisleKeywordsList.size() > 0) {
            mLookingForListviewLayout.setVisibility(View.VISIBLE);
            mLookingForListview.setAdapter(new LookingForAdapter(getActivity(),
                    mLookingForAisleKeywordsList));
        }
        mLookingForText.post(new Runnable() {
            public void run() {
                mLookingForText.setSelection(mLookingForText.getText()
                        .toString().length());
                mLookingForText.setFocusable(true);
                mLookingForText.requestFocus();
                mInputMethodManager.showSoftInput(mLookingForText, 0);
                if (mDataEntryActivity == null) {
                    mDataEntryActivity = (DataEntryActivity) getActivity();
                }
                mDataEntryActivity.mVueDataentryKeyboardLayout
                        .setVisibility(View.VISIBLE);
                mDataEntryActivity.mVueDataentryKeyboardDone
                        .setVisibility(View.VISIBLE);
                mDataEntryActivity.mVueDataentryKeyboardCancel
                        .setVisibility(View.GONE);
                mDataEntryActivity.hideDefaultActionbar();
            }
            
        });
        
    }
    
    private void categoryIconClickFunctionality() {
        mCategoryListview.setVisibility(View.VISIBLE);
        mCategoryListview.setAdapter(new CategoryAdapter(getActivity()));
        mCategoryListviewLayout.setVisibility(View.VISIBLE);
        mSelectCategoryLayout.setVisibility(View.VISIBLE);
    }
    
    // LookingFor....
    private class LookingForAdapter extends BaseAdapter {
        Activity context;
        ArrayList<String> lookingForKeywordsList = null;
        
        public LookingForAdapter(Activity context,
                ArrayList<String> lookingForKeywordsList) {
            super();
            this.context = context;
            this.lookingForKeywordsList = lookingForKeywordsList;
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
            try {
                if (lookingForKeywordsList.get(position).equals(
                        mLookingForText.getText().toString())) {
                    holder.dataentryitemname.setTextColor(getResources()
                            .getColor(R.color.black));
                    holder.dataentryitemname.setTypeface(null, Typeface.BOLD);
                } else {
                    holder.dataentryitemname.setTextColor(getResources()
                            .getColor(R.color.dataentrytextcolor));
                    holder.dataentryitemname.setTypeface(null, Typeface.NORMAL);
                }
                holder.dataentryitemname.setText(lookingForKeywordsList
                        .get(position));
                rowView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        mLookingFor = lookingForKeywordsList.get(position);
                        mMainHeadingRow.setVisibility(View.VISIBLE);
                        if (mOccasion != null) {
                            mLookingForOccasionTextview.setText(mLookingFor
                                    + " for " + mOccasion);
                        } else {
                            mLookingForOccasionTextview.setText("Looking for "
                                    + mLookingFor);
                        }
                        mLookingForText.setText(lookingForKeywordsList
                                .get(position));
                        mLookingForText.setSelection(mLookingForText.getText()
                                .toString().length());
                    }
                });
            } catch (NotFoundException e) {
                e.printStackTrace();
            }
            return rowView;
        }
        
        @Override
        public int getCount() {
            try {
                return lookingForKeywordsList.size();
            } catch (Exception e) {
                return 0;
            }
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
    
    // Occassion....
    private class OccassionAdapter extends BaseAdapter {
        Activity context;
        ArrayList<String> occassionKeywordsList = null;
        
        public OccassionAdapter(Activity context,
                ArrayList<String> occassionKeywordsList) {
            super();
            this.context = context;
            this.occassionKeywordsList = occassionKeywordsList;
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
            try {
                if (occassionKeywordsList.get(position).equals(
                        mOccasionText.getText().toString())) {
                    holder.dataentryitemname.setTextColor(getResources()
                            .getColor(R.color.black));
                    holder.dataentryitemname.setTypeface(null, Typeface.BOLD);
                } else {
                    holder.dataentryitemname.setTextColor(getResources()
                            .getColor(R.color.dataentrytextcolor));
                    holder.dataentryitemname.setTypeface(null, Typeface.NORMAL);
                }
                holder.dataentryitemname.setText(occassionKeywordsList
                        .get(position));
                rowView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        mOccasionText.setText(occassionKeywordsList
                                .get(position));
                        if (mOccasionText.getText().toString().trim().length() > 0) {
                            mOccasion = mOccasionText.getText().toString();
                            mLookingForOccasionTextview.setText(mLookingFor
                                    + " for " + mOccasion);
                        }
                        try {
                            mOccasionText.setSelection(mOccasionText.getText()
                                    .toString().length());
                        } catch (Exception e) {
                        }
                    }
                });
            } catch (NotFoundException e) {
                e.printStackTrace();
            }
            return rowView;
        }
        
        @Override
        public int getCount() {
            try {
                return occassionKeywordsList.size();
            } catch (Exception e) {
                return 0;
            }
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
    
    // Category....
    private class CategoryAdapter extends BaseAdapter {
        Activity context;
        
        public CategoryAdapter(Activity context) {
            super();
            this.context = context;
        }
        
        class ViewHolder {
            TextView dataentryitemname;
            ImageView categoryImage;
        }
        
        public View getView(final int position, View convertView,
                ViewGroup parent) {
            ViewHolder holder = null;
            View rowView = convertView;
            if (rowView == null) {
                LayoutInflater inflater = context.getLayoutInflater();
                rowView = inflater.inflate(R.layout.dataentry_category_row,
                        null, true);
                holder = new ViewHolder();
                holder.dataentryitemname = (TextView) rowView
                        .findViewById(R.id.dataentryitemname);
                holder.categoryImage = (ImageView) rowView
                        .findViewById(R.id.categoryimage);
                rowView.setTag(holder);
            } else {
                holder = (ViewHolder) rowView.getTag();
            }
            try {
                holder.categoryImage
                        .setImageResource(mCategoryitemsDrawablesArray[position]);
            } catch (Exception e) {
                holder.categoryImage.setImageResource(R.drawable.category);
            }
            if (mCategoryitemsArray[position].equals(mCategoryheading.getText()
                    .toString())) {
                holder.dataentryitemname.setTextColor(getResources().getColor(
                        R.color.black));
                holder.dataentryitemname.setTypeface(null, Typeface.BOLD);
            } else {
                holder.dataentryitemname.setTextColor(getResources().getColor(
                        R.color.dataentrytextcolor));
                holder.dataentryitemname.setTypeface(null, Typeface.NORMAL);
            }
            holder.dataentryitemname.setText(mCategoryitemsArray[position]);
            rowView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    mCategoryheading.setText(mCategoryitemsArray[position]);
                    mCategoryheadingLayout.setVisibility(View.VISIBLE);
                    mCategoryListview.setVisibility(View.GONE);
                    mCategoryListviewLayout.setVisibility(View.GONE);
                    mSelectCategoryLayout.setVisibility(View.GONE);
                    mSaySomethingAboutAisle.setVisibility(View.VISIBLE);
                    mSaysomethingClose.setVisibility(View.GONE);
                    if (mDataEntryActivity == null) {
                        mDataEntryActivity = (DataEntryActivity) getActivity();
                    }
                    mDataEntryActivity.mVueDataentryKeyboardLayout
                            .setVisibility(View.GONE);
                    mDataEntryActivity.mVueDataentryKeyboardDone
                            .setVisibility(View.GONE);
                    mDataEntryActivity.mVueDataentryKeyboardCancel
                            .setVisibility(View.GONE);
                    mDataEntryActivity.mVueDataentryPostLayout
                            .setVisibility(View.VISIBLE);
                    mDataEntryActivity.hideDefaultActionbar();
                }
            });
            return rowView;
        }
        
        @Override
        public int getCount() {
            return mCategoryitemsArray.length;
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
    
    public void setGalleryORCameraImage(String picturePath,
            boolean dontResizeImageFlag, Context context) {
        if (!Utils.getDataentryTopAddImageAisleFlag(context)
                && !mFromDetailsScreenFlag) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    lookingForTextClickFunctionality();
                }
            }, 200);
        } else {
            mOccasionLayout.setVisibility(View.GONE);
            mFindatLayout.setVisibility(View.GONE);
            mSaySomethingAboutAisle.setVisibility(View.GONE);
            mSaysomethingClose.setVisibility(View.GONE);
            if (mDataEntryActivity == null) {
                mDataEntryActivity = (DataEntryActivity) getActivity();
            }
            mDataEntryActivity.mVueDataentryKeyboardLayout
                    .setVisibility(View.GONE);
            mDataEntryActivity.mVueDataentryKeyboardDone
                    .setVisibility(View.GONE);
            mDataEntryActivity.mVueDataentryKeyboardCancel
                    .setVisibility(View.GONE);
            mDataEntryActivity.mVueDataentryPostLayout
                    .setVisibility(View.VISIBLE);
            mDataEntryActivity.hideDefaultActionbar();
        }
        try {
            mImagePath = picturePath;
            if (dontResizeImageFlag) {
                mResizedImagePath = mImagePath;
                if (mResizedImagePath != null) {
                    addImageToViewPager(false);
                } else {
                    Toast.makeText(getActivity(), "Image is not available.",
                            Toast.LENGTH_LONG).show();
                }
            } else {
                mImageResizeAsynTask = new ImageResizeAsynTask();
                mImageResizeAsynTask.execute(getActivity());
            }
            if (Utils.getDataentryAddImageAisleFlag(getActivity())) {
                if (mDataEntryActivity == null) {
                    mDataEntryActivity = (DataEntryActivity) getActivity();
                }
                getActivity().getActionBar().setTitle(
                        getResources().getString(
                                R.string.add_imae_to_aisle_screen_title));
                mMainHeadingRow.setVisibility(View.VISIBLE);
                mCategoryheadingLayout.setVisibility(View.VISIBLE);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
    
    private void addImageToAisle() {
        if (checkLimitForLoginDialog()) {
            if (mLoginWarningMessage == null) {
                mLoginWarningMessage = new LoginWarningMessage(getActivity());
            }
            mLoginWarningMessage.showLoginWarningMessageDialog(
                    "You need to Login with the app to add image to aisle.",
                    true, true, 0, null, null);
        } else {
            VueUser storedVueUser = null;
            try {
                storedVueUser = Utils.readUserObjectFromFile(getActivity(),
                        VueConstants.VUE_APP_USEROBJECT__FILENAME);
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            if (storedVueUser != null && storedVueUser.getId() != null) {
                if (VueConnectivityManager.isNetworkConnected(getActivity())) {
                    addImageToAisleToServer(storedVueUser, VueApplication
                            .getInstance().getClickedWindowID());
                } else {
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.no_network),
                            Toast.LENGTH_LONG).show();
                }
            } else {
                showLogInDialog(false);
            }
        }
    }
    
    // Creating New Aisle...
    private void addAisle() {
        if (checkLimitForLoginDialog()) {
            if (mLoginWarningMessage == null) {
                mLoginWarningMessage = new LoginWarningMessage(getActivity());
            }
            mLoginWarningMessage.showLoginWarningMessageDialog(
                    "You need to Login with the app to create aisle.", true,
                    true, 0, null, null);
        } else {
            SharedPreferences sharedPreferencesObj = getActivity()
                    .getSharedPreferences(VueConstants.SHAREDPREFERENCE_NAME, 0);
            int createdAisleCount = sharedPreferencesObj.getInt(
                    VueConstants.CREATED_AISLE_COUNT_IN_PREFERENCE, 0);
            boolean isUserLoggedInFlag = sharedPreferencesObj.getBoolean(
                    VueConstants.VUE_LOGIN, false);
            if (createdAisleCount == 4 && !isUserLoggedInFlag) {
                if (mLoginWarningMessage == null) {
                    mLoginWarningMessage = new LoginWarningMessage(
                            getActivity());
                }
                mLoginWarningMessage
                        .showLoginWarningMessageDialog(
                                "You have 1 aisle left to create aisle without logging in.",
                                false, true, 4, null, null);
            } else {
                SharedPreferences.Editor editor = sharedPreferencesObj.edit();
                editor.putInt(VueConstants.CREATED_AISLE_COUNT_IN_PREFERENCE,
                        createdAisleCount + 1);
                editor.commit();
                VueUser storedVueUser = null;
                try {
                    storedVueUser = Utils.readUserObjectFromFile(getActivity(),
                            VueConstants.VUE_APP_USEROBJECT__FILENAME);
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
                if (storedVueUser != null && storedVueUser.getId() != null) {
                    if (VueConnectivityManager
                            .isNetworkConnected(getActivity())) {
                        storeMetaAisleDataIntoLocalStorage();
                        addAisleToServer(storedVueUser);
                    } else {
                        Toast.makeText(getActivity(),
                                getResources().getString(R.string.no_network),
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    showLogInDialog(false);
                }
            }
        }
    }
    
    public void storeMetaAisleDataIntoLocalStorage() {
        saveAisleLookingForOccassionCategoryDataToDB();
    }
    
    private void addImageToViewPager(boolean dontAddFlag) {
        DataentryImage datentryImage = new DataentryImage(null, null,
                mResizedImagePath, mImagePath, mOtherSourceSelectedImageUrl,
                mFindAtText.getText().toString(),
                mOtherSourceImageOriginalWidth,
                mOtherSourceImageOriginalHeight,
                mOtherSourceSelectedImageStore, false);
        mAisleImagePathList = null;
        try {
            try {
                mAisleImagePathList = Utils.readAisleImagePathListFromFile(
                        getActivity(),
                        VueConstants.AISLE_IMAGE_PATH_LIST_FILE_NAME);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (mAisleImagePathList == null) {
                mAisleImagePathList = new ArrayList<DataentryImage>();
            }
            if (dontAddFlag) {
                mDataEntryAislesViewpager.setVisibility(View.VISIBLE);
                try {
                    mDataEntryAislesViewpager
                            .setAdapter(new DataEntryAilsePagerAdapter(
                                    getActivity(), mAisleImagePathList, true));
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            } else if (Utils.getTouchToChangeFlag(getActivity())) {
                int modifiedPosition = Utils
                        .getTouchToChangePosition(getActivity());
                Utils.putTouchToChnageImagePosition(getActivity(), -1);
                Utils.putTouchToChnageImageTempPosition(getActivity(), -1);
                Utils.putTouchToChnageImageFlag(getActivity(), false);
                if (modifiedPosition != -1) {
                    try {
                        mAisleImagePathList.get(modifiedPosition)
                                .setResizedImagePath(mResizedImagePath);
                        mAisleImagePathList.get(modifiedPosition)
                                .setOriginalImagePath(mImagePath);
                        mAisleImagePathList.get(modifiedPosition).setImageUrl(
                                mOtherSourceSelectedImageUrl);
                        mAisleImagePathList
                                .get(modifiedPosition)
                                .setDetailsUrl(mFindAtText.getText().toString());
                        mAisleImagePathList.get(modifiedPosition)
                                .setImageWidth(mOtherSourceImageOriginalWidth);
                        mAisleImagePathList
                                .get(modifiedPosition)
                                .setImageHeight(mOtherSourceImageOriginalHeight);
                        mAisleImagePathList.get(modifiedPosition)
                                .setImageStore(mOtherSourceSelectedImageStore);
                        mAisleImagePathList.get(modifiedPosition)
                                .setAddedToServerFlag(false);
                        Utils.writeAisleImagePathListToFile(getActivity(),
                                VueConstants.AISLE_IMAGE_PATH_LIST_FILE_NAME,
                                mAisleImagePathList);
                        mDataEntryAislesViewpager.setVisibility(View.VISIBLE);
                        try {
                            mDataEntryAislesViewpager
                                    .setAdapter(new DataEntryAilsePagerAdapter(
                                            getActivity(), mAisleImagePathList,
                                            true));
                            mDataEntryAislesViewpager
                                    .setCurrentItem(modifiedPosition);
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    } catch (Exception e) {
                    }
                }
            } else if (!dontAddFlag) {
                mAisleImagePathList.add(0, datentryImage);
                Utils.writeAisleImagePathListToFile(getActivity(),
                        VueConstants.AISLE_IMAGE_PATH_LIST_FILE_NAME,
                        mAisleImagePathList);
                mAisleImagePathList = Utils.readAisleImagePathListFromFile(
                        getActivity(),
                        VueConstants.AISLE_IMAGE_PATH_LIST_FILE_NAME);
                mDataEntryAislesViewpager.setVisibility(View.VISIBLE);
                try {
                    mDataEntryAislesViewpager
                            .setAdapter(new DataEntryAilsePagerAdapter(
                                    getActivity(), mAisleImagePathList, true));
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void saveAisleLookingForOccassionCategoryDataToDB() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                
                AisleData lookingForAisleDataObj = mDbManager
                        .getAisleMetaDataForKeyword(mLookingFor.trim(),
                                VueConstants.LOOKING_FOR_TABLE);
                if (lookingForAisleDataObj != null) {
                    lookingForAisleDataObj.count += 1;
                    lookingForAisleDataObj.isNew = false;
                } else {
                    lookingForAisleDataObj = new AisleData();
                    lookingForAisleDataObj.keyword = mLookingFor.trim();
                    lookingForAisleDataObj.count = 1;
                    lookingForAisleDataObj.isNew = true;
                }
                String currentTime = System.currentTimeMillis() + "";
                lookingForAisleDataObj.time = currentTime;
                mDbManager.addAisleMetaDataToDB(VueConstants.LOOKING_FOR_TABLE,
                        lookingForAisleDataObj);
                if (mOccasion != null) {
                    AisleData occassionAisleDataObj = mDbManager
                            .getAisleMetaDataForKeyword(mOccasion.trim(),
                                    VueConstants.OCCASION_TABLE);
                    if (occassionAisleDataObj != null) {
                        occassionAisleDataObj.count += 1;
                        occassionAisleDataObj.isNew = false;
                    } else {
                        occassionAisleDataObj = new AisleData();
                        occassionAisleDataObj.keyword = mOccasion.trim();
                        occassionAisleDataObj.count = 1;
                        occassionAisleDataObj.isNew = true;
                    }
                    occassionAisleDataObj.time = currentTime;
                    mDbManager.addAisleMetaDataToDB(
                            VueConstants.OCCASION_TABLE, occassionAisleDataObj);
                }
                AisleData categoryAisleDataObj = mDbManager
                        .getAisleMetaDataForKeyword(mCategoryheading.getText()
                                .toString().trim(), VueConstants.CATEGORY_TABLE);
                if (categoryAisleDataObj != null) {
                    categoryAisleDataObj.count += 1;
                    categoryAisleDataObj.isNew = false;
                } else {
                    categoryAisleDataObj = new AisleData();
                    categoryAisleDataObj.keyword = mCategoryheading.getText()
                            .toString().trim();
                    categoryAisleDataObj.count = 1;
                    categoryAisleDataObj.isNew = true;
                }
                categoryAisleDataObj.time = currentTime;
                mDbManager.addAisleMetaDataToDB(VueConstants.CATEGORY_TABLE,
                        categoryAisleDataObj);
            }
        }).start();
    }
    
    // update ailse and send to server.
    public void upDateAisleToServer(VueUser vueUser, String aisleId,
            String categoery, String lookingfor, String occasion,
            String description) {
        if ((mOtherSourceSelectedImageUrl != null && mOtherSourceSelectedImageUrl
                .trim().length() > 0) || mImagePath != null) {
            final Aisle aisle = new Aisle();
            if (aisleId != null) {
                aisle.setId(Long.parseLong(aisleId));
            }
            aisle.setCategory(categoery);
            aisle.setLookingFor(lookingfor);
            aisle.setName("Super Aisle"); // TODO By Krishna
            aisle.setOccassion(occasion);
            aisle.setOwnerUserId(Long.valueOf(vueUser.getId()));
            if (description.length() > 0) {
                aisle.setDescription(description);
            } else {
                aisle.setDescription("");
            }
            FlurryAgent.logEvent("Update_Aisle");
            VueTrendingAislesDataModel
                    .getInstance(VueApplication.getInstance())
                    .getNetworkHandler().requestUpdateAisle(aisle);
        } else {
            Toast.makeText(
                    getActivity(),
                    getResources()
                            .getString(
                                    R.string.dataentry_mandtory_field_add_aisleimage_mesg),
                    Toast.LENGTH_LONG).show();
        }
    }
    
    // Create ailse and send to server.
    public void addAisleToServer(VueUser vueUser) {
        if (mAisleImagePathList != null && mAisleImagePathList.size() > 0) {
            String categoery = mCategoryheading.getText().toString().trim();
            String lookingFor = mLookingFor.trim();
            String occassion = mOccasion;
            String description = mSaySomethingAboutAisle.getText().toString()
                    .trim();
            final Aisle aisle = new Aisle();
            aisle.setCategory(categoery);
            aisle.setLookingFor(lookingFor);
            aisle.setName("Super Aisle"); // TODO By Krishna
            aisle.setOccassion(occassion);
            aisle.setOwnerUserId(Long.valueOf(vueUser.getId()));
            if (description.length() > 0) {
                aisle.setDescription(description);
            } else {
                aisle.setDescription("");
            }
            final ArrayList<VueImage> vueImageList = new ArrayList<VueImage>();
            final ArrayList<String> originalImagePathList = new ArrayList<String>();
            String offlineAisleId = String.valueOf(System.currentTimeMillis());
            final ArrayList<String> offlineImageIdList = new ArrayList<String>();
            for (int i = 0; i < mAisleImagePathList.size(); i++) {
                VueImage image = new VueImage();
                image.setDetailsUrl(mAisleImagePathList.get(i).getDetailsUrl());
                image.setHeight(mAisleImagePathList.get(i).getImageHeight());
                image.setWidth(mAisleImagePathList.get(i).getImageWidth());
                image.setStore(mAisleImagePathList.get(i).getImageStore());
                image.setImageUrl(mAisleImagePathList.get(i).getImageUrl());
                image.setTitle("Android Test"); // TODO By Krishna
                image.setOwnerUserId(Long.valueOf(vueUser.getId()));
                vueImageList.add(image);
                originalImagePathList.add(mAisleImagePathList.get(i)
                        .getOriginalImagePath());
                String offlineImageId = String.valueOf(System
                        .currentTimeMillis() + i);
                offlineImageIdList.add(offlineImageId);
                mAisleImagePathList.get(i).setImageId(offlineImageId);
                mAisleImagePathList.get(i).setAisleId(offlineAisleId);
                mAisleImagePathList.get(i).setAddedToServerFlag(true);
            }
            try {
                Utils.writeAisleImagePathListToFile(getActivity(),
                        VueConstants.AISLE_IMAGE_PATH_LIST_FILE_NAME,
                        mAisleImagePathList);
            } catch (Exception e) {
            }
            FlurryAgent.logEvent("New_Aisle_Creation");
            // Camera or Gallery...
            if (vueImageList.get(0).getImageUrl() == null) {
                VueTrendingAislesDataModel
                        .getInstance(VueApplication.getInstance())
                        .getNetworkHandler()
                        .requestForUploadImage(
                                new File(originalImagePathList.get(0)),
                                new ImageUploadCallback() {
                                    @Override
                                    public void onImageUploaded(String imageUrl) {
                                        if (imageUrl != null) {
                                            vueImageList.get(0).setImageUrl(
                                                    imageUrl);
                                            aisle.setAisleImage(vueImageList
                                                    .get(0));
                                            VueTrendingAislesDataModel
                                                    .getInstance(
                                                            VueApplication
                                                                    .getInstance())
                                                    .getNetworkHandler()
                                                    .requestCreateAisle(
                                                            aisle,
                                                            new AisleManager.AisleUpdateCallback() {
                                                                @Override
                                                                public void onAisleUpdated(
                                                                        String aisleId,
                                                                        String imageId) {
                                                                    if (mAisleImagePathList != null
                                                                            && mAisleImagePathList
                                                                                    .size() > 0) {
                                                                        for (int i = 0; i < mAisleImagePathList
                                                                                .size(); i++) {
                                                                            mAisleImagePathList
                                                                                    .get(i)
                                                                                    .setAisleId(
                                                                                            aisleId);
                                                                            if (mAisleImagePathList
                                                                                    .get(i)
                                                                                    .getImageId()
                                                                                    .equals(offlineImageIdList
                                                                                            .get(0))) {
                                                                                mAisleImagePathList
                                                                                        .get(i)
                                                                                        .setImageId(
                                                                                                imageId);
                                                                            }
                                                                        }
                                                                        try {
                                                                            Utils.writeAisleImagePathListToFile(
                                                                                    getActivity(),
                                                                                    VueConstants.AISLE_IMAGE_PATH_LIST_FILE_NAME,
                                                                                    mAisleImagePathList);
                                                                        } catch (Exception e) {
                                                                        }
                                                                    }
                                                                    for (int j = 1; j < vueImageList
                                                                            .size(); j++) {
                                                                        vueImageList
                                                                                .get(j)
                                                                                .setOwnerAisleId(
                                                                                        Long.valueOf(aisleId));
                                                                        addSingleImageToServer(
                                                                                vueImageList
                                                                                        .get(j),
                                                                                originalImagePathList
                                                                                        .get(j),
                                                                                false,
                                                                                offlineImageIdList
                                                                                        .get(j));
                                                                    }
                                                                }
                                                            });
                                        }
                                    }
                                });
            } else {
                aisle.setAisleImage(vueImageList.get(0));
                VueTrendingAislesDataModel
                        .getInstance(VueApplication.getInstance())
                        .getNetworkHandler()
                        .requestCreateAisle(aisle,
                                new AisleManager.AisleUpdateCallback() {
                                    @Override
                                    public void onAisleUpdated(String aisleId,
                                            String imageId) {
                                        if (mAisleImagePathList != null
                                                && mAisleImagePathList.size() > 0) {
                                            for (int i = 0; i < mAisleImagePathList
                                                    .size(); i++) {
                                                mAisleImagePathList.get(i)
                                                        .setAisleId(aisleId);
                                                if (mAisleImagePathList
                                                        .get(i)
                                                        .getImageId()
                                                        .equals(offlineImageIdList
                                                                .get(0))) {
                                                    mAisleImagePathList
                                                            .get(i)
                                                            .setImageId(imageId);
                                                }
                                            }
                                            try {
                                                Utils.writeAisleImagePathListToFile(
                                                        getActivity(),
                                                        VueConstants.AISLE_IMAGE_PATH_LIST_FILE_NAME,
                                                        mAisleImagePathList);
                                            } catch (Exception e) {
                                            }
                                        }
                                        for (int j = 1; j < vueImageList.size(); j++) {
                                            vueImageList
                                                    .get(j)
                                                    .setOwnerAisleId(
                                                            Long.valueOf(aisleId));
                                            addSingleImageToServer(vueImageList
                                                    .get(j),
                                                    originalImagePathList
                                                            .get(j), false,
                                                    offlineImageIdList.get(j));
                                        }
                                    }
                                });
            }
            if (mDataEntryActivity == null) {
                mDataEntryActivity = (DataEntryActivity) getActivity();
            }
            mDataEntryActivity.shareViaVueClicked();
        } else {
            Toast.makeText(
                    getActivity(),
                    getResources()
                            .getString(
                                    R.string.dataentry_mandtory_field_add_aisleimage_mesg),
                    Toast.LENGTH_LONG).show();
        }
    }
    
    private void addSingleImageToServer(final VueImage vueImage,
            final String originalImagePath, final boolean fromDetailsScreen,
            final String imageId) { // Camera or Gallery...
        if (vueImage.getImageUrl() == null) {
            VueTrendingAislesDataModel
                    .getInstance(VueApplication.getInstance())
                    .getNetworkHandler()
                    .requestForUploadImage(new File(originalImagePath),
                            new ImageUploadCallback() {
                                
                                @Override
                                public void onImageUploaded(String imageUrl) {
                                    if (imageUrl != null) {
                                        vueImage.setImageUrl(imageUrl);
                                        String offlimeImageId = null;
                                        if (fromDetailsScreen) {
                                            offlimeImageId = imageId;
                                        }
                                        VueTrendingAislesDataModel
                                                .getInstance(
                                                        VueApplication
                                                                .getInstance())
                                                .getNetworkHandler()
                                                .requestForAddImage(
                                                        fromDetailsScreen,
                                                        offlimeImageId,
                                                        vueImage,
                                                        new ImageAddedCallback() {
                                                            
                                                            @Override
                                                            public void onImageAdded(
                                                                    String imageid) {
                                                                if (mAisleImagePathList != null
                                                                        && mAisleImagePathList
                                                                                .size() > 0) {
                                                                    for (int i = 0; i < mAisleImagePathList
                                                                            .size(); i++) {
                                                                        if (mAisleImagePathList
                                                                                .get(i)
                                                                                .getImageId()
                                                                                .equals(imageId)) {
                                                                            mAisleImagePathList
                                                                                    .get(i)
                                                                                    .setImageId(
                                                                                            imageid);
                                                                            try {
                                                                                Utils.writeAisleImagePathListToFile(
                                                                                        getActivity(),
                                                                                        VueConstants.AISLE_IMAGE_PATH_LIST_FILE_NAME,
                                                                                        mAisleImagePathList);
                                                                            } catch (Exception e) {
                                                                            }
                                                                            break;
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        });
                                    }
                                }
                            });
        } else {
            String offlimeImageId = null;
            if (fromDetailsScreen) {
                offlimeImageId = imageId;
            }
            VueTrendingAislesDataModel
                    .getInstance(VueApplication.getInstance())
                    .getNetworkHandler()
                    .requestForAddImage(fromDetailsScreen, offlimeImageId,
                            vueImage, new ImageAddedCallback() {
                                
                                @Override
                                public void onImageAdded(String imageid) {
                                    if (mAisleImagePathList != null
                                            && mAisleImagePathList.size() > 0) {
                                        for (int i = 0; i < mAisleImagePathList
                                                .size(); i++) {
                                            if (mAisleImagePathList.get(i)
                                                    .getImageId()
                                                    .equals(imageId)) {
                                                mAisleImagePathList.get(i)
                                                        .setImageId(imageid);
                                                try {
                                                    Utils.writeAisleImagePathListToFile(
                                                            getActivity(),
                                                            VueConstants.AISLE_IMAGE_PATH_LIST_FILE_NAME,
                                                            mAisleImagePathList);
                                                } catch (Exception e) {
                                                }
                                                break;
                                            }
                                        }
                                    }
                                }
                            });
        }
    }
    
    public void addImageToAisleToServer(VueUser storedVueUser,
            String ownerAisleId) {
        if (mAisleImagePathList != null && mAisleImagePathList.size() > 0) {
            final ArrayList<VueImage> vueImageList = new ArrayList<VueImage>();
            ArrayList<String> detailsUrl = new ArrayList<String>();
            ArrayList<String> imageUrl = new ArrayList<String>();
            ArrayList<Integer> imageWidth = new ArrayList<Integer>();
            ArrayList<Integer> imageHeight = new ArrayList<Integer>();
            ArrayList<String> imageStore = new ArrayList<String>();
            final ArrayList<String> originalImagePathList = new ArrayList<String>();
            final ArrayList<String> offlineImageIdList = new ArrayList<String>();
            for (int i = 0; i < mAisleImagePathList.size(); i++) {
                if (!(mAisleImagePathList.get(i).isAddedToServerFlag())) {
                    VueImage vueImage = new VueImage();
                    vueImage.setDetailsUrl(mAisleImagePathList.get(i)
                            .getDetailsUrl());
                    vueImage.setHeight(mAisleImagePathList.get(i)
                            .getImageHeight());
                    vueImage.setWidth(mAisleImagePathList.get(i)
                            .getImageWidth());
                    vueImage.setStore(mAisleImagePathList.get(i)
                            .getImageStore());
                    vueImage.setImageUrl(mAisleImagePathList.get(i)
                            .getImageUrl());
                    vueImage.setTitle("Android Test"); // TODO By Krishna
                    vueImage.setOwnerUserId(Long.valueOf(Long.valueOf(
                            storedVueUser.getId()).toString()));
                    vueImage.setOwnerAisleId(Long.valueOf(ownerAisleId));
                    vueImageList.add(vueImage);
                    String offlineImageId = String.valueOf(System
                            .currentTimeMillis() + i);
                    offlineImageIdList.add(offlineImageId);
                    originalImagePathList.add(mAisleImagePathList.get(i)
                            .getOriginalImagePath());
                    mAisleImagePathList.get(i).setImageId(offlineImageId);
                    mAisleImagePathList.get(i).setAisleId(ownerAisleId);
                    mAisleImagePathList.get(i).setAddedToServerFlag(true);
                    detailsUrl.add(mAisleImagePathList.get(i).getDetailsUrl());
                    imageUrl.add(mAisleImagePathList.get(i).getImageUrl());
                    imageWidth.add(mAisleImagePathList.get(i).getImageWidth());
                    imageHeight
                            .add(mAisleImagePathList.get(i).getImageHeight());
                    imageStore.add(mAisleImagePathList.get(i).getImageStore());
                }
            }
            if (vueImageList != null && vueImageList.size() > 0) {
                try {
                    Utils.writeAisleImagePathListToFile(getActivity(),
                            VueConstants.AISLE_IMAGE_PATH_LIST_FILE_NAME,
                            mAisleImagePathList);
                    mDataEntryAislesViewpager.setVisibility(View.VISIBLE);
                    try {
                        mDataEntryAislesViewpager
                                .setAdapter(new DataEntryAilsePagerAdapter(
                                        getActivity(), mAisleImagePathList,
                                        true));
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                }
                for (int j = 0; j < vueImageList.size(); j++) {
                    addImageToAisleWindow(VueApplication.getInstance()
                            .getClickedWindowID(),
                            originalImagePathList.get(j), imageUrl.get(j),
                            imageWidth.get(j), imageHeight.get(j),
                            detailsUrl.get(j), imageStore.get(j),
                            offlineImageIdList.get(j));
                }
                Intent intent = new Intent();
                Bundle b = new Bundle();
                b.putString(
                        VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_LOOKINGFOR,
                        mLookingFor);
                b.putString(
                        VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_OCCASION,
                        mOccasion);
                b.putString(
                        VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_CATEGORY,
                        mCategoryheading.getText().toString());
                b.putString(
                        VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_SAYSOMETHINGABOUTAISLE,
                        mSaySomethingAboutAisle.getText().toString());
                intent.putExtras(b);
                getActivity()
                        .setResult(
                                VueConstants.FROM_DETAILS_SCREEN_TO_DATAENTRY_SCREEN_ACTIVITY_RESULT,
                                intent);
                if (mDataEntryActivity == null) {
                    mDataEntryActivity = (DataEntryActivity) getActivity();
                }
                for (int j = 0; j < vueImageList.size(); j++) {
                    addSingleImageToServer(vueImageList.get(j),
                            originalImagePathList.get(j), true,
                            offlineImageIdList.get(j));
                }
                mDataEntryActivity.shareViaVueClicked();
            } else {
                Toast.makeText(
                        getActivity(),
                        getResources()
                                .getString(
                                        R.string.dataentry_mandtory_field_add_aisleimage_mesg),
                        Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(
                    getActivity(),
                    getResources()
                            .getString(
                                    R.string.dataentry_mandtory_field_add_aisleimage_mesg),
                    Toast.LENGTH_LONG).show();
        }
    }
    
    private class ImageResizeAsynTask extends
            AsyncTask<Activity, Activity, Activity> {
        
        @Override
        protected void onPostExecute(Activity result) {
            if (mResizedImagePath != null) {
                addImageToViewPager(false);
            } else {
                Toast.makeText(getActivity(), "Image is not available.",
                        Toast.LENGTH_LONG).show();
            }
            super.onPostExecute(result);
        }
        
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        
        @Override
        protected Activity doInBackground(Activity... params) {
            String[] returnArray = Utils.getResizedImage(new File(mImagePath),
                    mScreenHeight, mScreenWidth, params[0]);
            if (returnArray != null) {
                mResizedImagePath = returnArray[0];
                try {
                    mOtherSourceImageOriginalWidth = Integer
                            .parseInt(returnArray[1]);
                    mOtherSourceImageOriginalHeight = Integer
                            .parseInt(returnArray[2]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }
    
    private boolean checkLimitForLoginDialog() {
        SharedPreferences sharedPreferencesObj = getActivity()
                .getSharedPreferences(VueConstants.SHAREDPREFERENCE_NAME, 0);
        boolean isUserLoggedInFlag = sharedPreferencesObj.getBoolean(
                VueConstants.VUE_LOGIN, false);
        if (!isUserLoggedInFlag) {
            int createdAisleCount = sharedPreferencesObj.getInt(
                    VueConstants.CREATED_AISLE_COUNT_IN_PREFERENCE, 0);
            int commentsCount = sharedPreferencesObj.getInt(
                    VueConstants.COMMENTS_COUNT_IN_PREFERENCES, 0);
            if (createdAisleCount >= VueConstants.CREATE_AISLE_LIMIT_FOR_LOGIN
                    || commentsCount >= VueConstants.COMMENTS_LIMIT_FOR_LOGIN) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
    
    public void showOtherSourcesGridview(
            ArrayList<OtherSourceImageDetails> imagesList, String sourceUrl) {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
        if (imagesList != null && imagesList.size() > 0) {
            if (mOtherSourcesDialog == null) {
                mOtherSourcesDialog = new OtherSourcesDialog(getActivity());
            }
            mOtherSourcesDialog.showImageDailog(imagesList, false, sourceUrl);
        } else {
            Toast.makeText(getActivity(), "Sorry, there are no images.",
                    Toast.LENGTH_LONG).show();
        }
    }
    
    public void getImagesFromUrl(String sourceUrl) {
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog.show(getActivity(), "",
                    "Please wait...");
        }
        mOtherSourceSelectedImageStore = Utils.getStoreNameFromUrl(sourceUrl);
        GetOtherSourceImagesTask getImagesTask = new GetOtherSourceImagesTask(
                sourceUrl, getActivity(), false);
        getImagesTask.execute();
    }
    
    public void showLogInDialog(boolean hideCancelButton) {
        Intent i = new Intent(getActivity(), VueLoginActivity.class);
        Bundle b = new Bundle();
        b.putBoolean(VueConstants.CANCEL_BTN_DISABLE_FLAG, hideCancelButton);
        b.putString(VueConstants.FROM_INVITEFRIENDS, null);
        b.putBoolean(VueConstants.FBLOGIN_FROM_DETAILS_SHARE, false);
        b.putBoolean(VueConstants.FROM_BEZELMENU_LOGIN, false);
        i.putExtras(b);
        startActivity(i);
    }
    
    public class ShareViaVueListner implements ShareViaVueClickedListner {
        @Override
        public void onAisleShareToVue() {
            ((DataEntryActivity) getActivity()).shareViaVueClicked();
        }
    }
    
    public void showAlertMessageForBackendNotIntegrated(String message) {
        final Dialog dialog = new Dialog(getActivity(),
                R.style.Theme_Dialog_Translucent);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.vue_popup);
        TextView noButton = (TextView) dialog.findViewById(R.id.nobutton);
        TextView okButton = (TextView) dialog.findViewById(R.id.okbutton);
        TextView messagetext = (TextView) dialog.findViewById(R.id.messagetext);
        messagetext.setText(message);
        okButton.setText("OK");
        noButton.setVisibility(View.GONE);
        okButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
    
    void showDetailsScreenImagesInDataentryScreen() {
        ArrayList<AisleImageDetails> aisleImageDetailsList = null;
        // User Ailse from Details screen...
        if (mIsUserAisleFlag) {
            AisleWindowContent aisleWindowContent = VueTrendingAislesDataModel
                    .getInstance(getActivity()).getAisleAt(
                            VueApplication.getInstance().getClickedWindowID());
            if (aisleWindowContent != null) {
                aisleImageDetailsList = aisleWindowContent.getImageList();
            }
        } else {
            aisleImageDetailsList = VueTrendingAislesDataModel.getInstance(
                    getActivity()).getOwnerImages(
                    VueApplication.getInstance().getClickedWindowID(),
                    String.valueOf(VueApplication.getInstance().getmUserId()));
        }
        if (aisleImageDetailsList != null && aisleImageDetailsList.size() > 0) {
            mAisleImagePathList = new ArrayList<DataentryImage>();
            FileCache fileCache = new FileCache(getActivity());
            for (int i = 0; i < aisleImageDetailsList.size(); i++) {
                String originalImagePath = null;
                if (fileCache.getFile(aisleImageDetailsList.get(i).mImageUrl)
                        .exists()) {
                    originalImagePath = fileCache.getFile(
                            aisleImageDetailsList.get(i).mImageUrl).getPath();
                }
                String resizedImagePath = null;
                if (originalImagePath != null) {
                    resizedImagePath = fileCache.getVueAppResizedPictureFile(
                            String.valueOf(originalImagePath.hashCode()))
                            .getPath();
                } else {
                    resizedImagePath = fileCache
                            .getVueAppResizedPictureFile(
                                    String.valueOf(aisleImageDetailsList.get(i).mImageUrl
                                            .hashCode())).getPath();
                }
                DataentryImage aisleImage = new DataentryImage(
                        aisleImageDetailsList.get(i).mOwnerAisleId,
                        aisleImageDetailsList.get(i).mId, resizedImagePath,
                        originalImagePath,
                        aisleImageDetailsList.get(i).mImageUrl,
                        aisleImageDetailsList.get(i).mDetalsUrl,
                        aisleImageDetailsList.get(i).mAvailableWidth,
                        aisleImageDetailsList.get(i).mAvailableHeight,
                        aisleImageDetailsList.get(i).mStore, true);
                mAisleImagePathList.add(aisleImage);
            }
            try {
                Utils.writeAisleImagePathListToFile(getActivity(),
                        VueConstants.AISLE_IMAGE_PATH_LIST_FILE_NAME,
                        mAisleImagePathList);
            } catch (Exception e1) {
            }
            mDataEntryAislesViewpager.setVisibility(View.VISIBLE);
            try {
                mDataEntryAislesViewpager
                        .setAdapter(new DataEntryAilsePagerAdapter(
                                getActivity(), mAisleImagePathList, true));
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }
    
    public void deleteImage(ArrayList<Integer> deletedImagesPositions) {
        try {
            VueUser storedVueUser = null;
            try {
                storedVueUser = Utils.readUserObjectFromFile(getActivity(),
                        VueConstants.VUE_APP_USEROBJECT__FILENAME);
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            if (storedVueUser != null && storedVueUser.getId() != null) {
                if (VueConnectivityManager.isNetworkConnected(getActivity())) {
                    int imagesCountBeforeDeletions = mAisleImagePathList.size();
                    for (int position = 0; position < mAisleImagePathList
                            .size(); position++) {
                        if (deletedImagesPositions.contains(position)) {
                            Image image = new Image();
                            image.setDetailsUrl(mAisleImagePathList.get(
                                    position).getDetailsUrl());
                            image.setHeight(mAisleImagePathList.get(position)
                                    .getImageHeight());
                            image.setWidth(mAisleImagePathList.get(position)
                                    .getImageWidth());
                            image.setStore(mAisleImagePathList.get(position)
                                    .getImageStore());
                            image.setImageUrl(mAisleImagePathList.get(position)
                                    .getImageUrl());
                            image.setTitle("Android Test"); // TODO By Krishna
                            image.setOwnerUserId(Long.valueOf(Long.valueOf(
                                    storedVueUser.getId()).toString()));
                            String aisleId = mAisleImagePathList.get(position)
                                    .getAisleId();
                            if (aisleId == null || aisleId.equals("null")) {
                                aisleId = VueApplication.getInstance()
                                        .getClickedWindowID();
                            }
                            image.setOwnerAisleId(Long.valueOf(aisleId));
                            image.setId(Long.valueOf(mAisleImagePathList.get(
                                    position).getImageId()));
                            VueTrendingAislesDataModel
                                    .getInstance(VueApplication.getInstance())
                                    .getNetworkHandler()
                                    .requestForDeleteImage(image, aisleId);
                            imagesCountBeforeDeletions--;
                        }
                    }
                    if (imagesCountBeforeDeletions <= 0 && mIsUserAisleFlag) {
                        VueApplication.getInstance()
                                .setmFinishDetailsScreenFlag(true);
                    }
                    if (mDataEntryActivity == null) {
                        mDataEntryActivity = (DataEntryActivity) getActivity();
                    }
                    mDataEntryActivity.shareViaVueClicked();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void showAddMoreImagesDialog() {
        final Dialog dialog = new Dialog(getActivity(),
                R.style.Theme_Dialog_Translucent);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.vue_popup);
        final TextView noButton = (TextView) dialog.findViewById(R.id.nobutton);
        TextView yesButton = (TextView) dialog.findViewById(R.id.okbutton);
        TextView messagetext = (TextView) dialog.findViewById(R.id.messagetext);
        messagetext.setText("Do you want to Add more images?");
        yesButton.setText("Yes");
        noButton.setText("No");
        yesButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
                Utils.putTouchToChnageImagePosition(getActivity(), -1);
                Utils.putTouchToChnageImageTempPosition(getActivity(), -1);
                Utils.putTouchToChnageImageFlag(getActivity(), false);
                addImageToAisleButtonClickFunctionality(true);
            }
        });
        noButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
    
    private void addImageToAisleWindow(String aisleId, String imagePath,
            String imageUrl, int imageWidth, int imageHeight,
            String detailsUrl, String store, String imageId) {
        boolean isImageFromLocalSystem = false;
        if (imageUrl == null) {
            isImageFromLocalSystem = true;
        }
        FileCache fileCache = new FileCache(getActivity());
        File f = null;
        if (imageUrl != null) {
            f = fileCache.getFile(imageUrl);
        } else {
            imageUrl = imagePath;
            f = fileCache.getFile(imagePath);
        }
        File sourceFile = new File(imagePath);
        Bitmap bmp = BitmapLoaderUtils.getInstance().decodeFile(sourceFile,
                VueApplication.getInstance().mScreenHeight,
                VueApplication.getInstance().getVueDetailsCardWidth(),
                Utils.DETAILS_SCREEN);
        Utils.saveBitmap(bmp, f);
        addAisleToWindow(aisleId, imagePath, imageUrl, imageWidth, imageHeight,
                detailsUrl, store, imageId, isImageFromLocalSystem);
    }
    
    private void addAisleToWindow(String aisleId, String imgPath,
            String imageUrl, int imageWidth, int imageHeight,
            String detailsUrl, String store, String imageId,
            boolean isImageFromLocalSystem) {
        addAisleToContentWindow(aisleId, imgPath, imageUrl, imageWidth,
                imageHeight, "title", detailsUrl, store, imageId,
                isImageFromLocalSystem);
        
    }
    
    private void addAisleToContentWindow(String aisleId, String imagePath,
            String imageUrl, int imageWidth, int imageHeight, String title,
            String detailsUrl, String store, String imageId,
            boolean isImageFromLocalSystem) {
        Utils.sIsAisleChanged = true;
        Utils.mChangeAilseId = aisleId;
        
        AisleWindowContent aisleItem = VueTrendingAislesDataModel.getInstance(
                VueApplication.getInstance()).getAisleFromList(
                VueTrendingAislesDataModel.getInstance(
                        VueApplication.getInstance()).getAisleAt(aisleId));
        if (aisleItem != null) {
            
            AisleImageDetails imgDetails = new AisleImageDetails();
            imgDetails.mAvailableHeight = imageHeight;
            imgDetails.mAvailableWidth = imageWidth;
            if (imgDetails.mAvailableHeight < aisleItem
                    .getBestHeightForWindow()) {
                aisleItem.setBestHeightForWindow(imgDetails.mAvailableHeight);
                
            }
            imgDetails.mTitle = title;
            imgDetails.mImageUrl = imageUrl;
            imgDetails.mDetalsUrl = detailsUrl;
            imgDetails.mId = imageId; // offline imageid
            imgDetails.mIsFromLocalSystem = isImageFromLocalSystem;
            imgDetails.mStore = store;
            imgDetails.mTrendingImageHeight = imgDetails.mAvailableHeight;
            imgDetails.mTrendingImageWidth = imgDetails.mAvailableWidth;
            imgDetails.mOwnerAisleId = aisleItem.getAisleId();
            imgDetails.mOwnerUserId = Long.toString(VueApplication
                    .getInstance().getmUserId());
            aisleItem.getImageList().add(imgDetails);
            aisleItem.addAisleContent(aisleItem.getAisleContext(),
                    aisleItem.getImageList());
            int bestHeight = Utils.modifyHeightForDetailsView(aisleItem
                    .getImageList());
            aisleItem.setBestLargestHeightForWindow(bestHeight);
            VueTrendingAislesDataModel
                    .getInstance(VueApplication.getInstance()).dataObserver();
        }
        
    }
}