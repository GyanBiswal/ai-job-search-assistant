package com.aijobsearch.backend.dto;

import com.aijobsearch.backend.entity.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateStatusRequest {

    @NotNull(message = "Status is required")
    private ApplicationStatus status;
}