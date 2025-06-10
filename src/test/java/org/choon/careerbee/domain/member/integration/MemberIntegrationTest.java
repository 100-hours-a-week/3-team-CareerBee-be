package org.choon.careerbee.domain.member.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.choon.careerbee.fixture.MemberFixture.createMember;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import org.choon.careerbee.domain.member.dto.request.UpdateProfileCommand;
import org.choon.careerbee.domain.member.dto.request.WithdrawCommand;
import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.domain.member.entity.enums.MajorType;
import org.choon.careerbee.domain.member.entity.enums.PreferredJob;
import org.choon.careerbee.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MemberIntegrationTest {

    @Autowired
    private MemberRepository memberRepository;

    @PersistenceContext
    private EntityManager em;

    @Test
    @DisplayName("이력 정보 수정 - 자격증 수, 프로젝트 수, 전공, 회사명, 기간, 직무, 추가 경험이 DB에 반영된다")
    void updateResumeInfo_shouldPersistAllFields() {
        // given
        Member member = memberRepository.save(
            createMember("testnick", "test@test.com", 123L)
        );

        // when
        member.updateResumeInfo(
            PreferredJob.BACKEND,
            "BR1",
            3,
            2,
            MajorType.MAJOR,
            "nexon",
            20,
            "백엔드",
            "메이플스토리 만들었음"
        );
        em.flush();
        em.clear();

        // then
        Member updated = memberRepository.findById(member.getId()).orElseThrow();

        assertThat(updated.getPreferredJob()).isEqualTo(PreferredJob.BACKEND);
        assertThat(updated.getPsTier()).isEqualTo("BR1");
        assertThat(updated.getCertificationCount()).isEqualTo(3);
        assertThat(updated.getProjectCount()).isEqualTo(2);
        assertThat(updated.getMajorType()).isEqualTo(MajorType.MAJOR);
        assertThat(updated.getCompanyName()).isEqualTo("nexon");
        assertThat(updated.getWorkPeriod()).isEqualTo(20);
        assertThat(updated.getPosition()).isEqualTo("백엔드");
        assertThat(updated.getAdditionalExperiences()).isEqualTo("메이플스토리 만들었음");
    }

    @Test
    @DisplayName("내 정보 수정 - DB에 프로필 이미지, 이메일, 닉네임이 반영된다")
    void updateProfileInfo_shouldUpdateFields() {
        // given
        Member member = memberRepository.save(createMember("nick", "email@test.com", 999L));

        UpdateProfileCommand command = new UpdateProfileCommand(
            "https://example.com/profile.png", "새닉네임"
        );

        member.updateProfileInfo(command);
        em.flush();
        em.clear();

        // when
        Member updated = memberRepository.findById(member.getId()).orElseThrow();

        // then
        assertThat(updated.getImgUrl()).isEqualTo(command.profileImgUrl());
        assertThat(updated.getNickname()).isEqualTo(command.nickname());
    }

    @Test
    @DisplayName("회원 탈퇴 - 탈퇴 사유와 탈퇴 일시가 DB에 정상 반영된다")
    void withdraw_shouldPersistWithdrawReasonAndTime() {
        // given
        Member member = memberRepository.save(
            createMember("testnick", "test@test.com", 999L)
        );
        String reason = "서비스 종료";
        LocalDateTime withdrawAt = LocalDateTime.of(2025, 6, 6, 12, 0);

        // when
        member.withdraw(new WithdrawCommand(reason, withdrawAt));
        em.flush();
        em.clear();

        // then : native query 를 직접 사용하여 @SqlRestriction 우회
        Member withdrawn = (Member) em.createNativeQuery(
                "SELECT * FROM member WHERE id = :id", Member.class
            ).setParameter("id", member.getId())
            .getSingleResult();

        assertThat(withdrawn.getWithdrawReason()).isEqualTo(reason);
        assertThat(withdrawn.getWithdrawnAt()).isEqualTo(withdrawAt);
    }
}
