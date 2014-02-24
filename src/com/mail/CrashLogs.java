package com.mail;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CrashLogs {

    private static final String recipients = "surendra@23neem.com, raju.k@23neem.com, krishna@23neem.com";
    private static final String subject = "Vue crash logs dated: ";
    
    
    public static String getRecipients() {
        return recipients;
    }
    
    public static String subject() {
        return subject + getCurrentDate();
    }
    
    private static String getCurrentDate() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        Date date = new Date();    
        return dateFormat.format(date);
      }
}
