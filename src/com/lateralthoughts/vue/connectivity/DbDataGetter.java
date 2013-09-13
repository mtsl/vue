package com.lateralthoughts.vue.connectivity;

import java.util.ArrayList;

import android.os.AsyncTask;
import android.util.Log;

import com.lateralthoughts.vue.AisleWindowContent;
import com.lateralthoughts.vue.VueApplication;
import com.lateralthoughts.vue.VueTrendingAislesDataModel;
import com.lateralthoughts.vue.ui.NotifyProgress;

public class DbDataGetter extends AsyncTask<String, Void, Void>{
	
	String category;
	ArrayList<AisleWindowContent> aisleWindowList;
	NotifyProgress mNotifyProgress;

	public DbDataGetter(NotifyProgress progress){
		mNotifyProgress = progress;
	}
	@Override
	protected void onPreExecute() {

		VueTrendingAislesDataModel.getInstance(VueApplication.getInstance()).clearContent();

		mNotifyProgress.showProgress();
		super.onPreExecute();
	}

	@Override
	protected Void doInBackground(String... params) {
		category = params[0];
		aisleWindowList = DataBaseManager.getInstance(VueApplication.getInstance()).getAislesByCategory(category);
		for (int i = 0; i < aisleWindowList.size(); i++) {
			if (aisleWindowList.get(i).getImageList().size() == 0) {
				Log.i("loading from db",
						"loading from db2 this window has no images:  "
								+ aisleWindowList.get(i).getAisleId());
				// Log.i("loading from db",
				// "loading from db2 this window has no images:  "+aisleWindowList.get(i).);
			}
		}
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {

		for (AisleWindowContent content : aisleWindowList) {

			AisleWindowContent aisleItem = VueTrendingAislesDataModel.getInstance(VueApplication.getInstance()).getAisleItem(content
					.getAisleId());
			aisleItem.addAisleContent(content.getAisleContext(),
					content.getImageList());
			// getAisleItem(content.getAisleId());
			VueTrendingAislesDataModel.getInstance(VueApplication.getInstance()).addItemToList(aisleItem.getAisleId(),aisleItem);

		}
		VueTrendingAislesDataModel.getInstance(VueApplication.getInstance()).dataObserver();
		VueTrendingAislesDataModel.getInstance(VueApplication.getInstance()).dismissProgress();
		super.onPostExecute(result);
	}
}
