package com.lateralthoughts.vue;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;

public class SwipeHelp extends Activity {
    LinearLayout mHelpScreenForSwipping;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.swipe_help);
        SharedPreferences sharedPreferencesObj = this.getSharedPreferences(
                VueConstants.SHAREDPREFERENCE_NAME, 0);
        Editor editor = sharedPreferencesObj.edit();
        editor.putBoolean(VueConstants.AISLE_SWIPE, true);
        editor.commit();
        mHelpScreenForSwipping = (LinearLayout) findViewById(R.id.help_screen_for_swipping);
        mHelpScreenForSwipping.setOnTouchListener(new OnTouchListener() {
            
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                finish();
                return true;
            }
        });
        mHelpScreenForSwipping.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
        }
        return false;
        
    }
}
