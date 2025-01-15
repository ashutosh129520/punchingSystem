package com.ttn.punchingSystem.service;

import com.ttn.punchingSystem.model.PunchingDetails;
import com.ttn.punchingSystem.model.WorkScheduleDetails;
import com.ttn.punchingSystem.repository.WorkScheduleRepository;
import com.ttn.punchingSystem.utils.AppConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;
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
                List<WorkScheduleDetails> workScheduleDetailsList = workScheduleDetailsRepository.findAll();
                dataManagementService.updateCache(workScheduleDetailsList);
            } else if (key.startsWith(AppConstant.DEFAULTERS_CACHE)) {
                redisTemplate.delete(key);
                csvReaderService.processListOfDefaulters();
            } else {
                throw new IllegalArgumentException("Key does not match any known cache prefix: " + key);
            }
        }
    }

    public Map<String, List<PunchingDetails>> getDefaultersFromCache() {
        return (Map<String, List<PunchingDetails>>) redisTemplate.opsForValue().get(AppConstant.DEFAULTERS_CACHE);
    }

    public List<WorkScheduleDetails> getCachedWorkSchedulesDefaulterList(List<String> userEmails) {
        List<WorkScheduleDetails> cachedWorkSchedules = null;
        try {
            String cacheKey = generateCacheKey(AppConstant.DEFAULTERS_CACHE, userEmails);
            cachedWorkSchedules = (List<WorkScheduleDetails>) redisTemplate.opsForValue().get(cacheKey);
            if (Objects.isNull(cachedWorkSchedules) || cachedWorkSchedules.isEmpty()) {
                List<WorkScheduleDetails> workSchedules = workScheduleDetailsRepository.findAllByUserEmailIn(userEmails);
                redisTemplate.opsForValue().set(cacheKey, workSchedules, Duration.ofHours(24));
                return workSchedules;
            }
            return cachedWorkSchedules;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

