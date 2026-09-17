package com.gulsah.mini_doodle.controller;

import com.gulsah.mini_doodle.TestcontainersConfiguration;
import com.gulsah.mini_doodle.entity.Calendar;
import com.gulsah.mini_doodle.entity.Meeting;
import com.gulsah.mini_doodle.entity.SlotStatus;
import com.gulsah.mini_doodle.entity.TimeSlot;
import com.gulsah.mini_doodle.entity.User;
import com.gulsah.mini_doodle.repository.CalendarRepository;
import com.gulsah.mini_doodle.repository.MeetingRepository;
import com.gulsah.mini_doodle.repository.TimeSlotRepository;
import com.gulsah.mini_doodle.repository.UserRepository;
import com.gulsah.mini_doodle.service.MeetingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class MeetingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MeetingService meetingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CalendarRepository calendarRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private MeetingRepository meetingRepository;

    private User user;
    private Calendar calendar;

    @BeforeEach
    void setUp() {
        meetingRepository.deleteAll();
        timeSlotRepository.deleteAll();
        calendarRepository.deleteAll();
        userRepository.deleteAll();

        user = userRepository.save(new User("ayse", "ayse@example.com"));
        calendar = calendarRepository.save(new Calendar(user));
    }

    @Test
    void schedulesMeetingAndMarksSlotBusy() throws Exception {
        TimeSlot slot = saveSlot();
        User participant = userRepository.save(new User("mehmet", "mehmet@example.com"));

        mockMvc.perform(post("/api/v1/users/{userId}/meetings", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"slotId": %d, "title": "Planning", "participantIds": [%d]}
                                """.formatted(slot.getId(), participant.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Planning"))
                .andExpect(jsonPath("$.participants.length()").value(1));

        assertThat(timeSlotRepository.findById(slot.getId()).orElseThrow().getStatus())
                .isEqualTo(SlotStatus.BUSY);
    }

    @Test
    void rejectsMeetingOnBusySlot() throws Exception {
        TimeSlot slot = saveSlot();
        slot.markBusy();
        timeSlotRepository.save(slot);

        mockMvc.perform(post("/api/v1/users/{userId}/meetings", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"slotId": %d, "title": "Planning"}
                                """.formatted(slot.getId())))
                .andExpect(status().isConflict());
    }

    @Test
    void cancelsMeetingAndFreesSlot() throws Exception {
        TimeSlot slot = saveSlot();
        Meeting meeting = meetingService.scheduleMeeting(user.getId(), slot.getId(), "Planning", null, List.of());

        mockMvc.perform(delete("/api/v1/users/{userId}/meetings/{meetingId}", user.getId(), meeting.getId()))
                .andExpect(status().isNoContent());

        assertThat(meetingRepository.existsById(meeting.getId())).isFalse();
        assertThat(timeSlotRepository.findById(slot.getId()).orElseThrow().getStatus())
                .isEqualTo(SlotStatus.FREE);
    }

    @Test
    void slotWithMeetingCannotBeDeleted() throws Exception {
        TimeSlot slot = saveSlot();
        meetingService.scheduleMeeting(user.getId(), slot.getId(), "Planning", null, List.of());

        mockMvc.perform(delete("/api/v1/users/{userId}/slots/{slotId}", user.getId(), slot.getId()))
                .andExpect(status().isConflict());
    }

    @Test
    void onlyOneOfTwoConcurrentBookingsSucceeds() throws Exception {
        TimeSlot slot = saveSlot();
        CountDownLatch startSignal = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        Callable<Boolean> booking = () -> {
            startSignal.await();
            try {
                meetingService.scheduleMeeting(user.getId(), slot.getId(), "Planning", null, List.of());
                return true;
            } catch (RuntimeException e) {
                return false;
            }
        };

        Future<Boolean> first = executor.submit(booking);
        Future<Boolean> second = executor.submit(booking);
        startSignal.countDown();

        int successes = (first.get() ? 1 : 0) + (second.get() ? 1 : 0);
        executor.shutdown();

        assertThat(successes).isEqualTo(1);
        assertThat(meetingRepository.count()).isEqualTo(1);
    }

    private TimeSlot saveSlot() {
        return timeSlotRepository.save(new TimeSlot(Instant.parse("2026-09-21T10:00:00Z"), 30, calendar));
    }
}