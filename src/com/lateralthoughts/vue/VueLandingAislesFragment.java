package com.lateralthoughts.vue;

//generic android & java goodies
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.Fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.Transformation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.lateralthoughts.vue.connectivity.DataBaseManager;
import com.lateralthoughts.vue.ui.AisleContentBrowser;
import com.lateralthoughts.vue.ui.AisleContentBrowser.AisleContentClickListener;
import com.lateralthoughts.vue.ui.ArcMenu;
import com.lateralthoughts.vue.ui.MyCustomAnimation;
import com.lateralthoughts.vue.ui.ScaleImageView;
import com.lateralthoughts.vue.utils.Utils;

//java utils

//as soon as this fragment attaches to the activity we will go ahead and pull up the
//the list of current trending aisles.
//when the result is received we parse it and coalesce it into an array list of 
//AisleWindowContent objects. At this point we are ready to setup the adapter for the
//mTrendingAislesContentView.

public class VueLandingAislesFragment extends /* SherlockFragment */Fragment {
	private Context mContext;
	private VueContentGateway mVueContentGateway;
	private TrendingAislesLeftColumnAdapter mLeftColumnAdapter;
	private TrendingAislesRightColumnAdapter mRightColumnAdapter;

	private ListView mLeftColumnView;
	private ListView mRightColumnView;

	private AisleClickListener mAisleClickListener;
	// private MultiColumnListView mView;
	int[] mLeftViewsHeights;
	int[] mRightViewsHeights;
	public boolean mIsFlingCalled;

	private int animdelay = 4000;
	private int height;
	public boolean mIsIdleState;
	private LinearLayout pulltorefresh;

	private boolean mIsListRefreshRecently = true;

	RelativeLayout mRightLay;
	RelativeLayout mLeftLay;

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

		// without much ado lets get started with retrieving the trending aisles
		// list
		mVueContentGateway = VueContentGateway.getInstance();
		if (null == mVueContentGateway) {
			// assert here: this is a no go!
		}

		mAisleClickListener = new AisleClickListener();
		mLeftColumnAdapter = new TrendingAislesLeftColumnAdapter(mContext,
				mAisleClickListener, null);
		mRightColumnAdapter = new TrendingAislesRightColumnAdapter(mContext,
				mAisleClickListener, null);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// TODO: any particular state that we want to restore?

	}

	public void notifyAdapters() {
		if (mLeftColumnAdapter != null) {

			mLeftColumnAdapter.notifyDataSetChanged();

		}
		if (mRightColumnAdapter != null) {
			mRightColumnAdapter.notifyDataSetChanged();
			Log.i("listadapter", "adapter adapter notified");
		}
	}

	int targetHeight = 60;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// synchronized list view approach
		View v = inflater.inflate(R.layout.aisles_view_fragment2, container,
				false);
		mLeftColumnView = (ListView) v.findViewById(R.id.list_view_left);
		mRightColumnView = (ListView) v.findViewById(R.id.list_view_right);
		pulltorefresh = (LinearLayout) v.findViewById(R.id.pulltorefresh);
		// mLeftColumnView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		// mRightColumnView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		mLeftColumnView.setAdapter(mLeftColumnAdapter);
		mRightColumnView.setAdapter(mRightColumnAdapter);
		mLeftColumnView.setOverScrollMode(View.OVER_SCROLL_NEVER);
		mRightColumnView.setOverScrollMode(View.OVER_SCROLL_NEVER);
		mLeftColumnView.setOnTouchListener(touchListener);
		mRightColumnView.setOnTouchListener(touchListener);
		mLeftColumnView.setOnScrollListener(scrollListener);
		mRightColumnView.setOnScrollListener(scrollListener);

		mLeftColumnView.setClickable(true);
		mLeftColumnView
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						Log.e("Vinodh Clicks",
								"ok...we are getting item clicks!!");

					}
				});

		mLeftViewsHeights = new int[1000];
		mRightViewsHeights = new int[1000];
		Log.d("VueLandingAislesFragment",
				"Get ready to displayed staggered view");
		return v;
	}

	// Passing the touch event to the opposite list
	OnTouchListener touchListener = new OnTouchListener() {
		boolean dispatched = false;

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (v.equals(mLeftColumnView) && !dispatched) {
				dispatched = true;
				mRightColumnView.dispatchTouchEvent(event);
			} else if (v.equals(mRightColumnView) && !dispatched) {
				dispatched = true;
				mLeftColumnView.dispatchTouchEvent(event);
			}

			dispatched = false;
			return false;
		}
	};
	/**
	 * Synchronizing scrolling Distance from the top of the first visible
	 * element opposite list: sum_heights(opposite invisible screens) -
	 * sum_heights(invisible screens) + distance from top of the first visible
	 * child
	 */
	OnScrollListener scrollListener = new OnScrollListener() {
		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			mLeftColumnAdapter.setIsScrolling(scrollState != SCROLL_STATE_IDLE);
			mRightColumnAdapter
					.setIsScrolling(scrollState != SCROLL_STATE_IDLE);
			if (scrollState == SCROLL_STATE_FLING) {
				mIsFlingCalled = true;
				mIsIdleState = false;
			} else if (scrollState == SCROLL_STATE_IDLE) {
				mIsIdleState = true;
				mIsFlingCalled = false;
				// notify the adapters.
				mLeftColumnAdapter.notifyDataSetChanged();
				mRightColumnAdapter.notifyDataSetChanged();

			} else if (scrollState == SCROLL_STATE_TOUCH_SCROLL) {
				mIsIdleState = false;
				mIsFlingCalled = false;
				mIsListRefreshRecently = false;
				/*
				 * if(view.getLastVisiblePosition() < view.getCount() - 1) {
				 * LayoutParams params = new LayoutParams(0,
				 * LayoutParams.MATCH_PARENT);
				 * mProgressBar.setLayoutParams(params); }
				 */

			}
			int first = view.getFirstVisiblePosition();
			int count = view.getChildCount();
			if (scrollState == SCROLL_STATE_IDLE
					|| (first + count > mLeftColumnAdapter.getCount())
					|| (first + count > mRightColumnAdapter.getCount())) {
				mLeftColumnView.invalidateViews();
				mRightColumnView.invalidateViews();
			}
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			if (firstVisibleItem > 5) {
				mIsListRefreshRecently = false;
			}
			int localTop = 0;
			if (view.getChildAt(0) != null) {
				if (view.equals(mLeftColumnView)) {
					mLeftViewsHeights[view.getFirstVisiblePosition()] = view
							.getChildAt(0).getHeight();

					int h = 0;
					for (int i = 0; i < mRightColumnView
							.getFirstVisiblePosition(); i++) {
						h += mRightViewsHeights[i];
					}

					int hi = 0;
					for (int i = 0; i < mLeftColumnView
							.getFirstVisiblePosition(); i++) {
						hi += mLeftViewsHeights[i];
					}

					int top = h - hi + view.getChildAt(0).getTop();
					mRightColumnView.setSelectionFromTop(
							mRightColumnView.getFirstVisiblePosition(), top);
					/*
					 * localTop = top;
					 * 
					 * if(mLeftColumnView.getLastVisiblePosition() ==
					 * totalItemCount) {
					 * if(mRightColumnView.getLastVisiblePosition() <
					 * totalItemCount) { AisleWindowContent content =
					 * (AisleWindowContent) mRightColumnView
					 * .getAdapter().getItem
					 * (mRightColumnView.getLastVisiblePosition() + 1);
					 * mRightColumnView.setSelectionFromTop(
					 * mRightColumnView.getFirstVisiblePosition(), localTop +
					 * content
					 * .mWindowSmallestHeight+VueApplication.getInstance()
					 * .getPixel(100));
					 * 
					 * } }
					 */
				} else if (view.equals(mRightColumnView)) {
					mRightViewsHeights[view.getFirstVisiblePosition()] = view
							.getChildAt(0).getHeight();

					int h = 0;
					for (int i = 0; i < mLeftColumnView
							.getFirstVisiblePosition(); i++) {
						h += mLeftViewsHeights[i];
					}

					int hi = 0;
					for (int i = 0; i < mRightColumnView
							.getFirstVisiblePosition(); i++) {
						hi += mRightViewsHeights[i];
					}

					int top = h - hi + view.getChildAt(0).getTop();

					mLeftColumnView.setSelectionFromTop(
							mLeftColumnView.getFirstVisiblePosition(), top);
					/*
					 * localTop = top;
					 * if(mRightColumnView.getLastVisiblePosition() ==
					 * totalItemCount) {
					 * if(mLeftColumnView.getLastVisiblePosition() <
					 * totalItemCount) { AisleWindowContent content =
					 * (AisleWindowContent) mLeftColumnView
					 * .getAdapter().getItem
					 * (mLeftColumnView.getLastVisiblePosition() + 1);
					 * if(mLeftColumnView.canScrollVertically(-1)){
					 * mLeftColumnView.setScrollY(mLeftColumnView.getBottom());
					 * } mLeftColumnView.setSelectionFromTop(
					 * mLeftColumnView.getFirstVisiblePosition(), localTop +
					 * content
					 * .mWindowSmallestHeight+VueApplication.getInstance()
					 * .getPixel(100));
					 * 
					 * } }
					 */
				}

			}

			// VueLandingPageActivity lan = (VueLandingPageActivity)
			// getActivity();

			if (VueTrendingAislesDataModel.getInstance(mContext).loadOnRequest
					&& VueLandingPageActivity.mLandingScreenName != null
					&& VueLandingPageActivity.mLandingScreenName
							.equalsIgnoreCase(getResources().getString(
									R.string.trending))
					&& !VueTrendingAislesDataModel.getInstance(mContext).isFromDb) {
				int lastVisiblePosition = firstVisibleItem + visibleItemCount;
				Log.i("more aisle request", "more aisle request calling");
				int totalItems = 0;
				if (view.equals(mLeftColumnView)) {
					totalItems = mLeftColumnAdapter.getCount();
				} else if (view.equals(mRightColumnView)) {
					totalItems = mRightColumnAdapter.getCount();
				}
				/*
				 * if ((totalItems - lastVisiblePosition) < 2) { if
				 * (mProgressBar.getLayoutParams().height == 0) {
				 * ProgressBarAnimation a = new
				 * ProgressBarAnimation(mProgressBar, targetHeight, false);
				 * a.setDuration(1000); LayoutParams params = new
				 * LayoutParams(LayoutParams.MATCH_PARENT, 60);
				 * params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,
				 * mProgressBar.getId()); mProgressBar.setLayoutParams(params);
				 * mProgressBar.startAnimation(a); }
				 * 
				 * } else { if (mProgressBar.getLayoutParams().height > 0) {
				 * ProgressBarAnimation a = new
				 * ProgressBarAnimation(mProgressBar, 0, true);
				 * a.setDuration(1000); LayoutParams params = new
				 * LayoutParams(LayoutParams.MATCH_PARENT, 0);
				 * params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,
				 * mProgressBar.getId()); mProgressBar.setLayoutParams(params);
				 * mProgressBar.startAnimation(a); } }
				 */
				if ((totalItems - lastVisiblePosition) < 5) {
					Log.i("offeset and limit", "offeset00000: load moredata");
					VueTrendingAislesDataModel
							.getInstance(mContext)
							.getNetworkHandler()
							.requestMoreAisle(true,
									getResources().getString(R.string.trending));
				}
			} else {
				// LayoutParams params = new LayoutParams(0,
				// LayoutParams.MATCH_PARENT);
				Log.i("offeset and limit",
						"offeset00000: load moredata else "
								+ VueTrendingAislesDataModel
										.getInstance(mContext).loadOnRequest);
			}

		}
	};

	private class AisleClickListener implements AisleContentClickListener {
		@Override
		public void onAisleClicked(String id, int count, int aisleImgCurrentPos) {

			if (VueLandingPageActivity.mOtherSourceImagePath == null) {
				Map<String, String> articleParams = new HashMap<String, String>();
				VueUser storedVueUser = null;
				try {
					storedVueUser = Utils.readUserObjectFromFile(getActivity(),
							VueConstants.VUE_APP_USEROBJECT__FILENAME);
				} catch (Exception e2) {
					e2.printStackTrace();
				}
				if (storedVueUser != null) {
					articleParams.put("User_Id",
							Long.valueOf(storedVueUser.getId()).toString());
				} else {
					articleParams.put("User_Id", "anonymous");
				}
				Log.e("VueLandingAisleFragment",
						"Suru aisle clicked aisle Id: " + id);
				DataBaseManager.getInstance(mContext)
						.updateOrAddRecentlyViewedAisles(id);
				FlurryAgent.logEvent("User_Select_Aisle", articleParams);

				VueLandingPageActivity vueLandingPageActivity = (VueLandingPageActivity) getActivity();
				Log.i("clickedwindow", "clickedwindow ID: " + id);
				Intent intent = new Intent();
				intent.setClass(VueApplication.getInstance(),
						AisleDetailsViewActivity.class);
				VueApplication.getInstance().setClickedWindowID(id);
				VueApplication.getInstance().setClickedWindowCount(count);
				VueApplication.getInstance().setmAisleImgCurrentPos(
						aisleImgCurrentPos);
				Log.i("imageCurrenPosition",
						"imageCurrenPosition landing click: "
								+ aisleImgCurrentPos);
				startActivity(intent);
			} else {
				VueLandingPageActivity vueLandingPageActivity = (VueLandingPageActivity) getActivity();
				/*vueLandingPageActivity.mVueLandingKeyboardLayout
						.setVisibility(View.VISIBLE);*/
				VueLandingPageActivity.mOtherSourceAddImageAisleId = id;
				notifyAdapters();
			}
		}

		@Override
		public boolean isFlingCalled() {
			Log.i("flingcheck", "flingcheck  isFlingCalled val: "
					+ mIsFlingCalled);
			return mIsFlingCalled;
		}

		@Override
		public boolean isIdelState() {

			return mIsIdleState;
		}

		@Override
		public boolean onDoubleTap(String id) {
			AisleWindowContent windowItem = VueTrendingAislesDataModel
					.getInstance(VueApplication.getInstance()).getAisleAt(id);

			Log.i("aisleItem", "aisleItem: id " + windowItem.getAisleId());
			Log.i("aisleItem",
					"aisleItem:best smallest Height : "
							+ windowItem.getBestHeightForWindow());
			Log.i("aisleItem", "aisleItem:best cardwidth : "
					+ VueApplication.getInstance().getScreenWidth() / 2);
			String imageUrls = "";
			for (int i = 0; i < windowItem.getImageList().size(); i++) {
				Log.i("aisleItem", "aisleItem: imageUrl "
						+ windowItem.getImageList().get(i).mImageUrl);
				Log.i("aisleItem", "aisleItem: imageUrl height"
						+ windowItem.getImageList().get(i).mAvailableHeight
						+ " width: "
						+ windowItem.getImageList().get(i).mAvailableWidth);
			}
			int finalWidth = 0, finaHeight = 0;
			if (windowItem.getImageList().get(0).mAvailableHeight >= windowItem
					.getBestHeightForWindow()) {
				finalWidth = (windowItem.getImageList().get(0).mAvailableWidth * windowItem
						.getBestHeightForWindow())
						/ windowItem.getImageList().get(0).mAvailableHeight;
				finaHeight = windowItem.getBestHeightForWindow();
			}

			if (finalWidth > VueApplication.getInstance().getScreenWidth() / 2) {

				finaHeight = (finaHeight
						* VueApplication.getInstance().getScreenWidth() / 2)
						/ finalWidth;
				finalWidth = VueApplication.getInstance().getScreenWidth() / 2;
			}
			Log.i("aisleItem", "aisleItem: after resize aisle width "
					+ finalWidth + " height: " + finaHeight);

			String writeSdCard = null;
			writeSdCard = "*************************aisle info:"
					+ " started***********************\n";
			writeSdCard = writeSdCard + "\nAisleId: " + windowItem.getAisleId()
					+ "\n" + "Smallest Image Height: "
					+ windowItem.getImageList().get(0).mTrendingImageHeight
					+ "\n" + "Card Width: "
					+ VueApplication.getInstance().getVueDetailsCardWidth() / 2
					+ "\n";
			for (int i = 0; i < windowItem.getImageList().size(); i++) {
				writeSdCard = writeSdCard + "\n ImageUrl: "
						+ windowItem.getImageList().get(i).mImageUrl;
				writeSdCard = writeSdCard + "\n" + "image Width: "
						+ windowItem.getImageList().get(i).mAvailableWidth
						+ " Height: "
						+ windowItem.getImageList().get(i).mAvailableHeight;
			}
			writeSdCard = writeSdCard + "\n\n After Resized Aisle height: "
					+ windowItem.getImageList().get(0).mTrendingImageHeight
					+ " After Resized Aisle width: "
					+ VueApplication.getInstance().getVueDetailsCardWidth() / 2;

			for (int i = 0; i < windowItem.getImageList().size(); i++) {
				writeSdCard = writeSdCard + "\n CustomImageUrl: "
						+ windowItem.getImageList().get(i).mCustomImageUrl;
			}

			writeSdCard = writeSdCard
					+ "\n###################### info end ################################";
			writeToSdcard(writeSdCard);
			return false;
		}

		@Override
		public void refreshList() {
			mLeftColumnAdapter.notifyDataSetChanged();
			mRightColumnAdapter.notifyDataSetChanged();
		}
	}

	/*
	 * public void moveListToPosition(int position) {
	 * mLeftColumnView.setSelection(position);
	 * mLeftColumnView.smoothScrollToPosition(position); }
	 */

	public int getListPosition() {
		return mLeftColumnView.getFirstVisiblePosition();

	}

	private void initArcMenu(ArcMenu menu, int[] itemDrawables) {
		final int itemCount = itemDrawables.length;
		for (int i = 0; i < itemCount; i++) {
			ImageView item = new ImageView(getActivity());
			item.setImageResource(itemDrawables[i]);
			LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT);
			lp.setMargins(4, 4, 4, 4);
			final int position = i;
			menu.addItem(i, lp, item, new OnClickListener() {

				@Override
				public void onClick(View v) {
					Toast.makeText(getActivity(), "position:" + position,
							Toast.LENGTH_SHORT).show();
				}
			});
		}
	}

	private void writeToSdcard(String message) {

		String path = Environment.getExternalStorageDirectory().toString();
		File dir = new File(path + "/vueImageDetails/");
		if (!dir.isDirectory()) {
			dir.mkdir();
		}
		File file = new File(dir, "/"
				+ Calendar.getInstance().get(Calendar.DATE) + ".txt");
		try {
			file.createNewFile();
		} catch (IOException e) {
			Log.i("pathsaving", "pathsaving in sdcard2 error");
			e.printStackTrace();
		}

		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(
					new FileWriter(file, true)));
			out.write("\n" + message + "\n");
			out.flush();
			out.close();
			Log.i("pathsaving", "pathsaving in sdcard2 success");
		} catch (IOException e) {
			Log.i("pathsaving", "pathsaving in sdcard3 error");
			e.printStackTrace();
		}
	}

	private void headerAnimationGoneTask(final RelativeLayout pulltorefresh) {

		MyCustomAnimation a = new MyCustomAnimation(getActivity(),
				pulltorefresh, animdelay, MyCustomAnimation.COLLAPSE);
		a.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				ProgressBar pb = (ProgressBar) pulltorefresh
						.findViewById(R.id.leftlayprogress);
				if (pb != null)
					pb.setVisibility(View.GONE);
				TextView tv = (TextView) pulltorefresh
						.findViewById(R.id.rightlaytextvew);
				if (tv != null) {
					tv.setVisibility(View.GONE);
				}
			}

		});
		height = a.getHeight();

		pulltorefresh.startAnimation(a);

	}

	private void headerAnimationVisibleTask(final RelativeLayout pulltorefresh) {

		MyCustomAnimation a = new MyCustomAnimation(getActivity(),
				pulltorefresh, animdelay, MyCustomAnimation.EXPAND);
		a.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {

			}
		});
		pulltorefresh.startAnimation(a);

	}

	public void clearBitmaps() {

		for (int i = 0; i < mLeftColumnView.getChildCount(); i++) {
			Log.i("clearlist", "clearlist leftcolumn");
			LinearLayout leftLayout = (LinearLayout) mLeftColumnView
					.getChildAt(i);
			com.lateralthoughts.vue.ui.AisleContentBrowser aisleBrowser = (AisleContentBrowser) leftLayout
					.findViewById(R.id.aisle_content_flipper);
			// aisleBrowser.removeAllViews();
			clearBrowser(aisleBrowser);

		}
		for (int i = 0; i < mRightColumnView.getChildCount(); i++) {
			Log.i("clearlist", "clearlist rightcolumn");
			LinearLayout rightLayout = (LinearLayout) mRightColumnView
					.getChildAt(i);
			com.lateralthoughts.vue.ui.AisleContentBrowser aisleBrowser = (AisleContentBrowser) rightLayout
					.findViewById(R.id.aisle_content_flipper);
			// aisleBrowser.removeAllViews();
			clearBrowser(aisleBrowser);
		}
		mLeftColumnView.setAdapter(mLeftColumnAdapter);
		mRightColumnView.setAdapter(mRightColumnAdapter);
		mLeftColumnAdapter.notifyDataSetChanged();
		mRightColumnAdapter.notifyDataSetChanged();
	}

	private void clearBrowser(AisleContentBrowser aisleContentBrowser) {
		if (aisleContentBrowser != null) {
			ScaledImageViewFactory mViewFactory = ScaledImageViewFactory
					.getInstance(mContext);
			for (int i = 0; i < aisleContentBrowser.getChildCount(); i++) {
				/*
				 * try { ImageView image = (ScaleImageView) aisleContentBrowser
				 * .getChildAt(i); Bitmap bitmap = ((BitmapDrawable)
				 * image.getDrawable()) .getBitmap(); Log.i("clearlist",
				 * "clearlist bitmap recycling"); bitmap.recycle(); bitmap =
				 * null; } catch (Exception e) {
				 * 
				 * }
				 */

				mViewFactory
						.returnUsedImageView((ScaleImageView) aisleContentBrowser
								.getChildAt(i));

			}
		}
		if (aisleContentBrowser != null) {
			ContentAdapterFactory mContentAdapterFactory = ContentAdapterFactory
					.getInstance(mContext);
			mContentAdapterFactory.returnUsedAdapter(aisleContentBrowser
					.getCustomAdapter());
			aisleContentBrowser.setCustomAdapter(null);
			aisleContentBrowser.removeAllViews();
			aisleContentBrowser = null;
		}
		VueTrendingAislesDataModel.getInstance(VueApplication.getInstance())
				.dataObserver();
	}
	
  class ProgressBarAnimation extends Animation {
    private final int targetHeight;
    private final View view;
    //private final boolean down;


    public ProgressBarAnimation(View view, int targetHeight, boolean down) {
      this.view = view;
      this.targetHeight = targetHeight;
     // this.down = down;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
      // super.applyTransformation(interpolatedTime, t);
      int newHeight;
     // if (down) {
        newHeight = (int) (targetHeight * interpolatedTime);
   //   } else {
     //   newHeight = (int) (targetHeight * (1 - interpolatedTime));
     // }
      view.getLayoutParams().height = newHeight;
      view.requestLayout();
    }

    @Override
    public void initialize(int width, int height, int parentWidth,
            int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
    }

    @Override
    public boolean willChangeBounds() {
        return true;
    }
  }

}
