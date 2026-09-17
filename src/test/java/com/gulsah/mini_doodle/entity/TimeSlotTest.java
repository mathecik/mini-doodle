package com.gulsah.mini_doodle.entity;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class TimeSlotTest {

    private static final Instant TEN_O_CLOCK = Instant.parse("2026-09-21T10:00:00Z");

    @Test
    void newSlotIsFreeAndEndTimeIsCalculated() {
        TimeSlot slot = new TimeSlot(TEN_O_CLOCK, 30, null);

        assertThat(slot.getEndTime()).isEqualTo(Instant.parse("2026-09-21T10:30:00Z"));
        assertThat(slot.getDurationMinutes()).isEqualTo(30);
        assertThat(slot.getStatus()).isEqualTo(SlotStatus.FREE);
    }

    @Test
    void changeTimeUpdatesEndTimeAndDuration() {
        TimeSlot slot = new TimeSlot(TEN_O_CLOCK, 30, null);

        slot.changeTime(Instant.parse("2026-09-21T11:00:00Z"), 60);

        assertThat(slot.getStartTime()).isEqualTo(Instant.parse("2026-09-21T11:00:00Z"));
        assertThat(slot.getEndTime()).isEqualTo(Instant.parse("2026-09-21T12:00:00Z"));
        assertThat(slot.getDurationMinutes()).isEqualTo(60);
    }

    @Test
    void statusCanBeChanged() {
        TimeSlot slot = new TimeSlot(TEN_O_CLOCK, 30, null);

        slot.markBusy();
        assertThat(slot.getStatus()).isEqualTo(SlotStatus.BUSY);

        slot.markFree();
        assertThat(slot.getStatus()).isEqualTo(SlotStatus.FREE);
    }
}