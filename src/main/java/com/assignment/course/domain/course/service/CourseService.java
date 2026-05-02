package com.assignment.course.domain.course.service;

import com.assignment.course.domain.course.dto.CourseDto;
import com.assignment.course.domain.course.entity.Course;
import com.assignment.course.domain.course.repository.CourseRepository;
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
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;

    // 강의 등록 (강사 전용)
    @Transactional
    public CourseDto.Response createCourse(String userId, CourseDto.CreateRequest request) {
        // 유저 조회 → 강사 권한 확인
        User user = userRepository.findByUserIdAndDelDateIsNull(userId)
                .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));

        if (user.getRole() != User.Role.CREATOR) {
            throw new GlobalException(ErrorCode.NOT_CREATOR);
        }

        // 강의 엔티티 생성 (초기 상태 DRAFT)
        Course course = Course.builder()
                .userId(userId)
                .title(request.getTitle())
                .description(request.getDescription())
                .price(request.getPrice())
                .maxCapacity(request.getMaxCapacity())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();

        // 저장 후 반환
        courseRepository.save(course);

        return toResponse(course, 0);
    }

    // 강의 목록 조회 (상태 필터, 페이지네이션)
    public Page<CourseDto.Response> getCourses(Course.Status status, Pageable pageable) {
        // status 있으면 상태별 조회, 없으면 전체 조회
        Page<Course> courses = (status != null)
                ? courseRepository.findByStatusAndDelDateIsNull(status, pageable)
                : courseRepository.findByDelDateIsNull(pageable);

        // 각 강의의 현재 CONFIRMED + PENDING 인원 조회 후 반환
        return courses.map(course -> {
            int currentEnrollment = enrollmentRepository.countByCourseNoAndStatusIn(
                    course.getCourseNo(), java.util.List.of(Enrollment.Status.CONFIRMED, Enrollment.Status.PENDING));
            return toResponse(course, currentEnrollment);
        });
    }

    // 강의 상세 조회 (현재 신청 인원 포함)
    public CourseDto.Response getCourse(Long courseNo) {
        // 강의 조회 (삭제된 강의 제외)
        Course course = courseRepository.findByCourseNoAndDelDateIsNull(courseNo)
                .orElseThrow(() -> new GlobalException(ErrorCode.COURSE_NOT_FOUND));

        // 현재 CONFIRMED + PENDING 인원 조회
        int currentEnrollment = enrollmentRepository.countByCourseNoAndStatusIn(
                courseNo, java.util.List.of(Enrollment.Status.CONFIRMED, Enrollment.Status.PENDING));

        return toResponse(course, currentEnrollment);
    }

    // 강의 상태 변경 (강사 본인만 가능)
    @Transactional
    public CourseDto.Response updateCourseStatus(String userId, Long courseNo, CourseDto.UpdateRequest request) {
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

        // 상태 전환 유효성 검사 (DRAFT → OPEN → CLOSED)
        validateStatusTransition(course.getStatus(), request.getStatus());

        // 상태 변경
        course.updateStatus(request.getStatus());

        int currentEnrollment = enrollmentRepository.countByCourseNoAndStatusIn(
                courseNo, java.util.List.of(Enrollment.Status.CONFIRMED, Enrollment.Status.PENDING));

        return toResponse(course, currentEnrollment);
    }

    private void validateStatusTransition(Course.Status current, Course.Status next) {
        if (current == Course.Status.DRAFT && next == Course.Status.OPEN) return;
        if (current == Course.Status.OPEN && next == Course.Status.CLOSED) return;
        throw new GlobalException(ErrorCode.COURSE_INVALID_STATUS);
    }

    private CourseDto.Response toResponse(Course course, int currentEnrollment) {
        return CourseDto.Response.builder()
                .courseNo(course.getCourseNo())
                .title(course.getTitle())
                .description(course.getDescription())
                .price(course.getPrice())
                .maxCapacity(course.getMaxCapacity())
                .currentEnrollment(currentEnrollment)
                .startDate(course.getStartDate())
                .endDate(course.getEndDate())
                .status(course.getStatus())
                .userId(course.getUserId())
                .createdDate(course.getCreatedDate())
                .build();
    }
}
