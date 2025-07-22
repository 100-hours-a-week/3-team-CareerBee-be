package org.choon.careerbee.util.lock;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.common.exception.CustomException;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DistributedLockExecutor {

    private final RedissonClient redissonClient;

    public <T> T execute(
        String lockKey, long waitTime, long leaseTime, TimeUnit unit, Supplier<T> action
    ) {
        RLock lock = redissonClient.getLock(lockKey);
        boolean acquired = false;
        try {
            acquired = lock.tryLock(waitTime, leaseTime, unit);
            if (!acquired) {
                log.warn("LOCK 획득 실패: {}", lockKey);
                return null;
            }
            return action.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CustomException(CustomResponseStatus.FAILED_TO_ACQUIRE_DISTRIBUTED_LOCK);
        } catch (Exception e) {
            throw new CustomException(CustomResponseStatus.FAILED_TO_EXECUTE_DISTRIBUTED_LOCK);
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    public void execute(String lockKey, long waitTime, long leaseTime, TimeUnit unit,
        Runnable action) {
        execute(lockKey, waitTime, leaseTime, unit, () -> {
            action.run();
            return null;
        });
    }
}
