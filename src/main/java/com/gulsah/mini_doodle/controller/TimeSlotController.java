package com.gulsah.mini_doodle.controller;

import com.gulsah.mini_doodle.dto.SlotRequest;
import com.gulsah.mini_doodle.dto.SlotResponse;
import com.gulsah.mini_doodle.dto.SlotStatusRequest;
import com.gulsah.mini_doodle.entity.SlotStatus;
import com.gulsah.mini_doodle.entity.TimeSlot;
import com.gulsah.mini_doodle.service.TimeSlotService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users/{userId}/slots")
public class TimeSlotController {

    private final TimeSlotService timeSlotService;

    public TimeSlotController(TimeSlotService timeSlotService) {
        this.timeSlotService = timeSlotService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SlotResponse create(@PathVariable Long userId,
                               @Valid @RequestBody SlotRequest request) {
        TimeSlot slot = timeSlotService.createSlot(userId, request.startTime(), request.durationMinutes());
        return SlotResponse.from(slot);
    }

    @GetMapping
    public List<SlotResponse> list(@PathVariable Long userId,
                                   @RequestParam Instant from,
                                   @RequestParam Instant to,
                                   @RequestParam(required = false) SlotStatus status) {
        return timeSlotService.getSlots(userId, from, to, status).stream()
                .map(SlotResponse::from)
                .toList();
    }

    @PutMapping("/{slotId}")
    public SlotResponse update(@PathVariable Long userId,
                               @PathVariable Long slotId,
                               @Valid @RequestBody SlotRequest request) {
        TimeSlot slot = timeSlotService.updateSlot(userId, slotId,
                request.startTime(), request.durationMinutes());
        return SlotResponse.from(slot);
    }

    @PatchMapping("/{slotId}/status")
    public SlotResponse changeStatus(@PathVariable Long userId,
                                     @PathVariable Long slotId,
                                     @Valid @RequestBody SlotStatusRequest request) {
        return SlotResponse.from(timeSlotService.changeStatus(userId, slotId, request.status()));
    }

    @DeleteMapping("/{slotId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long userId, @PathVariable Long slotId) {
        timeSlotService.deleteSlot(userId, slotId);
    }
}
