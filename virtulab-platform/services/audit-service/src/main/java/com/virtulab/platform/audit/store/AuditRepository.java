package com.virtulab.platform.audit.store;

import com.virtulab.platform.contracts.audit.AuditEventDto;
import com.virtulab.platform.contracts.audit.AuditEventPage;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AuditRepository {

    private final JdbcTemplate jdbc;

    public AuditRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void insert(
            String eventType,
            String sourceTopic,
            String userId,
            String attemptId,
            String experimentId,
            String payloadJson
    ) {
        jdbc.update(
                """
                INSERT INTO audit.audit_events
                (event_type, source_topic, user_id, attempt_id, experiment_id, payload_json)
                VALUES (?, ?, ?, ?, ?, ?::jsonb)
                """,
                eventType,
                sourceTopic,
                userId,
                attemptId,
                experimentId,
                payloadJson);
    }

    public AuditEventPage findRecent(String userId, int limit) {
        String sql = """
                SELECT id, event_type, source_topic, user_id, attempt_id, experiment_id,
                       payload_json::text, recorded_at
                FROM audit.audit_events
                """;
        List<Object> args = new ArrayList<>();
        if (userId != null && !userId.isBlank()) {
            sql += " WHERE user_id = ?";
            args.add(userId);
        }
        sql += " ORDER BY recorded_at DESC LIMIT ?";
        args.add(limit);

        Long total = jdbc.queryForObject("SELECT COUNT(*) FROM audit.audit_events", Long.class);

        List<AuditEventDto> events = jdbc.query(
                sql,
                (rs, rowNum) -> map(rs),
                args.toArray());

        return new AuditEventPage(total != null ? total : 0, events);
    }

    private AuditEventDto map(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("recorded_at");
        return new AuditEventDto(
                rs.getObject("id", UUID.class).toString(),
                rs.getString("event_type"),
                rs.getString("source_topic"),
                rs.getString("user_id"),
                rs.getString("attempt_id"),
                rs.getString("experiment_id"),
                rs.getString("payload_json"),
                ts != null ? ts.toInstant() : Instant.now());
    }
}
