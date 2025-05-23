package org.choon.careerbee.domain.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.choon.careerbee.common.entity.BaseEntity;
import org.choon.careerbee.domain.auth.entity.enums.OAuthProvider;
import org.choon.careerbee.domain.member.entity.enums.MajorType;
import org.choon.careerbee.domain.member.entity.enums.PreferredJob;
import org.choon.careerbee.domain.member.entity.enums.RoleType;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SQLRestriction("withdrawn_at is NULL")
@Table(uniqueConstraints = {
    @UniqueConstraint(columnNames = {"provider", "provider_id"})
})
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 6)
    @Enumerated(EnumType.STRING)
    private OAuthProvider provider;

    @Column(nullable = false)
    private Long providerId;

    @Column(length = 20, nullable = false)
    private String nickname;

    @Column(length = 50, nullable = false, unique = true)
    private String email;

    @Column(length = 9)
    @Enumerated(EnumType.STRING)
    private PreferredJob preferredJob;

    @Column(length = 9)
    private String psTier;

    @Column(nullable = false)
    private Integer certificationCount;

    @Column(length = 9)
    @Enumerated(EnumType.STRING)
    private MajorType majorType;

    @Column(length = 50)
    private String companyName;

    @Column(nullable = false)
    private Integer workPeriod;

    @Column(length = 20)
    private String position;

    @Column(length = 100)
    private String additionalExperiences;

    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime withdrawnAt;

    @Column(nullable = false)
    private Integer points;

    @Column(length = 11, nullable = false)
    @Enumerated(EnumType.STRING)
    private RoleType role;

    @Column(length = 500)
    private String imgUrl;

    @Column(nullable = false)
    private Integer progress;

    @Builder
    public Member(String nickname, String email, OAuthProvider oAuthProvider, Long providerId) {
        this.nickname = nickname;
        this.email = email;
        this.provider = oAuthProvider;
        this.providerId = providerId;
        this.certificationCount = 0;
        this.role = RoleType.ROLE_MEMBER;
        this.points = 0;
        this.progress = 0;
        this.workPeriod = 0;
    }
}
