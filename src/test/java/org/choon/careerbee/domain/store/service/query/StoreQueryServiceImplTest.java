package org.choon.careerbee.domain.store.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.choon.careerbee.domain.store.domain.Ticket;
import org.choon.careerbee.domain.store.domain.enums.TicketType;
import org.choon.careerbee.domain.store.dto.response.TicketQuantityResp;
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

    @Test
    @DisplayName("[티켓 수량 조회] 티켓 수량 조회시 DB 조회")
    void fetchTicketQuantity_shouldCacheResult() {
        // given
        List<Ticket> expectedTickets = List.of(
            Ticket.of(1000, 3, "red.png", TicketType.RED),
            Ticket.of(1200, 5, "green.png", TicketType.GREEN),
            Ticket.of(1500, 7, "blue.png", TicketType.BLUE)
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
}
