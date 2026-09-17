package com.gulsah.mini_doodle.controller;

import com.gulsah.mini_doodle.dto.CreateMeetingRequest;
import com.gulsah.mini_doodle.dto.MeetingResponse;
import com.gulsah.mini_doodle.entity.Meeting;
import com.gulsah.mini_doodle.service.MeetingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/{userId}/meetings")
public class MeetingController {

    private final MeetingService meetingService;

    public MeetingController(MeetingService meetingService) {
        this.meetingService = meetingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MeetingResponse schedule(@PathVariable Long userId,
                                    @Valid @RequestBody CreateMeetingRequest request) {
        Meeting meeting = meetingService.scheduleMeeting(userId, request.slotId(),
                request.title(), request.description(), request.participantIds());
        return MeetingResponse.from(meeting);
    }

    @GetMapping("/{meetingId}")
    public MeetingResponse get(@PathVariable Long userId, @PathVariable Long meetingId) {
        return MeetingResponse.from(meetingService.getMeeting(userId, meetingId));
    }

    @DeleteMapping("/{meetingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable Long userId, @PathVariable Long meetingId) {
        meetingService.cancelMeeting(userId, meetingId);
    }
}