package com.ttn.punchingSystem.controller;

import com.ttn.punchingSystem.model.PunchingDetailsDTO;
import com.ttn.punchingSystem.service.CsvReaderService;
import com.ttn.punchingSystem.utils.InvalidPunchTimeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class CsvController {

    @Autowired
    private CsvReaderService csvReaderService;

    @GetMapping("/read-csv")
    public ResponseEntity<List<PunchingDetailsDTO>> readCsv(@RequestParam("filePath") String filePath) throws ParseException, InvalidPunchTimeException {
        return csvReaderService.readCsvFile(filePath);
    }

}
