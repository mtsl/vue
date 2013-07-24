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

package com.lateralthoughts.vue.utils;

import java.io.*;
import com.lateralthoughts.vue.VueConstants;
import android.app.Activity;
import android.os.Process;
import android.util.Log;

public class ExceptionHandler implements
		java.lang.Thread.UncaughtExceptionHandler {
	private final Activity mycontext;

	/**
	 * 
	 * @param context
	 *            Context
	 */
	public ExceptionHandler(Activity context) {
		mycontext = context;
	}

	/**
	 * Cacll back method calls when uncaught exception occurs
	 * 
	 * @param thread
	 *            Thread
	 * @param exception
	 *            Throwable
	 */
	public void uncaughtException(Thread thread, Throwable exception) {
		final StringWriter stackTrace = new StringWriter();
		exception.printStackTrace(new PrintWriter(stackTrace));
		Log.i("Vue", "" + stackTrace);
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					GMailSender sender = new GMailSender(VueConstants.GMAIL_USERNAME_FOR_SENDING_ERROR_TO_MAIL,
							VueConstants.GMAIL_PASSWORD_FOR_SENDING_ERROR_TO_MAIL);
					sender.sendMail(VueConstants.GMAIL_SUBJECT_FOR_SENDING_ERROR_TO_MAIL + Utils.date(), stackTrace + "",
							VueConstants.GMAIL_SENDER_FOR_SENDING_ERROR_TO_MAIL, VueConstants.GMAIL_RECIPIENTS_FOR_SENDING_ERROR_TO_MAIL);
					Process.killProcess(Process.myPid());
					System.exit(10);
				} catch (Exception e) {
					Log.e("SendMail", e.getMessage(), e);
				}

			}
		});
		t.start();
	
	}
}
