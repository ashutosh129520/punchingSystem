package com.ttn.punchingSystem.service.impl;
import com.google.gson.JsonObject;
import com.ttn.punchingSystem.config.SecretsManagerService;
import com.ttn.punchingSystem.service.EmailService;
import com.ttn.punchingSystem.utils.AppConstant;
import com.ttn.punchingSystem.utils.EmailConfigurationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private SecretsManagerService secretsManagerService;
    @Value("${spring.aws.secretsmanager.secretName}")
    private String secretName;

    private Session createEmailSession(JsonObject secrets) throws EmailConfigurationException {

        for(Map.Entry<String, String> entry : AppConstant.EMAIL_KEYS.entrySet()){
            String key = entry.getKey();
            String errorMessage = entry.getValue();
            if(!secrets.has(key) || secrets.get(key).getAsString().isEmpty()){
                throw new EmailConfigurationException(errorMessage);
            }
        }
            String smtpHost = secrets.get("SMTP_HOST").getAsString();
            String smtpPort = secrets.get("SMTP_PORT").getAsString();
            String smtpAuth = secrets.get("SMTP_AUTH").getAsString();
            String smtpStarttls = secrets.get("SMTP_STARTTLS").getAsString();
            String senderEmail = secrets.get("SENDER_EMAIL").getAsString();
            String senderPassword = secrets.get("SENDER_PASSWORD").getAsString();
            Properties properties = new Properties();
            properties.put("mail.smtp.host", smtpHost);
            properties.put("mail.smtp.port", smtpPort);
            properties.put("mail.smtp.auth", smtpAuth);
            properties.put("mail.smtp.starttls.enable", smtpStarttls);
            return Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(senderEmail, senderPassword);
                }
            });
    }

        public void sendEmail(List<String> recipients) throws MessagingException, EmailConfigurationException {
            JsonObject secrets = secretsManagerService.getSecrets(secretName);
            Session session = createEmailSession(secrets);
            Message message = new MimeMessage(session);
            String senderEmail = secrets.get("SENDER_EMAIL").getAsString();
            message.setFrom(new InternetAddress(senderEmail));
            InternetAddress[] recipientAddresses = recipients.stream().map(email -> {
                try{
                    return new InternetAddress(email);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).toArray(InternetAddress[]::new);
            message.setRecipients(Message.RecipientType.TO, recipientAddresses);
            message.setSubject("subject");
            message.setText("This is a test email");

            Transport.send(message);
            System.out.println("Email sent successfully");
        }
}
