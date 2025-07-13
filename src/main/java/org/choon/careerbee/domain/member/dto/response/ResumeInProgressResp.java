package org.choon.careerbee.domain.member.dto.response;

public record ResumeInProgressResp(
    String question
) implements AdvancedResumeResp {

    @Override
    public String message() {
        return "고급이력서 생성이 완료되고 있습니다.";
    }
}
