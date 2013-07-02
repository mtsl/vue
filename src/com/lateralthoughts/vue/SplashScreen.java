package com.lateralthoughts.vue;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;

public class SplashScreen extends Activity {

    //how long until we go to the next activity
    protected int SPLASH_SCREEN_DURATION = 3000; 

    private Thread mSplashTread;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        //final SplashScreen sPlashScreen = this; 

        // thread for displaying the SplashScreen
        mSplashTread = new Thread() {
            @Override
            public void run() {
                try {
                    synchronized(this){
                            //wait 5 sec
                            wait(SPLASH_SCREEN_DURATION);
                    }

                } catch(InterruptedException e) {}
                finally {        

                    //start a new activity
                    //Intent i = new Intent();
                    //i.setClass(sPlashScreen, VueLandingPageActivity.class);
                    //startActivity(i);
                    finish();
                }
            }
        };

        mSplashTread.start();
    }

    //Function that will handle the touch
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            synchronized(mSplashTread){
                    mSplashTread.notifyAll();
            }
        }
        return true;
    }

}