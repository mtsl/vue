package com.lateralthoughts.vue;

import java.util.List;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.lateralthoughts.vue.utils.ExceptionHandler;
import com.lateralthoughts.vue.utils.FbGPlusDetails;

public class VueLandingPageActivity extends BaseActivity {

 
 
	SharedPreferences sharedPreferencesObj;
    private static final int DELAY_TIME = 500;

	 public static List<FbGPlusDetails> googlePlusFriendsDetailsList = null;

  @Override
  public void onCreate(Bundle icicle) {
    Log.e("Profiling", "Profiling VueLandingPageActivity.onCreate() Start Before super call");
    super.onCreate(icicle);
    Log.e("Profiling", "Profiling VueLandingPageActivity.onCreate() After super call");
    Thread.setDefaultUncaughtExceptionHandler(new
    		ExceptionHandler(this));
    Log.e("Profiling", "Profiling VueLandingPageActivity.onCreate() Before setContentView(R.layout.vue_landing_main) call");
    setContentView(R.layout.vue_landing_main);
    Log.e("Profiling", "Profiling VueLandingPageActivity.onCreate() After setContentView(R.layout.vue_landing_main) call");
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
    Log.e("Profiling", "Profiling VueLandingPageActivity.onCreate() End");
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    Log.e("Profiling", "Profiling VueLandingPageActivity.onCreateOptionsMenu() Start");
    getSupportMenuInflater().inflate(R.menu.title_options, menu);
    getSupportActionBar().setHomeButtonEnabled(true);
    Log.e("Profiling", "Profiling VueLandingPageActivity.onCreateOptionsMenu() End");
    // Configure the search info and add any event listeners
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
      // Handle item selection
      switch (item.getItemId()) {
      case R.id.menu_create_aisles:
         Intent intent = new Intent(VueLandingPageActivity.this, CreateAisleSelectionActivity.class);
         startActivity(intent);
          return true;
      case android.R.id.home:
        getSlidingMenu().toggle();
        return true;
      default:
          return super.onOptionsItemSelected(item);
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
    
    new Handler().postDelayed(new Runnable() {
		
		@Override
		public void run() {
		   	 Rect rect= new Rect();
		   	 Window window=  VueLandingPageActivity.this.getWindow();
		   	 window.getDecorView().getWindowVisibleDisplayFrame(rect);
		   	 int statusBarHeight= rect.top;
	/*	   	 int contentViewTop= 
		   	     window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
		   	 int titleBarHeight= contentViewTop - statusBarHeight;*/
		    VueApplication.getInstance().setmStatusBarHeight(statusBarHeight);
			
		}
	}, DELAY_TIME);
  }

  @Override
  public void onPause() {
    super.onPause();

  }

 

}