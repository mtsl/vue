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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.Context;
import android.util.Log;

//java util imports
import java.util.ArrayList;

//internal imports
import com.lateralthoughts.vue.ui.AisleContentBrowser;
import com.lateralthoughts.vue.ui.AisleContentBrowser.AisleContentClickListener;

public class TrendingAislesRightColumnAdapter extends TrendingAislesGenericAdapter {
    private Context mContext;
    
    private final String TAG = "TrendingAislesRightColumnAdapter";
    
    private AisleLoader mLoader;
    
    private static final boolean DEBUG = false;
    
    public int firstX;
    public int lastX;
    
    public TrendingAislesRightColumnAdapter(Context c, ArrayList<AisleWindowContent> content) {
        super(c,content);
        mContext = c;
        if(DEBUG) Log.e(TAG,"About to initiate request for trending aisles");
        mLoader = AisleLoader.getInstance(mContext);        
    }
    
    public TrendingAislesRightColumnAdapter(Context c, AisleContentClickListener listener, ArrayList<AisleWindowContent> content) {
        super(c, listener, content);
        mContext = c;
        mLoader = AisleLoader.getInstance(mContext);
        
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
        }
        //AisleWindowContent windowContent = (AisleWindowContent)getItem(position);

        holder = (ViewHolder) convertView.getTag();
        holder.mWindowContent = (AisleWindowContent)getItem(position);
        holder.aisleContentBrowser.setAisleContentClickListener(mClickListener);
        int scrollIndex = 0; //getContentBrowserIndexForId(windowContent.getAisleId());
        //if(!mIsScrolling)
            mLoader.getAisleContentIntoView(holder, scrollIndex, position, false);
        
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
        holder.aisleContext.setText(occasion + " : " + lookingFor);
        //holder.aisleContext.setText(contextBuilder.toString());
        return convertView;
    }

    @Override
    public void onAisleDataUpdated(int newCount){
        notifyDataSetChanged();
    }

}