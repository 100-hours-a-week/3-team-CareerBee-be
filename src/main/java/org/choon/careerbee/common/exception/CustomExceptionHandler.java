package org.choon.careerbee.common.exception;

import io.sentry.Sentry;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.AccessDeniedException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.common.dto.CommonResponse;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class CustomExceptionHandler {

    /**
     * Valid 애너테이션의 유효성 검사를 통과하지 못한 경우 해당 컨트롤러에서 처리
     *
     * @param bindingResult : 유효성 검사에 실패한 값들이 Map 타입으로 들어옵니다.
     * @return : CustomException 처리에 맞게 처리됩니다.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonResponse<Map<String, String>>> handleValidationException(
        BindingResult bindingResult) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(CommonResponse.createValidError(bindingResult));
    }

    /**
     * CustomException 및 Exception 을 처리하는 메서드입니다.
     *
     * @param e : 서버에서 발생한 에러입니다.
     * @return : 처리되지 못한(CustomException 이 아닌) 예외의 경우 내부서버 오류로 리턴, 그 외의 예외는 CERS에 의해서 처리됨
     */
    @ExceptionHandler({CustomException.class, Exception.class})
    public ResponseEntity<CommonResponse<String>> handleException(Exception e) {
        if (!(e instanceof CustomException custom)) {
            String stackTrace = getStackTraceAsString(e);
            log.error("[ERROR] : {}\n{}", e.getMessage(), stackTrace);

            // ✅ Sentry로 에러 전송
            Sentry.captureException(e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CommonResponse.createError(CustomResponseStatus.INTERNAL_SERVER_ERROR));
        }

        String stackTrace = getStackTraceAsString(e);
        log.error("[ERROR] : {}\n{}", e.getMessage(), stackTrace);
        return ResponseEntity.status(
                ((CustomException) e).getCustomResponseStatus().getHttpStatusCode())
            .body(CommonResponse.createError(custom.getCustomResponseStatus()));
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
        AuthorizationDeniedException e) {

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