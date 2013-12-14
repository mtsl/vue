package com.lateralthoughts.vue;

import java.util.ArrayList;
import java.util.Collections;

import com.android.volley.toolbox.ImageLoader;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.crittercism.app.Crittercism;
import com.lateralthoughts.vue.ui.ScaleImageView;
import com.lateralthoughts.vue.utils.BitmapLruCache;
import com.lateralthoughts.vue.utils.FileCache;
import com.lateralthoughts.vue.utils.ListFragementObj;
import com.lateralthoughts.vue.utils.ShoppingApplicationDetails;
import com.lateralthoughts.vue.utils.SortBasedOnAppName;
import com.lateralthoughts.vue.utils.Utils;

public class VueApplication extends Application {
	private static VueApplication sInstance;
	private static final String CRITTERCISM_APP_ID = "5153c41e558d6a2403000009";
	private HttpClient mHttpClient;
	private FileCache mFileCache;
	private ScaleImageView mEmptyImageView;
	private String mWindowID;
	private int mWindowCount;
	private int mStatusBarHeight;
	private int mVueDetailsCardWidth = 0;
	private int mVueDetailsCardHeight = 0;
	public VueLandingPageActivity landingPage;
	public boolean mShareViaVueClickedFlag = false;
	public String mShareViaVueClickedAisleId = null;
	public String mShareViaVueClickedImageId = null;
	public boolean mNewViewSelection = false;
	public String mNewlySelectedView;
	public ArrayList<ShoppingApplicationDetails> mShoppingApplicationDetailsList;
	public ArrayList<ShoppingApplicationDetails> mMoreInstalledApplicationDetailsList;
	public static final int[] POPUP_ITEM_DRAWABLES = {
			R.drawable.composer_camera, R.drawable.composer_music,
			R.drawable.composer_place, R.drawable.composer_sleep,
			R.drawable.composer_thought };

	public long mLaunchTime;
	public long mLastRecordedTime;
	ListFragementObj mListRefresobj;

    public static final String MORE_AISLES_REQUEST_TAG = "MoreAislesTag";
    public static final String LOAD_IMAGES_REQUEST_TAG="LoadImagesTag";

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

	private String mUserInitials = null;

	public Long getmUserId() {
		return mUserId;
	}

	public void setmUserId(Long mUserId) {
		this.mUserId = mUserId;
	}

	public boolean ismFinishDetailsScreenFlag() {
		return mFinishDetailsScreenFlag;
	}

	public void setmFinishDetailsScreenFlag(boolean mFinishDetailsScreenFlag) {
		this.mFinishDetailsScreenFlag = mFinishDetailsScreenFlag;
	}

	private boolean mFinishDetailsScreenFlag;

	private Long mUserId = null;
	private String mUserName = null;

	public String getmUserName() {
		return mUserName;
	}

	public void setmUserName(String mUserName) {
		this.mUserName = mUserName;
	}

	public String getmUserInitials() {
		return mUserInitials;
	}

	public void setmUserInitials(String mUserInitials) {
		this.mUserInitials = mUserInitials;
	}

	public int getmAisleImgCurrentPos() {
		return mAisleImgCurrentPos;
	}

	public void setmAisleImgCurrentPos(int mAisleImgCurrentPos) {
		this.mAisleImgCurrentPos = mAisleImgCurrentPos;
	}

	public boolean mFbsharingflag = false;
	private RequestQueue mVolleyRequestQueue;
	private static final String[] SHOPPINGAPP_NAMES_ARRAY = { "Amazon", "eBay",
			"iShop", "Jewellery", "OLX", "Pinterest", "ZOVI" };
	private static final String[] SHOPPINGAPP_ACTIVITIES_ARRAY = {
			"com.amazon.mShop.home.HomeActivity",
			"com.ebay.mobile.activities.eBay", "com.shopping.StartPage",
			"com.greybit.jewellery.activity.Start", "com.olx.olx.activity.Olx",
			"com.pinterest.activity.PinterestActivity",
			"com.robemall.zovi.HomeActivity" };
	public static final String[] SHOPPINGAPP_PACKAGES_ARRAY = {
			"com.amazon.mShop.android", "com.ebay.mobile", "com.shopping",
			"com.greybit.jewellery", "com.olx.olx", "com.pinterest",
			"com.robemall.zovi" };

	public boolean mIsTrendingSelectedFromBezelMenuFlag = false;

	@SuppressWarnings("unchecked")
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
				Drawable appIcon = null;
				try {
					appIcon = this.getPackageManager().getApplicationIcon(
							SHOPPINGAPP_PACKAGES_ARRAY[i]);
				} catch (NameNotFoundException e) {
				}
				ShoppingApplicationDetails shoppingApplicationDetails = new ShoppingApplicationDetails(
						SHOPPINGAPP_NAMES_ARRAY[i],
						SHOPPINGAPP_ACTIVITIES_ARRAY[i],
						SHOPPINGAPP_PACKAGES_ARRAY[i], appIcon);
				mShoppingApplicationDetailsList.add(shoppingApplicationDetails);
			}
		}

		mMoreInstalledApplicationDetailsList = Utils
				.getInstalledApplicationsList(getApplicationContext());
		if (mMoreInstalledApplicationDetailsList != null) {
			Collections.sort(mMoreInstalledApplicationDetailsList,
					new SortBasedOnAppName());
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
    public void setListRefreshFrag(ListFragementObj obj){
    	mListRefresobj = obj;
    }
    public ListFragementObj getListRefdreshFrag(){
		
    	return mListRefresobj;
    }
	public int getPixel(int dp) {
		Resources r = getResources();
		int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
				dp, r.getDisplayMetrics());
		return px;
	}

	public int getVueDetailsCardWidth() {
		if (mVueDetailsCardWidth == 0) {
			int leftRightMargins = 13;
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

	/*
	 * public BitmapCache getBitmapCache() { if (sBitmapCache == null)
	 * sBitmapCache = new BitmapCache(512);
	 * 
	 * return sBitmapCache; }
	 */
    public ImageLoader getImageCacheLoader(){
        return mImageLoader;
    }
    private BitmapCache sBitmapCache;
    private ImageLoader mImageLoader;

}
