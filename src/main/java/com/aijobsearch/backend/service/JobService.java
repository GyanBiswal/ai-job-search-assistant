package com.aijobsearch.backend.service;

import com.aijobsearch.backend.dto.JobDescriptionRequest;
import com.aijobsearch.backend.dto.JobResponse;
import com.aijobsearch.backend.dto.ai.StructuredJobDescription;
import com.aijobsearch.backend.entity.Job;
import com.aijobsearch.backend.entity.User;
import com.aijobsearch.backend.exception.JobNotFoundException;
import com.aijobsearch.backend.exception.JobProcessingException;
import com.aijobsearch.backend.repository.JobRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final AiJobAnalyzer aiJobAnalyzer;
    private final ObjectMapper objectMapper;

    public JobResponse saveJobDescription(JobDescriptionRequest request, User user) {
        Job job = Job.builder()
                .user(user)
                .jobTitle(request.getJobTitle())
                .companyName(request.getCompanyName())
                .rawDescription(request.getRawDescription())
                .build();

        Job saved = jobRepository.save(job);
        return toResponse(saved);
    }

    public List<JobResponse> getMyJobs(Long userId) {
        return jobRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public JobResponse getMyJob(Long jobId, Long userId) {
        Job job = jobRepository.findByIdAndUserId(jobId, userId)
                .orElseThrow(() -> new JobNotFoundException("Job not found"));
        return toResponse(job);
    }

    public StructuredJobDescription analyzeJob(Long jobId, Long userId) {
        Job job = jobRepository.findByIdAndUserId(jobId, userId)
                .orElseThrow(() -> new JobNotFoundException("Job not found"));

        StructuredJobDescription structured = aiJobAnalyzer.analyzeJobDescription(job.getRawDescription());

        try {
            job.setStructuredData(objectMapper.writeValueAsString(structured));
            job.setAnalyzedAt(LocalDateTime.now());

            if (job.getJobTitle() == null || job.getJobTitle().isBlank()) {
                job.setJobTitle(structured.jobTitle());
            }
            if (job.getCompanyName() == null || job.getCompanyName().isBlank()) {
                job.setCompanyName(structured.companyName());
            }

            jobRepository.save(job);
        } catch (JsonProcessingException e) {
            throw new JobProcessingException("Failed to save job analysis: " + e.getMessage());
        }

        return structured;
    }

    private JobResponse toResponse(Job job) {
        StructuredJobDescription structured = null;
        if (job.getStructuredData() != null) {
            try {
                structured = objectMapper.readValue(job.getStructuredData(), StructuredJobDescription.class);
            } catch (JsonProcessingException e) {
                structured = null;
            }
        }

        return JobResponse.builder()
                .id(job.getId())
                .jobTitle(job.getJobTitle())
                .companyName(job.getCompanyName())
                .rawDescription(job.getRawDescription())
                .createdAt(job.getCreatedAt())
                .analyzedAt(job.getAnalyzedAt())
                .structuredAnalysis(structured)
                .build();
    }
}