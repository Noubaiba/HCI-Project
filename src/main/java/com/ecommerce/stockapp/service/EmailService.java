package com.ecommerce.stockapp.service;

import com.ecommerce.stockapp.config.SmtpConfig;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class EmailService {
    public void sendActivationEmail(String recipient, String name, String token) throws MessagingException {
        if (!SmtpConfig.configured()) {
            throw new MessagingException("SMTP is not configured. Set SMTP_USER, SMTP_PASSWORD and SMTP_FROM.");
        }

        String link = SmtpConfig.activationBaseUrl() + "?token=" + token;
        String body = """
                Hello %s,

                Welcome to the Product & Stock Management System.

                Your stock manager account has been created by an administrator.
                Activate your account and set your password using this link:

                %s

                Regards,
                Product & Stock Management System
                """.formatted(name, link);

        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", SmtpConfig.host());
        properties.put("mail.smtp.port", String.valueOf(SmtpConfig.port()));

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SmtpConfig.username(), SmtpConfig.password());
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(SmtpConfig.from()));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
        message.setSubject("Activate your stock manager account");
        message.setText(body);
        Transport.send(message);
    }
}
