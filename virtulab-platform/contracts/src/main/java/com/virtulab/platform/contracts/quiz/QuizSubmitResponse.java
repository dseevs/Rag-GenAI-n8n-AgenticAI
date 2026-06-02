package com.virtulab.platform.contracts.quiz;

import java.time.Instant;

public record QuizSubmitResponse(
        String attemptId,
        int score,
        int totalQuestions,
        int correctCount,
        String mode,
        boolean examEnforced,
        Instant submittedAt
) {}
