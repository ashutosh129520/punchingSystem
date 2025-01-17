package com.ttn.punchingSystem.controller;

import com.ttn.punchingSystem.scheduler.DynamicJobScheduler;
import com.ttn.punchingSystem.scheduler.JobScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/scheduler")
public class SchedulerController {

    @Autowired
    private DynamicJobScheduler jobScheduler;

    @PostMapping("/schedule")
    public String scheduleJob(@RequestParam String jobName, @RequestParam String jobGroup, @RequestParam String cronExpr) {
        try {
            jobScheduler.scheduleJob(JobScheduler.class, jobName, jobGroup, cronExpr);
            return "Scheduled job: " + jobName + " with cron: " + cronExpr;
        } catch (Exception e) {
            return "Failed to schedule job: " + e.getMessage();
        }
    }

    @PostMapping("/updateCron")
    public ResponseEntity<String> updateCron(@RequestParam String jobName, @RequestParam String jobGroup, @RequestParam String cron) {
        try {
            jobScheduler.rescheduleJob(jobName, jobGroup, cron);
            return ResponseEntity.ok().body("Cron expression updated to: " + cron);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to update cron expression: " + e.getMessage());
        }
    }
}
