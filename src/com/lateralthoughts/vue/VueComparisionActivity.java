package com.lateralthoughts.vue;

 

import com.lateralthoughts.vue.R;
//import com.lateralthoughts.vue.VueframeAdapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.Window;
import android.widget.LinearLayout;

public class VueComparisionActivity extends FragmentActivity {
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
 
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
		 
			Fragment fragment;
			 
			
			if(position == 0) {
				fragment = new VueAisleDetailsViewFragment();
			} else {
				fragment = new VueComparisionFragment();
			}
			
			
			return fragment;
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 2;
		}

/*		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				//return getString(R.string.title_section1).toUpperCase(l);
			case 1:
				//return getString(R.string.title_section2).toUpperCase(l);
			case 2:
				//return getString(R.string.title_section3).toUpperCase(l);
			}
			return null;
		}*/
	}
	
	
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
