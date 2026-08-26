package com.aijobsearch.backend.controller;

import com.aijobsearch.backend.dto.SuggestionDecisionRequest;
import com.aijobsearch.backend.dto.SuggestionResponse;
import com.aijobsearch.backend.entity.User;
import com.aijobsearch.backend.repository.UserRepository;
import com.aijobsearch.backend.service.SuggestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs/{jobId}/suggestions")
@RequiredArgsConstructor
public class SuggestionController {

    private final SuggestionService suggestionService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<List<SuggestionResponse>> generate(@PathVariable Long jobId, Authentication authentication) {
        User user = getCurrentUser(authentication);
        return ResponseEntity.ok(suggestionService.generateSuggestions(jobId, user.getId()));
    }

    @GetMapping
    public ResponseEntity<List<SuggestionResponse>> getAll(@PathVariable Long jobId, Authentication authentication) {
        User user = getCurrentUser(authentication);
        return ResponseEntity.ok(suggestionService.getSuggestions(jobId, user.getId()));
    }

    @PatchMapping("/{suggestionId}")
    public ResponseEntity<SuggestionResponse> decide(
            @PathVariable Long jobId,
            @PathVariable Long suggestionId,
            @Valid @RequestBody SuggestionDecisionRequest request,
            Authentication authentication
    ) {
        User user = getCurrentUser(authentication);
        return ResponseEntity.ok(suggestionService.decideSuggestion(jobId, suggestionId, user.getId(), request));
    }

    private User getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database"));
    }
}