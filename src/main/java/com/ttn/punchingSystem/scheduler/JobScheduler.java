package com.ttn.punchingSystem.scheduler;

import com.ttn.punchingSystem.model.PunchingDetailsDTO;
import com.ttn.punchingSystem.service.CsvReaderService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JobScheduler {

    @Autowired
    private CsvReaderService csvReaderService;

    @Scheduled(cron = "0 0 18 * * ?")
    @ApiOperation(value = "Read CSV from S3",
            notes = "This task runs at 6:00 PM every day and reads a CSV file from the S3 bucket.")
    public ResponseEntity<List<PunchingDetailsDTO>> readCsvFromS3() {
        return csvReaderService.readCsvFileFromS3();
    }
}
