package com.gulsah.mini_doodle.entity;

import jakarta.persistence.*;

import java.time.Duration;
import java.time.Instant;

@Entity
public class TimeSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Instant startTime;

    @Column(nullable = false)
    private Instant endTime;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "calendar_id", nullable = false)
    private Calendar calendar;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SlotStatus status;

    @Version
    private Long version;


    public TimeSlot(Instant startTime, int durationMinutes, Calendar calendar) {
        this.startTime = startTime;
        this.endTime = this.startTime.plus(Duration.ofMinutes(durationMinutes));
        this.calendar = calendar;
        this.status = SlotStatus.FREE;
    }


    protected TimeSlot() {}

    public Long getId() {
        return id;
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public Instant getStartTime() {
        return this.startTime;
    }

    public Instant getEndTime() {
        return this.endTime;
    }

    public SlotStatus getStatus() {
        return status;
    }

    public void markBusy() {
        this.status = SlotStatus.BUSY;
    }

    public void markFree() {
        this.status = SlotStatus.FREE;
    }

    public void changeTime(Instant startTime, int durationMinutes) {
        this.startTime = startTime;
        this.endTime = startTime.plus(Duration.ofMinutes(durationMinutes));
    }

    public long getDurationMinutes() {
        return Duration.between(startTime, endTime).toMinutes();
    }
}
