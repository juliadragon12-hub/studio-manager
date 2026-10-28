# Studio Manager

Client booking and session management for a photography business — built with **Java 21 + Spring Boot 3**.

A client visits the public booking page, picks a session package, and requests a date. The photographer manages everything from the dashboard: bookings move from inquiry to booked to completed, with an agreement, a comfort questionnaire, a four-stage editing pipeline, gallery due/delivered dates, and time tracking (including hand-logged time for when you forget to track live).

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
- **Photographer dashboard:** http://localhost:8080/dashboard
- **H2 console:** http://localhost:8080/h2-console (JDBC URL `jdbc:h2:file:./data/studiodb`, user `sa`, no password)

To build a runnable jar instead:

```bash
./mvnw clean package
java -jar target/studio-manager-1.0.0.jar
```

## What it does

1. **Public booking (`/`)** — shows the session packages and a booking form (name, email, phone, package, preferred date/time, location, message). Submitting creates a project with status `INQUIRY` and shows a confirmation page.
2. **Dashboard (`/dashboard`)** — all projects sorted by session date, filterable by status (`INQUIRY`, `BOOKED`, `COMPLETED`, `CANCELLED`).
3. **Project detail (`/projects/{id}`)** — edit status, session date, location, price, and gallery due/delivered dates; record the client agreement (signer + date + terms); save the comfort questionnaire (session goals, must-have shots, comfort notes); advance the editing pipeline (`CULLING → EDITING → REVIEW → DELIVERED`); log time entries by activity (`SHOOT`, `EDITING`, `ADMIN`, `TRAVEL`, `OTHER`) with start/end validation (end must be after start) and a manual-entry flag.

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
- **Project** — client, package, status, session date/time, location, price, gallery due/delivered dates, notes, editing stage.
- **Agreement** — signer name, signed date, terms text (one per project).
- **Questionnaire** — session goals, must-have shots, comfort notes (one per project).
- **TimeEntry** — activity type, start/end, computed duration, manual flag, notes.

## Demo data

On first run the app seeds the three packages above plus two **fictional** demo projects ("Emma & Liam", a booked couples session, and "Noah Bennett", a completed senior session) so the dashboard isn't empty. No real client data is in this repository.

## Built with AI assistance

This project was built AI-assisted: Julia Drake directed the design, the feature list, and the business rules, and an AI coding assistant wrote the code under her direction. She reviewed and owns the result. That workflow — directing an AI to build real, working software — is the point of this repository.

## Roadmap

- **v2 — Playbook / posing library:** the photographer's posing guide — searchable poses with prompts, variations, and favorites, plus a session-mode step-through for shoot day.
- **v2 — Client portal:** clients view their gallery status and agreement online.
- **v2 — Payments:** deposit collection at booking time.
- **v2 — Email notifications:** booking confirmations and gallery-ready emails.
