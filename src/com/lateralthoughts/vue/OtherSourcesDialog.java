package com.lateralthoughts.vue;

import java.io.File;
import java.util.ArrayList;
import com.lateralthoughts.vue.utils.FileCache;
import com.lateralthoughts.vue.utils.OtherSourceImageDetails;
import com.lateralthoughts.vue.utils.Utils;

import android.app.Activity;
import android.app.Dialog;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

public class OtherSourcesDialog {
	private Activity mActivity = null;
	private FileCache mFileCache = null;
	private ArrayList<OtherSourceImageDetails> imagesList;

	public OtherSourcesDialog(Activity activity) {
		mActivity = activity;
		mFileCache = new FileCache(mActivity);
	}

	public void showImageDailog(ArrayList<OtherSourceImageDetails> imagesList) {
		final Dialog dialog = new Dialog(mActivity,
				R.style.Theme_Dialog_Translucent);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.other_sources_images_dailog);
		ListView listview = (ListView) dialog
				.findViewById(R.id.othersources_listview);
		this.imagesList = imagesList;
		listview.setAdapter(new OtherSourcesImageAdapter(mActivity,
				this.imagesList));
		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (OtherSourcesDialog.this.imagesList.get(position)
						.getImageUri() != null) {
					dialog.dismiss();
					String picturePath = Utils.getPath(
							OtherSourcesDialog.this.imagesList.get(position)
									.getImageUri(), mActivity);
					DataEntryFragment fragment = (DataEntryFragment) ((FragmentActivity) DataEntryActivity.mDataEntryActivityContext)
							.getSupportFragmentManager().findFragmentById(
									R.id.create_aisles_view_fragment);
					fragment.setGalleryORCameraImage(picturePath, false);
				} else {
					File f = mFileCache.getVueAppResizedPictureFile(String
							.valueOf(OtherSourcesDialog.this.imagesList
									.get(position).getOriginUrl().hashCode()));
					if (f.exists()) {
						dialog.dismiss();
						DataEntryFragment fragment = (DataEntryFragment) ((FragmentActivity) DataEntryActivity.mDataEntryActivityContext)
								.getSupportFragmentManager().findFragmentById(
										R.id.create_aisles_view_fragment);
						fragment.mFindAtText
								.setText(OtherSourcesDialog.this.imagesList
										.get(position).getOriginUrl());
						fragment.setGalleryORCameraImage(f.getPath(), true);
					} else {
						Toast.makeText(mActivity,
								"Please wait. Image is loading.",
								Toast.LENGTH_LONG).show();
					}
				}
			}
		});
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
	}
}
