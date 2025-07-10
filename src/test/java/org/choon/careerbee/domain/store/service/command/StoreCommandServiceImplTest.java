package org.choon.careerbee.domain.store.service.command;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.common.exception.CustomException;
import org.choon.careerbee.domain.store.domain.enums.TicketType;
import org.choon.careerbee.domain.store.dto.request.TicketPurchaseReq;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

@ExtendWith(MockitoExtension.class)
class StoreCommandServiceImplTest {

    @InjectMocks
    private StoreCommandServiceImpl storeCommandService;

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private StorePurchaseProcessor storePurchaseProcessor;

    @Mock
    private RLock rLock;

    private static final Long MEMBER_ID = 1L;
    private static final TicketPurchaseReq REQUEST = new TicketPurchaseReq(TicketType.RED);

    @Test
    @DisplayName("[티켓 구매] 락 획득 성공 시 구매 로직이 실행된다")
    void purchaseTicket_success() throws InterruptedException {
        // given
        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        when(rLock.tryLock(2, 5, TimeUnit.SECONDS)).thenReturn(true);

        // when
        storeCommandService.purchaseTicket(REQUEST, MEMBER_ID);

        // then
        verify(storePurchaseProcessor, times(1)).executePurchase(REQUEST, MEMBER_ID);
        verify(rLock, times(1)).unlock();
    }

    @Test
    @DisplayName("[티켓 구매] 락 획득 실패 시 예외를 던진다")
    void purchaseTicket_lockFail_throwsException() throws InterruptedException {
        // given
        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        when(rLock.tryLock(2, 5, TimeUnit.SECONDS)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> storeCommandService.purchaseTicket(REQUEST, MEMBER_ID))
            .isInstanceOf(CustomException.class)
            .hasMessageContaining(CustomResponseStatus.LOCK_ACQUISITION_FAILED.getMessage());

        verify(storePurchaseProcessor, never()).executePurchase(any(), any());
        verify(rLock, never()).unlock();
    }

    @Test
    @DisplayName("[티켓 구매] 락 대기 중 인터럽트가 발생하면 예외를 던진다")
    void purchaseTicket_interrupted_throwsException() throws InterruptedException {
        // given
        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        when(rLock.tryLock(2, 5, TimeUnit.SECONDS)).thenThrow(new InterruptedException());

        // when & then
        assertThatThrownBy(() -> storeCommandService.purchaseTicket(REQUEST, MEMBER_ID))
            .isInstanceOf(CustomException.class)
            .hasMessageContaining(CustomResponseStatus.LOCK_ACQUISITION_FAILED.getMessage());

        verify(storePurchaseProcessor, never()).executePurchase(any(), any());
        verify(rLock, never()).unlock();
    }
}
