package com.lateralthoughts.vue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SlidingDrawer;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.flurry.android.FlurryAgent;
import com.lateralthoughts.vue.domain.Aisle;
import com.lateralthoughts.vue.ui.AisleContentBrowser;
import com.lateralthoughts.vue.ui.HorizontalListView;
import com.lateralthoughts.vue.ui.ScaleImageView;
import com.lateralthoughts.vue.utils.ActionBarHandler;
import com.lateralthoughts.vue.utils.BitmapCacheDetailsScreen;
import com.lateralthoughts.vue.utils.BitmapLoaderUtils;
import com.lateralthoughts.vue.utils.FileCache;
import com.lateralthoughts.vue.utils.Utils;
import com.slidingmenu.lib.SlidingMenu;

public class AisleDetailsViewActivity extends BaseActivity/* FragmentActivity */{
	Fragment mFragRight;
	public static final String CLICK_EVENT = "click";
	public static final String LONG_PRESS_EVENT = "longpress";
	public static final String SCREEN_TAG = "comparisonscreen";
	public static final String TOP_SCROLLER = "topscroller";
	public static final String BOTTOM_SCROLLER = "bottomscroller";
	private static final String DETAILS_SCREEN_VISITOR = "Details_Screen_Visitors";
	HorizontalListView mTopScroller, mBottomScroller;
	// int mStatusbarHeight;
	int mScreenTotalHeight;
	int mComparisionScreenHeight;
	Context mContext;
	AisleWindowContent mWindowContent;
	private SlidingDrawer mSlidingDrawer;
	ArrayList<AisleImageDetails> mImageDetailsArr = null;
	// AisleImageDetails mItemDetails = null;
	private VueTrendingAislesDataModel mVueTrendingAislesDataModel;
	private BitmapLoaderUtils mBitmapLoaderUtils;
	private int mLikeImageShowTime = 1000;
	private boolean isActionBarShown = false;
	private int mCurrentapiVersion;
	private HandleActionBar mHandleActionbar;
	private int mStatusbarHeight;
	private boolean mTempflag = true;
	VueAisleDetailsViewFragment mVueAiselFragment;
	ViewHolder viewHolder;
	LinearLayout MContentLinearLay;
	int mCurentAislePosistion;
	private FileCache mFileCache;
	private BitmapCacheDetailsScreen mAisleImagesCache;
	// AisleContentBrowser mTopScroller, mBottomScroller;
	private boolean isSlidePanleLoaded = false;
	// private ViewPager mTopScroller,mBottomScroller;

	ContentAdapterFactory mContentAdapterFactory;
	ScaledImageViewFactory mViewFactory;
	ComparisionAdapter mBottomAdapter, mTopAdapter;

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		// setContentView(R.layout.vuedetails_frag);
		setContentView(R.layout.aisle_details_activity_landing);

		VueUser storedVueUser = null;
		try {
			storedVueUser = Utils.readUserObjectFromFile(
					AisleDetailsViewActivity.this,
					VueConstants.VUE_APP_USEROBJECT__FILENAME);
		} catch (Exception e2) {
			e2.printStackTrace();
		}
		mCurrentapiVersion = android.os.Build.VERSION.SDK_INT;

		if (mCurrentapiVersion >= 11) {
			getSupportActionBar().hide();
		}
		mSlidingDrawer = (SlidingDrawer) findViewById(R.id.drawer2);

		/*
		 * mFileCache = VueApplication.getInstance().getFileCache();
		 * mAisleImagesCache =
		 * BitmapCacheDetailsScreen.getInstance(VueApplication.getInstance());
		 */

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
		MContentLinearLay = (LinearLayout) findViewById(R.id.content2);
		mTopScroller = (HorizontalListView) findViewById(R.id.topscroller);
		mBottomScroller = (HorizontalListView) findViewById(R.id.bottomscroller);
		mStatusbarHeight = VueApplication.getInstance().getmStatusBarHeight();
		mScreenTotalHeight = VueApplication.getInstance().getScreenHeight();
		mComparisionScreenHeight = mScreenTotalHeight - mStatusbarHeight;
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
						if (mVueAiselFragment == null) {
							mVueAiselFragment = (VueAisleDetailsViewFragment) getSupportFragmentManager()
									.findFragmentById(
											R.id.aisle_details_view_fragment);
						}
						mVueAiselFragment
								.changeLikeCount(position, CLICK_EVENT);
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
						if (mVueAiselFragment == null) {
							mVueAiselFragment = (VueAisleDetailsViewFragment) getSupportFragmentManager()
									.findFragmentById(
											R.id.aisle_details_view_fragment);
						}
						mVueAiselFragment.changeLikeCount(position,
								LONG_PRESS_EVENT);
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
						if (mVueAiselFragment == null) {
							mVueAiselFragment = (VueAisleDetailsViewFragment) getSupportFragmentManager()
									.findFragmentById(
											R.id.aisle_details_view_fragment);
						}
						mVueAiselFragment
								.changeLikeCount(position, CLICK_EVENT);
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
								if (mVueAiselFragment == null) {
									mVueAiselFragment = (VueAisleDetailsViewFragment) getSupportFragmentManager()
											.findFragmentById(
													R.id.aisle_details_view_fragment);
								}
								mVueAiselFragment.changeLikeCount(position,
										LONG_PRESS_EVENT);
							}
						}, mLikeImageShowTime);
						return false;
					}
				});
		if (VueLandingPageActivity.mOtherSourceImagePath != null) {
			addImageToAisle(VueLandingPageActivity.mOtherSourceImagePath);
			VueLandingPageActivity.mOtherSourceImagePath = null;
		}
	}

	@Override
	protected void onStart() {
		FlurryAgent.onStartSession(this, Utils.FLURRY_APP_KEY);
		FlurryAgent.onPageView();
		FlurryAgent.logEvent(DETAILS_SCREEN_VISITOR);
		super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
		FlurryAgent.onEndSession(this);

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
			intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			Utils.putFromDetailsScreenToDataentryCreateAisleScreenPreferenceFlag(
					AisleDetailsViewActivity.this, false);
			if (!CreateAisleSelectionActivity.isActivityShowing) {
				CreateAisleSelectionActivity.isActivityShowing = true;
				startActivity(intent);
			}
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

		int i = 0;

		public ComparisionAdapter(Context context) {
			minflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

			// mItemDetails = mImageDetailsArr.get(position);
			Bitmap bitmap = mBitmapLoaderUtils.getCachedBitmap(mImageDetailsArr
					.get(position).mCustomImageUrl);

			if (convertView == null) {
				viewHolder = new ViewHolder();
				convertView = minflater.inflate(R.layout.vuecompareimg, null);
				viewHolder.img = (ImageView) convertView
						.findViewById(R.id.vue_compareimg);
				viewHolder.likeImage = (ImageView) convertView
						.findViewById(R.id.compare_like_dislike);
				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
						mComparisionScreenHeight / 2,
						mComparisionScreenHeight / 2);
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
			viewHolder = (ViewHolder) convertView.getTag();
			viewHolder.likeImage.setVisibility(View.INVISIBLE);

			viewHolder.likeImage.setImageResource(R.drawable.thumb_up);
			if (bitmap != null) {
				Log.i("cachecheck", "cachecheck if :" + position);
				viewHolder.img.setImageBitmap(bitmap);
			} else {
				Log.i("cachecheck", "cachecheck else " + position);
				viewHolder.img.setImageResource(R.drawable.ic_launcher);
				BitmapWorkerTask task = new BitmapWorkerTask(null,
						viewHolder.img, mComparisionScreenHeight / 2);
				String[] imagesArray = {
						mImageDetailsArr.get(position).mCustomImageUrl,
						mImageDetailsArr.get(position).mImageUrl };
				task.execute(imagesArray);

			}
			return convertView;
		}

	}

	private class ViewHolder {
		ImageView img;
		ImageView likeImage;
	}

	@Override
	public void onResume() {
		mHandleActionbar = new HandleActionBar();
		if (mVueAiselFragment == null) {
			mVueAiselFragment = (VueAisleDetailsViewFragment) getSupportFragmentManager()
					.findFragmentById(R.id.aisle_details_view_fragment);
		}
		mVueAiselFragment.setActionBarHander(mHandleActionbar);

		super.onResume();
		Log.e("Land", "vueland 2");
		Bundle b = getIntent().getExtras();
		if (b != null && mTempflag) {
			mTempflag = false;
			if (b.getBoolean(VueConstants.FROM_OTHER_SOURCES_FLAG)) {
				Log.e("Land", "vueland 3");
				sendDataToDataentryScreen(b);
			}
		}

		if (mVueAiselFragment != null) {
			mImageDetailsArr = mVueAiselFragment.getAisleWindowImgList();
			if (mBottomAdapter != null) {
				mBottomAdapter.notifyDataSetChanged();
			}
			if (mTopAdapter != null) {
				mTopAdapter.notifyDataSetChanged();
			}
		}
		if (!isSlidePanleLoaded) {
			isSlidePanleLoaded = true;
			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					if (mVueAiselFragment != null) {
						if (mImageDetailsArr != null) {
							mImageDetailsArr = mVueAiselFragment
									.getAisleWindowImgList();
						}
						mBitmapLoaderUtils = BitmapLoaderUtils.getInstance();
						if (null != mImageDetailsArr
								&& mImageDetailsArr.size() != 0) {
							mBottomAdapter = new ComparisionAdapter(
									AisleDetailsViewActivity.this);
							mTopAdapter = new ComparisionAdapter(
									AisleDetailsViewActivity.this);
							mBottomScroller.setAdapter(mBottomAdapter);
							mTopScroller.setAdapter(mTopAdapter);

						}
					}
				}

			}, 500);
		}

	}

	private Handler mHandler = new Handler() {

		@SuppressWarnings("deprecation")
		@SuppressLint("HandlerLeak")
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
			} else if (mSlidingDrawer.isOpened()) {
				mSlidingDrawer.close();
			} else {
				if (mVueAiselFragment == null) {
					mVueAiselFragment = (VueAisleDetailsViewFragment) getSupportFragmentManager()
							.findFragmentById(R.id.aisle_details_view_fragment);
				}
				mVueAiselFragment.setAisleContentListenerNull();
				MContentLinearLay.removeAllViews();
				for (int i = 0; i < mImageDetailsArr.size(); i++) {
					// mBitmapLoaderUtils.removeBitmapFromCache(mImageDetailsArr.get(i));
				}
				super.onBackPressed();
			}
		}
		return false;

	}

	@Override
	protected void onDestroy() {
		Log.e("ondestory", "ondestory detailsview");
		super.onDestroy();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == VueConstants.INVITE_FRIENDS_LOGINACTIVITY_REQUEST_CODE
				&& resultCode == VueConstants.INVITE_FRIENDS_LOGINACTIVITY_REQUEST_CODE) {
			if (data != null) {
				if (data.getStringExtra(VueConstants.INVITE_FRIENDS_LOGINACTIVITY_BUNDLE_STRING_KEY) != null) {
					mFrag.getFriendsList(data
							.getStringExtra(VueConstants.INVITE_FRIENDS_LOGINACTIVITY_BUNDLE_STRING_KEY));
				}
			}
		} else if (requestCode == VueConstants.FROM_DETAILS_SCREEN_TO_DATAENTRY_SCREEN_ACTIVITY_RESULT
				&& resultCode == VueConstants.FROM_DETAILS_SCREEN_TO_DATAENTRY_SCREEN_ACTIVITY_RESULT) {
			VueApplication.getInstance().mAisleImagePathList.clear();
			Bundle b = data.getExtras();
			if (b != null) {
				String findAt = b
						.getString(VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_FINDAT);
				if (mVueAiselFragment == null) {
					mVueAiselFragment = (VueAisleDetailsViewFragment) getSupportFragmentManager()
							.findFragmentById(R.id.aisle_details_view_fragment);
				}
				if (findAt != null) {
					mVueAiselFragment.mEditTextFindAt.setText(findAt);
				}

				String imagePath = b
						.getString(VueConstants.CREATE_AISLE_CAMERA_GALLERY_IMAGE_PATH_BUNDLE_KEY);
				if (imagePath != null) {
					addImageToAisle(imagePath);
				}

			}
		} else if (requestCode == VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_ACTIVITY_RESULT
				&& resultCode == VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_ACTIVITY_RESULT) {
			Bundle b = data.getExtras();
			if (b != null) {
				sendDataToDataentryScreen(b);
			}
		} else {

			try {
				if (mVueAiselFragment == null) {
					mVueAiselFragment = (VueAisleDetailsViewFragment) getSupportFragmentManager()
							.findFragmentById(R.id.aisle_details_view_fragment);
				}
				if (mVueAiselFragment.mAisleDetailsAdapter.mShare.mShareIntentCalled) {
					mVueAiselFragment.mAisleDetailsAdapter.mShare.mShareIntentCalled = false;
					mVueAiselFragment.mAisleDetailsAdapter.mShare
							.dismisDialog();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	/*
	 * public void loadBitmap(String url, AisleContentBrowser flipper, ImageView
	 * imageView, int bestHeight) {
	 * 
	 * // if (cancelPotentialDownload(loc, imageView)) { BitmapWorkerTask task =
	 * new BitmapWorkerTask(flipper, imageView, bestHeight); ((ScaleImageView)
	 * imageView).setOpaqueWorkerObject(task); task.execute(url); // } }
	 */

	class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
		private final WeakReference<ImageView> imageViewReference;
		// private final WeakReference<AisleContentBrowser>viewFlipperReference;
		private String url = null;
		private String serverImageUrl = null;
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
			serverImageUrl = params[1];
			Bitmap bmp = null;
			// we want to get the bitmap and also add it into the memory cache
			bmp = mBitmapLoaderUtils.getBitmap(url, serverImageUrl, true,
					mBestHeight);

			// bmp = getBitmap(url, true, mBestHeight);
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
			if (!isActionBarShown) {
				isActionBarShown = true;
				// getSupportActionBar().hide();
			}

		}

		@Override
		public void hideActionBar() {
			if (isActionBarShown) {
				if (mCurrentapiVersion >= 11) {
					// getSupportActionBar().hide();
				}
				isActionBarShown = false;
			}

		}

	}

	private void sendDataToDataentryScreen(Bundle b) {
		Log.e("Land", "vueland 4");
		String lookingFor, occation, category/* , saySomething */;
		if (mVueAiselFragment == null) {
			mVueAiselFragment = (VueAisleDetailsViewFragment) getSupportFragmentManager()
					.findFragmentById(R.id.aisle_details_view_fragment);
		}
		AisleContext aisleInfo = mVueAiselFragment.getAisleContext();
		lookingFor = aisleInfo.mLookingForItem;
		occation = aisleInfo.mOccasion;
		category = aisleInfo.mCategory;
		String imagePath = b
				.getString(VueConstants.CREATE_AISLE_CAMERA_GALLERY_IMAGE_PATH_BUNDLE_KEY);
		Intent intent = new Intent(this, DataEntryActivity.class);
		Bundle b1 = new Bundle();
		b1.putString(
				VueConstants.CREATE_AISLE_CAMERA_GALLERY_IMAGE_PATH_BUNDLE_KEY,
				imagePath);
		b1.putBoolean(
				VueConstants.FROM_DETAILS_SCREEN_TO_DATAENTRY_SCREEN_FLAG, true);
		b1.putBoolean(
				VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_IS_USER_AISLE_FLAG,
				false);
		b1.putString(
				VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_LOOKINGFOR,
				lookingFor);
		b1.putString(
				VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_OCCASION,
				occation);
		b1.putString(
				VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_CATEGORY,
				category);
		b1.putString(
				VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_SAYSOMETHINGABOUTAISLE,
				null);
		b1.putString(VueConstants.FROM_OTHER_SOURCES_URL,
				b.getString(VueConstants.FROM_OTHER_SOURCES_URL));
		b1.putBoolean(VueConstants.FROM_OTHER_SOURCES_FLAG,
				b.getBoolean(VueConstants.FROM_OTHER_SOURCES_FLAG));
		b1.putParcelableArrayList(
				VueConstants.FROM_OTHER_SOURCES_IMAGE_URIS,
				b.getParcelableArrayList(VueConstants.FROM_OTHER_SOURCES_IMAGE_URIS));
		String findAt = null;
		findAt = mVueAiselFragment.mEditTextFindAt.getText().toString();
		b1.putString(
				VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_FINDAT,
				findAt);
		intent.putExtras(b1);
		Log.e("Land", "vueland 5");
		this.startActivityForResult(
				intent,
				VueConstants.FROM_DETAILS_SCREEN_TO_DATAENTRY_SCREEN_ACTIVITY_RESULT);
	}

	/*
	 * This function is strictly for use by internal APIs. Not that we have
	 * anything external but there is some trickery here! The getBitmap function
	 * cannot be invoked from the UI thread. Having to deal with complexity of
	 * when & how to call this API is too much for those who just want to have
	 * the bitmap. This is a utility function and is public because it is to be
	 * shared by other components in the internal implementation.
	 */
	private Bitmap getBitmap(String url, boolean cacheBitmap, int bestHeight) {
		Log.i("added url", "added url  getBitmap " + url);
		File f = mFileCache.getFile(url);
		Log.i("added url", "added url  getBitmap " + f);
		// from SD cache
		Bitmap b = decodeFile(f, bestHeight);
		Log.i("added url", "added url  getBitmap " + b);
		if (b != null) {

			if (cacheBitmap)
				mAisleImagesCache.putBitmap(url, b);
			return b;
		}

		// from web
		try {
			if (url == null || url.length() < 1) {

				return null;
			}
			Bitmap bitmap = null;
			URL imageUrl = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) imageUrl
					.openConnection();
			conn.setConnectTimeout(30000);
			conn.setReadTimeout(30000);
			conn.setInstanceFollowRedirects(true);
			InputStream is = conn.getInputStream();
			Log.i("added url", "added url  InputStream " + is);
			Log.i("added url", "added url  InputStream url " + url);

			int hashCode = url.hashCode();
			String filename = String.valueOf(hashCode);
			Log.i("added url", "added url  InputStream imgname " + filename);
			OutputStream os = new FileOutputStream(f);
			Utils.CopyStream(is, os);
			os.close();
			bitmap = decodeFile(f, bestHeight);
			if (cacheBitmap)
				mAisleImagesCache.putBitmap(url, bitmap);

			return bitmap;
		} catch (Throwable ex) {
			ex.printStackTrace();
			if (ex instanceof OutOfMemoryError) {
				// mAisleImagesCache.clear();
			}
			return null;
		}
	}

	// decodes image and scales it to reduce memory consumption
	public Bitmap decodeFile(File f, int bestHeight) {
		Log.i("added url", "added url in  decodeFile: bestheight is "
				+ bestHeight);

		try {
			// decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			FileInputStream stream1 = new FileInputStream(f);
			BitmapFactory.decodeStream(stream1, null, o);
			stream1.close();
			// Find the correct scale value. It should be the power of 2.
			// final int REQUIRED_SIZE = mScreenWidth/2;
			int height = o.outHeight;
			int width = o.outWidth;
			Log.i("added url", "added urldecodeFile  bitmap o.height : "
					+ height);
			Log.i("added url", "added urldecodeFile  bitmap o.width : " + width);
			int reqWidth = VueApplication.getInstance()
					.getVueDetailsCardWidth();

			int scale = 1;

			if (height > bestHeight) {

				// Calculate ratios of height and width to requested height and
				// width
				final int heightRatio = Math.round((float) height
						/ (float) bestHeight);
				final int widthRatio = Math.round((float) width
						/ (float) reqWidth);

				// Choose the smallest ratio as inSampleSize value, this will
				// guarantee
				// a final image with both dimensions larger than or equal to
				// the
				// requested height and width.
				scale = heightRatio; // < widthRatio ? heightRatio : widthRatio;

			}

			// decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			// o2.inSampleSize = o.inSampleSize;
			// if(DEBUG) Log.d("Jaws","using inSampleSizeScale = " + scale +
			// " original width = " + o.outWidth + "screen width = " +
			// mScreenWidth);
			FileInputStream stream2 = new FileInputStream(f);
			Bitmap bitmap = BitmapFactory.decodeStream(stream2, null, o2);

			stream2.close();
			if (bitmap != null) {
				width = bitmap.getWidth();
				height = bitmap.getHeight();

				if (width > reqWidth) {

					float tempHeight = (height * reqWidth) / width;
					height = (int) tempHeight;
					/*
					 * Bitmap bitmaptest = Bitmap.createScaledBitmap(bitmap,
					 * reqWidth, height, true);
					 */
					bitmap = getModifiedBitmap(bitmap, reqWidth, height);

				}
			}
			if (bitmap != null) {
				Log.i("added url",
						"added url  urldecodeFile width " + bitmap.getWidth());

			} else {
				Log.i("added url", "added urldecodeFile  bitmap null ");
			}
			return bitmap;
		} catch (FileNotFoundException e) {
			Log.i("added url", "added urldecodeFile  filenotfound exception ");
		} catch (IOException e) {
			Log.i("added url", "added urldecodeFile  io exception ");
			e.printStackTrace();
		} catch (Throwable ex) {
			Log.i("added url", "added urldecodeFile  throwable exception ");
			ex.printStackTrace();
			if (ex instanceof OutOfMemoryError) {
				// mAisleImagesCache.clear();
			}
			return null;
		}
		return null;
	}

	private Bitmap getModifiedBitmap(Bitmap originalImage, int width, int height) {
		// here width & height are the desired width & height values)

		// first lets create a new bitmap and a canvas to draw into it.
		Bitmap newBitmap = Bitmap.createBitmap((int) width, (int) height,
				Config.ARGB_8888);
		float originalWidth = originalImage.getWidth(), originalHeight = originalImage
				.getHeight();
		Canvas canvas = new Canvas(newBitmap);
		float scale = width / originalWidth;
		float xTranslation = 0.0f, yTranslation = (height - originalHeight
				* scale) / 2.0f;
		Matrix transformation = new Matrix();
		// now that we have the transformations, set that for our drawing ops
		transformation.postTranslate(xTranslation, yTranslation);
		transformation.preScale(scale, scale);
		// create a paint and draw into new canvas
		Paint paint = new Paint();
		paint.setFilterBitmap(true);
		canvas.drawBitmap(originalImage, transformation, paint);
		Log.i("imagenotcoming",
				"bitmap issue scalleddown: originalbitmap width "
						+ newBitmap.getWidth());
		Log.i("imagenotcoming",
				"bitmap issue:scalleddown originalbitmap height:  "
						+ newBitmap.getHeight());
		return newBitmap;
	}

	private void addImageToAisle(String imagePath) {
		FileCache fileCache = new FileCache(this);
		File f = fileCache.getFile(imagePath);
		File sourceFile = new File(imagePath);
		Bitmap bmp = BitmapLoaderUtils.getInstance().decodeFile(sourceFile,
				VueApplication.getInstance().mScreenHeight);
		Utils.saveBitmap(bmp, f);
		if (mVueAiselFragment == null) {
			mVueAiselFragment = (VueAisleDetailsViewFragment) getSupportFragmentManager()
					.findFragmentById(R.id.aisle_details_view_fragment);
		}
		mVueAiselFragment.addAisleToWindow(bmp, imagePath);
	}
}
