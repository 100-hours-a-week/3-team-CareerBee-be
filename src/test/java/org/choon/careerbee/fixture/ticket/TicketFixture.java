package org.choon.careerbee.fixture.ticket;

import org.choon.careerbee.domain.store.domain.Ticket;
import org.choon.careerbee.domain.store.domain.enums.TicketType;

public class TicketFixture {


    public static Ticket createTicket(
        Integer price, Integer quantity, String prizeName, String imgUrl, TicketType type
    ) {
        return Ticket.of(price, quantity, prizeName, imgUrl, type);
    }
}
