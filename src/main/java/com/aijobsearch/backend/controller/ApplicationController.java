package com.aijobsearch.backend.controller;

import com.aijobsearch.backend.dto.ApplicationResponse;
import com.aijobsearch.backend.dto.CreateApplicationRequest;
import com.aijobsearch.backend.dto.UpdateStatusRequest;
import com.aijobsearch.backend.entity.User;
import com.aijobsearch.backend.repository.UserRepository;
import com.aijobsearch.backend.service.ApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<ApplicationResponse> create(
            @Valid @RequestBody CreateApplicationRequest request,
            Authentication authentication
    ) {
        User user = getCurrentUser(authentication);
        ApplicationResponse response = applicationService.createApplication(request.getJobId(), user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ApplicationResponse>> getAll(Authentication authentication) {
        User user = getCurrentUser(authentication);
        return ResponseEntity.ok(applicationService.getMyApplications(user.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponse> getOne(@PathVariable Long id, Authentication authentication) {
        User user = getCurrentUser(authentication);
        return ResponseEntity.ok(applicationService.getMyApplication(id, user.getId()));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApplicationResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest request,
            Authentication authentication
    ) {
        User user = getCurrentUser(authentication);
        ApplicationResponse response = applicationService.updateStatus(id, user.getId(), request.getStatus());
        return ResponseEntity.ok(response);
    }

    private User getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database"));
    }
}