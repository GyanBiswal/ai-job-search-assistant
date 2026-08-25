package com.aijobsearch.backend.service;

import com.aijobsearch.backend.dto.ai.StructuredJobDescription;
import com.aijobsearch.backend.exception.JobProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GroqJobAnalyzer implements AiJobAnalyzer {

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = """
            You are a job description analysis assistant. Read the job description text
            provided by the user and extract structured information from it.

            Rules:
            - Only extract information that is explicitly stated or clearly implied in the text.
            - Distinguish clearly between required (must-have) skills and nice-to-have
              (preferred/bonus) skills, based on how the job description phrases them.
            - If the job title or company name is not present in the text, return an empty
              string for that field rather than guessing.
            - Keep the summary concise (2-3 sentences).
            """;

    @Override
    public StructuredJobDescription analyzeJobDescription(String rawDescription) {
        try {
            return chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .user(rawDescription)
                    .call()
                    .entity(StructuredJobDescription.class);
        } catch (Exception e) {
            throw new JobProcessingException("Failed to analyze job description with AI: " + e.getMessage());
        }
    }
}