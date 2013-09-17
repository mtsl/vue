package com.lateralthoughts.vue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationManager;
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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.google.android.gms.plus.model.people.Person.Image;
import com.googleplus.UserInfo;
import com.lateralthoughts.vue.AisleManager.AisleUpdateCallback;
import com.lateralthoughts.vue.connectivity.AisleData;
import com.lateralthoughts.vue.connectivity.DataBaseManager;
import com.lateralthoughts.vue.domain.Aisle;
import com.lateralthoughts.vue.domain.VueImage;
import com.lateralthoughts.vue.utils.BitmapLoaderUtils;
import com.lateralthoughts.vue.utils.EditTextBackEvent;
import com.lateralthoughts.vue.utils.FileCache;
import com.lateralthoughts.vue.utils.GetOtherSourceImagesTask;
import com.lateralthoughts.vue.utils.OtherSourceImageDetails;
import com.lateralthoughts.vue.utils.OnInterceptListener;
import com.lateralthoughts.vue.utils.Utils;
import com.lateralthoughts.vue.utils.clsShare;

/**
 * Fragment for creating Aisle
 * 
 */
public class DataEntryFragment extends Fragment {

	public static String testId = "";
	public static String testCutomUrl = "";
	private AisleWindowContentFactory mAisleWindowContentFactory;
	AisleWindowContent aisleItem = null;
	private ListView mCategoryListview = null, mLookingForListview = null,
			mOccasionListview = null;
	public LinearLayout mLookingForListviewLayout = null,
			mOccasionPopup = null, mCategoryPopup = null,
			mCategoryListviewLayout = null, mOccasionListviewLayout = null,
			mDataEntryRootLayout = null;
	public LinearLayout mLookingForPopup = null;
	public TextView mTouchToChangeImage = null, mLookingForBigText = null,
			mOccassionBigText = null, mCategoryText = null,
			mHintTextForSaySomeThing;
	public EditTextBackEvent mLookingForText = null, mOccasionText = null,
			mSaySomethingAboutAisle = null, mFindAtText = null;
	private static String mCategoryitemsArray[] = null;
	private Drawable mListDivider = null;
	public ImageView mCreateAisleBg = null, mCategoryIcon = null;
	private InputMethodManager mInputMethodManager;
	private boolean mDontGoToNextLookingFor = false,
			mDontGoToNextForOccasion = false;
	private String mPreviousLookingfor = null, mPreviousOcasion = null,
			mPreviousFindAtText = null, mPreviousSaySomething = null;
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
	private boolean mAddImageToAisleFlag = false, mEditAisleImageFlag = false;
	private float mScreenHeight = 0, mScreenWidth = 0;
	private LinearLayout mFindAtPopup = null;
	private ViewPager mDataEntryAislesViewpager = null;
	public static final int AISLE_IMAGE_MARGIN = 96;
	public static final String LOOKING_FOR = "Looking";
	public static final String OCCASION = " Occasion";
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
	public boolean mFromDetailsScreenFlag = false;
	public boolean mIsUserAisleFlag = false;
	private LoginWarningMessage mLoginWarningMessage = null;
	private OtherSourcesDialog mOtherSourcesDialog = null;
	private ProgressDialog mProgressDialog;
	private DataEntryActivity mDataEntryActivity;
	private List<VueImage> mImageList;

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
		mImageList = new ArrayList<VueImage>();
		mAisleWindowContentFactory = AisleWindowContentFactory
				.getInstance(getActivity());
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
		mDataEntryAislesViewpager = (ViewPager) mDataEntryFragmentView
				.findViewById(R.id.dataentry_aisles_viewpager);
		mOccasionListviewLayout = (LinearLayout) mDataEntryFragmentView
				.findViewById(R.id.ocassionlistviewlayout);
		mAisleBgProgressbar = (ProgressBar) mDataEntryFragmentView
				.findViewById(R.id.aisle_bg_progressbar);
		mDataEntryRootLayout = (LinearLayout) mDataEntryFragmentView
				.findViewById(R.id.dataentry_root_layout);
		mOccasionListview = (ListView) mDataEntryFragmentView
				.findViewById(R.id.ocassionlistview);
		mFindAtIcon = (ImageView) mDataEntryFragmentView
				.findViewById(R.id.find_at_icon);
		mLookingForListview = (ListView) mDataEntryFragmentView
				.findViewById(R.id.lookingforlistview);
		mFindAtText = (EditTextBackEvent) mDataEntryFragmentView
				.findViewById(R.id.find_at_text);
		mFindAtPopup = (LinearLayout) mDataEntryFragmentView
				.findViewById(R.id.find_at_popup);
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
		mCategoryText.setText(mCategoryitemsArray[0]);
		mCategoryListview = (ListView) mDataEntryFragmentView
				.findViewById(R.id.categorylistview);
		mCategoryListviewLayout = (LinearLayout) mDataEntryFragmentView
				.findViewById(R.id.categorylistviewlayout);
		mMainHeadingRow = (LinearLayout) mDataEntryFragmentView
				.findViewById(R.id.mainheadingrow);
		mListDivider = getResources().getDrawable(R.drawable.list_divider_line);
		mLookingForListviewLayout.setVisibility(View.GONE);
		mCreateAisleBg = (ImageView) mDataEntryFragmentView
				.findViewById(R.id.createaisel_bg);
		mCategoryListview.setDivider(mListDivider);
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
			mLookingForText.setText(mLookingForAisleKeywordsList.get(0));
			mLookingForBigText.setText(mLookingForAisleKeywordsList.get(0));
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
			mOccasionText.setText(mOccassionAisleKeywordsList.get(0));
			mOccassionBigText.setText(mOccassionAisleKeywordsList.get(0));
		}
		mCategoryAilseKeywordsList = mDbManager
				.getAisleKeywords(VueConstants.CATEGORY_TABLE);
		if (mCategoryAilseKeywordsList != null) {
			mCategoryText.setText(mCategoryAilseKeywordsList.get(0));
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
						return true;
					}
				});
		mSaySomeThingEditParent.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mSaySomeThingEditParent.setVisibility(View.GONE);
				mSaySomethingAboutAisle.setVisibility(View.VISIBLE);
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
				mFindAtPopup.setVisibility(View.GONE);
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
				// TODO Auto-generated method stub
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
					mOccassionBigText.setText(" "
							+ mOccasionText.getText().toString());
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
				// TODO Auto-generated method stub

			}

			@Override
			public void onKeyBackPressed() {
				occasionInterceptListnerFunctionality();
			}

			@Override
			public boolean getFlag() {
				// TODO Auto-generated method stub
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
				mFindAtPopup.setVisibility(View.GONE);
				mCategoryListview.setVisibility(View.GONE);
				mCategoryListviewLayout.setVisibility(View.GONE);
				mCategoryPopup.setVisibility(View.GONE);
				if (mAddImageToAisleFlag || mEditAisleImageFlag) {
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
				mFindAtPopup.setVisibility(View.GONE);
				mCategoryListview.setVisibility(View.GONE);
				mCategoryListviewLayout.setVisibility(View.GONE);
				mCategoryPopup.setVisibility(View.GONE);
				if (mAddImageToAisleFlag || mEditAisleImageFlag) {
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
				mFindAtPopup.setVisibility(View.GONE);
				if (mAddImageToAisleFlag || mEditAisleImageFlag) {
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
									VueApplication.getInstance().mAisleImagePathList
											.get(mDataEntryAislesViewpager
													.getCurrentItem()));
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
				mFindAtPopup.setVisibility(View.VISIBLE);
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
				mFindAtPopup.setVisibility(View.GONE);
				return true;
			};
		});
		mFindAtText.setonInterceptListen(new OnInterceptListener() {

			@Override
			public void setFlag(boolean flag) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onKeyBackPressed() {
				findAtInterceptListnerFunctionality();

			}

			@Override
			public boolean getFlag() {
				// TODO Auto-generated method stub
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
		return mDataEntryFragmentView;
	}

	private void hideAllEditableTextboxes() {
		if (mOccasionText.getText().toString().trim().length() > 0) {
			mOccassionBigText.setText(" " + mOccasionText.getText().toString());
		}
		if (mLookingForText.getText().toString().trim().length() > 0) {
			mLookingForBigText.setText(mLookingForText.getText().toString());
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
		mFindAtPopup.setVisibility(View.GONE);
		mCategoryListviewLayout.setVisibility(View.GONE);
		mOccassionBigText.setBackgroundColor(Color.TRANSPARENT);
		mLookingForBigText.setBackgroundColor(Color.TRANSPARENT);
		mSaySomeThingEditParent.setVisibility(View.VISIBLE);
		mSaySomethingAboutAisle.setVisibility(View.GONE);
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
		mFindAtPopup.setVisibility(View.GONE);
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
				&& !(mOccassionBigText.getText().toString().trim()
						.equals(OCCASION))) {
			if (mAddImageToAisleFlag) {
				addImageToAisleToServer();
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
							Intent intent = new Intent();
							Bundle b = new Bundle();
							b.putString(
									VueConstants.CREATE_AISLE_CAMERA_GALLERY_IMAGE_PATH_BUNDLE_KEY,
									mImagePath);
							if (!mIsUserAisleFlag) {
								b.putString(
										VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_LOOKINGFOR,
										mLookingForBigText.getText().toString());
								b.putString(
										VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_OCCASION,
										mOccassionBigText.getText().toString());
								b.putString(
										VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_CATEGORY,
										mCategoryText.getText().toString());
								b.putString(
										VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_SAYSOMETHINGABOUTAISLE,
										mSaySomethingAboutAisle.getText()
												.toString());
							}
							b.putString(
									VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_FINDAT,
									mFindAtText.getText().toString());
							intent.putExtras(b);
							Log.e("Land", "vueland 11");
							getActivity()
									.setResult(
											VueConstants.FROM_DETAILS_SCREEN_TO_DATAENTRY_SCREEN_ACTIVITY_RESULT,
											intent);
							getActivity().finish();
						} else {
							showAlertForMandotoryFields(getResources()
									.getString(
											R.string.dataentry_mandtory_field_add_aisleimage_mesg));
						}
					}
				} else {
					if (mAisleImageBitmap != null) {
						addAisleToServer();
					} else {
						showAlertForMandotoryFields(getResources()
								.getString(
										R.string.dataentry_mandtory_field_add_aisleimage_mesg));
					}
				}
			}
		} else {
			showAlertForMandotoryFields(getResources().getString(
					R.string.dataentry_mandtory_field_mesg));
		}
	}

	public void editButtonClickFunctionality() {
		mDataEntryInviteFriendsPopupLayout.setVisibility(View.GONE);
		mEditAisleImageFlag = true;
		mCurrentPagePosition = mDataEntryAislesViewpager.getCurrentItem();
		mResizedImagePath = VueApplication.getInstance().mAisleImagePathList
				.get(mCurrentPagePosition);
		mImagePath = VueApplication.getInstance().mAisleImagePathList
				.get(mCurrentPagePosition);
		File aisleFile = new File(
				VueApplication.getInstance().mAisleImagePathList
						.get(mCurrentPagePosition));
		if (aisleFile.exists()) {
			mAisleImageBitmap = BitmapFactory.decodeFile(aisleFile.getPath());
			mCreateAisleBg.setImageBitmap(mAisleImageBitmap);
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
		if (VueApplication.getInstance().mAisleImagePathList != null) {
			ArrayList<clsShare> imageUrlList = new ArrayList<clsShare>();
			for (int i = 0; i < VueApplication.getInstance().mAisleImagePathList
					.size(); i++) {
				clsShare shareObj = new clsShare(null,
						VueApplication.getInstance().mAisleImagePathList.get(i));
				imageUrlList.add(shareObj);
			}
			mShare.share(imageUrlList, "", "");
		}

	}

	public void addImageToAisleButtonClickFunctionality() {
		mDataEntryInviteFriendsPopupLayout.setVisibility(View.GONE);
		mAddImageToAisleFlag = true;
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
		dialog.setContentView(R.layout.googleplusappinstallationdialog);
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
		dialog.setContentView(R.layout.googleplusappinstallationdialog);
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
		mCategoryPopup.setVisibility(View.VISIBLE);
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
					mCategoryPopup.setVisibility(View.GONE);
					mCategoryListviewLayout.setVisibility(View.GONE);
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
					mCreateAisleBg.setImageBitmap(mAisleImageBitmap);
				} else {
					mCreateAisleBg.setImageDrawable(getResources().getDrawable(
							R.drawable.no_image));
				}
			} else {
				mImageResizeAsynTask = new ImageResizeAsynTask();
				mImageResizeAsynTask.execute(getActivity());
			}
			if (mAddImageToAisleFlag) {
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

	private void addImageToAisleToServer() {
		/*
		 * // Input parameters for Adding Aisle to server request... String
		 * category = mCategoryText.getText().toString(); String lookingFor =
		 * mLookingForBigText.getText().toString(); String occasion =
		 * mOccassionBigText.getText().toString(); String imageUrl = mImagePath;
		 * // This path is image location stored in // locally when user selects
		 * from Camera // OR Gallery. String title = ""; // For Camera and
		 * Gallery we don't have title. String store = ""; // For Camera and
		 * Gallery we don't have store.
		 */
		if (checkLimitForLoginDialog()) {
			if (mLoginWarningMessage == null) {
				mLoginWarningMessage = new LoginWarningMessage(getActivity());
			}
			mLoginWarningMessage.showLoginWarningMessageDialog(
					"You need to Login with the app to add image to aisle.",
					true, true, 0, null, null);
		} else {
			mImageList.add(getImage(mImagePath, 0, 0, "", 0L, 0L));
			// addImageToAisle(getImage(mImagePath, 0, 0, "", 0L, 0L));
			boolean isFirstImage = false;
			addImageToAisle(mImageList.remove(0), aisleItem.getAisleId(),
					isFirstImage);
			storeMetaAisleDataIntoLocalStorage();
		}
	}

	private void addAisleToServer() {
		/*
		 * // Input parameters for Adding Aisle to server request... String
		 * category = mCategoryText.getText().toString(); String lookingFor =
		 * mLookingForBigText.getText().toString(); String occasion =
		 * mOccassionBigText.getText().toString(); String imageUrl = mImagePath;
		 * // This path is image location stored in // locally when user selects
		 * from Camera // OR Gallery. String title = ""; // For Camera and
		 * Gallery we don't have title. String store = ""; // For Camera and
		 * Gallery we don't have store.
		 */
		// Updating Aisles Count in Preference to show LoginDialog.
		if (!mEditAisleImageFlag) {
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
					storeMetaAisleDataIntoLocalStorage();
					createNewAisle();
				}
			}
		} else {
			storeMetaAisleDataIntoLocalStorage();
		}
	}

	public void storeMetaAisleDataIntoLocalStorage() {
		showDataProgressOnNotification();
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
		if (mEditAisleImageFlag) {
			VueApplication.getInstance().mAisleImagePathList
					.remove(mCurrentPagePosition);
		}
		mEditAisleImageFlag = false;
		mMainHeadingRow.setVisibility(View.GONE);
		mTouchToChangeImage.setVisibility(View.GONE);
		mDataEntryBottomBottomLayout.setVisibility(View.GONE);
		mDataEntryBottomTopLayout.setVisibility(View.VISIBLE);
		VueApplication.getInstance().mAisleImagePathList.add(0,
				mResizedImagePath);
		mDataEntryAislesViewpager.setVisibility(View.VISIBLE);
		mCreateAisleBg.setVisibility(View.GONE);
		try {
			mDataEntryAislesViewpager
					.setAdapter(new DataEntryAilsePagerAdapter(getActivity(),
							VueApplication.getInstance().mAisleImagePathList));
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private void showDataProgressOnNotification() {
		final NotificationManager notifyManager = (NotificationManager) getActivity()
				.getSystemService(Context.NOTIFICATION_SERVICE);
		final Builder builder = new NotificationCompat.Builder(getActivity());
		builder.setContentTitle(getResources().getString(R.string.app_name))
				.setContentText(
						getResources().getString(R.string.uploading_mesg))
				.setSmallIcon(R.drawable.vue_launcher_icon);
		// Start a lengthy operation in a background thread
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
				AisleData occassionAisleDataObj = mDbManager
						.getAisleMetaDataForKeyword(mOccassionBigText.getText()
								.toString().trim(), VueConstants.OCCASION_TABLE);
				if (occassionAisleDataObj != null) {
					occassionAisleDataObj.count += 1;
					occassionAisleDataObj.isNew = false;
				} else {
					occassionAisleDataObj = new AisleData();
					occassionAisleDataObj.keyword = mOccassionBigText.getText()
							.toString().trim();
					occassionAisleDataObj.count = 1;
					occassionAisleDataObj.isNew = true;
				}
				occassionAisleDataObj.time = currentTime;
				mDbManager.addAisleMetaDataToDB(VueConstants.OCCASION_TABLE,
						occassionAisleDataObj);
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
				builder.setContentText("Uploading completed")
				// Removes the progress bar
						.setProgress(0, 0, false);
				notifyManager.notify(0, builder.build());
			}
		}
		// Starts the thread by calling the run() method in its Runnable
		).start();
		// upload empty aisle to server.

	}

	// create ailse and send to server.
	private void createNewAisle() {
		Log.i("create ailse functionality",
				"create ailse functionality addAilse");
		VueUser storedVueUser = null;
		try {
			storedVueUser = Utils.readObjectFromFile(getActivity(),
					VueConstants.VUE_APP_USEROBJECT__FILENAME);
		} catch (Exception e2) {
			e2.printStackTrace();
		}
		AisleManager aisleManager = AisleManager.getAisleManager();
		Aisle aisle = new Aisle();
		//aisle.setCategory(mCategoryText.getText().toString().trim());
		aisle.setCategory("occasion");

		aisle.setLookingFor(mLookingForBigText.getText().toString().trim());
		aisle.setName("Super Aisle");
		aisle.setOccassion(mOccassionBigText.getText().toString().trim());
		/*
		 * aisle.setmFisrtName("Vue"); aisle.setmLastName("TestAisle");
		 */
		String s = mFindAtText.getText().toString();
		Log.i("imageurl", "imageurl  from other sources: path  " + s);
		if (s != null && s.length()>2) {
			mImagePath = s;
			Log.i("imageurl", "imageurl  from other sources:  " + mImagePath);
		}
		mImageList.add(getImage(mImagePath, 340, 340, "dummy", 12345L, 123L));
		Log.i("userid", "userid123456 null check storedVueUser: "
				+ storedVueUser);
		if (storedVueUser != null) {
			aisle.setOwnerUserId(Long.valueOf(storedVueUser.getVueId()));
		}
		VueTrendingAislesDataModel
				.getInstance(VueApplication.getInstance())
				.getNetworkHandler()
				.requestCreateAisle(aisle,
						new AisleManager.AisleUpdateCallback() {
							@Override
							public void onAisleUpdated(
									AisleContext aisleContext, String aisleId) {
								Log.e("AisleCreationTest",
										"Aisle created1 successfully****!");
								setAisleContent(aisleContext, aisleId);
								// addImage();
							}
						});
	}

	// create Image object add to aisle
	private VueImage getImage(String originalImageLocation, int width,
			int height, String title, Long owneAisleId, Long ownerUserId) {

		VueImage image = new VueImage();
		image.setDetailsUrl("");
		image.setHeight(height);
		image.setWidth(width);
		// image.setId(0L);
		image.setImageUrl(originalImageLocation);
		image.setTitle(title);
		image.setOwnerAisleId(owneAisleId);
		image.setOwnerUserId(ownerUserId);
		// image.setRating(rating);
		// image.setStore(store);
		return image;
	}

	// add image to the created aisle
	private void addImage() {
		Log.i("addimagetoaisle", "addimagetoaisle  method call1");
		VueImage image = null;
		if (mImageList.size() > 0) {
			image = mImageList.remove(0);
		} else {
			Log.i("addimagetoaisle", "addimagetoaisle  method call returning");
			return;
		}
		Log.i("addimagetoaisle", "addimagetoaisle  method call2");
		AisleManager aisleManager = AisleManager.getAisleManager();
		aisleManager.addImageToAisle(image,
				new AisleManager.ImageAddedCallback() {

					@Override
					public void onImageAdded(AisleImageDetails imageDetails) {
						if (aisleItem != null) {
							ArrayList<AisleImageDetails> mAisleImagesList = aisleItem
									.getImageList();
							mAisleImagesList.add(imageDetails);
							aisleItem.addAisleContent(
									aisleItem.getAisleContext(),
									mAisleImagesList);
						}
						addImage();

					}
				});
	}

	private class ImageResizeAsynTask extends
			AsyncTask<Activity, Activity, Activity> {

		@Override
		protected void onPostExecute(Activity result) {
			Log.e("cs", "9");
			Log.e("Frag", mResizedImagePath);
			mAisleBgProgressbar.setVisibility(View.GONE);
			mCreateAisleBg.setVisibility(View.VISIBLE);
			if (mAisleImageBitmap != null) {
				mCreateAisleBg.setImageBitmap(mAisleImageBitmap);
			} else {
				mCreateAisleBg.setImageDrawable(getResources().getDrawable(
						R.drawable.no_image));
			}
			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
		}

		@Override
		protected Activity doInBackground(Activity... params) {
			mResizedImagePath = Utils.getResizedImage(new File(mImagePath),
					mScreenHeight, mScreenWidth, params[0]);
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
			ArrayList<OtherSourceImageDetails> imagesList) {
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
		if (imagesList != null && imagesList.size() > 0) {
			if (mOtherSourcesDialog == null) {
				mOtherSourcesDialog = new OtherSourcesDialog(getActivity());
			}
			mOtherSourcesDialog.showImageDailog(imagesList, false);
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
		GetOtherSourceImagesTask getImagesTask = new GetOtherSourceImagesTask(
				sourceUrl, getActivity(), false);
		getImagesTask.execute();
	}

	// create aisle window and add to list so that aisle will be visible in
	// list.
	private void setAisleContent(AisleContext userInfo, final String aisleId) {
		Log.i("imageurl", "imageurl  aisle creation success ");
		aisleItem = VueTrendingAislesDataModel.getInstance(getActivity())
				.getAisle(aisleId);
		aisleItem.setAisleId(aisleId);
		aisleItem.setAisleContext(userInfo);
		VueImage image = mImageList.remove(0);
		boolean isFirstImage = true;
		addImageToAisle(image, aisleId, isFirstImage);
		// VueTrendingAislesDataModel.getInstance(getActivity()).insertNewAisleToDb(aisleId);
	}

	private Bitmap saveBitmap(String sourcePath, String destPath) {

		FileCache fileCache = new FileCache(getActivity());
		File f = fileCache.getFile(destPath);
		File sourceFile = new File(sourcePath);
		Bitmap bmp = BitmapLoaderUtils.getInstance().decodeFile(sourceFile,
				VueApplication.getInstance().mScreenHeight);
		Utils.saveBitmap(bmp, f);
		return bmp;
	}

	// add image to the created aisle for now, we are creating the aisle
	// depending the
	// response from the server but for adding image without waiting for the
	// server
	// response add immediately after that send that image data to the back end.
	// because to create an aisle requires aisle id but to add image need not
	// wait for server response.
	private void addImageToAisle(VueImage image, final String aisleId,
			final boolean isFirstImage) {
		// Bitmap bmp = saveBitmap(image.getImageUrl(), image.getImageUrl());
		Log.i("aisleId", "aisleId: newly create aile id: "+aisleId);
		image.setHeight(340);
		image.setWidth(340);
		Log.i("imageurl", "imageurl adding image to aisle ");
		VueUser storedVueUser = null;
		try {
			storedVueUser = Utils.readObjectFromFile(getActivity(),
					VueConstants.VUE_APP_USEROBJECT__FILENAME);
		} catch (Exception e2) {
			e2.printStackTrace();
		}
		image = getImage(image.getImageUrl(), image.getWidth(),
				image.getHeight(), "", Long.valueOf(aisleItem.getAisleId()),
				Long.valueOf(storedVueUser.getVueId()));
		// AisleManager aisleManager = AisleManager.getAisleManager();
		VueTrendingAislesDataModel
				.getInstance(VueApplication.getInstance())
				.getNetworkHandler()
				.requestForAddImage(image,
						new AisleManager.ImageAddedCallback() {

							@Override
							public void onImageAdded(
									final AisleImageDetails imageDetails) {
								new Thread(new Runnable() {

									@Override
									public void run() {
										Log.i("imageurl",
												"imageurl image added suucessfully ");
										if (aisleItem != null) {
											ArrayList<AisleImageDetails> imageItemsArray = null;
											imageItemsArray = aisleItem
													.getImageList();
											if (imageItemsArray == null) {
												imageItemsArray = new ArrayList<AisleImageDetails>();
											}
											imageItemsArray.add(imageDetails);
											Log.i("imageurl", "imageurl is: "
													+ imageDetails.mImageUrl);
											int tempHeight = 340;
											Bitmap bmp = BitmapLoaderUtils
													.getInstance()
													.getBitmap(
															imageDetails.mImageUrl,
															true, tempHeight);
								 if(bmp == null){
									 bmp =	saveBitmap(imageDetails.mImageUrl,
												imageDetails.mImageUrl);
								 }
											
											cacheBitmap(imageDetails.mImageUrl,
													bmp);
										
											if (isFirstImage) {
												testId = aisleId;
												aisleItem.addAisleContent(
														aisleItem
																.getAisleContext(),
														imageItemsArray);
												imageDetails.mCustomImageUrl = aisleItem
														.getImageList().get(0).mCustomImageUrl;
												testCutomUrl = imageDetails.mCustomImageUrl;
												cacheBitmap(
														imageDetails.mCustomImageUrl,
														bmp);
												Log.i("imageurl",
														"imageurl original  imageDetails.mCustomImageUrl:  "
																+ imageDetails.mCustomImageUrl);
												// Bitmap bmp2 =
												// BitmapLoaderUtils.getInstance().getBitmap(imageDetails.mCustomImageUrl,
												// true, 340);
												// Log.i("imageurl",
												// "imageurl original bitmap1 widht and height2:  "+bmp2.getWidth()+" height: "+bmp2.getHeight());
												// saveBitmap(imageDetails.mImageUrl,
												// imageDetails.mCustomImageUrl);
												VueTrendingAislesDataModel
														.getInstance(
																getActivity())
														.addItemToListAt(
																aisleId,
																aisleItem, 0);
											} else {
												if (imageDetails.mAvailableHeight < aisleItem.mWindowSmallestHeight
														|| aisleItem.mWindowSmallestHeight == 0)
													aisleItem.mWindowSmallestHeight = imageDetails.mAvailableHeight;
												aisleItem
														.prepareCustomUrl(imageDetails);
										/*		saveBitmap(
														imageDetails.mImageUrl,
														imageDetails.mCustomImageUrl);*/
												cacheBitmap(
														imageDetails.mCustomImageUrl,
														bmp);
											}
											getActivity().runOnUiThread(new Runnable() {
												
												@Override
												public void run() {
													VueTrendingAislesDataModel
													.getInstance(getActivity())
													.dataObserver();
													
												}
											}); 
										
										}

									}
								}).start();

							}
						});
	}

	private void cacheBitmap(String imagePath, Bitmap bmp) {
		FileCache fileCache = new FileCache(getActivity());
		File f = fileCache.getFile(imagePath);
		Log.i("imageurl", "imageurl hashcode " + f.getPath());
		Log.i("imageurl", "imageurl imagepath " + imagePath);
		Utils.saveBitmap(bmp, f);

	}
}
