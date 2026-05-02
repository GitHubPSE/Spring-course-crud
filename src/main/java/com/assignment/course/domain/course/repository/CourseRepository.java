package com.assignment.course.domain.course.repository;

import com.assignment.course.domain.course.entity.Course;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {

    Optional<Course> findByCourseNoAndDelDateIsNull(Long courseNo);

    // 수강 신청 시 비관적 락 (SELECT FOR UPDATE)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Course c WHERE c.courseNo = :courseNo AND c.delDate IS NULL")
    Optional<Course> findByIdWithLock(@Param("courseNo") Long courseNo);

    Page<Course> findByStatusAndDelDateIsNull(Course.Status status, Pageable pageable);

    Page<Course> findByDelDateIsNull(Pageable pageable);

    List<Course> findByUserIdAndDelDateIsNull(String userId);
}
