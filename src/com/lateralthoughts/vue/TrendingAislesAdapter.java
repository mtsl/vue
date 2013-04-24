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
import android.os.Handler;
import android.os.ResultReceiver;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.content.Context;
import android.view.View.OnTouchListener;
import android.util.Log;

//java util imports
import java.util.ArrayList;
import java.util.HashMap;

//internal imports
import com.lateralthoughts.vue.VueContentGateway;
import com.lateralthoughts.vue.ui.AisleContentBrowser;

public class TrendingAislesAdapter extends BaseAdapter implements IAisleDataObserver {
    private Context mContext;
    private ResultReceiver mTrendingAislesContentParser;
    private Handler mResultsHandler;
    
	private final String TAG = "TrendingAislesAdapter";
	
	//DEBUG variables
	private int mDebugTapCount = 0;
	private long mDownPressStartTime = 0;
	private final int MAX_ELAPSED_DURATION_FOR_TAP = 200;
	private AisleLoader mLoader;
	
	private static final boolean DEBUG = false;
	
	public int firstX;
	public int lastX;
	public static final int SWIPE_MIN_DISTANCE = 30;
	public boolean mAnimationInProgress;
	private AisleWindowContentFactory mAisleWindowContentFactory;
	
	private VueTrendingAislesDataModel mVueTrendingAislesDataModel;
	private HashMap<String, Integer> mAisleWindowIndex = new HashMap<String,Integer>();
	
    public TrendingAislesAdapter(Context c, ArrayList<AisleWindowContent> content) {
        mContext = c;
        if(DEBUG) Log.e(TAG,"About to initiate request for trending aisles");
        mAisleWindowContentFactory = AisleWindowContentFactory.getInstance(mContext);
        //initializeTrendingAisleContent();
        //mVueContentGateway.getTrendingAisles(mLimit, mOffset, mTrendingAislesContentParser);
        mVueTrendingAislesDataModel = VueTrendingAislesDataModel.getInstance(mContext);
        mVueTrendingAislesDataModel.registerAisleDataObserver(this);
        mLoader = AisleLoader.getInstance(mContext);        
    }

    public int getCount(){
        return mVueTrendingAislesDataModel.getAisleCount();
    }

    public AisleWindowContent getItem(int position){
        return mVueTrendingAislesDataModel.getAisleAt(position);
    	//return mAisleContentList.get(position);
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
		            	mDownPressStartTime = System.currentTimeMillis();
		            	mAnimationInProgress= false;
		                firstX = (int) event.getX();
		            }else if(event.getAction() == MotionEvent.ACTION_UP){
		            	long tapElapsed = System.currentTimeMillis() - mDownPressStartTime;
		            	if(tapElapsed < MAX_ELAPSED_DURATION_FOR_TAP){
		            		mDebugTapCount++;
		            		if(mDebugTapCount == 9){
		            			if(DEBUG) Log.e(TAG,"Show the toast and reset the tap counter");
		            			CharSequence text = "Version = Apr 13";
		            			int duration = Toast.LENGTH_SHORT;

		            			Toast toast = Toast.makeText(mContext, text, duration);
		            			toast.show();
		            			mDebugTapCount = 0;
		            		}
		            	}else{
		            		if(DEBUG) Log.e(TAG,"tapElapsed = " + tapElapsed);
		            	}
		            }

		            if(event.getAction() == MotionEvent.ACTION_MOVE){
		            	lastX = (int)event.getX();
		                lastX = (int) event.getX();
		                
		                if(1 == aisleContentBrowser.getChildCount()){
		                	mLoader.fillAisleContentBrowser(aisleContentBrowser.getUniqueId());
		                }
		                		
		                if (firstX - lastX > SWIPE_MIN_DISTANCE) {
		                	if(false == mAnimationInProgress){
		                		Animation currentGoLeft = AnimationUtils.loadAnimation(mContext, R.anim.right_out);
		                		final Animation nextFadeIn = AnimationUtils.loadAnimation(mContext, R.anim.fade_in);
		                		mAnimationInProgress = true;
		                		currentGoLeft.setAnimationListener(new Animation.AnimationListener(){
			                		public void onAnimationEnd(Animation animation) {
			                			//mAnimationInProgress = false;
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
		                		Animation currentGoRight = AnimationUtils.loadAnimation(mContext, R.anim.left_in);
		                		final Animation nextFadeIn = AnimationUtils.loadAnimation(mContext, R.anim.fade_in);
		                		mAnimationInProgress = true;
		                		currentGoRight.setAnimationListener(new Animation.AnimationListener(){
			                		public void onAnimationEnd(Animation animation) {
			                			mAnimationInProgress = false;
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
		//AisleWindowContent windowContent = (AisleWindowContent)getItem(position);

		holder = (ViewHolder) convertView.getTag();
		holder.mWindowContent = (AisleWindowContent)getItem(position);
		int scrollIndex = 0; //getContentBrowserIndexForId(windowContent.getAisleId());
		mLoader.getAisleContentIntoView(holder, scrollIndex, position);
		AisleContext context = holder.mWindowContent.getAisleContext();

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
		AisleWindowContent mWindowContent;
	}
	
	@Override
	public void onAisleDataUpdated(int newCount){
	    notifyDataSetChanged();
	}
	private void setContentBrowserIndexForId(String aisleId, int index){
		mAisleWindowIndex.put(aisleId, Integer.valueOf(index));
	}
}
