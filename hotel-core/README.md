# Hotel Core — first presentation proof of concept

This Java 21 / Spring Boot 4.1.1 service demonstrates the project's four GoF patterns together in one reservation flow. Data is held in memory and is cleared when the process restarts. Payment and guest notifications are simulated; no card data or email is sent.

## Run and test

From this directory:

```powershell
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

On macOS/Linux use `./mvnw` instead. The server listens at `http://localhost:8080`. Java 21 or newer is needed. The Maven wrapper downloads Maven automatically on first use.

## API

| Action | Endpoint | Body or parameters |
|---|---|---|
| Create/list room types | `POST/GET /api/room-types` | `{"name":"Double","basePrice":100.00,"capacity":2}` |
| Create/list rooms | `POST/GET /api/rooms` | `{"number":"101","roomTypeId":"<UUID>"}` |
| Mark room active/out of service | `PATCH /api/rooms/{id}/status` | `{"status":"OUT_OF_SERVICE"}` or `ACTIVE` |
| Create/list guests | `POST/GET /api/guests` | `{"name":"Ada Guest","email":"ada@example.com","preferences":"Quiet room"}` |
| Find available rooms | `GET /api/rooms/availability` | `checkIn=YYYY-MM-DD&checkOut=YYYY-MM-DD&capacity=1` |
| Create reservation + simulate payment | `POST /api/reservations` | See example below |
| Read reservation/history | `GET /api/reservations/{id}` or `GET /api/reservations?guestId={id}` | |
| Cancel/check in/check out | `POST /api/reservations/{id}/cancel`, `/check-in`, `/check-out` | No body |
| View simulated notifications | `GET /api/notifications?guestId={id}` | |
| View audit events | `GET /api/audit` | |

Create a room type, room, and guest first; copy their IDs into the reservation request. Dates must be in the future or today, and `checkOut` must be after `checkIn`:

```json
{
  "guestId": "<guest UUID>",
  "roomId": "<room UUID>",
  "checkIn": "2026-11-02",
  "checkOut": "2026-11-04",
  "extras": ["BREAKFAST", "PARKING"],
  "simulatePaymentSuccess": true
}
```

Omit `extras` for no extras and omit `simulatePaymentSuccess` for successful payment. Set it to `false` to demonstrate a `PAYMENT_FAILED` reservation that does not occupy inventory. Responses show state, total price, chosen strategy, and itemized charges. Validation errors return HTTP 400; overlap or invalid state transitions return HTTP 409; unknown IDs return HTTP 404.

## Integrated patterns and business rules

1. **Strategy:** `PricingService` chooses `StandardPricingStrategy` for weekday-only stays and `SeasonalPricingStrategy` when the stay includes a Saturday or Sunday. The latter charges 1.25 × the room type's nightly base rate for weekend nights. Pricing is calculated from the room type and each night in the requested period.
2. **Decorator:** `BaseRoomPrice` is wrapped by `BreakfastDecorator` (15.00/night) and/or `ParkingDecorator` (10.00/night). The response exposes the resulting price lines.
3. **State:** New reservations start in `PENDING_PAYMENT`, then become `CONFIRMED` or `PAYMENT_FAILED`. Confirmed reservations can be cancelled or checked in; checked-in reservations can be completed by check-out. Invalid transitions are rejected. Check-in is allowed only during the reserved stay.
4. **Observer:** `BookingEventPublisher` sends lifecycle events to independent `AuditObserver` and `NotificationObserver` listeners. Confirmation, cancellation, failed payment, check-in, and completion generate simulated guest notifications.

Intervals are half-open: `[checkIn, checkOut)`. A booking ending on 4 November and another starting on 4 November may use the same room. Only `PENDING_PAYMENT`, `CONFIRMED`, and `CHECKED_IN` block dates. The shared in-process lock keeps availability checking and booking creation atomic, so concurrent requests for one room/date range have one winner.

## Deliberate first-stage limits

This is the pattern demonstration required by project requirement 5, not the final distributed system. The data, audit log, and notifications are lost on restart. No real payment provider, authentication, email delivery, reservation date amendment, persistence, or distributed transaction is included. In the final microservices stage, use a database constraint/transaction to enforce overlap across instances; this in-process lock is insufficient when services run on multiple nodes. The later stage also needs separate services, inter-service communication, RabbitMQ (or equivalent), Docker, a Postman collection, and CI/CD.
