package com.lateralthoughts.vue;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class LoginWarningMessage {

	Context mContext = null;

	public LoginWarningMessage(Context context) {
		mContext = context;
	}

	public void showLoginWarningMessageDialog(String warningMessage,
			boolean isLimitReachedFlag,
			final boolean isFromDataEntryScreenFlag, final int count,
			final EditText editText, final RelativeLayout view) {
		final Dialog dialog = new Dialog(mContext,
				R.style.Theme_Dialog_Translucent);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.googleplusappinstallationdialog);
		final TextView noButton = (TextView) dialog.findViewById(R.id.nobutton);
		TextView okButton = (TextView) dialog.findViewById(R.id.okbutton);
		TextView messagetext = (TextView) dialog.findViewById(R.id.messagetext);
		messagetext.setText(warningMessage);
		if (isLimitReachedFlag) {
			noButton.setText("Browse");
		} else {
			noButton.setText("Later");
		}
		okButton.setText("LoginNow");
		okButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				dialog.dismiss();
				Intent i = new Intent(mContext, VueLoginActivity.class);
				Bundle b = new Bundle();
				b.putBoolean(VueConstants.CANCEL_BTN_DISABLE_FLAG, false);
				b.putString(VueConstants.FROM_INVITEFRIENDS, null);
				b.putBoolean(VueConstants.FBLOGIN_FROM_DETAILS_SHARE, false);
				b.putBoolean(VueConstants.FROM_BEZELMENU_LOGIN, false);
				i.putExtras(b);
				mContext.startActivity(i);
			}
		});
		noButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				dialog.dismiss();
				if (noButton.getText().toString().equals("Later")) {
					if (isFromDataEntryScreenFlag) {
						SharedPreferences sharedPreferencesObj = mContext
								.getSharedPreferences(
										VueConstants.SHAREDPREFERENCE_NAME, 0);
						SharedPreferences.Editor editor = sharedPreferencesObj
								.edit();
						editor.putInt(
								VueConstants.CREATED_AISLE_COUNT_IN_PREFERENCE,
								count + 1);
						editor.commit();
						DataEntryFragment fragment = (DataEntryFragment) ((FragmentActivity) DataEntryActivity.mDataEntryActivityContext)
								.getSupportFragmentManager().findFragmentById(
										R.id.create_aisles_view_fragment);
						fragment.storeMetaAisleDataIntoLocalStorage();
					} else {
						SharedPreferences sharedPreferencesObj = mContext
								.getSharedPreferences(
										VueConstants.SHAREDPREFERENCE_NAME, 0);
						SharedPreferences.Editor editor = sharedPreferencesObj
								.edit();
						editor.putInt(
								VueConstants.COMMENTS_COUNT_IN_PREFERENCES,
								count + 1);
						editor.commit();
						VueAisleDetailsViewFragment mVueAiselFragment = (VueAisleDetailsViewFragment) ((FragmentActivity) AisleDetailsViewActivity.mAisleDetailsActivityContext)
								.getSupportFragmentManager().findFragmentById(
										R.id.aisle_details_view_fragment);
						mVueAiselFragment.addComment(editText, view);
					}
				}
			}
		});
		dialog.show();
	}
}
