package com.juliadrake.studiomanager.config;

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
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Seeds the database on first run with the real published packages and
 * fictional demo projects. All demo people are invented — no real clients.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final SessionPackageRepository packages;
    private final ClientRepository clients;
    private final ProjectRepository projects;

    public DataSeeder(SessionPackageRepository packages,
                      ClientRepository clients,
                      ProjectRepository projects) {
        this.packages = packages;
        this.clients = clients;
        this.projects = projects;
    }

    @Override
    public void run(String... args) {
        if (packages.count() > 0) {
            return; // already seeded
        }

        SessionPackage couples = packages.save(new SessionPackage(
                "Couples",
                "A relaxed one-hour couples session outdoors — golden hour, natural light, and plenty of time to just be yourselves.",
                new BigDecimal("200")));
        SessionPackage senior = packages.save(new SessionPackage(
                "Senior Portraits",
                "A one-hour senior portrait session at a location of your choice, with outfit changes and a mix of posed and candid shots.",
                new BigDecimal("150")));
        SessionPackage maternity = packages.save(new SessionPackage(
                "Maternity",
                "A gentle one-hour maternity session celebrating this season — partners and siblings welcome.",
                new BigDecimal("180")));

        // Demo project 1: an upcoming booked couples session.
        Client emmaLiam = clients.save(new Client("Emma & Liam", "emma.liam@example.com", "555-0101"));
        Project p1 = new Project(emmaLiam, couples,
                LocalDateTime.now().plusDays(6).withHour(17).withMinute(15).withSecond(0).withNano(0),
                "Riverside Park", new BigDecimal("200"),
                "Demo project — golden hour at the park, they love candid shots.");
        p1.setStatus(ProjectStatus.BOOKED);
        p1.setGalleryDueDate(LocalDate.now().plusDays(27));
        projects.save(p1);

        // Demo project 2: a completed senior session, gallery delivered.
        Client noah = clients.save(new Client("Noah Bennett", "noah.bennett@example.com", "555-0102"));
        Project p2 = new Project(noah, senior,
                LocalDateTime.now().minusDays(12).withHour(16).withMinute(0).withSecond(0).withNano(0),
                "Downtown & the old library steps", new BigDecimal("150"),
                "Demo project — senior portraits, cap-and-gown shots included.");
        p2.setStatus(ProjectStatus.COMPLETED);
        p2.setEditingStage(EditingStage.DELIVERED);
        p2.setGalleryDueDate(LocalDate.now().minusDays(2));
        p2.setGalleryDeliveredDate(LocalDate.now().minusDays(1));
        projects.save(p2);
        // A hand-logged editing block on the delivered project.
        p2.getTimeEntries().add(new TimeEntry(p2, ActivityType.EDITING,
                LocalDateTime.now().minusDays(5).withHour(19).withMinute(0),
                LocalDateTime.now().minusDays(5).withHour(21).withMinute(30),
                true, "Demo entry — evening edit, logged by hand."));
        projects.save(p2);
    }
}
