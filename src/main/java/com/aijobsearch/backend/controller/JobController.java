package com.aijobsearch.backend.controller;

import com.aijobsearch.backend.dto.JobDescriptionRequest;
import com.aijobsearch.backend.dto.JobResponse;
import com.aijobsearch.backend.dto.ai.StructuredJobDescription;
import com.aijobsearch.backend.entity.User;
import com.aijobsearch.backend.repository.UserRepository;
import com.aijobsearch.backend.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.aijobsearch.backend.dto.MatchResult;
import com.aijobsearch.backend.dto.JobRecommendation;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<JobResponse> saveJob(
            @Valid @RequestBody JobDescriptionRequest request,
            Authentication authentication
    ) {
        User user = getCurrentUser(authentication);
        JobResponse response = jobService.saveJobDescription(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<JobResponse>> getMyJobs(Authentication authentication) {
        User user = getCurrentUser(authentication);
        return ResponseEntity.ok(jobService.getMyJobs(user.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> getMyJob(@PathVariable Long id, Authentication authentication) {
        User user = getCurrentUser(authentication);
        return ResponseEntity.ok(jobService.getMyJob(id, user.getId()));
    }

    @PostMapping("/{id}/analyze")
    public ResponseEntity<StructuredJobDescription> analyzeJob(@PathVariable Long id, Authentication authentication) {
        User user = getCurrentUser(authentication);
        StructuredJobDescription response = jobService.analyzeJob(id, user.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/match")
    public ResponseEntity<MatchResult> calculateMatch(@PathVariable Long id, Authentication authentication) {
        User user = getCurrentUser(authentication);
        MatchResult response = jobService.calculateMatch(id, user.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/recommendation")
    public ResponseEntity<JobRecommendation> generateRecommendation(@PathVariable Long id, Authentication authentication) {
        User user = getCurrentUser(authentication);
        JobRecommendation response = jobService.generateRecommendation(id, user.getId());
        return ResponseEntity.ok(response);
    }

    private User getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database"));
    }
}