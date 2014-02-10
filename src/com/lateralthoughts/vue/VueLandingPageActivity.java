package com.lateralthoughts.vue;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.flurry.android.FlurryAgent;
import com.lateralthoughts.vue.AisleManager.ImageAddedCallback;
import com.lateralthoughts.vue.AisleManager.ImageUploadCallback;
import com.lateralthoughts.vue.ShareDialog.ShareViaVueClickedListner;
import com.lateralthoughts.vue.connectivity.DataBaseManager;
import com.lateralthoughts.vue.connectivity.VueConnectivityManager;
import com.lateralthoughts.vue.domain.AisleBookmark;
import com.lateralthoughts.vue.domain.VueImage;
import com.lateralthoughts.vue.parser.Parser;
import com.lateralthoughts.vue.pendingaisles.PendingAisles;
import com.lateralthoughts.vue.ui.NotifyProgress;
import com.lateralthoughts.vue.ui.StackViews;
import com.lateralthoughts.vue.ui.ViewInfo;
import com.lateralthoughts.vue.utils.BitmapLoaderUtils;
import com.lateralthoughts.vue.utils.ExceptionHandler;
import com.lateralthoughts.vue.utils.FbGPlusDetails;
import com.lateralthoughts.vue.utils.FileCache;
import com.lateralthoughts.vue.utils.GetOtherSourceImagesTask;
import com.lateralthoughts.vue.utils.OtherSourceImageDetails;
import com.lateralthoughts.vue.utils.Utils;
import com.lateralthoughts.vue.utils.clsShare;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

public class VueLandingPageActivity extends Activity implements
        SearchView.OnQueryTextListener, SearchView.OnCloseListener {
    
    private static final int DELAY_TIME = 500;
    public static List<FbGPlusDetails> mGooglePlusFriendsDetailsList = null;
    private ProgressDialog mProgressDialog;
    private FrameLayout mVueLandingKeyboardCancel, mVueLandingKeyboardDone;
    private View mVueLandingActionbarView;
    private OtherSourcesDialog mOtherSourcesDialog = null;
    private boolean mAddImageToAisleLayoutClickedAFlag = false;
    public static String mOtherSourceImagePath = null;
    public static String mOtherSourceImageUrl = null;
    public static int mOtherSourceImageWidth = 0;
    public static String mOtherSourceImageDetailsUrl = null;
    public static String mOtherSourceImageStore = null;
    public static int mOtherSourceImageHeight = 0;
    public static String mOtherSourceImageOccasion = null;
    public static String mOtherSourceImageLookingFor = null;
    public static String mOtherSourceImageCategory = null;
    public static String mOtherSourceAddImageAisleId = null;
    private static final String TRENDING_SCREEN_VISITORS = "Trending_Screen_Visitors";
    public static Activity landingPageActivity = null;
    private com.lateralthoughts.vue.VueListFragment mSlidListFrag;
    private ProgressDialog mPd;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private FrameLayout mContent_frame2;
    private Fragment mLandingAilsesFrag;
    EditText mSearchEdit;
    private Handler mHandler;
    private String mCatName;
    boolean mFromDialog;
    public static String mLandingScreenName = null;
    private boolean mHideDefaultActionbar = false;
    private LandingScreenTitleReceiver mLandingScreenTitleReceiver = null;
    public static boolean mIsMyAilseCallEnable = false;
    private MixpanelAPI mixpanel;
    private MixpanelAPI.People people;
    
    // SCREEN REFRESH TIME THRESHOLD IN MINUTES.
    public static final long SCREEN_REFRESH_TIME = 2 * 60;// 120 mins.
    public static long mLastRefreshTime;
    private ShareDialog mShare = null;
    private boolean mShowSwipeHelp = false;
    
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mixpanel = MixpanelAPI.getInstance(this,
                VueApplication.getInstance().MIXPANEL_TOKEN);
        mLandingScreenTitleReceiver = new LandingScreenTitleReceiver();
        IntentFilter ifiltercategory = new IntentFilter(
                VueConstants.LANDING_SCREEN_RECEIVER);
        VueApplication.getInstance().registerReceiver(
                mLandingScreenTitleReceiver, ifiltercategory);
        setContentView(R.layout.vue_landing_main);
        landingPageActivity = this;
        initialize();
        mContent_frame2 = (FrameLayout) findViewById(R.id.content_frame2);
        mSlidListFrag = (VueListFragment) getFragmentManager()
                .findFragmentById(R.id.listfrag);
        mPd = new ProgressDialog(this);
        mPd.setMessage("Loading...");
        mPd.setCancelable(false);
        getActionBar().setTitle(
                getString(R.string.sidemenu_option_Trending_Aisles));
        VueApplication.getInstance().mLaunchTime = System.currentTimeMillis();
        VueApplication.getInstance().mLastRecordedTime = System
                .currentTimeMillis();
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        invalidateOptionsMenu();
        mVueLandingActionbarView = LayoutInflater.from(this).inflate(
                R.layout.vue_landing_custom_actionbar, null);
        mVueLandingKeyboardCancel = (FrameLayout) mVueLandingActionbarView
                .findViewById(R.id.vue_landing_keyboard_cancel);
        mVueLandingKeyboardDone = (FrameLayout) mVueLandingActionbarView
                .findViewById(R.id.vue_landing_keyboard_done);
        mVueLandingKeyboardDone.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                addImageToExistingAisle(mOtherSourceAddImageAisleId);
                mOtherSourceAddImageAisleId = null;
                ((VueLandingAislesFragment) mLandingAilsesFrag)
                        .notifyAdapters();
                mHideDefaultActionbar = false;
                invalidateOptionsMenu();
            }
        });
        mVueLandingKeyboardCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mOtherSourceAddImageAisleId = null;
                mOtherSourceImagePath = null;
                mOtherSourceImageUrl = null;
                mOtherSourceImageWidth = 0;
                mOtherSourceImageHeight = 0;
                mOtherSourceImageDetailsUrl = null;
                mOtherSourceImageStore = null;
                mOtherSourceImageLookingFor = null;
                mOtherSourceImageCategory = null;
                mOtherSourceImageOccasion = null;
                ((VueLandingAislesFragment) mLandingAilsesFrag)
                        .notifyAdapters();
                mHideDefaultActionbar = false;
                invalidateOptionsMenu();
            }
        });
        SharedPreferences sharedPreferencesObj = this.getSharedPreferences(
                VueConstants.SHAREDPREFERENCE_NAME, 0);
        boolean isHelpOpend = sharedPreferencesObj.getBoolean(
                VueConstants.HELP_SCREEN_ACCES, false);
        if (!isHelpOpend) {
            Editor editor = sharedPreferencesObj.edit();
            editor.putLong(VueConstants.APP_FIRST_TIME_OPENED_TIME,
                    System.currentTimeMillis());
            editor.commit();
            Intent intent = new Intent(this, Help.class);
            intent.putExtra(VueConstants.HELP_KEY,
                    VueConstants.HelpSCREEN_FROM_LANDING);
            startActivity(intent);
        } else {
            sharedPreferencesObj = this.getSharedPreferences(
                    VueConstants.SHAREDPREFERENCE_NAME, 0);
            boolean aisleSwipe = sharedPreferencesObj.getBoolean(
                    VueConstants.AISLE_SWIPE, false);
            if (!aisleSwipe) {
                long hours = Utils.dateDifference(sharedPreferencesObj.getLong(
                        VueConstants.APP_FIRST_TIME_OPENED_TIME, 0));
                if (hours != -1 && hours >= 48) {
                    // mShowSwipeHelp = true;
                    mShowSwipeHelp = false;
                    Editor editor = sharedPreferencesObj.edit();
                    editor.putBoolean(VueConstants.AISLE_SWIPE, true);
                    editor.commit();
                }
            }
            VueUser storedVueUser = null;
            try {
                storedVueUser = Utils.readUserObjectFromFile(this,
                        VueConstants.VUE_APP_USEROBJECT__FILENAME);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            PackageInfo packageInfo;
            try {
                packageInfo = this.getPackageManager().getPackageInfo(
                        VueLandingPageActivity.this.getPackageName(), 0);
                int versionCode = packageInfo.versionCode;
                if (storedVueUser != null) {
                    sharedPreferencesObj = this.getSharedPreferences(
                            VueConstants.SHAREDPREFERENCE_NAME, 0);
                    long preVersionCode = sharedPreferencesObj.getLong(
                            VueConstants.VERSION_CODE_CHANGE, 0);
                    if (versionCode != preVersionCode) {
                        Editor editor = sharedPreferencesObj.edit();
                        editor.putLong(VueConstants.VERSION_CODE_CHANGE,
                                versionCode);
                        editor.commit();
                        if (storedVueUser != null
                                && storedVueUser.getGooglePlusId().equals(
                                        VueUser.DEFAULT_GOOGLEPLUS_ID)
                                && storedVueUser.getFacebookId().equals(
                                        VueUser.DEFAULT_FACEBOOK_ID)) {
                            mixpanel.identify(storedVueUser.getEmail());
                            people = mixpanel.getPeople();
                            people.identify(storedVueUser.getEmail());
                            JSONObject nameTag = new JSONObject();
                            try {
                                // Set an "mp_name_tag" super property
                                // for Streams if you find it useful.
                                // TODO: Check how it works.
                                nameTag.put("mp_name_tag",
                                        storedVueUser.getFirstName() + " "
                                                + storedVueUser.getLastName());
                                mixpanel.registerSuperProperties(nameTag);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            // TODO: start the LoginActivity
                            Intent i = new Intent(this, VueLoginActivity.class);
                            Bundle b = new Bundle();
                            b.putBoolean(VueConstants.CANCEL_BTN_DISABLE_FLAG,
                                    true);
                            b.putString(VueConstants.FROM_INVITEFRIENDS, null);
                            b.putBoolean(
                                    VueConstants.FBLOGIN_FROM_DETAILS_SHARE,
                                    false);
                            b.putBoolean(VueConstants.FROM_BEZELMENU_LOGIN,
                                    false);
                            b.putBoolean(
                                    VueConstants.SHOW_AISLE_SWIPE_HELP_LAYOUT_FLAG,
                                    mShowSwipeHelp);
                            b.putString(
                                    VueConstants.GUEST_LOGIN_MESSAGE,
                                    getResources().getString(
                                            R.string.guest_login_message));
                            i.putExtras(b);
                            startActivity(i);
                        }
                    } else {
                        if (mShowSwipeHelp) {
                            /*
                             * Intent swipeHelpIntent = new Intent(this,
                             * SwipeHelp.class); startActivity(swipeHelpIntent);
                             */
                        }
                    }
                    VueApplication.getInstance().setmUserInitials(
                            storedVueUser.getFirstName());
                    VueApplication.getInstance().setmUserId(
                            storedVueUser.getId());
                    VueApplication.getInstance().setmUserEmail(
                            storedVueUser.getEmail());
                    VueApplication.getInstance().setmUserName(
                            storedVueUser.getFirstName() + " "
                                    + storedVueUser.getLastName());
                } else {
                    showLogInDialog(false);
                }
                // check whether user is guest or not.
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                clearDataEntryData();
                handleSendText(intent, true);
            } else if (type.startsWith("image/")) {
                clearDataEntryData();
                handleSendImage(intent, true);
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                clearDataEntryData();
                handleSendMultipleImages(intent, true);
            }
        }
        loadDetailsScreenForNotificationClick(getIntent().getExtras());
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        VueApplication.getInstance().saveTrendingRefreshTime(0);
        try {
            if (mLandingScreenTitleReceiver != null) {
                VueApplication.getInstance().unregisterReceiver(
                        mLandingScreenTitleReceiver);
            }
        } catch (Exception e) {
        }
    }
    
    private void initialize() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        // set a custom shadow that overlays the main content when the drawer
        // opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
        // set up the drawer's list view with items and click listener
        
        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setBackgroundDrawable(
                getResources().getDrawable(R.drawable.actionbar_bg));
        
        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
        mDrawerLayout, /* DrawerLayout object */
        R.drawable.ic_navigation_drawer, /*
                                          * nav drawer image to replace 'Up'
                                          * caret
                                          */
        R.string.drawer_open, /* "open drawer" description for accessibility */
        R.string.drawer_close /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                invalidateOptionsMenu();
            }
            
            @SuppressLint("CutPasteId")
            public void onDrawerOpened(View drawerView) {
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mLandingAilsesFrag = new VueLandingAislesFragment();
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, mLandingAilsesFrag).commit();
        mDrawerLayout.setFocusableInTouchMode(false);
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
    
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        } else if (item.getItemId() == R.id.menu_create_aisle) {
            if (mOtherSourceImagePath == null) {
                mixpanel.track("Create Aisle Selected", null);
                FlurryAgent.logEvent("Create_Aisle_Button_Click");
                Intent intent = new Intent(VueLandingPageActivity.this,
                        CreateAisleSelectionActivity.class);
                Utils.putFromDetailsScreenToDataentryCreateAisleScreenPreferenceFlag(
                        VueLandingPageActivity.this, false);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                if (!CreateAisleSelectionActivity.isActivityShowing) {
                    CreateAisleSelectionActivity.isActivityShowing = true;
                    startActivity(intent);
                }
            } else {
                showDiscardOtherAppImageDialog();
            }
            
        } else if (item.getItemId() == R.id.menu_pending_aisle) {
            startActivity(new Intent(this, PendingAisles.class));
        }
        // Handle your other action bar items...
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.landing_actionbar, menu);
        getActionBar().setHomeButtonEnabled(true);
        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean isdrawOpen = mDrawerLayout.isDrawerOpen(mContent_frame2);
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setDisplayShowCustomEnabled(false);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setDisplayShowHomeEnabled(true);
        getActionBar().setCustomView(null);
        getActionBar().setDisplayShowTitleEnabled(true);
        menu.findItem(R.id.menu_pending_aisle).setVisible(false);
        if (isdrawOpen) {
            // set menu search visibility to true when backend functionality is
            // ready
            menu.findItem(R.id.menu_search).setVisible(false);
            menu.findItem(R.id.menu_create_aisle).setVisible(false);
            menu.findItem(R.id.menu_pending_aisle).setVisible(false);
        } else {
            if (mHideDefaultActionbar) {
                getActionBar().setDisplayShowTitleEnabled(false);
                getActionBar().setDisplayHomeAsUpEnabled(false);
                getActionBar().setDisplayShowCustomEnabled(true);
                getActionBar().setDisplayShowHomeEnabled(false);
                getActionBar().setCustomView(mVueLandingActionbarView);
                menu.findItem(R.id.menu_search).setVisible(false);
                menu.findItem(R.id.menu_create_aisle).setVisible(false);
                menu.findItem(R.id.menu_pending_aisle).setVisible(false);
            } else {
                menu.findItem(R.id.menu_search).setVisible(false);
                menu.findItem(R.id.menu_search).collapseActionView();
                menu.findItem(R.id.menu_create_aisle).setVisible(true);
                // TODO: UNCOMMENT THIS CODE WHEN NO IMAGE AISLE FEATURE
                // ENABLED.
                // menu.findItem(R.id.menu_pending_aisle).setVisible(true);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }
    
    @Override
    protected void onStart() {
        // TODO: 500 millis consumes this code
        new Thread(new Runnable() {
            
            @Override
            public void run() {
                FlurryAgent.onStartSession(VueLandingPageActivity.this,
                        Utils.FLURRY_APP_KEY);
                FlurryAgent.logEvent(TRENDING_SCREEN_VISITORS);
                VueUser vueUser = null;
                try {
                    vueUser = Utils.readUserObjectFromFile(
                            VueLandingPageActivity.this,
                            VueConstants.VUE_APP_USEROBJECT__FILENAME);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (vueUser != null) {
                    Map<String, String> articleParams = new HashMap<String, String>();
                    if (vueUser.getFacebookId().equals(
                            VueUser.DEFAULT_FACEBOOK_ID)
                            && vueUser.getGooglePlusId().equals(
                                    VueUser.DEFAULT_GOOGLEPLUS_ID)) {
                        articleParams.put("User_Status", "Un_Registered");
                    } else {
                        articleParams.put("User_Status", "Registered");
                        if ((!vueUser.getFacebookId().equals(
                                VueUser.DEFAULT_FACEBOOK_ID))
                                && (!vueUser.getGooglePlusId().equals(
                                        VueUser.DEFAULT_GOOGLEPLUS_ID))) {
                            articleParams.put("Registered_Source",
                                    "Registered with FB and GPLUS");
                            
                        } else if ((!vueUser.getGooglePlusId().equals(
                                VueUser.DEFAULT_GOOGLEPLUS_ID))) {
                            articleParams.put("Registered_Source",
                                    "Registered with GPLUS");
                        } else if ((!vueUser.getFacebookId().equals(
                                VueUser.DEFAULT_FACEBOOK_ID))) {
                            articleParams.put("Registered_Source",
                                    "Registered with FB");
                        }
                    }
                    FlurryAgent.logEvent("Rigestered_Users", articleParams);
                    FlurryAgent
                            .logEvent("Login_Time_Ends", articleParams, true);
                }
                FlurryAgent.onPageView();
                
            }
        }).start();
        
        mixpanel.flush();
        super.onStart();
        
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        FlurryAgent.onEndSession(this);
        VueUser vueUser = null;
        try {
            vueUser = Utils.readUserObjectFromFile(this,
                    VueConstants.VUE_APP_USEROBJECT__FILENAME);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (vueUser != null) {
            Map<String, String> articleParams = new HashMap<String, String>();
            if (vueUser.getFacebookId().equals(VueUser.DEFAULT_FACEBOOK_ID)
                    && vueUser.getGooglePlusId().equals(
                            VueUser.DEFAULT_GOOGLEPLUS_ID)) {
                articleParams.put("User_Status", "Un_Registered");
            } else {
                articleParams.put("User_Status", "Registered");
                if ((!vueUser.getFacebookId().equals(
                        VueUser.DEFAULT_FACEBOOK_ID))
                        && (!vueUser.getGooglePlusId().equals(
                                VueUser.DEFAULT_GOOGLEPLUS_ID))) {
                    articleParams.put("Registered_Source",
                            "Registered with FB and GPLUS");
                    
                } else if ((!vueUser.getGooglePlusId().equals(
                        VueUser.DEFAULT_GOOGLEPLUS_ID))) {
                    articleParams.put("Registered_Source",
                            "Registered with GPLUS");
                } else if ((!vueUser.getFacebookId().equals(
                        VueUser.DEFAULT_FACEBOOK_ID))) {
                    articleParams
                            .put("Registered_Source", "Registered with FB");
                }
            }
            FlurryAgent.logEvent("Rigestered_Users", articleParams);
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VueConstants.INVITE_FRIENDS_LOGINACTIVITY_REQUEST_CODE
                && resultCode == VueConstants.INVITE_FRIENDS_LOGINACTIVITY_REQUEST_CODE) {
            if (data != null) {
                if (data.getStringExtra(VueConstants.INVITE_FRIENDS_LOGINACTIVITY_BUNDLE_STRING_KEY) != null) {
                    mSlidListFrag
                            .getFriendsList(data
                                    .getStringExtra(VueConstants.INVITE_FRIENDS_LOGINACTIVITY_BUNDLE_STRING_KEY));
                }
            }
        } else {
            if (mShare != null && mShare.mShareIntentCalled) {
                mShare.mShareIntentCalled = false;
                mShare.dismisDialog();
            }
        }
    }
    
    private void handleSendText(Intent intent, boolean fromOnCreateMethodFlag) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            String sourceUrl = Utils.getUrlFromString(sharedText);
            if (Utils.isLoadDataentryScreenFlag(this)) {
                Utils.setLoadDataentryScreenFlag(this, false);
                if (Utils
                        .getFromDetailsScreenToDataentryCreateAisleScreenPreferenceFlag(VueLandingPageActivity.this)) {
                    Utils.putFromDetailsScreenToDataentryCreateAisleScreenPreferenceFlag(
                            VueLandingPageActivity.this, false);
                    if (VueTrendingAislesDataModel.getInstance(this)
                            .getAisleCount() > 0) {
                        Intent i = new Intent(this,
                                AisleDetailsViewActivity.class);
                        Bundle b = new Bundle();
                        b.putString(VueConstants.FROM_OTHER_SOURCES_URL,
                                sourceUrl);
                        b.putBoolean(VueConstants.FROM_OTHER_SOURCES_FLAG, true);
                        i.putExtras(b);
                        startActivity(i);
                    } else {
                        getImagesFromUrl(sourceUrl);
                    }
                } else {
                    Intent i = new Intent(this, DataEntryActivity.class);
                    Bundle b = new Bundle();
                    b.putString(VueConstants.FROM_OTHER_SOURCES_URL, sourceUrl);
                    b.putBoolean(VueConstants.FROM_OTHER_SOURCES_FLAG, true);
                    i.putExtras(b);
                    startActivity(i);
                }
            } else {
                getImagesFromUrl(sourceUrl);
            }
        }
    }
    
    private void handleSendImage(Intent intent, boolean fromOnCreateMethodFlag) {
        String sourceUrl = null;
        try {
            sourceUrl = intent.getStringExtra(Intent.EXTRA_TEXT);
        } catch (Exception e) {
        }
        Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
            if (Utils.isLoadDataentryScreenFlag(this)) {
                Utils.setLoadDataentryScreenFlag(this, false);
                if (Utils
                        .getFromDetailsScreenToDataentryCreateAisleScreenPreferenceFlag(VueLandingPageActivity.this)) {
                    Utils.putFromDetailsScreenToDataentryCreateAisleScreenPreferenceFlag(
                            VueLandingPageActivity.this, false);
                    if (VueTrendingAislesDataModel.getInstance(this)
                            .getAisleCount() > 0) {
                        Intent i = new Intent(this,
                                AisleDetailsViewActivity.class);
                        Bundle b = new Bundle();
                        ArrayList<Uri> imageUrisList = new ArrayList<Uri>();
                        imageUrisList.add(imageUri);
                        b.putParcelableArrayList(
                                VueConstants.FROM_OTHER_SOURCES_IMAGE_URIS,
                                imageUrisList);
                        b.putBoolean(VueConstants.FROM_OTHER_SOURCES_FLAG, true);
                        if (sourceUrl != null) {
                            b.putString(VueConstants.FROM_OTHER_SOURCES_URL,
                                    sourceUrl);
                        }
                        i.putExtras(b);
                        startActivity(i);
                    } else {
                        ArrayList<Uri> imageUriList = new ArrayList<Uri>();
                        imageUriList.add(imageUri);
                        showOtherSourcesGridview(
                                convertImageUrisToOtherSourceImageDetails(imageUriList),
                                sourceUrl);
                    }
                } else {
                    Intent i = new Intent(this, DataEntryActivity.class);
                    Bundle b = new Bundle();
                    ArrayList<Uri> imageUrisList = new ArrayList<Uri>();
                    imageUrisList.add(imageUri);
                    b.putParcelableArrayList(
                            VueConstants.FROM_OTHER_SOURCES_IMAGE_URIS,
                            imageUrisList);
                    b.putBoolean(VueConstants.FROM_OTHER_SOURCES_FLAG, true);
                    if (sourceUrl != null) {
                        b.putString(VueConstants.FROM_OTHER_SOURCES_URL,
                                sourceUrl);
                    }
                    i.putExtras(b);
                    startActivity(i);
                }
            } else {
                ArrayList<Uri> imageUriList = new ArrayList<Uri>();
                imageUriList.add(imageUri);
                showOtherSourcesGridview(
                        convertImageUrisToOtherSourceImageDetails(imageUriList),
                        sourceUrl);
            }
        }
    }
    
    private void handleSendMultipleImages(Intent intent,
            boolean fromOnCreateMethodFlag) {
        String sourceUrl = null;
        try {
            sourceUrl = intent.getStringExtra(Intent.EXTRA_TEXT);
        } catch (Exception e) {
        }
        ArrayList<Uri> imageUris = intent
                .getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (imageUris != null) {
            if (Utils.isLoadDataentryScreenFlag(this)) {
                Utils.setLoadDataentryScreenFlag(this, false);
                if (Utils
                        .getFromDetailsScreenToDataentryCreateAisleScreenPreferenceFlag(VueLandingPageActivity.this)) {
                    Utils.putFromDetailsScreenToDataentryCreateAisleScreenPreferenceFlag(
                            VueLandingPageActivity.this, false);
                    if (VueTrendingAislesDataModel.getInstance(this)
                            .getAisleCount() > 0) {
                        Intent i = new Intent(this,
                                AisleDetailsViewActivity.class);
                        Bundle b = new Bundle();
                        b.putParcelableArrayList(
                                VueConstants.FROM_OTHER_SOURCES_IMAGE_URIS,
                                imageUris);
                        b.putBoolean(VueConstants.FROM_OTHER_SOURCES_FLAG, true);
                        if (sourceUrl != null) {
                            b.putString(VueConstants.FROM_OTHER_SOURCES_URL,
                                    sourceUrl);
                        }
                        i.putExtras(b);
                        startActivity(i);
                    } else {
                        showOtherSourcesGridview(
                                convertImageUrisToOtherSourceImageDetails(imageUris),
                                sourceUrl);
                    }
                } else {
                    Intent i = new Intent(this, DataEntryActivity.class);
                    Bundle b = new Bundle();
                    
                    b.putParcelableArrayList(
                            VueConstants.FROM_OTHER_SOURCES_IMAGE_URIS,
                            imageUris);
                    b.putBoolean(VueConstants.FROM_OTHER_SOURCES_FLAG, true);
                    if (sourceUrl != null) {
                        b.putString(VueConstants.FROM_OTHER_SOURCES_URL,
                                sourceUrl);
                    }
                    i.putExtras(b);
                    startActivity(i);
                }
            } else {
                showOtherSourcesGridview(
                        convertImageUrisToOtherSourceImageDetails(imageUris),
                        sourceUrl);
            }
        }
        
    }
    
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            
            if (mDrawerLayout.isDrawerOpen(mContent_frame2)) {
                
                if (!mSlidListFrag.listener.onBackPressed()) {
                    mDrawerLayout.closeDrawer(mContent_frame2);
                }
            } else if (StackViews.getInstance().getStackCount() > 0) {
                showDefaultActionbar();
                final ViewInfo viewInfo = StackViews.getInstance().pull();
                if (viewInfo != null) {
                    getActionBar().setTitle(viewInfo.mVueName);
                    mLandingScreenName = viewInfo.mVueName;
                    showPreviousScreen(viewInfo.mVueName);
                } else {
                    if (VueApplication.getInstance().isUserSwipeAisle) {
                        SharedPreferences sharedPreferencesObj = this
                                .getSharedPreferences(
                                        VueConstants.SHAREDPREFERENCE_NAME, 0);
                        boolean isAisleAlreadySwipped = sharedPreferencesObj
                                .getBoolean(VueConstants.AISLE_SWIPE, false);
                        if (!isAisleAlreadySwipped) {
                            Editor editor = sharedPreferencesObj.edit();
                            editor.putBoolean(VueConstants.AISLE_SWIPE, true);
                            editor.commit();
                        }
                    }
                    super.onBackPressed();
                }
            } else {
                
                CancelNotification(this,
                        VueConstants.AISLE_INFO_UPLOAD_NOTIFICATION_ID);
                CancelNotification(this,
                        VueConstants.IMAGE_DELETE_NOTIFICATION_ID);
                CancelNotification(this,
                        VueConstants.CHANGE_USER_NOTIFICATION_ID);
                FileCache fileCache = new FileCache(
                        VueApplication.getInstance());
                fileCache.clearVueAppResizedPictures();
                fileCache.clearTwoDaysOldPictures();
                mOtherSourceImagePath = null;
                mOtherSourceImageLookingFor = null;
                mOtherSourceImageCategory = null;
                mOtherSourceImageOccasion = null;
                mOtherSourceImageUrl = null;
                mOtherSourceImageWidth = 0;
                mOtherSourceImageHeight = 0;
                mOtherSourceImageDetailsUrl = null;
                mOtherSourceImageStore = null;
                super.onBackPressed();
            }
        }
        return false;
    }
    
    private void showLogInDialog(boolean hideCancelButton) {
        Intent i = new Intent(this, VueLoginActivity.class);
        Bundle b = new Bundle();
        b.putBoolean(VueConstants.SHOW_AISLE_SWIPE_HELP_LAYOUT_FLAG,
                mShowSwipeHelp);
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
        mSlidListFrag.setEditTextVisible(false);
        // ShareViaVue...
        if (VueApplication.getInstance().mShareViaVueClickedFlag) {
            VueApplication.getInstance().mShareViaVueClickedFlag = false;
            if (VueApplication.getInstance().mShareViaVueClickedImageId != null) {
                String imageId = VueApplication.getInstance().mShareViaVueClickedImageId;
                String aisleId = VueApplication.getInstance().mShareViaVueClickedAisleId;
                VueApplication.getInstance().mShareViaVueClickedAisleId = null;
                VueApplication.getInstance().mShareViaVueClickedImageId = null;
                AisleImageDetails aisleImageDetails = VueTrendingAislesDataModel
                        .getInstance(this).getAisleImageForImageId(imageId,
                                aisleId, true);
                if (aisleImageDetails != null) {
                    String originalUrl = aisleImageDetails.mImageUrl;
                    String sourceUrl = aisleImageDetails.mDetailsUrl;
                    int width = aisleImageDetails.mAvailableWidth;
                    int height = aisleImageDetails.mAvailableHeight;
                    int widthandHeightMultipliedValue = width * height;
                    OtherSourceImageDetails otherSourceImageDetails = new OtherSourceImageDetails();
                    otherSourceImageDetails.setHeight(height);
                    otherSourceImageDetails.setWidth(width);
                    otherSourceImageDetails
                            .setWidthHeightMultipliedValue(widthandHeightMultipliedValue);
                    otherSourceImageDetails.setOriginUrl(originalUrl);
                    ArrayList<OtherSourceImageDetails> imagesList = new ArrayList<OtherSourceImageDetails>();
                    imagesList.add(otherSourceImageDetails);
                    showOtherSourcesGridview(imagesList, sourceUrl);
                }
            }
        }
        if (mLandingAilsesFrag != null) {
            ((VueLandingAislesFragment) mLandingAilsesFrag).notifyAdapters();
        }
        if (VueApplication.getInstance().mNewViewSelection) {
            boolean fromDialog = false;
            VueApplication.getInstance().mNewViewSelection = false;
            showCategory(VueApplication.getInstance().mNewlySelectedView,
                    fromDialog);
            
        }
        new Handler().postDelayed(new Runnable() {
            
            @Override
            public void run() {
                Rect rect = new Rect();
                Window window = VueLandingPageActivity.this.getWindow();
                window.getDecorView().getWindowVisibleDisplayFrame(rect);
                int statusBarHeight = rect.top;
                VueApplication.getInstance().setmStatusBarHeight(
                        statusBarHeight);
                
            }
        }, DELAY_TIME);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        if (VueConnectivityManager.isNetworkConnected(this)) {
            SharedPreferences sharedPreferencesObj = this.getSharedPreferences(
                    VueConstants.SHAREDPREFERENCE_NAME, 0);
            mLastRefreshTime = sharedPreferencesObj.getLong(
                    VueConstants.SCREEN_REFRESH_TIME, 0);
            if (mLastRefreshTime != 0) {
                long currentTime = System.currentTimeMillis();
                long currentMins = Utils.getMins(currentTime);
                long difMins = currentMins - mLastRefreshTime;
                if (difMins > VueLandingPageActivity.SCREEN_REFRESH_TIME) {
                    // Clean the data and fetch from server again.
                    Toast.makeText(this, "Syncing with server",
                            Toast.LENGTH_SHORT).show();
                    StackViews.getInstance().clearStack();
                    VueTrendingAislesDataModel
                            .getInstance(VueApplication.getInstance())
                            .getNetworkHandler().clearList(null);
                    VueTrendingAislesDataModel.getInstance(
                            VueApplication.getInstance())
                            .getFreshDataFromServer();
                    mLandingScreenName = getString(R.string.sidemenu_option_Trending_Aisles);
                }
            }
        }
    }
    
    @Override
    public void onPause() {
        
        long time_in_mins = Utils.getMins(System.currentTimeMillis());
        VueApplication.getInstance().saveTrendingRefreshTime(time_in_mins);
        super.onPause();
        
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // Get intent, action and MIME type
        String action = intent.getAction();
        String type = intent.getType();
        
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleSendText(intent, false); // Handle text being sent
            } else if (type.startsWith("image/")) {
                handleSendImage(intent, false); // Handle single image being
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                handleSendMultipleImages(intent, false); // Handle multiple
                                                         // images
            }
        } else {
            loadDetailsScreenForNotificationClick(intent.getExtras());
        }
    }
    
    public void showCategory(final String catName, boolean fromDialog) {
        mDrawerLayout.closeDrawer(mContent_frame2);
        mCatName = catName;
        mFromDialog = fromDialog;
        if (mHandler == null) {
            mHandler = new Handler();
        }
        // close the drawer before call for new views.
        mHandler.postDelayed(r, 100);
    }
    
    Runnable r = new Runnable() {
        @Override
        public void run() {
            if (!mDrawerLayout.isDrawerOpen(mContent_frame2)) {
                callForNewView(mCatName, mFromDialog);
            } else {
                mHandler.postDelayed(r, 100);
            }
        }
    };
    
    private void callForNewView(final String catName, boolean fromDialog) {
        if (mLandingScreenName != null
                && mLandingScreenName.equalsIgnoreCase(catName)) {
            return;
        }
        ViewInfo viewInfo = new ViewInfo();
        viewInfo.mVueName = getActionBar().getTitle().toString();
        viewInfo.mPosition = ((VueLandingAislesFragment) mLandingAilsesFrag)
                .getListPosition();
        viewInfo.mOffset = VueTrendingAislesDataModel
                .getInstance(VueLandingPageActivity.this).getNetworkHandler()
                .getmOffset();
        StackViews.getInstance().push(viewInfo);
        boolean loadMore = false;
        boolean fromServer = true;
        
        if (catName
                .equalsIgnoreCase(getString(R.string.sidemenu_sub_option_My_Aisles))) {
            mIsMyAilseCallEnable = true;
            if (VueConnectivityManager.isNetworkConnected(VueApplication
                    .getInstance())) {
                fromServer = true;
            } else {
                fromServer = false;
            }
            
            VueTrendingAislesDataModel
                    .getInstance(VueApplication.getInstance())
                    .getNetworkHandler()
                    .requestAislesByUser(fromServer, new ProgresStatus(),
                            catName);
        } else if (catName.trim().equalsIgnoreCase(
                getString(R.string.sidemenu_option_Trending_Aisles))) {
            mLandingScreenName = catName;
            if (fromDialog) {
                fromServer = false;
                loadMore = false;
                getTrendingAislesFromDb(
                        getString(R.string.sidemenu_option_Trending_Aisles),
                        fromServer, loadMore);
            } else {
                VueTrendingAislesDataModel
                        .getInstance(VueApplication.getInstance())
                        .getNetworkHandler().makeOffseZero();
                VueTrendingAislesDataModel.getInstance(
                        VueApplication.getInstance()).clearAisles();
                AisleWindowContentFactory.getInstance(
                        VueApplication.getInstance()).clearObjectsInUse();
                VueTrendingAislesDataModel.getInstance(
                        VueApplication.getInstance()).dataObserver();
                loadMore = true;
                VueTrendingAislesDataModel
                        .getInstance(VueApplication.getInstance())
                        .getNetworkHandler()
                        .loadTrendingAisle(loadMore, fromServer,
                                new ProgresStatus(), catName);
            }
        } else if (catName
                .equals(getString(R.string.sidemenu_sub_option_Bookmarks))) {
            
            getBookmarkedAisles(catName);
            
        } else if (catName
                .equals(getString(R.string.sidemenu_sub_option_Recently_Viewed_Aisles))) {
            ArrayList<AisleWindowContent> windowContent = DataBaseManager
                    .getInstance(this).getRecentlyViewedAisles();
            if (windowContent.size() > 0) {
                VueTrendingAislesDataModel.getInstance(this).clearAisles();
                AisleWindowContentFactory.getInstance(
                        VueApplication.getInstance()).clearObjectsInUse();
                for (AisleWindowContent content : windowContent) {
                    if (content.getImageList() != null
                            && content.getImageList().size() > 0) {
                        VueTrendingAislesDataModel.getInstance(this)
                                .addItemToList(content.getAisleId(), content);
                    }
                }
                getActionBar()
                        .setTitle(
                                getString(R.string.sidemenu_sub_option_Recently_Viewed_Aisles));
                mLandingScreenName = getString(R.string.sidemenu_sub_option_Recently_Viewed_Aisles);
                VueTrendingAislesDataModel.getInstance(
                        VueApplication.getInstance()).dataObserver();
            } else {
                Toast.makeText(this, "No Recently Viewed aisles",
                        Toast.LENGTH_LONG).show();
                StackViews.getInstance().pull();
            }
        } else {
            
        }
        JSONObject categorySelectedProps = new JSONObject();
        try {
            categorySelectedProps.put("Category Selected", catName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mixpanel.track("Bezel Category Selected", categorySelectedProps);
        FlurryAgent.logEvent(catName);
    }
    
    private void getBookmarkedAisles(String screenName) {
        
        ArrayList<AisleWindowContent> windowContent = null;
        ArrayList<AisleBookmark> bookmarkedAisles = DataBaseManager
                .getInstance(VueLandingPageActivity.this)
                .getBookmarkAisleIdsList();
        String[] bookmarked = new String[bookmarkedAisles.size()];
        for (int i = 0; i < bookmarkedAisles.size(); i++) {
            bookmarked[i] = Long.toString(bookmarkedAisles.get(i).getAisleId());
        }
        if (windowContent == null) {
            windowContent = new ArrayList<AisleWindowContent>();
        }
        DataBaseManager.getInstance(VueLandingPageActivity.this)
                .resetDbParams();
        ArrayList<AisleWindowContent> windowContentTemp = DataBaseManager
                .getInstance(VueLandingPageActivity.this).getAislesFromDB(
                        bookmarked, true);
        for (AisleWindowContent w : windowContentTemp) {
            // TODO: HERE THE LIST SHOULD NOT BE NULL BUT WE GOT NULL SOME TIMES
            // LIST NEED TO CHECK THIS CODE BY SURENDRA.
            if (w.getImageList() != null) {
                windowContent.add(w);
            }
        }
        if (windowContent != null && windowContent.size() > 0) {
            getActionBar().setTitle(screenName);
            mLandingScreenName = screenName;
            // ((VueLandingAislesFragment) mLandingAilsesFrag).clearBitmaps();
            VueTrendingAislesDataModel.getInstance(this).clearAisles();
            AisleWindowContentFactory.getInstance(VueApplication.getInstance())
                    .clearObjectsInUse();
            for (AisleWindowContent content : windowContent) {
                VueTrendingAislesDataModel.getInstance(this).addItemToList(
                        content.getAisleId(), content);
            }
            VueTrendingAislesDataModel
                    .getInstance(VueApplication.getInstance()).dataObserver();
        } else {
            Toast.makeText(this, "No Bookmarked aisles", Toast.LENGTH_LONG)
                    .show();
            StackViews.getInstance().pull();
        }
        
    }
    
    private void showPreviousScreen(String screenName) {
        boolean fromServer = false;
        boolean loadMore = false;
        // ((VueLandingAislesFragment) mLandingAilsesFrag).clearBitmaps();
        if (screenName
                .equalsIgnoreCase(getString(R.string.sidemenu_option_Trending_Aisles))) {
            getTrendingAislesFromDb(screenName, fromServer, loadMore);
            
        } else if (screenName
                .equalsIgnoreCase(getString(R.string.sidemenu_sub_option_My_Aisles))) {
            VueTrendingAislesDataModel
                    .getInstance(VueLandingPageActivity.this)
                    .getNetworkHandler()
                    .requestAislesByUser(fromServer, new ProgresStatus(),
                            screenName);
        } else if (screenName
                .equalsIgnoreCase(getString(R.string.sidemenu_sub_option_Bookmarks))) {
            
            getBookmarkedAisles(screenName);
        } else if (screenName
                .equalsIgnoreCase(getString(R.string.sidemenu_sub_option_Recently_Viewed_Aisles))) {
            
            ArrayList<AisleWindowContent> windowContent = DataBaseManager
                    .getInstance(this).getRecentlyViewedAisles();
            
            if (windowContent.size() > 0) {
                VueTrendingAislesDataModel.getInstance(this).clearAisles();
                AisleWindowContentFactory.getInstance(
                        VueApplication.getInstance()).clearObjectsInUse();
                for (AisleWindowContent content : windowContent) {
                    VueTrendingAislesDataModel.getInstance(this).addItemToList(
                            content.getAisleId(), content);
                }
                
                VueTrendingAislesDataModel.getInstance(
                        VueApplication.getInstance()).dataObserver();
                
            }
        } else {
        }
    }
    
    class ProgresStatus implements NotifyProgress {
        @Override
        public void showProgress() {
            mPd.show();
        }
        
        @Override
        public void dismissProgress(boolean fromWhere) {
            mPd.dismiss();
            if (fromWhere) {
                // mFragment.moveListToPosition(0);
            } else {
                // mFragment.moveListToPosition(mCurentScreenPosition);
            }
            if (mLandingAilsesFrag != null) {
                ((VueLandingAislesFragment) mLandingAilsesFrag)
                        .notifyAdapters();
            }
        }
        
        @Override
        public boolean isAlreadyDownloaed(String category) {
            boolean isDowoaded = StackViews.getInstance().categoryCheck(
                    category);
            return isDowoaded;
        }
        
        @Override
        public void clearBrowsers() {
            // ((VueLandingAislesFragment) mLandingAilsesFrag).clearBitmaps();
        }
    }
    
    public void showDiscardOtherAppImageDialog() {
        final Dialog dialog = new Dialog(this, R.style.Theme_Dialog_Translucent);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.vue_popup);
        final TextView noButton = (TextView) dialog.findViewById(R.id.nobutton);
        TextView yesButton = (TextView) dialog.findViewById(R.id.okbutton);
        TextView messagetext = (TextView) dialog.findViewById(R.id.messagetext);
        messagetext.setText(getResources().getString(
                R.string.discard_othersource_image_mesg));
        yesButton.setText("Yes");
        noButton.setText("No");
        yesButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mOtherSourceImagePath = null;
                mOtherSourceImageLookingFor = null;
                mOtherSourceImageCategory = null;
                mOtherSourceImageOccasion = null;
                mOtherSourceImageUrl = null;
                mOtherSourceImageWidth = 0;
                mOtherSourceImageHeight = 0;
                mOtherSourceImageDetailsUrl = null;
                mOtherSourceImageStore = null;
                dialog.dismiss();
            }
        });
        noButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
    
    public void showScreenSelectionForOtherSource(final String imagePath,
            final String imageUrl, final int imageWidth, final int imageHeight,
            final String detailsUrl, final String store) {
        mOtherSourceImagePath = imagePath;
        mOtherSourceImageHeight = imageHeight;
        mOtherSourceImageWidth = imageWidth;
        mOtherSourceImageUrl = imageUrl;
        mOtherSourceImageDetailsUrl = detailsUrl;
        mOtherSourceImageStore = store;
        mAddImageToAisleLayoutClickedAFlag = false;
        final Dialog dialog = new Dialog(this, R.style.Theme_Dialog_Translucent);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.other_source_landing_screen_selection);
        RelativeLayout addImageToTrendingLayout = (RelativeLayout) dialog
                .findViewById(R.id.landingothersourcedialogaddimageto_trending_buttonlayout);
        RelativeLayout addImageToBookmarksLayout = (RelativeLayout) dialog
                .findViewById(R.id.landingothersourcedialogaddimageto_bookmarks_buttonlayout);
        RelativeLayout addImageToMyAisleLayout = (RelativeLayout) dialog
                .findViewById(R.id.landingothersourcedialogaddimageto_myaisles_buttonlayout);
        RelativeLayout createAisleLayout = (RelativeLayout) dialog
                .findViewById(R.id.landingothersourcedialogcreateaisle_buttonlayout);
        addImageToTrendingLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dialog.dismiss();
                showCategory(
                        getString(R.string.sidemenu_option_Trending_Aisles),
                        true);
                Toast.makeText(VueLandingPageActivity.this,
                        "Choose the aisle to add to", Toast.LENGTH_LONG).show();
                mAddImageToAisleLayoutClickedAFlag = true;
            }
        });
        addImageToBookmarksLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dialog.dismiss();
                showCategory(getString(R.string.sidemenu_sub_option_Bookmarks),
                        true);
                Toast.makeText(VueLandingPageActivity.this,
                        "Choose the aisle to add to", Toast.LENGTH_LONG).show();
                mAddImageToAisleLayoutClickedAFlag = true;
            }
        });
        addImageToMyAisleLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                
                dialog.dismiss();
                showCategory(getString(R.string.sidemenu_sub_option_My_Aisles),
                        true);
                Toast.makeText(VueLandingPageActivity.this,
                        "Choose the aisle to add to", Toast.LENGTH_LONG).show();
                mAddImageToAisleLayoutClickedAFlag = true;
            }
        });
        createAisleLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String lookingfor = VueLandingPageActivity.mOtherSourceImageLookingFor;
                String category = VueLandingPageActivity.mOtherSourceImageCategory;
                String occasion = VueLandingPageActivity.mOtherSourceImageOccasion;
                dialog.dismiss();
                Intent intent = new Intent(VueLandingPageActivity.this,
                        DataEntryActivity.class);
                Bundle b = new Bundle();
                b.putString(
                        VueConstants.CREATE_AISLE_CAMERA_GALLERY_IMAGE_PATH_BUNDLE_KEY,
                        imagePath);
                b.putString(
                        VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_IMAGEURL,
                        imageUrl);
                b.putString(
                        VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_IMAGE_DETAILSURL,
                        detailsUrl);
                b.putString(
                        VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_FINDAT,
                        detailsUrl);
                b.putString(
                        VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_IMAGE_STORE,
                        store);
                b.putInt(
                        VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_IMAGE_WIDTH,
                        imageWidth);
                b.putInt(
                        VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_IMAGE_HEIGHT,
                        imageHeight);
                b.putString(
                        VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_LOOKINGFOR,
                        lookingfor);
                b.putString(
                        VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_OCCASION,
                        occasion);
                b.putString(
                        VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_CATEGORY,
                        category);
                intent.putExtras(b);
                startActivity(intent);
            }
        });
        dialog.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface arg0) {
                if (!mAddImageToAisleLayoutClickedAFlag) {
                    mOtherSourceImagePath = null;
                    mOtherSourceImageLookingFor = null;
                    mOtherSourceImageCategory = null;
                    mOtherSourceImageOccasion = null;
                    mOtherSourceImageUrl = null;
                    mOtherSourceImageWidth = 0;
                    mOtherSourceImageHeight = 0;
                    mOtherSourceImageDetailsUrl = null;
                    mOtherSourceImageStore = null;
                }
                mAddImageToAisleLayoutClickedAFlag = false;
            }
        });
        dialog.show();
    }
    
    public void showOtherSourcesGridview(
            ArrayList<OtherSourceImageDetails> imagesList, String sourceUrl) {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
        if (imagesList != null && imagesList.size() > 0) {
            if (mOtherSourcesDialog == null) {
                mOtherSourcesDialog = new OtherSourcesDialog(this);
            }
            mOtherSourcesDialog.showImageDailog(imagesList, true, sourceUrl);
        } else {
            Toast.makeText(this, "Sorry, there are no images.",
                    Toast.LENGTH_LONG).show();
        }
    }
    
    private void getImagesFromUrl(String sourceUrl) {
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog.show(this, "", "Please wait...");
        }
        GetOtherSourceImagesTask getImagesTask = new GetOtherSourceImagesTask(
                sourceUrl, this, true);
        getImagesTask.execute();
    }
    
    public void CancelNotification(Context ctx, int notifyId) {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) ctx
                .getSystemService(ns);
        nMgr.cancel(notifyId);
    }
    
    private ArrayList<OtherSourceImageDetails> convertImageUrisToOtherSourceImageDetails(
            ArrayList<Uri> imageUriList) {
        ArrayList<OtherSourceImageDetails> otherSourcesImageDetailsList = new ArrayList<OtherSourceImageDetails>();
        for (int i = 0; i < imageUriList.size(); i++) {
            int width = 0, height = 0;
            try {
                InputStream is = new FileInputStream(Utils.getPath(
                        imageUriList.get(i), VueLandingPageActivity.this));
                BitmapFactory.Options o = new BitmapFactory.Options();
                o.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(is, null, o);
                is.close();
                width = o.outWidth;
                height = o.outHeight;
            } catch (Exception e) {
                e.printStackTrace();
            }
            OtherSourceImageDetails otherSourceImageDetails = new OtherSourceImageDetails(
                    null, null, null, width, height, imageUriList.get(i), width
                            * height);
            otherSourcesImageDetailsList.add(otherSourceImageDetails);
        }
        return otherSourcesImageDetailsList;
    }
    
    private void clearDataEntryData() {
        Utils.putDataentryAddImageAisleFlag(VueLandingPageActivity.this, false);
        Utils.putDataentryTopAddImageAisleFlag(VueLandingPageActivity.this,
                false);
        Utils.putDataentryTopAddImageAisleLookingFor(
                VueLandingPageActivity.this, null);
        Utils.putDataentryTopAddImageAisleCategory(VueLandingPageActivity.this,
                null);
        Utils.putDataentryTopAddImageAisleOccasion(VueLandingPageActivity.this,
                null);
        Utils.putDataentryTopAddImageAisleDescription(
                VueLandingPageActivity.this, null);
        Utils.putTouchToChnageImagePosition(VueLandingPageActivity.this, -1);
        Utils.putTouchToChnageImageTempPosition(VueLandingPageActivity.this, -1);
        Utils.putTouchToChnageImageFlag(VueLandingPageActivity.this, false);
        ArrayList<DataentryImage> mAisleImagePathList = null;
        try {
            mAisleImagePathList = Utils.readAisleImagePathListFromFile(
                    VueLandingPageActivity.this,
                    VueConstants.AISLE_IMAGE_PATH_LIST_FILE_NAME);
            mAisleImagePathList.clear();
            Utils.writeAisleImagePathListToFile(VueLandingPageActivity.this,
                    VueConstants.AISLE_IMAGE_PATH_LIST_FILE_NAME,
                    mAisleImagePathList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        FileCache fileCache = new FileCache(VueApplication.getInstance());
        fileCache.clearVueAppResizedPictures();
    }
    
    private void getTrendingAislesFromDb(String screenName, boolean fromServer,
            boolean loadMore) {
        
        VueTrendingAislesDataModel.getInstance(VueApplication.getInstance()).loadOnRequest = false;
        
        VueTrendingAislesDataModel.getInstance(VueApplication.getInstance()).mIsFromDb = true;
        VueTrendingAislesDataModel.getInstance(VueLandingPageActivity.this)
                .clearContent();
        
        if (!fromServer)
            DataBaseManager.getInstance(VueApplication.getInstance())
                    .resetDbParams();
        
        VueTrendingAislesDataModel
                .getInstance(VueLandingPageActivity.this)
                .getNetworkHandler()
                .reqestByCategory(screenName, new ProgresStatus(), fromServer,
                        loadMore, screenName);
    }
    
    private void addImageToExistingAisle(String aisleId) {
        if (mOtherSourceImagePath != null) {
            final VueImage image = new VueImage();
            image.setDetailsUrl(mOtherSourceImageDetailsUrl);
            image.setHeight(mOtherSourceImageHeight);
            image.setWidth(mOtherSourceImageWidth);
            image.setImageUrl(mOtherSourceImageUrl);
            image.setStore(mOtherSourceImageStore);
            image.setTitle("Android Test"); // TODO By Krishna
            image.setOwnerUserId(VueApplication.getInstance().getmUserId());
            image.setOwnerAisleId(Long.valueOf(aisleId));
            final String offlineImageId = String.valueOf(System
                    .currentTimeMillis());
            // Camera or Gallery...
            if (mOtherSourceImageUrl == null) {
                VueTrendingAislesDataModel
                        .getInstance(VueApplication.getInstance())
                        .getNetworkHandler()
                        .requestForUploadImage(new File(mOtherSourceImagePath),
                                new ImageUploadCallback() {
                                    @Override
                                    public void onImageUploaded(String imageUrl) {
                                        if (imageUrl != null) {
                                            image.setImageUrl(imageUrl);
                                            VueTrendingAislesDataModel
                                                    .getInstance(
                                                            VueApplication
                                                                    .getInstance())
                                                    .getNetworkHandler()
                                                    .requestForAddImage(
                                                            null,
                                                            true,
                                                            offlineImageId,
                                                            image,
                                                            new ImageAddedCallback() {
                                                                
                                                                @Override
                                                                public void onImageAdded(
                                                                        String aisleId,
                                                                        String imageId,
                                                                        boolean fromDetailScreen) {
                                                                    JSONObject imageUploadProps = new JSONObject();
                                                                    AisleWindowContent aisleWindowContent = VueTrendingAislesDataModel
                                                                            .getInstance(
                                                                                    VueLandingPageActivity.this)
                                                                            .getAisleAt(
                                                                                    aisleId);
                                                                    if (aisleWindowContent != null) {
                                                                        VueUser storedVueUser = null;
                                                                        try {
                                                                            storedVueUser = Utils
                                                                                    .readUserObjectFromFile(
                                                                                            VueLandingPageActivity.this,
                                                                                            VueConstants.VUE_APP_USEROBJECT__FILENAME);
                                                                        } catch (Exception e2) {
                                                                            e2.printStackTrace();
                                                                        }
                                                                        if (storedVueUser != null) {
                                                                            if (String
                                                                                    .valueOf(
                                                                                            storedVueUser
                                                                                                    .getId())
                                                                                    .equals(aisleWindowContent
                                                                                            .getAisleContext().mUserId)) {
                                                                                try {
                                                                                    imageUploadProps
                                                                                            .put("isOwnerOfAisle",
                                                                                                    true);
                                                                                } catch (JSONException e) {
                                                                                    e.printStackTrace();
                                                                                }
                                                                            } else {
                                                                                try {
                                                                                    imageUploadProps
                                                                                            .put("isOwnerOfAisle",
                                                                                                    false);
                                                                                } catch (JSONException e) {
                                                                                    e.printStackTrace();
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                    
                                                                    try {
                                                                        imageUploadProps
                                                                                .put("AisleId",
                                                                                        aisleId);
                                                                        imageUploadProps
                                                                                .put("imageId",
                                                                                        imageId);
                                                                        
                                                                    } catch (JSONException e) {
                                                                        
                                                                        e.printStackTrace();
                                                                    }
                                                                    mixpanel.track(
                                                                            "Added Image To Existing Aisle",
                                                                            imageUploadProps);
                                                                    
                                                                }
                                                            });
                                        }
                                    }
                                });
            } else {
                image.setImageUrl(mOtherSourceImageUrl);
                VueTrendingAislesDataModel
                        .getInstance(VueApplication.getInstance())
                        .getNetworkHandler()
                        .requestForAddImage(null, true, offlineImageId, image,
                                new ImageAddedCallback() {
                                    
                                    @Override
                                    public void onImageAdded(String aisleId,
                                            String imageId,
                                            boolean fromDetailScreen) {
                                        JSONObject imageUploadProps = new JSONObject();
                                        AisleWindowContent aisleWindowContent = VueTrendingAislesDataModel
                                                .getInstance(
                                                        VueLandingPageActivity.this)
                                                .getAisleAt(aisleId);
                                        if (aisleWindowContent != null) {
                                            VueUser storedVueUser = null;
                                            try {
                                                storedVueUser = Utils
                                                        .readUserObjectFromFile(
                                                                VueLandingPageActivity.this,
                                                                VueConstants.VUE_APP_USEROBJECT__FILENAME);
                                            } catch (Exception e2) {
                                                e2.printStackTrace();
                                            }
                                            if (storedVueUser != null) {
                                                if (String
                                                        .valueOf(
                                                                storedVueUser
                                                                        .getId())
                                                        .equals(aisleWindowContent
                                                                .getAisleContext().mUserId)) {
                                                    try {
                                                        imageUploadProps
                                                                .put("isOwnerOfAisle",
                                                                        true);
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                } else {
                                                    try {
                                                        imageUploadProps
                                                                .put("isOwnerOfAisle",
                                                                        false);
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }
                                        }
                                        
                                        try {
                                            imageUploadProps.put("AisleId",
                                                    aisleId);
                                            imageUploadProps.put("imageId",
                                                    imageId);
                                            
                                        } catch (JSONException e) {
                                            
                                            e.printStackTrace();
                                        }
                                        
                                        mixpanel.track(
                                                "Added Image To Existing Aisle",
                                                imageUploadProps);
                                    }
                                });
            }
            addImageToAisle(aisleId, mOtherSourceImagePath,
                    mOtherSourceImageUrl, mOtherSourceImageWidth,
                    mOtherSourceImageHeight, mOtherSourceImageDetailsUrl,
                    mOtherSourceImageStore, offlineImageId);
            mOtherSourceImagePath = null;
            mOtherSourceImageUrl = null;
            mOtherSourceImageWidth = 0;
            mOtherSourceImageHeight = 0;
            mOtherSourceImageDetailsUrl = null;
            mOtherSourceImageStore = null;
            mOtherSourceImageLookingFor = null;
            mOtherSourceImageCategory = null;
            mOtherSourceImageOccasion = null;
        }
    }
    
    private void addImageToAisle(String aisleId, String imagePath,
            String imageUrl, int imageWidth, int imageHeight,
            String detailsUrl, String store, String imageId) {
        boolean isImageFromLocalSystem = false;
        if (imageUrl == null) {
            isImageFromLocalSystem = true;
        }
        
        FileCache fileCache = new FileCache(this);
        File f = null;
        if (imageUrl != null) {
            f = fileCache.getFile(imageUrl);
        } else {
            imageUrl = imagePath;
            f = fileCache.getFile(imagePath);
        }
        File sourceFile = new File(imagePath);
        Bitmap bmp = BitmapLoaderUtils.getInstance().decodeFile(sourceFile,
                VueApplication.getInstance().mScreenHeight,
                VueApplication.getInstance().getVueDetailsCardWidth(),
                Utils.DETAILS_SCREEN);
        Utils.saveBitmap(bmp, f);
        addAisleToWindow(aisleId, imagePath, imageUrl, imageWidth, imageHeight,
                detailsUrl, store, imageId, isImageFromLocalSystem);
    }
    
    private void addAisleToWindow(String aisleId, String imgPath,
            String imageUrl, int imageWidth, int imageHeight,
            String detailsUrl, String store, String imageId,
            boolean isImageFromLocalSystem) {
        addAisleToContentWindow(aisleId, imgPath, imageUrl, imageWidth,
                imageHeight, "title", detailsUrl, store, imageId,
                isImageFromLocalSystem);
        
    }
    
    private void addAisleToContentWindow(String aisleId, String imagePath,
            String imageUrl, int imageWidth, int imageHeight, String title,
            String detailsUrl, String store, String imageId,
            boolean isImageFromLocalSystem) {
        Utils.sIsAisleChanged = true;
        Utils.mChangeAilseId = aisleId;
        
        AisleWindowContent aisleItem = VueTrendingAislesDataModel.getInstance(
                VueApplication.getInstance()).getAisleFromList(
                VueTrendingAislesDataModel.getInstance(
                        VueApplication.getInstance()).getAisleAt(aisleId));
        if (aisleItem != null
                && VueApplication.getInstance().getmUserId() != null) {
            AisleImageDetails imgDetails = new AisleImageDetails();
            imgDetails.mAvailableHeight = imageHeight;
            imgDetails.mAvailableWidth = imageWidth;
            
            if (imgDetails.mAvailableHeight < aisleItem
                    .getBestHeightForWindow()) {
                aisleItem.setBestHeightForWindow(imgDetails.mAvailableHeight);
                
            }
            imgDetails.mTitle = title;
            imgDetails.mImageUrl = imageUrl;
            imgDetails.mDetailsUrl = detailsUrl;
            imgDetails.mId = imageId; // offline imageid
            imgDetails.mIsFromLocalSystem = isImageFromLocalSystem;
            imgDetails.mStore = store;
            imgDetails.mTrendingImageHeight = imgDetails.mAvailableHeight;
            imgDetails.mTrendingImageWidth = imgDetails.mAvailableWidth;
            imgDetails.mOwnerAisleId = aisleItem.getAisleId();
            imgDetails.mOwnerUserId = Long.toString(VueApplication
                    .getInstance().getmUserId());
            aisleItem.getImageList().add(imgDetails);
            aisleItem.addAisleContent(aisleItem.getAisleContext(),
                    aisleItem.getImageList());
            int bestHeight = Utils.modifyHeightForDetailsView(aisleItem
                    .getImageList());
            aisleItem.setBestLargestHeightForWindow(bestHeight);
            VueTrendingAislesDataModel
                    .getInstance(VueApplication.getInstance()).dataObserver();
        }
        
    }
    
    public void hideDefaultActionbar() {
        mHideDefaultActionbar = true;
        invalidateOptionsMenu();
    }
    
    public void showDefaultActionbar() {
        mHideDefaultActionbar = false;
        invalidateOptionsMenu();
    }
    
    public class LandingScreenTitleReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            if (intent != null) {
                if (intent
                        .getStringExtra(VueConstants.LANDING_SCREEN_RECEIVER_KEY) != null) {
                    getActionBar()
                            .setTitle(
                                    intent.getStringExtra(VueConstants.LANDING_SCREEN_RECEIVER_KEY));
                    mLandingScreenName = intent
                            .getStringExtra(VueConstants.LANDING_SCREEN_RECEIVER_KEY);
                }
            }
        }
    }
    
    public boolean onQueryTextChange(String newText) {
        
        return false;
    }
    
    public boolean onQueryTextSubmit(String query) {
        return false;
    }
    
    public boolean onClose() {
        return false;
    }
    
    public void onClick(View view) {
    }
    
    private void loadDetailsScreenForNotificationClick(Bundle notificationBundle) {
        if (notificationBundle != null) {
            final String notificationImageId = notificationBundle.getString(
                    VueConstants.NOTIFICATION_IMAGE_ID, null);
            final String notificationAisleId = notificationBundle.getString(
                    VueConstants.NOTIFICATION_AISLE_ID, null);
            if (notificationAisleId != null) {
                final ProgressDialog progressDialog = ProgressDialog.show(this,
                        "", "Loading...");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final AisleWindowContent aisleWindowContent = new Parser()
                                .getAisleForAisleId(notificationAisleId);
                        VueLandingPageActivity.this
                                .runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (progressDialog != null) {
                                            progressDialog.dismiss();
                                        }
                                        if (aisleWindowContent != null) {
                                            VueTrendingAislesDataModel
                                                    .getInstance(
                                                            VueLandingPageActivity.this)
                                                    .dataObserver();
                                            List<AisleWindowContent> aisleList = new ArrayList<AisleWindowContent>();
                                            aisleList.add(aisleWindowContent);
                                            DataBaseManager
                                                    .getInstance(
                                                            VueApplication
                                                                    .getInstance())
                                                    .addTrentingAislesFromServerToDB(
                                                            VueApplication
                                                                    .getInstance(),
                                                            aisleList,
                                                            VueTrendingAislesDataModel
                                                                    .getInstance(
                                                                            VueApplication
                                                                                    .getInstance())
                                                                    .getNetworkHandler().offset,
                                                            DataBaseManager.AISLE_CREATED);
                                            Map<String, String> articleParams = new HashMap<String, String>();
                                            VueUser storedVueUser = null;
                                            try {
                                                storedVueUser = Utils
                                                        .readUserObjectFromFile(
                                                                VueLandingPageActivity.this,
                                                                VueConstants.VUE_APP_USEROBJECT__FILENAME);
                                            } catch (Exception e2) {
                                                e2.printStackTrace();
                                            }
                                            if (storedVueUser != null) {
                                                articleParams
                                                        .put("User_Id",
                                                                Long.valueOf(
                                                                        storedVueUser
                                                                                .getId())
                                                                        .toString());
                                            } else {
                                                articleParams.put("User_Id",
                                                        "anonymous");
                                            }
                                            
                                            DataBaseManager
                                                    .getInstance(
                                                            VueLandingPageActivity.this)
                                                    .updateOrAddRecentlyViewedAisles(
                                                            aisleWindowContent
                                                                    .getAisleId());
                                            
                                            FlurryAgent.logEvent(
                                                    "User_Select_Aisle",
                                                    articleParams);
                                            Intent intent = new Intent();
                                            intent.setClass(
                                                    VueLandingPageActivity.this,
                                                    AisleDetailsViewActivity.class);
                                            VueApplication
                                                    .getInstance()
                                                    .setClickedWindowID(
                                                            aisleWindowContent
                                                                    .getAisleId());
                                            VueApplication
                                                    .getInstance()
                                                    .setClickedWindowCount(
                                                            aisleWindowContent
                                                                    .getImageList()
                                                                    .size());
                                            VueApplication
                                                    .getInstance()
                                                    .setmAisleImgCurrentPos(
                                                            VueTrendingAislesDataModel
                                                                    .getInstance(
                                                                            VueLandingPageActivity.this)
                                                                    .getImagePositionInAisle(
                                                                            aisleWindowContent,
                                                                            notificationImageId));
                                            startActivity(intent);
                                        }
                                    }
                                });
                    }
                }).start();
            }
        }
    }
    
    public void share(AisleWindowContent aisleWindowContent,
            int currentDispImageIndex) {
        mShare = new ShareDialog(this, this, null, null);
        FileCache ObjFileCache = new FileCache(this);
        ArrayList<clsShare> imageUrlList = new ArrayList<clsShare>();
        if (aisleWindowContent.getImageList() != null
                && aisleWindowContent.getImageList().size() > 0) {
            String isUserAisle = "0";
            if (String.valueOf(VueApplication.getInstance().getmUserId())
                    .equals(aisleWindowContent.getAisleContext().mUserId)) {
                isUserAisle = "1";
            }
            for (int i = 0; i < aisleWindowContent.getImageList().size(); i++) {
                clsShare obj = new clsShare(
                        aisleWindowContent.getImageList().get(i).mImageUrl,
                        ObjFileCache
                                .getFile(
                                        aisleWindowContent.getImageList()
                                                .get(i).mImageUrl).getPath(),
                        aisleWindowContent.getAisleContext().mLookingForItem,
                        aisleWindowContent.getAisleContext().mFirstName
                                + " "
                                + aisleWindowContent.getAisleContext().mLastName,
                        isUserAisle,
                        aisleWindowContent.getAisleContext().mAisleId,
                        aisleWindowContent.getImageList().get(i).mId);
                imageUrlList.add(obj);
            }
            mShare.share(
                    imageUrlList,
                    aisleWindowContent.getAisleContext().mOccasion,
                    (aisleWindowContent.getAisleContext().mFirstName + " " + aisleWindowContent
                            .getAisleContext().mLastName),
                    currentDispImageIndex, null, null, new ShareViaVueListner());
        }
        if (aisleWindowContent.getImageList() != null
                && aisleWindowContent.getImageList().size() > 0) {
            FileCache ObjFileCache1 = new FileCache(this);
            for (int i = 0; i < aisleWindowContent.getImageList().size(); i++) {
                final File f = ObjFileCache1.getFile(aisleWindowContent
                        .getImageList().get(i).mImageUrl);
                if (!f.exists()) {
                    @SuppressWarnings("rawtypes")
                    Response.Listener listener = new Response.Listener<Bitmap>() {
                        @Override
                        public void onResponse(Bitmap bmp) {
                            Utils.saveBitmap(bmp, f);
                        }
                    };
                    Response.ErrorListener errorListener = new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError arg0) {
                        }
                    };
                    if (aisleWindowContent.getImageList().get(i).mImageUrl != null) {
                        @SuppressWarnings("unchecked")
                        ImageRequest imagerequestObj = new ImageRequest(
                                aisleWindowContent.getImageList().get(i).mImageUrl,
                                listener, 0, 0, null, errorListener);
                        VueApplication.getInstance().getRequestQueue()
                                .add(imagerequestObj);
                    }
                }
            }
        }
    }
    
    public class ShareViaVueListner implements ShareViaVueClickedListner {
        @Override
        public void onAisleShareToVue() {
            if (mShare != null) {
                if (mShare.mShareIntentCalled) {
                    mShare.mShareIntentCalled = false;
                }
                mShare.dismisDialog();
            }
            // ShareViaVue...
            if (VueApplication.getInstance().mShareViaVueClickedFlag) {
                VueApplication.getInstance().mShareViaVueClickedFlag = false;
                if (VueApplication.getInstance().mShareViaVueClickedImageId != null) {
                    String imageId = VueApplication.getInstance().mShareViaVueClickedImageId;
                    String aisleId = VueApplication.getInstance().mShareViaVueClickedAisleId;
                    VueApplication.getInstance().mShareViaVueClickedAisleId = null;
                    VueApplication.getInstance().mShareViaVueClickedImageId = null;
                    AisleImageDetails aisleImageDetails = VueTrendingAislesDataModel
                            .getInstance(VueLandingPageActivity.this)
                            .getAisleImageForImageId(imageId, aisleId, true);
                    if (aisleImageDetails != null) {
                        String originalUrl = aisleImageDetails.mImageUrl;
                        String sourceUrl = aisleImageDetails.mDetailsUrl;
                        int width = aisleImageDetails.mAvailableWidth;
                        int height = aisleImageDetails.mAvailableHeight;
                        int widthandHeightMultipliedValue = width * height;
                        OtherSourceImageDetails otherSourceImageDetails = new OtherSourceImageDetails();
                        otherSourceImageDetails.setHeight(height);
                        otherSourceImageDetails.setWidth(width);
                        otherSourceImageDetails
                                .setWidthHeightMultipliedValue(widthandHeightMultipliedValue);
                        otherSourceImageDetails.setOriginUrl(originalUrl);
                        ArrayList<OtherSourceImageDetails> imagesList = new ArrayList<OtherSourceImageDetails>();
                        imagesList.add(otherSourceImageDetails);
                        showOtherSourcesGridview(imagesList, sourceUrl);
                    }
                }
            }
        }
    }
}
