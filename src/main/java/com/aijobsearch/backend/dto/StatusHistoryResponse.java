package com.aijobsearch.backend.dto;

import com.aijobsearch.backend.entity.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class StatusHistoryResponse {
    private ApplicationStatus status;
    private LocalDateTime changedAt;
}