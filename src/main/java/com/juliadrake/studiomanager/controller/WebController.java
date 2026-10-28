package com.juliadrake.studiomanager.controller;

import com.juliadrake.studiomanager.model.Agreement;
import com.juliadrake.studiomanager.model.Client;
import com.juliadrake.studiomanager.model.EditingStage;
import com.juliadrake.studiomanager.model.Project;
import com.juliadrake.studiomanager.model.ProjectStatus;
import com.juliadrake.studiomanager.model.Questionnaire;
import com.juliadrake.studiomanager.model.SessionPackage;
import com.juliadrake.studiomanager.model.TimeEntry;
import com.juliadrake.studiomanager.model.ActivityType;
import com.juliadrake.studiomanager.repository.ClientRepository;
import com.juliadrake.studiomanager.repository.ProjectRepository;
import com.juliadrake.studiomanager.repository.SessionPackageRepository;
import com.juliadrake.studiomanager.repository.TimeEntryRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Server-rendered pages: public booking flow + photographer dashboard. */
@Controller
public class WebController {

    private final SessionPackageRepository packages;
    private final ClientRepository clients;
    private final ProjectRepository projects;
    private final TimeEntryRepository timeEntries;

    public WebController(SessionPackageRepository packages, ClientRepository clients,
                         ProjectRepository projects, TimeEntryRepository timeEntries) {
        this.packages = packages;
        this.clients = clients;
        this.projects = projects;
        this.timeEntries = timeEntries;
    }

    // ---------- Public booking ----------

    @GetMapping("/")
    public String bookingPage(Model model) {
        model.addAttribute("packages", packages.findByActiveTrueOrderByBasePriceAsc());
        return "index";
    }

    @PostMapping("/book")
    public String book(@RequestParam String name,
                       @RequestParam String email,
                       @RequestParam(required = false) String phone,
                       @RequestParam Long packageId,
                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime sessionDateTime,
                       @RequestParam(required = false) String location,
                       @RequestParam(required = false) String message) {
        SessionPackage sessionPackage = packages.findById(packageId).orElseThrow();
        Client client = clients.save(new Client(name, email, phone));
        Project project = new Project(client, sessionPackage, sessionDateTime,
                location, sessionPackage.getBasePrice(), message);
        project.setStatus(ProjectStatus.INQUIRY);
        projects.save(project);
        return "redirect:/confirmation/" + project.getId();
    }

    @GetMapping("/confirmation/{id}")
    public String confirmation(@PathVariable Long id, Model model) {
        model.addAttribute("project", projects.findById(id).orElseThrow());
        return "confirmation";
    }

    // ---------- Photographer dashboard ----------

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(required = false) ProjectStatus status, Model model) {
        List<Project> list = status == null
                ? projects.findAllByOrderBySessionDateTimeAsc()
                : projects.findByStatusOrderBySessionDateTimeAsc(status);
        model.addAttribute("projects", list);
        model.addAttribute("statuses", ProjectStatus.values());
        model.addAttribute("selectedStatus", status);
        return "dashboard";
    }

    @GetMapping("/projects/{id}")
    public String projectDetail(@PathVariable Long id, Model model) {
        Project project = projects.findById(id).orElseThrow();
        model.addAttribute("project", project);
        model.addAttribute("statuses", ProjectStatus.values());
        model.addAttribute("stages", EditingStage.values());
        model.addAttribute("activities", ActivityType.values());
        model.addAttribute("timeEntries", timeEntries.findByProjectIdOrderByStartTimeAsc(id));
        return "project";
    }

    @PostMapping("/projects/{id}/details")
    public String updateDetails(@PathVariable Long id,
                                @RequestParam ProjectStatus status,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime sessionDateTime,
                                @RequestParam(required = false) String location,
                                @RequestParam(required = false) BigDecimal price,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate galleryDueDate,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate galleryDeliveredDate,
                                @RequestParam(required = false) String notes) {
        Project project = projects.findById(id).orElseThrow();
        project.setStatus(status);
        if (sessionDateTime != null) project.setSessionDateTime(sessionDateTime);
        project.setLocation(location);
        if (price != null) project.setPrice(price);
        project.setGalleryDueDate(galleryDueDate);
        project.setGalleryDeliveredDate(galleryDeliveredDate);
        project.setNotes(notes);
        projects.save(project);
        return "redirect:/projects/" + id;
    }

    @PostMapping("/projects/{id}/agreement")
    public String saveAgreement(@PathVariable Long id,
                                @RequestParam String signerName,
                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate signedDate,
                                @RequestParam(required = false) String termsText) {
        Project project = projects.findById(id).orElseThrow();
        Agreement agreement = project.getAgreement();
        if (agreement == null) {
            agreement = new Agreement(project, signerName, signedDate, termsText);
            project.setAgreement(agreement);
        } else {
            agreement.setSignerName(signerName);
            agreement.setSignedDate(signedDate);
            agreement.setTermsText(termsText);
        }
        projects.save(project);
        return "redirect:/projects/" + id;
    }

    @PostMapping("/projects/{id}/questionnaire")
    public String saveQuestionnaire(@PathVariable Long id,
                                    @RequestParam(required = false) String sessionGoals,
                                    @RequestParam(required = false) String mustHaveShots,
                                    @RequestParam(required = false) String comfortNotes) {
        Project project = projects.findById(id).orElseThrow();
        Questionnaire q = project.getQuestionnaire();
        if (q == null) {
            q = new Questionnaire(project, sessionGoals, mustHaveShots, comfortNotes);
            project.setQuestionnaire(q);
        } else {
            q.setSessionGoals(sessionGoals);
            q.setMustHaveShots(mustHaveShots);
            q.setComfortNotes(comfortNotes);
        }
        projects.save(project);
        return "redirect:/projects/" + id;
    }

    @PostMapping("/projects/{id}/stage")
    public String advanceStage(@PathVariable Long id, @RequestParam EditingStage editingStage) {
        Project project = projects.findById(id).orElseThrow();
        project.setEditingStage(editingStage);
        projects.save(project);
        return "redirect:/projects/" + id;
    }

    @PostMapping("/projects/{id}/time-entries")
    public String addTimeEntry(@PathVariable Long id,
                               @RequestParam ActivityType activityType,
                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
                               @RequestParam(defaultValue = "false") boolean manual,
                               @RequestParam(required = false) String notes,
                               RedirectAttributes redirect) {
        if (!endTime.isAfter(startTime)) {
            redirect.addFlashAttribute("timeError", "End time must be after start time.");
            return "redirect:/projects/" + id;
        }
        Project project = projects.findById(id).orElseThrow();
        timeEntries.save(new TimeEntry(project, activityType, startTime, endTime, manual, notes));
        return "redirect:/projects/" + id;
    }

    @PostMapping("/projects/{id}/time-entries/{entryId}/delete")
    public String deleteTimeEntry(@PathVariable Long id, @PathVariable Long entryId) {
        timeEntries.deleteById(entryId);
        return "redirect:/projects/" + id;
    }
}
