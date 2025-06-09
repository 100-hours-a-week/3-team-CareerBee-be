package org.choon.careerbee.domain.competition.data;

import static org.choon.careerbee.fixture.MemberFixture.createMember;
import static org.choon.careerbee.fixture.competition.CompetitionFixture.createCompetition;
import static org.choon.careerbee.fixture.competition.CompetitionResultFixture.createCompetitionResult;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import org.choon.careerbee.domain.competition.domain.Competition;
import org.choon.careerbee.domain.competition.domain.CompetitionResult;
import org.choon.careerbee.domain.competition.domain.CompetitionSummary;
import org.choon.careerbee.domain.competition.domain.enums.SummaryType;
import org.choon.careerbee.domain.competition.dto.request.CompetitionResultSubmitReq;
import org.choon.careerbee.domain.competition.repository.CompetitionRepository;
import org.choon.careerbee.domain.competition.repository.CompetitionResultRepository;
import org.choon.careerbee.domain.competition.repository.CompetitionSummaryRepository;
import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.domain.member.repository.MemberRepository;
import org.choon.careerbee.fixture.competition.CompetitionSummaryFixture;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
//@Transactional
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
        Competition competition = competitionRepository.save(
            createCompetition(
                LocalDateTime.of(2025, 6, 9, 13, 0),
                LocalDateTime.of(2025, 6, 9, 13, 10)));

        /* 3) CompetitionResult 삽입 */
        List<Integer> solvedList = List.of(3, 3, 3, 4, 4, 5, 5, 2, 1, 2);
        List<Integer> elapsedList = List.of(240000, 240050, 190000, 300000, 310000, 400000, 420000,
            200000, 100000, 200003);

        List<CompetitionResult> results = new ArrayList<>();
        for (int i = 0; i < members.size(); i++) {
            Member member = members.get(i);
            int solved = solvedList.get(i);
            int elapsed = elapsedList.get(i);

            CompetitionResultSubmitReq submitReq = new CompetitionResultSubmitReq((short) solved,
                elapsed);
            CompetitionResult result = createCompetitionResult(competition, member, submitReq);
            results.add(result);
        }
        competitionResultRepository.saveAll(results);

        /* 4) 예시 Summary (월간)도 만들어 보고 싶다면 */
        LocalDate weekStart = LocalDate.of(2025, 6, 2);
        LocalDate weekEnd = LocalDate.of(2025, 6, 8);

        List<CompetitionSummary> weekSummaries = IntStream.rangeClosed(1, 7)
            .mapToObj(i -> CompetitionSummaryFixture.createSummary(
                members.get(i - 1),
                (short) (i * 3),              // solvedSum 예시
                i * 500L,                     // timeSum 예시
                (long) i,                            // rank 예시
                3,                            // maxStreak 예시
                0.75,                         // correctRate 예시
                SummaryType.WEEK,
                weekStart, weekEnd))
            .toList();

        LocalDate monthStart = LocalDate.of(2025, 6, 1);
        LocalDate monthEnd = LocalDate.of(2025, 6, 30);

        List<CompetitionSummary> monthSummaries = IntStream.rangeClosed(1, 7)
            .mapToObj(i -> CompetitionSummaryFixture.createSummary(
                members.get(i - 1),
                (short) (i * 3),              // solvedSum 예시
                i * 500L,                     // timeSum 예시
                (long) i,                            // rank 예시
                3,                            // maxStreak 예시
                0.75,                         // correctRate 예시
                SummaryType.MONTH,
                monthStart, monthEnd))
            .toList();

        competitionSummaryRepository.saveAll(weekSummaries);
        competitionSummaryRepository.saveAll(monthSummaries);
    }
}
