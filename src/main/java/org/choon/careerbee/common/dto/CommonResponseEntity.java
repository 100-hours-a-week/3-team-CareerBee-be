package org.choon.careerbee.common.dto;

import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.springframework.http.ResponseEntity;

public class CommonResponseEntity {

    public static <T> ResponseEntity<CommonResponse<T>> ok(
        T data,
        CustomResponseStatus status,
        String message
    ) {
        return ResponseEntity
            .status(status.getHttpStatusCode())
            .body(CommonResponse.createSuccessWithMessage(data, status, message));
    }

    public static ResponseEntity<CommonResponse<Void>> ok(
        CustomResponseStatus status,
        String message
    ) {
        return ResponseEntity
            .status(status.getHttpStatusCode())
            .body(CommonResponse.createSuccessWithNoContent(status, message));
    }

    public static ResponseEntity<CommonResponse<String>> error(
        CustomResponseStatus status
    ) {
        return ResponseEntity
            .status(status.getHttpStatusCode())
            .body(CommonResponse.createError(status));
    }
}
