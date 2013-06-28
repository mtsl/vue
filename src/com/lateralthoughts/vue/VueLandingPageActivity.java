package com.lateralthoughts.vue;

//generic android goodies
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.plus.PlusClient;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.plus.model.people.PersonBuffer;
import com.googleplus.MomentUtil;
import com.googleplus.PlusClientFragment;
import com.googleplus.PlusClientFragment.OnSignedInListener;
 
import com.lateralthoughts.vue.utils.FbGPlusDetails;

public class VueLandingPageActivity extends BaseActivity
/* FragmentActivity */implements OnSignedInListener, PlusClient.OnPeopleLoadedListener {

  private Dialog loginDialog;

  // Google+ integration
  public static final int REQUEST_CODE_PLUS_CLIENT_FRAGMENT = 0;
  public static PlusClientFragment mSignInFragment;
  public static PlusClient plusClient;
  private boolean googleplusloggedinDialogFlag = false;
  private String googlepluspackagename = "com.google.android.apps.plus";

  private SharedPreferences sharedPreferencesObj = null;

  private ImageView trendingbg;
  
  public List<FbGPlusDetails> googlePlusFriendsDetailsList = null;
  
  public static Activity mainActivityContext;
  
  public String mFrom = null;
  

  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    
  /*  Thread.setDefaultUncaughtExceptionHandler(new
    		ExceptionHandler(this));*/
    
    
    setContentView(R.layout.vue_landing_main);

    
    mainActivityContext = this;
    
    trendingbg = (ImageView) findViewById(R.id.trendingbg);
    trendingbg.setVisibility(View.GONE);

    mSignInFragment = PlusClientFragment.getPlusClientFragment(
        VueLandingPageActivity.this, MomentUtil.VISIBLE_ACTIVITIES);


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

      showLogInDialog(false, null);

    }
    // Check the CreatedAisleCount and Comments count
    else {
      int createdaislecount = sharedPreferencesObj.getInt(
          VueConstants.CREATED_AISLE_COUNT_IN_PREFERENCE, 0);
      int commentscount = sharedPreferencesObj.getInt(
          VueConstants.COMMENTS_COUNT_IN_PREFERENCES, 0);

      if (createdaislecount == VueConstants.CREATE_AISLE_LIMIT_FOR_LOGIN
          || commentscount == VueConstants.COMMENTS_LIMIT_FOR_LOGIN) {
        showLogInDialog(true, null);
      }

    }

    // No need to show splash screen
    /*
     * Intent i = new Intent(); i.setClass(this, SplashScreen.class);
     * startActivity(i);
     */
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

  public void showLogInDialog(boolean hideCancelButton, String from) {
   
    renderDialogForSocailNetworkingIntegration(hideCancelButton, from);
      trendingbg.setVisibility(View.VISIBLE);
   
  }

  private void renderDialogForSocailNetworkingIntegration(
      boolean hideCancelButton, final String from) {
    // Select Image Dialog...
    loginDialog = new Dialog(this, R.style.Theme_Dialog_Translucent);
    loginDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    loginDialog.setContentView(R.layout.socialnetworkingloginscreen);

    RelativeLayout googleplusign_in_buttonlayout = (RelativeLayout) loginDialog
        .findViewById(R.id.googleplusign_in_buttonlayout);

    RelativeLayout fblog_in_buttonlayout = (RelativeLayout) loginDialog
        .findViewById(R.id.fblog_in_buttonlayout);

    RelativeLayout cancellayout = (RelativeLayout) loginDialog
        .findViewById(R.id.cancellayout);

    if (hideCancelButton) {
      cancellayout.setVisibility(View.GONE);
    }
    
    
    if(from != null)
    {
    	if(from.equals(VueConstants.FACEBOOK))
    	{
    		googleplusign_in_buttonlayout.setVisibility(View.GONE);
    	}
    	if(from.equals(VueConstants.GOOGLEPLUS))
    	{
    		fblog_in_buttonlayout.setVisibility(View.GONE);
    	}
    }

    googleplusign_in_buttonlayout.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View arg0) {
        // TODO Auto-generated method stub
    	  
    	  mFrom = null;
    	  
    	  mFrom = from;

        googleplusloggedinDialogFlag = true;

        mSignInFragment.signIn(REQUEST_CODE_PLUS_CLIENT_FRAGMENT);

      }
    });

    fblog_in_buttonlayout.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View arg0) {
        // TODO Auto-generated method stub

        loginDialog.dismiss();

        // start Facebook Login
        Session.openActiveSession(VueLandingPageActivity.this, true,
            new Session.StatusCallback() {

              // callback when session changes state
              @Override
              public void call(Session session, SessionState state,
                  Exception exception) {
                if (session.isOpened()) {

                  SharedPreferences.Editor editor = sharedPreferencesObj.edit();
                  editor.putString(VueConstants.FACEBOOK_ACCESSTOKEN,
                      session.getAccessToken());
                  editor
                      .putString(VueConstants.VUELOGIN, VueConstants.FACEBOOK);
                  editor.commit();
                  if (from != null && from.equals(VueConstants.FACEBOOK)) {
                	  mFrag.getFriendsList(getResources().getString(
                	  R.string.sidemenu_sub_option_Facebook));
                  }

                  // make request to the /me API
                  Request.executeMeRequestAsync(session,
                      new Request.GraphUserCallback() {

                        // callback after Graph API
                        // response with user object
                        @Override
                        public void onCompleted(GraphUser user,
                            Response response) {
                          if (user != null) {
                          }
                        }
                      });
                }
              }
            });
      }
    });

    cancellayout.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View arg0) {
        // TODO Auto-generated method stub

        loginDialog.dismiss();

      }
    });
    loginDialog.show();

    loginDialog.setOnDismissListener(new OnDismissListener() {

      @Override
      public void onDismiss(DialogInterface arg0) {
        trendingbg.setVisibility(View.GONE);
      }
    });

    loginDialog.setCancelable(false);
  }

  private void showAlertMessageForGoolgePlusAppInstalation() {

    final Dialog gplusdialog = new Dialog(this,
        R.style.Theme_Dialog_Translucent);
    gplusdialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    gplusdialog.setContentView(R.layout.googleplusappinstallationdialog);
    TextView noButton = (TextView) gplusdialog.findViewById(R.id.nobutton);
    TextView okButton = (TextView) gplusdialog.findViewById(R.id.okbutton);
    okButton.setOnClickListener(new OnClickListener() {

      public void onClick(View v) {
        gplusdialog.dismiss();

        Intent goToMarket = new Intent(Intent.ACTION_VIEW).setData(Uri
            .parse("market://details?id=" + googlepluspackagename));
        startActivity(goToMarket);
      }
    });
    noButton.setOnClickListener(new OnClickListener() {

      public void onClick(View v) {
        gplusdialog.dismiss();
      }
    });

    gplusdialog.show();

  }

  @Override
  public void onResume() {
    super.onResume();
  }

  @Override
  public void onPause() {
    super.onPause();

  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    try {
      Session.getActiveSession().onActivityResult(this, requestCode,
          resultCode, data);

    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    try {
      mSignInFragment.handleOnActivityResult(requestCode, resultCode, data);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  @Override
  public void onSignedIn(PlusClient plusClient) {
    // TODO Auto-generated method stub

    // VueLandingPageActivity.plusClient is used to share to Google+ from
    // Details or other screens.
    VueLandingPageActivity.plusClient = plusClient;
    
    VueLandingPageActivity.plusClient.loadPeople(this, Person.Collection.VISIBLE);


    SharedPreferences.Editor editor = sharedPreferencesObj.edit();
    editor.putString(VueConstants.VUELOGIN, VueConstants.GOOGLEPLUS);
    editor.commit();

    Toast.makeText(this, plusClient.getAccountName() + " is connected.",
        Toast.LENGTH_LONG).show();

    // To show Google+ App install dialog after login with Google+
    if (googleplusloggedinDialogFlag) {
    
      boolean installed = appInstalledOrNot(googlepluspackagename);
      if (!installed) showAlertMessageForGoolgePlusAppInstalation();
    }

    if (loginDialog != null) loginDialog.dismiss();

    loginDialog = null;
  }

  private boolean appInstalledOrNot(String uri) {
    PackageManager pm = getPackageManager();
    boolean app_installed = false;
    try {
      pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
      app_installed = true;
    } catch (PackageManager.NameNotFoundException e) {
      app_installed = false;
    }
    return app_installed;
  }
  // Test code for sharing image and text to google+
  // VueLandingPageActivity.mSignInFragment.share(VueLandingPageActivity.plusClient,
  // getActivity(),
  // "This is krishna posted from Android Test app from his mobile.","/sdcard/hi.jpg");

@Override
public void onPeopleLoaded(ConnectionResult status,
		PersonBuffer personBuffer, String nextPageToken) {
	


	Log.e("VueShare", "google friends called on people loaded");
	
	if (ConnectionResult.SUCCESS == status.getErrorCode()) {
		Log.e("VueShare", "google friends called sucess");
		if (personBuffer != null && personBuffer.getCount() > 0) {
			Log.e("VueShare", "google friends called count greater then 0");
			googlePlusFriendsDetailsList = new ArrayList<FbGPlusDetails>();
			for (Person p : personBuffer) {
				Log.e("VueShare", "google friends called person bug");
				FbGPlusDetails googlePlusFriendsDetailsObj = new FbGPlusDetails(
						p.getDisplayName(), p.getImage().getUrl());

				googlePlusFriendsDetailsList
						.add(googlePlusFriendsDetailsObj);

			}
			 if (mFrom != null && mFrom.equals(VueConstants.GOOGLEPLUS)) {
				 mFrom = null;
           	  mFrag.getFriendsList(getResources().getString(
           	  R.string.sidemenu_sub_option_Gmail));
             }
		}
	}

}


  // The below code is used to get the Facebook friends information.
  /*
   * final VueShare obj = new VueShare(); Thread thread=new Thread(new
   * Runnable() {
   * 
   * @Override public void run() {
   * 
   * 
   * try { obj.getFacebookFriends(VueLandingPageActivity.this); } catch
   * (Exception e) { // TODO Auto-generated catch block e.printStackTrace(); } }
   * });thread.start();
   */

  /*
   * // Code for getting Google+ friends list new
   * VueShare().getGooglePlusFriends(VueLandingPageActivity.plusClient);
   */

}