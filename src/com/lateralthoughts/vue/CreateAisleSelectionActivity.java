package com.lateralthoughts.vue;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
    private static final String CREATE_AISLE_POPUP_SELECTION = "Source Selection options presented";
    public static boolean isActivityShowing = false;
    private ArcMenu mDataentryArcMenu = null;
    private static final int ANIM_DELAY = 100;
    private boolean mIsClickedFlag = false;
    private ShareDialog mShareDialog = null;
    private AlertDialog.Builder mAlertDialogBuilder;
    private Button mDataentryPopupCancelBtn;
    private TextView mDataentryPopupScreenTitle;
    private MixpanelAPI mixpanel;
    private AlertDialog mAlertDialog;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        isActivityShowing = true;
        mixpanel = MixpanelAPI.getInstance(this,
                VueApplication.getInstance().MIXPANEL_TOKEN);
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
                                .get(i).getAppIconPath());
                mDataEntryShoppingApplicationsList
                        .add(shoppingApplicationDetails);
                
            }
        }
    }
    
    @Override
    protected void onStart() {
        mixpanel.track(CREATE_AISLE_POPUP_SELECTION, null);
        super.onStart();
    }
    
    @Override
    protected void onStop() {
        super.onStop();
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
        mixpanel.track("Selected Gallery", null);
        Intent i = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(i, GALLERY_ALERT_MESSAGE),
                VueConstants.SELECT_PICTURE);
    }
    
    private void cameraIntent() {
        mixpanel.track("Selected Camera", null);
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
        mixpanel.track("Selected Other Source", null);
        if (mDataEntryShoppingApplicationsList != null
                && mDataEntryShoppingApplicationsList.size() > 0) {
            if (mShareDialog == null) {
                mShareDialog = new ShareDialog(this, this, null, null);
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
                    b.putString(VueConstants.IMAGE_FROM,
                            VueConstants.GALLERY_IMAGE);
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
                    b.putString(VueConstants.IMAGE_FROM,
                            VueConstants.GALLERY_IMAGE);
                    b.putString(
                            VueConstants.CREATE_AISLE_CAMERA_GALLERY_IMAGE_PATH_BUNDLE_KEY,
                            selectedImagePath);
                    intent.putExtras(b);
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent();
                    Bundle b = new Bundle();
                    b.putString(VueConstants.IMAGE_FROM,
                            VueConstants.GALLERY_IMAGE);
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
                        b.putString(VueConstants.IMAGE_FROM,
                                VueConstants.CAMERA_IMAGE);
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
                        b.putString(VueConstants.IMAGE_FROM,
                                VueConstants.CAMERA_IMAGE);
                        b.putString(
                                VueConstants.CREATE_AISLE_CAMERA_GALLERY_IMAGE_PATH_BUNDLE_KEY,
                                mCameraImageName);
                        intent.putExtras(b);
                        startActivity(intent);
                        finish();
                    } else {
                        Intent intent = new Intent();
                        Bundle b = new Bundle();
                        b.putString(VueConstants.IMAGE_FROM,
                                VueConstants.CAMERA_IMAGE);
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
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Vue");
        alertDialogBuilder
                .setMessage("Install " + appName + " from Play Store");
        alertDialogBuilder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        Intent goToMarket = new Intent(Intent.ACTION_VIEW)
                                .setData(Uri.parse("market://details?id="
                                        + packageName));
                        startActivity(goToMarket);
                    }
                });
        alertDialogBuilder.setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        
                        dialog.cancel();
                        
                    }
                });
        alertDialogBuilder.setOnCancelListener(new OnCancelListener() {
            
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
                
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
    
    private void openHintDialog(final String source, String app,
            final String activityName, final String packageName) {
        mAlertDialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(
                CreateAisleSelectionActivity.this, R.style.AppBaseTheme));
        mAlertDialogBuilder.setCancelable(false);
        mAlertDialogBuilder.setTitle(getResources()
                .getString(R.string.app_name));
        mAlertDialogBuilder.setPositiveButton("Don't show again",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SharedPreferences sharedPreferences = CreateAisleSelectionActivity.this
                                .getSharedPreferences(
                                        VueConstants.SHAREDPREFERENCE_NAME, 0);
                        SharedPreferences.Editor editor = sharedPreferences
                                .edit();
                        editor.putBoolean("dontshowpopup", true);
                        editor.commit();
                        dialog.cancel();
                        if (source.equalsIgnoreCase("Gallery")) {
                            galleryIntent();
                        } else if (source.equalsIgnoreCase("Camera")) {
                            cameraIntent();
                        } else {
                            otherSourceIntent(activityName, packageName);
                        }
                    }
                });
        mAlertDialogBuilder.setNegativeButton("Proceed",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        if (source.equalsIgnoreCase("Gallery")) {
                            galleryIntent();
                        } else if (source.equalsIgnoreCase("Camera")) {
                            cameraIntent();
                        } else {
                            otherSourceIntent(activityName, packageName);
                        }
                    }
                });
        ListView listview = new ListView(CreateAisleSelectionActivity.this);
        final ArrayList<String> hint_array_list = new ArrayList<String>();
        if (source.equalsIgnoreCase("Gallery")) {
            mAlertDialogBuilder.setTitle("Gallery");
            hint_array_list.add("1. Go to gallery");
            hint_array_list.add("2. Find the right image");
            hint_array_list.add("3. Share with vue");
            hint_array_list.add("4. Comeback to vue");
        } else if (source.equalsIgnoreCase("Camera")) {
            mAlertDialogBuilder.setTitle("Camera");
            hint_array_list.add("1. Go to camera");
            hint_array_list.add("2. Take a picture");
            hint_array_list.add("3. Come back to vue");
        } else if (source.equalsIgnoreCase("OtherSource")) {
            mAlertDialogBuilder.setTitle(app);
            String temp = "1. Proceed to " + app;
            hint_array_list.add(temp);
            hint_array_list.add("2. Select an image");
            hint_array_list.add("3. Share with vue");
            hint_array_list.add("4. Come back to vue");
        }
        ListAdapter adapter = new ArrayAdapter<String>(
                CreateAisleSelectionActivity.this,
                android.R.layout.select_dialog_item, android.R.id.text1,
                hint_array_list) {
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                TextView tv = (TextView) v.findViewById(android.R.id.text1);
                tv.setTextSize(16);
                return v;
            }
        };
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapter, View v,
                    int position, long id) {
                if (position == 0) {
                    mAlertDialog.dismiss();
                    if (source.equals("Camera")) {
                        cameraIntent();
                    } else if (source.equals("Gallery")) {
                        galleryIntent();
                    } else {
                        otherSourceIntent(activityName, packageName);
                    }
                }
            }
        });
        mAlertDialogBuilder.setView(listview);
        mAlertDialog = mAlertDialogBuilder.create();
        mAlertDialog.show();
    }
    
    private void showDiscardOtherAppImageDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Do you want to cancel addImage?");
        alertDialogBuilder.setTitle("Vue");
        alertDialogBuilder.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        finish();
                    }
                });
        alertDialogBuilder.setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        
                        dialog.cancel();
                        
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
