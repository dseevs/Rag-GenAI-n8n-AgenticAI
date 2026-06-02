package com.virtulab.platform.rag.temporal;

import com.virtulab.platform.rag.corpus.CorpusDocument;
import com.virtulab.platform.rag.corpus.CorpusLoader;
import com.virtulab.platform.rag.embedding.OllamaEmbeddingClient;
import com.virtulab.platform.rag.store.VectorRepository;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ReindexActivitiesImpl implements ReindexActivities {

    private final CorpusLoader corpusLoader;
    private final VectorRepository vectorRepository;
    private final OllamaEmbeddingClient embeddingClient;

    public ReindexActivitiesImpl(
            CorpusLoader corpusLoader,
            VectorRepository vectorRepository,
            OllamaEmbeddingClient embeddingClient
    ) {
        this.corpusLoader = corpusLoader;
        this.vectorRepository = vectorRepository;
        this.embeddingClient = embeddingClient;
    }

    @Override
    public int reindexCorpus() {
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
            return count;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}

