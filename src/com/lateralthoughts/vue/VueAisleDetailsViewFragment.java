package com.lateralthoughts.vue;

//generic android & java goodies
import com.lateralthoughts.vue.indicators.IndicatorView;
import com.lateralthoughts.vue.ui.AisleContentBrowser.AisleDetailSwipeListener;
import com.lateralthoughts.vue.ui.MyCustomAnimation;
import com.lateralthoughts.vue.utils.Helper;
import com.lateralthoughts.vue.utils.Utils;

import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
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
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.support.v4.app.Fragment;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

//java utils

//as soon as this fragment attaches to the activity we will go ahead and pull up the
//the list of current trending aisles.
//when the result is received we parse it and coalesce it into an array list of 
//AisleWindowContent objects. At this point we are ready to setup the adapter for the
//mTrendingAislesContentView.

public class VueAisleDetailsViewFragment extends Fragment {
    private Context mContext;
    public  static final  String  SCREEN_NAME = "DETAILS_SCREEN";
    private static final int AISLE_HEADER_SHOW_TIME = 5000;
    private static final int ANIM_SPEED_EDITEXPAND = 500;
    private static final int USER_COMMENT_MARIGIN = 4;
    private VueContentGateway mVueContentGateway;
    AisleDetailsViewAdapter mAisleDetailsAdapter;  
    private ListView mAisleDetailsList;
    AisleDetailsSwipeListner mSwipeListener;
    IndicatorView mIndicatorView;
    private int mCurrentScreen;
    private int  mTotalScreenCount ;
    private String mScreenDirection;
    private LinearLayout mVueDetailsContainer;
    int mListCount =5;
    int mFirstVisibleItem;
    int mVisibleItemCount;
    TextView mVueUserName;
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
        // adding test comment
        //without much ado lets get started with retrieving the trending aisles list
        mVueContentGateway = VueContentGateway.getInstance();
        if(null == mVueContentGateway){
            //assert here: this is a no go!
        }  
        mSwipeListener = new AisleDetailsSwipeListner();
        mAisleDetailsAdapter = new AisleDetailsViewAdapter(mContext,mSwipeListener,mListCount ,null);
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
          mVueUserName = (TextView) v.findViewById(R.id.vue_user_name);
           mVueUserName.setTextSize(Utils.MEDIUM_TEXT_SIZE);
 
 
     
        final LinearLayout dotIndicatorBg = (LinearLayout)v.findViewById(R.id.dot_indicator_bg);
        
          TextView vueAisleHeading = (TextView)v.findViewById(R.id.vue_aisle_heading);
          vueAisleHeading.setTextSize(Utils.LARGE_TEXT_SIZE);
        RelativeLayout mVueImageIndicator = (RelativeLayout)v.findViewById(R.id.vue_image_indicator);
   /*     TextView  leftGo = (TextView) v.findViewById(R.id.leftgo);
        TextView  rightGo = (TextView) v.findViewById(R.id.rightgo);*/
        mIndicatorView = new IndicatorView(getActivity());
        mIndicatorView.setId(1234);
        RelativeLayout.LayoutParams relParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        relParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        relParams.addRule(RelativeLayout.CENTER_VERTICAL);
        mIndicatorView.setLayoutParams(relParams);
        mVueImageIndicator.addView(mIndicatorView);
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
               if (mListCount - 1 == arg2) {
                  TextView v = (TextView) arg1
                        .findViewById(R.id.vue_user_entercomment);
                  EditText vueEdt = (EditText) arg1
                        .findViewById(R.id.edtcomment);
                  vueEdt.setVisibility(View.VISIBLE);
                  MyCustomAnimation manim = new MyCustomAnimation(
                        getActivity(), vueEdt,ANIM_SPEED_EDITEXPAND ,
                        MyCustomAnimation.EXPAND);
                  vueEdt.startAnimation(manim);
                  vueEdt.setFocusable(true);
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
                     params.setMargins(
                           VueApplication.getInstance().getPixel(
                                 USER_COMMENT_MARIGIN),
                           VueApplication.getInstance().getPixel(
                                 USER_COMMENT_MARIGIN),
                           VueApplication.getInstance().getPixel(
                                 USER_COMMENT_MARIGIN),
                           VueApplication.getInstance().getPixel(
                                 USER_COMMENT_MARIGIN));
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
        dotIndicatorBg.getBackground().setAlpha(45);
      new Handler().postDelayed(new Runnable() {

 
			@Override
			public void run() {
				// TODO need to invisible this view in a smooth way
				dotIndicatorBg.setVisibility(View.GONE);
			}
		},AISLE_HEADER_SHOW_TIME);
		RelativeLayout vueShareLayout = (RelativeLayout) v
				.findViewById(R.id.vuesharelayout);
		vueShareLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				mAisleDetailsAdapter.share(getActivity(), getActivity());
			}
		});
        return v;
    }
    @Override
    public void onResume() {
       super.onResume();
       mAisleDetailsAdapter.notifyDataSetChanged();
       ViewTreeObserver vto = mVueUserName.getViewTreeObserver(); 
       vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() { 
           @Override 
           public void onGlobalLayout() { 
               mVueUserName.getViewTreeObserver().removeGlobalOnLayoutListener(this); 
               mVueUserName.setText(mAisleDetailsAdapter.mVueusername);
           } 
       });
    }
    /**
     * 
     *  
     *while swiping the views inside the AisleContentWindow onAisleSwipe method will be
     *called to indicate the current position of the image.
     */
    private class AisleDetailsSwipeListner implements AisleDetailSwipeListener {

      @Override
      public void onAisleSwipe(String direction) {
         mPrevPosition = mCurrentScreen;
         mIndicatorView.switchToScreen(mPrevPosition,checkScreenBoundaries(direction,mCurrentScreen)
                );
      }
       public void onReceiveImageCount(int count) {
          mTotalScreenCount = count;
       }
      @Override
      public void onResetAdapter() {
         //TODO: need to remove these counts when original comments available.
         if(mListCount == 5){
         mListCount = 10;
         } else {
            mListCount = 5;
         }
          mAisleDetailsAdapter = new AisleDetailsViewAdapter(mContext,mSwipeListener,mListCount ,null);
          mAisleDetailsList.setAdapter(mAisleDetailsAdapter);
         
      }
      /**
       * when user enters the comment it will be added to the comment list at the top.
       */
      @Override
      public void onAddCommentClick(final TextView view, final EditText editText) {
         mAisleDetailsList.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
          final InputMethodManager inputMethodManager=(InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
             inputMethodManager.toggleSoftInputFromWindow(editText.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0); 
          editText.setVisibility(View.VISIBLE);
          editText.setCursorVisible(true);
          editText.setTextColor(Color.parseColor(getResources().getString(R.color.black)));
          editText.requestFocus();
          editText.setFocusable(true);
          editText.setImeOptions(EditorInfo.IME_ACTION_GO);
          editText.setScroller(new Scroller(getActivity()));
          editText.setVerticalScrollBarEnabled(true);
          editText.setMovementMethod(new ScrollingMovementMethod());
          editText.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            	
            	
            	editText.setOnTouchListener(new OnTouchListener() {
            	       public boolean onTouch(View view, MotionEvent event) {
            	            // TODO Auto-generated method stub
            	            if (view.getId() == R.id.edtcomment) {
            	            	mAisleDetailsList.requestDisallowInterceptTouchEvent(true);
            	                switch (event.getAction()&MotionEvent.ACTION_MASK){
            	                case MotionEvent.ACTION_UP:
            	                	mAisleDetailsList.requestDisallowInterceptTouchEvent(false);
            	                    break;
            	                
            	                }
            	            }
            	            return false;
            	        }
            	    });
            	
            	
            	
            	
            	
            	
            	
            	
            	
            	 if(actionId == EditorInfo.IME_ACTION_DONE) {
            		 
            	 } else if(actionId == EditorInfo.IME_ACTION_GO) {
            		 
            	 } else if(actionId == EditorInfo.IME_ACTION_NEXT) {
            		 
            	 }
            	 
                inputMethodManager.toggleSoftInputFromWindow(editText.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0); 
                mAisleDetailsAdapter.mTempComments2 =  new String[mAisleDetailsAdapter.mTempComments.length+1];
                mAisleDetailsAdapter.mTempComments2[0] = v.getText().toString();
                for(int i =0;i< mAisleDetailsAdapter.mTempComments.length;i++) {
                mAisleDetailsAdapter.mTempComments2[i+1]= mAisleDetailsAdapter.mTempComments[i];
                }
                mAisleDetailsAdapter.mTempComments =  new String[mAisleDetailsAdapter.mTempComments.length+1];
                for(int i =0;i< mAisleDetailsAdapter.mTempComments2.length;i++) {
                   mAisleDetailsAdapter.mTempComments[i]= mAisleDetailsAdapter.mTempComments2[i];
                   }
                editText.setVisibility(View.GONE);
                editText.setText("");
                view.setVisibility(View.VISIBLE);
                mAisleDetailsAdapter.notifyDataSetChanged();
               return false;
            }
         });
          view.setVisibility(View.GONE);

         
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
    protected MotionEvent mLastOnDownEvent = null;
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
