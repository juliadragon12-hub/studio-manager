package com.juliadrake.studiomanager.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.Duration;
import java.time.LocalDateTime;

@Entity
public class TimeEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(optional = false)
    private Project project;

    @Enumerated(EnumType.STRING)
    private ActivityType activityType;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    /** True when the entry was typed in by hand after the fact. */
    private boolean manual;

    @Column(length = 1000)
    private String notes;

    protected TimeEntry() {
    }

    public TimeEntry(Project project, ActivityType activityType,
                    LocalDateTime startTime, LocalDateTime endTime,
                    boolean manual, String notes) {
        if (endTime != null && startTime != null && !endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("End time must be after start time.");
        }
        this.project = project;
        this.activityType = activityType;
        this.startTime = startTime;
        this.endTime = endTime;
        this.manual = manual;
        this.notes = notes;
    }

    public Long getId() { return id; }
    public Project getProject() { return project; }
    public ActivityType getActivityType() { return activityType; }
    public void setActivityType(ActivityType activityType) { this.activityType = activityType; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public boolean isManual() { return manual; }
    public void setManual(boolean manual) { this.manual = manual; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    /** Duration in whole minutes, computed from start/end. */
    public long getDurationMinutes() {
        if (startTime == null || endTime == null) {
            return 0;
        }
        return Duration.between(startTime, endTime).toMinutes();
    }
}
