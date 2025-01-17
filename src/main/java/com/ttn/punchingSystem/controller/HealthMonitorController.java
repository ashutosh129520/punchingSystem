package com.ttn.punchingSystem.controller;

import com.ttn.punchingSystem.model.JobStatus;
import com.ttn.punchingSystem.scheduler.DynamicJobScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/scheduler")
public class HealthMonitorController {

    @Autowired
    private DynamicJobScheduler dynamicJobScheduler;

    @GetMapping("/health")
    public ResponseEntity<JobStatus> getSchedulerHealth(@RequestParam String jobName) {
        try {
            JobStatus jobStatus = dynamicJobScheduler.getJobStatus(jobName);
            return ResponseEntity.ok(jobStatus);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new JobStatus("Error", null, null, e.getMessage()));
        }
    }
}
