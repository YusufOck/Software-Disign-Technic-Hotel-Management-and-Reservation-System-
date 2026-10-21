# Hotel Management and Reservation System — Project Master Plan

**Last verified:** 21 October 2026  
**Assigned topic:** Project 6 — Hotel Management and Reservation System  
**Team:** Two students; names and the exact presentation slot must be added after confirmation.

## 1. Course scope

The course brief requires a Spring Boot backend. Requirements 1–6 are presented at midterm and requirements 7–9 at the final presentation.

| Requirement | Points | Required evidence |
|---:|---:|---|
| 1 | 0.50 | At least four design patterns |
| 2 | 0.50 | A project-specific justification for every pattern |
| 3 | 1.00 | One comprehensive, integrated UML class diagram |
| 4 | 0.75 | Two detailed UML sequence diagrams |
| 5 | 1.25 | A Java proof of concept in which the selected patterns interact |
| 6 | 2.00 | Monolith, microservices and a third distributed style; component/deployment views, trade-offs and a decision |
| 7 | 2.00 | At least two independent services, service communication, Docker, Postman and run instructions |
| 8 | 1.00 | Asynchronous queue communication between at least two services |
| 9 | 1.00 | CI/CD that builds, tests and deploys at least to a local Docker environment |

The brief does not state the exact presentation date, duration or upload format. These details must be confirmed with the lecturer or Moodle.

## 2. Current midterm design

The executable `hotel-core` module is one Spring Boot application with an in-memory store. Four GoF patterns work in one booking flow:

1. **Strategy** — `StandardPricingStrategy` and `SeasonalPricingStrategy` isolate alternative room-rate algorithms.
2. **Decorator** — `BreakfastDecorator` and `ParkingDecorator` add optional services without creating a class for every combination.
3. **State** — concrete `ReservationState` classes own valid payment, cancellation, check-in and check-out transitions.
4. **Observer** — `BookingEventPublisher` fans one booking event out to `AuditObserver` and `NotificationObserver`.

### Why State replaced Facade

The early draft selected Facade because booking coordinates availability, pricing, payment and events. The implementation later showed that `BookingService` already provides the use-case entry point, so adding a separate `BookingFacade` would mostly rename the existing application service and provide weak pattern evidence.

Reservation lifecycle behaviour is a stronger project-specific problem. Without State, transition rules would be scattered through `if/switch` blocks. The current code delegates transitions to `PendingPaymentState`, `ConfirmedState`, `CheckedInState` and terminal state classes. This makes the pattern visible in Java, UML, tests and the live demo. Facade is not wrong in general; State is simply the stronger fourth pattern for this implementation and assessment.

## 3. Core business rules

- A stay uses a half-open interval: `[checkIn, checkOut)`. A new stay may begin on the previous stay's checkout date.
- Two stays overlap when `existing.checkIn < requestedCheckOut && requestedCheckIn < existing.checkOut`.
- `PENDING_PAYMENT`, `CONFIRMED` and `CHECKED_IN` block a room in the PoC. `PAYMENT_FAILED`, `CANCELLED` and `COMPLETED` do not.
- `RoomStatus` currently contains only `ACTIVE` and `OUT_OF_SERVICE`. Physical `OCCUPIED/CLEANING` status is not implemented in the midterm PoC.
- Monetary calculations use `BigDecimal`. The 1.25 weekend multiplier, 15-per-night breakfast and 10-per-night parking prices are demonstration rules, not course requirements.
- Availability is rechecked while `BookingService.create` holds the shared store lock.
- `synchronized(HotelStore)` protects only one JVM. The final version needs a PostgreSQL transaction and exclusion constraint.

## 4. Final architecture decision

The final target contains two independently built Spring Boot services:

- **`hotel-core`:** owns rooms, room types, guests, pricing, reservations, simulated payment, check-in/out and the no-overlap invariant.
- **`notification-service`:** consumes RabbitMQ events and owns notification history plus processed event IDs.

There is no separate inventory service. Keeping room allocation and reservation consistency in one authority avoids an unnecessary distributed transaction while still satisfying the requirement for at least two services. RabbitMQ provides the required asynchronous inter-service communication.

## 5. Repository source of truth

```text
Project/
├─ README.md
├─ hotel-core/                         # executable midterm PoC
├─ demo/midterm-demo.ps1              # repeatable REST demonstration
└─ docs/
   ├─ PROJECT_MASTER_PLAN.md
   ├─ REQUIREMENTS_TRACEABILITY.md
   ├─ PRESENTATION_AND_DEMO_PLAN.md
   ├─ MIDTERM_REPORT.md
   ├─ presentation/MIDTERM_SLIDES.md
   └─ uml/                             # the only UML source directory
```

All repository files, names, diagrams and professor-facing text are English. User-facing explanations in chat may be Turkish.

## 6. Verified status on 21 October 2026

Completed and technically verified:

- Spring Boot PoC builds successfully.
- All 6 automated tests pass.
- Room type, room and guest create/list APIs exist.
- Availability search, reservation, simulated payment, cancellation, check-in and check-out exist.
- Overlapping stays are rejected and adjacent stays are accepted.
- A 16-request concurrency test produces exactly one winner.
- The four patterns execute in the same reservation flow.
- One class diagram, two sequence diagrams and six architecture diagrams have valid PlantUML sources and rendered PNGs.
- A live REST smoke run verified a 425 total with three price lines, overlap 409, premature checkout 409, and the `CHECKED_IN -> COMPLETED` path.

Still required before the midterm presentation:

- Add team names and confirmed presentation details.
- Review all rendered diagrams for readability on the presentation screen.
- Rehearse the complete live demo at least twice.
- Have the second team member review the report/UML/code mapping before marking it `VERIFIED`.
- Test build and startup on a second clean computer.

Not implemented yet for the final stage:

- PostgreSQL and Flyway persistence.
- Reservation modification and remaining management updates.
- Separate `notification-service`.
- RabbitMQ producer/consumer, idempotency and dead-letter handling.
- Dockerfiles and Docker Compose.
- Postman/Newman collection and environment.
- GitHub Actions build/test/deploy/smoke workflow.

## 7. Recommended order of work

Before midterm:

1. Confirm administrative details.
2. Review diagrams and the English report.
3. Run `hotel-core\mvnw.cmd clean test` on both computers.
4. Start the application and run `demo\midterm-demo.ps1`.
5. Rehearse questions about patterns, overlap and architecture.
6. Freeze the demonstrated state with a Git tag or release.

For the final stage:

1. Move `hotel-core` persistence to PostgreSQL/Flyway and add the database overlap constraint.
2. Implement reservation modification and the selected remaining hotel functions.
3. Create `notification-service` and the RabbitMQ event contract.
4. Add idempotent consumption and dead-letter handling.
5. Package both services and infrastructure with Docker Compose.
6. Create the Postman/Newman flow.
7. Add GitHub Actions build, test, Compose deployment, health check and smoke test.
