package com.lateralthoughts.vue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken.FacebookLoginListener;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Request.Callback;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.plus.PlusClient;
import com.google.android.gms.plus.PlusClient.OnPeopleLoadedListener;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.plus.model.people.PersonBuffer;
import com.googleplus.MomentUtil;
import com.googleplus.PlusClientFragment;
import com.googleplus.PlusClientFragment.OnSignedInListener;
import com.lateralthoughts.vue.utils.FbGPlusDetails;
import com.lateralthoughts.vue.utils.Utils;
import com.lateralthoughts.vue.utils.clsShare;





public class VueLoginActivity extends FragmentActivity implements OnSignedInListener, OnPeopleLoadedListener{

	
	private boolean hide_cancelbtn = false;
	private boolean from_bezelmenu_login = false;
	private String from_invitefriends = null;
	private boolean from_details_fbshare = false;
	private boolean dontCallUserInfoChangesMethod = false;
	private boolean fromInvitefriendsgoogleplus = false;
	private boolean facebookflag = false;
	
	static SharedPreferences sharedPreferencesObj;
	
	  // Google+ integration
	  public static final int REQUEST_CODE_PLUS_CLIENT_FRAGMENT = 0;
	  public PlusClientFragment mSignInFragment;
	  
	  private boolean googleplusloggedinDialogFlag = false;
	  private String googlepluspackagename = "com.google.android.apps.plus";
	  
	  Activity context;
	 
	 LinearLayout socialintegrationmainlayotu;
	 
	 public static FacebookLoginCompletedListener mfblogincompletedListener;
	
	 Bundle bundle = null;
	 
	
		
		private final List<String> PERMISSIONS = Arrays.asList("publish_actions");
		
		ProgressDialog fbprogressdialog, gplusdialog;
		
		private final String PENDING_ACTION_BUNDLE_KEY = VueApplication.getInstance()
				.getString(R.string.pendingActionBundleKey);
		private PendingAction pendingAction = PendingAction.NONE;
		 enum PendingAction {
				NONE, POST_PHOTO, POST_STATUS_UPDATE
			}
		 private UiLifecycleHelper uiHelper;
			private Session.StatusCallback callback = new Session.StatusCallback() {

				public void call(Session session, SessionState state,
						Exception exception) {
					onSessionStateChange(session, state, exception);
				}
			};
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	setContentView(R.layout.socialnetworkingloginscreen);
	
	Log.e("fb","oncreate");
	
	// Below code for FB... By Krishna.V
	uiHelper = new UiLifecycleHelper(this, callback);
	uiHelper.onCreate(savedInstanceState);
	
	  RelativeLayout googleplusign_in_buttonlayout = (RelativeLayout) findViewById(R.id.googleplusign_in_buttonlayout);
	    RelativeLayout fblog_in_buttonlayout = (RelativeLayout)findViewById(R.id.fblog_in_buttonlayout);

	    LoginButton login_button = (LoginButton)findViewById(R.id.login_button);
	    
	    socialintegrationmainlayotu = (LinearLayout) findViewById(R.id.socialintegrationmainlayotu);
	    
	    RelativeLayout cancellayout = (RelativeLayout) findViewById(R.id.cancellayout);
	
		if (savedInstanceState != null) {/*
			String name = savedInstanceState
					.getString(PENDING_ACTION_BUNDLE_KEY);
			pendingAction = PendingAction.valueOf(name);

			Session session = Session.getActiveSession();

			Log.e("fb", "on saved instance state" + session);

			if (session != null) {
				Log.e("fb", "opened" + session.isOpened());

				socialintegrationmainlayotu.setVisibility(View.GONE);
				
				from_invitefriends = savedInstanceState.getString(VueConstants.FROM_INVITEFRIENDS);
				from_details_fbshare = savedInstanceState.getBoolean(VueConstants.FBLOGIN_FROM_DETAILS_SHARE);
				
				
				saveFBLoginDetails(session.getAccessToken());

				if (from_details_fbshare) {
					from_details_fbshare = false;
					shareToFacebook(null, "hi android");
				}
			}

		*/}
	
	
	context = this;
	
	   mfblogincompletedListener = new FacebookLoginCompletedListener();
	
	
	
    sharedPreferencesObj = this.getSharedPreferences(
	        VueConstants.SHAREDPREFERENCE_NAME, 0);
    
    fbprogressdialog = ProgressDialog.show(
			context, "Facebook", "Sharing....",
			true);
    
    gplusdialog = ProgressDialog.show(
			context, "Google+", "Loading....",
			true);
    gplusdialog.dismiss();
    

	fbprogressdialog.dismiss();
    
	
	bundle = getIntent().getExtras();
	if(bundle != null)
	{
		hide_cancelbtn = bundle.getBoolean(VueConstants.CANCEL_BTN_DISABLE_FLAG);
		from_bezelmenu_login = bundle.getBoolean(VueConstants.FROM_BEZELMENU_LOGIN);
		from_invitefriends = bundle.getString(VueConstants.FROM_INVITEFRIENDS);
		from_details_fbshare = bundle.getBoolean(VueConstants.FBLOGIN_FROM_DETAILS_SHARE);
	}
	
  
	
	if(from_details_fbshare)
	{
		socialintegrationmainlayotu.setVisibility(View.GONE);
		
		ImageView trendingbg = (ImageView) findViewById(R.id.trendingbg);
		
		trendingbg.setVisibility(View.GONE);
	  
	  boolean facebookloginflag = sharedPreferencesObj.getBoolean(
		        VueConstants.FACEBOOK_LOGIN, false);
	  
	  if(facebookloginflag)
	  {
		  
		  ArrayList<clsShare> filePathList = bundle.getParcelableArrayList(VueConstants.FBPOST_IMAGEURLS);
		  
		  shareToFacebook(filePathList, bundle.getString(VueConstants.FBPOST_TEXT));
	  }
	  else
	  {
		  socialintegrationmainlayotu.setVisibility(View.VISIBLE);
			googleplusign_in_buttonlayout.setVisibility(View.GONE);
			fblog_in_buttonlayout.setVisibility(View.INVISIBLE);
			cancellayout.setVisibility(View.GONE);
			login_button.performClick();
	  }
		
	}
	else
	{
		 mSignInFragment = PlusClientFragment.getPlusClientFragment(
			        VueLoginActivity.this, MomentUtil.VISIBLE_ACTIVITIES);
			    

	 

	    if (hide_cancelbtn) {
	      cancellayout.setVisibility(View.GONE);
	   }
	    

	 
		boolean fbloginfalg = sharedPreferencesObj.getBoolean(
		          VueConstants.FACEBOOK_LOGIN, false);
		boolean googleplusloginfalg = sharedPreferencesObj.getBoolean(
		          VueConstants.GOOGLEPLUS_LOGIN, false);
	    
	    if(from_invitefriends != null)
	    {
	    	if(from_invitefriends.equals(VueConstants.FACEBOOK))
	    	{
	    		googleplusign_in_buttonlayout.setVisibility(View.GONE);
	    		facebookflag = true;
	    	}
	    	if(from_invitefriends.equals(VueConstants.GOOGLEPLUS))
	    	{
	    		dontCallUserInfoChangesMethod = true;
	    		fromInvitefriendsgoogleplus = true;
	    		fblog_in_buttonlayout.setVisibility(View.GONE);
	    	}
	    }
	    else if(from_bezelmenu_login)
	    {
	    	if(fbloginfalg) 
	    		{
	    		dontCallUserInfoChangesMethod = true; 
	    		fblog_in_buttonlayout.setVisibility(View.GONE);
	    		}
	    	else if(googleplusloginfalg)
	    		{
	    		facebookflag = true;
	    		googleplusign_in_buttonlayout.setVisibility(View.GONE);
	    		}
	    
	    }
	    	

	    googleplusign_in_buttonlayout.setOnClickListener(new OnClickListener() {

	      @Override
	      public void onClick(View arg0) {
	    	  
	    	  socialintegrationmainlayotu.setVisibility(View.GONE);

	       googleplusloggedinDialogFlag = true;

	        mSignInFragment.signIn(REQUEST_CODE_PLUS_CLIENT_FRAGMENT);

	      }
	    });
	    
	    login_button.setPublishPermissions(PERMISSIONS);
	    login_button
				.setUserInfoChangedCallback(new LoginButton.UserInfoChangedCallback() {

					public void onUserInfoFetched(GraphUser user) {

						Log.e("fb","user info feched");
					if(!dontCallUserInfoChangesMethod)	updateUI();

					}
				});
	    

	
	    cancellayout.setOnClickListener(new OnClickListener() {

	      @Override
	      public void onClick(View arg0) {

	    	  finish();

	      }
	    });
	}

	
  
  
    	 
	
	
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		uiHelper.onDestroy();
	}

	
	@Override
	public void onPause() {
		super.onPause();
		uiHelper.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		uiHelper.onResume();
		Log.e("fb","onresume");
		//updateUI();
	}
	private void onSessionStateChange(Session session, SessionState state,
			Exception exception) {
		Log.e("onSessionStateChange", "sexxion: " + session + " state:" + state
				+ " exception: " + exception);
		if (pendingAction != PendingAction.NONE
				&& (exception instanceof FacebookOperationCanceledException || exception instanceof FacebookAuthorizationException)) {
			new AlertDialog.Builder(VueLoginActivity.this)
					.setTitle(R.string.cancelled)
					.setMessage(R.string.permission_not_granted)
					.setPositiveButton(R.string.ok, null).show();
			pendingAction = PendingAction.NONE;
		} else if (state == SessionState.OPENED_TOKEN_UPDATED) {
			handlePendingAction(null, null);
		}
		//updateUI();
	}
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		uiHelper.onSaveInstanceState(outState);
		outState.putString(PENDING_ACTION_BUNDLE_KEY, pendingAction.name());
		outState.putString(VueConstants.FROM_INVITEFRIENDS, from_invitefriends);
		outState.putBoolean(VueConstants.FBLOGIN_FROM_DETAILS_SHARE, from_details_fbshare);
	}
	
	 @Override
	  public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    Log.e("fb","onactivityresult");
	    try {
	    	uiHelper.onActivityResult(requestCode, resultCode, data);
	    	updateUI();
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

		if(fromInvitefriendsgoogleplus) gplusdialog.show();
		 
	    // VueLandingPageActivity.plusClient is used to share to Google+ from
	    // Details or other screens.
	    VueLandingPageActivity.plusClient = plusClient;
	    
	    


	    SharedPreferences.Editor editor = sharedPreferencesObj.edit();
	    editor.putBoolean(VueConstants.VUE_LOGIN, true);
	    editor.putBoolean(VueConstants.GOOGLEPLUS_LOGIN, true);
	    editor.commit();

	    Toast.makeText(this, plusClient.getAccountName() + " is connected.",
	        Toast.LENGTH_LONG).show();

	    // To show Google+ App install dialog after login with Google+
	    if (googleplusloggedinDialogFlag) {
	    
	      boolean installed = appInstalledOrNot(googlepluspackagename);
	      if (!installed)
	    	  {
	    	  if(gplusdialog != null && gplusdialog.isShowing()) gplusdialog.dismiss();
	    	  showAlertMessageForGoolgePlusAppInstalation();
	    	  }
	    }
	    
	    VueLandingPageActivity.plusClient.loadPeople(this, Person.Collection.VISIBLE);

	
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
	 
	@Override
	public void onPeopleLoaded(ConnectionResult status,
			PersonBuffer personBuffer, String nextPageToken) {
		


		Log.e("VueShare", "google friends called on people loaded");
		
		if (ConnectionResult.SUCCESS == status.getErrorCode()) {
			Log.e("VueShare", "google friends called sucess");
			if (personBuffer != null && personBuffer.getCount() > 0) {
				Log.e("VueShare", "google friends called count greater then 0");
				VueLandingPageActivity.googlePlusFriendsDetailsList = new ArrayList<FbGPlusDetails>();
				for (Person p : personBuffer) {
					Log.e("VueShare", "google friends called person bug");
					FbGPlusDetails googlePlusFriendsDetailsObj = new FbGPlusDetails(
							p.getDisplayName(), p.getImage().getUrl());

					VueLandingPageActivity.googlePlusFriendsDetailsList
							.add(googlePlusFriendsDetailsObj);

				}
				 if (from_invitefriends != null && from_invitefriends.equals(VueConstants.GOOGLEPLUS)) {
				  BaseActivity.mFrag.getFriendsList(getResources().getString(
	           	  R.string.sidemenu_sub_option_Gmail));
	             }
			}
		}
		 if(gplusdialog != null && gplusdialog.isShowing()) gplusdialog.dismiss();
		if(!facebookflag)finish();

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
	    
	    
	    gplusdialog.setOnDismissListener(new OnDismissListener() {
			
			@Override
			public void onDismiss(DialogInterface arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});

	  }
	
	
	public void saveFBLoginDetails(String accessToken)
	{
		sharedPreferencesObj = this.getSharedPreferences(
    	        VueConstants.SHAREDPREFERENCE_NAME, 0);
		 SharedPreferences.Editor editor = sharedPreferencesObj.edit();
         editor.putString(VueConstants.FACEBOOK_ACCESSTOKEN,
         		accessToken);
         editor
             .putBoolean(VueConstants.VUE_LOGIN, true);
         editor
         .putBoolean(VueConstants.FACEBOOK_LOGIN, true);
         editor.commit();
         if (from_invitefriends != null && from_invitefriends.equals(VueConstants.FACEBOOK)) {
        	 from_invitefriends = null;
       	  try {
   			BaseActivity.mFrag.getFriendsList(context.getResources().getString(
   			  R.string.sidemenu_sub_option_Facebook));
   		} catch (Exception e) {
   			e.printStackTrace();
   		}
         }
         
       if(!from_details_fbshare)  finish();
	}
	
	  public class FacebookLoginCompletedListener implements FacebookLoginListener{

		@Override
		public void onLoginCompleted(String acessToken) {
			// TODO Auto-generated method stub
			
			saveFBLoginDetails(acessToken);
		}
	      
	    }
	  private void updateUI() {
		  
		  Session session = Session.getActiveSession();
			boolean fbloggedin = (session != null && session.isOpened());

			if (fbloggedin) {
				saveFBLoginDetails(session.getAccessToken());
			
			if(from_details_fbshare)
				{
				from_details_fbshare = false;
				shareToFacebook(null, "hi android");
				}
			}
			
			
		}
	  
	
	  
	  public void shareToFacebook(ArrayList<clsShare> fileList, String articledesc) {
			Log.e("share", "f1");
			performPublish(PendingAction.POST_PHOTO, fileList, articledesc);
		}
		
		void performPublish(PendingAction action, ArrayList<clsShare> fileList,
				String articledesc) {
			Log.e("share", "f2");
			Session session = Session.getActiveSession();
			if (session != null) {
				if (hasPublishPermission()) {
					Log.e("share", "f4");
					// We can do the action right away.
					handlePendingAction(fileList, articledesc);
				} else {
					Log.e("share", "f5");
					// We need to get new permissions, then complete the action when
					// we get called back.
					session.requestNewPublishPermissions(new Session.NewPermissionsRequest(
							this, PERMISSIONS));
				}
			}
		}
		
		boolean hasPublishPermission() {
			Log.e("share", "f3");
			Session session = Session.getActiveSession();
			return session != null
					&& session.getPermissions().contains("publish_actions");
		}
		
		void handlePendingAction( ArrayList<clsShare> fileList, String articledesc) {
			Log.e("share", "f6");
				postPhoto(fileList, articledesc);
		}

		void postPhoto(final ArrayList<clsShare> fileList, final String articledesc) {
			Log.e("share", "fw");
			
			if (hasPublishPermission()) {
				fbprogressdialog.show();

				// Post photo....
				if (fileList != null) {

					
					  Thread t = new Thread(new Runnable() {

			                @Override
			                public void run() {
			                	for (int i = 0; i < fileList.size(); i++) {
									File f = new File(fileList.get(i).getFilepath());
									if(!f.exists()) downloadImage(fileList.get(i).getImageUrl(), f);
									final int index = i;
									VueLoginActivity.this.runOnUiThread(new Runnable() {

					                      @Override
					                      public void run() {
					                    	  
					                    	  
					                    		
					      					Bundle parameters = new Bundle(1);
					      				//	parameters.putString("picture",  imagePathArray.get(0).getImageUrl());
					      					parameters.putString("message", articledesc + "");
					      					ParcelFileDescriptor descriptor = null;
					      					try {
					      						descriptor = ParcelFileDescriptor.open(new File(fileList.get(index).getFilepath()), ParcelFileDescriptor.MODE_READ_ONLY);
					      					} catch (FileNotFoundException e) {
					      						// TODO Auto-generated catch block
					      						e.printStackTrace();
					      					}
					      					parameters.putParcelable("picture", descriptor);
					      					
					      					Callback callback = new Request.Callback() {

					      						public void onCompleted(Response response) {
					      							fbprogressdialog.dismiss();
					      							showPublishResult(
					      									VueLoginActivity.this
					      											.getString(R.string.photo_post),
					      									response.getGraphObject(), response.getError());
					      						}
					      					};

					      					Request request = new Request(Session.getActiveSession(),
					      							"me/photos", parameters, HttpMethod.POST, callback);

					      					request.executeAsync();
					      				
					      					/*Request request6 = null;
					      					try {
					      						request6 = Request.newUploadPhotoRequest(
					      								Session.getActiveSession(),
					      						        imagePathArray.get(0).getFile(), callback);
					      					} catch (FileNotFoundException e) {
					      						// TODO Auto-generated catch block
					      						e.printStackTrace();
					      					}
					      				    RequestAsyncTask task6 = new RequestAsyncTask(request6);
					      				    task6.execute();*/
					      				
					      				
					                      }
					                    });
									
								}
			                } 
		                });t.start();
					
					
					
					
				
				}
			 else {
					fbprogressdialog.dismiss();
				}

			}
		}
		
		
		private void downloadImage(String url, File f)
		{
		      try {
				URL imageUrl = new URL(url);
				  HttpURLConnection conn = (HttpURLConnection)imageUrl.openConnection();
				  conn.setConnectTimeout(30000);
				  conn.setReadTimeout(30000);
				  conn.setInstanceFollowRedirects(true);
				  InputStream is=conn.getInputStream();
				  OutputStream os = new FileOutputStream(f);
				  Utils.CopyStream(is, os);
				  os.close();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		private void showPublishResult(String message, GraphObject result,
				FacebookRequestError error) {
			// String title = null;
			String alertMessage = null;
			if (error == null) {
				// title = activity.getString(R.string.success);
				alertMessage = VueLoginActivity.this
						.getString(R.string.successfully_posted_post);
			} else {

				if (error.getErrorCode() == 506) {
					// title = activity.getString(R.string.alerttitle);
					alertMessage = VueLoginActivity.this
							.getString(R.string.duplicatepostmesg);
				} else if (error.getErrorCode() == 368) {
					// title = activity.getString(R.string.fbwarningmesgtitle);
					alertMessage = VueLoginActivity.this
							.getString(R.string.facebookwarningmesg);
				} else {
					// title = activity.getString(R.string.error);
					alertMessage = error.getErrorMessage();
				}
			}
			final Dialog dialog = new Dialog(VueLoginActivity.this, R.style.Theme_Dialog_Translucent);
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.setContentView(R.layout.networkdialogue);
			TextView messagetext = (TextView) dialog.findViewById(R.id.messagetext);
			TextView okbutton = (TextView) dialog.findViewById(R.id.okbutton);

			okbutton.setVisibility(View.GONE);

			View networkdialogline = dialog.findViewById(R.id.networkdialogline);
			networkdialogline.setVisibility(View.GONE);

			TextView nobutton = (TextView) dialog.findViewById(R.id.nobutton);
			
			nobutton.setText("ok");
			messagetext.setText(alertMessage);

			nobutton.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					dialog.dismiss();
				}
			});
			dialog.setOnCancelListener(new OnCancelListener() {

				public void onCancel(DialogInterface dialog) {

				}
			});
			dialog.show();
			
			dialog.setOnDismissListener(new OnDismissListener() {
				
				@Override
				public void onDismiss(DialogInterface arg0) {
					// TODO Auto-generated method stub
					finish();
				}
			});

		}
		
		

}
