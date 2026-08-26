package com.aijobsearch.backend.dto;

import java.util.List;

public record JobRecommendation(
        String category,
        int matchScorePercent,
        String reasoning,
        List<String> keyStrengths,
        List<String> keyGaps
) {}