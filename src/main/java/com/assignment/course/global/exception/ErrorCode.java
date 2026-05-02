package com.assignment.course.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 유저
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 유저입니다."),
    NOT_CREATOR(HttpStatus.FORBIDDEN, "강사만 이용할 수 있습니다."),
    NOT_CLASSMATE(HttpStatus.FORBIDDEN, "수강생만 이용할 수 있습니다."),

    // 강의
    COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 강의입니다."),
    COURSE_NOT_OWNER(HttpStatus.FORBIDDEN, "본인 강의가 아닙니다."),
    COURSE_INVALID_STATUS(HttpStatus.BAD_REQUEST, "유효하지 않은 상태 전환입니다."),
    COURSE_NOT_OPEN(HttpStatus.BAD_REQUEST, "수강 신청이 불가능한 강의입니다."),

    // 동시성
    LOCK_TIMEOUT(HttpStatus.SERVICE_UNAVAILABLE, "현재 수강 신청이 몰려 있습니다. 잠시 후 다시 시도해주세요."),

    // 수강 신청
    ENROLLMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 수강 신청입니다."),
    ENROLLMENT_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 신청한 강의입니다."),
    ENROLLMENT_FULL(HttpStatus.CONFLICT, "수강 정원이 꽉 찼습니다."),
    ENROLLMENT_CANCEL_EXPIRED(HttpStatus.BAD_REQUEST, "취소 가능 기간(7일)이 지났습니다."),
    ENROLLMENT_NOT_CONFIRMED(HttpStatus.BAD_REQUEST, "수강 확정 상태가 아닙니다."),

;

    private final HttpStatus status;
    private final String message;
}
