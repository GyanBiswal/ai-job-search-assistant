package com.aijobsearch.backend.repository;

import com.aijobsearch.backend.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JobRepository extends JpaRepository<Job, Long> {

    List<Job> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Job> findByIdAndUserId(Long id, Long userId);

    long countByUserId(Long userId);

    long countByUserIdAndStructuredDataIsNotNull(Long userId);

    @Query("SELECT AVG(j.matchScorePercent) FROM Job j WHERE j.user.id = :userId AND j.matchScorePercent IS NOT NULL")
    Double findAverageMatchScoreByUserId(@Param("userId") Long userId);
}