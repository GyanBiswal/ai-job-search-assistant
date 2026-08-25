package com.aijobsearch.backend.service;

import com.aijobsearch.backend.dto.ai.StructuredResume;

public interface AiResumeAnalyzer {
    StructuredResume structureResume(String resumeText);
}