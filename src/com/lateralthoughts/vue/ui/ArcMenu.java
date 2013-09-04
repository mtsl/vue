package com.lateralthoughts.vue.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.lateralthoughts.vue.CreateAisleSelectionActivity;
import com.lateralthoughts.vue.R;
import com.lateralthoughts.vue.VueApplication;
import com.lateralthoughts.vue.VueLandingPageActivity;

public class ArcMenu extends RelativeLayout {
	public ArcLayout mArcLayout;

	public ImageView mHintView;

	public Context mContext;

	public ArcMenu(Context context) {
		super(context);
		init(context);
	}

	public ArcMenu(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
		applyAttrs(attrs);
	}

	private void init(Context context) {
		mContext = context;
		LayoutInflater li = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		li.inflate(R.layout.arc_menu, this);

		mArcLayout = (ArcLayout) findViewById(R.id.item_layout);
		mArcLayout.setVisibility(View.INVISIBLE);

		final ViewGroup controlLayout = (ViewGroup) findViewById(R.id.control_layout);
		controlLayout.setClickable(true);
		controlLayout.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					mHintView
							.startAnimation(createHintSwitchAnimation(mArcLayout
									.isExpanded()));
					mArcLayout.switchState(true);
				}
				return false;
			}
		});

		mHintView = (ImageView) findViewById(R.id.control_hint);
	}

	private void applyAttrs(AttributeSet attrs) {
		if (attrs != null) {
			/*
			 * TypedArray a = getContext().obtainStyledAttributes(attrs,
			 * R.styleable.ArcLayout, 0, 0);
			 */

			float fromDegrees = ArcLayout.DEFAULT_FROM_DEGREES;
			float toDegrees = ArcLayout.DEFAULT_TO_DEGREES;
			mArcLayout.setArc(fromDegrees, toDegrees);

			// int defaultChildSize = mArcLayout.getChildSize();
			int newChildSize = ArcLayout.CHILD_SIZE;
			mArcLayout.setChildSize(newChildSize);

			// a.recycle();
		}
	}

	public void addItem(int position, View item, OnClickListener listener) {
		mArcLayout.addView(item);
		item.setOnClickListener(getItemClickListener(position, listener));
	}

	private OnClickListener getItemClickListener(final int position,
			final OnClickListener listener) {
		return new OnClickListener() {

			@Override
			public void onClick(final View viewClicked) {

				if (position == 4) {
					CreateAisleSelectionActivity createAisleSelectionActivity = (CreateAisleSelectionActivity) mContext;
					createAisleSelectionActivity.moreClickFunctionality();
				} else {
					Animation animation = bindItemAnimation(viewClicked, true,
							400);
					animation.setAnimationListener(new AnimationListener() {

						@Override
						public void onAnimationStart(Animation animation) {

						}

						@Override
						public void onAnimationRepeat(Animation animation) {

						}

						@Override
						public void onAnimationEnd(Animation animation) {
							postDelayed(new Runnable() {

								@Override
								public void run() {
									itemDidDisappear(position);
								}
							}, 0);
						}
					});

					final int itemCount = mArcLayout.getChildCount();
					for (int i = 0; i < itemCount; i++) {
						View item = mArcLayout.getChildAt(i);
						if (viewClicked != item) {
							bindItemAnimation(item, false, 300);
						}
					}

					mArcLayout.invalidate();
					mHintView.startAnimation(createHintSwitchAnimation(true));

					if (listener != null) {
						listener.onClick(viewClicked);
					}
				}
			}
		};
	}

	private Animation bindItemAnimation(final View child,
			final boolean isClicked, final long duration) {
		Animation animation = createItemDisapperAnimation(duration, isClicked);
		child.setAnimation(animation);

		return animation;
	}

	private void itemDidDisappear(int position) {
		final int itemCount = mArcLayout.getChildCount();
		for (int i = 0; i < itemCount; i++) {
			View item = mArcLayout.getChildAt(i);
			item.clearAnimation();
		}
		mArcLayout.setVisibility(View.INVISIBLE);
		CreateAisleSelectionActivity createAisleSelectionActivity = (CreateAisleSelectionActivity) mContext;
		switch (position) {
		case 0:
			createAisleSelectionActivity.cameraFunctionality();
			break;
		case 1:
			createAisleSelectionActivity.galleryFunctionality();
			break;
		case 2:
			if (VueApplication.getInstance().mShoppingApplicationDetailsList != null
					&& VueApplication.getInstance().mShoppingApplicationDetailsList
							.size() > 0) {
				createAisleSelectionActivity
						.loadShoppingApplication(
								VueApplication.getInstance().mShoppingApplicationDetailsList
										.get(0).getActivityName(),
								VueApplication.getInstance().mShoppingApplicationDetailsList
										.get(0).getPackageName());
			} else {
				Toast.makeText(mContext, "There is no application.",
						Toast.LENGTH_LONG).show();
				createAisleSelectionActivity.finish();
			}
			break;
		case 3:
			if (VueApplication.getInstance().mShoppingApplicationDetailsList != null
					&& VueApplication.getInstance().mShoppingApplicationDetailsList
							.size() > 1) {
				createAisleSelectionActivity
						.loadShoppingApplication(
								VueApplication.getInstance().mShoppingApplicationDetailsList
										.get(1).getActivityName(),
								VueApplication.getInstance().mShoppingApplicationDetailsList
										.get(1).getPackageName());
			} else {
				Toast.makeText(mContext, "There is no application.",
						Toast.LENGTH_LONG).show();
				createAisleSelectionActivity.finish();
			}
			break;
		default:
			createAisleSelectionActivity.finish();
			break;
		}
		mArcLayout.mExpanded = !mArcLayout.mExpanded;
		mArcLayout.removeAllViews();
	}

	static Animation createItemDisapperAnimation(final long duration,
			final boolean isClicked) {
		AnimationSet animationSet = new AnimationSet(true);
		animationSet.addAnimation(new ScaleAnimation(1.0f, isClicked ? 2.0f
				: 0.0f, 1.0f, isClicked ? 2.0f : 0.0f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f));
		animationSet.addAnimation(new AlphaAnimation(1.0f, 0.0f));

		animationSet.setDuration(duration);
		animationSet.setInterpolator(new DecelerateInterpolator());
		animationSet.setFillAfter(true);

		return animationSet;
	}

	public static Animation createHintSwitchAnimation(final boolean expanded) {
		Animation animation = new RotateAnimation(expanded ? 45 : 0,
				expanded ? 0 : 45, Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f);
		animation.setStartOffset(0);
		animation.setDuration(100);
		animation.setInterpolator(new DecelerateInterpolator());
		animation.setFillAfter(true);

		return animation;
	}

	public void initArcMenu(ArcMenu menu, int[] itemDrawables) {
		final int itemCount = itemDrawables.length;
		for (int i = 0; i < itemCount; i++) {
			ImageView item = new ImageView(mContext);
			item.setImageResource(itemDrawables[i]);

			final int position = i;
			menu.addItem(i, item, new OnClickListener() {

				@Override
				public void onClick(View v) {
					Toast.makeText(mContext, "position:" + position,
							Toast.LENGTH_SHORT).show();
				}
			});
		}
	}
}
