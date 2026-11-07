package com.juliadrake.studiomanager;

import com.juliadrake.studiomanager.model.MiniSessionEvent;
import com.juliadrake.studiomanager.model.Project;
import com.juliadrake.studiomanager.model.ProjectStatus;
import com.juliadrake.studiomanager.model.SessionPackage;
import com.juliadrake.studiomanager.repository.MiniSessionEventRepository;
import com.juliadrake.studiomanager.repository.ProjectRepository;
import com.juliadrake.studiomanager.repository.SessionPackageRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end tests of the one-flow booking: a full session completes with
 * a signed agreement and a demo retainer; a mini slot cannot be
 * overbooked; and the agreement gate rejects unsigned submissions.
 * All people here are fictional test data.
 */
@SpringBootTest
@AutoConfigureMockMvc
class BookingFlowTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private SessionPackageRepository packages;

    @Autowired
    private MiniSessionEventRepository miniEvents;

    @Autowired
    private ProjectRepository projects;

    /** The cheapest seeded package is Senior Portraits at $150. */
    private SessionPackage cheapestPackage() {
        return packages.findByActiveTrueOrderByBasePriceAsc().get(0);
    }

    @Test
    void fullBookingCompletesWithSignedAgreementAndDemoRetainer() throws Exception {
        SessionPackage pkg = cheapestPackage();
        assertEquals(0, pkg.getBasePrice().compareTo(new BigDecimal("150")));

        MvcResult result = mvc.perform(post("/book/complete")
                        .param("bookingType", "FULL")
                        .param("packageId", String.valueOf(pkg.getId()))
                        .param("sessionDateTime", "2026-12-05T17:00")
                        .param("name", "Test Person")
                        .param("email", "test.person@example.com")
                        .param("phone", "555-0199")
                        .param("location", "Test Park")
                        .param("message", "Looking forward to it")
                        .param("sessionGoals", "Relaxed, natural photos")
                        .param("mustHaveShots", "One by the river")
                        .param("comfortNotes", "Candids over poses")
                        .param("signerName", "Test Person")
                        .param("agreeConsent", "true"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        String url = result.getResponse().getRedirectedUrl();
        assertNotNull(url);
        Long id = Long.valueOf(url.substring(url.lastIndexOf('/') + 1));

        Project project = projects.findById(id).orElseThrow();
        assertEquals(ProjectStatus.BOOKED, project.getStatus());
        assertNotNull(project.getBookingReference());
        assertTrue(project.getBookingReference().startsWith("SM-2026-"),
                "reference was " + project.getBookingReference());

        // Agreement: e-signed, with a timestamp and the standard terms stored.
        assertTrue(project.isAgreementSigned());
        assertEquals("Test Person", project.getAgreement().getSignerName());
        assertNotNull(project.getAgreement().getSignedAt());
        assertTrue(project.getAgreement().getTermsText().contains("Retainer"));

        // Questionnaire was attached.
        assertEquals("Relaxed, natural photos", project.getQuestionnaire().getSessionGoals());

        // Payments are a lazy collection on the entity, so assert them
        // through the API JSON — which also covers the extended project
        // representation. Retainer: 25% of $150 = $37.50 → $38 rounded;
        // balance $112.
        mvc.perform(get("/api/projects/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingReference").value(project.getBookingReference()))
                .andExpect(jsonPath("$.agreementSigned").value(true))
                .andExpect(jsonPath("$.payments.length()").value(1))
                .andExpect(jsonPath("$.payments[0].label").value("Retainer"))
                .andExpect(jsonPath("$.payments[0].status").value("PAID_DEMO"))
                .andExpect(jsonPath("$.payments[0].method").value("DEMO"))
                .andExpect(jsonPath("$.payments[0].amount").value(38))
                .andExpect(jsonPath("$.retainerDue").value(38))
                .andExpect(jsonPath("$.balanceDue").value(112));

        // The confirmation page shows the booking reference.
        mvc.perform(get("/confirmation/" + id))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(project.getBookingReference())));
    }

    @Test
    void miniSlotCannotBeOverbooked() throws Exception {
        SessionPackage pkg = cheapestPackage();
        MiniSessionEvent event = miniEvents.save(new MiniSessionEvent(
                "Capacity Test Minis", LocalDate.now().plusDays(10),
                LocalTime.of(10, 0), LocalTime.of(10, 20),
                20, 1, new BigDecimal("75"), "Test Field", pkg));
        String slotStart = event.slotStarts().get(0)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        String slotValue = event.getId() + "|" + slotStart;

        // First booking takes the only seat.
        mvc.perform(post("/book/complete")
                        .param("bookingType", "MINI")
                        .param("miniSlot", slotValue)
                        .param("name", "First Booker")
                        .param("email", "first.booker@example.com")
                        .param("signerName", "First Booker")
                        .param("agreeConsent", "true"))
                .andExpect(status().is3xxRedirection());

        // A second booking on the same slot is rejected, even as a direct POST.
        mvc.perform(post("/book/complete")
                        .param("bookingType", "MINI")
                        .param("miniSlot", slotValue)
                        .param("name", "Second Booker")
                        .param("email", "second.booker@example.com")
                        .param("signerName", "Second Booker")
                        .param("agreeConsent", "true"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("filled up")));

        assertEquals(1, projects.countByMiniEventIdAndSessionDateTimeAndStatusNot(
                event.getId(), event.slotStarts().get(0), ProjectStatus.CANCELLED));
    }

    @Test
    void bookingWithoutSignatureOrConsentIsRejected() throws Exception {
        SessionPackage pkg = cheapestPackage();
        long before = projects.count();

        // No typed signature name.
        mvc.perform(post("/book/complete")
                        .param("bookingType", "FULL")
                        .param("packageId", String.valueOf(pkg.getId()))
                        .param("sessionDateTime", "2026-12-06T17:00")
                        .param("name", "Unsigned Person")
                        .param("email", "unsigned@example.com")
                        .param("agreeConsent", "true"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("sign the agreement")));

        // Signature but no consent checkbox.
        mvc.perform(post("/book/complete")
                        .param("bookingType", "FULL")
                        .param("packageId", String.valueOf(pkg.getId()))
                        .param("sessionDateTime", "2026-12-06T17:00")
                        .param("name", "Unconsenting Person")
                        .param("email", "unconsenting@example.com")
                        .param("signerName", "Unconsenting Person"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("tick the box")));

        assertEquals(before, projects.count());
    }
}
