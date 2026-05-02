package com.assignment.course.domain.course.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Course")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Course {

    public enum Status { DRAFT, OPEN, CLOSED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Course_No")
    private Long courseNo;

    @Column(name = "Title", nullable = false)
    private String title;

    @Column(name = "Description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "Price", nullable = false)
    private int price;

    @Column(name = "Max_Capacity", nullable = false)
    private int maxCapacity;

    @Column(name = "Start_Date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "End_Date", nullable = false)
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false, length = 6)
    private Status status;

    @Column(name = "Created_Date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "Updated_Date")
    private LocalDateTime updatedDate;

    @Column(name = "Del_Date")
    private LocalDateTime delDate;

    @Column(name = "User_Id", nullable = false, length = 20)
    private String userId;

    public void updateStatus(Status newStatus) {
        this.status = newStatus;
        this.updatedDate = LocalDateTime.now();
    }

    @Builder
    public Course(String userId, String title, String description, int price, int maxCapacity,
                  LocalDateTime startDate, LocalDateTime endDate) {
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.price = price;
        this.maxCapacity = maxCapacity;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = Status.DRAFT;
        this.createdDate = LocalDateTime.now();
    }
}
