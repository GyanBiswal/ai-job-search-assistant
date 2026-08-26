package com.aijobsearch.backend.repository;

import com.aijobsearch.backend.entity.ResumeSuggestion;
import com.aijobsearch.backend.entity.SuggestionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ResumeSuggestionRepository extends JpaRepository<ResumeSuggestion, Long> {

    List<ResumeSuggestion> findByJobIdOrderByCreatedAtAsc(Long jobId);

    Optional<ResumeSuggestion> findByIdAndJobId(Long id, Long jobId);

    void deleteByJobId(Long jobId);

    @Query("SELECT COUNT(s) FROM ResumeSuggestion s WHERE s.job.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(s) FROM ResumeSuggestion s WHERE s.job.user.id = :userId AND s.status = :status")
    long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") SuggestionStatus status);
}