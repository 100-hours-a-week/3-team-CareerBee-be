package org.choon.careerbee.domain.store.dto.response;

public record TicketInfoResp(
    TicketInfo redTicket,
    TicketInfo greenTicket,
    TicketInfo blueTicket
) {

    public record TicketInfo(
        String prizeName,
        String prizeImgUrl
    ) {

    }
}
