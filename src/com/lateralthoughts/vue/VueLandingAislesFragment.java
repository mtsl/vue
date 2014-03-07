package com.lateralthoughts.vue;

//generic android & java goodies
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

import com.lateralthoughts.vue.connectivity.DataBaseManager;
import com.lateralthoughts.vue.logging.Logger;
import com.lateralthoughts.vue.ui.AisleContentBrowser.AisleContentClickListener;
import com.lateralthoughts.vue.ui.ArcMenu;
import com.lateralthoughts.vue.user.VueUser;
import com.lateralthoughts.vue.utils.Utils;
import com.origamilabs.library.views.StaggeredGridView;

//java utils

//as soon as this fragment attaches to the activity we will go ahead and pull up the
//the list of current trending aisles.
//when the result is received we parse it and coalesce it into an array list of 
//AisleWindowContent objects. At this point we are ready to setup the adapter for the
//mTrendingAislesContentView.

public class VueLandingAislesFragment extends Fragment {
    private Context mContext;
    private VueContentGateway mVueContentGateway;
    private LandingPageViewAdapter mStaggeredAdapter;
    
    private StaggeredGridView mStaggeredView;
    
    private AisleClickListener mAisleClickListener;
    public boolean mIsFlingCalled;
    
    public boolean mIsIdleState;
    private ProgressBar mTrendingLoad;
    private boolean mHelpDialogShown = false;
    private boolean mIsMyPointsDownLoadDone = false;
    
    // TODO: define a public interface that can be implemented by the parent
    // activity so that we can notify it with an ArrayList of AisleWindowContent
    // once we have received the result and parsed it. The idea is that the
    // activity
    // can then initiate a worker in the background to go fetch more content and
    // get
    // ready to launch other activities/fragments within the application
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
        
        // without much ado lets get started with retrieving the trending aisles
        // list
        mVueContentGateway = VueContentGateway.getInstance();
        if (null == mVueContentGateway) {
            // assert here: this is a no go!
        }
        
        mAisleClickListener = new AisleClickListener();
        mStaggeredAdapter = new LandingPageViewAdapter(mContext,
                mAisleClickListener);
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // TODO: any particular state that we want to restore?
        
    }
    
    public void notifyAdapters() {
        if (null != mStaggeredAdapter) {
            mStaggeredAdapter.notifyDataSetChanged();
        }
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // synchronized list view approach
        View v = inflater.inflate(R.layout.aisles_view_fragment, container,
                false);
        mStaggeredView = (StaggeredGridView) v.findViewById(R.id.aisles_grid);
        mTrendingLoad = (ProgressBar) v.findViewById(R.id.trending_load);
        int margin = getResources().getDimensionPixelSize(R.dimen.margin);
        mStaggeredView.setItemMargin(margin); // set the GridView margin
        SharedPreferences sharedPreferencesObj = getActivity()
                .getSharedPreferences(VueConstants.SHAREDPREFERENCE_NAME, 0);
        int trendingSwipeCountMaxReached = sharedPreferencesObj.getInt(
                VueConstants.TRENDING_SWIPE_COUNT, 0);
        if (trendingSwipeCountMaxReached > 3) {
            AisleLoader.trendingSwipeBlock = true;
        }
        mStaggeredView.setPadding(margin, 0, margin, 0); // have the margin on
                                                         // the sides as well
        mStaggeredView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return false; // To change body of implemented methods use File
                              // | Settings | File Templates.
            }
        });
        
        mStaggeredView.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
                switch (i) {
                case SCROLL_STATE_FLING:
                case SCROLL_STATE_TOUCH_SCROLL:
                    mStaggeredAdapter.setIsScrolling(true);
                    VueApplication.getInstance().getRequestQueue()
                            .cancelAll(VueApplication.LOAD_IMAGES_REQUEST_TAG);
                    break;
                
                case SCROLL_STATE_IDLE:
                    mStaggeredAdapter.setIsScrolling(false);
                    VueApplication.getInstance().getRequestQueue()
                            .cancelAll(VueApplication.MORE_AISLES_REQUEST_TAG);
                    AisleLoader.isScrolling = false;
                    if (!AisleLoader.trendingSwipeBlock) {
                        mStaggeredAdapter.swipeFromAdapterImage();
                        if (AisleLoader.sTrendingSwipeCount > 3) {
                            saveAisleSwip(4);
                        }
                    }
                    break;
                }
            }
            
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                    int visibleItemCount, int totalItemCount) {
                /*
                 * if (totalItemCount > 0 && mTrendingLoad.getVisibility() ==
                 * View.VISIBLE) { mTrendingLoad.setVisibility(View.GONE); }
                 */
                AisleLoader.isScrolling = true;
                if (VueTrendingAislesDataModel.getInstance(mContext).loadOnRequest
                        && VueLandingPageActivity.mLandingScreenName != null
                        && VueLandingPageActivity.mLandingScreenName
                                .equalsIgnoreCase(getResources().getString(
                                        R.string.trending))
                        && !VueTrendingAislesDataModel.getInstance(mContext).mIsFromDb) {
                    int lastVisiblePosition = firstVisibleItem
                            + visibleItemCount;
                    if ((totalItemCount - lastVisiblePosition) < 5) {
                        if (!VueContentGateway.mNomoreTrendingAilse) {
                            VueTrendingAislesDataModel
                                    .getInstance(mContext)
                                    .getNetworkHandler()
                                    .requestMoreAisle(
                                            true,
                                            getResources().getString(
                                                    R.string.trending));
                        }
                    }
                }
                
            }
        });
        mStaggeredView.setAdapter(mStaggeredAdapter);
        return v;
    }
    
    private class AisleClickListener implements AisleContentClickListener {
        @Override
        public void onAisleClicked(String id, int count, int aisleImgCurrentPos) {
            if (VueLandingPageActivity.mOtherSourceImagePath == null) {
                VueApplication.getInstance().saveTrendingRefreshTime(0);
                
                VueUser storedVueUser = null;
                try {
                    storedVueUser = Utils.readUserObjectFromFile(getActivity(),
                            VueConstants.VUE_APP_USEROBJECT__FILENAME);
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
                DataBaseManager.getInstance(mContext)
                        .updateOrAddRecentlyViewedAisles(id);
                Intent intent = new Intent();
                intent.setClass(VueApplication.getInstance(),
                        AisleDetailsViewActivity.class);
                VueApplication.getInstance().setClickedWindowID(id);
                VueApplication.getInstance().setClickedWindowCount(count);
                VueApplication.getInstance().setmAisleImgCurrentPos(
                        aisleImgCurrentPos);
                startActivity(intent);
            } else {
                VueLandingPageActivity vueLandingPageActivity = (VueLandingPageActivity) getActivity();
                vueLandingPageActivity.hideDefaultActionbar();
                VueLandingPageActivity.mOtherSourceAddImageAisleId = id;
                notifyAdapters();
            }
            
        }
        
        @Override
        public boolean isFlingCalled() {
            return mIsFlingCalled;
        }
        
        @Override
        public boolean isIdelState() {
            
            return mIsIdleState;
        }
        
        @Override
        public boolean onDoubleTap(String id) {
            AisleWindowContent windowItem = VueTrendingAislesDataModel
                    .getInstance(VueApplication.getInstance()).getAisleAt(id);
            int finalWidth = 0, finaHeight = 0;
            if (windowItem.getImageList().get(0).mAvailableHeight >= windowItem
                    .getBestHeightForWindow()) {
                finalWidth = (windowItem.getImageList().get(0).mAvailableWidth * windowItem
                        .getBestHeightForWindow())
                        / windowItem.getImageList().get(0).mAvailableHeight;
                finaHeight = windowItem.getBestHeightForWindow();
            }
            
            if (finalWidth > VueApplication.getInstance().getScreenWidth() / 2) {
                
                finaHeight = (finaHeight
                        * VueApplication.getInstance().getScreenWidth() / 2)
                        / finalWidth;
                finalWidth = VueApplication.getInstance().getScreenWidth() / 2;
            }
            
            String writeSdCard = null;
            writeSdCard = "*************************aisle info:"
                    + " started***********************\n";
            writeSdCard = writeSdCard + "\nAisleId: " + windowItem.getAisleId()
                    + "\n" + "Smallest Image Height: "
                    + windowItem.getImageList().get(0).mTrendingImageHeight
                    + "\n" + "Card Width: "
                    + VueApplication.getInstance().getVueDetailsCardWidth() / 2
                    + "\n";
            for (int i = 0; i < windowItem.getImageList().size(); i++) {
                writeSdCard = writeSdCard + "\n ImageUrl: "
                        + windowItem.getImageList().get(i).mImageUrl;
                writeSdCard = writeSdCard + "\n" + "image Width: "
                        + windowItem.getImageList().get(i).mAvailableWidth
                        + " Height: "
                        + windowItem.getImageList().get(i).mAvailableHeight;
            }
            writeSdCard = writeSdCard + "\n\n After Resized Aisle height: "
                    + windowItem.getImageList().get(0).mTrendingImageHeight
                    + " After Resized Aisle width: "
                    + VueApplication.getInstance().getVueDetailsCardWidth() / 2;
            
            for (int i = 0; i < windowItem.getImageList().size(); i++) {
                writeSdCard = writeSdCard + "\n CustomImageUrl: "
                        + windowItem.getImageList().get(i).mCustomImageUrl;
            }
            
            writeSdCard = writeSdCard
                    + "\n###################### info end ################################";
            
            writeSdCard = writeSdCard + "\n "
                    + windowItem.getAisleContext().mFirstName + " ???? "
                    + windowItem.getAisleContext().mLastName;
            
            writeToSdcard(writeSdCard);
            return false;
        }
        
        @Override
        public void refreshList() {
            mStaggeredAdapter.notifyDataSetChanged();
        }
        
        @Override
        public void hideProgressBar(int count) {
            if (mTrendingLoad.getVisibility() == View.VISIBLE) {
                mTrendingLoad.setVisibility(View.GONE);
                notifyAdapters();
                if (!mIsMyPointsDownLoadDone) {
                    // load lazily after completion of all trending initial data
                    // loading
                    int waitTime = 10000;
                    new Handler().postDelayed(new Runnable() {
                        
                        @Override
                        public void run() {
                            MyPoints points = new MyPoints();
                            points.execute();
                        }
                    }, waitTime);
                    
                }
            }
        }
        
        @Override
        public void showProgressBar(int count) {
            if (mTrendingLoad.getVisibility() == View.GONE) {
                if (VueLandingPageActivity.mLandingScreenActive) {
                    mTrendingLoad.setVisibility(View.VISIBLE);
                } else {
                    mTrendingLoad.setVisibility(View.GONE);
                }
            }
        }
    }
    
    public int getListPosition() {
        return mStaggeredView.getFirstPosition();
        
    }
    
    private void initArcMenu(ArcMenu menu, int[] itemDrawables) {
        final int itemCount = itemDrawables.length;
        for (int i = 0; i < itemCount; i++) {
            ImageView item = new ImageView(getActivity());
            item.setImageResource(itemDrawables[i]);
            LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT);
            lp.setMargins(4, 4, 4, 4);
            final int position = i;
            menu.addItem(i, lp, item, new OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    Toast.makeText(getActivity(), "position:" + position,
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    
    private void writeToSdcard(String message) {
        if (!Logger.sWrightToSdCard) {
            return;
        }
        String path = Environment.getExternalStorageDirectory().toString();
        File dir = new File(path + "/vueImageDetails/");
        if (!dir.isDirectory()) {
            dir.mkdir();
        }
        File file = new File(dir, "/" + "vueImageDetails_"
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
    
    private class MyPoints extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            VueLandingPageActivity.sMyPointsAvailable = false;
        }
        
        @Override
        protected Void doInBackground(Void... params) {
            mIsMyPointsDownLoadDone = true;
            Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
            VueTrendingAislesDataModel
                    .getInstance(VueApplication.getInstance())
                    .getNetworkHandler().getMyAislesPoints();
            VueLandingPageActivity.sMyPointsAvailable = true;
            return null;
        }
        
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
    }
    
    private void saveAisleSwip(int count) {
        SharedPreferences sharedPreferencesObj = getActivity()
                .getSharedPreferences(VueConstants.SHAREDPREFERENCE_NAME, 0);
        Editor editor = sharedPreferencesObj.edit();
        editor.putInt(VueConstants.TRENDING_SWIPE_COUNT, count);
        editor.commit();
        AisleLoader.trendingSwipeBlock = true;
    }
}
