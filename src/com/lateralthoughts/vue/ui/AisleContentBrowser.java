package com.lateralthoughts.vue.ui;

//android imports
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ViewFlipper;

import com.lateralthoughts.vue.AisleContext;
import com.lateralthoughts.vue.AisleDetailsViewActivity;
import com.lateralthoughts.vue.AisleWindowContent;
import com.lateralthoughts.vue.IAisleContentAdapter;
import com.lateralthoughts.vue.R;
import com.lateralthoughts.vue.VueAisleDetailsViewFragment;
import com.lateralthoughts.vue.VueApplication;

public class AisleContentBrowser extends ViewFlipper {
	private String mAisleUniqueId;
	private String mSourceName;
	int mCurrentIndex;

	public String getmSourceName() {
		return mSourceName;
	}

	public void setmSourceName(String mSourceName) {
		this.mSourceName = mSourceName;
	}

	private int mScrollIndex;
	// private AisleContentTouchListener mCustomTouchListener;
	private Context mContext;

	public boolean mAnimationInProgress;
	private int mDebugTapCount = 0;
	private long mDownPressStartTime = 0;
	private final int MAX_ELAPSED_DURATION_FOR_TAP = 200;
	public static final int SWIPE_MIN_DISTANCE = 30;
	private IAisleContentAdapter mSpecialNeedsAdapter;

	public int mFirstX;
	public int mLastX;
	public int mFirstY;
	public int mLastY;
	private boolean mTouchMoved;
	private int mTapTimeout;
	private String holderName;
	private String mBrowserArea;
	private boolean mSetPosition;

	public String getmBrowserArea() {
		return mBrowserArea;
	}

	public void setmBrowserArea(String mBrowserArea) {
		this.mBrowserArea = mBrowserArea;
	}

	public String getHolderName() {
		return holderName;
	}

	public void setHolderName(String holderName) {
		this.holderName = holderName;
	}

	private GestureDetector mDetector;

	public AisleContentBrowser(Context context) {
		super(context);
		mContext = context;
		mAisleUniqueId = AisleWindowContent.EMPTY_AISLE_CONTENT_ID;
		mScrollIndex = 0;
	}

	public AisleContentBrowser(Context context, AttributeSet attribs) {
		super(context, attribs);
		mAisleUniqueId = AisleWindowContent.EMPTY_AISLE_CONTENT_ID;
		mScrollIndex = 0;
		mAnimationInProgress = false;
		mContext = context;
		this.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Intent intent = new Intent();
				// intent.setClass(VueApplication.getInstance(),
				// AisleDetailsViewActivity.class);
				// callOnClick();
				// mContext.startActivity(intent);
			}
		});
		mTapTimeout = ViewConfiguration.getTapTimeout();
		this.setBackgroundColor(Color.WHITE);
		mDetector = new GestureDetector(AisleContentBrowser.this.getContext(),
				new mListener());
	}

	public void setUniqueId(String id) {
		mAisleUniqueId = id;
	}

	public String getUniqueId() {
		return mAisleUniqueId;
	}

	public void setScrollIndex(int scrollIndex) {
		mScrollIndex = scrollIndex;

	}

	public int getScrollIndex() {
		return mScrollIndex;
	}

	@Override
	public void onAnimationEnd() {
		super.onAnimationEnd();
		Log.e("AisleContentAdapter", "onAnimationEnd is called!");
	}

	@Override
 
	public boolean onTouchEvent(MotionEvent event){
	        final AisleContentBrowser aisleContentBrowser = (AisleContentBrowser)this;
	      /*  if(aisleContentBrowser.getCurrentView() == null) {
    		    return false;
	        }*/
	        boolean result = mDetector.onTouchEvent(event);
	        if (event.getAction() == MotionEvent.ACTION_DOWN) {
	            mAnimationInProgress= false;
	            mFirstX = (int) event.getX();
	            mFirstY = (int)event.getY();
	            mDownPressStartTime = System.currentTimeMillis();
	            return super.onTouchEvent(event);
	        }else if(event.getAction() == MotionEvent.ACTION_UP){
	            //long elapsedTimeFromDown = System.currentTimeMillis() - mDownPressStartTime;
	            if(mTouchMoved){
	                mTouchMoved = false;
	                return true;
	            }
	            mAnimationInProgress = false;
	            
	            mFirstX = 0;
	            mLastX = 0;
	            return super.onTouchEvent(event);
	        }

	        else if(event.getAction() == MotionEvent.ACTION_MOVE){	
	        	Log.i("browsermove", "browsermove1");
	            mLastX = (int)event.getX();
	            mLastY = (int)event.getY();
	            if(mFirstY - mLastY > SWIPE_MIN_DISTANCE ||
	                    mLastY - mFirstY > SWIPE_MIN_DISTANCE){
	                return super.onTouchEvent(event);
	            }
                 
	            if (mFirstX - mLastX > SWIPE_MIN_DISTANCE) {
	                //In this case, the user is moving the finger right to left
	                //The current image needs to slide out left and the "next" image
	                //needs to fade in
	                mTouchMoved = true;
	                requestDisallowInterceptTouchEvent(true);
	                if(false == mAnimationInProgress){
	                	View nextView = null;
	                    final int currentIndex = aisleContentBrowser.indexOfChild(aisleContentBrowser.getCurrentView());
	                    	  nextView = (ScaleImageView)aisleContentBrowser.getChildAt(currentIndex+1);
	                    	  
	                
	                   // if((currentIndex+1)>=0 && (currentIndex+1) < aisleContentBrowser.getChildCount() )
	                  
	                    if(null != mSpecialNeedsAdapter && null == nextView){
	                    	
	                        if(!mSpecialNeedsAdapter.setAisleContent(AisleContentBrowser.this,currentIndex, currentIndex+1, true)){
	                            mAnimationInProgress = true;
	                          
	                            Animation cantWrapRight = AnimationUtils.loadAnimation(mContext, R.anim.cant_wrap_right);
	                            cantWrapRight.setAnimationListener(new Animation.AnimationListener(){
	                                public void onAnimationEnd(Animation animation) {
	                                    Animation cantWrapRightPart2 = AnimationUtils.loadAnimation(mContext, R.anim.cant_wrap_right2);
	                                    aisleContentBrowser.getCurrentView().startAnimation(cantWrapRightPart2);
	                                    if(mSwipeListener != null) {
                                          	 
                                           	mSwipeListener.onAllowListResponse();
                                           }
	                                  
	                                 
	                                }
	                                public void onAnimationStart(Animation animation) {

	                                }
	                                public void onAnimationRepeat(Animation animation) {

	                                }
	                            });
	                            aisleContentBrowser.getCurrentView().startAnimation(cantWrapRight);
	                            return super.onTouchEvent(event);
	                        }
	                    }
	                  
	                    Animation currentGoLeft = AnimationUtils.loadAnimation(mContext, R.anim.right_out);
	                    final Animation nextFadeIn = AnimationUtils.loadAnimation(mContext, R.anim.fade_in);
	                    mAnimationInProgress = true;
	                    aisleContentBrowser.setInAnimation(nextFadeIn);
	                    aisleContentBrowser.setOutAnimation(currentGoLeft);
	                    currentGoLeft.setAnimationListener(new Animation.AnimationListener(){
                            public void onAnimationEnd(Animation animation) {
                                Log.e("AisleContentAdapter","End of go left animtation");
                                if(mSwipeListener != null) {
                                  	 
                                   	mSwipeListener.onAllowListResponse();
                                   }
                                if(mSwipeListener != null) {
                                	mSwipeListener.onAisleSwipe(VueAisleDetailsViewFragment.SWIPE_LEFT_TO_RIGHT,currentIndex+1);
                                	//mSwipeListener.onDissAllowListResponse();
                                }
        	                    mCurrentIndex = currentIndex+1;
        	                    if(detailImgClickListenr != null) {
        		                    detailImgClickListenr.onImageSwipe(currentIndex+1);
        		                    }
        	                   // aisleContentBrowser.setDisplayedChild(currentIndex+1);
                            }
                            public void onAnimationStart(Animation animation) {

                            }
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
	                   
	                    aisleContentBrowser.setDisplayedChild(currentIndex+1);
	                    //aisleContentBrowser.invalidate();
	                    return super.onTouchEvent(event);
	                }                           
	            } else if (mLastX - mFirstX > SWIPE_MIN_DISTANCE){
	                requestDisallowInterceptTouchEvent(true);
	                Log.i("browsermove", "browsermove1 right");
	                mTouchMoved = true;
	                if(false == mAnimationInProgress){
	                       final int currentIndex = aisleContentBrowser.indexOfChild(aisleContentBrowser.getCurrentView());
	                       View nextView = null;
		                    	  nextView = (ScaleImageView)aisleContentBrowser.getChildAt(currentIndex-1);
	                    
	                       
	                      // if((currentIndex-1)>=0 && (currentIndex-1) < aisleContentBrowser.getChildCount() )
	                      
	                        if(null != mSpecialNeedsAdapter && null == nextView){
	                        	
	                            if(!mSpecialNeedsAdapter.setAisleContent(AisleContentBrowser.this,currentIndex, currentIndex-1, true)){
	                            	
	                                Animation cantWrapLeft = AnimationUtils.loadAnimation(mContext, R.anim.cant_wrap_left);
	                                
	                                cantWrapLeft.setAnimationListener(new Animation.AnimationListener(){
	                                    public void onAnimationEnd(Animation animation) {
	                                        Animation cantWrapLeftPart2 = AnimationUtils.loadAnimation(mContext, R.anim.cant_wrap_left2);
	                                        aisleContentBrowser.getCurrentView().startAnimation(cantWrapLeftPart2);
	                                        if(mSwipeListener != null) {
	                                           	 
	                                           	mSwipeListener.onAllowListResponse();
	                                           }
	                                    }
	                                    public void onAnimationStart(Animation animation) {

	                                    }
	                                    public void onAnimationRepeat(Animation animation) {

	                                    }
	                                });
	                                aisleContentBrowser.getCurrentView().startAnimation(cantWrapLeft);
	                                return super.onTouchEvent(event);
	                            }
	                        }
	                   
	                    Animation currentGoRight = AnimationUtils.loadAnimation(mContext, R.anim.left_in);
	                    final Animation nextFadeIn = AnimationUtils.loadAnimation(mContext, R.anim.fade_in);
	                    mAnimationInProgress = true;
	                    aisleContentBrowser.setInAnimation(nextFadeIn);
	                    aisleContentBrowser.setOutAnimation(currentGoRight);
	                    currentGoRight.setAnimationListener(new Animation.AnimationListener(){
                            public void onAnimationEnd(Animation animation) {
                                Log.e("AisleContentAdapter","End of go right animtation");
                                if(mSwipeListener != null) {
                                  	 
                                   	mSwipeListener.onAllowListResponse();
                                   }
                                mCurrentIndex = currentIndex-1;
    	                        if(mSwipeListener != null) {
    	                           	mSwipeListener.onAisleSwipe(VueAisleDetailsViewFragment.SWIPE_RIGHT_TO_LEFT,currentIndex-1);
    	                          // 	mSwipeListener.onDissAllowListResponse();
    	                           }
    	                        if(detailImgClickListenr != null) {
    	 	                       detailImgClickListenr.onImageSwipe(currentIndex-1);
    	 	                       }
    	                       // aisleContentBrowser.setDisplayedChild(currentIndex-1);
                            }
                            public void onAnimationStart(Animation animation) {

                            }
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
	                    aisleContentBrowser.setDisplayedChild(currentIndex-1);
	                   
	                    return super.onTouchEvent(event);
	                }
	            }
	        }
	        return super.onTouchEvent(event);
	    }
	//}
	public void setCurrentImage(){
		for(int i=0;i<VueApplication.getInstance().getmAisleImgCurrentPos();i++) {
		 mSpecialNeedsAdapter.setAisleContent(AisleContentBrowser.this,i, i+1, true);
		}
		final AisleContentBrowser aisleContentBrowser = (AisleContentBrowser)this;
		  aisleContentBrowser.setDisplayedChild(VueApplication.getInstance().getmAisleImgCurrentPos());
 
	}

	// }
 

	public void setCustomAdapter(IAisleContentAdapter adapter) {
		mSpecialNeedsAdapter = adapter;
	}

	public IAisleContentAdapter getCustomAdapter() {
		return mSpecialNeedsAdapter;
	}

	class mListener extends GestureDetector.SimpleOnGestureListener {
		@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}

		@Override
		public boolean onDoubleTap(MotionEvent e) {
			if (detailImgClickListenr != null && null != mSpecialNeedsAdapter) {
				detailImgClickListenr.onImageDoubleTap();
			}
			return super.onDoubleTap(e);
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent event) {
			if (mClickListener != null && null != mSpecialNeedsAdapter) {
				mClickListener.onAisleClicked(mAisleUniqueId,
						mSpecialNeedsAdapter.getAisleItemsCount(),
						mCurrentIndex);

			}
			if (detailImgClickListenr != null && null != mSpecialNeedsAdapter) {
				detailImgClickListenr.onSetBrowserArea(getmBrowserArea());
				detailImgClickListenr.onImageClicked();
			}

			return true;
		}

		@Override
		public void onLongPress(MotionEvent e) {
			if (detailImgClickListenr != null && null != mSpecialNeedsAdapter) {
				detailImgClickListenr.onSetBrowserArea(getmBrowserArea());
				detailImgClickListenr.onImageLongPress();
			}
			super.onLongPress(e);
		}
	}

	public interface AisleContentClickListener {
		public void onAisleClicked(String id, int count, int currentPosition);

		public boolean isFlingCalled();

		public boolean isIdelState();
	}

	public interface DetailClickListener {
		public void onImageClicked();

		public void onImageLongPress();

		public void onImageSwipe(int position);

		public void onImageDoubleTap();

		public void onSetBrowserArea(String area);
	}

	DetailClickListener detailImgClickListenr;

	public void setDetailImageClickListener(DetailClickListener detailLestener) {
		detailImgClickListenr = detailLestener;
	}

	public void setAisleContentClickListener(AisleContentClickListener listener) {
		mClickListener = listener;
	}

	public interface AisleDetailSwipeListener {
		public void onAisleSwipe(String id, int position);

		public void onReceiveImageCount(int count);

		public void onResetAdapter();

		public void onAddCommentClick(RelativeLayout view, EditText editText,
				ImageView commentSend, FrameLayout editLay);

		public void onDissAllowListResponse();

		public void onAllowListResponse();

		public void setFindAtText(String findAt);

		public void setOccasion(String occasion);
	}

	public void setAisleDetailSwipeListener(
			AisleDetailSwipeListener swipListener) {
		mSwipeListener = swipListener;
	}

	public void setReferedObjectsNull() {
		// To relese the refrence objects form the browser to avoid the memory
		// leaks.
		mSwipeListener = null;
		mClickListener = null;
		detailImgClickListenr = null;
	}

	private AisleContentClickListener mClickListener;
	public AisleDetailSwipeListener mSwipeListener;

}
