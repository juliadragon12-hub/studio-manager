package com.juliadrake.studiomanager.controller;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * Everything a client submits in the one-flow booking, bound from the
 * single form on /book: package → date/time → details → questionnaire →
 * agreement e-signature → demo retainer. Validated server-side by
 * BookingService; on any error the form is re-rendered with the values
 * kept, so nothing the client typed is lost.
 */
public class BookingForm {

    /** FULL = a full session at a requested date/time; MINI = a mini-session slot. */
    private String bookingType = "FULL";

    private Long packageId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime sessionDateTime;

    /** Chosen mini slot, encoded "{miniEventId}|{ISO slot start}" by the form. */
    private String miniSlot;

    private String name;
    private String email;
    private String phone;
    private String location;
    private String message;

    private String sessionGoals;
    private String mustHaveShots;
    private String comfortNotes;

    /** Typed full name acting as the e-signature. Required. */
    private String signerName;

    /** "I agree to the session agreement" consent checkbox. Required. */
    private Boolean agreeConsent;

    public String getBookingType() { return bookingType; }
    public void setBookingType(String bookingType) { this.bookingType = bookingType; }
    public Long getPackageId() { return packageId; }
    public void setPackageId(Long packageId) { this.packageId = packageId; }
    public LocalDateTime getSessionDateTime() { return sessionDateTime; }
    public void setSessionDateTime(LocalDateTime sessionDateTime) { this.sessionDateTime = sessionDateTime; }
    public String getMiniSlot() { return miniSlot; }
    public void setMiniSlot(String miniSlot) { this.miniSlot = miniSlot; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getSessionGoals() { return sessionGoals; }
    public void setSessionGoals(String sessionGoals) { this.sessionGoals = sessionGoals; }
    public String getMustHaveShots() { return mustHaveShots; }
    public void setMustHaveShots(String mustHaveShots) { this.mustHaveShots = mustHaveShots; }
    public String getComfortNotes() { return comfortNotes; }
    public void setComfortNotes(String comfortNotes) { this.comfortNotes = comfortNotes; }
    public String getSignerName() { return signerName; }
    public void setSignerName(String signerName) { this.signerName = signerName; }
    public Boolean getAgreeConsent() { return agreeConsent; }
    public void setAgreeConsent(Boolean agreeConsent) { this.agreeConsent = agreeConsent; }
}
