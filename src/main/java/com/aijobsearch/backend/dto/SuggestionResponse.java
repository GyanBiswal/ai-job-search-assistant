package com.aijobsearch.backend.dto;

import com.aijobsearch.backend.entity.SuggestionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class SuggestionResponse {
    private Long id;
    private String category;
    private String suggestionText;
    private String reasoning;
    private SuggestionStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime decidedAt;
}