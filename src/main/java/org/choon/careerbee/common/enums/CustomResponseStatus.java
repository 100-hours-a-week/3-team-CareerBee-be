package org.choon.careerbee.common.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum CustomResponseStatus {
    SUCCESS(HttpStatus.OK.value(), "요청에 성공하였습니다."),
    SUCCESS_WITH_NO_CONTENT(HttpStatus.NO_CONTENT.value(), "요청에 성공하였습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN.value(), "권한이 없습니다."),

    DUPLICATE_REQUEST(HttpStatus.TOO_MANY_REQUESTS.value(), "중복된 요청입니다. 잠시 후 다시 시도해주세요."),
    LOCK_ACQUISITION_FAILED(HttpStatus.TOO_MANY_REQUESTS.value(),
        "현재 요청이 많아 처리할 수 없습니다. 잠시 후 다시 시도해주세요."),

    INVALID_LOGIN_LOGIC(HttpStatus.INTERNAL_SERVER_ERROR.value(), "비정상적인 로그인 로직이 감지되었습니다."),
    MEMBER_NOT_EXIST(HttpStatus.NOT_FOUND.value(), "존재하지 않는 회원입니다."),
    EMAIL_ALREADY_EXIST(HttpStatus.CONFLICT.value(), "이미 존재하는 이메일입니다."),
    MEMBER_ALREADY_WITHDRAWAL(HttpStatus.CONFLICT.value(), "이미 탈퇴한 회원입니다."),
    WITHDRAWAL_MEMBER(HttpStatus.GONE.value(), "탈퇴한 회원입니다."),
    NOTIFICATION_UPDATE_INVALID(HttpStatus.BAD_REQUEST.value(), "일부 알림은 존재하지 않거나 접근 권한이 없습니다."),
    NOT_ENOUGH_POINT(HttpStatus.BAD_REQUEST.value(), "포인트가 부족합니다."),

    INVALID_LATITUDE_ERROR(HttpStatus.BAD_REQUEST.value(), "위도를 정확하게 입력해주세요 : 34~44"),
    INVALID_LONGITUDE_ERROR(HttpStatus.BAD_REQUEST.value(), "경도를 정확하게 입력해주세요 : 124~134"),
    COMPANY_NOT_EXIST(HttpStatus.NOT_FOUND.value(), "존재하지 않는 기업입니다."),
    WISH_ALREADY_EXIST(HttpStatus.CONFLICT.value(), "이미 관심기업에 등록되어 있습니다."),
    WISH_COMPANY_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "존재하지 않는 관심기업 입니다."),

    COMPETITION_NOT_EXIST(HttpStatus.NOT_FOUND.value(), "존재하지 않는 대회입니다."),
    COMPETITION_ALREADY_JOIN(HttpStatus.CONFLICT.value(), "이미 참여한 대회입니다."),
    RESULT_ALREADY_SUBMIT(HttpStatus.CONFLICT.value(), "이미 제출하였습니다."),
    RANKING_NOT_EXIST(HttpStatus.NOT_FOUND.value(), "대회 랭킹 정보가 존재하지 않습니다."),

    TICKET_NOT_EXIST(HttpStatus.NOT_FOUND.value(), "티켓 정보가 존재하지 않습니다."),
    TICKET_OUT_OF_STOCK(HttpStatus.BAD_REQUEST.value(), "해당 티켓이 품절되었습니다."),

    OAUTH_PROVIDER_NOT_EXIST(HttpStatus.NOT_FOUND.value(), "존재하지 않는 OAuth Provider 입니다."),
    EXTENSION_NOT_EXIST(HttpStatus.BAD_REQUEST.value(), "제공하지 않는 확장자입니다."),

    INTERVIEW_PROBLEM_NOT_EXIST(HttpStatus.NOT_FOUND.value(), "존재하지 않는 면접문제 입니다."),
    SOLVED_INTERVIEW_PROBLEM_NOT_EXIST(HttpStatus.NOT_FOUND.value(), "풀지않은 문제입니다."),
    INTERVIEW_PROBLEM_ALREADY_SAVED(HttpStatus.CONFLICT.value(), "이미 저장한 문제입니다."),
    INTERVIEW_PROBLEM_ALREADY_UNSAVED(HttpStatus.CONFLICT.value(), "저장안된 문제입니다."),

    BAD_JWT(HttpStatus.BAD_REQUEST.value(), "잘못된 토큰입니다."),
    EXPIRED_JWT(HttpStatus.UNAUTHORIZED.value(), "만료된 토큰입니다."),
    NULL_JWT(HttpStatus.BAD_REQUEST.value(), "토큰이 공백입니다."),

    LOGOUT_MEMBER(HttpStatus.NOT_FOUND.value(), "로그아웃 되었습니다. 다시 로그인을 진행해주세요."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED.value(), "리프레시 토큰이 만료되었습니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "유효한 리프레시 토큰이 존재하지 않습니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST.value(), "유효하지 않은 입력값 입니다."),
    JSON_PARSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "JSON 파싱 과정중 오류 발생"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "내부 서버 오류입니다."),
    AI_INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "AI 서버 오류입니다."),
    SARAMIN_INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "사람인 서버 오류입니다."),
    SARAMIN_API_KEY_EMPTY_ERROR(HttpStatus.BAD_REQUEST.value(), "사람인 API 요청시 액세스키 누락시 발생한 에러입니다."),
    SARAMIN_API_KEY_INVALID_ERROR(HttpStatus.BAD_REQUEST.value(),
        "사람인 API 요청시 유효한지 않은 액세스키로 발생 에러입니다."),
    SARAMIN_INVALID_PARAM_ERROR(HttpStatus.BAD_REQUEST.value(),
        "사람인 API 요청시 유효한지 않은 파라미터로 발생 에러입니다."),
    SARAMIN_TOO_MANY_REQUEST_ERROR(HttpStatus.TOO_MANY_REQUESTS.value(),
        "사람인 API의 일일 요청 횟수 초과하였습니다."),

    FAILED_TO_ACQUIRE_DISTRIBUTED_LOCK(HttpStatus.CONFLICT.value(), "분산 락 획득에 실패했습니다."),
    FAILED_TO_EXECUTE_DISTRIBUTED_LOCK(HttpStatus.INTERNAL_SERVER_ERROR.value(),
        "분산 락 실행 중 예외가 발생했습니다."),

    ALREADY_SOLVED_FREE_PROBLEM(HttpStatus.BAD_REQUEST.value(), "이미 풀이한 무료 문제입니다."),
    ALREADY_SOLVED_PAY_PROBLEM(HttpStatus.BAD_REQUEST.value(), "오늘 하루에 풀 수 있는 유료 문제를 모두 푸셨습니다."),
    ALREADY_SOLVED_PROBLEM(HttpStatus.BAD_REQUEST.value(), "이미 풀이한 문제입니다."),
    ALREADY_HAS_SOLVE_CHANCE(HttpStatus.BAD_REQUEST.value(), "아직 무료 풀이 기회가 남아있습니다.");

    private final int httpStatusCode;
    private String message;

    CustomResponseStatus(int httpStatusCode, String message) {
        this.httpStatusCode = httpStatusCode;
        this.message = message;
    }

}
