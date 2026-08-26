package com.aijobsearch.backend.service;

import com.aijobsearch.backend.dto.MatchResult;
import com.aijobsearch.backend.dto.ai.StructuredJobDescription;
import com.aijobsearch.backend.dto.ai.StructuredResume;
import com.aijobsearch.backend.dto.ai.SuggestionBatch;

public interface AiSuggestionGenerator {
    SuggestionBatch generateSuggestions(StructuredResume resume, StructuredJobDescription job, MatchResult matchResult);
}