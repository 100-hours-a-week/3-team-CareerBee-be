package org.choon.careerbee.domain.competition.service.query;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.common.exception.CustomException;
import org.choon.careerbee.domain.competition.dto.response.CompetitionIdResp;
import org.choon.careerbee.domain.competition.dto.response.CompetitionParticipationResp;
import org.choon.careerbee.domain.competition.dto.response.CompetitionProblemResp;
import org.choon.careerbee.domain.competition.dto.response.CompetitionRankingResp;
import org.choon.careerbee.domain.competition.dto.response.LiveRankingResp;
import org.choon.careerbee.domain.competition.dto.response.MemberLiveRankingResp;
import org.choon.careerbee.domain.competition.dto.response.MemberRankingResp;
import org.choon.careerbee.domain.competition.repository.CompetitionParticipantRepository;
import org.choon.careerbee.domain.competition.repository.CompetitionRepository;
import org.choon.careerbee.domain.competition.repository.CompetitionResultRepository;
import org.choon.careerbee.domain.competition.repository.CompetitionSummaryRepository;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CompetitionQueryServiceImpl implements CompetitionQueryService {

    private final CompetitionRepository competitionRepository;
    private final CompetitionParticipantRepository competitionParticipantRepository;
    private final CompetitionSummaryRepository competitionSummaryRepository;
    private final CompetitionResultRepository competitionResultRepository;

    @Override
    public CompetitionParticipationResp checkCompetitionParticipationById(
        Long competitionId, Long accessMemberId
    ) {
        if (!competitionRepository.existsById(competitionId)) {
            throw new CustomException(CustomResponseStatus.COMPETITION_NOT_EXIST);
        }

        return new CompetitionParticipationResp(
            competitionParticipantRepository.existsByMemberIdAndCompetitionId(
                accessMemberId, competitionId)
        );
    }

    @Override
    public CompetitionProblemResp fetchProblems(Long competitionId) {
        if (!competitionRepository.existsById(competitionId)) {
            throw new CustomException(CustomResponseStatus.COMPETITION_NOT_EXIST);
        }

        return competitionRepository.fetchCompetitionProblemsByCompetitionId(competitionId);
    }

    @Override
    public CompetitionRankingResp fetchRankings(LocalDate today) {
        return competitionSummaryRepository.fetchRankings(today);
    }

    @Override
    public CompetitionIdResp fetchCompetitionIdBy(LocalDate today) {
        return competitionRepository.fetchCompetitionIdFromToday(today);
    }

    @Override
    public MemberRankingResp fetchMemberCompetitionRankingById(
        Long accessMemberId, LocalDate today
    ) {
        return competitionSummaryRepository.fetchMemberRankingById(accessMemberId, today);
    }

    @Override
    public MemberLiveRankingResp fetchMemberLiveRanking(
        Long accessMemberId, LocalDate today
    ) {
        return competitionResultRepository.fetchMemberLiveRankingByDate(
            accessMemberId, today
        ).orElse(null);
    }

    @Override
    public LiveRankingResp fetchLiveRanking(LocalDate today) {
        return competitionResultRepository.fetchLiveRankingByDate(today);
    }
}
