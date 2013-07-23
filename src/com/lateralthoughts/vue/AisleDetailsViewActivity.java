package com.lateralthoughts.vue;

//generic android goodies

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SlidingDrawer;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.lateralthoughts.vue.ui.AisleContentBrowser;
import com.lateralthoughts.vue.ui.HorizontalListView;
import com.lateralthoughts.vue.utils.ActionBarHandler;
import com.lateralthoughts.vue.utils.BitmapLoaderUtils;
import com.slidingmenu.lib.SlidingMenu;

public class AisleDetailsViewActivity extends BaseActivity/* FragmentActivity */{
	Fragment mFragRight;
	public static final String CLICK_EVENT = "click";
	public static final String LONG_PRESS_EVENT = "longpress";
	HorizontalListView mTopScroller, mBottomScroller;
	int mStatusbarHeight;
	int mScreenTotalHeight;
	int mCoparisionScreenHeight;
	Context mContext;
	AisleWindowContent mWindowContent;
	private SlidingDrawer mSlidingDrawer;
	ArrayList<AisleImageDetails> mImageDetailsArr = null;
	AisleImageDetails mItemDetails = null;
	private VueTrendingAislesDataModel mVueTrendingAislesDataModel;
	private BitmapLoaderUtils mBitmapLoaderUtils;
	private int mLikeImageShowTime = 1000;
	private boolean isActionBarShown = false;
	private int mCurrentapiVersion;
	private HandleActionBar handleActionbar;

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		// setContentView(R.layout.vuedetails_frag);
		setContentView(R.layout.aisle_details_activity_landing);
		//getSupportActionBar().hide();
		getSupportActionBar().setTitle(getResources().getString(R.string.trending));
		mCurrentapiVersion = android.os.Build.VERSION.SDK_INT; 
		  if(mCurrentapiVersion >= 11) { 
			  getActionBar().hide(); 
		  }
		 

		mSlidingDrawer = (SlidingDrawer) findViewById(R.id.drawer2);
		mSlidingDrawer
				.setOnDrawerScrollListener(new SlidingDrawer.OnDrawerScrollListener() {
					private Runnable mRunnable = new Runnable() {
						@Override
						public void run() {
							// While the SlidingDrawer is moving; do nothing.
							while (mSlidingDrawer.isMoving()) {
								// Allow another thread to process its
								// instructions.
								Thread.yield();
							}

							// When the SlidingDrawer is no longer moving;
							// trigger mHandler.
							mHandler.sendEmptyMessage(0);
						}
					};

					@Override
					public void onScrollStarted() {
						getSlidingMenu().setTouchModeAbove(
								SlidingMenu.TOUCHMODE_NONE);
					}

					@Override
					public void onScrollEnded() {
						new Thread(mRunnable).start();
					}
				});
		mTopScroller = (HorizontalListView) findViewById(R.id.topscroller);
		mBottomScroller = (HorizontalListView) findViewById(R.id.bottomscroller);
		mStatusbarHeight = VueApplication.getInstance().getmStatusBarHeight();
		mScreenTotalHeight = VueApplication.getInstance().getScreenHeight();
		mCoparisionScreenHeight = mScreenTotalHeight - mStatusbarHeight
				- VueApplication.getInstance().getPixel(30);
		mVueTrendingAislesDataModel = VueTrendingAislesDataModel
				.getInstance(mContext);
		mBitmapLoaderUtils = BitmapLoaderUtils.getInstance(mContext);
		for (int i = 0; i < mVueTrendingAislesDataModel.getAisleCount(); i++) {
			mWindowContent = (AisleWindowContent) mVueTrendingAislesDataModel
					.getAisleAt(i);
			if (mWindowContent.getAisleId().equalsIgnoreCase(
					VueApplication.getInstance().getClickedWindowID())) {
				mWindowContent = (AisleWindowContent) mVueTrendingAislesDataModel
						.getAisleAt(i);
				break;
			}
		}
		mImageDetailsArr = mWindowContent.getImageList();
		if (null != mImageDetailsArr && mImageDetailsArr.size() != 0) {
		}
		mTopScroller.setAdapter(new ComparisionAdapter(
				AisleDetailsViewActivity.this));
		mTopScroller.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					final int position, long arg3) {
				final ImageView img = (ImageView) arg1
						.findViewById(R.id.compare_like_dislike);
				img.setImageResource(R.drawable.thumb_up);
				img.setVisibility(View.VISIBLE);
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						img.setVisibility(View.INVISIBLE);
						VueAisleDetailsViewFragment fragment = (VueAisleDetailsViewFragment) getSupportFragmentManager()
								.findFragmentById(
										R.id.aisle_details_view_fragment);
						fragment.changeLikeCount(position, CLICK_EVENT);
					}
				}, mLikeImageShowTime);

			}
		});
		mTopScroller.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					final int position, long arg3) {
				final ImageView img = (ImageView) arg1
						.findViewById(R.id.compare_like_dislike);
				img.setImageResource(R.drawable.thdown);
				img.setVisibility(View.VISIBLE);
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						img.setVisibility(View.INVISIBLE);
						VueAisleDetailsViewFragment fragment = (VueAisleDetailsViewFragment) getSupportFragmentManager()
								.findFragmentById(
										R.id.aisle_details_view_fragment);
						fragment.changeLikeCount(position, LONG_PRESS_EVENT);
					}
				}, mLikeImageShowTime);
				return false;
			}
		});
		mBottomScroller.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					final int position, long arg3) {
				final ImageView img = (ImageView) arg1
						.findViewById(R.id.compare_like_dislike);
				img.setImageResource(R.drawable.thumb_up);
				img.setVisibility(View.VISIBLE);
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						img.setVisibility(View.INVISIBLE);
						VueAisleDetailsViewFragment fragment = (VueAisleDetailsViewFragment) getSupportFragmentManager()
								.findFragmentById(
										R.id.aisle_details_view_fragment);
						fragment.changeLikeCount(position, CLICK_EVENT);
					}
				}, mLikeImageShowTime);

			}
		});
		mBottomScroller
				.setOnItemLongClickListener(new OnItemLongClickListener() {

					@Override
					public boolean onItemLongClick(AdapterView<?> arg0,
							View arg1, final int position, long arg3) {
						final ImageView img = (ImageView) arg1
								.findViewById(R.id.compare_like_dislike);
						img.setImageResource(R.drawable.thdown);
						img.setVisibility(View.VISIBLE);
						new Handler().postDelayed(new Runnable() {
							@Override
							public void run() {
								img.setVisibility(View.INVISIBLE);
								VueAisleDetailsViewFragment fragment = (VueAisleDetailsViewFragment) getSupportFragmentManager()
										.findFragmentById(
												R.id.aisle_details_view_fragment);
								fragment.changeLikeCount(position,
										LONG_PRESS_EVENT);
							}
						}, mLikeImageShowTime);
						return false;
					}
				});
		mBottomScroller.setAdapter(new ComparisionAdapter(
				AisleDetailsViewActivity.this));

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.title_options, menu);
		getSupportActionBar().setHomeButtonEnabled(true);
		// Configure the search info and add any event listeners
		return true;// super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.menu_create_aisles:
			Intent intent = new Intent(AisleDetailsViewActivity.this,
					CreateAisleSelectionActivity.class);
			startActivity(intent);
			return true;
		case android.R.id.home:
			getSlidingMenu().toggle();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	class ComparisionAdapter extends BaseAdapter {
		LayoutInflater minflater;
		ViewHolder viewHolder;

		public ComparisionAdapter(Context context) {
			minflater = (LayoutInflater) getSystemService(context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return mImageDetailsArr.size();
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			mItemDetails = mImageDetailsArr.get(position);
			Bitmap bitmap = mBitmapLoaderUtils
					.getCachedBitmap(mItemDetails.mCustomImageUrl);
			if (convertView == null) {
				viewHolder = new ViewHolder();
				convertView = minflater.inflate(R.layout.vuecompareimg, null);
				viewHolder.img = (ImageView) convertView
						.findViewById(R.id.vue_compareimg);
				viewHolder.likeImage = (ImageView) convertView
						.findViewById(R.id.compare_like_dislike);
				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
						mCoparisionScreenHeight / 2,
						mCoparisionScreenHeight / 2);
				params.addRule(RelativeLayout.CENTER_IN_PARENT);
				params.setMargins(VueApplication.getInstance().getPixel(10), 0,
						0, 0);
				viewHolder.img.setLayoutParams(params);
				viewHolder.img.setBackgroundColor(Color
						.parseColor(getResources().getString(R.color.white)));
				RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				params2.addRule(RelativeLayout.CENTER_IN_PARENT);
				viewHolder.likeImage.setLayoutParams(params2);

				convertView.setTag(viewHolder);
			}

			viewHolder.likeImage.setVisibility(View.INVISIBLE);
			viewHolder = (ViewHolder) convertView.getTag();
			viewHolder.likeImage.setImageResource(R.drawable.thumb_up);
			if (bitmap != null) {
				viewHolder.img.setImageBitmap(bitmap);
			} else {
				viewHolder.img.setImageResource(R.drawable.ic_launcher);
				BitmapWorkerTask task = new BitmapWorkerTask(null,
						viewHolder.img, mCoparisionScreenHeight / 2);
				task.execute(mItemDetails.mCustomImageUrl);

			}
			return convertView;
		}

		private class ViewHolder {
			ImageView img;
			ImageView likeImage;
		}
	}

	@Override
	public void onResume() {
		handleActionbar = new HandleActionBar();
		VueAisleDetailsViewFragment fragment = (VueAisleDetailsViewFragment) getSupportFragmentManager()
				.findFragmentById(R.id.aisle_details_view_fragment);
		fragment.setActionBarHander(handleActionbar);
 
		super.onResume();
	}

	private Handler mHandler = new Handler() {

		public void handleMessage(Message msg) {

			if (mSlidingDrawer.isOpened()) {
			} else {
				getSlidingMenu()
						.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
			}
		}
	};

	@Override
	public void onPause() {
		super.onPause();

	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (getSlidingMenu().isMenuShowing()) {
				if (!mFrag.listener.onBackPressed()) {
					getSlidingMenu().toggle();
				}
			} else {
				if (!VueApplication.getInstance().mSoftKeboardIndicator) {
					VueAisleDetailsViewFragment fragment = (VueAisleDetailsViewFragment) getSupportFragmentManager()
							.findFragmentById(R.id.aisle_details_view_fragment);
					fragment.setAisleContentListenerNull();
			    super.onBackPressed();
				} else {
					VueApplication.getInstance().mSoftKeboardIndicator = false;
				}
 
			}
		}
		return false;

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.e("share+", "details activity result" + requestCode + resultCode);
		if (requestCode == VueConstants.INVITE_FRIENDS_LOGINACTIVITY_REQUEST_CODE
				&& resultCode == VueConstants.INVITE_FRIENDS_LOGINACTIVITY_REQUEST_CODE) {
			if (data != null) {
				if (data.getStringExtra(VueConstants.INVITE_FRIENDS_LOGINACTIVITY_BUNDLE_STRING_KEY) != null) {
					mFrag.getFriendsList(data
							.getStringExtra(VueConstants.INVITE_FRIENDS_LOGINACTIVITY_BUNDLE_STRING_KEY));
				}
			}
		} else {

			try {
				VueAisleDetailsViewFragment fragment = (VueAisleDetailsViewFragment) getSupportFragmentManager()
						.findFragmentById(R.id.aisle_details_view_fragment);

				if (fragment.mAisleDetailsAdapter.mShare.shareIntentCalled) {
					fragment.mAisleDetailsAdapter.mShare.shareIntentCalled = false;
					fragment.mAisleDetailsAdapter.mShare.dismisDialog();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
		private final WeakReference<ImageView> imageViewReference;
		// private final WeakReference<AisleContentBrowser>viewFlipperReference;
		private String url = null;
		private int mBestHeight;

		public BitmapWorkerTask(AisleContentBrowser vFlipper,
				ImageView imageView, int bestHeight) {
			// Use a WeakReference to ensure the ImageView can be garbage
			// collected
			imageViewReference = new WeakReference<ImageView>(imageView);
			mBestHeight = bestHeight;
		}

		// Decode image in background.
		@Override
		protected Bitmap doInBackground(String... params) {
			url = params[0];
			Bitmap bmp = null;
			// we want to get the bitmap and also add it into the memory cache
			bmp = mBitmapLoaderUtils.getBitmap(url, true, mBestHeight);
			return bmp;
		}

		// Once complete, see if ImageView is still around and set bitmap.
		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if (imageViewReference != null && bitmap != null) {
				final ImageView imageView = imageViewReference.get();
				imageView.setImageBitmap(bitmap);

			}
		}
	}
	
	private class HandleActionBar implements ActionBarHandler {

		@Override
		public void showActionBar() {
			if(!isActionBarShown) {
			isActionBarShown = true;
			 if(mCurrentapiVersion >= 11) { 
				//  getActionBar().show(); 
			  }
			}
			
		}

		@Override
		public void hideActionBar() {
			if(isActionBarShown) {
				  if(mCurrentapiVersion >= 11) { 
					 // getActionBar().hide(); 
				  }
			isActionBarShown = false;
			}
			
		}

	 
		
	}
	
	/*
	 * @Override public boolean onCreateOptionsMenu(Menu menu) {
	 * getSupportMenuInflater().inflate(R.menu.title_options, menu); //
	 * Configure the search info and add any event listeners return
	 * super.onCreateOptionsMenu(menu); }
	 */
}
