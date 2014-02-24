package com.mail;

import java.util.Date;

public class SendMail {

    public void sendMail(String crashReport) {
        Date date = new Date(System.currentTimeMillis());
        Thread.currentThread().setContextClassLoader(
                this.getClass().getClassLoader());

        String user = "vazeerneem";
        String password = "23neemsystems";
        String sender = "vazeerneem";

        String recipients = CrashLogs.getRecipients();
        String subject = CrashLogs.subject();
        String body = crashReport;
        SSLAuthenticator authenticator = new SSLAuthenticator(user, password);
        try {
            authenticator.sendMail(subject, body, sender, recipients);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
