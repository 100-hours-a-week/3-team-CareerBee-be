package org.choon.careerbee.domain.member.dto.response;

public record ResumeCompleteResp(
    String resumeDownloadUrl
) implements AdvancedResumeResp {

    @Override
    public String message() {
        return "고급이력서 생성이 완료되었습니다.";
    }
}
