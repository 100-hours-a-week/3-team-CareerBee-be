package org.choon.careerbee.common.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum CustomResponseStatus {
    SUCCESS(HttpStatus.OK.value(), "요청에 성공하였습니다."),
    SUCCESS_WITH_NO_CONTENT(HttpStatus.NO_CONTENT.value(), "요청에 성공하였습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN.value(), "권한이 없습니다."),

    MEMBER_NOT_EXIST(HttpStatus.NOT_FOUND.value(), "존재하지 않는 회원입니다."),

    INVALID_LATITUDE_ERROR(HttpStatus.BAD_REQUEST.value(), "위도를 정확하게 입력해주세요 : 34~44"),
    INVALID_LONGITUDE_ERROR(HttpStatus.BAD_REQUEST.value(), "경도를 정확하게 입력해주세요 : 124~134"),
    COMPANY_NOT_EXIST(HttpStatus.NOT_FOUND.value(), "존재하지 않는 기업입니다."),
    WISH_ALREADY_EXIST(HttpStatus.CONFLICT.value(), "이미 관심기업에 등록되어 있습니다."),
    WISH_COMPANY_NOT_FOUND(HttpStatus.CONFLICT.value(), "존재하지 않는 관심기업 입니다."),

    OAUTH_PROVIDER_NOT_EXIST(HttpStatus.NOT_FOUND.value(), "존재하지 않는 OAuth Provider 입니다."),

    BAD_JWT(HttpStatus.BAD_REQUEST.value(), "잘못된 토큰입니다."),
    EXPIRED_JWT(HttpStatus.UNAUTHORIZED.value(), "만료된 토큰입니다."),
    NULL_JWT(HttpStatus.BAD_REQUEST.value(), "토큰이 공백입니다."),

    LOGOUT_MEMBER(HttpStatus.NOT_FOUND.value(), "로그아웃 되었습니다. 다시 로그인을 진행해주세요."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED.value(), "리프레시 토큰이 만료되었습니다."),
    REFRESH_TOKEN_NOT_MATCH(HttpStatus.CONFLICT.value(), "잘못된 리프레시 토큰입니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "유효한 리프레시 토큰이 존재하지 않습니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST.value(), "유효하지 않은 입력값 입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "내부 서버 오류입니다."),
    ;

    private final int httpStatusCode;
    private String message;

    CustomResponseStatus(int httpStatusCode, String message) {
        this.httpStatusCode = httpStatusCode;
        this.message = message;
    }

    public CustomResponseStatus withMessage(String customMessage) {
        this.message = customMessage;
        return this;
    }
}
