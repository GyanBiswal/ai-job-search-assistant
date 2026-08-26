package com.aijobsearch.backend.service;

import com.aijobsearch.backend.dto.ApplicationResponse;
import com.aijobsearch.backend.dto.StatusHistoryResponse;
import com.aijobsearch.backend.entity.Application;
import com.aijobsearch.backend.entity.ApplicationStatus;
import com.aijobsearch.backend.entity.ApplicationStatusHistory;
import com.aijobsearch.backend.entity.Job;
import com.aijobsearch.backend.entity.User;
import com.aijobsearch.backend.exception.ApplicationAlreadyExistsException;
import com.aijobsearch.backend.exception.ApplicationNotFoundException;
import com.aijobsearch.backend.exception.InvalidStatusTransitionException;
import com.aijobsearch.backend.exception.JobNotFoundException;
import com.aijobsearch.backend.repository.ApplicationRepository;
import com.aijobsearch.backend.repository.ApplicationStatusHistoryRepository;
import com.aijobsearch.backend.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationStatusHistoryRepository historyRepository;
    private final JobRepository jobRepository;

    @Transactional
    public ApplicationResponse createApplication(Long jobId, Long userId) {
        Job job = jobRepository.findByIdAndUserId(jobId, userId)
                .orElseThrow(() -> new JobNotFoundException("Job not found"));

        if (applicationRepository.existsByJobId(job.getId())) {
            throw new ApplicationAlreadyExistsException("This job is already being tracked as an application");
        }

        Application application = Application.builder()
                .user(job.getUser())
                .job(job)
                .status(ApplicationStatus.SAVED)
                .build();

        Application saved = applicationRepository.save(application);

        ApplicationStatusHistory historyEntry = ApplicationStatusHistory.builder()
                .application(saved)
                .status(ApplicationStatus.SAVED)
                .build();
        historyRepository.save(historyEntry);

        return toResponse(saved);
    }

    public List<ApplicationResponse> getMyApplications(Long userId) {
        return applicationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ApplicationResponse getMyApplication(Long applicationId, Long userId) {
        Application application = applicationRepository.findByIdAndUserId(applicationId, userId)
                .orElseThrow(() -> new ApplicationNotFoundException("Application not found"));
        return toResponse(application);
    }

    @Transactional
    public ApplicationResponse updateStatus(Long applicationId, Long userId, ApplicationStatus newStatus) {
        Application application = applicationRepository.findByIdAndUserId(applicationId, userId)
                .orElseThrow(() -> new ApplicationNotFoundException("Application not found"));

        ApplicationStatus currentStatus = application.getStatus();

        if (currentStatus.isTerminal()) {
            throw new InvalidStatusTransitionException(
                    "Cannot change status: this application is already " + currentStatus
            );
        }

        if (newStatus == currentStatus) {
            throw new InvalidStatusTransitionException("Application is already in status " + newStatus);
        }

        if (newStatus != ApplicationStatus.REJECTED && newStatus.getRank() <= currentStatus.getRank()) {
            throw new InvalidStatusTransitionException(
                    "Cannot move status backward from " + currentStatus + " to " + newStatus
            );
        }

        application.setStatus(newStatus);
        application.setUpdatedAt(LocalDateTime.now());
        Application saved = applicationRepository.save(application);

        ApplicationStatusHistory historyEntry = ApplicationStatusHistory.builder()
                .application(saved)
                .status(newStatus)
                .build();
        historyRepository.save(historyEntry);

        return toResponse(saved);
    }

    private ApplicationResponse toResponse(Application application) {
        List<StatusHistoryResponse> history = historyRepository
                .findByApplicationIdOrderByChangedAtAsc(application.getId())
                .stream()
                .map(h -> StatusHistoryResponse.builder()
                        .status(h.getStatus())
                        .changedAt(h.getChangedAt())
                        .build())
                .toList();

        return ApplicationResponse.builder()
                .id(application.getId())
                .jobId(application.getJob().getId())
                .jobTitle(application.getJob().getJobTitle())
                .companyName(application.getJob().getCompanyName())
                .status(application.getStatus())
                .createdAt(application.getCreatedAt())
                .updatedAt(application.getUpdatedAt())
                .statusHistory(history)
                .build();
    }
}