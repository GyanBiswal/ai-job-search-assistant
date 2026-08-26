package com.aijobsearch.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateApplicationRequest {

    @NotNull(message = "jobId is required")
    private Long jobId;
}