package com.lateralthoughts.vue;

//android imports
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ResultReceiver;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
//java imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.lateralthoughts.vue.connectivity.DataBaseManager;
import com.lateralthoughts.vue.connectivity.DbHelper;
import com.lateralthoughts.vue.connectivity.VueBatteryManager;
import com.lateralthoughts.vue.connectivity.VueConnectivityManager;
import com.lateralthoughts.vue.ui.NotifyProgress;

public class VueTrendingAislesDataModel {

	private static final boolean DEBUG = false;
	private Context mContext;
	private static VueTrendingAislesDataModel sVueTrendingAislesDataModel;
	private ArrayList<IAisleDataObserver> mAisleDataObserver;

	// ========================= START OF PARSING TAGS
	// ========================================================
	// the following strings are pre-defined to help with JSON parsing
	// the tags defined here should be in sync the API documentation for the
	// backend
	private static final String ITEM_CATEGORY_TAG = "category";
	private static final String LOOKING_FOR_TAG = "lookingFor";
	private static final String OCCASION_TAG = "occasion";
	private static final String CONTENT_ID_TAG = "id";
	private static final String USER_OBJECT_TAG = "user";
	private static final String USER_FIRST_NAME_TAG = "firstName";
	private static final String USER_LAST_NAME_TAG = "lastName";
	private static final String USER_JOIN_TIME_TAG = "joinTime";
	private static final String USER_IMAGES_TAG = "images";
	private static final String USER_IMAGE_ID_TAG = "id";
	private static final String USER_IMAGE_DETALS_TAG = "detailsUrl";
	private static final String USER_IMAGE_URL_TAG = "imageUrl";
	private static final String USER_IMAGE_STORE_TAG = "store";
	private static final String USER_IMAGE_TITLE_TAG = "title";
	private static final String IMAGE_HEIGHT_TAG = "height";
	private static final String IMAGE_WIDTH_TAG = "width";
	private ArrayList<AisleWindowContent> mAisleContentList;
	NotifyProgress mNotifyProgress;
	private HashMap<String, AisleWindowContent> mAisleContentListMap = new HashMap<String, AisleWindowContent>();

	// private static final int TRENDING_AISLES_SAMPLE_SIZE = 100;
	private static final int TRENDING_AISLES_BATCH_SIZE = 5;
	public static final int TRENDING_AISLES_BATCH_INITIAL_SIZE = 10;
	private static final int NOTIFICATION_THRESHOLD = 4;
	private int mPoolSize = 1;
	private int mMaxPoolSize = 1;
	private long mKeepAliveTime = 10;
	public boolean mMoreDataAvailable;
	private boolean mMarkAislesToDelete = false;

	// ===== The following set of variables are used for state management
	// ==================================
	private int mState;
	// this variable above is usually set to one of the following values
	private final int AISLE_TRENDING_LIST_DATA = 1;
	// private final int AISLE_TRENDING_CONTENT_DATA = 2;
	// ====== End of state variables
	// ========================================================================

	protected int mLimit;
	protected int mOffset;
	private AisleWindowContentFactory mAisleWindowContentFactory;
	private boolean mAisleDataRequested;
	private long mRequestStartTime;
	protected VueContentGateway mVueContentGateway;
	protected TrendingAislesContentParser mTrendingAislesParser;

	private final String TAG = "VueTrendingAislesModel";
	public boolean loadOnRequest = false;
	private ThreadPoolExecutor threadPool;
	private final LinkedBlockingQueue<Runnable> threadsQueue = new LinkedBlockingQueue<Runnable>();
	private DataBaseManager mDbManager;
	public boolean isDownloadFail = false;
	public boolean cancleDownload = false;

	private VueTrendingAislesDataModel(Context context) {
		Log.e("Profiling", "Profining DATA CHECK VueTrendingAislesDataModel()");
		mContext = context;
		isDownloadFail = false;
		cancleDownload = false;
		mVueContentGateway = VueContentGateway.getInstance();
		mAisleWindowContentFactory = AisleWindowContentFactory
				.getInstance(mContext);
		mTrendingAislesParser = new TrendingAislesContentParser(new Handler());
		mAisleDataObserver = new ArrayList<IAisleDataObserver>();

		mLimit = TRENDING_AISLES_BATCH_INITIAL_SIZE;
		mOffset = 0;
		mState = AISLE_TRENDING_LIST_DATA;
		mAisleContentList = new ArrayList<AisleWindowContent>();
		mDbManager = DataBaseManager.getInstance(mContext);
		threadPool = new ThreadPoolExecutor(mPoolSize, mMaxPoolSize,
				mKeepAliveTime, TimeUnit.SECONDS, threadsQueue);
		loadData();
	}

	public void registerAisleDataObserver(IAisleDataObserver observer) {
		if (!mAisleDataObserver.contains(observer))
			mAisleDataObserver.add(observer);

		// but if we already have the data we should notify right away
		observer.onAisleDataUpdated(mAisleContentList.size());
	}

	// this class is used to handle the aisle parsing
	private class TrendingAislesContentParser extends ResultReceiver {
		public TrendingAislesContentParser(Handler handler) {
			super(handler);
		}

		@Override
		protected void onReceiveResult(int resultCode, Bundle resultData) {
			switch (mState) {
			case AISLE_TRENDING_LIST_DATA:
				// we were expecting a list of currently trending aisles list
				// parse and get this result
				// If onCreateView was already invoked before we got this call
				// we
				// should
				// have a valid mTrendingAislesContentView and mContentAdapter.
				// Add the AisleWindowContent information to the adapter so that
				// we
				// can
				// start displaying stuff!
				// mAdapterState = AISLE_TRENDING_CONTENT_DATA; //we are no
				// longer
				// waiting for list!
				long elapsed = System.currentTimeMillis() - mRequestStartTime;
				if (mAisleDataRequested) {
					if (DEBUG)
						Log.e(TAG, "It took " + elapsed
								+ " seconds for first request to return");
					mAisleDataRequested = false;
				}
				if (!mMoreDataAvailable) {
					if (DEBUG)
						Log.e(TAG, "No more data is available. mOffset = "
								+ mOffset);
				} else {
					if (cancleDownload) {
						cancleDownload = false;
						return;
					}
					parseTrendingAislesResultData(resultData
							.getString("result"));
					// if(mOffset > NOTIFICATION_THRESHOLD *
					// TRENDING_AISLES_BATCH_INITIAL_SIZE){
					// if this is the first set of data we are receiving go
					// ahead
					// notify the data set changed
					for (IAisleDataObserver observer : mAisleDataObserver) {
						observer.onAisleDataUpdated(mAisleContentList.size());
					}
					// notifyDataSetChanged();
					if (mOffset < NOTIFICATION_THRESHOLD
							* TRENDING_AISLES_BATCH_SIZE)
						mOffset += mLimit;
					else {
						mOffset += mLimit;
						mLimit = TRENDING_AISLES_BATCH_SIZE;
					}
					if (DEBUG)
						Log.e(TAG, "There is more data to parse. offset = "
								+ mOffset);
					if (!VueBatteryManager.isConnected(mContext)
							&& VueBatteryManager.batteryLevel(mContext) < VueBatteryManager.MINIMUM_BATTERY_LEVEL) {
						mMoreDataAvailable = false;
						loadOnRequest = true;
					}
				}
				if (mMoreDataAvailable) {
					mVueContentGateway.getTrendingAisles(mLimit, mOffset, this);
				}
				break;

			default:
				// we should never have to encounter this!
				break;
			}
		}

		// TODO: code cleanup. This function here allocated the new
		// AisleImageObjects but re-uses the
		// imageItemsArray. Instead the called function clones and keeps a copy.
		// This is pretty inconsistent.
		// Let the allocation happen in one place for both items. Fix this!
		@SuppressWarnings("unused")
		private void parseTrendingAislesResultData(String resultString) {
			String category;
			String aisleId;
			String context;
			String occasion;
			String ownerFirstName;
			String ownerLastName;
			long joinTime;
			AisleContext userInfo;

			AisleWindowContent aisleItem = null;
			ArrayList<AisleImageDetails> imageItemsArray = new ArrayList<AisleImageDetails>();
			JSONObject imageItem;
			String imageUrl;
			String imageDetalsUrl;
			String imageId;
			String imageStore;
			String imageTitle;
			AisleImageDetails imageItemDetails;
			JSONArray contentArray = null;
			try {
				contentArray = handleResponce(resultString);
				if (contentArray == null) {
					return;
				}
				for (int i = 0; i < contentArray.length(); i++) {
					userInfo = new AisleContext();
					JSONObject contentItem = contentArray.getJSONObject(i);
					category = contentItem.getString(ITEM_CATEGORY_TAG);
					aisleId = contentItem.getString(CONTENT_ID_TAG);
					userInfo.mAisleId = contentItem.getString(CONTENT_ID_TAG);
					JSONArray imagesArray = contentItem
							.getJSONArray(USER_IMAGES_TAG);

					// within the content item we have a user object
					JSONObject user = contentItem
							.getJSONObject(USER_OBJECT_TAG);
					userInfo.mFirstName = user.getString(USER_FIRST_NAME_TAG);
					userInfo.mLastName = user.getString(USER_LAST_NAME_TAG);
					userInfo.mUserId = user.getString(CONTENT_ID_TAG);
					userInfo.mLookingForItem = contentItem
							.getString(LOOKING_FOR_TAG);
					userInfo.mOccasion = contentItem.getString(OCCASION_TAG);
					userInfo.mCategory = category;
					userInfo.mJoinTime = Long.parseLong(user
							.getString(USER_JOIN_TIME_TAG));
					for (int j = 0; j < imagesArray.length(); j++) {
						imageItemDetails = new AisleImageDetails();
						imageItem = imagesArray.getJSONObject(j);
						imageItemDetails.mDetalsUrl = imageItem
								.getString(USER_IMAGE_DETALS_TAG);
						imageItemDetails.mId = imageItem
								.getString(USER_IMAGE_ID_TAG);
						imageItemDetails.mStore = imageItem
								.getString(USER_IMAGE_STORE_TAG);
						imageItemDetails.mTitle = imageItem
								.getString(USER_IMAGE_TITLE_TAG);
						imageItemDetails.mImageUrl = imageItem
								.getString(USER_IMAGE_URL_TAG);
						imageItemDetails.mAvailableHeight = imageItem
								.getInt(IMAGE_HEIGHT_TAG);
						imageItemDetails.mAvailableWidth = imageItem
								.getInt(IMAGE_WIDTH_TAG);
						imageItemsArray.add(imageItemDetails);
					}
					aisleItem = getAisleItem(aisleId);
					aisleItem.addAisleContent(userInfo, imageItemsArray);
					imageItemsArray.clear();
				}
			} catch (JSONException ex1) {
				if (DEBUG)
					Log.e(TAG,
							"Some exception is caught? ex1 = " + ex1.toString());
			}
		}
	}

	private JSONArray handleResponce(String resultString) {
		JSONArray contentArray = null;
		try {
			contentArray = new JSONArray(resultString);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		Log.e("VueTrendingDatamodel", "handle response :::: " + resultString);
		if (resultString.equals("error") || 0 == contentArray.length()) {
			Log.e("VueTrendingDatamodel", "handle response if called :::: "
					+ resultString);
			if (resultString != null && resultString.equals("error")) {
				isDownloadFail = true;
			}
			mMoreDataAvailable = false;
			if (!mMarkAislesToDelete
					&& (contentArray == null || 0 == contentArray.length())) {
				mMarkAislesToDelete = DataBaseManager
						.markOldAislesToDelete(mContext);
			}
			runTask(new Runnable() {

				@Override
				public void run() {
					// To do AislesList is getting 0 in the middile.
					try {
						DataBaseManager
								.addTrentingAislesFromServerToDB(mContext);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
		return contentArray;
	}

	private AisleWindowContent getAisleItem(String aisleId) {
		AisleWindowContent aisleItem = null;
		aisleItem = mAisleContentListMap.get(aisleId);
		if (null == aisleItem) {
			if (null != mAisleContentListMap
					.get(mAisleWindowContentFactory.EMPTY_AISLE_ID)) {
				aisleItem = mAisleContentListMap
						.get(mAisleWindowContentFactory.EMPTY_AISLE_ID);
				if (mAisleContentList.contains(aisleItem)) {
					int index = mAisleContentList.indexOf(aisleItem);
					mAisleContentList.remove(index);
					mAisleContentList.add(index, aisleItem);
				}
			} else {
				aisleItem = mAisleWindowContentFactory.getEmptyAisleWindow();

			}
			aisleItem.setAisleId(aisleId);
			mAisleContentListMap.put(aisleId, aisleItem);
			mAisleContentList.add(aisleItem);
		}
		return aisleItem;
	}

	public int getAisleCount() {
		if (null != mAisleContentList) {
			Log.i("mAisleContentList", " mAisleContentList size is:  "
					+ mAisleContentList.size());
			return mAisleContentList.size();
		} else {
			Log.i("mAisleContentList",
					" mAisleContentList is null returining 0");
		}
		return 0;
	}

	public AisleWindowContent getAisleAt(int position) {
		return mAisleContentList.get(position);
	}

	public static VueTrendingAislesDataModel getInstance(Context context) {
		if (null == sVueTrendingAislesDataModel) {
			Log.e("Profiling", "Profiling getInstance()111 : new instance");
			sVueTrendingAislesDataModel = new VueTrendingAislesDataModel(
					context);
		}
		return sVueTrendingAislesDataModel;
	}

	public void clearAisles() {
		if (mAisleContentListMap != null) {
			mAisleContentListMap.clear();
		}
		if (mAisleContentList != null) {
			mAisleContentList.clear();
		}
		for (IAisleDataObserver observer : mAisleDataObserver) {
			observer.onAisleDataUpdated(mAisleContentList.size());
		}
	}

	public void loadMoreAisles() {
		mMoreDataAvailable = true;
		loadOnRequest = false;
		if (mOffset < NOTIFICATION_THRESHOLD * TRENDING_AISLES_BATCH_SIZE)
			mOffset += mLimit;
		else {
			mOffset += mLimit;
			mLimit = TRENDING_AISLES_BATCH_SIZE;
		}
		mVueContentGateway.getTrendingAisles(mLimit, mOffset,
				mTrendingAislesParser);
	}

	public void loadData() {
		if (!VueConnectivityManager.isNetworkConnected(mContext)) {
			Toast.makeText(mContext, R.string.no_network, Toast.LENGTH_SHORT)
					.show();
			isDownloadFail = true;
			ArrayList<AisleWindowContent> aisleContentArray = mDbManager
					.getAislesFromDB(null);
			if (aisleContentArray.size() == 0) {
				return;
			}
			Message msg = new Message();
			msg.obj = aisleContentArray;
			mHandler.sendMessage(msg);
		} else {
			// initializeTrendingAisleContent();
			mMoreDataAvailable = true;
			mVueContentGateway.getTrendingAisles(mLimit, mOffset,
					mTrendingAislesParser);
		}
	}

	protected Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			@SuppressWarnings("unchecked")
			ArrayList<AisleWindowContent> aisleContentArray = (ArrayList<AisleWindowContent>) msg.obj;
			for (AisleWindowContent content : aisleContentArray) {
				AisleWindowContent aisleItem = getAisleItem(content
						.getAisleId());
				aisleItem.addAisleContent(content.getAisleContext(),
						content.getImageList());
			}
			for (IAisleDataObserver observer : mAisleDataObserver) {
				observer.onAisleDataUpdated(mAisleContentList.size());
			}
			runTask(new Runnable() {
				public void run() {
					ArrayList<AisleWindowContent> aislesList = mDbManager
							.getAislesFromDB(null);
					if (aislesList.size() == 0) {
						return;
					}
					Message msg = new Message();
					msg.obj = aislesList;
					mHandler.sendMessage(msg);
				};
			});

		};
	};

	/**
	 * to start thread pool.
	 * 
	 * @param task
	 *            Runnable
	 */
	public void runTask(Runnable task) {
		threadPool.execute(task);
	}

	public void displayCategoryAisles(String category, NotifyProgress progress) {
		mNotifyProgress = progress;
		DbDataGetter dBgetter = new DbDataGetter();
		dBgetter.execute(category);
	}

	/*
	 * protected abstract class VueHandler { public abstract void
	 * handleMessage(android.os.Message msg);
	 * 
	 * public void sendMessage(android.os.Message msg) { handleMessage(msg); } }
	 */

	private static void copyDB() {
		try {
			File sd = Environment.getExternalStorageDirectory();
			File data = Environment.getDataDirectory();

			if (sd.canWrite()) {
				String currentDBPath = "//data//com.lateralthoughts.vue//databases//"
						+ DbHelper.DATABASE_NAME;
				String backupDBPath = DbHelper.DATABASE_NAME;
				File currentDB = new File(data, currentDBPath);
				File backupDB = new File(sd, backupDBPath);

				if (currentDB.exists()) {
					FileChannel src = new FileInputStream(currentDB)
							.getChannel();
					FileChannel dst = new FileOutputStream(backupDB)
							.getChannel();
					dst.transferFrom(src, 0, src.size());
					src.close();
					dst.close();
				}
			}
		} catch (Exception e) {
		}
	}

	private class DbDataGetter extends AsyncTask<String, Void, Void> {
		String category;
		ArrayList<AisleWindowContent> aisleWindowList;

		@Override
		protected void onPreExecute() {
			mNotifyProgress.showProgress();
			clearAisles();
			AisleWindowContentFactory.getInstance(VueApplication.getInstance())
					.clearObjectsInUse();
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(String... params) {
			category = params[0];
			aisleWindowList = mDbManager.getAislesByCategory(category);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			for (AisleWindowContent content : aisleWindowList) {
				AisleWindowContent aisleItem = getAisleItem(content
						.getAisleId());
				aisleItem.addAisleContent(content.getAisleContext(),
						content.getImageList());
				// getAisleItem(content.getAisleId());
			}
			for (IAisleDataObserver observer : mAisleDataObserver) {
				observer.onAisleDataUpdated(mAisleContentList.size());
			}
			mNotifyProgress.dismissProgress();
			super.onPostExecute(result);
		}
	}
}
