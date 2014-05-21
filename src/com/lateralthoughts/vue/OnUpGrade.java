package com.lateralthoughts.vue;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class OnUpGrade extends Activity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.on_upgrade);
    }
    @Override
    protected void onResume() {
        super.onResume();
        new Handler().postDelayed(new Runnable() {
            
            @Override
            public void run() {
                finish();
                
                Intent i = new Intent(OnUpGrade.this,VueLandingPageActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.fade_in_activity, R.anim.fade_out_activity);
                
            }
        }, 1000);
    }
}
