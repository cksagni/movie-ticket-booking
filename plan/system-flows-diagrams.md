# Movie Ticket Booking System Flows (Sequence Diagrams)

## 1. Discovery Flow
```mermaid
sequenceDiagram
    actor User
    participant CityController
    participant MovieController as CityController(Movie API)
    participant ShowController
    participant CityService
    participant MovieService
    participant ShowService
    participant DB

    User->>CityController: GET /cities
    CityController->>CityService: getAllCities()
    CityService->>DB: SELECT * FROM cities
    DB-->>CityService: cities
    CityService-->>CityController: cities
    CityController-->>User: 200 cities

    User->>MovieController: GET /cities/{cityId}/movies
    MovieController->>MovieService: getMoviesByCity(cityId)
    MovieService->>DB: SELECT city + mapped movies
    DB-->>MovieService: city/movies or empty
    MovieService-->>MovieController: movies
    MovieController-->>User: 200 movies / 404 CITY_NOT_FOUND

    User->>ShowController: GET /movies/{movieId}/shows?cityId&date
    ShowController->>ShowService: getShows(movieId, cityId, date)
    ShowService->>DB: validate city + city_movie mapping
    ShowService->>DB: query shows in day window
    DB-->>ShowService: shows
    ShowService-->>ShowController: show DTOs
    ShowController-->>User: 200 shows

    User->>ShowController: GET /shows/{showId}/seats
    ShowController->>ShowService: getSeats(showId)
    ShowService->>DB: validate show + fetch show_seats
    DB-->>ShowService: seat status list
    ShowService-->>ShowController: seat DTOs
    ShowController-->>User: 200 seats / 404 SHOW_NOT_FOUND
```

## 2. Admin Setup (City, Theatre, Screen)
```mermaid
sequenceDiagram
    actor Admin
    participant AdminSetupController
    participant CityService
    participant TheatreService
    participant DB

    Admin->>AdminSetupController: POST /admin/cities
    AdminSetupController->>CityService: saveCity(city)
    CityService->>DB: validate + trim + INSERT city
    DB-->>CityService: city
    CityService-->>AdminSetupController: city
    AdminSetupController-->>Admin: 200 city / 400 INVALID_CITY

    Admin->>AdminSetupController: POST /admin/theatres
    AdminSetupController->>TheatreService: createTheatre(request)
    TheatreService->>DB: validate + trim + INSERT theatre
    DB-->>TheatreService: theatre
    TheatreService-->>AdminSetupController: theatre
    AdminSetupController-->>Admin: 200 theatre / 400 INVALID_THEATRE

    Admin->>AdminSetupController: POST /admin/theatres/{theatreId}/screens
    AdminSetupController->>TheatreService: addScreen(theatreId, request)
    TheatreService->>DB: validate theatre + screen payload
    TheatreService->>DB: INSERT screen
    DB-->>TheatreService: screen
    TheatreService-->>AdminSetupController: screen
    AdminSetupController-->>Admin: 200 screen / 404 THEATRE_NOT_FOUND
```

## 3. Admin Show Setup
```mermaid
sequenceDiagram
    actor Admin
    participant AdminShowController
    participant ShowService
    participant DB

    Admin->>AdminShowController: POST /admin/shows
    AdminShowController->>ShowService: createShow(request)
    ShowService->>DB: validate movie/screen
    ShowService->>ShowService: validate screen has theatre, showTime in future, price > 0
    ShowService->>DB: INSERT show
    DB-->>ShowService: show
    ShowService-->>AdminShowController: show
    AdminShowController-->>Admin: 200 show / 400 INVALID_* / 404

    Admin->>AdminShowController: POST /admin/shows/{showId}/show-seats/init
    AdminShowController->>ShowService: initializeShowSeats(showId)
    ShowService->>DB: SELECT show
    ShowService->>DB: SELECT existing show_seats
    alt already initialized
        ShowService-->>AdminShowController: existing count
    else first initialization
        ShowService->>DB: SELECT screen seats
        ShowService->>DB: INSERT show_seats (AVAILABLE)
        ShowService-->>AdminShowController: initialized count
    end
    AdminShowController-->>Admin: 200 count
```

## 4. Seat Hold Flow
```mermaid
sequenceDiagram
    actor User
    participant BookingController
    participant BookingService
    participant DB

    User->>BookingController: POST /bookings/hold (userId, showId, seatIds)
    BookingController->>BookingService: holdSeats(request)
    BookingService->>DB: validate user + show
    BookingService->>DB: lock show_seats FOR UPDATE
    BookingService->>BookingService: validate availability + constraints
    alt valid
        BookingService->>DB: update show_seats -> LOCKED
        BookingService->>DB: insert reservation (PENDING)
        BookingService->>DB: insert reservation_seats
        BookingService-->>BookingController: reservationId, expiresAt, amount
        BookingController-->>User: 200 hold created
    else invalid
        BookingService-->>BookingController: domain error
        BookingController-->>User: 400/409 error
    end
```

## 5. Payment and Confirmation Flow
```mermaid
sequenceDiagram
    actor User
    participant BookingController
    participant BookingService
    participant PaymentService
    participant DB

    User->>BookingController: POST /bookings/{reservationId}/pay
    BookingController->>BookingService: pay(reservationId, request)
    BookingService->>DB: load reservation
    BookingService->>BookingService: ownership + state checks
    BookingService->>DB: lock reservation show_seats FOR UPDATE
    BookingService->>DB: create/fetch payment INITIATED
    BookingService->>PaymentService: isPaymentSuccessful(token)

    alt payment success
        PaymentService-->>BookingService: true
        BookingService->>DB: payment -> SUCCESS
        BookingService->>DB: reservation -> CONFIRMED + ticketRef
        BookingService->>DB: seats -> BOOKED, clear locks
        BookingService-->>BookingController: CONFIRMED/SUCCESS
        BookingController-->>User: 200 confirmed
    else payment failed
        PaymentService-->>BookingService: false
        BookingService->>DB: payment -> FAILED
        BookingService->>DB: reservation -> FAILED
        BookingService->>DB: seats -> AVAILABLE
        BookingService-->>BookingController: FAILED/FAILED
        BookingController-->>User: 200 failed state
    end
```

## 6. Booking Retrieval + History
```mermaid
sequenceDiagram
    actor User
    participant BookingController
    participant BookingService
    participant DB

    User->>BookingController: GET /bookings/{reservationId}?userId
    BookingController->>BookingService: getBooking(reservationId, userId)
    BookingService->>DB: load reservation
    BookingService->>BookingService: ownership check
    BookingService->>DB: load reservation_seats
    BookingService-->>BookingController: booking DTO
    BookingController-->>User: 200 booking / 404 / 409

    User->>BookingController: GET /bookings?userId
    BookingController->>BookingService: getBookingHistory(userId)
    BookingService->>DB: load user reservations desc
    loop each reservation
        BookingService->>DB: load reservation seats
    end
    BookingService-->>BookingController: booking list
    BookingController-->>User: 200 history
```

## 7. Cancellation + Refund
```mermaid
sequenceDiagram
    actor User
    participant BookingController
    participant BookingService
    participant DB

    User->>BookingController: POST /bookings/{reservationId}/cancel
    BookingController->>BookingService: cancel(reservationId, userId)
    BookingService->>DB: load reservation
    BookingService->>BookingService: ownership/state/cutoff checks
    BookingService->>DB: lock reservation show_seats
    BookingService->>DB: seats -> AVAILABLE, clear locks
    BookingService->>DB: load payment by reservation
    alt payment SUCCESS exists
        BookingService->>DB: payment -> REFUNDED
    end
    BookingService->>DB: reservation -> CANCELLED
    BookingService-->>BookingController: booking DTO
    BookingController-->>User: 200 cancelled / 409 not allowed
```

## 8. Lock Expiry Scheduler
```mermaid
sequenceDiagram
    participant Scheduler as SeatLockExpiryScheduler
    participant BookingService
    participant DB

    loop every booking.lock.expiry.scan-ms (default 30000)
        Scheduler->>BookingService: expireStaleLocks()
        BookingService->>DB: find LOCKED show_seats expired
        BookingService->>DB: set seats AVAILABLE + clear lock fields
        BookingService->>DB: find PENDING reservations expired
        BookingService->>DB: set reservations EXPIRED
    end
```

## 9. Reservation State Machine
```mermaid
stateDiagram-v2
    [*] --> PENDING: hold created
    PENDING --> CONFIRMED: payment success
    PENDING --> FAILED: payment failure
    PENDING --> EXPIRED: lock timeout
    PENDING --> CANCELLED: user cancel (before cutoff)
    CONFIRMED --> CANCELLED: user cancel (before cutoff)
```

## 10. Payment State Machine
```mermaid
stateDiagram-v2
    [*] --> INITIATED
    INITIATED --> SUCCESS
    INITIATED --> FAILED
    SUCCESS --> REFUNDED: on cancellation
```

## 11. Show Seat State Machine
```mermaid
stateDiagram-v2
    [*] --> AVAILABLE
    AVAILABLE --> LOCKED: hold
    LOCKED --> BOOKED: payment success
    LOCKED --> AVAILABLE: payment failed or lock expired or cancel
```
