package com.lateralthoughts.vue.notification;

import java.util.ArrayList;

import com.lateralthoughts.vue.R;
import com.lateralthoughts.vue.VueApplication;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

public class PopupFragment extends Fragment {
    Context mContext;
    LayoutInflater mInflater;
    private int mListWidthFactor = 20;
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
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
        ArrayList<String> hintKeywordsList = getAdapterList();
        list.setAdapter(new NotificationListAdapter(mContext, hintKeywordsList));
        EditText editText = (EditText) heder.findViewById(R.id.message_id);
        TextView textView = (TextView) heder.findViewById(R.id.hint_text_id);
        textView.setVisibility(View.INVISIBLE);
        //headerWatcher(editText, textView);
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
    
    // get your adapter data here.
    private ArrayList<String> getAdapterList() {
        ArrayList<String> hintKeywordsList = new ArrayList<String>();
        hintKeywordsList.add("asdf");
        hintKeywordsList.add("asdf");
        hintKeywordsList.add("asdf");
        hintKeywordsList.add("asdf");
        hintKeywordsList.add("asdf");
        hintKeywordsList.add("asdf");
        hintKeywordsList.add("asdf");
        hintKeywordsList.add("asdf");
        hintKeywordsList.add("asdf");
        hintKeywordsList.add("asdf");
        hintKeywordsList.add("asdf");
        hintKeywordsList.add("asdf");
        hintKeywordsList.add("asdf");
        hintKeywordsList.add("asdf");
        hintKeywordsList.add("asdf");
        hintKeywordsList.add("asdf");
        return hintKeywordsList;
    }
    
    private void headerWatcher(EditText edtText, final TextView hintView) {
        edtText.addTextChangedListener(new TextWatcher() {
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                    int count) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                    int after) {
                String s2 = s.toString();
                if (s2.length() == 0) {
                    afterTextChange(hintView, VueApplication.getInstance()
                            .getPixel(40));
                } else if(s2.length() == 1){
                    afterTextChange(hintView, VueApplication.getInstance()
                            .getPixel(-40)); 
                }
                
            }
            
            @Override
            public void afterTextChanged(Editable s) {
            
                
            }
        });
    }
    
    private void beforeTextChange(final View view, final int moiveSpace) {
        TranslateAnimation anim = new TranslateAnimation(0, 0, -moiveSpace, 0);
        anim.setDuration(500);
        
        anim.setAnimationListener(new TranslateAnimation.AnimationListener() {
            
            @Override
            public void onAnimationStart(Animation animation) {
            }
            
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
            
            @Override
            public void onAnimationEnd(Animation animation) {
                
                  RelativeLayout.LayoutParams params =
                   (RelativeLayout.LayoutParams)view.getLayoutParams();
                    params.bottomMargin  = moiveSpace; 
                   view.setLayoutParams(params);
                 
            }
        });
        view.setVisibility(View.VISIBLE);
        view.startAnimation(anim);
    }
    
    private void afterTextChange(final View view, final int moiveSpace) {
        
        TranslateAnimation anim = new TranslateAnimation(0, 0, 0, moiveSpace);
        anim.setDuration(500);
        
        anim.setAnimationListener(new TranslateAnimation.AnimationListener() {
            
            @Override
            public void onAnimationStart(Animation animation) {
            }
            
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
            
            @Override
            public void onAnimationEnd(Animation animation) {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view
                        .getLayoutParams();
                params.bottomMargin  = moiveSpace; 
                view.setLayoutParams(params);
                view.setVisibility(View.VISIBLE);
            }
        });
        
        view.startAnimation(anim);
        
    }
}
