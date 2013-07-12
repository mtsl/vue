package com.lateralthoughts.vue;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;

import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.SlidingMenu.OnClosedListener;
import com.slidingmenu.lib.app.SlidingFragmentActivity;

public class BaseActivity extends SlidingFragmentActivity {

  private int mTitleRes = 0;
  protected static VueListFragment mFrag;
  boolean isBaseOnResumeCalled = false;
  //private OnBackHandle onBackHandle;
  //private boolean isBaseStarts = true;
  
  public BaseActivity(int titleRes) {
    mTitleRes = titleRes;
  }

  public BaseActivity() {
    Log.e("Profiling", "Profiling BaseActivity()");
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    Log.e("Profiling", "Profiling BaseActivity.onCreate() Start Before super call");
    super.onCreate(savedInstanceState);
    Log.e("Profiling", "Profiling BaseActivity.onCreate() After super call");
    // set the Behind View
    setBehindContentView(R.layout.menu_frame);
    if (mTitleRes != 0) {
      setTitle(mTitleRes);
    }

    Log.e("Profiling", "Profiling BaseActivity.onCreate() End");
     //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
  }

  
  @Override
	protected void onResume() {
    Log.e("Profiling", "Profiling BaseActivity.onResume() Start Before super call");
		super.onResume();
		Log.e("Profiling", "Profiling BaseActivity.onResume() After super call");
		if (!isBaseOnResumeCalled) {
			final SlidingMenu sm = getSlidingMenu();
			sm.setShadowWidthRes(R.dimen.shadow_width);
			sm.setShadowDrawable(R.drawable.shadow);
			sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
			sm.setFadeDegree(0.35f);
			sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
			isBaseOnResumeCalled = true;

			// customize the SlidingMenu
			FragmentTransaction t = BaseActivity.this
					.getSupportFragmentManager().beginTransaction();
			mFrag = new VueListFragment();
			// mFrag.setListClass(new ListClass());
			t.replace(R.id.menu_frame, mFrag);
			t.commit();
		}
		Log.e("Profiling", "Profiling BaseActivity.onResume() End");
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