package org.choon.careerbee.domain.store.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.common.dto.CommonResponse;
import org.choon.careerbee.common.dto.CommonResponseEntity;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.domain.store.dto.response.TicketQuantityResp;
import org.choon.careerbee.domain.store.service.command.StoreCommandService;
import org.choon.careerbee.domain.store.service.query.StoreQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/")
public class StoreController {

    private final StoreCommandService commandService;
    private final StoreQueryService queryService;

    @GetMapping("/tickets")
    public ResponseEntity<CommonResponse<TicketQuantityResp>> fetchTicketQuantity() {
        TicketQuantityResp response = queryService.fetchTicketQuantity();

        return CommonResponseEntity.ok(
            response,
            CustomResponseStatus.SUCCESS,
            "티켓 수량 조회에 성공하였습니다."
        );
    }
}
