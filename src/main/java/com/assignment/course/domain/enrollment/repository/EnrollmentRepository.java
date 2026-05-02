package com.assignment.course.domain.enrollment.repository;

import com.assignment.course.domain.enrollment.entity.Enrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    Page<Enrollment> findByUserId(String userId, Pageable pageable);

    int countByCourseNoAndStatus(Long courseNo, Enrollment.Status status);

    List<Enrollment> findByCourseNoAndStatus(Long courseNo, Enrollment.Status status);

    Optional<Enrollment> findFirstByCourseNoAndStatusOrderByWaitlistOrderAsc(Long courseNo, Enrollment.Status status);

    boolean existsByCourseNoAndUserIdAndStatusNot(Long courseNo, String userId, Enrollment.Status status);

    // 30분 이상 지난 PENDING 신청 조회 (자동 취소용)
    List<Enrollment> findByStatusAndCreatedDateBefore(Enrollment.Status status, LocalDateTime dateTime);

    // CONFIRMED + PENDING 합산 인원 조회 (정원 체크용)
    int countByCourseNoAndStatusIn(Long courseNo, List<Enrollment.Status> statuses);
}
