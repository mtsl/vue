package com.lateralthoughts.vue.connectivity;

import java.util.ArrayList;

import android.os.AsyncTask;

import com.lateralthoughts.vue.AisleWindowContent;
import com.lateralthoughts.vue.VueApplication;
import com.lateralthoughts.vue.VueTrendingAislesDataModel;
import com.lateralthoughts.vue.ui.NotifyProgress;

public class DbDataGetter extends AsyncTask<String, Void, Void> {
    
    String mCategory;
    ArrayList<AisleWindowContent> mAisleWindowList;
    NotifyProgress mNotifyProgress;
    
    public DbDataGetter(NotifyProgress progress) {
        mNotifyProgress = progress;
    }
    
    @Override
    protected void onPreExecute() {
        
        VueTrendingAislesDataModel.getInstance(VueApplication.getInstance())
                .clearContent();
        
        mNotifyProgress.showProgress();
        super.onPreExecute();
    }
    
    @Override
    protected Void doInBackground(String... params) {
        mCategory = params[0];
        mAisleWindowList = DataBaseManager.getInstance(
                VueApplication.getInstance()).getAislesByCategory(mCategory);
        return null;
    }
    
    @Override
    protected void onPostExecute(Void result) {
        
        for (AisleWindowContent content : mAisleWindowList) {
            
            AisleWindowContent aisleItem = VueTrendingAislesDataModel
                    .getInstance(VueApplication.getInstance()).getAisleItem(
                            content.getAisleId());
            aisleItem.addAisleContent(content.getAisleContext(),
                    content.getImageList());
            VueTrendingAislesDataModel
                    .getInstance(VueApplication.getInstance()).addItemToList(
                            aisleItem.getAisleId(), aisleItem);
            
        }
        VueTrendingAislesDataModel.getInstance(VueApplication.getInstance())
                .dataObserver();
        VueTrendingAislesDataModel.getInstance(VueApplication.getInstance())
                .dismissProgress();
        super.onPostExecute(result);
    }
}
