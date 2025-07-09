package org.choon.careerbee.domain.store.service.query;

import org.choon.careerbee.domain.store.domain.Ticket;
import org.choon.careerbee.domain.store.domain.enums.TicketType;
import org.choon.careerbee.domain.store.dto.response.TicketInfoResp;
import org.choon.careerbee.domain.store.dto.response.TicketQuantityResp;

public interface StoreQueryService {

    TicketQuantityResp fetchTicketQuantity();

    TicketQuantityResp fetchMemberTicketQuantity(Long accessMemberId);

    Ticket findTicketByType(TicketType ticketType);

    TicketInfoResp fetchTicketInfo();
}
