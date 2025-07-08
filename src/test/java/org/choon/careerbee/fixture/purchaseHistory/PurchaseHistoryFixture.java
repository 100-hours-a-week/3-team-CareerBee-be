package org.choon.careerbee.fixture.purchaseHistory;

import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.domain.store.domain.PurchaseHistory;
import org.choon.careerbee.domain.store.domain.Ticket;

public class PurchaseHistoryFixture {

    public static PurchaseHistory createPurchaseHistory(Member member, Ticket ticket) {
        return PurchaseHistory.of(member, ticket);
    }

}
