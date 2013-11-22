package com.lateralthoughts.vue;

import java.io.File;
import java.util.ArrayList;
import com.lateralthoughts.vue.utils.FileCache;
import com.lateralthoughts.vue.utils.OtherSourceImageDetails;
import com.lateralthoughts.vue.utils.Utils;

import android.app.Activity;
import android.app.Dialog;
import android.os.Handler;
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
	private OtherSourcesImageAdapter mOtherSourcesImageAdapter;

	public OtherSourcesDialog(Activity activity) {
		mActivity = activity;
		mFileCache = new FileCache(mActivity);
	}

	public void showImageDailog(ArrayList<OtherSourceImageDetails> imagesList,
			final boolean fromLandingScreenFlag, final String sourceUrl) {
		final Dialog dialog = new Dialog(mActivity,
				R.style.Theme_Dialog_Translucent);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.other_sources_images_dailog);
		ListView listview = (ListView) dialog
				.findViewById(R.id.othersources_listview);
		this.imagesList = imagesList;
		mOtherSourcesImageAdapter = new OtherSourcesImageAdapter(mActivity,
				this.imagesList);
		listview.setAdapter(mOtherSourcesImageAdapter);
		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (OtherSourcesDialog.this.imagesList.get(position)
						.getImageUri() != null) {
					String picturePath = Utils.getPath(
							OtherSourcesDialog.this.imagesList.get(position)
									.getImageUri(), mActivity);
					if (!fromLandingScreenFlag) {
						final DataEntryFragment fragment = (DataEntryFragment) ((FragmentActivity) mActivity)
								.getSupportFragmentManager().findFragmentById(
										R.id.create_aisles_view_fragment);
						fragment.mOtherSourceSelectedImageUrl = OtherSourcesDialog.this.imagesList
								.get(position).getOriginUrl();
						fragment.mOtherSourceImageOriginalHeight = OtherSourcesDialog.this.imagesList
								.get(position).getHeight();
						fragment.mOtherSourceSelectedImageDetailsUrl = sourceUrl;
						fragment.mOtherSourceSelectedImageStore = Utils
								.getStoreNameFromUrl(sourceUrl);
						fragment.mOtherSourceImageOriginalWidth = OtherSourcesDialog.this.imagesList
								.get(position).getWidth();
						fragment.mFindAtText.setText(sourceUrl);
						fragment.mPreviousFindAtText = fragment.mFindAtText
								.getText().toString();
						fragment.mOtherSourceSelectedImageDetailsUrl = sourceUrl;
						OtherSourcesDialog.this.imagesList.clear();
						mOtherSourcesImageAdapter.notifyDataSetChanged();
						dialog.cancel();
						fragment.setGalleryORCameraImage(picturePath, false);
					} else {
						VueLandingPageActivity vueLandingPageActivity = (VueLandingPageActivity) mActivity;
						String otherSourceSelectedImageUrl = OtherSourcesDialog.this.imagesList
								.get(position).getOriginUrl();
						int otherSourceImageOriginalHeight = OtherSourcesDialog.this.imagesList
								.get(position).getHeight();
						String otherSourceSelectedImageDetailsUrl = sourceUrl;
						String otherSourceSelectedImageStore = Utils
								.getStoreNameFromUrl(sourceUrl);
						int otherSourceImageOriginalWidth = OtherSourcesDialog.this.imagesList
								.get(position).getWidth();
						OtherSourcesDialog.this.imagesList.clear();
						mOtherSourcesImageAdapter.notifyDataSetChanged();
						dialog.cancel();
						vueLandingPageActivity
								.showScreenSelectionForOtherSource(picturePath,
										otherSourceSelectedImageUrl,
										otherSourceImageOriginalWidth,
										otherSourceImageOriginalHeight,
										otherSourceSelectedImageDetailsUrl,
										otherSourceSelectedImageStore);
					}
				} else {
					File f = mFileCache.getVueAppResizedPictureFile(String
							.valueOf(OtherSourcesDialog.this.imagesList
									.get(position).getOriginUrl().hashCode()));
					if (f.exists()) {
						if (!fromLandingScreenFlag) {
							final DataEntryFragment fragment = (DataEntryFragment) ((FragmentActivity) mActivity)
									.getSupportFragmentManager()
									.findFragmentById(
											R.id.create_aisles_view_fragment);
							fragment.mOtherSourceSelectedImageUrl = OtherSourcesDialog.this.imagesList
									.get(position).getOriginUrl();
							fragment.mOtherSourceImageOriginalHeight = OtherSourcesDialog.this.imagesList
									.get(position).getHeight();
							fragment.mOtherSourceImageOriginalWidth = OtherSourcesDialog.this.imagesList
									.get(position).getWidth();
							fragment.mOtherSourceSelectedImageDetailsUrl = sourceUrl;
							fragment.mOtherSourceSelectedImageStore = Utils
									.getStoreNameFromUrl(sourceUrl);
							fragment.mFindAtText.setText(sourceUrl);
							fragment.mPreviousFindAtText = fragment.mFindAtText
									.getText().toString();
							fragment.mOtherSourceSelectedImageDetailsUrl = sourceUrl;
							OtherSourcesDialog.this.imagesList.clear();
							mOtherSourcesImageAdapter.notifyDataSetChanged();
							dialog.cancel();
							fragment.setGalleryORCameraImage(f.getPath(), true);
						} else {
							VueLandingPageActivity vueLandingPageActivity = (VueLandingPageActivity) mActivity;
							String otherSourceSelectedImageUrl = OtherSourcesDialog.this.imagesList
									.get(position).getOriginUrl();
							int otherSourceImageOriginalHeight = OtherSourcesDialog.this.imagesList
									.get(position).getHeight();
							String otherSourceSelectedImageDetailsUrl = sourceUrl;
							String otherSourceSelectedImageStore = Utils
									.getStoreNameFromUrl(sourceUrl);
							int otherSourceImageOriginalWidth = OtherSourcesDialog.this.imagesList
									.get(position).getWidth();
							OtherSourcesDialog.this.imagesList.clear();
							mOtherSourcesImageAdapter.notifyDataSetChanged();
							dialog.cancel();
							vueLandingPageActivity
									.showScreenSelectionForOtherSource(
											f.getPath(),
											otherSourceSelectedImageUrl,
											otherSourceImageOriginalWidth,
											otherSourceImageOriginalHeight,
											otherSourceSelectedImageDetailsUrl,
											otherSourceSelectedImageStore);
						}
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
