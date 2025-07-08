package org.choon.careerbee.domain.notification.repository.custom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.choon.careerbee.fixture.MemberFixture.createMember;
import static org.choon.careerbee.fixture.ticket.TicketFixture.createTicket;

import org.choon.careerbee.config.querydsl.QueryDSLConfig;
import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.domain.member.repository.MemberRepository;
import org.choon.careerbee.domain.store.domain.PurchaseHistory;
import org.choon.careerbee.domain.store.domain.Ticket;
import org.choon.careerbee.domain.store.domain.enums.TicketType;
import org.choon.careerbee.domain.store.dto.response.TicketQuantityResp;
import org.choon.careerbee.domain.store.repository.PurchaseHistoryRepository;
import org.choon.careerbee.domain.store.repository.TicketRepository;
import org.choon.careerbee.domain.store.repository.custom.PurchaseHistoryCustomRepositoryImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@Import({QueryDSLConfig.class, PurchaseHistoryCustomRepositoryImpl.class})
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
class PurchaseHistoryCustomRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private PurchaseHistoryRepository purchaseHistoryRepository;

    @Test
    @DisplayName("회원별 티켓 구매 수량 조회 - 티켓 타입별로 수량이 올바르게 집계됨")
    void fetchMemberTicketQuantity_success() {
        // given
        Member member = memberRepository.save(createMember("ticketUser", "ticket@test.com", 77L));

        Ticket red = ticketRepository.save(createTicket(1000, 1, "red.png", TicketType.RED));
        Ticket green = ticketRepository.save(createTicket(1200, 1, "green.png", TicketType.GREEN));
        Ticket blue = ticketRepository.save(createTicket(1500, 1, "blue.png", TicketType.BLUE));

        purchaseHistoryRepository.save(PurchaseHistory.of(member, red));
        purchaseHistoryRepository.save(PurchaseHistory.of(member, red));
        purchaseHistoryRepository.save(PurchaseHistory.of(member, green));
        purchaseHistoryRepository.save(PurchaseHistory.of(member, blue));
        purchaseHistoryRepository.save(PurchaseHistory.of(member, blue));
        purchaseHistoryRepository.save(PurchaseHistory.of(member, blue));

        // when
        TicketQuantityResp result = purchaseHistoryRepository.fetchMemberTicketQuantity(
            member.getId());

        // then
        assertThat(result.redCount()).isEqualTo(2);
        assertThat(result.greenCount()).isEqualTo(1);
        assertThat(result.blueCount()).isEqualTo(3);
    }
}
