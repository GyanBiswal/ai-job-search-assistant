package com.aijobsearch.backend.dto.ai;

import java.util.List;

public record StructuredJobDescription(
        String jobTitle,
        String companyName,
        String seniorityLevel,
        List<String> requiredSkills,
        List<String> niceToHaveSkills,
        List<String> responsibilities,
        String summary
) {}