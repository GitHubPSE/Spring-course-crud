package com.assignment.course.domain.enrollment.dto;

import com.assignment.course.domain.enrollment.entity.Enrollment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

public class EnrollmentDto {

    @Getter
    public static class CreateRequest {
        private Long courseNo;
    }

    @Getter
    @Builder
    public static class Response {
        private Long enrollmentNo;
        private Long courseNo;
        private String userId;
        private Enrollment.Status status;
        private LocalDateTime confirmedDate;
        private LocalDateTime cancelledDate;
        private Integer waitlistOrder;
        private LocalDateTime createdDate;
    }
}
