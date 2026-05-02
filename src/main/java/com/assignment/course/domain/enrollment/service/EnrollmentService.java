package com.assignment.course.domain.enrollment.service;

import com.assignment.course.domain.course.entity.Course;
import com.assignment.course.domain.course.repository.CourseRepository;
import com.assignment.course.domain.enrollment.dto.EnrollmentDto;
import com.assignment.course.domain.enrollment.entity.Enrollment;
import com.assignment.course.domain.enrollment.repository.EnrollmentRepository;
import com.assignment.course.domain.user.entity.User;
import com.assignment.course.domain.user.repository.UserRepository;
import com.assignment.course.global.exception.ErrorCode;
import com.assignment.course.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    // 수강 신청 (수강생 전용, 정원 초과 시 대기열)
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public EnrollmentDto.Response enroll(String userId, EnrollmentDto.CreateRequest request) {
        // 유저 조회 → 수강생 권한 확인
        User user = userRepository.findByUserIdAndDelDateIsNull(userId)
                .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));

        if (user.getRole() != User.Role.CLASSMATE) {
            throw new GlobalException(ErrorCode.NOT_CLASSMATE);
        }

        // 강의 조회 → OPEN 상태인지 확인 (비관적 락 - SELECT FOR UPDATE)
        Course course = courseRepository.findByIdWithLock(request.getCourseNo())
                .orElseThrow(() -> new GlobalException(ErrorCode.COURSE_NOT_FOUND));

        if (course.getStatus() != Course.Status.OPEN) {
            throw new GlobalException(ErrorCode.COURSE_NOT_OPEN);
        }

        // 중복 신청 여부 확인 (CANCELLED 제외)
        if (enrollmentRepository.existsByCourseNoAndUserIdAndStatusNot(request.getCourseNo(), userId, Enrollment.Status.CANCELLED)) {
            throw new GlobalException(ErrorCode.ENROLLMENT_ALREADY_EXISTS);
        }

        // 현재 CONFIRMED + PENDING 합산 인원 조회 (PENDING도 자리 차지)
        int occupiedCount = enrollmentRepository.countByCourseNoAndStatusIn(
                request.getCourseNo(), List.of(Enrollment.Status.CONFIRMED, Enrollment.Status.PENDING));

        Enrollment enrollment;
        if (occupiedCount < course.getMaxCapacity()) {
            // 정원 미달 → PENDING으로 신청
            enrollment = Enrollment.builder()
                    .courseNo(request.getCourseNo())
                    .userId(userId)
                    .status(Enrollment.Status.PENDING)
                    .waitlistOrder(null)
                    .build();
        } else {
            // 정원 초과 → WAITLISTED로 대기열 등록
            int waitlistCount = enrollmentRepository.countByCourseNoAndStatus(request.getCourseNo(), Enrollment.Status.WAITLISTED);
            enrollment = Enrollment.builder()
                    .courseNo(request.getCourseNo())
                    .userId(userId)
                    .status(Enrollment.Status.WAITLISTED)
                    .waitlistOrder(waitlistCount + 1)
                    .build();
        }

        // 저장 후 반환
        enrollmentRepository.save(enrollment);

        return toResponse(enrollment);
    }

    // 결제 확정 (PENDING → CONFIRMED)
    @Transactional
    public EnrollmentDto.Response confirm(String userId, Long enrollmentNo) {
        // 수강 신청 조회 → 본인 신청인지 확인
        Enrollment enrollment = enrollmentRepository.findById(enrollmentNo)
                .orElseThrow(() -> new GlobalException(ErrorCode.ENROLLMENT_NOT_FOUND));

        if (!enrollment.getUserId().equals(userId)) {
            throw new GlobalException(ErrorCode.ENROLLMENT_NOT_FOUND);
        }

        // 상태가 PENDING인지 확인
        if (enrollment.getStatus() != Enrollment.Status.PENDING) {
            throw new GlobalException(ErrorCode.ENROLLMENT_NOT_CONFIRMED);
        }

        // CONFIRMED로 상태 변경
        enrollment.confirm();

        return toResponse(enrollment);
    }

    // 수강 취소 (확정 후 7일 이내, 취소 시 대기자 자동 PENDING 전환)
    @Transactional
    public EnrollmentDto.Response cancel(String userId, Long enrollmentNo) {
        // 수강 신청 조회 → 본인 신청인지 확인
        Enrollment enrollment = enrollmentRepository.findById(enrollmentNo)
                .orElseThrow(() -> new GlobalException(ErrorCode.ENROLLMENT_NOT_FOUND));

        if (!enrollment.getUserId().equals(userId)) {
            throw new GlobalException(ErrorCode.ENROLLMENT_NOT_FOUND);
        }

        // 상태가 CONFIRMED인지 확인
        if (enrollment.getStatus() != Enrollment.Status.CONFIRMED) {
            throw new GlobalException(ErrorCode.ENROLLMENT_NOT_CONFIRMED);
        }

        // 확정 후 7일 이내인지 확인
        if (enrollment.getConfirmedDate().plusDays(7).isBefore(LocalDateTime.now())) {
            throw new GlobalException(ErrorCode.ENROLLMENT_CANCEL_EXPIRED);
        }

        // CANCELLED로 상태 변경
        enrollment.cancel();

        // 대기열 첫 번째 항목 → PENDING으로 자동 전환
        enrollmentRepository.findFirstByCourseNoAndStatusOrderByWaitlistOrderAsc(
                enrollment.getCourseNo(), Enrollment.Status.WAITLISTED)
                .ifPresent(waitlisted -> {
                    waitlisted.updateStatus(Enrollment.Status.PENDING);
                    // TODO: 대기자에게 수강 신청 가능 안내 문자 발송
                });

        return toResponse(enrollment);
    }

    // 내 수강 신청 목록 조회 (페이지네이션)
    public Page<EnrollmentDto.Response> getMyEnrollments(String userId, Pageable pageable) {
        // 유저 조회 → 수강생 권한 확인
        User user = userRepository.findByUserIdAndDelDateIsNull(userId)
                .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));

        if (user.getRole() != User.Role.CLASSMATE) {
            throw new GlobalException(ErrorCode.NOT_CLASSMATE);
        }

        // 내 수강 신청 목록 조회 후 반환
        return enrollmentRepository.findByUserId(userId, pageable)
                .map(this::toResponse);
    }

    // 강의별 수강생 목록 조회 (강사 전용)
    public List<EnrollmentDto.Response> getEnrollmentsByCourse(String userId, Long courseNo) {
        // 유저 조회 → 강사 권한 확인
        User user = userRepository.findByUserIdAndDelDateIsNull(userId)
                .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));

        if (user.getRole() != User.Role.CREATOR) {
            throw new GlobalException(ErrorCode.NOT_CREATOR);
        }

        // 강의 조회 → 본인 강의인지 확인
        Course course = courseRepository.findByCourseNoAndDelDateIsNull(courseNo)
                .orElseThrow(() -> new GlobalException(ErrorCode.COURSE_NOT_FOUND));

        if (!course.getUserId().equals(userId)) {
            throw new GlobalException(ErrorCode.COURSE_NOT_OWNER);
        }

        // 강의별 CONFIRMED 수강생 목록 조회 후 반환
        return enrollmentRepository.findByCourseNoAndStatus(courseNo, Enrollment.Status.CONFIRMED)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private EnrollmentDto.Response toResponse(Enrollment enrollment) {
        return EnrollmentDto.Response.builder()
                .enrollmentNo(enrollment.getEnrollmentNo())
                .courseNo(enrollment.getCourseNo())
                .userId(enrollment.getUserId())
                .status(enrollment.getStatus())
                .confirmedDate(enrollment.getConfirmedDate())
                .cancelledDate(enrollment.getCancelledDate())
                .waitlistOrder(enrollment.getWaitlistOrder())
                .createdDate(enrollment.getCreatedDate())
                .build();
    }
}
