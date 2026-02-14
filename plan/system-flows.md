# Movie Ticket Booking System Flows

## 1. Discovery Flow
Goal: allow user to discover cities, movies, shows, and seat availability.

### 1.1 Get all cities
- API: `GET /cities`
- Service: `CityService.getAllCities()`
- Data source: `cities` table via `CityRepository.findAll()`
- Response: list of cities

### 1.2 Get movies by city
- API: `GET /cities/{cityId}/movies`
- Service: `MovieService.getMoviesByCity(cityId)`
- Logic:
1. Fetch city by `cityId`
2. If city not found -> `CITY_NOT_FOUND`
3. Return city-movie mapping

### 1.3 Get shows by movie/date/city
- API: `GET /movies/{movieId}/shows?cityId={id}&date=YYYY-MM-DD`
- Service: `ShowService.getShows(movieId, cityId, date)`
- Logic:
1. Validate city exists
2. Verify movie is mapped to the city
3. Query shows for day range `[date 00:00, date+1 00:00)`
4. Return show details (movie, time, price, screen, theatre)

### 1.4 Get show seats
- API: `GET /shows/{showId}/seats`
- Service: `ShowService.getSeats(showId)`
- Logic:
1. Validate show exists
2. Fetch `show_seats`
3. Return seat row/number/status/price/lock expiry

## 2. Admin Setup Flow
Goal: set up master data and theatre hierarchy (city, theatre, screen) for show creation.

### 2.1 Create city
- API: `POST /admin/cities`
- Controller: `AdminSetupController`
- Service: `CityService.saveCity(city)`
- Logic:
1. Validate non-empty `name`, `state`, `country`
2. Trim input values
3. Save city and return city DTO

### 2.2 Create theatre (cinema hall)
- API: `POST /admin/theatres`
- Controller: `AdminSetupController`
- Service: `TheatreService.createTheatre(request)`
- Logic:
1. Validate non-empty theatre `name` and `location`
2. Trim values
3. Save theatre and return theatre DTO

### 2.3 Add screen in theatre
- API: `POST /admin/theatres/{theatreId}/screens`
- Controller: `AdminSetupController`
- Service: `TheatreService.addScreen(theatreId, request)`
- Logic:
1. Validate theatre exists
2. Validate non-empty screen name
3. Validate `totalSeats > 0`
4. Save screen linked to theatre

## 3. Admin Show Setup Flow
Goal: create shows and initialize show-wise seat inventory.

### 3.1 Create show
- API: `POST /admin/shows`
- Service: `ShowService.createShow(request)`
- Logic:
1. Validate movie exists
2. Validate screen exists
3. Validate screen is linked to a theatre
4. Validate show time is in future
5. Validate price > 0
6. Save show

### 3.2 Initialize show seats
- API: `POST /admin/shows/{showId}/show-seats/init`
- Service: `ShowService.initializeShowSeats(showId)`
- Logic:
1. Validate show exists
2. If already initialized, return current count
3. Load physical seats of show’s screen
4. Create `show_seats` with status `AVAILABLE` and default show price

## 4. Seat Hold Flow
Goal: lock selected seats temporarily before payment.

- API: `POST /bookings/hold`
- Service: `BookingService.holdSeats(request)`
- Request: `userId`, `showId`, `seatIds[]`
- Constraints: seat count 1 to 10, no duplicates, show must be in future

### Steps
1. Validate unique seat IDs
2. Validate user and show exist
3. Lock selected `show_seats` rows (`PESSIMISTIC_WRITE`)
4. Validate each seat belongs to show screen and is available
5. Set seat status to `LOCKED`, assign `lockedByUser`, set `lockExpiresAt = now + 5 min`
6. Create reservation with `PENDING`
7. Create reservation-seat mappings
8. Return `reservationId`, `expiresAt`, `amount`, `PENDING`

### Failure conditions
- `INVALID_SEAT_SELECTION`
- `INVALID_SHOW`
- `SEAT_NOT_AVAILABLE`
- `USER_NOT_FOUND` / `SHOW_NOT_FOUND`

## 5. Payment and Confirmation Flow
Goal: convert a held reservation into confirmed booking.

- API: `POST /bookings/{reservationId}/pay`
- Service: `BookingService.pay(reservationId, request)`
- Request: `userId`, `paymentMethod`, `paymentToken`

### Steps
1. Load reservation
2. Verify reservation ownership
3. If already `CONFIRMED`, return existing payment status and ticket
4. Ensure reservation state is `PENDING`
5. Ensure reservation not expired
6. Lock reservation’s `show_seats`
7. Verify lock ownership and lock expiry
8. Create or fetch payment record (`INITIATED`)
9. Simulate payment result via `PaymentService`:
- token containing `fail` -> failure
- otherwise success
10. On success:
- payment -> `SUCCESS`
- reservation -> `CONFIRMED`
- seats -> `BOOKED` (clear lock fields)
- generate ticket reference `TKT-XXXXXXXX`
11. On failure:
- payment -> `FAILED`
- reservation -> `FAILED`
- seats -> `AVAILABLE`

### Failure conditions
- `RESERVATION_NOT_FOUND`
- `RESERVATION_ACCESS_DENIED`
- `INVALID_RESERVATION_STATE`
- `RESERVATION_EXPIRED`

## 6. Booking Retrieval Flow
Goal: user views booking details and history.

### 6.1 Get booking by id
- API: `GET /bookings/{reservationId}?userId={id}`
- Service: `BookingService.getBooking(reservationId, userId)`
- Logic:
1. Load reservation
2. Validate reservation ownership
3. Load reservation seats
4. Return booking summary

### 6.2 Get booking history
- API: `GET /bookings?userId={id}`
- Service: `BookingService.getBookingHistory(userId)`
- Logic:
1. Fetch reservations by user (latest first)
2. Fetch mapped seat IDs for each reservation
3. Return booking list

## 7. Cancellation and Refund Flow
Goal: cancel eligible bookings and free seats.

- API: `POST /bookings/{reservationId}/cancel`
- Service: `BookingService.cancel(reservationId, userId)`
- Request: `userId`

### Steps
1. Load reservation
2. Verify reservation ownership
3. Allow only `PENDING` or `CONFIRMED`
4. Enforce cancellation cutoff: not allowed within 30 minutes of show time
5. Lock and release all reservation seats -> `AVAILABLE`
6. If payment exists with `SUCCESS`, mark payment `REFUNDED`
7. Mark reservation `CANCELLED`
8. Return latest booking snapshot

### Failure conditions
- `BOOKING_CANCELLATION_NOT_ALLOWED`
- `RESERVATION_ACCESS_DENIED`
- `RESERVATION_NOT_FOUND`

## 8. Lock Expiry Background Flow
Goal: auto-release stale holds.

- Scheduler: `SeatLockExpiryScheduler.releaseExpiredLocks()`
- Frequency: every `booking.lock.expiry.scan-ms` (default 30000 ms)
- Service: `BookingService.expireStaleLocks()`

### Steps
1. Find `show_seats` with `LOCKED` and `lock_expires_at < now`
2. Set those seats back to `AVAILABLE`, clear lock metadata
3. Find reservations with `PENDING` and `expires_at < now`
4. Mark them `EXPIRED`

## 9. Reservation/Payment/Seat State Model

### Reservation states
- `PENDING`: hold successful, awaiting payment
- `CONFIRMED`: payment successful
- `FAILED`: payment failed
- `CANCELLED`: user cancelled
- `EXPIRED`: hold timed out

### Payment states
- `INITIATED`
- `SUCCESS`
- `FAILED`
- `REFUNDED`

### Show seat states
- `AVAILABLE`
- `LOCKED`
- `BOOKED`

## 10. Error Response Contract
All domain errors are normalized by global exception handling.

Response fields:
- `code`
- `message`
- `details`
- `traceId`
- `timestamp`

Common HTTP mappings:
- `400`: bad request/validation
- `404`: missing entities
- `409`: state conflict/business rule violation
- `500`: unexpected errors
