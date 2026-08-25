package com.aijobsearch.backend.service;

import com.aijobsearch.backend.dto.ai.StructuredResume;
import com.aijobsearch.backend.exception.ResumeProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GroqResumeAnalyzer implements AiResumeAnalyzer {

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = """
            You are a resume parsing assistant. Read the resume text provided by the user
            and extract structured information from it.

            Rules:
            - Only extract information that is explicitly present in the resume text.
            - Do NOT invent, assume, or add any skill, job, project, or qualification
              that is not clearly stated in the text.
            - If a section (education, experience, projects) is missing from the resume,
              return an empty list for it rather than guessing.
            - Keep descriptions concise (1-2 sentences).
            """;

    @Override
    public StructuredResume structureResume(String resumeText) {
        try {
            return chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .user(resumeText)
                    .call()
                    .entity(StructuredResume.class);
        } catch (Exception e) {
            throw new ResumeProcessingException("Failed to analyze resume with AI: " + e.getMessage());
        }
    }
}