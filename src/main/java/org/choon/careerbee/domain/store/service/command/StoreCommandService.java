package org.choon.careerbee.domain.store.service.command;

import org.choon.careerbee.domain.store.dto.request.TicketPurchaseReq;

public interface StoreCommandService {

    void purchaseTicket(TicketPurchaseReq ticketPurchaseReq, Long accessMemberId);
}
