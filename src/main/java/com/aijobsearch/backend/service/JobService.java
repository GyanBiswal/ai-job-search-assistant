package com.aijobsearch.backend.service;

import com.aijobsearch.backend.dto.JobDescriptionRequest;
import com.aijobsearch.backend.dto.JobResponse;
import com.aijobsearch.backend.dto.MatchResult;
import com.aijobsearch.backend.dto.ai.StructuredJobDescription;
import com.aijobsearch.backend.dto.ai.StructuredResume;
import com.aijobsearch.backend.entity.Job;
import com.aijobsearch.backend.entity.Resume;
import com.aijobsearch.backend.entity.User;
import com.aijobsearch.backend.exception.JobNotAnalyzedException;
import com.aijobsearch.backend.exception.JobNotFoundException;
import com.aijobsearch.backend.exception.JobProcessingException;
import com.aijobsearch.backend.exception.ResumeNotFoundException;
import com.aijobsearch.backend.exception.ResumeNotStructuredException;
import com.aijobsearch.backend.repository.JobRepository;
import com.aijobsearch.backend.repository.ResumeRepository;
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
    private final ResumeRepository resumeRepository;
    private final AiJobAnalyzer aiJobAnalyzer;
    private final SkillMatchingService skillMatchingService;
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

    public MatchResult calculateMatch(Long jobId, Long userId) {
        Job job = jobRepository.findByIdAndUserId(jobId, userId)
                .orElseThrow(() -> new JobNotFoundException("Job not found"));

        if (job.getStructuredData() == null) {
            throw new JobNotAnalyzedException("Analyze this job description before calculating a match");
        }

        Resume resume = resumeRepository.findByUserId(userId)
                .orElseThrow(() -> new ResumeNotFoundException("No resume uploaded yet"));

        if (resume.getStructuredData() == null) {
            throw new ResumeNotStructuredException("Structure your resume before calculating a match");
        }

        try {
            StructuredJobDescription jobAnalysis =
                    objectMapper.readValue(job.getStructuredData(), StructuredJobDescription.class);
            StructuredResume resumeAnalysis =
                    objectMapper.readValue(resume.getStructuredData(), StructuredResume.class);

            MatchResult matchResult = skillMatchingService.calculateMatch(resumeAnalysis.skills(), jobAnalysis);

            job.setMatchResultData(objectMapper.writeValueAsString(matchResult));
            job.setMatchCalculatedAt(LocalDateTime.now());
            jobRepository.save(job);

            return matchResult;
        } catch (JsonProcessingException e) {
            throw new JobProcessingException("Failed to calculate match: " + e.getMessage());
        }
    }

    private JobResponse toResponse(Job job) {
        StructuredJobDescription structured = parseQuietly(job.getStructuredData(), StructuredJobDescription.class);
        MatchResult matchResult = parseQuietly(job.getMatchResultData(), MatchResult.class);

        return JobResponse.builder()
                .id(job.getId())
                .jobTitle(job.getJobTitle())
                .companyName(job.getCompanyName())
                .rawDescription(job.getRawDescription())
                .createdAt(job.getCreatedAt())
                .analyzedAt(job.getAnalyzedAt())
                .structuredAnalysis(structured)
                .matchCalculatedAt(job.getMatchCalculatedAt())
                .matchResult(matchResult)
                .build();
    }

    private <T> T parseQuietly(String json, Class<T> targetType) {
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, targetType);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}