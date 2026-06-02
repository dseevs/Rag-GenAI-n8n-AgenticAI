package com.virtulab.platform.ml.web;

import com.virtulab.platform.contracts.ml.MlBatchScoreResult;
import com.virtulab.platform.contracts.ml.MlModelVsActualReport;
import com.virtulab.platform.contracts.ml.MlPredictionDto;
import com.virtulab.platform.contracts.ml.MlScoreResult;
import com.virtulab.platform.ml.security.AuthHelper;
import com.virtulab.platform.ml.service.MlScoringService;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ml")
public class MlController {

    private final MlScoringService scoringService;

    public MlController(MlScoringService scoringService) {
        this.scoringService = scoringService;
    }

    @PostMapping("/score/attempt/{attemptId}")
    public MlScoreResult scoreAttempt(
            @PathVariable String attemptId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        AuthHelper.assertDeveloperOrAbove(jwt);
        return scoringService.scoreAttempt(attemptId);
    }

    @PostMapping("/score/batch")
    public MlBatchScoreResult scoreBatch(
            @RequestParam(defaultValue = "org-dev") String orgId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        AuthHelper.assertDeveloperOrAbove(jwt);
        return scoringService.scoreBatch(orgId);
    }

    @GetMapping("/predictions")
    public List<MlPredictionDto> predictions(
            @RequestParam String attemptId,
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal Jwt jwt
    ) {
        AuthHelper.assertDeveloperOrAbove(jwt);
        return scoringService.predictions(attemptId, Math.min(limit, 50));
    }

    @GetMapping("/model-vs-actual")
    public MlModelVsActualReport modelVsActual(
            @RequestParam(defaultValue = "org-dev") String orgId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        AuthHelper.assertDeveloperOrAbove(jwt);
        return scoringService.modelVsActual(orgId);
    }
}
