package org.choon.careerbee.domain.company.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.choon.careerbee.domain.company.entity.enums.Source;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CompanyRating {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "company_id", nullable = false)
  private Company company;

  @Column(length = 9, nullable = false)
  @Enumerated(EnumType.STRING)
  private Source source;

  @Column(nullable = false, precision = 3, scale = 2)
  private BigDecimal rating;
}