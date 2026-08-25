package com.aijobsearch.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ResumeResponse {
    private Long id;
    private String originalFileName;
    private String extractedText;
    private LocalDateTime uploadedAt;
}