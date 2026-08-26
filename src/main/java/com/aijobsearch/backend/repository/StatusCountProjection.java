package com.aijobsearch.backend.repository;

import com.aijobsearch.backend.entity.ApplicationStatus;

public interface StatusCountProjection {
    ApplicationStatus getStatus();
    Long getCount();
}