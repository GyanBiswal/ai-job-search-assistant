package com.aijobsearch.backend.service;

import com.aijobsearch.backend.dto.MatchResult;
import com.aijobsearch.backend.dto.ai.StructuredJobDescription;
import com.aijobsearch.backend.dto.ai.StructuredResume;
import com.aijobsearch.backend.dto.ai.SuggestionBatch;
import com.aijobsearch.backend.exception.JobProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GroqSuggestionGenerator implements AiSuggestionGenerator {

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = """
            You are a resume improvement assistant. You help candidates present their EXISTING
            experience more effectively for a specific job — you never invent new experience.

            You will be given the candidate's structured resume, a job's requirements, and which
            required skills matched, partially matched, or were missing.

            Generate 3 to 6 specific, actionable resume improvement suggestions.

            STRICT RULES:
            - NEVER suggest the candidate claim a skill, tool, or experience that is not already
              present in their resume. This is a hard rule with no exceptions.
            - For skills the candidate genuinely lacks (missing skills), you may only suggest
              honest options such as highlighting transferable experience, or noting it as an
              area they are actively learning — never suggest claiming the skill outright.
            - Favor suggestions like: rewording a bullet point to use the job's terminology,
              reordering sections to highlight relevant matching skills first, quantifying an
              existing achievement with metrics, or expanding on a project that already uses a
              required skill.
            - Each suggestion needs a short category label (e.g. "Wording", "Emphasis",
              "Quantify Achievement", "Section Order"), the suggestion itself, and a one-sentence
              reasoning tied to the specific job.
            """;

    @Override
    public SuggestionBatch generateSuggestions(
            StructuredResume resume,
            StructuredJobDescription job,
            MatchResult matchResult
    ) {
        String userMessage = """
                Candidate's resume skills: %s
                Candidate's experience: %s
                Candidate's projects: %s

                Job title: %s
                Required skills: %s
                Nice-to-have skills: %s

                Matching required skills: %s
                Partially matching required skills: %s
                Missing required skills: %s
                """.formatted(
                resume.skills(),
                resume.experience(),
                resume.projects(),
                job.jobTitle(),
                job.requiredSkills(),
                job.niceToHaveSkills(),
                matchResult.matchingSkills(),
                matchResult.partiallyMatchingSkills(),
                matchResult.missingSkills()
        );

        try {
            return chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .user(userMessage)
                    .call()
                    .entity(SuggestionBatch.class);
        } catch (Exception e) {
            throw new JobProcessingException("Failed to generate resume suggestions: " + e.getMessage());
        }
    }
}