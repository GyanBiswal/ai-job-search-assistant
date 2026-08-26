package com.aijobsearch.backend.dto;

import com.aijobsearch.backend.entity.SuggestionStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SuggestionDecisionRequest {

    @NotNull(message = "Status is required")
    private SuggestionStatus status;
}