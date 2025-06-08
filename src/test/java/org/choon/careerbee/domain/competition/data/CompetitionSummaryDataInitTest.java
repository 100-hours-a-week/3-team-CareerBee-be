package org.choon.careerbee.domain.competition.data;

import static org.choon.careerbee.fixture.MemberFixture.createMember;
import static org.choon.careerbee.fixture.competition.CompetitionFixture.createCompetition;
import static org.choon.careerbee.fixture.competition.CompetitionResultFixture.createCompetitionResult;

import jakarta.transaction.Transactional;
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
        List<Member> members = IntStream.rangeClosed(1, 9)
            .mapToObj(i -> memberRepository.save(
                createMember("testNick" + i, "test" + i + "@test.com", (long) i)))
            .toList();

        /* 2) 대회 8회 (6/2 - 6/8, 18:00-18:10) */
        List<Competition> comps = IntStream.rangeClosed(2, 8)
            .mapToObj(d -> competitionRepository.save(
                createCompetition(
                    LocalDateTime.of(2025, 6, d, 18, 0),
                    LocalDateTime.of(2025, 6, d, 18, 10))))
            .toList();

        /* 3) CompetitionResult 삽입 ─ 공식: solved = (mIdx + cIdx) % 5 / elapsed = 300+m*10+c */
        List<CompetitionResult> results = new ArrayList<>();

        for (int ci = 0; ci < comps.size(); ci++) {
            Competition comp = comps.get(ci);
            int cIdx = ci + 2;                           // 2~9
            for (int mi = 0; mi < members.size(); mi++) {
                Member mem = members.get(mi);
                int mIdx = mi + 1;                       // 1~9

                short solved = (short) ((mIdx + cIdx) % 5);        // 0~4
                int elapsed = 300 + mIdx * 10 + cIdx;             // 312~399

                results.add(createCompetitionResult(
                    comp, mem, new CompetitionResultSubmitReq(solved, elapsed)
                ));
            }
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
