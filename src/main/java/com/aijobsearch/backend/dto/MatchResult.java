package com.aijobsearch.backend.dto;

import java.util.List;

public record MatchResult(
        int matchScorePercent,
        List<String> matchingSkills,
        List<String> partiallyMatchingSkills,
        List<String> missingSkills,
        List<String> matchingNiceToHaveSkills,
        List<String> missingNiceToHaveSkills
) {}