package com.ttn.punchingSystem.service.impl;

import com.ttn.punchingSystem.model.Project;
import com.ttn.punchingSystem.model.WorkScheduleDetails;
import com.ttn.punchingSystem.repository.ProjectRepository;
import com.ttn.punchingSystem.repository.WorkScheduleRepository;
import com.ttn.punchingSystem.service.DataManagementService;
import com.ttn.punchingSystem.utils.AppConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class DataManagementServiceImpl implements DataManagementService {
    @Autowired
    private WorkScheduleRepository workScheduleRepository;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    public RedisTemplate<String, Object> redisTemplate;

    @Transactional
    public void saveWorkScheduleDetails(List<WorkScheduleDetails> workScheduleDetailsList) {
        if (Objects.isNull(workScheduleDetailsList) || workScheduleDetailsList.isEmpty()) {
            throw new IllegalArgumentException("WorkScheduleDetails list cannot be null or empty");
        }
        for (WorkScheduleDetails workScheduleDetails : workScheduleDetailsList) {
            if (workScheduleDetails.getUserEmail().isEmpty()
                    || Objects.isNull(workScheduleDetails.getProject())) {
                throw new IllegalArgumentException("Work schedule mandatory details cannot be null or empty");
            }
            Project project = workScheduleDetails.getProject();
            Optional<Project> existingProject = projectRepository.findById(project.getProjectId());
            existingProject.ifPresent(workScheduleDetails::setProject);
        }
        workScheduleRepository.saveAll(workScheduleDetailsList);
        updateCache(workScheduleDetailsList);
    }

    @Override
    public void updateCache(List<WorkScheduleDetails> workScheduleDetailsList) {
        Map<String, Object> cachedData = new HashMap<>();
        Map<String, List<WorkScheduleDetails>> userEmailToDetailsMap = new HashMap<>();
        userEmailToDetailsMap = workScheduleDetailsList.stream().collect(Collectors.groupingBy(WorkScheduleDetails::getUserEmail));
        for(Map.Entry<String, List<WorkScheduleDetails>> entry : userEmailToDetailsMap.entrySet()){
            cachedData.put(entry.getKey(), entry.getValue());
        }
        redisTemplate.opsForValue().multiSet(cachedData);
        cachedData.keySet().forEach(key -> redisTemplate.expire(key, 10, TimeUnit.MINUTES));
    }
}
