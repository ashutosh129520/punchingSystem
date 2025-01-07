package com.ttn.punchingSystem.service;

import com.google.gson.JsonObject;
import com.ttn.punchingSystem.model.PunchingDetails;
import com.ttn.punchingSystem.utils.EmailConfigurationException;
import org.springframework.stereotype.Service;
import javax.mail.MessagingException;
import java.util.List;
import java.util.Map;

public interface EmailService {

    void sendDefaultersReport(Map<String, List<PunchingDetails>> managersToDefaultersMap) throws MessagingException, EmailConfigurationException;

}
