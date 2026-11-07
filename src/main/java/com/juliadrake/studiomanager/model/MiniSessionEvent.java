package com.juliadrake.studiomanager.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A mini-session event: one date blocked into short back-to-back slots
 * (e.g. an afternoon of 20-minute minis). Slots are not stored rows —
 * they are derived from the start/end times and the slot length. A slot
 * is booked by the projects that point at this event and whose session
 * start equals the slot start (see MiniSessionService for the counting).
 */
@Entity
public class MiniSessionEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private int slotLengthMinutes = 20;
    private int capacityPerSlot = 1;
    private BigDecimal price;
    private String location;

    /** The package a mini booking is recorded under. */
    @ManyToOne(optional = false)
    private SessionPackage sessionPackage;

    protected MiniSessionEvent() {
    }

    public MiniSessionEvent(String title, LocalDate date, LocalTime startTime, LocalTime endTime,
                            int slotLengthMinutes, int capacityPerSlot, BigDecimal price,
                            String location, SessionPackage sessionPackage) {
        this.title = title;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.slotLengthMinutes = slotLengthMinutes;
        this.capacityPerSlot = capacityPerSlot;
        this.price = price;
        this.location = location;
        this.sessionPackage = sessionPackage;
    }

    /** Start instant of every slot, stepping from startTime by the slot length. */
    public List<LocalDateTime> slotStarts() {
        List<LocalDateTime> slots = new ArrayList<>();
        if (date == null || startTime == null || endTime == null || slotLengthMinutes <= 0) {
            return slots;
        }
        LocalDateTime cursor = date.atTime(startTime);
        LocalDateTime end = date.atTime(endTime);
        while (!cursor.plusMinutes(slotLengthMinutes).isAfter(end)) {
            slots.add(cursor);
            cursor = cursor.plusMinutes(slotLengthMinutes);
        }
        return slots;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public int getSlotLengthMinutes() { return slotLengthMinutes; }
    public void setSlotLengthMinutes(int slotLengthMinutes) { this.slotLengthMinutes = slotLengthMinutes; }
    public int getCapacityPerSlot() { return capacityPerSlot; }
    public void setCapacityPerSlot(int capacityPerSlot) { this.capacityPerSlot = capacityPerSlot; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public SessionPackage getSessionPackage() { return sessionPackage; }
    public void setSessionPackage(SessionPackage sessionPackage) { this.sessionPackage = sessionPackage; }
}
