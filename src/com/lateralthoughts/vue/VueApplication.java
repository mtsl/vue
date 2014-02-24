package com.lateralthoughts.vue;

import gcm.com.vue.android.gcmclient.RegisterGCMClient;

import java.util.ArrayList;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.LruCache;
import android.util.TypedValue;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.crittercism.app.Crittercism;
import com.lateralthoughts.vue.ui.ScaleImageView;
import com.lateralthoughts.vue.utils.FileCache;
import com.lateralthoughts.vue.utils.ListFragementObj;
import com.lateralthoughts.vue.utils.Logging;
import com.lateralthoughts.vue.utils.ShoppingApplicationDetails;
import com.lateralthoughts.vue.utils.UrlConstants;
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
	public long mLaunchTime;
	public long mLastRecordedTime;
	ListFragementObj mListRefresobj;
	public String MIXPANEL_TOKEN = "d5ac13097eaf8acefc264d21457307a1";//"178a869c17a98b1f044ae5548ad9f4c4"; // Vidya
																		// token
																		// new
	/*
	 * "0b560991fe02d44932efedeff66e54f5" Vidya token old not working ;
	 *//* "d5ac13097eaf8acefc264d21457307a1" Surendra token working */

	public static final String MORE_AISLES_REQUEST_TAG = "MoreAislesTag";
	public static final String LOAD_IMAGES_REQUEST_TAG = "LoadImagesTag";
	public static final int LOG_LOW = 0;
	public static final int LOG_MED = 1;
	public static final int LOG_HIGH = 2;
	public boolean isUserSwipeAisle = false;
	private AisleWindowContent mAisleWindow;

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
	private String mUserEmail = null;

	public String getmUserEmail() {
		return mUserEmail;
	}

	public void setmUserEmail(String mUserEmail) {
		this.mUserEmail = mUserEmail;
	}

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
	public static final String[] SHOPPINGAPP_NAMES_ARRAY = { "Etsy", "Fancy",
			"Wanelo", "Amazon", "Pinterest" };
	public static final String[] SHOPPINGAPP_ACTIVITIES_ARRAY = {
			"com.etsy.android.ui.HomeActivity", "com.thefancy.app.common.Main",
			"com.wanelo.android.ui.activity.LoginActivity",
			"com.amazon.mShop.home.HomeActivity",
			"com.pinterest.activity.PinterestActivity" };
	public static final String[] SHOPPINGAPP_PACKAGES_ARRAY = {
			"com.etsy.android", "com.thefancy.app", "com.wanelo.android",
			"com.amazon.mShop.android", "com.pinterest" };

	public boolean mIsTrendingSelectedFromBezelMenuFlag = false;
	private final int MAX_BITMAP_COUNT = 128;

	@Override
	public void onCreate() {
		super.onCreate();
		if(Utils.sIsLoged){
		    Logging.i("profile", "profile app oncreate started");
		}
		sInstance = this;
		mVueApplicationContext = this;
		RegisterGCMClient.registerClient(VueApplication.getInstance(),
				UrlConstants.CURRENT_SERVER_PROJECT_ID);
		ScaledImageViewFactory.getInstance(this);
		AisleWindowContentFactory.getInstance(this);
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
		getInstalledApplications(this);
		Crittercism.init(getApplicationContext(), CRITTERCISM_APP_ID,
				crittercismConfig);
		mImageLoader = new NetworkImageLoader(mVolleyRequestQueue,
				new ImageLoader.ImageCache() {
					private final LruCache<String, Bitmap> mCache = new LruCache<String, Bitmap>(
							MAX_BITMAP_COUNT);

					public void putBitmap(String url, Bitmap bitmap) {
						mCache.put(url, bitmap);
					}

					public Bitmap getBitmap(String url) {
						return mCache.get(url);
					}
				});
		if(Utils.sIsLoged){
            Logging.i("profile", "profile app oncreate ended");
        }
	}

	public static VueApplication getInstance() {
		return sInstance;
	}

	public HttpClient getHttpClient() {
		if (mHttpClient == null) {
			mHttpClient = new DefaultHttpClient();
		}
		return mHttpClient;
	}

	public FileCache getFileCache() {
		if (mFileCache == null) {
			mFileCache = new FileCache(this);
		}
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

	public void setListRefreshFrag(ListFragementObj obj) {
		mListRefresobj = obj;
	}

	public ListFragementObj getListRefdreshFrag() {

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
			mVolleyRequestQueue = Volley.newRequestQueue(this);
			return mVolleyRequestQueue;
			// throw new IllegalStateException("RequestQueue not initialized");
		}
	}

	public ImageLoader getImageCacheLoader() {
		return mImageLoader;
	}

	private ImageLoader mImageLoader;

	public void saveTrendingRefreshTime(long time_in_mins) {
		SharedPreferences sharedPreferencesObj = this.getSharedPreferences(
				VueConstants.SHAREDPREFERENCE_NAME, 0);
		Editor editor = sharedPreferencesObj.edit();
		editor.putLong(VueConstants.SCREEN_REFRESH_TIME, time_in_mins);
		editor.commit();
	}

	public void setPendingAisle(AisleWindowContent aisleWindow) {
		mAisleWindow = aisleWindow;
	}

	public AisleWindowContent getPedningAisle() {
		return mAisleWindow;
	}

	private void getInstalledApplications(final Context context) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				mShoppingApplicationDetailsList = new ArrayList<ShoppingApplicationDetails>();
				for (int i = 0; i < SHOPPINGAPP_NAMES_ARRAY.length; i++) {
					if (Utils.appInstalledOrNot(SHOPPINGAPP_PACKAGES_ARRAY[i],
							context)) {
						Drawable appIcon = null;
						try {
							appIcon = context.getPackageManager()
									.getApplicationIcon(
											SHOPPINGAPP_PACKAGES_ARRAY[i]);
						} catch (NameNotFoundException e) {
						}
						ShoppingApplicationDetails shoppingApplicationDetails = new ShoppingApplicationDetails(
								SHOPPINGAPP_NAMES_ARRAY[i],
								SHOPPINGAPP_ACTIVITIES_ARRAY[i],
								SHOPPINGAPP_PACKAGES_ARRAY[i], appIcon);
						mShoppingApplicationDetailsList
								.add(shoppingApplicationDetails);
					}
				}
				mMoreInstalledApplicationDetailsList = Utils
						.getInstalledApplicationsList(getApplicationContext());

			}
		}).start();

	}
	/*
	 * public void registerUser(MixpanelAPI mixpanel) { VueUser storedVueUser =
	 * null; try { storedVueUser = Utils.readUserObjectFromFile(
	 * VueApplication.getInstance(), VueConstants.VUE_APP_USEROBJECT__FILENAME);
	 * } catch (Exception e2) { e2.printStackTrace(); } if (storedVueUser !=
	 * null) ; JSONObject nameTag = new JSONObject(); try { // Set an
	 * "mp_name_tag" super property // for Streams if you find it useful.
	 * nameTag.put("mp_name_tag", storedVueUser.getFirstName() + " " +
	 * storedVueUser.getLastName()); mixpanel.registerSuperProperties(nameTag);
	 * } catch (JSONException e) { e.printStackTrace(); } }
	 * 
	 * public void unregisterUser(MixpanelAPI mixpanel) { VueUser storedVueUser
	 * = null; try { storedVueUser = Utils.readUserObjectFromFile(
	 * VueApplication.getInstance(), VueConstants.VUE_APP_USEROBJECT__FILENAME);
	 * mixpanel.unregisterSuperProperty(storedVueUser.getFirstName() + " " +
	 * storedVueUser.getLastName()); //mixpanel.clearSuperProperties(); } catch
	 * (Exception e2) { e2.printStackTrace(); } }
	 */

}
