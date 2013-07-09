package com.lateralthoughts.vue;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import android.os.Bundle;
import android.view.KeyEvent;
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
	    getSupportMenuInflater().inflate(R.menu.title_options2, menu);
	    getSupportActionBar().setHomeButtonEnabled(true); 
	  
	    // Configure the search info and add any event listeners
	    return super.onCreateOptionsMenu(menu);
	  }

	 
	 @Override
	public boolean onOptionsItemSelected(MenuItem item) {
	// Handle item selection
	      switch (item.getItemId()) {
	        case android.R.id.home:
	          getSlidingMenu().toggle();
	          break;
	        case R.id.menu_cancel:
	          finish();
	      }
	return super.onOptionsItemSelected(item);
	}
}
