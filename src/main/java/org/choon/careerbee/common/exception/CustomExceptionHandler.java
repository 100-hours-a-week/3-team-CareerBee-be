package org.choon.careerbee.common.exception;

import io.sentry.Sentry;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.AccessDeniedException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.common.dto.CommonResponse;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.domain.auth.service.cookie.CookieService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class CustomExceptionHandler {

    private final CookieService cookieService;

    /**
     * Valid 애너테이션의 유효성 검사를 통과하지 못한 경우 해당 컨트롤러에서 처리
     *
     * @param bindingResult : 유효성 검사에 실패한 값들이 Map 타입으로 들어옵니다.
     * @return : CustomException 처리에 맞게 처리됩니다.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonResponse<Map<String, String>>> handleValidationException(
        BindingResult bindingResult
    ) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(CommonResponse.createValidError(bindingResult));
    }

    @ExceptionHandler({
        MethodArgumentTypeMismatchException.class,
        MissingPathVariableException.class,
        MissingServletRequestParameterException.class,
        MissingRequestHeaderException.class,
        MissingRequestCookieException.class
    })
    public ResponseEntity<CommonResponse<String>> handleTypeMismatch(Exception e) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(CommonResponse.createError(CustomResponseStatus.INVALID_INPUT_VALUE));
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<CommonResponse<String>> handleCustom(
        CustomException ex, HttpServletResponse resp
    ) {
        log.error("[ERROR] : {}\n{}", ex.getMessage(), getStackTraceAsString(ex));

        Sentry.captureException(ex);

        switch (ex.getCustomResponseStatus()) {
            case REFRESH_TOKEN_EXPIRED, REFRESH_TOKEN_NOT_FOUND ->
                cookieService.deleteRefreshTokenCookie(resp);
        }

        return ResponseEntity
            .status(ex.getCustomResponseStatus().getHttpStatusCode())
            .body(CommonResponse.createError(ex.getCustomResponseStatus()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponse<String>> handleUnknown(
        Exception ex, HttpServletResponse resp
    ) {
        log.error("[UNCAUGHT ERROR] : {}\n{}", ex.getMessage(), getStackTraceAsString(ex));

        Sentry.captureException(ex);

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(CommonResponse.createError(CustomResponseStatus.INTERNAL_SERVER_ERROR));
    }

    /**
     * 인증에 대한 Exception 을 처리하는 메서드입니다.
     *
     * @param e : 인증 흐름에 있어서 발생한 에러입니다.
     * @return : CERS에 맞게 처리됩니다.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<CommonResponse<String>> handleAccessDeniedException(CustomException e) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(CommonResponse.createError(e.getCustomResponseStatus()));
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<CommonResponse<String>> handleAuthorizationDeniedException(
        AuthorizationDeniedException e
    ) {

        // ✅ Sentry로 에러 전송
        Sentry.captureException(e);

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(CommonResponse.createError(CustomResponseStatus.ACCESS_DENIED));
    }

    private String getStackTraceAsString(Throwable e) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        return stringWriter.toString();
    }
}
