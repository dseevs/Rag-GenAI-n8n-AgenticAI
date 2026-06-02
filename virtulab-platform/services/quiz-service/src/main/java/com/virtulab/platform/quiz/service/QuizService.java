package com.virtulab.platform.quiz.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.virtulab.platform.contracts.quiz.ExamIntegrityReport;
import com.virtulab.platform.contracts.quiz.QuizAttemptDto;
import com.virtulab.platform.contracts.quiz.QuizSubmitRequest;
import com.virtulab.platform.contracts.quiz.QuizSubmitResponse;
import com.virtulab.platform.quiz.messaging.QuizEventPublisher;
import com.virtulab.platform.quiz.store.QuizRepository;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class QuizService {

    private final QuizRepository repository;
    private final QuizEventPublisher publisher;
    private final ObjectMapper objectMapper;

    public QuizService(QuizRepository repository, QuizEventPublisher publisher, ObjectMapper objectMapper) {
        this.repository = repository;
        this.publisher = publisher;
        this.objectMapper = objectMapper;
    }

    public QuizSubmitResponse submit(QuizSubmitRequest request, Jwt jwt) {
        var session = repository.findSession(request.attemptId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));

        String userId = com.virtulab.platform.quiz.security.AuthHelper.userId(jwt);
        if (!session.userId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Attempt does not belong to user");
        }
        if (!session.experimentId().equals(request.experimentId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Experiment mismatch");
        }
        if ("EXAM".equals(session.mode()) && repository.existsForAttempt(request.attemptId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Exam already submitted — one attempt only");
        }

        List<QuizRepository.QuestionRow> questions =
                repository.questionsForExperiment(request.experimentId());
        if (questions.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No quiz questions for experiment");
        }

        Map<String, String> answers = new HashMap<>();
        request.answers().forEach(a -> answers.put(a.questionId(), a.answer()));

        int correct = 0;
        for (QuizRepository.QuestionRow q : questions) {
            String given = answers.getOrDefault(q.id(), "").trim();
            if (normalize(given).equals(normalize(q.correctAnswer()))) {
                correct++;
            }
        }

        int total = questions.size();
        int score = total == 0 ? 0 : (int) Math.round(100.0 * correct / total);

        try {
            repository.insertAttempt(
                    request.attemptId(),
                    userId,
                    request.experimentId(),
                    session.orgId(),
                    session.mode(),
                    score,
                    total,
                    correct,
                    objectMapper.writeValueAsString(answers));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Quiz already submitted");
        }

        QuizSubmitResponse response = new QuizSubmitResponse(
                request.attemptId(),
                score,
                total,
                correct,
                session.mode(),
                "EXAM".equals(session.mode()),
                Instant.now());

        publisher.publish(userId, request.experimentId(), response);
        return response;
    }

    public QuizAttemptDto getAttempt(String attemptId, Jwt jwt) {
        var attempt = repository.findAttempt(attemptId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz attempt not found"));
        String userId = com.virtulab.platform.quiz.security.AuthHelper.userId(jwt);
        if (!attempt.userId().equals(userId)
                && !com.virtulab.platform.quiz.security.AuthHelper.roles(jwt).contains("SUPER_ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot view attempt");
        }
        return attempt;
    }

    public ExamIntegrityReport integrityReport(String orgId) {
        return repository.integrityReport(orgId);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
