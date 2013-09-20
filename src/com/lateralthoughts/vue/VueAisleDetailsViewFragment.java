package com.lateralthoughts.vue;

//generic android & java goodies
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.actionbarsherlock.app.SherlockFragment;
import com.flurry.android.FlurryAgent;
import com.lateralthoughts.vue.ui.AisleContentBrowser.AisleDetailSwipeListener;
import com.lateralthoughts.vue.utils.ActionBarHandler;
import com.lateralthoughts.vue.utils.EditTextBackEvent;
import com.lateralthoughts.vue.utils.OnInterceptListener;
import com.lateralthoughts.vue.utils.Utils;

//java utils

//as soon as this fragment attaches to the activity we will go ahead and pull up the
//the list of current trending aisles.
//when the result is received we parse it and coalesce it into an array list of 
//AisleWindowContent objects. At this point we are ready to setup the adapter for the
//mTrendingAislesContentView.

public class VueAisleDetailsViewFragment extends SherlockFragment/* Fragment */{
	private Context mContext;
	public static final String SCREEN_NAME = "DETAILS_SCREEN";
	public static final String SWIPE_LEFT_TO_RIGHT = "LEFT";
	public static final String SWIPE_RIGHT_TO_LEFT = "RIGHT";
	private static final int AISLE_HEADER_SHOW_TIME = 5000;
	private int DOT_MAX_COUUNT = 10;
	private int mDotsCount = 0;
	private int mHighlightPosition;
	private int mTotalPageCount;
	private int mListCount = 5;
	private int mTotalScreenCount;
	private VueContentGateway mVueContentGateway;
	AisleDetailsViewAdapter mAisleDetailsAdapter;
	AisleDetailsSwipeListner mSwipeListener;
	private ActionBarHandler mHandleActionBar;
	private ScaledImageViewFactory mImageViewFactory;
	private ImageView mDetailsAddImageToAisle = null, mAddVueAisle;
	private LoginWarningMessage mLoginWarningMessage = null;
	private View mDetailsContentView = null;
	private ImageView mDotOne, mDotTwo, mDotThree, mDotFour, mDotFive, mDotSix,
			mDotSeven, mDotEight, mDotNine, mDotTen;
	TextView mLeftArrow, mRightArrow, mVueUserName;
	private ListView mAisleDetailsList;
	EditTextBackEvent mEditTextFindAt;
	LinearLayout mDetailsFindAtPopup;

	// TODO: define a public interface that can be implemented by the parent
	// activity so that we can notify it with an ArrayList of AisleWindowContent
	// once we have received the result and parsed it. The idea is that the
	// activity
	// can then initiate a worker in the background to go fetch more content and
	// get
	// ready to launch other activities/fragments within the application

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mContext = activity;
		// adding test comment
		// without much ado lets get started with retrieving the trending aisles
		// list
		mVueContentGateway = VueContentGateway.getInstance();
		if (null == mVueContentGateway) {
			// assert here: this is a no go!
		}
		mSwipeListener = new AisleDetailsSwipeListner();
		mAisleDetailsAdapter = new AisleDetailsViewAdapter(mContext,
				mSwipeListener, mListCount, null);

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// TODO: any particular state that we want to restore?

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mHighlightPosition = VueApplication.getInstance()
				.getmAisleImgCurrentPos();
		mDetailsContentView = inflater.inflate(
				R.layout.aisles_detailed_view_fragment, container, false);
		mTotalPageCount = VueApplication.getInstance().getClickedWindowCount();
		mDetailsFindAtPopup = (LinearLayout) mDetailsContentView
				.findViewById(R.id.detaisl_find_at_popup);
		mDetailsAddImageToAisle = (ImageView) mDetailsContentView
				.findViewById(R.id.details_add_image_to_aisle);
		mEditTextFindAt = (EditTextBackEvent) mDetailsContentView
				.findViewById(R.id.detaisl_find_at_text);
		mEditTextFindAt.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				final InputMethodManager inputMethodManager = (InputMethodManager) getActivity()
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				inputMethodManager.toggleSoftInputFromWindow(
						mEditTextFindAt.getApplicationWindowToken(),
						InputMethodManager.SHOW_FORCED, 0);
				mDetailsFindAtPopup.setVisibility(View.GONE);
				return false;
			}
		});
		mEditTextFindAt.setonInterceptListen(new OnInterceptListener() {

			@Override
			public void setFlag(boolean flag) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onKeyBackPressed() {
				if (mEditTextFindAt.getVisibility() == View.VISIBLE) {
					mDetailsFindAtPopup.setVisibility(View.GONE);
					final InputMethodManager inputMethodManager = (InputMethodManager) getActivity()
							.getSystemService(Context.INPUT_METHOD_SERVICE);
					inputMethodManager.toggleSoftInputFromWindow(
							mEditTextFindAt.getApplicationWindowToken(),
							InputMethodManager.SHOW_FORCED, 0);
				}

			}

			@Override
			public boolean getFlag() {
				// TODO Auto-generated method stub
				return false;
			}
		});

		mDetailsAddImageToAisle.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				FlurryAgent.logEvent("ADD_IMAGE_TO_AISLE_DETAILSVIEW");
				Intent intent = new Intent(getActivity(),
						CreateAisleSelectionActivity.class);
				Utils.putFromDetailsScreenToDataentryCreateAisleScreenPreferenceFlag(
						getActivity(), true);
				intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				Bundle b = new Bundle();
				b.putBoolean(
						VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_FLAG,
						true);
				intent.putExtras(b);
				if (!CreateAisleSelectionActivity.isActivityShowing) {
					CreateAisleSelectionActivity.isActivityShowing = true;
					getActivity()
							.startActivityForResult(
									intent,
									VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_ACTIVITY_RESULT);
				}
			}
		});
		RelativeLayout bottomBar = (RelativeLayout) mDetailsContentView
				.findViewById(R.id.vue_bottom_bar);
		bottomBar.getBackground().setAlpha(25);
		mAddVueAisle = (ImageView) mDetailsContentView
				.findViewById(R.id.vue_aisle);
		mAddVueAisle.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				FlurryAgent.logEvent("FINDAT_DETAILSVIEW");
				final InputMethodManager inputMethodManager = (InputMethodManager) getActivity()
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				inputMethodManager.toggleSoftInputFromWindow(
						mEditTextFindAt.getApplicationWindowToken(),
						InputMethodManager.SHOW_FORCED, 0);
				mDetailsFindAtPopup.setVisibility(View.VISIBLE);
				mEditTextFindAt.requestFocus();

			}
		});

		mAisleDetailsList = (ListView) mDetailsContentView
				.findViewById(R.id.aisle_details_list);
		mAisleDetailsList.setAdapter(mAisleDetailsAdapter);
		mVueUserName = (TextView) mDetailsContentView
				.findViewById(R.id.vue_user_name);
		mVueUserName.setTextSize(Utils.MEDIUM_TEXT_SIZE);
		final LinearLayout dotIndicatorBg = (LinearLayout) mDetailsContentView
				.findViewById(R.id.dot_indicator_bg);
		TextView vueAisleHeading = (TextView) mDetailsContentView
				.findViewById(R.id.vue_aisle_heading);
		vueAisleHeading.setTextSize(Utils.LARGE_TEXT_SIZE);
		mDotOne = (ImageView) mDetailsContentView.findViewById(R.id.one);
		mDotTwo = (ImageView) mDetailsContentView.findViewById(R.id.two);
		mDotThree = (ImageView) mDetailsContentView.findViewById(R.id.three);
		mDotFour = (ImageView) mDetailsContentView.findViewById(R.id.four);
		mDotFive = (ImageView) mDetailsContentView.findViewById(R.id.five);
		mDotSix = (ImageView) mDetailsContentView.findViewById(R.id.six);
		mDotSeven = (ImageView) mDetailsContentView.findViewById(R.id.seven);
		mDotEight = (ImageView) mDetailsContentView.findViewById(R.id.eight);
		mDotNine = (ImageView) mDetailsContentView.findViewById(R.id.nine);
		mDotTen = (ImageView) mDetailsContentView.findViewById(R.id.ten);
		mLeftArrow = (TextView) mDetailsContentView
				.findViewById(R.id.leftarrow);
		mRightArrow = (TextView) mDetailsContentView
				.findViewById(R.id.rightarrow);
		if (mAisleDetailsList != null) {
			mAisleDetailsList.setOnScrollListener(new OnScrollListener() {
				public void onScrollStateChanged(AbsListView view,
						int scrollState) {
					/*
					 * if (scrollState != 0) isScrolling = true; else {
					 * isScrolling = false; ((MyBaseAdapter)
					 * imglist.getAdapter()).notifyDataSetChanged(); }
					 */
					switch (scrollState) {
					case OnScrollListener.SCROLL_STATE_IDLE:
						if (mAisleDetailsList.getChildAt(0).getTop() == 0) {
							Log.i("scrolling",
									"scrolling here scrolling is SCROLL_STATE_IDLE");
							if (mHandleActionBar != null) {
								mHandleActionBar.showActionBar();
							}
						} else if (mAisleDetailsList.getChildAt(0).getTop() <= -30) {

							if (mHandleActionBar != null) {
								mHandleActionBar.hideActionBar();
							}
						}
						break;
					case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:

						break;
					case OnScrollListener.SCROLL_STATE_FLING:

						break;
					default:
						break;
					}
				}

				public void onScroll(AbsListView view, int firstVisibleItem,
						int visibleItemCount, int totalItemCount) {

				}
			});
		}
		mAisleDetailsList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				if (arg2 != 0 && arg2 != 1) {
					if (mListCount - 1 == arg2) {
						// will be called when press on the enter comment text
						// edit text will be expand
						TextView v = (TextView) arg1
								.findViewById(R.id.vue_user_entercomment);
						EditText vueEdt = (EditText) arg1
								.findViewById(R.id.edtcomment);
						vueEdt.setVisibility(View.VISIBLE);
						vueEdt.setFocusable(true);
						mAisleDetailsAdapter.notifyDataSetChanged();

					} else {
						mAisleDetailsAdapter.closeKeyboard();
						// will be called when press on the user comment,
						// comment text will be expand and collapse for
						// alternative clicks
						TextView v = (TextView) arg1
								.findViewById(R.id.vue_user_comment);
						int x = v.getLineCount();
						if (x == 2) {
							LinearLayout.LayoutParams params;
							params = new LinearLayout.LayoutParams(
									LayoutParams.MATCH_PARENT,
									LayoutParams.WRAP_CONTENT);
							// get the pixel equivalent to given dp value
							int leftMargin = VueApplication.getInstance()
									.getPixel(8);
							int rightMargin = VueApplication.getInstance()
									.getPixel(14);
							int topBottomMargin = VueApplication.getInstance()
									.getPixel(6);
							params.setMargins(leftMargin, topBottomMargin,
									rightMargin, topBottomMargin);
							v.setLayoutParams(params);
							v.setMaxLines(Integer.MAX_VALUE);
						} else {
							v.setMaxLines(2);
						}
					}
				} else if (arg2 == 0) {
					mAisleDetailsAdapter.closeKeyboard();
					// will be called when press on the description, description
					// text will be expand and collapse for
					// alternative clicks
					// get the pixel equivalent to given dp value
					int leftRightMargin = VueApplication.getInstance()
							.getPixel(8);
					int topBottomMargin = VueApplication.getInstance()
							.getPixel(6);
					TextView v = (TextView) arg1
							.findViewById(R.id.vue_details_descreption);
					int x = v.getLineCount();
					if (x == 3) {
						LinearLayout.LayoutParams params;
						params = new LinearLayout.LayoutParams(
								LayoutParams.MATCH_PARENT,
								LayoutParams.WRAP_CONTENT);
						params.setMargins(leftRightMargin, topBottomMargin,
								leftRightMargin, topBottomMargin);
						v.setMaxLines(Integer.MAX_VALUE);
					} else {
						 
						v.setMaxLines(3);
					}
				}
			}
		});
		dotIndicatorBg.getBackground().setAlpha(50);
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO need to invisible this view in a smooth way
				dotIndicatorBg.setVisibility(View.GONE);
			}
		}, AISLE_HEADER_SHOW_TIME);
		RelativeLayout vueShareLayout = (RelativeLayout) mDetailsContentView
				.findViewById(R.id.vuesharelayout);
		vueShareLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				FlurryAgent.logEvent("SHARE_AISLE_DETAILSVIEW");
				mAisleDetailsAdapter.share(getActivity(), getActivity());
			}
		});
		mAisleDetailsAdapter.notifyDataSetChanged();
		return mDetailsContentView;
	}

	@Override
	public void onResume() {
		super.onResume();
		// setMaxIndiCount();
		upDatePageDots(mHighlightPosition, "right");
		mAisleDetailsAdapter.notifyDataSetChanged();
		ViewTreeObserver vto = mVueUserName.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

			@Override
			public void onGlobalLayout() {
				mVueUserName.getViewTreeObserver()
						.removeGlobalOnLayoutListener(this);
				mVueUserName.setText(mAisleDetailsAdapter.mVueusername);
			}
		});
	}

	/**
	 * 
	 * 
	 * while swiping the views inside the AisleContentWindow onAisleSwipe method
	 * will be called to indicate the current position of the image.
	 */
	private class AisleDetailsSwipeListner implements AisleDetailSwipeListener {

		@Override
		public void onAisleSwipe(String direction, int position) {
			upDatePageDots(position, direction);
		}

		public void onReceiveImageCount(int count) {
			mTotalScreenCount = count;
		}

		@Override
		public void onResetAdapter() {
			mAisleDetailsAdapter = new AisleDetailsViewAdapter(mContext,
					mSwipeListener, mListCount, null);
			mAisleDetailsList.setAdapter(mAisleDetailsAdapter);
		}

		/**
		 * when user enters the comment it will be added to the comment list at
		 * the top.
		 */
		@Override
		public void onAddCommentClick(final RelativeLayout view,
				final EditText editText, final ImageView sendComment,
				final FrameLayout edtCommentLay) {
			mAisleDetailsList
					.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
			mAisleDetailsList.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					// TODO Auto-generated method stub
					editText.getParent().requestDisallowInterceptTouchEvent(
							false);
					return false;
				}
			});
			editText.setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					v.getParent().requestDisallowInterceptTouchEvent(true);
					return false;
				}
			});
			view.setVisibility(View.GONE);
			final InputMethodManager inputMethodManager = (InputMethodManager) getActivity()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			inputMethodManager.toggleSoftInputFromWindow(
					editText.getApplicationWindowToken(),
					InputMethodManager.SHOW_FORCED, 0);
			
			editText.requestFocus();
			final InputMethodManager mInputMethodManager = (InputMethodManager) getActivity()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			mInputMethodManager.showSoftInput(editText, 0);
			
			
			edtCommentLay.setVisibility(View.VISIBLE);
			editText.setVisibility(View.VISIBLE);
			editText.setCursorVisible(true);
			editText.setTextColor(Color.parseColor(getResources().getString(
					R.color.black)));
			editText.requestFocus();

			((EditTextBackEvent) editText)
					.setonInterceptListen(new OnInterceptListener() {

						@Override
						public void setFlag(boolean flag) {

						}

						@Override
						public void onKeyBackPressed() {
						 
							
							mInputMethodManager.hideSoftInputFromWindow(
									editText.getWindowToken(), 0);
							
							
							editText.setText("");
							edtCommentLay.setVisibility(View.GONE);
							view.setVisibility(View.VISIBLE);
							mAisleDetailsAdapter.notifyDataSetChanged();

						}

						@Override
						public boolean getFlag() {
							return false;
						}
					});
			editText.setFocusable(true);
			editText.setImeOptions(EditorInfo.IME_ACTION_GO);
			editText.setScroller(new Scroller(getActivity()));
			editText.setVerticalScrollBarEnabled(true);
			editText.setMovementMethod(new ScrollingMovementMethod());
			sendComment.setVisibility(View.GONE);
			sendComment.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					FlurryAgent.logEvent("ADD_COMMENTS_DETAILSVIEW");
					String etText = editText.getText().toString();

					if (etText != null && etText.length() >= 1) {
						if (mAisleDetailsAdapter.checkLimitForLoginDialog()) {
							if (mLoginWarningMessage == null) {
								mLoginWarningMessage = new LoginWarningMessage(
										mContext);
							}
							mLoginWarningMessage
									.showLoginWarningMessageDialog(
											"You need to Login with the app to Comment.",
											true, false, 0, null, null);
						} else {
							// Updating Comments Count in Preference to show
							// LoginDialog.
							SharedPreferences sharedPreferencesObj = getActivity()
									.getSharedPreferences(
											VueConstants.SHAREDPREFERENCE_NAME,
											0);
							int commentsCount = sharedPreferencesObj.getInt(
									VueConstants.COMMENTS_COUNT_IN_PREFERENCES,
									0);
							boolean isUserLoggedInFlag = sharedPreferencesObj
									.getBoolean(VueConstants.VUE_LOGIN, false);
							if (commentsCount == 8 && !isUserLoggedInFlag) {
								if (mLoginWarningMessage == null) {
									mLoginWarningMessage = new LoginWarningMessage(
											getActivity());
								}
								mLoginWarningMessage
										.showLoginWarningMessageDialog(
												"You have 2 aisle left to comment without logging in.",
												false, false, 8, editText, view);
							} else if (commentsCount == 9
									&& !isUserLoggedInFlag) {
								if (mLoginWarningMessage == null) {
									mLoginWarningMessage = new LoginWarningMessage(
											getActivity());
								}
								mLoginWarningMessage
										.showLoginWarningMessageDialog(
												"You have 1 aisle left to comment without logging in.",
												false, false, 9, editText, view);
							} else {
								SharedPreferences.Editor editor = sharedPreferencesObj
										.edit();
								editor.putInt(
										VueConstants.COMMENTS_COUNT_IN_PREFERENCES,
										commentsCount + 1);
								editor.commit();
								addComment(editText, view);
							}
						}
					}
				}
			});
			editText.addTextChangedListener(new TextWatcher() {

				@Override
				public void onTextChanged(CharSequence s, int start,
						int before, int count) {
					if (s != null && s.length() >= 1) {
						sendComment.setVisibility(View.VISIBLE);
					} else {
						sendComment.setVisibility(View.GONE);
					}
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {

				}

				@Override
				public void afterTextChanged(Editable s) {
					if (s != null && s.length() >= 1) {
						sendComment.setVisibility(View.VISIBLE);
					} else {
						sendComment.setVisibility(View.GONE);
					}
				}
			});
		}

		@Override
		public void onDissAllowListResponse() {
			mAisleDetailsList.requestDisallowInterceptTouchEvent(true);
		}

		@Override
		public void onAllowListResponse() {
			mAisleDetailsList.requestDisallowInterceptTouchEvent(false);
		}
	}

	public void addComment(EditText editText, RelativeLayout view) {
		String etText = editText.getText().toString().trim();
		InputMethodManager inputMethodManager = (InputMethodManager) getActivity()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.toggleSoftInputFromWindow(
				editText.getApplicationWindowToken(),
				InputMethodManager.SHOW_FORCED, 0);
		@SuppressWarnings("unchecked")
		ArrayList<String> commentList = (ArrayList<String>) mAisleDetailsAdapter.mCommentsMapList
				.get(mAisleDetailsAdapter.mCurrentDispImageIndex);
		if (commentList != null) {
			commentList.add(0, etText);
		}
		mAisleDetailsAdapter.sendDataToDb(
				mAisleDetailsAdapter.mCurrentDispImageIndex,
				mAisleDetailsAdapter.CHANGE_COMMENT);
		mAisleDetailsAdapter.mShowingList = commentList;
		editText.setVisibility(View.GONE);
		editText.setText("");
		view.setVisibility(View.VISIBLE);
		mAisleDetailsAdapter.notifyDataSetChanged();
	}

	protected MotionEvent mLastOnDownEvent = null;

	public void changeLikeCount(int position, String clickType) {
		mAisleDetailsAdapter.changeLikesCountFromCopmareScreen(position,
				clickType);
	}

	public void setActionBarHander(ActionBarHandler handleActionBar) {
		mHandleActionBar = handleActionBar;
	}

	public void setAisleContentListenerNull() {
		mAisleDetailsAdapter.setAisleBrowserObjectsNull();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mDetailsContentView = null;
		mAisleDetailsAdapter = null;
		mContext = null;

	}

	@Override
	public void onDestroy() {
		mAisleDetailsAdapter = null;
		super.onDestroy();
	}

	public void addAisleToWindow(Bitmap bitmap, String imgUrl) {
		mTotalScreenCount = VueApplication.getInstance()
				.getClickedWindowCount();
		VueApplication.getInstance().setClickedWindowCount(
				mTotalScreenCount + 1);
		mTotalScreenCount = mTotalScreenCount + 1;
		upDatePageDots(0, "right");
		mAisleDetailsAdapter.addAisleToContentWindow(bitmap, imgUrl, "title");

	}

	public AisleContext getAisleContext() {

		return mAisleDetailsAdapter.getAisleContext();

	}

	private void showLeftArrow() {
		mLeftArrow.setVisibility(View.VISIBLE);
	}

	private void showRightArrow() {
		mRightArrow.setVisibility(View.VISIBLE);
	}

	private void disableLeftArrow() {
		mLeftArrow.setVisibility(View.GONE);
	}

	private void disableRightArrow() {
		mRightArrow.setVisibility(View.GONE);
	}

	public ArrayList<AisleImageDetails> getAisleWindowImgList() {
		return mAisleDetailsAdapter.getImageList();
	}

	private void upDatePageDots(int currentPosition, String direction) {

		mHighlightPosition = currentPosition % 10;
		hightLightCurrentPage(mHighlightPosition);
		mTotalPageCount = VueApplication.getInstance().getClickedWindowCount();
		if (mTotalPageCount > DOT_MAX_COUUNT) {

			if (currentPosition < DOT_MAX_COUUNT) {

				mDotsCount = DOT_MAX_COUUNT;
				showDots(mDotsCount);
				disableLeftArrow();
				showRightArrow();

			} else {
				showLeftArrow();
				int remainingDots = mTotalPageCount - currentPosition;
				if (currentPosition % 10 == 0) {
					if (remainingDots > DOT_MAX_COUUNT) {
						showRightArrow();
						showDots(DOT_MAX_COUUNT);
					} else {
						disableRightArrow();
						if (remainingDots == 0) {
							showDots(1);
						} else {
							showDots(remainingDots);
						}
					}
				} else {
					// Swiping from left to right...
					if (direction.equalsIgnoreCase(SWIPE_LEFT_TO_RIGHT)) {
						if (mRightArrow.getVisibility() == View.VISIBLE) {
							showRightArrow();
							showDots(DOT_MAX_COUUNT);
						} else {
							disableRightArrow();
							if (mTotalPageCount % 10 == 0) {
								showDots(10);
							} else {
								showDots(mTotalPageCount % 10);
							}
						}
					} else if (direction.equalsIgnoreCase(SWIPE_RIGHT_TO_LEFT)) {
						if ((currentPosition + 1) % 10 == 0) {
							showDots(DOT_MAX_COUUNT);
							if (remainingDots > 1) {
								showRightArrow();
							}
						} else {
							if (mRightArrow.getVisibility() == View.VISIBLE) {
								showRightArrow();
								showDots(DOT_MAX_COUUNT);
							} else {
								disableRightArrow();
								if (mTotalPageCount % 10 == 0) {
									showDots(10);
								} else {
									showDots(mTotalPageCount % 10);
								}
							}
						}
					}
				}
			}

		} else {
			disableLeftArrow();
			disableRightArrow();
			showDots(mTotalPageCount);
		}

	}

	private void hightLightCurrentPage(int position) {
		if (position == 0) {
			mDotOne.setImageResource(R.drawable.number_active);
		} else {
			mDotOne.setImageResource(R.drawable.number_inactive);
		}
		if (position == 1) {
			mDotTwo.setImageResource(R.drawable.number_active);
		} else {
			mDotTwo.setImageResource(R.drawable.number_inactive);
		}
		if (position == 2) {
			mDotThree.setImageResource(R.drawable.number_active);
		} else {
			mDotThree.setImageResource(R.drawable.number_inactive);
		}
		if (position == 3) {
			mDotFour.setImageResource(R.drawable.number_active);
		} else {
			mDotFour.setImageResource(R.drawable.number_inactive);
		}
		if (position == 4) {
			mDotFive.setImageResource(R.drawable.number_active);
		} else {
			mDotFive.setImageResource(R.drawable.number_inactive);
		}
		if (position == 5) {
			mDotSix.setImageResource(R.drawable.number_active);
		} else {
			mDotSix.setImageResource(R.drawable.number_inactive);
		}
		if (position == 6) {
			mDotSeven.setImageResource(R.drawable.number_active);
		} else {
			mDotSeven.setImageResource(R.drawable.number_inactive);
		}
		if (position == 7) {
			mDotEight.setImageResource(R.drawable.number_active);
		} else {
			mDotEight.setImageResource(R.drawable.number_inactive);
		}
		if (position == 8) {
			mDotNine.setImageResource(R.drawable.number_active);
		} else {
			mDotNine.setImageResource(R.drawable.number_inactive);
		}
		if (position == 9) {
			mDotTen.setImageResource(R.drawable.number_active);
		} else {
			mDotTen.setImageResource(R.drawable.number_inactive);
		}

	}

	private void showDots(int dotsCount) {
		for (int i = 0; i < dotsCount; i++) {
			if (i == 0) {
				mDotOne.setVisibility(View.VISIBLE);
			} else if (i == 1) {
				mDotTwo.setVisibility(View.VISIBLE);
			} else if (i == 2) {
				mDotThree.setVisibility(View.VISIBLE);
			} else if (i == 3) {
				mDotFour.setVisibility(View.VISIBLE);
			} else if (i == 4) {
				mDotFive.setVisibility(View.VISIBLE);
			} else if (i == 5) {
				mDotSix.setVisibility(View.VISIBLE);
			} else if (i == 6) {
				mDotSeven.setVisibility(View.VISIBLE);
			} else if (i == 7) {
				mDotEight.setVisibility(View.VISIBLE);
			} else if (i == 8) {
				mDotNine.setVisibility(View.VISIBLE);
			} else if (i == 9) {
				mDotTen.setVisibility(View.VISIBLE);
			}
		}
		for (int i = dotsCount; i < DOT_MAX_COUUNT; i++) {
			if (i == 0) {
				mDotOne.setVisibility(View.GONE);
			} else if (i == 1) {
				mDotTwo.setVisibility(View.GONE);
			} else if (i == 2) {
				mDotThree.setVisibility(View.GONE);
			} else if (i == 3) {
				mDotFour.setVisibility(View.GONE);
			} else if (i == 4) {
				mDotFive.setVisibility(View.GONE);
			} else if (i == 5) {
				mDotSix.setVisibility(View.GONE);
			} else if (i == 6) {
				mDotSeven.setVisibility(View.GONE);
			} else if (i == 7) {
				mDotEight.setVisibility(View.GONE);
			} else if (i == 8) {
				mDotNine.setVisibility(View.GONE);
			} else if (i == 9) {
				mDotTen.setVisibility(View.GONE);
			}
		}

	}

}
