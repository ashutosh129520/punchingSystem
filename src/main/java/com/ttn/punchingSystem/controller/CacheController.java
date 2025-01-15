package com.ttn.punchingSystem.controller;

import com.ttn.punchingSystem.model.PunchingDetails;
import com.ttn.punchingSystem.service.CacheService;
import com.ttn.punchingSystem.utils.AppConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/cache")
public class CacheController {

    @Autowired
    private CacheService cacheService;
    @Autowired
    RedisTemplate redisTemplate;

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
    public ResponseEntity<String> fetchDefaulterList(@RequestParam String matchingKey) {
        try {
            Map<String, List<PunchingDetails>> defaultersFromCache = new HashMap<>();
                List<PunchingDetails> defaulterDetails = cacheService.getDefaultersFromCache(matchingKey);
                if (defaulterDetails != null) {
                    defaultersFromCache.put(matchingKey, defaulterDetails);
                }
            if(Objects.nonNull(defaulterDetails)){
                return ResponseEntity.status(HttpStatus.OK)
                        .body("Defaulter list fetched successfully for provided keys: " + defaultersFromCache);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error getting defaulter list from caches: " + e.getMessage());
        }
        return null;
    }
}
