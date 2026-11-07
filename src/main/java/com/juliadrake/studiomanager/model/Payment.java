package com.juliadrake.studiomanager.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A payment recorded against a project. In this build payments come only
 * from the demo checkout: the method is always "DEMO" and the status
 * PAID_DEMO. No real card details are ever collected or stored.
 */
@Entity
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(optional = false)
    private Project project;

    /** What the payment is for, e.g. "Retainer". */
    private String label;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    /** Payment method. Always "DEMO" in this build. */
    private String method;

    private LocalDateTime paidAt;

    protected Payment() {
    }

    public Payment(Project project, String label, BigDecimal amount,
                   PaymentStatus status, String method, LocalDateTime paidAt) {
        this.project = project;
        this.label = label;
        this.amount = amount;
        this.status = status;
        this.method = method;
        this.paidAt = paidAt;
    }

    public Long getId() { return id; }
    public Project getProject() { return project; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
}
