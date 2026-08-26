package com.aijobsearch.backend.service;

import com.aijobsearch.backend.dto.MatchResult;
import com.aijobsearch.backend.dto.SuggestionDecisionRequest;
import com.aijobsearch.backend.dto.SuggestionResponse;
import com.aijobsearch.backend.dto.ai.StructuredJobDescription;
import com.aijobsearch.backend.dto.ai.StructuredResume;
import com.aijobsearch.backend.dto.ai.SuggestionBatch;
import com.aijobsearch.backend.entity.Job;
import com.aijobsearch.backend.entity.Resume;
import com.aijobsearch.backend.entity.ResumeSuggestion;
import com.aijobsearch.backend.entity.SuggestionStatus;
import com.aijobsearch.backend.exception.JobNotAnalyzedException;
import com.aijobsearch.backend.exception.JobNotFoundException;
import com.aijobsearch.backend.exception.JobProcessingException;
import com.aijobsearch.backend.exception.MatchNotCalculatedException;
import com.aijobsearch.backend.exception.ResumeNotFoundException;
import com.aijobsearch.backend.exception.ResumeNotStructuredException;
import com.aijobsearch.backend.exception.SuggestionNotFoundException;
import com.aijobsearch.backend.repository.JobRepository;
import com.aijobsearch.backend.repository.ResumeRepository;
import com.aijobsearch.backend.repository.ResumeSuggestionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SuggestionService {

    private final ResumeSuggestionRepository suggestionRepository;
    private final JobRepository jobRepository;
    private final ResumeRepository resumeRepository;
    private final AiSuggestionGenerator aiSuggestionGenerator;
    private final ObjectMapper objectMapper;

    @Transactional
    public List<SuggestionResponse> generateSuggestions(Long jobId, Long userId) {
        Job job = jobRepository.findByIdAndUserId(jobId, userId)
                .orElseThrow(() -> new JobNotFoundException("Job not found"));

        if (job.getStructuredData() == null) {
            throw new JobNotAnalyzedException("Analyze this job description before generating suggestions");
        }
        if (job.getMatchResultData() == null) {
            throw new MatchNotCalculatedException("Calculate the match score before generating suggestions");
        }

        Resume resume = resumeRepository.findByUserId(userId)
                .orElseThrow(() -> new ResumeNotFoundException("No resume uploaded yet"));

        if (resume.getStructuredData() == null) {
            throw new ResumeNotStructuredException("Structure your resume before generating suggestions");
        }

        try {
            StructuredJobDescription jobAnalysis =
                    objectMapper.readValue(job.getStructuredData(), StructuredJobDescription.class);
            StructuredResume resumeAnalysis =
                    objectMapper.readValue(resume.getStructuredData(), StructuredResume.class);
            MatchResult matchResult =
                    objectMapper.readValue(job.getMatchResultData(), MatchResult.class);

            SuggestionBatch batch = aiSuggestionGenerator.generateSuggestions(resumeAnalysis, jobAnalysis, matchResult);

            // Replace any previous suggestions for this job with the fresh batch
            suggestionRepository.deleteByJobId(job.getId());

            List<ResumeSuggestion> entities = batch.suggestions().stream()
                    .map(item -> ResumeSuggestion.builder()
                            .job(job)
                            .category(item.category())
                            .suggestionText(item.suggestionText())
                            .reasoning(item.reasoning())
                            .status(SuggestionStatus.PENDING)
                            .build())
                    .toList();

            List<ResumeSuggestion> saved = suggestionRepository.saveAll(entities);

            return saved.stream().map(this::toResponse).toList();
        } catch (JsonProcessingException e) {
            throw new JobProcessingException("Failed to generate suggestions: " + e.getMessage());
        }
    }

    public List<SuggestionResponse> getSuggestions(Long jobId, Long userId) {
        jobRepository.findByIdAndUserId(jobId, userId)
                .orElseThrow(() -> new JobNotFoundException("Job not found"));

        return suggestionRepository.findByJobIdOrderByCreatedAtAsc(jobId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public SuggestionResponse decideSuggestion(
            Long jobId,
            Long suggestionId,
            Long userId,
            SuggestionDecisionRequest request
    ) {
        jobRepository.findByIdAndUserId(jobId, userId)
                .orElseThrow(() -> new JobNotFoundException("Job not found"));

        ResumeSuggestion suggestion = suggestionRepository.findByIdAndJobId(suggestionId, jobId)
                .orElseThrow(() -> new SuggestionNotFoundException("Suggestion not found"));

        if (request.getStatus() != SuggestionStatus.ACCEPTED && request.getStatus() != SuggestionStatus.REJECTED) {
            throw new IllegalArgumentException("Status must be ACCEPTED or REJECTED");
        }

        suggestion.setStatus(request.getStatus());
        suggestion.setDecidedAt(LocalDateTime.now());
        ResumeSuggestion saved = suggestionRepository.save(suggestion);

        return toResponse(saved);
    }

    private SuggestionResponse toResponse(ResumeSuggestion suggestion) {
        return SuggestionResponse.builder()
                .id(suggestion.getId())
                .category(suggestion.getCategory())
                .suggestionText(suggestion.getSuggestionText())
                .reasoning(suggestion.getReasoning())
                .status(suggestion.getStatus())
                .createdAt(suggestion.getCreatedAt())
                .decidedAt(suggestion.getDecidedAt())
                .build();
    }
}