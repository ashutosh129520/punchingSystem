package com.ttn.punchingSystem.controller;

import com.ttn.punchingSystem.model.PunchingDetails;
import com.ttn.punchingSystem.model.PunchingDetailsDTO;
import com.ttn.punchingSystem.service.CsvReaderService;
import com.ttn.punchingSystem.service.EmailService;
import com.ttn.punchingSystem.utils.EmailConfigurationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class CsvController {

    @Autowired
    private CsvReaderService csvReaderService;
    @Autowired
    private EmailService emailService;

    @GetMapping("/read-csv")
    public ResponseEntity<List<PunchingDetailsDTO>> readCsvFromS3() {
        return csvReaderService.readCsvFileFromS3();
    }

    //Will be scheduler later
    @PostMapping("/sendEmail")
    public void sendEmailOfDefaulters() throws MessagingException, EmailConfigurationException {
        Map<String, List<PunchingDetails>> managerToDefaultersMap = csvReaderService.processListOfDefaulters();
        emailService.sendDefaultersReport(managerToDefaultersMap);
    }

    @GetMapping("/reprocess")
    public ResponseEntity<String> reprocessCsvForDate(@RequestParam String date) {
        try {
            boolean isReprocessed = csvReaderService.reprocessFileForDate(date);
            if (isReprocessed) {
                return ResponseEntity.ok("File for " + date + " is successfully reprocessed.");
            } else {
                return ResponseEntity.ok("File for " + date + " is missing or invalid.");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
