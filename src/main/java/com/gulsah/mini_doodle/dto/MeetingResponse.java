package com.gulsah.mini_doodle.dto;

import com.gulsah.mini_doodle.entity.Meeting;

import java.time.Instant;
import java.util.List;

public record MeetingResponse(Long id, String title, String description,
                              Long slotId, Instant startTime, Instant endTime,
                              List<UserResponse> participants) {

    public static MeetingResponse from(Meeting meeting) {
        return new MeetingResponse(
                meeting.getId(),
                meeting.getTitle(),
                meeting.getDescription(),
                meeting.getSlot().getId(),
                meeting.getSlot().getStartTime(),
                meeting.getSlot().getEndTime(),
                meeting.getParticipants().stream().map(UserResponse::from).toList()
        );
    }
}