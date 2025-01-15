package com.ttn.punchingSystem.utils;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class CacheMetrics {

    private final MeterRegistry meterRegistry;

    public CacheMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    // Method to increment cache hit for a given cache name or key
    public void incrementCacheHit(String cacheName) {
        Counter cacheHitCounter = meterRegistry.counter("cache_hits", "cache", cacheName);
        cacheHitCounter.increment();
    }

    // Method to increment cache miss for a given cache name or key
    public void incrementCacheMiss(String cacheName) {
        Counter cacheMissCounter = meterRegistry.counter("cache_misses", "cache", cacheName);
        cacheMissCounter.increment();
    }
}
