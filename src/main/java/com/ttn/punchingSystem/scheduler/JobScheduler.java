package com.ttn.punchingSystem.scheduler;

import com.ttn.punchingSystem.config.DistributedLockManager;
import com.ttn.punchingSystem.model.PunchingDetailsDTO;
import com.ttn.punchingSystem.service.CsvReaderService;
import com.ttn.punchingSystem.utils.AppConstant;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class JobScheduler implements Job {

    private final DistributedLockManager lockManager;
    private final CsvReaderService csvReaderService;

    @Autowired
    public JobScheduler(DistributedLockManager lockManager, CsvReaderService csvReaderService) {
        this.lockManager = lockManager;
        this.csvReaderService = csvReaderService;
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        System.out.println("Executing CSV reading task...");

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
            throw new RuntimeException();
        } finally {
            lockManager.unlock(lock);
        }
    }
}
