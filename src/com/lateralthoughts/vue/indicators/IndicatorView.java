package com.lateralthoughts.vue.indicators;

import com.lateralthoughts.vue.R;
import com.lateralthoughts.vue.VueApplication;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;


public class IndicatorView extends LinearLayout {

    private Bitmap mIndicatorBitmap;
    private Bitmap mInactiveIndicatorBitmap;
    private Bitmap mActiveIndicatorBitmap;
    private AnimateDrawable mDrawable;
    Context mContext;

    private int mTotalScreenNo = VueApplication.getInstance().getClickedWindowCount();
      
    private int mCurrentScreen = 1;
    private int mNextScreen = 2;
    private int mIndicatorBitmapWidth,mIndicatorBitmapHeigt;
    private int mIndicatorBgwidth = 50;
 
    Drawable mMovingDot;

	public IndicatorView(Context context) {
		super(context);
		this.mContext = context;
		initialize(context);

	}
 
    @Override
    public void setId(int id) {
        
       super.setId(id);
    }
 
   public void setNumberofScreens(int numberOfScreens) {
      mTotalScreenNo = numberOfScreens;
    
   }
    
	public void setDrawables(int movingDot, int backGround, int inactiveDots) {
		mIndicatorBitmap = BitmapFactory.decodeResource(
				mContext.getResources(), backGround);
		mInactiveIndicatorBitmap = BitmapFactory.decodeResource(
				mContext.getResources(), inactiveDots);
		mIndicatorBgwidth = getIndicatorBgWidht(
				mInactiveIndicatorBitmap.getWidth() * 2, mTotalScreenNo);
		mIndicatorBitmap = getNewBitmap(mIndicatorBitmap, mIndicatorBgwidth);
		mActiveIndicatorBitmap = BitmapFactory.decodeResource(
				mContext.getResources(), movingDot);
		this.mMovingDot = mContext.getResources().getDrawable(movingDot);
		this.mMovingDot.setBounds(0, 0, mActiveIndicatorBitmap.getWidth(),
				mActiveIndicatorBitmap.getHeight());
		setFocusable(true);
		setFocusableInTouchMode(true);
		mIndicatorBitmapWidth = mIndicatorBitmap.getWidth();
		mIndicatorBitmapHeigt = mIndicatorBitmap.getHeight();
		/* this.setBackgroundResource(backGround); */

	}
    
	public void switchToScreen(int sourceScreenNumber, int destScreennumber) {
		mCurrentScreen = sourceScreenNumber;
		mNextScreen = destScreennumber;
		if (mTotalScreenNo == 0) {
			mTotalScreenNo = 4;
		}
		int distance = mIndicatorBitmapWidth / mTotalScreenNo;
		int travelTo = 0;
		int start;
		start = (mCurrentScreen * distance) - (distance / 2)
				- (mActiveIndicatorBitmap.getWidth() / 2);
		if ((mNextScreen == 1 && mCurrentScreen == 1)
				|| (mNextScreen == mTotalScreenNo && mCurrentScreen == mTotalScreenNo))
			travelTo = start;
		else if (mCurrentScreen < mNextScreen)
			travelTo = start + distance;
		else
			travelTo = start - distance;
		Animation an;
		if (mCurrentScreen < mNextScreen)
			an = new TranslateAnimation(start, travelTo, mIndicatorBitmapHeigt
					/ 2 - (mActiveIndicatorBitmap.getHeight() / 2),
					mIndicatorBitmapHeigt / 2
							- (mActiveIndicatorBitmap.getHeight() / 2));
		else
			an = new TranslateAnimation(start, travelTo, mIndicatorBitmapHeigt
					/ 2 - (mActiveIndicatorBitmap.getHeight() / 2),
					mIndicatorBitmapHeigt / 2
							- (mActiveIndicatorBitmap.getHeight() / 2));

		an.setInterpolator(new AccelerateDecelerateInterpolator());
		an.setDuration(300);
		an.setRepeatCount(0);
		an.initialize(0, 0, 100, 100);
		mDrawable = new AnimateDrawable(mMovingDot, an);
		an.startNow();
	}

    public IndicatorView(Context context, AttributeSet attrs) {
     super(context, attrs);
      initialize(context);
    }
    @Override
    protected void onDraw(Canvas canvas) {
    int distance = mIndicatorBitmap.getWidth() / mTotalScreenNo;
    int i =1;
    while(i<=mTotalScreenNo){
    int start = (i * distance) - (distance / 2)
      - (mActiveIndicatorBitmap.getWidth() / 2);
      canvas.drawBitmap(
      mInactiveIndicatorBitmap,
     start,
    mIndicatorBitmapHeigt / 2
    - (mInactiveIndicatorBitmap.getHeight() / 2), null);
    i++;
    }

    mDrawable.draw(canvas);
   invalidate();
    }

    @SuppressWarnings("deprecation")
	private void initialize(Context context) {
		mIndicatorBitmap = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.bullets_bg);
		// indicatorBitmap = getNewBitmap(indicatorBitmap);
		mInactiveIndicatorBitmap = BitmapFactory.decodeResource(
				context.getResources(), R.drawable.number_inactive);
		mActiveIndicatorBitmap = BitmapFactory.decodeResource(
				context.getResources(), R.drawable.number_active);
		mMovingDot = context.getResources().getDrawable(
				R.drawable.number_active);
		mMovingDot.setBounds(0, 0, mActiveIndicatorBitmap.getWidth() / 2,
				mActiveIndicatorBitmap.getHeight() / 2);
		setFocusable(true);
		setFocusableInTouchMode(true);
		// this.setBackgroundResource(R.drawable.bullets_bg);
		mIndicatorBgwidth = getIndicatorBgWidht(
				mActiveIndicatorBitmap.getWidth() * 2, mTotalScreenNo);
		Bitmap newBitmap = getNewBitmap(mIndicatorBitmap, mIndicatorBgwidth);
		Drawable d = getResources().getDrawable(R.drawable.bullets_bg);
		d = new BitmapDrawable(getResources(), mIndicatorBitmap);
		this.setBackgroundDrawable(d);
	}

	private Bitmap getNewBitmap(Bitmap bitmap, int newWidth) {
		return Bitmap.createScaledBitmap(bitmap, newWidth, 20, true);
	}

	private int getIndicatorBgWidht(int eachDotWidht, int totalDots) {
		return eachDotWidht * totalDots;
	}

	public int getIndicatorBgWidht() {
		return mIndicatorBgwidth;
	}
}