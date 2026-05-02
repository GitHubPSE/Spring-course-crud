package com.assignment.course.domain.enrollment.scheduler;

import com.assignment.course.domain.enrollment.entity.Enrollment;
import com.assignment.course.domain.enrollment.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

// 수강 신청 자동 취소 스케줄러
@Component
@RequiredArgsConstructor
public class EnrollmentScheduler {

    private final EnrollmentRepository enrollmentRepository;

    // 5분마다 실행 - 30분 이상 PENDING 상태인 신청 자동 취소
    @Scheduled(fixedDelay = 300000)
    @Transactional
    public void cancelExpiredEnrollments() {
        LocalDateTime expiredTime = LocalDateTime.now().minusMinutes(30);

        // 30분 이상 지난 PENDING 신청 조회
        List<Enrollment> expiredEnrollments = enrollmentRepository
                .findByStatusAndCreatedDateBefore(Enrollment.Status.PENDING, expiredTime);

        for (Enrollment enrollment : expiredEnrollments) {
            // CANCELLED 처리
            enrollment.cancel();

            // 대기열 첫 번째 항목 → PENDING으로 자동 전환
            enrollmentRepository.findFirstByCourseNoAndStatusOrderByWaitlistOrderAsc(
                    enrollment.getCourseNo(), Enrollment.Status.WAITLISTED)
                    .ifPresent(waitlisted -> {
                        waitlisted.updateStatus(Enrollment.Status.PENDING);
                        // TODO: 대기자에게 수강 신청 가능 안내 문자 발송
                    });
        }
    }
}
