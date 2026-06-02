package com.virtulab.platform.quiz.store;

import com.virtulab.platform.contracts.quiz.ExamIntegrityReport;
import com.virtulab.platform.contracts.quiz.ExamIntegrityRow;
import com.virtulab.platform.contracts.quiz.QuizAttemptDto;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class QuizRepository {

    private final JdbcTemplate jdbc;

    public QuizRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<SessionContext> findSession(String attemptId) {
        return jdbc.query(
                """
                SELECT user_id, experiment_id, org_id, mode
                FROM session.lab_sessions
                WHERE attempt_id = ?
                """,
                rs -> rs.next()
                        ? Optional.of(new SessionContext(
                                rs.getString("user_id"),
                                rs.getString("experiment_id"),
                                rs.getString("org_id"),
                                rs.getString("mode")))
                        : Optional.empty(),
                attemptId);
    }

    public List<QuestionRow> questionsForExperiment(String experimentId) {
        return jdbc.query(
                """
                SELECT id, prompt, correct_answer
                FROM quiz.quiz_questions
                WHERE experiment_id = ?
                ORDER BY sort_order
                """,
                (rs, rowNum) -> new QuestionRow(
                        rs.getString("id"),
                        rs.getString("prompt"),
                        rs.getString("correct_answer")),
                experimentId);
    }

    public boolean existsForAttempt(String attemptId) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM quiz.quiz_attempts WHERE attempt_id = ?",
                Integer.class,
                attemptId);
        return count != null && count > 0;
    }

    public void insertAttempt(
            String attemptId,
            String userId,
            String experimentId,
            String orgId,
            String mode,
            int score,
            int totalQuestions,
            int correctCount,
            String answersJson
    ) {
        jdbc.update(
                """
                INSERT INTO quiz.quiz_attempts
                (attempt_id, user_id, experiment_id, org_id, mode, score, total_questions, correct_count, answers_json)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb)
                """,
                attemptId,
                userId,
                experimentId,
                orgId,
                mode,
                score,
                totalQuestions,
                correctCount,
                answersJson);
    }

    public Optional<QuizAttemptDto> findAttempt(String attemptId) {
        List<QuizAttemptDto> rows = jdbc.query(
                """
                SELECT id, attempt_id, user_id, experiment_id, mode, score, total_questions, correct_count, submitted_at
                FROM quiz.quiz_attempts WHERE attempt_id = ?
                """,
                (rs, rowNum) -> mapAttempt(rs),
                attemptId);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    public ExamIntegrityReport integrityReport(String orgId) {
        List<ExamIntegrityRow> rows = jdbc.query(
                """
                SELECT attempt_id, user_id, experiment_id, mode, score, submitted_at
                FROM quiz.quiz_attempts
                WHERE org_id = ?
                ORDER BY submitted_at DESC
                LIMIT 100
                """,
                (rs, rowNum) -> {
                    Timestamp ts = rs.getTimestamp("submitted_at");
                    return new ExamIntegrityRow(
                            rs.getString("attempt_id"),
                            rs.getString("user_id"),
                            rs.getString("experiment_id"),
                            rs.getString("mode"),
                            rs.getInt("score"),
                            ts != null ? ts.toInstant() : Instant.now());
                },
                orgId);

        int exam = (int) rows.stream().filter(r -> "EXAM".equals(r.mode())).count();
        int practice = rows.size() - exam;

        return new ExamIntegrityReport(orgId, rows.size(), exam, practice, 0, rows);
    }

    private QuizAttemptDto mapAttempt(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("submitted_at");
        return new QuizAttemptDto(
                rs.getObject("id", UUID.class).toString(),
                rs.getString("attempt_id"),
                rs.getString("user_id"),
                rs.getString("experiment_id"),
                rs.getString("mode"),
                rs.getInt("score"),
                rs.getInt("total_questions"),
                rs.getInt("correct_count"),
                ts != null ? ts.toInstant() : Instant.now());
    }

    public record SessionContext(String userId, String experimentId, String orgId, String mode) {}

    public record QuestionRow(String id, String prompt, String correctAnswer) {}
}
