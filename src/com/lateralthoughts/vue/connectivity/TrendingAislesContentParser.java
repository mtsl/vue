package com.lateralthoughts.vue.connectivity;

import java.util.ArrayList;

import junit.framework.Assert;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.ResultReceiver;

import com.lateralthoughts.vue.AisleWindowContent;
import com.lateralthoughts.vue.R;
import com.lateralthoughts.vue.VueApplication;
import com.lateralthoughts.vue.VueConstants;
import com.lateralthoughts.vue.VueLandingPageActivity;
import com.lateralthoughts.vue.VueTrendingAislesDataModel;
import com.lateralthoughts.vue.parser.Parser;

public class TrendingAislesContentParser extends ResultReceiver {
    private int mState;
    
    // this variable above is usually set to one of the following values
    private static final int MORE_AISLES_PARSED = 1;
    private static final int INITIALIZE = 2;
    
    public TrendingAislesContentParser(Handler handler, int receiverSource) {
        super(handler);
        mState = receiverSource;
        
        synchronized (TrendingAislesContentParser.class) {
            if (sParserDBThreadHandler == null) {
                // Create a global thread and start it.
                Thread t = new Thread(new ContentParserDBThread());
                t.setName("DB Writer");
                t.start();
                try {
                    TrendingAislesContentParser.class.wait();
                    Message msg = new Message();
                    msg.what = INITIALIZE;
                    sParserDBThreadHandler.sendMessage(msg);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    @Override
    protected void onReceiveResult(int resultCode, final Bundle resultData) {
        switch (mState) {
        case VueConstants.AISLE_TRENDING_LIST_DATA:
            VueApplication.getInstance().mLastRecordedTime = System
                    .currentTimeMillis();
            
            Message msg = new Message();
            msg.what = MORE_AISLES_PARSED;
            msg.obj = resultData;
            sParserDBThreadHandler.sendMessage(msg);
            break;
        case VueConstants.AISLE_TRENDING_PARSED_DATA:
            VueTrendingAislesDataModel
                    .getInstance(VueApplication.getInstance())
                    .dismissProgress();
            // if this is the first set of data we are receiving
            // go
            // ahead
            // notify the data set changed
            VueTrendingAislesDataModel
                    .getInstance(VueApplication.getInstance()).dataObserver();
            break;
        default:
            // we should never have to encounter this!
            break;
        }
    }
    
    private static Handler sParserDBThreadHandler;
    
    private static class ContentParserDBThread implements Runnable {
        // Message id for initializing a new WebViewCore.
        private static final int INITIALIZE = 0;
        
        public void run() {
            Looper.prepare();
            Assert.assertNull(sParserDBThreadHandler);
            synchronized (TrendingAislesContentParser.class) {
                sParserDBThreadHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        switch (msg.what) {
                        case INITIALIZE:
                            Process.setThreadPriority(Process.THREAD_PRIORITY_MORE_FAVORABLE);
                            break;
                        case MORE_AISLES_PARSED: {
                            Bundle resultData = (Bundle) msg.obj;
                            boolean refreshListFlag = true;
                            final ArrayList<AisleWindowContent> aislesList = new Parser()
                                    .parseTrendingAislesResultData(
                                            resultData.getString("result"),
                                            resultData.getBoolean("loadMore"));
                            int offset = resultData.getInt("offset");
                            
                            if (VueLandingPageActivity.landingPageActivity != null
                                    && VueLandingPageActivity.mLandingScreenName != null
                                    && (VueLandingPageActivity.mLandingScreenName
                                            .equals(VueApplication
                                                    .getInstance()
                                                    .getString(
                                                            R.string.sidemenu_option_Trending_Aisles)))) {
                                if (VueApplication.getInstance().mIsTrendingSelectedFromBezelMenuFlag) {
                                    VueApplication.getInstance().mIsTrendingSelectedFromBezelMenuFlag = false;
                                    if (resultData.getInt("offset") == 0) {
                                        refreshListFlag = true;
                                    }
                                } else {
                                    refreshListFlag = true;
                                }
                            }
                            VueLandingPageActivity.landingPageActivity
                                    .runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            VueTrendingAislesDataModel
                                                    .getInstance(
                                                            VueApplication
                                                                    .getInstance())
                                                    .dismissProgress();
                                            
                                        }
                                    });
                            if (VueLandingPageActivity.landingPageActivity != null
                                    && VueLandingPageActivity.mLandingScreenName != null) {
                                if (VueLandingPageActivity.mLandingScreenName
                                        .equals(VueApplication
                                                .getInstance()
                                                .getString(
                                                        R.string.sidemenu_sub_option_My_Aisles))
                                        || VueLandingPageActivity.mLandingScreenName
                                                .equals(VueApplication
                                                        .getInstance()
                                                        .getString(
                                                                R.string.sidemenu_sub_option_Recently_Viewed_Aisles))) {
                                    refreshListFlag = false;
                                }
                            }
                            
                            if (refreshListFlag) {
                                VueLandingPageActivity.landingPageActivity
                                        .runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                
                                                if (VueLandingPageActivity.mLandingScreenName != null
                                                        && VueLandingPageActivity.mLandingScreenName
                                                                .equals(VueApplication
                                                                        .getInstance()
                                                                        .getString(
                                                                                R.string.sidemenu_sub_option_Bookmarks))) {
                                                    VueTrendingAislesDataModel
                                                            .getInstance(VueApplication
                                                                    .getInstance()).loadOnRequest = false;
                                                } else {
                                                    if (VueLandingPageActivity.notification != null
                                                            && VueLandingPageActivity.notification
                                                                    .equalsIgnoreCase("MyAisles")) {
                                                        //do not refresh trending screen when user access from notification click
                                                        //do nothing here.
                                                    } else {
                                                        VueTrendingAislesDataModel
                                                                .getInstance(VueApplication
                                                                        .getInstance()).loadOnRequest = true;
                                                        if (aislesList != null
                                                                && aislesList
                                                                        .size() > 0) {
                                                            for (int i = 0; i < aislesList
                                                                    .size(); i++) {
                                                                VueTrendingAislesDataModel model = VueTrendingAislesDataModel
                                                                        .getInstance(VueApplication
                                                                                .getInstance());
                                                                model.addItemToList(
                                                                        aislesList
                                                                                .get(i)
                                                                                .getAisleContext().mAisleId,
                                                                        aislesList
                                                                                .get(i));
                                                            }
                                                            
                                                            VueTrendingAislesDataModel
                                                                    .getInstance(
                                                                            VueApplication
                                                                                    .getInstance())
                                                                    .dismissProgress();
                                                            // if this is the
                                                            // first
                                                            // set of
                                                            // data we are
                                                            // receiving
                                                            // go ahead
                                                            // notify the data
                                                            // set
                                                            // changed
                                                            VueTrendingAislesDataModel
                                                                    .getInstance(
                                                                            VueApplication
                                                                                    .getInstance())
                                                                    .dataObserver();
                                                        }
                                                    }
                                                }
                                            }
                                        });
                            }
                            DataBaseManager.getInstance(
                                    VueApplication.getInstance())
                                    .addTrentingAislesFromServerToDB(
                                            VueApplication.getInstance(),
                                            aislesList, offset,
                                            DataBaseManager.TRENDING);
                        }
                            break;
                        }
                    }
                };
                TrendingAislesContentParser.class.notify();
            }
            Looper.loop();
        }
    }
}
