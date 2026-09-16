package com.gulsah.mini_doodle.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Meeting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY,  optional = false)
    @JoinColumn(name= "slot_id", nullable = false, unique = true)
    private TimeSlot slot;

    @ManyToMany
    @JoinTable(
            name = "meeting_participant",
            joinColumns = @JoinColumn(name = "meeting_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> participants = new ArrayList<>();

    @Column(nullable = false)
    private String title;

    @Column(length = 1500)
    private String description;

    public Meeting(TimeSlot slot, List<User> participants, String title, String description) {
        this.slot = slot;
        this.participants = new ArrayList<>(participants);
        this.title = title;
        this.description = description;
    }

    protected  Meeting() {}

    public Long getId() {
        return id;
    }

    public TimeSlot getSlot() {
        return slot;
    }

    public List<User> getParticipants() {
        return participants;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

}
