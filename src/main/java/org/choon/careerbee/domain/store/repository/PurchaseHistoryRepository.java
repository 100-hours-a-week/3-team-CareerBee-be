package org.choon.careerbee.domain.store.repository;

import org.choon.careerbee.domain.store.domain.PurchaseHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseHistoryRepository extends JpaRepository<PurchaseHistory, Long> {

}
