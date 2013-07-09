package com.lateralthoughts.vue;

//generic android goodies
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import com.slidingmenu.lib.SlidingMenu;

public class AisleDetailsViewActivity extends BaseActivity/* FragmentActivity*/  {
	
  VueComparisionFragment mFragRight;
	
    @SuppressLint("NewApi")
	@Override
    public void onCreate(Bundle icicle){
        super.onCreate(icicle);
        setContentView(R.layout.aisle_details_activity_landing);
 
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= 11){
        	 getActionBar().hide();
        	 
        	    mFragRight=  new VueComparisionFragment();

               /* getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
                final SlidingMenu sm = getSlidingMenu();
                sm.setMode(SlidingMenu.LEFT_RIGHT);
                sm.setSecondaryMenu(R.layout.menu_frame_two);
               getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.menu_frame_two, mFragRight)
                .commit();
               sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);*/
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
 
      @Override
      public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
          if (getSlidingMenu().isMenuShowing()) {
            if (!mFrag.listener.onBackPressed()) {
              getSlidingMenu().toggle();
            }
          } else {
            super.onBackPressed();
          }
        }
        return false;
      }
      
      /*@Override
      public boolean onCreateOptionsMenu(Menu menu) {
          getMenuInflater().inflate(R.menu.title_options, menu);
          // Configure the search info and add any event listeners
          return super.onCreateOptionsMenu(menu);
      }*/
}
