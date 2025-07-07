package org.choon.careerbee.domain.store.repository;

import org.choon.careerbee.domain.store.domain.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

}
