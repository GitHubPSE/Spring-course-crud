package com.assignment.course.domain.enrollment.controller;

import com.assignment.course.domain.enrollment.dto.EnrollmentDto;
import com.assignment.course.domain.enrollment.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    // 수강 신청 (수강생 전용)
    @PostMapping
    public ResponseEntity<EnrollmentDto.Response> enroll(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody EnrollmentDto.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(enrollmentService.enroll(userId, request));
    }

    // 결제 확정 (PENDING → CONFIRMED)
    @PostMapping("/{enrollmentNo}/confirm")
    public ResponseEntity<EnrollmentDto.Response> confirm(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable Long enrollmentNo) {
        return ResponseEntity.ok(enrollmentService.confirm(userId, enrollmentNo));
    }

    // 수강 취소 (확정 후 7일 이내)
    @PostMapping("/{enrollmentNo}/cancel")
    public ResponseEntity<EnrollmentDto.Response> cancel(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable Long enrollmentNo) {
        return ResponseEntity.ok(enrollmentService.cancel(userId, enrollmentNo));
    }

    // 내 수강 신청 목록 조회 (페이지네이션)
    @GetMapping("/my")
    public ResponseEntity<Page<EnrollmentDto.Response>> getMyEnrollments(
            @RequestHeader("X-User-Id") String userId,
            Pageable pageable) {
        return ResponseEntity.ok(enrollmentService.getMyEnrollments(userId, pageable));
    }

    // 강의별 수강생 목록 조회 (강사 전용)
    @GetMapping("/courses/{courseNo}")
    public ResponseEntity<List<EnrollmentDto.Response>> getEnrollmentsByCourse(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable Long courseNo) {
        return ResponseEntity.ok(enrollmentService.getEnrollmentsByCourse(userId, courseNo));
    }
}
