package com.lateralthoughts.vue.ui;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.Interpolator;
import android.view.animation.LayoutAnimationController;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;

import com.lateralthoughts.vue.CreateAisleSelectionActivity;
import com.lateralthoughts.vue.utils.Utils;

public class ArcLayout extends ViewGroup {
	/**
	 * children will be set the same size.
	 */
	public static int CHILD_SIZE = 48;

	private int mChildPadding = 5;

	private int mLayoutPadding = 10;

	public static final float DEFAULT_FROM_DEGREES = -90.0f;

	public static final float DEFAULT_TO_DEGREES = -378.0f;

	private float mFromDegrees = DEFAULT_FROM_DEGREES;

	private float mToDegrees = DEFAULT_TO_DEGREES;

	private static int MIN_RADIUS = 76;

	/* the distance between the layout's center and any child's center */
	private int mRadius;

	public boolean mExpanded = false;

	private Context mContext;

	public ArcLayout(Context context) {
		super(context);
	}

	public ArcLayout(Context context, AttributeSet attrs) {
		super(context, attrs);

		mContext = context;

		CHILD_SIZE = 48;
		MIN_RADIUS = 76;
		mChildPadding = 5;
		mLayoutPadding = 10;

		CHILD_SIZE = (int) Utils.dipToPixels(context, CHILD_SIZE);
		MIN_RADIUS = (int) Utils.dipToPixels(context, MIN_RADIUS);
		mChildPadding = (int) Utils.dipToPixels(context, mChildPadding);
		mLayoutPadding = (int) Utils.dipToPixels(context, mLayoutPadding);

		if (attrs != null) {
			/*
			 * TypedArray a = getContext().obtainStyledAttributes(attrs,
			 * R.styleable.ArcLayout, 0, 0);
			 */
			mFromDegrees = DEFAULT_FROM_DEGREES;
			mToDegrees = DEFAULT_TO_DEGREES;

			// a.recycle();
		}
	}

	private static int computeRadius(final float arcDegrees,
			final int childCount, final int childSize, final int childPadding,
			final int minRadius) {
		if (childCount < 2) {
			return minRadius;
		}

		final float perDegrees = arcDegrees / (childCount - 1);
		final float perHalfDegrees = perDegrees / 2;
		final int perSize = childSize + childPadding;

		final int radius = (int) ((perSize / 2) / Math.sin(Math
				.toRadians(perHalfDegrees)));

		return Math.max(radius, minRadius);
	}

	private static Rect computeChildFrame(final int centerX, final int centerY,
			final int radius, final float degrees, final int size) {

		final double childCenterX = centerX + radius
				* Math.cos(Math.toRadians(degrees));
		final double childCenterY = centerY + radius
				* Math.sin(Math.toRadians(degrees));

		return new Rect((int) (childCenterX - size / 2),
				(int) (childCenterY - size / 2),
				(int) (childCenterX + size / 2),
				(int) (childCenterY + size / 2));
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		final int radius = mRadius = computeRadius(
				Math.abs(mToDegrees - mFromDegrees), getChildCount(),
				CHILD_SIZE, mChildPadding, MIN_RADIUS);
		final int size = radius * 2 + CHILD_SIZE + mChildPadding
				+ mLayoutPadding * 2;

		setMeasuredDimension(size, size);

		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			getChildAt(i)
					.measure(
							MeasureSpec.makeMeasureSpec(CHILD_SIZE,
									MeasureSpec.EXACTLY),
							MeasureSpec.makeMeasureSpec(CHILD_SIZE,
									MeasureSpec.EXACTLY));
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		final int centerX = getWidth() / 2;
		final int centerY = getHeight() / 2;
		final int radius = mExpanded ? mRadius : 0;

		final int childCount = getChildCount();
		final float perDegrees = (mToDegrees - mFromDegrees) / (childCount - 1);

		float degrees = mFromDegrees;
		for (int i = 0; i < childCount; i++) {
			Rect frame = computeChildFrame(centerX, centerY, radius, degrees,
					CHILD_SIZE);
			degrees += perDegrees;
			getChildAt(i).layout(frame.left, frame.top, frame.right,
					frame.bottom);
		}
	}

	/**
	 * refers to {@link LayoutAnimationController#getDelayForView(View view)}
	 */
	private static long computeStartOffset(final int childCount,
			final boolean expanded, final int index, final float delayPercent,
			final long duration, Interpolator interpolator) {
		final float delay = delayPercent * duration;
		final long viewDelay = (long) (getTransformedIndex(expanded,
				childCount, index) * delay);
		final float totalDelay = delay * childCount;

		float normalizedDelay = viewDelay / totalDelay;
		normalizedDelay = interpolator.getInterpolation(normalizedDelay);

		return (long) (normalizedDelay * totalDelay);
	}

	private static int getTransformedIndex(final boolean expanded,
			final int count, final int index) {
		if (expanded) {
			return count - 1 - index;
		}

		return index;
	}

	private static Animation createExpandAnimation(float fromXDelta,
			float toXDelta, float fromYDelta, float toYDelta, long startOffset,
			long duration, Interpolator interpolator) {
		Animation animation = new RotateAndTranslateAnimation(0, toXDelta, 0,
				toYDelta, 0, 720);
		animation.setStartOffset(startOffset);
		animation.setDuration(duration);
		animation.setInterpolator(interpolator);
		animation.setFillAfter(true);

		return animation;
	}

	private static Animation createShrinkAnimation(float fromXDelta,
			float toXDelta, float fromYDelta, float toYDelta, long startOffset,
			long duration, Interpolator interpolator) {
		AnimationSet animationSet = new AnimationSet(false);
		animationSet.setFillAfter(true);

		final long preDuration = duration / 2;
		Animation rotateAnimation = new RotateAnimation(0, 360,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		rotateAnimation.setStartOffset(startOffset);
		rotateAnimation.setDuration(preDuration);
		rotateAnimation.setInterpolator(new LinearInterpolator());
		rotateAnimation.setFillAfter(true);

		animationSet.addAnimation(rotateAnimation);

		Animation translateAnimation = new RotateAndTranslateAnimation(0,
				toXDelta, 0, toYDelta, 360, 720);
		translateAnimation.setStartOffset(startOffset + preDuration);
		translateAnimation.setDuration(duration - preDuration);
		translateAnimation.setInterpolator(interpolator);
		translateAnimation.setFillAfter(true);

		animationSet.addAnimation(translateAnimation);

		return animationSet;
	}

	private void bindChildAnimation(final View child, final int index,
			final long duration, final boolean isExpand) {
		final boolean expanded = mExpanded;
		final int centerX = getWidth() / 2;
		final int centerY = getHeight() / 2;
		final int radius = expanded ? 0 : mRadius;

		final int childCount = getChildCount();
		final float perDegrees = (mToDegrees - mFromDegrees) / (childCount - 1);
		Rect frame = computeChildFrame(centerX, centerY, radius, mFromDegrees
				+ index * perDegrees, CHILD_SIZE);

		final int toXDelta = frame.left - child.getLeft();
		final int toYDelta = frame.top - child.getTop();

		Interpolator interpolator = mExpanded ? new AccelerateInterpolator()
				: new OvershootInterpolator(1.5f);
		final long startOffset = computeStartOffset(childCount, mExpanded,
				index, 0.1f, duration, interpolator);

		Animation animation = mExpanded ? createShrinkAnimation(0, toXDelta, 0,
				toYDelta, startOffset, duration, interpolator)
				: createExpandAnimation(0, toXDelta, 0, toYDelta, startOffset,
						duration, interpolator);

		final boolean isLast = getTransformedIndex(expanded, childCount, index) == childCount - 1;
		animation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				if (isLast) {
					postDelayed(new Runnable() {

						@Override
						public void run() {
							onAllAnimationsEnd(isExpand);
						}
					}, 0);
				}
			}
		});

		child.setAnimation(animation);
	}

	public boolean isExpanded() {
		return mExpanded;
	}

	public void setArc(float fromDegrees, float toDegrees) {
		if (mFromDegrees == fromDegrees && mToDegrees == toDegrees) {
			return;
		}

		mFromDegrees = fromDegrees;
		mToDegrees = toDegrees;

		requestLayout();
	}

	public void setChildSize(int size) {
		if (CHILD_SIZE == size || size < 0) {
			return;
		}

		CHILD_SIZE = size;

		requestLayout();
	}

	public int getChildSize() {
		return CHILD_SIZE;
	}

	/**
	 * switch between expansion and shrinkage
	 * 
	 * @param showAnimation
	 */
	public void switchState(final boolean showAnimation) {
		if (showAnimation) {
			final int childCount = getChildCount();
			final boolean isExpand = mExpanded;
			for (int i = 0; i < childCount; i++) {
				bindChildAnimation(getChildAt(i), i, 300, isExpand);
			}
		}

		mExpanded = !mExpanded;

		if (!showAnimation) {
			requestLayout();
		}

		invalidate();
	}

	private void onAllAnimationsEnd(boolean isExpand) {
		final int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			getChildAt(i).clearAnimation();
		}
		if (isExpand) {
			setVisibility(View.INVISIBLE);
			CreateAisleSelectionActivity createAisleSelectionActivity = (CreateAisleSelectionActivity) mContext;
			createAisleSelectionActivity.finish();
		}
		requestLayout();
	}

}
