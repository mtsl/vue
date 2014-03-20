package com.lateralthoughts.vue;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.view.Window;

public class CrashActivity extends Activity {
    public static final String STACKTRACE = "stacktrace";
    
    /**
     * @param icicle
     *            Bundle
     */
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        final String stackTrace = getIntent().getStringExtra(STACKTRACE);
        // show the popup for the exception
        showAlert(stackTrace);
    }
    
    /**
     * 
     * @param message
     *            String
     */
 
    void showAlert(String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setPositiveButton(
                getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        
                        dialog.cancel();
                        finish();
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
        
        return;
    }
    
}
