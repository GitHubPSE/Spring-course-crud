package com.assignment.course.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "User")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    public enum Role { CREATOR, CLASSMATE }

    @Id
    @Column(name = "User_Id", length = 20)
    private String userId;

    @Column(name = "Name", nullable = false, length = 10)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "Role", nullable = false, length = 10)
    private Role role;

    @Column(name = "Created_Date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "Updated_Date")
    private LocalDateTime updatedDate;

    @Column(name = "Del_Date")
    private LocalDateTime delDate;
}
