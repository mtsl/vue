package com.slidingmenu.lib;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.slidingmenu.lib.SlidingMenu.CanvasTransformer;

public class CustomViewBehind extends ViewGroup {

private static final String TAG = "CustomViewBehind";
//left_rigt margin width
private static final int MARGIN_THRESHOLD =32; //32 dips
//private int menuOpenSiweAloweDistance = 0;
private CustomViewAbove mViewAbove;

private View mContent;
private View mSecondaryContent;
private int mMarginThreshold;
private int mWidthOffset;
private CanvasTransformer mTransformer;
private boolean mChildrenEnabled;
ClosePopup onbackhandle;

public CustomViewBehind(Context context) {
this(context, null);
}

public CustomViewBehind(Context context, AttributeSet attrs) {
super(context, attrs);
mMarginThreshold = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
MARGIN_THRESHOLD, getResources().getDisplayMetrics());
//`menuOpenSiweAloweDistance = convertDpToPixel(10);
}

public void setCustomViewAbove(CustomViewAbove customViewAbove) {
mViewAbove = customViewAbove;
}

public void setCanvasTransformer(CanvasTransformer t) {
mTransformer = t;
}

public void setWidthOffset(int i) {
mWidthOffset = i;
requestLayout();
}

public int getBehindWidth() {
return mContent.getWidth();
}

public void setContent(View v) {
if (mContent != null)
removeView(mContent);
mContent = v;
addView(mContent);
}

public View getContent() {
return mContent;
}

/**
* Sets the secondary (right) menu for use when setMode is called with SlidingMenu.LEFT_RIGHT.
* @param v the right menu
*/
public void setSecondaryContent(View v) {
if (mSecondaryContent != null)
removeView(mSecondaryContent);
mSecondaryContent = v;
addView(mSecondaryContent);
}

public View getSecondaryContent() {
return mSecondaryContent;
}

public void setChildrenEnabled(boolean enabled) {
mChildrenEnabled = enabled;
}

@Override
public void scrollTo(int x, int y) {
super.scrollTo(x, y);
if (mTransformer != null)
invalidate();
}

@Override
public boolean onInterceptTouchEvent(MotionEvent e) {
return false;
}

@Override
public boolean onTouchEvent(MotionEvent e) {
return mChildrenEnabled;
}

@Override
protected void dispatchDraw(Canvas canvas) {
if (mTransformer != null) {
canvas.save();
mTransformer.transformCanvas(canvas, mViewAbove.getPercentOpen());
super.dispatchDraw(canvas);
canvas.restore();
} else
super.dispatchDraw(canvas);
}

@Override
protected void onLayout(boolean changed, int l, int t, int r, int b) {
final int width = r - l;
final int height = b - t;
mContent.layout(0, 0, width-mWidthOffset, height);
if (mSecondaryContent != null)
mSecondaryContent.layout(0, 0, width-mWidthOffset, height);
}

@Override
protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
int width = getDefaultSize(0, widthMeasureSpec);
int height = getDefaultSize(0, heightMeasureSpec);
setMeasuredDimension(width, height);
final int contentWidth = getChildMeasureSpec(widthMeasureSpec, 0, width-mWidthOffset);
final int contentHeight = getChildMeasureSpec(heightMeasureSpec, 0, height);
mContent.measure(contentWidth, contentHeight);
if (mSecondaryContent != null)
mSecondaryContent.measure(contentWidth, contentHeight);
}

private int mMode;
private boolean mFadeEnabled;
private final Paint mFadePaint = new Paint();
private float mScrollScale;
private Drawable mShadowDrawable;
private Drawable mSecondaryShadowDrawable;
private int mShadowWidth;
private float mFadeDegree;

public void setMode(int mode) {
if (mode == SlidingMenu.LEFT || mode == SlidingMenu.RIGHT) {
if (mContent != null)
mContent.setVisibility(View.VISIBLE);
if (mSecondaryContent != null)
mSecondaryContent.setVisibility(View.GONE);
}
mMode = mode;
}

public int getMode() {
return mMode;
}

public void setScrollScale(float scrollScale) {
mScrollScale = scrollScale;
}

public float getScrollScale() {
return mScrollScale;
}

public void setShadowDrawable(Drawable shadow) {
mShadowDrawable = shadow;
invalidate();
}

public void setSecondaryShadowDrawable(Drawable shadow) {
mSecondaryShadowDrawable = shadow;
invalidate();
}

public void setShadowWidth(int width) {
mShadowWidth = width;
invalidate();
}

public void setFadeEnabled(boolean b) {
mFadeEnabled = b;
}

public void setFadeDegree(float degree) {
if (degree > 1.0f || degree < 0.0f)
throw new IllegalStateException("The BehindFadeDegree must be between 0.0f and 1.0f");
mFadeDegree = degree;
}

public int getMenuPage(int page) {
page = (page > 1) ? 2 : ((page < 1) ? 0 : page);
if (mMode == SlidingMenu.LEFT && page > 1) {
return 0;
} else if (mMode == SlidingMenu.RIGHT && page < 1) {
return 2;
} else {
return page;
}
}

public void scrollBehindTo(View content, int x, int y) {
int vis = View.VISIBLE;
if (mMode == SlidingMenu.LEFT) {
if (x >= content.getLeft()) vis = View.GONE;
scrollTo((int)((x + getBehindWidth())*mScrollScale), y);
} else if (mMode == SlidingMenu.RIGHT) {
if (x <= content.getLeft()) vis = View.GONE;
scrollTo((int)(getBehindWidth() - getWidth() +
(x-getBehindWidth())*mScrollScale), y);
} else if (mMode == SlidingMenu.LEFT_RIGHT) {
mContent.setVisibility(x >= content.getLeft() ? View.GONE : View.VISIBLE);
mSecondaryContent.setVisibility(x <= content.getLeft() ? View.GONE : View.VISIBLE);
vis = x == 0 ? View.GONE : View.VISIBLE;
if (x <= content.getLeft()) {
scrollTo((int)((x + getBehindWidth())*mScrollScale), y);
} else {
scrollTo((int)(getBehindWidth() - getWidth() +
(x-getBehindWidth())*mScrollScale), y);
}
}
setVisibility(vis);
}

public int getMenuLeft(View content, int page) {
if (mMode == SlidingMenu.LEFT) {
return content.getLeft() - getBehindWidth();
} else if (mMode == SlidingMenu.RIGHT) {
return content.getLeft() + getBehindWidth();
} else if (mMode == SlidingMenu.LEFT_RIGHT) {
switch (page) {
case 0:
return content.getLeft() - getBehindWidth();
case 2:
return content.getLeft() + getBehindWidth();
}
}
return 0;
}

public int getAbsLeftBound(View content) {
if (mMode == SlidingMenu.LEFT || mMode == SlidingMenu.LEFT_RIGHT) {
return content.getLeft() - getBehindWidth();
} else if (mMode == SlidingMenu.RIGHT) {
return content.getLeft();
}
return 0;
}

public int getAbsRightBound(View content) {
if (mMode == SlidingMenu.LEFT) {
return content.getLeft();
} else if (mMode == SlidingMenu.RIGHT || mMode == SlidingMenu.LEFT_RIGHT) {
return content.getLeft() + getBehindWidth();
}
return 0;
}

public boolean marginTouchAllowed(View content, int x) {
int left = content.getLeft();
int right = content.getRight();
if (mMode == SlidingMenu.LEFT) {
return (x >= left && x <= mMarginThreshold + left);
} else if (mMode == SlidingMenu.RIGHT) {
return (x <= right && x >= right - mMarginThreshold);
} else if (mMode == SlidingMenu.LEFT_RIGHT) {
return (x >= left && x <= mMarginThreshold + left) ||
(x <= right && x >= right - mMarginThreshold);
}
return false;
}

public boolean menuOpenTouchAllowed(View content, int currPage, int x) {
if (mMode == SlidingMenu.LEFT || (mMode == SlidingMenu.LEFT_RIGHT && currPage == 0)) {
	//Log.i("toggle", "togglewhen content.getLeft():  "+content.getLeft());
	//Log.i("toggle", "togglewhen x:  "+content.getLeft());
	//written by raju here menuOpenSiweAloweDistance = 10 while swiping when menu is open to occupy the finger space.
	//actula code is return x>= contetn.getLeft().
return x >= (content.getLeft()/*-menuOpenSiweAloweDistance*/);
} else if (mMode == SlidingMenu.RIGHT || (mMode == SlidingMenu.LEFT_RIGHT && currPage == 2)) {
return x <= content.getRight();
}
return false;
}

public boolean menuClosedSlideAllowed(float dx) {
if (mMode == SlidingMenu.LEFT) {
return dx > 0;
} else if (mMode == SlidingMenu.RIGHT) {
return dx < 0;
} else if (mMode == SlidingMenu.LEFT_RIGHT) {
return true;
}
return false;
}

public boolean menuOpenSlideAllowed(float dx) {
if (mMode == SlidingMenu.LEFT) {
return dx < 0;
} else if (mMode == SlidingMenu.RIGHT) {
return dx > 0;
} else if (mMode == SlidingMenu.LEFT_RIGHT) {
return true;
}
return false;
}

public void drawShadow(View content, Canvas canvas) {
if (mShadowDrawable == null || mShadowWidth <= 0) return;
int left = 0;
if (mMode == SlidingMenu.LEFT) {
left = content.getLeft() - mShadowWidth;
} else if (mMode == SlidingMenu.RIGHT) {
left = content.getRight();
} else if (mMode == SlidingMenu.LEFT_RIGHT) {
if (mSecondaryShadowDrawable != null) {
left = content.getRight();
mSecondaryShadowDrawable.setBounds(left, 0, left + mShadowWidth, getHeight());
mSecondaryShadowDrawable.draw(canvas);
}
left = content.getLeft() - mShadowWidth;
}
mShadowDrawable.setBounds(left, 0, left + mShadowWidth, getHeight());
mShadowDrawable.draw(canvas);
}

public void drawFade(View content, Canvas canvas, float openPercent) {
if (!mFadeEnabled) return;
 
final int alpha = (int) (mFadeDegree * 255 * Math.abs(1-openPercent));
mFadePaint.setColor(Color.argb(alpha, 0, 0, 0));
int left = 0;
int right = 0;
if (mMode == SlidingMenu.LEFT) {
left = content.getLeft() - getBehindWidth();
right = content.getLeft();
} else if (mMode == SlidingMenu.RIGHT) {
left = content.getRight();
right = content.getRight() + getBehindWidth();
} else if (mMode == SlidingMenu.LEFT_RIGHT) {
left = content.getLeft() - getBehindWidth();
right = content.getLeft();
canvas.drawRect(left, 0, right, getHeight(), mFadePaint);
left = content.getRight();
right = content.getRight() + getBehindWidth();
}
canvas.drawRect(left, 0, right, getHeight(), mFadePaint);
}

public void setPopHandler(ClosePopup onbackhandle) {
	this.onbackhandle = onbackhandle;
}
public void closeThePopup() {
	if(onbackhandle != null)
	onbackhandle.closeKeyboardPopup();
}
public int convertDpToPixel(int x) {

Resources r = getResources();
int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, x, r.getDisplayMetrics());
return px;
}
}