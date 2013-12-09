package com.lateralthoughts.vue.utils;

import com.lateralthoughts.vue.R;

import android.content.Context;
import android.view.ActionProvider;
import android.view.LayoutInflater;
import android.view.View;

public class CustomActionbar extends ActionProvider {

	private Context mContext;
	public CustomActionbar(Context context) {
		super(context);
		mContext = context;
	}

	@Override
	public View onCreateActionView() {
		LayoutInflater layoutInflater = LayoutInflater.from(mContext);
	    View view = layoutInflater.inflate(R.layout.vue_landing_custom_actionbar, null);
		return view;
	}

}
