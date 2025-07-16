package org.choon.careerbee.common.async;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class FutureStorage {

    private final Map<FutureType, Map<Long, CompletableFuture<?>>> futureMap = new ConcurrentHashMap<>();

    public <T> void put(FutureType type, Long memberId, CompletableFuture<T> future) {
        futureMap
            .computeIfAbsent(type, k -> new ConcurrentHashMap<>())
            .put(memberId, future);
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<CompletableFuture<T>> get(FutureType type, Long memberId) {
        Map<Long, CompletableFuture<?>> map = futureMap.get(type);
        if (map == null) {
            return Optional.empty();
        }
        return Optional.ofNullable((CompletableFuture<T>) map.get(memberId));
    }

    public void remove(FutureType type, Long memberId) {
        Map<Long, CompletableFuture<?>> map = futureMap.get(type);
        if (map != null) {
            map.remove(memberId);
            if (map.isEmpty()) {
                futureMap.remove(type);
            }
        }
    }
}
