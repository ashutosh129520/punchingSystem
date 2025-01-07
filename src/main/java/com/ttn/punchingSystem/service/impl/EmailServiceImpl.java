package com.ttn.punchingSystem.service.impl;
import com.google.gson.JsonObject;
import com.ttn.punchingSystem.config.SecretsManagerService;
import com.ttn.punchingSystem.model.PunchingDetails;
import com.ttn.punchingSystem.service.EmailService;
import com.ttn.punchingSystem.utils.AppConstant;
import com.ttn.punchingSystem.utils.EmailConfigurationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.annotation.PostConstruct;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.*;

@Service
public class EmailServiceImpl implements EmailService {

    @Value("${mail.smtp.host}")
    private String smtpHost;

    @Value("${mail.smtp.port}")
    private int smtpPort;

    @Value("${mail.smtp.auth}")
    private String smtpAuth;

    @Value("${mail.smtp.starttls.enable}")
    private String smtpStarttlsEnable;

    @Autowired
    private SecretsManagerService secretsManagerService;
    @Autowired
    private TemplateEngine templateEngine;
    @Value("${spring.aws.secretsmanager.secretName}")
    private String secretName;
    private volatile JsonObject cachedSecrets;
    @PostConstruct
    public void init() {
        cachedSecrets = secretsManagerService.getSecrets(secretName);
    }

    private Session createEmailSession(JsonObject cachedSecrets) throws EmailConfigurationException {

        for(Map.Entry<String, String> entry : AppConstant.EMAIL_KEYS.entrySet()){
            String key = entry.getKey();
            String errorMessage = entry.getValue();
            if(!cachedSecrets.has(key) || cachedSecrets.get(key).getAsString().isEmpty()){
                throw new EmailConfigurationException(errorMessage);
            }
        }
            String senderEmail = cachedSecrets.get("SENDER_EMAIL").getAsString();
            String senderPassword = cachedSecrets.get("SENDER_PASSWORD").getAsString();
            Properties properties = new Properties();
            properties.put("mail.smtp.host", smtpHost);
            properties.put("mail.smtp.port", smtpPort);
            properties.put("mail.smtp.auth", smtpAuth);
            properties.put("mail.smtp.starttls.enable", smtpStarttlsEnable);
            return Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(senderEmail, senderPassword);
                }
            });
    }

    public void sendEmail(String secretName,
                          List<String> recipients,
                          String subject,
                          String templateName,
                          Map<String, Object> keyToValuesMap) throws MessagingException, EmailConfigurationException {
        Session session = createEmailSession(cachedSecrets);
        String senderEmail = cachedSecrets.get("SENDER_EMAIL").getAsString();

        String emailBody = generateEmailBody(templateName, keyToValuesMap);

        for (String recipient : recipients) {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
            message.setSubject(subject);
            message.setContent(emailBody, "text/html");

            Transport.send(message);
            System.out.println("Email sent successfully to: " + recipient);
        }
    }

    private String generateEmailBody(String templateName, Map<String, Object> keyToValuesMap) {
        Context context = new Context();
        context.setVariables(keyToValuesMap);
        return templateEngine.process(templateName, context);
    }

    public void sendDefaultersReport(Map<String, List<PunchingDetails>> managersToDefaultersMap) throws MessagingException, EmailConfigurationException {
        for (Map.Entry<String, List<PunchingDetails>> entry : managersToDefaultersMap.entrySet()) {
            String reportingManagerEmail = entry.getKey();
            List<PunchingDetails> defaulters = entry.getValue();
            Map<String, Object> keyToValuesMap = new HashMap<>();
            keyToValuesMap.put(AppConstant.REPORTING_MANAGER_MAIL, reportingManagerEmail.split("@")[0]);
            keyToValuesMap.put(AppConstant.DEFAULTERS, defaulters);

            sendEmail(secretName, Collections.singletonList(reportingManagerEmail), AppConstant.DEFAULTERS_REPORT, AppConstant.DEFAULTERS_REPORT_NAME, keyToValuesMap);
        }
    }
}
