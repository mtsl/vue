package com.lateralthoughts.vue.connectivity;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

public class VueBatteryManager {

  public static int MINIMUM_BATTERY_LEVEL = 15;
  
  public static boolean isConnected(Context context) {
    Intent intent = context.registerReceiver(null, new IntentFilter(Intent
        .ACTION_BATTERY_CHANGED));
    int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
    return plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged ==
        BatteryManager.BATTERY_PLUGGED_USB;
  }

  public static int batteryLevel(Context context) {
    Intent batteryStatus = context.registerReceiver(null, new IntentFilter(Intent
        .ACTION_BATTERY_CHANGED));
    int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
    int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
    return (level* 100) / scale;
  }
}
