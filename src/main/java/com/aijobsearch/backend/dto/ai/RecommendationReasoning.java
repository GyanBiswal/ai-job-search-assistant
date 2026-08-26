package com.aijobsearch.backend.dto.ai;

import java.util.List;

public record RecommendationReasoning(
        String reasoning,
        List<String> keyStrengths,
        List<String> keyGaps
) {}