package com.lateralthoughts.vue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Request.Callback;
import com.facebook.Request.GraphUserCallback;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
import com.flurry.android.FlurryAgent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.plus.PlusClient;
import com.google.android.gms.plus.PlusClient.OnPeopleLoadedListener;
import com.google.android.gms.plus.PlusClient.OnPersonLoadedListener;
import com.google.android.gms.plus.PlusShare;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.plus.model.people.PersonBuffer;
import com.googleplus.MomentUtil;
import com.googleplus.PlusClientFragment;
import com.googleplus.PlusClientFragment.OnSignedInListener;
import com.instagram.InstagramApp;
import com.instagram.InstagramApp.OAuthAuthenticationListener;
import com.lateralthoughts.vue.VueUserManager.UserUpdateCallback;
import com.lateralthoughts.vue.connectivity.VueConnectivityManager;
import com.lateralthoughts.vue.domain.Aisle;
import com.lateralthoughts.vue.utils.FbGPlusDetails;
import com.lateralthoughts.vue.utils.SortBasedOnName;
import com.lateralthoughts.vue.utils.Utils;
import com.lateralthoughts.vue.utils.clsShare;

public class VueLoginActivity extends FragmentActivity implements
    OnSignedInListener, OnPeopleLoadedListener, OnPersonLoadedListener {

  private boolean mHideCancelButton = false;
  private boolean mFromBezelMenuLogin = false;
  private String mFromInviteFriends = null;
  private boolean mFromDetailsFbShare = false;
  private boolean mDontCallUserInfoChangesMethod = false;
  private boolean mFromGoogleplusInvitefriends = false;
  private boolean mFacebookFlag = false;
  private String mFbFriendId = null;
  private String mFbFriendName = null;
  private boolean mGoogleplusFriendInvite = false;
  private boolean mGoogleplusAutomaticLogin = false;
  private SharedPreferences mSharedPreferencesObj;
  private ImageView mTrendingbg = null;
  private static final String LABEL_VIEW_ITEM = "VIEW_ITEM";
  private static final int REQUEST_CODE_PLUS_CLIENT_FRAGMENT = 0;
  private PlusClientFragment mSignInFragment;
  private boolean mGoogleplusLoggedinDialogFlag = false;
  private static final int REQUEST_CODE_INTERACTIVE_POST = 3;
  private Activity mContext;
  private LinearLayout mSocialIntegrationMainLayout;
  private Bundle mBundle = null;
  private static final String TAG = "VueLoginActivity";
  private final List<String> PUBLISH_PERMISSIONS = Arrays
      .asList("publish_actions");
  private final List<String> READ_PERMISSIONS = Arrays.asList("email",
      "user_birthday");
  private ProgressDialog mFacebookProgressDialog, mGooglePlusProgressDialog;
  private final String PENDING_ACTION_BUNDLE_KEY = VueApplication.getInstance()
      .getString(R.string.pendingActionBundleKey);
  private PendingAction mPendingAction = PendingAction.NONE;

  private enum PendingAction {
    NONE, POST_PHOTO, POST_STATUS_UPDATE
  }

  private UiLifecycleHelper mUiHelper;
  private Session.StatusCallback mCallback = new Session.StatusCallback() {
    public void call(Session session, SessionState state, Exception exception) {
      onSessionStateChange(session, state, exception);
    }
  };
  private InstagramApp mInstagramApp = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.socialnetworkingloginscreen);
    mUiHelper = new UiLifecycleHelper(this, mCallback);
    mUiHelper.onCreate(savedInstanceState);
    mTrendingbg = (ImageView) findViewById(R.id.trendingbg);
    try {
      mTrendingbg.setBackgroundResource(R.drawable.trendingbg);
    } catch (Throwable e) {
      e.printStackTrace();
    }
    RelativeLayout googleplusign_in_buttonlayout = (RelativeLayout) findViewById(R.id.googleplusign_in_buttonlayout);
    RelativeLayout instagramSignInButtonLayout = (RelativeLayout) findViewById(R.id.instagramsign_in_buttonlayout);
    RelativeLayout fblog_in_buttonlayout = (RelativeLayout) findViewById(R.id.fblog_in_buttonlayout);
    LoginButton login_button = (LoginButton) findViewById(R.id.login_button);
    mSocialIntegrationMainLayout = (LinearLayout) findViewById(R.id.socialintegrationmainlayotu);
    RelativeLayout cancellayout = (RelativeLayout) findViewById(R.id.cancellayout);
    mContext = this;
    mSharedPreferencesObj = this.getSharedPreferences(
        VueConstants.SHAREDPREFERENCE_NAME, 0);
    mFacebookProgressDialog = ProgressDialog.show(mContext, getResources()
        .getString(R.string.sidemenu_sub_option_Facebook), getResources()
        .getString(R.string.sharing_mesg), true);
    mGooglePlusProgressDialog = ProgressDialog.show(mContext, getResources()
        .getString(R.string.sidemenu_sub_option_Googleplus), getResources()
        .getString(R.string.loading_mesg), true);
    mGooglePlusProgressDialog.dismiss();
    mFacebookProgressDialog.dismiss();

    mBundle = getIntent().getExtras();
    if (mBundle != null) {
      mHideCancelButton = mBundle
          .getBoolean(VueConstants.CANCEL_BTN_DISABLE_FLAG);
      mFromBezelMenuLogin = mBundle
          .getBoolean(VueConstants.FROM_BEZELMENU_LOGIN);
      mFromInviteFriends = mBundle.getString(VueConstants.FROM_INVITEFRIENDS);
      mFromDetailsFbShare = mBundle
          .getBoolean(VueConstants.FBLOGIN_FROM_DETAILS_SHARE);
      mFbFriendId = mBundle.getString(VueConstants.FB_FRIEND_ID);
      mFbFriendName = mBundle.getString(VueConstants.FB_FRIEND_NAME);
      mGoogleplusFriendInvite = mBundle
          .getBoolean(VueConstants.GOOGLEPLUS_FRIEND_INVITE);
      mGoogleplusAutomaticLogin = mBundle
          .getBoolean(VueConstants.GOOGLEPLUS_AUTOMATIC_LOGIN);
    }

    // Facebook Invite friend
    if (mFbFriendId != null) {
      mSocialIntegrationMainLayout.setVisibility(View.GONE);
      mTrendingbg.setVisibility(View.GONE);
      publishFeedDialog(mFbFriendId, mFbFriendName);
    }
    // Google+ invite friend
    else if (mGoogleplusFriendInvite) {
      mSocialIntegrationMainLayout.setVisibility(View.GONE);
      mTrendingbg.setVisibility(View.GONE);
      mGooglePlusProgressDialog.show();
      mDontCallUserInfoChangesMethod = true;
      mSignInFragment = PlusClientFragment.getPlusClientFragment(
          VueLoginActivity.this, MomentUtil.VISIBLE_ACTIVITIES);
    } else if (mGoogleplusAutomaticLogin) {
      mSocialIntegrationMainLayout.setVisibility(View.GONE);
      mTrendingbg.setVisibility(View.GONE);
      mGooglePlusProgressDialog.show();
      mDontCallUserInfoChangesMethod = true;
      mSignInFragment = PlusClientFragment.getPlusClientFragment(
          VueLoginActivity.this, MomentUtil.VISIBLE_ACTIVITIES);
      mSignInFragment.signIn(REQUEST_CODE_PLUS_CLIENT_FRAGMENT);
    } else {
      if (mFromDetailsFbShare) {
        mSocialIntegrationMainLayout.setVisibility(View.GONE);
        mTrendingbg.setVisibility(View.GONE);
        mSharedPreferencesObj = this.getSharedPreferences(
            VueConstants.SHAREDPREFERENCE_NAME, 0);
        boolean facebookloginflag = mSharedPreferencesObj.getBoolean(
            VueConstants.FACEBOOK_LOGIN, false);
        if (facebookloginflag) {
          ArrayList<clsShare> filePathList = mBundle
              .getParcelableArrayList(VueConstants.FBPOST_IMAGEURLS);
          shareToFacebook(filePathList,
              mBundle.getString(VueConstants.FBPOST_TEXT));
        } else {
          mSocialIntegrationMainLayout.setVisibility(View.VISIBLE);
          googleplusign_in_buttonlayout.setVisibility(View.GONE);
          instagramSignInButtonLayout.setVisibility(View.GONE);
          fblog_in_buttonlayout.setVisibility(View.INVISIBLE);
          cancellayout.setVisibility(View.GONE);
          login_button.performClick();
        }
      } else {
        mSignInFragment = PlusClientFragment.getPlusClientFragment(
            VueLoginActivity.this, MomentUtil.VISIBLE_ACTIVITIES);
        if (mHideCancelButton) {
          cancellayout.setVisibility(View.GONE);
        }
        boolean fbloginfalg = mSharedPreferencesObj.getBoolean(
            VueConstants.FACEBOOK_LOGIN, false);
        boolean googleplusloginfalg = mSharedPreferencesObj.getBoolean(
            VueConstants.GOOGLEPLUS_LOGIN, false);
        boolean instagramloginfalg = mSharedPreferencesObj.getBoolean(
            VueConstants.INSTAGRAM_LOGIN, false);
        if (mFromInviteFriends != null) {
          if (mFromInviteFriends.equals(VueConstants.FACEBOOK)) {
            googleplusign_in_buttonlayout.setVisibility(View.GONE);
            instagramSignInButtonLayout.setVisibility(View.GONE);
            mFacebookFlag = true;
          }
          if (mFromInviteFriends.equals(VueConstants.GOOGLEPLUS)) {
            mDontCallUserInfoChangesMethod = true;
            mFromGoogleplusInvitefriends = true;
            fblog_in_buttonlayout.setVisibility(View.GONE);
            instagramSignInButtonLayout.setVisibility(View.GONE);
          }
        } else if (mFromBezelMenuLogin) {
          if (fbloginfalg) {
            mDontCallUserInfoChangesMethod = true;
            fblog_in_buttonlayout.setVisibility(View.GONE);
          } else if (googleplusloginfalg) {
            mFacebookFlag = true;
            googleplusign_in_buttonlayout.setVisibility(View.GONE);
          }
          if (instagramloginfalg) {
            instagramSignInButtonLayout.setVisibility(View.GONE);
          }
        }
        googleplusign_in_buttonlayout.setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View arg0) {
            if (VueConnectivityManager
                .isNetworkConnected(VueLoginActivity.this)) {
              mSocialIntegrationMainLayout.setVisibility(View.GONE);
              mGoogleplusLoggedinDialogFlag = true;
              if (mFromGoogleplusInvitefriends)
                mGooglePlusProgressDialog.show();
              mSignInFragment.signIn(REQUEST_CODE_PLUS_CLIENT_FRAGMENT);
            } else {
              Toast.makeText(VueLoginActivity.this,
                  getResources().getString(R.string.no_network),
                  Toast.LENGTH_LONG).show();
            }
          }
        });
        instagramSignInButtonLayout.setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View arg0) {
            if (VueConnectivityManager
                .isNetworkConnected(VueLoginActivity.this)) {
              mInstagramApp = new InstagramApp(VueLoginActivity.this,
                  getResources().getString(R.string.instagram_client_id),
                  getResources().getString(R.string.instagram_client_secret),
                  getResources().getString(R.string.instagram_callbackurl));
              mInstagramApp.setListener(listener);
              if (!mInstagramApp.hasAccessToken()) {
                mInstagramApp.authorize();
              } else {
                Toast.makeText(VueLoginActivity.this,
                    "This User is Already Logged in with Instagram",
                    Toast.LENGTH_LONG);
              }
            } else {
              Toast.makeText(VueLoginActivity.this,
                  getResources().getString(R.string.no_network),
                  Toast.LENGTH_LONG).show();
            }
          }
        });
        login_button.setReadPermissions(READ_PERMISSIONS);
        login_button
            .setUserInfoChangedCallback(new LoginButton.UserInfoChangedCallback() {
              public void onUserInfoFetched(GraphUser user) {
                if (!mDontCallUserInfoChangesMethod) {
                  Log.e("VueLoginActivity",
                      "update UI called from user info changed method");
                  updateUI();
                }
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
    try {
      HttpsURLConnection.setDefaultHostnameVerifier(new NullHostNameVerifier());
      SSLContext context = SSLContext.getInstance("TLS");
      context.init(null, new X509TrustManager[] {new NullX509TrustManager()},
          new SecureRandom());
      HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
    } catch (NoSuchAlgorithmException ex) {

    } catch (KeyManagementException ex2) {

    }
  }

  private static class NullX509TrustManager implements X509TrustManager {

    public void checkClientTrusted(X509Certificate[] cert, String authType) {
    }

    public void checkServerTrusted(X509Certificate[] cert, String authType) {
    }

    public X509Certificate[] getAcceptedIssuers() {
      return null;
    }
  }

  private class NullHostNameVerifier implements HostnameVerifier {

    public boolean verify(String hostname, SSLSession session) {
      Log.i("RestUtilImpl", "Approving certificate for " + hostname);
      return true;
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    mUiHelper.onDestroy();
  }

  @Override
  public void onPause() {
    super.onPause();
    mUiHelper.onPause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    mUiHelper.onResume();
  }

  private void onSessionStateChange(Session session, SessionState state,
      Exception exception) {
    if (mPendingAction != PendingAction.NONE
        && (exception instanceof FacebookOperationCanceledException || exception
            instanceof FacebookAuthorizationException)) {
      new AlertDialog.Builder(VueLoginActivity.this)
          .setTitle(R.string.cancelled)
          .setMessage(R.string.permission_not_granted)
          .setPositiveButton(R.string.ok, null).show();
      mPendingAction = PendingAction.NONE;
    } else if (state == SessionState.OPENED_TOKEN_UPDATED) {
      handlePendingAction(null, null);
    }
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mUiHelper.onSaveInstanceState(outState);
    outState.putString(PENDING_ACTION_BUNDLE_KEY, mPendingAction.name());
    outState.putString(VueConstants.FROM_INVITEFRIENDS, mFromInviteFriends);
    outState.putBoolean(VueConstants.FBLOGIN_FROM_DETAILS_SHARE,
        mFromDetailsFbShare);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    // From Googleplus app to send the invitation to friend
    if (requestCode == REQUEST_CODE_INTERACTIVE_POST) {
      if (mGooglePlusProgressDialog != null)
        if (mGooglePlusProgressDialog.isShowing())
          mGooglePlusProgressDialog.dismiss();
      finish();
    }
    try {
      mUiHelper.onActivityResult(requestCode, resultCode, data);
      if (!mDontCallUserInfoChangesMethod) {
        Log.e("VueLoginActivity",
            "update UI called from onActivityResult method");
        updateUI();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    try {
      mSignInFragment.handleOnActivityResult(requestCode, resultCode, data);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void onSignedIn(PlusClient plusClient) {
    SharedPreferences.Editor editor = mSharedPreferencesObj.edit();
    editor.putBoolean(VueConstants.VUE_LOGIN, true);
    editor.putBoolean(VueConstants.GOOGLEPLUS_LOGIN, true);
    editor.putString(VueConstants.GOOGLEPLUS_USER_EMAIL,
        plusClient.getAccountName());
    editor.commit();
    plusClient.loadPerson(this, "me");
    if (!mGoogleplusFriendInvite && !mFacebookFlag
        && !mGoogleplusAutomaticLogin) {
      Toast.makeText(
          this,
          plusClient.getAccountName() + " "
              + getResources().getString(R.string.isconnected_mesg),
          Toast.LENGTH_LONG).show();
    }
    // To show Google+ App install dialog after login with Google+
    if (mGoogleplusLoggedinDialogFlag) {

      if (!Utils.appInstalledOrNot(VueConstants.GOOGLEPLUS_PACKAGE_NAME, this)) {
        if (mGooglePlusProgressDialog != null
            && mGooglePlusProgressDialog.isShowing())
          mGooglePlusProgressDialog.dismiss();
        showAlertMessageForAppInstalation("Google+",
            VueConstants.GOOGLEPLUS_PACKAGE_NAME);
      }
    }

    if (mGoogleplusFriendInvite) {
      share(plusClient, this, VueConstants.INVITATION_MESG,
          mBundle.getIntegerArrayList(VueConstants.GOOGLEPLUS_FRIEND_INDEX));
    } else {
      plusClient.loadPeople(this, Person.Collection.VISIBLE);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public void onPeopleLoaded(ConnectionResult status,
      PersonBuffer personBuffer, String nextPageToken) {
    if (ConnectionResult.SUCCESS == status.getErrorCode()) {
      VueLandingPageActivity.mGooglePlusFriendsDetailsList = new ArrayList<FbGPlusDetails>();
      if (personBuffer != null && personBuffer.getCount() > 0) {
        for (Person p : personBuffer) {
          FbGPlusDetails googlePlusFriendsDetailsObj = new FbGPlusDetails(null,
              p.getDisplayName(), p.getImage().getUrl(), p);
          VueLandingPageActivity.mGooglePlusFriendsDetailsList
              .add(googlePlusFriendsDetailsObj);
        }
        if (VueLandingPageActivity.mGooglePlusFriendsDetailsList != null) {
          Collections.sort(
              VueLandingPageActivity.mGooglePlusFriendsDetailsList,
              new SortBasedOnName());
        }
        if (mFromInviteFriends != null
            && mFromInviteFriends.equals(VueConstants.GOOGLEPLUS)) {
          Intent resultIntent = new Intent();
          Bundle b = new Bundle();
          b.putString(
              VueConstants.INVITE_FRIENDS_LOGINACTIVITY_BUNDLE_STRING_KEY,
              getResources().getString(R.string.sidemenu_sub_option_Googleplus));
          resultIntent.putExtras(b);
          setResult(VueConstants.INVITE_FRIENDS_LOGINACTIVITY_REQUEST_CODE,
              resultIntent);
          finish();
        }
      }
    }
    if (mGooglePlusProgressDialog != null
        && mGooglePlusProgressDialog.isShowing())
      mGooglePlusProgressDialog.dismiss();
    if (!mFacebookFlag) finish();
  }

  private void showAlertMessageForAppInstalation(String appName,
      final String packageName) {
    final Dialog dialog = new Dialog(this, R.style.Theme_Dialog_Translucent);
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    dialog.setContentView(R.layout.vue_popup);
    TextView noButton = (TextView) dialog.findViewById(R.id.nobutton);
    TextView okButton = (TextView) dialog.findViewById(R.id.okbutton);
    TextView messagetext = (TextView) dialog.findViewById(R.id.messagetext);
    messagetext.setText(getResources()
        .getString(R.string.app_installation_mesg) + " " + appName + "?");
    okButton.setText("Install " + appName);
    okButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        dialog.dismiss();
        Intent goToMarket = new Intent(Intent.ACTION_VIEW).setData(Uri
            .parse("market://details?id=" + packageName));
        startActivity(goToMarket);
      }
    });
    noButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        dialog.dismiss();
      }
    });
    dialog.show();
    dialog.setOnDismissListener(new OnDismissListener() {
      @Override
      public void onDismiss(DialogInterface arg0) {
        finish();
      }
    });
  }

  private void saveFBLoginDetails(final Session session) {
    mSharedPreferencesObj = this.getSharedPreferences(
        VueConstants.SHAREDPREFERENCE_NAME, 0);
    final SharedPreferences.Editor editor = mSharedPreferencesObj.edit();
    Request.executeMeRequestAsync(session, new GraphUserCallback() {
      @Override
      public void onCompleted(GraphUser user, com.facebook.Response response) {
        if (user != null) {
          FlurryAgent.logEvent("Facebook_Logins");
          FlurryAgent.endTimedEvent("Login_Time_Ends");
          FlurryAgent.logEvent("Login_Success");
          String location = "";
          VueUserManager userManager = VueUserManager.getUserManager();
          VueUser storedVueUser = null;
          try {
            storedVueUser = Utils.readUserObjectFromFile(VueLoginActivity.this,
                VueConstants.VUE_APP_USEROBJECT__FILENAME);
          } catch (Exception e2) {
            e2.printStackTrace();
          }
          if (storedVueUser != null) {
            if (storedVueUser.getUserIdentity().equals(
                VueUserManager.PreferredIdentityLayer.DEVICE_ID)) {
              storedVueUser
                  .setUserIdentityMethod(VueUserManager.PreferredIdentityLayer.FB);
            } else if (storedVueUser.getUserIdentity().equals(
                VueUserManager.PreferredIdentityLayer.GPLUS)) {
              storedVueUser
                  .setUserIdentityMethod(VueUserManager.PreferredIdentityLayer.GPLUS_FB);
            } else if (storedVueUser.getUserIdentity().equals(
                VueUserManager.PreferredIdentityLayer.INSTAGRAM)) {
              storedVueUser
                  .setUserIdentityMethod(VueUserManager.PreferredIdentityLayer.FB_INSTAGRAM);
            } else if (storedVueUser.getUserIdentity().equals(
                VueUserManager.PreferredIdentityLayer.GPLUS_INSTAGRAM)) {
              storedVueUser
                  .setUserIdentityMethod(VueUserManager.PreferredIdentityLayer.ALL_IDS_AVAILABLE);
            } else {
              storedVueUser
                  .setUserIdentityMethod(VueUserManager.PreferredIdentityLayer.FB);
            }
            userManager.updateFBIdentifiedUser(user, storedVueUser,
                new UserUpdateCallback() {
                  @Override
                  public void onUserUpdated(VueUser user) {
                    try {
                      Utils.writeUserObjectToFile(VueLoginActivity.this,
                          VueConstants.VUE_APP_USEROBJECT__FILENAME, user);
                    } catch (Exception e) {
                      e.printStackTrace();
                    }
                  }
                });
          } else {
            userManager.createFBIdentifiedUser(user,
                new VueUserManager.UserUpdateCallback() {
                  @Override
                  public void onUserUpdated(VueUser user) {
                    try {
                      Utils.writeUserObjectToFile(VueLoginActivity.this,
                          VueConstants.VUE_APP_USEROBJECT__FILENAME, user);
                    } catch (Exception e) {
                      e.printStackTrace();
                    }
                    Log.e("Vue User Creation",
                        "callback from successful user creation");
                  }
                });
          }
          try {
            if (user.getLocation() != null) {
              JSONObject jsonObject = user.getLocation().getInnerJSONObject();
              location = jsonObject
                  .getString(VueConstants.FACEBOOK_GRAPHIC_OBJECT_NAME_KEY);
            }
          } catch (JSONException e1) {
            e1.printStackTrace();
          }

          try {
            VueUserProfile storedUserProfile = null;
            try {
              storedUserProfile = Utils.readUserProfileObjectFromFile(
                  VueLoginActivity.this,
                  VueConstants.VUE_APP_USERPROFILEOBJECT__FILENAME);
            } catch (Exception e) {
              e.printStackTrace();
            }
            if (storedUserProfile == null
                || (storedUserProfile != null && !storedUserProfile
                    .isUserDetailsModified())) {
              VueUserProfile vueUserProfile = new VueUserProfile(
                  VueConstants.FACEBOOK_USER_PROFILE_PICTURE_MAIN_URL
                      + user.getId()
                      + VueConstants.FACEBOOK_USER_PROFILE_PICTURE_SUB_URL,
                  user.getProperty(VueConstants.FACEBOOK_GRAPHIC_OBJECT_EMAIL_KEY)
                      + "",
                  user.getName(),
                  user.getBirthday(),
                  user.getProperty(VueConstants.FACEBOOK_GRAPHIC_OBJECT_GENDER_KEY)
                      + "", location, false);
              Utils.writeUserProfileObjectToFile(VueLoginActivity.this,
                  VueConstants.VUE_APP_USERPROFILEOBJECT__FILENAME,
                  vueUserProfile);
            }
          } catch (Exception e1) {
            e1.printStackTrace();
          }

          editor.putString(VueConstants.FACEBOOK_ACCESSTOKEN,
              session.getAccessToken());
          editor.putBoolean(VueConstants.VUE_LOGIN, true);
          editor.putBoolean(VueConstants.FACEBOOK_LOGIN, true);
          editor.commit();
          if (mFromInviteFriends != null
              && mFromInviteFriends.equals(VueConstants.FACEBOOK)) {
            mFromInviteFriends = null;
            try {
              Intent resultIntent = new Intent();
              Bundle b = new Bundle();
              b.putString(
                  VueConstants.INVITE_FRIENDS_LOGINACTIVITY_BUNDLE_STRING_KEY,
                  getResources().getString(
                      R.string.sidemenu_sub_option_Facebook));
              resultIntent.putExtras(b);
              setResult(VueConstants.INVITE_FRIENDS_LOGINACTIVITY_REQUEST_CODE,
                  resultIntent);
              finish();
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
          if (!mFromDetailsFbShare) finish();
        } else {
          editor.putString(VueConstants.FACEBOOK_ACCESSTOKEN,
              session.getAccessToken());
          editor.putBoolean(VueConstants.VUE_LOGIN, true);
          editor.putBoolean(VueConstants.FACEBOOK_LOGIN, true);
          editor.commit();
          if (mFromInviteFriends != null
              && mFromInviteFriends.equals(VueConstants.FACEBOOK)) {
            mFromInviteFriends = null;
            try {
              Intent resultIntent = new Intent();
              Bundle b = new Bundle();
              b.putString(
                  VueConstants.INVITE_FRIENDS_LOGINACTIVITY_BUNDLE_STRING_KEY,
                  getResources().getString(
                      R.string.sidemenu_sub_option_Facebook));
              resultIntent.putExtras(b);
              setResult(VueConstants.INVITE_FRIENDS_LOGINACTIVITY_REQUEST_CODE,
                  resultIntent);
              finish();
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
          if (!mFromDetailsFbShare) finish();
        }
      }
    });
    try {
      mSocialIntegrationMainLayout.setVisibility(View.GONE);
      mTrendingbg.setVisibility(View.GONE);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void updateUI() {
    Session session = Session.getActiveSession();
    boolean fbloggedin = (session != null && session.isOpened());
    if (fbloggedin) {
      saveFBLoginDetails(session);
      if (mFromDetailsFbShare) {
        try {
          ArrayList<clsShare> filePathList = mBundle
              .getParcelableArrayList(VueConstants.FBPOST_IMAGEURLS);
          shareToFacebook(filePathList,
              mBundle.getString(VueConstants.FBPOST_TEXT));
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }

  private void shareToFacebook(ArrayList<clsShare> fileList, String articledesc) {
    performPublish(PendingAction.POST_PHOTO, fileList, articledesc);
  }

  private void performPublish(PendingAction action,
      ArrayList<clsShare> fileList, String articledesc) {
    Session session = Session.getActiveSession();
    if (session != null) {
      if (hasPublishPermission()) {
        handlePendingAction(fileList, articledesc);
      } else {
        session.requestNewPublishPermissions(new Session.NewPermissionsRequest(
            this, PUBLISH_PERMISSIONS));
      }
    }
  }

  private boolean hasPublishPermission() {
    Session session = Session.getActiveSession();
    return session != null
        && session.getPermissions().contains(
            getResources().getString(R.string.publish_actions));
  }

  private void handlePendingAction(ArrayList<clsShare> fileList,
      String articledesc) {
    postPhoto(fileList, articledesc);
  }

  private void postPhoto(final ArrayList<clsShare> fileList,
      final String articledesc) {
    if (hasPublishPermission()) {
      mFacebookProgressDialog.show();
      // Post photo....
      if (fileList != null) {
        Thread t = new Thread(new Runnable() {
          @Override
          public void run() {
            for (int i = 0; i < fileList.size(); i++) {
              final File f = new File(fileList.get(i).getFilepath());
              if (!f.exists()) {
                @SuppressWarnings("rawtypes")
                Response.Listener listener = new Response.Listener<InputStream>() {
                  @Override
                  public void onResponse(InputStream is) {
                    OutputStream os = null;
                    try {
                      os = new FileOutputStream(f);
                    } catch (FileNotFoundException e) {
                      e.printStackTrace();
                    }
                    Utils.CopyStream(is, os);
                  }
                };
                Response.ErrorListener errorListener = new Response.ErrorListener() {
                  @Override
                  public void onErrorResponse(VolleyError arg0) {
                    Log.e(TAG, arg0.getMessage());
                  }
                };
                if (fileList.get(i).getImageUrl() != null) {
                  @SuppressWarnings("unchecked")
                  ImageRequest imagerequestObj = new ImageRequest(fileList.get(
                      i).getImageUrl(), listener, 0, 0, null, errorListener);
                  VueApplication.getInstance().getRequestQueue()
                      .add(imagerequestObj);
                }
              }
              final int index = i;
              VueLoginActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                  Bundle parameters = new Bundle(1);
                  parameters.putString("message", articledesc + "");
                  ParcelFileDescriptor descriptor = null;
                  try {
                    descriptor = ParcelFileDescriptor.open(new File(fileList
                        .get(index).getFilepath()),
                        ParcelFileDescriptor.MODE_READ_ONLY);
                  } catch (FileNotFoundException e) {
                    e.printStackTrace();
                  }
                  parameters.putParcelable("picture", descriptor);
                  Callback callback = new Request.Callback() {
                    public void onCompleted(com.facebook.Response response) {
                      if (index == fileList.size() - 1) {
                        mFacebookProgressDialog.dismiss();
                        showPublishResult(VueLoginActivity.this
                            .getString(R.string.photo_post), response
                            .getGraphObject(), response.getError());
                      }
                    }
                  };
                  Request request = new Request(Session.getActiveSession(),
                      "me/photos", parameters, HttpMethod.POST, callback);
                  request.executeAsync();
                }
              });
            }
          }
        });
        t.start();
      } else {
        mFacebookProgressDialog.dismiss();
      }
    }
  }

  private void showPublishResult(String message, GraphObject result,
      FacebookRequestError error) {
    String alertMessage = null;
    if (error == null) {
      alertMessage = VueLoginActivity.this
          .getString(R.string.successfully_posted_post);
    } else {
      if (error.getErrorCode() == 506) {
        alertMessage = VueLoginActivity.this
            .getString(R.string.duplicatepostmesg);
      } else if (error.getErrorCode() == 368) {
        alertMessage = VueLoginActivity.this
            .getString(R.string.facebookwarningmesg);
      } else {
        alertMessage = error.getErrorMessage();
      }
    }
    final Dialog dialog = new Dialog(VueLoginActivity.this,
        R.style.Theme_Dialog_Translucent);
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    dialog.setContentView(R.layout.networkdialogue);
    TextView messagetext = (TextView) dialog.findViewById(R.id.messagetext);
    TextView okbutton = (TextView) dialog.findViewById(R.id.okbutton);
    okbutton.setVisibility(View.GONE);
    View networkdialogline = dialog.findViewById(R.id.networkdialogline);
    networkdialogline.setVisibility(View.GONE);
    TextView nobutton = (TextView) dialog.findViewById(R.id.nobutton);
    nobutton.setText(getResources().getString(R.string.ok));
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
        finish();
      }
    });
  }

  private void publishFeedDialog(String friend_uid, String friendname) {
    Bundle params = new Bundle();
    params.putString("to", friend_uid);
    WebDialog feedDialog = (new WebDialog.FeedDialogBuilder(
        VueLoginActivity.this, Session.getActiveSession(), params))
        .setOnCompleteListener(new OnCompleteListener() {
          @Override
          public void onComplete(Bundle values, FacebookException error) {
            if (error == null) {
              final String postId = values.getString("post_id");
              if (postId != null) {
                Toast.makeText(VueLoginActivity.this,
                    "Posted story, id: " + postId, Toast.LENGTH_SHORT).show();
              } else {
                Toast.makeText(VueLoginActivity.this, "Publish cancelled",
                    Toast.LENGTH_SHORT).show();
              }
            } else if (error instanceof FacebookOperationCanceledException) {
              Toast.makeText(VueLoginActivity.this, "Publish cancelled",
                  Toast.LENGTH_SHORT).show();
            } else {
              Toast.makeText(VueLoginActivity.this, "Error posting story",
                  Toast.LENGTH_SHORT).show();
            }
          }
        }).build();
    feedDialog.show();
    feedDialog.setOnDismissListener(new OnDismissListener() {
      @Override
      public void onDismiss(DialogInterface arg0) {
        finish();
      }
    });
  }

  @Override
  public void onSignedFail() {
    boolean fbloginflag = mSharedPreferencesObj.getBoolean(
        VueConstants.FACEBOOK_LOGIN, false);
    SharedPreferences.Editor editor = mSharedPreferencesObj.edit();
    if (!fbloginflag) editor.putBoolean(VueConstants.VUE_LOGIN, false);
    editor.putBoolean(VueConstants.GOOGLEPLUS_LOGIN, false);
    editor.commit();
    if (mGooglePlusProgressDialog != null
        && mGooglePlusProgressDialog.isShowing())
      mGooglePlusProgressDialog.dismiss();
    finish();
  }

  private Intent getInteractivePostIntent(PlusClient plusClient,
      Activity activity, String post, ArrayList<Integer> personIndexList) {
    String action = "/?view=true";
    Uri callToActionUrl = Uri
        .parse(getString(R.string.plus_example_deep_link_url) + action);
    String callToActionDeepLinkId = getString(R.string.plus_example_deep_link_id)
        + action;
    // Create an interactive post builder.
    PlusShare.Builder builder = new PlusShare.Builder(activity, plusClient);
    // Set call-to-action metadata.
    builder.addCallToAction(LABEL_VIEW_ITEM, callToActionUrl,
        callToActionDeepLinkId);
    // Set the target url (for desktop use).
    builder.setContentUrl(Uri
        .parse(getString(R.string.plus_example_deep_link_url)));
    // Set the target deep-link ID (for mobile use).
    builder.setContentDeepLinkId(getString(R.string.plus_example_deep_link_id),
        null, null, null);
    List<Person> googlefriendList = new ArrayList<Person>();
    if (personIndexList != null
        && VueLandingPageActivity.mGooglePlusFriendsDetailsList != null) {
      for (int i = 0; i < personIndexList.size(); i++) {
        googlefriendList
            .add(VueLandingPageActivity.mGooglePlusFriendsDetailsList.get(
                personIndexList.get(i)).getGoogleplusFriend());
      }
    }
    if (mBundle != null) {
      String aisleImagePath = mBundle
          .getString(VueConstants.GOOGLEPLUS_FRIEND_IMAGE_PATH_LIST_KEY);
      if (aisleImagePath != null) {
        builder.setStream(Uri.fromFile(new File(aisleImagePath)));
      }
    }
    builder.setRecipients(googlefriendList);
    // Set the pre-filled message.
    builder.setText(post);
    builder.setType("text/plain");
    return builder.getIntent();
  }

  private void share(PlusClient plusClient, Activity activity, String post,
      ArrayList<Integer> personIndexList) {
    startActivityForResult(
        getInteractivePostIntent(plusClient, activity, post, personIndexList),
        REQUEST_CODE_INTERACTIVE_POST);
  }

  @Override
  public void onPersonLoaded(ConnectionResult connectionresult, Person person) {
    if (connectionresult.getErrorCode() == ConnectionResult.SUCCESS) {

      FlurryAgent.logEvent("GooglePlus_Logins");
      FlurryAgent.endTimedEvent("Login_Time_Ends");
      FlurryAgent.logEvent("Login_Success");
      mSharedPreferencesObj = this.getSharedPreferences(
          VueConstants.SHAREDPREFERENCE_NAME, 0);
      VueUserManager userManager = VueUserManager.getUserManager();
      VueUser vueUser = new VueUser(null, person.getDisplayName(), null, null,
          mSharedPreferencesObj.getString(VueConstants.GOOGLEPLUS_USER_EMAIL,
              null));
      vueUser.setUsersName(person.getDisplayName(), "");
      VueUser storedVueUser = null;
      try {
        storedVueUser = Utils.readUserObjectFromFile(VueLoginActivity.this,
            VueConstants.VUE_APP_USEROBJECT__FILENAME);
      } catch (Exception e2) {
        e2.printStackTrace();
      }
      if (storedVueUser != null) {
        vueUser.setDeviceId(storedVueUser.getDeviceId());
        vueUser.setFacebookId(storedVueUser.getFacebookId());
        vueUser.setInstagramId(storedVueUser.getInstagramId());
        vueUser.setVueUserId(storedVueUser.getVueId());
        if (storedVueUser.getUserIdentity().equals(
            VueUserManager.PreferredIdentityLayer.DEVICE_ID)) {
          vueUser
              .setUserIdentityMethod(VueUserManager.PreferredIdentityLayer.GPLUS);
        } else if (storedVueUser.getUserIdentity().equals(
            VueUserManager.PreferredIdentityLayer.FB)) {
          vueUser
              .setUserIdentityMethod(VueUserManager.PreferredIdentityLayer.GPLUS_FB);
        } else if (storedVueUser.getUserIdentity().equals(
            VueUserManager.PreferredIdentityLayer.INSTAGRAM)) {
          vueUser
              .setUserIdentityMethod(VueUserManager.PreferredIdentityLayer.GPLUS_INSTAGRAM);
        } else if (storedVueUser.getUserIdentity().equals(
            VueUserManager.PreferredIdentityLayer.FB_INSTAGRAM)) {
          vueUser
              .setUserIdentityMethod(VueUserManager.PreferredIdentityLayer.ALL_IDS_AVAILABLE);
        } else {
          vueUser
              .setUserIdentityMethod(VueUserManager.PreferredIdentityLayer.GPLUS);
        }
        userManager.updateGooglePlusIdentifiedUser(vueUser,
            new UserUpdateCallback() {
              @Override
              public void onUserUpdated(VueUser user) {
                try {
                  Utils.writeUserObjectToFile(VueLoginActivity.this,
                      VueConstants.VUE_APP_USEROBJECT__FILENAME, user);
                } catch (Exception e) {
                  e.printStackTrace();
                }
              }
            });
      } else {
        userManager.createGooglePlusIdentifiedUser(vueUser,
            new VueUserManager.UserUpdateCallback() {
              @Override
              public void onUserUpdated(VueUser user) {
                try {
                  Utils.writeUserObjectToFile(VueLoginActivity.this,
                      VueConstants.VUE_APP_USEROBJECT__FILENAME, user);
                } catch (Exception e) {
                  e.printStackTrace();
                }
                Log.e("Vue User Creation",
                    "callback from successful user creation");
              }
            });
      }
      VueUserProfile storedUserProfile = null;
      try {
        storedUserProfile = Utils.readUserProfileObjectFromFile(this,
            VueConstants.VUE_APP_USERPROFILEOBJECT__FILENAME);
      } catch (Exception e) {
        e.printStackTrace();
      }
      if (storedUserProfile == null) {
        VueUserProfile vueUserProfile = new VueUserProfile(person.getImage()
            .getUrl(), mSharedPreferencesObj.getString(
            VueConstants.GOOGLEPLUS_USER_EMAIL, null), person.getDisplayName(),
            null, null, null, false);
        try {
          Utils.writeUserProfileObjectToFile(this,
              VueConstants.VUE_APP_USERPROFILEOBJECT__FILENAME, vueUserProfile);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }

  OAuthAuthenticationListener listener = new OAuthAuthenticationListener() {

    @Override
    public void onSuccess() {
      saveInstagramUserDetails();
    }

    @Override
    public void onFail(String error) {
      Toast.makeText(VueLoginActivity.this, error, Toast.LENGTH_SHORT).show();
    }
  };

  private void saveInstagramUserDetails() {
    if (mInstagramApp != null) {
      FlurryAgent.logEvent("Instagram_Logins");
      FlurryAgent.endTimedEvent("Login_Time_Ends");
      FlurryAgent.logEvent("Login_Success");
      mSharedPreferencesObj = this.getSharedPreferences(
          VueConstants.SHAREDPREFERENCE_NAME, 0);
      SharedPreferences.Editor editor = mSharedPreferencesObj.edit();
      editor.putBoolean(VueConstants.VUE_LOGIN, true);
      editor.putBoolean(VueConstants.INSTAGRAM_LOGIN, true);
      editor.commit();

      VueUserManager userManager = VueUserManager.getUserManager();
      VueUser vueUser = new VueUser(null, null, mInstagramApp.getName(), null,
          null);
      vueUser.setUsersName(mInstagramApp.getName(), "");
      VueUser storedVueUser = null;
      try {
        storedVueUser = Utils.readUserObjectFromFile(VueLoginActivity.this,
            VueConstants.VUE_APP_USEROBJECT__FILENAME);
      } catch (Exception e2) {
        e2.printStackTrace();
      }
      if (storedVueUser != null) {
        vueUser.setDeviceId(storedVueUser.getDeviceId());
        vueUser.setFacebookId(storedVueUser.getFacebookId());
        vueUser.setEmailId(storedVueUser.getEmailId());
        vueUser.setGooglePlusId(storedVueUser.getGooglePlusId());
        vueUser.setVueUserId(storedVueUser.getVueId());
        if (storedVueUser.getUserIdentity().equals(
            VueUserManager.PreferredIdentityLayer.DEVICE_ID)) {
          vueUser
              .setUserIdentityMethod(VueUserManager.PreferredIdentityLayer.INSTAGRAM);
        } else if (storedVueUser.getUserIdentity().equals(
            VueUserManager.PreferredIdentityLayer.FB)) {
          vueUser
              .setUserIdentityMethod(VueUserManager.PreferredIdentityLayer.FB_INSTAGRAM);
        } else if (storedVueUser.getUserIdentity().equals(
            VueUserManager.PreferredIdentityLayer.GPLUS)) {
          vueUser
              .setUserIdentityMethod(VueUserManager.PreferredIdentityLayer.GPLUS_INSTAGRAM);
        } else if (storedVueUser.getUserIdentity().equals(
            VueUserManager.PreferredIdentityLayer.GPLUS_FB)) {
          vueUser
              .setUserIdentityMethod(VueUserManager.PreferredIdentityLayer.ALL_IDS_AVAILABLE);
        } else {
          vueUser
              .setUserIdentityMethod(VueUserManager.PreferredIdentityLayer.INSTAGRAM);
        }
        userManager.updateInstagramIdentifiedUser(vueUser,
            new UserUpdateCallback() {
              @Override
              public void onUserUpdated(VueUser user) {
                try {
                  Utils.writeUserObjectToFile(VueLoginActivity.this,
                      VueConstants.VUE_APP_USEROBJECT__FILENAME, user);
                } catch (Exception e) {
                  e.printStackTrace();
                }
              }
            });
      } else {
        userManager.createInstagramIdentifiedUser(vueUser,
            new VueUserManager.UserUpdateCallback() {
              @Override
              public void onUserUpdated(VueUser user) {
                try {
                  Utils.writeUserObjectToFile(VueLoginActivity.this,
                      VueConstants.VUE_APP_USEROBJECT__FILENAME, user);
                } catch (Exception e) {
                  e.printStackTrace();
                }
                Log.e("Vue User Creation",
                    "callback from successful user creation");
              }
            });
      }
      VueUserProfile storedUserProfile = null;
      try {
        storedUserProfile = Utils.readUserProfileObjectFromFile(this,
            VueConstants.VUE_APP_USERPROFILEOBJECT__FILENAME);
      } catch (Exception e) {
      }
      if (storedUserProfile == null) {
        VueUserProfile vueUserProfile = new VueUserProfile(
            mInstagramApp.getProfilePicture(), mInstagramApp.getUserName(),
            mInstagramApp.getName(), null, null, null, false);
        try {
          Utils.writeUserProfileObjectToFile(this,
              VueConstants.VUE_APP_USERPROFILEOBJECT__FILENAME, vueUserProfile);
        } catch (Exception e1) {
          e1.printStackTrace();
        }
      }
      Toast.makeText(VueLoginActivity.this,
          mInstagramApp.getName() + "is succeffully Logged in.",
          Toast.LENGTH_SHORT).show();
      if (!Utils.appInstalledOrNot(VueConstants.INSTAGRAM_PACKAGE_NAME, this)) {
        showAlertMessageForAppInstalation("Instagram",
            VueConstants.INSTAGRAM_PACKAGE_NAME);
      } else {
        finish();
      }
    }
  }

}
