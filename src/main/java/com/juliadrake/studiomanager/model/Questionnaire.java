package com.juliadrake.studiomanager.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

@Entity
public class Questionnaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @OneToOne(optional = false)
    private Project project;

    @Column(length = 2000)
    private String sessionGoals;

    @Column(length = 2000)
    private String mustHaveShots;

    @Column(length = 2000)
    private String comfortNotes;

    protected Questionnaire() {
    }

    public Questionnaire(Project project, String sessionGoals, String mustHaveShots, String comfortNotes) {
        this.project = project;
        this.sessionGoals = sessionGoals;
        this.mustHaveShots = mustHaveShots;
        this.comfortNotes = comfortNotes;
    }

    public Long getId() { return id; }
    public Project getProject() { return project; }
    public String getSessionGoals() { return sessionGoals; }
    public void setSessionGoals(String sessionGoals) { this.sessionGoals = sessionGoals; }
    public String getMustHaveShots() { return mustHaveShots; }
    public void setMustHaveShots(String mustHaveShots) { this.mustHaveShots = mustHaveShots; }
    public String getComfortNotes() { return comfortNotes; }
    public void setComfortNotes(String comfortNotes) { this.comfortNotes = comfortNotes; }
}
