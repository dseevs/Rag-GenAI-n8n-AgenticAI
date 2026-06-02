package com.virtulab.platform.contracts.quiz;

import java.util.List;

public record ExamIntegrityReport(
        String orgId,
        int totalSubmissions,
        int examSubmissions,
        int practiceSubmissions,
        int duplicateExamAttempts,
        List<ExamIntegrityRow> rows
) {}
