package com.virtulab.platform.tutor.rag;

import com.pgvector.PGvector;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class VectorSearchRepository {

    private final JdbcTemplate jdbc;

    public VectorSearchRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public int activeVersion() {
        Integer v = jdbc.queryForObject(
                "SELECT version FROM rag.corpus_version WHERE active = TRUE ORDER BY version DESC LIMIT 1",
                Integer.class);
        return v != null ? v : 0;
    }

    public List<KnowledgeChunk> search(float[] query, String experimentId, String lang, int limit, boolean excludeQuiz) {
        int version = activeVersion();
        String sql = """
                SELECT id, content, metadata->>'docType' AS doc_type, metadata->>'experimentId' AS experiment_id,
                       1 - (embedding <=> ?) AS score
                FROM rag.knowledge_chunks
                WHERE corpus_version = ?
                  AND metadata->>'experimentId' = ?
                  AND metadata->>'lang' = ?
                """ + (excludeQuiz ? " AND metadata->>'docType' <> 'quiz' " : "") + """
                ORDER BY embedding <=> ?
                LIMIT ?
                """;
        List<KnowledgeChunk> chunks = new ArrayList<>();
        jdbc.query(
                sql,
                (ResultSet rs) -> {
                    try {
                        while (rs.next()) {
                            chunks.add(new KnowledgeChunk(
                                    UUID.fromString(rs.getString("id")),
                                    rs.getString("content"),
                                    rs.getString("doc_type"),
                                    rs.getString("experiment_id")));
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                },
                new PGvector(query),
                version,
                experimentId,
                lang,
                new PGvector(query),
                limit);
        return chunks;
    }

    public List<KnowledgeChunk> searchByDocType(String experimentId, String lang, String docType, int limit) {
        int version = activeVersion();
        return jdbc.query(
                """
                SELECT id, content, metadata->>'docType' AS doc_type, metadata->>'experimentId' AS experiment_id
                FROM rag.knowledge_chunks
                WHERE corpus_version = ? AND metadata->>'experimentId' = ? AND metadata->>'lang' = ?
                  AND metadata->>'docType' = ?
                LIMIT ?
                """,
                (rs, rowNum) -> new KnowledgeChunk(
                        UUID.fromString(rs.getString("id")),
                        rs.getString("content"),
                        rs.getString("doc_type"),
                        rs.getString("experiment_id")),
                version,
                experimentId,
                lang,
                docType,
                limit);
    }
}
