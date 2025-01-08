package com.ttn.punchingSystem.controller;

import com.ttn.punchingSystem.model.PunchingDetails;
import com.ttn.punchingSystem.model.PunchingDetailsDTO;
import com.ttn.punchingSystem.service.CsvReaderService;
import com.ttn.punchingSystem.service.EmailService;
import com.ttn.punchingSystem.utils.EmailConfigurationException;
import com.ttn.punchingSystem.utils.InvalidPunchTimeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class CsvController {

    @Autowired
    private CsvReaderService csvReaderService;
    @Autowired
    private EmailService emailService;

    @Scheduled(cron = "0 0 18 * * ?") // This cron expression runs the task at 6:00 PM every day
    public ResponseEntity<List<PunchingDetailsDTO>> readCsvFromS3() throws ParseException, InvalidPunchTimeException {
        return csvReaderService.readCsvFileFromS3();
    }

    //Will be scheduler later
    @PostMapping("/sendEmail")
    public void sendEmailOfDefaulters() throws MessagingException, EmailConfigurationException {
        Map<String, List<PunchingDetails>> managerToDefaultersMap = csvReaderService.processListOfDefaulters();
        emailService.sendDefaultersReport(managerToDefaultersMap);
    }

}
