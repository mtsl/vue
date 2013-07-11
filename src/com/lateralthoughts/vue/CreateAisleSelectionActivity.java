package com.lateralthoughts.vue;


import java.util.ArrayList;
import java.util.List;

import com.lateralthoughts.vue.utils.Utils;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class CreateAisleSelectionActivity extends Activity {
	
	RelativeLayout topleftgreencircle, toprightgreencircle, bottomleftgreencircle, bottomrightgreencircle, totalbottom;
	
	LinearLayout boxwithcirclelayout;
	
	AnimatorSet animSetXY;
	
	Animation anim;
	
	boolean fromCreateAilseScreenflag = false;
	
@Override
protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.create_asilse_selection);
	
	Bundle b = getIntent().getExtras();
	
	if(b != null)
	{
		fromCreateAilseScreenflag = b.getBoolean(VueConstants.FROMCREATEAILSESCREENFLAG);
	}
	
	topleftgreencircle = (RelativeLayout) findViewById(R.id.topleftgreencircle);
	toprightgreencircle = (RelativeLayout) findViewById(R.id.toprightgreencircle);
	bottomleftgreencircle = (RelativeLayout) findViewById(R.id.bottomleftgreencircle);
	bottomrightgreencircle = (RelativeLayout) findViewById(R.id.bottomrightgreencircle);
	totalbottom = (RelativeLayout) findViewById(R.id.totalbottom);
	
	boxwithcirclelayout = (LinearLayout) findViewById(R.id.boxwithcirclelayout);
	
	
	anim  = (TranslateAnimation) AnimationUtils.loadAnimation(this, R.anim.layout_down_animation);
	anim.setDuration(600);
	
	
	List<ObjectAnimator> arrayListObjectAnimators = new ArrayList<ObjectAnimator>();
	
ObjectAnimator animY = ObjectAnimator.ofFloat(topleftgreencircle, View.SCALE_X,0, 1f);
	animY.setInterpolator(new BounceInterpolator());
	
	arrayListObjectAnimators.add(animY);//ArrayList of ObjectAnimators
	
	ObjectAnimator animY2 = ObjectAnimator.ofFloat(topleftgreencircle, View.SCALE_Y,0, 1f);
	animY2.setInterpolator(new BounceInterpolator());
	
	arrayListObjectAnimators.add(animY2);//ArrayList of ObjectAnimators

	ObjectAnimator animY1 = ObjectAnimator.ofFloat(toprightgreencircle,View.SCALE_X,0, 1f);
	animY1.setInterpolator(new BounceInterpolator());
	arrayListObjectAnimators.add(animY1);
	
	ObjectAnimator animY3 = ObjectAnimator.ofFloat(toprightgreencircle,View.SCALE_Y,0, 1f);
	animY3.setInterpolator(new BounceInterpolator());
	arrayListObjectAnimators.add(animY3);
	
	
	
	ObjectAnimator animY11 = ObjectAnimator.ofFloat(bottomleftgreencircle,View.SCALE_X,0, 1f);
	animY11.setInterpolator(new BounceInterpolator());
	arrayListObjectAnimators.add(animY11);
	
	ObjectAnimator animY31 = ObjectAnimator.ofFloat(bottomleftgreencircle,View.SCALE_Y,0, 1f);
	animY31.setInterpolator(new BounceInterpolator());
	arrayListObjectAnimators.add(animY31);
	
	
	ObjectAnimator animY12 = ObjectAnimator.ofFloat(bottomrightgreencircle,View.SCALE_X,0, 1f);
	animY12.setInterpolator(new BounceInterpolator());
	arrayListObjectAnimators.add(animY12);
	
	ObjectAnimator animY32 = ObjectAnimator.ofFloat(bottomrightgreencircle,View.SCALE_Y,0, 1f);
	animY32.setInterpolator(new BounceInterpolator());
	arrayListObjectAnimators.add(animY32);
	
	
	ObjectAnimator animY13 = ObjectAnimator.ofFloat(totalbottom,View.SCALE_X,0, 1f);
	animY13.setInterpolator(new BounceInterpolator());
	arrayListObjectAnimators.add(animY13);
	
	ObjectAnimator animY33 = ObjectAnimator.ofFloat(totalbottom,View.SCALE_Y,0, 1f);
	animY33.setInterpolator(new BounceInterpolator());
	arrayListObjectAnimators.add(animY33);
	
	

	ObjectAnimator[] objectAnimators = arrayListObjectAnimators.toArray(new ObjectAnimator[arrayListObjectAnimators.size()]);
	 animSetXY = new AnimatorSet();
	 
	animSetXY.playTogether(objectAnimators);
	animSetXY.setDuration(1000);//1sec


	
	List<ObjectAnimator> arrayListObjectAnimators1 = new ArrayList<ObjectAnimator>();

	ObjectAnimator animY5 = ObjectAnimator.ofFloat(boxwithcirclelayout,
			View.Y, 20, 0f);
	arrayListObjectAnimators1.add(animY5);// ArrayList of ObjectAnimators
	
	anim.setAnimationListener(new AnimationListener() {
		
		@Override
		public void onAnimationStart(Animation arg0) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onAnimationRepeat(Animation arg0) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onAnimationEnd(Animation arg0) {
			// TODO Auto-generated method stub
			
			toprightgreencircle.setVisibility(View.VISIBLE);
			topleftgreencircle.setVisibility(View.VISIBLE);
			bottomleftgreencircle.setVisibility(View.VISIBLE);
			bottomrightgreencircle.setVisibility(View.VISIBLE);
			totalbottom.setVisibility(View.VISIBLE);
			
			animSetXY.start();
			
		}
	});
	
	topleftgreencircle.setVisibility(View.GONE);
	toprightgreencircle.setVisibility(View.GONE);
	totalbottom.setVisibility(View.GONE);
	bottomleftgreencircle.setVisibility(View.GONE);
	bottomrightgreencircle.setVisibility(View.GONE);
	
	boxwithcirclelayout.setVisibility(View.GONE);
	
	boxwithcirclelayout.setVisibility(View.VISIBLE);
	boxwithcirclelayout.startAnimation(anim);
	
	// Gallery....
	topleftgreencircle.setOnClickListener(new OnClickListener() {
		
		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			  
	        Intent i = new Intent(
                    Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
	  
			startActivityForResult(Intent.createChooser(i,
			"Select Picture"), VueConstants.SELECT_PICTURE);
		}
	});
}


@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	// TODO Auto-generated method stub
	super.onActivityResult(requestCode, resultCode, data);
	
		try {
			// From Gallery...
			if (requestCode == VueConstants.SELECT_PICTURE) {
				Uri selectedImageUri = data.getData();

				// MEDIA GALLERY
				String selectedImagePath = Utils.getPath(selectedImageUri, this);

				if(!fromCreateAilseScreenflag)
				{
				 Intent intent = new Intent(this, CreateAisleActivity.class);
				 Bundle b = new Bundle();
				 b.putString(VueConstants.CREATE_AISLE_GALLERY_IMAGE_PATH_BUNDLE_KEY, selectedImagePath);
				 intent.putExtras(b);
		         startActivity(intent);
		         
		         finish();
				}
				else 
				{
					Log.e("CreateSelectionActivty", "onactivti result if" + requestCode+resultCode);
					Intent intent = new Intent();
					Bundle b = new Bundle();
				    b.putString(VueConstants.CREATE_AISLE_GALLERY_IMAGE_PATH_BUNDLE_KEY, selectedImagePath);
				    intent.putExtras(b);
				    setResult(VueConstants.CREATE_AILSE_ACTIVITY_RESULT, intent);
				    finish();
				}
				
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
}
}
