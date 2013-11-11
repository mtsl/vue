package com.lateralthoughts.vue;

import java.io.File;
import java.util.ArrayList;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import android.widget.TextView.OnEditorActionListener;
import com.flurry.android.FlurryAgent;
import com.lateralthoughts.vue.AisleManager.ImageUploadCallback;
import com.lateralthoughts.vue.connectivity.AisleData;
import com.lateralthoughts.vue.connectivity.DataBaseManager;
import com.lateralthoughts.vue.connectivity.VueConnectivityManager;
import com.lateralthoughts.vue.domain.Aisle;
import com.lateralthoughts.vue.domain.VueImage;
import com.lateralthoughts.vue.utils.*;

/**
 * Fragment for creating Aisle, Updating Ailse and AddingImageToAisle
 * 
 */
public class DataEntryFragment extends Fragment {

	public static String testId = "";
	public static String testCutomUrl = "";
	private ListView mCategoryListview = null, mLookingForListview = null,
			mOccasionListview = null;
	public LinearLayout mLookingForListviewLayout = null,
			mOccasionPopup = null, mCategoryPopup = null,
			mCategoryListviewLayout = null, mOccasionListviewLayout = null,
			mDataEntryRootLayout = null;
	public LinearLayout mLookingForPopup = null;
	public TextView mTouchToChangeImage = null, mLookingForBigText = null,
			mOccassionBigText = null, mCategoryText = null,
			mHintTextForSaySomeThing, mForTextView;
	public EditTextBackEvent mLookingForText = null, mOccasionText = null,
			mSaySomethingAboutAisle = null, mFindAtText = null;
	private String mCategoryitemsArray[] = null;
	private int mCategoryitemsDrawablesArray[] = {
			R.drawable.vue_launcher_icon, R.drawable.vue_launcher_icon,
			R.drawable.vue_launcher_icon, R.drawable.vue_launcher_icon,
			R.drawable.vue_launcher_icon, R.drawable.vue_launcher_icon,
			R.drawable.vue_launcher_icon };
	public ImageView mCreateAisleBg = null, mCategoryIcon = null;
	private InputMethodManager mInputMethodManager;
	private boolean mDontGoToNextLookingFor = false,
			mDontGoToNextForOccasion = false;
	private String mPreviousLookingfor = null, mPreviousOcasion = null,
			mPreviousSaySomething = null;
	public String mPreviousFindAtText = null;
	private String mImagePath = null, mResizedImagePath = null;
	private LinearLayout mMainHeadingRow = null;
	private RelativeLayout mDataEntryBottomTopLayout = null,
			mDataEntryInviteFriendsLayout = null,
			mDataEntryInviteFriendsFacebookLayout = null,
			mDataEntryBottomBottomLayout = null,
			mDataEntryInviteFriendsCancelLayout = null,
			mDataEntryInviteFriendsGoogleplusLayout = null;
	public RelativeLayout mDataEntryInviteFriendsPopupLayout = null;
	private ImageView mFindAtIcon = null;
	public ShareDialog mShare = null;
	private float mScreenHeight = 0, mScreenWidth = 0;
	private LinearLayout mFindAtIconLayout = null;
	private ViewPager mDataEntryAislesViewpager = null;
	public static final int AISLE_IMAGE_MARGIN = 96;
	public static final String LOOKING_FOR = "Looking";
	public static final String OCCASION = "Occasion";
	public static final String CATEGORY = "Category";
	public static final String FINDAT = "findat";
	public static final String SAY_SOMETHING_ABOUT_AISLE = "SaysomethingAboutAisle";
	private int mCurrentPagePosition = 0;
	public static boolean mSaySomethingAboutAisleClicked = false;
	private ArrayList<String> mLookingForAisleKeywordsList = null,
			mOccassionAisleKeywordsList = null,
			mCategoryAilseKeywordsList = null;
	private DataBaseManager mDbManager;
	public RelativeLayout mSaySomeThingEditParent;
	private View mDataEntryFragmentView;
	private ImageResizeAsynTask mImageResizeAsynTask = null;
	private Bitmap mAisleImageBitmap = null;
	public ProgressBar mAisleBgProgressbar;
	private GestureDetector mDetector;
	private ImageView mOccasionClose, mFindatClose, mLookingforClose,
			mSaysomethingClose;
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
	private static final int CATEGORY_POPUP_DELAY = 2000;
	private boolean mIsEmptyAisle;
	

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
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
		mLookingForText = (EditTextBackEvent) mDataEntryFragmentView
				.findViewById(R.id.lookingfortext);
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
		mAisleBgProgressbar = (ProgressBar) mDataEntryFragmentView
				.findViewById(R.id.aisle_bg_progressbar);
		mSaysomethingClose = (ImageView) mDataEntryFragmentView
				.findViewById(R.id.saysomethingclose);
		mDataEntryRootLayout = (LinearLayout) mDataEntryFragmentView
				.findViewById(R.id.dataentry_root_layout);
		mOccasionListview = (ListView) mDataEntryFragmentView
				.findViewById(R.id.ocassionlistview);
		mFindAtIcon = (ImageView) mDataEntryFragmentView
				.findViewById(R.id.find_at_icon);
		mLookingForListview = (ListView) mDataEntryFragmentView
				.findViewById(R.id.lookingforlistview);
		mForTextView = (TextView) mDataEntryFragmentView
				.findViewById(R.id.for_text);
		mFindAtText = (EditTextBackEvent) mDataEntryFragmentView
				.findViewById(R.id.find_at_text);
		mFindAtIconLayout = (LinearLayout) mDataEntryFragmentView
				.findViewById(R.id.findaticonlayout);
		mDataEntryInviteFriendsCancelLayout = (RelativeLayout) mDataEntryFragmentView
				.findViewById(R.id.dataentry_invitefriends_cancellayout);
		mDataEntryBottomBottomLayout = (RelativeLayout) mDataEntryFragmentView
				.findViewById(R.id.dataentry_bottom_bottom_layout);
		mDataEntryInviteFriendsGoogleplusLayout = (RelativeLayout) mDataEntryFragmentView
				.findViewById(R.id.dataentry_invitefriends_googlepluslayout);
		mLookingForBigText = (TextView) mDataEntryFragmentView
				.findViewById(R.id.lookingforbigtext);
		mLookingForBigText.setBackgroundColor(getResources().getColor(
				R.color.yellowbgcolor));
		mDataEntryBottomTopLayout = (RelativeLayout) mDataEntryFragmentView
				.findViewById(R.id.dataentry_bottom_top_layout);
		mDataEntryInviteFriendsFacebookLayout = (RelativeLayout) mDataEntryFragmentView
				.findViewById(R.id.dataentry_invitefriends_facebooklayout);
		mDataEntryInviteFriendsPopupLayout = (RelativeLayout) mDataEntryFragmentView
				.findViewById(R.id.dataentry_invite_friends_popup_layout);
		mOccassionBigText = (TextView) mDataEntryFragmentView
				.findViewById(R.id.occassionbigtext);
		mOccasionPopup = (LinearLayout) mDataEntryFragmentView
				.findViewById(R.id.ocassionpopup);
		mOccasionText = (EditTextBackEvent) mDataEntryFragmentView
				.findViewById(R.id.occasiontext);
		mLookingForListviewLayout = (LinearLayout) mDataEntryFragmentView
				.findViewById(R.id.lookingforlistviewlayout);
		mLookingForPopup = (LinearLayout) mDataEntryFragmentView
				.findViewById(R.id.lookingforpopup);
		mTouchToChangeImage = (TextView) mDataEntryFragmentView
				.findViewById(R.id.touchtochangeimage);
		mSaySomethingAboutAisle = (EditTextBackEvent) mDataEntryFragmentView
				.findViewById(R.id.saysomethingaboutaisle);
		mCategoryIcon = (ImageView) mDataEntryFragmentView
				.findViewById(R.id.categoeryicon);
		mCategoryPopup = (LinearLayout) mDataEntryFragmentView
				.findViewById(R.id.categoerypopup);
		mCategoryText = (TextView) mDataEntryFragmentView
				.findViewById(R.id.categorytext);
		mCategoryListview = (ListView) mDataEntryFragmentView
				.findViewById(R.id.categorylistview);
		mCategoryListviewLayout = (LinearLayout) mDataEntryFragmentView
				.findViewById(R.id.categorylistviewlayout);
		mMainHeadingRow = (LinearLayout) mDataEntryFragmentView
				.findViewById(R.id.mainheadingrow);
		mLookingForListviewLayout.setVisibility(View.GONE);
		mCreateAisleBg = (ImageView) mDataEntryFragmentView
				.findViewById(R.id.createaisel_bg);
		mDataEntryInviteFriendsLayout = (RelativeLayout) mDataEntryFragmentView
				.findViewById(R.id.dataentry_invite_friends_layout);
		mHintTextForSaySomeThing = (TextView) mDataEntryFragmentView
				.findViewById(R.id.hinttext);
		mPreviousLookingfor = mLookingForText.getText().toString();
		mPreviousOcasion = mOccasionText.getText().toString();
		mPreviousSaySomething = mSaySomethingAboutAisle.getText().toString();
		mLookingForAisleKeywordsList = mDbManager
				.getAisleKeywords(VueConstants.LOOKING_FOR_TABLE);
		mSaySomeThingEditParent = (RelativeLayout) mDataEntryFragmentView
				.findViewById(R.id.sayeditparentlay);
		if (mLookingForAisleKeywordsList != null) {
			if (Utils.getDataentryAddImageAisleFlag(getActivity())) {
				if (Utils.getDataentryScreenAisleId(getActivity()) != null) {
					try {
						mLookingForText
								.setText(VueTrendingAislesDataModel
										.getInstance(getActivity())
										.getAisleAt(
												Utils.getDataentryScreenAisleId(getActivity()))
										.getAisleContext().mLookingForItem);
						mLookingForBigText
								.setText(VueTrendingAislesDataModel
										.getInstance(getActivity())
										.getAisleAt(
												Utils.getDataentryScreenAisleId(getActivity()))
										.getAisleContext().mLookingForItem);
					} catch (Exception e) {
						mLookingForText.setText(mLookingForAisleKeywordsList
								.get(0));
						mLookingForBigText.setText(mLookingForAisleKeywordsList
								.get(0));
					}
				} else {
					mLookingForText
							.setText(mLookingForAisleKeywordsList.get(0));
					mLookingForBigText.setText(mLookingForAisleKeywordsList
							.get(0));
				}
			} else {
				mLookingForText.setText(mLookingForAisleKeywordsList.get(0));
				mLookingForBigText.setText(mLookingForAisleKeywordsList.get(0));
			}
			mLookingForPopup.setVisibility(View.GONE);
			mLookingForBigText.setBackgroundColor(Color.TRANSPARENT);
			mLookingForListviewLayout.setVisibility(View.GONE);
		} else {
			mLookingForListviewLayout.setVisibility(View.GONE);
			mLookingForText.requestFocus();
			mInputMethodManager.showSoftInput(mLookingForText, 0);
		}
		mOccassionAisleKeywordsList = mDbManager
				.getAisleKeywords(VueConstants.OCCASION_TABLE);
		if (mOccassionAisleKeywordsList != null) {
			if (Utils.getDataentryAddImageAisleFlag(getActivity())) {
				if (Utils.getDataentryScreenAisleId(getActivity()) != null) {
					try {
						mOccasionText
								.setText(VueTrendingAislesDataModel
										.getInstance(getActivity())
										.getAisleAt(
												Utils.getDataentryScreenAisleId(getActivity()))
										.getAisleContext().mOccasion);
						if (VueTrendingAislesDataModel
								.getInstance(getActivity())
								.getAisleAt(
										Utils.getDataentryScreenAisleId(getActivity()))
								.getAisleContext().mOccasion != null
								&& VueTrendingAislesDataModel
										.getInstance(getActivity())
										.getAisleAt(
												Utils.getDataentryScreenAisleId(getActivity()))
										.getAisleContext().mOccasion.length() > 0) {
							mOccassionBigText
									.setText(VueTrendingAislesDataModel
											.getInstance(getActivity())
											.getAisleAt(
													Utils.getDataentryScreenAisleId(getActivity()))
											.getAisleContext().mOccasion);
						} else {
							mOccassionBigText.setText(OCCASION);
						}

					} catch (Exception e) {
						mOccasionText.setText(mOccassionAisleKeywordsList
								.get(0));
						mOccassionBigText.setText(mOccassionAisleKeywordsList
								.get(0));
					}
				} else {
					mOccasionText.setText(mOccassionAisleKeywordsList.get(0));
					mOccassionBigText.setText(mOccassionAisleKeywordsList
							.get(0));
				}
			} else {
				mOccasionText.setText(mOccassionAisleKeywordsList.get(0));
				mOccassionBigText.setText(mOccassionAisleKeywordsList.get(0));
			}
		}
		mCategoryAilseKeywordsList = mDbManager
				.getAisleKeywords(VueConstants.CATEGORY_TABLE);
		if (mCategoryAilseKeywordsList != null) {
			if (Utils.getDataentryAddImageAisleFlag(getActivity())) {
				if (Utils.getDataentryScreenAisleId(getActivity()) != null) {
					try {
						mCategoryText
								.setText(VueTrendingAislesDataModel
										.getInstance(getActivity())
										.getAisleAt(
												Utils.getDataentryScreenAisleId(getActivity()))
										.getAisleContext().mCategory);
					} catch (Exception e) {
						/*
						 * mCategoryText
						 * .setText(mCategoryAilseKeywordsList.get(0));
						 */
					}
				} else {
					// mCategoryText.setText(mCategoryAilseKeywordsList.get(0));
				}
			} else {
				// mCategoryText.setText(mCategoryAilseKeywordsList.get(0));
			}
		}
		if (Utils.getDataentryAddImageAisleFlag(getActivity())) {
			if (Utils.getDataentryScreenAisleId(getActivity()) != null) {
				try {
					String description = VueTrendingAislesDataModel
							.getInstance(getActivity())
							.getAisleAt(
									Utils.getDataentryScreenAisleId(getActivity()))
							.getAisleContext().mDescription;
					if (description != null && description.trim().length() > 0) {
						mSaySomethingAboutAisle.setText(description);
						mHintTextForSaySomeThing.setText(description);
					}

				} catch (Exception e) {
				}
			}
		}
		mSaySomethingAboutAisle
				.setOnEditorActionListener(new OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView arg0, int arg1,
							KeyEvent arg2) {
						mSaySomethingAboutAisle.setCursorVisible(false);
						mPreviousSaySomething = mSaySomethingAboutAisle
								.getText().toString();
						mInputMethodManager.hideSoftInputFromWindow(
								mSaySomethingAboutAisle.getWindowToken(), 0);
						mInputMethodManager.hideSoftInputFromWindow(
								mOccasionText.getWindowToken(), 0);
						mInputMethodManager.hideSoftInputFromWindow(
								mLookingForText.getWindowToken(), 0);
						String tempString = mSaySomethingAboutAisle.getText()
								.toString();
						if (tempString != null
								&& !tempString.equalsIgnoreCase("")) {
							mHintTextForSaySomeThing.setText(tempString);
						}
						mSaySomeThingEditParent.setVisibility(View.VISIBLE);
						mSaySomethingAboutAisle.setVisibility(View.GONE);
						mSaysomethingClose.setVisibility(View.GONE);
						return true;
					}
				});
		mSaySomeThingEditParent.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mSaySomeThingEditParent.setVisibility(View.GONE);
				mSaySomethingAboutAisle.setVisibility(View.VISIBLE);
				mSaysomethingClose.setVisibility(View.VISIBLE);
				mSaySomeThingEditParent.post(new Runnable() {

					@Override
					public void run() {
						mSaySomethingAboutAisle.requestFocus();
						mSaySomethingAboutAisle.setFocusable(true);
						mSaySomethingAboutAisle.setCursorVisible(true);
						mSaySomethingAboutAisle
								.setSelection(mSaySomethingAboutAisle.getText()
										.toString().length());
					}
				});
				mSaySomethingAboutAisleClicked = true;
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
				mCategoryPopup.setVisibility(View.GONE);
				mFindatClose.setVisibility(View.GONE);
				mFindAtIconLayout.setVisibility(View.GONE);
				mFindAtText.setVisibility(View.GONE);
				mCategoryListviewLayout.setVisibility(View.GONE);
				mOccassionBigText.setBackgroundColor(Color.TRANSPARENT);
				mLookingForBigText.setBackgroundColor(Color.TRANSPARENT);
				final InputMethodManager inputMethodManager = (InputMethodManager) getActivity()
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				inputMethodManager.toggleSoftInputFromWindow(
						mSaySomethingAboutAisle.getApplicationWindowToken(),
						InputMethodManager.SHOW_FORCED, 0);

			}
		});
		final OnInterceptListener mSayBoutListner = new OnInterceptListener() {

			@Override
			public void setFlag(boolean flag) {
			}

			@Override
			public void onKeyBackPressed() {
				saySomethingABoutAisleInterceptListnerFunctionality();
			}

			@Override
			public boolean getFlag() {
				return false;
			}
		};

		mSaySomethingAboutAisle.setonInterceptListen(mSayBoutListner);
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
						mLookingForBigText
								.setBackgroundColor(Color.TRANSPARENT);
						if (mLookingForText.getText().toString().trim()
								.length() > 0) {
							mLookingForBigText.setText(mLookingForText
									.getText().toString());
						} else {
							mLookingForBigText.setText(LOOKING_FOR);
						}
						mPreviousLookingfor = mLookingForText.getText()
								.toString();
						mLookingForPopup.setVisibility(View.GONE);
						mLookingForListviewLayout.setVisibility(View.GONE);
						mInputMethodManager.hideSoftInputFromWindow(
								mLookingForText.getWindowToken(), 0);
						if (!mDontGoToNextLookingFor) {
							mOccassionBigText.setBackgroundColor(getResources()
									.getColor(R.color.yellowbgcolor));
							mOccasionPopup.setVisibility(View.VISIBLE);
							if (mOccassionAisleKeywordsList != null
									&& mOccassionAisleKeywordsList.size() > 0) {
								mOccasionListviewLayout
										.setVisibility(View.VISIBLE);
								mOccasionListview
										.setAdapter(new OccassionAdapter(
												getActivity(),
												mOccassionAisleKeywordsList));
							}
							mOccasionText.requestFocus();
							mInputMethodManager.showSoftInput(mOccasionText, 0);
						}
						return true;
					}
				});
		mLookingForText.setonInterceptListen(new OnInterceptListener() {
			public void onKeyBackPressed() {
				lookingForInterceptListnerFunctionality();
			}

			@Override
			public void setFlag(boolean flag) {
			}

			@Override
			public boolean getFlag() {
				return false;
			}
		});
		mOccasionText.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView arg0, int actionId,
					KeyEvent arg2) {
				mInputMethodManager.hideSoftInputFromWindow(
						mOccasionText.getWindowToken(), 0);
				mOccassionBigText.setBackgroundColor(Color.TRANSPARENT);
				if (mOccasionText.getText().toString().trim().length() > 0) {
					mOccassionBigText.setText(mOccasionText.getText()
							.toString());
				} else {
					mOccassionBigText.setText(OCCASION);
				}
				mPreviousOcasion = mOccasionText.getText().toString();
				mOccasionPopup.setVisibility(View.GONE);
				mOccasionListviewLayout.setVisibility(View.GONE);
				if (!mDontGoToNextForOccasion) {
					mCategoryListview.setVisibility(View.VISIBLE);
					mCategoryListview.setAdapter(new CategoryAdapter(
							getActivity()));
					mCategoryListviewLayout.setVisibility(View.VISIBLE);
					mCategoryPopup.setVisibility(View.VISIBLE);
				}
				return true;
			};
		});
		mOccasionText.setonInterceptListen(new OnInterceptListener() {

			@Override
			public void setFlag(boolean flag) {
			}

			@Override
			public void onKeyBackPressed() {
				occasionInterceptListnerFunctionality();
			}

			@Override
			public boolean getFlag() {
				return false;
			}
		});
		mLookingForBigText.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				hideAllEditableTextboxes();
				mOccassionBigText.setBackgroundColor(Color.TRANSPARENT);
				mOccasionPopup.setVisibility(View.GONE);
				mOccasionListviewLayout.setVisibility(View.GONE);
				mInputMethodManager.hideSoftInputFromWindow(
						mOccasionText.getWindowToken(), 0);
				mInputMethodManager.hideSoftInputFromWindow(
						mSaySomethingAboutAisle.getWindowToken(), 0);
				mInputMethodManager.hideSoftInputFromWindow(
						mFindAtText.getWindowToken(), 0);
				mFindatClose.setVisibility(View.GONE);
				mFindAtIconLayout.setVisibility(View.GONE);
				mFindAtText.setVisibility(View.GONE);				
				mCategoryListview.setVisibility(View.GONE);
				mCategoryListviewLayout.setVisibility(View.GONE);
				mCategoryPopup.setVisibility(View.GONE);
				if (Utils.getDataentryAddImageAisleFlag(getActivity())
						|| Utils.getDataentryEditAisleFlag(getActivity())) {
					showAlertForEditPermission(LOOKING_FOR);
				} else {
					lookingForTextClickFunctionality();
				}
			}
		});
		mOccassionBigText.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				hideAllEditableTextboxes();
				mLookingForPopup.setVisibility(View.GONE);
				mLookingForListviewLayout.setVisibility(View.GONE);
				mLookingForBigText.setBackgroundColor(Color.TRANSPARENT);
				mInputMethodManager.hideSoftInputFromWindow(
						mLookingForText.getWindowToken(), 0);
				mInputMethodManager.hideSoftInputFromWindow(
						mFindAtText.getWindowToken(), 0);
				mInputMethodManager.hideSoftInputFromWindow(
						mSaySomethingAboutAisle.getWindowToken(), 0);
				mFindatClose.setVisibility(View.GONE);
				mFindAtIconLayout.setVisibility(View.GONE);
				mFindAtText.setVisibility(View.GONE);
				mCategoryListview.setVisibility(View.GONE);
				mCategoryListviewLayout.setVisibility(View.GONE);
				mCategoryPopup.setVisibility(View.GONE);
				if (Utils.getDataentryAddImageAisleFlag(getActivity())
						|| Utils.getDataentryEditAisleFlag(getActivity())) {
					showAlertForEditPermission(OCCASION);
				} else {
					occassionTextClickFunctionality();
				}
			}
		});
		mForTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				hideAllEditableTextboxes();
				mLookingForPopup.setVisibility(View.GONE);
				mLookingForListviewLayout.setVisibility(View.GONE);
				mLookingForBigText.setBackgroundColor(Color.TRANSPARENT);
				mInputMethodManager.hideSoftInputFromWindow(
						mLookingForText.getWindowToken(), 0);
				mInputMethodManager.hideSoftInputFromWindow(
						mFindAtText.getWindowToken(), 0);
				mInputMethodManager.hideSoftInputFromWindow(
						mSaySomethingAboutAisle.getWindowToken(), 0);
				mFindatClose.setVisibility(View.GONE);
				mFindAtIconLayout.setVisibility(View.GONE);
				mFindAtText.setVisibility(View.GONE);
				mCategoryListview.setVisibility(View.GONE);
				mCategoryListviewLayout.setVisibility(View.GONE);
				mCategoryPopup.setVisibility(View.GONE);
				if (Utils.getDataentryAddImageAisleFlag(getActivity())
						|| Utils.getDataentryEditAisleFlag(getActivity())) {
					showAlertForEditPermission(OCCASION);
				} else {
					occassionTextClickFunctionality();
				}
			}
		});
		mCategoryIcon.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mLookingForPopup.setVisibility(View.GONE);
				mLookingForListviewLayout.setVisibility(View.GONE);
				mOccasionPopup.setVisibility(View.GONE);
				mOccasionListviewLayout.setVisibility(View.GONE);
				mOccassionBigText.setBackgroundColor(Color.TRANSPARENT);
				mLookingForBigText.setBackgroundColor(Color.TRANSPARENT);
				mInputMethodManager.hideSoftInputFromWindow(
						mOccasionText.getWindowToken(), 0);
				mInputMethodManager.hideSoftInputFromWindow(
						mLookingForText.getWindowToken(), 0);
				mInputMethodManager.hideSoftInputFromWindow(
						mFindAtText.getWindowToken(), 0);
				mInputMethodManager.hideSoftInputFromWindow(
						mSaySomethingAboutAisle.getWindowToken(), 0);
				mFindatClose.setVisibility(View.GONE);
				mFindAtIconLayout.setVisibility(View.GONE);
				mFindAtText.setVisibility(View.GONE);
				if (Utils.getDataentryAddImageAisleFlag(getActivity())
						|| Utils.getDataentryEditAisleFlag(getActivity())) {
					showAlertForEditPermission(CATEGORY);
				} else {
					categoryIconClickFunctionality();
				}
			}
		});
		mTouchToChangeImage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				touchToChangeImageClickFunctionality();
			}
		});
		mDataEntryInviteFriendsLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				mDataEntryInviteFriendsPopupLayout.setVisibility(View.VISIBLE);
			}
		});
		mDataEntryInviteFriendsFacebookLayout
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						mDataEntryInviteFriendsPopupLayout
								.setVisibility(View.GONE);
						Intent i = new Intent(getActivity(),
								DataEntryInviteFriendsActivity.class);
						Bundle b = new Bundle();
						b.putBoolean(
								VueConstants.DATA_ENTRY_INVITE_FRIENDS_BUNDLE_FROM_GOOGLEPLUS_FLAG_KEY,
								false);
						i.putExtras(b);
						getActivity().startActivity(i);
					}
				});
		mDataEntryInviteFriendsGoogleplusLayout
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						mDataEntryInviteFriendsPopupLayout
								.setVisibility(View.GONE);
						Intent i = new Intent(getActivity(),
								DataEntryInviteFriendsActivity.class);
						Bundle b = new Bundle();
						b.putBoolean(
								VueConstants.DATA_ENTRY_INVITE_FRIENDS_BUNDLE_FROM_GOOGLEPLUS_FLAG_KEY,
								true);
						try {
							b.putString(
									VueConstants.DATA_ENTRY_INVITE_FRIENDS_BUNDLE_FROM_FILE_PATH_ARRAY_KEY,
									Utils.readAisleImagePathListFromFile(
											getActivity(),
											VueConstants.AISLE_IMAGE_PATH_LIST_FILE_NAME)
											.get(mDataEntryAislesViewpager
													.getCurrentItem())
											.getImagePath());
						} catch (Exception e) {
						}
						i.putExtras(b);
						getActivity().startActivity(i);
					}
				});
		mDataEntryInviteFriendsCancelLayout
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						mDataEntryInviteFriendsPopupLayout
								.setVisibility(View.GONE);
					}
				});
		mFindAtIcon.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mFindatClose.setVisibility(View.VISIBLE);
				mFindAtIconLayout.setVisibility(View.VISIBLE);
				mFindAtText.setVisibility(View.VISIBLE);
				mLookingForPopup.setVisibility(View.GONE);
				mLookingForListviewLayout.setVisibility(View.GONE);
				mOccasionPopup.setVisibility(View.GONE);
				mOccasionListviewLayout.setVisibility(View.GONE);
				mCategoryPopup.setVisibility(View.GONE);
				mCategoryListviewLayout.setVisibility(View.GONE);
				mOccassionBigText.setBackgroundColor(Color.TRANSPARENT);
				mLookingForBigText.setBackgroundColor(Color.TRANSPARENT);
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
			}
		});
		mFindAtText.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView arg0, int actionId,
					KeyEvent arg2) {
				mInputMethodManager.hideSoftInputFromWindow(
						mFindAtText.getWindowToken(), 0);
				mPreviousFindAtText = mFindAtText.getText().toString();
				mOtherSourceSelectedImageDetailsUrl = mPreviousFindAtText;
				mFindatClose.setVisibility(View.GONE);
				mFindAtIconLayout.setVisibility(View.GONE);
				mFindAtText.setVisibility(View.GONE);
				return true;
			};
		});
		mFindAtText.setonInterceptListen(new OnInterceptListener() {

			@Override
			public void setFlag(boolean flag) {

			}

			@Override
			public void onKeyBackPressed() {
				findAtInterceptListnerFunctionality();

			}

			@Override
			public boolean getFlag() {
				return false;
			}
		});
		mDataEntryRootLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				hideAllEditableTextboxes();
			}
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
		mDetector = new GestureDetector(getActivity(), new mListener());
		mCreateAisleBg.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				mDetector.onTouchEvent(arg1);
				return true;
			}
		});
		mDataEntryAislesViewpager
				.setOnPageChangeListener(new OnPageChangeListener() {

					@Override
					public void onPageSelected(int arg0) {
						mDataEntryInviteFriendsPopupLayout
								.setVisibility(View.GONE);
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
				mHintTextForSaySomeThing.setText("");
			}
		});
		return mDataEntryFragmentView;
	}

	private void hideAllEditableTextboxes() {
		if (mOccasionText.getText().toString().trim().length() > 0) {
			mOccassionBigText.setText(mOccasionText.getText().toString());
		} else {
			mOccassionBigText.setText(OCCASION);
		}
		if (mLookingForText.getText().toString().trim().length() > 0) {
			mLookingForBigText.setText(mLookingForText.getText().toString());
		} else {
			mLookingForBigText.setText(LOOKING_FOR);
		}
		mSaySomethingAboutAisle.setCursorVisible(false);
		mPreviousSaySomething = mSaySomethingAboutAisle.getText().toString();
		String tempString = mSaySomethingAboutAisle.getText().toString();
		if (tempString != null && !tempString.equalsIgnoreCase("")) {
			mHintTextForSaySomeThing.setText(tempString);
		}
		// hiding keyboard
		mInputMethodManager.hideSoftInputFromWindow(
				mSaySomethingAboutAisle.getWindowToken(), 0);
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
		mCategoryPopup.setVisibility(View.GONE);
		mFindatClose.setVisibility(View.GONE);
		mFindAtIconLayout.setVisibility(View.GONE);
		mFindAtText.setVisibility(View.GONE);
		mCategoryListviewLayout.setVisibility(View.GONE);
		mOccassionBigText.setBackgroundColor(Color.TRANSPARENT);
		mLookingForBigText.setBackgroundColor(Color.TRANSPARENT);
		mSaySomeThingEditParent.setVisibility(View.VISIBLE);
		mSaySomethingAboutAisle.setVisibility(View.GONE);
		mSaysomethingClose.setVisibility(View.GONE);
		mDataEntryInviteFriendsPopupLayout.setVisibility(View.GONE);
	}

	public void lookingForInterceptListnerFunctionality() {
		mLookingForPopup.setVisibility(View.GONE);
		mLookingForListviewLayout.setVisibility(View.GONE);
		mOccasionPopup.setVisibility(View.GONE);
		mOccasionListviewLayout.setVisibility(View.GONE);
		mLookingForText.setText(mPreviousLookingfor);
		mOccassionBigText.setBackgroundColor(Color.TRANSPARENT);
		mLookingForBigText.setBackgroundColor(Color.TRANSPARENT);
		mInputMethodManager.hideSoftInputFromWindow(
				mSaySomethingAboutAisle.getWindowToken(), 0);
		mInputMethodManager.hideSoftInputFromWindow(
				mOccasionText.getWindowToken(), 0);
		mInputMethodManager.hideSoftInputFromWindow(
				mLookingForText.getWindowToken(), 0);
	}

	public void occasionInterceptListnerFunctionality() {
		mLookingForPopup.setVisibility(View.GONE);
		mLookingForListviewLayout.setVisibility(View.GONE);
		mOccasionPopup.setVisibility(View.GONE);
		mOccasionListviewLayout.setVisibility(View.GONE);
		mOccasionText.setText(mPreviousOcasion);
		mOccassionBigText.setBackgroundColor(Color.TRANSPARENT);
		mLookingForBigText.setBackgroundColor(Color.TRANSPARENT);
		mInputMethodManager.hideSoftInputFromWindow(
				mSaySomethingAboutAisle.getWindowToken(), 0);
		mInputMethodManager.hideSoftInputFromWindow(
				mOccasionText.getWindowToken(), 0);
		mInputMethodManager.hideSoftInputFromWindow(
				mLookingForText.getWindowToken(), 0);
	}

	public void findAtInterceptListnerFunctionality() {
		mFindAtText.setText(mPreviousFindAtText);
		mFindatClose.setVisibility(View.GONE);
		mFindAtIconLayout.setVisibility(View.GONE);
		mFindAtText.setVisibility(View.GONE);
		mInputMethodManager.hideSoftInputFromWindow(
				mSaySomethingAboutAisle.getWindowToken(), 0);
		mInputMethodManager.hideSoftInputFromWindow(
				mOccasionText.getWindowToken(), 0);
		mInputMethodManager.hideSoftInputFromWindow(
				mLookingForText.getWindowToken(), 0);
		mInputMethodManager.hideSoftInputFromWindow(
				mFindAtText.getWindowToken(), 0);
	}

	public void saySomethingABoutAisleInterceptListnerFunctionality() {
		mSaySomethingAboutAisleClicked = false;
		mSaySomethingAboutAisle.setCursorVisible(false);
		mInputMethodManager.hideSoftInputFromWindow(
				mSaySomethingAboutAisle.getWindowToken(), 0);
		mSaySomethingAboutAisle.setText(mPreviousSaySomething);
		mSaySomeThingEditParent.setVisibility(View.VISIBLE);
		mSaySomethingAboutAisle.setVisibility(View.GONE);
		mSaysomethingClose.setVisibility(View.GONE);

	}

	private void touchToChangeImageClickFunctionality() {
		hideAllEditableTextboxes();
		Intent intent = new Intent(getActivity(),
				CreateAisleSelectionActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		if (mFromDetailsScreenFlag) {
			Utils.putFromDetailsScreenToDataentryCreateAisleScreenPreferenceFlag(
					getActivity(), true);
		} else {
			Utils.putFromDetailsScreenToDataentryCreateAisleScreenPreferenceFlag(
					getActivity(), false);
		}
		Bundle b = new Bundle();
		b.putBoolean(VueConstants.FROMCREATEAILSESCREENFLAG, true);
		intent.putExtras(b);
		if (!CreateAisleSelectionActivity.isActivityShowing) {
			CreateAisleSelectionActivity.isActivityShowing = true;
			getActivity().startActivityForResult(intent,
					VueConstants.CREATE_AILSE_ACTIVITY_RESULT);
		}
	}

	public void createAisleClickFunctionality() {
		hideAllEditableTextboxes();
		if (!(mLookingForBigText.getText().toString().trim()
				.equals(LOOKING_FOR))
				&& (mCategoryText.getText().toString().trim().length() > 0)) {
			if (Utils.getDataentryAddImageAisleFlag(getActivity())) {
				addImageToAisle();
			} else {
				if (mFromDetailsScreenFlag) {
					if (checkLimitForLoginDialog()) {
						if (mLoginWarningMessage == null) {
							mLoginWarningMessage = new LoginWarningMessage(
									getActivity());
						}
						mLoginWarningMessage
								.showLoginWarningMessageDialog(
										"You need to Login with the app to add image to aisle.",
										true, true, 0, null, null);
					} else {
						if (mAisleImageBitmap != null) {
							Log.e("Land", "vueland 10");
							VueUser storedVueUser = null;
							try {
								storedVueUser = Utils
										.readUserObjectFromFile(
												getActivity(),
												VueConstants.VUE_APP_USEROBJECT__FILENAME);
							} catch (Exception e2) {
								e2.printStackTrace();
							}
							if (storedVueUser != null
									&& storedVueUser.getId() != null) {
								if (VueConnectivityManager
										.isNetworkConnected(getActivity())) {
									Intent intent = new Intent();
									Bundle b = new Bundle();
									b.putString(
											VueConstants.CREATE_AISLE_CAMERA_GALLERY_IMAGE_PATH_BUNDLE_KEY,
											mImagePath);
									b.putString(
											VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_LOOKINGFOR,
											mLookingForBigText.getText()
													.toString());
									b.putString(
											VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_OCCASION,
											mOccassionBigText.getText()
													.toString());
									b.putString(
											VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_CATEGORY,
											mCategoryText.getText().toString());
									b.putString(
											VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_SAYSOMETHINGABOUTAISLE,
											mSaySomethingAboutAisle.getText()
													.toString());
									b.putString(
											VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_FINDAT,
											mFindAtText.getText().toString());
									b.putString(
											VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_IMAGEURL,
											mOtherSourceSelectedImageUrl);
									b.putInt(
											VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_IMAGE_WIDTH,
											mOtherSourceImageOriginalWidth);
									b.putInt(
											VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_IMAGE_HEIGHT,
											mOtherSourceImageOriginalHeight);
									b.putString(
											VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_IMAGE_DETAILSURL,
											mOtherSourceSelectedImageDetailsUrl);
									b.putString(
											VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_IMAGE_STORE,
											mOtherSourceSelectedImageStore);
									String offlineImageId = String
											.valueOf(System.currentTimeMillis());
									b.putString(
											VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_OFFLINE_IMAGE_ID,
											offlineImageId);
									intent.putExtras(b);
									Log.e("Land", "vueland 11");
									getActivity()
											.setResult(
													VueConstants.FROM_DETAILS_SCREEN_TO_DATAENTRY_SCREEN_ACTIVITY_RESULT,
													intent);
									addImageToAisleToServer(
											Long.valueOf(storedVueUser.getId())
													.toString(), VueApplication
													.getInstance()
													.getClickedWindowID(),
											true, offlineImageId);
									String categoery = mCategoryText.getText()
											.toString().trim();
									String lookingFor = mLookingForBigText
											.getText().toString().trim();
									String occassion = "";
									if (!(mOccassionBigText.getText()
											.toString().trim().equals(OCCASION
											.trim()))) {
										occassion = mOccassionBigText.getText()
												.toString().trim();
									}
									String description = mSaySomethingAboutAisle
											.getText().toString().trim();

									checkForAisleUpdate(storedVueUser,
											VueApplication.getInstance()
													.getClickedWindowID(),
											categoery, lookingFor, occassion,
											description);
									getActivity().finish();
								} else {
									Toast.makeText(
											getActivity(),
											getResources().getString(
													R.string.no_network),
											Toast.LENGTH_LONG).show();
								}
							} else {
								showLogInDialog(false);
							}
						} else {
							showAlertForMandotoryFields(getResources()
									.getString(
											R.string.dataentry_mandtory_field_add_aisleimage_mesg));
						}
					}
				} else {
					if (mAisleImageBitmap != null) {
						addORUpdateAisle();
					} else {
						showAlertForMandotoryFields(getResources()
								.getString(
										R.string.dataentry_mandtory_field_add_aisleimage_mesg));
					}
				}
			}
		} else {
			if (mCategoryText.getText().toString().trim().length() == 0) {
				categoryIconClickFunctionality();
				showAlertForMandotoryFields("choose a category");
			} else {
				showAlertForMandotoryFields(getResources().getString(
						R.string.dataentry_mandtory_field_mesg));
			}
		}
	}

	public void editButtonClickFunctionality() {
		mDataEntryInviteFriendsPopupLayout.setVisibility(View.GONE);
		Utils.putDataentryEditAisleFlag(getActivity(), true);
		mCurrentPagePosition = mDataEntryAislesViewpager.getCurrentItem();
		ArrayList<DataentryImage> mAisleImagePathList = null;
		try {
			mAisleImagePathList = Utils
					.readAisleImagePathListFromFile(getActivity(),
							VueConstants.AISLE_IMAGE_PATH_LIST_FILE_NAME);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (mAisleImagePathList != null && mAisleImagePathList.size() > 0) {
			mResizedImagePath = mAisleImagePathList.get(mCurrentPagePosition)
					.getImagePath();
			mImagePath = mAisleImagePathList.get(mCurrentPagePosition)
					.getImagePath();
			mFindAtText.setText(mAisleImagePathList.get(mCurrentPagePosition)
					.getSourceUrl());
			mOtherSourceSelectedImageDetailsUrl = mAisleImagePathList.get(
					mCurrentPagePosition).getSourceUrl();
			File aisleFile = new File(mAisleImagePathList.get(
					mCurrentPagePosition).getImagePath());
			if (aisleFile.exists()) {
				mAisleImageBitmap = BitmapFactory.decodeFile(aisleFile
						.getPath());
				mCreateAisleBg.setImageBitmap(mAisleImageBitmap);
			}
		}
		mDataEntryAislesViewpager.setVisibility(View.GONE);
		mCreateAisleBg.setVisibility(View.VISIBLE);
		if (mDataEntryActivity == null) {
			mDataEntryActivity = (DataEntryActivity) getActivity();
		}
		mDataEntryActivity.mVueDataentryActionbarScreenName
				.setText(getResources().getString(
						R.string.edit_aisle_screen_title));
		mDataEntryActivity.mVueDataentryActionbarBottomLayout
				.setVisibility(View.GONE);
		mDataEntryActivity.mVueDataentryActionbarTopLayout
				.setVisibility(View.VISIBLE);
		mDataEntryBottomBottomLayout.setVisibility(View.VISIBLE);
		mDataEntryBottomTopLayout.setVisibility(View.GONE);
		mMainHeadingRow.setVisibility(View.VISIBLE);
		mTouchToChangeImage.setVisibility(View.VISIBLE);
		mOccassionBigText.setBackgroundColor(Color.TRANSPARENT);
		mLookingForBigText.setBackgroundColor(Color.TRANSPARENT);
	}

	public void shareClickFunctionality() {
		mDataEntryInviteFriendsPopupLayout.setVisibility(View.GONE);
		mShare = new ShareDialog(getActivity(), getActivity());
		ArrayList<DataentryImage> mAisleImagePathList = null;
		try {
			mAisleImagePathList = Utils
					.readAisleImagePathListFromFile(getActivity(),
							VueConstants.AISLE_IMAGE_PATH_LIST_FILE_NAME);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (mAisleImagePathList != null) {
			ArrayList<clsShare> imageUrlList = new ArrayList<clsShare>();
			String lookingFor = "", aisleOwnerName = "", isUserAisleFlag = "0";
			try {
				if (Utils.getDataentryScreenAisleId(getActivity()) != null) {
					Log.e("DataentryFragment",
							"share aisleid : "
									+ Utils.getDataentryScreenAisleId(getActivity()));
					AisleWindowContent aisleWindowContent = VueTrendingAislesDataModel
							.getInstance(getActivity())
							.getAisleAt(
									Utils.getDataentryScreenAisleId(getActivity()));
					if (aisleWindowContent != null) {
						lookingFor = aisleWindowContent.getAisleContext().mLookingForItem;
						aisleOwnerName = aisleWindowContent.getAisleContext().mFirstName
								+ " "
								+ aisleWindowContent.getAisleContext().mLastName;
						if (String
								.valueOf(
										VueApplication.getInstance()
												.getmUserId())
								.equals(aisleWindowContent.getAisleContext().mUserId)) {
							isUserAisleFlag = "1";
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			for (int i = 0; i < mAisleImagePathList.size(); i++) {
				clsShare shareObj = new clsShare(null, mAisleImagePathList.get(
						i).getImagePath(), lookingFor, aisleOwnerName,
						isUserAisleFlag);
				imageUrlList.add(shareObj);
			}
			if (mDataEntryAislesViewpager != null) {
				mShare.share(imageUrlList, "", "",
						mDataEntryAislesViewpager.getCurrentItem());
			} else {
				mShare.share(imageUrlList, "", "", 0);
			}
		}
	}

	public void addImageToAisleButtonClickFunctionality() {
		mDataEntryInviteFriendsPopupLayout.setVisibility(View.GONE);
		Utils.putDataentryAddImageAisleFlag(getActivity(), true);
		Intent intent = new Intent(getActivity(),
				CreateAisleSelectionActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		Utils.putFromDetailsScreenToDataentryCreateAisleScreenPreferenceFlag(
				getActivity(), false);
		Bundle b = new Bundle();
		b.putBoolean(VueConstants.FROMCREATEAILSESCREENFLAG, true);
		intent.putExtras(b);
		if (!CreateAisleSelectionActivity.isActivityShowing) {
			CreateAisleSelectionActivity.isActivityShowing = true;
			getActivity().startActivityForResult(intent,
					VueConstants.CREATE_AILSE_ACTIVITY_RESULT);
		}
	}

	private void showAlertForEditPermission(final String sourceName) {
		final Dialog dialog = new Dialog(getActivity(),
				R.style.Theme_Dialog_Translucent);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.vue_popup);
		TextView noButton = (TextView) dialog.findViewById(R.id.nobutton);
		TextView okButton = (TextView) dialog.findViewById(R.id.okbutton);
		TextView messagetext = (TextView) dialog.findViewById(R.id.messagetext);
		messagetext.setText(getResources().getString(
				R.string.dataentry_edit_permission));
		okButton.setText(getResources().getString(R.string.yes_mesg));
		noButton.setText(getResources().getString(R.string.no_mesg));
		okButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				dialog.dismiss();
				if (sourceName.equals(LOOKING_FOR)) {
					lookingForTextClickFunctionality();
				} else if (sourceName.equals(OCCASION)) {
					occassionTextClickFunctionality();
				} else if (sourceName.equals(CATEGORY)) {
					categoryIconClickFunctionality();
				}
			}
		});
		noButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.show();
	}

	private void showAlertForMandotoryFields(String message) {
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

	private void lookingForTextClickFunctionality() {
		mDontGoToNextLookingFor = true;
		mLookingForPopup.setVisibility(View.VISIBLE);
		if (mLookingForAisleKeywordsList != null
				&& mLookingForAisleKeywordsList.size() > 0) {
			mLookingForListviewLayout.setVisibility(View.VISIBLE);
			mLookingForListview.setAdapter(new LookingForAdapter(getActivity(),
					mLookingForAisleKeywordsList));
		}
		mLookingForBigText.setBackgroundColor(getResources().getColor(
				R.color.yellowbgcolor));
		mLookingForText.requestFocus();
		mLookingForText.setSelection(mLookingForText.getText().toString()
				.length());
		mInputMethodManager.showSoftInput(mLookingForText, 0);
	}

	private void occassionTextClickFunctionality() {
		mDontGoToNextForOccasion = true;
		mOccasionPopup.setVisibility(View.VISIBLE);
		if (mOccassionAisleKeywordsList != null
				&& mOccassionAisleKeywordsList.size() > 0) {
			mOccasionListviewLayout.setVisibility(View.VISIBLE);
			mOccasionListview.setAdapter(new OccassionAdapter(getActivity(),
					mOccassionAisleKeywordsList));
		}
		mOccassionBigText.setBackgroundColor(getResources().getColor(
				R.color.yellowbgcolor));
		mOccasionText.requestFocus();
		mOccasionText.setSelection(mOccasionText.getText().toString().length());
		mInputMethodManager.showSoftInput(mOccasionText, 0);
	}

	private void categoryIconClickFunctionality() {
		mCategoryListview.setVisibility(View.VISIBLE);
		mCategoryListview.setAdapter(new CategoryAdapter(getActivity()));
		mCategoryListviewLayout.setVisibility(View.VISIBLE);
		if (mCategoryText.getText().toString().trim().length() > 0) {
			mCategoryPopup.setVisibility(View.VISIBLE);
		} else {
			mCategoryPopup.setVisibility(View.GONE);
		}
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
						mLookingForBigText.setText(lookingForKeywordsList
								.get(position));
						mLookingForText.setText(lookingForKeywordsList
								.get(position));
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
						mOccassionBigText.setText(occassionKeywordsList
								.get(position));
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
			if (mCategoryitemsArray[position].equals(mCategoryText.getText()
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
					mCategoryText.setText(mCategoryitemsArray[position]);
					mCategoryListview.setVisibility(View.GONE);
					mCategoryPopup.setVisibility(View.VISIBLE);
					mCategoryListviewLayout.setVisibility(View.GONE);
					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
							mCategoryPopup.setVisibility(View.GONE);
						}
					}, CATEGORY_POPUP_DELAY);
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
			boolean dontResizeImageFlag) {
		if (!Utils.getDataentryAddImageAisleFlag(getActivity())
				&& !mFromDetailsScreenFlag) {
			mLookingForPopup.setVisibility(View.VISIBLE);
		}
		try {
			Log.e("frag1", "gallery called,,,," + picturePath);
			Log.e("cs", "8");
			mAisleBgProgressbar.setVisibility(View.VISIBLE);
			mCreateAisleBg.setVisibility(View.GONE);
			mImagePath = picturePath;
			if (dontResizeImageFlag) {
				mAisleBgProgressbar.setVisibility(View.GONE);
				mCreateAisleBg.setVisibility(View.VISIBLE);
				mResizedImagePath = mImagePath;
				if (mImagePath != null) {
					mAisleImageBitmap = BitmapFactory.decodeFile(mImagePath);
				}
				if (mAisleImageBitmap != null) {
					RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
							(int) mAisleImageBitmap.getWidth(),
							(int) mAisleImageBitmap.getHeight());
					lp.addRule(RelativeLayout.CENTER_IN_PARENT,
							RelativeLayout.TRUE);
					lp.setMargins(8, 8, 8, 8);
					mTouchToChangeImage.setLayoutParams(lp);
					mCreateAisleBg.setImageBitmap(mAisleImageBitmap);
				} else {
					mCreateAisleBg.setImageDrawable(getResources().getDrawable(
							R.drawable.no_image));
				}
			} else {
				mImageResizeAsynTask = new ImageResizeAsynTask();
				mImageResizeAsynTask.execute(getActivity());
			}
			if (Utils.getDataentryAddImageAisleFlag(getActivity())) {
				mDataEntryAislesViewpager.setVisibility(View.GONE);
				if (mDataEntryActivity == null) {
					mDataEntryActivity = (DataEntryActivity) getActivity();
				}
				mDataEntryActivity.mVueDataentryActionbarScreenName
						.setText(getResources().getString(
								R.string.add_imae_to_aisle_screen_title));
				mDataEntryActivity.mVueDataentryActionbarBottomLayout
						.setVisibility(View.GONE);
				mDataEntryActivity.mVueDataentryActionbarTopLayout
						.setVisibility(View.VISIBLE);
				mDataEntryBottomBottomLayout.setVisibility(View.VISIBLE);
				mDataEntryBottomTopLayout.setVisibility(View.GONE);
				mMainHeadingRow.setVisibility(View.VISIBLE);
				mTouchToChangeImage.setVisibility(View.VISIBLE);
				mOccassionBigText.setBackgroundColor(Color.TRANSPARENT);
				mLookingForBigText.setBackgroundColor(Color.TRANSPARENT);
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
			if (Utils.getDataentryScreenAisleId(getActivity()) != null) {
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
						addImageToAisleToServer(
								Long.valueOf(storedVueUser.getId()).toString(),
								Utils.getDataentryScreenAisleId(getActivity()),
								false, null);
						String categoery = mCategoryText.getText().toString()
								.trim();
						String lookingFor = mLookingForBigText.getText()
								.toString().trim();
						String occassion = null;
						if (!(mOccassionBigText.getText().toString().trim()
								.equals(OCCASION.trim()))) {
							occassion = mOccassionBigText.getText().toString()
									.trim();
						}
						String description = mSaySomethingAboutAisle.getText()
								.toString().trim();
						checkForAisleUpdate(storedVueUser, null, categoery,
								lookingFor, occassion, description);
					} else {
						Toast.makeText(getActivity(),
								getResources().getString(R.string.no_network),
								Toast.LENGTH_LONG).show();
					}
				} else {
					showLogInDialog(false);
				}
			} else {
				Toast.makeText(getActivity(),
						"This Aisle is not uploaded to server.",
						Toast.LENGTH_LONG).show();
			}
		}
	}

	private void addORUpdateAisle() {
		// Creating New Aisle...
		if (!Utils.getDataentryEditAisleFlag(getActivity())) {
			if (checkLimitForLoginDialog()) {
				if (mLoginWarningMessage == null) {
					mLoginWarningMessage = new LoginWarningMessage(
							getActivity());
				}
				mLoginWarningMessage.showLoginWarningMessageDialog(
						"You need to Login with the app to create aisle.",
						true, true, 0, null, null);
			} else {
				SharedPreferences sharedPreferencesObj = getActivity()
						.getSharedPreferences(
								VueConstants.SHAREDPREFERENCE_NAME, 0);
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
					SharedPreferences.Editor editor = sharedPreferencesObj
							.edit();
					editor.putInt(
							VueConstants.CREATED_AISLE_COUNT_IN_PREFERENCE,
							createdAisleCount + 1);
					editor.commit();
					VueUser storedVueUser = null;
					try {
						storedVueUser = Utils.readUserObjectFromFile(
								getActivity(),
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
							Toast.makeText(
									getActivity(),
									getResources().getString(
											R.string.no_network),
									Toast.LENGTH_LONG).show();
						}
					} else {
						showLogInDialog(false);
					}
				}
			}
		}
		// Updating Aisle...
		else {
			VueUser storedVueUser = null;
			try {
				storedVueUser = Utils.readUserObjectFromFile(getActivity(),
						VueConstants.VUE_APP_USEROBJECT__FILENAME);
			} catch (Exception e2) {
				e2.printStackTrace();
			}
			if (storedVueUser != null && storedVueUser.getId() != null) {
				if (VueConnectivityManager.isNetworkConnected(getActivity())) {
					storeMetaAisleDataIntoLocalStorage();
					String categoery = mCategoryText.getText().toString()
							.trim();
					String lookingFor = mLookingForBigText.getText().toString()
							.trim();
					String occassion = null;
					if (!(mOccassionBigText.getText().toString().trim()
							.equals(OCCASION.trim()))) {
						occassion = mOccassionBigText.getText().toString()
								.trim();
					}
					String description = mSaySomethingAboutAisle.getText()
							.toString().trim();
					upDateAisleToServer(storedVueUser, null, categoery,
							lookingFor, occassion, description);
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

	public void storeMetaAisleDataIntoLocalStorage() {
		saveAisleLookingForOccassionCategoryDataToDB();
		renderUIAfterAddingAisleToServer();
	}

	private void renderUIAfterAddingAisleToServer() {
		if (mDataEntryActivity == null) {
			mDataEntryActivity = (DataEntryActivity) getActivity();
		}
		mDataEntryActivity.mVueDataentryActionbarScreenName
				.setText(getResources().getString(R.string.app_name));
		mDataEntryActivity.mVueDataentryActionbarBottomLayout
				.setVisibility(View.VISIBLE);
		mDataEntryActivity.mVueDataentryActionbarTopLayout
				.setVisibility(View.GONE);
		try {
			if (Utils.getDataentryEditAisleFlag(getActivity())) {
				ArrayList<DataentryImage> mAisleImagePathList = null;
				try {
					mAisleImagePathList = Utils.readAisleImagePathListFromFile(
							getActivity(),
							VueConstants.AISLE_IMAGE_PATH_LIST_FILE_NAME);
					mAisleImagePathList.remove(mCurrentPagePosition);
					Utils.writeAisleImagePathListToFile(getActivity(),
							VueConstants.AISLE_IMAGE_PATH_LIST_FILE_NAME,
							mAisleImagePathList);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e1) {

		}
		Utils.putDataentryEditAisleFlag(getActivity(), false);
		mMainHeadingRow.setVisibility(View.GONE);
		mTouchToChangeImage.setVisibility(View.GONE);
		mDataEntryBottomBottomLayout.setVisibility(View.GONE);
		mDataEntryBottomTopLayout.setVisibility(View.VISIBLE);
		DataentryImage datentryImage = new DataentryImage(mResizedImagePath,
				mFindAtText.getText().toString());
		ArrayList<DataentryImage> mAisleImagePathList = null;
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
			mAisleImagePathList.add(0, datentryImage);
			Utils.writeAisleImagePathListToFile(getActivity(),
					VueConstants.AISLE_IMAGE_PATH_LIST_FILE_NAME,
					mAisleImagePathList);
			mAisleImagePathList = Utils
					.readAisleImagePathListFromFile(getActivity(),
							VueConstants.AISLE_IMAGE_PATH_LIST_FILE_NAME);
		} catch (Exception e) {
			e.printStackTrace();
		}
		mDataEntryAislesViewpager.setVisibility(View.VISIBLE);
		mCreateAisleBg.setVisibility(View.GONE);
		try {
			mDataEntryAislesViewpager
					.setAdapter(new DataEntryAilsePagerAdapter(getActivity(),
							mAisleImagePathList));
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private void saveAisleLookingForOccassionCategoryDataToDB() {
		new Thread(new Runnable() {
			@Override
			public void run() {

				AisleData lookingForAisleDataObj = mDbManager
						.getAisleMetaDataForKeyword(mLookingForBigText
								.getText().toString().trim(),
								VueConstants.LOOKING_FOR_TABLE);
				if (lookingForAisleDataObj != null) {
					lookingForAisleDataObj.count += 1;
					lookingForAisleDataObj.isNew = false;
				} else {
					lookingForAisleDataObj = new AisleData();
					lookingForAisleDataObj.keyword = mLookingForBigText
							.getText().toString().trim();
					lookingForAisleDataObj.count = 1;
					lookingForAisleDataObj.isNew = true;
				}
				String currentTime = Utils.date();
				lookingForAisleDataObj.time = currentTime;
				mDbManager.addAisleMetaDataToDB(VueConstants.LOOKING_FOR_TABLE,
						lookingForAisleDataObj);
				if (!(mOccassionBigText.getText().toString().trim()
						.equals(OCCASION.trim()))) {
					AisleData occassionAisleDataObj = mDbManager
							.getAisleMetaDataForKeyword(mOccassionBigText
									.getText().toString().trim(),
									VueConstants.OCCASION_TABLE);
					if (occassionAisleDataObj != null) {
						occassionAisleDataObj.count += 1;
						occassionAisleDataObj.isNew = false;
					} else {
						occassionAisleDataObj = new AisleData();
						occassionAisleDataObj.keyword = mOccassionBigText
								.getText().toString().trim();
						occassionAisleDataObj.count = 1;
						occassionAisleDataObj.isNew = true;
					}
					occassionAisleDataObj.time = currentTime;
					mDbManager.addAisleMetaDataToDB(
							VueConstants.OCCASION_TABLE, occassionAisleDataObj);
				}
				AisleData categoryAisleDataObj = mDbManager
						.getAisleMetaDataForKeyword(mCategoryText.getText()
								.toString().trim(), VueConstants.CATEGORY_TABLE);
				if (categoryAisleDataObj != null) {
					categoryAisleDataObj.count += 1;
					categoryAisleDataObj.isNew = false;
				} else {
					categoryAisleDataObj = new AisleData();
					categoryAisleDataObj.keyword = mCategoryText.getText()
							.toString().trim();
					categoryAisleDataObj.count = 1;
					categoryAisleDataObj.isNew = true;
				}
				categoryAisleDataObj.time = currentTime;
				mDbManager.addAisleMetaDataToDB(VueConstants.CATEGORY_TABLE,
						categoryAisleDataObj);
				// When the loop is finished, updates the notification
				// builder.setContentText("Uploading completed");
				// notifyManager.notify(0, builder.getNotification());
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
			} else {
				aisle.setId(Long.parseLong(Utils
						.getDataentryScreenAisleId(getActivity())));
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
		if ((mOtherSourceSelectedImageUrl != null && mOtherSourceSelectedImageUrl
				.trim().length() > 0) || mImagePath != null) {
			String categoery = mCategoryText.getText().toString().trim();
			String lookingFor = mLookingForBigText.getText().toString().trim();
			String occassion = null;
			if (!(mOccassionBigText.getText().toString().trim().equals(OCCASION
					.trim()))) {
				occassion = mOccassionBigText.getText().toString().trim();
			}
			String description = mSaySomethingAboutAisle.getText().toString()
					.trim();
			String findAt = mFindAtText.getText().toString();
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
			final VueImage image = new VueImage();
			image.setDetailsUrl(findAt);
			image.setHeight(mOtherSourceImageOriginalHeight);
			image.setWidth(mOtherSourceImageOriginalWidth);
			image.setStore(mOtherSourceSelectedImageStore);
			image.setTitle("Android Test"); // TODO By Krishna
			FlurryAgent.logEvent("New_Aisle_Creation");
			image.setOwnerUserId(Long.valueOf(vueUser.getId()));
			FlurryAgent.logEvent("Create_Aisle");
			// Camera or Gallery...
			if (mOtherSourceSelectedImageUrl == null) {
				VueTrendingAislesDataModel
						.getInstance(VueApplication.getInstance())
						.getNetworkHandler()
						.requestForUploadImage(new File(mImagePath),
								new ImageUploadCallback() {
									@Override
									public void onImageUploaded(String imageUrl) {
										if (imageUrl != null) {
											image.setImageUrl(imageUrl);
											aisle.setAisleImage(image);
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
																		String aisleId) {
																	Utils.putDataentryScreenAisleId(
																			getActivity(),
																			aisleId);
																}
															});
										}
									}
								});
			} else {
				image.setImageUrl(mOtherSourceSelectedImageUrl);
				aisle.setAisleImage(image);
				VueTrendingAislesDataModel
						.getInstance(VueApplication.getInstance())
						.getNetworkHandler()
						.requestCreateAisle(aisle,
								new AisleManager.AisleUpdateCallback() {
									@Override
									public void onAisleUpdated(String aisleId) {
										Utils.putDataentryScreenAisleId(
												getActivity(), aisleId);
									}
								});
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

	public void addImageToAisleToServer(String ownerUserId,
			String ownerAisleId, final boolean fromDetailsScreenFlag,
			final String imageId) {
		if ((mOtherSourceSelectedImageUrl != null && mOtherSourceSelectedImageUrl
				.trim().length() > 0) || mImagePath != null) {

			final VueImage image = new VueImage();
			String detailsUrl = mFindAtText.getText().toString();
			image.setDetailsUrl(detailsUrl);
			image.setHeight(mOtherSourceImageOriginalHeight);
			image.setWidth(mOtherSourceImageOriginalWidth);
			image.setStore(mOtherSourceSelectedImageStore);
			image.setTitle("Android Test"); // TODO By Krishna
			image.setOwnerUserId(Long.valueOf(ownerUserId));
			image.setOwnerAisleId(Long.valueOf(ownerAisleId));
			// Camera or Gallery...
			if (mOtherSourceSelectedImageUrl == null) {
				VueTrendingAislesDataModel
						.getInstance(VueApplication.getInstance())
						.getNetworkHandler()
						.requestForUploadImage(new File(mImagePath),
								new ImageUploadCallback() {
									@Override
									public void onImageUploaded(String imageUrl) {
										if (imageUrl != null) {
											image.setImageUrl(imageUrl);
											VueTrendingAislesDataModel
													.getInstance(
															VueApplication
																	.getInstance())
													.getNetworkHandler()
													.requestForAddImage(
															fromDetailsScreenFlag,
															imageId, image);
										}
									}
								});
			} else {
				image.setImageUrl(mOtherSourceSelectedImageUrl);
				VueTrendingAislesDataModel
						.getInstance(VueApplication.getInstance())
						.getNetworkHandler()
						.requestForAddImage(fromDetailsScreenFlag, imageId,
								image);
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
			Log.e("cs", "9");
			mAisleBgProgressbar.setVisibility(View.GONE);
			mCreateAisleBg.setVisibility(View.VISIBLE);
			if (mAisleImageBitmap != null) {
				RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
						(int) mAisleImageBitmap.getWidth(),
						(int) mAisleImageBitmap.getHeight());
				lp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
				lp.setMargins(8, 8, 8, 8);
				mTouchToChangeImage.setLayoutParams(lp);
				mCreateAisleBg.setImageBitmap(mAisleImageBitmap);
			} else {
				mCreateAisleBg.setImageDrawable(getResources().getDrawable(
						R.drawable.no_image));
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
			if (mResizedImagePath != null) {
				mAisleImageBitmap = BitmapFactory.decodeFile(mResizedImagePath);
			}
			return null;
		}
	}

	class mListener extends GestureDetector.SimpleOnGestureListener {
		@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}

		@Override
		public boolean onDoubleTap(MotionEvent e) {
			return super.onDoubleTap(e);
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent event) {
			if (mTouchToChangeImage.getVisibility() == View.VISIBLE) {
				touchToChangeImageClickFunctionality();
			}
			return true;
		}

		@Override
		public void onLongPress(MotionEvent e) {
			super.onLongPress(e);
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
		 sourceUrl = "http://www.wish.com/c/50f1b7b83b97ee7282ea8e9d";
		// /*"http://pages.ebay.com/link/?nav=item.view&id=251351111265";*/"http://www.amazon.com/dp/B00BI2BA7G/ref=cm_sw_r_an_am_ap_am_us?ie=UTF8";
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

	private void checkForAisleUpdate(final VueUser storedVueUser,
			String aisleId, final String categoery, final String lookingfor,
			final String occasion, final String description) {
		mIsEmptyAisle = false;
		if (aisleId == null) {
			mIsEmptyAisle = true;
			aisleId = Utils.getDataentryScreenAisleId(getActivity());
		}
		final String id = aisleId;
		new Thread(new Runnable() {
			@Override
			public void run() {
				boolean updateAisleFlag = false;
				if (id != null) {
					ArrayList<AisleWindowContent> aisle = DataBaseManager
							.getInstance(getActivity()).getAisleByAisleId(id);
					if (aisle != null && aisle.size() > 0
							&& aisle.get(0) != null
							&& aisle.get(0).getAisleContext() != null) {
						if (!lookingfor.trim().equals(
								aisle.get(0).getAisleContext().mLookingForItem)) {
							updateAisleFlag = true;
						}
						if (!occasion.trim().equals(
								aisle.get(0).getAisleContext().mOccasion)
								&& !occasion.trim().equals(OCCASION)) {
							updateAisleFlag = true;
						}
						if (!categoery.trim().equals(
								aisle.get(0).getAisleContext().mCategory)) {
							updateAisleFlag = true;
						}
						if (!description.trim().equals(
								aisle.get(0).getAisleContext().mDescription)) {
							updateAisleFlag = true;
						}
					}
				}
				if (updateAisleFlag) {
					String sourceAisleId = null;
					if (!mIsEmptyAisle) {
						sourceAisleId = id;
					}
					upDateAisleToServer(storedVueUser, sourceAisleId,
							categoery, lookingfor, occasion, description);
				}
			}
		}).start();
	}
}
