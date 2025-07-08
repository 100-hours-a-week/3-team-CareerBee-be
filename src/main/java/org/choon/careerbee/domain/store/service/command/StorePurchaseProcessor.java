package org.choon.careerbee.domain.store.service.command;

import lombok.RequiredArgsConstructor;
import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.domain.member.service.MemberQueryService;
import org.choon.careerbee.domain.store.domain.PurchaseHistory;
import org.choon.careerbee.domain.store.domain.Ticket;
import org.choon.careerbee.domain.store.dto.request.TicketPurchaseReq;
import org.choon.careerbee.domain.store.repository.PurchaseHistoryRepository;
import org.choon.careerbee.domain.store.service.query.StoreQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StorePurchaseProcessor {

    private final MemberQueryService memberQueryService;
    private final StoreQueryService storeQueryService;
    private final PurchaseHistoryRepository purchaseHistoryRepository;

    @Transactional
    public void executePurchase(TicketPurchaseReq ticketPurchaseReq, Long accessMemberId) {
        Member member = memberQueryService.findById(accessMemberId);
        Ticket ticket = storeQueryService.findTicketByType(ticketPurchaseReq.ticketType());

        member.minusPoint(ticket.getPrice());
        ticket.use();

        purchaseHistoryRepository.save(PurchaseHistory.of(member, ticket));
    }
}
