package com.aijobsearch.backend.entity;

public enum ApplicationStatus {
    SAVED(0),
    APPLIED(1),
    ASSESSMENT(2),
    INTERVIEW(3),
    OFFER(4),
    REJECTED(5);

    private final int rank;

    ApplicationStatus(int rank) {
        this.rank = rank;
    }

    public int getRank() {
        return rank;
    }

    public boolean isTerminal() {
        return this == OFFER || this == REJECTED;
    }
}