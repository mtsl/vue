package com.lateralthoughts.vue;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.lateralthoughts.vue.utils.FileCache;
import com.lateralthoughts.vue.utils.OtherSourceImageDetails;
import com.lateralthoughts.vue.utils.Utils;

public class OtherSourcesDialog {
    private Activity mActivity = null;
    private FileCache mFileCache = null;
    private ArrayList<OtherSourceImageDetails> mImagesList;
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
        this.mImagesList = imagesList;
        mOtherSourcesImageAdapter = new OtherSourcesImageAdapter(mActivity,
                this.mImagesList);
        listview.setAdapter(mOtherSourcesImageAdapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                if (OtherSourcesDialog.this.mImagesList.get(position)
                        .getImageUri() != null) {
                    String picturePath = Utils.getPath(
                            OtherSourcesDialog.this.mImagesList.get(position)
                                    .getImageUri(), mActivity);
                    if (!fromLandingScreenFlag) {
                        final DataEntryFragment fragment = (DataEntryFragment) (mActivity)
                                .getFragmentManager().findFragmentById(
                                        R.id.create_aisles_view_fragment);
                        fragment.mOtherSourceSelectedImageUrl = OtherSourcesDialog.this.mImagesList
                                .get(position).getOriginUrl();
                        fragment.mOtherSourceImageOriginalHeight = OtherSourcesDialog.this.mImagesList
                                .get(position).getHeight();
                        fragment.mOtherSourceSelectedImageDetailsUrl = sourceUrl;
                        fragment.mOtherSourceSelectedImageStore = Utils
                                .getStoreNameFromUrl(sourceUrl);
                        fragment.mOtherSourceImageOriginalWidth = OtherSourcesDialog.this.mImagesList
                                .get(position).getWidth();
                        fragment.mFindAtText.setText(sourceUrl);
                        fragment.mPreviousFindAtText = fragment.mFindAtText
                                .getText().toString();
                        fragment.mOtherSourceSelectedImageDetailsUrl = sourceUrl;
                        OtherSourcesDialog.this.mImagesList.clear();
                        mOtherSourcesImageAdapter.notifyDataSetChanged();
                        dialog.cancel();
                        fragment.setGalleryORCameraImage(picturePath, false,
                                mActivity);
                    } else {
                        VueLandingPageActivity vueLandingPageActivity = (VueLandingPageActivity) mActivity;
                        String otherSourceSelectedImageUrl = OtherSourcesDialog.this.mImagesList
                                .get(position).getOriginUrl();
                        int otherSourceImageOriginalHeight = OtherSourcesDialog.this.mImagesList
                                .get(position).getHeight();
                        String otherSourceSelectedImageDetailsUrl = sourceUrl;
                        String otherSourceSelectedImageStore = Utils
                                .getStoreNameFromUrl(sourceUrl);
                        int otherSourceImageOriginalWidth = OtherSourcesDialog.this.mImagesList
                                .get(position).getWidth();
                        OtherSourcesDialog.this.mImagesList.clear();
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
                            .valueOf(OtherSourcesDialog.this.mImagesList
                                    .get(position).getOriginUrl().hashCode()));
                    if (f.exists()) {
                        if (!fromLandingScreenFlag) {
                            final DataEntryFragment fragment = (DataEntryFragment) (mActivity)
                                    .getFragmentManager().findFragmentById(
                                            R.id.create_aisles_view_fragment);
                            fragment.mOtherSourceSelectedImageUrl = OtherSourcesDialog.this.mImagesList
                                    .get(position).getOriginUrl();
                            fragment.mOtherSourceImageOriginalHeight = OtherSourcesDialog.this.mImagesList
                                    .get(position).getHeight();
                            fragment.mOtherSourceImageOriginalWidth = OtherSourcesDialog.this.mImagesList
                                    .get(position).getWidth();
                            fragment.mOtherSourceSelectedImageDetailsUrl = sourceUrl;
                            fragment.mOtherSourceSelectedImageStore = Utils
                                    .getStoreNameFromUrl(sourceUrl);
                            fragment.mFindAtText.setText(sourceUrl);
                            fragment.mPreviousFindAtText = fragment.mFindAtText
                                    .getText().toString();
                            fragment.mOtherSourceSelectedImageDetailsUrl = sourceUrl;
                            OtherSourcesDialog.this.mImagesList.clear();
                            mOtherSourcesImageAdapter.notifyDataSetChanged();
                            dialog.cancel();
                            fragment.setGalleryORCameraImage(f.getPath(), true,
                                    mActivity);
                        } else {
                            VueLandingPageActivity vueLandingPageActivity = (VueLandingPageActivity) mActivity;
                            String otherSourceSelectedImageUrl = OtherSourcesDialog.this.mImagesList
                                    .get(position).getOriginUrl();
                            int otherSourceImageOriginalHeight = OtherSourcesDialog.this.mImagesList
                                    .get(position).getHeight();
                            String otherSourceSelectedImageDetailsUrl = sourceUrl;
                            String otherSourceSelectedImageStore = Utils
                                    .getStoreNameFromUrl(sourceUrl);
                            int otherSourceImageOriginalWidth = OtherSourcesDialog.this.mImagesList
                                    .get(position).getWidth();
                            OtherSourcesDialog.this.mImagesList.clear();
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
                        Toast.makeText(mActivity, "Image is not downloaded.",
                                Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        Toast.makeText(mActivity, "Select Image", Toast.LENGTH_LONG).show();
    }
}
