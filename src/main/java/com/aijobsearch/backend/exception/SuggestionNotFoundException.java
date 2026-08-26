package com.aijobsearch.backend.exception;

public class SuggestionNotFoundException extends RuntimeException {
    public SuggestionNotFoundException(String message) {
        super(message);
    }
}