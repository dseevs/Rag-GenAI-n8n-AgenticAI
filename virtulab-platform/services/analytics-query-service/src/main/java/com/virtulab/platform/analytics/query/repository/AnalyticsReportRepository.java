package com.virtulab.platform.analytics.query.repository;

import com.virtulab.platform.contracts.analytics.AiUsageRow;
import com.virtulab.platform.contracts.analytics.AiVsProgressReport;
import com.virtulab.platform.contracts.analytics.ExperimentSummaryReport;
import com.virtulab.platform.contracts.analytics.OrgFunnelReport;
import com.virtulab.platform.contracts.analytics.OrgFunnelRow;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AnalyticsReportRepository {

    private final JdbcTemplate jdbc;

    public AnalyticsReportRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public OrgFunnelReport orgFunnel(String orgId) {
        List<OrgFunnelRow> rows = jdbc.query(
                """
                SELECT
                    o.org_name,
                    u.user_id,
                    COALESCE(s.experiment_id, fp.experiment_id) AS experiment_id,
                    COUNT(DISTINCT s.attempt_id) AS session_count,
                    COUNT(fp.id) AS event_count,
                    COALESCE(SUM(fp.time_spent_sec), 0) AS total_time
                FROM analytics.dim_org o
                JOIN analytics.dim_user u ON u.org_id = o.org_id
                LEFT JOIN session.lab_sessions s ON s.user_id = u.user_id AND s.org_id = o.org_id
                LEFT JOIN analytics.fact_progress fp ON fp.attempt_id = s.attempt_id
                WHERE o.org_id = ?
                GROUP BY o.org_name, u.user_id, COALESCE(s.experiment_id, fp.experiment_id)
                ORDER BY event_count DESC
                """,
                (rs, rowNum) -> new OrgFunnelRow(
                        rs.getString("org_name"),
                        rs.getString("user_id"),
                        rs.getString("experiment_id"),
                        rs.getLong("session_count"),
                        rs.getLong("event_count"),
                        rs.getLong("total_time")),
                orgId);

        String tenantId = jdbc.queryForObject(
                "SELECT tenant_id FROM analytics.dim_org WHERE org_id = ?",
                String.class,
                orgId);

        return new OrgFunnelReport(orgId, tenantId, rows, System.currentTimeMillis());
    }

    public ExperimentSummaryReport experimentSummary(String experimentId) {
        List<ExperimentSummaryReport> rows = jdbc.query(
                """
                SELECT
                    ? AS experiment_id,
                    COUNT(DISTINCT user_id) AS distinct_users,
                    COUNT(*) AS total_events,
                    COALESCE(SUM(time_spent_sec), 0) AS total_time,
                    COALESCE(AVG(progress_pct), 0) AS avg_pct
                FROM analytics.fact_progress
                WHERE experiment_id = ?
                """,
                (rs, rowNum) -> new ExperimentSummaryReport(
                        rs.getString("experiment_id"),
                        rs.getLong("distinct_users"),
                        rs.getLong("total_events"),
                        rs.getLong("total_time"),
                        rs.getDouble("avg_pct")),
                experimentId,
                experimentId);
        if (rows.isEmpty()) {
            return new ExperimentSummaryReport(experimentId, 0, 0, 0, 0);
        }
        return rows.get(0);
    }

    public AiVsProgressReport aiVsProgress(String orgId) {
        List<AiUsageRow> rows = jdbc.query(
                """
                WITH progress AS (
                    SELECT user_id, experiment_id, COUNT(*) AS progress_count
                    FROM analytics.fact_progress
                    WHERE org_id = ?
                    GROUP BY user_id, experiment_id
                ),
                ai AS (
                    SELECT user_id, experiment_id, COUNT(*) AS ai_count, AVG(latency_ms) AS avg_latency
                    FROM analytics.fact_ai
                    GROUP BY user_id, experiment_id
                )
                SELECT
                    COALESCE(p.user_id, a.user_id) AS user_id,
                    COALESCE(p.experiment_id, a.experiment_id) AS experiment_id,
                    COALESCE(a.ai_count, 0) AS ai_events,
                    COALESCE(p.progress_count, 0) AS progress_events,
                    COALESCE(a.avg_latency, 0) AS avg_latency
                FROM progress p
                FULL OUTER JOIN ai a
                  ON p.user_id = a.user_id AND p.experiment_id = a.experiment_id
                ORDER BY ai_events DESC, progress_events DESC
                """,
                (rs, rowNum) -> new AiUsageRow(
                        rs.getString("user_id"),
                        rs.getString("experiment_id"),
                        rs.getLong("ai_events"),
                        rs.getLong("progress_events"),
                        rs.getDouble("avg_latency")),
                orgId);

        return new AiVsProgressReport(orgId, rows);
    }
}
