package org.choon.careerbee.domain.store.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.common.dto.CommonResponse;
import org.choon.careerbee.common.dto.CommonResponseEntity;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.domain.auth.security.PrincipalDetails;
import org.choon.careerbee.domain.store.dto.request.TicketPurchaseReq;
import org.choon.careerbee.domain.store.dto.response.TicketInfoResp;
import org.choon.careerbee.domain.store.dto.response.TicketQuantityResp;
import org.choon.careerbee.domain.store.service.command.StoreCommandService;
import org.choon.careerbee.domain.store.service.query.StoreQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/")
public class StoreController {

    private final StoreCommandService commandService;
    private final StoreQueryService queryService;

    @PostMapping("tickets")
    public ResponseEntity<CommonResponse<Void>> purchaseTicket(
        @RequestBody TicketPurchaseReq ticketPurchaseReq,
        @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        commandService.purchaseTicket(
            ticketPurchaseReq, principalDetails.getId()
        );

        return CommonResponseEntity.ok(
            CustomResponseStatus.SUCCESS_WITH_NO_CONTENT,
            "티켓 구매에 성공하였습니다."
        );
    }

    @GetMapping("tickets/info")
    public ResponseEntity<CommonResponse<TicketInfoResp>> fetchTicketInfo() {
        TicketInfoResp response = queryService.fetchTicketInfo();

        return CommonResponseEntity.ok(
            response,
            CustomResponseStatus.SUCCESS,
            "티켓 정보 조회에 성공하였습니다."
        );
    }

    @GetMapping("tickets")
    public ResponseEntity<CommonResponse<TicketQuantityResp>> fetchTicketQuantity() {
        TicketQuantityResp response = queryService.fetchTicketQuantity();

        return CommonResponseEntity.ok(
            response,
            CustomResponseStatus.SUCCESS,
            "티켓 수량 조회에 성공하였습니다."
        );
    }

    @GetMapping("members/tickets")
    public ResponseEntity<CommonResponse<TicketQuantityResp>> fetchTicketQuantity(
        @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        log.info("here?");
        TicketQuantityResp response = queryService.fetchMemberTicketQuantity(
            principalDetails.getId());

        return CommonResponseEntity.ok(
            response,
            CustomResponseStatus.SUCCESS,
            "회원 티켓 수량 조회에 성공하였습니다."
        );
    }
}
