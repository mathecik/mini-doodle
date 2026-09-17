package com.gulsah.mini_doodle.repository;

import com.gulsah.mini_doodle.entity.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {

    Optional<TimeSlot> findByIdAndCalendarUserId(Long id, Long userId);

    @Query("""
            select s from TimeSlot s
            where s.calendar.id = :calendarId
              and s.startTime >= :from
              and s.startTime < :to
            order by s.startTime
            """)
    List<TimeSlot> findInRange(Long calendarId, Instant from, Instant to);

    @Query("""
            select count(s) from TimeSlot s
            where s.calendar.id = :calendarId
              and s.startTime < :endTime
              and s.endTime > :startTime
            """)
    long countOverlapping(Long calendarId, Instant startTime, Instant endTime);

    @Query("""
            select count(s) from TimeSlot s
            where s.calendar.id = :calendarId
              and s.id <> :excludedSlotId
              and s.startTime < :endTime
              and s.endTime > :startTime
            """)
    long countOverlappingExcept(Long calendarId, Instant startTime, Instant endTime, Long excludedSlotId);
}