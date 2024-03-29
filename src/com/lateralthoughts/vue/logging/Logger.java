package com.lateralthoughts.vue.logging;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;

import android.os.Environment;
import android.util.Log;

public class Logger {
  private static boolean wrightToSdCard = true;

  public static void log(String level, String TAG, String message) {
   
    if (level.equalsIgnoreCase("INFO")) {
      Log.i( TAG, message);
    } else if (level.equalsIgnoreCase("DEBUG")) {
      Log.d(TAG, message);
    } else if (level.equalsIgnoreCase("ERROR")) {
      Log.e(TAG, message);
    } else if(level.equalsIgnoreCase("VERBOSE")) {
      Log.v(TAG, message);
    }
    writeToSdcard(level + "=> " + TAG + " : " + message);
  }

  private static void writeToSdcard(String message) {
    if (!wrightToSdCard) {
      return;
    }
    String path = Environment.getExternalStorageDirectory().toString();
    File dir = new File(path + "/vueAppLogs/");
    if(!dir.isDirectory()) {
      dir.mkdir();
    }
    File file = new File(dir, "/" + Calendar.getInstance().get(Calendar.DATE) + ".txt");
      try {
        file.createNewFile();
      } catch (IOException e) {
        e.printStackTrace();
      }
      
      try {
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
        out.write("\n"+message+"\n");
        out.flush();
        out.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
  }
  
  public long calculateTimeDeff(long startTime, long endTime) {
    long deff = (endTime - startTime);
    long seconds = (deff / 1000);
    return seconds;
  }
}
