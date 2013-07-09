package com.lateralthoughts.vue;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class CreateAisleActivity extends BaseActivity{

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_aisle_main);
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
	 @Override
	  public boolean onCreateOptionsMenu(Menu menu) {
	    getMenuInflater().inflate(R.menu.title_options, menu);
	    ImageView icon = (ImageView) findViewById(android.R.id.home);
	    icon.setOnClickListener(new OnClickListener() {

	      @Override
	      public void onClick(View arg0) {
	        getSlidingMenu().toggle();
	      }
	    });
	  
	    // Configure the search info and add any event listeners
	    return super.onCreateOptionsMenu(menu);
	  }

}
