# Studio Manager

Client booking and session management for a photography business — built with **Java 21 + Spring Boot 3**.

A client books in one flow — package, date or mini-session slot, details, questionnaire, e-signed agreement, and a retainer to hold the date — and lands on a confirmation with a booking reference. The photographer manages everything from the dashboard: bookings move from inquiry to booked to completed, with an agreement, a comfort questionnaire, a four-stage editing pipeline, gallery due/delivered dates, payment and balance tracking, and time tracking (including hand-logged time for when you forget to track live). Mini-session events (an afternoon of short back-to-back slots) are managed from their own admin page.

This is the working tool behind **Julia Drake Photography** (Grove City, PA), rebuilt as a standalone Java app.

## Tech stack

- Java 21, Spring Boot 3.3
- Spring Web (Thymeleaf server-rendered pages) + Spring Data JPA
- H2 database, file-based (`./data/studiodb`) so data persists between runs
- Maven (wrapper included — no local Maven install needed)

## Run it

```bash
./mvnw spring-boot:run
```

Then open:

- **Booking page:** http://localhost:8080/
- **One-flow booking:** http://localhost:8080/book
- **Photographer dashboard:** http://localhost:8080/dashboard
- **Mini-session admin:** http://localhost:8080/admin/minis
- **H2 console:** http://localhost:8080/h2-console (JDBC URL `jdbc:h2:file:./data/studiodb`, user `sa`, no password)

To build a runnable jar instead:

```bash
./mvnw clean package
java -jar target/studio-manager-1.0.0.jar
```

## What it does

1. **One-flow booking (`/book`)** — one page, six visible steps, nothing to chase by email afterwards:
   1. **Package** — pick a session package.
   2. **Date & time** — for a full session, request a date/time; for a **mini session**, pick an open slot of a published mini-session event (slots show how many seats are left, and full slots can't be picked).
   3. **Details** — name, email, phone, location ideas, notes.
   4. **Questionnaire** — session goals, must-have shots, comfort notes.
   5. **Agreement** — the client reads the terms, types their full name as an e-signature, and ticks a consent box. Both are required and validated on the server; the signer name and the exact signing timestamp are stored with the terms they agreed to.
   6. **Retainer** — a **demo checkout** (see below) records the retainer, and the booking is complete.
   
   A completed booking creates a project with status `BOOKED`, a booking reference (e.g. `SM-2026-0007`), the signed agreement, the questionnaire, and the retainer payment. The confirmation page shows the reference, the session summary, the retainer paid, the balance due, and what happens next.
2. **Mini sessions (`/admin/minis`)** — the photographer creates mini-session events (title, date, start/end time, slot length, seats per slot, price). Slots are derived from the times; the admin page and the booking flow both show per-slot booked/remaining counts. Capacity is enforced on the server at submit time, so a slot that filled up after the page loaded still can't be double-booked — not even by posting the form directly.
3. **Dashboard (`/dashboard`)** — all projects sorted by session date, filterable by status (`INQUIRY`, `BOOKED`, `COMPLETED`, `CANCELLED`), with booking references and balances due.
4. **Project detail (`/projects/{id}`)** — booking & payment summary (reference, agreement status with signer and timestamp, retainer record, balance due); edit status, session date, location, price, and gallery due/delivered dates; record the client agreement (signer + date + terms); save the comfort questionnaire (session goals, must-have shots, comfort notes); advance the editing pipeline (`CULLING → EDITING → REVIEW → DELIVERED`); log time entries by activity (`SHOOT`, `EDITING`, `ADMIN`, `TRAVEL`, `OTHER`) with start/end validation (end must be after start) and a manual-entry flag.

### Retainer rule and the demo checkout

- The retainer is **25% of the session price, rounded to whole dollars** (e.g. $50 on a $200 session, $38 on a $150 session). The balance — price minus everything paid — is shown on the confirmation page, the dashboard, and the project detail page.
- The checkout is a **demo**: the step is clearly labeled "Demo checkout," the card fields are disabled placeholders, and **no real payment is processed and no card details are ever collected or stored**. Submitting records a payment with status `PAID_DEMO` and method `DEMO` so the rest of the flow (balances, records) works exactly as it will with a real processor. Wiring up real payment processing is a future decision (see the roadmap).

## REST API

The pages above are backed by a JSON API under `/api`:

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/packages` | List active session packages |
| GET | `/api/projects` | List projects (optional `?status=BOOKED`) |
| GET | `/api/projects/{id}` | One project |
| POST | `/api/projects` | Create a booking (status `INQUIRY`) |
| PUT | `/api/projects/{id}` | Update status, dates, price, notes, editing stage |
| GET | `/api/projects/{id}/time-entries` | Time entries for a project |
| POST | `/api/projects/{id}/time-entries` | Add a time entry (end must be after start) |
| PUT | `/api/projects/{id}/stage` | Set the editing stage |
| GET | `/api/mini-events` | Upcoming mini-session events with per-slot availability |

Project JSON includes the booking fields added by the one-flow booking: `bookingReference`, `agreementSigned`, `signedAt` on the agreement, the `payments` list, and the computed `retainerDue`, `totalPaid`, and `balanceDue`.

Example — create a booking:

```bash
curl -X POST http://localhost:8080/api/projects \
  -H 'Content-Type: application/json' \
  -d '{"clientName":"Emma & Liam","clientEmail":"emma.liam@example.com","packageId":1,
       "sessionDateTime":"2026-11-07T17:15:00","location":"Riverside Park"}'
```

Example — set the editing stage:

```bash
curl -X PUT http://localhost:8080/api/projects/1/stage \
  -H 'Content-Type: application/json' \
  -d '{"editingStage":"EDITING"}'
```

## Data model

- **SessionPackage** — name, description, base price. Seeded with the real published packages: Couples (from $200), Senior Portraits (from $150), Maternity (from $180).
- **Client** — name, email, phone.
- **Project** — client, package, status, session date/time, location, price, gallery due/delivered dates, notes, editing stage, booking reference, and (for mini bookings) the mini-session event it took a slot in.
- **Agreement** — signer name, signed date, e-signature timestamp, terms text (one per project).
- **Questionnaire** — session goals, must-have shots, comfort notes (one per project).
- **TimeEntry** — activity type, start/end, computed duration, manual flag, notes.
- **MiniSessionEvent** — title, date, start/end time, slot length, capacity per slot, price, location, package. Slots are derived from the times, not stored; a slot's bookings are the projects pointing at the event with that slot's start time.
- **Payment** — label, amount, status, method, paid-at timestamp (many per project). In this build the only source is the demo checkout: status `PAID_DEMO`, method `DEMO`.

## Demo data

On first run the app seeds the three packages above plus two **fictional** demo projects ("Emma & Liam", a booked couples session, and "Noah Bennett", a completed senior session) so the dashboard isn't empty, and one fictional demo mini-session event ("Autumn Mini Sessions — Demo", the next Saturday, 20-minute slots) so the mini flow is clickable out of the box. No real client data is in this repository.

## Built with AI assistance

This project was built AI-assisted: Julia Drake directed the design, the feature list, and the business rules, and an AI coding assistant wrote the code under her direction. She reviewed and owns the result. That workflow — directing an AI to build real, working software — is the point of this repository.

## Roadmap

- **v2 — Playbook / posing library:** the photographer's posing guide — searchable poses with prompts, variations, and favorites, plus a session-mode step-through for shoot day.
- **v2 — Client portal:** clients view their gallery status and agreement online.
- **Future decision — Real payments:** replace the demo checkout with a real processor (e.g. Stripe) so the 25% retainer is actually collected at booking time. The payment model and balance math are already in place.
- **Future decision — Hosting:** the app currently runs locally with its file-based H2 database; public hosting (and the database choice that goes with it) is still to be decided.
- **v2 — Email notifications:** booking confirmations and gallery-ready emails.
