package com.gulsah.mini_doodle.service;

import com.gulsah.mini_doodle.dto.AvailabilityResponse;
import com.gulsah.mini_doodle.dto.SlotResponse;
import com.gulsah.mini_doodle.entity.SlotStatus;
import com.gulsah.mini_doodle.entity.TimeSlot;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class AvailabilityService {

    private final TimeSlotService timeSlotService;

    public AvailabilityService(TimeSlotService timeSlotService) {
        this.timeSlotService = timeSlotService;
    }

    @Transactional(readOnly = true)
    public AvailabilityResponse getAvailability(Long userId, Instant from, Instant to) {
        List<TimeSlot> slots = timeSlotService.getSlots(userId, from, to, null);

        List<SlotResponse> free = filterByStatus(slots, SlotStatus.FREE);
        List<SlotResponse> busy = filterByStatus(slots, SlotStatus.BUSY);

        return new AvailabilityResponse(from, to,
                totalMinutes(free), totalMinutes(busy), free, busy);
    }

    private List<SlotResponse> filterByStatus(List<TimeSlot> slots, SlotStatus status) {
        return slots.stream()
                .filter(slot -> slot.getStatus() == status)
                .map(SlotResponse::from)
                .toList();
    }

    private long totalMinutes(List<SlotResponse> slots) {
        return slots.stream().mapToLong(SlotResponse::durationMinutes).sum();
    }
}