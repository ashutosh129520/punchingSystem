package com.ttn.punchingSystem.service;

import com.ttn.punchingSystem.model.WorkScheduleDetails;

import java.util.List;

public interface DataManagementService {
    void saveWorkScheduleDetails(List<WorkScheduleDetails> workScheduleDetails);
    void updateCache(List<WorkScheduleDetails> workScheduleDetailsList);
}
