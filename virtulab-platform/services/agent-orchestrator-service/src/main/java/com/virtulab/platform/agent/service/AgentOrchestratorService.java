package com.virtulab.platform.agent.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.virtulab.platform.contracts.ai.AgentRunRequest;
import com.virtulab.platform.contracts.ai.AgentRunResponse;
import com.virtulab.platform.contracts.ai.AgentStepDto;
import com.virtulab.platform.contracts.ai.FoodForThoughtRequest;
import com.virtulab.platform.contracts.ai.RagAskRequest;
import com.virtulab.platform.contracts.ai.RagCitation;
import com.virtulab.platform.agent.store.AgentRunRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class AgentOrchestratorService {

    private final WebClient aiTutorWebClient;
    private final AgentRunRepository agentRunRepository;

    public AgentOrchestratorService(WebClient aiTutorWebClient, AgentRunRepository agentRunRepository) {
        this.aiTutorWebClient = aiTutorWebClient;
        this.agentRunRepository = agentRunRepository;
    }

    public Mono<AgentRunResponse> run(AgentRunRequest request, String bearerToken) {
        UUID runId = UUID.randomUUID();
        List<AgentStepDto> steps = new ArrayList<>();
        List<RagCitation> citations = new ArrayList<>();

        return switch (request.agentType()) {
            case "postExperimentTutor" -> runPostExperiment(request, bearerToken, runId, steps, citations);
            case "quizExplainer" -> runQuizExplainer(request, bearerToken, runId, steps, citations);
            default -> Mono.error(new IllegalArgumentException("Unknown agentType: " + request.agentType()));
        };
    }

    private Mono<AgentRunResponse> runPostExperiment(
            AgentRunRequest request,
            String bearerToken,
            UUID runId,
            List<AgentStepDto> steps,
            List<RagCitation> citations
    ) {
        String userMsg = request.userMessage() != null && !request.userMessage().isBlank()
                ? request.userMessage()
                : "Help me understand what I learned in the acids and bases lab.";
        RagAskRequest ask = new RagAskRequest(
                request.attemptId(), request.experimentId(), userMsg, request.lang());
        FoodForThoughtRequest fft = new FoodForThoughtRequest(
                "acids and bases", request.experimentId(), request.lang());

        return aiTutorWebClient.post()
                .uri("/api/v1/ai/ask")
                .headers(h -> h.setBearerAuth(bearerToken))
                .bodyValue(ask)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .flatMap(askNode -> {
                    steps.add(new AgentStepDto(0, "retrieveLabDocs", "Searched lab corpus (pgvector)"));
                    steps.add(new AgentStepDto(1, "explainFromCorpus", "Generated RAG-grounded explanation"));
                    askNode.path("citations").forEach(c -> citations.add(new RagCitation(
                            c.path("chunkId").asText(),
                            c.path("excerpt").asText(),
                            c.path("docType").asText())));
                    String explanation = askNode.path("answer").asText("");

                    return aiTutorWebClient.post()
                            .uri("/api/v1/ai/food-for-thought")
                            .headers(h -> h.setBearerAuth(bearerToken))
                            .bodyValue(fft)
                            .retrieve()
                            .bodyToMono(JsonNode.class)
                            .map(fftNode -> {
                                steps.add(new AgentStepDto(
                                        2, "generateFoodForThought", "Generated reflection questions"));
                                List<String> questions = new ArrayList<>();
                                fftNode.path("questions").forEach(q -> questions.add(q.asText()));
                                String reflection = questions.stream().map(q -> "• " + q).collect(Collectors.joining("\n"));
                                String finalAnswer = """
                                        EXPLANATION
                                        %s

                                        REFLECTION QUESTIONS
                                        %s
                                        """.formatted(explanation, reflection).trim();
                                AgentRunResponse response = new AgentRunResponse(
                                        runId.toString(),
                                        request.agentType(),
                                        "COMPLETED",
                                        finalAnswer,
                                        steps,
                                        citations);
                                persist(runId, request, response);
                                return response;
                            });
                });
    }

    private Mono<AgentRunResponse> runQuizExplainer(
            AgentRunRequest request,
            String bearerToken,
            UUID runId,
            List<AgentStepDto> steps,
            List<RagCitation> citations
    ) {
        String question = request.userMessage() != null ? request.userMessage() : "Why was my answer wrong?";
        RagAskRequest ask = new RagAskRequest(request.attemptId(), request.experimentId(), question, request.lang());

        return aiTutorWebClient.post()
                .uri("/api/v1/ai/ask")
                .headers(h -> h.setBearerAuth(bearerToken))
                .bodyValue(ask)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(node -> {
                    steps.add(new AgentStepDto(0, "retrieveLabDocs", "Retrieved lab context"));
                    steps.add(new AgentStepDto(1, "explain", "Generated explanation"));
                    node.path("citations").forEach(c -> citations.add(new RagCitation(
                            c.path("chunkId").asText(),
                            c.path("excerpt").asText(),
                            c.path("docType").asText())));
                    AgentRunResponse response = new AgentRunResponse(
                            runId.toString(),
                            request.agentType(),
                            "COMPLETED",
                            node.path("answer").asText(""),
                            steps,
                            citations);
                    persist(runId, request, response);
                    return response;
                });
    }

    private void persist(UUID runId, AgentRunRequest request, AgentRunResponse response) {
        try {
            agentRunRepository.saveRun(runId, request.attemptId(), request.agentType(), response.status(), request, response);
            for (AgentStepDto step : response.steps()) {
                agentRunRepository.saveStep(
                        UUID.randomUUID(),
                        runId,
                        step.stepIndex(),
                        step.toolName(),
                        "",
                        step.summary(),
                        step.summary());
            }
        } catch (Exception ignored) {
            // audit persistence is best-effort in dev
        }
    }
}
