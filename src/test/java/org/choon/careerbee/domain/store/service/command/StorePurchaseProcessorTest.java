package org.choon.careerbee.domain.store.service.command;

import static org.choon.careerbee.fixture.MemberFixture.createMember;
import static org.choon.careerbee.fixture.ticket.TicketFixture.createTicket;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.domain.member.service.MemberQueryService;
import org.choon.careerbee.domain.store.domain.PurchaseHistory;
import org.choon.careerbee.domain.store.domain.Ticket;
import org.choon.careerbee.domain.store.domain.enums.TicketType;
import org.choon.careerbee.domain.store.dto.request.TicketPurchaseReq;
import org.choon.careerbee.domain.store.repository.PurchaseHistoryRepository;
import org.choon.careerbee.domain.store.service.query.StoreQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StorePurchaseProcessorTest {

    @InjectMocks
    private StorePurchaseProcessor storePurchaseProcessor;

    @Mock
    private MemberQueryService memberQueryService;

    @Mock
    private StoreQueryService storeQueryService;

    @Mock
    private PurchaseHistoryRepository purchaseHistoryRepository;

    @Test
    @DisplayName("[티켓 구매 처리] 포인트 차감, 티켓 차감, 구매내역 저장이 실행된다")
    void executePurchase_shouldProcessCorrectly() {
        // given
        Long memberId = 1L;
        TicketPurchaseReq request = new TicketPurchaseReq(TicketType.RED);

        Member mockMember = createMember("testNick", "test@test.com", 3452L);
        Ticket mockTicket = createTicket(5, 10, "test.jpg", TicketType.RED);

        mockMember.plusPoint(10);
        when(memberQueryService.findById(memberId)).thenReturn(mockMember);
        when(storeQueryService.findTicketByType(TicketType.RED)).thenReturn(mockTicket);

        // when
        storePurchaseProcessor.executePurchase(request, memberId);

        // then
        verify(memberQueryService).findById(memberId);
        verify(storeQueryService).findTicketByType(TicketType.RED);
        verify(purchaseHistoryRepository, times(1)).save(any(PurchaseHistory.class));
    }
}
