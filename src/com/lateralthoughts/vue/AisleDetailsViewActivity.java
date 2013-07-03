package com.lateralthoughts.vue;

//generic android goodies
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

public class AisleDetailsViewActivity extends BaseActivity/* FragmentActivity*/  {
	
    @SuppressLint("NewApi")
	@Override
    public void onCreate(Bundle icicle){
        super.onCreate(icicle);
        setContentView(R.layout.aisle_details_activity_landing);
 
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= 11){
        	 getActionBar().hide();
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
