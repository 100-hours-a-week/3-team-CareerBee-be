package org.choon.careerbee.domain.interview.domain.enums;

public enum ProblemType {
    AI("ai"),
    BACKEND("be"),
    DEVOPS("devops"),
    FRONTEND("fe");

    private final String prefix;

    ProblemType(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }
}
