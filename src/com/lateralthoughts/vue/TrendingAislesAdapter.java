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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;
import android.view.animation.AnimationUtils;
import android.content.Context;
import android.os.ResultReceiver;
import android.view.LayoutInflater;
import android.util.Log;

//java util imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//internal imports
import com.lateralthoughts.vue.VueContentGateway;
import com.lateralthoughts.vue.StaggeredViewAdapter.ViewHolder;
import com.lateralthoughts.vue.ui.ScaleImageView;
import com.lateralthoughts.vue.utils.ContentLoader;

public class TrendingAislesAdapter extends BaseAdapter {
    private Context mContext;
    private ResultReceiver mTrendingAislesContentParser;
    private Handler mResultsHandler;

	//===== The following set of variables are used for state management ==================================
	private int mAdapterState;
	//this variable above is usually set to one of the following values
	private final int AISLE_TRENDING_LIST_DATA = 1;
	private final int AISLE_TRENDING_CONTENT_DATA = 2;
	//====== End of state variables ========================================================================
	
	private int mLimit;
	private int mOffset;
	private VueContentGateway mVueContentGateway;
	private static final String UNKNOWN_CATEGORY = "CategoryUnknown";
	private String mCurrentParsingCategory;	
	
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
	
	private HashMap<String, AisleWindowContent> mAisleContentMap;
	private ContentLoader mLoader;
	private static final int TRENDING_AISLES_SAMPLE_SIZE = 50;
	private static final int TRENDING_AISLES_BATCH_SIZE = 5;
	
	//========================== END OF PARSING TAGS =========================================================
	
    public TrendingAislesAdapter(Context c, ArrayList<AisleWindowContent> content) {
        mContext = c;
        mResultsHandler = new Handler();
        mTrendingAislesContentParser = new TrendingAislesContentParser(mResultsHandler);
        mAdapterState = AISLE_TRENDING_LIST_DATA;
        mVueContentGateway = VueContentGateway.getInstance();
        mLimit = TRENDING_AISLES_BATCH_SIZE;
        mOffset = 0;
        mAisleContentMap = new HashMap<String, AisleWindowContent>();
        mVueContentGateway.getTrendingAisles(mLimit, mOffset, mTrendingAislesContentParser);
        mLoader = new ContentLoader(mContext);
        mCurrentParsingCategory = UNKNOWN_CATEGORY;        
    }

    public int getCount() {
    	if(null != mAisleContentMap){
    		Log.e("Jaws","Adapter size is being asked. Size = " + mAisleContentMap.size());
    		return mAisleContentMap.size();
    	}
    	return 0;
    }

    public AisleWindowContent getItem(int position) {
    	int index = 0;
    	HashMap.Entry<String, AisleWindowContent> entry = null;
    	Iterator<HashMap.Entry<String, AisleWindowContent>> entries = mAisleContentMap.entrySet().iterator();
    	while (entries.hasNext()) {
    		entry = entries.next();
    		if(position == index)
    			break;    	    
    	    index++;
    	}
        return entry.getValue();
    }

    public long getItemId(int position) {
        return 0; //mContent;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		AisleWindowContent content;
		StringBuilder sb = new StringBuilder();

		if (convertView == null) {
			LayoutInflater layoutInflator = LayoutInflater.from(mContext);
			convertView = layoutInflator.inflate(R.layout.staggered_row_item, null);
			holder = new ViewHolder();
			holder.viewFlipper = (ViewFlipper) convertView .findViewById(R.id.aisle_content_flipper);
			//holder.viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(mContext, R.anim.right_out));
			holder.viewFlipper.setInAnimation(AnimationUtils.loadAnimation(mContext, R.anim.left_in));
			convertView.setTag(holder);
			holder.viewFlipper.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					ViewFlipper flipper = (ViewFlipper)v;
		            flipper.showNext();
				}
			});
		}

		holder = (ViewHolder) convertView.getTag();
		TextView ownerNameView = (TextView)convertView.findViewById(R.id.descriptor_aisle_owner_name);
		TextView contextView = (TextView)convertView.findViewById(R.id.descriptor_aisle_context);
		if(holder.viewFlipper.getChildCount() < 3){
			Log.e("Jaws","getView at position = " + position + " for flipper = " + holder.viewFlipper);
			content = getItem(position);
			
			mLoader.DisplayContent(getItem(position), holder.viewFlipper);
			AisleContext context = content.getAisleContext();
			sb.append(context.mFirstName).append(" ").append(context.mLastName);
			ownerNameView.setText(sb.toString());
			StringBuilder contextBuilder = new StringBuilder();
			contextBuilder.append(context.mOccasion).append(" : ").append(context.mLookingForItem);
			contextView.setText(contextBuilder.toString());
		}
		return convertView;
    }

	static class ViewHolder {
		ViewFlipper viewFlipper;
	}
	
    public void loadBitmap(String loc, ImageView imageView) {
        //BitmapWorkerTask task = new BitmapWorkerTask(imageView);
        //task.execute(loc);
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
				mOffset += TRENDING_AISLES_BATCH_SIZE;
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
					aisleItem.addAisleContent(userInfo,  imageItemsArray);
					imageItemsArray.clear();
					notifyDataSetChanged();					
				}
			}catch(JSONException ex1){
				
			}
			
		}
	}
	
	private AisleWindowContent getAisleItem(String aisleId){
		AisleWindowContent aisleItem = null;
		aisleItem = mAisleContentMap.get(aisleId);
		if(null == aisleItem){
			aisleItem = new AisleWindowContent(aisleId);
			mAisleContentMap.put(aisleId, aisleItem);
		}
		return aisleItem;
		
	}
}
