package org.choon.careerbee.domain.company.repository.recruitment;

import org.choon.careerbee.domain.company.entity.recruitment.Recruitment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecruitmentRepository extends JpaRepository<Recruitment, Long> {

    boolean existsByRecruitingId(Long recruitingId);
}
