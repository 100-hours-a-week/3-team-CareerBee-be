package org.choon.careerbee.domain.company.repository.custom.recruitment;

import static org.choon.careerbee.domain.company.entity.recruitment.QRecruitment.recruitment;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.choon.careerbee.domain.company.dto.response.CompanyActiveCount;
import org.choon.careerbee.domain.company.entity.recruitment.Recruitment;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RecruitmentCustomRepositoryImpl implements
    RecruitmentCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Collection<Long> findRecruitingIdByRecruitingIdIn(List<Long> jobIds) {
        if (jobIds == null || jobIds.isEmpty()) {
            return Collections.emptyList();
        }

        return queryFactory
            .select(recruitment.recruitingId)
            .from(recruitment)
            .where(recruitment.recruitingId.in(jobIds))
            .fetch();
    }

    @Override
    public List<Recruitment> findExpiredBefore(LocalDateTime now) {
        return queryFactory
            .selectFrom(recruitment)
            .where(
                recruitment.endDate.before(now),
                recruitment.expiredAt.isNull()
            )
            .fetch();
    }

    @Override
    public List<CompanyActiveCount> countActiveByCompany() {
        return queryFactory
            .select(Projections.constructor(
                CompanyActiveCount.class,
                recruitment.company.id,
                recruitment.count()
            ))
            .from(recruitment)
            .where(recruitment.expiredAt.isNull())
            .groupBy(recruitment.company.id)
            .fetch();
    }
}
