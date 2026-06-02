package com.virtulab.platform.rag.service;

import com.virtulab.platform.contracts.ai.RagJobStatusResponse;
import com.virtulab.platform.contracts.ai.RagReindexResponse;
import com.virtulab.platform.contracts.ai.RagStatsResponse;
import com.virtulab.platform.rag.corpus.CorpusDocument;
import com.virtulab.platform.rag.corpus.CorpusLoader;
import com.virtulab.platform.rag.embedding.OllamaEmbeddingClient;
import com.virtulab.platform.rag.job.ReindexJobStore;
import com.virtulab.platform.rag.store.VectorRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class ReindexService {

    private final CorpusLoader corpusLoader;
    private final VectorRepository vectorRepository;
    private final OllamaEmbeddingClient embeddingClient;
    private final ReindexJobStore jobStore;

    public ReindexService(
            CorpusLoader corpusLoader,
            VectorRepository vectorRepository,
            OllamaEmbeddingClient embeddingClient,
            ReindexJobStore jobStore
    ) {
        this.corpusLoader = corpusLoader;
        this.vectorRepository = vectorRepository;
        this.embeddingClient = embeddingClient;
        this.jobStore = jobStore;
    }

    public Mono<RagReindexResponse> startReindex() {
        String jobId = UUID.randomUUID().toString();
        jobStore.put(new RagJobStatusResponse(jobId, "RUNNING", 0, "Started"));
        Mono.fromRunnable(() -> runJob(jobId))
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
        return Mono.just(new RagReindexResponse(jobId, "RUNNING"));
    }

    private void runJob(String jobId) {
        try {
            int version = vectorRepository.nextCorpusVersion();
            vectorRepository.registerVersion(version);
            List<CorpusDocument> docs = corpusLoader.loadAll();
            int count = 0;
            for (CorpusDocument doc : docs) {
                float[] embedding = embeddingClient.embed(doc.content()).block();
                vectorRepository.insertChunk(UUID.randomUUID(), doc.content(), embedding, doc.metadata(), version);
                count++;
            }
            vectorRepository.activateVersion(version);
            jobStore.put(new RagJobStatusResponse(jobId, "COMPLETED", count, "Indexed " + count + " chunks"));
        } catch (Exception e) {
            jobStore.put(new RagJobStatusResponse(jobId, "FAILED", 0, e.getMessage()));
        }
    }

    public Mono<RagJobStatusResponse> jobStatus(String jobId) {
        return Mono.fromCallable(() -> {
            RagJobStatusResponse status = jobStore.get(jobId);
            if (status == null) {
                throw new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Unknown job: " + jobId);
            }
            return status;
        });
    }

    public Mono<RagStatsResponse> stats() {
        return Mono.fromCallable(vectorRepository::stats);
    }
}
