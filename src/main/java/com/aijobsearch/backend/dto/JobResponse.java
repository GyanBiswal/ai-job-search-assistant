package com.aijobsearch.backend.dto;

import com.aijobsearch.backend.dto.ai.StructuredJobDescription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class JobResponse {
    private Long id;
    private String jobTitle;
    private String companyName;
    private String rawDescription;
    private LocalDateTime createdAt;
    private LocalDateTime analyzedAt;
    private StructuredJobDescription structuredAnalysis;
}