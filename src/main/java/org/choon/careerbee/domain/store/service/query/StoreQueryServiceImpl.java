package org.choon.careerbee.domain.store.service.query;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.common.exception.CustomException;
import org.choon.careerbee.domain.store.domain.Ticket;
import org.choon.careerbee.domain.store.domain.enums.TicketType;
import org.choon.careerbee.domain.store.dto.response.TicketInfoResp;
import org.choon.careerbee.domain.store.dto.response.TicketInfoResp.TicketInfo;
import org.choon.careerbee.domain.store.dto.response.TicketQuantityResp;
import org.choon.careerbee.domain.store.repository.PurchaseHistoryRepository;
import org.choon.careerbee.domain.store.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
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

    @Override
    public Ticket findTicketByType(TicketType ticketType) {
        return ticketRepository.findTicketByType(ticketType)
            .orElseThrow(() -> new CustomException(CustomResponseStatus.TICKET_NOT_EXIST));
    }

    @Override
    public TicketInfoResp fetchTicketInfo() {
        List<Ticket> tickets = ticketRepository.findAll();

        Map<TicketType, TicketInfo> ticketInfoMap = tickets.stream()
            .collect(Collectors.toMap(
                Ticket::getType,
                ticket -> new TicketInfo(ticket.getPrizeName(), ticket.getImgUrl())
            ));

        return new TicketInfoResp(
            ticketInfoMap.get(TicketType.RED),
            ticketInfoMap.get(TicketType.GREEN),
            ticketInfoMap.get(TicketType.BLUE)
        );
    }

}
