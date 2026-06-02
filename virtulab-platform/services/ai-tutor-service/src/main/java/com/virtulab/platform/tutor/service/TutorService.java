package com.virtulab.platform.tutor.service;

import com.virtulab.platform.contracts.ai.FoodForThoughtRequest;
import com.virtulab.platform.contracts.ai.FoodForThoughtResponse;
import com.virtulab.platform.contracts.ai.RagAskRequest;
import com.virtulab.platform.contracts.ai.RagAskResponse;
import com.virtulab.platform.contracts.ai.RagCitation;
import com.virtulab.platform.tutor.embedding.OllamaClient;
import com.virtulab.platform.tutor.kafka.AiEventPublisher;
import com.virtulab.platform.tutor.rag.KnowledgeChunk;
import com.virtulab.platform.tutor.rag.VectorSearchRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class TutorService {

    private static final Logger log = LoggerFactory.getLogger(TutorService.class);

    private static final List<String> STATIC_FFT = List.of(
            "What did you observe that surprised you?",
            "How does this result connect to what you learned in theory?",
            "What would you do differently if you repeated the experiment?"
    );

    private final OllamaClient ollamaClient;
    private final VectorSearchRepository vectorSearch;
    private final ReactiveStringRedisTemplate redis;
    private final AiEventPublisher aiEventPublisher;

    public TutorService(
            OllamaClient ollamaClient,
            VectorSearchRepository vectorSearch,
            ReactiveStringRedisTemplate redis,
            AiEventPublisher aiEventPublisher
    ) {
        this.ollamaClient = ollamaClient;
        this.vectorSearch = vectorSearch;
        this.redis = redis;
        this.aiEventPublisher = aiEventPublisher;
    }

    public Mono<FoodForThoughtResponse> foodForThought(FoodForThoughtRequest req, String userId) {
        long start = System.currentTimeMillis();
        String experimentId = req.experimentId() != null ? req.experimentId() : "v1-chemistry";
        String cacheKey = "fft:" + req.topic() + ":" + req.lang() + ":" + experimentId;

        return redis.opsForValue().get(cacheKey)
                .flatMap(json -> Mono.just(new FoodForThoughtResponse(List.of(json.split("\\|")), "cache")))
                .switchIfEmpty(buildFft(req, experimentId)
                        .flatMap(resp -> redis.opsForValue()
                                .set(cacheKey, String.join("|", resp.questions()))
                                .onErrorResume(e -> Mono.empty())
                                .thenReturn(resp))
                        .doOnSuccess(r -> aiEventPublisher.publish(
                                "food_for_thought",
                                userId,
                                experimentId,
                                null,
                                r.source(),
                                System.currentTimeMillis() - start)))
                .onErrorResume(e -> {
                    log.warn("Redis/cache skipped for FFT: {}", e.getMessage());
                    return buildFft(req, experimentId);
                });
    }

    private Mono<FoodForThoughtResponse> buildFft(FoodForThoughtRequest req, String experimentId) {
        return Mono.defer(() -> {
            try {
                List<KnowledgeChunk> reflection =
                        vectorSearch.searchByDocType(experimentId, req.lang(), "reflection", 3);
                if (reflection.isEmpty()) {
                    reflection = vectorSearch.searchByDocType(experimentId, req.lang(), "theory", 3);
                }
                if (reflection.isEmpty()) {
                    return Mono.just(new FoodForThoughtResponse(STATIC_FFT, "static"));
                }
                String context = reflection.stream().map(KnowledgeChunk::content).collect(Collectors.joining("\n\n"));
                String prompt = """
                        Based only on this lab context, write exactly 3 short reflection questions about topic "%s".
                        Return one question per line, no numbering.
                        Context:
                        %s
                        """.formatted(req.topic(), context);

                return ollamaClient.generate(prompt)
                        .map(text -> {
                            List<String> lines = text.lines()
                                    .map(String::trim)
                                    .filter(s -> !s.isBlank())
                                    .limit(3)
                                    .toList();
                            if (lines.size() < 3) {
                                return new FoodForThoughtResponse(STATIC_FFT, "static-fallback");
                            }
                            return new FoodForThoughtResponse(lines, "ollama-rag");
                        })
                        .defaultIfEmpty(new FoodForThoughtResponse(STATIC_FFT, "static-fallback"));
            } catch (Exception e) {
                log.warn("FFT build failed (DB/RAG?): {} — using static questions", e.getMessage());
                return Mono.just(new FoodForThoughtResponse(STATIC_FFT, "static-error-fallback"));
            }
        });
    }

    public Mono<RagAskResponse> ask(RagAskRequest req, String userId) {
        long start = System.currentTimeMillis();
        return ollamaClient.embed(req.question())
                .flatMap(vector -> {
                    List<KnowledgeChunk> chunks = vectorSearch.search(vector, req.experimentId(), req.lang(), 5, false);
                    if (chunks.isEmpty()) {
                        return Mono.just(new RagAskResponse(
                                "No indexed lab content found. Ask your teacher to run RAG reindex.",
                                List.of(),
                                "empty-index"));
                    }
                    List<RagCitation> citations = chunks.stream()
                            .map(c -> new RagCitation(
                                    c.id().toString(),
                                    c.content().length() > 200 ? c.content().substring(0, 200) + "..." : c.content(),
                                    c.docType()))
                            .toList();
                    String context = chunks.stream().map(KnowledgeChunk::content).collect(Collectors.joining("\n\n---\n\n"));
                    String prompt = """
                            Answer the student question using ONLY the lab context below.
                            If the answer is not in the context, say you do not have enough information.
                            Question: %s
                            Context:
                            %s
                            """.formatted(req.question(), context);

                    return ollamaClient.generate(prompt)
                            .defaultIfEmpty("I could not generate an answer right now. Please review the theory tab.")
                            .map(answer -> new RagAskResponse(answer, citations, "ollama-rag"));
                })
                .doOnSuccess(r -> aiEventPublisher.publish(
                        "ask",
                        userId,
                        req.experimentId(),
                        req.attemptId(),
                        r.source(),
                        System.currentTimeMillis() - start));
    }
}
