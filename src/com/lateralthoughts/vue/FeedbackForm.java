package com.lateralthoughts.vue;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
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

  EditText editone;
  Button send, cancle;
  TextView phoneinfo;
  String phoneinfotext = null;
  TextView feedbackheading, contentone, contenttwo, feedbackform_customtextid;
  //Typeface face;
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.feedbackform);
   // face = Typeface.createFromAsset(this.getAssets(), "Roboto-Regular.ttf");
    phoneinfo = (TextView) findViewById(R.id.phoneinfo);
    editone = (EditText) findViewById(R.id.editone);
    send = (Button) findViewById(R.id.send);
    cancle = (Button) findViewById(R.id.cancle);
   // feedbackform_customtextid = (TextView) findViewById(R.id.feedbackform_customtextid);
    feedbackheading = (TextView) findViewById(R.id.aboutfishwrap);
    contentone = (TextView) findViewById(R.id.contentone);
    contenttwo = (TextView) findViewById(R.id.contenttwo);
    cancle.setOnClickListener(new OnClickListener() {
      
      public void onClick(View v) {
        editone.setText("");
        
      }
    });
 
    setHeaderText();    

    DisplayMetrics metrics = new DisplayMetrics();
    FeedbackForm.this.getWindowManager().
    getDefaultDisplay().getMetrics(metrics);
    int height = metrics.heightPixels;
    int wwidth = metrics.widthPixels;
    String name = Build.MANUFACTURER + " - " + Build.MODEL;
    int version = Build.VERSION.SDK_INT;
    DisplayMetrics displayMetrics = new DisplayMetrics();
    ((WindowManager) FeedbackForm.this.
    getSystemService(Context.WINDOW_SERVICE)).
    getDefaultDisplay().getMetrics(
    displayMetrics);
    String resolution = null;
    switch (displayMetrics.densityDpi) {
    case DisplayMetrics.DENSITY_HIGH:  
    resolution = "high"; break;
    case DisplayMetrics.DENSITY_MEDIUM: 
    resolution = "medium"; break;
    case DisplayMetrics.DENSITY_LOW: 
    resolution = "low"; break;
    default : resolution = "xhdpi"; break;

    }
    phoneinfotext = "\nScreen height: " + height + "\nScreen width: "
        + wwidth + "\nPhone info: " + name + "\nVersion:" + version
        + "\nDensity: " + resolution;
    phoneinfo.setText(phoneinfotext);   
    
    send.setOnClickListener(new OnClickListener() {

 
			public void onClick(View v) {
				String feedbackmessart = editone.getText().toString();
				if (feedbackmessart != null && feedbackmessart.trim().length() > 0) {
					Intent sendIntent = new Intent(
							android.content.Intent.ACTION_SEND);
					sendIntent.setType("text/plain");
					sendIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
							"Vue feedback");
					sendIntent.putExtra(android.content.Intent.EXTRA_TEXT,
							feedbackmessart + "\n" + phoneinfotext);
					sendIntent.setClassName("com.google.android.gm",
							"com.google.android.gm.ComposeActivityGmail");
					sendIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
							new String[] { "vue@thesilverlabs.com" });
					FeedbackForm.this.startActivity(sendIntent);
				}
			}

    });
    
    editone.setOnKeyListener(new OnKeyListener() {

		@Override
		public boolean onKey(View view, int keyCode, KeyEvent event) {
			if (event.getAction() == KeyEvent.ACTION_DOWN){
	    	      switch (keyCode) {
	    	        case KeyEvent.KEYCODE_DPAD_CENTER:
	    	        case KeyEvent.KEYCODE_ENTER:

	    				String feedbackmessart = editone.getText().toString();
	    				if (feedbackmessart != null && feedbackmessart.trim().length() > 0) {
	    					Intent sendIntent = new Intent(
	    							android.content.Intent.ACTION_SEND);
	    					sendIntent.setType("text/plain");
	    					sendIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
	    							"Vue feedback");
	    					sendIntent.putExtra(android.content.Intent.EXTRA_TEXT,
	    							feedbackmessart + "\n" + phoneinfotext);
	    					sendIntent.setClassName("com.google.android.gm",
	    							"com.google.android.gm.ComposeActivityGmail");
	    					sendIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
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
    
    feedbackheading.setText(Html.fromHtml("Feedback on " + " <b>"
        + this.getResources().getString(R.string.app_name) + "</b>" + "!"));
    contentone.setText(Html.fromHtml(this.getResources().getString(
        R.string.abouttextfedbacktext)
        + " <b>"
        + this.getResources().getString(R.string.app_name)
        + "</b>"
        + " "
        + this.getResources().getString(R.string.abouttextfedbacktexttwo)));
    contenttwo.setText(this.getResources().getString(
        R.string.abouttextfedbacktextt));

  /*  feedbackheading.setTypeface(face);
    contentone.setTypeface(face);
    contenttwo.setTypeface(face);
    phoneinfo.setTypeface(face);*/
  }
}
