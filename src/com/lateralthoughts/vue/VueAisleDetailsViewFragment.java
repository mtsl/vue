package com.lateralthoughts.vue;

//generic android & java goodies
import com.lateralthoughts.vue.indicators.IndicatorView;
import com.lateralthoughts.vue.ui.AisleContentBrowser.AisleDetailSwipeListener;
import com.lateralthoughts.vue.ui.MyCustomAnimation;
import com.lateralthoughts.vue.utils.Helper;
import com.lateralthoughts.vue.utils.Utils;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.support.v4.app.Fragment;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.webkit.WebView.FindListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

//java utils

//as soon as this fragment attaches to the activity we will go ahead and pull up the
//the list of current trending aisles.
//when the result is received we parse it and coalesce it into an array list of 
//AisleWindowContent objects. At this point we are ready to setup the adapter for the
//mTrendingAislesContentView.

public class VueAisleDetailsViewFragment extends Fragment {
    private Context mContext;
    private VueContentGateway mVueContentGateway;
    private AisleDetailsViewAdapter mAisleDetailsAdapter;  
    private ListView mAisleDetailsList;
    AisleDetailsSwipeListner mSwipeListener;
    IndicatorView mIndicatorView;
    private int mCurrentScreen;
    private int  mTotalScreenCount ;
    private String mScreenDirection;
    private LinearLayout mVueDetailsContainer;
    int mlistCount =5;
    int mFirstVisibleItem;
    int mVisibleItemCount;
    TextView vue_user_name;
    
    int mCurentIndPosition;
    static final int MAX_DOTS_TO_SHOW = 3;
    int mPrevPosition;

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
        Log.i("windowID", "windowID: receivedd  "+ VueApplication.getInstance().getClickedWindowID());
        mSwipeListener = new AisleDetailsSwipeListner();
        mAisleDetailsAdapter = new AisleDetailsViewAdapter(mContext,mSwipeListener,mlistCount ,null);
        //mVueDetailsContainer = mAisleDetailsAdapter.prepareDetailsVue();
        
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        //TODO: any particular state that we want to restore?
        
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.aisles_detailed_view_fragment, container, false);
        mAisleDetailsList = (ListView)v.findViewById(R.id.aisle_details_list); 
        mAisleDetailsList.setAdapter(mAisleDetailsAdapter);
        ImageView vue_aisle = (ImageView) v.findViewById(R.id.vue_aisle);
        vue_aisle.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
 
				/* Intent intent = new Intent();
		            intent.setClass(VueApplication.getInstance(), VueComparisionActivity.class);
		            startActivity(intent); */
				
			}
		});
      
          vue_user_name = (TextView) v.findViewById(R.id.vue_user_name);
           vue_user_name.setTextSize(Utils.MEDIUM_TEXT_SIZE);
 
 
     
        final LinearLayout dot_indicator_bg = (LinearLayout)v.findViewById(R.id.dot_indicator_bg);
        
          TextView vue_aisle_heading = (TextView)v.findViewById(R.id.vue_aisle_heading);
          vue_aisle_heading.setTextSize(Utils.LARGE_TEXT_SIZE);
        RelativeLayout vue_image_indicator = (RelativeLayout)v.findViewById(R.id.vue_image_indicator);
   /*     TextView  leftGo = (TextView) v.findViewById(R.id.leftgo);
        TextView  rightGo = (TextView) v.findViewById(R.id.rightgo);*/
        
        mIndicatorView = new IndicatorView(getActivity());
        mIndicatorView.setId(1234);
        RelativeLayout.LayoutParams relParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
         
        relParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        relParams.addRule(RelativeLayout.CENTER_VERTICAL);
        mIndicatorView.setLayoutParams(relParams);
        vue_image_indicator.addView(mIndicatorView);
        RelativeLayout.LayoutParams relParams1 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        relParams1.addRule(RelativeLayout.CENTER_VERTICAL);
        relParams1.addRule(RelativeLayout.LEFT_OF, mIndicatorView.getId());
       // leftGo.setLayoutParams(relParams1);
        RelativeLayout.LayoutParams relParams2 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        relParams2.addRule(RelativeLayout.RIGHT_OF, mIndicatorView.getId());
        relParams2.addRule(RelativeLayout.CENTER_VERTICAL);
       
       // rightGo.setLayoutParams(relParams2);
        mIndicatorView.setNumberofScreens(mTotalScreenCount);
        mIndicatorView.setDrawables(R.drawable.number_active,
        R.drawable.bullets_bg, R.drawable.number_inactive);
        mCurrentScreen = 1;
        mTotalScreenCount = VueApplication.getInstance().getClickedWindowCount();
        mIndicatorView.setNumberofScreens(mTotalScreenCount);
        mIndicatorView.switchToScreen(mCurrentScreen, mCurrentScreen);
 
        
        mAisleDetailsList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				if (arg2 != 0 && arg2 != 1) {
					if (mlistCount - 1 == arg2) {
		 
						TextView v = (TextView) arg1
						.findViewById(R.id.vue_user_entercomment);
						EditText vue_edt = (EditText)arg1.findViewById(R.id.edtcomment);
						vue_edt.setVisibility(View.VISIBLE);
						MyCustomAnimation manim = new MyCustomAnimation(getActivity(), vue_edt, 500, MyCustomAnimation.EXPAND);
						vue_edt.startAnimation(manim);
						vue_edt.setFocusable(true);
						mAisleDetailsAdapter.notifyDataSetChanged();
						 
					} else {

						TextView v = (TextView) arg1
								.findViewById(R.id.vue_user_comment);
						int x = v.getLineCount();
						if (x == 2) {
							LinearLayout.LayoutParams params;
							params = new LinearLayout.LayoutParams(
									LayoutParams.MATCH_PARENT,
									LayoutParams.WRAP_CONTENT);
							params.setMargins(VueApplication.getInstance()
									.getPixel(4), VueApplication.getInstance()
									.getPixel(4), VueApplication.getInstance()
									.getPixel(4), VueApplication.getInstance()
									.getPixel(4));
							v.setLayoutParams(params);
							v.setMaxLines(Integer.MAX_VALUE);
						} else {
							v.setMaxLines(2);
						}
					}
				} else if (arg2 == 0) {

					TextView v = (TextView) arg1
							.findViewById(R.id.vue_details_descreption);
					int x = v.getLineCount();
					if (x == 3) {
						LinearLayout.LayoutParams params;
						params = new LinearLayout.LayoutParams(
								LayoutParams.MATCH_PARENT,
								LayoutParams.WRAP_CONTENT);
						params.setMargins(VueApplication.getInstance()
								.getPixel(12), VueApplication.getInstance()
								.getPixel(4), VueApplication.getInstance()
								.getPixel(12), VueApplication.getInstance()
								.getPixel(4));
						v.setLayoutParams(params);
						v.setMaxLines(Integer.MAX_VALUE);
					} else {
						v.setMaxLines(3);
					}
				}
			}
		});
 
        
        
        dot_indicator_bg.getBackground().setAlpha(45);
        new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO need to invisible this view in a smooth way
				dot_indicator_bg.setVisibility(View.GONE);
			}
		}, 5000);
        
     
        Log.d("VueAisleDetailsViewFragment","Get ready to display details view");
        
        ImageView vue_share = (ImageView) v.findViewById(R.id.vue_share);
        
        vue_share.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
            
               mAisleDetailsAdapter.share(getActivity(), getActivity());
            	
            }
        });
        
        return v;
    }
    @Override
    public void onResume() {
    	super.onResume();
    	mAisleDetailsAdapter.notifyDataSetChanged();
    	
    	 
    	ViewTreeObserver vto = vue_user_name.getViewTreeObserver(); 
    	vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() { 
    	    @Override 
    	    public void onGlobalLayout() { 
    	        vue_user_name.getViewTreeObserver().removeGlobalOnLayoutListener(this); 
    	     
    	        vue_user_name.setText(mAisleDetailsAdapter.vue_user_name);
    	    } 
    	});
    	
    	
    	
/*        mAisleDetailsList.setOnScrollListener(new OnScrollListener() {
			
 			@Override
 			public void onScrollStateChanged(AbsListView view, int scrollState) {
 				 
 				switch(scrollState) {
 				case OnScrollListener.SCROLL_STATE_IDLE:
 					TextView v = (TextView) view.findViewById(R.id.vue_details_descreption);
 					TextView comment = (TextView) view.findViewById(R.id.vue_user_comment);
 					if(v != null) {
 						mAisleDetailsAdapter.setText(v,10,mAisleDetailsAdapter.mDescriptionDefaultHeight);
 					}
 					if(comment != null) {
 						mAisleDetailsAdapter.setText(comment,4,mAisleDetailsAdapter.mComentTextDefaultHeight); 
 					}
 					  break;
 				case OnScrollListener.SCROLL_STATE_FLING:
 					  break;
 				}
 			}
 			
 			@Override
 			public void onScroll(AbsListView view, int firstVisibleItem,
 					int visibleItemCount, int totalItemCount) {
 				TextView v = (TextView) view.findViewById(R.id.vue_details_descreption);
					if(v != null) {
						mAisleDetailsAdapter.setText(v,10,mAisleDetailsAdapter.mDescriptionDefaultHeight);
					}
 				mFirstVisibleItem = visibleItemCount;
 				mFirstVisibleItem = firstVisibleItem;
 			}
 		});*/
    }
    /**
     * 
     * @author raju
     *while swiping the views inside the AisleContentWindow onAisleSwipe method will be
     *called to idicate the current position of the image.
     */
    private class AisleDetailsSwipeListner implements AisleDetailSwipeListener {

		@Override
		public void onAisleSwipe(String direction) {
			// TODO Auto-generated method stub
			//Toast.makeText(getActivity(), "swipe: "+direction, Toast.LENGTH_LONG).show();
			//int x = checkScreenBoundaries(direction,mCurrentScreen);
			
			//Log.i("screenswitch", "screenswitch x: "+x);
			mPrevPosition = mCurrentScreen;
			mIndicatorView.switchToScreen(mPrevPosition,checkScreenBoundaries(direction,mCurrentScreen)
					 );
		}
    	public void onReceiveImageCount(int count) {
    		mTotalScreenCount = count;
    		//Toast.makeText(getActivity(), "swipe image Count: "+count, Toast.LENGTH_LONG).show();
    	}
		@Override
		public void onResetAdapter() {
			if(mlistCount == 5){
			mlistCount = 10;
			} else {
				mlistCount = 5;
			}
			 mAisleDetailsAdapter = new AisleDetailsViewAdapter(mContext,mSwipeListener,mlistCount ,null);
			 mAisleDetailsList.setAdapter(mAisleDetailsAdapter);
			
		}
    }
    private int checkScreenBoundaries(String direction,int mCurrentScreen){
		  if(direction.equalsIgnoreCase("left")) {
			  if(mCurrentScreen == mTotalScreenCount){
				  this.mCurrentScreen = mTotalScreenCount;
				  return this.mCurrentScreen;
			  } else if(mCurrentScreen < mTotalScreenCount){
				/*  if(mCurrentScreen < MAX_DOTS_TO_SHOW) {
					  this.mCurrentScreen++;
				  } else {
					  this.mCurrentScreen = getHighlightPosition(mCurrentScreen);
				  }*/
				  this.mCurrentScreen++;
				  
				  return this.mCurrentScreen;
			  }
		  } else {
			    if(direction.equalsIgnoreCase("right")) {
			    	if(mCurrentScreen == 1){
			    		this.mCurrentScreen = 1;
			    		return this.mCurrentScreen;
			    	} else {
			    		this.mCurrentScreen--;
			    		return this.mCurrentScreen;
			    	}
			    }
		  }
    	
    	return mCurrentScreen;
    	
    }
    
    private int getHighlightPosition(int cur_pos) {
    	mCurentIndPosition = cur_pos;
    	int highlightPosition;
    	if(mCurentIndPosition <= mTotalScreenCount) {
    		if(mCurentIndPosition+MAX_DOTS_TO_SHOW > mTotalScreenCount) {
    			int temp = mTotalScreenCount - mCurentIndPosition;
    			  highlightPosition = MAX_DOTS_TO_SHOW - temp;
    		} else {
    			int temp = mCurentIndPosition % MAX_DOTS_TO_SHOW;
    			highlightPosition = temp;
    		}
    		return highlightPosition;
    	}
		return mCurentIndPosition;
    }
}
