package com.gulsah.mini_doodle.service;

import com.gulsah.mini_doodle.entity.Meeting;
import com.gulsah.mini_doodle.entity.SlotStatus;
import com.gulsah.mini_doodle.entity.TimeSlot;
import com.gulsah.mini_doodle.entity.User;
import com.gulsah.mini_doodle.exception.ConflictException;
import com.gulsah.mini_doodle.exception.NotFoundException;
import com.gulsah.mini_doodle.repository.MeetingRepository;
import com.gulsah.mini_doodle.repository.TimeSlotRepository;
import com.gulsah.mini_doodle.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final UserRepository userRepository;

    public MeetingService(MeetingRepository meetingRepository,
                          TimeSlotRepository timeSlotRepository,
                          UserRepository userRepository) {
        this.meetingRepository = meetingRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Meeting scheduleMeeting(Long userId, Long slotId, String title,
                                   String description, List<Long> participantIds) {
        TimeSlot slot = timeSlotRepository.findByIdAndCalendarUserId(slotId, userId)
                .orElseThrow(() -> new NotFoundException("Slot not found: " + slotId));

        if (slot.getStatus() != SlotStatus.FREE) {
            throw new ConflictException("The slot is not free");
        }

        List<Long> ids = participantIds == null
                ? List.of()
                : participantIds.stream().distinct().toList();
        List<User> participants = userRepository.findAllById(ids);
        if (participants.size() != ids.size()) {
            throw new NotFoundException("One or more participants were not found");
        }

        slot.markBusy();
        return meetingRepository.save(new Meeting(slot, participants, title, description));
    }

    @Transactional(readOnly = true)
    public Meeting getMeeting(Long userId, Long meetingId) {
        return findMeetingOfUser(userId, meetingId);
    }

    @Transactional
    public void cancelMeeting(Long userId, Long meetingId) {
        Meeting meeting = findMeetingOfUser(userId, meetingId);
        meeting.getSlot().markFree();
        meetingRepository.delete(meeting);
    }

    private Meeting findMeetingOfUser(Long userId, Long meetingId) {
        return meetingRepository.findByIdAndSlotCalendarUserId(meetingId, userId)
                .orElseThrow(() -> new NotFoundException("Meeting not found: " + meetingId));
    }
}