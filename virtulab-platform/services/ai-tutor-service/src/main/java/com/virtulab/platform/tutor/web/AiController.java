package com.virtulab.platform.tutor.web;

import com.virtulab.platform.contracts.ai.FoodForThoughtRequest;
import com.virtulab.platform.contracts.ai.FoodForThoughtResponse;
import com.virtulab.platform.contracts.ai.RagAskRequest;
import com.virtulab.platform.contracts.ai.RagAskResponse;
import com.virtulab.platform.tutor.service.TutorService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/ai")
public class AiController {

    private final TutorService tutorService;

    public AiController(TutorService tutorService) {
        this.tutorService = tutorService;
    }

    @PostMapping("/food-for-thought")
    public Mono<FoodForThoughtResponse> foodForThought(
            @Valid @RequestBody FoodForThoughtRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return tutorService.foodForThought(request, jwt.getSubject());
    }

    @PostMapping("/ask")
    public Mono<RagAskResponse> ask(
            @Valid @RequestBody RagAskRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return tutorService.ask(request, jwt.getSubject());
    }
}
