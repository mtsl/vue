package com.lateralthoughts.vue.ui;

//android imports
import com.lateralthoughts.vue.AisleLoader;
import com.lateralthoughts.vue.AisleWindowContent;
import com.lateralthoughts.vue.R;
import com.lateralthoughts.vue.IAisleContentAdapter;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

//android UI & graphics imports
import android.widget.Toast;
import android.widget.ViewAnimator;
import android.widget.ViewFlipper;

import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class AisleContentBrowser extends ViewFlipper {
    private String mAisleUniqueId;
    private int mScrollIndex;
    private AisleContentTouchListener mCustomTouchListener;
    private Context mContext;
    
    public boolean mAnimationInProgress;
    private int mDebugTapCount = 0;
    private long mDownPressStartTime = 0;
    private final int MAX_ELAPSED_DURATION_FOR_TAP = 200;
    public static final int SWIPE_MIN_DISTANCE = 30;
    private AisleLoader mLoader;
    private IAisleContentAdapter mSpecialNeedsAdapter;
    private Animation mCantWrapRight;
    private Animation mCantWrapLeft;
    
    public int mFirstX;
    public int mLastX;
    public int mFirstY;
    public int mLastY;
    
	public AisleContentBrowser(Context context){
		super(context);
		mContext = context;
		mAisleUniqueId = AisleWindowContent.EMPTY_AISLE_CONTENT_ID;
		mScrollIndex = 0;
		mCustomTouchListener = new AisleContentTouchListener();
		mCantWrapRight = AnimationUtils.loadAnimation(mContext, R.anim.cant_wrap_right);
		mCantWrapLeft = AnimationUtils.loadAnimation(mContext, R.anim.cant_wrap_left);
		this.setOnTouchListener(mCustomTouchListener);
	}
	
	public AisleContentBrowser(Context context, AttributeSet attribs){
		super(context, attribs);
		mAisleUniqueId = AisleWindowContent.EMPTY_AISLE_CONTENT_ID;
		mScrollIndex = 0;
		mAnimationInProgress = false;
		mContext = context;
		mCustomTouchListener = new AisleContentTouchListener();
		this.setOnTouchListener(mCustomTouchListener);
		mLoader = AisleLoader.getInstance(mContext);
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
	
	class AisleContentTouchListener implements View.OnTouchListener {
	    
	    @Override
	    public boolean onTouch(View v, MotionEvent event){
	        final AisleContentBrowser aisleContentBrowser = (AisleContentBrowser)v;
	        if (event.getAction() == MotionEvent.ACTION_DOWN) {
	            mAnimationInProgress= false;
	            mFirstX = (int) event.getX();
	            mFirstY = (int)event.getY();
	        }else if(event.getAction() == MotionEvent.ACTION_UP){
	            long tapElapsed = System.currentTimeMillis() - mDownPressStartTime;
	        }

	        if(event.getAction() == MotionEvent.ACTION_MOVE){
	            mLastX = (int)event.getX();
	            mLastY = (int)event.getY();
	            if(mFirstY - mLastY > SWIPE_MIN_DISTANCE ||
	                    mLastY - mFirstY > SWIPE_MIN_DISTANCE){
	                return false;
	            }

	            /*if(1 == aisleContentBrowser.getChildCount()){
	                mLoader.fillAisleContentBrowser(aisleContentBrowser.getUniqueId());
                }*/

	            if (mFirstX - mLastX > SWIPE_MIN_DISTANCE) {
	                
	                //In this case, the user is moving the finger right to left
	                //The current image needs to slide out left and the "next" image
	                //needs to fade in
	                requestDisallowInterceptTouchEvent(true);
	                if(false == mAnimationInProgress){
	                    int currentIndex = aisleContentBrowser.indexOfChild(aisleContentBrowser.getCurrentView());
	                    ScaleImageView nextView = (ScaleImageView)aisleContentBrowser.getChildAt(currentIndex+1);
	                    
	                    if(null != mSpecialNeedsAdapter && null == nextView){
	                        if(null == nextView){
	                            Log.e("AisleContentAdapter","we want the next image which isn't available. currentIndex = " + currentIndex);
	                        }else{
	                            Log.e("AisleContentAdapter","Hey look! We are trying to go to the next view and " +
	                            		"    it looks like an imageview is already present there? currentIndex = " + currentIndex);
	                        }
	                        if(!mSpecialNeedsAdapter.setAisleContent(AisleContentBrowser.this, nextView, currentIndex, currentIndex+1, true)){
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
	                            return false;
	                        }
	                    }
	                    Animation currentGoLeft = AnimationUtils.loadAnimation(mContext, R.anim.right_out);
	                    final Animation nextFadeIn = AnimationUtils.loadAnimation(mContext, R.anim.fade_in);
	                    mAnimationInProgress = true;
	                    aisleContentBrowser.setInAnimation(nextFadeIn);
	                    aisleContentBrowser.setOutAnimation(currentGoLeft);
	                    aisleContentBrowser.showNext();
	                    return true;
	                }                           
	            } else if (mLastX - mFirstX > SWIPE_MIN_DISTANCE){
	                requestDisallowInterceptTouchEvent(true);
	                if(false == mAnimationInProgress){
	                       int currentIndex = aisleContentBrowser.indexOfChild(aisleContentBrowser.getCurrentView());
	                       ScaleImageView nextView = (ScaleImageView)aisleContentBrowser.getChildAt(currentIndex-1);
	                        
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
	                                return false;
	                            }
	                        }
	                    Animation currentGoRight = AnimationUtils.loadAnimation(mContext, R.anim.left_in);
	                    final Animation nextFadeIn = AnimationUtils.loadAnimation(mContext, R.anim.fade_in);
	                    mAnimationInProgress = true;
	                    aisleContentBrowser.setInAnimation(nextFadeIn);
	                    aisleContentBrowser.setOutAnimation(currentGoRight);
	                    aisleContentBrowser.showPrevious();
	                    return true;
	                }
	            }
	            
	            if (event.getAction() == MotionEvent.ACTION_UP) {
	                mAnimationInProgress = false;                       
	            }
	            return true;
	        }
	        return true;
	    }
	}
	
	public void setCustomAdapter(IAisleContentAdapter adapter){
	    mSpecialNeedsAdapter = adapter;
	}
	
	public IAisleContentAdapter getCustomAdapter(){
	    return mSpecialNeedsAdapter;
	}
}
