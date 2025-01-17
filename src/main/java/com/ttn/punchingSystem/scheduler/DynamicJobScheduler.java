package com.ttn.punchingSystem.scheduler;

import com.ttn.punchingSystem.config.SpringJobFactory;
import com.ttn.punchingSystem.model.JobStatus;
import com.ttn.punchingSystem.utils.AppConstant;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DynamicJobScheduler {

    private final Scheduler scheduler;
    private final SpringJobFactory jobFactory;
    private final Map<String, JobKey> jobKeys = new HashMap<>();

    @Value("${JOB_CRON_EXPRESSION}")
    private String defaultCronExpression;

    @Autowired
    public DynamicJobScheduler(SpringJobFactory jobFactory) throws SchedulerException {
        this.scheduler = StdSchedulerFactory.getDefaultScheduler();
        this.jobFactory = jobFactory;
        this.scheduler.setJobFactory(jobFactory);
    }

    @PostConstruct
    public void startScheduler() throws SchedulerException {
        System.out.println("JOB_CRON_EXPRESSION from env: " + System.getenv("JOB_CRON_EXPRESSION"));
        scheduler.start();
        scheduleJob(JobScheduler.class, "csvReadingJob", "csvReadingGroup", defaultCronExpression);
    }

    public void scheduleJob(Class<? extends Job> jobClass, String jobName, String jobGroup, String cronExpr) throws SchedulerException {
        if (cronExpr == null || cronExpr.isEmpty()) {
            cronExpr = defaultCronExpression;
        }
        JobKey jobKey = new JobKey(jobName, jobGroup);
        JobDetail jobDetail = JobBuilder.newJob(jobClass).withIdentity(jobKey).build();
        Trigger trigger = TriggerBuilder.newTrigger().withIdentity(jobName, jobGroup).withSchedule(CronScheduleBuilder.cronSchedule(cronExpr).withMisfireHandlingInstructionFireAndProceed()).build();
        scheduler.scheduleJob(jobDetail, trigger);
        jobKeys.put(jobName, jobKey);
        System.out.println("Scheduled Job: " + jobName + " with cron: " + cronExpr);
    }

    public void rescheduleJob(String jobName, String jobGroup, String newCronExpr) throws SchedulerException {
        JobKey jobKey = jobKeys.get(jobName);
        if (jobKey == null) {
            System.out.println("Job " + jobName + " not found.");
            return;
        }
        TriggerKey triggerKey = new TriggerKey(jobName, jobGroup);
        Trigger newTrigger = TriggerBuilder.newTrigger().withIdentity(triggerKey).withSchedule(CronScheduleBuilder.cronSchedule(newCronExpr).withMisfireHandlingInstructionFireAndProceed()).build();
        scheduler.rescheduleJob(triggerKey, newTrigger);
        System.out.println("Rescheduled Job: " + jobName + " with new cron: " + newCronExpr);
    }

    public JobStatus getJobStatus(String jobName) throws SchedulerException {
        JobKey jobKey = jobKeys.get(jobName);
        String status = "";
        if (jobKey == null) {
            return new JobStatus("Not Found", null, null, "Job does not exist");
        }
        List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
        if (triggers.isEmpty()) {
            return new JobStatus("No Triggers", null, null, "No associated triggers");
        }
        Trigger trigger = triggers.get(0);
        Trigger.TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
        if (triggerState == Trigger.TriggerState.NORMAL) {
            status = AppConstant.JOB_RUNNING;
        } else if (triggerState == Trigger.TriggerState.PAUSED) {
            status = AppConstant.JOB_PAUSED;
        } else {
            status = AppConstant.JOB_UNKNOWN;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String lastRunTime = (trigger.getPreviousFireTime() != null) ? sdf.format(trigger.getPreviousFireTime()) : "Never";
        String nextRunTime = (trigger.getNextFireTime() != null) ? sdf.format(trigger.getNextFireTime()) : "Not Scheduled";

        return new JobStatus(status, lastRunTime, nextRunTime, "Job retrieved successfully");
    }
}
