package com.ttn.punchingSystem.config;

import org.quartz.Job;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class SpringJobFactory implements JobFactory {

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public Job newJob(TriggerFiredBundle bundle, Scheduler scheduler) throws SchedulerException {
        Class<? extends Job> jobClass = bundle.getJobDetail().getJobClass();
        try {
            return applicationContext.getAutowireCapableBeanFactory().createBean(jobClass);
        } catch (Exception e) {
            throw new SchedulerException("Error creating job instance", e);
        }
    }
}

