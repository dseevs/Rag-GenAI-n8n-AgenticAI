package com.virtulab.platform.ml.store;

import com.virtulab.platform.contracts.ml.MlModelVsActualReport;
import com.virtulab.platform.contracts.ml.MlModelVsActualRow;
import com.virtulab.platform.contracts.ml.MlPredictionDto;
import com.virtulab.platform.ml.model.AttemptFeatureExtractor;
import com.virtulab.platform.ml.model.CompletionScorer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MlPredictionRepository {

    private final JdbcTemplate jdbc;
    private final double scoreThreshold;

    public MlPredictionRepository(
            JdbcTemplate jdbc,
            @Value("${virtulab.ml.score-threshold:0.5}") double scoreThreshold
    ) {
        this.jdbc = jdbc;
        this.scoreThreshold = scoreThreshold;
    }

    public void insert(CompletionScorer.ScoredAttempt scored) {
        var f = scored.features();
        jdbc.update(
                """
                INSERT INTO ml.ml_predictions
                (attempt_id, user_id, experiment_id, org_id, model_version, score, features_json)
                VALUES (?, ?, ?, ?, ?, ?, ?::jsonb)
                """,
                f.attemptId(),
                f.userId(),
                f.experimentId(),
                f.orgId(),
                scored.modelVersion(),
                scored.score(),
                f.features().toString());
    }

    public List<String> distinctAttemptIds(String orgId) {
        if (orgId != null && !orgId.isBlank()) {
            return jdbc.queryForList(
                    """
                    SELECT DISTINCT attempt_id
                    FROM analytics.fact_progress
                    WHERE org_id = ?
                    ORDER BY attempt_id
                    """,
                    String.class,
                    orgId);
        }
        return jdbc.queryForList(
                "SELECT DISTINCT attempt_id FROM analytics.fact_progress ORDER BY attempt_id",
                String.class);
    }

    public List<MlPredictionDto> findByAttempt(String attemptId, int limit) {
        return jdbc.query(
                """
                SELECT id, attempt_id, user_id, experiment_id, model_version, score,
                       features_json::text, predicted_at
                FROM ml.ml_predictions
                WHERE attempt_id = ?
                ORDER BY predicted_at DESC
                LIMIT ?
                """,
                (rs, rowNum) -> mapPrediction(rs),
                attemptId,
                limit);
    }

    public MlModelVsActualReport modelVsActual(String orgId, String modelVersion) {
        List<MlModelVsActualRow> rows = jdbc.query(
                """
                WITH latest_pred AS (
                    SELECT DISTINCT ON (attempt_id)
                        attempt_id, user_id, score, model_version
                    FROM ml.ml_predictions
                    WHERE org_id = ? AND model_version = ?
                    ORDER BY attempt_id, predicted_at DESC
                ),
                actual AS (
                    SELECT attempt_id, COALESCE(MAX(progress_pct), 0) AS max_pct
                    FROM analytics.fact_progress
                    WHERE org_id = ?
                    GROUP BY attempt_id
                )
                SELECT p.attempt_id, p.user_id, p.score, a.max_pct
                FROM latest_pred p
                JOIN actual a ON a.attempt_id = p.attempt_id
                ORDER BY p.score DESC
                LIMIT 100
                """,
                (rs, rowNum) -> {
                    double score = rs.getDouble("score");
                    int maxPct = rs.getInt("max_pct");
                    boolean predicted = score >= scoreThreshold;
                    boolean actual = maxPct >= 100;
                    return new MlModelVsActualRow(
                            rs.getString("attempt_id"),
                            rs.getString("user_id"),
                            score,
                            maxPct,
                            predicted,
                            actual);
                },
                orgId,
                modelVersion,
                orgId);

        int predictedComplete = 0;
        int actuallyComplete = 0;
        int correct = 0;
        for (MlModelVsActualRow row : rows) {
            if (row.predictedComplete()) {
                predictedComplete++;
            }
            if (row.actuallyComplete()) {
                actuallyComplete++;
            }
            if (row.predictedComplete() == row.actuallyComplete()) {
                correct++;
            }
        }
        double accuracy = rows.isEmpty() ? 0.0 : (double) correct / rows.size();

        return new MlModelVsActualReport(
                orgId,
                modelVersion,
                rows.size(),
                predictedComplete,
                actuallyComplete,
                accuracy,
                rows);
    }

    private MlPredictionDto mapPrediction(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("predicted_at");
        return new MlPredictionDto(
                rs.getObject("id", UUID.class).toString(),
                rs.getString("attempt_id"),
                rs.getString("user_id"),
                rs.getString("experiment_id"),
                rs.getString("model_version"),
                rs.getDouble("score"),
                rs.getString("features_json"),
                ts != null ? ts.toInstant() : Instant.now());
    }
}
