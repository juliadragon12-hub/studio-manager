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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Project {

    /** Retainer charged at booking time: 25% of the price, rounded to whole dollars. */
    public static final BigDecimal RETAINER_FRACTION = new BigDecimal("0.25");

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

    /** Human-friendly reference given to the client, e.g. SM-2026-0007. Set by the booking flow. */
    private String bookingReference;

    /** Set when this booking took a slot in a mini-session event; null for full sessions. */
    @ManyToOne
    private MiniSessionEvent miniEvent;

    @Enumerated(EnumType.STRING)
    private EditingStage editingStage = EditingStage.CULLING;

    @OneToOne(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private Agreement agreement;

    @OneToOne(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private Questionnaire questionnaire;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TimeEntry> timeEntries = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Payment> payments = new ArrayList<>();

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
    public String getBookingReference() { return bookingReference; }
    public void setBookingReference(String bookingReference) { this.bookingReference = bookingReference; }
    public MiniSessionEvent getMiniEvent() { return miniEvent; }
    public void setMiniEvent(MiniSessionEvent miniEvent) { this.miniEvent = miniEvent; }
    public EditingStage getEditingStage() { return editingStage; }
    public void setEditingStage(EditingStage editingStage) { this.editingStage = editingStage; }
    public Agreement getAgreement() { return agreement; }
    public void setAgreement(Agreement agreement) { this.agreement = agreement; }
    public Questionnaire getQuestionnaire() { return questionnaire; }
    public void setQuestionnaire(Questionnaire questionnaire) { this.questionnaire = questionnaire; }
    public List<TimeEntry> getTimeEntries() { return timeEntries; }
    public List<Payment> getPayments() { return payments; }

    /** Total minutes logged across all time entries. */
    public long getTotalMinutes() {
        return timeEntries.stream().mapToLong(TimeEntry::getDurationMinutes).sum();
    }

    /** True once an agreement with a signer name exists for this project. */
    public boolean isAgreementSigned() {
        return agreement != null
                && agreement.getSignerName() != null
                && !agreement.getSignerName().isBlank();
    }

    /** The retainer payment recorded at booking time, if any. */
    public Payment getRetainerPayment() {
        return payments.isEmpty() ? null : payments.get(0);
    }

    /** Sum of all recorded payments. */
    public BigDecimal getTotalPaid() {
        return payments.stream()
                .map(p -> p.getAmount() == null ? BigDecimal.ZERO : p.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Retainer due at booking: 25% of the price, rounded to whole dollars. */
    public BigDecimal getRetainerDue() {
        if (price == null) {
            return null;
        }
        return price.multiply(RETAINER_FRACTION).setScale(0, RoundingMode.HALF_UP);
    }

    /** Price minus everything paid so far; null when no price is set. */
    public BigDecimal getBalanceDue() {
        if (price == null) {
            return null;
        }
        return price.subtract(getTotalPaid());
    }
}
