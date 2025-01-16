package com.ttn.punchingSystem.controller;

import com.ttn.punchingSystem.scheduler.DynamicJobScheduler;
import com.ttn.punchingSystem.model.JobStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/scheduler")
public class SchedulerController {

    @Autowired
    private DynamicJobScheduler jobScheduler;

    // API to update cron expression dynamically
    @PostMapping("/update")
    public String updateCron(@RequestParam String cron) {
        try {
            jobScheduler.rescheduleJob(cron);
            return "Cron expression updated to: " + cron;
        } catch (Exception e) {
            return "Failed to update cron expression: " + e.getMessage();
        }
    }
}
