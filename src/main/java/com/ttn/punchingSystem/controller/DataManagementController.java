package com.ttn.punchingSystem.controller;

import com.ttn.punchingSystem.model.WorkScheduleDetails;
import com.ttn.punchingSystem.service.DataManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class DataManagementController {

    @Autowired
    private DataManagementService dataManagementService;

    @PostMapping("/save-workSchedule")
    public void saveWorkScheduleDetails(@RequestBody List<WorkScheduleDetails> workScheduleDetails) {
        dataManagementService.saveWorkScheduleDetails(workScheduleDetails);
    }
}
