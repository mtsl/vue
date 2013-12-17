package com.lateralthoughts.vue;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class FeedbackForm extends Activity {
    
    EditText mEditone;
    Button mSend, mCancle;
    TextView mPhoneinfo;
    String mPhoneinfotext = null;
    TextView mFeedbackheading, mContentOne, mContentTwo,
            mFeedbackFormCustomTextId;
    
    // Typeface face;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.feedbackform);
        mPhoneinfo = (TextView) findViewById(R.id.phoneinfo);
        mEditone = (EditText) findViewById(R.id.editone);
        mSend = (Button) findViewById(R.id.send);
        mCancle = (Button) findViewById(R.id.cancle);
        mFeedbackheading = (TextView) findViewById(R.id.aboutfishwrap);
        mContentOne = (TextView) findViewById(R.id.contentone);
        mContentTwo = (TextView) findViewById(R.id.contenttwo);
        mCancle.setOnClickListener(new OnClickListener() {
            
            public void onClick(View v) {
                mEditone.setText("");
                
            }
        });
        
        setHeaderText();
        
        DisplayMetrics metrics = new DisplayMetrics();
        FeedbackForm.this.getWindowManager().getDefaultDisplay()
                .getMetrics(metrics);
        int height = metrics.heightPixels;
        int wwidth = metrics.widthPixels;
        String name = Build.MANUFACTURER + " - " + Build.MODEL;
        int version = Build.VERSION.SDK_INT;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) FeedbackForm.this
                .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()
                .getMetrics(displayMetrics);
        String resolution = null;
        switch (displayMetrics.densityDpi) {
        case DisplayMetrics.DENSITY_HIGH:
            resolution = "high";
            break;
        case DisplayMetrics.DENSITY_MEDIUM:
            resolution = "medium";
            break;
        case DisplayMetrics.DENSITY_LOW:
            resolution = "low";
            break;
        default:
            resolution = "xhdpi";
            break;
        
        }
        mPhoneinfotext = "\nScreen height: " + height + "\nScreen width: "
                + wwidth + "\nPhone info: " + name + "\nVersion:" + version
                + "\nDensity: " + resolution;
        mPhoneinfo.setText(mPhoneinfotext);
        
        mSend.setOnClickListener(new OnClickListener() {
            
            public void onClick(View v) {
                String feedbackmessart = mEditone.getText().toString();
                if (feedbackmessart != null
                        && feedbackmessart.trim().length() > 0) {
                    Intent sendIntent = new Intent(
                            android.content.Intent.ACTION_SEND);
                    sendIntent.setType("text/plain");
                    sendIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                            "Vue feedback");
                    sendIntent.putExtra(android.content.Intent.EXTRA_TEXT,
                            feedbackmessart + "\n" + mPhoneinfotext);
                    sendIntent.setClassName("com.google.android.gm",
                            "com.google.android.gm.ComposeActivityGmail");
                    sendIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
                            new String[] { "vue@thesilverlabs.com" });
                    FeedbackForm.this.startActivity(sendIntent);
                }
            }
            
        });
        
        mEditone.setOnKeyListener(new OnKeyListener() {
            
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                    case KeyEvent.KEYCODE_ENTER:
                        
                        String feedbackmessart = mEditone.getText().toString();
                        if (feedbackmessart != null
                                && feedbackmessart.trim().length() > 0) {
                            Intent sendIntent = new Intent(
                                    android.content.Intent.ACTION_SEND);
                            sendIntent.setType("text/plain");
                            sendIntent.putExtra(
                                    android.content.Intent.EXTRA_SUBJECT,
                                    "Vue feedback");
                            sendIntent.putExtra(
                                    android.content.Intent.EXTRA_TEXT,
                                    feedbackmessart + "\n" + mPhoneinfotext);
                            sendIntent
                                    .setClassName("com.google.android.gm",
                                            "com.google.android.gm.ComposeActivityGmail");
                            sendIntent.putExtra(
                                    android.content.Intent.EXTRA_EMAIL,
                                    new String[] { "vue@thesilverlabs.com" });
                            FeedbackForm.this.startActivity(sendIntent);
                        }
                        return true;
                    default:
                        break;
                    }
                }
                return false;
            }
            
        });
    }
    
    private void setHeaderText() {
        
        mFeedbackheading.setText(Html.fromHtml("Feedback on " + " <b>"
                + this.getResources().getString(R.string.app_name) + "</b>"
                + "!"));
        mContentOne.setText(Html.fromHtml(this.getResources().getString(
                R.string.abouttextfedbacktext)
                + " <b>"
                + this.getResources().getString(R.string.app_name)
                + "</b>"
                + " "
                + this.getResources().getString(
                        R.string.abouttextfedbacktexttwo)));
        mContentTwo.setText(this.getResources().getString(
                R.string.abouttextfedbacktextt));
    }
}
