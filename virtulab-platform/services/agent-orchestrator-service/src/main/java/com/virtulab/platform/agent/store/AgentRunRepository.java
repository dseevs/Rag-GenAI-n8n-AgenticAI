package com.virtulab.platform.agent.store;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AgentRunRepository {

    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper;

    public AgentRunRepository(JdbcTemplate jdbc, ObjectMapper objectMapper) {
        this.jdbc = jdbc;
        this.objectMapper = objectMapper;
    }

    public void saveRun(UUID id, String attemptId, String agentType, String status, Object input, Object output)
            throws JsonProcessingException {
        jdbc.update(
                "INSERT INTO agents.agent_runs (id, attempt_id, agent_type, status, input_json, output_json) VALUES (?, ?, ?, ?, ?::jsonb, ?::jsonb)",
                id,
                attemptId,
                agentType,
                status,
                objectMapper.writeValueAsString(input),
                objectMapper.writeValueAsString(output));
    }

    public void saveStep(UUID id, UUID runId, int stepIndex, String toolName, String toolInput, String toolOutput, String message) {
        jdbc.update(
                "INSERT INTO agents.agent_steps (id, run_id, step_index, tool_name, tool_input, tool_output, llm_message) VALUES (?, ?, ?, ?, ?, ?, ?)",
                id,
                runId,
                stepIndex,
                toolName,
                toolInput,
                toolOutput,
                message);
    }
}
