package com.juliadrake.studiomanager.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import java.time.LocalDate;

@Entity
public class Agreement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @OneToOne(optional = false)
    private Project project;

    private String signerName;
    private LocalDate signedDate;
    private String termsText;

    protected Agreement() {
    }

    public Agreement(Project project, String signerName, LocalDate signedDate, String termsText) {
        this.project = project;
        this.signerName = signerName;
        this.signedDate = signedDate;
        this.termsText = termsText;
    }

    public Long getId() { return id; }
    public Project getProject() { return project; }
    public String getSignerName() { return signerName; }
    public void setSignerName(String signerName) { this.signerName = signerName; }
    public LocalDate getSignedDate() { return signedDate; }
    public void setSignedDate(LocalDate signedDate) { this.signedDate = signedDate; }
    public String getTermsText() { return termsText; }
    public void setTermsText(String termsText) { this.termsText = termsText; }
}
