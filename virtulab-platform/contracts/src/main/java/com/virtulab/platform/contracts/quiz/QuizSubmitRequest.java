package com.virtulab.platform.contracts.quiz;

import java.util.List;

public record QuizSubmitRequest(
        String attemptId,
        String experimentId,
        List<QuizAnswerInput> answers
) {}
