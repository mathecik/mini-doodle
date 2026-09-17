package com.gulsah.mini_doodle.repository;

import com.gulsah.mini_doodle.entity.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {
    List<TimeSlot> findByCalendarIdAndStartTimeGreaterThanEqualAndStartTimeLessThanOrderByStartTime(
            Long calendarId, Instant from, Instant to);
}