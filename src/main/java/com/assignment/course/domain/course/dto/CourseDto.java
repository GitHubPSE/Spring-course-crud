package com.assignment.course.domain.course.dto;

import com.assignment.course.domain.course.entity.Course;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

public class CourseDto {

    @Getter
    public static class CreateRequest {
        private String title;
        private String description;
        private int price;
        private int maxCapacity;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
    }

    @Getter
    public static class UpdateRequest {
        private Course.Status status;
    }

    @Getter
    @Builder
    public static class Response {
        private Long courseNo;
        private String title;
        private String description;
        private int price;
        private int maxCapacity;
        private int currentEnrollment;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private Course.Status status;
        private String userId;
        private LocalDateTime createdDate;
    }
}
