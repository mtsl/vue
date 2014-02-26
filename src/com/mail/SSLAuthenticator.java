package com.mail;

import java.util.Properties;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import javax.mail.Message;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/** SMTP server using SSL connection. */
public class SSLAuthenticator extends javax.mail.Authenticator {

  String username;
  String password;
  Session session;
  String mailhost = /*"smtpout.asia.secureserver.net"*/"smtp.gmail.com"/*"imap.mail.yahoo.com"*/ /*"smtp.mail.yahoo.co.in"*/;
  String filePath;
  public SSLAuthenticator(String username, String password) {
    this.username = username;
    this.password = password;
   // this.filePath = filePath;
    Properties props = getGmailProperties();

    session = Session.getDefaultInstance(props, this);

  }

  @Override
  protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
    return new javax.mail.PasswordAuthentication(username, password);
  }

  public synchronized void sendMail(String subject, String body, String sender,
      String recipients) throws Exception {
    try {

      // add handlers for main MIME types
      MailcapCommandMap mc = (MailcapCommandMap) CommandMap
          .getDefaultCommandMap();
      mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
      mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
      mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
      mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
      mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
      CommandMap.setDefaultCommandMap(mc);
      MimeMessage message = new MimeMessage(session);
      message.setSender(new InternetAddress(sender));
      message.setSubject(subject);
    
        
      // Create the message part
   //vaz   BodyPart messageBodyPart = new MimeBodyPart();
      
      // Fill the message
     //vaz   messageBodyPart.setText(body);
     // vaz Multipart multipart = new MimeMultipart();
     // vaz  multipart.addBodyPart(messageBodyPart);

      // Part two is attachment
    /*  messageBodyPart = new MimeBodyPart();
      String filename = filePath;
      DataSource source = new FileDataSource(filename);
      messageBodyPart.setDataHandler(new DataHandler(source));
      messageBodyPart.setFileName(filename);
      multipart.addBodyPart(messageBodyPart);*/

      // Put parts in message
      message.setContent(body,"text/html");
      if (recipients.indexOf(',') > 0) {
        message.setRecipients(Message.RecipientType.TO,
            InternetAddress.parse(recipients));
        System.out.println("sendMail() = 7.0");
      } else {
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(
            recipients));
        System.out.println("sendMail() = 7.1");
      }
      Transport transport = session.getTransport("smtp");
      transport.connect(mailhost, username, password);
      transport.sendMessage(message, message.getAllRecipients());
      transport.close();
      // Transport.send(message);
      System.out.println("Mail sent sucessfully...");
    } catch (NoSuchProviderException e) {
      e.printStackTrace();
    }
  }
  private Properties getGmailProperties() {
    Properties props = new Properties();
    props.put("mail.smtp.host", mailhost); // for gmail
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.socketFactory.port", "465");
    props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

    return props;
  }
  private Properties getYahooMailProperties() {
 // Auth.
    String host = "smtp.mail.yahoo.com";
    String port = "465";

 // Configure your JavaMail.
   /* Properties props = new Properties();
    props.setProperty( "mail.transport.protocol", "smtps");
    props.setProperty( "mail.smtps.auth", "true");
    props.setProperty( "mail.host", host);
    props.setProperty( "mail.port", port);
    props.setProperty( "mail.user", username);
    props.setProperty( "mail.password", password);
*/
    Properties props = new Properties();
    props.setProperty("mail.transport.protocol", "smtps");
    props.put("mail.smtps.auth", "true");
    props.setProperty("mail.smtps.host", "smtp.mail.yahoo.com");
    props.put("mail.smtps.port", "465");
    props.setProperty("mail.user", username);
    props.setProperty("mail.password", password);
    return props;
  }
  private Properties get23NeemProperties() {
    Properties props = new Properties();
    props.put("mail.smtp.host", "smtpout.asia.secureserver.net"); // for Godady.com
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.socketFactory.port", "465");
    props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
    

  /*  props.setProperty( "mail.transport.protocol", "smtps");
    props.setProperty( "mail.smtps.auth", "true");
    props.setProperty( "mail.host", "smtpout.secureserver.net");
    props.setProperty( "mail.port", "80");
    props.setProperty( "mail.user", username);
    props.setProperty( "mail.password", password);*/
    
    return props;
  }
}
