package com.lateralthoughts.vue;

//generic android & java goodies
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.app.Activity;
import android.content.Context;
import android.widget.ListView;
import android.view.View.OnTouchListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.view.MotionEvent;

//java utils

//goodies from support libs
import android.support.v4.widget.StaggeredGridView;

//as soon as this fragment attaches to the activity we will go ahead and pull up the
//the list of current trending aisles.
//when the result is received we parse it and coalesce it into an array list of 
//AisleWindowContent objects. At this point we are ready to setup the adapter for the
//mTrendingAislesContentView.

public class VueLandingAislesFragment extends Fragment {
	private Context mContext;
	private VueContentGateway mVueContentGateway;
	
	private StaggeredGridView mTrendingAislesContentView;
	private TrendingAislesAdapter mContentAdapter;
	private TrendingAislesLeftColumnAdapter mLeftColumnAdapter;
	private TrendingAislesRightColumnAdapter mRightColumnAdapter;
	
	private ListView mLeftColumnView;
	private ListView mRightColumnView;
	
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
		mTrendingAislesContentView = null;
		
		//without much ado lets get started with retrieving the trending aisles list
		mVueContentGateway = VueContentGateway.getInstance();
		if(null == mVueContentGateway){
			//assert here: this is a no go!
		}		
		//mContentAdapter = VueApplication.getInstance().getTrendingAislesAdapter();
		mContentAdapter = new TrendingAislesAdapter(mContext, null);
		mLeftColumnAdapter = new TrendingAislesLeftColumnAdapter(mContext, null);
		mRightColumnAdapter = new TrendingAislesRightColumnAdapter(mContext, null);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		//TODO: any particular state that we want to restore?
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		/*View v = inflater.inflate(R.layout.aisles_view_fragment, container, false);
		mTrendingAislesContentView = (StaggeredGridView) v.findViewById(R.id.aisles_grid);
		
		int margin = getResources().getDimensionPixelSize(R.dimen.margin);
		
		mTrendingAislesContentView.setItemMargin(margin); // set the GridView margin
		
		mTrendingAislesContentView.setPadding(margin, 0, margin, 0); // have the margin on the sides as well 
		mTrendingAislesContentView.setOverScrollMode(View.OVER_SCROLL_NEVER);
		mTrendingAislesContentView.setAdapter(mContentAdapter);*/
	    
	    //synchronized list view approach
	    View v = inflater.inflate(R.layout.aisles_view_fragment2, container, false);
	    mLeftColumnView = (ListView)v.findViewById(R.id.list_view_left);
	    
	    mRightColumnView = (ListView)v.findViewById(R.id.list_view_right);
	    mLeftColumnView.setAdapter(mLeftColumnAdapter);
	    mRightColumnView.setAdapter(mRightColumnAdapter);
	    
	    mLeftColumnView.setOnTouchListener(touchListener);
	    mRightColumnView.setOnTouchListener(touchListener);        
	    mLeftColumnView.setOnScrollListener(scrollListener);
	    mRightColumnView.setOnScrollListener(scrollListener);
	    
	    mLeftViewsHeights = new int[1000];
	    mRightViewsHeights = new int[1000]; 
	    
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
        public void onScrollStateChanged(AbsListView v, int scrollState) {  
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
            
        }
    };

}
