package Util;

import jakarta.mail.Session;
import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Message;
import jakarta.mail.Transport;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.*;
import java.util.Properties;

public class EmailSender {

    public static void send(String to, String subject, String content) {

        final String from = "lhngocc1304@gmail.com";
        final String password = "dyyj uxas daxj axsj";

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props,
            new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(from, password);
                }
            });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(to)
            );

            message.setSubject(subject);
            message.setText(content);

            Transport.send(message);

            System.out.println("Đã gửi: " + to);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}