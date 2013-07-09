package com.lateralthoughts.vue.connectivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NetworkStateChangeReciver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent inetent) {
    Log.e("NetworkStateChangeReciver", "SURU NEWWORK STATE CHANGED : ");
    if(VueConnectivityManager.isNetworkConnected(context)) {
      Log.e("NetworkStateChangeReciver", "SURU NEWWORK STATE CHANGED : network CONNECTED");
      // TODO: need to sync the data which has not synced at the time of user input in to
      // the app because of not network connection.
    }
  }
}
