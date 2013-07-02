package com.lateralthoughts.vue;

 

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
 
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;

import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.SlidingMenu.OnClosedListener;
import com.slidingmenu.lib.app.SlidingFragmentActivity;

public class BaseActivity extends SlidingFragmentActivity {

  private int mTitleRes = 0;
  protected static VueListFragment mFrag;
  //private OnBackHandle onBackHandle;
  //private boolean isBaseStarts = true;
  
  public BaseActivity(int titleRes) {
    mTitleRes = titleRes;
  }

  public BaseActivity() {
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // set the Behind View
    setBehindContentView(R.layout.menu_frame);
    if (mTitleRes != 0) {
      setTitle(mTitleRes);
    }

    // customize the SlidingMenu
    SlidingMenu sm = getSlidingMenu();
    sm.setShadowWidthRes(R.dimen.shadow_width);
    sm.setShadowDrawable(R.drawable.shadow);
    sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
    sm.setFadeDegree(0.35f);
    sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
    FragmentTransaction t = BaseActivity.this.getSupportFragmentManager().beginTransaction();
    mFrag = new VueListFragment();
    //mFrag.setListClass(new ListClass());
    t.replace(R.id.menu_frame, mFrag);
    t.commit();
     //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
  }

  
  @Override
	protected void onResume() {
		super.onResume();
		/*if(isBaseStarts) {
			isBaseStarts = false;
		new Handler().postDelayed(new Runnable() {
			
			public void run() {
				  FragmentTransaction t = BaseActivity.this.getSupportFragmentManager().beginTransaction();
				    mFrag = new SampleListFragment();
				    mFrag.setListClass(new ListClass());
				    t.replace(R.id.menu_frame, mFrag);
				    t.commit();
				
			}
		}, 50);
		}*/
	  
	}

  public void disp(String catname, int no, String tag) {
  
  }

  public void onTextSizeChanged() {
   
  }
  public void notifyDataCursor() {
	  
  }
  
  public class ClosingDashboard implements OnClosedListener {

    public void onClosed() {

      InputMethodManager imm = (InputMethodManager) BaseActivity.this
          .getSystemService(Context.INPUT_METHOD_SERVICE);
      imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
    }

  }

}