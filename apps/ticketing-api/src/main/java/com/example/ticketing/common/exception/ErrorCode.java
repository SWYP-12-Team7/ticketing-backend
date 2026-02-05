package com.example.ticketing.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "잘못된 입력입니다"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다"),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 유저입니다"),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다"),
    NAME_ALREADY_SET(HttpStatus.BAD_REQUEST, "이름은 변경할 수 없습니다"),
    INVALID_NICKNAME(HttpStatus.BAD_REQUEST, "닉네임은 최소 2자, 최대 7자 이내여야 합니다."),

    // Exhibition
    EXHIBITION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 전시입니다"),

    // Curation
    CURATION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 행사입니다"),

    // Favorite
    ALREADY_FAVORITED(HttpStatus.CONFLICT, "이미 찜한 행사입니다"),
    FAVORITE_NOT_FOUND(HttpStatus.NOT_FOUND, "찜하지 않은 행사입니다"),

    // Folder
    FOLDER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 폴더입니다"),
    FOLDER_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "폴더는 최대 10개까지 생성할 수 있습니다"),
    DUPLICATE_FOLDER_NAME(HttpStatus.CONFLICT, "이미 존재하는 폴더 이름입니다"),

    // Auth
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다"),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다"),
    KAKAO_AUTH_FAILED(HttpStatus.BAD_REQUEST, "카카오 인증에 실패했습니다"),
    ;

    private final HttpStatus status;
    private final String message;
}
