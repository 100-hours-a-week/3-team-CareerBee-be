package org.choon.careerbee.domain.store.service.command;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.common.exception.CustomException;
import org.choon.careerbee.domain.store.dto.request.TicketPurchaseReq;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StoreCommandServiceImpl implements StoreCommandService {

    private static final String TICKET_LOCK_KEY = "lock:ticket:";
    private final RedissonClient redissonClient;
    private final StorePurchaseProcessor storePurchaseProcessor;

    @Override
    public void purchaseTicket(TicketPurchaseReq ticketPurchaseReq, Long accessMemberId) {
        String lockKey = TICKET_LOCK_KEY + ticketPurchaseReq.ticketType();
        RLock lock = redissonClient.getLock(lockKey);
        boolean isLocked = false;

        try {
            isLocked = lock.tryLock(2, 5, TimeUnit.SECONDS);
            if (!isLocked) {
                throw new CustomException(CustomResponseStatus.LOCK_ACQUISITION_FAILED);
            }

            storePurchaseProcessor.executePurchase(ticketPurchaseReq, accessMemberId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CustomException(CustomResponseStatus.LOCK_ACQUISITION_FAILED);
        } finally {
            if (isLocked) {
                lock.unlock();
            }
        }
    }

}
