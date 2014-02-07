package com.lateralthoughts.vue.pendingaisles;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.haarman.listviewanimations.swinginadapters.prepared.SwingBottomInAnimationAdapter;
import com.lateralthoughts.vue.AisleDetailsViewActivity;
import com.lateralthoughts.vue.AisleWindowContent;
import com.lateralthoughts.vue.R;
import com.lateralthoughts.vue.VueApplication;
import com.lateralthoughts.vue.connectivity.DataBaseManager;

public class PendingAisles extends Activity {
    SwingBottomInAnimationAdapter swingBottomInAnimationAdapter;
    Colors colors;
    private LayoutInflater mInflater;
    ArrayList<AisleWindowContent> mWindowList = null;
    private  String clickedPendingAisleId = "";
    public static boolean mIsImageAddedToPendingAisle = false;
    SampleAdapter mAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pending_aisle_list);
        colors = new Colors();
        getActionBar().setTitle("Pending");
        mWindowList = DataBaseManager.getInstance(VueApplication.getInstance())
                .getPendingAisles();
        if (mWindowList != null && mWindowList.size() > 0) {
            ListView mListView = (ListView) findViewById(R.id.listview);
            mAdapter = new SampleAdapter(this);
            swingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter(
                    mAdapter);
            swingBottomInAnimationAdapter.setListView(mListView);
            mListView.setAdapter(swingBottomInAnimationAdapter);
        } else {
            Toast.makeText(this, "There are no pending aisles",
                    Toast.LENGTH_LONG).show();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if(mWindowList != null && mWindowList.size() > 0){
            for(int i =0;i<mWindowList.size(); i++){
                if(clickedPendingAisleId.equalsIgnoreCase(mWindowList.get(i).getAisleContext().mAisleId)){
                    if(mIsImageAddedToPendingAisle) {
                    mWindowList.remove(i);
                    mIsImageAddedToPendingAisle = false;
                    }
                    break;
                }
            }
        }
        if(mAdapter != null){
            mAdapter.notifyDataSetChanged();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.landing_actionbar, menu);
        getActionBar().setHomeButtonEnabled(true);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        
        // Handle your other action bar items...
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // set menu search visibility to true when backend functionality is
        // ready
        menu.findItem(R.id.menu_search).setVisible(false);
        menu.findItem(R.id.menu_create_aisle).setVisible(false);
        menu.findItem(R.id.menu_pending_aisle).setVisible(false);
        return super.onPrepareOptionsMenu(menu);
    }
    
    private class SampleAdapter extends BaseAdapter {
        Context mContext;
        
        public SampleAdapter(Context context) {
            mContext = context;
        }
        
        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mWindowList.size();
        }
        
        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return position;
        }
        
        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            final int pos = position;
            ViewHOldr holder;
            if (convertView == null) {
                holder = new ViewHOldr();
                if (mInflater == null) {
                    mInflater = (LayoutInflater) mContext
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                }
                convertView = (RelativeLayout) mInflater.inflate(
                        R.layout.penidng_aisle_child, null);
                holder.textView = (TextView) convertView
                        .findViewById(R.id.child_itemTextview);
                holder.initial = (TextView) convertView
                        .findViewById(R.id.initial);
                holder.lookinf_for = (TextView) convertView
                        .findViewById(R.id.looking_for);
                holder.lookingfor_lay = (LinearLayout) convertView
                        .findViewById(R.id.lookingfor_lay);
                convertView.setTag(holder);
            }
            String test = mWindowList.get(position).getAisleContext().mLookingForItem;
            String lastWord = test.substring(test.lastIndexOf(" ") + 1);
            lastWord = lastWord.toUpperCase();
            char firstChar = lastWord.charAt(0);
            holder = (ViewHOldr) convertView.getTag();
            String userName = getUserName(mWindowList.get(position)
                    .getAisleContext().mFirstName, mWindowList.get(position)
                    .getAisleContext().mLastName);
            if (userName != null && userName.length() > 2) {
                userName = userName.substring(0, 1).toUpperCase()
                        + userName.substring(1);
            }
            holder.textView.setText(userName);
            holder.initial.setText(String.valueOf(firstChar));
            holder.lookingfor_lay.setBackgroundColor(Color.parseColor(colors
                    .getColorCode(position)));
            holder.initial.setTextColor(Color.parseColor(colors
                    .getColorCode(position)));
            holder.lookinf_for
                    .setText("Looking for "
                            + mWindowList.get(position).getAisleContext().mLookingForItem);
            convertView.setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    clickedPendingAisleId = mWindowList.get(pos).getAisleContext().mAisleId;
                    VueApplication.getInstance().setPendingAisle(
                            mWindowList.get(pos));
                    Intent intent = new Intent();
                    intent.setClass(VueApplication.getInstance(),
                            AisleDetailsViewActivity.class);
                    VueApplication.getInstance().setClickedWindowID(
                            mWindowList.get(pos).getAisleContext().mAisleId);
                    VueApplication.getInstance().setClickedWindowCount(0);
                    VueApplication.getInstance().setmAisleImgCurrentPos(0);
                    startActivity(intent);
                    
                }
            });
            return convertView;
        }
        
    }
    
    private String getUserName(String firstName, String lastName) {
        String mVueusername = " ";
        if (firstName != null && lastName != null) {
            mVueusername = firstName + " " + lastName;
        } else if (firstName != null) {
            if (firstName.equals("Anonymous")) {
                mVueusername = VueApplication.getInstance().getmUserInitials();
            } else {
                mVueusername = firstName;
            }
        } else if (lastName != null) {
            mVueusername = lastName;
        }
        if (mVueusername != null
                && mVueusername.trim().equalsIgnoreCase("Anonymous")) {
            if (VueApplication.getInstance().getmUserInitials() != null) {
                mVueusername = VueApplication.getInstance().getmUserInitials();
            }
        }
        return mVueusername;
    }
    
    private class ViewHOldr {
        TextView textView;
        TextView initial;
        TextView lookinf_for;
        LinearLayout lookingfor_lay;
    }
    
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            VueApplication.getInstance().setPendingAisle(null);
            finish();
        }
        return false;
    }
}
