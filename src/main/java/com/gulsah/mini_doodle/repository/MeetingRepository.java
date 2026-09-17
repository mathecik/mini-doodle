package com.gulsah.mini_doodle.repository;

import com.gulsah.mini_doodle.entity.Meeting;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    boolean existsBySlotId(Long slotId);

    @EntityGraph(attributePaths = {"slot", "participants"})
    Optional<Meeting> findByIdAndSlotCalendarUserId(Long id, Long userId);
}