package com.ttn.punchingSystem.service;

import com.ttn.punchingSystem.model.PunchingDetails;
import com.ttn.punchingSystem.model.WorkScheduleDetails;
import com.ttn.punchingSystem.model.WorkScheduleResult;
import com.ttn.punchingSystem.repository.WorkScheduleRepository;
import com.ttn.punchingSystem.utils.AppConstant;
import com.ttn.punchingSystem.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

@Service
public class CacheService {

    @Autowired
    private WorkScheduleRepository workScheduleDetailsRepository;
    @Autowired
    private DataManagementService dataManagementService;
    @Autowired
    private CsvReaderService csvReaderService;
    @Autowired
    public RedisTemplate<String, Object> redisTemplate;

    public static String generateCacheKey(String prefix, List<String> userEmails) {
        return prefix + String.join(",", userEmails);
    }

    public void clearCacheBasedOnKey(String key) {
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            redisTemplate.delete(key);
        } else {
            throw new IllegalArgumentException("No cache found for key: " + key);
        }
    }

    public void refreshSpecificCache(String keyPrefix) {
        Set<String> matchingKeys = redisTemplate.keys(keyPrefix + "*");
        if (matchingKeys == null || matchingKeys.isEmpty()) {
            throw new IllegalArgumentException("No cache keys found for prefix: " + keyPrefix);
        }
        for (String key : matchingKeys) {
            if (key.startsWith(AppConstant.CACHE_KEY_PREFIX)) {
                redisTemplate.delete(key);
                dataManagementService.updateCache();
            } else if (key.startsWith(AppConstant.DEFAULTERS_CACHE)) {
                redisTemplate.delete(key);
                csvReaderService.processListOfDefaulters();
            } else {
                throw new IllegalArgumentException("Key does not match any known cache prefix: " + key);
            }
        }
    }

    public List<WorkScheduleDetails> getDefaultersFromCache(String key) {
        List<WorkScheduleDetails> cachedWorkSchedules = (List<WorkScheduleDetails>) redisTemplate.opsForValue().get(key + ":");
        if(Objects.nonNull(cachedWorkSchedules)) {
            if (!cachedWorkSchedules.isEmpty()) {
                return cachedWorkSchedules;
            } else {
                throw new IllegalArgumentException("Key does not match any known cache prefix: " + key);
            }
        }
        return null;
    }

    public WorkScheduleResult getCachedWorkSchedulesDefaulterList(List<String> userEmails) {
        try {
            String todaysDate = DateUtil.getFormattedTodaysDate();
            List<WorkScheduleDetails> cachedWorkSchedules = (List<WorkScheduleDetails>) redisTemplate.opsForValue().get(todaysDate + ":");
            if (cachedWorkSchedules != null && !cachedWorkSchedules.isEmpty()) {
                return new WorkScheduleResult(cachedWorkSchedules, true);
            } else {
                List<WorkScheduleDetails> workSchedulesDefaultersList = workScheduleDetailsRepository.findAllByUserEmailIn(userEmails);
                redisTemplate.opsForValue().set(todaysDate + ":", workSchedulesDefaultersList, Duration.ofDays(7));
                return new WorkScheduleResult(workSchedulesDefaultersList, false);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}

