package com.gulsah.mini_doodle.service;

import com.gulsah.mini_doodle.entity.Calendar;
import com.gulsah.mini_doodle.entity.SlotStatus;
import com.gulsah.mini_doodle.entity.TimeSlot;
import com.gulsah.mini_doodle.exception.BadRequestException;
import com.gulsah.mini_doodle.exception.ConflictException;
import com.gulsah.mini_doodle.exception.NotFoundException;
import com.gulsah.mini_doodle.repository.CalendarRepository;
import com.gulsah.mini_doodle.repository.MeetingRepository;
import com.gulsah.mini_doodle.repository.TimeSlotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class TimeSlotService {

    private static final Duration MAX_QUERY_RANGE = Duration.ofDays(31);

    private final TimeSlotRepository timeSlotRepository;
    private final CalendarRepository calendarRepository;
    private final MeetingRepository meetingRepository;

    public TimeSlotService(TimeSlotRepository timeSlotRepository,
                           CalendarRepository calendarRepository,
                           MeetingRepository meetingRepository) {
        this.timeSlotRepository = timeSlotRepository;
        this.calendarRepository = calendarRepository;
        this.meetingRepository = meetingRepository;
    }

    @Transactional
    public TimeSlot createSlot(Long userId, Instant startTime, int durationMinutes) {
        Calendar calendar = getCalendar(userId);
        Instant endTime = startTime.plus(Duration.ofMinutes(durationMinutes));

        if (timeSlotRepository.countOverlapping(calendar.getId(), startTime, endTime) > 0) {
            throw new ConflictException("The slot overlaps with an existing slot");
        }
        return timeSlotRepository.save(new TimeSlot(startTime, durationMinutes, calendar));
    }

    @Transactional(readOnly = true)
        public List<TimeSlot> getSlots(Long userId, Instant from, Instant to, SlotStatus status) {
        if (!from.isBefore(to)) {
            throw new BadRequestException("'from' must be before 'to'");
        }
        if (Duration.between(from, to).compareTo(MAX_QUERY_RANGE) > 0) {
            throw new BadRequestException("The time range can be at most 31 days");
        }
        Calendar calendar = getCalendar(userId);
        List<TimeSlot> slots = timeSlotRepository.findInRange(calendar.getId(), from, to);

        if (status == null) {
            return slots;
        }
        return slots.stream()
                .filter(slot -> slot.getStatus() == status)
                .toList();
    }

    @Transactional
    public TimeSlot updateSlot(Long userId, Long slotId, Instant startTime, int durationMinutes) {
        TimeSlot slot = getSlotOfUser(userId, slotId);
        ensureNoMeeting(slot);

        Instant endTime = startTime.plus(Duration.ofMinutes(durationMinutes));
        long overlapping = timeSlotRepository.countOverlappingExcept(
                slot.getCalendar().getId(), startTime, endTime, slot.getId());
        if (overlapping > 0) {
            throw new ConflictException("The slot overlaps with an existing slot");
        }

        slot.changeTime(startTime, durationMinutes);
        return slot;
    }

    @Transactional
    public TimeSlot changeStatus(Long userId, Long slotId, SlotStatus status) {
        TimeSlot slot = getSlotOfUser(userId, slotId);

        if (status == SlotStatus.FREE) {
            ensureNoMeeting(slot);
            slot.markFree();
        } else {
            slot.markBusy();
        }
        return slot;
    }

    @Transactional
    public void deleteSlot(Long userId, Long slotId) {
        TimeSlot slot = getSlotOfUser(userId, slotId);
        ensureNoMeeting(slot);
        timeSlotRepository.delete(slot);
    }

    private Calendar getCalendar(Long userId) {
        return calendarRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
    }

    private TimeSlot getSlotOfUser(Long userId, Long slotId) {
        return timeSlotRepository.findByIdAndCalendarUserId(slotId, userId)
                .orElseThrow(() -> new NotFoundException("Slot not found: " + slotId));
    }

    private void ensureNoMeeting(TimeSlot slot) {
        if (meetingRepository.existsBySlotId(slot.getId())) {
            throw new ConflictException("The slot has a meeting. Cancel the meeting first.");
        }
    }
}