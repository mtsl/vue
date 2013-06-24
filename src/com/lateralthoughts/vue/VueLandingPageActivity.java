package com.lateralthoughts.vue;

//generic android goodies
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

/*import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;*/

public class VueLandingPageActivity extends BaseActivity/*FragmentActivity*/ {
	
	//public static boolean isFriendsListVisible = false;
	
	@Override
	public void onCreate(Bundle icicle){
		super.onCreate(icicle);
		setContentView(R.layout.vue_landing_main);
	    // start Facebook Login
/*	    Session.openActiveSession(this, true, new Session.StatusCallback() {

	      // callback when session changes state
	      @Override
	      public void call(Session session, SessionState state, Exception exception) {
	        if (session.isOpened()) {

	          // make request to the /me API
	          Request.executeMeRequestAsync(session, new Request.GraphUserCallback() {

	            // callback after Graph API response with user object
	            @Override
	            public void onCompleted(GraphUser user, Response response) {
	              if (user != null) {
	              }
	            }
	          });
	        }
	      }
	    });*/
	    
        Intent i = new Intent();
        i.setClass(this, SplashScreen.class);
        startActivityForResult(i, 0);
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
	     // Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
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
	  
	  @Override
		public boolean onKeyUp(int keyCode, KeyEvent event) {
		    if(keyCode == KeyEvent.KEYCODE_BACK) {
		    	/*if(isFriendsListVisible) {
		    		isFriendsListVisible = false;*/
		    		mFrag.listener.onBackPressed();
		    	//}
		    }
			return false;
		}
}
