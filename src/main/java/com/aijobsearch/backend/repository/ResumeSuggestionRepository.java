package com.aijobsearch.backend.repository;

import com.aijobsearch.backend.entity.ResumeSuggestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResumeSuggestionRepository extends JpaRepository<ResumeSuggestion, Long> {

    List<ResumeSuggestion> findByJobIdOrderByCreatedAtAsc(Long jobId);

    Optional<ResumeSuggestion> findByIdAndJobId(Long id, Long jobId);

    void deleteByJobId(Long jobId);
}