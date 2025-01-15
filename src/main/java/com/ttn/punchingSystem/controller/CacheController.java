package com.ttn.punchingSystem.controller;

import com.ttn.punchingSystem.service.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cache")
public class CacheController {

    @Autowired
    private CacheService cacheService;

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
    public ResponseEntity<String> fetchDefaulterList() {
        try {
            cacheService.getDefaultersFromCache();
            return ResponseEntity.status(HttpStatus.OK).body("All caches defaulter list fetched successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error getting caches: " + e.getMessage());
        }
    }
}
