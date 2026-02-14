# API Reference (Current Implementation)

## Base Notes
- Content-Type: `application/json`
- Validation and domain errors return normalized payload:
  - `code`, `message`, `details`, `traceId`, `timestamp`
- Common status codes:
  - `200 OK`
  - `400 Bad Request`
  - `404 Not Found`
  - `409 Conflict`

## 1. Discovery APIs

### 1.1 Get Cities
- Method: `GET`
- Path: `/cities`
- Response: `List<City>`

### 1.2 Get Movies by City
- Method: `GET`
- Path: `/cities/{cityId}/movies`
- Response: `Set<Movie>`
- Errors:
  - `404 CITY_NOT_FOUND`

### 1.3 Get Shows by Movie and Date
- Method: `GET`
- Path: `/movies/{movieId}/shows`
- Query params:
  - `cityId` (Long, required)
  - `date` (`YYYY-MM-DD`, required)
- Response:
```json
[
  {
    "id": 10,
    "movieId": 1,
    "movieTitle": "Movie A",
    "showTime": "2026-02-15T19:30:00",
    "price": 250.0,
    "screenId": 5,
    "theatreName": "PVR Downtown"
  }
]
```
- Errors:
  - `404 CITY_NOT_FOUND`

### 1.4 Get Show Seats
- Method: `GET`
- Path: `/shows/{showId}/seats`
- Response:
```json
[
  {
    "showSeatId": 101,
    "seatId": 11,
    "seatRow": "A",
    "seatNumber": 7,
    "status": "AVAILABLE",
    "price": 250.0,
    "lockExpiresAt": null
  }
]
```
- Errors:
  - `404 SHOW_NOT_FOUND`

## 2. Booking APIs

### 2.1 Hold Seats
- Method: `POST`
- Path: `/bookings/hold`
- Request:
```json
{
  "userId": 1,
  "showId": 10,
  "seatIds": [11, 12]
}
```
- Response:
```json
{
  "reservationId": 500,
  "expiresAt": "2026-02-11T21:05:00",
  "amount": 450.0,
  "bookingStatus": "PENDING"
}
```
- Key errors:
  - `400 INVALID_SEAT_SELECTION`
  - `400 INVALID_SHOW`
  - `404 USER_NOT_FOUND`
  - `404 SHOW_NOT_FOUND`
  - `409 SEAT_NOT_AVAILABLE`

### 2.2 Pay Reservation
- Method: `POST`
- Path: `/bookings/{reservationId}/pay`
- Request:
```json
{
  "userId": 1,
  "paymentMethod": "CARD",
  "paymentToken": "tok_ok"
}
```
- Response:
```json
{
  "bookingStatus": "CONFIRMED",
  "paymentStatus": "SUCCESS",
  "ticketRef": "TKT-AB12CD34"
}
```
- Key errors:
  - `404 RESERVATION_NOT_FOUND`
  - `409 RESERVATION_ACCESS_DENIED`
  - `409 INVALID_RESERVATION_STATE`
  - `409 RESERVATION_EXPIRED`

### 2.3 Get Booking
- Method: `GET`
- Path: `/bookings/{reservationId}`
- Query params:
  - `userId` (Long, required)
- Response:
```json
{
  "reservationId": 500,
  "userId": 1,
  "showId": 10,
  "status": "CONFIRMED",
  "expiresAt": "2026-02-11T21:05:00",
  "amount": 450.0,
  "ticketRef": "TKT-AB12CD34",
  "seatIds": [11, 12]
}
```

### 2.4 Get Booking History
- Method: `GET`
- Path: `/bookings`
- Query params:
  - `userId` (Long, required)
- Response: `List<BookingResponse>`

### 2.5 Cancel Booking
- Method: `POST`
- Path: `/bookings/{reservationId}/cancel`
- Request:
```json
{
  "userId": 1
}
```
- Response: `BookingResponse` with `status = CANCELLED`
- Key errors:
  - `404 RESERVATION_NOT_FOUND`
  - `409 RESERVATION_ACCESS_DENIED`
  - `409 BOOKING_CANCELLATION_NOT_ALLOWED`

## 3. Admin Setup APIs

### 3.1 Create City
- Method: `POST`
- Path: `/admin/cities`
- Request:
```json
{
  "name": "Bengaluru",
  "state": "Karnataka",
  "country": "India"
}
```
- Response:
```json
{
  "id": 1,
  "name": "Bengaluru",
  "state": "Karnataka",
  "country": "India"
}
```
- Key errors:
  - `400 INVALID_CITY`

### 3.2 Create Theatre (Cinema Hall)
- Method: `POST`
- Path: `/admin/theatres`
- Request:
```json
{
  "name": "PVR Orion",
  "location": "Rajajinagar"
}
```
- Response:
```json
{
  "id": 10,
  "name": "PVR Orion",
  "location": "Rajajinagar"
}
```
- Key errors:
  - `400 INVALID_THEATRE`

### 3.3 Add Screen to Theatre
- Method: `POST`
- Path: `/admin/theatres/{theatreId}/screens`
- Request:
```json
{
  "name": "Screen 1",
  "totalSeats": 180
}
```
- Response:
```json
{
  "id": 100,
  "theatreId": 10,
  "name": "Screen 1",
  "totalSeats": 180
}
```
- Key errors:
  - `404 THEATRE_NOT_FOUND`
  - `400 INVALID_SCREEN`

## 4. Admin Show APIs

### 4.1 Create Show
- Method: `POST`
- Path: `/admin/shows`
- Request:
```json
{
  "movieId": 1,
  "screenId": 100,
  "showTime": "2026-02-15T19:30:00",
  "price": 250.0
}
```
- Response: `Show` entity
- Key errors:
  - `404 MOVIE_NOT_FOUND`
  - `404 SCREEN_NOT_FOUND`
  - `400 INVALID_SCREEN`
  - `400 INVALID_SHOW_TIME`
  - `400 INVALID_PRICE`

### 4.2 Initialize Show Seats
- Method: `POST`
- Path: `/admin/shows/{showId}/show-seats/init`
- Response:
```json
{
  "showId": 200,
  "seatsInitialized": 180
}
```
- Key errors:
  - `404 SHOW_NOT_FOUND`

## 5. Payment Token Simulation Rule
Used by `PaymentService` for current implementation:
- If `paymentToken` contains `"fail"` (case-insensitive) -> payment failure path
- Otherwise -> payment success path
