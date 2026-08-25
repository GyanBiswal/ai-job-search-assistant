package com.aijobsearch.backend.dto.ai;

import java.util.List;

public record StructuredResume(
        String summary,
        List<String> skills,
        List<EducationItem> education,
        List<ExperienceItem> experience,
        List<ProjectItem> projects
) {
    public record EducationItem(String degree, String institution, String year) {}

    public record ExperienceItem(String title, String company, String duration, String description) {}

    public record ProjectItem(String name, String description, List<String> technologies) {}
}