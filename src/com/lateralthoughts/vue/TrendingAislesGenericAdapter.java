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

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.lateralthoughts.vue.ui.AisleContentBrowser;
import com.lateralthoughts.vue.ui.AisleContentBrowser.AisleContentClickListener;

public class TrendingAislesGenericAdapter extends BaseAdapter implements
        IAisleDataObserver {
    private Context mContext;
    
    private final String TAG = "TrendingAislesGenericAdapter";
    
    protected AisleLoader mLoader;
    public boolean mAnimationInProgress;
    protected boolean mIsScrolling;
    protected AisleContentClickListener mClickListener;
    protected VueTrendingAislesDataModel mVueTrendingAislesDataModel;
    
    public TrendingAislesGenericAdapter(Context c,
            ArrayList<AisleWindowContent> content) {
        mContext = c;
        mVueTrendingAislesDataModel = VueTrendingAislesDataModel
                .getInstance(mContext);
        mVueTrendingAislesDataModel.registerAisleDataObserver(this);
        mLoader = AisleLoader.getInstance(mContext);
        mIsScrolling = false;
    }
    
    public TrendingAislesGenericAdapter(Context c,
            AisleContentClickListener listener,
            ArrayList<AisleWindowContent> content) {
        mContext = c;
        
        mVueTrendingAislesDataModel = VueTrendingAislesDataModel
                .getInstance(mContext);
        mVueTrendingAislesDataModel.registerAisleDataObserver(this);
        mLoader = AisleLoader.getInstance(mContext);
        mIsScrolling = false;
        mClickListener = listener;
    }
    
    public int getCount() {
        return mVueTrendingAislesDataModel.getAisleCount() / 2;
    }
    
    public AisleWindowContent getItem(int position) {
        return mVueTrendingAislesDataModel.getAisleAt(position);
    }
    
    public long getItemId(int position) {
        return 0;
    }
    
    public boolean hasStableIds() {
        return false;
    }
    
    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        return convertView;
    }
    
    @Override
    public void onAisleDataUpdated(int newCount) {
        notifyDataSetChanged();
        
    }
    
    private int calculateActualPosition(int viewPosition) {
        int actualPosition = 0;
        if (0 != viewPosition)
            actualPosition = (viewPosition * 2);
        
        return actualPosition;
    }
    
    public void setIsScrolling(boolean isScrolling) {
        
    }
    
    static class ViewHolder {
        AisleContentBrowser aisleContentBrowser;
        TextView aisleOwnersName;
        TextView aisleContext;
        ImageView starIcon;
        NetworkImageView profileThumbnail;
        String uniqueContentId;
        LinearLayout aisleDescriptor;
        AisleWindowContent mWindowContent;
        LinearLayout aisleselectlay;
        View viewBar;
        TextView likeCount, bookMarkCount,share_count;
        ImageView bookmarkImageView,shareImage;
        RelativeLayout socialCard;
    }
    
}
