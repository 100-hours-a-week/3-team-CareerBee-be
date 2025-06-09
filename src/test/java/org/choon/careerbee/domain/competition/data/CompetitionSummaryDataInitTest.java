package org.choon.careerbee.domain.competition.data;

import static org.choon.careerbee.fixture.MemberFixture.createMember;
import static org.choon.careerbee.fixture.competition.CompetitionFixture.createCompetition;
import static org.choon.careerbee.fixture.competition.CompetitionResultFixture.createCompetitionResult;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import org.choon.careerbee.domain.competition.domain.Competition;
import org.choon.careerbee.domain.competition.domain.CompetitionResult;
import org.choon.careerbee.domain.competition.dto.request.CompetitionResultSubmitReq;
import org.choon.careerbee.domain.competition.repository.CompetitionRepository;
import org.choon.careerbee.domain.competition.repository.CompetitionResultRepository;
import org.choon.careerbee.domain.competition.repository.CompetitionSummaryRepository;
import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Disabled("로컬 DB 초기 데이터 넣을 때만 실행")
public class CompetitionSummaryDataInitTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CompetitionRepository competitionRepository;

    @Autowired
    private CompetitionResultRepository competitionResultRepository;

    @Autowired
    private CompetitionSummaryRepository competitionSummaryRepository;

    @Test
    void initCompetitionResults() {

        /* 1) 멤버 9명 */
        List<Member> members = IntStream.rangeClosed(1, 10)
            .mapToObj(i -> memberRepository.save(
                createMember("testNick" + i, "test" + i + "@test.com", (long) i)))
            .toList();

        /* 2) 대회 8회 (6/2 - 6/8, 18:00-18:10) */
        Competition competition0 = competitionRepository.save(
            createCompetition(
                LocalDateTime.of(2025, 6, 8, 13, 0),
                LocalDateTime.of(2025, 6, 8, 13, 10)));

        Competition competition = competitionRepository.save(
            createCompetition(
                LocalDateTime.of(2025, 6, 9, 13, 0),
                LocalDateTime.of(2025, 6, 9, 13, 10)));

        Competition competition1 = competitionRepository.save(
            createCompetition(
                LocalDateTime.of(2025, 6, 10, 13, 0),
                LocalDateTime.of(2025, 6, 10, 13, 10)));

        /* 3) CompetitionResult 삽입 */
        List<Integer> solvedList0 = List.of(3, 3, 3, 4, 4, 5, 5, 2, 1, 2);
        List<Integer> elapsedList0 = List.of(240000, 240050, 190000, 300000, 310000, 400000, 420000,
            200000, 100000, 200003);

        List<Integer> solvedList = List.of(3, 3, 3, 4, 4, 5, 5, 2, 1, 2);
        List<Integer> elapsedList = List.of(240000, 240050, 190000, 300000, 310000, 400000, 420000,
            200000, 100000, 200003);

        List<Integer> solvedList2 = List.of(2, 4, 4, 5, 2, 3, 2, 4, 3, 4);
        List<Integer> elapsedList2 = List.of(180000, 300000, 310000, 240000, 200000, 300000, 210000,
            400000, 320000, 314000);

        List<CompetitionResult> results = new ArrayList<>();
        for (int i = 0; i < members.size(); i++) {
            Member member = members.get(i);
            int solved0 = solvedList0.get(i);
            int elapsed0 = elapsedList0.get(i);

            int solved = solvedList.get(i);
            int elapsed = elapsedList.get(i);

            int solved2 = solvedList2.get(i);
            int elapsed2 = elapsedList2.get(i);

            CompetitionResultSubmitReq submitReq0 = new CompetitionResultSubmitReq((short) solved0,
                elapsed0);
            CompetitionResult result0 = createCompetitionResult(competition, member, submitReq0);

            CompetitionResultSubmitReq submitReq = new CompetitionResultSubmitReq((short) solved,
                elapsed);
            CompetitionResult result = createCompetitionResult(competition, member, submitReq);

            CompetitionResultSubmitReq submitReq2 = new CompetitionResultSubmitReq((short) solved2,
                elapsed2);
            CompetitionResult result2 = createCompetitionResult(competition, member, submitReq2);
            results.add(result0);
            results.add(result);
            results.add(result2);
        }
        competitionResultRepository.saveAll(results);
    }
}
