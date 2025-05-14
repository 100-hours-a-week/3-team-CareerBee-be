package org.choon.careerbee.common.api;

import org.choon.careerbee.common.dto.ApiResponseEntity;
import org.choon.careerbee.common.dto.CommonResponse;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SystemController {

    @GetMapping("/health-check")
    public ResponseEntity<CommonResponse<String>> healthCheck() {
        return ApiResponseEntity.ok(
            "Healthy",
            CustomResponseStatus.SUCCESS,
            "헬스체크에 성공하였습니다."
        );
    }
}
