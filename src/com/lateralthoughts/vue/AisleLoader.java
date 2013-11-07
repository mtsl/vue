package com.lateralthoughts.vue;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

//import com.lateralthoughts.vue.TrendingAislesAdapter.ViewHolder;
import com.lateralthoughts.vue.ui.AisleContentBrowser;
import com.lateralthoughts.vue.ui.ScaleImageView;
import com.lateralthoughts.vue.ui.AisleContentBrowser.AisleContentClickListener;
import com.lateralthoughts.vue.utils.BitmapLoaderUtils;
import com.lateralthoughts.vue.utils.FileCache;
import com.lateralthoughts.vue.utils.Utils;
import com.lateralthoughts.vue.TrendingAislesGenericAdapter.ViewHolder;

public class AisleLoader {
	private static final boolean DEBUG = false;
	private static final String TAG = "AisleLoader";
	Handler handler = new Handler();
	private Context mContext;
	private ContentAdapterFactory mContentAdapterFactory;

	private static AisleLoader sAisleLoaderInstance = null;
	private ScaledImageViewFactory mViewFactory = null;
	private BitmapLoaderUtils mBitmapLoaderUtils;
	AisleContentClickListener mListener;

	// private HashMap<String, ViewHolder> mContentViewMap = new HashMap<String,
	// ViewHolder>();
	// private List<ViewHolder> browserList = new
	// ArrayList<TrendingAislesGenericAdapter.ViewHolder>();

	// Design Notes: The SGV is powered by data from the TrendingAislesAdapter.
	// This adapter starts
	// the information flow by requesting top aisles in batches. As the aisle
	// details start coming through the adapter notifies the view of changes in
	// data set which in turn triggers the getView() callback.
	// The complexity starts at this point: we are dealing with
	// an incredibly large amount of data. Each aisle window makes up
	// one item in the SGV. Each of this window consists of an image, below
	// which we can description of the image, the profile of the owner, the
	// context,
	// the occasion etc. On top of this, the image itself can be flicked to
	// reveal
	// a carousel of images that a user can swipe through. We can't possibly
	// download all of these and more importantly, we want to have top
	// performance for a couple of very important scenarios:
	// 1. When the user flings the SGV up & down the scrolling needs to be
	// smooth
	// 2. User should be able to swipe across as AisleWindow and browse the
	// content.
	// Here is what we will do:
	// 1. When an AisleWindowContent needs to be loaded up, we also get
	// the view into which it goes. Inside this viewFlipper we will store the
	// id of the AisleWindowContent.
	// 2. When the viewFlipper is being recycled the if of the
	// AisleWindowContent
	// and the id that ViewFlipper points to will be different. At this point
	// we will cancel all image download requests started for this ViewFlipper
	// 3. If the ids match then we don't need to do anything.

	// In addition, we should consider keeping a pool of ScaledImageView objects
	// for efficiency. Right now, we are creating ScaledImageView objects
	// everytime
	// we handle getView() and thats definitely hurting us!
	public static AisleLoader getInstance(Context context) {
		if (null == sAisleLoaderInstance) {
			sAisleLoaderInstance = new AisleLoader(context);
		}
		return sAisleLoaderInstance;
	}

	private AisleLoader(Context context) {
		// we don't want everyone creating an instance of this. We will
		// instead use a factory pattern and return an instance using a
		// static method
		mContext = context;
		mViewFactory = ScaledImageViewFactory.getInstance(context);
		mBitmapLoaderUtils = BitmapLoaderUtils.getInstance();
		mContentAdapterFactory = ContentAdapterFactory.getInstance(mContext);
		// mTopBottomMargin = (int)
		// Utils.dipToPixels(VueApplication.getInstance(), mTopBottomMargin);
		if (DEBUG)
			Log.e(TAG, "Log something to remove warning");
	}

	// This method adds the intelligence to fetch the contents of this aisle
	// window and
	// updates the relevant view so that the item is visible to the user.
	// Some caveats: This API is aware of the internals of the View - i.e.,
	// ViewHolder
	// which represents one item in the staggered grid view.
	// This is less than ideal but it doesn't make sense to aritificially
	// constrain this
	// class from being aware of the UI side of things.
	// The logic itself is reasonably simple: given an AisleWindowContent object
	// we first determine
	// the number of images that need to be in this view item. We then need to
	// determine how to
	// load each of the images in a non-intrusive way.
	// I have implemented a relatively robust asynctask pattern for this: for
	// each image view,
	// start an async task and use a standard DownloadedDrawable object to keep
	// track of the task
	// When the task completes check to make sure that the url for which the
	// task was started is still
	// valid. If so, add the downloaded image to the view object
	@SuppressWarnings("deprecation")
	public void getAisleContentIntoView(ViewHolder holder, int scrollIndex,
			int position, boolean placeholderOnly,
			AisleContentClickListener listener) {

		Log.i("TrendingDataModel",
				"DataObserver for List Refresh: getAisleContentView called "
						+ holder.mWindowContent.getAisleId() + "???" + position
						+ "????" + placeholderOnly);
		ScaleImageView imageView = null;
		ArrayList<AisleImageDetails> imageDetailsArr = null;
		AisleImageDetails itemDetails = null;
		AisleContentBrowser contentBrowser = null;

		if (null == holder)
			return;
		AisleWindowContent windowContent = holder.mWindowContent;

		if (null == windowContent)
			return;

		// String currentContentId = holder.aisleContentBrowser.getUniqueId();

		String desiredContentId = windowContent.getAisleId();
		contentBrowser = holder.aisleContentBrowser;
		if(Utils.isAisleChanged) {
			if(Utils.mChangeAilseId != null && desiredContentId.equalsIgnoreCase(Utils.mChangeAilseId)) {
			//Utils.isAisleChanged = false;
			Utils.mChangeAilseId = null;
			holder.uniqueContentId = AisleWindowContent.EMPTY_AISLE_CONTENT_ID;
			
			}
			
		}
		
		if (holder.uniqueContentId.equals(desiredContentId)) {
			// we are looking at a visual object that has either not been used
			// before or has to be filled with same content. Either way, no need
			// to worry about cleaning up anything!
			holder.aisleContentBrowser.setScrollIndex(scrollIndex);
			Log.i("memory issue", "memory issue aisle missing return  "+windowContent.getAisleId());

			return;
		} else {
			Log.i("memory issue", "memory issue aisle missing 1  "+windowContent.getAisleId());
			Log.i("memory issue", "memory issue aisle  list size   "+VueTrendingAislesDataModel.getInstance(VueApplication.getInstance()).listSize());
			 
			// we are going to re-use an existing object to show some new
			// content
			// lets release the scaleimageviews first
			for (int i = 0; i < contentBrowser.getChildCount(); i++) {
				// ((ScaleImageView)contentBrowser.getChildAt(i)).setContainerObject(null);
				mViewFactory
						.returnUsedImageView((ScaleImageView) contentBrowser
								.getChildAt(i));
			}
			Log.i("calling", "calling new view   ");
			IAisleContentAdapter adapter = mContentAdapterFactory
					.getAisleContentAdapter();
			mContentAdapterFactory.returnUsedAdapter(holder.aisleContentBrowser
					.getCustomAdapter());
			holder.aisleContentBrowser.setCustomAdapter(null);
			adapter.setContentSource(desiredContentId, holder.mWindowContent);
			holder.aisleContentBrowser.setCustomAdapter(adapter);
			holder.uniqueContentId = desiredContentId;
			holder.aisleContentBrowser.removeAllViews();
			holder.aisleContentBrowser.setUniqueId(desiredContentId);
			holder.aisleContentBrowser.setScrollIndex(scrollIndex);
			holder.aisleContentBrowser.setCustomAdapter(adapter);
			holder.uniqueContentId = desiredContentId;
			// mContentViewMap.put(holder.uniqueContentId, holder);
		}
		mListener = listener;
		imageDetailsArr = windowContent.getImageList();
		if (null != imageDetailsArr && imageDetailsArr.size() != 0) {
			itemDetails = imageDetailsArr.get(0);
			imageView = mViewFactory.getPreconfiguredImageView(position);
			imageView.setContainerObject(holder);

			Log.i("AisleLoader", "CustomImageUrl:? "
					+ itemDetails.mCustomImageUrl);
			Bitmap bitmap = null;
			/*
			 * Bitmap bitmap = mBitmapLoaderUtils
			 * .getCachedBitmap(itemDetails.mCustomImageUrl);
			 */
			int bestHeight = windowContent.getBestHeightForWindow();
			FrameLayout.LayoutParams mShowpieceParams2 = new FrameLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, itemDetails.mTrendingImageHeight);

			Log.i("cardHeight", "bestsamallest cardHeight bestHeight11: "
					+ bestHeight);
			contentBrowser.setLayoutParams(mShowpieceParams2);
			Log.i("bestsamallest", "bestsamallest height22: "
					+ itemDetails.mTrendingImageHeight);
			if (bitmap != null) {
				imageView.setImageBitmap(bitmap);
				contentBrowser.addView(imageView);
			} else {
				contentBrowser.addView(imageView);
				if (!placeholderOnly)
					loadBitmap(itemDetails.mCustomImageUrl,
							itemDetails.mImageUrl, contentBrowser, imageView,
							bestHeight, windowContent.getAisleId(),
							itemDetails, listener);
			}
		}
		if (VueApplication.getInstance().getmUserId() != null) {
			if (String.valueOf(VueApplication.getInstance().getmUserId())
					.equals(windowContent.getAisleContext().mUserId)) {
				File f = new FileCache(mContext)
						.getVueAppUserProfilePictureFile(VueConstants.USER_PROFILE_IMAGE_FILE_NAME);
				if (f.exists()) {
					holder.profileThumbnail
							.setBackgroundDrawable(new BitmapDrawable(
									BitmapFactory.decodeFile(f.getPath())));
				} else {
					holder.profileThumbnail
							.setBackgroundResource(R.drawable.profile_thumbnail);
				}
			} else {
				holder.profileThumbnail
						.setBackgroundResource(R.drawable.profile_thumbnail);
			}
		} else {
			holder.profileThumbnail
					.setBackgroundResource(R.drawable.profile_thumbnail);
		}
		 
	}

	public void loadBitmap(String loc, String serverImageUrl,
			AisleContentBrowser flipper, ImageView imageView, int bestHeight,
			String asileId, AisleImageDetails itemDetails,AisleContentClickListener listener) {
		 
		if(Utils.isAisleChanged){
			Utils.isAisleChanged = false;
			BitmapWorkerTask task = new BitmapWorkerTask(flipper, imageView,
					bestHeight, asileId, itemDetails,listener);
			((ScaleImageView) imageView).setOpaqueWorkerObject(task);
			String[] urlsArray = { loc, serverImageUrl };
			task.execute(urlsArray);
		} else {
		if (cancelPotentialDownload(loc, imageView)) {
			BitmapWorkerTask task = new BitmapWorkerTask(flipper, imageView,
					bestHeight, asileId, itemDetails,listener);
			((ScaleImageView) imageView).setOpaqueWorkerObject(task);
			String[] urlsArray = { loc, serverImageUrl };
			task.execute(urlsArray);
		}
		}
		/*
		 * ((ScaleImageView) imageView).setImageUrl(serverImageUrl, new
		 * ImageLoader(VueApplication.getInstance().getRequestQueue(),
		 * VueApplication.getInstance().getBitmapCache()),
		 * VueApplication.getInstance().getVueDetailsCardWidth() / 2,
		 * bestHeight);
		 */
	}

	class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
		private final WeakReference<ImageView> imageViewReference;
		private final WeakReference<AisleContentBrowser> viewFlipperReference;
		private String url = null;
		private int mBestHeight;

		AisleImageDetails mItemDetails;
		AisleContentClickListener mListener;

		public BitmapWorkerTask(AisleContentBrowser vFlipper,
				ImageView imageView, int bestHeight, String aisleId,
				AisleImageDetails itemDetails,AisleContentClickListener listener) {
			// Use a WeakReference to ensure the ImageView can be garbage
			// collected
			imageViewReference = new WeakReference<ImageView>(imageView);
			viewFlipperReference = new WeakReference<AisleContentBrowser>(
					vFlipper);
			mListener = listener;
			mItemDetails = itemDetails;
		}

		// Decode image in background.
		@Override
		protected Bitmap doInBackground(String... params) {
			if(!mListener.isFlingCalled()){
			Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
			Log.i("downloading speed", "fling calls stop");
			} else {
				Log.i("downloading speed", "fling calls");
			}
			url = params[0];
			Bitmap bmp = null;
			// we want to get the bitmap and also add it into the memory cache
			boolean cacheBitmap = false;
			bmp = mBitmapLoaderUtils.getBitmap(url, params[1], cacheBitmap,
					mItemDetails.mTrendingImageHeight,
					mItemDetails.mTrendingImageWidth, Utils.TRENDING_SCREEN);
			return bmp;
		}

		// Once complete, see if ImageView is still around and set bitmap.
		@Override
		protected void onPostExecute(Bitmap bitmap) {

			if (viewFlipperReference != null && imageViewReference != null
					&& bitmap != null) {
				Log.i("memory issue", "memory issue aisle missing  bitmap not null  ");
				final ImageView imageView = imageViewReference.get();
				BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

				if (this == bitmapWorkerTask) {
					ViewHolder holder = (ViewHolder) ((ScaleImageView) imageView)
							.getContainerObject();
					if (null != holder) {
						holder.aisleContext.setVisibility(View.VISIBLE);
						holder.aisleOwnersName.setVisibility(View.VISIBLE);
						holder.profileThumbnail.setVisibility(View.VISIBLE);
						holder.aisleDescriptor.setVisibility(View.VISIBLE);
					}
					imageView.setImageBitmap(bitmap);
					if (mListener.isFlingCalled()) {
						// mListener.refreshList();
					}
				}
			} else {
				Log.i("memory issue", "memory issue aisle  bitmap is   null  ");
			}
		}
	}

	// utility functions to keep track of all the async tasks that we
	// instantiate
	private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
		if (imageView != null) {
			Object task = ((ScaleImageView) imageView).getOpaqueWorkerObject();
			if (task instanceof BitmapWorkerTask) {
				BitmapWorkerTask workerTask = (BitmapWorkerTask) task;
				return workerTask;
			}
		}
		return null;
	}

	private static boolean cancelPotentialDownload(String url,
			ImageView imageView) {
		BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

		if (bitmapWorkerTask != null) {
			String bitmapUrl = bitmapWorkerTask.url;
			if ((bitmapUrl == null) || (!bitmapUrl.equals(url))) {
				bitmapWorkerTask.cancel(true);
			} else {
				// The same URL is already being downloaded.
				return false;
			}
		}
		return true;
	}
}
