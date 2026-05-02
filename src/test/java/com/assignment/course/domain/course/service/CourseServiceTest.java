package com.assignment.course.domain.course.service;

import com.assignment.course.domain.course.dto.CourseDto;
import com.assignment.course.domain.course.entity.Course;
import com.assignment.course.global.exception.GlobalException;
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
class CourseServiceTest {

    @Autowired
    private CourseService courseService;

    // ==================== createCourse ====================

    @Test
    void 강의_등록_성공() throws Exception {
        CourseDto.Response response = courseService.createCourse("kim_creat1", createCourseRequest(3));

        assertThat(response.getTitle()).isEqualTo("테스트 강의");
        assertThat(response.getStatus()).isEqualTo(Course.Status.DRAFT);
        assertThat(response.getUserId()).isEqualTo("kim_creat1");
        assertThat(response.getCurrentEnrollment()).isEqualTo(0);
    }

    @Test
    void 수강생은_강의_등록_불가() throws Exception {
        assertThatThrownBy(() -> courseService.createCourse("jung_class1", createCourseRequest(3)))
                .isInstanceOf(GlobalException.class)
                .hasMessage("강사만 이용할 수 있습니다.");
    }

    @Test
    void 존재하지_않는_유저는_강의_등록_불가() throws Exception {
        assertThatThrownBy(() -> courseService.createCourse("unknown_user", createCourseRequest(3)))
                .isInstanceOf(GlobalException.class)
                .hasMessage("존재하지 않는 유저입니다.");
    }

    // ==================== getCourse ====================

    @Test
    void 강의_상세_조회_성공() throws Exception {
        CourseDto.Response created = courseService.createCourse("kim_creat1", createCourseRequest(3));

        CourseDto.Response response = courseService.getCourse(created.getCourseNo());

        assertThat(response.getCourseNo()).isEqualTo(created.getCourseNo());
        assertThat(response.getTitle()).isEqualTo("테스트 강의");
    }

    @Test
    void 존재하지_않는_강의_조회_실패() {
        assertThatThrownBy(() -> courseService.getCourse(999L))
                .isInstanceOf(GlobalException.class)
                .hasMessage("존재하지 않는 강의입니다.");
    }

    // ==================== getCourses ====================

    @Test
    void 강의_목록_전체_조회() throws Exception {
        courseService.createCourse("kim_creat1", createCourseRequest(3));
        courseService.createCourse("lee_creat1", createCourseRequest(5));

        Page<CourseDto.Response> result = courseService.getCourses(null, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isGreaterThanOrEqualTo(2);
    }

    @Test
    void 강의_목록_상태_필터_조회() throws Exception {
        CourseDto.Response created = courseService.createCourse("kim_creat1", createCourseRequest(3));
        courseService.updateCourseStatus("kim_creat1", created.getCourseNo(), createStatusRequest(Course.Status.OPEN));

        Page<CourseDto.Response> result = courseService.getCourses(Course.Status.OPEN, PageRequest.of(0, 10));

        assertThat(result.getContent()).allMatch(c -> c.getStatus() == Course.Status.OPEN);
    }

    // ==================== updateCourseStatus ====================

    @Test
    void 강의_상태_변경_DRAFT_to_OPEN() throws Exception {
        CourseDto.Response created = courseService.createCourse("kim_creat1", createCourseRequest(3));

        CourseDto.Response response = courseService.updateCourseStatus(
                "kim_creat1", created.getCourseNo(), createStatusRequest(Course.Status.OPEN));

        assertThat(response.getStatus()).isEqualTo(Course.Status.OPEN);
    }

    @Test
    void 강의_상태_변경_OPEN_to_CLOSED() throws Exception {
        CourseDto.Response created = courseService.createCourse("kim_creat1", createCourseRequest(3));
        courseService.updateCourseStatus("kim_creat1", created.getCourseNo(), createStatusRequest(Course.Status.OPEN));

        CourseDto.Response response = courseService.updateCourseStatus(
                "kim_creat1", created.getCourseNo(), createStatusRequest(Course.Status.CLOSED));

        assertThat(response.getStatus()).isEqualTo(Course.Status.CLOSED);
    }

    @Test
    void 유효하지_않은_상태_전환_실패() throws Exception {
        CourseDto.Response created = courseService.createCourse("kim_creat1", createCourseRequest(3));

        // DRAFT → CLOSED 불가
        assertThatThrownBy(() -> courseService.updateCourseStatus(
                "kim_creat1", created.getCourseNo(), createStatusRequest(Course.Status.CLOSED)))
                .isInstanceOf(GlobalException.class)
                .hasMessage("유효하지 않은 상태 전환입니다.");
    }

    @Test
    void 본인_강의가_아니면_상태_변경_불가() throws Exception {
        CourseDto.Response created = courseService.createCourse("kim_creat1", createCourseRequest(3));

        assertThatThrownBy(() -> courseService.updateCourseStatus(
                "lee_creat1", created.getCourseNo(), createStatusRequest(Course.Status.OPEN)))
                .isInstanceOf(GlobalException.class)
                .hasMessage("본인 강의가 아닙니다.");
    }

    // ==================== 헬퍼 메서드 ====================

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
