package com.ttn.punchingSystem.controller;

import com.ttn.punchingSystem.model.PunchData;
import com.ttn.punchingSystem.service.CsvReaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CsvController {

    @Autowired
    private CsvReaderService csvReaderService;

    @GetMapping("/read-csv")
    public List<PunchData> readCsv() {
        return csvReaderService.readCsvFile();
    }
}
