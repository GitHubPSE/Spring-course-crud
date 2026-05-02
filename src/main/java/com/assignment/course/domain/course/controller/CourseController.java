package com.assignment.course.domain.course.controller;

import com.assignment.course.domain.course.dto.CourseDto;
import com.assignment.course.domain.course.entity.Course;
import com.assignment.course.domain.course.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    // 강의 등록 (강사 전용)
    @PostMapping
    public ResponseEntity<CourseDto.Response> createCourse(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody CourseDto.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(courseService.createCourse(userId, request));
    }

    // 강의 목록 조회 (상태 필터, 페이지네이션)
    @GetMapping
    public ResponseEntity<Page<CourseDto.Response>> getCourses(
            @RequestParam(required = false) Course.Status status,
            Pageable pageable) {
        return ResponseEntity.ok(courseService.getCourses(status, pageable));
    }

    // 강의 상세 조회
    @GetMapping("/{courseNo}")
    public ResponseEntity<CourseDto.Response> getCourse(@PathVariable Long courseNo) {
        return ResponseEntity.ok(courseService.getCourse(courseNo));
    }

    // 강의 상태 변경 (강사 본인만 가능)
    @PatchMapping("/{courseNo}/status")
    public ResponseEntity<CourseDto.Response> updateCourseStatus(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable Long courseNo,
            @RequestBody CourseDto.UpdateRequest request) {
        return ResponseEntity.ok(courseService.updateCourseStatus(userId, courseNo, request));
    }
}
