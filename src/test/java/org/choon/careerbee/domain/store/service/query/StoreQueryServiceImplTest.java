package org.choon.careerbee.domain.store.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.choon.careerbee.fixture.ticket.TicketFixture.createTicket;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.choon.careerbee.domain.store.domain.Ticket;
import org.choon.careerbee.domain.store.domain.enums.TicketType;
import org.choon.careerbee.domain.store.dto.response.TicketQuantityResp;
import org.choon.careerbee.domain.store.repository.PurchaseHistoryRepository;
import org.choon.careerbee.domain.store.repository.TicketRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StoreQueryServiceImplTest {

    @InjectMocks
    private StoreQueryServiceImpl storeQueryService;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private PurchaseHistoryRepository purchaseHistoryRepository;


    @Test
    @DisplayName("[티켓 수량 조회] 티켓 수량 조회시 DB 조회")
    void fetchTicketQuantity_shouldCacheResult() {
        // given
        List<Ticket> expectedTickets = List.of(
            Ticket.of(1000, 3, "무뜨", "red.png", TicketType.RED),
            Ticket.of(1200, 5, "요아정", "green.png", TicketType.GREEN),
            Ticket.of(1500, 7, "하겐다즈", "blue.png", TicketType.BLUE)
        );

        when(ticketRepository.findAll()).thenReturn(expectedTickets);

        // when
        TicketQuantityResp actualTicketList = storeQueryService.fetchTicketQuantity();

        // then
        verify(ticketRepository, times(1)).findAll();
        assertThat(actualTicketList.redCount()).isEqualTo(3);
        assertThat(actualTicketList.greenCount()).isEqualTo(5);
        assertThat(actualTicketList.blueCount()).isEqualTo(7);
    }

    @Test
    @DisplayName("[회원별 티켓 수량 조회] 특정 회원이 구매한 RED, GREEN, BLUE 수량을 반환한다")
    void fetchMemberTicketQuantity_shouldReturnCorrectTicketCounts() {
        // given
        Long memberId = 1L;
        TicketQuantityResp expectedResp = new TicketQuantityResp(2, 5, 7);

        when(purchaseHistoryRepository.fetchMemberTicketQuantity(memberId))
            .thenReturn(expectedResp);

        // when
        TicketQuantityResp actualResp = storeQueryService.fetchMemberTicketQuantity(memberId);

        // then
        verify(purchaseHistoryRepository, times(1)).fetchMemberTicketQuantity(memberId);
        assertThat(actualResp.redCount()).isEqualTo(2);
        assertThat(actualResp.greenCount()).isEqualTo(5);
        assertThat(actualResp.blueCount()).isEqualTo(7);
    }

    @Test
    @DisplayName("[티켓 단건 조회] TicketType에 해당하는 티켓이 존재하면 반환한다")
    void findTicketByType_shouldReturnTicket_whenTicketExists() {
        // given
        TicketType ticketType = TicketType.RED;
        Ticket expectedTicket = Ticket.of(1000, 10, "무뜨", "red.png", ticketType);
        when(ticketRepository.findTicketByType(ticketType)).thenReturn(
            java.util.Optional.of(expectedTicket));

        // when
        Ticket actualTicket = storeQueryService.findTicketByType(ticketType);

        // then
        verify(ticketRepository, times(1)).findTicketByType(ticketType);
        assertThat(actualTicket).isEqualTo(expectedTicket);
    }

    @Test
    @DisplayName("[티켓 단건 조회] TicketType에 해당하는 티켓이 없으면 예외를 발생시킨다")
    void findTicketByType_shouldThrowException_whenTicketNotExists() {
        // given
        TicketType ticketType = TicketType.RED;
        when(ticketRepository.findTicketByType(ticketType)).thenReturn(java.util.Optional.empty());

        // when & then
        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                storeQueryService.findTicketByType(ticketType))
            .isInstanceOf(org.choon.careerbee.common.exception.CustomException.class)
            .hasMessageContaining("티켓 정보가 존재하지 않습니다.");

        verify(ticketRepository, times(1)).findTicketByType(ticketType);
    }

    @Test
    @DisplayName("[티켓 정보 조회] 티켓 정보 조회 시 RED, GREEN, BLUE 티켓 정보를 반환한다")
    void fetchTicketInfo_shouldReturnCorrectTicketInfo() {
        // given
        List<Ticket> expectedTickets = List.of(
            createTicket(1000, 3, "무뜨", "red.png", TicketType.RED),
            createTicket(1200, 5, "요아정", "green.png", TicketType.GREEN),
            createTicket(1500, 7, "하겐다즈", "blue.png", TicketType.BLUE)
        );

        when(ticketRepository.findAll()).thenReturn(expectedTickets);

        // when
        var actualResp = storeQueryService.fetchTicketInfo();

        // then
        verify(ticketRepository, times(1)).findAll();

        assertThat(actualResp.redTicket()).isNotNull();
        assertThat(actualResp.redTicket().prizeName()).isEqualTo("무뜨");
        assertThat(actualResp.redTicket().prizeImgUrl()).isEqualTo("red.png");

        assertThat(actualResp.greenTicket().prizeImgUrl()).isNotNull();
        assertThat(actualResp.greenTicket().prizeName()).isEqualTo("요아정");
        assertThat(actualResp.greenTicket().prizeImgUrl()).isEqualTo("green.png");

        assertThat(actualResp.blueTicket().prizeImgUrl()).isNotNull();
        assertThat(actualResp.blueTicket().prizeName()).isEqualTo("하겐다즈");
        assertThat(actualResp.blueTicket().prizeImgUrl()).isEqualTo("blue.png");
    }

    @Test
    @DisplayName("[티켓 정보 조회] 특정 티켓이 없는 경우 null을 반환한다")
    void fetchTicketInfo_shouldReturnNullForMissingTypes() {
        // given
        List<Ticket> partialTickets = List.of(
            Ticket.of(1000, 3, "무뜨", "red.png", TicketType.RED),
            Ticket.of(1200, 5, "요아정", "green.png", TicketType.GREEN)
            // BLUE 없음
        );

        when(ticketRepository.findAll()).thenReturn(partialTickets);

        // when
        var actualResp = storeQueryService.fetchTicketInfo();

        // then
        verify(ticketRepository, times(1)).findAll();

        assertThat(actualResp.redTicket()).isNotNull();
        assertThat(actualResp.greenTicket()).isNotNull();
        assertThat(actualResp.blueTicket()).isNull();
    }
}
