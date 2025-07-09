package org.choon.careerbee.domain.store.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.choon.careerbee.domain.store.domain.enums.TicketType.RED;
import static org.choon.careerbee.fixture.MemberFixture.createMember;
import static org.choon.careerbee.fixture.ticket.TicketFixture.createTicket;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.domain.member.repository.MemberRepository;
import org.choon.careerbee.domain.member.service.MemberQueryService;
import org.choon.careerbee.domain.store.domain.Ticket;
import org.choon.careerbee.domain.store.dto.request.TicketPurchaseReq;
import org.choon.careerbee.domain.store.repository.PurchaseHistoryRepository;
import org.choon.careerbee.domain.store.repository.TicketRepository;
import org.choon.careerbee.domain.store.service.command.StoreCommandService;
import org.choon.careerbee.domain.store.service.query.StoreQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class TicketConcurrencyTest {

    @Autowired
    private StoreCommandService storeCommandService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private PurchaseHistoryRepository purchaseHistoryRepository;

    @Autowired
    private MemberQueryService memberQueryService;

    @Autowired
    private StoreQueryService storeQueryService;

    private Ticket ticket;

    @Test
    @DisplayName("재고 100개일 때 100명이 동시 구매하면 재고가 0이 되어야한다.")
    void 재고_100개에_100명이_동시_구매하면_재고() throws InterruptedException {
        int initialStock = 100;
        int requestCount = 100;

        // given
        ticket = createTicket(initialStock, 100, "무뜨", "test.url", RED);
        ticketRepository.save(ticket);

        ExecutorService executorService = Executors.newFixedThreadPool(5);
        CountDownLatch latch = new CountDownLatch(requestCount);
        TicketPurchaseReq request = new TicketPurchaseReq(ticket.getType());

        for (long i = 0; i < requestCount; i++) {
            final long idx = i;
            executorService.execute(() -> {
                try {
                    Member m = createMember("nick" + idx, "email" + idx + "@test.com", idx);
                    m.plusPoint(200_000);
                    memberRepository.saveAndFlush(m);

                    storeCommandService.purchaseTicket(request, m.getId());
                } catch (Exception e) {
                    System.out.println("예외 발생: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Ticket updated = ticketRepository.findById(ticket.getId()).orElseThrow();
        long successCount = purchaseHistoryRepository.count();

        System.out.println("남은 재고 = " + updated.getQuantity());
        System.out.println("구매 성공 수 = " + successCount);

        assertThat(updated.getQuantity()).isEqualTo(0);
        assertThat(successCount).isEqualTo(initialStock);
    }

}
