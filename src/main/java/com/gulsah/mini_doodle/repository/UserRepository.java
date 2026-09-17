package com.gulsah.mini_doodle.repository;

import com.gulsah.mini_doodle.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
}