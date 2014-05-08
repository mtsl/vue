package com.lateralthoughts.vue.notification;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.lateralthoughts.vue.R;
import com.lateralthoughts.vue.domain.NotificationAisle;
import com.lateralthoughts.vue.ui.NotificationListAdapter;

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
        LinearLayout listLay = (LinearLayout) v.findViewById(R.id.list_lay_id);
        ListView list = (ListView) v.findViewById(R.id.list_id);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                getScreenWidth(), LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        listLay.setLayoutParams(params);
        list.addHeaderView(heder);
        list.setAdapter(new NotificationListAdapter(mContext, mNotificationList));
        TextView textView = (TextView) heder.findViewById(R.id.hint_text_id);
        textView.setVisibility(View.INVISIBLE);
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
