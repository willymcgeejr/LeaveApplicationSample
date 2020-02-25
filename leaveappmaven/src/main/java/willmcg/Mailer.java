package willmcg;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

/** A class containing functions that allow for email confirmation of submissions. */
public class Mailer {
  // Define the connection parameters for the email API
  private String mailHost = "REDACTED";
  private String port = "587";
  private String emailUser = "REDACTED";
  private String emailPassword = "REDACTED";
  private String fromEmail = "REDACTED";
  private String fromName = "REDACTED";

  /**
   * A function that allows you to specify the recipient and content of an email, then sends the
   * email.
   *
   * @param to the recipient email address as a String (example@website.com)
   * @param cn the common name of the recipient given as a String (John Doe)
   * @param subject the subject line of the email as a String
   * @param body the body of the email given as a String
   */
  public void sendMail(String to, String cn, String subject, String body) {
    try {
      // Specify the properties of the connection
      Properties props = new Properties();
      props.put("mail.smtp.host", mailHost);
      props.put("mail.smtp.port", port);
      props.put("mail.smtp.ssl.trust", mailHost);
      props.put("mail.smtp.starttls.enable", "true");
      props.put("mail.smtp.auth", "true");
      props.put("mail.smtp.connectiontimeout", "10000");

      // Connect to the mail server with the dummy credentials
      Session session =
          Session.getInstance(
              props,
              new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                  return new PasswordAuthentication(emailUser, emailPassword);
                }
              });

      // Set the message parameters/details
      session.setDebug(true);
      InternetAddress fromAddress = new InternetAddress(fromEmail, fromName);
      InternetAddress toAddress = new InternetAddress(to, cn);
      String msgSubject = subject;
      String msgBody = body;

      // Pass the parameters to the API
      Message msg = new MimeMessage(session);
      msg.setFrom(fromAddress);
      msg.addRecipient(Message.RecipientType.TO, toAddress);
      msg.setSubject(msgSubject);
      msg.setContent(msgBody, "text/html");

      // Send it!
      Transport transport = session.getTransport("smtp");
      transport.connect();
      transport.sendMessage(msg, msg.getAllRecipients());
    } catch (MessagingException e) {
      System.out.println(e.getMessage() + e.getStackTrace());
    } catch (UnsupportedEncodingException e) {
      System.out.println(e.getMessage());
    }
  }

  /**
   * A function that allows you to specify the recipient and content of an email, then sends the
   * email with a button that links to a file
   *
   * @param to the recipient email address as a String (example@website.com)
   * @param cn the common name of the recipient given as a String (John Doe)
   * @param subject the subject line of the email as a String
   * @param body the body of the email given as a String
   * @param filepath the filepath of the "Review" button that is included at the end of the email
   */
  public void buttonMail(String to, String cn, String subject, String body, String filepath) {
    try {
      // Specify the properties of the connection
      Properties props = new Properties();
      props.put("mail.smtp.host", mailHost);
      props.put("mail.smtp.port", port);
      props.put("mail.smtp.ssl.trust", mailHost);
      props.put("mail.smtp.starttls.enable", "true");
      props.put("mail.smtp.auth", "true");
      props.put("mail.smtp.connectiontimeout", "10000");

      Session session =
          Session.getInstance(
              props,
              new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                  return new PasswordAuthentication(emailUser, emailPassword);
                }
              });

      // Set the message parameters/details
      session.setDebug(true);
      InternetAddress fromAddress = new InternetAddress(fromEmail, fromName);
      InternetAddress toAddress = new InternetAddress(to, cn);
      String msgSubject = subject;
      String msgBody = body;

      // Pass the parameters to the API
      Message msg = new MimeMessage(session);
      msg.setFrom(fromAddress);
      msg.addRecipient(Message.RecipientType.TO, toAddress);
      msg.setSubject(msgSubject);
      msg.setContent(
          msgBody
              + "<br/>"
              + "<a href=\""
              + filepath
              + "\"><input class=\"MyButton\"  type = \"button\" value =\"Review Request\" /></a>\r\n"
              + "</form>",
          "text/html");

      // Send it!
      Transport transport = session.getTransport("smtp");
      transport.connect();
      transport.sendMessage(msg, msg.getAllRecipients());
    } catch (MessagingException e) {
      System.out.println(e.getMessage() + e.getStackTrace());
    } catch (UnsupportedEncodingException e) {
      System.out.println(e.getMessage());
    }
  }
}
