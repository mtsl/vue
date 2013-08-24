package com.lateralthoughts.vue;

import java.util.ArrayList;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.crittercism.app.Crittercism;
import com.lateralthoughts.vue.ui.ScaleImageView;
import com.lateralthoughts.vue.utils.FileCache;
import com.lateralthoughts.vue.utils.ShoppingApplicationDetails;
import com.lateralthoughts.vue.utils.Utils;

public class VueApplication extends Application {
	private static VueApplication sInstance;

	// private VueMemoryCache<Bitmap> mVueAisleImagesCache;
	// private VueMemoryCache<Bitmap> mAisleContentCache;
	// private VueMemoryCache<String> mVueAisleOwnerNamesCache;
	// private VueMemoryCache<String> mVueAisleContextInfoCache;
	private static final String CRITTERCISM_APP_ID = "5153c41e558d6a2403000009";
	private HttpClient mHttpClient;
	private FileCache mFileCache;
	private ScaleImageView mEmptyImageView;
	private String mWindowID;
	private int mWindowCount;
	private int mStatusBarHeight;
	private int mVueDetailsCardWidth = 0;
	private int mVueDetailsCardHeight = 0;
	private boolean newVueTrendingAislesDataModel = false;
	public ArrayList<String> mAisleImagePathList = new ArrayList<String>();
	public ArrayList<ShoppingApplicationDetails> mShoppingApplicationDetailsList;

	public boolean ismFromDetailsScreenToDataentryCreateAisleScreenFlag() {
		return mFromDetailsScreenToDataentryCreateAisleScreenFlag;
	}

	public void setmFromDetailsScreenToDataentryCreateAisleScreenFlag(
			boolean mFromDetailsScreenToDataentryCreateAisleScreenFlag) {
		this.mFromDetailsScreenToDataentryCreateAisleScreenFlag = mFromDetailsScreenToDataentryCreateAisleScreenFlag;
	}

	private boolean mFromDetailsScreenToDataentryCreateAisleScreenFlag;

	public int getmStatusBarHeight() {
		return mStatusBarHeight;
	}

	public void setmStatusBarHeight(int mStatusBarHeight) {
		this.mStatusBarHeight = mStatusBarHeight;
	}

	public int mScreenHeight;
	public int mScreenWidth;
	private int mTextSize = 18;
	public Context mVueApplicationContext;
	private int mAisleImgCurrentPos;

	public int getmAisleImgCurrentPos() {
		return mAisleImgCurrentPos;
	}

	public void setmAisleImgCurrentPos(int mAisleImgCurrentPos) {
		this.mAisleImgCurrentPos = mAisleImgCurrentPos;
	}

	// public int totalDataDownload = 0;
	public boolean mFbsharingflag = false;
	private RequestQueue mVolleyRequestQueue;
	private static final String[] SHOPPINGAPP_NAMES_ARRAY = { "Amazon", "eBay",
			"Etsy", "Fancy", "iShop", "Jewellery", "OLX", "Pinterest", "ZOVI" };
	private static final String[] SHOPPINGAPP_ACTIVITIES_ARRAY = {
			"com.amazon.mShop.home.HomeActivity",
			"com.ebay.mobile.activities.eBay",
			"com.etsy.android.ui.HomeActivity", "com.thefancy.app.common.Main",
			"com.shopping.StartPage", "com.greybit.jewellery.activity.Start",
			"com.olx.olx.activity.Olx",
			"com.pinterest.activity.PinterestActivity",
			"com.robemall.zovi.HomeActivity" };
	private static final String[] SHOPPINGAPP_PACKAGES_ARRAY = {
			"com.amazon.mShop.android", "com.ebay.mobile", "com.etsy.android",
			"com.thefancy.app", "com.shopping", "com.greybit.jewellery",
			"com.olx.olx", "com.pinterest", "com.robemall.zovi" };

	@Override
	public void onCreate() {
		super.onCreate();

		sInstance = this;

		mVueApplicationContext = this;

		// mVueAisleImagesCache = new VueMemoryCache<Bitmap>();
		// mVueAisleImagesCache.setLimit(40);
		// mVueAisleOwnerNamesCache = new VueMemoryCache<String>();
		// mVueAisleOwnerNamesCache.setLimit(1);
		// mVueAisleContextInfoCache = new VueMemoryCache<String>();
		// mVueAisleContextInfoCache.setLimit(1);
		ScaledImageViewFactory.getInstance(this);
		AisleWindowContentFactory.getInstance(this);

		// mAisleContentCache = new VueMemoryCache<Bitmap>();
		// mAisleContentCache.setLimit(10);

		mHttpClient = new DefaultHttpClient();
		mFileCache = new FileCache(this);

		ContentAdapterFactory.getInstance(this);

		// create the JSONObject. (Do not forget to import org.json.JSONObject!)
		JSONObject crittercismConfig = new JSONObject();
		try {
			crittercismConfig.put("shouldCollectLogcat", true); // send logcat
																// data for
																// devices with
																// API Level 16
																// and higher
		} catch (JSONException je) {
		}

		mEmptyImageView = new ScaleImageView(this);
		Drawable d = getResources().getDrawable(R.drawable.aisle_content_empty);
		mEmptyImageView.setImageDrawable(d);

		DisplayMetrics dm = getResources().getDisplayMetrics();
		mScreenHeight = dm.heightPixels;
		mScreenWidth = dm.widthPixels;
		mVolleyRequestQueue = Volley.newRequestQueue(this);

		mShoppingApplicationDetailsList = new ArrayList<ShoppingApplicationDetails>();
		for (int i = 0; i < SHOPPINGAPP_NAMES_ARRAY.length; i++) {
			if (Utils.appInstalledOrNot(SHOPPINGAPP_PACKAGES_ARRAY[i], this)) {
				ShoppingApplicationDetails shoppingApplicationDetails = new ShoppingApplicationDetails(
						SHOPPINGAPP_NAMES_ARRAY[i],
						SHOPPINGAPP_ACTIVITIES_ARRAY[i],
						SHOPPINGAPP_PACKAGES_ARRAY[i]);
				mShoppingApplicationDetailsList.add(shoppingApplicationDetails);
			}
		}

		// R.drawable.aisle_content_empty;
		Crittercism.init(getApplicationContext(), CRITTERCISM_APP_ID,
				crittercismConfig);
	}

	public static VueApplication getInstance() {
		return sInstance;
	}

	/*
	 * public VueMemoryCache<Bitmap> getAisleImagesMemCache() { return
	 * mVueAisleImagesCache; }
	 */

	public HttpClient getHttpClient() {
		return mHttpClient;
	}

	/*
	 * public VueMemoryCache<Bitmap> getAisleContentCache() { return
	 * mAisleContentCache; }
	 */

	public FileCache getFileCache() {
		return mFileCache;
	}

	public int getScreenHeight() {
		return mScreenHeight;
	}

	public int getScreenWidth() {
		return mScreenWidth;
	}

	public void setClickedWindowID(String id) {
		mWindowID = id;
	}

	public String getClickedWindowID() {
		return mWindowID;
	}

	public void setClickedWindowCount(int count) {
		mWindowCount = count;
	}

	public int getClickedWindowCount() {
		return mWindowCount;
	}

	public int getmTextSize() {
		return mTextSize;
	}

	public int getPixel(int dp) {
		Resources r = getResources();
		int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
				dp, r.getDisplayMetrics());
		return px;
	}

	public int getVueDetailsCardWidth() {
		if (mVueDetailsCardWidth == 0) {
			int leftRightMargins = 20;
			mVueDetailsCardWidth = mScreenWidth - getPixel(leftRightMargins);
		}
		return mVueDetailsCardWidth;
	}

	public int getVueDetailsCardHeight() {
		if (mVueDetailsCardHeight == 0) {
			int statusBarHeight = getmStatusBarHeight();
			if (statusBarHeight == 0) {
				statusBarHeight = 24;
			}

			// 4+4 top bottom margins
			// 8 dot indicator height.
			int topBottomMargin = 4 + 4 + 8 + statusBarHeight;
			topBottomMargin = getPixel(topBottomMargin);
			mVueDetailsCardHeight = mScreenHeight - getPixel(topBottomMargin);
		}
		return mVueDetailsCardHeight;
	}

	public RequestQueue getRequestQueue() {
		if (mVolleyRequestQueue != null) {
			return mVolleyRequestQueue;
		} else {
			throw new IllegalStateException("RequestQueue not initialized");
		}
	}

	/*
	 * public boolean newVueTrendingAislesDataModel() { Log.e("Profiling",
	 * "Profiling newVueTrendingAislesDataModel : " +
	 * newVueTrendingAislesDataModel); return newVueTrendingAislesDataModel; }
	 */

	/*
	 * public void setNewVueTrendingAislesDataModel (boolean createNewObject) {
	 * newVueTrendingAislesDataModel = createNewObject; }
	 */

}
