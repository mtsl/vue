package com.lateralthoughts.vue;

import java.io.File;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.lateralthoughts.vue.user.VueUserProfile;
import com.lateralthoughts.vue.utils.FileCache;
import com.lateralthoughts.vue.utils.InstalledPackageRetriever;
import com.lateralthoughts.vue.utils.ShoppingApplicationDetails;
import com.lateralthoughts.vue.utils.Utils;
import com.lateralthoughts.vue.utils.clsShare;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

/**
 * 
 * common class for share functionality capable of handling
 * email,gmail,twitter,facebook and used action send intent to call the other
 * applications
 * 
 */
public class ShareDialog {
    
    private LayoutInflater mLayoutInflater;
    private AlertDialog.Builder mScreenDialog;
    private ArrayList<String> mAppNames = new ArrayList<String>();
    private ArrayList<String> mActivityNames = new ArrayList<String>();
    private ArrayList<String> mPackageNames = new ArrayList<String>();
    private ArrayList<String> mAppIconsPath = new ArrayList<String>();
    private Intent mSendIntent;
    private Context mContext;
    private Activity mActivity;
    private InstalledPackageRetriever mShareIntentObj;
    public boolean mShareIntentCalled = false;
    private Dialog mDialog;
    private int mCurrentAislePosition;
    private ArrayList<clsShare> mImagePathArray;
    private ProgressDialog mShareDialog;
    private boolean mFromCreateAislePopupFlag = false;
    private boolean mLoadAllApplications = false;
    private VueAisleDetailsViewFragment.ShareViaVueListner mDetailsScreenShareViaVueListner;
    private VueLandingPageActivity.ShareViaVueListner mLandingScreenShareViaVueListner;
    private DataEntryFragment.ShareViaVueListner mDataentryScreenShareViaVueListner;
    private ListView mListview = null;
    private MixpanelAPI mixpanel;
    private static final String APPLINK = "https://play.google.com/store/apps/details?id=com.lateralthoughts.vue";
    private JSONObject aisleSharedProps;
    private FileCache mFileCache = null;
    VueLandingPageActivity.OnShare shareIndicatorObject;
    
    public void dismisDialog() {
        mShareDialog.dismiss();
    }
    
    /**
     * 
     * @param context
     *            Context
     */
    public ShareDialog(Context context, Activity activity,
            MixpanelAPI mixpanel, JSONObject aisleSharedProps) {
        mFileCache = new FileCache(context);
        this.mixpanel = mixpanel;
        this.mContext = context;
        this.mActivity = activity;
        this.aisleSharedProps = aisleSharedProps;
        mLayoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    
    public void share(
            ArrayList<clsShare> imagePathArray,
            String aisleTitle,
            String name,
            int currentAislePosition,
            VueAisleDetailsViewFragment.ShareViaVueListner detailsScreenShareViaVueListner,
            DataEntryFragment.ShareViaVueListner dataentryScreenShareViaVueListner,
            VueLandingPageActivity.ShareViaVueListner landingScreenShareViaVueListner,VueLandingPageActivity.OnShare shareIndicatorObj) {  
        shareIndicatorObject = shareIndicatorObj;
        mShareIntentCalled = false;
        mDetailsScreenShareViaVueListner = detailsScreenShareViaVueListner;
        mDataentryScreenShareViaVueListner = dataentryScreenShareViaVueListner;
        mLandingScreenShareViaVueListner = landingScreenShareViaVueListner;
        this.mImagePathArray = imagePathArray;
        mCurrentAislePosition = currentAislePosition;
        prepareShareIntentData();
        openScreenDialog();
    }
    
    private void showAllInstalledApplications() {
        mLoadAllApplications = true;
        prepareDisplayData(VueApplication.getInstance().mMoreInstalledApplicationDetailsList);
        mListview.setAdapter(new CustomAdapter());
    }
    
    public void loadShareApplications(
            ArrayList<ShoppingApplicationDetails> dataEntryShoppingApplicationsList) {
        mFromCreateAislePopupFlag = true;
        if (mAppNames.size() == 0) {
            prepareDisplayData(dataEntryShoppingApplicationsList);
        }
        openScreenDialog();
    }
    
    /** to show pop-up */
    private void openScreenDialog() { 
        mShareDialog = ProgressDialog.show(mContext, mContext
                .getString(R.string.app_name), mContext.getResources()
                .getString(R.string.sharing_mesg), true);
        mShareDialog.dismiss();
        mDialog = new Dialog(mContext, R.style.Theme_Dialog_Translucent);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(R.layout.sharedialogue);
        TextView dialogtitle = (TextView) mDialog
                .findViewById(R.id.dialogtitle);
        mListview = (ListView) mDialog.findViewById(R.id.networklist);
        TextView okbuton = (TextView) mDialog.findViewById(R.id.shownetworkok);
        if (mFromCreateAislePopupFlag || mLoadAllApplications) {
            dialogtitle.setText("Open ...");
        }
        mListview.setAdapter(new CustomAdapter());
        mListview.setDivider(mContext.getResources().getDrawable(
                R.drawable.share_dialog_divider));
        mListview.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapter, View v,
                    int position, long id) {
                if(shareIndicatorObject != null){
                shareIndicatorObject.onShare(true);
                }
                if (mFromCreateAislePopupFlag) {
                    CreateAisleSelectionActivity createAisleSelectionActivity = (CreateAisleSelectionActivity) mContext;
                    if (createAisleSelectionActivity != null) {
                        if (mPackageNames.get(position) == null) {
                            if (mAppNames.get(position).equals(
                                    mContext.getResources().getString(
                                            R.string.more))) {
                                // show another dialog for displaying installed
                                // apps...
                                showAllInstalledApplications();
                            }
                        } else {
                            mDialog.dismiss();
                            createAisleSelectionActivity
                                    .loadShoppingApplication(
                                            mActivityNames.get(position),
                                            mPackageNames.get(position),
                                            mAppNames.get(position));
                        }
                    }
                } else if (mLoadAllApplications) {
                    mDialog.dismiss();
                    CreateAisleSelectionActivity createAisleSelectionActivity = (CreateAisleSelectionActivity) mContext;
                    createAisleSelectionActivity.loadShoppingApplication(
                            mActivityNames.get(position),
                            mPackageNames.get(position),
                            mAppNames.get(position));
                } else {
                    String sharedVia = shareIntent(position);
                    
                    try {
                        if (aisleSharedProps != null && sharedVia != null)
                            aisleSharedProps.put("Shared Via", sharedVia);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (mixpanel != null)
                        mixpanel.track("Aisle Shared", aisleSharedProps);
                }
            }
        });
        mDialog.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface arg0) {
                try {
                    mAppNames.clear();
                    mActivityNames.clear();
                    mAppIconsPath.clear();
                    mPackageNames.clear();
                } catch (Exception e) {
                }
                mShareIntentObj = null;
            }
        });
        mDialog.show();
        okbuton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
                InputMethodManager i1pm = (InputMethodManager) mContext
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                i1pm.hideSoftInputFromWindow(null, 0);
            }
        });
        mScreenDialog = new AlertDialog.Builder(mContext);
        mScreenDialog.setTitle(mContext.getResources().getString(
                R.string.share_via_mesg));
    }
    
    /**
     * Adapter to set on popup dialogue
     * 
     * */
    private class CustomAdapter extends BaseAdapter {
        /**
         * returns the count
         * 
         * @return int
         */
        public int getCount() {
            return mAppNames.size();
        }
        
        /**
         * @param arg0
         *            int
         * @return Object
         */
        public Object getItem(int arg0) {
            return arg0;
        }
        
        /**
         * @param arg0
         *            int
         * @return long
         */
        public long getItemId(int arg0) {
            return arg0;
        }
        
        /**
         * @param position
         *            int
         * 
         * @param convertView
         *            View
         * @param parent
         *            ViewGroup
         * @return View
         */
        public View getView(final int position, View convertView,
                ViewGroup parent) {
            Holder holderView = null;
            if (convertView == null) {
                holderView = new Holder();
                convertView = mLayoutInflater.inflate(R.layout.sharedialog_row,
                        null);
                holderView.network = (TextView) convertView
                        .findViewById(R.id.gmail);
                holderView.launcheicon = (ImageView) convertView
                        .findViewById(R.id.launchericon);
                convertView.setTag(holderView);
            } else {
                holderView = (Holder) convertView.getTag();
            }
            if (mAppIconsPath.get(position) != null) {
                try {
                    holderView.launcheicon.setImageDrawable(mContext
                            .getPackageManager().getApplicationIcon(
                                    mPackageNames.get(position)));
                } catch (Exception e) {
                }
            } else {
                if (mAppNames.get(position).equals(
                        mContext.getResources().getString(R.string.more))) {
                    holderView.launcheicon
                            .setImageResource(R.drawable.more_icon);
                } else if (mAppNames.get(position).equals(
                        mContext.getResources().getString(R.string.browser))) {
                    holderView.launcheicon
                            .setImageResource(R.drawable.browser_icon);
                } else {
                    try {
                        holderView.launcheicon.setImageDrawable(mContext
                                .getPackageManager().getApplicationIcon(
                                        mPackageNames.get(position)));
                    } catch (Exception e) {
                    }
                }
            }
            holderView.network.setText(mAppNames.get(position));
            return convertView;
        }
    }
    
    private String shareIntent(final int position) {
        mShareDialog.show();
        String shareVia = null;
        try {
            if (mAppNames.get(position).equalsIgnoreCase(
                    VueConstants.FACEBOOK_APP_NAME)) {
                shareVia = "Facebook";
                mDialog.dismiss();
                mShareDialog.dismiss();
                String shareText = "";
                // User Aisle...
                if (mImagePathArray.get(0).isUserAisle().equals("1")) {
                    shareText = mImagePathArray.get(0).getAisleOwnerName()
                            + " would like your opinion in finding "
                            + mImagePathArray.get(0).getLookingFor()
                            + ". Please help out by liking the picture you choose. Get Vue to create your own aisles and help more. "
                            + APPLINK;
                    
                } else {
                    String userName = null;
                    if (VueApplication.getInstance().getmUserName() != null) {
                        userName = VueApplication.getInstance().getmUserName();
                    } else {
                        VueUserProfile storedUserProfile = null;
                        try {
                            storedUserProfile = Utils
                                    .readUserProfileObjectFromFile(
                                            mContext,
                                            VueConstants.VUE_APP_USERPROFILEOBJECT__FILENAME);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (storedUserProfile != null) {
                            userName = storedUserProfile.getUserName();
                        }
                    }
                    shareText = userName
                            + " would like you to check this aisle out on Vue - "
                            + mImagePathArray.get(0).getLookingFor() + " by "
                            + mImagePathArray.get(0).getAisleOwnerName()
                            + ". Get Vue to create your own aisles! " + APPLINK;
                }
                Intent i = new Intent(mContext, VueLoginActivity.class);
                Bundle b = new Bundle();
                b.putBoolean(VueConstants.CANCEL_BTN_DISABLE_FLAG, false);
                b.putString(VueConstants.FROM_INVITEFRIENDS, null);
                b.putBoolean(VueConstants.FBLOGIN_FROM_DETAILS_SHARE, true);
                b.putBoolean(VueConstants.FROM_BEZELMENU_LOGIN, false);
                b.putString(VueConstants.FBPOST_TEXT, shareText);
                b.putParcelableArrayList(VueConstants.FBPOST_IMAGEURLS,
                        mImagePathArray);
                i.putExtras(b);
                mContext.startActivity(i);
            } else if (mAppNames.get(position).equalsIgnoreCase(
                    VueConstants.GOOGLEPLUS_APP_NAME)) {
                shareVia = "GooglePlus";
                shareImageAndText(position);
            } else if (mAppNames.get(position).equalsIgnoreCase(
                    VueConstants.GMAIL_APP_NAME)) {
                shareVia = "Gmail";
                shareImageAndText(position);
            } else if (mAppNames.get(position).equalsIgnoreCase(
                    VueConstants.INSTAGRAM_APP_NAME)) {
                shareVia = "Instagram";
                shareSingleImage(position, mCurrentAislePosition, false);
            } else if (mAppNames.get(position).equalsIgnoreCase(
                    VueConstants.TWITTER_APP_NAME)) {
                shareVia = "Twitter";
                shareSingleImage(position, mCurrentAislePosition, true);
            } else if (mAppNames.get(position)
                    .equalsIgnoreCase(
                            mContext.getApplicationContext()
                                    .getApplicationInfo()
                                    .loadLabel(mContext.getPackageManager())
                                    .toString())) {
                shareVia = "Vue";
                if (mImagePathArray.get(mCurrentAislePosition).getAisleId() != null
                        && mImagePathArray.get(mCurrentAislePosition)
                                .getImageId() != null) {
                    shareToVue(mImagePathArray.get(mCurrentAislePosition)
                            .getAisleId(),
                            mImagePathArray.get(mCurrentAislePosition)
                                    .getImageId());
                } else {
                    shareSingleImage(position, mCurrentAislePosition, false);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            mDialog.dismiss();
            mShareDialog.dismiss();
            showAlertMessageShareError(mAppNames.get(position), false);
        }
        return shareVia;
    }
    
    private void showAlertMessageShareError(String appName, boolean fberror) {
        String message = "";
         if(!fberror){
             message = "Unable to Share content to " + appName;
         } else {
             message = appName;
         }
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
        alertDialogBuilder.setMessage(message);
         alertDialogBuilder.setPositiveButton("OK",new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int id) {
                    dialog.cancel();
               }
              });
         AlertDialog alertDialog = alertDialogBuilder.create();
         alertDialog.show();
    }
    
    private void prepareShareIntentData() {
        if (mShareIntentObj == null) {
            mSendIntent = new Intent(android.content.Intent.ACTION_SEND);
            mSendIntent.setType("text/plain");
            mShareIntentObj = new InstalledPackageRetriever(mContext);
            mShareIntentObj.getInstalledPackages();
            mAppNames = mShareIntentObj.getAppNames();
            mPackageNames = mShareIntentObj.getpackageNames();
            mAppIconsPath = mShareIntentObj.getDrawables();
        }
    }
    
    private void prepareDisplayData(
            ArrayList<ShoppingApplicationDetails> dataEntryShoppingApplicationsList) {
        if (dataEntryShoppingApplicationsList != null
                && dataEntryShoppingApplicationsList.size() > 0) {
            mAppNames.clear();
            mActivityNames.clear();
            mAppIconsPath.clear();
            mPackageNames.clear();
            for (int i = 0; i < dataEntryShoppingApplicationsList.size(); i++) {
                mAppNames.add(dataEntryShoppingApplicationsList.get(i)
                        .getAppName());
                mPackageNames.add(dataEntryShoppingApplicationsList.get(i)
                        .getPackageName());
                mAppIconsPath.add(dataEntryShoppingApplicationsList.get(i)
                        .getAppIconPath());
                mActivityNames.add(dataEntryShoppingApplicationsList.get(i)
                        .getActivityName());
            }
        }
    }
    
    private class Holder {
        TextView network;
        ImageView launcheicon;
    }
    
    private void shareToVue(String aisleId, String imageId) {
        VueApplication.getInstance().mShareViaVueClickedFlag = true;
        VueApplication.getInstance().mShareViaVueClickedAisleId = aisleId;
        VueApplication.getInstance().mShareViaVueClickedImageId = imageId;
        mDialog.dismiss();
        if (mDataentryScreenShareViaVueListner != null) {
            mDataentryScreenShareViaVueListner.onAisleShareToVue();
        } else if (mDetailsScreenShareViaVueListner != null) {
            mDetailsScreenShareViaVueListner.onAisleShareToVue();
        } else if (mLandingScreenShareViaVueListner != null) {
            mLandingScreenShareViaVueListner.onAisleShareToVue();
        }
    }
    
    private void shareImageAndText(final int position) {
        mDialog.dismiss();
        mShareIntentCalled = true;
        mShareDialog.show();
        Thread t = new Thread(new Runnable() {
            @SuppressWarnings({ "rawtypes", "unchecked" })
            @Override
            public void run() {
                ArrayList<Uri> imageUris = new ArrayList<Uri>();
                if (mImagePathArray != null && mImagePathArray.size() > 0) {
                    for (int i = 0; i < mImagePathArray.size(); i++) {
                        final File f = new File(mImagePathArray.get(i)
                                .getFilepath());
                        if (!f.exists()) {
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
                            if (mImagePathArray.get(i).getImageUrl() != null) {
                                ImageRequest imagerequestObj = new ImageRequest(
                                        mImagePathArray.get(i).getImageUrl(),
                                        listener, 0, 0, null, errorListener);
                                VueApplication.getInstance().getRequestQueue()
                                        .add(imagerequestObj);
                            }
                        }
                        Uri screenshotUri = Uri.fromFile(f);
                        imageUris.add(screenshotUri);
                    }
                }
                String shareText = "";
                // User Aisle...
                if (mImagePathArray.get(0).isUserAisle().equals("1")) {
                    shareText = mImagePathArray.get(0).getAisleOwnerName()
                            + " would like your opinion in finding "
                            + mImagePathArray.get(0).getLookingFor()
                            + ". Please help out by liking the picture you choose. Get Vue to create your own aisles and help more. "
                            + APPLINK;
                } else {
                    String userName = null;
                    if (VueApplication.getInstance().getmUserName() != null) {
                        userName = VueApplication.getInstance().getmUserName();
                    } else {
                        VueUserProfile storedUserProfile = null;
                        try {
                            storedUserProfile = Utils
                                    .readUserProfileObjectFromFile(
                                            mContext,
                                            VueConstants.VUE_APP_USERPROFILEOBJECT__FILENAME);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (storedUserProfile != null) {
                            userName = storedUserProfile.getUserName();
                        }
                    }
                    shareText = userName
                            + " would like you to check this aisle out on Vue - "
                            + mImagePathArray.get(0).getLookingFor() + " by "
                            + mImagePathArray.get(0).getAisleOwnerName()
                            + ". Get Vue to create your own aisles! " + APPLINK;
                }
                mSendIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
                mSendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("mailto:"));
                mSendIntent.putExtra(android.content.Intent.EXTRA_TEXT,
                        shareText);
                mSendIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM,
                        imageUris);
                String activityname = null;
                if (mAppNames.get(position).equals(VueConstants.GMAIL_APP_NAME)) {
                    activityname = VueConstants.GMAIL_ACTIVITY_NAME;
                } else if (mAppNames.get(position).equals(
                        VueConstants.GOOGLEPLUS_APP_NAME)) {
                    activityname = VueConstants.GOOGLEPLUS_ACTIVITY_NAME;
                } else if (mAppNames.get(position).equals(
                        VueConstants.TWITTER_APP_NAME)) {
                    activityname = VueApplication.getInstance().twitterActivityName;
                } else if (mAppNames.get(position).equals(
                        VueConstants.INSTAGRAM_APP_NAME)) {
                    activityname = VueConstants.INSTAGRAM_ACTIVITY_NAME;
                }
                mSendIntent.setClassName(mPackageNames.get(position),
                        activityname);
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mScreenDialog.setCancelable(true);
                        mActivity.startActivityForResult(mSendIntent,
                                VueConstants.SHARE_INTENT_REQUEST_CODE);
                    }
                });
            }
        });
        t.start();
    }
    
    private void shareSingleImage(final int position,
            final int currentAislePosition, final boolean fromTwitterApp) {
        mDialog.dismiss();
        mShareIntentCalled = true;
        mShareDialog.show();
        Thread t = new Thread(new Runnable() {
            @SuppressWarnings({ "rawtypes", "unchecked" })
            @Override
            public void run() {
                Uri imageUri = null;
                if (mImagePathArray != null && mImagePathArray.size() > 0) {
                    final File f = new File(mImagePathArray.get(
                            currentAislePosition).getFilepath());
                    if (!f.exists()) {
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
                        if (mImagePathArray.get(currentAislePosition)
                                .getImageUrl() != null) {
                            ImageRequest imagerequestObj = new ImageRequest(
                                    mImagePathArray.get(currentAislePosition)
                                            .getImageUrl(), listener, 0, 0,
                                    null, errorListener);
                            VueApplication.getInstance().getRequestQueue()
                                    .add(imagerequestObj);
                        }
                    }
                    imageUri = Uri.fromFile(f);
                }
                String shareText = "";
                if (fromTwitterApp) {
                    shareText = mImagePathArray.get(0).getLookingFor() + " by "
                            + mImagePathArray.get(0).getAisleOwnerName() + ". "
                            + APPLINK;
                } else {
                    // User Aisle...
                    if (mImagePathArray.get(0).isUserAisle().equals("1")) {
                        shareText = mImagePathArray.get(0).getAisleOwnerName()
                                + " would like your opinion in finding "
                                + mImagePathArray.get(0).getLookingFor()
                                + ". Please help out by liking the picture you choose. Get Vue to create your own aisles and help more. "
                                + APPLINK;
                    } else {
                        String userName = null;
                        if (VueApplication.getInstance().getmUserName() != null) {
                            userName = VueApplication.getInstance()
                                    .getmUserName();
                        } else {
                            VueUserProfile storedUserProfile = null;
                            try {
                                storedUserProfile = Utils
                                        .readUserProfileObjectFromFile(
                                                mContext,
                                                VueConstants.VUE_APP_USERPROFILEOBJECT__FILENAME);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (storedUserProfile != null) {
                                userName = storedUserProfile.getUserName();
                            }
                        }
                        shareText = userName
                                + " would like you to check this aisle out on Vue - "
                                + mImagePathArray.get(0).getLookingFor()
                                + " by "
                                + mImagePathArray.get(0).getAisleOwnerName()
                                + ". Get Vue to create your own aisles! "
                                + APPLINK;
                    }
                }
                mSendIntent.setAction(Intent.ACTION_SEND);
                mSendIntent.setType("image/*");
                mSendIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                mSendIntent.putExtra(android.content.Intent.EXTRA_TEXT,
                        shareText);
                String activityname = null;
                if (mAppNames.get(position).equals(VueConstants.GMAIL_APP_NAME)) {
                    activityname = VueConstants.GMAIL_ACTIVITY_NAME;
                } else if (mAppNames.get(position).equals(
                        VueConstants.GOOGLEPLUS_APP_NAME)) {
                    activityname = VueConstants.GOOGLEPLUS_ACTIVITY_NAME;
                } else if (mAppNames.get(position).equals(
                        VueConstants.TWITTER_APP_NAME)) {
                    activityname = VueApplication.getInstance().twitterActivityName;
                } else if (mAppNames.get(position).equals(
                        VueConstants.INSTAGRAM_APP_NAME)) {
                    activityname = VueConstants.INSTAGRAM_ACTIVITY_NAME;
                } else if (mAppNames.get(position).equalsIgnoreCase(
                        mContext.getApplicationContext().getApplicationInfo()
                                .loadLabel(mContext.getPackageManager())
                                .toString())) {
                    activityname = VueConstants.VUE_ACTIVITY_NAME;
                }
                mSendIntent.setClassName(mPackageNames.get(position),
                        activityname);
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mScreenDialog.setCancelable(true);
                        mActivity.startActivityForResult(mSendIntent,
                                VueConstants.SHARE_INTENT_REQUEST_CODE);
                    }
                });
            }
        });
        t.start();
    }
    
    public interface ShareViaVueClickedListner {
        public void onAisleShareToVue();
    }
   
}