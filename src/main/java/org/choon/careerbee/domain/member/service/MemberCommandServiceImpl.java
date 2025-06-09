package org.choon.careerbee.domain.member.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.choon.careerbee.api.ai.AiApiClient;
import org.choon.careerbee.domain.auth.service.oauth.OAuthInfoResponse;
import org.choon.careerbee.domain.image.dto.request.ExtractResumeReq;
import org.choon.careerbee.domain.image.service.ImageService;
import org.choon.careerbee.domain.member.dto.request.ResumeDraftReq;
import org.choon.careerbee.domain.member.dto.request.UpdateProfileCommand;
import org.choon.careerbee.domain.member.dto.request.UpdateProfileInfoReq;
import org.choon.careerbee.domain.member.dto.request.UpdateResumeReq;
import org.choon.careerbee.domain.member.dto.request.UploadCompleteReq;
import org.choon.careerbee.domain.member.dto.request.WithdrawCommand;
import org.choon.careerbee.domain.member.dto.request.WithdrawalReq;
import org.choon.careerbee.domain.member.dto.response.ExtractResumeResp;
import org.choon.careerbee.domain.member.dto.response.ResumeDraftResp;
import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.domain.member.repository.MemberRepository;
import org.choon.careerbee.util.NicknameGenerator;
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
            .nickname(NicknameGenerator.generate())
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
        memberQueryService.checkEmailExist(updateProfileInfoReq.newEmail());

        Member validMember = memberQueryService.findById(accessMemberId);
        validMember.updateProfileInfo(new UpdateProfileCommand(
            updateProfileInfoReq.newProfileUrl(),
            updateProfileInfoReq.newEmail(),
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
        ExtractResumeResp extractResumeResp = aiApiClient.requestExtractResume(extractResumeReq);
        System.out.println("extractResumeResp = " + extractResumeResp);
        return extractResumeResp;
    }
}
