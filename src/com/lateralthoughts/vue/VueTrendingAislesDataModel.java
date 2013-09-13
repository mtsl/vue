package com.lateralthoughts.vue;

//android imports
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.lateralthoughts.vue.connectivity.DataBaseManager;
import com.lateralthoughts.vue.connectivity.DbHelper;
import com.lateralthoughts.vue.connectivity.NetworkHandler;
import com.lateralthoughts.vue.connectivity.TrendingAislesContentParser;
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
	private ArrayList<AisleWindowContent> mAisleContentList;
	NotifyProgress mNotifyProgress;
	boolean mRequestToServer;
	ArrayList<String> aisleIds = new ArrayList<String>();
	private HashMap<String, AisleWindowContent> mAisleContentListMap = new HashMap<String, AisleWindowContent>();

	// private static final int TRENDING_AISLES_SAMPLE_SIZE = 100;
	//private static final int TRENDING_AISLES_BATCH_SIZE = 10;
	public static final int TRENDING_AISLES_BATCH_INITIAL_SIZE = 10;
	//private static final int NOTIFICATION_THRESHOLD = 4;
	private int mPoolSize = 1;
	private int mMaxPoolSize = 1;
	private long mKeepAliveTime = 10;
	public boolean mMoreDataAvailable = true;
	private boolean mMarkAislesToDelete = false;

	// ===== The following set of variables are used for state management
	// ==================================
	private int mState;
	// this variable above is usually set to one of the following values
	private final int AISLE_TRENDING_LIST_DATA = 1;
	// private final int AISLE_TRENDING_CONTENT_DATA = 2;
	// ====== End of state variables
	// ========================================================================

	//protected int mLimit;
	//protected int mOffset;
/*	public int getmOffset() {
		return mOffset;
	}

	public void setmOffset(int mOffset) {
		this.mOffset = mOffset;
	}*/

	private AisleWindowContentFactory mAisleWindowContentFactory;
	private boolean mAisleDataRequested;
	private long mRequestStartTime;
	private final String TAG = "VueTrendingAislesModel";
	public boolean loadOnRequest = false;
	private ThreadPoolExecutor threadPool;
	private final LinkedBlockingQueue<Runnable> threadsQueue = new LinkedBlockingQueue<Runnable>();
	private DataBaseManager mDbManager;
	public boolean isDownloadFail = false;
	public boolean cancleDownload = false;
	NetworkHandler mNetworkHandler;

	private VueTrendingAislesDataModel(Context context) {
		Log.e("Profiling", "Profining DATA CHECK VueTrendingAislesDataModel()");
		mContext = context;
		isDownloadFail = false;
		cancleDownload = false;
		mAisleWindowContentFactory = AisleWindowContentFactory
				.getInstance(mContext);
		mAisleDataObserver = new ArrayList<IAisleDataObserver>();

		//mLimit = TRENDING_AISLES_BATCH_INITIAL_SIZE;
		//mOffset = 0;
		mState = AISLE_TRENDING_LIST_DATA;
		mAisleContentList = new ArrayList<AisleWindowContent>();
		mDbManager = DataBaseManager.getInstance(mContext);
		threadPool = new ThreadPoolExecutor(mPoolSize, mMaxPoolSize,
				mKeepAliveTime, TimeUnit.SECONDS, threadsQueue);
		//loadData(true);
		mNetworkHandler = new NetworkHandler(mContext);
		mNetworkHandler.loadInitialData(true, mHandler);
	}
    public NetworkHandler getNetworkHandler(){
    	return mNetworkHandler;
    }
	public void registerAisleDataObserver(IAisleDataObserver observer) {
		if (!mAisleDataObserver.contains(observer))
			mAisleDataObserver.add(observer);

		// but if we already have the data we should notify right away
		observer.onAisleDataUpdated(mAisleContentList.size());
	}
 
	public void insertNewAisleToDb(String aisleId){
		Log.e("Profiling", "Profiling inserting new aisles to db id: user created2********************** "+aisleId);
		aisleIds.add(aisleId);
		writeToDb();
	}
public void writeToDb(){
	new Thread(new Runnable() {
		
		@Override
		public void run() {
			Log.e("Profiling", "Profiling inserting new aisles to db id: user created3********************** ");
			DataBaseManager
			.addTrentingAislesFromServerToDB(mContext); //TODO: need to add arrayList as second parameter.
			
		}
	}).start();
	 
}
	public AisleWindowContent getAisleItem(String aisleId) {
		AisleWindowContent aisleItem = null;
		aisleItem = mAisleContentListMap.get(aisleId);
		if (null == aisleItem) {
			aisleItem = getAisle(aisleId);
			aisleItem.setAisleId(aisleId);
		}
		return aisleItem;
	}
	public AisleWindowContent getAisle(String aisleId){
		AisleWindowContent aisleItem = null;
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
		//mAisleContentListMap.put(aisleId, aisleItem);
		//mAisleContentList.add(aisleItem);
		return aisleItem;
	}
    public void addItemToListAt(String aisleId,AisleWindowContent aisleItem,int position ){
    	mAisleContentListMap.put(aisleId, aisleItem);
		mAisleContentList.add(position, aisleItem);
    }
    public void addItemToList(String aisleId,AisleWindowContent aisleItem) {
    	mAisleContentListMap.put(aisleId, aisleItem);
		mAisleContentList.add(aisleItem);
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
	public AisleWindowContent getAisleAt(String aisleId) {
		return mAisleContentListMap.get(aisleId);
	}
	public static VueTrendingAislesDataModel getInstance(Context context) {
		if (null == sVueTrendingAislesDataModel) {
			Log.e("Profiling", "Profiling getInstance()111 : new instance");
			sVueTrendingAislesDataModel = new VueTrendingAislesDataModel(context);
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
		dataObserver();
	}
  public void dataObserver(){
		for (IAisleDataObserver observer : mAisleDataObserver) {
			observer.onAisleDataUpdated(mAisleContentList.size());
		} 
		loadOnRequest = true;
  }
  public void listSize(){
	  Log.i("mAisleContentList", "mAisleContentList: "+mAisleContentList.size());
  }
/*	public void loadMoreAisles(boolean loadMore) {
		Log.i("offeset and limit", "offeset1: load moredata");
		if(isMoreDataAvailable()){
		loadOnRequest = false;
		if (mOffset < NOTIFICATION_THRESHOLD * TRENDING_AISLES_BATCH_SIZE)
			mOffset += mLimit;
		else {
			mOffset += mLimit;
			mLimit = TRENDING_AISLES_BATCH_SIZE;
		}
		Log.i("offeset and limit", "offeset1: " + mOffset + " and limit: "+ mLimit);
		
		mVueContentGateway.getTrendingAisles(mLimit, mOffset,
				mTrendingAislesParser, loadMore);
		} else {
			Log.i("offeset and limit", "offeset1: else part");
		}
	}*/

/*	public void loadData(boolean loadMore) {
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
			setMoreDataAVailable(true);
			mVueContentGateway.getTrendingAisles(mLimit, mOffset,
					mTrendingAislesParser, loadMore);
		}
	}*/

	protected Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			@SuppressWarnings("unchecked")
			ArrayList<AisleWindowContent> aisleContentArray = (ArrayList<AisleWindowContent>) msg.obj;
			for (AisleWindowContent content : aisleContentArray) {
				AisleWindowContent aisleItem = getAisleItem(content
						.getAisleId());
				aisleItem.addAisleContent(content.getAisleContext(),
						content.getImageList());
				addItemToList(aisleItem.getAisleId(),aisleItem);
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
 public void setNotificationProgress( NotifyProgress progress,boolean fromServer){
	 mNotifyProgress = progress;
	 mRequestToServer = fromServer;
 }
/*	public void displayCategoryAisles(String category, NotifyProgress progress,
			boolean fromServer, boolean loadMore) {
		mRequestToServer = fromServer;
		mNotifyProgress = progress;
		String downLoadFromServer = "fromDb";
		if (fromServer == true) {
			 if(progress.isAlreadyDownloaed(category)){ 

			downLoadFromServer = "fromServer";
			mOffset = 0;
			mLimit = TRENDING_AISLES_BATCH_INITIAL_SIZE;
			clearContent();
			showProgress();
			setMoreDataAVailable(true);
			mVueContentGateway.getTrendingAisles(mLimit, mOffset,
					mTrendingAislesParser, loadMore);
			Log.i("loading from db", "loading from server");
			
			 * } else { downLoadFromServer = "fromDb"; DbDataGetter dBgetter =
			 * new DbDataGetter();
			 * dBgetter.execute(category,downLoadFromServer); }
			 
		} else {
			Log.i("loading from db", "loading from db");
			downLoadFromServer = "fromDb";
			DbDataGetter dBgetter = new DbDataGetter();
			dBgetter.execute(category, downLoadFromServer);
		}

	}*/
public boolean isMoreDataAvailable(){
	return mMoreDataAvailable;
}
public void setMoreDataAVailable(boolean dataState){
	mMoreDataAvailable = dataState;
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
/*
	private class DbDataGetter extends AsyncTask<String, Void, Void> {
		String category;
		ArrayList<AisleWindowContent> aisleWindowList;

		@Override
		protected void onPreExecute() {

			clearContent();

			mNotifyProgress.showProgress();
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(String... params) {
			category = params[0];
			aisleWindowList = mDbManager.getAislesByCategory(category);
			for (int i = 0; i < aisleWindowList.size(); i++) {
				if (aisleWindowList.get(i).getImageList().size() == 0) {
					Log.i("loading from db",
							"loading from db2 this window has no images:  "
									+ aisleWindowList.get(i).getAisleId());
					// Log.i("loading from db",
					// "loading from db2 this window has no images:  "+aisleWindowList.get(i).);
				}
			}
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
				addItemToList(aisleItem.getAisleId(),aisleItem);

			}
			for (IAisleDataObserver observer : mAisleDataObserver) {
				observer.onAisleDataUpdated(mAisleContentList.size());
			}
			dismissProgress();
			super.onPostExecute(result);
		}
	}*/

	public void clearContent() {
		clearAisles();
		AisleWindowContentFactory.getInstance(VueApplication.getInstance())
				.clearObjectsInUse();
	}

	public void showProgress() {
		mNotifyProgress.showProgress();
	}

	public void dismissProgress() {
		if (mNotifyProgress != null) {
			mNotifyProgress.dismissProgress(mRequestToServer);
		}
	}
}
