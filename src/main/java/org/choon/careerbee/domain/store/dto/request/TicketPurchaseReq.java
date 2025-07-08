package org.choon.careerbee.domain.store.dto.request;

import org.choon.careerbee.domain.store.domain.enums.TicketType;

public record TicketPurchaseReq(
    TicketType ticketType
) {

}
