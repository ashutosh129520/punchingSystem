package com.ttn.punchingSystem.controller;

import com.ttn.punchingSystem.model.JobStatus;
import com.ttn.punchingSystem.scheduler.DynamicJobScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/scheduler")
public class HealthMonitorController {

    @Autowired
    private DynamicJobScheduler dynamicJobScheduler;

    @Autowired
    public HealthMonitorController(DynamicJobScheduler dynamicJobScheduler) {
        this.dynamicJobScheduler = dynamicJobScheduler;
    }

    @GetMapping("/health")
    public ResponseEntity<JobStatus> getSchedulerHealth() {
        try {
            JobStatus jobStatus = dynamicJobScheduler.getJobStatus();
            return ResponseEntity.ok(jobStatus);  // Return job status as JSON response
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new JobStatus("Error", null, null, null));
        }
    }
}

