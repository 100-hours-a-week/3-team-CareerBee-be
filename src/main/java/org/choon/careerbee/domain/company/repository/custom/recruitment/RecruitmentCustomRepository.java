package org.choon.careerbee.domain.company.repository.custom.recruitment;

import java.util.Collection;
import java.util.List;

public interface RecruitmentCustomRepository {
    
    Collection<Long> findRecruitingIdByRecruitingIdIn(List<Long> jobIds);
    
}
