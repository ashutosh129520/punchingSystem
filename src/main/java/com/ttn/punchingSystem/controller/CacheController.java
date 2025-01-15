package com.ttn.punchingSystem.controller;

import com.ttn.punchingSystem.model.PunchingDetails;
import com.ttn.punchingSystem.model.WorkScheduleDetails;
import com.ttn.punchingSystem.repository.PunchLogRepository;
import com.ttn.punchingSystem.service.CacheService;
import com.ttn.punchingSystem.utils.AppConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/cache")
public class CacheController {

    @Autowired
    private CacheService cacheService;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    PunchLogRepository punchLogRepository;

    @DeleteMapping("/clearCache")
    public ResponseEntity<String> clearCache(@RequestParam String key) {
        try {
            cacheService.clearCacheBasedOnKey(key);
            return ResponseEntity.status(HttpStatus.OK).body("Cache cleared ");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error clearing cache: " + e.getMessage());
        }
    }

    @PostMapping("/refreshCache")
    public ResponseEntity<String> refreshCache(@RequestParam String key) {
        try {
            cacheService.refreshSpecificCache(key);
            return ResponseEntity.status(HttpStatus.OK).body("Cache refreshed ");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error refreshing cache: " + e.getMessage());
        }
    }

    @PostMapping("/defaulter-list")
    public ResponseEntity<String> fetchDefaulterList(@RequestParam int days) {
        try {
            LocalDate today = LocalDate.now();
            Map<String, List<PunchingDetails>> defaultersFromCache = new HashMap<>();
            for (int i = 0; i < days; i++) {
                String dateKey = today.minusDays(i).format(DateTimeFormatter.ofPattern(AppConstant.DATE_FORMAT_WITHOUT_TIME));
                List<WorkScheduleDetails> defaulterDetails = cacheService.getDefaultersFromCache(dateKey);
                if(Objects.nonNull(defaulterDetails)) {
                    Map<String, List<PunchingDetails>> userEmailToPunchingDetails = punchingDetailsMapOfDefaulters(defaulterDetails);
                    defaultersFromCache.putAll(userEmailToPunchingDetails);
                }
            }
            if (!defaultersFromCache.isEmpty()) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body("Defaulter list fetched successfully for the last " + days + " days: " + defaultersFromCache);
            } else {
                return ResponseEntity.status(HttpStatus.NO_CONTENT)
                        .body("No defaulter data found for the last " + days + " days.");
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error getting defaulter list from caches: " + e.getMessage());
        }
    }

    public Map<String, List<PunchingDetails>> punchingDetailsMapOfDefaulters(List<WorkScheduleDetails> defaulterDetails) {
        Map<String, List<PunchingDetails>> mapOfDefaulters = new HashMap<>();
        for (WorkScheduleDetails workSchedule : defaulterDetails) {
            String userEmail = workSchedule.getUserEmail();
            List<PunchingDetails> defaultersPunchingDetails = getPunchingDetailsForUser(userEmail);
            if (!mapOfDefaulters.containsKey(userEmail)) {
                mapOfDefaulters.put(userEmail, new ArrayList<>());
            }
            mapOfDefaulters.get(userEmail).addAll(defaultersPunchingDetails);
        }
        return mapOfDefaulters;
    }

    public List<PunchingDetails> getPunchingDetailsForUser(String userEmail) {
        return punchLogRepository.findByUserEmail(userEmail);
    }
}
