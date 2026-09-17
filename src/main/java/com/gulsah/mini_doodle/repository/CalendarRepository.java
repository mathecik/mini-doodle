package com.gulsah.mini_doodle.repository;

import com.gulsah.mini_doodle.entity.Calendar;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CalendarRepository extends JpaRepository<Calendar, Long> {
    Optional<Calendar> findByUserId(Long userId);
}