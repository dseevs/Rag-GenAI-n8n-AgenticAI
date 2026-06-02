package com.virtulab.platform.quiz.web;

import com.virtulab.platform.contracts.quiz.ExamIntegrityReport;
import com.virtulab.platform.contracts.quiz.QuizAttemptDto;
import com.virtulab.platform.contracts.quiz.QuizSubmitRequest;
import com.virtulab.platform.contracts.quiz.QuizSubmitResponse;
import com.virtulab.platform.quiz.security.AuthHelper;
import com.virtulab.platform.quiz.service.QuizService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/quiz")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @PostMapping("/submit")
    public QuizSubmitResponse submit(
            @Valid @RequestBody QuizSubmitRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return quizService.submit(request, jwt);
    }

    @GetMapping("/attempts/{attemptId}")
    public QuizAttemptDto getAttempt(
            @PathVariable String attemptId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return quizService.getAttempt(attemptId, jwt);
    }

    @GetMapping("/integrity-report")
    public ExamIntegrityReport integrityReport(
            @RequestParam(defaultValue = "org-dev") String orgId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        AuthHelper.assertSuperAdmin(jwt);
        return quizService.integrityReport(orgId);
    }
}
