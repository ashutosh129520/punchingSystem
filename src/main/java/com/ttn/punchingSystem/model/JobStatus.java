package com.ttn.punchingSystem.model;

public class JobStatus {
    private String status;
    private String lastRunTime;
    private String nextRunTime;
    private String cronExpression;

    public JobStatus(String status, String lastRunTime, String nextRunTime, String cronExpression) {
        this.status = status;
        this.lastRunTime = lastRunTime;
        this.nextRunTime = nextRunTime;
        this.cronExpression = cronExpression;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getLastRunTime() {
        return lastRunTime;
    }
    public void setLastRunTime(String lastRunTime) {
        this.lastRunTime = lastRunTime;
    }
    public String getNextRunTime() {
        return nextRunTime;
    }

    public void setNextRunTime(String nextRunTime) {
        this.nextRunTime = nextRunTime;
    }

    public String getCronExpression() {
        return cronExpression;
    }
    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }
}
