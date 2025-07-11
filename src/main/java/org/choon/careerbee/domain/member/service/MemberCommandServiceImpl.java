package org.choon.careerbee.domain.member.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.choon.careerbee.api.ai.AiApiClient;
import org.choon.careerbee.domain.auth.service.oauth.OAuthInfoResponse;
import org.choon.careerbee.domain.image.dto.request.ExtractResumeReq;
import org.choon.careerbee.domain.image.dto.response.GetPresignedUrlResp;
import org.choon.careerbee.domain.image.service.ImageService;
import org.choon.careerbee.domain.member.dto.internal.AdvancedResumeInitReq;
import org.choon.careerbee.domain.member.dto.internal.AdvancedResumeRespFromAi;
import org.choon.careerbee.domain.member.dto.request.AdvancedResumeUpdateReq;
import org.choon.careerbee.domain.member.dto.request.AdvancedResumeUpdateReqToAi;
import org.choon.careerbee.domain.member.dto.request.ResumeDraftReq;
import org.choon.careerbee.domain.member.dto.request.UpdateProfileCommand;
import org.choon.careerbee.domain.member.dto.request.UpdateProfileInfoReq;
import org.choon.careerbee.domain.member.dto.request.UpdateResumeReq;
import org.choon.careerbee.domain.member.dto.request.UploadCompleteReq;
import org.choon.careerbee.domain.member.dto.request.WithdrawCommand;
import org.choon.careerbee.domain.member.dto.request.WithdrawalReq;
import org.choon.careerbee.domain.member.dto.response.AdvancedResumeInitResp;
import org.choon.careerbee.domain.member.dto.response.AdvancedResumeResp;
import org.choon.careerbee.domain.member.dto.response.ExtractResumeResp;
import org.choon.careerbee.domain.member.dto.response.ResumeCompleteResp;
import org.choon.careerbee.domain.member.dto.response.ResumeDraftResp;
import org.choon.careerbee.domain.member.dto.response.ResumeInProgressResp;
import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.domain.member.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberCommandServiceImpl implements MemberCommandService {

    private final MemberQueryService memberQueryService;
    private final MemberRepository memberRepository;
    private final ImageService imageService;
    private final AiApiClient aiApiClient;

    @Override
    public Member forceJoin(OAuthInfoResponse oAuthInfo) {
        Member newMember = Member.builder()
            .nickname(oAuthInfo.getNickname())
            .email(oAuthInfo.getEmail())
            .oAuthProvider(oAuthInfo.getOauthProvider())
            .providerId(oAuthInfo.getProviderId())
            .build();

        return memberRepository.save(newMember);
    }

    @Override
    public void updateResumeInfo(UpdateResumeReq updateResumeReq, Long accessMemberId) {
        Member validMember = memberQueryService.findById(accessMemberId);

        validMember.updateResumeInfo(
            updateResumeReq.preferredJob(),
            updateResumeReq.psTier(),
            updateResumeReq.certificationCount(),
            updateResumeReq.projectCount(),
            updateResumeReq.majorType(),
            updateResumeReq.companyName(),
            updateResumeReq.workPeriod(),
            updateResumeReq.position(),
            updateResumeReq.additionalExperiences()
        );
    }

    @Override
    public void updateProfileInfo(UpdateProfileInfoReq updateProfileInfoReq, Long accessMemberId) {
        Member validMember = memberQueryService.findById(accessMemberId);

        String profileUrl = updateProfileInfoReq.newProfileUrl();
        String objectUrl = (profileUrl == null)
            ? validMember.getImgUrl()
            : imageService.getObjectUrlByKey(profileUrl).objectUrl();

        validMember.updateProfileInfo(new UpdateProfileCommand(
            objectUrl,
            updateProfileInfoReq.newNickname())
        );
    }

    @Override
    public void withdrawal(WithdrawalReq withdrawalReq, Long accessMemberId,
        LocalDateTime withdrawAt) {
        Member validMember = memberQueryService.findById(accessMemberId);
        validMember.withdraw(new WithdrawCommand(withdrawalReq.withdrawReason(), withdrawAt));
    }

    @Override
    public ResumeDraftResp generateResumeDraft(Long accessMemberId) {
        Member validMember = memberQueryService.findById(accessMemberId);

        return aiApiClient.requestResumeDraft(ResumeDraftReq.from(validMember));
    }

    @Override
    public ExtractResumeResp extractResumeInfoFromAi(UploadCompleteReq uploadCompleteReq) {
        // 1. object key를 통해서 get 용 presigned-url 생성로직
        ExtractResumeReq extractResumeReq = new ExtractResumeReq(
            imageService.generateGetPresignedUrlByObjectKey(uploadCompleteReq).presignedUrl()
        );

        // 2. 만들어진 정보로 ai서버에 이력서 정보추출 요청
        return ExtractResumeResp.from(aiApiClient.requestExtractResume(extractResumeReq));
    }

    @Override
    public AdvancedResumeInitResp generateAdvancedResumeInit(Long accessMemberId) {
        Member validMember = memberQueryService.findById(accessMemberId);

        return aiApiClient.requestAdvancedResumeInit(
            new AdvancedResumeInitReq(
                validMember.getId(),
                ResumeDraftReq.from(validMember)
            )
        );
    }

    @Override
    public AdvancedResumeResp generateAdvancedResumeUpdate(
        AdvancedResumeUpdateReq advancedResumeUpdateReq,
        Long accessMemberId
    ) {
        AdvancedResumeRespFromAi result = aiApiClient.requestAdvancedResumeUpdate(
            AdvancedResumeUpdateReqToAi.of(accessMemberId, advancedResumeUpdateReq.answer())
        );

        if (result.isComplete()) {
            GetPresignedUrlResp presignedUrlResp = imageService.generateGetPresignedUrlByObjectKey(
                new UploadCompleteReq(result.resumeObjectKey())
            );

            return new ResumeCompleteResp(
                presignedUrlResp.presignedUrl()
            );
        }

        return new ResumeInProgressResp(
            result.question()
        );
    }
}
