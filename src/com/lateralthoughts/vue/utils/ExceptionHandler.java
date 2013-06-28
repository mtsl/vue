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

import com.lateralthoughts.vue.CrashActivity;

import android.content.*;
import android.os.Process;
import android.util.Log;
/**
 * 
 * @author raju
 *
 */
public class ExceptionHandler implements 
java.lang.Thread.UncaughtExceptionHandler {
	private final Context mycontext;
/**
 * 
 * @param context Context
 */
	public ExceptionHandler(Context context) {
		mycontext = context;
	}
/**
 * Cacll back method calls when uncaught exception occurs
 * @param thread Thread
 * @param exception Throwable
 */
	public void uncaughtException(Thread thread, Throwable exception) {
		StringWriter stackTrace = new StringWriter();
		exception.printStackTrace(new PrintWriter(stackTrace));
		Log.i("FISH", "" + stackTrace);
		Intent intent = new Intent(mycontext, CrashActivity.class);
		intent.putExtra(CrashActivity.STACKTRACE, stackTrace.toString());
		mycontext.startActivity(intent);
		Process.killProcess(Process.myPid());
		System.exit(10);
	}
}
