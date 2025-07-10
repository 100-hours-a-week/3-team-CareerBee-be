package org.choon.careerbee.domain.member.dto.response;

public sealed interface AdvancedResumeResp
    permits ResumeInProgressResp, ResumeCompleteResp {

    String message();
}
