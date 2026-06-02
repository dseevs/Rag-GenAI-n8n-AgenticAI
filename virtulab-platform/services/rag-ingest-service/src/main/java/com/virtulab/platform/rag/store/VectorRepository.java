package com.virtulab.platform.rag.store;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pgvector.PGvector;
import com.virtulab.platform.contracts.ai.RagStatsResponse;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class VectorRepository {

    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper;

    public VectorRepository(JdbcTemplate jdbc, ObjectMapper objectMapper) {
        this.jdbc = jdbc;
        this.objectMapper = objectMapper;
    }

    public int nextCorpusVersion() {
        Integer max = jdbc.queryForObject("SELECT COALESCE(MAX(version), 0) FROM rag.corpus_version", Integer.class);
        return max + 1;
    }

    public void registerVersion(int version) {
        jdbc.update("INSERT INTO rag.corpus_version (version, active) VALUES (?, FALSE)", version);
    }

    public void activateVersion(int version) {
        jdbc.update("UPDATE rag.corpus_version SET active = FALSE WHERE active = TRUE");
        jdbc.update("UPDATE rag.corpus_version SET active = TRUE WHERE version = ?", version);
    }

    public int activeVersion() {
        Integer v = jdbc.queryForObject(
                "SELECT version FROM rag.corpus_version WHERE active = TRUE ORDER BY version DESC LIMIT 1",
                Integer.class);
        return v != null ? v : 0;
    }

    public void insertChunk(UUID id, String content, float[] embedding, Map<String, String> metadata, int corpusVersion)
            throws JsonProcessingException {
        String metaJson = objectMapper.writeValueAsString(metadata);
        jdbc.update(
                "INSERT INTO rag.knowledge_chunks (id, content, embedding, metadata, corpus_version) VALUES (?, ?, ?, ?::jsonb, ?)",
                id,
                content,
                new PGvector(embedding),
                metaJson,
                corpusVersion);
    }

    public RagStatsResponse stats() {
        int active = activeVersion();
        Long total = jdbc.queryForObject(
                "SELECT COUNT(*) FROM rag.knowledge_chunks WHERE corpus_version = ?",
                Long.class,
                active);
        Map<String, Long> byExperiment = new HashMap<>();
        jdbc.query(
                "SELECT metadata->>'experimentId' AS exp, COUNT(*) AS cnt FROM rag.knowledge_chunks WHERE corpus_version = ? GROUP BY exp",
                (ResultSet rs) -> {
                    try {
                        while (rs.next()) {
                            byExperiment.put(rs.getString("exp"), rs.getLong("cnt"));
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                },
                active);
        return new RagStatsResponse(active, total != null ? total : 0L, byExperiment);
    }
}
