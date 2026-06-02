package com.virtulab.platform.contracts.quiz;

import java.time.Instant;

public record ExamIntegrityRow(
        String attemptId,
        String userId,
        String experimentId,
        String mode,
        int score,
        Instant submittedAt
) {}
