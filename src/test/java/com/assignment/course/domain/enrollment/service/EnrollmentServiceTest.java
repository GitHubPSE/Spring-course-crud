package com.assignment.course.domain.enrollment.service;

import com.assignment.course.domain.course.dto.CourseDto;
import com.assignment.course.domain.course.entity.Course;
import com.assignment.course.domain.course.service.CourseService;
import com.assignment.course.domain.enrollment.dto.EnrollmentDto;
import com.assignment.course.domain.enrollment.entity.Enrollment;
import com.assignment.course.domain.enrollment.repository.EnrollmentRepository;
import com.assignment.course.global.exception.GlobalException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class EnrollmentServiceTest {

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    private Long openCourseNo;

    @BeforeEach
    void setUp() throws Exception {
        CourseDto.Response course = courseService.createCourse("kim_creat1", createCourseRequest(3));
        courseService.updateCourseStatus("kim_creat1", course.getCourseNo(), createStatusRequest(Course.Status.OPEN));
        openCourseNo = course.getCourseNo();
    }

    // ==================== enroll ====================

    @Test
    void 수강_신청_성공_PENDING() {
        EnrollmentDto.Response response = enrollmentService.enroll("jung_class1", createEnrollRequest(openCourseNo));

        assertThat(response.getStatus()).isEqualTo(Enrollment.Status.PENDING);
        assertThat(response.getUserId()).isEqualTo("jung_class1");
        assertThat(response.getCourseNo()).isEqualTo(openCourseNo);
    }

    @Test
    void 정원_초과_시_대기열_등록() {
        enrollAndConfirm("jung_class1", openCourseNo);
        enrollAndConfirm("choi_class1", openCourseNo);
        enrollAndConfirm("kang_class1", openCourseNo);

        EnrollmentDto.Response response = enrollmentService.enroll("yoon_class1", createEnrollRequest(openCourseNo));

        assertThat(response.getStatus()).isEqualTo(Enrollment.Status.WAITLISTED);
        assertThat(response.getWaitlistOrder()).isEqualTo(1);
    }

    @Test
    void 강사는_수강_신청_불가() {
        assertThatThrownBy(() -> enrollmentService.enroll("kim_creat1", createEnrollRequest(openCourseNo)))
                .isInstanceOf(GlobalException.class)
                .hasMessage("수강생만 이용할 수 있습니다.");
    }

    @Test
    void OPEN_상태_아닌_강의_신청_불가() throws Exception {
        CourseDto.Response course = courseService.createCourse("kim_creat1", createCourseRequest(3));
        courseService.updateCourseStatus("kim_creat1", course.getCourseNo(), createStatusRequest(Course.Status.OPEN));
        courseService.updateCourseStatus("kim_creat1", course.getCourseNo(), createStatusRequest(Course.Status.CLOSED));

        assertThatThrownBy(() -> enrollmentService.enroll("jung_class1", createEnrollRequest(course.getCourseNo())))
                .isInstanceOf(GlobalException.class)
                .hasMessage("수강 신청이 불가능한 강의입니다.");
    }

    @Test
    void 중복_수강_신청_불가() {
        enrollmentService.enroll("jung_class1", createEnrollRequest(openCourseNo));

        assertThatThrownBy(() -> enrollmentService.enroll("jung_class1", createEnrollRequest(openCourseNo)))
                .isInstanceOf(GlobalException.class)
                .hasMessage("이미 신청한 강의입니다.");
    }

    // ==================== confirm ====================

    @Test
    void 결제_확정_성공_PENDING_to_CONFIRMED() {
        EnrollmentDto.Response enrollment = enrollmentService.enroll("jung_class1", createEnrollRequest(openCourseNo));

        EnrollmentDto.Response response = enrollmentService.confirm("jung_class1", enrollment.getEnrollmentNo());

        assertThat(response.getStatus()).isEqualTo(Enrollment.Status.CONFIRMED);
        assertThat(response.getConfirmedDate()).isNotNull();
    }

    @Test
    void PENDING_아닌_상태에서_결제_확정_불가() {
        EnrollmentDto.Response enrollment = enrollmentService.enroll("jung_class1", createEnrollRequest(openCourseNo));
        enrollmentService.confirm("jung_class1", enrollment.getEnrollmentNo());

        assertThatThrownBy(() -> enrollmentService.confirm("jung_class1", enrollment.getEnrollmentNo()))
                .isInstanceOf(GlobalException.class)
                .hasMessage("수강 확정 상태가 아닙니다.");
    }

    // ==================== cancel ====================

    @Test
    void 수강_취소_성공() {
        EnrollmentDto.Response enrollment = enrollmentService.enroll("jung_class1", createEnrollRequest(openCourseNo));
        enrollmentService.confirm("jung_class1", enrollment.getEnrollmentNo());

        EnrollmentDto.Response response = enrollmentService.cancel("jung_class1", enrollment.getEnrollmentNo());

        assertThat(response.getStatus()).isEqualTo(Enrollment.Status.CANCELLED);
        assertThat(response.getCancelledDate()).isNotNull();
    }

    @Test
    void 취소_시_대기자_자동_PENDING_전환() {
        enrollAndConfirm("jung_class1", openCourseNo);
        enrollAndConfirm("choi_class1", openCourseNo);
        Long confirmNo = enrollAndConfirm("kang_class1", openCourseNo);
        EnrollmentDto.Response waitlisted = enrollmentService.enroll("yoon_class1", createEnrollRequest(openCourseNo));

        assertThat(waitlisted.getStatus()).isEqualTo(Enrollment.Status.WAITLISTED);

        enrollmentService.cancel("kang_class1", confirmNo);

        Enrollment updated = enrollmentRepository.findById(waitlisted.getEnrollmentNo()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(Enrollment.Status.PENDING);
    }

    // ==================== getMyEnrollments ====================

    @Test
    void 내_수강_신청_목록_조회() {
        enrollmentService.enroll("jung_class1", createEnrollRequest(openCourseNo));

        Page<EnrollmentDto.Response> result = enrollmentService.getMyEnrollments("jung_class1", PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isGreaterThanOrEqualTo(1);
        assertThat(result.getContent()).allMatch(e -> e.getUserId().equals("jung_class1"));
    }

    // ==================== getEnrollmentsByCourse ====================

    @Test
    void 강의별_수강생_목록_조회_강사_전용() {
        enrollAndConfirm("jung_class1", openCourseNo);
        enrollAndConfirm("choi_class1", openCourseNo);

        var result = enrollmentService.getEnrollmentsByCourse("kim_creat1", openCourseNo);

        assertThat(result).hasSize(2);
    }

    @Test
    void 수강생은_강의별_수강생_목록_조회_불가() {
        assertThatThrownBy(() -> enrollmentService.getEnrollmentsByCourse("jung_class1", openCourseNo))
                .isInstanceOf(GlobalException.class)
                .hasMessage("강사만 이용할 수 있습니다.");
    }

    // ==================== 헬퍼 메서드 ====================

    private Long enrollAndConfirm(String userId, Long courseNo) {
        EnrollmentDto.Response enrollment = enrollmentService.enroll(userId, createEnrollRequest(courseNo));
        EnrollmentDto.Response confirmed = enrollmentService.confirm(userId, enrollment.getEnrollmentNo());
        return confirmed.getEnrollmentNo();
    }

    private EnrollmentDto.CreateRequest createEnrollRequest(Long courseNo) {
        try {
            EnrollmentDto.CreateRequest request = new EnrollmentDto.CreateRequest();
            setField(request, "courseNo", courseNo);
            return request;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private CourseDto.CreateRequest createCourseRequest(int maxCapacity) throws Exception {
        CourseDto.CreateRequest request = new CourseDto.CreateRequest();
        setField(request, "title", "테스트 강의");
        setField(request, "description", "테스트 설명");
        setField(request, "price", 10000);
        setField(request, "maxCapacity", maxCapacity);
        setField(request, "startDate", LocalDateTime.now().plusDays(1));
        setField(request, "endDate", LocalDateTime.now().plusDays(30));
        return request;
    }

    private CourseDto.UpdateRequest createStatusRequest(Course.Status status) throws Exception {
        CourseDto.UpdateRequest request = new CourseDto.UpdateRequest();
        setField(request, "status", status);
        return request;
    }

    private void setField(Object obj, String fieldName, Object value) throws Exception {
        var field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }
}
