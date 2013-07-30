package com.lateralthoughts.vue;

//generic android & java goodies
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
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


public class VueAisleDetailsViewFragment extends SherlockFragment/*Fragment*/ {
  
    private Context mContext;
    public  static final  String  SCREEN_NAME = "DETAILS_SCREEN";
    private static final int AISLE_HEADER_SHOW_TIME = 5000;
    private VueContentGateway mVueContentGateway;
    AisleDetailsViewAdapter mAisleDetailsAdapter;  
    private ListView mAisleDetailsList;
    AisleDetailsSwipeListner mSwipeListener;
    IndicatorView mIndicatorView;
    private int mCurrentScreen;
    private int  mTotalScreenCount ;
    int mListCount =5;
    TextView mVueUserName;
    int mCurentIndPosition;
    static final int MAX_DOTS_TO_SHOW = 3;
    int mPrevPosition;
    private ActionBarHandler mHandleActionBar;
    private ScaledImageViewFactory mImageViewFactory;
    ImageView mAddVueAisle;
    RelativeLayout mVueImageIndicator;
    private ImageView mDetailsAddImageToAisle = null;
    //TODO: define a public interface that can be implemented by the parent
    //activity so that we can notify it with an ArrayList of AisleWindowContent
    //once we have received the result and parsed it. The idea is that the activity
    //can then initiate a worker in the background to go fetch more content and get
    //ready to launch other activities/fragments within the application
    
    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        mContext = activity;
        // adding test comment
        //without much ado lets get started with retrieving the trending aisles list
        mVueContentGateway = VueContentGateway.getInstance();
        if(null == mVueContentGateway){
            //assert here: this is a no go!
        }  
        mSwipeListener = new AisleDetailsSwipeListner();
        mAisleDetailsAdapter = new AisleDetailsViewAdapter(mContext,mSwipeListener,mListCount ,null);
        //mVueDetailsContainer = mAisleDetailsAdapter.prepareDetailsVue();
        
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        //TODO: any particular state that we want to restore?
        
    }
    
    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.aisles_detailed_view_fragment,
				container, false);

		mDetailsAddImageToAisle = (ImageView) v
				.findViewById(R.id.details_add_image_to_aisle);
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
		//RelativeLayout bottomBar = (RelativeLayout)v.findViewById(R.id.vue_bottom_bar);
		//bottomBar.getBackground().setAlpha(75);
		 mImageViewFactory  = ScaledImageViewFactory.getInstance(mContext);
		 mImageViewFactory.clearAllViews();
		 mAddVueAisle = (ImageView) v.findViewById(R.id.vue_aisle);
		 mAddVueAisle.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mTotalScreenCount = VueApplication.getInstance()
						.getClickedWindowCount();
				 VueApplication.getInstance().setClickedWindowCount(mTotalScreenCount+1);
				setIndicatorr();
				mAisleDetailsAdapter.addAisleToContentWindow(null,null,"title");
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
		setIndicatorr();
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
						if(mAisleDetailsList.getChildAt(0).getTop() == 0) {
							Log.i("scrolling", "scrolling here scrolling is SCROLL_STATE_IDLE");
							if(mHandleActionBar != null) {
							mHandleActionBar.showActionBar();
							}
						} else if(mAisleDetailsList.getChildAt(0).getTop() <= -30) {
						 
							if(mHandleActionBar != null) {
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
						//will be called when press on the enter comment text edit text will be expand
						TextView v = (TextView) arg1
								.findViewById(R.id.vue_user_entercomment);
						EditText vueEdt = (EditText) arg1
								.findViewById(R.id.edtcomment);
						vueEdt.setVisibility(View.VISIBLE);
						vueEdt.setFocusable(true);
						mAisleDetailsAdapter.notifyDataSetChanged();

					} else {
						//will be called when press on the user comment, comment text will be expand and collapse for 
						//alternative clicks
						TextView v = (TextView) arg1
								.findViewById(R.id.vue_user_comment);
						int x = v.getLineCount();
						if (x == 2) {
							LinearLayout.LayoutParams params;
							params = new LinearLayout.LayoutParams(
									LayoutParams.MATCH_PARENT,
									LayoutParams.WRAP_CONTENT);
							//get the pixel equivalent to given dp value
							int leftMargin = VueApplication.getInstance().getPixel(16);
							int rightMargin = VueApplication.getInstance().getPixel(28);
							int topBottomMargin = VueApplication.getInstance().getPixel(12);
							params.setMargins(
									leftMargin ,
									topBottomMargin,
									rightMargin,
											topBottomMargin);
							v.setLayoutParams(params);
							v.setMaxLines(Integer.MAX_VALUE);
						} else {
							v.setMaxLines(2);
						}
					}
				} else if (arg2 == 0) {
					//will be called when press on the description, description text will be expand and collapse for 
					//alternative clicks
					//get the pixel equivalent to given dp value
					int leftRightMargin = VueApplication.getInstance().getPixel(16);
					int topBottomMargin = VueApplication.getInstance().getPixel(12);
					TextView v = (TextView) arg1
							.findViewById(R.id.vue_details_descreption);
					int x = v.getLineCount();
					if (x == 3) {
						LinearLayout.LayoutParams params;
						params = new LinearLayout.LayoutParams(
								LayoutParams.MATCH_PARENT,
								LayoutParams.WRAP_CONTENT);
						params.setMargins(leftRightMargin,topBottomMargin,leftRightMargin,topBottomMargin);
						v.setMaxLines(Integer.MAX_VALUE);
					} else {
						v.setMaxLines(3);
					}
				}
			}
		});
		dotIndicatorBg.getBackground().setAlpha(45);
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
       mAisleDetailsAdapter.notifyDataSetChanged();
       ViewTreeObserver vto = mVueUserName.getViewTreeObserver(); 
       vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() { 
           @Override 
           public void onGlobalLayout() { 
               mVueUserName.getViewTreeObserver().removeGlobalOnLayoutListener(this); 
               mVueUserName.setText(mAisleDetailsAdapter.mVueusername);
           } 
       });
    }
    /**
     * 
     *  
     *while swiping the views inside the AisleContentWindow onAisleSwipe method will be
     *called to indicate the current position of the image.
     */
    private class AisleDetailsSwipeListner implements AisleDetailSwipeListener {

      @Override
      public void onAisleSwipe(String direction) {
         mPrevPosition = mCurrentScreen;
         mIndicatorView.switchToScreen(mPrevPosition,checkScreenBoundaries(direction,mCurrentScreen)
                );
      }
       public void onReceiveImageCount(int count) {
          mTotalScreenCount = count;
       }
      @Override
      public void onResetAdapter() {
      }
      /**
       * when user enters the comment it will be added to the comment list at the top.
       */
      @Override
      public void onAddCommentClick(final RelativeLayout view, final EditText editText,final ImageView sendComment,final FrameLayout edtCommentLay) {
         mAisleDetailsList.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
         mAisleDetailsList.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				editText.getParent().requestDisallowInterceptTouchEvent(false);
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
          final InputMethodManager inputMethodManager=(InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
             inputMethodManager.toggleSoftInputFromWindow(editText.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
             edtCommentLay.setVisibility(View.VISIBLE);
          editText.setVisibility(View.VISIBLE);
          editText.setCursorVisible(true);
          editText.setTextColor(Color.parseColor(getResources().getString(R.color.black)));
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
					String etText = editText.getText().toString();

					if (etText != null && etText.length() >= 1) {
						etText = etText.trim();
						inputMethodManager.toggleSoftInputFromWindow(
								editText.getApplicationWindowToken(),
								InputMethodManager.SHOW_FORCED, 0);

					
						@SuppressWarnings("unchecked")
						ArrayList<String> commentList = (ArrayList<String>) mAisleDetailsAdapter.mCommentsMapList
								.get(mAisleDetailsAdapter.mCurrentDispImageIndex);
						commentList.add(0, etText);
						mAisleDetailsAdapter.sendDataToDb(mAisleDetailsAdapter.mCurrentDispImageIndex,mAisleDetailsAdapter.CHANGE_COMMENT);
						
						// Updating Comments Count in Preference to show
						// LoginDialog.
						SharedPreferences sharedPreferencesObj = getActivity()
								.getSharedPreferences(
										VueConstants.SHAREDPREFERENCE_NAME, 0);
						int commentsCount = sharedPreferencesObj.getInt(
								VueConstants.COMMENTS_COUNT_IN_PREFERENCES, 0);
						SharedPreferences.Editor editor = sharedPreferencesObj
								.edit();
						editor.putInt(
								VueConstants.COMMENTS_COUNT_IN_PREFERENCES,
								commentsCount++);
						editor.commit();
						//mAisleDetailsAdapter.mTempComments2 = new String[commentList.size() + 1];
					/*	
						mAisleDetailsAdapter.mTempComments2 = commentList
								.toArray(mAisleDetailsAdapter.mTempComments2);*/
						mAisleDetailsAdapter.mShowingList = commentList;
						editText.setVisibility(View.GONE);
						editText.setText("");
						view.setVisibility(View.VISIBLE);
						mAisleDetailsAdapter.notifyDataSetChanged();
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
    private int checkScreenBoundaries(String direction,int mCurrentScreen){
        if(direction.equalsIgnoreCase("left")) {
           if(mCurrentScreen == mTotalScreenCount){
              this.mCurrentScreen = mTotalScreenCount;
              return this.mCurrentScreen;
           } else if(mCurrentScreen < mTotalScreenCount){
              this.mCurrentScreen++;
              return this.mCurrentScreen;
           }
        } else {
             if(direction.equalsIgnoreCase("right")) {
                if(mCurrentScreen == 1){
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
    private int getHighlightPosition(int cur_pos) {
       mCurentIndPosition = cur_pos;
       int highlightPosition;
       if(mCurentIndPosition <= mTotalScreenCount) {
          if(mCurentIndPosition+MAX_DOTS_TO_SHOW > mTotalScreenCount) {
             int temp = mTotalScreenCount - mCurentIndPosition;
               highlightPosition = MAX_DOTS_TO_SHOW - temp;
          } else {
             int temp = mCurentIndPosition % MAX_DOTS_TO_SHOW;
             highlightPosition = temp;
          }
          return highlightPosition;
       }
      return mCurentIndPosition;
    }
    public void changeLikeCount(int position,String clickType) {
    	mAisleDetailsAdapter.changeLikesCountFromCopmareScreen(position,clickType);
    }
    public void setActionBarHander(ActionBarHandler handleActionBar) {
    	mHandleActionBar = handleActionBar;
    }
 
    public void setAisleContentListenerNull() {
    	mAisleDetailsAdapter.setAisleBrowserObjectsNull();
    }
    private void setIndicatorr() {
    	RelativeLayout.LayoutParams relParams = new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		// relParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
		relParams.addRule(RelativeLayout.CENTER_VERTICAL);
		mVueImageIndicator.removeAllViews();
		mVueImageIndicator.addView(mIndicatorView);
		mTotalScreenCount = VueApplication.getInstance()
				.getClickedWindowCount();
		mIndicatorView.setNumberofScreens(mTotalScreenCount);
		mIndicatorView.setDrawables(R.drawable.number_active,
				R.drawable.bullets_bg, R.drawable.number_inactive);
		mCurrentScreen = 1;
		int indicatorLeftMargin = ((VueApplication.getInstance().getScreenWidth()* 95)/100)
				/ 2 - mIndicatorView.getIndicatorBgWidht() / 2;
		relParams.setMargins(indicatorLeftMargin, 0, 0, 0);
		mIndicatorView.setLayoutParams(relParams);
		mIndicatorView.switchToScreen(mCurrentScreen, mCurrentScreen);
    }
    @Override
    public void onDestroy() {
    	mAisleDetailsAdapter = null;
    	super.onDestroy();
    }
    private void moveIndicatorDot(String swipeDerection) {
    	int indicatorPos = 0;
    	int currentImagePos;
    	int MAX_INDI_COUNT = 0;
    	
    	if(indicatorPos == 0 && swipeDerection.equalsIgnoreCase("left")) {
    		
    	} else if(indicatorPos != 0 && swipeDerection.equalsIgnoreCase("left")) {
    		
    	} else if(indicatorPos == 0 && swipeDerection.equalsIgnoreCase("right")) {
    		
    	} else if(indicatorPos == MAX_INDI_COUNT && swipeDerection.equalsIgnoreCase("right")) {
    		
    	} else if(indicatorPos == MAX_INDI_COUNT && swipeDerection.equalsIgnoreCase("left")) {
    		
    	}
    }
}
