package org.choon.careerbee.domain.store.service.query;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.domain.store.repository.TicketRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StoreQueryServiceImpl implements StoreQueryService {

    private final TicketRepository ticketRepository;

}
