package org.choon.careerbee.domain.competition.service;

public interface CompetitionCommandService {

    void joinCompetition(Long competitionId, Long accessMemberId);
}
