package org.choon.careerbee.domain.company.repository.custom.recruitment;

import static org.choon.careerbee.domain.company.entity.recruitment.QRecruitment.recruitment;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
            .select(recruitment.id)
            .from(recruitment)
            .where(recruitment.recruitingId.in(jobIds))
            .fetch();
    }
}
