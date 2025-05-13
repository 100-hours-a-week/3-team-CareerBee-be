package org.choon.careerbee.common.dto;

import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.springframework.http.ResponseEntity;

public class ApiResponseEntity {

    public static <T> ResponseEntity<ApiResponse<T>> ok(
        T data,
        CustomResponseStatus status,
        String message
    ) {
        return ResponseEntity
            .status(status.getHttpStatusCode())
            .body(ApiResponse.createSuccessWithMessage(data, status, message));
    }

    public static ResponseEntity<ApiResponse<Void>> ok(
        CustomResponseStatus status,
        String message
    ) {
        return ResponseEntity
            .status(status.getHttpStatusCode())
            .body(ApiResponse.createSuccessWithNoContent(status, message));
    }

    public static ResponseEntity<ApiResponse<String>> error(
        CustomResponseStatus status
    ) {
        return ResponseEntity
            .status(status.getHttpStatusCode())
            .body(ApiResponse.createError(status));
    }
}