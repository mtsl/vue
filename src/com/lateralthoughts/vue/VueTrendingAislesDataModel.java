  package com.lateralthoughts.vue;

//android imports
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
//java imports
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.lateralthoughts.vue.connectivity.DbHelper;
import com.lateralthoughts.vue.connectivity.VueBatteryManager;

public class VueTrendingAislesDataModel {
	
    private static final boolean DEBUG = false;
	private Context mContext;
	private static VueTrendingAislesDataModel sVueTrendingAislesDataModel;
	private ArrayList<IAisleDataObserver> mAisleDataObserver;
	
	   //========================= START OF PARSING TAGS ========================================================
    //the following strings are pre-defined to help with JSON parsing
    //the tags defined here should be in sync the API documentation for the backend
    private static final String ITEM_CATEGORY_TAG = "category";
    private static final String LOOKING_FOR_TAG = "lookingFor";
    private static final String OCCASION_TAG = "occasion";
    private static final String CONTENT_ID_TAG = "id";
    private static final String USER_OBJECT_TAG = "user";
    private static final String USER_FIRST_NAME_TAG = "firstName";
    private static final String USER_LAST_NAME_TAG ="lastName";
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
    private HashMap<String, AisleWindowContent> mAisleContentListMap = new HashMap<String, AisleWindowContent>();

    //private static final int TRENDING_AISLES_SAMPLE_SIZE = 100;
    private static final int TRENDING_AISLES_BATCH_SIZE = 5;
    private static final int TRENDING_AISLES_BATCH_INITIAL_SIZE = 10;
    private static final int NOTIFICATION_THRESHOLD = 4;
    private boolean mMoreDataAvailable;
    
    //===== The following set of variables are used for state management ==================================
    private int mState;
    //this variable above is usually set to one of the following values
    private final int AISLE_TRENDING_LIST_DATA = 1;
    //private final int AISLE_TRENDING_CONTENT_DATA = 2;
    //====== End of state variables ========================================================================
    
    private int mLimit;
    private int mOffset;
    private AisleWindowContentFactory mAisleWindowContentFactory;
    private boolean mAisleDataRequested;
    private long mRequestStartTime;
    private VueContentGateway mVueContentGateway;
    private TrendingAislesContentParser mTrendingAislesParser;
    
    private final String TAG = "VueTrendingAislesModel";
    public boolean loadOnRequest = false;
    
	private VueTrendingAislesDataModel(Context context){
		mContext = context;
		mVueContentGateway = VueContentGateway.getInstance();
		mAisleWindowContentFactory = AisleWindowContentFactory.getInstance(mContext);
		mTrendingAislesParser = new TrendingAislesContentParser(new Handler());
		mAisleDataObserver = new ArrayList<IAisleDataObserver>();
		
		mLimit = TRENDING_AISLES_BATCH_INITIAL_SIZE;
        mOffset = 0;
        mState = AISLE_TRENDING_LIST_DATA;
        mAisleContentList = new ArrayList<AisleWindowContent>();
        getAislesFromDb();
        // initializeTrendingAisleContent();
        mMoreDataAvailable = true;
        Log.e("Profiling", "Profiling VueTrendingAislesDataModel.VueTrendingAislesDataModel() Before getTrendingAisles() calling");
        mVueContentGateway.getTrendingAisles(mLimit, mOffset, mTrendingAislesParser);
        Log.e("Profiling", "Profiling VueTrendingAislesDataModel.VueTrendingAislesDataModel() after getTrendingAisles() calling");
	}

	public void registerAisleDataObserver(IAisleDataObserver observer){
		if(!mAisleDataObserver.contains(observer))
			mAisleDataObserver.add(observer);

		//but if we already have the data we should notify right away
		observer.onAisleDataUpdated(mAisleContentList.size());
	}
	
	//this class is used to handle the aisle parsing
	private class TrendingAislesContentParser extends ResultReceiver {
	    public TrendingAislesContentParser(Handler handler){
	        super(handler);
	    }

	    @Override
	    protected void onReceiveResult(int resultCode, Bundle resultData){  
	        switch(mState){
	        case AISLE_TRENDING_LIST_DATA:
	          Log.e("VueTrendingAislesDataModel", "JSONArray size(): TEST 4");
	            //we were expecting a list of currently trending aisles list
	            //parse and get this result
	            //If onCreateView was already invoked before we got this call we should
	            //have a valid mTrendingAislesContentView and mContentAdapter.
	            //Add the AisleWindowContent information to the adapter so that we can
	            //start displaying stuff!
	            //mAdapterState = AISLE_TRENDING_CONTENT_DATA; //we are no longer waiting for list!
	            long elapsed = System.currentTimeMillis() - mRequestStartTime;
	            if(mAisleDataRequested){
	                if(DEBUG) Log.e(TAG,"It took " + elapsed + " seconds for first request to return");
	                mAisleDataRequested = false;
	            }
	            if(!mMoreDataAvailable){
	              Log.e("VueTrendingAislesDataModel", "JSONArray size(): TEST 5");
	                if(DEBUG) Log.e(TAG,"No more data is available. mOffset = " + mOffset);
	            }else{
	                
	                parseTrendingAislesResultData(resultData.getString("result"));
	                //if(mOffset > NOTIFICATION_THRESHOLD * TRENDING_AISLES_BATCH_INITIAL_SIZE){
	                //if this is the first set of data we are receiving go ahead
	                //notify the data set changed
	                for(IAisleDataObserver observer : mAisleDataObserver){
	                    observer.onAisleDataUpdated(mAisleContentList.size());
	                }
	                //notifyDataSetChanged();
	                if(mOffset < NOTIFICATION_THRESHOLD*TRENDING_AISLES_BATCH_SIZE)
	                    mOffset += mLimit;
	                else{
	                    mOffset += mLimit;
	                    mLimit = TRENDING_AISLES_BATCH_SIZE;
	                }
	                if(DEBUG) Log.e(TAG,"There is more data to parse. offset = " + mOffset);
                    if (!VueBatteryManager.isConnected(mContext)
                         && VueBatteryManager.batteryLevel(mContext) < VueBatteryManager.MINIMUM_BATTERY_LEVEL) {
	                  mMoreDataAvailable = false;
	                  loadOnRequest = true;
	                }
	            }

	            if(mMoreDataAvailable){
	                mVueContentGateway.getTrendingAisles(mLimit, mOffset, this);
	            }
	            break;

	        default:
	            //we should never have to encounter this!
	            break;
	        }
	    }

	    //TODO: code cleanup. This function here allocated the new AisleImageObjects but re-uses the
	    //imageItemsArray. Instead the called function clones and keeps a copy. This is pretty inconsistent.
	    //Let the allocation happen in one place for both items. Fix this!
	    @SuppressWarnings("unused")
	    private void parseTrendingAislesResultData(String resultString){            
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

	        try{
	            JSONArray contentArray = new JSONArray(resultString);
                Log.e("VueTrendingAislesDataModel", "JSONArray size(): " + contentArray.length());
	            if(0 == contentArray.length()){
	                mMoreDataAvailable = false;
	                addAislesToDb();
	            }
	            DbHelper helper = new DbHelper(mContext);
	            SQLiteDatabase db = helper.getWritableDatabase();
	            
	            
	            for (int i = 0; i < contentArray.length(); i++) {
	                userInfo  = new AisleContext();
	                JSONObject contentItem = contentArray.getJSONObject(i);
	                category = contentItem.getString(ITEM_CATEGORY_TAG);
	                aisleId = contentItem.getString(CONTENT_ID_TAG);
	                Cursor c = db.query(DbHelper.DATABASE_TABLE_AISLES, new String[] {VueConstants.AISLE_ID}, VueConstants.AISLE_ID + "=?",
	                    new String[] {aisleId}, null, null, null);
	                if(c.getCount() != 0) {
	                  db.delete(DbHelper.DATABASE_TABLE_AISLES,  VueConstants.AISLE_ID + "=?", new String[] {aisleId});
	                  db.delete(DbHelper.DATABASE_TABLE_AISLES_IMAGES,  VueConstants.AISLE_ID + "=?", new String[] {aisleId});
	                }
	                userInfo.mAisleId = contentItem.getString(CONTENT_ID_TAG);
	                JSONArray imagesArray = contentItem.getJSONArray(USER_IMAGES_TAG);

	                //within the content item we have a user object
	                JSONObject user = contentItem.getJSONObject(USER_OBJECT_TAG);
	                userInfo.mFirstName = user.getString(USER_FIRST_NAME_TAG);
	                userInfo.mLastName = user.getString(USER_LAST_NAME_TAG);
	                userInfo.mUserId= user.getString(CONTENT_ID_TAG);
                    userInfo.mLookingForItem = contentItem.getString(LOOKING_FOR_TAG);
	                userInfo.mOccasion = contentItem.getString(OCCASION_TAG);
	                userInfo.mJoinTime = Long.parseLong(user.getString(USER_JOIN_TIME_TAG));                    
	                for(int j=0; j<imagesArray.length(); j++){
	                    imageItemDetails = new AisleImageDetails();
	                    imageItem = imagesArray.getJSONObject(j);
	                    imageItemDetails.mDetalsUrl = imageItem.getString(USER_IMAGE_DETALS_TAG);
	                    imageItemDetails.mId = imageItem.getString(USER_IMAGE_ID_TAG);
	                    imageItemDetails.mStore = imageItem.getString(USER_IMAGE_STORE_TAG);
	                    imageItemDetails.mTitle = imageItem.getString(USER_IMAGE_TITLE_TAG);
	                    imageItemDetails.mImageUrl = imageItem.getString(USER_IMAGE_URL_TAG);
	                    imageItemDetails.mAvailableHeight = imageItem.getInt(IMAGE_HEIGHT_TAG);
	                    imageItemDetails.mAvailableWidth = imageItem.getInt(IMAGE_WIDTH_TAG);
	                    imageItemsArray.add(imageItemDetails);                      
	                }
                  aisleItem = getAisleItem(aisleId);
                  aisleItem.addAisleContent(userInfo,  imageItemsArray);
                  // addAislesToDb(userInfo, imageItemsArray);
	              imageItemsArray.clear();
                  c.close();
	            }
	            db.close();
	        }catch(JSONException ex1){
	            if(DEBUG) Log.e(TAG,"Some exception is caught? ex1 = " + ex1.toString());

	        }

	    }
	}
	   
	private AisleWindowContent getAisleItem(String aisleId){
	    AisleWindowContent aisleItem = null;
	    aisleItem = mAisleContentListMap.get(aisleId);
	    if(null == aisleItem){
	        if(null != mAisleContentListMap.get(mAisleWindowContentFactory.EMPTY_AISLE_ID)){
	            aisleItem = mAisleContentListMap.get(mAisleWindowContentFactory.EMPTY_AISLE_ID);
	            if(mAisleContentList.contains(aisleItem)){
	                int index = mAisleContentList.indexOf(aisleItem);
	                mAisleContentList.remove(index);
	                mAisleContentList.add(index,  aisleItem);

	            }
	        }else{
	            aisleItem = mAisleWindowContentFactory.getEmptyAisleWindow(); //new AisleWindowContent(aisleId);
	        }
	        aisleItem.setAisleId(aisleId);
	        mAisleContentListMap.put(aisleId, aisleItem);
	        mAisleContentList.add(aisleItem);
	    }
	    return aisleItem;       
	}

	public int getAisleCount(){
	    if(null != mAisleContentList){
	        return mAisleContentList.size();
	    }
	    return 0;
	}
	
	public AisleWindowContent getAisleAt(int position){
	    return mAisleContentList.get(position);
	}
	
	public static VueTrendingAislesDataModel getInstance(Context context){
		if(null == sVueTrendingAislesDataModel){
		    sVueTrendingAislesDataModel = new VueTrendingAislesDataModel(context);
		}
		return sVueTrendingAislesDataModel;		
	}

	private void addAislesToDb() {
	    Log.e("Profiling", "Profiling VueTrendingAislesDataModel.addAislesToDb() Start");
		DbHelper helper = new DbHelper(mContext);
		SQLiteDatabase db = helper.getWritableDatabase();
		db.beginTransaction();
		for(int i = 0; i < getAisleCount(); i++) {
          AisleWindowContent content = getAisleAt(i);
          AisleContext info = content.getAisleContext();
          ArrayList<AisleImageDetails> imageItemsArray = content.getImageList();
		  ContentValues values = new ContentValues();
		  values.put(VueConstants.FIRST_NAME, info.mFirstName);
		  values.put(VueConstants.LAST_NAME, info.mLastName);
		  values.put(VueConstants.JOIN_TIME, info.mJoinTime);
		  values.put(VueConstants.LOOKING_FOR, info.mLookingForItem);
		  values.put(VueConstants.OCCASION, info.mOccasion);
		  values.put(VueConstants.USER_ID, info.mUserId);
		  values.put(VueConstants.AISLE_ID, info.mAisleId);
		  db.insert(DbHelper.DATABASE_TABLE_AISLES, null, values);
		  for(AisleImageDetails imageDetails : imageItemsArray) {
			  ContentValues imgValues = new ContentValues();
			  imgValues.put(VueConstants.TITLE, imageDetails.mTitle);
			  imgValues.put(VueConstants.IMAGE_URL, imageDetails.mImageUrl);
			  imgValues.put(VueConstants.DETAILS_URL, imageDetails.mDetalsUrl);
			  imgValues.put(VueConstants.HEIGHT, imageDetails.mAvailableHeight);
			  imgValues.put(VueConstants.WIDTH, imageDetails.mAvailableWidth);
			  imgValues.put(VueConstants.STORE, imageDetails.mStore);
			  imgValues.put(VueConstants.IMAGE_ID, imageDetails.mId);
			  imgValues.put(VueConstants.USER_ID, info.mUserId);
			  imgValues.put(VueConstants.AISLE_ID, info.mAisleId);
			  db.insert(DbHelper.DATABASE_TABLE_AISLES_IMAGES, null, imgValues);
		  }
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
		Log.e("Profiling", "Profiling VueTrendingAislesDataModel.addAislesToDb() End");
	}

	private void getAislesFromDb() {
	  Log.e("Profiling", "Profiling VueTrendingAislesDataModel.getAislesFromDb() Start");
	  AisleContext userInfo;
	  AisleImageDetails imageItemDetails;
	  AisleWindowContent aisleItem = null;
      ArrayList<AisleImageDetails> imageItemsArray = new ArrayList<AisleImageDetails>();
	  DbHelper helper = new DbHelper(mContext);
      SQLiteDatabase db = helper.getWritableDatabase();
      Cursor aislesCursor = db.query(DbHelper.DATABASE_TABLE_AISLES, null, null, null, null, null, VueConstants.ID + " ASC");
      if(aislesCursor.moveToFirst()) {
       do {
        userInfo  = new AisleContext();
        userInfo.mAisleId = aislesCursor.getString(aislesCursor.getColumnIndex(VueConstants.AISLE_ID));
        userInfo.mUserId = aislesCursor.getString(aislesCursor.getColumnIndex(VueConstants.USER_ID));
        userInfo.mFirstName = aislesCursor.getString(aislesCursor.getColumnIndex(VueConstants.FIRST_NAME));
        userInfo.mLastName = aislesCursor.getString(aislesCursor.getColumnIndex(VueConstants.LAST_NAME));
        userInfo.mJoinTime = Long.parseLong(aislesCursor.getString(aislesCursor.getColumnIndex(VueConstants.JOIN_TIME)));
        userInfo.mLookingForItem = aislesCursor.getString(aislesCursor.getColumnIndex(VueConstants.LOOKING_FOR));
        userInfo.mOccasion = aislesCursor.getString(aislesCursor.getColumnIndex(VueConstants.OCCASION));
        Cursor aisleImagesCursor = db.query(DbHelper.DATABASE_TABLE_AISLES_IMAGES, null, VueConstants.AISLE_ID + "=?",
            new String[] {userInfo.mAisleId}, null, null, VueConstants.ID + " ASC");
        if(aisleImagesCursor.moveToFirst()) {
          do {
            imageItemDetails = new AisleImageDetails();
            imageItemDetails.mTitle = aisleImagesCursor.getString(aisleImagesCursor.getColumnIndex(VueConstants.TITLE));
            imageItemDetails.mImageUrl = aisleImagesCursor.getString(aisleImagesCursor.getColumnIndex(VueConstants.IMAGE_URL));
            imageItemDetails.mDetalsUrl = aisleImagesCursor.getString(aisleImagesCursor.getColumnIndex(VueConstants.DETAILS_URL));
            imageItemDetails.mStore = aisleImagesCursor.getString(aisleImagesCursor.getColumnIndex(VueConstants.STORE));
            imageItemDetails.mId = aisleImagesCursor.getString(aisleImagesCursor.getColumnIndex(VueConstants.IMAGE_ID));
            imageItemDetails.mAvailableHeight = Integer.parseInt(aisleImagesCursor.getString(aisleImagesCursor
                .getColumnIndex(VueConstants.HEIGHT)));
            imageItemDetails.mAvailableWidth = Integer.parseInt(aisleImagesCursor.getString(aisleImagesCursor
                .getColumnIndex(VueConstants.WIDTH)));
            imageItemsArray.add(imageItemDetails);
          } while(aisleImagesCursor.moveToNext());
        }
        aisleItem = getAisleItem(userInfo.mAisleId);
        aisleItem.addAisleContent(userInfo,  imageItemsArray);
        imageItemsArray.clear();
        aisleImagesCursor.close();
       } while(aislesCursor.moveToNext());
      }
      aislesCursor.close();
      db.close();
      Log.e("Profiling", "Profiling VueTrendingAislesDataModel.getAislesFromDb() End");
	}

  public void loadMoreAisles() {
    mMoreDataAvailable = true;
    loadOnRequest = false;
    if(mOffset < NOTIFICATION_THRESHOLD * TRENDING_AISLES_BATCH_SIZE)
      mOffset += mLimit;
    else {
      mOffset += mLimit;
      mLimit = TRENDING_AISLES_BATCH_SIZE;
    }
    Log.e("VueTrendingAislesDataModel", "JSONArray size(): TEST 6 ++ mOffset: " + mOffset + ", mLimit" + mLimit);
    mVueContentGateway.getTrendingAisles(mLimit, mOffset, mTrendingAislesParser);
  }

	private void copyDB() {
		try {
	        File sd = Environment.getExternalStorageDirectory();
	        File data = Environment.getDataDirectory();

	        if (sd.canWrite()) {
	            String currentDBPath = "//data//{package name}//databases//{database name}";
	            String backupDBPath = "{database name}";
	            File currentDB = new File(data, currentDBPath);
	            File backupDB = new File(sd, backupDBPath);

	            if (currentDB.exists()) {
	                FileChannel src = new FileInputStream(currentDB).getChannel();
	                FileChannel dst = new FileOutputStream(backupDB).getChannel();
	                dst.transferFrom(src, 0, src.size());
	                src.close();
	                dst.close();
	            }
	        }
	    } catch (Exception e) {
	    }
	}
}
