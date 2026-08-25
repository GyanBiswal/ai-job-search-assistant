package com.aijobsearch.backend.service;

import com.aijobsearch.backend.dto.ai.StructuredJobDescription;

public interface AiJobAnalyzer {
    StructuredJobDescription analyzeJobDescription(String rawDescription);
}