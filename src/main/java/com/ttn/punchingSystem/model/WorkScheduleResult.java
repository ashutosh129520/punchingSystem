package com.ttn.punchingSystem.model;

import java.util.List;

public class WorkScheduleResult {
    private List<WorkScheduleDetails> workSchedules;
    private boolean fromCache;

    public WorkScheduleResult(List<WorkScheduleDetails> workSchedules, boolean fromCache) {
        this.workSchedules = workSchedules;
        this.fromCache = fromCache;
    }

    public List<WorkScheduleDetails> getWorkSchedules() {
        return workSchedules;
    }

    public boolean isFromCache() {
        return fromCache;
    }
}

