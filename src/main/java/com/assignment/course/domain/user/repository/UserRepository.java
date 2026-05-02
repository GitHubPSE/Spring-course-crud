package com.assignment.course.domain.user.repository;

import com.assignment.course.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByUserIdAndDelDateIsNull(String userId);
}
