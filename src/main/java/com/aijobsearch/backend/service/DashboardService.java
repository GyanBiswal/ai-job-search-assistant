package com.aijobsearch.backend.service;

import com.aijobsearch.backend.dto.DashboardStatsResponse;
import com.aijobsearch.backend.entity.ApplicationStatus;
import com.aijobsearch.backend.entity.SuggestionStatus;
import com.aijobsearch.backend.repository.ApplicationRepository;
import com.aijobsearch.backend.repository.JobRepository;
import com.aijobsearch.backend.repository.ResumeSuggestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final ResumeSuggestionRepository suggestionRepository;

    public DashboardStatsResponse getStats(Long userId) {
        long totalJobsSaved = jobRepository.countByUserId(userId);
        long totalJobsAnalyzed = jobRepository.countByUserIdAndStructuredDataIsNotNull(userId);
        Double averageMatchScore = jobRepository.findAverageMatchScoreByUserId(userId);

        long totalApplications = applicationRepository.countByUserId(userId);

        Map<ApplicationStatus, Long> applicationsByStatus = new EnumMap<>(ApplicationStatus.class);
        for (ApplicationStatus status : ApplicationStatus.values()) {
            applicationsByStatus.put(status, 0L);
        }
        applicationRepository.countGroupedByStatus(userId)
                .forEach(row -> applicationsByStatus.put(row.getStatus(), row.getCount()));

        long totalSuggestions = suggestionRepository.countByUserId(userId);
        long accepted = suggestionRepository.countByUserIdAndStatus(userId, SuggestionStatus.ACCEPTED);
        long rejected = suggestionRepository.countByUserIdAndStatus(userId, SuggestionStatus.REJECTED);
        long pending = suggestionRepository.countByUserIdAndStatus(userId, SuggestionStatus.PENDING);

        return DashboardStatsResponse.builder()
                .totalJobsSaved(totalJobsSaved)
                .totalJobsAnalyzed(totalJobsAnalyzed)
                .averageMatchScorePercent(averageMatchScore)
                .totalApplications(totalApplications)
                .applicationsByStatus(applicationsByStatus)
                .totalSuggestionsGenerated(totalSuggestions)
                .suggestionsAccepted(accepted)
                .suggestionsRejected(rejected)
                .suggestionsPending(pending)
                .build();
    }
}