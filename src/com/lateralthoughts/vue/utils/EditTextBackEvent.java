package com.lateralthoughts.vue.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

/**
 * 
 * EditText Custom class for EditText to hanled the events
 */
public class EditTextBackEvent extends EditText {
    Context mContext;
    boolean mKeyborOpenFlag = false;
    private OnInterceptListener mOnInterceptListenr;
    
    /**
     * 
     * @param context
     *            Context
     */
    public EditTextBackEvent(Context context) {
        super(context);
    }
    
    /**
     * 
     * @param context
     *            Context
     * @param attrs
     *            AttributeSet
     */
    public EditTextBackEvent(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }
    
    /**
     * 
     * @param context
     *            Context
     * @param attrs
     *            AttributeSet
     * @param defStyle
     *            int
     */
    public EditTextBackEvent(Context context, AttributeSet attrs, int defStyle) {
        
        super(context, attrs, defStyle);
        this.mContext = context;
    }
    
    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        try {
            if (keyCode == KeyEvent.KEYCODE_BACK
                    && event.getAction() == KeyEvent.ACTION_UP) {
                mOnInterceptListenr.onKeyBackPressed();
                return true;
            }
            return super.dispatchKeyEvent(event);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
    
    /**
     * set the disable listener
     * 
     * @param listener
     *            EditTextImeBackListener
     */
    public void setOnEditTextImeBackListener(EditTextImeBackListener listener) {
    }
    
    public interface EditTextImeBackListener {
        
        /**
         * 
         * @param ctrl
         *            EditTextBackEvent parent
         * @param text
         *            String
         */
        public abstract void onImeBack(EditTextBackEvent ctrl, String text);
    }
    
    /**
     * 
     * @param actionListen
     *            OnInterceptListener
     */
    public void setonInterceptListen(OnInterceptListener actionListen) {
        mOnInterceptListenr = actionListen;
    }
}
