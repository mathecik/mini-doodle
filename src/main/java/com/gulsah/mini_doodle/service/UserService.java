package com.gulsah.mini_doodle.service;

import com.gulsah.mini_doodle.entity.Calendar;
import com.gulsah.mini_doodle.entity.User;
import com.gulsah.mini_doodle.exception.ConflictException;
import com.gulsah.mini_doodle.exception.NotFoundException;
import com.gulsah.mini_doodle.repository.CalendarRepository;
import com.gulsah.mini_doodle.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final CalendarRepository calendarRepository;

    public UserService(UserRepository userRepository, CalendarRepository calendarRepository) {
        this.userRepository = userRepository;
        this.calendarRepository = calendarRepository;
    }

    @Transactional
    public User createUser(String name, String email) {
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("A user with this email already exists");
        }
        User user = userRepository.save(new User(name, email));
        calendarRepository.save(new Calendar(user));
        return user;
    }

    @Transactional(readOnly = true)
    public User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));
    }
}