package com.gulsah.mini_doodle.dto;

import com.gulsah.mini_doodle.entity.SlotStatus;
import jakarta.validation.constraints.NotNull;

public record SlotStatusRequest(@NotNull SlotStatus status) {}
