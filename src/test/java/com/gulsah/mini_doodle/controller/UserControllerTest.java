package com.gulsah.mini_doodle.controller;

import com.gulsah.mini_doodle.TestcontainersConfiguration;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CalendarRepository calendarRepository;

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @BeforeEach
    void cleanUp() {
        meetingRepository.deleteAll();
        timeSlotRepository.deleteAll();
        calendarRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void createsUserWithCalendar() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content("""
                                {"username": "minion", "email": "minion@example.com"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.username").value("minion"))
                .andExpect(jsonPath("$.email").value("minion@example.com"));

        assertThat(calendarRepository.count()).isEqualTo(1);
    }

    @Test
    void returnsExistingUser() throws Exception {
        User user = userRepository.save(new User("minion", "minion@example.com"));

        mockMvc.perform(get("/api/v1/users/{id}", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("minion@example.com"));
    }

    @Test
    void rejectsDuplicateEmail() throws Exception {
        userRepository.save(new User("minion", "minion@example.com"));

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "Another minion", "email": "minion@example.com"}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void rejectsInvalidEmail() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "minion", "email": "not-an-email"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsNotFoundForUnknownUser() throws Exception {
        mockMvc.perform(get("/api/v1/users/{id}", 999999))
                .andExpect(status().isNotFound());
    }
}