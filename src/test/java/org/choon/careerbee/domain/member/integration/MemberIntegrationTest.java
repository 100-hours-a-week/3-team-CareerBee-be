package org.choon.careerbee.domain.member.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.choon.careerbee.fixture.MemberFixture.createMember;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.choon.careerbee.domain.member.dto.request.UpdateProfileCommand;
import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.domain.member.entity.enums.MajorType;
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
    void testDirtyChecking() {
        // given
        Member member = memberRepository.save(
            createMember("testnick", "test@test.com", 123L)
        );

        // when
        member.updateResumeInfo(
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
            "https://example.com/profile.png", "new@test.com", "새닉네임"
        );

        member.updateProfileInfo(command);
        em.flush();
        em.clear();

        // when
        Member updated = memberRepository.findById(member.getId()).orElseThrow();

        // then
        assertThat(updated.getImgUrl()).isEqualTo(command.profileImgUrl());
        assertThat(updated.getEmail()).isEqualTo(command.email());
        assertThat(updated.getNickname()).isEqualTo(command.nickname());
    }
}