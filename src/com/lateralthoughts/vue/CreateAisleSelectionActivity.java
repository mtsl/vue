package com.lateralthoughts.vue;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.lateralthoughts.vue.ui.ArcMenu;
import com.lateralthoughts.vue.utils.ShoppingApplicationDetails;
import com.lateralthoughts.vue.utils.Utils;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

public class CreateAisleSelectionActivity extends Activity {
    
    private boolean mFromCreateAilseScreenFlag = false,
            mFromDetailsScreenFlag = false;
    private String mCameraImageName = null;
    private static final String GALLERY_ALERT_MESSAGE = "Select Picture";
    private static final String CAMERA_INTENT_NAME = "android.media.action.IMAGE_CAPTURE";
    private ArrayList<ShoppingApplicationDetails> mDataEntryShoppingApplicationsList = new ArrayList<ShoppingApplicationDetails>();
    private static final String CREATE_AISLE_POPUP = "Selection_Popup";
    private static final String CREATE_AISLE_POPUP_SELECTION = "CreateAisle_Selection_Popup";
    public static boolean isActivityShowing = false;
    private ArcMenu mDataentryArcMenu = null;
    private static final int ANIM_DELAY = 100;
    private boolean mIsClickedFlag = false;
    private ShareDialog mShareDialog = null;
    private Dialog mDialog;
    private Button mDataentryPopupCancelBtn;
    private TextView mDataentryPopupScreenTitle;
    private MixpanelAPI mixpanel;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        isActivityShowing = true;
        mixpanel = MixpanelAPI.getInstance(this, VueApplication.getInstance().MIXPANEL_TOKEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_asilse_selection);
        Bundle b = getIntent().getExtras();
        if (b != null) {
            mFromCreateAilseScreenFlag = b
                    .getBoolean(VueConstants.FROMCREATEAILSESCREENFLAG);
            mFromDetailsScreenFlag = b
                    .getBoolean(VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_FLAG);
        }
        mDataentryPopupScreenTitle = (TextView) findViewById(R.id.dataentry_popup_screen_title);
        if (mFromDetailsScreenFlag) {
            mDataentryPopupScreenTitle
                    .setText("Post image to this aisle \n Select One of the Sources");
        }
        mDataentryPopupCancelBtn = (Button) findViewById(R.id.dataentry_popup_cancel_btn);
        mDataentryPopupCancelBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (!mIsClickedFlag) {
                    mIsClickedFlag = true;
                    mDataentryArcMenu.arcLayout.switchState(true);
                }
            }
        });
        mDataentryArcMenu = (ArcMenu) findViewById(R.id.dataentry_arc_menu);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showPopUp();
            }
        }, ANIM_DELAY);
        if (VueApplication.getInstance().mShoppingApplicationDetailsList != null
                && VueApplication.getInstance().mShoppingApplicationDetailsList
                        .size() > 1) {
            for (int i = 1; i < VueApplication.getInstance().mShoppingApplicationDetailsList
                    .size(); i++) {
                ShoppingApplicationDetails shoppingApplicationDetails = new ShoppingApplicationDetails(
                        VueApplication.getInstance().mShoppingApplicationDetailsList
                                .get(i).getAppName(), VueApplication
                                .getInstance().mShoppingApplicationDetailsList
                                .get(i).getActivityName(), VueApplication
                                .getInstance().mShoppingApplicationDetailsList
                                .get(i).getPackageName(), VueApplication
                                .getInstance().mShoppingApplicationDetailsList
                                .get(i).getAppIcon());
                mDataEntryShoppingApplicationsList
                        .add(shoppingApplicationDetails);
                
            }
        }
        // More... to show the list of installed applications in the separate
        // dialog.
        ShoppingApplicationDetails shoppingApplicationDetails = new ShoppingApplicationDetails(
                getResources().getString(R.string.more), null, null, null);
        mDataEntryShoppingApplicationsList.add(shoppingApplicationDetails);
    }
    
    @Override
    protected void onStart() {
        VueApplication.getInstance().unregisterUser(mixpanel);
        mixpanel.track(CREATE_AISLE_POPUP_SELECTION, null);
        FlurryAgent.onStartSession(this, Utils.FLURRY_APP_KEY);
        FlurryAgent.onPageView();
        FlurryAgent.logEvent(CREATE_AISLE_POPUP);
        super.onStart();
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        FlurryAgent.onEndSession(this);
        mixpanel.flush();
        
    }
    
    public void cameraFunctionality() {
        SharedPreferences sharedPreferences = CreateAisleSelectionActivity.this
                .getSharedPreferences(VueConstants.SHAREDPREFERENCE_NAME, 0);
        boolean flag = sharedPreferences.getBoolean("dontshowpopup", false);
        if (flag) {
            cameraIntent();
        } else {
            openHintDialog("Camera", null, null, null);
        }
    }
    
    private void galleryIntent() {
        VueApplication.getInstance().registerUser(mixpanel);
        mixpanel.track("Added Image From Gallery", null);
        FlurryAgent.logEvent("ADD_IMAGE_GALLERY");
        Intent i = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(i, GALLERY_ALERT_MESSAGE),
                VueConstants.SELECT_PICTURE);
    }
    
    private void cameraIntent() {
        VueApplication.getInstance().registerUser(mixpanel);
        mixpanel.track("Added Image From Camera", null);
        FlurryAgent.logEvent("ADD_IMAGE_CAMERA");
        mCameraImageName = Utils
                .vueAppCameraImageFileName(CreateAisleSelectionActivity.this);
        File cameraImageFile = new File(mCameraImageName);
        Intent intent = new Intent(CAMERA_INTENT_NAME);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(cameraImageFile));
        startActivityForResult(intent, VueConstants.CAMERA_REQUEST);
    }
    
    public void galleryFunctionality() {
        SharedPreferences sharedPreferences = CreateAisleSelectionActivity.this
                .getSharedPreferences(VueConstants.SHAREDPREFERENCE_NAME, 0);
        boolean flag = sharedPreferences.getBoolean("dontshowpopup", false);
        if (flag) {
            galleryIntent();
        } else {
            openHintDialog("Gallery", null, null, null);
        }
    }
    
    public void moreClickFunctionality() {
        VueApplication.getInstance().registerUser(mixpanel);
        mixpanel.track("Added Image From Other Source", null);
        FlurryAgent.logEvent("ADD_IMAGE_MORE");
        if (mDataEntryShoppingApplicationsList != null
                && mDataEntryShoppingApplicationsList.size() > 0) {
            if (mShareDialog == null) {
                mShareDialog = new ShareDialog(this, this);
            }
            mShareDialog
                    .loadShareApplications(mDataEntryShoppingApplicationsList);
        } else {
            Toast.makeText(this, "There are no applications.",
                    Toast.LENGTH_LONG).show();
            finish();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        isActivityShowing = false;
    }
    
    public void loadShoppingApplication(String activityName,
            String packageName, String appName) {
        SharedPreferences sharedPreferences = CreateAisleSelectionActivity.this
                .getSharedPreferences(VueConstants.SHAREDPREFERENCE_NAME, 0);
        boolean flag = sharedPreferences.getBoolean("dontshowpopup", false);
        if (flag) {
            otherSourceIntent(activityName, packageName);
        } else {
            openHintDialog("OtherSource", appName, activityName, packageName);
        }
    }
    
    private void otherSourceIntent(String activityName, String packageName) {
        if (Utils.appInstalledOrNot(packageName, this)) {
            Intent shoppingAppIntent = new Intent(
                    android.content.Intent.ACTION_VIEW);
            shoppingAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Utils.setLoadDataentryScreenFlag(this, true);
            shoppingAppIntent.setClassName(packageName, activityName);
            finish();
            startActivity(shoppingAppIntent);
        } else {
            Toast.makeText(this, "Sorry, This application was not installed.",
                    Toast.LENGTH_LONG).show();
            finish();
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            // From Gallery...
            if (requestCode == VueConstants.SELECT_PICTURE) {
                // TODO: Gallery image
                Uri selectedImageUri = data.getData();
                // MEDIA GALLERY
                String selectedImagePath = Utils
                        .getPath(selectedImageUri, this);
                if (mFromDetailsScreenFlag) {
                    Intent intent = new Intent();
                    Bundle b = new Bundle();
                    b.putString(VueConstants.IMAGE_FROM, VueConstants.GALLERY_IMAGE);
                    b.putString(
                            VueConstants.CREATE_AISLE_CAMERA_GALLERY_IMAGE_PATH_BUNDLE_KEY,
                            selectedImagePath);
                    intent.putExtras(b);
                    setResult(
                            VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_ACTIVITY_RESULT,
                            intent);
                    finish();
                } else if (!mFromCreateAilseScreenFlag) {
                    Intent intent = new Intent(this, DataEntryActivity.class);
                    Bundle b = new Bundle();
                    b.putString(VueConstants.IMAGE_FROM, VueConstants.GALLERY_IMAGE);
                    b.putString(
                            VueConstants.CREATE_AISLE_CAMERA_GALLERY_IMAGE_PATH_BUNDLE_KEY,
                            selectedImagePath);
                    intent.putExtras(b);
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent();
                    Bundle b = new Bundle();
                    b.putString(VueConstants.IMAGE_FROM, VueConstants.GALLERY_IMAGE);
                    b.putString(
                            VueConstants.CREATE_AISLE_CAMERA_GALLERY_IMAGE_PATH_BUNDLE_KEY,
                            selectedImagePath);
                    intent.putExtras(b);
                    setResult(VueConstants.CREATE_AILSE_ACTIVITY_RESULT, intent);
                    finish();
                }
            }
            // From Camera...
            else if (requestCode == VueConstants.CAMERA_REQUEST) {
                
                // TODO: Camera Image
                File cameraImageFile = new File(mCameraImageName);
                if (cameraImageFile.exists()) {
                    if (mFromDetailsScreenFlag) {
                        Intent intent = new Intent();
                        Bundle b = new Bundle();
                        b.putString(VueConstants.IMAGE_FROM, VueConstants.CAMERA_IMAGE);
                        b.putString(
                                VueConstants.CREATE_AISLE_CAMERA_GALLERY_IMAGE_PATH_BUNDLE_KEY,
                                mCameraImageName);
                        intent.putExtras(b);
                        setResult(
                                VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_ACTIVITY_RESULT,
                                intent);
                        finish();
                    } else if (!mFromCreateAilseScreenFlag) {
                        Intent intent = new Intent(this,
                                DataEntryActivity.class);
                        Bundle b = new Bundle();
                        b.putString(VueConstants.IMAGE_FROM, VueConstants.CAMERA_IMAGE);
                        b.putString(
                                VueConstants.CREATE_AISLE_CAMERA_GALLERY_IMAGE_PATH_BUNDLE_KEY,
                                mCameraImageName);
                        intent.putExtras(b);
                        startActivity(intent);
                        finish();
                    } else {
                        Intent intent = new Intent();
                        Bundle b = new Bundle();
                        b.putString(VueConstants.IMAGE_FROM, VueConstants.CAMERA_IMAGE);
                        b.putString(
                                VueConstants.CREATE_AISLE_CAMERA_GALLERY_IMAGE_PATH_BUNDLE_KEY,
                                mCameraImageName);
                        intent.putExtras(b);
                        setResult(VueConstants.CREATE_AILSE_ACTIVITY_RESULT,
                                intent);
                        finish();
                    }
                } else {
                    finish();
                }
                
            }
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }
    }
    
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!mFromCreateAilseScreenFlag) {
                if (!mIsClickedFlag) {
                    mIsClickedFlag = true;
                    mDataentryArcMenu.arcLayout.switchState(true);
                }
            } else {
                showDiscardOtherAppImageDialog();
            }
        }
        return false;
    }
    
    private void showPopUp() {
        mDataentryArcMenu.arcLayout.setVisibility(View.VISIBLE);
        mDataentryArcMenu.arcLayout.switchState(true);
    }
    
    public void closeScreen() {
        finish();
    }
    
    public void showAlertMessageForAppInstalation(final String packageName,
            final String appName) {
        final Dialog dialog = new Dialog(this, R.style.Theme_Dialog_Translucent);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.vue_popup);
        TextView noButton = (TextView) dialog.findViewById(R.id.nobutton);
        TextView okButton = (TextView) dialog.findViewById(R.id.okbutton);
        TextView messagetext = (TextView) dialog.findViewById(R.id.messagetext);
        messagetext.setText("Install " + appName + " from Play Store");
        okButton.setText("OK");
        okButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
                Intent goToMarket = new Intent(Intent.ACTION_VIEW).setData(Uri
                        .parse("market://details?id=" + packageName));
                startActivity(goToMarket);
            }
        });
        noButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dialog.dismiss();
            }
        });
        dialog.setOnDismissListener(new OnDismissListener() {
            
            @Override
            public void onDismiss(DialogInterface arg0) {
                finish();
            }
        });
        dialog.show();
    }
    
    private void openHintDialog(final String source, String app,
            final String activityName, final String packageName) {
        mDialog = new Dialog(this, R.style.Theme_Dialog_Translucent);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(R.layout.hintdialog);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(false);
        TextView dialogtitle = (TextView) mDialog
                .findViewById(R.id.dialogtitle);
        ListView listview = (ListView) mDialog.findViewById(R.id.networklist);
        listview.setDivider(getResources().getDrawable(
                R.drawable.share_dialog_divider));
        TextView dontshow = (TextView) mDialog.findViewById(R.id.dontshow);
        TextView proceed = (TextView) mDialog.findViewById(R.id.proceed);
        ArrayList<String> hint_array_list = new ArrayList<String>();
        if (source.equalsIgnoreCase("Gallery")) {
            dialogtitle.setText("Gallery");
            hint_array_list.add("1. Go to gallery");
            hint_array_list.add("2. Find the right image");
            hint_array_list.add("3. Share(share icon) with vue(vue icon)");
            hint_array_list.add("4. Comeback to vue");
        } else if (source.equalsIgnoreCase("Camera")) {
            dialogtitle.setText("Camera");
            
            hint_array_list.add("1. Go to camera");
            hint_array_list.add("2. Take a picture");
            hint_array_list.add("3. Come back to vue");
        } else if (source.equalsIgnoreCase("OtherSource")) {
            dialogtitle.setText(app);
            String temp = "1. Proceed to " + app;
            hint_array_list.add(temp);
            hint_array_list.add("2. Select an image");
            hint_array_list.add("3. Share(share icon) with vue(vue icon)");
            hint_array_list.add("4. Come back to vue");
        }
        
        mDialog.show();
        mDialog.setOnDismissListener(new OnDismissListener() {
            
            @Override
            public void onDismiss(DialogInterface arg0) {
                // finish();
            }
        });
        dontshow.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = CreateAisleSelectionActivity.this
                        .getSharedPreferences(
                                VueConstants.SHAREDPREFERENCE_NAME, 0);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("dontshowpopup", true);
                editor.commit();
                mDialog.dismiss();
                if (source.equalsIgnoreCase("Gallery")) {
                    galleryIntent();
                } else if (source.equalsIgnoreCase("Camera")) {
                    cameraIntent();
                } else {
                    otherSourceIntent(activityName, packageName);
                }
            }
        });
        proceed.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
                if (source.equalsIgnoreCase("Gallery")) {
                    galleryIntent();
                } else if (source.equalsIgnoreCase("Camera")) {
                    cameraIntent();
                } else {
                    otherSourceIntent(activityName, packageName);
                }
            }
        });
        
        listview.setAdapter(new HintAdapter(hint_array_list, source,
                activityName, packageName));
        
    }
    
    private class HintAdapter extends BaseAdapter {
        ArrayList<String> mHintList;
        String mSource, mActivityName, mPackageName;
        
        public HintAdapter(ArrayList<String> hintList, String source,
                String activityName, String packageName) {
            mHintList = hintList;
            mSource = source;
            mActivityName = activityName;
            mPackageName = packageName;
        }
        
        @Override
        public int getCount() {
            return mHintList.size();
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
            
            Holder holder = null;
            if (convertView == null) {
                
                holder = new Holder();
                LayoutInflater mLayoutInflater = (LayoutInflater) CreateAisleSelectionActivity.this
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = mLayoutInflater.inflate(R.layout.hintpopup, null);
                holder.textone = (TextView) convertView
                        .findViewById(R.id.gmail);
                holder.texttwo = (TextView) convertView.findViewById(R.id.vue);
                holder.imageone = (ImageView) convertView
                        .findViewById(R.id.shareicon);
                holder.imagetwo = (ImageView) convertView
                        .findViewById(R.id.shareicon2);
                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }
            String text = mHintList.get(position);
            if (position == 0) {
                holder.textone.setOnClickListener(new OnClickListener() {
                    
                    @Override
                    public void onClick(View v) {
                        if (mDialog != null) {
                            mDialog.dismiss();
                        }
                        if (mSource.equals("Camera")) {
                            cameraIntent();
                        } else if (mSource.equals("Gallery")) {
                            galleryIntent();
                        } else {
                            otherSourceIntent(mActivityName, mPackageName);
                        }
                    }
                });
            }
            
            if (text.contains("share") && text.contains("with vue")) {
                holder.textone.setText("3. Share");
                holder.texttwo.setText("with vue");
                holder.imageone.setVisibility(View.VISIBLE);
                holder.imagetwo.setVisibility(View.VISIBLE);
                holder.texttwo.setVisibility(View.VISIBLE);
            } else {
                holder.imageone.setVisibility(View.GONE);
                holder.imagetwo.setVisibility(View.GONE);
                holder.texttwo.setVisibility(View.GONE);
                holder.textone.setText(text);
            }
            return convertView;
        }
    }
    
    private class Holder {
        TextView textone, texttwo;
        ImageView imageone, imagetwo;
    }
    
    private void showDiscardOtherAppImageDialog() {
        final Dialog dialog = new Dialog(this, R.style.Theme_Dialog_Translucent);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.vue_popup);
        final TextView noButton = (TextView) dialog.findViewById(R.id.nobutton);
        TextView yesButton = (TextView) dialog.findViewById(R.id.okbutton);
        TextView messagetext = (TextView) dialog.findViewById(R.id.messagetext);
        messagetext.setText("Do you want to cancel addImage?");
        yesButton.setText("Yes");
        noButton.setText("No");
        yesButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
                finish();
            }
        });
        noButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}
