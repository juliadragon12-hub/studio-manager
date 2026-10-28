package com.juliadrake.studiomanager.controller;

import com.juliadrake.studiomanager.model.ActivityType;
import com.juliadrake.studiomanager.model.Client;
import com.juliadrake.studiomanager.model.EditingStage;
import com.juliadrake.studiomanager.model.Project;
import com.juliadrake.studiomanager.model.ProjectStatus;
import com.juliadrake.studiomanager.model.SessionPackage;
import com.juliadrake.studiomanager.model.TimeEntry;
import com.juliadrake.studiomanager.repository.ClientRepository;
import com.juliadrake.studiomanager.repository.ProjectRepository;
import com.juliadrake.studiomanager.repository.SessionPackageRepository;
import com.juliadrake.studiomanager.repository.TimeEntryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** REST API mirroring the web pages. All responses are JSON. */
@RestController
@RequestMapping("/api")
public class ApiController {

    private final SessionPackageRepository packages;
    private final ClientRepository clients;
    private final ProjectRepository projects;
    private final TimeEntryRepository timeEntries;

    public ApiController(SessionPackageRepository packages, ClientRepository clients,
                         ProjectRepository projects, TimeEntryRepository timeEntries) {
        this.packages = packages;
        this.clients = clients;
        this.projects = projects;
        this.timeEntries = timeEntries;
    }

    @GetMapping("/packages")
    public List<SessionPackage> listPackages() {
        return packages.findByActiveTrueOrderByBasePriceAsc();
    }

    @GetMapping("/projects")
    public List<Project> listProjects(@RequestParam(required = false) ProjectStatus status) {
        return status == null
                ? projects.findAllByOrderBySessionDateTimeAsc()
                : projects.findByStatusOrderBySessionDateTimeAsc(status);
    }

    @GetMapping("/projects/{id}")
    public Project getProject(@PathVariable Long id) {
        return projects.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public record CreateProjectRequest(
            String clientName, String clientEmail, String clientPhone,
            Long packageId, LocalDateTime sessionDateTime,
            String location, String message) {
    }

    @PostMapping("/projects")
    public ResponseEntity<Project> createProject(@RequestBody CreateProjectRequest req) {
        SessionPackage sessionPackage = packages.findById(req.packageId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown package"));
        Client client = clients.save(new Client(req.clientName(), req.clientEmail(), req.clientPhone()));
        Project project = new Project(client, sessionPackage, req.sessionDateTime(),
                req.location(), sessionPackage.getBasePrice(), req.message());
        project.setStatus(ProjectStatus.INQUIRY);
        return ResponseEntity.status(HttpStatus.CREATED).body(projects.save(project));
    }

    public record UpdateProjectRequest(
            ProjectStatus status, LocalDateTime sessionDateTime, String location,
            BigDecimal price, LocalDate galleryDueDate, LocalDate galleryDeliveredDate,
            String notes, EditingStage editingStage) {
    }

    @PutMapping("/projects/{id}")
    public Project updateProject(@PathVariable Long id, @RequestBody UpdateProjectRequest req) {
        Project project = projects.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (req.status() != null) project.setStatus(req.status());
        if (req.sessionDateTime() != null) project.setSessionDateTime(req.sessionDateTime());
        if (req.location() != null) project.setLocation(req.location());
        if (req.price() != null) project.setPrice(req.price());
        if (req.galleryDueDate() != null) project.setGalleryDueDate(req.galleryDueDate());
        if (req.galleryDeliveredDate() != null) project.setGalleryDeliveredDate(req.galleryDeliveredDate());
        if (req.notes() != null) project.setNotes(req.notes());
        if (req.editingStage() != null) project.setEditingStage(req.editingStage());
        return projects.save(project);
    }

    @GetMapping("/projects/{id}/time-entries")
    public List<TimeEntry> listTimeEntries(@PathVariable Long id) {
        if (!projects.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return timeEntries.findByProjectIdOrderByStartTimeAsc(id);
    }

    public record CreateTimeEntryRequest(
            ActivityType activityType, LocalDateTime startTime, LocalDateTime endTime,
            boolean manual, String notes) {
    }

    @PostMapping("/projects/{id}/time-entries")
    public ResponseEntity<?> addTimeEntry(@PathVariable Long id, @RequestBody CreateTimeEntryRequest req) {
        Project project = projects.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (req.endTime() == null || req.startTime() == null || !req.endTime().isAfter(req.startTime())) {
            return ResponseEntity.badRequest().body(Map.of("error", "End time must be after start time."));
        }
        TimeEntry entry = new TimeEntry(project, req.activityType(), req.startTime(), req.endTime(), req.manual(), req.notes());
        return ResponseEntity.status(HttpStatus.CREATED).body(timeEntries.save(entry));
    }

    @PutMapping("/projects/{id}/stage")
    public Project updateStage(@PathVariable Long id, @RequestBody Map<String, EditingStage> body) {
        Project project = projects.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        project.setEditingStage(body.get("editingStage"));
        return projects.save(project);
    }
}
