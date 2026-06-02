package com.virtulab.platform.ml.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class AttemptFeatureExtractor {

    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper;

    public AttemptFeatureExtractor(JdbcTemplate jdbc, ObjectMapper objectMapper) {
        this.jdbc = jdbc;
        this.objectMapper = objectMapper;
    }

    public AttemptFeatures extract(String attemptId) {
        Integer maxProgress = jdbc.queryForObject(
                """
                SELECT COALESCE(MAX(progress_pct), 0)
                FROM analytics.fact_progress
                WHERE attempt_id = ?
                """,
                Integer.class,
                attemptId);

        Integer progressEvents = jdbc.queryForObject(
                """
                SELECT COUNT(*)::int
                FROM analytics.fact_progress
                WHERE attempt_id = ?
                """,
                Integer.class,
                attemptId);

        Integer aiEvents = jdbc.queryForObject(
                """
                SELECT COUNT(*)::int
                FROM analytics.fact_ai
                WHERE attempt_id = ?
                """,
                Integer.class,
                attemptId);

        Map<String, Object> meta = jdbc.query(
                """
                SELECT user_id, experiment_id, org_id
                FROM analytics.fact_progress
                WHERE attempt_id = ?
                ORDER BY event_ts DESC
                LIMIT 1
                """,
                rs -> {
                    if (!rs.next()) {
                        return Map.of("userId", (Object) null, "experimentId", null, "orgId", "org-dev");
                    }
                    return Map.of(
                            "userId", rs.getString("user_id"),
                            "experimentId", rs.getString("experiment_id"),
                            "orgId", rs.getString("org_id"));
                },
                attemptId);

        ObjectNode features = objectMapper.createObjectNode();
        features.put("maxProgressPct", maxProgress != null ? maxProgress : 0);
        features.put("progressEventCount", progressEvents != null ? progressEvents : 0);
        features.put("aiEventCount", aiEvents != null ? aiEvents : 0);

        return new AttemptFeatures(
                attemptId,
                (String) meta.get("userId"),
                (String) meta.get("experimentId"),
                meta.get("orgId") != null ? (String) meta.get("orgId") : "org-dev",
                features);
    }

    public record AttemptFeatures(
            String attemptId,
            String userId,
            String experimentId,
            String orgId,
            ObjectNode features
    ) {}
}
