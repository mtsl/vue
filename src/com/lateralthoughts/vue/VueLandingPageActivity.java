package com.lateralthoughts.vue;

//generic android goodies
import java.io.IOException;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
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
import com.google.android.gms.plus.PlusClient.OnPeopleLoadedListener;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.plus.model.people.PersonBuffer;
import com.googleplus.MomentUtil;
import com.googleplus.PlusClientFragment;
import com.googleplus.PlusClientFragment.OnSignedInListener;

public class VueLandingPageActivity extends BaseActivity
		/* FragmentActivity */implements OnSignedInListener {

	private Dialog loginDialog;

	// Google+ integration
	public static final int REQUEST_CODE_PLUS_CLIENT_FRAGMENT = 0;
	public static PlusClientFragment mSignInFragment;
	public static PlusClient plusClient;
	private boolean googleplusloggedinDialogFlag = false;
	private String googlepluspackagename = "com.google.android.apps.plus";

	private SharedPreferences sharedPreferencesObj = null;

	private ImageView trendingbg;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.vue_landing_main);

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
			editor.putBoolean(VueConstants.FIRSTTIME_LOGIN_PREFRENCE_FLAG,
					false);
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
			/*
			 * if(isFriendsListVisible) { isFriendsListVisible = false;
			 */
			mFrag.listener.onBackPressed();
			// }
		}
		return false;
	}

	private void showLogInDialog(boolean hideCancelButton) {
		Session session = Session.getActiveSession();
		boolean showdilaog = (session != null && session.isOpened());

		if (!showdilaog) {
			renderDialogForSocailNetworkingIntegration(hideCancelButton);
			trendingbg.setVisibility(View.VISIBLE);
		}
	}

	private void renderDialogForSocailNetworkingIntegration(
			boolean hideCancelButton) {
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

		googleplusign_in_buttonlayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub

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
							public void call(Session session,
									SessionState state, Exception exception) {
								if (session.isOpened()) {
									
										SharedPreferences.Editor editor = sharedPreferencesObj.edit();
										editor.putString(VueConstants.FACEBOOK_ACCESSTOKEN, session.getAccessToken());
										editor.putString(VueConstants.VUELOGIN, VueConstants.FACEBOOK);
										editor.commit();
									
									
									// make request to the /me API
									Request.executeMeRequestAsync(session,
											new Request.GraphUserCallback() {

												// callback after Graph API
												// response with user object
												@Override
												public void onCompleted(
														GraphUser user,
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
			mSignInFragment.handleOnActivityResult(requestCode, resultCode,
					data);
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

		
		// To show Google+ App install dialog after login with Google+
		if (googleplusloggedinDialogFlag) {
			SharedPreferences.Editor editor = sharedPreferencesObj.edit();
			editor.putString(VueConstants.VUELOGIN,
					VueConstants.GOOGLEPLUS);
			editor.commit();
			
			Toast.makeText(this,
					plusClient.getAccountName() + " is connected.",
					Toast.LENGTH_LONG).show();
			boolean installed = appInstalledOrNot(googlepluspackagename);
			if (!installed)
				showAlertMessageForGoolgePlusAppInstalation();
		}

		if (loginDialog != null)
			loginDialog.dismiss();

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


	// The below code is used to get the Facebook friends information.
	/*final VueShare obj = new VueShare();
	Thread thread=new Thread(new Runnable() {
					
					@Override
					public void run() {			
								
					
				try {
					obj.getFacebookFriends(VueLandingPageActivity.this);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
					}
	});thread.start();*/
	
	/*// Code for getting Google+ friends list
	new VueShare().getGooglePlusFriends(VueLandingPageActivity.plusClient);*/

}