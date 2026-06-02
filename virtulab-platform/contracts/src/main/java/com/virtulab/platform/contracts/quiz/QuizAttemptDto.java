package com.virtulab.platform.contracts.quiz;

import java.time.Instant;

public record QuizAttemptDto(
        String id,
        String attemptId,
        String userId,
        String experimentId,
        String mode,
        int score,
        int totalQuestions,
        int correctCount,
        Instant submittedAt
) {}
