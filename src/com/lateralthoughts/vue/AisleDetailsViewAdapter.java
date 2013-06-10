/**
 * 
 * @author Vinodh Sundararajan
 * 
 **/

package com.lateralthoughts.vue;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.HorizontalScrollView;
import android.content.Context;
import android.util.Log;
import android.util.TypedValue;

//java util imports
import java.util.ArrayList;

//internal imports
import com.lateralthoughts.vue.ui.AisleContentBrowser;

public class AisleDetailsViewAdapter extends TrendingAislesGenericAdapter {
    private Context mContext;
    
    private final String TAG = "AisleDetailsViewAdapter";
    private static final boolean DEBUG = false;
    
    public int firstX;
    public int lastX;
    private AisleDetailsViewListLoader mViewLoader;
    
    //we need to customize the layout depending on screen height & width which
    //we will get on the fly
    private int mScreenHeight;
    private int mScreenWidth;
    private int mShowPieceHeight;
    private int mShowPieceWidth;
    private int mThumbnailsHeight;
    private int mActionBarHeight;

    public AisleDetailsViewAdapter(Context c, ArrayList<AisleWindowContent> content) {
        super(c, content);
        mContext = c;
        mViewLoader = AisleDetailsViewListLoader.getInstance(mContext);
        
        mScreenHeight = VueApplication.getInstance().getScreenHeight();
        mScreenWidth = VueApplication.getInstance().getScreenWidth();
        float scale = mContext.getResources().getDisplayMetrics().density;
        
        //the action bar height is 50 dp
        mActionBarHeight = (int)(50 * scale + 0.5f);
        
        TypedValue tv = new TypedValue();
        mContext.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true);
        int actionBarHeight = mContext.getResources().getDimensionPixelSize(tv.resourceId);
        
        //the show piece item would occupy about 60% of the screen
        mShowPieceHeight = (int)((mScreenHeight-actionBarHeight)*0.60f);
        mShowPieceWidth = (int)(mScreenWidth);
        //the thumbnail item would occupy about 25% of the screen
        mThumbnailsHeight = (int)(mScreenHeight - (mShowPieceHeight + mActionBarHeight)); //(int)(mScreenHeight*0.30f);
        
        if(DEBUG) Log.e(TAG,"About to initiate request for trending aisles");      
    }

    @Override
    public int getCount(){
        return mVueTrendingAislesDataModel.getAisleCount();
    }

    @Override
    public AisleWindowContent getItem(int position){     
        return mVueTrendingAislesDataModel.getAisleAt(position);
    }
    
    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {     
        ViewHolder holder;
        //StringBuilder sb = new StringBuilder();

        if (null == convertView) {
            LayoutInflater layoutInflator = LayoutInflater.from(mContext);
            AbsListView.LayoutParams params = new AbsListView.LayoutParams(mScreenWidth, mScreenHeight-156);
            convertView = layoutInflator.inflate(R.layout.aisle_detailed_view_row_item, null);
            //convertView.setLayoutParams(params)
            convertView.setLayoutParams(params);

            holder = new ViewHolder();
            holder.aisleContentBrowser = (AisleContentBrowser) convertView.findViewById(R.id.showpiece);
            FrameLayout fl = (FrameLayout) convertView.findViewById(R.id.showpiece_container);
            //holder.thumbnailContainer = (HorizontalScrollView)convertView.findViewById(R.id.thumbnail_scroller_container);
            holder.thumbnailScroller = (LinearLayout)convertView.findViewById(R.id.thumbnail_scroller);
            FrameLayout.LayoutParams showpieceParams = 
                    new FrameLayout.LayoutParams(mShowPieceWidth, mShowPieceHeight);
            LinearLayout.LayoutParams containerParams = 
                    new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            
            holder.aisleContentBrowser.setLayoutParams(showpieceParams);
            
            FrameLayout.LayoutParams thumbnailParams = 
                    new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, mThumbnailsHeight);
            holder.thumbnailScroller.setLayoutParams(thumbnailParams);
            
            holder.uniqueContentId = AisleWindowContent.EMPTY_AISLE_CONTENT_ID;
            convertView.setTag(holder);
        }
        
        holder = (ViewHolder) convertView.getTag();
        holder.mWindowContent = (AisleWindowContent)getItem(position);
        int scrollIndex = 0;
        mViewLoader.getAisleContentIntoView(holder, scrollIndex, position);
        return convertView;
    }
    
    static class ViewHolder{
        AisleContentBrowser aisleContentBrowser;
        HorizontalScrollView thumbnailContainer;
        LinearLayout thumbnailScroller;
        TextView aisleOwnersName;
        TextView aisleContext;
        ImageView profileThumbnail;
        String uniqueContentId;
        LinearLayout aisleDescriptor;
        AisleWindowContent mWindowContent;
    }
}
