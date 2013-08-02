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
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.lateralthoughts.vue.indicators.IndicatorView;
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
	private static final int AISLE_HEADER_SHOW_TIME = 5000;
	public static final String SWIPE_LEFT_TO_RIGHT = "LEFT";
	public static final String SWIPE_RIGHT_TO_LEFT = "RIGHT";
	private VueContentGateway mVueContentGateway;
	AisleDetailsViewAdapter mAisleDetailsAdapter;
	private ListView mAisleDetailsList;
	AisleDetailsSwipeListner mSwipeListener;
	IndicatorView mIndicatorView;
	private int mCurrentScreen;
	private int mTotalScreenCount;
	int mListCount = 5;
	TextView mVueUserName;
	int mCurentIndPosition;
	static final int MAX_DOTS_TO_SHOW_LIMIT = 5;
	int mPrevPosition;
	private ActionBarHandler mHandleActionBar;
	private ScaledImageViewFactory mImageViewFactory;
	ImageView mAddVueAisle;
	RelativeLayout mVueImageIndicator;
	private ImageView mDetailsAddImageToAisle = null;
	int mDotIndicatorPos = 1;
	int mCurrentImagePos = 1;
	int MAX_INDI_COUNT = 0;
	int TOTAL_IMAGE_COUNT = 0;
	LinearLayout mDetailsFindAtPopup;
	EditTextBackEvent mEditTextFindAt;
	private LoginWarningMessage mLoginWarningMessage = null;

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
		// mVueDetailsContainer = mAisleDetailsAdapter.prepareDetailsVue();

	}
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// TODO: any particular state that we want to restore?

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.aisles_detailed_view_fragment,
				container, false);
		mDetailsFindAtPopup = (LinearLayout) v
				.findViewById(R.id.detaisl_find_at_popup);
		mDetailsAddImageToAisle = (ImageView) v
				.findViewById(R.id.details_add_image_to_aisle);
		mEditTextFindAt = (EditTextBackEvent) v
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
				Intent intent = new Intent(getActivity(),
						CreateAisleSelectionActivity.class);
				Bundle b = new Bundle();
				b.putBoolean(
						VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_FLAG,
						true);
				intent.putExtras(b);
				getActivity()
						.startActivityForResult(
								intent,
								VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_ACTIVITY_RESULT);
			}
		});
		RelativeLayout bottomBar = (RelativeLayout) v
				.findViewById(R.id.vue_bottom_bar);
		bottomBar.getBackground().setAlpha(25);
		mImageViewFactory = ScaledImageViewFactory.getInstance(mContext);
		mImageViewFactory.clearAllViews();
		mAddVueAisle = (ImageView) v.findViewById(R.id.vue_aisle);
		mAddVueAisle.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				final InputMethodManager inputMethodManager = (InputMethodManager) getActivity()
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				inputMethodManager.toggleSoftInputFromWindow(
						mEditTextFindAt.getApplicationWindowToken(),
						InputMethodManager.SHOW_FORCED, 0);
				mDetailsFindAtPopup.setVisibility(View.VISIBLE);
				mEditTextFindAt.requestFocus();

			}
		});

		mAisleDetailsList = (ListView) v.findViewById(R.id.aisle_details_list);
		mAisleDetailsList.setAdapter(mAisleDetailsAdapter);
		mVueUserName = (TextView) v.findViewById(R.id.vue_user_name);
		mVueUserName.setTextSize(Utils.MEDIUM_TEXT_SIZE);
		final LinearLayout dotIndicatorBg = (LinearLayout) v
				.findViewById(R.id.dot_indicator_bg);
		TextView vueAisleHeading = (TextView) v
				.findViewById(R.id.vue_aisle_heading);
		vueAisleHeading.setTextSize(Utils.LARGE_TEXT_SIZE);
		mVueImageIndicator = (RelativeLayout) v
				.findViewById(R.id.vue_image_indicator);
		mIndicatorView = new IndicatorView(getActivity());
		mIndicatorView.setId(1234);
		setIndicator();
		if(mAisleDetailsList != null ) {
 
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
		RelativeLayout vueShareLayout = (RelativeLayout) v
				.findViewById(R.id.vuesharelayout);
		vueShareLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				mAisleDetailsAdapter.share(getActivity(), getActivity());
			}
		});
		mAisleDetailsAdapter.notifyDataSetChanged();
		return v;
	}

	@Override
	public void onResume() {
		super.onResume();
		setMaxIndiCount();
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
		public void onAisleSwipe(String direction) {
			/*
			 * mPrevPosition = mCurrentScreen;
			 * mIndicatorView.switchToScreen(mPrevPosition
			 * ,checkScreenBoundaries(direction,mCurrentScreen));
			 */
			moveIndicatorDot(direction);
		}

		public void onReceiveImageCount(int count) {
			mTotalScreenCount = count;
		}

		@Override
		public void onResetAdapter() {
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
							inputMethodManager.toggleSoftInputFromWindow(
									editText.getApplicationWindowToken(),
									InputMethodManager.SHOW_FORCED, 0);
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
					Log.e("clicked", "1");
					String etText = editText.getText().toString();

					if (etText != null && etText.length() >= 1) {
						Log.e("clicked", "2");
						if (mAisleDetailsAdapter.checkLimitForLoginDialog()) {
							Log.e("clicked", "3");
							if (mLoginWarningMessage == null) {
								mLoginWarningMessage = new LoginWarningMessage(
										mContext);
							}
							mLoginWarningMessage
									.showLoginWarningMessageDialog(
											"You need to Login with the app to Comment.",
											true, false, 0, null, null);
						} else {
							Log.e("clicked", "4");
							// Updating Comments Count in Preference to show
							// LoginDialog.
							SharedPreferences sharedPreferencesObj = getActivity()
									.getSharedPreferences(
											VueConstants.SHAREDPREFERENCE_NAME,
											0);
							int commentsCount = sharedPreferencesObj.getInt(
									VueConstants.COMMENTS_COUNT_IN_PREFERENCES,
									0);
							Log.e("clicked", "5" + commentsCount);
							if (commentsCount == 8) {
								if (mLoginWarningMessage == null) {
									mLoginWarningMessage = new LoginWarningMessage(
											getActivity());
								}
								mLoginWarningMessage
										.showLoginWarningMessageDialog(
												"You have 2 aisle left to comment without logging in.",
												false, false, 8, editText, view);
							} else if (commentsCount == 9) {
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
		commentList.add(0, etText);
		mAisleDetailsAdapter.sendDataToDb(
				mAisleDetailsAdapter.mCurrentDispImageIndex,
				mAisleDetailsAdapter.CHANGE_COMMENT);
		mAisleDetailsAdapter.mShowingList = commentList;
		editText.setVisibility(View.GONE);
		editText.setText("");
		view.setVisibility(View.VISIBLE);
		mAisleDetailsAdapter.notifyDataSetChanged();
	}

	private int checkScreenBoundaries(String direction, int mCurrentScreen) {
		if (direction.equalsIgnoreCase("left")) {
			if (mCurrentScreen == mTotalScreenCount) {
				this.mCurrentScreen = mTotalScreenCount;
				return this.mCurrentScreen;
			} else if (mCurrentScreen < mTotalScreenCount) {
				this.mCurrentScreen++;
				return this.mCurrentScreen;
			}
		} else {
			if (direction.equalsIgnoreCase("right")) {
				if (mCurrentScreen == 1) {
					this.mCurrentScreen = 1;
					return this.mCurrentScreen;
				} else {
					this.mCurrentScreen--;
					return this.mCurrentScreen;
				}
			}
		}
		return mCurrentScreen;
	}

	protected MotionEvent mLastOnDownEvent = null;
    public void changeLikeCount(int position,String clickType) {
    	mAisleDetailsAdapter.changeLikesCountFromCopmareScreen(position,clickType);
    }
    public void setActionBarHander(ActionBarHandler handleActionBar) {
    	mHandleActionBar = handleActionBar;
    }
 
    public void setAisleContentListenerNull() {
    	mAisleDetailsAdapter.setAisleBrowserObjectsNull();
    }
    private void setIndicator() {
    	RelativeLayout.LayoutParams relParams = new RelativeLayout.LayoutParams(
 
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		// relParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
		// relParams.addRule(RelativeLayout.CENTER_IN_PARENT);
		mVueImageIndicator.removeAllViews();
		mVueImageIndicator.addView(mIndicatorView);
		mTotalScreenCount = VueApplication.getInstance()
				.getClickedWindowCount();
		// mIndicatorView.setNumberofScreens(mTotalScreenCount);
		setMaxIndiCount();
		mIndicatorView.setDrawables(R.drawable.number_active,
				R.drawable.bullets_bg, R.drawable.number_inactive);
		mCurrentScreen = 1;
		int indicatorLeftMargin = ((VueApplication.getInstance()
				.getScreenWidth() * 98) / 100)
				/ 2
				- mIndicatorView.getIndicatorBgWidht() / 2;
		relParams.setMargins(indicatorLeftMargin, 0, 0, 0);
		mIndicatorView.setLayoutParams(relParams);
		mIndicatorView.switchToScreen(mCurrentScreen, mCurrentScreen);
		mDotIndicatorPos = 1;
		mCurrentImagePos = 1;
	}

	@Override
	public void onDestroy() {
		mAisleDetailsAdapter = null;
		super.onDestroy();
	}

	public void addAisleToWindow(Bitmap bitmap, String imgUrl) {
		mTotalScreenCount = VueApplication.getInstance()
				.getClickedWindowCount();
		 VueApplication.getInstance().setClickedWindowCount(mTotalScreenCount+1);
		 setMaxIndiCount();
		setIndicator();
		mAisleDetailsAdapter.addAisleToContentWindow(bitmap,imgUrl,"title");
    }
    public AisleContext getAisleContext(){
    	
    	return mAisleDetailsAdapter.getAisleContext();
    	
    }
    private void moveIndicatorDot(String swipeDerection) {
    	if(mDotIndicatorPos == 2 && mCurrentImagePos > 2 && swipeDerection.equalsIgnoreCase(SWIPE_RIGHT_TO_LEFT)) {
    		//when the indicator is in 2 position and more images on left side so
    		//so stop the indicator at posion 2 until image count reach first.
    		if(mCurrentImagePos > 2){
    			showLeftArrow();
    			mCurrentImagePos -= 1;
    		}else {
    			//nothing to do.
    		}
    	} else if(mDotIndicatorPos != 0 && swipeDerection.equalsIgnoreCase(SWIPE_RIGHT_TO_LEFT)) {
    	/*	if(mCurrentImagePos > mDotIndicatorPos) {
    			  showLeftArrow();
    		} else {
    			disableLeftArrow();
    		}*/
    		if(mDotIndicatorPos != 1){
    		moveDotLeft(mDotIndicatorPos);
    		mCurrentImagePos -= 1;
    		}
    	
    		
    	}else if(((mDotIndicatorPos+1) == MAX_INDI_COUNT )&& (TOTAL_IMAGE_COUNT - mCurrentImagePos)>1&& swipeDerection.equalsIgnoreCase(SWIPE_LEFT_TO_RIGHT))  {
    		//when the indicator is in 2 from last position and more images on right side so
    		// stop the indicator at posion 2 from last until image count reach last.
    		int remainingImagesCount = TOTAL_IMAGE_COUNT - mCurrentImagePos;
    		if(remainingImagesCount > 0){
    			mCurrentImagePos += 1;
    		}
    	}
    	
    	else if(swipeDerection.equalsIgnoreCase(SWIPE_LEFT_TO_RIGHT)) {
    		int remainingDots = MAX_INDI_COUNT - mDotIndicatorPos;
    		int remainingImagesCount = TOTAL_IMAGE_COUNT - mCurrentImagePos;
    		/*if(remainingImagesCount > remainingDots) {
    			showRightArrow();
    		} else {
    			disableRightArrow();
    		}*/
    		if(remainingDots != 0) {
    			moveDotRight(mDotIndicatorPos);
    			
    		} else {
    			//don't move to right it is at last position.
    		}
    		if(remainingImagesCount > 0){
    			mCurrentImagePos += 1;
    		}
    	} 
    }
    private void showLeftArrow(){
    	//TODO: show the left arrow
    }
    private void showRightArrow() {
    	//TODO: show the right arrow
    }
    private void moveDotLeft(int moveFrom) {
    	mDotIndicatorPos -= 1;
    	mIndicatorView.switchToScreen(moveFrom, mDotIndicatorPos);
    }
    private void moveDotRight(int moveFrom){
    	mDotIndicatorPos += 1;
    	mIndicatorView.switchToScreen(moveFrom, mDotIndicatorPos);
    }
    private void disableLeftArrow(){
    	//TODO: disable the left arrow
    }
    private void disableRightArrow(){
    	//TODO: disable the right arrow
    }
    private void setMaxIndiCount(){
    	TOTAL_IMAGE_COUNT = VueApplication.getInstance().getClickedWindowCount();
    	if(VueApplication.getInstance().getClickedWindowCount() > MAX_DOTS_TO_SHOW_LIMIT) {
    		MAX_INDI_COUNT = MAX_DOTS_TO_SHOW_LIMIT;
    	} else {
    		MAX_INDI_COUNT = VueApplication.getInstance().getClickedWindowCount();
    	}
    	mIndicatorView.setNumberofScreens(MAX_INDI_COUNT);
    }


}
