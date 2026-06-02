package com.virtulab.platform.analytics.ingest.store;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AnalyticsFactRepository {

    private final JdbcTemplate jdbc;

    public AnalyticsFactRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<SessionContext> findSessionContext(String attemptId) {
        return jdbc.query(
                """
                SELECT tenant_id, org_id, user_id
                FROM session.lab_sessions
                WHERE attempt_id = ?
                LIMIT 1
                """,
                rs -> rs.next()
                        ? Optional.of(new SessionContext(
                                rs.getString("tenant_id"),
                                rs.getString("org_id"),
                                rs.getString("user_id")))
                        : Optional.empty(),
                attemptId);
    }

    public void upsertUser(String userId, String tenantId, String orgId) {
        jdbc.update(
                """
                INSERT INTO analytics.dim_user (user_id, tenant_id, org_id, display_name)
                VALUES (?, ?, ?, ?)
                ON CONFLICT (user_id) DO UPDATE SET tenant_id = EXCLUDED.tenant_id, org_id = EXCLUDED.org_id
                """,
                userId,
                tenantId,
                orgId,
                userId);
    }

    public boolean insertProgress(
            UUID eventId,
            String attemptId,
            String userId,
            String experimentId,
            String tenantId,
            String orgId,
            String stepId,
            Integer timeSpentSec,
            String progressTab,
            Integer progressPct,
            Instant eventTs
    ) {
        try {
            jdbc.update(
                    """
                    INSERT INTO analytics.fact_progress
                    (event_id, attempt_id, user_id, experiment_id, tenant_id, org_id, step_id,
                     time_spent_sec, progress_tab, progress_pct, event_ts)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """,
                    eventId,
                    attemptId,
                    userId,
                    experimentId,
                    tenantId,
                    orgId,
                    stepId,
                    timeSpentSec,
                    progressTab,
                    progressPct,
                    Timestamp.from(eventTs));
            return true;
        } catch (DuplicateKeyException e) {
            return false;
        }
    }

    public void insertAi(
            String userId,
            String experimentId,
            String attemptId,
            String eventType,
            String source,
            Long latencyMs,
            Instant eventTs
    ) {
        jdbc.update(
                """
                INSERT INTO analytics.fact_ai
                (user_id, experiment_id, attempt_id, event_type, source, latency_ms, event_ts)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """,
                userId,
                experimentId,
                attemptId,
                eventType,
                source,
                latencyMs,
                Timestamp.from(eventTs));
    }

    public void insertQuiz(
            String attemptId,
            String userId,
            String experimentId,
            String mode,
            int score,
            int totalQuestions,
            int correctCount,
            Instant eventTs
    ) {
        jdbc.update(
                """
                INSERT INTO analytics.fact_quiz
                (attempt_id, user_id, experiment_id, mode, score, total_questions, correct_count, event_ts)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                attemptId,
                userId,
                experimentId,
                mode,
                score,
                totalQuestions,
                correctCount,
                Timestamp.from(eventTs));
    }

    public record SessionContext(String tenantId, String orgId, String userId) {}
}
