package com.ttn.punchingSystem.scheduler;

import com.ttn.punchingSystem.config.DistributedLockManager;
import com.ttn.punchingSystem.model.PunchingDetailsDTO;
import com.ttn.punchingSystem.service.CsvReaderService;
import com.ttn.punchingSystem.utils.AppConstant;
import io.swagger.annotations.ApiOperation;
import org.redisson.api.RLock;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class JobScheduler {

    private final DistributedLockManager lockManager;
    private final CsvReaderService csvReaderService;

    public JobScheduler(DistributedLockManager lockManager, CsvReaderService csvReaderService) {
        this.lockManager = lockManager;
        this.csvReaderService = csvReaderService;
    }

    @Scheduled(cron = "0 59 14 * * ?")
    @ApiOperation(value = "Read CSV from S3",
            notes = "This task runs at 2:59 PM every day and reads a CSV file from the S3 bucket.")
    public void readCsvFromS3() {
        RLock lock = null;
        try {
            lock = lockManager.tryLock(AppConstant.LOCK_KEY, 0, 5, TimeUnit.MINUTES);
            if (lock != null) {
                ResponseEntity<List<PunchingDetailsDTO>> response = csvReaderService.readCsvFileFromS3();
                System.out.println("Task executed successfully: " + response.getBody());
            } else {
                System.out.println("Another instance is already running the task.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lockManager.unlock(lock);
        }
    }
}
