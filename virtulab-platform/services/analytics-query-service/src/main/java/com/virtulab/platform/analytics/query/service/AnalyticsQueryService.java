package com.virtulab.platform.analytics.query.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.virtulab.platform.analytics.query.repository.AnalyticsReportRepository;
import com.virtulab.platform.contracts.analytics.AiVsProgressReport;
import com.virtulab.platform.contracts.analytics.ExperimentSummaryReport;
import com.virtulab.platform.contracts.analytics.OrgFunnelReport;
import java.time.Duration;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsQueryService {

    private final AnalyticsReportRepository repository;
    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;
    private final Duration cacheTtl;

    public AnalyticsQueryService(
            AnalyticsReportRepository repository,
            StringRedisTemplate redis,
            ObjectMapper objectMapper,
            @Value("${virtulab.analytics.cache-ttl-seconds:300}") long cacheTtlSeconds
    ) {
        this.repository = repository;
        this.redis = redis;
        this.objectMapper = objectMapper;
        this.cacheTtl = Duration.ofSeconds(cacheTtlSeconds);
    }

    public OrgFunnelReport orgFunnel(String orgId) {
        return cached("org-funnel:" + orgId, OrgFunnelReport.class, () -> repository.orgFunnel(orgId));
    }

    public ExperimentSummaryReport experimentSummary(String experimentId) {
        return cached(
                "experiment:" + experimentId,
                ExperimentSummaryReport.class,
                () -> repository.experimentSummary(experimentId));
    }

    public AiVsProgressReport aiVsProgress(String orgId) {
        return cached("ai-vs-progress:" + orgId, AiVsProgressReport.class, () -> repository.aiVsProgress(orgId));
    }

    private <T> T cached(String key, Class<T> type, java.util.function.Supplier<T> loader) {
        String cacheKey = "analytics:" + key;
        String json = redis.opsForValue().get(cacheKey);
        if (json != null) {
            try {
                return objectMapper.readValue(json, type);
            } catch (JsonProcessingException ignored) {
                // fall through
            }
        }
        T value = loader.get();
        try {
            redis.opsForValue().set(cacheKey, objectMapper.writeValueAsString(value), cacheTtl);
        } catch (JsonProcessingException ignored) {
            // skip cache write
        }
        return value;
    }
}
