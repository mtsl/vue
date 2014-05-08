package com.lateralthoughts.vue.notification;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

import com.lateralthoughts.vue.R;
import com.lateralthoughts.vue.VueConstants;
import com.lateralthoughts.vue.VueLandingPageActivity;
import com.lateralthoughts.vue.domain.NotificationAisle;
import com.lateralthoughts.vue.ui.NotificationListAdapter;
import com.lateralthoughts.vue.utils.Utils;

public class PopupFragment extends Fragment {
    Context mContext;
    LayoutInflater mInflater;
    private int mListWidthFactor = 20;
    private ArrayList<NotificationAisle> mNotificationList;
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
    }
    
    public PopupFragment(ArrayList<NotificationAisle> notificationList) {
        mNotificationList = notificationList;
    }
    
    public PopupFragment() {
        
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.popup_list_layout, container, false);
        RelativeLayout relBg = (RelativeLayout) v
                .findViewById(R.id.overflow_listlayout_layout);
        relBg.getBackground().setAlpha(85);
        View heder = inflater.inflate(R.layout.popup_header, null);
        ImageView sendButton = (ImageView) heder.findViewById(R.id.send_button);
        LinearLayout listLay = (LinearLayout) v.findViewById(R.id.list_lay_id);
        ListView list = (ListView) v.findViewById(R.id.list_id);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                getScreenWidth(), LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        listLay.setLayoutParams(params);
        list.addHeaderView(heder);
        list.setAdapter(new NotificationListAdapter(mContext, mNotificationList));
        
        final EditText editText = (EditText) heder
                .findViewById(R.id.message_id);
        sendButton.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                final String feedBackText = editText.getText().toString();
                if (feedBackText.length() > 0) {
                    InputMethodManager imm = (InputMethodManager) getActivity()
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                    editText.setText("");
                    VueLandingPageActivity activity = (VueLandingPageActivity) getActivity();
                    activity.hideNotificationListFragment(true, feedBackText);
                } else {
                    Toast.makeText(getActivity(), "Type your feedback",
                            Toast.LENGTH_SHORT).show();
                }
                
            }
        });
        list.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        list.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                editText.getParent().requestDisallowInterceptTouchEvent(false);
                return false;
            }
        });
        editText.setOnTouchListener(new OnTouchListener() {
            
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // to provide touch scroll for edit text
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });
        return v;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
    
    @Override
    public void onDetach() {
        super.onDetach();
    }
    
    private int getScreenWidth() {
        DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
        int widthOfList = dm.widthPixels - (dm.widthPixels * mListWidthFactor)
                / 100;
        return widthOfList;
        
    }
}
