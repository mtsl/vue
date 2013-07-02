package com.lateralthoughts.vue;

import com.lateralthoughts.vue.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.widget.LinearLayout;

public class VueComparisionActivity extends Activity {
	LinearLayout vuefame_top,vueframe_bottom;
 @SuppressLint("NewApi")
@Override
 protected void onCreate(android.os.Bundle savedInstanceState) {
	 super.onCreate(savedInstanceState);
	 setContentView(R.layout.aisle_comparision_activity_landing);
	 getActionBar().hide();
/*	vuefame_top = (LinearLayout)findViewById(R.id.vue_top_scroller);
	vueframe_bottom = (LinearLayout)findViewById(R.id.vue_bottom_scroller);
		VueframeAdapter frameAdapter = new VueframeAdapter(this, null, null);
	frameAdapter.setImageViews(vuefame_top, vueframe_bottom);*/
	 
 };
 @Override
protected void onResume() {
	// TODO Auto-generated method stub
	super.onResume();
}
 @Override
protected void onPause() {
	// TODO Auto-generated method stub
	super.onPause();
}
 
}
