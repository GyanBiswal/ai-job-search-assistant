package com.aijobsearch.backend.dto.ai;

import java.util.List;

public record SuggestionBatch(List<SuggestionItem> suggestions) {
    public record SuggestionItem(String category, String suggestionText, String reasoning) {}
}