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
import android.graphics.Typeface;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import android.widget.TextView.OnEditorActionListener;
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
import com.lateralthoughts.vue.utils.*;

/**
 * Fragment for creating Aisle, Updating Ailse and AddingImageToAisle
 * 
 */
public class DataEntryFragment extends Fragment {
	private ListView mCategoryListview = null, mLookingForListview = null,
			mOccasionListview = null;
	public TextView mCategoryheading /* mFindAtheading *//* mDescriptionheading */;
	// public LinearLayout mSubmitlayout;
	public LinearLayout mLookingForListviewLayout = null,
			mOccasionPopup = null, mCategoryPopup = null, mFindAtPopUp,
			mCategoryListviewLayout = null, mOccasionListviewLayout = null;
	RelativeLayout mDataEntryRootLayout = null;

	public LinearLayout mLookingForPopup = null, mCategoryheadingLayout
	/* mFindatheadinglayout, *//* mDescriptionheadingLayout */;
	public TextView /* mLookingForBigText = null, mOccassionBigText = null */
	mCategoryText = null,/* mHintTextForSaySomeThing, *//* , mForTextView */
	mLookingForOccasionTextview;
	public EditTextBackEvent mLookingForText = null, mOccasionText = null,
			mSaySomethingAboutAisle = null, mFindAtText = null;
	private String mCategoryitemsArray[] = null;
	private int mCategoryitemsDrawablesArray[] = {
			R.drawable.vue_launcher_icon, R.drawable.vue_launcher_icon,
			R.drawable.vue_launcher_icon, R.drawable.vue_launcher_icon,
			R.drawable.vue_launcher_icon, R.drawable.vue_launcher_icon,
			R.drawable.vue_launcher_icon };
	// public ImageView mCategoryIcon = null;
	InputMethodManager mInputMethodManager;
	// private TextView mAddimagebtn, mSavebtn;
	/*
	 * private boolean mDontGoToNextLookingFor = false, mDontGoToNextForOccasion
	 * = false;
	 */
	// private String mPreviousLookingfor = null, mPreviousOcasion = null,
	// mPreviousSaySomething = null;
	public String mPreviousFindAtText = null;
	private String mImagePath = null, mResizedImagePath = null;
	LinearLayout mMainHeadingRow = null;
	RelativeLayout mDataEntryBottomTopLayout = null;
	private RelativeLayout mDataEntryInviteFriendsLayout = null;
	private RelativeLayout mDataEntryInviteFriendsFacebookLayout = null;
	// RelativeLayout mDataEntryBottomBottomLayout = null;
	private RelativeLayout mDataEntryInviteFriendsCancelLayout = null;
	private RelativeLayout mDataEntryInviteFriendsGoogleplusLayout = null;
	public RelativeLayout mDataEntryInviteFriendsPopupLayout = null;
	// private ImageView mFindAtIcon = null;
	public ShareDialog mShare = null;
	private float mScreenHeight = 0, mScreenWidth = 0;
	// LinearLayout mFindAtIconLayout = null;
	private ViewPager mDataEntryAislesViewpager = null;
	public static final int AISLE_IMAGE_MARGIN = 96;
	public static final String LOOKING_FOR = "Looking";
	public static final String OCCASION = "Occasion";
	public static final String CATEGORY = "Category";
	public static final String FINDAT = "findat";
	public static final String SAY_SOMETHING_ABOUT_AISLE = "SaysomethingAboutAisle";
	public static boolean mSaySomethingAboutAisleClicked = false;
	private ArrayList<String> mLookingForAisleKeywordsList = null,
			mOccassionAisleKeywordsList = null,
			mCategoryAilseKeywordsList = null;
	private DataBaseManager mDbManager;
	// public RelativeLayout mSaySomeThingEditParent;
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
	private static final int CATEGORY_POPUP_DELAY = 2000;
	private boolean mIsEmptyAisle;
	ArrayList<DataentryImage> mAisleImagePathList = null;
	String mLookingFor = null;
	String mOccasion = null;
	LinearLayout mSelectCategoryLayout;

	// View mFindAtLeftLine, mFindAtRightLine, mFindAtBottomLine;

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
		mFindAtPopUp = (LinearLayout) mDataEntryFragmentView
				.findViewById(R.id.find_at_popup);
		/*
		 * mSavebtn = (TextView)
		 * mDataEntryFragmentView.findViewById(R.id.savebtn); mAddimagebtn =
		 * (TextView) mDataEntryFragmentView .findViewById(R.id.addimagebtn);
		 */
		/*
		 * mFindAtLeftLine = mDataEntryFragmentView
		 * .findViewById(R.id.findleftline); mFindAtRightLine =
		 * mDataEntryFragmentView .findViewById(R.id.findrightline);
		 */
		/*
		 * mSubmitlayout = (LinearLayout) mDataEntryFragmentView
		 * .findViewById(R.id.submitlayout);
		 */
		/*
		 * mFindAtBottomLine = mDataEntryFragmentView
		 * .findViewById(R.id.findbottomine);
		 */
		mCategoryheadingLayout = (LinearLayout) mDataEntryFragmentView
				.findViewById(R.id.categoryheading);
		/*
		 * mFindatheadinglayout = (LinearLayout) mDataEntryFragmentView
		 * .findViewById(R.id.findatheading);
		 */
		/*
		 * mDescriptionheadingLayout = (LinearLayout) mDataEntryFragmentView
		 * .findViewById(R.id.descriptionheading);
		 */
		mCategoryheading = (TextView) mDataEntryFragmentView
				.findViewById(R.id.category_heading_textview);
		/*
		 * mFindAtheading = (TextView) mDataEntryFragmentView
		 * .findViewById(R.id.findat_heading_textview); mDescriptionheading =
		 * (TextView) mDataEntryFragmentView
		 * .findViewById(R.id.description_heading_textview);
		 */
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
		mDataEntryRootLayout = (RelativeLayout) mDataEntryFragmentView
				.findViewById(R.id.dataentry_root_layout);
		mOccasionListview = (ListView) mDataEntryFragmentView
				.findViewById(R.id.ocassionlistview);
		/*
		 * mFindAtIcon = (ImageView) mDataEntryFragmentView
		 * .findViewById(R.id.find_at_icon);
		 */
		mLookingForOccasionTextview = (TextView) mDataEntryFragmentView
				.findViewById(R.id.looking_for_occasion_textview);
		mLookingForListview = (ListView) mDataEntryFragmentView
				.findViewById(R.id.lookingforlistview);
		/*
		 * mForTextView = (TextView) mDataEntryFragmentView
		 * .findViewById(R.id.for_text);
		 */
		mFindAtText = (EditTextBackEvent) mDataEntryFragmentView
				.findViewById(R.id.find_at_text);
		/*
		 * mFindAtIconLayout = (LinearLayout) mDataEntryFragmentView
		 * .findViewById(R.id.findaticonlayout);
		 */
		mDataEntryInviteFriendsCancelLayout = (RelativeLayout) mDataEntryFragmentView
				.findViewById(R.id.dataentry_invitefriends_cancellayout);
		/*
		 * mDataEntryBottomBottomLayout = (RelativeLayout)
		 * mDataEntryFragmentView
		 * .findViewById(R.id.dataentry_bottom_bottom_layout);
		 */
		mDataEntryInviteFriendsGoogleplusLayout = (RelativeLayout) mDataEntryFragmentView
				.findViewById(R.id.dataentry_invitefriends_googlepluslayout);
		/*
		 * mLookingForBigText = (TextView) mDataEntryFragmentView
		 * .findViewById(R.id.lookingforbigtext);
		 * mLookingForBigText.setBackgroundColor(getResources().getColor(
		 * R.color.yellowbgcolor));
		 */
		mDataEntryBottomTopLayout = (RelativeLayout) mDataEntryFragmentView
				.findViewById(R.id.dataentry_bottom_top_layout);
		mDataEntryInviteFriendsFacebookLayout = (RelativeLayout) mDataEntryFragmentView
				.findViewById(R.id.dataentry_invitefriends_facebooklayout);
		mDataEntryInviteFriendsPopupLayout = (RelativeLayout) mDataEntryFragmentView
				.findViewById(R.id.dataentry_invite_friends_popup_layout);
		/*
		 * mOccassionBigText = (TextView) mDataEntryFragmentView
		 * .findViewById(R.id.occassionbigtext);
		 */
		mOccasionPopup = (LinearLayout) mDataEntryFragmentView
				.findViewById(R.id.ocassionpopup);
		mOccasionText = (EditTextBackEvent) mDataEntryFragmentView
				.findViewById(R.id.occasiontext);
		mLookingForListviewLayout = (LinearLayout) mDataEntryFragmentView
				.findViewById(R.id.lookingforlistviewlayout);
		mLookingForPopup = (LinearLayout) mDataEntryFragmentView
				.findViewById(R.id.lookingforpopup);
		mSaySomethingAboutAisle = (EditTextBackEvent) mDataEntryFragmentView
				.findViewById(R.id.saysomethingaboutaisle);
		/*
		 * mCategoryIcon = (ImageView) mDataEntryFragmentView
		 * .findViewById(R.id.categoeryicon);
		 */
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
		mDataEntryInviteFriendsLayout = (RelativeLayout) mDataEntryFragmentView
				.findViewById(R.id.dataentry_invite_friends_layout);
		/*
		 * mHintTextForSaySomeThing = (TextView) mDataEntryFragmentView
		 * .findViewById(R.id.hinttext);
		 */
		// mPreviousLookingfor = mLookingForText.getText().toString();
		// mPreviousOcasion = mOccasionText.getText().toString();
		// mPreviousSaySomething = mSaySomethingAboutAisle.getText().toString();
		mLookingForAisleKeywordsList = mDbManager
				.getAisleKeywords(VueConstants.LOOKING_FOR_TABLE);
		/*
		 * mSaySomeThingEditParent = (RelativeLayout) mDataEntryFragmentView
		 * .findViewById(R.id.sayeditparentlay);
		 */
		String savedLookingFor = Utils
				.getDataentryTopAddImageAisleLookingFor(getActivity());
		String savedCategory = Utils
				.getDataentryTopAddImageAisleCategory(getActivity());
		String savedOccasion = Utils
				.getDataentryTopAddImageAisleOccasion(getActivity());
		String savedDescription = Utils
				.getDataentryTopAddImageAisleDescription(getActivity());
		mLookingForText.requestFocus();
		mInputMethodManager.showSoftInput(mLookingForText, 0);
		if (mDataEntryActivity == null) {
			mDataEntryActivity = (DataEntryActivity) getActivity();
		}
		mDataEntryActivity.mDataentryActionbarMainLayout
				.setVisibility(View.GONE);
		mDataEntryActivity.mVueDataentryKeyboardLayout
				.setVisibility(View.VISIBLE);
		mDataEntryActivity.mVueDataentryKeyboardDone
				.setVisibility(View.VISIBLE);
		mDataEntryActivity.mVueDataentryKeyboardCancel.setVisibility(View.GONE);

		if (Utils.getDataentryTopAddImageAisleFlag(getActivity())
				&& savedLookingFor != null
				&& savedLookingFor.trim().length() > 0) {
			mLookingForText.setText(savedLookingFor);
			mLookingFor = savedLookingFor;
			// mLookingForBigText.setText(savedLookingFor);
			mLookingForPopup.setVisibility(View.VISIBLE);
			// mLookingForBigText.setBackgroundColor(Color.TRANSPARENT);
			// mLookingForListviewLayout.setVisibility(View.VISIBLE);
		} else if (mLookingForAisleKeywordsList != null) {
			if (Utils.getDataentryAddImageAisleFlag(getActivity())) {
				if (Utils.getDataentryScreenAisleId(getActivity()) != null) {
					try {
						mLookingForText
								.setText(VueTrendingAislesDataModel
										.getInstance(getActivity())
										.getAisleAt(
												Utils.getDataentryScreenAisleId(getActivity()))
										.getAisleContext().mLookingForItem);
						/*
						 * mLookingForBigText
						 * .setText(VueTrendingAislesDataModel
						 * .getInstance(getActivity()) .getAisleAt(
						 * Utils.getDataentryScreenAisleId(getActivity()))
						 * .getAisleContext().mLookingForItem);
						 */
						mLookingFor = VueTrendingAislesDataModel
								.getInstance(getActivity())
								.getAisleAt(
										Utils.getDataentryScreenAisleId(getActivity()))
								.getAisleContext().mLookingForItem;
					} catch (Exception e) {
						mLookingForText.setText(mLookingForAisleKeywordsList
								.get(0));
						/*
						 * mLookingForBigText.setText(mLookingForAisleKeywordsList
						 * .get(0));
						 */
						mLookingFor = mLookingForAisleKeywordsList.get(0);
					}
				} else {
					mLookingForText
							.setText(mLookingForAisleKeywordsList.get(0));
					/*
					 * mLookingForBigText.setText(mLookingForAisleKeywordsList
					 * .get(0));
					 */
					mLookingFor = mLookingForAisleKeywordsList.get(0);
				}
			} else {
				mLookingForText.setText(mLookingForAisleKeywordsList.get(0));
				// mLookingForBigText.setText(mLookingForAisleKeywordsList.get(0));
				mLookingFor = mLookingForAisleKeywordsList.get(0);
			}
			mLookingForPopup.setVisibility(View.VISIBLE);
			// mLookingForBigText.setBackgroundColor(Color.TRANSPARENT);
			// mLookingForListviewLayout.setVisibility(View.VISIBLE);
		} else {
			mLookingForListviewLayout.setVisibility(View.GONE);
			mLookingForText.requestFocus();
		}
		mOccassionAisleKeywordsList = mDbManager
				.getAisleKeywords(VueConstants.OCCASION_TABLE);
		if (Utils.getDataentryTopAddImageAisleFlag(getActivity())
				&& savedOccasion != null && savedOccasion.trim().length() > 0) {
			mOccasionText.setText(savedOccasion);
			mOccasion = savedOccasion;
			// mOccassionBigText.setText(savedOccasion);
		} else if (mOccassionAisleKeywordsList != null) {
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
							mOccasion = VueTrendingAislesDataModel
									.getInstance(getActivity())
									.getAisleAt(
											Utils.getDataentryScreenAisleId(getActivity()))
									.getAisleContext().mOccasion;
							/*
							 * mOccassionBigText
							 * .setText(VueTrendingAislesDataModel
							 * .getInstance(getActivity()) .getAisleAt(
							 * Utils.getDataentryScreenAisleId(getActivity()))
							 * .getAisleContext().mOccasion);
							 */
						} else {
							// mOccassionBigText.setText(OCCASION);
						}

					} catch (Exception e) {
						mOccasionText.setText(mOccassionAisleKeywordsList
								.get(0));
						mOccasion = mOccassionAisleKeywordsList.get(0);
						/*
						 * mOccassionBigText.setText(mOccassionAisleKeywordsList
						 * .get(0));
						 */
					}
				} else {
					mOccasionText.setText(mOccassionAisleKeywordsList.get(0));
					mOccasion = mOccassionAisleKeywordsList.get(0);
					/*
					 * mOccassionBigText.setText(mOccassionAisleKeywordsList
					 * .get(0));
					 */
				}
			} else {
				mOccasionText.setText(mOccassionAisleKeywordsList.get(0));
				mOccasion = mOccassionAisleKeywordsList.get(0);
				// mOccassionBigText.setText(mOccassionAisleKeywordsList.get(0));
			}
		} else {
			// mOccassionBigText.setText(OCCASION);
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
		mCategoryAilseKeywordsList = mDbManager
				.getAisleKeywords(VueConstants.CATEGORY_TABLE);
		if (Utils.getDataentryTopAddImageAisleFlag(getActivity())
				&& savedCategory != null && savedCategory.trim().length() > 0) {
			mCategoryText.setText(savedCategory);
			mCategoryheading.setText(savedCategory);
			mCategoryheadingLayout.setVisibility(View.VISIBLE);
		} else if (mCategoryAilseKeywordsList != null) {
			if (Utils.getDataentryAddImageAisleFlag(getActivity())) {
				if (Utils.getDataentryScreenAisleId(getActivity()) != null) {
					try {
						mCategoryText
								.setText(VueTrendingAislesDataModel
										.getInstance(getActivity())
										.getAisleAt(
												Utils.getDataentryScreenAisleId(getActivity()))
										.getAisleContext().mCategory);
						mCategoryheading
								.setText(VueTrendingAislesDataModel
										.getInstance(getActivity())
										.getAisleAt(
												Utils.getDataentryScreenAisleId(getActivity()))
										.getAisleContext().mCategory);
						mCategoryheadingLayout.setVisibility(View.VISIBLE);
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
		if (Utils.getDataentryTopAddImageAisleFlag(getActivity())
				&& savedDescription != null
				&& savedDescription.trim().length() > 0) {
			mSaySomethingAboutAisle.setText(savedDescription);
			// mDescriptionheading.setText(savedDescription);
			// mDescriptionheadingLayout.setVisibility(View.GONE);
			// mHintTextForSaySomeThing.setText(savedDescription);
		} else if (Utils.getDataentryAddImageAisleFlag(getActivity())) {
			if (Utils.getDataentryScreenAisleId(getActivity()) != null) {
				try {
					String description = VueTrendingAislesDataModel
							.getInstance(getActivity())
							.getAisleAt(
									Utils.getDataentryScreenAisleId(getActivity()))
							.getAisleContext().mDescription;
					if (description != null && description.trim().length() > 0) {
						mSaySomethingAboutAisle.setText(description);
						// mDescriptionheading.setText(description);
						// mDescriptionheadingLayout.setVisibility(View.GONE);
						// mHintTextForSaySomeThing.setText(description);
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
						/*
						 * mPreviousSaySomething = mSaySomethingAboutAisle
						 * .getText().toString();
						 */
						mInputMethodManager.hideSoftInputFromWindow(
								mSaySomethingAboutAisle.getWindowToken(), 0);
						mInputMethodManager.hideSoftInputFromWindow(
								mOccasionText.getWindowToken(), 0);
						mInputMethodManager.hideSoftInputFromWindow(
								mLookingForText.getWindowToken(), 0);
						if (mDataEntryActivity == null) {
							mDataEntryActivity = (DataEntryActivity) getActivity();
						}
						mDataEntryActivity.mDataentryActionbarMainLayout
								.setVisibility(View.VISIBLE);
						mDataEntryActivity.mVueDataentryKeyboardLayout
								.setVisibility(View.GONE);
						mDataEntryActivity.mVueDataentryKeyboardDone
								.setVisibility(View.GONE);
						mDataEntryActivity.mVueDataentryKeyboardCancel
								.setVisibility(View.GONE);
						String tempString = mSaySomethingAboutAisle.getText()
								.toString();
						if (tempString != null
								&& !tempString.equalsIgnoreCase("")) {
							// mDescriptionheading.setText(tempString);
							// mDescriptionheadingLayout.setVisibility(View.GONE);
							// mHintTextForSaySomeThing.setText(tempString);
						}
						// mSaySomeThingEditParent.setVisibility(View.VISIBLE);
						mSaySomethingAboutAisle.setVisibility(View.GONE);
						mSaysomethingClose.setVisibility(View.GONE);
						// mSubmitlayout.setVisibility(View.VISIBLE);
						return true;
					}
				});
		/*
		 * mSaySomeThingEditParent.setOnClickListener(new OnClickListener() {
		 * 
		 * @Override public void onClick(View v) {
		 * mSaySomeThingEditParent.setVisibility(View.GONE);
		 * mSaySomethingAboutAisle.setVisibility(View.VISIBLE);
		 * mSaysomethingClose.setVisibility(View.VISIBLE);
		 * mSaySomeThingEditParent.post(new Runnable() {
		 * 
		 * @Override public void run() { mSaySomethingAboutAisle.requestFocus();
		 * mSaySomethingAboutAisle.setFocusable(true);
		 * mSaySomethingAboutAisle.setCursorVisible(true);
		 * mSaySomethingAboutAisle
		 * .setSelection(mSaySomethingAboutAisle.getText()
		 * .toString().length()); } }); mSaySomethingAboutAisleClicked = true;
		 * mInputMethodManager.hideSoftInputFromWindow(
		 * mOccasionText.getWindowToken(), 0);
		 * mInputMethodManager.hideSoftInputFromWindow(
		 * mLookingForText.getWindowToken(), 0);
		 * mInputMethodManager.hideSoftInputFromWindow(
		 * mFindAtText.getWindowToken(), 0);
		 * mLookingForPopup.setVisibility(View.GONE);
		 * mLookingForListviewLayout.setVisibility(View.GONE);
		 * mOccasionPopup.setVisibility(View.GONE);
		 * mOccasionListviewLayout.setVisibility(View.GONE);
		 * mCategoryPopup.setVisibility(View.GONE);
		 * mFindatClose.setVisibility(View.GONE);
		 * mFindAtIconLayout.setVisibility(View.GONE);
		 * mFindAtText.setVisibility(View.GONE);
		 * mFindAtLeftLine.setVisibility(View.GONE);
		 * mFindAtRightLine.setVisibility(View.GONE);
		 * mFindAtBottomLine.setVisibility(View.GONE);
		 * mCategoryListviewLayout.setVisibility(View.GONE);
		 * mSelectCategoryLayout.setVisibility(View.GONE); //
		 * mOccassionBigText.setBackgroundColor(Color.TRANSPARENT); //
		 * mLookingForBigText.setBackgroundColor(Color.TRANSPARENT); final
		 * InputMethodManager inputMethodManager = (InputMethodManager)
		 * getActivity() .getSystemService(Context.INPUT_METHOD_SERVICE);
		 * inputMethodManager.toggleSoftInputFromWindow(
		 * mSaySomethingAboutAisle.getApplicationWindowToken(),
		 * InputMethodManager.SHOW_FORCED, 0);
		 * 
		 * } });
		 */
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
						/*
						 * mLookingForBigText
						 * .setBackgroundColor(Color.TRANSPARENT); if
						 * (mLookingForText.getText().toString().trim()
						 * .length() > 0) {
						 * mLookingForBigText.setText(mLookingForText
						 * .getText().toString()); } else {
						 * mLookingForBigText.setText(LOOKING_FOR); }
						 * mPreviousLookingfor = mLookingForText.getText()
						 * .toString();
						 */
						if (mLookingForText.getText().toString().trim()
								.length() > 0) {
							mLookingFor = mLookingForText.getText().toString();
							mLookingForPopup.setVisibility(View.GONE);
							mLookingForListviewLayout.setVisibility(View.GONE);
							mInputMethodManager.hideSoftInputFromWindow(
									mLookingForText.getWindowToken(), 0);
							// if (!mDontGoToNextLookingFor) {
							/*
							 * mOccassionBigText.setBackgroundColor(getResources(
							 * ) .getColor(R.color.yellowbgcolor));
							 */
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
							if (mDataEntryActivity == null) {
								mDataEntryActivity = (DataEntryActivity) getActivity();
							}
							mDataEntryActivity.mDataentryActionbarMainLayout
									.setVisibility(View.GONE);
							mDataEntryActivity.mVueDataentryKeyboardLayout
									.setVisibility(View.VISIBLE);
							mDataEntryActivity.mVueDataentryKeyboardDone
									.setVisibility(View.VISIBLE);
							mDataEntryActivity.mVueDataentryKeyboardCancel
									.setVisibility(View.VISIBLE);
							// }
							mMainHeadingRow.setVisibility(View.VISIBLE);
							if (mOccasion != null) {
								mLookingForOccasionTextview.setText(mLookingFor
										+ " for " + mOccasion);
							} else {
								mLookingForOccasionTextview
										.setText("Looking for " + mLookingFor);
							}
						} else {
							Toast.makeText(getActivity(),
									"LookingFor is mandotory.",
									Toast.LENGTH_LONG).show();
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
				if (mDataEntryActivity == null) {
					mDataEntryActivity = (DataEntryActivity) getActivity();
				}
				mDataEntryActivity.mDataentryActionbarMainLayout
						.setVisibility(View.VISIBLE);
				mDataEntryActivity.mVueDataentryKeyboardLayout
						.setVisibility(View.GONE);
				mDataEntryActivity.mVueDataentryKeyboardDone
						.setVisibility(View.GONE);
				mDataEntryActivity.mVueDataentryKeyboardCancel
						.setVisibility(View.GONE);
				// mOccassionBigText.setBackgroundColor(Color.TRANSPARENT);

				if (mOccasionText.getText().toString().trim().length() > 0) {
					/*
					 * mOccassionBigText.setText(mOccasionText.getText()
					 * .toString());
					 */
					mOccasion = mOccasionText.getText().toString();
					mLookingForOccasionTextview.setText(mLookingFor + " for "
							+ mOccasion);
				} else {
					// mOccassionBigText.setText(OCCASION);
				}
				// mPreviousOcasion = mOccasionText.getText().toString();
				mOccasionPopup.setVisibility(View.GONE);
				mOccasionListviewLayout.setVisibility(View.GONE);
				// if (!mDontGoToNextForOccasion) {
				categoryIconClickFunctionality();
				// }

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
		/*
		 * mLookingForBigText.setOnClickListener(new OnClickListener() {
		 * 
		 * @Override public void onClick(View arg0) {
		 * hideAllEditableTextboxes();
		 * mOccassionBigText.setBackgroundColor(Color.TRANSPARENT);
		 * mOccasionPopup.setVisibility(View.GONE);
		 * mOccasionListviewLayout.setVisibility(View.GONE);
		 * mInputMethodManager.hideSoftInputFromWindow(
		 * mOccasionText.getWindowToken(), 0);
		 * mInputMethodManager.hideSoftInputFromWindow(
		 * mSaySomethingAboutAisle.getWindowToken(), 0);
		 * mInputMethodManager.hideSoftInputFromWindow(
		 * mFindAtText.getWindowToken(), 0);
		 * mFindatClose.setVisibility(View.GONE);
		 * mFindAtIconLayout.setVisibility(View.GONE);
		 * mFindAtText.setVisibility(View.GONE);
		 * mCategoryListview.setVisibility(View.GONE);
		 * mCategoryListviewLayout.setVisibility(View.GONE);
		 * mCategoryPopup.setVisibility(View.GONE); if
		 * (Utils.getDataentryAddImageAisleFlag(getActivity()) ||
		 * Utils.getDataentryEditAisleFlag(getActivity())) {
		 * showAlertForEditPermission(LOOKING_FOR); } else {
		 * lookingForTextClickFunctionality(); } } });
		 * mOccassionBigText.setOnClickListener(new OnClickListener() {
		 * 
		 * @Override public void onClick(View arg0) {
		 * hideAllEditableTextboxes();
		 * mLookingForPopup.setVisibility(View.GONE);
		 * mLookingForListviewLayout.setVisibility(View.GONE);
		 * mLookingForBigText.setBackgroundColor(Color.TRANSPARENT);
		 * mInputMethodManager.hideSoftInputFromWindow(
		 * mLookingForText.getWindowToken(), 0);
		 * mInputMethodManager.hideSoftInputFromWindow(
		 * mFindAtText.getWindowToken(), 0);
		 * mInputMethodManager.hideSoftInputFromWindow(
		 * mSaySomethingAboutAisle.getWindowToken(), 0);
		 * mFindatClose.setVisibility(View.GONE);
		 * mFindAtIconLayout.setVisibility(View.GONE);
		 * mFindAtText.setVisibility(View.GONE);
		 * mCategoryListview.setVisibility(View.GONE);
		 * mCategoryListviewLayout.setVisibility(View.GONE);
		 * mCategoryPopup.setVisibility(View.GONE); if
		 * (Utils.getDataentryAddImageAisleFlag(getActivity()) ||
		 * Utils.getDataentryEditAisleFlag(getActivity())) {
		 * showAlertForEditPermission(OCCASION); } else {
		 * occassionTextClickFunctionality(); } } });
		 * mForTextView.setOnClickListener(new OnClickListener() {
		 * 
		 * @Override public void onClick(View arg0) {
		 * hideAllEditableTextboxes();
		 * mLookingForPopup.setVisibility(View.GONE);
		 * mLookingForListviewLayout.setVisibility(View.GONE);
		 * mLookingForBigText.setBackgroundColor(Color.TRANSPARENT);
		 * mInputMethodManager.hideSoftInputFromWindow(
		 * mLookingForText.getWindowToken(), 0);
		 * mInputMethodManager.hideSoftInputFromWindow(
		 * mFindAtText.getWindowToken(), 0);
		 * mInputMethodManager.hideSoftInputFromWindow(
		 * mSaySomethingAboutAisle.getWindowToken(), 0);
		 * mFindatClose.setVisibility(View.GONE);
		 * mFindAtIconLayout.setVisibility(View.GONE);
		 * mFindAtText.setVisibility(View.GONE);
		 * mCategoryListview.setVisibility(View.GONE);
		 * mCategoryListviewLayout.setVisibility(View.GONE);
		 * mCategoryPopup.setVisibility(View.GONE); if
		 * (Utils.getDataentryAddImageAisleFlag(getActivity()) ||
		 * Utils.getDataentryEditAisleFlag(getActivity())) {
		 * showAlertForEditPermission(OCCASION); } else {
		 * occassionTextClickFunctionality(); } } });
		 */
		/*
		 * mCategoryIcon.setOnClickListener(new OnClickListener() {
		 * 
		 * @Override public void onClick(View v) {
		 * mLookingForPopup.setVisibility(View.GONE);
		 * mLookingForListviewLayout.setVisibility(View.GONE);
		 * mOccasionPopup.setVisibility(View.GONE);
		 * mOccasionListviewLayout.setVisibility(View.GONE);
		 * mOccassionBigText.setBackgroundColor(Color.TRANSPARENT);
		 * mLookingForBigText.setBackgroundColor(Color.TRANSPARENT);
		 * mInputMethodManager.hideSoftInputFromWindow(
		 * mOccasionText.getWindowToken(), 0);
		 * mInputMethodManager.hideSoftInputFromWindow(
		 * mLookingForText.getWindowToken(), 0);
		 * mInputMethodManager.hideSoftInputFromWindow(
		 * mFindAtText.getWindowToken(), 0);
		 * mInputMethodManager.hideSoftInputFromWindow(
		 * mSaySomethingAboutAisle.getWindowToken(), 0);
		 * mFindatClose.setVisibility(View.GONE);
		 * mFindAtIconLayout.setVisibility(View.GONE);
		 * mFindAtText.setVisibility(View.GONE); if
		 * (Utils.getDataentryAddImageAisleFlag(getActivity()) ||
		 * Utils.getDataentryEditAisleFlag(getActivity())) {
		 * showAlertForEditPermission(CATEGORY); } else {
		 * categoryIconClickFunctionality(); } } });
		 */
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
											.getResizedImagePath());
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
		/*
		 * mFindAtIcon.setOnClickListener(new OnClickListener() {
		 * 
		 * @Override public void onClick(View v) {
		 * mFindatClose.setVisibility(View.VISIBLE);
		 * mFindAtIconLayout.setVisibility(View.VISIBLE);
		 * mFindAtText.setVisibility(View.VISIBLE);
		 * mFindAtLeftLine.setVisibility(View.VISIBLE);
		 * mFindAtRightLine.setVisibility(View.VISIBLE);
		 * mFindAtBottomLine.setVisibility(View.VISIBLE);
		 * mLookingForPopup.setVisibility(View.GONE);
		 * mLookingForListviewLayout.setVisibility(View.GONE);
		 * mOccasionPopup.setVisibility(View.GONE);
		 * mOccasionListviewLayout.setVisibility(View.GONE);
		 * mCategoryPopup.setVisibility(View.GONE);
		 * mCategoryListviewLayout.setVisibility(View.GONE);
		 * mSelectCategoryLayout.setVisibility(View.GONE); //
		 * mOccassionBigText.setBackgroundColor(Color.TRANSPARENT); //
		 * mLookingForBigText.setBackgroundColor(Color.TRANSPARENT);
		 * mInputMethodManager.hideSoftInputFromWindow(
		 * mOccasionText.getWindowToken(), 0);
		 * mInputMethodManager.hideSoftInputFromWindow(
		 * mLookingForText.getWindowToken(), 0);
		 * mInputMethodManager.hideSoftInputFromWindow(
		 * mSaySomethingAboutAisle.getWindowToken(), 0);
		 * mFindAtText.requestFocus();
		 * mFindAtText.setSelection(mFindAtText.getText().toString() .length());
		 * mInputMethodManager.showSoftInput(mFindAtText, 0); } });
		 */
		mFindAtText.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView arg0, int actionId,
					KeyEvent arg2) {
				mInputMethodManager.hideSoftInputFromWindow(
						mFindAtText.getWindowToken(), 0);
				mPreviousFindAtText = mFindAtText.getText().toString();
				mOtherSourceSelectedImageDetailsUrl = mPreviousFindAtText;
				mFindatClose.setVisibility(View.GONE);
				// mFindAtIconLayout.setVisibility(View.GONE);
				mFindAtPopUp.setVisibility(View.GONE);
				// mFindAtLeftLine.setVisibility(View.GONE);
				// mFindAtRightLine.setVisibility(View.GONE);
				// mFindAtBottomLine.setVisibility(View.GONE);
				// mSaySomeThingEditParent.setVisibility(View.GONE);
				mSaySomethingAboutAisle.setVisibility(View.VISIBLE);
				mSaysomethingClose.setVisibility(View.VISIBLE);
				/*
				 * mSaySomeThingEditParent.post(new Runnable() {
				 * 
				 * @Override public void run() {
				 * mSaySomethingAboutAisle.requestFocus();
				 * mSaySomethingAboutAisle.setFocusable(true);
				 * mSaySomethingAboutAisle.setCursorVisible(true);
				 * mSaySomethingAboutAisle
				 * .setSelection(mSaySomethingAboutAisle.getText()
				 * .toString().length()); } });
				 */
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
				// mFindAtIconLayout.setVisibility(View.GONE);
				mFindAtPopUp.setVisibility(View.GONE);
				// mFindAtLeftLine.setVisibility(View.GONE);
				// mFindAtRightLine.setVisibility(View.GONE);
				// mFindAtBottomLine.setVisibility(View.GONE);
				mCategoryListviewLayout.setVisibility(View.GONE);
				mSelectCategoryLayout.setVisibility(View.GONE);
				// mOccassionBigText.setBackgroundColor(Color.TRANSPARENT);
				// mLookingForBigText.setBackgroundColor(Color.TRANSPARENT);
				final InputMethodManager inputMethodManager = (InputMethodManager) getActivity()
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				inputMethodManager.toggleSoftInputFromWindow(
						mSaySomethingAboutAisle.getApplicationWindowToken(),
						InputMethodManager.SHOW_FORCED, 0);
				mSaySomethingAboutAisle.requestFocus();
				mInputMethodManager.showSoftInput(mSaySomethingAboutAisle, 0);
				if (mDataEntryActivity == null) {
					mDataEntryActivity = (DataEntryActivity) getActivity();
				}
				mDataEntryActivity.mDataentryActionbarMainLayout
						.setVisibility(View.GONE);
				mDataEntryActivity.mVueDataentryKeyboardLayout
						.setVisibility(View.VISIBLE);
				mDataEntryActivity.mVueDataentryKeyboardDone
						.setVisibility(View.VISIBLE);
				mDataEntryActivity.mVueDataentryKeyboardCancel
						.setVisibility(View.VISIBLE);
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
					if (tempLookingForKeywordsList.size() > 0) {
						mLookingForListviewLayout.setVisibility(View.VISIBLE);
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
						mDataEntryInviteFriendsPopupLayout
								.setVisibility(View.GONE);
						if (mAisleImagePathList != null
								&& mAisleImagePathList.size() > 0) {
							mFindAtText.setText(mAisleImagePathList.get(
									position).getDetailsUrl());
							/*
							 * mFindAtheading.setText(mAisleImagePathList.get(
							 * position).getDetailsUrl());
							 * mFindatheadinglayout.setVisibility(View.GONE);
							 */
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
				// mHintTextForSaySomeThing.setText("");
			}
		});
		/*
		 * mAddimagebtn.setOnClickListener(new OnClickListener() {
		 * 
		 * @Override public void onClick(View arg0) {
		 * addImageToAisleButtonClickFunctionality(true); } });
		 * mSavebtn.setOnClickListener(new OnClickListener() {
		 * 
		 * @Override public void onClick(View arg0) {
		 * createAisleClickFunctionality(); } });
		 */
		addImageToViewPager(true);
		return mDataEntryFragmentView;
	}

	public void hideAllEditableTextboxes() {
		/*
		 * if (mOccasionText.getText().toString().trim().length() > 0) {
		 * mOccassionBigText.setText(mOccasionText.getText().toString()); } else
		 * { mOccassionBigText.setText(OCCASION); } if
		 * (mLookingForText.getText().toString().trim().length() > 0) {
		 * mLookingForBigText.setText(mLookingForText.getText().toString()); }
		 * else { mLookingForBigText.setText(LOOKING_FOR); }
		 */
		mSaySomethingAboutAisle.setCursorVisible(false);
		// mPreviousSaySomething = mSaySomethingAboutAisle.getText().toString();
		String tempString = mSaySomethingAboutAisle.getText().toString();
		if (tempString != null && !tempString.equalsIgnoreCase("")) {
			// mDescriptionheading.setText(tempString);
			// mDescriptionheadingLayout.setVisibility(View.GONE);
			// mHintTextForSaySomeThing.setText(tempString);
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
		if (mDataEntryActivity == null) {
			mDataEntryActivity = (DataEntryActivity) getActivity();
		}
		mDataEntryActivity.mDataentryActionbarMainLayout
				.setVisibility(View.VISIBLE);
		mDataEntryActivity.mVueDataentryKeyboardLayout.setVisibility(View.GONE);
		mDataEntryActivity.mVueDataentryKeyboardDone.setVisibility(View.GONE);
		mDataEntryActivity.mVueDataentryKeyboardCancel.setVisibility(View.GONE);
		// mLookingForPopup.setVisibility(View.GONE);
		// mLookingForListviewLayout.setVisibility(View.GONE);
		// mOccasionPopup.setVisibility(View.GONE);
		// mOccasionListviewLayout.setVisibility(View.GONE);
		// mCategoryPopup.setVisibility(View.GONE);
		// mFindatClose.setVisibility(View.GONE);
		// mFindAtIconLayout.setVisibility(View.GONE);
		// mFindAtText.setVisibility(View.GONE);
		// mCategoryListviewLayout.setVisibility(View.GONE);
		// mOccassionBigText.setBackgroundColor(Color.TRANSPARENT);
		// mLookingForBigText.setBackgroundColor(Color.TRANSPARENT);
		// mSaySomeThingEditParent.setVisibility(View.VISIBLE);

		mSaysomethingClose.setVisibility(View.GONE);
		mDataEntryInviteFriendsPopupLayout.setVisibility(View.GONE);
		if (mLookingForPopup.getVisibility() == View.VISIBLE) {
			if (mLookingForText.getText().toString().trim().length() > 0) {
				mLookingFor = mLookingForText.getText().toString();
				mLookingForPopup.setVisibility(View.GONE);
				mLookingForListviewLayout.setVisibility(View.GONE);
				mInputMethodManager.hideSoftInputFromWindow(
						mLookingForText.getWindowToken(), 0);
				// if (!mDontGoToNextLookingFor) {
				/*
				 * mOccassionBigText.setBackgroundColor(getResources( )
				 * .getColor(R.color.yellowbgcolor));
				 */
				mOccasionPopup.setVisibility(View.VISIBLE);
				if (mOccassionAisleKeywordsList != null
						&& mOccassionAisleKeywordsList.size() > 0) {
					mOccasionListviewLayout.setVisibility(View.VISIBLE);
					mOccasionListview.setAdapter(new OccassionAdapter(
							getActivity(), mOccassionAisleKeywordsList));
				}
				mOccasionText.requestFocus();
				mInputMethodManager.showSoftInput(mOccasionText, 0);
				if (mDataEntryActivity == null) {
					mDataEntryActivity = (DataEntryActivity) getActivity();
				}
				mDataEntryActivity.mDataentryActionbarMainLayout
						.setVisibility(View.GONE);
				mDataEntryActivity.mVueDataentryKeyboardLayout
						.setVisibility(View.VISIBLE);
				mDataEntryActivity.mVueDataentryKeyboardDone
						.setVisibility(View.VISIBLE);
				mDataEntryActivity.mVueDataentryKeyboardCancel
						.setVisibility(View.VISIBLE);
				// }
				mMainHeadingRow.setVisibility(View.VISIBLE);
				if (mOccasion != null) {
					mLookingForOccasionTextview.setText(mLookingFor + " for "
							+ mOccasion);
				} else {
					mLookingForOccasionTextview.setText("Looking for "
							+ mLookingFor);
				}
			} else {
				mLookingForText.requestFocus();
				mLookingForText.setSelection(mLookingForText.getText()
						.toString().length());
				mInputMethodManager.showSoftInput(mLookingForText, 0);
				if (mDataEntryActivity == null) {
					mDataEntryActivity = (DataEntryActivity) getActivity();
				}
				mDataEntryActivity.mDataentryActionbarMainLayout
						.setVisibility(View.GONE);
				mDataEntryActivity.mVueDataentryKeyboardLayout
						.setVisibility(View.VISIBLE);
				mDataEntryActivity.mVueDataentryKeyboardDone
						.setVisibility(View.VISIBLE);
				mDataEntryActivity.mVueDataentryKeyboardCancel
						.setVisibility(View.GONE);
				Toast.makeText(getActivity(), "LookingFor is mandotory.",
						Toast.LENGTH_LONG).show();
			}
		} else if (mOccasionPopup.getVisibility() == View.VISIBLE) {
			mInputMethodManager.hideSoftInputFromWindow(
					mOccasionText.getWindowToken(), 0);
			if (mDataEntryActivity == null) {
				mDataEntryActivity = (DataEntryActivity) getActivity();
			}
			mDataEntryActivity.mDataentryActionbarMainLayout
					.setVisibility(View.VISIBLE);
			mDataEntryActivity.mVueDataentryKeyboardLayout
					.setVisibility(View.GONE);
			mDataEntryActivity.mVueDataentryKeyboardDone
					.setVisibility(View.GONE);
			mDataEntryActivity.mVueDataentryKeyboardCancel
					.setVisibility(View.GONE);
			// mOccassionBigText.setBackgroundColor(Color.TRANSPARENT);

			if (mOccasionText.getText().toString().trim().length() > 0) {
				/*
				 * mOccassionBigText.setText(mOccasionText.getText()
				 * .toString());
				 */
				mOccasion = mOccasionText.getText().toString();
				mLookingForOccasionTextview.setText(mLookingFor + " for "
						+ mOccasion);
			} else {
				// mOccassionBigText.setText(OCCASION);
			}
			// mPreviousOcasion = mOccasionText.getText().toString();
			mOccasionPopup.setVisibility(View.GONE);
			mOccasionListviewLayout.setVisibility(View.GONE);
			// if (!mDontGoToNextForOccasion) {
			categoryIconClickFunctionality();
		} else if (mCategoryListviewLayout.getVisibility() == View.VISIBLE) {
			if (mFindAtText.getText().toString().trim().length() == 0) {
				mFindatClose.setVisibility(View.VISIBLE);
				// mFindAtIconLayout.setVisibility(View.VISIBLE);
				mFindAtPopUp.setVisibility(View.VISIBLE);
				// mFindAtLeftLine.setVisibility(View.VISIBLE);
				// mFindAtRightLine.setVisibility(View.VISIBLE);
				// mFindAtBottomLine.setVisibility(View.VISIBLE);
				mLookingForPopup.setVisibility(View.GONE);
				mLookingForListviewLayout.setVisibility(View.GONE);
				mOccasionPopup.setVisibility(View.GONE);
				mOccasionListviewLayout.setVisibility(View.GONE);
				mCategoryPopup.setVisibility(View.GONE);
				mCategoryListviewLayout.setVisibility(View.GONE);
				mSelectCategoryLayout.setVisibility(View.GONE);
				// mOccassionBigText.setBackgroundColor(Color.TRANSPARENT);
				// mLookingForBigText.setBackgroundColor(Color.TRANSPARENT);
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
				mDataEntryActivity.mDataentryActionbarMainLayout
						.setVisibility(View.GONE);
				mDataEntryActivity.mVueDataentryKeyboardLayout
						.setVisibility(View.VISIBLE);
				mDataEntryActivity.mVueDataentryKeyboardDone
						.setVisibility(View.VISIBLE);
				mDataEntryActivity.mVueDataentryKeyboardCancel
						.setVisibility(View.VISIBLE);
			} else {
				// mSaySomeThingEditParent.setVisibility(View.GONE);
				mSaySomethingAboutAisle.setVisibility(View.VISIBLE);
				mSaysomethingClose.setVisibility(View.VISIBLE);
				/*
				 * mSaySomeThingEditParent.post(new Runnable() {
				 * 
				 * @Override public void run() {
				 * mSaySomethingAboutAisle.requestFocus();
				 * mSaySomethingAboutAisle.setFocusable(true);
				 * mSaySomethingAboutAisle.setCursorVisible(true);
				 * mSaySomethingAboutAisle
				 * .setSelection(mSaySomethingAboutAisle.getText()
				 * .toString().length()); } });
				 */
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
				// mFindAtIconLayout.setVisibility(View.GONE);
				mFindAtPopUp.setVisibility(View.GONE);
				mCategoryListviewLayout.setVisibility(View.GONE);
				mSelectCategoryLayout.setVisibility(View.GONE);
				// mOccassionBigText.setBackgroundColor(Color.TRANSPARENT);
				// mLookingForBigText.setBackgroundColor(Color.TRANSPARENT);
				final InputMethodManager inputMethodManager = (InputMethodManager) getActivity()
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				inputMethodManager.toggleSoftInputFromWindow(
						mSaySomethingAboutAisle.getApplicationWindowToken(),
						InputMethodManager.SHOW_FORCED, 0);
				if (mDataEntryActivity == null) {
					mDataEntryActivity = (DataEntryActivity) getActivity();
				}
				mDataEntryActivity.mDataentryActionbarMainLayout
						.setVisibility(View.GONE);
				mDataEntryActivity.mVueDataentryKeyboardLayout
						.setVisibility(View.VISIBLE);
				mDataEntryActivity.mVueDataentryKeyboardDone
						.setVisibility(View.VISIBLE);
				mDataEntryActivity.mVueDataentryKeyboardCancel
						.setVisibility(View.VISIBLE);
			}
		} else if (mFindAtPopUp.getVisibility() == View.VISIBLE) {
			// mSaySomeThingEditParent.setVisibility(View.GONE);
			mSaySomethingAboutAisle.setVisibility(View.VISIBLE);
			mSaysomethingClose.setVisibility(View.VISIBLE);
			/*
			 * mSaySomeThingEditParent.post(new Runnable() {
			 * 
			 * @Override public void run() {
			 * mSaySomethingAboutAisle.requestFocus();
			 * mSaySomethingAboutAisle.setFocusable(true);
			 * mSaySomethingAboutAisle.setCursorVisible(true);
			 * mSaySomethingAboutAisle
			 * .setSelection(mSaySomethingAboutAisle.getText()
			 * .toString().length()); } });
			 */
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
			// mFindAtIconLayout.setVisibility(View.GONE);
			mFindAtPopUp.setVisibility(View.GONE);
			// mFindAtLeftLine.setVisibility(View.GONE);
			// mFindAtRightLine.setVisibility(View.GONE);
			// mFindAtBottomLine.setVisibility(View.GONE);
			mCategoryListviewLayout.setVisibility(View.GONE);
			mSelectCategoryLayout.setVisibility(View.GONE);
			// mOccassionBigText.setBackgroundColor(Color.TRANSPARENT);
			// mLookingForBigText.setBackgroundColor(Color.TRANSPARENT);
			final InputMethodManager inputMethodManager = (InputMethodManager) getActivity()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			inputMethodManager.toggleSoftInputFromWindow(
					mSaySomethingAboutAisle.getApplicationWindowToken(),
					InputMethodManager.SHOW_FORCED, 0);
			if (mDataEntryActivity == null) {
				mDataEntryActivity = (DataEntryActivity) getActivity();
			}
			mDataEntryActivity.mDataentryActionbarMainLayout
					.setVisibility(View.GONE);
			mDataEntryActivity.mVueDataentryKeyboardLayout
					.setVisibility(View.VISIBLE);
			mDataEntryActivity.mVueDataentryKeyboardDone
					.setVisibility(View.VISIBLE);
			mDataEntryActivity.mVueDataentryKeyboardCancel
					.setVisibility(View.VISIBLE);
		} else if (mSaySomethingAboutAisle.getVisibility() == View.VISIBLE) {
			mSaySomethingAboutAisle.setVisibility(View.GONE);
			// mSubmitlayout.setVisibility(View.VISIBLE);
		}
	}

	public void lookingForInterceptListnerFunctionality() {
		if (mLookingFor != null && mLookingFor.trim().length() > 0) {
			mLookingForPopup.setVisibility(View.GONE);
			mLookingForListviewLayout.setVisibility(View.GONE);
			// mOccasionPopup.setVisibility(View.GONE);
			// mOccasionListviewLayout.setVisibility(View.GONE);
			// mLookingForText.setText(mPreviousLookingfor);
			// mOccassionBigText.setBackgroundColor(Color.TRANSPARENT);
			// mLookingForBigText.setBackgroundColor(Color.TRANSPARENT);
			mInputMethodManager.hideSoftInputFromWindow(
					mSaySomethingAboutAisle.getWindowToken(), 0);
			mInputMethodManager.hideSoftInputFromWindow(
					mOccasionText.getWindowToken(), 0);
			mInputMethodManager.hideSoftInputFromWindow(
					mLookingForText.getWindowToken(), 0);
			mOccasionPopup.setVisibility(View.VISIBLE);
			if (mOccassionAisleKeywordsList != null
					&& mOccassionAisleKeywordsList.size() > 0) {
				mOccasionListviewLayout.setVisibility(View.VISIBLE);
				mOccasionListview.setAdapter(new OccassionAdapter(
						getActivity(), mOccassionAisleKeywordsList));
			}
			mOccasionText.requestFocus();
			mInputMethodManager.showSoftInput(mOccasionText, 0);
			if (mDataEntryActivity == null) {
				mDataEntryActivity = (DataEntryActivity) getActivity();
			}
			mDataEntryActivity.mDataentryActionbarMainLayout
					.setVisibility(View.GONE);
			mDataEntryActivity.mVueDataentryKeyboardLayout
					.setVisibility(View.VISIBLE);
			mDataEntryActivity.mVueDataentryKeyboardDone
					.setVisibility(View.VISIBLE);
			mDataEntryActivity.mVueDataentryKeyboardCancel
					.setVisibility(View.VISIBLE);
		} else {
			Toast.makeText(getActivity(), "Lookingfor is mandotory.",
					Toast.LENGTH_LONG).show();
			lookingForTextClickFunctionality();
		}
	}

	public void occasionInterceptListnerFunctionality() {
		mLookingForPopup.setVisibility(View.GONE);
		mLookingForListviewLayout.setVisibility(View.GONE);
		mOccasionPopup.setVisibility(View.GONE);
		mOccasionListviewLayout.setVisibility(View.GONE);
		// mOccasionText.setText(mPreviousOcasion);
		// mOccassionBigText.setBackgroundColor(Color.TRANSPARENT);
		// mLookingForBigText.setBackgroundColor(Color.TRANSPARENT);
		mInputMethodManager.hideSoftInputFromWindow(
				mSaySomethingAboutAisle.getWindowToken(), 0);
		mInputMethodManager.hideSoftInputFromWindow(
				mOccasionText.getWindowToken(), 0);
		mInputMethodManager.hideSoftInputFromWindow(
				mLookingForText.getWindowToken(), 0);
		if (mDataEntryActivity == null) {
			mDataEntryActivity = (DataEntryActivity) getActivity();
		}
		mDataEntryActivity.mDataentryActionbarMainLayout
				.setVisibility(View.VISIBLE);
		mDataEntryActivity.mVueDataentryKeyboardLayout.setVisibility(View.GONE);
		mDataEntryActivity.mVueDataentryKeyboardDone.setVisibility(View.GONE);
		mDataEntryActivity.mVueDataentryKeyboardCancel.setVisibility(View.GONE);
		categoryIconClickFunctionality();
	}

	public void findAtInterceptListnerFunctionality() {
		mFindAtText.setText(mPreviousFindAtText);
		mFindatClose.setVisibility(View.GONE);
		// mFindAtIconLayout.setVisibility(View.GONE);
		mFindAtPopUp.setVisibility(View.GONE);
		// mFindAtLeftLine.setVisibility(View.GONE);
		// mFindAtRightLine.setVisibility(View.GONE);
		// mFindAtBottomLine.setVisibility(View.GONE);
		mInputMethodManager.hideSoftInputFromWindow(
				mSaySomethingAboutAisle.getWindowToken(), 0);
		mInputMethodManager.hideSoftInputFromWindow(
				mOccasionText.getWindowToken(), 0);
		mInputMethodManager.hideSoftInputFromWindow(
				mLookingForText.getWindowToken(), 0);
		mInputMethodManager.hideSoftInputFromWindow(
				mFindAtText.getWindowToken(), 0);
		// mSaySomeThingEditParent.setVisibility(View.GONE);
		mSaySomethingAboutAisle.setVisibility(View.VISIBLE);
		mSaysomethingClose.setVisibility(View.VISIBLE);
		/*
		 * mSaySomeThingEditParent.post(new Runnable() {
		 * 
		 * @Override public void run() { mSaySomethingAboutAisle.requestFocus();
		 * mSaySomethingAboutAisle.setFocusable(true);
		 * mSaySomethingAboutAisle.setCursorVisible(true);
		 * mSaySomethingAboutAisle.setSelection(mSaySomethingAboutAisle
		 * .getText().toString().length()); } });
		 */
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
		// mFindAtIconLayout.setVisibility(View.GONE);
		mFindAtPopUp.setVisibility(View.GONE);
		// mFindAtLeftLine.setVisibility(View.GONE);
		// /mFindAtRightLine.setVisibility(View.GONE);
		// mFindAtBottomLine.setVisibility(View.GONE);
		mCategoryListviewLayout.setVisibility(View.GONE);
		mSelectCategoryLayout.setVisibility(View.GONE);
		// mOccassionBigText.setBackgroundColor(Color.TRANSPARENT);
		// mLookingForBigText.setBackgroundColor(Color.TRANSPARENT);
		final InputMethodManager inputMethodManager = (InputMethodManager) getActivity()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.toggleSoftInputFromWindow(
				mSaySomethingAboutAisle.getApplicationWindowToken(),
				InputMethodManager.SHOW_FORCED, 0);
		mSaySomethingAboutAisle.requestFocus();
		mInputMethodManager.showSoftInput(mSaySomethingAboutAisle, 0);
		if (mDataEntryActivity == null) {
			mDataEntryActivity = (DataEntryActivity) getActivity();
		}
		mDataEntryActivity.mDataentryActionbarMainLayout
				.setVisibility(View.GONE);
		mDataEntryActivity.mVueDataentryKeyboardLayout
				.setVisibility(View.VISIBLE);
		mDataEntryActivity.mVueDataentryKeyboardDone
				.setVisibility(View.VISIBLE);
		mDataEntryActivity.mVueDataentryKeyboardCancel
				.setVisibility(View.VISIBLE);
	}

	public void saySomethingABoutAisleInterceptListnerFunctionality() {
		mSaySomethingAboutAisleClicked = false;
		mSaySomethingAboutAisle.setCursorVisible(false);
		mInputMethodManager.hideSoftInputFromWindow(
				mSaySomethingAboutAisle.getWindowToken(), 0);
		// mSaySomethingAboutAisle.setText(mPreviousSaySomething);
		// mSaySomeThingEditParent.setVisibility(View.VISIBLE);
		mSaySomethingAboutAisle.setVisibility(View.GONE);
		mSaysomethingClose.setVisibility(View.GONE);
		// mSubmitlayout.setVisibility(View.VISIBLE);
		if (mDataEntryActivity == null) {
			mDataEntryActivity = (DataEntryActivity) getActivity();
		}
		mDataEntryActivity.mDataentryActionbarMainLayout
				.setVisibility(View.VISIBLE);
		mDataEntryActivity.mVueDataentryKeyboardLayout.setVisibility(View.GONE);
		mDataEntryActivity.mVueDataentryKeyboardDone.setVisibility(View.GONE);
		mDataEntryActivity.mVueDataentryKeyboardCancel.setVisibility(View.GONE);

	}

	public void createAisleClickFunctionality() {
		hideAllEditableTextboxes();
		if (mLookingFor != null/*
								 * !(mLookingForBigText.getText().toString().trim
								 * () .equals(LOOKING_FOR))
								 */
				&& (mCategoryText.getText().toString().trim().length() > 0)) {
			if (Utils.getDataentryEditAisleFlag(getActivity())
					|| Utils.getDataentryAddImageAisleFlag(getActivity())) {
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
						VueUser storedVueUser = null;
						try {
							storedVueUser = Utils.readUserObjectFromFile(
									getActivity(),
									VueConstants.VUE_APP_USEROBJECT__FILENAME);
						} catch (Exception e2) {
							e2.printStackTrace();
						}
						if (storedVueUser != null
								&& storedVueUser.getId() != null) {
							if (VueConnectivityManager
									.isNetworkConnected(getActivity())) {
								addImageToAisleToServer(storedVueUser,
										VueApplication.getInstance()
												.getClickedWindowID(), true);
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
				} else {
					if (mResizedImagePath != null) {
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
		mDataEntryAislesViewpager.setVisibility(View.VISIBLE);
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
		mDataEntryActivity.mVueDataentryActionbarSaveLayout
				.setVisibility(View.VISIBLE);
		// mDataEntryBottomBottomLayout.setVisibility(View.VISIBLE);
		mDataEntryBottomTopLayout.setVisibility(View.GONE);
		mMainHeadingRow.setVisibility(View.VISIBLE);
		mCategoryheadingLayout.setVisibility(View.VISIBLE);
		// mOccassionBigText.setBackgroundColor(Color.TRANSPARENT);
		// mLookingForBigText.setBackgroundColor(Color.TRANSPARENT);
		try {
			mDataEntryAislesViewpager
					.setAdapter(new DataEntryAilsePagerAdapter(getActivity(),
							mAisleImagePathList));
		} catch (Throwable e) {
			e.printStackTrace();
		}
		lookingForTextClickFunctionality();
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
			String lookingFor = "", aisleOwnerName = "", isUserAisleFlag = "0", aisleId = null;
			AisleWindowContent aisleWindowContent = null;
			try {
				if (Utils.getDataentryScreenAisleId(getActivity()) != null) {
					aisleId = Utils.getDataentryScreenAisleId(getActivity());
					Log.e("DataentryFragment",
							"share aisleid : "
									+ Utils.getDataentryScreenAisleId(getActivity()));
					aisleWindowContent = VueTrendingAislesDataModel
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
			for (int i = mAisleImagePathList.size() - 1; i >= 0; i--) {
				String imageId = null;
				if (aisleWindowContent != null
						&& aisleWindowContent.getImageList() != null
						&& aisleWindowContent.getImageList().size() > 0) {
					try {
						imageId = aisleWindowContent.getImageList().get(i).mId;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				clsShare shareObj = new clsShare(null, mAisleImagePathList.get(
						i).getResizedImagePath(), lookingFor, aisleOwnerName,
						isUserAisleFlag, aisleId, imageId);
				imageUrlList.add(shareObj);
			}
			if (mDataEntryAislesViewpager != null) {
				mShare.share(imageUrlList, "", "",
						mDataEntryAislesViewpager.getCurrentItem(), null,
						new ShareViaVueListner());
			} else {
				mShare.share(imageUrlList, "", "", 0, null,
						new ShareViaVueListner());
			}
		}
	}

	public void addImageToAisleButtonClickFunctionality(
			boolean topAddImageToAisleFlag) {
		mDataEntryInviteFriendsPopupLayout.setVisibility(View.GONE);
		if (topAddImageToAisleFlag) {
			Utils.putDataentryTopAddImageAisleFlag(getActivity(), true);
			Utils.putDataentryTopAddImageAisleLookingFor(getActivity(),
					mLookingFor);
			Utils.putDataentryTopAddImageAisleOccasion(getActivity(), mOccasion);
			Utils.putDataentryTopAddImageAisleCategory(getActivity(),
					mCategoryText.getText().toString());
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

	public void lookingForTextClickFunctionality() {
		// mDontGoToNextLookingFor = true;
		System.out.println("looking for text click functionality is called.");
		mLookingForPopup.setVisibility(View.VISIBLE);
		if (mLookingForAisleKeywordsList != null
				&& mLookingForAisleKeywordsList.size() > 0) {
			mLookingForListviewLayout.setVisibility(View.VISIBLE);
			mLookingForListview.setAdapter(new LookingForAdapter(getActivity(),
					mLookingForAisleKeywordsList));
		}
		/*
		 * mLookingForBigText.setBackgroundColor(getResources().getColor(
		 * R.color.yellowbgcolor));
		 */
		mLookingForText.post(new Runnable() {
			public void run() {
				System.out
						.println("looking for text click functionality is called.11");
				mLookingForText.setSelection(mLookingForText.getText()
						.toString().length());
				mLookingForText.setFocusable(true);
				mLookingForText.requestFocus();
				mInputMethodManager.showSoftInput(mLookingForText, 0);
				if (mDataEntryActivity == null) {
					mDataEntryActivity = (DataEntryActivity) getActivity();
				}
				mDataEntryActivity.mDataentryActionbarMainLayout
						.setVisibility(View.GONE);
				mDataEntryActivity.mVueDataentryKeyboardLayout
						.setVisibility(View.VISIBLE);
				mDataEntryActivity.mVueDataentryKeyboardDone
						.setVisibility(View.VISIBLE);
				mDataEntryActivity.mVueDataentryKeyboardCancel
						.setVisibility(View.GONE);
			}

		});

	}

	private void occassionTextClickFunctionality() {
		// mDontGoToNextForOccasion = true;
		mOccasionPopup.setVisibility(View.VISIBLE);
		if (mOccassionAisleKeywordsList != null
				&& mOccassionAisleKeywordsList.size() > 0) {
			mOccasionListviewLayout.setVisibility(View.VISIBLE);
			mOccasionListview.setAdapter(new OccassionAdapter(getActivity(),
					mOccassionAisleKeywordsList));
		}
		/*
		 * mOccassionBigText.setBackgroundColor(getResources().getColor(
		 * R.color.yellowbgcolor));
		 */
		mOccasionText.requestFocus();
		mOccasionText.setSelection(mOccasionText.getText().toString().length());
		mInputMethodManager.showSoftInput(mOccasionText, 0);
		if (mDataEntryActivity == null) {
			mDataEntryActivity = (DataEntryActivity) getActivity();
		}
		mDataEntryActivity.mDataentryActionbarMainLayout
				.setVisibility(View.GONE);
		mDataEntryActivity.mVueDataentryKeyboardLayout
				.setVisibility(View.VISIBLE);
		mDataEntryActivity.mVueDataentryKeyboardDone
				.setVisibility(View.VISIBLE);
		mDataEntryActivity.mVueDataentryKeyboardCancel
				.setVisibility(View.VISIBLE);
	}

	private void categoryIconClickFunctionality() {
		mCategoryListview.setVisibility(View.VISIBLE);
		mCategoryListview.setAdapter(new CategoryAdapter(getActivity()));
		mCategoryListviewLayout.setVisibility(View.VISIBLE);
		mSelectCategoryLayout.setVisibility(View.VISIBLE);
		if (mCategoryText.getText().toString().trim().length() > 0) {
			mCategoryPopup.setVisibility(View.GONE);
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
						/*
						 * mLookingForBigText.setText(lookingForKeywordsList
						 * .get(position));
						 */
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
							/*
							 * mOccassionBigText.setText(mOccasionText.getText()
							 * .toString());
							 */
							mOccasion = mOccasionText.getText().toString();
							mLookingForOccasionTextview.setText(mLookingFor
									+ " for " + mOccasion);
						}
						/*
						 * mOccassionBigText.setText(occassionKeywordsList
						 * .get(position));
						 */
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
					mCategoryheading.setText(mCategoryitemsArray[position]);
					mCategoryheadingLayout.setVisibility(View.VISIBLE);
					mCategoryListview.setVisibility(View.GONE);
					mCategoryPopup.setVisibility(View.GONE);
					mCategoryListviewLayout.setVisibility(View.GONE);
					mSelectCategoryLayout.setVisibility(View.GONE);
					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
							mCategoryPopup.setVisibility(View.GONE);
						}
					}, CATEGORY_POPUP_DELAY);
					if (mFindAtText.getText().toString().trim().length() == 0) {
						mFindatClose.setVisibility(View.VISIBLE);
						// mFindAtIconLayout.setVisibility(View.VISIBLE);
						mFindAtPopUp.setVisibility(View.VISIBLE);
						// mFindAtLeftLine.setVisibility(View.VISIBLE);
						// mFindAtRightLine.setVisibility(View.VISIBLE);
						// mFindAtBottomLine.setVisibility(View.VISIBLE);
						mLookingForPopup.setVisibility(View.GONE);
						mLookingForListviewLayout.setVisibility(View.GONE);
						mOccasionPopup.setVisibility(View.GONE);
						mOccasionListviewLayout.setVisibility(View.GONE);
						mCategoryPopup.setVisibility(View.GONE);
						mCategoryListviewLayout.setVisibility(View.GONE);
						mSelectCategoryLayout.setVisibility(View.GONE);
						// mOccassionBigText.setBackgroundColor(Color.TRANSPARENT);
						// mLookingForBigText.setBackgroundColor(Color.TRANSPARENT);
						mInputMethodManager.hideSoftInputFromWindow(
								mOccasionText.getWindowToken(), 0);
						mInputMethodManager.hideSoftInputFromWindow(
								mLookingForText.getWindowToken(), 0);
						mInputMethodManager.hideSoftInputFromWindow(
								mSaySomethingAboutAisle.getWindowToken(), 0);
						mFindAtText.requestFocus();
						mFindAtText.setSelection(mFindAtText.getText()
								.toString().length());
						mInputMethodManager.showSoftInput(mFindAtText, 0);
						if (mDataEntryActivity == null) {
							mDataEntryActivity = (DataEntryActivity) getActivity();
						}
						mDataEntryActivity.mDataentryActionbarMainLayout
								.setVisibility(View.GONE);
						mDataEntryActivity.mVueDataentryKeyboardLayout
								.setVisibility(View.VISIBLE);
						mDataEntryActivity.mVueDataentryKeyboardDone
								.setVisibility(View.VISIBLE);
						mDataEntryActivity.mVueDataentryKeyboardCancel
								.setVisibility(View.VISIBLE);
					} else {
						// mSaySomeThingEditParent.setVisibility(View.GONE);
						mSaySomethingAboutAisle.setVisibility(View.VISIBLE);
						mSaysomethingClose.setVisibility(View.VISIBLE);
						/*
						 * mSaySomeThingEditParent.post(new Runnable() {
						 * 
						 * @Override public void run() {
						 * mSaySomethingAboutAisle.requestFocus();
						 * mSaySomethingAboutAisle.setFocusable(true);
						 * mSaySomethingAboutAisle.setCursorVisible(true);
						 * mSaySomethingAboutAisle
						 * .setSelection(mSaySomethingAboutAisle
						 * .getText().toString().length()); } });
						 */
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
						// mFindAtIconLayout.setVisibility(View.GONE);
						mFindAtPopUp.setVisibility(View.GONE);
						// mFindAtLeftLine.setVisibility(View.GONE);
						// mFindAtRightLine.setVisibility(View.GONE);
						// mFindAtBottomLine.setVisibility(View.GONE);
						mCategoryListviewLayout.setVisibility(View.GONE);
						mSelectCategoryLayout.setVisibility(View.GONE);
						// mOccassionBigText.setBackgroundColor(Color.TRANSPARENT);
						// mLookingForBigText.setBackgroundColor(Color.TRANSPARENT);
						final InputMethodManager inputMethodManager = (InputMethodManager) getActivity()
								.getSystemService(Context.INPUT_METHOD_SERVICE);
						inputMethodManager.toggleSoftInputFromWindow(
								mSaySomethingAboutAisle
										.getApplicationWindowToken(),
								InputMethodManager.SHOW_FORCED, 0);
						mSaySomethingAboutAisle.requestFocus();
						mInputMethodManager.showSoftInput(
								mSaySomethingAboutAisle, 0);
						if (mDataEntryActivity == null) {
							mDataEntryActivity = (DataEntryActivity) getActivity();
						}
						mDataEntryActivity.mDataentryActionbarMainLayout
								.setVisibility(View.GONE);
						mDataEntryActivity.mVueDataentryKeyboardLayout
								.setVisibility(View.VISIBLE);
						mDataEntryActivity.mVueDataentryKeyboardDone
								.setVisibility(View.VISIBLE);
						mDataEntryActivity.mVueDataentryKeyboardCancel
								.setVisibility(View.VISIBLE);
					}
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
		if (!Utils.getDataentryTopAddImageAisleFlag(getActivity())
				&& !Utils.getDataentryAddImageAisleFlag(getActivity())
				&& !mFromDetailsScreenFlag) {
			mLookingForPopup.setVisibility(View.VISIBLE);
		}
		try {
			Log.e("frag1", "gallery called,,,," + picturePath);
			Log.e("cs", "8");
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
				mDataEntryActivity.mVueDataentryActionbarScreenName
						.setText(getResources().getString(
								R.string.add_imae_to_aisle_screen_title));
				mDataEntryActivity.mVueDataentryActionbarBottomLayout
						.setVisibility(View.GONE);
				mDataEntryActivity.mVueDataentryActionbarTopLayout
						.setVisibility(View.VISIBLE);
				mDataEntryActivity.mVueDataentryActionbarSaveLayout
						.setVisibility(View.VISIBLE);
				// mDataEntryBottomBottomLayout.setVisibility(View.VISIBLE);
				mDataEntryBottomTopLayout.setVisibility(View.GONE);
				mMainHeadingRow.setVisibility(View.VISIBLE);
				mCategoryheadingLayout.setVisibility(View.VISIBLE);
				// mOccassionBigText.setBackgroundColor(Color.TRANSPARENT);
				// mLookingForBigText.setBackgroundColor(Color.TRANSPARENT);
			}
			if (Utils.getDataentryEditAisleFlag(getActivity())) {
				if (mDataEntryActivity == null) {
					mDataEntryActivity = (DataEntryActivity) getActivity();
				}
				mDataEntryActivity.mVueDataentryActionbarScreenName
						.setText(getResources().getString(
								R.string.edit_aisle_screen_title));
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		// lookingForTextClickFunctionality();

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
						addImageToAisleToServer(storedVueUser,
								Utils.getDataentryScreenAisleId(getActivity()),
								false);
						storeMetaAisleDataIntoLocalStorage();
						String categoery = mCategoryText.getText().toString()
								.trim();
						String lookingFor = mLookingFor;/*
														 * mLookingForBigText.
														 * getText()
														 * .toString().trim()
														 */
						;
						String occassion = mOccasion;
						/*
						 * if (!(mOccassionBigText.getText().toString().trim()
						 * .equals(OCCASION.trim()))) { occassion =
						 * mOccassionBigText.getText().toString() .trim(); }
						 */
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
					String lookingFor = /*
										 * mLookingForBigText.getText().toString(
										 * ) .trim()
										 */mLookingFor;
					String occassion = mOccasion;
					/*
					 * if (!(mOccassionBigText.getText().toString().trim()
					 * .equals(OCCASION.trim()))) { occassion =
					 * mOccassionBigText.getText().toString() .trim(); }
					 */
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
		mDataEntryActivity.mVueDataentryActionbarSaveLayout
				.setVisibility(View.GONE);
		try {
			mDataEntryAislesViewpager
					.setAdapter(new DataEntryAilsePagerAdapter(getActivity(),
							mAisleImagePathList));
		} catch (Throwable e) {
			e.printStackTrace();
		}
		Utils.putDataentryEditAisleFlag(getActivity(), false);
		mMainHeadingRow.setVisibility(View.GONE);
		mCategoryheadingLayout.setVisibility(View.GONE);
		// mDataEntryBottomBottomLayout.setVisibility(View.GONE);
		mDataEntryBottomTopLayout.setVisibility(View.VISIBLE);
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
									getActivity(), mAisleImagePathList));
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
											getActivity(), mAisleImagePathList));
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
									getActivity(), mAisleImagePathList));
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
		if (mAisleImagePathList != null && mAisleImagePathList.size() > 0) {
			String categoery = mCategoryText.getText().toString().trim();
			String lookingFor = mLookingFor.trim();
			String occassion = mOccasion;
			/*
			 * if
			 * (!(mOccassionBigText.getText().toString().trim().equals(OCCASION
			 * .trim()))) { occassion =
			 * mOccassionBigText.getText().toString().trim(); }
			 */
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
																	Utils.putDataentryScreenAisleId(
																			getActivity(),
																			aisleId);
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
										Utils.putDataentryScreenAisleId(
												getActivity(), aisleId);
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
			String ownerAisleId, final boolean fromDetailsScreenFlag) {
		if (mAisleImagePathList != null && mAisleImagePathList.size() > 0) {
			ArrayList<VueImage> vueImageList = new ArrayList<VueImage>();
			ArrayList<String> originalImagePathList = new ArrayList<String>();
			ArrayList<String> detailsUrlList = null;
			ArrayList<String> imageUrlList = null;
			ArrayList<Integer> imageWidthList = null;
			ArrayList<Integer> imageHeightList = null;
			ArrayList<String> imageStoreList = null;
			ArrayList<String> offlineImageIdList = new ArrayList<String>();
			if (fromDetailsScreenFlag) {
				detailsUrlList = new ArrayList<String>();
				imageUrlList = new ArrayList<String>();
				imageWidthList = new ArrayList<Integer>();
				imageHeightList = new ArrayList<Integer>();
				imageStoreList = new ArrayList<String>();
			}
			for (int i = 0; i < mAisleImagePathList.size(); i++) {
				if (!(mAisleImagePathList.get(i).isAddedToServerFlag())) {
					VueImage image = new VueImage();
					image.setDetailsUrl(mAisleImagePathList.get(i)
							.getDetailsUrl());
					image.setHeight(mAisleImagePathList.get(i).getImageHeight());
					image.setWidth(mAisleImagePathList.get(i).getImageWidth());
					image.setStore(mAisleImagePathList.get(i).getImageStore());
					image.setImageUrl(mAisleImagePathList.get(i).getImageUrl());
					image.setTitle("Android Test"); // TODO By Krishna
					image.setOwnerUserId(Long.valueOf(Long.valueOf(
							storedVueUser.getId()).toString()));
					image.setOwnerAisleId(Long.valueOf(ownerAisleId));
					vueImageList.add(image);
					originalImagePathList.add(mAisleImagePathList.get(i)
							.getOriginalImagePath());
					String offlineImageId = String.valueOf(System
							.currentTimeMillis() + i);
					offlineImageIdList.add(offlineImageId);
					mAisleImagePathList.get(i).setImageId(offlineImageId);
					mAisleImagePathList.get(i).setAisleId(ownerAisleId);
					mAisleImagePathList.get(i).setAddedToServerFlag(true);
					if (fromDetailsScreenFlag) {
						detailsUrlList.add(mAisleImagePathList.get(i)
								.getDetailsUrl());
						imageUrlList.add(mAisleImagePathList.get(i)
								.getImageUrl());
						imageWidthList.add(mAisleImagePathList.get(i)
								.getImageWidth());
						imageHeightList.add(mAisleImagePathList.get(i)
								.getImageHeight());
						imageStoreList.add(mAisleImagePathList.get(i)
								.getImageStore());
					}
				}
			}
			if (vueImageList.size() > 0) {
				if (!fromDetailsScreenFlag) {
					try {
						Utils.writeAisleImagePathListToFile(getActivity(),
								VueConstants.AISLE_IMAGE_PATH_LIST_FILE_NAME,
								mAisleImagePathList);
					} catch (Exception e) {
					}
				} else {
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
							mCategoryText.getText().toString());
					b.putString(
							VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_SAYSOMETHINGABOUTAISLE,
							mSaySomethingAboutAisle.getText().toString());
					b.putStringArrayList(
							VueConstants.CREATE_AISLE_CAMERA_GALLERY_IMAGE_PATH_BUNDLE_KEY,
							originalImagePathList);
					b.putStringArrayList(
							VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_FINDAT,
							detailsUrlList);
					b.putStringArrayList(
							VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_IMAGEURL,
							imageUrlList);
					b.putIntegerArrayList(
							VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_IMAGE_WIDTH,
							imageWidthList);
					b.putIntegerArrayList(
							VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_IMAGE_HEIGHT,
							imageHeightList);
					b.putStringArrayList(
							VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_IMAGE_DETAILSURL,
							detailsUrlList);
					b.putStringArrayList(
							VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_IMAGE_STORE,
							imageStoreList);
					b.putStringArrayList(
							VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_OFFLINE_IMAGE_ID,
							offlineImageIdList);
					intent.putExtras(b);
					Log.e("Land", "vueland 11");
					getActivity()
							.setResult(
									VueConstants.FROM_DETAILS_SCREEN_TO_DATAENTRY_SCREEN_ACTIVITY_RESULT,
									intent);
					String categoery = mCategoryText.getText().toString()
							.trim();
					String lookingFor = mLookingFor.trim();
					String occassion = mOccasion;
					/*
					 * if (!(mOccassionBigText.getText().toString().trim()
					 * .equals(OCCASION.trim()))) { occassion = mOccasion
					 * .trim(); }
					 */
					String description = mSaySomethingAboutAisle.getText()
							.toString().trim();

					checkForAisleUpdate(storedVueUser, VueApplication
							.getInstance().getClickedWindowID(), categoery,
							lookingFor, occassion, description);
					if (mDataEntryActivity == null) {
						mDataEntryActivity = (DataEntryActivity) getActivity();
					}
					mDataEntryActivity.shareViaVueClicked();
				}
				for (int j = 0; j < vueImageList.size(); j++) {
					if (!fromDetailsScreenFlag) {
						addSingleImageToServer(vueImageList.get(j),
								originalImagePathList.get(j),
								fromDetailsScreenFlag,
								offlineImageIdList.get(j));
					} else {
						addSingleImageToServer(vueImageList.get(j),
								originalImagePathList.get(j),
								fromDetailsScreenFlag,
								offlineImageIdList.get(j));
					}
				}
			} else {
				Toast.makeText(
						getActivity(),
						getResources()
								.getString(
										R.string.dataentry_mandtory_field_add_aisleimage_mesg),
						Toast.LENGTH_LONG).show();
			}
		} else {
			if (Utils.getDataentryEditAisleFlag(getActivity())) {
				Toast.makeText(
						getActivity(),
						getResources()
								.getString(
										R.string.dataentry_mandtory_field_add_aisleimage_mesg),
						Toast.LENGTH_LONG).show();
			}
		}
	}

	private class ImageResizeAsynTask extends
			AsyncTask<Activity, Activity, Activity> {

		@Override
		protected void onPostExecute(Activity result) {
			Log.e("cs", "9");
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
		// sourceUrl = "http://www.wish.com/c/50f1b7b83b97ee7282ea8e9d";
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

	public class ShareViaVueListner implements ShareViaVueClickedListner {
		@Override
		public void onAisleShareToVue() {
			((DataEntryActivity) getActivity()).shareViaVueClicked();
		}
	}

	public void touchToChangeImageClickFunctionality(int position) {
		hideAllEditableTextboxes();
		Intent intent = new Intent(getActivity(),
				CreateAisleSelectionActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		Bundle b = new Bundle();
		b.putBoolean(VueConstants.FROMCREATEAILSESCREENFLAG, true);
		b.putInt(VueConstants.TOUCH_TO_CHANGE_IMAGE_POSITION, position);
		intent.putExtras(b);
		if (!CreateAisleSelectionActivity.isActivityShowing) {
			if (mFromDetailsScreenFlag) {
				Utils.putFromDetailsScreenToDataentryCreateAisleScreenPreferenceFlag(
						getActivity(), true);
			} else {
				Utils.putFromDetailsScreenToDataentryCreateAisleScreenPreferenceFlag(
						getActivity(), false);
			}
			Utils.putTouchToChnageImageFlag(getActivity(), true);
			Utils.putTouchToChnageImageTempPosition(getActivity(), position);
			CreateAisleSelectionActivity.isActivityShowing = true;
			getActivity().startActivityForResult(intent,
					VueConstants.CREATE_AILSE_ACTIVITY_RESULT);
		}
	}

	public boolean isAisleAddedScreenVisible() {
		if (mDataEntryActivity == null) {
			mDataEntryActivity = (DataEntryActivity) getActivity();
		}
		if (mDataEntryActivity.mVueDataentryActionbarBottomLayout
				.getVisibility() == View.VISIBLE) {
			return true;
		}
		return false;
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
		Log.e("DataentryFragment",
				"showDetailsScreenImagesInDataentryScreen : "
						+ mIsUserAisleFlag);
		// User Ailse from Details screen...
		if (mIsUserAisleFlag) {
			Log.e("DataentryFragment", "iif from details screen : "
					+ mIsUserAisleFlag);
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
								getActivity(), mAisleImagePathList));
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

	public void deleteImage(int position) {
		try {
			Log.e("deleteimage", "deleteimage : " + position);
			VueUser storedVueUser = null;
			try {
				storedVueUser = Utils.readUserObjectFromFile(getActivity(),
						VueConstants.VUE_APP_USEROBJECT__FILENAME);
			} catch (Exception e2) {
				e2.printStackTrace();
			}
			if (storedVueUser != null && storedVueUser.getId() != null) {
				Log.e("deleteimage", "deleteimage 1 : " + position);
				if (VueConnectivityManager.isNetworkConnected(getActivity())) {
					Log.e("deleteimage", "deleteimage 2 : " + position);
					Image image = new Image();
					image.setDetailsUrl(mAisleImagePathList.get(position)
							.getDetailsUrl());
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
					Log.e("deleteimage", "deleteimage 2 : "
							+ mAisleImagePathList.get(position).getAisleId());
					String aisleId = mAisleImagePathList.get(position)
							.getAisleId();
					if (aisleId == null || aisleId.equals("null")) {
						Log.e("deleteimage", "deleteimage 3 : ");
						if (mFromDetailsScreenFlag) {
							Log.e("deleteimage", "deleteimage 4 : ");
							aisleId = VueApplication.getInstance()
									.getClickedWindowID();
						} else {
							Log.e("deleteimage", "deleteimage 5 : ");
							Utils.getDataentryScreenAisleId(getActivity());
						}
					}
					image.setOwnerAisleId(Long.valueOf(aisleId));
					image.setId(Long.valueOf(mAisleImagePathList.get(position)
							.getImageId()));
					VueTrendingAislesDataModel
							.getInstance(VueApplication.getInstance())
							.getNetworkHandler()
							.requestForDeleteImage(image, aisleId);
					mAisleImagePathList.remove(position);
					try {
						Utils.writeAisleImagePathListToFile(getActivity(),
								VueConstants.AISLE_IMAGE_PATH_LIST_FILE_NAME,
								mAisleImagePathList);
					} catch (Exception e) {
						e.printStackTrace();
					}
					if (mAisleImagePathList != null
							&& mAisleImagePathList.size() > 0) {
						Log.e("deleteimage", "deleteimage 3 : "
								+ mAisleImagePathList.size());
						mDataEntryAislesViewpager.setVisibility(View.VISIBLE);
						try {
							mDataEntryAislesViewpager
									.setAdapter(new DataEntryAilsePagerAdapter(
											getActivity(), mAisleImagePathList));
							mDataEntryAislesViewpager.setCurrentItem(position);
						} catch (Throwable e) {
							e.printStackTrace();
						}
					} else {
						if (mIsUserAisleFlag) {
							VueApplication.getInstance()
									.setmFinishDetailsScreenFlag(true);
						}
						if (mDataEntryActivity == null) {
							mDataEntryActivity = (DataEntryActivity) getActivity();
						}
						mDataEntryActivity.shareViaVueClicked();
					}
				}
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
	}
}