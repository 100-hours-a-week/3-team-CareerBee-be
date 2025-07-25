package org.choon.careerbee.common.pubsub.enums;

import lombok.Getter;

@Getter
public enum EventName {

    RESUME_EXTRACTED("resume-extracted"),
    ADVANCED_RESUME_INIT("advanced-resume-init"),
    ADVANCED_RESUME_UPDATE("advanced-resume-update"),
    PROBLEM_FEEDBACK("problem-feedback");

    private String value;

    EventName(String value) {
        this.value = value;
    }
}
