package com.juliadrake.studiomanager.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Client client;

    @ManyToOne(optional = false)
    private SessionPackage sessionPackage;

    @Enumerated(EnumType.STRING)
    private ProjectStatus status = ProjectStatus.INQUIRY;

    private LocalDateTime sessionDateTime;
    private String location;
    private BigDecimal price;
    private LocalDate galleryDueDate;
    private LocalDate galleryDeliveredDate;
    private String notes;
    private LocalDateTime createdAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private EditingStage editingStage = EditingStage.CULLING;

    @OneToOne(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private Agreement agreement;

    @OneToOne(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private Questionnaire questionnaire;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TimeEntry> timeEntries = new ArrayList<>();

    protected Project() {
    }

    public Project(Client client, SessionPackage sessionPackage, LocalDateTime sessionDateTime,
                   String location, BigDecimal price, String notes) {
        this.client = client;
        this.sessionPackage = sessionPackage;
        this.sessionDateTime = sessionDateTime;
        this.location = location;
        this.price = price;
        this.notes = notes;
    }

    public Long getId() { return id; }
    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }
    public SessionPackage getSessionPackage() { return sessionPackage; }
    public void setSessionPackage(SessionPackage sessionPackage) { this.sessionPackage = sessionPackage; }
    public ProjectStatus getStatus() { return status; }
    public void setStatus(ProjectStatus status) { this.status = status; }
    public LocalDateTime getSessionDateTime() { return sessionDateTime; }
    public void setSessionDateTime(LocalDateTime sessionDateTime) { this.sessionDateTime = sessionDateTime; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public LocalDate getGalleryDueDate() { return galleryDueDate; }
    public void setGalleryDueDate(LocalDate galleryDueDate) { this.galleryDueDate = galleryDueDate; }
    public LocalDate getGalleryDeliveredDate() { return galleryDeliveredDate; }
    public void setGalleryDeliveredDate(LocalDate galleryDeliveredDate) { this.galleryDeliveredDate = galleryDeliveredDate; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public EditingStage getEditingStage() { return editingStage; }
    public void setEditingStage(EditingStage editingStage) { this.editingStage = editingStage; }
    public Agreement getAgreement() { return agreement; }
    public void setAgreement(Agreement agreement) { this.agreement = agreement; }
    public Questionnaire getQuestionnaire() { return questionnaire; }
    public void setQuestionnaire(Questionnaire questionnaire) { this.questionnaire = questionnaire; }
    public List<TimeEntry> getTimeEntries() { return timeEntries; }

    /** Total minutes logged across all time entries. */
    public long getTotalMinutes() {
        return timeEntries.stream().mapToLong(TimeEntry::getDurationMinutes).sum();
    }
}
