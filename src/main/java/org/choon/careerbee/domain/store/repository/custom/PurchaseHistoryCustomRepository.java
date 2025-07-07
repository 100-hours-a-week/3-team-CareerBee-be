package org.choon.careerbee.domain.store.repository.custom;

import org.choon.careerbee.domain.store.dto.response.TicketQuantityResp;

public interface PurchaseHistoryCustomRepository {

    TicketQuantityResp fetchMemberTicketQuantity(Long accessMemberId);

}
