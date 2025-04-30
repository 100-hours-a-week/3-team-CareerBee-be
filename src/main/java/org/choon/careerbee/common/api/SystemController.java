package org.choon.careerbee.common.api;

import org.choon.careerbee.common.dto.ApiResponse;
import org.choon.careerbee.common.dto.ApiResponseEntity;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SystemController {

  @GetMapping("/")
  public ResponseEntity<ApiResponse<String>> healthCheck() {
    return ApiResponseEntity.ok(
        "Healthy",
        CustomResponseStatus.SUCCESS
    );
  }
}
