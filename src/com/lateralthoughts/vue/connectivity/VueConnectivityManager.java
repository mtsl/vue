package com.lateralthoughts.vue.connectivity;

import com.lateralthoughts.vue.R;
import com.lateralthoughts.vue.utils.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.widget.Toast;

public class VueConnectivityManager {
 
  private static boolean isTostShown = false;

  /**
   * Get the network info
   * 
   * @param context
   * @return
   */
  public static NetworkInfo getNetworkInfo(Context context) {
    ConnectivityManager cm = (ConnectivityManager) context
        .getSystemService(Context.CONNECTIVITY_SERVICE);
    return cm.getActiveNetworkInfo();
  }


  /**
   * Check if there is any connectivity
   * 
   * @param context
   * @return
   */
  public static boolean isConnected(Context context) {
    NetworkInfo info = VueConnectivityManager.getNetworkInfo(context);
    return (info != null && info.isConnected());
  }


  /**
   * Check if there is any connectivity to a Wifi network
   * 
   * @param context
   * @param type
   * @return
   * 
   */
  public static boolean isConnectedWifi(Context context) {
    NetworkInfo info = VueConnectivityManager.getNetworkInfo(context);
    return (info != null && info.isConnected() && info.getType()
        == ConnectivityManager.TYPE_WIFI);
  }
  
  /**
  * Check if there is any connectivity to a mobile network
  * @param context
  * @param type
  * @return
  */
  public static boolean isConnectedMobile(Context context) {
    NetworkInfo info = VueConnectivityManager.getNetworkInfo(context);
    return (info != null && info.isConnected() && info.getType()
        == ConnectivityManager.TYPE_MOBILE);
  }
  
  /**
   * Check if there is fast connectivity
   * 
   * @param context
   * @return
   */
  public static boolean isConnectedFast(Context context) {
    NetworkInfo info = VueConnectivityManager.getNetworkInfo(context);
    return (info != null && info.isConnected() && VueConnectivityManager
        .isConnectionFast(info.getType(), info.getSubtype()));
  }
  
  
  /**
   * Check if the connection is fast
   * 
   * @param type
   * @param subType
   * @return
   */
  public static boolean isConnectionFast(int type, int subType) {

    if (type == ConnectivityManager.TYPE_WIFI) {
      return true;
    } else if (type == ConnectivityManager.TYPE_MOBILE) {
      switch (subType) {
        case TelephonyManager.NETWORK_TYPE_1xRTT:
          return false; // ~ 50-100 kbps
        case TelephonyManager.NETWORK_TYPE_CDMA:
          return false; // ~ 14-64 kbps
        case TelephonyManager.NETWORK_TYPE_EDGE:
          return false; // ~ 50-100 kbps
        case TelephonyManager.NETWORK_TYPE_EVDO_0:
          return true; // ~ 400-1000 kbps
        case TelephonyManager.NETWORK_TYPE_EVDO_A:
          return true; // ~ 600-1400 kbps
        case TelephonyManager.NETWORK_TYPE_GPRS:
          return false; // ~ 100 kbps
        case TelephonyManager.NETWORK_TYPE_HSDPA:
          return true; // ~ 2-14 Mbps
        case TelephonyManager.NETWORK_TYPE_HSPA:
          return true; // ~ 700-1700 kbps
        case TelephonyManager.NETWORK_TYPE_HSUPA:
          return true; // ~ 1-23 Mbps
        case TelephonyManager.NETWORK_TYPE_UMTS:
          return true; // ~ 400-7000 kbps
          /*
           * Above API level 7, make sure to set android:targetSdkVersion to
           * appropriate level to use these
           */
        case TelephonyManager.NETWORK_TYPE_EHRPD: // API level 11
          return true; // ~ 1-2 Mbps
        case TelephonyManager.NETWORK_TYPE_EVDO_B: // API level 9
          return true; // ~ 5 Mbps
        case TelephonyManager.NETWORK_TYPE_HSPAP: // API level 13
          return true; // ~ 10-20 Mbps
        case TelephonyManager.NETWORK_TYPE_IDEN: // API level 8
          return false; // ~25 kbps
        case TelephonyManager.NETWORK_TYPE_LTE: // API level 11
          return true; // ~ 10+ Mbps
          // Unknown
        case TelephonyManager.NETWORK_TYPE_UNKNOWN:
        default:
          return false;
      }
    } else {
      return false;
    }
  }
  
  /**
   * Check if the network connection is available, it takes the user preferences
   * stored in SharedPreferences.
   * 
   * @param context Context.
   * @param isWifi boolean. if true then checks for wifi connectivity else
   *        checks for any network available.
   * @return boolean.
   * */
  private static boolean checkConnection(Context context, boolean isWifi) {
    NetworkInfo info = VueConnectivityManager.getNetworkInfo(context);
    if (info == null) {
      return false;
    }
    int subType = info.getSubtype();
    int type = info.getType();

    boolean isNetworkConnected = false;
    if (isConnected(context)) {
      isNetworkConnected = true;
    }

    if (isWifi == true) {
      if (isConnectedWifi(context)) {
        isNetworkConnected = true;
      } else {
        isNetworkConnected = false;
      }
    } else {
      if (isConnectedWifi(context)) {
        isNetworkConnected = true;
      } else if (isConnectedMobile(context)) {
        isNetworkConnected = true;
      }
    }
    if (isNetworkConnected && !isConnectionFast(type, subType)) {
      try {
        Toast
            .makeText(context, "Netowrk connection is slow", Toast.LENGTH_LONG)
            .show();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return isNetworkConnected;
  }

  public static boolean isNetworkConnected(Context context) {
    boolean isConneted = true;
    if(!isConnectedWifi(context)) {
      isConneted = false;
      if (isAirplaneModeOn(context)) {
        isConneted = false;
      } else if (VueConnectivityManager.isConnectedMobile(context)) {
        isConneted = true; 
      }
    }
    return isConneted;
  }
  
  /**
* Gets the state of Airplane Mode.
* 
* @param context
* @return true if enabled.
*/
  private static boolean isAirplaneModeOn(Context context) {

    return Settings.System.getInt(context.getContentResolver(),
        Settings.System.AIRPLANE_MODE_ON, 0) != 0;

  }
}
