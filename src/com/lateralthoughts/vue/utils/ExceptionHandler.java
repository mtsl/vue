package com.lateralthoughts.vue.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import android.app.Activity;
import android.util.Log;

public class ExceptionHandler implements
		java.lang.Thread.UncaughtExceptionHandler {

	/**
	 * 
	 * @param context
	 *            Context
	 */
	public ExceptionHandler(Activity context) {
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
	}
}
