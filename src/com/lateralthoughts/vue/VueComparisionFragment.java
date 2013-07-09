package com.lateralthoughts.vue;

import android.annotation.TargetApi;
import android.app.Activity;

import android.os.Build;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class VueComparisionFragment extends Fragment {
    LinearLayout mTopScroller,mBottomScroller;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.aisle_comparision_view_fragment, container, false);

        mTopScroller = (LinearLayout) v.findViewById(R.id.vue_top_scroller);
        mBottomScroller = (LinearLayout) v.findViewById(R.id.vue_bottom_scroller);
        VueComparisionAdapter vcAdapter = new VueComparisionAdapter(getActivity(), null, null);
        vcAdapter.setUpImages(mTopScroller,mBottomScroller);
        return v;
    }

}
