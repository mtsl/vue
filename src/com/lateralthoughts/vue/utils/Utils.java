package com.lateralthoughts.vue.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings.Secure;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Toast;

import com.lateralthoughts.vue.AisleImageDetails;
import com.lateralthoughts.vue.DataentryImage;
import com.lateralthoughts.vue.R;
import com.lateralthoughts.vue.VueApplication;
import com.lateralthoughts.vue.VueConstants;
import com.lateralthoughts.vue.user.VueUser;
import com.lateralthoughts.vue.user.VueUserProfile;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

public class Utils {
    private static final String CURRENT_FONT_SIZE = "currentFontSize";
    public static final String NETWORK_SETTINGS = "networkSettings";
    public static final int LARGE_TEXT_SIZE = 22;
    public static final int MEDIUM_TEXT_SIZE = 18;
    public static final int SMALL_TEXT_SIZE = 14;
    public static final String DETAILS_SCREEN = "details_screen";
    public static final String TRENDING_SCREEN = "trending_screen";
    public static final String FLURRY_APP_KEY = "6938R8DC7R5HZWF976TJ";
    public static int MAX_RETRIES = 3;
    public static String mChangeAilseId;
    public static boolean sIsAisleChanged = false;
    public static boolean sAinmate = true;
    public static int sUserPoints;
    public static boolean sIsLoged = true;
    public static int sAisleParserCount = -1;
    
    public static void CopyStream(InputStream is, OutputStream os) {
        final int buffer_size = 1024;
        try {
            byte[] bytes = new byte[buffer_size];
            for (;;) {
                int count = is.read(bytes, 0, buffer_size);
                if (count == -1)
                    break;
                os.write(bytes, 0, count);
            }
            os.close();
            is.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public static String addImageInfo(String url, int width, int height) {
        if (url.contains("width") && url.contains("height")) {
            return url;
        }
        StringBuilder modifiedUrl = new StringBuilder(url);
        modifiedUrl.append("?width=" + width);
        modifiedUrl.append("&height=" + height);
        return modifiedUrl.toString();
    }
    
    /**
     * To get the CURRENT_FONT_SIZE value stored in SharedPreferences. it will
     * return default value which is MEDIUM_TEXT_SIZE (18sp) if not value is
     * stored in SharedPreferences.
     * 
     * @param Context
     *            context.
     * @return int Current font size from SharedPreferences.
     * */
    public static int getCurrentFontSize(Context context) {
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(context);
        return pref.getInt(CURRENT_FONT_SIZE, MEDIUM_TEXT_SIZE);
    }
    
    /**
     * To change the CURRENT_FONT_SIZE value stored in SharedPreferences.
     * 
     * @param Context
     *            context
     * @param int newFontSize
     * */
    public static void changeCurrentFontSize(Context context, int newFontSize) {
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(context);
        Editor editor = pref.edit();
        editor.putInt(CURRENT_FONT_SIZE, newFontSize);
        editor.commit();
    }
    
    /**
     * To save the network settings in SharedPreferances.
     * 
     * @param Context
     *            context.
     * @param boolean isWifiOnly, if isWifiOnly is true then network excess
     *        should be made only if wifi is on.
     * */
    public static void saveNetworkSettings(Context context, boolean isWifiOnly) {
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(context);
        Editor editor = pref.edit();
        editor.putBoolean(NETWORK_SETTINGS, isWifiOnly);
        editor.commit();
    }
    
    /**
     * To get the network settings value from SharedPreferances.
     * 
     * @param Context
     *            context.
     * @return boolean, if true then network excess should be made only if wifi
     *         is on.
     * */
    public static boolean isWifiOnly(Context context) {
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(context);
        return pref.getBoolean(NETWORK_SETTINGS, false);
    }
    
    /**
     * 
     * @param bmp
     * @param file
     */
    public static void saveBitmap(Bitmap bmp, File file) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            
            e.printStackTrace();
        } catch (Exception e) {
            
            e.printStackTrace();
        }
    }
    
    // Getting Image file path from URI.
    public static String getPath(Uri uri, Activity activity) {
        
        Cursor cursor = activity.getContentResolver().query(uri, null, null,
                null, null);
        
        if (cursor == null) { // Source is Dropbox or other similar local file
            // path
            return uri.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor
                    .getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(idx);
        }
    }
    
    // String[] ... [0] is resizedImagePath, [1] originalImageWidth, [2]
    // orignalImageHeight
    public static String[] getResizedImage(File f, float screenHeight,
            float screenWidth, Context mContext) {
        try {
            String[] returnArray = new String[3];
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            FileInputStream stream1 = new FileInputStream(f);
            BitmapFactory.decodeStream(stream1, null, o);
            stream1.close();
            int height = o.outHeight;
            int width = o.outWidth;
            returnArray[1] = width + "";
            returnArray[2] = height + "";
            int scale = 1;
            int heightRatio = 0;
            int widthRatio = 0;
            if (height > screenHeight) {
                heightRatio = Math.round((float) height / (float) screenHeight);
            }
            if (width > screenWidth) {
                widthRatio = Math.round((float) width / (float) screenWidth);
            }
            scale = heightRatio < widthRatio ? heightRatio : widthRatio;
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = (int) scale;
            FileInputStream stream2 = new FileInputStream(f);
            Bitmap resizedBitmap = BitmapFactory
                    .decodeStream(stream2, null, o2);
            stream2.close();
            File resizedFileName = new File(
                    vueAppResizedImageFileName(mContext));
            saveBitmap(resizedBitmap, resizedFileName);
            BitmapFactory.Options o3 = new BitmapFactory.Options();
            o3.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(resizedFileName),
                    null, o3);
            if (o3.outWidth > VueApplication.getInstance().mScreenWidth
                    || o3.outHeight > VueApplication.getInstance().mScreenHeight) {
                resizedBitmap = Utils.getBestDementions(resizedBitmap,
                        o3.outWidth, o3.outHeight,
                        VueApplication.getInstance().mScreenWidth,
                        VueApplication.getInstance().mScreenHeight);
                Utils.saveBitmap(resizedBitmap, resizedFileName);
            }
            resizedBitmap.recycle();
            returnArray[0] = resizedFileName.getPath();
            return returnArray;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static String vueAppCameraImageFileName(Context context) {
        FileCache fileCacheObj = new FileCache(context);
        if (fileCacheObj.getmVueAppCameraPicsDir() != null) {
            File fv[] = fileCacheObj.getmVueAppCameraPicsDir().listFiles();
            if (fv != null) {
                return fileCacheObj.getVueAppCameraPictureFile(
                        fv.length + 1 + "").getPath();
            }
        }
        
        return fileCacheObj.getVueAppCameraPictureFile(1 + "").getPath();
    }
    
    public static String vueAppResizedImageFileName(Context context) {
        FileCache fileCacheObj = new FileCache(context);
        if (fileCacheObj.getmVueAppResizedImagesDir() != null) {
            File fv[] = fileCacheObj.getmVueAppResizedImagesDir().listFiles();
            if (fv != null) {
                return fileCacheObj.getVueAppResizedPictureFile(
                        fv.length + 1 + "").getPath();
            }
        }
        
        return fileCacheObj.getVueAppCameraPictureFile(1 + "").getPath();
    }
    
    public static ImageDimension getScalledImage(Bitmap bitmap,
            int availableWidth, int availableHeight) {
        ImageDimension imgDimension = new ImageDimension();
        float requiredWidth, requiredHeight;
        float bitmapOriginalWidth = bitmap.getWidth();
        float bitmapOriginalHeight = bitmap.getHeight();
        float scaleFactor;
        requiredHeight = availableHeight;
        if ((availableWidth < VueApplication.getInstance().getScreenWidth() && availableHeight < VueApplication
                .getInstance().getScreenHeight())) {
            imgDimension.mImgWidth = availableWidth;
            imgDimension.mImgHeight = availableHeight;
            return imgDimension;
        }
        
        if (availableWidth > VueApplication.getInstance().getScreenWidth()) {
            requiredWidth = VueApplication.getInstance().getScreenWidth();
        } else {
            requiredWidth = availableWidth;
        }
        
        float temp = requiredWidth / bitmapOriginalWidth;
        if (temp <= 1) {
            // reduce the image size to the smallest one of given dimensions
            scaleFactor = Math.min(requiredWidth / bitmapOriginalWidth,
                    requiredHeight / bitmapOriginalHeight);
            requiredHeight = Math.round(bitmapOriginalHeight * scaleFactor);
            requiredWidth = Math.round(bitmapOriginalWidth * scaleFactor);
        } else {
            // increase the image size to required width increase the height
            // proportioned to width
            scaleFactor = requiredWidth / bitmapOriginalWidth;
            requiredHeight = Math.round(bitmapOriginalHeight * scaleFactor);
            requiredWidth = Math.round(bitmapOriginalWidth * scaleFactor);
        }
        if (requiredHeight > VueApplication.getInstance().getScreenHeight()) {
            // decrease the image to card height and decrease the imageWidht
            // proportioned to height
            scaleFactor = VueApplication.getInstance().getScreenHeight()
                    / requiredHeight;
            requiredHeight = Math.round(requiredHeight * scaleFactor);
            requiredWidth = Math.round(requiredWidth * scaleFactor);
        }
        
        imgDimension.mImgWidth = (int) requiredWidth;
        imgDimension.mImgHeight = (int) requiredHeight;
        return imgDimension;
        
    }
    
    public static float dipToPixels(Context context, float dipValue) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue,
                metrics);
    }
    
    /**
     * 7 * 24 * 60 * 60 * 1000
     * 
     * Getting two weeks before time
     */
    public static String twoWeeksBeforeTime() {
        long twoWeeksDifferenceTime = (System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000));
        return twoWeeksDifferenceTime + "";
    }
    
    /*
     * public static Bitmap getBitmap() { Bitmap icon =
     * BitmapFactory.decodeResource( VueApplication.getInstance()
     * .getResources(), R.drawable.ic_launcher); return icon; }
     */
    
    public static boolean appInstalledOrNot(String uri, Context context) {
        PackageManager pm = context.getPackageManager();
        boolean app_installed = false;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }
    
    public static String getUrlFromString(String stringWithUrl) {
        if (stringWithUrl != null) {
            String regex = "\\(?\\b(http://|www[.])[-A-Za-z0-9+&@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&@#/%=~_()|]";
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(stringWithUrl);
            while (m.find()) {
                String urlStr = m.group();
                if (urlStr.startsWith("(") && urlStr.endsWith(")")) {
                    urlStr = urlStr.substring(1, urlStr.length() - 1);
                }
                return urlStr;
            }
        }
        return null;
    }
    
    public static VueUser writeUserObjectToFile(Context context,
            String fileName, VueUser vueUser) throws Exception {
        FileOutputStream fos = context.openFileOutput(fileName,
                Context.MODE_PRIVATE);
        ObjectOutputStream os = new ObjectOutputStream(fos);
        os.writeObject(vueUser);
        os.close();
        return vueUser;
    }
    
    public static VueUser readUserObjectFromFile(Context context,
            String fileName) throws Exception {
        FileInputStream fis = context.openFileInput(fileName);
        ObjectInputStream is = new ObjectInputStream(fis);
        VueUser vueUser = (VueUser) is.readObject();
        is.close();
        return vueUser;
    }
    
    public static void writeUserProfileObjectToFile(Context context,
            String fileName, VueUserProfile vueUserProfile) throws Exception {
        FileOutputStream fos = context.openFileOutput(fileName,
                Context.MODE_PRIVATE);
        ObjectOutputStream os = new ObjectOutputStream(fos);
        os.writeObject(vueUserProfile);
        os.close();
    }
    
    public static VueUserProfile readUserProfileObjectFromFile(Context context,
            String fileName) throws Exception {
        FileInputStream fis = context.openFileInput(fileName);
        ObjectInputStream is = new ObjectInputStream(fis);
        VueUserProfile vueUserProfile = (VueUserProfile) is.readObject();
        is.close();
        return vueUserProfile;
    }
    
    public static void writeAisleImagePathListToFile(Context context,
            String fileName, ArrayList<DataentryImage> imagePathList)
            throws Exception {
        FileOutputStream fos = context.openFileOutput(fileName,
                Context.MODE_PRIVATE);
        ObjectOutputStream os = new ObjectOutputStream(fos);
        os.writeObject(imagePathList);
        os.close();
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static ArrayList<DataentryImage> readAisleImagePathListFromFile(
            Context context, String fileName) throws Exception {
        FileInputStream fis = context.openFileInput(fileName);
        ObjectInputStream is = new ObjectInputStream(fis);
        ArrayList<DataentryImage> imagePathList = (ArrayList) is.readObject();
        is.close();
        return imagePathList;
    }
    
    public static String getDeviceId() {
        String deviceId = Secure.getString(VueApplication.getInstance()
                .getContentResolver(), Secure.ANDROID_ID);
        return deviceId;
    }
    
    public static boolean getFromDetailsScreenToDataentryCreateAisleScreenPreferenceFlag(
            Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                VueConstants.SHAREDPREFERENCE_NAME, 0);
        return sharedPreferences
                .getBoolean(
                        VueConstants.FROM_DETAILS_SCREEN_TO_DATAENTRY_CREATE_AISLESCREEN_FLAG,
                        false);
    }
    
    public static void putFromDetailsScreenToDataentryCreateAisleScreenPreferenceFlag(
            Context context, boolean flag) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                VueConstants.SHAREDPREFERENCE_NAME, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(
                VueConstants.FROM_DETAILS_SCREEN_TO_DATAENTRY_CREATE_AISLESCREEN_FLAG,
                flag);
        editor.commit();
    }
    
    public static void putTouchToChnageImageFlag(Context context, boolean flag) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                VueConstants.SHAREDPREFERENCE_NAME, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(VueConstants.TOUCH_TO_CHANGE_IMAGE_FLAG, flag);
        editor.commit();
    }
    
    public static boolean getTouchToChangeFlag(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                VueConstants.SHAREDPREFERENCE_NAME, 0);
        return sharedPreferences.getBoolean(
                VueConstants.TOUCH_TO_CHANGE_IMAGE_FLAG, false);
    }
    
    public static void putTouchToChnageImagePosition(Context context,
            int position) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                VueConstants.SHAREDPREFERENCE_NAME, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(VueConstants.TOUCH_TO_CHANGE_IMAGE_POSITION, position);
        editor.commit();
    }
    
    public static int getTouchToChangePosition(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                VueConstants.SHAREDPREFERENCE_NAME, 0);
        return sharedPreferences.getInt(
                VueConstants.TOUCH_TO_CHANGE_IMAGE_POSITION, -1);
    }
    
    public static void putTouchToChnageImageTempPosition(Context context,
            int position) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                VueConstants.SHAREDPREFERENCE_NAME, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(VueConstants.TOUCH_TO_CHANGE_IMAGE_TEMP_POSITION,
                position);
        editor.commit();
    }
    
    public static int getTouchToChangeTempPosition(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                VueConstants.SHAREDPREFERENCE_NAME, 0);
        return sharedPreferences.getInt(
                VueConstants.TOUCH_TO_CHANGE_IMAGE_TEMP_POSITION, -1);
    }
    
    public static boolean getDataentryAddImageAisleFlag(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                VueConstants.SHAREDPREFERENCE_NAME, 0);
        return sharedPreferences.getBoolean(
                VueConstants.DATAENTRY_ADDIMAGE_AISLE_FLAG, false);
    }
    
    public static void putDataentryAddImageAisleFlag(Context context,
            boolean flag) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                VueConstants.SHAREDPREFERENCE_NAME, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(VueConstants.DATAENTRY_ADDIMAGE_AISLE_FLAG, flag);
        editor.commit();
    }
    
    public static boolean getDataentryTopAddImageAisleFlag(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                VueConstants.SHAREDPREFERENCE_NAME, 0);
        return sharedPreferences.getBoolean(
                VueConstants.DATAENTRY_TOP_ADDIMAGE_AISLE_FLAG, false);
    }
    
    public static void putDataentryTopAddImageAisleFlag(Context context,
            boolean flag) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                VueConstants.SHAREDPREFERENCE_NAME, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(VueConstants.DATAENTRY_TOP_ADDIMAGE_AISLE_FLAG, flag);
        editor.commit();
    }
    
    public static String getDataentryTopAddImageAisleLookingFor(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                VueConstants.SHAREDPREFERENCE_NAME, 0);
        return sharedPreferences.getString(
                VueConstants.DATAENTRY_TOP_ADDIMAGE_AISLE_LOOKINGFOR, null);
    }
    
    public static String getDataentryTopAddImageAisleOccasion(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                VueConstants.SHAREDPREFERENCE_NAME, 0);
        return sharedPreferences.getString(
                VueConstants.DATAENTRY_TOP_ADDIMAGE_AISLE_OCCASION, null);
    }
    
    public static boolean getDataentryTopAddImageIncreamentMixpanelPostCount(
            Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                VueConstants.SHAREDPREFERENCE_NAME, 0);
        return sharedPreferences.getBoolean(
                VueConstants.INCREAMENT_MIXPANEL_POSTCOUNT, false);
    }
    
    public static String getDataentryTopAddImageAisleCategory(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                VueConstants.SHAREDPREFERENCE_NAME, 0);
        return sharedPreferences.getString(
                VueConstants.DATAENTRY_TOP_ADDIMAGE_AISLE_CATEGORY, null);
    }
    
    public static String getDataentryTopAddImageAisleDescription(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                VueConstants.SHAREDPREFERENCE_NAME, 0);
        return sharedPreferences.getString(
                VueConstants.DATAENTRY_TOP_ADDIMAGE_AISLE_DESCRIPTION, null);
    }
    
    public static void putDataentryTopAddImageAisleLookingFor(Context context,
            String lookingfor) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                VueConstants.SHAREDPREFERENCE_NAME, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(VueConstants.DATAENTRY_TOP_ADDIMAGE_AISLE_LOOKINGFOR,
                lookingfor);
        editor.commit();
        
    }
    
    public static void putDataentryTopAddImageAisleOccasion(Context context,
            String occasion) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                VueConstants.SHAREDPREFERENCE_NAME, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(VueConstants.DATAENTRY_TOP_ADDIMAGE_AISLE_OCCASION,
                occasion);
        editor.commit();
    }
    
    public static void putDataentryTopAddImageIncreamentMixpanelPostCount(
            Context context, boolean flag) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                VueConstants.SHAREDPREFERENCE_NAME, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(VueConstants.INCREAMENT_MIXPANEL_POSTCOUNT, flag);
        editor.commit();
    }
    
    public static void putDataentryTopAddImageAisleCategory(Context context,
            String category) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                VueConstants.SHAREDPREFERENCE_NAME, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(VueConstants.DATAENTRY_TOP_ADDIMAGE_AISLE_CATEGORY,
                category);
        editor.commit();
    }
    
    public static void putDataentryTopAddImageAisleDescription(Context context,
            String description) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                VueConstants.SHAREDPREFERENCE_NAME, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(VueConstants.DATAENTRY_TOP_ADDIMAGE_AISLE_DESCRIPTION,
                description);
        editor.commit();
    }
    
    public static boolean isLoadDataentryScreenFlag(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                VueConstants.SHAREDPREFERENCE_NAME, 0);
        return sharedPreferences.getBoolean(
                VueConstants.LOAD_DATAENTRY_SCREEN_FLAG, false);
    }
    
    public static void setLoadDataentryScreenFlag(Context context,
            boolean loadDataentryScreenFlag) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                VueConstants.SHAREDPREFERENCE_NAME, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(VueConstants.LOAD_DATAENTRY_SCREEN_FLAG,
                loadDataentryScreenFlag);
        editor.commit();
    }
    
    public static int modifyHeightForDetailsView(
            ArrayList<AisleImageDetails> imageList) {
        int mWindowLargestHeight = 0;
        float[] imageHeightList = new float[imageList.size()];
        float availableScreenHeight = VueApplication.getInstance()
                .getVueDetailsCardHeight();
        float adjustedImageHeight, adjustedImageWidth;
        float imageHeight, imageWidth;
        float cardWidth = VueApplication.getInstance().getVueDetailsCardWidth();
        
        for (int i = 0; i < imageList.size(); i++) {
            imageHeight = imageList.get(i).mAvailableHeight;
            imageWidth = imageList.get(i).mAvailableWidth;
            if (imageHeight > availableScreenHeight) {
                adjustedImageHeight = availableScreenHeight;
                adjustedImageWidth = (adjustedImageHeight / imageHeight)
                        * imageWidth;
                imageHeight = adjustedImageHeight;
                imageWidth = adjustedImageWidth;
            }
            if (imageWidth > cardWidth) {
                adjustedImageWidth = cardWidth;
                adjustedImageHeight = (adjustedImageWidth / imageWidth)
                        * imageHeight;
                imageHeight = adjustedImageHeight;
                imageWidth = adjustedImageWidth;
            }
            imageList.get(i).mDetailsImageHeight = Math.round(imageHeight);
            imageList.get(i).mDetailsImageWidth = Math.round(imageWidth);
            imageHeightList[i] = imageHeight;
            
        }
        mWindowLargestHeight = (int) imageHeightList[0];
        
        for (int i = 0; i < imageHeightList.length; i++) {
            if (mWindowLargestHeight < imageHeightList[i]) {
                mWindowLargestHeight = (int) imageHeightList[i];
                
            }
        }
        
        return mWindowLargestHeight;
    }
    
    public static Bitmap getBestDementions(Bitmap bitmap, int originalWidth,
            int originalHeight, int availableScreenWidth,
            int availableScreenHeight) {
        float adjustedImageHeight, adjustedImageWidth;
        float imageHeight = originalHeight, imageWidth = originalWidth;
        if (imageHeight > availableScreenHeight) {
            adjustedImageHeight = availableScreenHeight;
            adjustedImageWidth = (adjustedImageHeight / imageHeight)
                    * imageWidth;
            imageHeight = adjustedImageHeight;
            imageWidth = adjustedImageWidth;
        }
        if (imageWidth > availableScreenWidth) {
            adjustedImageWidth = availableScreenWidth;
            adjustedImageHeight = (adjustedImageWidth / imageWidth)
                    * imageHeight;
            imageHeight = adjustedImageHeight;
            imageWidth = adjustedImageWidth;
        }
        return BitmapLoaderUtils.getInstance().getBitmap(bitmap,
                (int) imageWidth, (int) imageHeight);
    }
    
    public static String getStoreNameFromUrl(String url) {
        if (url != null) {
            if (VueApplication.getInstance().mShoppingApplicationDetailsList != null
                    && VueApplication.getInstance().mShoppingApplicationDetailsList
                            .size() > 0) {
                for (int i = 0; i < VueApplication.getInstance().mShoppingApplicationDetailsList
                        .size(); i++) {
                    if (url.toLowerCase()
                            .contains(
                                    VueApplication.getInstance().mShoppingApplicationDetailsList
                                            .get(i).getAppName().toLowerCase())) {
                        return VueApplication.getInstance().mShoppingApplicationDetailsList
                                .get(i).getAppName();
                    }
                }
            }
        }
        return "UnKnown";
    }
    
    public static void showAlertMessageForBackendNotIntegrated(
            final Activity activity, final boolean finishActivity) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                activity);
        alertDialogBuilder.setTitle("Vue");
        alertDialogBuilder
                .setMessage("Sorry, Server side integration is pending.");
        alertDialogBuilder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        alertDialogBuilder.setOnCancelListener(new OnCancelListener() {
            
            @Override
            public void onCancel(DialogInterface dialog) {
                if (finishActivity) {
                    activity.finish();
                }
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
    
    @SuppressWarnings("unchecked")
    public static ArrayList<ShoppingApplicationDetails> getInstalledApplicationsList(
            Context context) {
        ArrayList<ShoppingApplicationDetails> installedApplicationdetailsList = new ArrayList<ShoppingApplicationDetails>();
        if (VueApplication.getInstance().mShoppingApplicationDetailsList != null
                && VueApplication.getInstance().mShoppingApplicationDetailsList
                        .size() > 1) {
            for (int i = 1; i < VueApplication.getInstance().mShoppingApplicationDetailsList
                    .size(); i++) {
                installedApplicationdetailsList.add(VueApplication
                        .getInstance().mShoppingApplicationDetailsList.get(i));
            }
        }
        Intent intent = new Intent("android.intent.action.MAIN", null);
        intent.addCategory("android.intent.category.LAUNCHER");
        List<ResolveInfo> activities = context.getPackageManager()
                .queryIntentActivities(intent, 0);
        if (activities != null) {
            Collections.sort(activities, new SortResolveInfoBasedOnAppName());
        }
        PackageManager pm = context.getPackageManager();
        final Object a[] = activities.toArray();
        for (int i = 0; i < activities.size(); i++) {
            boolean isSystemApp = false;
            try {
                isSystemApp = isSystemPackage(pm
                        .getPackageInfo(
                                ((ResolveInfo) a[i]).activityInfo.applicationInfo.packageName,
                                PackageManager.GET_ACTIVITIES));
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
            String packageName = ((ResolveInfo) a[i]).activityInfo.applicationInfo.packageName;
            if (!isSystemApp
                    && !(packageName.equals("com.lateralthoughts.vue"))
                    && !(Arrays
                            .asList(VueApplication.SHOPPINGAPP_PACKAGES_ARRAY)
                            .contains(packageName))) {
                ShoppingApplicationDetails shoppingApplicationDetails = new ShoppingApplicationDetails(
                        ((ResolveInfo) a[i]).activityInfo.applicationInfo
                                .loadLabel(context.getPackageManager())
                                .toString(),
                        ((ResolveInfo) a[i]).activityInfo.name, packageName,
                        null);
                if (packageName.equals(VueConstants.TWITTER_PACKAGE_NAME)) {
                    VueApplication.getInstance().twitterActivityName = ((ResolveInfo) a[i]).activityInfo.name;
                }
                installedApplicationdetailsList.add(shoppingApplicationDetails);
            }
        }
        return installedApplicationdetailsList;
    }
    
    public static boolean isSystemPackage(PackageInfo pkgInfo) {
        return ((pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) ? true
                : false;
    }
    
    public static long getMins(long millis) {
        long minutes = (millis / (1000 * 60));
        return minutes;
    }
    
    public static String getWeekDay(int dayNo) {
        switch (dayNo) {
        case 1:
            return "SUN";
        case 2:
            return "MON";
        case 3:
            return "TUE";
        case 4:
            return "WED";
        case 5:
            return "THU";
        case 6:
            return "FRI";
        case 7:
            return "SAT";
        default:
            return "SAT";
        }
    }
    
    public static String getMonthForInt(int num) {
        String month = "wrong";
        DateFormatSymbols dfs = new DateFormatSymbols();
        String[] months = dfs.getMonths();
        if (num >= 0 && num <= 11) {
            month = months[num];
        }
        return month;
    }
    
    public static long dateDifference(long date) {
        try {
            long differenceInHours = (System.currentTimeMillis() - date)
                    / (1000 * 60 * 60);
            return differenceInHours;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
    
    public static void saveUserPoints(String key, int value, Context context) {
        int prevPoints = getUserPoints();
        SharedPreferences sharedPreferencesObj = VueApplication.getInstance()
                .getSharedPreferences(VueConstants.SHAREDPREFERENCE_NAME, 0);
        int count = sharedPreferencesObj.getInt(key, 0);
        count = count + value;
        Editor editor = sharedPreferencesObj.edit();
        editor.putInt(key, count);
        editor.commit();
        int currentPoints = getUserPoints();
        if (prevPoints < 100 && currentPoints >= 100) {
            boolean isDialogShown = sharedPreferencesObj.getBoolean(
                    VueConstants.USER_POINTS_DIALOG_SHOWN, false);
            if (context != null && !isDialogShown) {
                showRewardDialog(currentPoints, context);
            } else {
            }
        } else if (currentPoints >= 100) {
            boolean isDialogShown = sharedPreferencesObj.getBoolean(
                    VueConstants.USER_POINTS_DIALOG_SHOWN, false);
            if (!isDialogShown && context != null) {
                showRewardDialog(currentPoints, context);
            }
        }
    }
    
    private static void showRewardDialog(int currentPoints, Context context) {
        showRewardsDialog("", currentPoints, context);
        SharedPreferences sharedPreferencesObj = VueApplication.getInstance()
                .getSharedPreferences(VueConstants.SHAREDPREFERENCE_NAME, 0);
        Editor editor2 = sharedPreferencesObj.edit();
        editor2.putBoolean(VueConstants.USER_POINTS_DIALOG_SHOWN, true);
        editor2.commit();
    }
    
    public static int getUserPoints() {
        SharedPreferences sharedPreferencesObj = VueApplication.getInstance()
                .getSharedPreferences(VueConstants.SHAREDPREFERENCE_NAME, 0);
        int likesCount = sharedPreferencesObj.getInt(
                VueConstants.USER_LIKES_POINTS, 0);
        int CommentsCount = sharedPreferencesObj.getInt(
                VueConstants.USER_COMMENTS_POINTS, 0);
        int addImageCount = sharedPreferencesObj.getInt(
                VueConstants.USER_ADD_IAMGE_POINTS, 0);
        int inviteFriends = sharedPreferencesObj.getInt(
                VueConstants.USER_INVITE_FRIEND_POINTS, 0);
        int totalCount = likesCount + CommentsCount + addImageCount
                + inviteFriends;
        int installPoints = 5;
        totalCount = totalCount + sUserPoints + installPoints;
        
        return totalCount;
        
    }
    
    private static void showRewardsDialog(String userType, int pointsEarned,
            final Context context) {
        if (context == null) {
            return;
        }
        if (pointsEarned < 100) {
            userType = "bronze";
        } else {
            userType = "silver";
        }
        if (userType.equals("silver")) {
            StringBuilder sb = new StringBuilder(
                    "Congratulations! You are now a Silver Vuer! As a thank you, we will gladly send you $5 to shop online.");
            // AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
            // new
            // ContextThemeWrapper(getActivity(),R.style.AlertDialogCustom));
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    new ContextThemeWrapper(context, R.style.AppBaseTheme));
            alertDialogBuilder.setTitle("Vue");
            alertDialogBuilder.setMessage(sb.toString());
            alertDialogBuilder.setPositiveButton(context.getResources()
                    .getString(R.string.redeem_it_now),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            VueUser storedVueUser = null;
                            try {
                                storedVueUser = Utils
                                        .readUserObjectFromFile(
                                                context,
                                                VueConstants.VUE_APP_USEROBJECT__FILENAME);
                                if (storedVueUser != null) {
                                    JSONObject nameTag = new JSONObject();
                                    nameTag.put("Redeem", "RedeemItNow");
                                    nameTag.put("Id", storedVueUser.getId());
                                    nameTag.put("Email",
                                            storedVueUser.getEmail());
                                    SharedPreferences sharedPreferencesObj = VueApplication
                                            .getInstance()
                                            .getSharedPreferences(
                                                    VueConstants.SHAREDPREFERENCE_NAME,
                                                    0);
                                    Editor editor = sharedPreferencesObj.edit();
                                    editor.putBoolean(
                                            VueConstants.USER_POINTS_DIALOG_SHOWN,
                                            true);
                                    editor.commit();
                                    MixpanelAPI mixpanel = MixpanelAPI
                                            .getInstance(
                                                    context,
                                                    VueApplication
                                                            .getInstance().MIXPANEL_TOKEN);
                                    mixpanel.track("Coupon", nameTag);
                                }
                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }
                            
                            Toast.makeText(
                                    context,
                                    "Thank you for being such an awesome Vuer! Expect to see the rewards from us shortly in your email inbox.",
                                    Toast.LENGTH_LONG).show();
                            
                        }
                    });
            alertDialogBuilder.setNegativeButton(context.getResources()
                    .getString(R.string.continue_earning),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            
                            dialog.cancel();
                            
                        }
                    });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }
    
    public static Uri takeScreenshot(Activity activity) {
        View rootView = activity.findViewById(android.R.id.content)
                .getRootView();
        rootView.setDrawingCacheEnabled(true);
        Bitmap bitmap = rootView.getDrawingCache();
        File file = new FileCache(activity)
                .getVueAppUserProfilePictureFile(VueConstants.BUG_REPORT_SCREENSHOT);
        saveBitmap(bitmap, file);
        return Uri.fromFile(file);
    }
}
