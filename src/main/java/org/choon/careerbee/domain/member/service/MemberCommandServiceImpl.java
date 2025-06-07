package org.choon.careerbee.domain.member.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.choon.careerbee.domain.auth.service.oauth.OAuthInfoResponse;
import org.choon.careerbee.domain.member.dto.request.UpdateProfileCommand;
import org.choon.careerbee.domain.member.dto.request.UpdateProfileInfoReq;
import org.choon.careerbee.domain.member.dto.request.UpdateResumeReq;
import org.choon.careerbee.domain.member.dto.request.WithdrawCommand;
import org.choon.careerbee.domain.member.dto.request.WithdrawalReq;
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
    public void withdrawal(WithdrawalReq withdrawalReq, Long accessMemberId, LocalDateTime withdrawAt) {
        Member validMember = memberQueryService.findById(accessMemberId);
        validMember.withdraw(new WithdrawCommand(withdrawalReq.withdrawReason(), withdrawAt));
    }
}
