package com.aijobsearch.backend.repository;

import com.aijobsearch.backend.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JobRepository extends JpaRepository<Job, Long> {

    List<Job> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Job> findByIdAndUserId(Long id, Long userId);
}