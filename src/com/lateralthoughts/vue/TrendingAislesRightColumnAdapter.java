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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

//java util imports
import java.lang.ref.WeakReference;
import java.util.ArrayList;

//internal imports
import com.lateralthoughts.vue.TrendingAislesLeftColumnAdapter.BitmapWorkerTask;
import com.lateralthoughts.vue.TrendingAislesLeftColumnAdapter.TestViewHolder;
import com.lateralthoughts.vue.ui.AisleContentBrowser;
import com.lateralthoughts.vue.ui.ScaleImageView;
import com.lateralthoughts.vue.ui.AisleContentBrowser.AisleContentClickListener;
import com.lateralthoughts.vue.utils.BitmapLoaderUtils;

public class TrendingAislesRightColumnAdapter extends TrendingAislesGenericAdapter {
    private Context mContext;
    
    private final String TAG = "TrendingAislesRightColumnAdapter";
    
    private AisleLoader mLoader;
    
    private static final boolean DEBUG = true;
    
    public int firstX;
    public int lastX;
    public static boolean mIsRightDataChanged = false;
    AisleContentClickListener listener;
    LinearLayout.LayoutParams mShowpieceParams,mShowpieceParamsDefault;
    BitmapLoaderUtils mBitmapLoaderUtils;
    public TrendingAislesRightColumnAdapter(Context c, ArrayList<AisleWindowContent> content) {
        super(c,content);
        mContext = c;
        if(DEBUG) Log.e(TAG,"About to initiate request for trending aisles");
        mLoader = AisleLoader.getInstance(mContext);        
    }
    
    public TrendingAislesRightColumnAdapter(Context c, AisleContentClickListener listener, ArrayList<AisleWindowContent> content) {
        super(c, listener, content);
        mBitmapLoaderUtils = BitmapLoaderUtils.getInstance();
        mContext = c;
        mLoader = AisleLoader.getInstance(mContext);
        this.listener = listener;
        
        if(DEBUG) Log.e(TAG,"About to initiate request for trending aisles");
        //mVueTrendingAislesDataModel.registerAisleDataObserver(this);       
    }

    @Override
    public int getCount(){
        return mVueTrendingAislesDataModel.getAisleCount()/2;
    }

    @Override
    public AisleWindowContent getItem(int position){
        int positionFactor = 2;
        int actualPosition = 1;
        if(0 != position)
            actualPosition = (positionFactor*position)+actualPosition;
        
        return mVueTrendingAislesDataModel.getAisleAt(actualPosition);
    }
    
    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
    		Log.i("SCROLL_STATE_IDLE", "SCROLL_STATE_IDLE 3 getview");
        ViewHolder holder;
        StringBuilder sb = new StringBuilder();
        Log.i("TrendingDataModel", "DataObserver for List Refresh:  Right getview ");
        if (null == convertView) {
        	Log.i("TrendingDataModel", "DataObserver for List Refresh: Right getview if ");
        	LayoutInflater layoutInflator = LayoutInflater.from(mContext);
            convertView = layoutInflator.inflate(R.layout.staggered_row_item, null);
            holder = new ViewHolder();
            holder.aisleContentBrowser = (AisleContentBrowser) convertView .findViewById(R.id.aisle_content_flipper);
            LinearLayout.LayoutParams showpieceParams = new LinearLayout.LayoutParams(
					VueApplication.getInstance().getScreenWidth()/2,
					 200);
        	//holder.aisleContentBrowser.setLayoutParams(showpieceParams);
            holder.aisleDescriptor = (LinearLayout) convertView .findViewById(R.id.aisle_descriptor);
            holder.profileThumbnail = (ImageView)holder.aisleDescriptor.findViewById(R.id.profile_icon_descriptor);
            holder.aisleOwnersName = (TextView)holder.aisleDescriptor.findViewById(R.id.descriptor_aisle_owner_name);
            holder.aisleContext = (TextView)holder.aisleDescriptor.findViewById(R.id.descriptor_aisle_context);
            holder.uniqueContentId = AisleWindowContent.EMPTY_AISLE_CONTENT_ID;
            convertView.setTag(holder);
            mShowpieceParams = new LinearLayout.LayoutParams(
    				VueApplication.getInstance().getScreenWidth()/2,
    				 300);
        
            //holder.aisleContentBrowser.setLayoutParams(mShowpieceParams);
            
          mShowpieceParamsDefault = new LinearLayout.LayoutParams(
    				 LayoutParams.MATCH_PARENT,
    				 LayoutParams.MATCH_PARENT);
            if(DEBUG) Log.e("Jaws2","getView invoked for a new view at position2 = " + position);
        }
        //AisleWindowContent windowContent = (AisleWindowContent)getItem(position);
        holder = (ViewHolder) convertView.getTag();
        holder.mWindowContent = (AisleWindowContent)getItem(position);
        if(holder.mWindowContent.mIsDataChanged) {
        	holder.mWindowContent.mIsDataChanged = false;
        	 holder.uniqueContentId = AisleWindowContent.EMPTY_AISLE_CONTENT_ID;
        }

        holder.aisleContentBrowser.setAisleContentClickListener(mClickListener);
        int scrollIndex = 0; //getContentBrowserIndexForId(windowContent.getAisleId());
        mLoader.getAisleContentIntoView(holder, scrollIndex, position, false,listener);
        AisleContext context = holder.mWindowContent.getAisleContext();

        sb.append(context.mFirstName).append(" ").append(context.mLastName);
        holder.aisleOwnersName.setText(sb.toString());
        StringBuilder contextBuilder = new StringBuilder();
        contextBuilder.append(context.mOccasion).append(" : ").append(context.mLookingForItem);
        //TODO: this is just temporary: currently the occasion and context info is
        //coming out as occasion_clothing and lookingfor_clothing and stuff like that.
        //just display something a little more realistic so we can see what the app
        //actually look like
        int index = position/mPossibleOccasions.length;
        if(index >= mPossibleOccasions.length)
            index = 0;
        String occasion = mPossibleOccasions[index];
        index = position/mPossibleCategories.length;
        if(index >= mPossibleCategories.length)
            index = 0;
        String lookingFor = mPossibleCategories[index];
    	if(context.mOccasion != null && context.mOccasion.length() >1){
			occasion = context.mOccasion;
		}
		if(context.mLookingForItem != null && context.mLookingForItem.length() > 1){
			lookingFor = context.mLookingForItem;
		}
		holder.aisleContext.setText(occasion + " : " + lookingFor);
     
        //holder.aisleContext.setText(contextBuilder.toString());
        return convertView;
    	}
    

    @Override
    public void onAisleDataUpdated(int newCount){
    	Log.i("TrendingDataModel", "DataObserver for List Refresh: Right List AisleUpdate Called ");
        notifyDataSetChanged();
    }

	private int calculateActualPosition(int viewPosition) {
		int actualPosition = 0;
		if (0 != viewPosition)
			actualPosition = (viewPosition * 2);

		return actualPosition;
	}

 
	  static class TestViewHolder {
	        TextView aisleOwnersName;
	        TextView aisleContext;
	        ImageView profileThumbnail;
	        ImageView image;
	        String uniqueContentId;
	        LinearLayout aisleDescriptor;
	        AisleWindowContent mWindowContent;
	    }
		class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
			private final WeakReference<ImageView> imageViewReference;
			private String url = null;
			private int mBestHeight;

			public BitmapWorkerTask(
					ImageView imageView, int bestHeight) {
				// Use a WeakReference to ensure the ImageView can be garbage
				// collected
				imageViewReference = new WeakReference<ImageView>(imageView);
		 
				mBestHeight = bestHeight;
			}

			// Decode image in background.
			@Override
			protected Bitmap doInBackground(String... params) {
				url = params[0];
				Bitmap bmp = null;
				// we want to get the bitmap and also add it into the memory cache
				Log.e("Profiling", "Profiling New doInBackground()");
				bmp = mBitmapLoaderUtils.getBitmap(url, params[1], true,
						mBestHeight, VueApplication.getInstance().getVueDetailsCardWidth()/2);
				return bmp;
			}

			// Once complete, see if ImageView is still around and set bitmap.
			@Override
			protected void onPostExecute(Bitmap bitmap) {

				if (imageViewReference != null
						&& bitmap != null) {
					final ImageView imageView = imageViewReference.get();
					imageView.setImageBitmap(bitmap);
					// final AisleContentBrowser vFlipper =
					// viewFlipperReference.get();
		 
				}
			}
		}
		// utility functions to keep track of all the async tasks that we
		// instantiate
		private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
			if (imageView != null) {
				Object task = ((ScaleImageView) imageView).getOpaqueWorkerObject();
				if (task instanceof BitmapWorkerTask) {
					BitmapWorkerTask workerTask = (BitmapWorkerTask) task;
					return workerTask;
				}
			}
			return null;
		}

		private static boolean cancelPotentialDownload(String url,
				ImageView imageView) {
			BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

			if (bitmapWorkerTask != null) {
				String bitmapUrl = bitmapWorkerTask.url;
				if ((bitmapUrl == null) || (!bitmapUrl.equals(url))) {
					bitmapWorkerTask.cancel(true);
				} else {
					// The same URL is already being downloaded.
					return false;
				}
			}
			return true;
		}
}