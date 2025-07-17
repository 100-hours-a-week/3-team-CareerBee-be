package org.choon.careerbee.domain.notification.service.sse;

import org.choon.careerbee.domain.interview.dto.response.AiFeedbackResp;
import org.choon.careerbee.domain.member.dto.response.AdvancedResumeInitResp;
import org.choon.careerbee.domain.member.dto.response.AdvancedResumeResp;
import org.choon.careerbee.domain.member.dto.response.ExtractResumeResp;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface SseService<T> {

    SseEmitter connect(Long memberId);

    void sendTo(Long memberId);

    void pushResumeExtracted(Long memberId, ExtractResumeResp resp);

    void pushAdvancedResumeInit(Long memberId, AdvancedResumeInitResp resp);

    void pushAdvancedResumeUpdate(Long memberId, AdvancedResumeResp resp);

    void pushProblemFeedback(Long problemId, AiFeedbackResp resp);

    void sendAll();

    void sendPingToAll();
}
