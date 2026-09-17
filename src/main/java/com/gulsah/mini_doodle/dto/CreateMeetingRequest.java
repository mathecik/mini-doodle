package com.gulsah.mini_doodle.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateMeetingRequest(
        @NotNull Long slotId,
        @NotBlank @Size(max = 255) String title,
        @Size(max = 1500) String description,
        List<Long> participantIds
) {}