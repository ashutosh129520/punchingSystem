package com.ttn.punchingSystem.controller;

import com.ttn.punchingSystem.model.EmailDTO;
import com.ttn.punchingSystem.model.PunchingDetailsDTO;
import com.ttn.punchingSystem.service.CsvReaderService;
import com.ttn.punchingSystem.service.EmailService;
import com.ttn.punchingSystem.utils.EmailConfigurationException;
import com.ttn.punchingSystem.utils.InvalidPunchTimeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
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
    public ResponseEntity<List<PunchingDetailsDTO>> readCsv(@RequestParam("filePath") String filePath) throws ParseException, InvalidPunchTimeException {
        return csvReaderService.readCsvFile(filePath);
    }

    //Will be scheduler later
    @PostMapping("/sendEmail")
    public void sendEmailOfDefaulters() throws MessagingException, EmailConfigurationException {
        Map<String, List<String>> managerToDefaultersMap = csvReaderService.processListOfDefaulters();
       // emailService.sendEmail(managerToDefaultersMap);
    }

}
