package org.choon.careerbee.domain.competition.repository.custom.competition;

import java.util.List;
import org.choon.careerbee.domain.competition.dto.internal.ProblemAnswerInfo;

public interface CompetitionProblemCustomRepository {

    List<ProblemAnswerInfo> getProblemAnswerInfoByCompetitionId(Long competitionId);

}
