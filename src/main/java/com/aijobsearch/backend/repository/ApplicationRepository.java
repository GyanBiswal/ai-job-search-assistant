package com.aijobsearch.backend.repository;

import com.aijobsearch.backend.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Application> findByIdAndUserId(Long id, Long userId);

    boolean existsByJobId(Long jobId);

    long countByUserId(Long userId);

    @Query("SELECT a.status AS status, COUNT(a) AS count FROM Application a WHERE a.user.id = :userId GROUP BY a.status")
    List<StatusCountProjection> countGroupedByStatus(@Param("userId") Long userId);
}