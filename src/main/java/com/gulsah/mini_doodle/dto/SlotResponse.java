package com.gulsah.mini_doodle.dto;

import com.gulsah.mini_doodle.entity.SlotStatus;
import com.gulsah.mini_doodle.entity.TimeSlot;

import java.time.Instant;

public record SlotResponse(Long id, Instant startTime, Instant endTime,
                           long durationMinutes, SlotStatus status) {

    public static SlotResponse from(TimeSlot slot) {
        return new SlotResponse(slot.getId(), slot.getStartTime(), slot.getEndTime(),
                slot.getDurationMinutes(), slot.getStatus());
    }
}