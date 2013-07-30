package com.lateralthoughts.vue.utils;

import java.io.*;
import com.lateralthoughts.vue.VueConstants;
import com.lateralthoughts.vue.logging.Logger;

import android.app.Activity;
import android.os.Looper;
import android.os.Process;
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
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Logger.log("ERROR", "CrashActivity", stackTrace + "");
					GMailSender sender = new GMailSender(
							VueConstants.GMAIL_USERNAME_FOR_SENDING_ERROR_TO_MAIL,
							VueConstants.GMAIL_PASSWORD_FOR_SENDING_ERROR_TO_MAIL);
					sender.sendMail(
							VueConstants.GMAIL_SUBJECT_FOR_SENDING_ERROR_TO_MAIL
									+ Utils.date() + " (APK From Krishna)",
 
							stackTrace + "",
							VueConstants.GMAIL_SENDER_FOR_SENDING_ERROR_TO_MAIL,
							VueConstants.GMAIL_RECIPIENTS_FOR_SENDING_ERROR_TO_MAIL);
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
