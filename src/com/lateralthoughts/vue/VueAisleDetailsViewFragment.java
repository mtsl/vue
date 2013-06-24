package com.lateralthoughts.vue;

//generic android & java goodies
import com.lateralthoughts.vue.indicators.IndicatorView;
import com.lateralthoughts.vue.ui.AisleContentBrowser.AisleDetailSwipeListener;
import com.lateralthoughts.vue.ui.MyCustomAnimation;
import com.lateralthoughts.vue.utils.Helper;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.support.v4.app.Fragment;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.webkit.WebView.FindListener;
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
    private ScrollView mAisleDetailsList;
    AisleDetailsSwipeListner mSwipeListener;
    IndicatorView mIndicatorView;
    private int mCurrentScreen;
    private int  mTotalScreenCount ;
    private String mScreenDirection;
    private LinearLayout mVueDetailsContainer;

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
        mAisleDetailsAdapter = new AisleDetailsViewAdapter(mContext,mSwipeListener, null);
        mVueDetailsContainer = mAisleDetailsAdapter.prepareDetailsVue();
        
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        //TODO: any particular state that we want to restore?
        
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.aisles_detailed_view_fragment, container, false);
        
        mAisleDetailsList = (ScrollView)v.findViewById(R.id.aisle_details_list);  
       // mAisleDetailsList.setOverScrollMode(View.OVER_SCROLL_NEVER);
       // mAisleDetailsAdapter.addComments(mVueDetailsContainer.findViewById(R.id.vuewndow_user_comments_lay));
        ListView list = (ListView) mVueDetailsContainer.findViewById(R.id.commentsList);
        LayoutInflater layoutInflator = LayoutInflater.from(mContext);
    	View headerView = layoutInflator.inflate(R.layout.addcomment, null);
         final EditText edtcomment = (EditText)headerView.findViewById(R.id.edtcomment);
         TextView enterComment = (TextView) headerView.findViewById(R.id.vue_user_entercomment);
         
         
         
         
         enterComment.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
			
		         MyCustomAnimation a = new MyCustomAnimation(
						 getActivity(), edtcomment,
						500, MyCustomAnimation.EXPAND);
				a.setHeight(100); 
				edtcomment.setVisibility(View.VISIBLE);
				edtcomment.startAnimation(a);
				edtcomment.setFocusable(true);

		         if(edtcomment.requestFocus()) {
		        	 ((InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
		        	}
				
			}
		});
         list.addHeaderView(headerView);
        

         
         
        list.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				return false;
			}
		});
		list.setAdapter(new Comments());
        
        Helper.getListViewSize(list);
        list.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int action = event.getAction();
				switch(action) {
				 case MotionEvent.ACTION_DOWN:
		                // Disallow ScrollView to intercept touch events.
		                v.getParent().requestDisallowInterceptTouchEvent(true);
		                break;

		            case MotionEvent.ACTION_UP:
		                // Allow ScrollView to intercept touch events.
		                v.getParent().requestDisallowInterceptTouchEvent(false);
		                break;
		            }

		            // Handle ListView touch events.
		            v.onTouchEvent(event);
		            return true;
		 
			}
		});
        mAisleDetailsList.addView(mVueDetailsContainer);
       // mAisleDetailsList.setAdapter(mAisleDetailsAdapter);
       // mAisleDetailsAdapter.notifyDataSetChanged();
        final LinearLayout dot_indicator_bg = (LinearLayout)v.findViewById(R.id.dot_indicator_bg);
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
       
        mIndicatorView.switchToScreen(mCurrentScreen, mCurrentScreen);
        
        
        
        
        dot_indicator_bg.getBackground().setAlpha(45);
        new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO need to invisible this view in a smooth way
				dot_indicator_bg.setVisibility(View.GONE);
			}
		}, 5000);
        
        /*mAisleDetailsList.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Log.e("VinoTouchFragment","onClick in fragment. Id = " + v.getId());
                
            }
        });*/
        Log.d("VueAisleDetailsViewFragment","Get ready to display details view");
        return v;
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
			mIndicatorView.switchToScreen(mCurrentScreen,checkScreenBoundaries(direction,mCurrentScreen)
					 );
		}
    	public void onReceiveImageCount(int count) {
    		mTotalScreenCount = count;
    		//Toast.makeText(getActivity(), "swipe image Count: "+count, Toast.LENGTH_LONG).show();
    	}
    }
    private int checkScreenBoundaries(String direction,int mCurrentScreen){
		  if(direction.equalsIgnoreCase("left")) {
			  if(mCurrentScreen == mTotalScreenCount){
				  this.mCurrentScreen = mTotalScreenCount;
				  return this.mCurrentScreen;
			  } else if(mCurrentScreen < mTotalScreenCount){
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
	private class Comments extends BaseAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return 10;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
		    CommentsHolder commentHolder;
		    if(convertView == null) {
		    	commentHolder = new CommentsHolder();
		    	LayoutInflater layoutInflator = LayoutInflater.from(mContext);
		    	convertView = layoutInflator.inflate(R.layout.comments, null);
		    	commentHolder. userPic = (ImageView) convertView
						.findViewById(R.id.vue_user_img);
		    	commentHolder.userComment = (TextView) convertView
						.findViewById(R.id.vue_user_comment);
		    	commentHolder.userComment.setTextSize(VueApplication.getInstance().getmTextSize());
		    	commentHolder. enterComment = (TextView) convertView
						.findViewById(R.id.vue_user_entercomment);
		    	
		    	
		    	convertView.setTag(commentHolder);
		    }
			commentHolder = (CommentsHolder) convertView.getTag();
		 
		
		
			if (position == 9) {
 
				commentHolder. enterComment.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						Toast.makeText(mContext, "clicked", Toast.LENGTH_SHORT)
								.show();

					}
				});

			}
			return convertView;
		}
		
	}
  private class CommentsHolder {
	  TextView userComment,enterComment;
	  ImageView userPic;
  }
}
