package com.aijobsearch.backend.dto;

import com.aijobsearch.backend.entity.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
public class DashboardStatsResponse {
    private long totalJobsSaved;
    private long totalJobsAnalyzed;
    private Double averageMatchScorePercent;
    private long totalApplications;
    private Map<ApplicationStatus, Long> applicationsByStatus;
    private long totalSuggestionsGenerated;
    private long suggestionsAccepted;
    private long suggestionsRejected;
    private long suggestionsPending;
}