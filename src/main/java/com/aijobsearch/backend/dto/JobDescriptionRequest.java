package com.aijobsearch.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JobDescriptionRequest {

    private String jobTitle;

    private String companyName;

    @NotBlank(message = "Job description text is required")
    private String rawDescription;
}