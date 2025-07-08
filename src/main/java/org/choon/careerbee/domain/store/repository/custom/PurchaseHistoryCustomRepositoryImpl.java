package org.choon.careerbee.domain.store.repository.custom;

import static org.choon.careerbee.domain.store.domain.QPurchaseHistory.purchaseHistory;
import static org.choon.careerbee.domain.store.domain.QTicket.ticket;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.choon.careerbee.domain.store.domain.enums.TicketType;
import org.choon.careerbee.domain.store.dto.response.TicketQuantityResp;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PurchaseHistoryCustomRepositoryImpl implements
    PurchaseHistoryCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public TicketQuantityResp fetchMemberTicketQuantity(Long accessMemberId) {
        List<Tuple> result = queryFactory
            .select(ticket.type, purchaseHistory.count())
            .from(purchaseHistory)
            .join(purchaseHistory.ticket, ticket)
            .where(purchaseHistory.member.id.eq(accessMemberId))
            .groupBy(ticket.type)
            .fetch();

        int red = 0, green = 0, blue = 0;

        for (Tuple tuple : result) {
            TicketType type = tuple.get(ticket.type);
            Long count = tuple.get(purchaseHistory.count());

            if (type == TicketType.RED) {
                red = count.intValue();
            } else if (type == TicketType.GREEN) {
                green = count.intValue();
            } else if (type == TicketType.BLUE) {
                blue = count.intValue();
            }
        }

        return new TicketQuantityResp(red, green, blue);
    }
}
