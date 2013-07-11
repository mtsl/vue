package com.lateralthoughts.vue.ui;

//android imports
import com.lateralthoughts.vue.AisleDetailsViewActivity;
import com.lateralthoughts.vue.AisleWindowContent;
import com.lateralthoughts.vue.IAisleContentAdapter;
import com.lateralthoughts.vue.R;
import com.lateralthoughts.vue.VueApplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;

//android UI & graphics imports
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ViewFlipper;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.GestureDetector;

public class AisleContentBrowser extends ViewFlipper {
    private String mAisleUniqueId;
    private int mScrollIndex;
    //private AisleContentTouchListener mCustomTouchListener;
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
    public String getHolderName() {
		return holderName;
	}

	public void setHolderName(String holderName) {
		this.holderName = holderName;
	}
	private GestureDetector mDetector;
    
	public AisleContentBrowser(Context context){
		super(context);
		mContext = context;
		mAisleUniqueId = AisleWindowContent.EMPTY_AISLE_CONTENT_ID;
		mScrollIndex = 0;
	}
	
	public AisleContentBrowser(Context context, AttributeSet attribs){
		super(context, attribs);
		mAisleUniqueId = AisleWindowContent.EMPTY_AISLE_CONTENT_ID;
		mScrollIndex = 0;
		mAnimationInProgress = false;
		mContext = context;
	    this.setOnClickListener(new OnClickListener(){
	            @Override
	            public void onClick(View v){
	                //Intent intent = new Intent();
	                //intent.setClass(VueApplication.getInstance(), AisleDetailsViewActivity.class);
	                //callOnClick();
	                //mContext.startActivity(intent);
  	            }           
	        });
	    mTapTimeout = ViewConfiguration.getTapTimeout();
		this.setBackgroundColor(Color.WHITE);
		mDetector = new GestureDetector(AisleContentBrowser.this.getContext(), new mListener());
	}
	
	public void setUniqueId(String id){
		mAisleUniqueId = id;
	}
	
	public String getUniqueId(){
		return mAisleUniqueId;
	}
	
	public void setScrollIndex(int scrollIndex){
		mScrollIndex = scrollIndex;
	}
	
	public int getScrollIndex(){
		return mScrollIndex;
	}
	
	@Override
	public void onAnimationEnd(){
	    super.onAnimationEnd();
	    Log.e("AisleContentAdapter","onAnimationEnd is called!");
	}

	@Override
	public boolean onTouchEvent(MotionEvent event){
	        final AisleContentBrowser aisleContentBrowser = (AisleContentBrowser)this;
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
	                    int currentIndex = aisleContentBrowser.indexOfChild(aisleContentBrowser.getCurrentView());
	                    ScaleImageView nextView = (ScaleImageView)aisleContentBrowser.getChildAt(currentIndex+1);
	                    if(mSwipeListener != null) {
                        	mSwipeListener.onAisleSwipe("Left");
                        }
	                    if(null != mSpecialNeedsAdapter && null == nextView){
	                        if(!mSpecialNeedsAdapter.setAisleContent(AisleContentBrowser.this, null, currentIndex, currentIndex+1, true)){
	                            mAnimationInProgress = true;
	                            Animation cantWrapRight = AnimationUtils.loadAnimation(mContext, R.anim.cant_wrap_right);
	                            cantWrapRight.setAnimationListener(new Animation.AnimationListener(){
	                                public void onAnimationEnd(Animation animation) {
	                                    Animation cantWrapRightPart2 = AnimationUtils.loadAnimation(mContext, R.anim.cant_wrap_right2);
	                                    aisleContentBrowser.getCurrentView().startAnimation(cantWrapRightPart2);
	                                  
	                                 
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
	                mTouchMoved = true;
	                if(false == mAnimationInProgress){
	                       int currentIndex = aisleContentBrowser.indexOfChild(aisleContentBrowser.getCurrentView());
	                       ScaleImageView nextView = (ScaleImageView)aisleContentBrowser.getChildAt(currentIndex-1);
	                       if(mSwipeListener != null) {
                           	mSwipeListener.onAisleSwipe("Right");
                           }
	                        if(null != mSpecialNeedsAdapter && null == nextView){
	                            if(!mSpecialNeedsAdapter.setAisleContent(AisleContentBrowser.this, nextView, currentIndex, currentIndex-1, true)){
	                                Animation cantWrapLeft = AnimationUtils.loadAnimation(mContext, R.anim.cant_wrap_left);
	                                
	                                cantWrapLeft.setAnimationListener(new Animation.AnimationListener(){
	                                    public void onAnimationEnd(Animation animation) {
	                                        Animation cantWrapLeftPart2 = AnimationUtils.loadAnimation(mContext, R.anim.cant_wrap_left2);
	                                        aisleContentBrowser.getCurrentView().startAnimation(cantWrapLeftPart2);
	                                       
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
	
	public void setCustomAdapter(IAisleContentAdapter adapter){
	    mSpecialNeedsAdapter = adapter;
	}
	
	public IAisleContentAdapter getCustomAdapter(){
	    return mSpecialNeedsAdapter;
	}
	
	class mListener extends GestureDetector.SimpleOnGestureListener {
	    @Override
	    public boolean onDown(MotionEvent e) {
	        return true;
	    }
	    
	    @Override
	    public boolean onSingleTapConfirmed(MotionEvent event){
	        Log.e("Vinodh Clicks","ok...we are getting item clicks!!");
	        if(mClickListener != null && null != mSpecialNeedsAdapter) {
	        mClickListener.onAisleClicked(mAisleUniqueId,mSpecialNeedsAdapter.getAisleItemsCount());
	          
	        }
	        if(detailImgClickListenr != null && null != mSpecialNeedsAdapter) {
	        	detailImgClickListenr.onImageClicked();
	        }
	        
	        return true;
	    }
	    @Override
	    public void onLongPress(MotionEvent e) {
	    	  if(detailImgClickListenr != null && null != mSpecialNeedsAdapter) {
		        	detailImgClickListenr.onImageLongPress();
		        }
	    	super.onLongPress(e);
	    }
	 }
	
	public interface AisleContentClickListener{
	    public void onAisleClicked(String id,int count);
	}
	public interface DetailClickListener{
	    public void onImageClicked();
	    public void onImageLongPress();
	}
	DetailClickListener detailImgClickListenr;
	public  void setDetailImageClickListener(DetailClickListener detailLestener) {
		detailImgClickListenr = detailLestener;
	}
	public void setAisleContentClickListener(AisleContentClickListener listener){
	    mClickListener = listener;
	}
	public interface AisleDetailSwipeListener{
	    public void onAisleSwipe(String id);
	    public void onReceiveImageCount(int count);
	    public void onResetAdapter();
	    public void onAddCommentClick(TextView view,EditText editText);
	}
	public void setAisleDetailSwipeListener(AisleDetailSwipeListener swipListener) {
		mSwipeListener = swipListener; 
	}
	private AisleContentClickListener mClickListener;
	public AisleDetailSwipeListener mSwipeListener;
}
