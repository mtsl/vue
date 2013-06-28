/*
 * Copyright (C) 2007-2011 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package com.lateralthoughts.vue;

 

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
/**
 * 
 * @author raju
 *
 */
public class CrashActivity extends Activity {
	public static final String STACKTRACE = "stacktrace";
/**
 * @param icicle Bundle
 */
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		final String stackTrace = getIntent().getStringExtra(STACKTRACE);
		//show the popup for the exception
		showAlert(stackTrace);
	}
	/**
	 * 
	 * @param message String
	 */
	   void showAlert(String message) {
    	   AlertDialog alert = new AlertDialog.Builder(this).create();
    	      alert.setMessage(message);
    	      alert.setButton("OK",
    	          new DialogInterface.OnClickListener() {
    	            public void onClick(DialogInterface dialog, int which) {
    	              dialog.dismiss();
    	              finish();
    	            }
    	          });
    	      alert.show();
    	      alert.setOnCancelListener(new OnCancelListener() {
    			
    			public void onCancel(DialogInterface dialog) {
    				finish();
    			}
    		});
    	      return;
    }
	   private void showAlert(String title,String message ){
			final Dialog dialog = new Dialog(this);
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.setContentView(R.layout.alertbox);
			TextView titleview = (TextView) dialog.findViewById(R.id.title);
			TextView messageview = (TextView) dialog.findViewById(R.id.message);
			titleview.setText(Html.fromHtml("<b>fashion</b>wrap"));
			messageview.setText(message);
			Button okbut = (Button) dialog.findViewById(R.id.okbutn);
			Button cancelbut = (Button) dialog.findViewById(R.id.cancelbutn);
			cancelbut.setVisibility(View.GONE);
			okbut.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					dialog.dismiss();
					  
					 
					// if dialogue creates for no network message just close the
					// dialogue and stay in the same screen
				 
				}
			});
	 
			dialog.show();
	  }
    
}
