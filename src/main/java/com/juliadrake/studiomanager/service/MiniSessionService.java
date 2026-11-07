package com.juliadrake.studiomanager.service;

import com.juliadrake.studiomanager.model.MiniSessionEvent;
import com.juliadrake.studiomanager.model.ProjectStatus;
import com.juliadrake.studiomanager.repository.MiniSessionEventRepository;
import com.juliadrake.studiomanager.repository.ProjectRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Slot math for mini-session events: derives the slots of an event and
 * counts how many seats of each slot are already taken. A seat is taken
 * by a non-cancelled project pointing at the event whose session start
 * equals the slot start.
 */
@Service
public class MiniSessionService {

    /** One derived slot with its booking counts. */
    public record SlotInfo(LocalDateTime start, LocalDateTime end,
                           int capacity, long booked, int remaining) {
        public boolean isFull() {
            return remaining <= 0;
        }
    }

    /** An event plus the availability of each of its slots. */
    public record EventView(MiniSessionEvent event, List<SlotInfo> slots) {
        public int totalRemaining() {
            return slots.stream().mapToInt(SlotInfo::remaining).sum();
        }
    }

    private final MiniSessionEventRepository events;
    private final ProjectRepository projects;

    public MiniSessionService(MiniSessionEventRepository events, ProjectRepository projects) {
        this.events = events;
        this.projects = projects;
    }

    /** Seats already taken in one slot of an event. */
    public long bookedCount(MiniSessionEvent event, LocalDateTime slotStart) {
        return projects.countByMiniEventIdAndSessionDateTimeAndStatusNot(
                event.getId(), slotStart, ProjectStatus.CANCELLED);
    }

    /** Every slot of the event with booked/remaining counts. */
    public List<SlotInfo> slotsFor(MiniSessionEvent event) {
        return event.slotStarts().stream().map(start -> {
            long booked = bookedCount(event, start);
            int remaining = (int) Math.max(0, event.getCapacityPerSlot() - booked);
            return new SlotInfo(start, start.plusMinutes(event.getSlotLengthMinutes()),
                    event.getCapacityPerSlot(), booked, remaining);
        }).toList();
    }

    public EventView view(MiniSessionEvent event) {
        return new EventView(event, slotsFor(event));
    }

    /** Events today or later, each with slot availability — the public list. */
    public List<EventView> upcomingViews() {
        return events.findByDateGreaterThanEqualOrderByDateAscStartTimeAsc(LocalDate.now())
                .stream().map(this::view).toList();
    }

    /** All events, earliest first — the admin list. */
    public List<EventView> allViews() {
        return events.findAllByOrderByDateAscStartTimeAsc()
                .stream().map(this::view).toList();
    }
}
