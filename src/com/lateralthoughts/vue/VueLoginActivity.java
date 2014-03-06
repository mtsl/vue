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
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.FragmentActivity;
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
import com.lateralthoughts.vue.logging.Logger;
import com.lateralthoughts.vue.user.VueUser;
import com.lateralthoughts.vue.user.VueUserManager;
import com.lateralthoughts.vue.user.VueUserProfile;
import com.lateralthoughts.vue.utils.FileCache;
import com.lateralthoughts.vue.utils.Utils;
import com.lateralthoughts.vue.utils.clsShare;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

public class VueLoginActivity extends FragmentActivity {
    
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
    private final List<String> PUBLISH_PERMISSIONS = Arrays
            .asList("publish_actions");
    private ProgressDialog mFacebookProgressDialog;
    private final String PENDING_ACTION_BUNDLE_KEY = VueApplication
            .getInstance().getString(R.string.pendingActionBundleKey);
    private PendingAction mPendingAction = PendingAction.NONE;
    private boolean mIsAlreadyLoggedInWithVue = false;
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
        }
        if (mGuestUserMessage != null) {
            Toast.makeText(this, mGuestUserMessage, Toast.LENGTH_LONG).show();
        }
        // Facebook Invite friend
        if (mFbFriendId != null) {
            mSocialIntegrationMainLayout.setVisibility(View.GONE);
            mTrendingbg.setVisibility(View.GONE);
            publishFeedDialog(mFbFriendId, mFbFriendName);
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
                boolean fbloginfalg = mSharedPreferencesObj.getBoolean(
                        VueConstants.FACEBOOK_LOGIN, false);
                if (mFromInviteFriends != null) {
                    if (mFromInviteFriends.equals(VueConstants.GOOGLEPLUS)) {
                        mDontCallUserInfoChangesMethod = true;
                        fblog_in_buttonlayout.setVisibility(View.GONE);
                    }
                } else if (mFromBezelMenuLogin) {
                    if (fbloginfalg) {
                        mDontCallUserInfoChangesMethod = true;
                        fblog_in_buttonlayout.setVisibility(View.GONE);
                    }
                }
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
            ResolveInfo resolveInfo = VueApplication.getInstance().mVueApplicationContext
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
        try {
            mUiHelper.onActivityResult(requestCode, resultCode, data);
            if (!mDontCallUserInfoChangesMethod) {
                updateUI(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                        loginprops.put(
                                "Email",
                                user.getProperty(VueConstants.FACEBOOK_GRAPHIC_OBJECT_EMAIL_KEY)
                                        + "");
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
                                        new Handler().postDelayed(
                                                new Runnable() {
                                                    
                                                    @Override
                                                    public void run() {
                                                        VueApplication
                                                                .getInstance()
                                                                .getInstalledApplications(
                                                                        VueApplication
                                                                                .getInstance());
                                                    }
                                                }, 500);
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
                    saveFacebookProfileDetails(user);
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
    
    private void saveFacebookProfileDetails(GraphUser user) {
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
            
            mixpanel.identify(storedUserProfile.getUserEmail());
            people.identify(vueUserProfile.getUserEmail());
            people.set("$first_name", user.getFirstName());
            people.set("$last_name", user.getLastName());
            people.set("Gender", vueUserProfile.getUserGender());
            people.set("$email", vueUserProfile.getUserEmail());
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
        refreshBezelMenu(
                VueConstants.FACEBOOK_USER_PROFILE_PICTURE_MAIN_URL
                        + user.getId()
                        + VueConstants.FACEBOOK_USER_PROFILE_PICTURE_SUB_URL,
                new FileCache(VueLoginActivity.this)
                        .getVueAppUserProfilePictureFile(VueConstants.USER_PROFILE_IMAGE_FILE_NAME));
    }
    
    private void updateUI(boolean fromOnActivityResult) {
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
        } else {
            if (fromOnActivityResult) {
                writeToSdcard("After Fb login failure: " + new Date());
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
                showAisleSwipeHelp();
            }
        });
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
