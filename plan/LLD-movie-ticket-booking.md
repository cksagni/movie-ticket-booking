# Low-Level Design (LLD): Movie Ticket Booking System

## 1. Objective
Design a reliable movie ticket booking backend that supports:
- City-wise movie discovery
- Theatre/screen/show browsing
- Seat selection and booking
- Payment and booking confirmation
- Cancellation (and optional refund flow)

This LLD is aligned to the current Spring Boot + JPA project structure under `src/main/java/com/springboot/mtbs`.

## 2. Scope
### In Scope
- Core domain model and relationships
- REST APIs for customer booking flow
- Data consistency for concurrent seat booking
- Payment status handling
- Basic operational concerns (validation, errors, observability)

### Out of Scope (for current phase)
- 3rd-party payment gateway integration details
- Recommendation/search ranking engine
- Dynamic pricing algorithms
- Loyalty, coupons, wallet

## 3. Functional Requirements
1. User can list cities and movies available in a city.
2. User can list theatres and shows for a selected movie/date/city.
3. User can view seat layout and availability for a show.
4. User can reserve 1 to 10 seats for a show (max 10 seats per booking).
5. User can pay for a reservation and receive a booking confirmation.
6. System prevents double-booking of the same seat for a show.
7. User can view booking history and booking status.
8. User can cancel booking before show cutoff and trigger refund flow (if paid).

## 4. Non-Functional Requirements
- Consistency: no double-booking.
- Availability: read-heavy APIs should remain responsive.
- Performance target:
  - Browse APIs: p95 < 300 ms
  - Booking submit: p95 < 1.5 s (excluding external gateway latency)
- Security: authenticated booking APIs, hashed passwords, PII-safe logs.
- Observability: structured logs + request correlation id + metrics.

## 5. High-Level Component Design
- `CityController`, `MovieController`, `ShowController`, `BookingController`, `PaymentController`
- Service layer:
  - `CityService`, `MovieService`, `ShowService`
  - `BookingService` (reservation + seat lock/commit)
  - `PaymentService` (payment state transition)
- Repository layer (Spring Data JPA):
  - Existing repositories in `dao/`
  - Additional custom queries for show-seat availability and locking
- Database: MySQL (existing project choice)

### Suggested package structure
- `controller/` REST endpoints
- `service/` business logic
- `dao/` repositories + custom queries
- `entity/` JPA entities
- `dto/` request-response contracts
- `exception/` domain exceptions + global handler

## 6. Detailed Domain Model
Current entities:
- `User`, `City`, `Movie`, `Theatre`, `Screen`, `Show`, `Seat`, `Reservation`, `Payment`

### Key relationship notes
- `City` <-> `Movie`: Many-to-Many (`city_movie`)
- `Theatre` -> `Screen`: One-to-Many
- `Screen` -> `Seat`: One-to-Many (physical seats)
- `Show` -> `Movie`, `Screen`: Many-to-One each
- `Reservation` -> `User`, `Show`: Many-to-One each
- `Payment` -> `Reservation`: currently Many-to-One, should become One-to-One for single-payment booking in v1

### Important design correction
Current `Seat.is_reserved` is screen-level and not show-level. This cannot support multiple show timings correctly.

Introduce `ShowSeat`:
- `id`
- `show_id` (FK)
- `seat_id` (FK physical seat)
- `status` (`AVAILABLE`, `LOCKED`, `BOOKED`)
- `locked_by_user_id` (nullable)
- `lock_expires_at` (nullable)
- `price` (optional override)
- unique key (`show_id`, `seat_id`)

This table is the source of truth for seat availability per show.

## 7. Database Design (Logical)
### Existing tables
- `users`, `cities`, `movies`, `city_movie`, `theaters`, `screens`, `seats`, `shows`, `reservations`, `payments`

### Recommended additions/changes
1. Add `show_seats` table (mandatory for correctness).
2. Add `reservation_seats` join table:
   - `reservation_id`, `show_seat_id`
3. Add status columns:
   - `reservations.status`: `PENDING`, `CONFIRMED`, `FAILED`, `CANCELLED`, `EXPIRED`
   - `payments.status`: `INITIATED`, `SUCCESS`, `FAILED`, `REFUNDED`
4. Add unique/indexes:
   - `show_seats(show_id, seat_id)` unique
   - `show_seats(show_id, status)`
   - `shows(movie_id, show_time)`
   - `reservations(user_id, created_at)`

## 8. API Design (v1)
### Discovery APIs
1. `GET /cities`
2. `GET /cities/{cityId}/movies`
3. `GET /movies/{movieId}/shows?cityId={id}&date=YYYY-MM-DD`
4. `GET /shows/{showId}/seats`

### Booking APIs
1. `POST /bookings/hold`
   - Request: `userId`, `showId`, `seatIds[]`
   - Response: `reservationId`, `expiresAt`, `amount`
2. `POST /bookings/{reservationId}/pay`
   - Request: `userId`, `paymentMethod`, `paymentToken`
   - Response: `bookingStatus`, `paymentStatus`, `ticketRef`
3. `GET /bookings/{reservationId}?userId={id}`
4. `POST /bookings/{reservationId}/cancel`
   - Request: `userId`

### Admin APIs (minimal)
1. `POST /admin/cities`
2. `POST /admin/theatres`
3. `POST /admin/theatres/{theatreId}/screens`
4. `POST /admin/shows`
5. `POST /admin/shows/{showId}/show-seats/init`

## 9. Core Booking Flow (Detailed)
### 9.1 Seat Hold
1. Validate user/show/seat input.
2. Start transaction.
3. Lock target `show_seats` rows using pessimistic write lock.
4. Ensure all requested seats are `AVAILABLE` or expired `LOCKED`.
5. Mark as `LOCKED`, set `lock_expires_at` (example: now + 5 minutes), `locked_by_user_id`.
6. Create `reservation` with status `PENDING`.
7. Insert rows into `reservation_seats`.
8. Commit and return hold info.

### 9.2 Payment + Confirmation
1. Validate reservation belongs to user and not expired.
2. Start transaction.
3. Re-check lock validity on `show_seats`.
4. Create `payment` with `INITIATED`.
5. Call payment provider (outside DB lock if async pattern; or after short lock window).
6. On success:
   - `payments.status = SUCCESS`
   - `show_seats.status = BOOKED`
   - clear lock fields
   - `reservations.status = CONFIRMED`
7. On failure:
   - `payments.status = FAILED`
   - release seat locks to `AVAILABLE`
   - `reservations.status = FAILED`

### 9.3 Lock Expiry Job
- Scheduled task every 30-60 seconds:
  - Find `show_seats` where `status=LOCKED` and `lock_expires_at < now()`
  - Set status to `AVAILABLE`, clear lock metadata
  - Mark corresponding `PENDING` reservations as `EXPIRED`

## 10. Concurrency & Consistency Strategy
- Use DB transaction boundaries at service layer.
- For seat locking:
  - `SELECT ... FOR UPDATE` on `show_seats` rows.
  - Keep lock duration short.
- Enforce uniqueness and status checks in DB and code.
- Idempotency:
  - Accept `Idempotency-Key` for `/pay` to avoid duplicate charge/confirm on retries.

## 11. Validation & Error Handling
- Validation:
  - show must be in future for new bookings.
  - show creation requires screen to be linked to a theatre.
  - seat ids must belong to show’s screen.
  - max seats per booking (configurable, example 10).
- Domain errors:
  - `SEAT_NOT_AVAILABLE`
  - `RESERVATION_EXPIRED`
  - `PAYMENT_FAILED`
  - `BOOKING_CANCELLATION_NOT_ALLOWED`
- Return normalized error payload:
  - `code`, `message`, `details`, `traceId`

## 12. Security
- Authentication via JWT/session.
- Authorization:
  - user can access own bookings only.
  - admin endpoints role-gated.
- Store password hashes (BCrypt/Argon2), never plaintext.
- Mask payment and PII fields in logs.

## 13. Observability
- Metrics:
  - hold success/failure rate
  - payment success/failure rate
  - lock expiry count
  - booking confirmation latency
- Logs:
  - include `traceId`, `userId`, `reservationId`, `showId` for booking events.
- Alerts:
  - payment failure spike
  - seat lock contention spike

## 14. Implementation Mapping to Current Codebase
### Implemented (Current)
1. Added entities:
   - `ShowSeat`, `ReservationSeat`
2. Extended repositories with locking queries:
   - `ShowSeatRepository` with pessimistic lock methods
3. Added services:
   - `ShowService`, `BookingService`, `PaymentService`, `TheatreService`
4. Added controllers:
   - `ShowController`, `BookingController`, `AdminShowController`, `AdminSetupController`
5. Added DTOs and global exception handler.
6. Added scheduler for lock expiry.
7. Added unit tests covering booking/payment/cancel, show discovery/setup, city setup, theatre/screen setup.

### Remaining / Next
1. Add integration tests for true concurrent booking with database locks.
2. Introduce schema migrations (Flyway/Liquibase).
3. Add authentication/authorization for admin and user endpoints.

## 15. Risks & Mitigations
- Risk: high contention on popular shows.
  - Mitigation: short lock timeout + selective row locks + queue throttling.
- Risk: payment timeout after hold.
  - Mitigation: async payment callback handling + explicit reservation expiry.
- Risk: schema drift from current entities.
  - Mitigation: introduce Flyway/Liquibase migrations and lock schema changes.

## 16. Suggested Milestones
1. Milestone 1: Schema updates + seat hold API + basic tests.
2. Milestone 2: Payment integration contract + booking confirmation.
3. Milestone 3: Cancellation/refunds + observability + hardening.
