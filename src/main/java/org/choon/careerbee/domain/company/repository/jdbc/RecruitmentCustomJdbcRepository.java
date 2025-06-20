package org.choon.careerbee.domain.company.repository.jdbc;

import java.util.List;
import org.choon.careerbee.domain.company.entity.recruitment.Recruitment;

public interface RecruitmentCustomJdbcRepository {

    void batchInsert(List<Recruitment> recruitments);

}
