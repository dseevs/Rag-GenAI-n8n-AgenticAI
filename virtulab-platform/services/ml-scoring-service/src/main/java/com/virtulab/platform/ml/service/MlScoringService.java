package com.virtulab.platform.ml.service;

import com.virtulab.platform.contracts.ml.MlBatchScoreResult;
import com.virtulab.platform.contracts.ml.MlModelVsActualReport;
import com.virtulab.platform.contracts.ml.MlPredictionDto;
import com.virtulab.platform.contracts.ml.MlScoreResult;
import com.virtulab.platform.ml.model.AttemptFeatureExtractor;
import com.virtulab.platform.ml.model.CompletionScorer;
import com.virtulab.platform.ml.store.MlPredictionRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MlScoringService {

    private final AttemptFeatureExtractor featureExtractor;
    private final CompletionScorer scorer;
    private final MlPredictionRepository repository;
    private final String modelVersion;

    public MlScoringService(
            AttemptFeatureExtractor featureExtractor,
            CompletionScorer scorer,
            MlPredictionRepository repository,
            @Value("${virtulab.ml.model-version}") String modelVersion
    ) {
        this.featureExtractor = featureExtractor;
        this.scorer = scorer;
        this.repository = repository;
        this.modelVersion = modelVersion;
    }

    public MlScoreResult scoreAttempt(String attemptId) {
        var features = featureExtractor.extract(attemptId);
        var scored = scorer.score(features);
        repository.insert(scored);
        return toResult(scored, true);
    }

    public MlBatchScoreResult scoreBatch(String orgId) {
        List<String> attemptIds = repository.distinctAttemptIds(orgId);
        List<MlScoreResult> results = new ArrayList<>();
        for (String attemptId : attemptIds) {
            try {
                results.add(scoreAttempt(attemptId));
            } catch (Exception ignored) {
                // skip attempts with no analytics data
            }
        }
        return new MlBatchScoreResult(results.size(), modelVersion, results);
    }

    public List<MlPredictionDto> predictions(String attemptId, int limit) {
        return repository.findByAttempt(attemptId, limit);
    }

    public MlModelVsActualReport modelVsActual(String orgId) {
        return repository.modelVsActual(orgId, modelVersion);
    }

    private static MlScoreResult toResult(CompletionScorer.ScoredAttempt scored, boolean stored) {
        return new MlScoreResult(
                scored.features().attemptId(),
                scored.modelVersion(),
                scored.score(),
                scored.features().features().toString(),
                stored);
    }
}
