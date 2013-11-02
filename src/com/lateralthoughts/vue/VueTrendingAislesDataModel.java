package com.lateralthoughts.vue;

//android imports
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.lateralthoughts.vue.connectivity.DataBaseManager;
import com.lateralthoughts.vue.connectivity.DbHelper;
import com.lateralthoughts.vue.connectivity.NetworkHandler;
import com.lateralthoughts.vue.ui.NotifyProgress;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
	private HashMap<String, AisleWindowContent> mAisleContentListMap = new HashMap<String, AisleWindowContent>();
	private int mPoolSize = 1;
	private int mMaxPoolSize = 1;
	private long mKeepAliveTime = 10;
	public boolean mMoreDataAvailable = true;
	private boolean mMarkAislesToDelete = false;
	public boolean isFromDb = false;

	// ===== The following set of variables are used for state management
	// ==================================
	private int mState;
	// this variable above is usually set to one of the following values
	private final int AISLE_TRENDING_LIST_DATA = 1;
	// private final int AISLE_TRENDING_CONTENT_DATA = 2;
	// ====== End of state variables
	// ========================================================================
	private AisleWindowContentFactory mAisleWindowContentFactory;
	private boolean mAisleDataRequested;
	private long mRequestStartTime;
	private final String TAG = "VueTrendingAislesModel";
	public boolean loadOnRequest = true;
	private ThreadPoolExecutor threadPool;
	private final LinkedBlockingQueue<Runnable> threadsQueue = new LinkedBlockingQueue<Runnable>();
	public DataBaseManager mDbManager;
	NetworkHandler mNetworkHandler;

  private VueTrendingAislesDataModel(Context context) {
    Log.e("Profiling", "Profining DATA CHECK VueTrendingAislesDataModel()");
    mContext = context;
    mAisleWindowContentFactory = AisleWindowContentFactory
        .getInstance(mContext);
    mAisleDataObserver = new ArrayList<IAisleDataObserver>();
    mState = AISLE_TRENDING_LIST_DATA;
    mAisleContentList = new ArrayList<AisleWindowContent>();
    mDbManager = DataBaseManager.getInstance(mContext);
    threadPool = new ThreadPoolExecutor(mPoolSize, mMaxPoolSize,
        mKeepAliveTime, TimeUnit.SECONDS, threadsQueue);
    mNetworkHandler = new NetworkHandler(mContext);
    boolean loadMore = true;
    long elapsedTime = System.currentTimeMillis()
        - VueApplication.getInstance().mLastRecordedTime;
    Log.e("PERF_VUE", "about to invoke loadInitialData. Time elapsed = "
        + elapsedTime);
    VueApplication.getInstance().mLastRecordedTime = System.currentTimeMillis();
    mNetworkHandler.loadInitialData(loadMore, mHandler,
			mContext.getResources().getString(R.string.trending));  
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
 
	/*public void insertNewAisleToDb(String aisleId){
		Log.e("Profiling", "Profiling inserting new aisles to db id: user created2********************** "+aisleId);
		aisleIds.add(aisleId);
		writeToDb();
	}*/
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
    	  if(mAisleContentListMap.get(aisleId) == null) {
    	mAisleContentListMap.put(aisleId, aisleItem);
		mAisleContentList.add(aisleItem);
    	  }
    }
    public AisleWindowContent removeAisleFromList(int position){
    	return mAisleContentList.remove(position);
    }
	public int getAisleCount() {
		if (null != mAisleContentList) {
			return mAisleContentList.size();
		} else {
			Log.i("mAisleContentList",
					" mAisleContentList is null returining 0");
		}
		return 0;
	}
    public AisleWindowContent getAisleFromList(AisleWindowContent ailseItem) {
		int index = mAisleContentList.indexOf(ailseItem);
		if(index != -1 && index < mAisleContentList.size()){
		ailseItem = mAisleContentList.get(index);
		return ailseItem;
		}
		return null;
    }
	public AisleWindowContent getAisleAt(int position) {
		try {
			return mAisleContentList.get(position);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	public AisleWindowContent getAisleAt(String aisleId) {
		return mAisleContentListMap.get(aisleId);
	}
	public int getAilsePosition(AisleWindowContent aisleItem){
		return mAisleContentList.indexOf(aisleItem);
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
			Log.i("TrendingDataModel", "DataObserver for List Refresh:  ");
			observer.onAisleDataUpdated(mAisleContentList.size());
		}
		for(AisleWindowContent content : mAisleContentList) {
		  Log.e("TrendingDataModel", "bookmarkfeaturetest: count in TrendingDataModel: " + content.getAisleContext().mBookmarkCount);
		}
		loadOnRequest = true;
		Log.i("TrendingDataModel", "loadOnRequest:  "+loadOnRequest);
  }
  public int listSize(){
	   return mAisleContentList.size();
	 
  }
	public Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			@SuppressWarnings("unchecked")
			ArrayList<AisleWindowContent> aisleContentArray = (ArrayList<AisleWindowContent>) msg.obj;
			isFromDb = true;
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
					loadOnRequest = false;
					Log.i("TrendingDataModel", "loadOnRequest from db:  "+loadOnRequest);
					ArrayList<AisleWindowContent> aislesList = mDbManager
							.getAislesFromDB(null, false);
					Log.i("arrayList", "arrayList from db sized1: "+aislesList.size());
					if (aislesList.size() == 0) {
						loadOnRequest = true;
					  isFromDb = false;
					  VueTrendingAislesDataModel.getInstance(VueApplication.getInstance()).setmOffset(listSize());
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
 protected void setmOffset(int listSize) {
		// TODO Auto-generated method stub
		
	}
public void setNotificationProgress( NotifyProgress progress,boolean fromServer){
	 mNotifyProgress = progress;
	 mRequestToServer = fromServer;
 }
public boolean isMoreDataAvailable(){
	return mMoreDataAvailable;
}
public void setMoreDataAVailable(boolean dataState){
	mMoreDataAvailable = dataState;
}
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
