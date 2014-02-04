package com.lateralthoughts.vue;

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

import com.lateralthoughts.vue.connectivity.DataBaseManager;
import com.lateralthoughts.vue.connectivity.DbHelper;
import com.lateralthoughts.vue.connectivity.NetworkHandler;
import com.lateralthoughts.vue.ui.NotifyProgress;

public class VueTrendingAislesDataModel {
    
    private Context mContext;
    private static VueTrendingAislesDataModel sVueTrendingAislesDataModel;
    private ArrayList<IAisleDataObserver> mAisleDataObserver;
    
    // ========================= START OF PARSING TAGS
    // ========================================================
    // the following strings are pre-defined to help with JSON parsing
    // the tags defined here should be in sync the API documentation for the
    // backend
    public ArrayList<AisleWindowContent> mAisleContentList;
    NotifyProgress mNotifyProgress;
    boolean mRequestToServer;
    public HashMap<String, AisleWindowContent> mAisleContentListMap = new HashMap<String, AisleWindowContent>();
    private int mPoolSize = 1;
    private int mMaxPoolSize = 1;
    private long mKeepAliveTime = 10;
    public boolean mMoreDataAvailable = true;
    private boolean mMarkAislesToDelete = false;
    public boolean mIsFromDb = false;
    
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
        VueApplication.getInstance().mLastRecordedTime = System
                .currentTimeMillis();
        mNetworkHandler.loadInitialData(loadMore, mHandler, mContext
                .getResources().getString(R.string.trending));
    }
    
    public NetworkHandler getNetworkHandler() {
        return mNetworkHandler;
    }
    
    public void registerAisleDataObserver(IAisleDataObserver observer) {
        if (!mAisleDataObserver.contains(observer))
            mAisleDataObserver.add(observer);
        
        // but if we already have the data we should notify right away
        observer.onAisleDataUpdated(mAisleContentList.size());
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
    
    public AisleWindowContent getAisle(String aisleId) {
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
        return aisleItem;
    }
    
    public void addItemToListAt(String aisleId, AisleWindowContent aisleItem,
            int position) {
        mAisleContentListMap.put(aisleId, aisleItem);
        mAisleContentList.add(position, aisleItem);
    }
    
    public void addItemToList(String aisleId, AisleWindowContent aisleItem) {
        if (mAisleContentListMap.get(aisleId) == null) {
            mAisleContentListMap.put(aisleId, aisleItem);
            mAisleContentList.add(aisleItem);
        }
    }
    
    public AisleWindowContent removeAisleFromList(int position) {
        mAisleContentListMap.remove(mAisleContentList.get(position)
                .getAisleId());
        return mAisleContentList.remove(position);
    }
    
    public int getAisleCount() {
        if (null != mAisleContentList) {
            return mAisleContentList.size();
        }
        return 0;
    }
    
    public AisleWindowContent getAisleFromList(AisleWindowContent ailseItem) {
        int index = mAisleContentList.indexOf(ailseItem);
        if (index != -1 && index < mAisleContentList.size()) {
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
    
    public void updateProfileImatgeInAisles(String userId,
            String profileImageUrl) {
        for (int i = 0; i < mAisleContentList.size(); i++) {
            AisleWindowContent aisleItem = mAisleContentList.get(i);
            if (aisleItem.getAisleContext().mUserId.equals(userId)) {
                aisleItem.getAisleContext().mAisleOwnerImageURL = profileImageUrl;
            }
        }
        dataObserver();
    }
    
    public AisleWindowContent getAisleAt(String aisleId) {
        return mAisleContentListMap.get(aisleId);
    }
    
    public AisleImageDetails getAisleImageForImageId(String imageId,
            String aisleId, boolean fromLandingScreenOnResumeForShareViaVue) {
        if (imageId != null && aisleId != null) {
            AisleWindowContent aisleWindowContent = getAisleAt(aisleId);
            if (aisleWindowContent != null
                    && aisleWindowContent.getImageList() != null) {
                for (int i = 0; i < aisleWindowContent.getImageList().size(); i++) {
                    if (aisleWindowContent.getImageList().get(i).mId
                            .equals(imageId)) {
                        if (fromLandingScreenOnResumeForShareViaVue) {
                            VueLandingPageActivity.mOtherSourceImageLookingFor = aisleWindowContent
                                    .getAisleContext().mLookingForItem;
                            VueLandingPageActivity.mOtherSourceImageOccasion = aisleWindowContent
                                    .getAisleContext().mOccasion;
                            VueLandingPageActivity.mOtherSourceImageCategory = aisleWindowContent
                                    .getAisleContext().mCategory;
                        }
                        return aisleWindowContent.getImageList().get(i);
                    }
                }
            }
        }
        return null;
    }
    
    public int getAilsePosition(AisleWindowContent aisleItem) {
        return mAisleContentList.indexOf(aisleItem);
    }
    
    public static VueTrendingAislesDataModel getInstance(Context context) {
        if (null == sVueTrendingAislesDataModel) {
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
        dataObserver();
    }
    
    public void dataObserver() {
        for (IAisleDataObserver observer : mAisleDataObserver) {
            observer.onAisleDataUpdated(mAisleContentList.size());
        }
        loadOnRequest = true;
    }
    
    public int listSize() {
        return mAisleContentList.size();
        
    }
    
    /**
     * 
     * @param ratedList
     *            update the user image rating status in aisle window list.
     */
    public void updateImageRatingStatus(ArrayList<String> ratedList) {
        for (int aisleIndex = 0; aisleIndex < mAisleContentList.size(); aisleIndex++) {
            if (mAisleContentList.get(aisleIndex).getImageList() != null) {
                for (int imageIndex = 0; imageIndex < mAisleContentList
                        .get(aisleIndex).getImageList().size(); imageIndex++) {
                    if (ratedList.contains(mAisleContentList.get(aisleIndex)
                            .getImageList().get(imageIndex).mId)) {
                        mAisleContentList.get(aisleIndex).getImageList()
                                .get(imageIndex).mLikeDislikeStatus = VueConstants.IMG_LIKE_STATUS;
                        break;
                    }
                }
            }
        }
        dataObserver();
    }
    
    /**
     * 
     * @param bookmarkList
     *            update the user bookmark status in aisle window list.
     */
    public void updateBookmarkAisleStatus(ArrayList<String> bookmarkList) {
        for (int i = 0; i < bookmarkList.size(); i++) {
            String bookmarkAisleId = bookmarkList.get(i);
            for (int j = 0; j < mAisleContentList.size(); j++) {
                if (mAisleContentList.get(j).getAisleContext().mAisleId
                        .equals(bookmarkAisleId)) {
                    mAisleContentList.get(j).mAisleBookmarkIndicator = true;
                    break;
                }
            }
        }
    }
    
    public Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            @SuppressWarnings("unchecked")
            ArrayList<AisleWindowContent> aisleContentArray = (ArrayList<AisleWindowContent>) msg.obj;
            mIsFromDb = true;
            
            for (AisleWindowContent content : aisleContentArray) {
                
                if (content.getImageList() != null
                        && content.getImageList().size() > 0) {
                    
                    content.addAisleContent(content.getAisleContext(),
                            content.getImageList());
                    addItemToList(content.getAisleId(), content);
                }
            }
            for (IAisleDataObserver observer : mAisleDataObserver) {
                observer.onAisleDataUpdated(mAisleContentList.size());
            }
            runTask(new Runnable() {
                public void run() {
                    
                    loadOnRequest = false;
                    ArrayList<AisleWindowContent> aislesList = mDbManager
                            .getAislesFromDB(null, false);
                    if (aislesList.size() == 0) {
                        loadOnRequest = true;
                        mIsFromDb = false;
                        VueTrendingAislesDataModel.getInstance(
                                VueApplication.getInstance()).setmOffset(
                                listSize());
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
    
    public void setNotificationProgress(NotifyProgress progress,
            boolean fromServer) {
        mNotifyProgress = progress;
        mRequestToServer = fromServer;
    }
    
    public boolean isMoreDataAvailable() {
        return mMoreDataAvailable;
    }
    
    public void setMoreDataAVailable(boolean dataState) {
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
        if (mNotifyProgress != null)
            mNotifyProgress.showProgress();
    }
    
    public ArrayList<AisleImageDetails> getOwnerImages(String aisleId,
            String userId) {
        AisleWindowContent aisle = getAisleAt(aisleId);
        ArrayList<AisleImageDetails> userImageList = new ArrayList<AisleImageDetails>();
        if (aisle != null) {
            for (int i = 0; i < aisle.getImageList().size(); i++) {
                if (userId.equals(aisle.getImageList().get(i).mOwnerUserId)) {
                    userImageList.add(aisle.getImageList().get(i));
                }
            }
        }
        return userImageList;
    }
    
    public void dismissProgress() {
        if (mNotifyProgress != null) {
            mNotifyProgress.dismissProgress(mRequestToServer);
        }
    }
    
    public int getImagePositionInAisle(AisleWindowContent aisleWindowContent,
            String imageId) {
        int position = 0;
        if (aisleWindowContent != null
                && aisleWindowContent.getImageList() != null) {
            ArrayList<AisleImageDetails> imageList = aisleWindowContent
                    .getImageList();
            for (int i = 0; i < imageList.size(); i++) {
                if (imageList.get(i).mId.equals(imageId)) {
                    position = i;
                    break;
                }
            }
        }
        return position;
    }
    
    public void getFreshDataFromServer() {
        boolean loadMore = true;
        mNetworkHandler.loadInitialData(loadMore, mHandler, mContext
                .getResources().getString(R.string.trending));
    }
    
    public void setImageLikeOrDisLikeForImage(
            AisleImageDetails aisleImageDetails, Long ratingId,
            boolean likeOrDislike) {
        if (aisleImageDetails != null && aisleImageDetails.mRatingsList != null
                && aisleImageDetails.mRatingsList.size() > 0) {
            for (int i = 0; i < aisleImageDetails.mRatingsList.size(); i++) {
                if (aisleImageDetails.mRatingsList.get(i).mId.equals(ratingId)) {
                    aisleImageDetails.mRatingsList.get(i).mLiked = likeOrDislike;
                    break;
                }
            }
        }
    }
}
