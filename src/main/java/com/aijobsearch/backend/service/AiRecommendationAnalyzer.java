package com.aijobsearch.backend.service;

import com.aijobsearch.backend.dto.MatchResult;
import com.aijobsearch.backend.dto.ai.RecommendationReasoning;
import com.aijobsearch.backend.dto.ai.StructuredJobDescription;

public interface AiRecommendationAnalyzer {
    RecommendationReasoning generateReasoning(String category, MatchResult matchResult, StructuredJobDescription jobAnalysis);
}