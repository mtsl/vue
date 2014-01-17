package com.lateralthoughts.vue;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.flurry.android.FlurryAgent;
import com.lateralthoughts.vue.ui.AisleContentBrowser;
import com.lateralthoughts.vue.ui.HorizontalListView;
import com.lateralthoughts.vue.utils.BitmapLoaderUtils;
import com.lateralthoughts.vue.utils.Utils;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

public class AisleDetailsViewActivity extends Activity {
    Fragment mFragRight;
    public static final String CLICK_EVENT = "click";
    public static final String LONG_PRESS_EVENT = "longpress";
    public static final String SCREEN_TAG = "comparisonscreen";
    public static final String TOP_SCROLLER = "topscroller";
    public static final String BOTTOM_SCROLLER = "bottomscroller";
    private static final String DETAILS_SCREEN_VISITOR = "Details_Screen_Visitors";
    private HorizontalListView mTopScroller, mBottomScroller;
    private int mComparisionDelay = 500;
    private int mScreenTotalHeight;
    private int mComparisionScreenHeight;
    private ArrayList<AisleImageDetails> mImageDetailsArr = null;
    private BitmapLoaderUtils mBitmapLoaderUtils;
    private int mLikeImageShowTime = 1000;
    private int mCurrentapiVersion;
    private int mStatusbarHeight;
    private boolean mTempflag = true;
    private VueAisleDetailsViewFragment mVueAiselFragment;
    private ViewHolder mViewHolder;
    private LinearLayout mContentLinearLay;
    private boolean mIsSlidePanleLoaded = false;
    private ComparisionAdapter mBottomAdapter, mTopAdapter;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private FrameLayout mDrawerLeft, mDrawerRight;
    private com.lateralthoughts.vue.VueListFragment mSlidListFrag;
    private MixpanelAPI mixpanel;
    
    @SuppressLint("NewApi")
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mixpanel = MixpanelAPI.getInstance(this, VueApplication.getInstance().MIXPANEL_TOKEN);
        setContentView(R.layout.aisle_details_activity_landing);
        mDrawerRight = (FrameLayout) findViewById(R.id.drawer_right);
        initialize();
        mDrawerLeft = (FrameLayout) findViewById(R.id.content_frame2);
        
        mSlidListFrag = (VueListFragment) getFragmentManager()
                .findFragmentById(R.id.listfrag);
        mCurrentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (mCurrentapiVersion >= 11) {
            getActionBar().hide();
        }
        mContentLinearLay = (LinearLayout) findViewById(R.id.content2);
        mTopScroller = (HorizontalListView) findViewById(R.id.topscroller);
        mBottomScroller = (HorizontalListView) findViewById(R.id.bottomscroller);
        mStatusbarHeight = VueApplication.getInstance().getmStatusBarHeight();
        mScreenTotalHeight = VueApplication.getInstance().getScreenHeight();
        mComparisionScreenHeight = mScreenTotalHeight - mStatusbarHeight;
        getActionBar().hide();
        
        mTopScroller.setOnTouchListener(new OnTouchListener() {
            
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                case MotionEvent.ACTION_DOWN:
                    // Disallow Drawer to intercept touch events.
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    break;
                
                case MotionEvent.ACTION_UP:
                    // Allow Drawer to intercept touch events.
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                    break;
                }
                
                // Handle Horizontal touch events.
                v.onTouchEvent(event);
                return true;
            }
        });
        
        mBottomScroller.setOnTouchListener(new OnTouchListener() {
            
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                case MotionEvent.ACTION_DOWN:
                    // Disallow Drawer to intercept touch events.
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    break;
                
                case MotionEvent.ACTION_UP:
                    // Allow Drawer to intercept touch events.
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                    break;
                }
                
                // Handle Horizontal touch events.
                v.onTouchEvent(event);
                return true;
            }
        });
        mTopScroller.setOnItemClickListener(new OnItemClickListener() {
            
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                    final int position, long arg3) {
                final ImageView img = (ImageView) arg1
                        .findViewById(R.id.compare_like_dislike);
                img.setImageResource(R.drawable.heart);
                img.setVisibility(View.VISIBLE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        img.setVisibility(View.INVISIBLE);
                        mVueAiselFragment
                                .changeLikeCount(position, CLICK_EVENT);
                    }
                }, mLikeImageShowTime);
                
            }
        });
        mTopScroller.setOnItemLongClickListener(new OnItemLongClickListener() {
            
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                    final int position, long arg3) {
                final ImageView img = (ImageView) arg1
                        .findViewById(R.id.compare_like_dislike);
                img.setImageResource(R.drawable.heart_dark);
                img.setVisibility(View.VISIBLE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        img.setVisibility(View.INVISIBLE);
                        mVueAiselFragment.changeLikeCount(position,
                                LONG_PRESS_EVENT);
                    }
                }, mLikeImageShowTime);
                return false;
            }
        });
        mBottomScroller.setOnItemClickListener(new OnItemClickListener() {
            
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                    final int position, long arg3) {
                final ImageView img = (ImageView) arg1
                        .findViewById(R.id.compare_like_dislike);
                img.setImageResource(R.drawable.heart);
                img.setVisibility(View.VISIBLE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        img.setVisibility(View.INVISIBLE);
                        mVueAiselFragment
                                .changeLikeCount(position, CLICK_EVENT);
                    }
                }, mLikeImageShowTime);
                
            }
        });
        mBottomScroller
                .setOnItemLongClickListener(new OnItemLongClickListener() {
                    
                    @Override
                    public boolean onItemLongClick(AdapterView<?> arg0,
                            View arg1, final int position, long arg3) {
                        final ImageView img = (ImageView) arg1
                                .findViewById(R.id.compare_like_dislike);
                        img.setImageResource(R.drawable.heart_dark);
                        img.setVisibility(View.VISIBLE);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                img.setVisibility(View.INVISIBLE);
                                mVueAiselFragment.changeLikeCount(position,
                                        LONG_PRESS_EVENT);
                            }
                        }, mLikeImageShowTime);
                        return false;
                    }
                });
    }
    
    @Override
    public void onBackPressed() {
        mixpanel.flush();
        super.onBackPressed();
    }
    private void initialize() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        // set a custom shadow that overlays the main content when the drawer
        // opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
        // set up the drawer's list view with items and click listener
        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(false);
        getActionBar().setHomeButtonEnabled(true);
        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
        mDrawerLayout, /* DrawerLayout object */
        R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
        R.string.drawer_open, /* "open drawer" description for accessibility */
        R.string.drawer_close /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                invalidateOptionsMenu(); // creates call to
                                         // onPrepareOptionsMenu()
                mSlidListFrag.closeKeybaord();
            }
            
            public void onDrawerOpened(View drawerView) {
                invalidateOptionsMenu(); // creates call to
                                         // onPrepareOptionsMenu()
            }
            
            @Override
            public void onDrawerStateChanged(int newState) {
                super.onDrawerStateChanged(newState);
                
                if (newState == DrawerLayout.STATE_DRAGGING) {
                    
                } else if (newState == DrawerLayout.STATE_IDLE) {
                    try {
                        if (mDrawerLayout.isDrawerOpen(mDrawerRight)
                                && mDrawerLayout.isDrawerOpen(mDrawerLeft)) {
                            // should never happens this case
                        } else if (mDrawerLayout.isDrawerOpen(mDrawerLeft)) {
                            mDrawerLayout.setDrawerLockMode(
                                    DrawerLayout.LOCK_MODE_LOCKED_CLOSED,
                                    mDrawerRight);
                        } else if (mDrawerLayout.isDrawerOpen(mDrawerRight)) {
                            mDrawerLayout.setDrawerLockMode(
                                    DrawerLayout.LOCK_MODE_LOCKED_CLOSED,
                                    mDrawerLeft);
                        } else {
                            mDrawerLayout.setDrawerLockMode(
                                    DrawerLayout.LOCK_MODE_UNLOCKED,
                                    mDrawerRight);
                            mDrawerLayout.setDrawerLockMode(
                                    DrawerLayout.LOCK_MODE_UNLOCKED,
                                    mDrawerLeft);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (newState == DrawerLayout.STATE_SETTLING) {
                }
            }
            
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                if (drawerView == mDrawerRight) {
                } else if (drawerView == mDrawerLeft) {
                }
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mVueAiselFragment = new VueAisleDetailsViewFragment();
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, mVueAiselFragment).commit();
        mDrawerLayout.setFocusableInTouchMode(false);
    }
    
    @Override
    protected void onStart() {
        mixpanel.track(DETAILS_SCREEN_VISITOR, null);
        FlurryAgent.onStartSession(this, Utils.FLURRY_APP_KEY);
        FlurryAgent.onPageView();
        FlurryAgent.logEvent(DETAILS_SCREEN_VISITOR);
        super.onStart();
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        FlurryAgent.onEndSession(this);
    }
    
    class ComparisionAdapter extends BaseAdapter {
        LayoutInflater minflater;
        int i = 0;
        
        public ComparisionAdapter(Context context) {
            minflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        
        @Override
        public int getCount() {
            return mImageDetailsArr.size();
        }
        
        @Override
        public Object getItem(int position) {
            return position;
        }
        
        @Override
        public long getItemId(int position) {
            return position;
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                mViewHolder = new ViewHolder();
                convertView = minflater.inflate(R.layout.vuecompareimg, null);
                mViewHolder.img = (ImageView) convertView
                        .findViewById(R.id.vue_compareimg);
                mViewHolder.likeImage = (ImageView) convertView
                        .findViewById(R.id.compare_like_dislike);
                mViewHolder.pb = (ProgressBar) convertView
                        .findViewById(R.id.progressBar1);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                        mComparisionScreenHeight / 2,
                        mComparisionScreenHeight / 2);
                params.addRule(RelativeLayout.CENTER_IN_PARENT);
                params.setMargins(VueApplication.getInstance().getPixel(10), 0,
                        0, 0);
                mViewHolder.img.setLayoutParams(params);
                mViewHolder.img.setBackgroundColor(Color
                        .parseColor(getResources().getString(R.color.white)));
                RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(
                        LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                params2.addRule(RelativeLayout.CENTER_IN_PARENT);
                mViewHolder.likeImage.setLayoutParams(params2);
                convertView.setTag(mViewHolder);
            }
            mViewHolder = (ViewHolder) convertView.getTag();
            mViewHolder.likeImage.setVisibility(View.INVISIBLE);
            mViewHolder.likeImage.setImageResource(R.drawable.thumb_up);
            mViewHolder.img.setImageResource(R.drawable.no_image);
            BitmapWorkerTask task = new BitmapWorkerTask(null, mViewHolder.img,
                    mComparisionScreenHeight / 2, mViewHolder.pb);
            String[] imagesArray = {
                    mImageDetailsArr.get(position).mCustomImageUrl,
                    mImageDetailsArr.get(position).mImageUrl };
            task.execute(imagesArray);
            
            return convertView;
        }
        
    }
    
    private class ViewHolder {
        ImageView img;
        ImageView likeImage;
        ProgressBar pb;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        Bundle b = getIntent().getExtras();
        if (b != null && mTempflag) {
            mTempflag = false;
            if (b.getBoolean(VueConstants.FROM_OTHER_SOURCES_FLAG)) {
                sendDataToDataentryScreen(b);
            }
        }
        
        if (mVueAiselFragment != null) {
            mImageDetailsArr = mVueAiselFragment.getAisleWindowImgList();
            if (mBottomAdapter != null) {
                mBottomAdapter.notifyDataSetChanged();
            }
            if (mTopAdapter != null) {
                mTopAdapter.notifyDataSetChanged();
            }
        }
        if (!mIsSlidePanleLoaded) {
            mIsSlidePanleLoaded = true;
            new Handler().postDelayed(new Runnable() {
                
                @Override
                public void run() {
                    if (mVueAiselFragment != null) {
                        if (mImageDetailsArr != null) {
                            mImageDetailsArr = mVueAiselFragment
                                    .getAisleWindowImgList();
                        }
                        mBitmapLoaderUtils = BitmapLoaderUtils.getInstance();
                        if (null != mImageDetailsArr
                                && mImageDetailsArr.size() != 0) {
                            mBottomAdapter = new ComparisionAdapter(
                                    AisleDetailsViewActivity.this);
                            mTopAdapter = new ComparisionAdapter(
                                    AisleDetailsViewActivity.this);
                            mBottomScroller.setAdapter(mBottomAdapter);
                            mTopScroller.setAdapter(mTopAdapter);
                            
                        }
                    }
                }
                
            }, mComparisionDelay);
        }
    }
    
    @Override
    public void onPause() {
        super.onPause();
        
    }
    
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            
            if (mDrawerLayout.isDrawerOpen(mDrawerLeft)) {
                
                if (!mSlidListFrag.listener.onBackPressed()) {
                    mDrawerLayout.closeDrawer(mDrawerLeft);
                }
            } else if (mDrawerLayout.isDrawerOpen(mDrawerRight)) {
                if (!mSlidListFrag.listener.onBackPressed()) {
                    mDrawerLayout.closeDrawer(mDrawerRight);
                }
            } else {
                try {
                    if (VueLandingPageActivity.landingPageActivity != null
                            && VueLandingPageActivity.mLandingScreenName != null
                            && (VueLandingPageActivity.mLandingScreenName
                                    .equals(VueApplication
                                            .getInstance()
                                            .getString(
                                                    R.string.sidemenu_sub_option_Bookmarks)))) {
                        if (!(VueTrendingAislesDataModel.getInstance(this)
                                .getAisleAt(
                                        VueApplication.getInstance()
                                                .getClickedWindowID())
                                .getWindowBookmarkIndicator())) {
                            VueTrendingAislesDataModel
                                    .getInstance(this)
                                    .removeAisleFromList(
                                            VueTrendingAislesDataModel
                                                    .getInstance(this)
                                                    .getAilsePosition(
                                                            VueTrendingAislesDataModel
                                                                    .getInstance(
                                                                            this)
                                                                    .getAisleAt(
                                                                            VueApplication
                                                                                    .getInstance()
                                                                                    .getClickedWindowID())));
                            VueTrendingAislesDataModel.getInstance(this)
                                    .dataObserver();
                        }
                    }
                } catch (Exception e) {
                    
                }
                mVueAiselFragment.setAisleContentListenerNull();
                mContentLinearLay.removeAllViews();
                clearBitmaps();
                super.onBackPressed();
            }
        }
        return false;
        
    }
    
    @Override
    protected void onDestroy() {
        if (mVueAiselFragment != null)
            mVueAiselFragment.setAisleContentListenerNull();
        super.onDestroy();
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (VueApplication.getInstance().mNewViewSelection) {
            finish();
        }
        if (requestCode == VueConstants.INVITE_FRIENDS_LOGINACTIVITY_REQUEST_CODE
                && resultCode == VueConstants.INVITE_FRIENDS_LOGINACTIVITY_REQUEST_CODE) {
            if (data != null) {
                if (data.getStringExtra(VueConstants.INVITE_FRIENDS_LOGINACTIVITY_BUNDLE_STRING_KEY) != null) {
                    mSlidListFrag
                            .getFriendsList(data
                                    .getStringExtra(VueConstants.INVITE_FRIENDS_LOGINACTIVITY_BUNDLE_STRING_KEY));
                }
            }
        } else if (requestCode == VueConstants.FROM_DETAILS_SCREEN_TO_DATAENTRY_SCREEN_ACTIVITY_RESULT
                && resultCode == VueConstants.FROM_DETAILS_SCREEN_TO_DATAENTRY_SCREEN_ACTIVITY_RESULT) {
            updateAisleScreen();
            Bundle b = data.getExtras();
            if (b != null) {
                String lookingfor = b
                        .getString(VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_LOOKINGFOR);
                String occasion = b
                        .getString(VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_OCCASION);
                String description = b
                        .getString(VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_SAYSOMETHINGABOUTAISLE);
                String category = b
                        .getString(VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_CATEGORY);
                if (lookingfor != null && lookingfor.trim().length() > 0
                        && !lookingfor.equals("Looking")) {
                    VueTrendingAislesDataModel
                            .getInstance(AisleDetailsViewActivity.this)
                            .getAisleAt(
                                    VueApplication.getInstance()
                                            .getClickedWindowID())
                            .getAisleContext().mLookingForItem = lookingfor;
                }
                if (occasion != null && occasion.trim().length() > 0
                        && !occasion.trim().equals("Occasion")) {
                    VueTrendingAislesDataModel
                            .getInstance(AisleDetailsViewActivity.this)
                            .getAisleAt(
                                    VueApplication.getInstance()
                                            .getClickedWindowID())
                            .getAisleContext().mOccasion = occasion;
                }
                if (category != null && category.trim().length() > 0) {
                    VueTrendingAislesDataModel
                            .getInstance(AisleDetailsViewActivity.this)
                            .getAisleAt(
                                    VueApplication.getInstance()
                                            .getClickedWindowID())
                            .getAisleContext().mCategory = category;
                }
                if (description != null && description.trim().length() > 0) {
                    VueTrendingAislesDataModel
                            .getInstance(AisleDetailsViewActivity.this)
                            .getAisleAt(
                                    VueApplication.getInstance()
                                            .getClickedWindowID())
                            .getAisleContext().mDescription = description;
                }
                mVueAiselFragment.notifyAdapter();
                ArrayList<String> findAtArrayList = b
                        .getStringArrayList(VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_FINDAT);
                if (findAtArrayList != null && findAtArrayList.size() > 0) {
                    mVueAiselFragment.mEditTextFindAt.setText(findAtArrayList
                            .get(0));
                } else {
                    mVueAiselFragment.mEditTextFindAt.setText("");
                }
                addImageToAisle();
            }
        } else if (requestCode == VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_ACTIVITY_RESULT
                && resultCode == VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_ACTIVITY_RESULT) {
            Bundle b = data.getExtras();
            if (b != null) {
                sendDataToDataentryScreen(b);
            }
        } else {
            try {
                if (mVueAiselFragment.mAisleDetailsAdapter.mShare != null
                        && mVueAiselFragment.mAisleDetailsAdapter.mShare.mShareIntentCalled) {
                    mVueAiselFragment.mAisleDetailsAdapter.mShare.mShareIntentCalled = false;
                    mVueAiselFragment.mAisleDetailsAdapter.mShare
                            .dismisDialog();
                } else {
                    updateAisleScreen();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            
        }
    }
    
    class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private String url = null;
        private int mBestHeight;
        private ProgressBar progressBar;
        
        public BitmapWorkerTask(AisleContentBrowser vFlipper,
                ImageView imageView, int bestHeight, ProgressBar bp) {
            // Use a WeakReference to ensure the ImageView can be garbage
            // collected
            progressBar = bp;
            imageViewReference = new WeakReference<ImageView>(imageView);
            mBestHeight = bestHeight;
        }
        
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }
        
        // Decode image in background.
        @Override
        protected Bitmap doInBackground(String... params) {
            url = params[0];
            Bitmap bmp = null;
            // we want to get the bitmap and also add it into the memory cache
            bmp = mBitmapLoaderUtils
                    .getBitmap(url, params[1], true, mBestHeight,
                            VueApplication.getInstance()
                                    .getVueDetailsCardWidth() / 2,
                            Utils.DETAILS_SCREEN);
            return bmp;
        }
        
        // Once complete, see if ImageView is still around and set bitmap.
        @SuppressWarnings("null")
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            progressBar.setVisibility(View.GONE);
            if (imageViewReference != null && bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);
                } else {
                    imageView.setImageResource(R.drawable.no_image);
                }
                
            }
        }
    }
    
    public void sendDataToDataentryScreen(Bundle b) {
        String lookingFor, occation, category, userId, description;
        AisleContext aisleInfo = mVueAiselFragment.getAisleContext();
        lookingFor = aisleInfo.mLookingForItem;
        occation = aisleInfo.mOccasion;
        category = aisleInfo.mCategory;
        userId = aisleInfo.mUserId;
        description = aisleInfo.mDescription;
        Intent intent = new Intent(this, DataEntryActivity.class);
        Bundle b1 = new Bundle();
        b1.putBoolean(
                VueConstants.FROM_DETAILS_SCREEN_TO_DATAENTRY_SCREEN_FLAG, true);
        VueUser storedVueUser = null;
        boolean isUserAisleFlag = false;
        try {
            storedVueUser = Utils.readUserObjectFromFile(this,
                    VueConstants.VUE_APP_USEROBJECT__FILENAME);
            if (userId.equals(String.valueOf(storedVueUser.getId()))) {
                isUserAisleFlag = true;
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        b1.putBoolean(
                VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_IS_USER_AISLE_FLAG,
                isUserAisleFlag);
        b1.putString(
                VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_LOOKINGFOR,
                lookingFor);
        b1.putString(
                VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_OCCASION,
                occation);
        b1.putString(
                VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_CATEGORY,
                category);
        b1.putString(
                VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_SAYSOMETHINGABOUTAISLE,
                description);
        if (b != null) {
            String imagePath = b
                    .getString(VueConstants.CREATE_AISLE_CAMERA_GALLERY_IMAGE_PATH_BUNDLE_KEY);
            b1.putString(VueConstants.FROM_OTHER_SOURCES_URL,
                    b.getString(VueConstants.FROM_OTHER_SOURCES_URL));
            b1.putBoolean(VueConstants.FROM_OTHER_SOURCES_FLAG,
                    b.getBoolean(VueConstants.FROM_OTHER_SOURCES_FLAG));
            b1.putParcelableArrayList(
                    VueConstants.FROM_OTHER_SOURCES_IMAGE_URIS,
                    b.getParcelableArrayList(VueConstants.FROM_OTHER_SOURCES_IMAGE_URIS));
            b1.putString(
                    VueConstants.CREATE_AISLE_CAMERA_GALLERY_IMAGE_PATH_BUNDLE_KEY,
                    imagePath);
            b1.putBoolean(VueConstants.EDIT_IMAGE_FROM_DETAILS_SCREEN_FALG,
                    false);
        } else {
            b1.putBoolean(VueConstants.EDIT_IMAGE_FROM_DETAILS_SCREEN_FALG,
                    true);
        }
        intent.putExtras(b1);
        this.startActivityForResult(
                intent,
                VueConstants.FROM_DETAILS_SCREEN_TO_DATAENTRY_SCREEN_ACTIVITY_RESULT);
    }
    
    private void addImageToAisle() {
        mVueAiselFragment.addAisleToWindow();
    }
    
    private void clearBitmaps() {
        
        for (int i = 0; i < mTopScroller.getChildCount(); i++) {
            
            RelativeLayout topLayout = (RelativeLayout) mTopScroller
                    .getChildAt(i);
            
            ImageView imageViewImage = (ImageView) topLayout
                    .findViewById(R.id.vue_compareimg);
            ImageView imageViewLike = (ImageView) topLayout
                    .findViewById(R.id.compare_like_dislike);
            try {
                Bitmap bitmap = ((BitmapDrawable) imageViewImage.getDrawable())
                        .getBitmap();
                bitmap.recycle();
                bitmap = null;
                imageViewImage.setImageDrawable(null);
                imageViewLike.setImageResource(0);
            } catch (Exception e) {
                
            }
        }
        for (int i = 0; i < mBottomScroller.getChildCount(); i++) {
            
            RelativeLayout topLayout = (RelativeLayout) mBottomScroller
                    .getChildAt(i);
            
            ImageView imageViewImage = (ImageView) topLayout
                    .findViewById(R.id.vue_compareimg);
            ImageView imageViewLike = (ImageView) topLayout
                    .findViewById(R.id.compare_like_dislike);
            try {
                Bitmap bitmap = ((BitmapDrawable) imageViewImage.getDrawable())
                        .getBitmap();
                bitmap.recycle();
                bitmap = null;
                imageViewImage.setImageDrawable(null);
                imageViewLike.setImageResource(0);
            } catch (Exception e) {
                
            }
        }
        
    }
    
    public void shareViaVueClicked() {
        finish();
    }
    
    public void updateAisleScreen() {
        if (VueApplication.getInstance().ismFinishDetailsScreenFlag()) {
            VueApplication.getInstance().setmFinishDetailsScreenFlag(false);
            finish();
        } else {
            mVueAiselFragment.updateAisleScreen();
        }
    }
    
}
