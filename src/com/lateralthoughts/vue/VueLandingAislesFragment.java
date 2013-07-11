package com.lateralthoughts.vue;

//generic android & java goodies
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.app.Activity;
import android.content.Intent;
import android.content.Context;
import android.widget.ListView;
import android.view.View.OnTouchListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.view.MotionEvent;
import android.widget.AdapterView;

import com.actionbarsherlock.app.SherlockFragment;
import com.lateralthoughts.vue.ui.AisleContentBrowser.AisleContentClickListener;

//java utils

//as soon as this fragment attaches to the activity we will go ahead and pull up the
//the list of current trending aisles.
//when the result is received we parse it and coalesce it into an array list of 
//AisleWindowContent objects. At this point we are ready to setup the adapter for the
//mTrendingAislesContentView.

public class VueLandingAislesFragment extends SherlockFragment/*Fragment*/ {
	private Context mContext;
	private VueContentGateway mVueContentGateway;
	private TrendingAislesLeftColumnAdapter mLeftColumnAdapter;
	private TrendingAislesRightColumnAdapter mRightColumnAdapter;
	
	private ListView mLeftColumnView;
	private ListView mRightColumnView;
	private AisleClickListener mAisleClickListener;
	//private MultiColumnListView mView;
	
	int[] mLeftViewsHeights;
	int[] mRightViewsHeights;

	//TODO: define a public interface that can be implemented by the parent
	//activity so that we can notify it with an ArrayList of AisleWindowContent
	//once we have received the result and parsed it. The idea is that the activity
	//can then initiate a worker in the background to go fetch more content and get
	//ready to launch other activities/fragments within the application
	
	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		mContext = activity;
		
		//without much ado lets get started with retrieving the trending aisles list
		mVueContentGateway = VueContentGateway.getInstance();
		if(null == mVueContentGateway){
			//assert here: this is a no go!
		}
	        
		mAisleClickListener = new AisleClickListener();
		mLeftColumnAdapter = new TrendingAislesLeftColumnAdapter(mContext, mAisleClickListener, null);
		mRightColumnAdapter = new TrendingAislesRightColumnAdapter(mContext, mAisleClickListener, null);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		//TODO: any particular state that we want to restore?
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
	    
	    //synchronized list view approach
	    View v = inflater.inflate(R.layout.aisles_view_fragment2, container, false);
	    
	    mLeftColumnView = (ListView)v.findViewById(R.id.list_view_left);	    
	    mRightColumnView = (ListView)v.findViewById(R.id.list_view_right);
	    
	    mLeftColumnView.setAdapter(mLeftColumnAdapter);
	    mRightColumnView.setAdapter(mRightColumnAdapter);
	    
	    mLeftColumnView.setOverScrollMode(View.OVER_SCROLL_NEVER);
	    mRightColumnView.setOverScrollMode(View.OVER_SCROLL_NEVER);
	    mLeftColumnView.setOnTouchListener(touchListener);
	    mRightColumnView.setOnTouchListener(touchListener);        
	    mLeftColumnView.setOnScrollListener(scrollListener);
	    mRightColumnView.setOnScrollListener(scrollListener);
	    
	    mLeftColumnView.setClickable(true);
	    /*mLeftColumnView.setOnItemClickListener(new OnItemClickListener(){
	        public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
                Intent intent = new Intent();
                intent.setClass(VueApplication.getInstance(), AisleDetailsViewActivity.class);
                mContext.startActivity(intent);
	         }
	    });
	    
	    mRightColumnView.setOnItemClickListener(new OnItemClickListener(){
	            public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
	                //MyClass selItem = (MyClass) adapter.getItem(position);
	                Intent intent = new Intent();
	                intent.setClass(VueApplication.getInstance(), AisleDetailsViewActivity.class);
	                mContext.startActivity(intent);
	             }
	        });*/
	       mLeftColumnView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	            @Override
	            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
	                Log.e("Vinodh Clicks","ok...we are getting item clicks!!");
	                
	            }
	        });
	       
	    mLeftViewsHeights = new int[1000];
	    mRightViewsHeights = new int[1000];
	    Log.d("VueLandingAislesFragment","Get ready to displayed staggered view");
	    
        return v;
	}
	   // Passing the touch event to the opposite list
    OnTouchListener touchListener = new OnTouchListener() {                 
        boolean dispatched = false;
        
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (v.equals(mLeftColumnView) && !dispatched) {
                dispatched = true;
                mRightColumnView.dispatchTouchEvent(event);
            } else if (v.equals(mRightColumnView) && !dispatched) {
                dispatched = true;
                mLeftColumnView.dispatchTouchEvent(event);
            }
            
            dispatched = false;
            return false;
        }
    };
    
    /**
     * Synchronizing scrolling 
     * Distance from the top of the first visible element opposite list:
     * sum_heights(opposite invisible screens) - sum_heights(invisible screens) + distance from top of the first visible child
     */
    OnScrollListener scrollListener = new OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            mLeftColumnAdapter.setIsScrolling(scrollState != SCROLL_STATE_IDLE);
            mRightColumnAdapter.setIsScrolling(scrollState != SCROLL_STATE_IDLE);
            
            int first = view.getFirstVisiblePosition();
            int count = view.getChildCount();

            if (scrollState == SCROLL_STATE_IDLE || (first + count > mLeftColumnAdapter.getCount())
                    || (first + count > mRightColumnAdapter.getCount())) {
                mLeftColumnView.invalidateViews();
                mRightColumnView.invalidateViews();
            }
        }
        
        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                int visibleItemCount, int totalItemCount) {
            
            if (view.getChildAt(0) != null) {
                if (view.equals(mLeftColumnView) ){
                    mLeftViewsHeights[view.getFirstVisiblePosition()] = view.getChildAt(0).getHeight();
                    
                    int h = 0;
                    for (int i = 0; i < mRightColumnView.getFirstVisiblePosition(); i++) {
                        h += mRightViewsHeights[i];
                    }
                    
                    int hi = 0;
                    for (int i = 0; i < mLeftColumnView.getFirstVisiblePosition(); i++) {
                        hi += mLeftViewsHeights[i];
                    }
                    
                    int top = h - hi + view.getChildAt(0).getTop();
                    mRightColumnView.setSelectionFromTop(mRightColumnView.getFirstVisiblePosition(), top);
                } else if (view.equals(mRightColumnView)) {
                    mRightViewsHeights[view.getFirstVisiblePosition()] = view.getChildAt(0).getHeight();
                    
                    int h = 0;
                    for (int i = 0; i < mLeftColumnView.getFirstVisiblePosition(); i++) {
                        h += mLeftViewsHeights[i];
                    }
                    
                    int hi = 0;
                    for (int i = 0; i < mRightColumnView.getFirstVisiblePosition(); i++) {
                        hi += mRightViewsHeights[i];
                    }
                    
                    int top = h - hi + view.getChildAt(0).getTop();
                    mLeftColumnView.setSelectionFromTop(mLeftColumnView.getFirstVisiblePosition(), top);
                }
                
            }
            
          /*  if(VueTrendingAislesDataModel.getInstance(mContext).loadOnRequest) {
              int lastVisiblePosition = firstVisibleItem + visibleItemCount;
              int totalItems = 0;
              if (view.equals(mLeftColumnView) ){
                totalItems = mLeftColumnAdapter.getCount();
              } else if (view.equals(mRightColumnView) ){
                totalItems = mRightColumnAdapter.getCount();
              }
              if((totalItems - lastVisiblePosition) < 5) {
                VueTrendingAislesDataModel.getInstance(mContext).loadMoreAisles();
              }
            }*/
            
        }
    };
    
    private class AisleClickListener implements AisleContentClickListener{
        @Override
        public void onAisleClicked(String id,int count){
            Intent intent = new Intent();
            intent.setClass(VueApplication.getInstance(), AisleDetailsViewActivity.class);
            VueApplication.getInstance().setClickedWindowID(id);
            VueApplication.getInstance().setClickedWindowCount(count);
            startActivity(intent);
        }
    }

}
