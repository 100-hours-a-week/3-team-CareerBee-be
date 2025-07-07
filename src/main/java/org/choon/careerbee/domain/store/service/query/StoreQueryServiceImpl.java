package org.choon.careerbee.domain.store.service.query;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.domain.store.domain.Ticket;
import org.choon.careerbee.domain.store.domain.enums.TicketType;
import org.choon.careerbee.domain.store.dto.response.TicketQuantityResp;
import org.choon.careerbee.domain.store.repository.PurchaseHistoryRepository;
import org.choon.careerbee.domain.store.repository.TicketRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StoreQueryServiceImpl implements StoreQueryService {

    private final TicketRepository ticketRepository;
    private final PurchaseHistoryRepository purchaseHistoryRepository;

    @Override
    public TicketQuantityResp fetchTicketQuantity() {
        Map<TicketType, Integer> quantityMap = ticketRepository.findAll().stream()
            .collect(java.util.stream.Collectors.toMap(
                Ticket::getType,
                Ticket::getQuantity,
                (existing, replacement) -> existing
            ));

        return new TicketQuantityResp(
            quantityMap.getOrDefault(TicketType.RED, 0),
            quantityMap.getOrDefault(TicketType.GREEN, 0),
            quantityMap.getOrDefault(TicketType.BLUE, 0)
        );
    }

    @Override
    public TicketQuantityResp fetchMemberTicketQuantity(Long accessMemberId) {
        return purchaseHistoryRepository.fetchMemberTicketQuantity(accessMemberId);
    }
}
