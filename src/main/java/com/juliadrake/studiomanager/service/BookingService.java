package com.juliadrake.studiomanager.service;

import com.juliadrake.studiomanager.controller.BookingForm;
import com.juliadrake.studiomanager.model.Agreement;
import com.juliadrake.studiomanager.model.Client;
import com.juliadrake.studiomanager.model.MiniSessionEvent;
import com.juliadrake.studiomanager.model.Payment;
import com.juliadrake.studiomanager.model.PaymentStatus;
import com.juliadrake.studiomanager.model.Project;
import com.juliadrake.studiomanager.model.ProjectStatus;
import com.juliadrake.studiomanager.model.Questionnaire;
import com.juliadrake.studiomanager.model.SessionPackage;
import com.juliadrake.studiomanager.repository.ClientRepository;
import com.juliadrake.studiomanager.repository.MiniSessionEventRepository;
import com.juliadrake.studiomanager.repository.ProjectRepository;
import com.juliadrake.studiomanager.repository.SessionPackageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

/**
 * The one-flow booking: validates a submitted BookingForm end to end and,
 * only if everything checks out, creates the booked project with its
 * questionnaire, e-signed agreement, and demo retainer payment in a
 * single transaction. Any problem raises BookingException with a
 * client-facing message and nothing is saved.
 */
@Service
public class BookingService {

    /** The agreement terms shown in the flow and stored verbatim on each signed agreement. */
    public static final String STANDARD_TERMS = """
            Session Agreement — Julia Drake Photography

            1. Retainer. A retainer of 25% of the session price reserves your date. The remaining balance is due on the day of your session.
            2. Rescheduling. You may reschedule once, with at least 7 days' notice, at no extra charge. The retainer is not refundable if you cancel.
            3. The session. Your session lasts the time in your package (mini sessions last their slot length). Please arrive on time — late arrivals shorten the session.
            4. Your gallery. An online gallery of edited images is delivered within 4 weeks of your session (mini-session galleries within 2 weeks).
            5. Image use. The photographer may share images from your session in her portfolio and on social media unless you tell her in writing beforehand that you prefer she does not.
            6. Copyright. Copyright stays with the photographer; you receive a personal print release for your images.""";

    private final SessionPackageRepository packages;
    private final ClientRepository clients;
    private final ProjectRepository projects;
    private final MiniSessionEventRepository miniEvents;
    private final MiniSessionService miniSessions;

    public BookingService(SessionPackageRepository packages, ClientRepository clients,
                          ProjectRepository projects, MiniSessionEventRepository miniEvents,
                          MiniSessionService miniSessions) {
        this.packages = packages;
        this.clients = clients;
        this.projects = projects;
        this.miniEvents = miniEvents;
        this.miniSessions = miniSessions;
    }

    @Transactional
    public Project completeBooking(BookingForm form) {
        // --- Agreement gate: nothing books without a signature + consent. ---
        if (isBlank(form.getSignerName())) {
            throw new BookingException("Please type your full name to sign the agreement (step 5).");
        }
        if (!Boolean.TRUE.equals(form.getAgreeConsent())) {
            throw new BookingException("Please tick the box in step 5 confirming that you agree to the session agreement.");
        }
        if (isBlank(form.getName()) || isBlank(form.getEmail())) {
            throw new BookingException("Please add your name and email (step 3) so we can reach you about your session.");
        }

        // --- Package, price, and when — full session or mini slot. ---
        SessionPackage sessionPackage;
        BigDecimal price;
        LocalDateTime when;
        MiniSessionEvent miniEvent = null;
        String location = form.getLocation();

        if ("MINI".equals(form.getBookingType())) {
            MiniSlotChoice choice = parseMiniSlot(form.getMiniSlot());
            miniEvent = miniEvents.findById(choice.eventId())
                    .orElseThrow(() -> new BookingException("That mini-session event is no longer available — please pick another slot."));
            if (!miniEvent.slotStarts().contains(choice.slotStart())) {
                throw new BookingException("Please choose one of the listed mini-session slots.");
            }
            // Capacity is checked here, at submit time, so a direct POST
            // cannot overbook a slot that filled up after the page loaded.
            if (miniSessions.bookedCount(miniEvent, choice.slotStart()) >= miniEvent.getCapacityPerSlot()) {
                throw new BookingException("Sorry — that mini-session slot just filled up. Please choose another slot.");
            }
            sessionPackage = miniEvent.getSessionPackage();
            price = miniEvent.getPrice();
            when = choice.slotStart();
            if (isBlank(location)) {
                location = miniEvent.getLocation();
            }
        } else {
            if (form.getPackageId() == null) {
                throw new BookingException("Please choose a session package (step 1).");
            }
            if (form.getSessionDateTime() == null) {
                throw new BookingException("Please pick your preferred date and time (step 2).");
            }
            sessionPackage = packages.findById(form.getPackageId())
                    .orElseThrow(() -> new BookingException("That package is no longer available — please choose another."));
            price = sessionPackage.getBasePrice();
            when = form.getSessionDateTime();
        }

        // --- Assemble the booking. ---
        Client client = clients.save(new Client(form.getName().trim(), form.getEmail().trim(), form.getPhone()));
        Project project = new Project(client, sessionPackage, when, location, price, form.getMessage());
        project.setStatus(ProjectStatus.BOOKED);
        project.setMiniEvent(miniEvent);
        projects.save(project); // first save assigns the id the reference is built from
        project.setBookingReference(bookingReferenceFor(project));

        LocalDateTime now = LocalDateTime.now();
        Agreement agreement = new Agreement(project, form.getSignerName().trim(), now.toLocalDate(), STANDARD_TERMS);
        agreement.setSignedAt(now);
        project.setAgreement(agreement);
        project.setQuestionnaire(new Questionnaire(project,
                form.getSessionGoals(), form.getMustHaveShots(), form.getComfortNotes()));

        // Demo checkout: record the retainer as paid-in-demo. No real
        // payment is processed and no card details are ever collected.
        BigDecimal retainer = project.getRetainerDue();
        project.getPayments().add(new Payment(project, "Retainer", retainer,
                PaymentStatus.PAID_DEMO, "DEMO", now));

        return projects.save(project);
    }

    /** SM-{session year}-{project id, zero padded}, e.g. SM-2026-0007. */
    static String bookingReferenceFor(Project project) {
        int year = project.getSessionDateTime() != null
                ? project.getSessionDateTime().getYear()
                : LocalDateTime.now().getYear();
        return String.format("SM-%d-%04d", year, project.getId());
    }

    private record MiniSlotChoice(long eventId, LocalDateTime slotStart) {
    }

    /** Parses the form's "{miniEventId}|{ISO slot start}" slot value. */
    private static MiniSlotChoice parseMiniSlot(String miniSlot) {
        if (miniSlot == null || !miniSlot.contains("|")) {
            throw new BookingException("Please choose a mini-session slot (step 2).");
        }
        String[] parts = miniSlot.split("\\|", 2);
        try {
            return new MiniSlotChoice(Long.parseLong(parts[0]), LocalDateTime.parse(parts[1]));
        } catch (NumberFormatException | DateTimeParseException e) {
            throw new BookingException("Please choose one of the listed mini-session slots.");
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
