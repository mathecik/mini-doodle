package com.gulsah.mini_doodle.dto;

import java.time.Instant;
import java.util.List;

public record AvailabilityResponse(Instant from, Instant to,
                                   long freeMinutes, long busyMinutes,
                                   List<SlotResponse> free, List<SlotResponse> busy) {
}