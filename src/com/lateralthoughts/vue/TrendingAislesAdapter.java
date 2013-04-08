/**
 * 
 * @author Vinodh Sundararajan
 * One of the more complex parts of the adapter is the mechanism for interacting
 * with content gateway and keeping track of aisle window content.
 * An AisleWindowContent is made up of a bunch of images all belonging to one
 * category but contributed by several different users.
 * We are going to be dealing with humongous amounts of data so need to careful
 * about this.
 * As soon as the adapter goes live we will initiate a request to get the top trending
 * aisles. But we have no idea how many are top trending; meaning, if there are hundreds of
 * them it will take forever for the data to come back We will therefore use the limit
 * and offset parameters to get data in smaller chunks.
 * The adapter keep an array of AisleWindowContent each of which contains array of content
 * When a new item comes in, it the category has already been created we add its content
 * to an existing AisleWindowContent. Otherwise, create a new one.
 * 
 * Relationship between AisleWindowContent and grid item: Each AisleWindowContent object will
 * take up one spot in the StaggeredGridView. This spot consists of a ViewFlipper so there will
 * many many images. This spot also contains a "meta" field with information relating to the person
 * who added the item, thumbnail image of the person, context and occasion for the category.
 *
 */

package com.lateralthoughts.vue;

import android.widget.BaseAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.content.Context;
import android.view.View.OnTouchListener;
import android.util.Log;

//java util imports
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//internal imports
import com.lateralthoughts.vue.VueContentGateway;
import com.lateralthoughts.vue.ui.AisleContentBrowser;

public class TrendingAislesAdapter extends BaseAdapter {
    private Context mContext;
    private ResultReceiver mTrendingAislesContentParser;
    private Handler mResultsHandler;

	//===== The following set of variables are used for state management ==================================
	private int mAdapterState;
	//this variable above is usually set to one of the following values
	private final int AISLE_TRENDING_LIST_DATA = 1;
	//private final int AISLE_TRENDING_CONTENT_DATA = 2;
	//====== End of state variables ========================================================================
	
	private int mLimit;
	private int mOffset;
	private VueContentGateway mVueContentGateway;
	
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
	private static final String USER_IMAGE_DETALS_TAG = "detalsUrl";
	private static final String USER_IMAGE_URL_TAG = "imageUrl";
	private static final String USER_IMAGE_STORE_TAG = "store";
	private static final String USER_IMAGE_TITLE_TAG = "title";
	
	private ArrayList<AisleWindowContent> mAisleContentList;
	private AisleLoader mLoader;
	private static final int TRENDING_AISLES_SAMPLE_SIZE = 100;
	private static final int TRENDING_AISLES_BATCH_SIZE = 5;
	private static final int TRENDING_AISLES_BATCH_INITIAL_SIZE = 10;
	private static final int NOTIFICATION_THRESHOLD = 1;
	
	private static final boolean DEBUG = false;
	
	public int firstX;
	public int lastX;
	public static final int SWIPE_MIN_DISTANCE = 30;
	public boolean mAnimationInProgress;
	
	private HashMap<String, Integer> mAisleWindowIndex = new HashMap<String,Integer>();
 	//========================== END OF PARSING TAGS =========================================================
	
    public TrendingAislesAdapter(Context c, ArrayList<AisleWindowContent> content) {
        mContext = c;
        mResultsHandler = new Handler();
        mTrendingAislesContentParser = new TrendingAislesContentParser(mResultsHandler);
        mAdapterState = AISLE_TRENDING_LIST_DATA;
        mVueContentGateway = VueContentGateway.getInstance();
        mLimit = TRENDING_AISLES_BATCH_INITIAL_SIZE; //TRENDING_AISLES_BATCH_SIZE;
        mOffset = 0;
        mAisleContentList = new ArrayList<AisleWindowContent>();
        mVueContentGateway.getTrendingAisles(mLimit, mOffset, mTrendingAislesContentParser);
        mLoader = AisleLoader.getInstance(mContext);        
    }

    public int getCount() {
    	if(null != mAisleContentList){
    		return mAisleContentList.size();
    	}
    	return 0;
    }

    public AisleWindowContent getItem(int position) {
    	return mAisleContentList.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    public boolean hasStableIds(){
    	return true;
    }
    
    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {    	
		ViewHolder holder;
		StringBuilder sb = new StringBuilder();
		final int position2 = position;

		if (null == convertView) {
			LayoutInflater layoutInflator = LayoutInflater.from(mContext);
			convertView = layoutInflator.inflate(R.layout.staggered_row_item, null);
			holder = new ViewHolder();
			holder.aisleContentBrowser = (AisleContentBrowser) convertView .findViewById(R.id.aisle_content_flipper);
			holder.aisleDescriptor = (LinearLayout) convertView .findViewById(R.id.aisle_descriptor);
			holder.profileThumbnail = (ImageView)holder.aisleDescriptor.findViewById(R.id.profile_icon_descriptor);
			holder.aisleOwnersName = (TextView)holder.aisleDescriptor.findViewById(R.id.descriptor_aisle_owner_name);
			holder.aisleContext = (TextView)holder.aisleDescriptor.findViewById(R.id.descriptor_aisle_context);
			holder.uniqueContentId = AisleWindowContent.EMPTY_AISLE_CONTENT_ID;
			convertView.setTag(holder);
			if(DEBUG) Log.e("Jaws2","getView invoked for a new view at position = " + position);
			
			holder.aisleContentBrowser.setOnTouchListener(new OnTouchListener() {
		        public boolean onTouch(View v, MotionEvent event) {
		        	final AisleContentBrowser aisleContentBrowser = (AisleContentBrowser)v;

		            if (event.getAction() == MotionEvent.ACTION_DOWN) {
		            	mAnimationInProgress= false;
		                firstX = (int) event.getX();
		            }

		            if(event.getAction() == MotionEvent.ACTION_MOVE){
		            	lastX = (int)event.getX();
		                lastX = (int) event.getX();

		                //Log.e("Jaws","firstX = " + firstX + " lastX = " + lastX);
		                if (firstX - lastX > SWIPE_MIN_DISTANCE) {
		                	if(false == mAnimationInProgress){
		                		Log.e("Jaws","mAnimationInProgress is not true...setting one now!");
		                		Animation currentGoLeft = AnimationUtils.loadAnimation(mContext, R.anim.right_out);
		                		final Animation nextFadeIn = AnimationUtils.loadAnimation(mContext, R.anim.fade_in);
		                		mAnimationInProgress = true;
		                		currentGoLeft.setAnimationListener(new Animation.AnimationListener(){
			                		public void onAnimationEnd(Animation animation) {
			                			//mAnimationInProgress = false;
			                			Log.e("Jaws","onAnimationEnd is invoked and we are calling showNext! position = " + position2);
			                			aisleContentBrowser.showNext();
			                			setContentBrowserIndexForId(aisleContentBrowser.getUniqueId(),
			                					aisleContentBrowser.indexOfChild(aisleContentBrowser.getCurrentView()));
			                			
			                			aisleContentBrowser.getCurrentView().startAnimation(nextFadeIn);
			                		}
			                		public void onAnimationStart(Animation animation) {
			                			
			                		}
			                		public void onAnimationRepeat(Animation animation) {
			                			
			                		}
			                	});
		                		nextFadeIn.setAnimationListener(new Animation.AnimationListener(){
			                		public void onAnimationEnd(Animation animation) {		                			
			                		}
			                		public void onAnimationStart(Animation animation) {
			                			
			                		}
			                		public void onAnimationRepeat(Animation animation) {
			                			
			                		}
			                	});
		                		
		                		aisleContentBrowser.getCurrentView().startAnimation(currentGoLeft);
		                		return true;
		                	}		                	
		                } else if (lastX - firstX > SWIPE_MIN_DISTANCE) {
		                	if(false == mAnimationInProgress){
		                		Log.e("Jaws","mAnimationInProgress is not true...setting one now!");
		                		Animation currentGoRight = AnimationUtils.loadAnimation(mContext, R.anim.left_in);
		                		final Animation nextFadeIn = AnimationUtils.loadAnimation(mContext, R.anim.fade_in);
		                		mAnimationInProgress = true;
		                		currentGoRight.setAnimationListener(new Animation.AnimationListener(){
			                		public void onAnimationEnd(Animation animation) {
			                			mAnimationInProgress = false;
			                			Log.e("Jaws","onAnimationEnd is invoked and we are calling showPrevious!");
			                			aisleContentBrowser.showPrevious();
			                			setContentBrowserIndexForId(aisleContentBrowser.getUniqueId(),
			                					aisleContentBrowser.indexOfChild(aisleContentBrowser.getCurrentView()));
			                			aisleContentBrowser.getCurrentView().startAnimation(nextFadeIn);
			                		}
			                		public void onAnimationStart(Animation animation) {
			                			
			                		}
			                		public void onAnimationRepeat(Animation animation) {
			                			
			                		}
			                	});
		                		nextFadeIn.setAnimationListener(new Animation.AnimationListener(){
			                		public void onAnimationEnd(Animation animation) {		                			
			                		}
			                		public void onAnimationStart(Animation animation) {
			                			
			                		}
			                		public void onAnimationRepeat(Animation animation) {
			                			
			                		}
			                	});
		                		
		                		aisleContentBrowser.getCurrentView().startAnimation(currentGoRight);
		                		return true;
		                	}
		                }
		            }
		            if (event.getAction() == MotionEvent.ACTION_UP) {
		            	mAnimationInProgress = false;		            	
		            }
		            return true;
		        }
		    });
		}
		AisleWindowContent windowContent = (AisleWindowContent)getItem(position);

		holder = (ViewHolder) convertView.getTag();
		int scrollIndex = 0; //getContentBrowserIndexForId(windowContent.getAisleId());
		mLoader.getAisleContentIntoView(windowContent, holder, scrollIndex, position);
		AisleContext context = windowContent.getAisleContext();
		sb.append(context.mFirstName).append(" ").append(context.mLastName);
		holder.aisleOwnersName.setText(sb.toString());
		StringBuilder contextBuilder = new StringBuilder();
		contextBuilder.append(context.mOccasion).append(" : ").append(context.mLookingForItem);
		holder.aisleContext.setText(contextBuilder.toString());
		return convertView;
    }

	static class ViewHolder {
		AisleContentBrowser aisleContentBrowser;
		TextView aisleOwnersName;
		TextView aisleContext;
		ImageView profileThumbnail;
		String uniqueContentId;
		LinearLayout aisleDescriptor;
	}
    
	private class TrendingAislesContentParser extends ResultReceiver {
		public TrendingAislesContentParser(Handler handler){
			super(handler);
		}
		
		@Override
		protected void onReceiveResult(int resultCode, Bundle resultData){	
			switch(mAdapterState){
			case AISLE_TRENDING_LIST_DATA:
				//we were expecting a list of currently trending aisles list
				//parse and get this result
				//If onCreateView was already invoked before we got this call we should
				//have a valid mTrendingAislesContentView and mContentAdapter.
				//Add the AisleWindowContent information to the adapter so that we can
				//start displaying stuff!
				//mAdapterState = AISLE_TRENDING_CONTENT_DATA; //we are no longer waiting for list!
				parseTrendingAislesResultData(resultData.getString("result"));
				if(mOffset > NOTIFICATION_THRESHOLD * TRENDING_AISLES_BATCH_INITIAL_SIZE){
					//if this is the first set of data we are receiving go ahead
					//notify the data set changed
					Log.e("Smartie Pants","Notifying that the data set has changed...");
					notifyDataSetChanged();
					mOffset += TRENDING_AISLES_BATCH_SIZE;
				}else{
					//trying to be a bit clever here about the experience. When the app
					//loads up we inevitably need a little time to set everything up
					//properly. We need to download all these aisle content and the moment
					//we indicate content is available the view system is going to start
					//pounding us with request for view data.
					//We have part of the view data but the other half (image content, bitmaps)
					//we will still get them on the fly. Let's start the background download
					//of images here. The idea is to do a file cache of the images so that
					//when the GridView starts asking for view at position <n> we don't have to
					//make a round trip network request - we can just get it from file!
					/*AisleWindowContent windowContent = (AisleWindowContent)getItem(position);

					holder = (ViewHolder) convertView.getTag();
					int scrollIndex = 0; //getContentBrowserIndexForId(windowContent.getAisleId());
					mLoader.getAisleContentIntoView(windowContent, holder, scrollIndex, position);*/
					int startIndex = mOffset;
					int length = TRENDING_AISLES_BATCH_INITIAL_SIZE;
					mLoader.prefetchImages(mAisleContentList, startIndex, length);
					
					mOffset += TRENDING_AISLES_BATCH_INITIAL_SIZE;
					
				}
				
				if(mOffset < TRENDING_AISLES_SAMPLE_SIZE){
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
				
				if(0 == contentArray.length()){
					//oops!?
				}
				
				for (int i = 0; i < contentArray.length(); i++) {
					userInfo  = new AisleContext();
					JSONObject contentItem = contentArray.getJSONObject(i);
					category = contentItem.getString(ITEM_CATEGORY_TAG);
					aisleId = contentItem.getString(CONTENT_ID_TAG);
					//Log.e("Jaws","aisleId = " + aisleId);
					JSONArray imagesArray = contentItem.getJSONArray(USER_IMAGES_TAG);
					
					//within the content item we have a user object
					JSONObject user = contentItem.getJSONObject(USER_OBJECT_TAG);
					userInfo.mFirstName = user.getString(USER_FIRST_NAME_TAG);
					userInfo.mLastName = user.getString(USER_LAST_NAME_TAG);

					
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
						imageItemsArray.add(imageItemDetails);						
					}
					
					aisleItem = getAisleItem(aisleId);
					//Log.e("Jaws","urls for user: " + userInfo.mFirstName + 
					//		" " + userInfo.mLastName + " will be added at position " + mAisleContentList.size());
					aisleItem.addAisleContent(userInfo,  imageItemsArray);
					imageItemsArray.clear();
										
				}
			}catch(JSONException ex1){
				
			}
			
		}
	}
	
	private AisleWindowContent getAisleItem(String aisleId){
		AisleWindowContent aisleItem = null;
		aisleItem = new AisleWindowContent(aisleId);
		mAisleContentList.add(aisleItem);
		return aisleItem;
		
	}
	
	//inside the trending aisles grid view, each grid item is a scrollable list
	//of images. When a user scrolls through content we want to remember the last 
	//position they were looking at. We will store it in this using a map
	/*private int getContentBrowserIndexForId(String aisleId){
		if(null == aisleId || null == mAisleWindowIndex)
			return 0;
		return mAisleWindowIndex.get(aisleId);		
	}*/
	
	private void setContentBrowserIndexForId(String aisleId, int index){
		mAisleWindowIndex.put(aisleId, Integer.valueOf(index));
	}
}
