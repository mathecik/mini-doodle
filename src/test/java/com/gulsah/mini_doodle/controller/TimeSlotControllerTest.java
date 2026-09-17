package com.gulsah.mini_doodle.controller;

import com.gulsah.mini_doodle.TestcontainersConfiguration;
import com.gulsah.mini_doodle.entity.Calendar;
import com.gulsah.mini_doodle.entity.TimeSlot;
import com.gulsah.mini_doodle.entity.User;
import com.gulsah.mini_doodle.repository.CalendarRepository;
import com.gulsah.mini_doodle.repository.MeetingRepository;
import com.gulsah.mini_doodle.repository.TimeSlotRepository;
import com.gulsah.mini_doodle.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class TimeSlotControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
    void createsSlot() throws Exception {
        mockMvc.perform(post("/api/v1/users/{userId}/slots", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"startTime": "2026-09-21T10:00:00Z", "durationMinutes": 30}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.endTime").value("2026-09-21T10:30:00Z"))
                .andExpect(jsonPath("$.status").value("FREE"));
    }

    @Test
    void rejectsOverlappingSlot() throws Exception {
        saveSlot("2026-09-21T10:00:00Z");

        mockMvc.perform(post("/api/v1/users/{userId}/slots", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"startTime": "2026-09-21T10:15:00Z", "durationMinutes": 30}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void listsSlotsFilteredByStatus() throws Exception {
        saveSlot("2026-09-21T10:00:00Z");
        TimeSlot busySlot = saveSlot("2026-09-21T11:00:00Z");
        busySlot.markBusy();
        timeSlotRepository.save(busySlot);

        mockMvc.perform(get("/api/v1/users/{userId}/slots", user.getId())
                        .param("from", "2026-09-21T00:00:00Z")
                        .param("to", "2026-09-22T00:00:00Z")
                        .param("status", "BUSY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].startTime").value("2026-09-21T11:00:00Z"));
    }

    @Test
    void updatesSlotTime() throws Exception {
        TimeSlot slot = saveSlot("2026-09-21T10:00:00Z");

        mockMvc.perform(put("/api/v1/users/{userId}/slots/{slotId}", user.getId(), slot.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"startTime": "2026-09-21T10:15:00Z", "durationMinutes": 60}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.endTime").value("2026-09-21T11:15:00Z"));
    }

    @Test
    void marksSlotBusy() throws Exception {
        TimeSlot slot = saveSlot("2026-09-21T10:00:00Z");

        mockMvc.perform(patch("/api/v1/users/{userId}/slots/{slotId}/status", user.getId(), slot.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "BUSY"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BUSY"));
    }

    @Test
    void deletesSlot() throws Exception {
        TimeSlot slot = saveSlot("2026-09-21T10:00:00Z");

        mockMvc.perform(delete("/api/v1/users/{userId}/slots/{slotId}", user.getId(), slot.getId()))
                .andExpect(status().isNoContent());

        assertThat(timeSlotRepository.existsById(slot.getId())).isFalse();
    }

    private TimeSlot saveSlot(String startTime) {
        return timeSlotRepository.save(new TimeSlot(Instant.parse(startTime), 30, calendar));
    }
}