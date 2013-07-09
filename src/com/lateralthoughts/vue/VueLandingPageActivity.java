package com.lateralthoughts.vue;

import java.util.List;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.google.android.gms.plus.PlusClient;
import com.lateralthoughts.vue.utils.ExceptionHandler;
import com.lateralthoughts.vue.utils.FbGPlusDetails;

public class VueLandingPageActivity extends BaseActivity {

 
 
	SharedPreferences sharedPreferencesObj;
  

	 public static List<FbGPlusDetails> googlePlusFriendsDetailsList = null;

  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);

    Thread.setDefaultUncaughtExceptionHandler(new
    		ExceptionHandler(this));

    setContentView(R.layout.vue_landing_main);

    // Checking wheather app is opens for first time or not?
    sharedPreferencesObj = this.getSharedPreferences(
        VueConstants.SHAREDPREFERENCE_NAME, 0);
    boolean isFirstTime = sharedPreferencesObj.getBoolean(
        VueConstants.FIRSTTIME_LOGIN_PREFRENCE_FLAG, true);

    // Application opens first time.
    if (isFirstTime) {

      SharedPreferences.Editor editor = sharedPreferencesObj.edit();
      editor.putBoolean(VueConstants.FIRSTTIME_LOGIN_PREFRENCE_FLAG, false);
      editor.commit();

      showLogInDialog(false);
    }
    // Check the CreatedAisleCount and Comments count
    else {
      int createdaislecount = sharedPreferencesObj.getInt(
          VueConstants.CREATED_AISLE_COUNT_IN_PREFERENCE, 0);
      int commentscount = sharedPreferencesObj.getInt(
          VueConstants.COMMENTS_COUNT_IN_PREFERENCES, 0);

      if (createdaislecount == VueConstants.CREATE_AISLE_LIMIT_FOR_LOGIN
          || commentscount == VueConstants.COMMENTS_LIMIT_FOR_LOGIN) {
        showLogInDialog(true);
      }

    }

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

/*  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
      // Handle item selection
      switch (item.getItemId()) {
      case R.id.menu_create_aisles:
         Intent intent = new Intent(VueLandingPageActivity.this, CreateAisleActivity.class);
         startActivity(intent);
          return true;
      default:
          return super.onOptionsItemSelected(item);
      }
  }*/
  
  
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

  public void showLogInDialog(boolean hideCancelButton) {
	  
	  Intent i = new Intent(this, VueLoginActivity.class);
	  Bundle b = new Bundle();
	  b.putBoolean(VueConstants.CANCEL_BTN_DISABLE_FLAG, hideCancelButton);
	  b.putString(VueConstants.FROM_INVITEFRIENDS, null);
	  b.putBoolean(VueConstants.FBLOGIN_FROM_DETAILS_SHARE, false);
	  b.putBoolean(VueConstants.FROM_BEZELMENU_LOGIN, false);
	  i.putExtras(b);
	  startActivity(i);
  }


  @Override
  public void onResume() {
    super.onResume();

  }

  @Override
  public void onPause() {
    super.onPause();

  }

 

}