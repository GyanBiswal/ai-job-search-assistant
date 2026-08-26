package com.aijobsearch.backend.exception;

public class JobNotAnalyzedException extends RuntimeException {
    public JobNotAnalyzedException(String message) {
        super(message);
    }
}