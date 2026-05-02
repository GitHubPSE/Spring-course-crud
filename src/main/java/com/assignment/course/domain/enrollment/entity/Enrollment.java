package com.assignment.course.domain.enrollment.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Enrollment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Enrollment {

    public enum Status { PENDING, CONFIRMED, CANCELLED, WAITLISTED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Enrollment_No")
    private Long enrollmentNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false, length = 10)
    private Status status;

    @Column(name = "Confirmed_Date")
    private LocalDateTime confirmedDate;

    @Column(name = "Cancelled_Date")
    private LocalDateTime cancelledDate;

    @Column(name = "Created_Date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "Updated_Date")
    private LocalDateTime updatedDate;

    @Column(name = "Waitlist_Order")
    private Integer waitlistOrder;

    @Column(name = "Course_No", nullable = false)
    private Long courseNo;

    @Column(name = "User_Id", nullable = false, length = 20)
    private String userId;

    @Builder
    public Enrollment(Long courseNo, String userId, Status status, Integer waitlistOrder) {
        this.courseNo = courseNo;
        this.userId = userId;
        this.status = status;
        this.waitlistOrder = waitlistOrder;
        this.createdDate = LocalDateTime.now();
    }

    public void updateStatus(Status newStatus) {
        this.status = newStatus;
        this.updatedDate = LocalDateTime.now();
    }

    public void confirm() {
        this.status = Status.CONFIRMED;
        this.confirmedDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
    }

    public void cancel() {
        this.status = Status.CANCELLED;
        this.cancelledDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
    }
}
