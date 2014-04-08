package com.lateralthoughts.vue.ui;

import com.lateralthoughts.vue.VueLandingPageActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class Launcher extends Activity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = new Intent(this,VueLandingPageActivity.class);
        startActivity(i);
        finish();
    }
}

 
