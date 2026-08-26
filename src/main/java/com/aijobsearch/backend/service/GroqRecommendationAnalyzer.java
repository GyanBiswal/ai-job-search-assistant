package com.aijobsearch.backend.service;

import com.aijobsearch.backend.dto.MatchResult;
import com.aijobsearch.backend.dto.ai.RecommendationReasoning;
import com.aijobsearch.backend.dto.ai.StructuredJobDescription;
import com.aijobsearch.backend.exception.JobProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GroqRecommendationAnalyzer implements AiRecommendationAnalyzer {

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = """
            You are a career advisor helping a job seeker decide whether to apply to a job.
            You will be given:
            - A recommendation category that has ALREADY been decided (APPLY, CONSIDER, or SKIP)
              based on a computed match score. Do NOT change, question, or contradict this category.
            - The job's required and nice-to-have skills.
            - Which skills from the candidate's resume matched, partially matched, or were missing.

            Your job is ONLY to explain this decision in plain, encouraging, honest language.

            Rules:
            - Do not invent skills, experience, or qualifications the candidate doesn't have.
            - Reference specific skills from the data provided, not generic statements.
            - Keep the reasoning to 2-4 sentences.
            - List 2-4 concrete key strengths and 2-4 concrete key gaps, based only on the
              provided matching/missing skill data.
            """;

    @Override
    public RecommendationReasoning generateReasoning(
            String category,
            MatchResult matchResult,
            StructuredJobDescription jobAnalysis
    ) {
        String userMessage = """
                Recommendation category: %s
                Match score: %d%%
                Job title: %s

                Matching required skills: %s
                Partially matching required skills: %s
                Missing required skills: %s
                Matching nice-to-have skills: %s
                Missing nice-to-have skills: %s
                """.formatted(
                category,
                matchResult.matchScorePercent(),
                jobAnalysis.jobTitle(),
                matchResult.matchingSkills(),
                matchResult.partiallyMatchingSkills(),
                matchResult.missingSkills(),
                matchResult.matchingNiceToHaveSkills(),
                matchResult.missingNiceToHaveSkills()
        );

        try {
            return chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .user(userMessage)
                    .call()
                    .entity(RecommendationReasoning.class);
        } catch (Exception e) {
            throw new JobProcessingException("Failed to generate recommendation reasoning: " + e.getMessage());
        }
    }
}