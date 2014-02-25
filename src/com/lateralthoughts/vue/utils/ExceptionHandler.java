package com.lateralthoughts.vue.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;

import android.app.Activity;
import android.os.Environment;

import com.mail.SendMail;

public class ExceptionHandler implements
        java.lang.Thread.UncaughtExceptionHandler {
    
    /**
     * 
     * @param context
     *            Context
     */
    public ExceptionHandler(Activity context) {
    }
    
    /**
     * Cacll back method calls when uncaught exception occurs
     * 
     * @param thread
     *            Thread
     * @param exception
     *            Throwable
     */
    public void uncaughtException(Thread thread, Throwable exception) {
        final StringWriter stackTrace = new StringWriter();
        exception.printStackTrace(new PrintWriter(stackTrace));
        writeToSdcard(stackTrace.toString());
    }
    
    private void writeToSdcard(String message) {
        String path = Environment.getExternalStorageDirectory().toString();
        File dir = new File(path + "/vueExceptions/");
        if (!dir.isDirectory()) {
            dir.mkdir();
        }
        File file = new File(dir, "/" + "vueExceptions"
                + (Calendar.getInstance().get(Calendar.MONTH) + 1) + "-"
                + Calendar.getInstance().get(Calendar.DATE) + "_"
                + Calendar.getInstance().get(Calendar.YEAR) + ".txt");
        
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(
                    new FileWriter(file, true)));
            out.write("\n" + message + "\n");
            out.flush();
            out.close();
            
        } catch (IOException e) {
            
            e.printStackTrace();
        }
    }
}
