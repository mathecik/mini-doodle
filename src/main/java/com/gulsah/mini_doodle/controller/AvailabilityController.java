package com.gulsah.mini_doodle.controller;

import com.gulsah.mini_doodle.dto.AvailabilityResponse;
import com.gulsah.mini_doodle.service.AvailabilityService;
import com.gulsah.mini_doodle.service.TimeSlotService;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/users/{userId}/availability")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    public AvailabilityController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @GetMapping
    public AvailabilityResponse get(@PathVariable Long userId,
                                    @RequestParam Instant from,
                                    @RequestParam Instant to) {
        return availabilityService.getAvailability(userId, from, to);
    }
}