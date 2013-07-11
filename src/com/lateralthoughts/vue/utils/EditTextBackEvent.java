package com.lateralthoughts.vue.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

/**
 * 
 * @author raju EditText Custom class for EditText to hanled the events
 */
public class EditTextBackEvent extends EditText {
  Context context;
  public int x;
  private OnInterceptListener onInterceptListenr;
  //private EditTextImeBackListener mOnImeBack;

  /**
   * 
   * @param context Context
   */
  public EditTextBackEvent(Context context) {
    super(context);
  }

  /**
   * 
   * @param context Context
   * @param attrs AttributeSet
   */
  public EditTextBackEvent(Context context, AttributeSet attrs) {
    super(context, attrs);
    this.context = context;
  }

  /**
   * 
   * @param context Context
   * @param attrs AttributeSet
   * @param defStyle int
   */
  public EditTextBackEvent(Context context, AttributeSet attrs, int defStyle) {

    super(context, attrs, defStyle);
    this.context = context;
  }

  @Override
  public boolean onKeyPreIme(int keyCode, KeyEvent event) {
    try {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
		  onInterceptListenr.onInterceptTouch();
		  return false;
		}
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

    return super.dispatchKeyEvent(event);
  }

  /**
   * set the disable listener
   * 
   * @param listener EditTextImeBackListener
   */
  public void setOnEditTextImeBackListener(EditTextImeBackListener listener) {
    //mOnImeBack = listener;
  }

  /**
   * 
   * @author raju
   * 
   */
  public interface EditTextImeBackListener {

    /**
     * 
     * @param ctrl EditTextBackEvent parent
     * @param text String
     */
    public abstract void onImeBack(EditTextBackEvent ctrl, String text);
  }

  /**
   * 
   * @param actionListen OnInterceptListener
   */
  public void setonInterceptListen(OnInterceptListener actionListen) {
    onInterceptListenr = actionListen;
  }
}
