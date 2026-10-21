# Presentation and Demonstration Plan

The course brief does not state a duration. This plan assumes a 10–12 minute explanation followed by a 3–5 minute demonstration. Preserve requirements 1–6 evidence if the lecturer gives a shorter slot.

## Midterm presentation

| Slide | Topic | Main message |
|---:|---|---|
| 1 | Project and scope | Project 6, Spring Boot and the deliberately limited PoC |
| 2 | Booking invariant | `[checkIn, checkOut)`, overlap rule and booking-time recheck |
| 3 | Four patterns | Strategy, Decorator, State and Observer solve four distinct problems |
| 4 | Integrated flow | Pricing strategy → extras decorators → payment State transition → Observer fan-out |
| 5 | Integrated class UML | Domain, four patterns and clearly marked planned infrastructure |
| 6 | Sequence 1 | Search, recheck, quote, simulated payment, state, events and failure branches |
| 7 | Sequence 2 | Confirmed booking check-in, checkout and invalid transition rejection |
| 8 | Executable PoC | Price breakdown, overlap 409, State transitions, notifications and audit |
| 9 | Three architectures | Modular monolith, two-service microservices and event-centred CQRS |
| 10 | Decision and final path | Modular PoC now; two services, RabbitMQ, Docker, Postman and CI/CD at final |

The detailed English slide script is in `presentation/MIDTERM_SLIDES.md`.

## Live midterm demo

Preparation:

1. Run `hotel-core\mvnw.cmd clean test`.
2. Start the application with `hotel-core\mvnw.cmd spring-boot:run`.
3. From the repository root run `powershell -ExecutionPolicy Bypass -File demo\midterm-demo.ps1`.
4. Keep the test output and API responses as backup evidence.

The script demonstrates:

- Room type, two rooms and one guest creation.
- A weekend-inclusive booking with breakfast and parking.
- Strategy name, itemised charges and confirmed total.
- HTTP 409 for an overlapping second booking.
- An adjacent stay beginning on the first stay's checkout date.
- HTTP 409 for checkout before check-in.
- `CONFIRMED -> CHECKED_IN -> COMPLETED` State transitions.
- Notification and audit records produced by Observer listeners.

Use a separate reservation beginning on the actual demo day for check-in/out. The PoC changes reservation state; it does not yet store physical `OCCUPIED/CLEANING` room states.

## Final presentation

| Slide | Topic | Evidence |
|---:|---|---|
| 1 | Two independent services | `hotel-core` and `notification-service` ownership boundaries |
| 2 | Async communication | Event envelope, RabbitMQ, idempotent consumer and DLQ |
| 3 | Persistence and overlap | PostgreSQL/Flyway, `[)` range and exclusion constraint |
| 4 | Docker deployment | Both applications, databases, RabbitMQ and health checks |
| 5 | Postman/Newman | Implemented features plus happy and error paths |
| 6 | CI/CD | Build → test → image → Compose → health → Newman |
| 7 | Live final demo | Booking, async notification, duplicate event, overlap and pipeline result |
| 8 | Rubric map | Direct evidence for requirements 7, 8 and 9 |

## Questions to rehearse

### Why State instead of Facade?

`BookingService` already acts as the application-level entry point, so a separate Facade would add little behaviour. State owns real lifecycle rules, replaces scattered conditionals and has visible UML, code and test evidence.

### Are Observer and RabbitMQ the same?

No. Observer demonstrates an in-process object relationship. RabbitMQ provides durable asynchronous communication across two independent processes.

### Why only two services?

The brief requires at least two. Notification is a meaningful independent responsibility; splitting room allocation from reservation ownership would add an unnecessary distributed consistency problem.

### What happens during a concurrent booking race?

The PoC locks the shared in-memory store and the 16-request test produces one winner. The final version must enforce the invariant with a PostgreSQL exclusion constraint.

### Does check-in mark a room OCCUPIED?

Not in the current PoC. It changes the reservation to `CHECKED_IN`. Physical occupancy and cleaning status are deferred.
