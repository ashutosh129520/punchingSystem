package com.ttn.punchingSystem.scheduler;

import com.ttn.punchingSystem.config.SpringJobFactory;
import com.ttn.punchingSystem.model.JobStatus;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.List;

@Component
public class DynamicJobScheduler {

    private final Scheduler scheduler;
    private final SpringJobFactory jobFactory;

    @Value("${JOB_CRON_EXPRESSION:0 0/9 * * * ?}")
    private String cronExpression;

    @Autowired
    public DynamicJobScheduler(SpringJobFactory jobFactory) throws SchedulerException {
        this.scheduler = StdSchedulerFactory.getDefaultScheduler();
        this.jobFactory = jobFactory;
        this.scheduler.setJobFactory(jobFactory);
    }

    @PostConstruct
    public void startScheduler() throws SchedulerException {
        scheduler.start();
        scheduleJob(cronExpression);
    }

    public void scheduleJob(String cronExpression) throws SchedulerException {
        JobDetail jobDetail = JobBuilder.newJob(JobScheduler.class).withIdentity("csvReadingJob", "group1").build();
        Trigger trigger = TriggerBuilder.newTrigger().withIdentity("csvReadingTrigger", "group1")
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression).withMisfireHandlingInstructionFireAndProceed()).build();
        scheduler.scheduleJob(jobDetail, trigger);
        System.out.println("Job scheduled with cron: " + cronExpression);
    }

    public void rescheduleJob(String newCronExpression) throws SchedulerException {
        scheduler.deleteJob(new JobKey("csvReadingJob", "group1"));
        scheduleJob(newCronExpression);
    }

    public JobStatus getJobStatus() throws SchedulerException {
        JobKey jobKey = new JobKey("csvReadingJob", "group1");
        JobDetail jobDetail = scheduler.getJobDetail(jobKey);
        if (jobDetail == null) {
            return new JobStatus("Not found", null, null, null);
        }
        List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
        if (triggers.isEmpty()) {
            return new JobStatus("No triggers", null, null, null);
        }
        Trigger trigger = triggers.get(0);
        String status = "Unknown";
        if (scheduler.getTriggerState(trigger.getKey()) == Trigger.TriggerState.NORMAL) {
            status = "Running";
        } else if (scheduler.getTriggerState(trigger.getKey()) == Trigger.TriggerState.PAUSED) {
            status = "Paused";
        }
        if (trigger instanceof CronTrigger) {
            cronExpression = ((CronTrigger) trigger).getCronExpression();
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return new JobStatus(status, sdf.format(trigger.getPreviousFireTime()), sdf.format(trigger.getNextFireTime()), cronExpression);
    }
}
