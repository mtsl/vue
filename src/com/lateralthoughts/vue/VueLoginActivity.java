package com.lateralthoughts.vue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import org.json.JSONArray;
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
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.FragmentActivity;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
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
import com.facebook.Session.AuthorizationRequest;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
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
import com.lateralthoughts.vue.connectivity.VueConnectivityManager;
import com.lateralthoughts.vue.logging.Logger;
import com.lateralthoughts.vue.user.VueUser;
import com.lateralthoughts.vue.user.VueUserManager;
import com.lateralthoughts.vue.user.VueUserManager.UserUpdateCallback;
import com.lateralthoughts.vue.user.VueUserProfile;
import com.lateralthoughts.vue.utils.FbGPlusDetails;
import com.lateralthoughts.vue.utils.FileCache;
import com.lateralthoughts.vue.utils.SortBasedOnName;
import com.lateralthoughts.vue.utils.Utils;
import com.lateralthoughts.vue.utils.clsShare;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

public class VueLoginActivity extends FragmentActivity implements
        OnSignedInListener, OnPeopleLoadedListener, OnPersonLoadedListener {
    
    private boolean mFromBezelMenuLogin = false;
    private String mFromInviteFriends = null;
    private boolean mFromDetailsFbShare = false;
    private boolean mDontCallUserInfoChangesMethod = false;
    private String mFbFriendId = null;
    private String mFbFriendName = null;
    private SharedPreferences mSharedPreferencesObj;
    private ImageView mTrendingbg = null;
    private Activity mContext;
    private LinearLayout mSocialIntegrationMainLayout;
    private Bundle mBundle = null;
    private static final String LABEL_VIEW_ITEM = "VIEW_ITEM";
    private final List<String> PUBLISH_PERMISSIONS = Arrays
            .asList("publish_actions");
    private ProgressDialog mFacebookProgressDialog, mGooglePlusProgressDialog;
    private final String PENDING_ACTION_BUNDLE_KEY = VueApplication
            .getInstance().getString(R.string.pendingActionBundleKey);
    private PendingAction mPendingAction = PendingAction.NONE;
    private boolean mIsAlreadyLoggedInWithVue = false, mGoogleplusFriendInvite,
            mFromGoogleplusInvitefriends, mFacebookFlag,
            mGoogleplusAutomaticLogin;
    public static boolean mIsLogInScreenIsVisible = false;
    private MixpanelAPI mixpanel;
    private MixpanelAPI.People people;
    JSONObject loginSelectedProps, loginActivity;
    private String mGuestUserMessage = null;
    private boolean mShowAisleSwipeHelpLayoutFlag = false;
    private ProgressDialog mDialog = null;
    
    private enum PendingAction {
        NONE, POST_PHOTO, POST_STATUS_UPDATE
    }
    
    private UiLifecycleHelper mUiHelper;
    private Session.StatusCallback mCallback = new Session.StatusCallback() {
        public void call(Session session, SessionState state,
                Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };
    // Google+ integration
    public static final int REQUEST_CODE_PLUS_CLIENT_FRAGMENT = 0;
    public PlusClientFragment mSignInFragment;
    private static final int REQUEST_CODE_INTERACTIVE_POST = 3;
    private boolean mIsGplusButtonClicked = false,
            mGoogleplusLoggedinDialogFlag;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mixpanel = MixpanelAPI.getInstance(this,
                VueApplication.getInstance().MIXPANEL_TOKEN);
        people = mixpanel.getPeople();
        loginActivity = new JSONObject();
        try {
            loginActivity.put("Social", "Facebook, GooglePlus");
        } catch (JSONException e2) {
            e2.printStackTrace();
        }
        mixpanel.track("Login Page Displayed", loginActivity);
        mIsLogInScreenIsVisible = true;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vue_login_screen);
        mUiHelper = new UiLifecycleHelper(this, mCallback);
        mUiHelper.onCreate(savedInstanceState);
        mTrendingbg = (ImageView) findViewById(R.id.trendingbg);
        RelativeLayout googleplusign_in_buttonlayout = (RelativeLayout) findViewById(R.id.googleplusign_in_buttonlayout);
        RelativeLayout fblog_in_buttonlayout = (RelativeLayout) findViewById(R.id.fblog_in_buttonlayout);
        LoginButton login_button = (LoginButton) findViewById(R.id.login_button);
        mSocialIntegrationMainLayout = (LinearLayout) findViewById(R.id.socialintegrationmainlayotu);
        VueUser storedVueUser = null;
        try {
            storedVueUser = Utils.readUserObjectFromFile(VueLoginActivity.this,
                    VueConstants.VUE_APP_USEROBJECT__FILENAME);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        if (storedVueUser != null) {
            mIsAlreadyLoggedInWithVue = true;
        }
        mContext = this;
        mSharedPreferencesObj = this.getSharedPreferences(
                VueConstants.SHAREDPREFERENCE_NAME, 0);
        mFacebookProgressDialog = ProgressDialog.show(mContext, getResources()
                .getString(R.string.sidemenu_sub_option_Facebook),
                getResources().getString(R.string.sharing_mesg), true);
        mGooglePlusProgressDialog = ProgressDialog.show(
                mContext,
                getResources().getString(
                        R.string.sidemenu_sub_option_Googleplus),
                getResources().getString(R.string.loading_mesg), true);
        mGooglePlusProgressDialog.dismiss();
        mFacebookProgressDialog.dismiss();
        mBundle = getIntent().getExtras();
        if (mBundle != null) {
            mShowAisleSwipeHelpLayoutFlag = mBundle
                    .getBoolean(VueConstants.SHOW_AISLE_SWIPE_HELP_LAYOUT_FLAG);
            mGuestUserMessage = mBundle
                    .getString(VueConstants.GUEST_LOGIN_MESSAGE);
            mFromBezelMenuLogin = mBundle
                    .getBoolean(VueConstants.FROM_BEZELMENU_LOGIN);
            mFromInviteFriends = mBundle
                    .getString(VueConstants.FROM_INVITEFRIENDS);
            mFromDetailsFbShare = mBundle
                    .getBoolean(VueConstants.FBLOGIN_FROM_DETAILS_SHARE);
            mFbFriendId = mBundle.getString(VueConstants.FB_FRIEND_ID);
            mFbFriendName = mBundle.getString(VueConstants.FB_FRIEND_NAME);
            mGoogleplusFriendInvite = mBundle
                    .getBoolean(VueConstants.GOOGLEPLUS_FRIEND_INVITE);
            mGoogleplusAutomaticLogin = mBundle
                    .getBoolean(VueConstants.GOOGLEPLUS_AUTOMATIC_LOGIN);
        }
        if (mGuestUserMessage != null) {
            Toast.makeText(this, mGuestUserMessage, Toast.LENGTH_LONG).show();
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
                    fblog_in_buttonlayout.setVisibility(View.INVISIBLE);
                    googleplusign_in_buttonlayout.setVisibility(View.GONE);
                    AuthorizationRequest request = new Session.NewPermissionsRequest(
                            this, PUBLISH_PERMISSIONS);
                    Intent intent = com.facebook.NativeProtocol
                            .createLoginDialog20121101Intent(
                                    this,
                                    getResources().getString(R.string.app_id),
                                    new ArrayList<String>(request
                                            .getPermissions()), request
                                            .getDefaultAudience()
                                            .getNativeProtocolAudience());
                    if (!resolveIntent(intent)) {
                        login_button.setPublishPermissions(PUBLISH_PERMISSIONS);
                    }
                    login_button.performClick();
                }
            } else {
                mSignInFragment = PlusClientFragment.getPlusClientFragment(
                        VueLoginActivity.this, MomentUtil.VISIBLE_ACTIVITIES);
                boolean fbloginfalg = mSharedPreferencesObj.getBoolean(
                        VueConstants.FACEBOOK_LOGIN, false);
                boolean googleplusloginfalg = mSharedPreferencesObj.getBoolean(
                        VueConstants.GOOGLEPLUS_LOGIN, false);
                if (mFromInviteFriends != null) {
                    if (mFromInviteFriends.equals(VueConstants.GOOGLEPLUS)) {
                        googleplusign_in_buttonlayout.setVisibility(View.GONE);
                        mDontCallUserInfoChangesMethod = true;
                        fblog_in_buttonlayout.setVisibility(View.GONE);
                    }
                    if (mFromInviteFriends.equals(VueConstants.GOOGLEPLUS)) {
                        mDontCallUserInfoChangesMethod = true;
                        mFromGoogleplusInvitefriends = true;
                        fblog_in_buttonlayout.setVisibility(View.GONE);
                    }
                } else if (mFromBezelMenuLogin) {
                    if (fbloginfalg) {
                        mDontCallUserInfoChangesMethod = true;
                        fblog_in_buttonlayout.setVisibility(View.GONE);
                    } else if (googleplusloginfalg) {
                        mFacebookFlag = true;
                        googleplusign_in_buttonlayout.setVisibility(View.GONE);
                    }
                }
                googleplusign_in_buttonlayout
                        .setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View arg0) {
                                writeToSdcard("Before google+ login : "
                                        + new Date());
                                mixpanel.track("GooglePlus Login Selected",
                                        null);
                                mIsGplusButtonClicked = true;
                                if (VueConnectivityManager
                                        .isNetworkConnected(VueLoginActivity.this)) {
                                    if (Utils
                                            .appInstalledOrNot(
                                                    VueConstants.GOOGLE_PLAY_SERVICES_PACKAGE_NAME,
                                                    VueLoginActivity.this)) {
                                        mGoogleplusLoggedinDialogFlag = true;
                                        if (mFromGoogleplusInvitefriends)
                                            mGooglePlusProgressDialog.show();
                                        mSignInFragment
                                                .signIn(REQUEST_CODE_PLUS_CLIENT_FRAGMENT);
                                    } else {
                                        showAlertMessageForAppInstalation(
                                                "Google Play services",
                                                VueConstants.GOOGLE_PLAY_SERVICES_PACKAGE_NAME);
                                    }
                                } else {
                                    Toast.makeText(
                                            VueLoginActivity.this,
                                            getResources().getString(
                                                    R.string.no_network),
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                AuthorizationRequest request = new Session.NewPermissionsRequest(
                        this, PUBLISH_PERMISSIONS);
                Intent intent = com.facebook.NativeProtocol
                        .createLoginDialog20121101Intent(
                                this,
                                getResources().getString(R.string.app_id),
                                new ArrayList<String>(request.getPermissions()),
                                request.getDefaultAudience()
                                        .getNativeProtocolAudience());
                if (!resolveIntent(intent)) {
                    login_button.setPublishPermissions(PUBLISH_PERMISSIONS);
                }
                login_button
                        .setUserInfoChangedCallback(new LoginButton.UserInfoChangedCallback() {
                            public void onUserInfoFetched(GraphUser user) {
                                if (!mDontCallUserInfoChangesMethod) {
                                    updateUI(false);
                                }
                            }
                        });
                
            }
        }
        try {
            HttpsURLConnection
                    .setDefaultHostnameVerifier(new NullHostNameVerifier());
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null,
                    new X509TrustManager[] { new NullX509TrustManager() },
                    new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(context
                    .getSocketFactory());
        } catch (NoSuchAlgorithmException ex) {
            
        } catch (KeyManagementException ex2) {
            
        }
    }
    
    private boolean resolveIntent(Intent intent) {
        try {
            ResolveInfo resolveInfo = VueApplication.getInstance()
                    .getPackageManager().resolveActivity(intent, 0);
            if (resolveInfo == null) {
                return false;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    @Override
    protected void onStop() {
        mixpanel.flush();
        super.onStop();
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
            return true;
        }
    }
    
    @Override
    public void onDestroy() {
        mIsLogInScreenIsVisible = false;
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
                && (exception instanceof FacebookOperationCanceledException || exception instanceof FacebookAuthorizationException)) {
            mixpanel.track("Facebook Login Fail", null);
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
            showAisleSwipeHelp();
        }
        try {
            mUiHelper.onActivityResult(requestCode, resultCode, data);
            if (!mDontCallUserInfoChangesMethod) {
                updateUI(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            mSignInFragment.handleOnActivityResult(requestCode, resultCode,
                    data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void onSignedIn(PlusClient plusClient) {
        if (mIsGplusButtonClicked) {
            writeToSdcard("After google+ succefull login : " + new Date());
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
                        plusClient.getAccountName()
                                + " "
                                + getResources().getString(
                                        R.string.isconnected_mesg),
                        Toast.LENGTH_LONG).show();
            }
            // To show Google+ App install dialog after login with Google+
            if (mGoogleplusLoggedinDialogFlag) {
                
                if (!Utils.appInstalledOrNot(
                        VueConstants.GOOGLEPLUS_PACKAGE_NAME, this)) {
                    if (mGooglePlusProgressDialog != null
                            && mGooglePlusProgressDialog.isShowing())
                        mGooglePlusProgressDialog.dismiss();
                    showAlertMessageForAppInstalation("Google+",
                            VueConstants.GOOGLEPLUS_PACKAGE_NAME);
                }
            }
        }
        if (mGoogleplusFriendInvite) {
            share(plusClient,
                    this,
                    VueConstants.INVITATION_MESG,
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
                    FbGPlusDetails googlePlusFriendsDetailsObj = new FbGPlusDetails(
                            null, p.getDisplayName(), p.getImage().getUrl(), p);
                    VueLandingPageActivity.mGooglePlusFriendsDetailsList
                            .add(googlePlusFriendsDetailsObj);
                }
                if (VueLandingPageActivity.mGooglePlusFriendsDetailsList != null) {
                    Collections
                            .sort(VueLandingPageActivity.mGooglePlusFriendsDetailsList,
                                    new SortBasedOnName());
                }
                if (mFromInviteFriends != null
                        && mFromInviteFriends.equals(VueConstants.GOOGLEPLUS)) {
                    Intent resultIntent = new Intent();
                    Bundle b = new Bundle();
                    b.putString(
                            VueConstants.INVITE_FRIENDS_LOGINACTIVITY_BUNDLE_STRING_KEY,
                            getResources().getString(
                                    R.string.sidemenu_sub_option_Googleplus));
                    resultIntent.putExtras(b);
                    setResult(
                            VueConstants.INVITE_FRIENDS_LOGINACTIVITY_REQUEST_CODE,
                            resultIntent);
                    showAisleSwipeHelp();
                }
            }
        }
        if (mGooglePlusProgressDialog != null
                && mGooglePlusProgressDialog.isShowing())
            mGooglePlusProgressDialog.dismiss();
    }
    
    private void showAlertMessageForAppInstalation(String appName,
            final String packageName) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder( new ContextThemeWrapper(this,R.style.AppBaseTheme));
        //AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(VueLoginActivity.this);
        alertDialogBuilder.setTitle(getResources().getString(R.string.app_name));
       alertDialogBuilder.setMessage(getResources().getString(
               R.string.app_installation_mesg)
               + " " + appName + "?");
        
        alertDialogBuilder.setPositiveButton("Install " + appName,new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog,int id) {
               
                   dialog.cancel();
                   Intent goToMarket = new Intent(Intent.ACTION_VIEW).setData(Uri
                           .parse("market://details?id=" + packageName));
                   startActivity(goToMarket);
              }
             });
       
        alertDialogBuilder.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog,int id) {
               
                   dialog.cancel();
           
               }
           });
      
         
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
   
        }
    private void saveFBLoginDetails(final Session session) {
        writeToSdcard("After Fb succefull login : " + new Date());
        mSharedPreferencesObj = this.getSharedPreferences(
                VueConstants.SHAREDPREFERENCE_NAME, 0);
        final SharedPreferences.Editor editor = mSharedPreferencesObj.edit();
        Request.newMeRequest(session, new GraphUserCallback() {
            @Override
            public void onCompleted(final GraphUser user,
                    com.facebook.Response response) {
                if (user != null) {
                    JSONObject loginprops = new JSONObject();
                    try {
                        JSONObject innerObject = user.getInnerJSONObject();
                        String email;
                        try {
                            email = innerObject
                                    .getString(VueConstants.FACEBOOK_GRAPHIC_OBJECT_EMAIL_KEY);
                        } catch (Exception e) {
                            email = user.getUsername();
                            e.printStackTrace();
                        }
                        loginprops.put("Email", email);
                        loginprops.put("Login with", "Facebook");
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                    mixpanel.track("Login Success", loginprops);
                    VueUserManager userManager = VueUserManager
                            .getUserManager();
                    writeToSdcard("Before Server login for Facebook : "
                            + new Date());
                    userManager.facebookAuthenticationWithServer(
                            VueConstants.FACEBOOK_USER_PROFILE_PICTURE_MAIN_URL
                                    + user.getId()
                                    + VueConstants.FACEBOOK_USER_PROFILE_PICTURE_SUB_URL,
                            user, new VueUserManager.UserUpdateCallback() {
                                @Override
                                public void onUserUpdated(VueUser vueUser,
                                        final boolean loginSuccessFlag) {
                                    if (vueUser != null) {
                                        VueApplication.getInstance()
                                                .getInstalledApplications(
                                                        VueApplication
                                                                .getInstance());
                                        writeToSdcard("After server Succefull login for Facebook : "
                                                + new Date());
                                        try {
                                            Utils.writeUserObjectToFile(
                                                    VueLoginActivity.this,
                                                    VueConstants.VUE_APP_USEROBJECT__FILENAME,
                                                    vueUser);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        saveFacebookProfileDetails(user,
                                                String.valueOf(vueUser.getId()));
                                    }
                                    if (VueLandingPageActivity.landingPageActivity != null) {
                                        VueLandingPageActivity.landingPageActivity
                                                .runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        if (mDialog != null) {
                                                            mDialog.dismiss();
                                                        }
                                                        if (!mFromDetailsFbShare
                                                                && loginSuccessFlag) {
                                                            loadRewardPoints();
                                                            showAisleSwipeHelp();
                                                        } else {
                                                            try {
                                                                mSocialIntegrationMainLayout
                                                                        .setVisibility(View.VISIBLE);
                                                                mTrendingbg
                                                                        .setVisibility(View.VISIBLE);
                                                            } catch (Exception e) {
                                                                e.printStackTrace();
                                                            }
                                                            Toast.makeText(
                                                                    VueLoginActivity.this,
                                                                    "Currently there is a problem with the server. Please try again later.",
                                                                    Toast.LENGTH_LONG)
                                                                    .show();
                                                        }
                                                    }
                                                });
                                    }
                                }
                            });
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
                                    getResources()
                                            .getString(
                                                    R.string.sidemenu_sub_option_Facebook));
                            resultIntent.putExtras(b);
                            setResult(
                                    VueConstants.INVITE_FRIENDS_LOGINACTIVITY_REQUEST_CODE,
                                    resultIntent);
                            showAisleSwipeHelp();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
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
                                    getResources()
                                            .getString(
                                                    R.string.sidemenu_sub_option_Facebook));
                            resultIntent.putExtras(b);
                            setResult(
                                    VueConstants.INVITE_FRIENDS_LOGINACTIVITY_REQUEST_CODE,
                                    resultIntent);
                            showAisleSwipeHelp();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (!mFromDetailsFbShare)
                        showAisleSwipeHelp();
                }
            }
        }).executeAsync();
        mDialog = new ProgressDialog(VueLoginActivity.this);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setMessage("Authenticating...");
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(false);
        mDialog.show();
    }
    
    private void saveFacebookProfileDetails(GraphUser user, String userId) {
        
        String location = "";
        try {
            if (user.getLocation() != null) {
                JSONObject jsonObject = user.getLocation().getInnerJSONObject();
                location = jsonObject
                        .getString(VueConstants.FACEBOOK_GRAPHIC_OBJECT_NAME_KEY);
            }
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
        VueUserProfile storedUserProfile = null;
        try {
            storedUserProfile = Utils.readUserProfileObjectFromFile(
                    VueApplication.getInstance(),
                    VueConstants.VUE_APP_USERPROFILEOBJECT__FILENAME);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        if (storedUserProfile == null
                || (storedUserProfile != null && !storedUserProfile
                        .isUserDetailsModified())) {
            JSONObject innerObject = user.getInnerJSONObject();
            String email;
            try {
                email = innerObject
                        .getString(VueConstants.FACEBOOK_GRAPHIC_OBJECT_EMAIL_KEY);
            } catch (Exception e) {
                email = user.getUsername();
                e.printStackTrace();
            }
            VueUserProfile vueUserProfile = new VueUserProfile(
                    VueConstants.FACEBOOK_USER_PROFILE_PICTURE_MAIN_URL
                            + user.getId()
                            + VueConstants.FACEBOOK_USER_PROFILE_PICTURE_SUB_URL,
                    email,
                    user.getName(),
                    user.getBirthday(),
                    user.getProperty(VueConstants.FACEBOOK_GRAPHIC_OBJECT_GENDER_KEY)
                            + "", location, false);
            try {
                Utils.writeUserProfileObjectToFile(VueLoginActivity.this,
                        VueConstants.VUE_APP_USERPROFILEOBJECT__FILENAME,
                        vueUserProfile);
                storedUserProfile = Utils.readUserProfileObjectFromFile(
                        VueApplication.getInstance(),
                        VueConstants.VUE_APP_USERPROFILEOBJECT__FILENAME);
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            refreshBezelMenu(
                    VueConstants.FACEBOOK_USER_PROFILE_PICTURE_MAIN_URL
                            + user.getId()
                            + VueConstants.FACEBOOK_USER_PROFILE_PICTURE_SUB_URL,
                    new FileCache(VueLoginActivity.this)
                            .getVueAppUserProfilePictureFile(VueConstants.USER_PROFILE_IMAGE_FILE_NAME));
            String mixpanelUserEmail = null;
            if (vueUserProfile.getUserEmail() != null) {
                mixpanelUserEmail = vueUserProfile.getUserEmail();
            } else {
                mixpanelUserEmail = user.getUsername();
            }
            mixpanel.identify(userId);
            people.identify(userId);
            SharedPreferences sharedPreferencesObj = VueApplication
                    .getInstance().getSharedPreferences(
                            VueConstants.SHAREDPREFERENCE_NAME, 0);
            people.setPushRegistrationId(sharedPreferencesObj.getString(
                    VueConstants.GCM_REGISTRATION_ID, null));
            people.set("$first_name", user.getFirstName());
            people.set("$last_name", user.getLastName());
            people.set("Gender", vueUserProfile.getUserGender());
            people.set("$email", mixpanelUserEmail);
            people.setOnce("no of aisles created", 0);
            people.setOnce("no of suggestions posted", 0);
            people.set("Current location", vueUserProfile.getUserLocation());
            people.setOnce("Joined On", new Date());
            people.set("loggedIn with", "Facebook");
            JSONObject nameTag = new JSONObject();
            try {
                // Set an "mp_name_tag" super property
                // for Streams if you find it useful.
                nameTag.put("mp_name_tag",
                        user.getFirstName() + " " + user.getLastName());
                mixpanel.registerSuperProperties(nameTag);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            
        }
    }
    
    private void updateUI(boolean fromOnActivityResult) {
        Session session = Session.getActiveSession();
        boolean fbloggedin = (session != null && session.isOpened());
        if (fbloggedin) {
            if (mFromDetailsFbShare) {
                try {
                    mSharedPreferencesObj = this.getSharedPreferences(
                            VueConstants.SHAREDPREFERENCE_NAME, 0);
                    SharedPreferences.Editor editor = mSharedPreferencesObj
                            .edit();
                    editor.putString(VueConstants.FACEBOOK_ACCESSTOKEN,
                            session.getAccessToken());
                    editor.putBoolean(VueConstants.VUE_LOGIN, true);
                    editor.putBoolean(VueConstants.FACEBOOK_LOGIN, true);
                    editor.commit();
                    ArrayList<clsShare> filePathList = mBundle
                            .getParcelableArrayList(VueConstants.FBPOST_IMAGEURLS);
                    shareToFacebook(filePathList,
                            mBundle.getString(VueConstants.FBPOST_TEXT));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                saveFBLoginDetails(session);
            }
        } else {
            if (fromOnActivityResult) {
                if (VueApplication.getInstance().mFBLoginFailureReason
                        .equals(getResources().getString(
                                R.string.facebook_session_expire_mesg))) {
                    showAlertToLoginWithFacebookApp(getResources().getString(
                            R.string.facebook_session_expire_mesg));
                }
                writeToSdcard("After Fb login failure: " + new Date() + "????"
                        + VueApplication.getInstance().mFBLoginFailureReason);
                JSONObject loginprops = new JSONObject();
                try {
                    loginprops.put("Login with", "Facebook");
                    loginprops.put("Failure Reason",
                            VueApplication.getInstance().mFBLoginFailureReason);
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
                mixpanel.track("Login Failed", loginprops);
            }
            if (mFromDetailsFbShare) {
                if (fromOnActivityResult) {
                    Toast.makeText(VueLoginActivity.this,
                            "Please login with Facebook to share.",
                            Toast.LENGTH_LONG);
                    showAisleSwipeHelp();
                }
            }
        }
    }
    
    private void shareToFacebook(ArrayList<clsShare> fileList,
            String articledesc) {
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
                            final File f = new File(fileList.get(i)
                                    .getFilepath());
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
                                    }
                                };
                                if (fileList.get(i).getImageUrl() != null) {
                                    @SuppressWarnings("unchecked")
                                    ImageRequest imagerequestObj = new ImageRequest(
                                            fileList.get(i).getImageUrl(),
                                            listener, 0, 0, null, errorListener);
                                    VueApplication.getInstance()
                                            .getRequestQueue()
                                            .add(imagerequestObj);
                                }
                            }
                            final int index = i;
                            VueLoginActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Bundle parameters = new Bundle(1);
                                    parameters.putString("message", articledesc
                                            + "");
                                    ParcelFileDescriptor descriptor = null;
                                    try {
                                        descriptor = ParcelFileDescriptor
                                                .open(new File(fileList.get(
                                                        index).getFilepath()),
                                                        ParcelFileDescriptor.MODE_READ_ONLY);
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                    parameters.putParcelable("picture",
                                            descriptor);
                                    Callback callback = new Request.Callback() {
                                        public void onCompleted(
                                                com.facebook.Response response) {
                                            if (index == fileList.size() - 1) {
                                                mFacebookProgressDialog
                                                        .dismiss();
                                                showPublishResult(
                                                        VueLoginActivity.this
                                                                .getString(R.string.photo_post),
                                                        response.getGraphObject(),
                                                        response.getError());
                                            }
                                        }
                                    };
                                    Session session = Session
                                            .getActiveSession();
                                    Request request = new Request(session,
                                            "me/photos", parameters,
                                            HttpMethod.POST, callback);
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
        // AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder( new
        // ContextThemeWrapper(getActivity(),R.style.AlertDialogCustom));
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                new ContextThemeWrapper(this, R.style.AppBaseTheme));
        alertDialogBuilder
                .setTitle(getResources().getString(R.string.app_name));
        alertDialogBuilder.setMessage(alertMessage);
        alertDialogBuilder.setPositiveButton(
                getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        
                        dialog.cancel();
                        
                    }
                });
        
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
    
    private void publishFeedDialog(String friend_uid, String friendname) {
        Bundle params = new Bundle();
        params.putString("to", friend_uid);
        params.putString("link", "https://www.facebook.com/vueitnow");
        JSONArray jsonArray = new JSONArray();
        JSONObject j = new JSONObject();
        try {
            j.put("name", "Get Vue");
            j.put("link",
                    "https://play.google.com/store/apps/details?id=com.lateralthoughts.vue");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        jsonArray.put(j);
        params.putString("actions", jsonArray.toString());
        VueApplication.getInstance().isPostingOnFriendsWallFlag = true;
        WebDialog feedDialog = (new WebDialog.FeedDialogBuilder(
                VueLoginActivity.this, Session.getActiveSession(), params))
                .setOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(Bundle values,
                            FacebookException error) {
                        if (error == null) {
                            final String postId = values.getString("post_id");
                            if (postId != null) {
                                Toast.makeText(VueLoginActivity.this,
                                        "Posted story, id: " + postId,
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(VueLoginActivity.this,
                                        "Publish cancelled", Toast.LENGTH_SHORT)
                                        .show();
                            }
                        } else if (error instanceof FacebookOperationCanceledException) {
                            Toast.makeText(VueLoginActivity.this,
                                    "Publish cancelled", Toast.LENGTH_SHORT)
                                    .show();
                        } else {
                            Toast.makeText(VueLoginActivity.this,
                                    "Error posting story", Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
                }).build();
        feedDialog.show();
        feedDialog.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface arg0) {
                showAisleSwipeHelp();
            }
        });
    }
    
    @Override
    public void onSignedFail() {
        if (mIsGplusButtonClicked) {
            JSONObject loginprops = new JSONObject();
            try {
                loginprops.put("Login with", "Facebook");
                loginprops.put("Failure Reason", "");
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            mixpanel.track("Login Failed", loginprops);
            writeToSdcard("After google+ failure login : " + new Date());
            SharedPreferences.Editor editor = mSharedPreferencesObj.edit();
            editor.putBoolean(VueConstants.GOOGLEPLUS_LOGIN, false);
            editor.commit();
            if (mGooglePlusProgressDialog != null
                    && mGooglePlusProgressDialog.isShowing())
                mGooglePlusProgressDialog.dismiss();
        }
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
                .parse("https://play.google.com/store/apps/details?id=com.lateralthoughts.vue"));
        // Set the target deep-link ID (for mobile use).
        builder.setContentDeepLinkId(
                getString(R.string.plus_example_deep_link_id), null, null, null);
        List<Person> googlefriendList = new ArrayList<Person>();
        if (personIndexList != null
                && VueLandingPageActivity.mGooglePlusFriendsDetailsList != null) {
            for (int i = 0; i < personIndexList.size(); i++) {
                googlefriendList
                        .add(VueLandingPageActivity.mGooglePlusFriendsDetailsList
                                .get(personIndexList.get(i))
                                .getGoogleplusFriend());
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
        if (Utils.appInstalledOrNot(VueConstants.GOOGLEPLUS_PACKAGE_NAME, this)) {
            try {
                startActivityForResult(
                        getInteractivePostIntent(plusClient, activity, post,
                                personIndexList), REQUEST_CODE_INTERACTIVE_POST);
            } catch (Exception e) {
                Toast.makeText(this, "Unable to invite friend",
                        Toast.LENGTH_LONG).show();
            }
        } else {
            if (mGooglePlusProgressDialog != null)
                if (mGooglePlusProgressDialog.isShowing())
                    mGooglePlusProgressDialog.dismiss();
            showAlertMessageForAppInstalation("Google+",
                    VueConstants.GOOGLEPLUS_PACKAGE_NAME);
        }
        
    }
    
    @Override
    public void onPersonLoaded(ConnectionResult connectionresult,
            final Person person) {
        VueUser vueUser = null;
        if (connectionresult.getErrorCode() == ConnectionResult.SUCCESS) {
            mSharedPreferencesObj = this.getSharedPreferences(
                    VueConstants.SHAREDPREFERENCE_NAME, 0);
            mDialog = new ProgressDialog(VueLoginActivity.this);
            mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mDialog.setMessage("Authenticating...");
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.setCancelable(false);
            mDialog.show();
            VueUserManager userManager = VueUserManager.getUserManager();
            String googleplusId = null;
            try {
                if (mSharedPreferencesObj.getString(
                        VueConstants.GOOGLEPLUS_USER_EMAIL, null) != null) {
                    googleplusId = mSharedPreferencesObj.getString(
                            VueConstants.GOOGLEPLUS_USER_EMAIL, null);
                    googleplusId = googleplusId.replace(".", "");
                }
            } catch (Exception e) {
                googleplusId = mSharedPreferencesObj.getString(
                        VueConstants.GOOGLEPLUS_USER_EMAIL, null);
            }
            JSONObject loginprops = new JSONObject();
            try {
                loginprops.put("Email", mSharedPreferencesObj.getString(
                        VueConstants.GOOGLEPLUS_USER_EMAIL, null));
                loginprops.put("Login with", "Google+");
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            mixpanel.track("Login Success", loginprops);
            vueUser = new VueUser(null, mSharedPreferencesObj.getString(
                    VueConstants.GOOGLEPLUS_USER_EMAIL, null),
                    person.getDisplayName(), "", System.currentTimeMillis(),
                    Utils.getDeviceId(), VueUser.DEFAULT_FACEBOOK_ID,
                    googleplusId, person.getImage().getUrl());
            writeToSdcard("Before Server login for Google+ : " + new Date());
            userManager.googlePlusAuthenticationWithServer(person.getImage()
                    .getUrl(), vueUser, new UserUpdateCallback() {
                @Override
                public void onUserUpdated(VueUser user,
                        final boolean loginSuccessFlag) {
                    if (user != null) {
                        VueApplication.getInstance().getInstalledApplications(
                                VueApplication.getInstance());
                        writeToSdcard("After server Succefull login for Google+ : "
                                + new Date());
                        try {
                            Utils.writeUserObjectToFile(VueLoginActivity.this,
                                    VueConstants.VUE_APP_USEROBJECT__FILENAME,
                                    user);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        saveGooglePlusUserProfile(person,
                                String.valueOf(user.getId()));
                    }
                    if (VueLandingPageActivity.landingPageActivity != null) {
                        VueLandingPageActivity.landingPageActivity
                                .runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (mDialog != null) {
                                            mDialog.dismiss();
                                        }
                                        if (!mFromDetailsFbShare
                                                && loginSuccessFlag) {
                                            loadRewardPoints();
                                            showAisleSwipeHelp();
                                        } else {
                                            try {
                                                mSocialIntegrationMainLayout
                                                        .setVisibility(View.VISIBLE);
                                                mTrendingbg
                                                        .setVisibility(View.VISIBLE);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            Toast.makeText(
                                                    VueLoginActivity.this,
                                                    "Currently there is a problem with the server. Please try again later.",
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                    }
                    
                }
            });
        }
    }
    
    private void saveGooglePlusUserProfile(Person person, String userId) {
        VueUserProfile storedUserProfile = new VueUserProfile(person.getImage()
                .getUrl(), mSharedPreferencesObj.getString(
                VueConstants.GOOGLEPLUS_USER_EMAIL, null),
                person.getDisplayName(), null, null, null, false);
        try {
            Utils.writeUserProfileObjectToFile(this,
                    VueConstants.VUE_APP_USERPROFILEOBJECT__FILENAME,
                    storedUserProfile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        refreshBezelMenu(
                person.getImage().getUrl(),
                new FileCache(VueLoginActivity.this)
                        .getVueAppUserProfilePictureFile(VueConstants.USER_PROFILE_IMAGE_FILE_NAME));
        
        mixpanel.identify(userId);
        people.identify(userId);
        SharedPreferences sharedPreferencesObj = VueApplication.getInstance()
                .getSharedPreferences(VueConstants.SHAREDPREFERENCE_NAME, 0);
        people.setPushRegistrationId(sharedPreferencesObj.getString(
                VueConstants.GCM_REGISTRATION_ID, null));
        people.set("$first_name", storedUserProfile.getUserName());
        people.set("$last_name", "");
        people.set("Gender", storedUserProfile.getUserGender());
        people.set("$email", storedUserProfile.getUserEmail());
        people.setOnce("no of aisles created", 0);
        people.setOnce("no of suggestions posted", 0);
        people.set("Current location", storedUserProfile.getUserLocation());
        people.setOnce("Joined On", new Date());
        people.set("loggedIn with", "Google+");
        JSONObject nameTag = new JSONObject();
        try {
            // Set an "mp_name_tag" super property
            // for Streams if you find it useful.
            nameTag.put("mp_name_tag", storedUserProfile.getUserName());
            mixpanel.registerSuperProperties(nameTag);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mIsAlreadyLoggedInWithVue) {
                super.onBackPressed();
            }
        }
        return false;
        
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void refreshBezelMenu(final String imageUrl, final File filePath) {
        Response.Listener listener = new Response.Listener<Bitmap>() {
            
            @Override
            public void onResponse(Bitmap bmp) {
                Utils.saveBitmap(bmp, filePath);
                getProfileImageChangeListenor();
            }
        };
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            
            @Override
            public void onErrorResponse(VolleyError arg0) {
            }
        };
        
        ImageRequest imagerequestObj = new ImageRequest(imageUrl, listener, 0,
                0, null, errorListener);
        VueApplication.getInstance().getRequestQueue().add(imagerequestObj);
        
    }
    
    public void getProfileImageChangeListenor() {
        Intent i = new Intent("RefreshBezelMenuReciver");
        VueApplication.getInstance().sendBroadcast(i);
    }
    
    private void showAisleSwipeHelp() {
        if (mDialog != null) {
            mDialog.dismiss();
        }
        if (mShowAisleSwipeHelpLayoutFlag) {
            finish();
            // Intent swipeHelpIntent = new Intent(this, HelpOnTrending.class);
            // startActivity(swipeHelpIntent);
        } else {
            finish();
        }
    }
    
    private void loadRewardPoints() {
        int userPointsExecuteTime = 60000;
        // load lazily after completion of all trending inital data
        // need to improve this code so that it should start exactly after
        // completion of trending ailse download.
        new Handler().postDelayed(new Runnable() {
            
            @Override
            public void run() {
                new Thread(new Runnable() {
                    
                    @Override
                    public void run() {
                        VueTrendingAislesDataModel
                                .getInstance(VueApplication.getInstance())
                                .getNetworkHandler().getMyAislesPoints();
                    }
                });
                
            }
        }, userPointsExecuteTime);
    }
    
    private void showAlertToLoginWithFacebookApp(String message) {
        //AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder( new ContextThemeWrapper(getActivity(),R.style.AlertDialogCustom));
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder( new ContextThemeWrapper(this,R.style.AppBaseTheme));
        alertDialogBuilder.setTitle(getResources().getString(R.string.app_name));
       alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setPositiveButton(VueApplication.getInstance().getString(R.string.ok),new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog,int id) {
               
                   dialog.cancel();
                  
              }
             });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
    
    private void writeToSdcard(String message) {
        if (!Logger.sWrightToSdCard) {
            return;
        }
        String path = Environment.getExternalStorageDirectory().toString();
        File dir = new File(path + "/vueLoginTimes/");
        if (!dir.isDirectory()) {
            dir.mkdir();
        }
        File file = new File(dir, "/" + "vueLoginTimes_"
                + (Calendar.getInstance().get(Calendar.MONTH) + 1) + "-"
                + Calendar.getInstance().get(Calendar.DATE) + "_"
                + Calendar.getInstance().get(Calendar.YEAR) + ".txt");
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(
                    new FileWriter(file, true)));
            out.write("\n" + message + "\n");
            out.flush();
            out.close();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
}
