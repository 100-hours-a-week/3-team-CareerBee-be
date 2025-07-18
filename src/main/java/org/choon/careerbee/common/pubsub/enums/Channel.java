package org.choon.careerbee.common.pubsub.enums;

import java.util.Arrays;
import lombok.Getter;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.common.exception.CustomException;

@Getter
public enum Channel {
    RESUME_EXTRACTED("resume.extract.complete"),
    ADVANCED_RESUME_INIT("advanced.resume.init.complete"),
    ADVANCED_RESUME_UPDATE("advanced.resume.update.complete"),
    PROBLEM_FEEDBACK("interview.problem.feedback.complete"),
    COMPETITION_POINT("competition.participant.point"),
    OPEN_RECRUITING("open-recruiting"),
    DAILY_WINNER("daily-winner"),
    AI_ERROR_CHANNEL("ai-error-channel");

    private String value;

    Channel(String value) {
        this.value = value;
    }

    public static Channel from(String value) {
        return Arrays.stream(Channel.values())
            .filter(c -> c.getValue().equals(value))
            .findFirst()
            .orElseThrow(() -> new CustomException(CustomResponseStatus.CHANNEL_NOT_FOUND));
    }
}
