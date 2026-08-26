package com.aijobsearch.backend.service;

import org.springframework.stereotype.Service;

@Service
public class RecommendationCategorizer {

    private static final int APPLY_THRESHOLD = 70;
    private static final int CONSIDER_THRESHOLD = 40;

    public String categorize(int matchScorePercent) {
        if (matchScorePercent >= APPLY_THRESHOLD) {
            return "APPLY";
        } else if (matchScorePercent >= CONSIDER_THRESHOLD) {
            return "CONSIDER";
        } else {
            return "SKIP";
        }
    }
}