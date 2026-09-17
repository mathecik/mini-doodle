package com.gulsah.mini_doodle.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record SlotRequest(
        @NotNull Instant startTime,
        @NotNull @Min(30) @Max(480) Integer durationMinutes // one slot: 30 minutes to 8 hours
) {}