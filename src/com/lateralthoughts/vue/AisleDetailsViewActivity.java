package com.lateralthoughts.vue;

//generic android goodies

import com.actionbarsherlock.view.Window;
import com.slidingmenu.lib.SlidingMenu;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;

public class AisleDetailsViewActivity extends BaseActivity/*FragmentActivity*/  {
    Fragment mFragRight;
    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;
    @SuppressLint("NewApi")
    @Override
    public void onCreate(Bundle icicle){
        super.onCreate(icicle);
       // setContentView(R.layout.vuedetails_frag);
        setContentView(R.layout.aisle_details_activity_landing);
        getSupportActionBar().hide();
        
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
       /* int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= 11){
        
        } */
 
   /* 	mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);*/
       
      
 
        
        
    mFragRight = new VueComparisionFragment();
    getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
    final SlidingMenu sm = getSlidingMenu();
    sm.setMode(SlidingMenu.LEFT_RIGHT);
    sm.setSecondaryMenu(R.layout.menu_frame_two);
    getSupportFragmentManager().beginTransaction()
        .replace(R.id.menu_frame_two, mFragRight).commit();
    sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
       
	 
        //sm.setSecondaryShadowDrawable(R.drawable.shadowright);
       // sm.setShadowDrawable(R.drawable.shadow);
       
    
      
 
    }
    class SectionsPagerAdapter extends FragmentPagerAdapter {

   		public SectionsPagerAdapter(FragmentManager fm) {
   			super(fm);
   		}

   		@Override
   		public Fragment getItem(int position) {
   		 
   			Fragment fragment;
   			 
   			
   			if(position == 0) {
   				fragment = new VueAisleDetailsViewFragment();
   			} else {
   				fragment = new VueComparisionFragment();
   			}
   			
   			
   			return fragment;
   		}

   		@Override
   		public int getCount() {
   			 
   			return 2;
   		}
 
   	}
    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    public void onPause(){
        super.onPause();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("share+", "details activity result"+requestCode+resultCode);
     
        try {
			VueAisleDetailsViewFragment fragment = (VueAisleDetailsViewFragment) getSupportFragmentManager().findFragmentById(R.id.aisle_details_view_fragment);
			  
			if(fragment.mAisleDetailsAdapter.share.shareIntentCalled)
			{
				fragment.mAisleDetailsAdapter.share.shareIntentCalled = false;
			    fragment.mAisleDetailsAdapter.share.dismisDialog();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
 
      /*@Override
      public boolean onCreateOptionsMenu(Menu menu) {
          getMenuInflater().inflate(R.menu.title_options, menu);
          // Configure the search info and add any event listeners
          return super.onCreateOptionsMenu(menu);
      }*/
}