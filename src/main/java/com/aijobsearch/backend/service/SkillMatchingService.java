package com.aijobsearch.backend.service;

import com.aijobsearch.backend.dto.MatchResult;
import com.aijobsearch.backend.dto.ai.StructuredJobDescription;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SkillMatchingService {

    public MatchResult calculateMatch(List<String> resumeSkills, StructuredJobDescription jobAnalysis) {
        Set<String> normalizedResumeSkills = resumeSkills.stream()
                .map(this::normalize)
                .collect(Collectors.toSet());

        List<String> matching = new ArrayList<>();
        List<String> partial = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        for (String requiredSkill : jobAnalysis.requiredSkills()) {
            String normalizedRequired = normalize(requiredSkill);

            if (normalizedResumeSkills.contains(normalizedRequired)) {
                matching.add(requiredSkill);
            } else if (hasPartialMatch(normalizedRequired, normalizedResumeSkills)) {
                partial.add(requiredSkill);
            } else {
                missing.add(requiredSkill);
            }
        }

        List<String> matchingNiceToHave = new ArrayList<>();
        List<String> missingNiceToHave = new ArrayList<>();

        for (String niceToHaveSkill : jobAnalysis.niceToHaveSkills()) {
            String normalizedNiceToHave = normalize(niceToHaveSkill);

            if (normalizedResumeSkills.contains(normalizedNiceToHave)
                    || hasPartialMatch(normalizedNiceToHave, normalizedResumeSkills)) {
                matchingNiceToHave.add(niceToHaveSkill);
            } else {
                missingNiceToHave.add(niceToHaveSkill);
            }
        }

        int matchScorePercent = calculateScore(matching.size(), partial.size(), jobAnalysis.requiredSkills().size());

        return new MatchResult(
                matchScorePercent,
                matching,
                partial,
                missing,
                matchingNiceToHave,
                missingNiceToHave
        );
    }

    private int calculateScore(int exactMatches, int partialMatches, int totalRequired) {
        if (totalRequired == 0) {
            return 0;
        }
        double weightedScore = (exactMatches * 1.0 + partialMatches * 0.5) / totalRequired * 100;
        return (int) Math.round(Math.min(weightedScore, 100));
    }

    private boolean hasPartialMatch(String normalizedSkill, Set<String> normalizedResumeSkills) {
        if (normalizedSkill.length() < 3) {
            return false; // Too short for substring matching to be meaningful (e.g. "C", "R", "Go")
        }
        return normalizedResumeSkills.stream()
                .anyMatch(resumeSkill ->
                        resumeSkill.length() >= 3
                                && (resumeSkill.contains(normalizedSkill) || normalizedSkill.contains(resumeSkill))
                );
    }

    private String normalize(String skill) {
        return skill.toLowerCase().trim();
    }
}