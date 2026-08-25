package com.aijobsearch.backend.controller;

import com.aijobsearch.backend.dto.ResumeResponse;
import com.aijobsearch.backend.entity.User;
import com.aijobsearch.backend.repository.UserRepository;
import com.aijobsearch.backend.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.aijobsearch.backend.dto.ai.StructuredResume;

@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;
    private final UserRepository userRepository;

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<ResumeResponse> upload(
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {
        User user = getCurrentUser(authentication);
        ResumeResponse response = resumeService.uploadResume(file, user);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<ResumeResponse> getMyResume(Authentication authentication) {
        User user = getCurrentUser(authentication);
        ResumeResponse response = resumeService.getMyResume(user.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/structure")
    public ResponseEntity<StructuredResume> structure(Authentication authentication) {
        User user = getCurrentUser(authentication);
        StructuredResume response = resumeService.structureResume(user.getId());
        return ResponseEntity.ok(response);
    }

    private User getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database"));
    }
}