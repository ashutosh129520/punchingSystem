package com.ttn.punchingSystem.service;

import com.google.gson.JsonObject;
import com.ttn.punchingSystem.utils.EmailConfigurationException;
import org.springframework.stereotype.Service;
import javax.mail.MessagingException;
import java.util.List;

public interface EmailService {

    void sendEmail(List<String> recipients) throws MessagingException, EmailConfigurationException;

}
