package org.choon.careerbee.common.api;

import io.swagger.v3.oas.annotations.Operation;
import org.choon.careerbee.common.dto.CommonResponse;
import org.choon.careerbee.common.dto.CommonResponseEntity;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SystemController {

    @Operation(
        summary = "헬스체크용 요청",
        description = "헬스체크를 위해 사용되는 요청입니다.",
        tags = {"System"}
    )
    @GetMapping("/health-check")
    public ResponseEntity<CommonResponse<String>> healthCheck() {
        return CommonResponseEntity.ok(
            "Healthy",
            CustomResponseStatus.SUCCESS,
            "헬스체크에 성공하였습니다."
        );
    }

    @GetMapping("/favicon.ico")
    public ResponseEntity<CommonResponse<Void>> faviconHandle() {
        return CommonResponseEntity.ok(
            CustomResponseStatus.SUCCESS_WITH_NO_CONTENT,
            "헬스체크에 성공하였습니다."
        );
    }
}
