package com.ttn.punchingSystem.controller;

import com.ttn.punchingSystem.model.PunchData;
import com.ttn.punchingSystem.service.CsvReaderService;
import org.springframework.beans.factory.annotation.Autowired;
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
    public List<PunchData> readCsv(@RequestParam("filePath") String filePath) throws ParseException {
        return csvReaderService.readCsvFile(filePath);
    }

}
