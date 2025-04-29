package org.choon.careerbee.domain.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.choon.careerbee.common.entity.BaseEntity;
import org.choon.careerbee.domain.auth.entity.enums.OAuthProvider;
import org.choon.careerbee.domain.member.entity.enums.CompanyType;
import org.choon.careerbee.domain.member.entity.enums.MajorType;
import org.choon.careerbee.domain.member.entity.enums.PreferredJob;
import org.choon.careerbee.domain.member.entity.enums.Role;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SQLRestriction("withdrawnAt is NULL")
public class Member extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(length = 6)
  @Enumerated(EnumType.STRING)
  private OAuthProvider oauthProvider;

  private Long providerId;

  @Column(length = 20, nullable = false)
  private String nickname;

  @Column(length = 50, nullable = false)
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

  @Column(length = 10)
  @Enumerated(EnumType.STRING)
  private CompanyType companyType;

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
  private Role role;

  @Column(length = 500)
  private String imgUrl;

  @Column(nullable = false)
  private Integer progress;
}
