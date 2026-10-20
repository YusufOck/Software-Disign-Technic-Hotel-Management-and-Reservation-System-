# Hotel Management and Reservation System

**Software Design Techniques — Project 6**  
**Midterm submission report: requirements 1–6**  
**Team:** names to be inserted after Moodle registration check  
**Presentation window:** class presentations begin on 1 November 2026; this team's exact slot has not been announced.

## 1. Scope and interpretation

The assigned system manages rooms, room types, guests, reservations, check-in and check-out, simulated payments and booking notifications. Guests need date-based availability search and booking actions. Staff need room and guest management and stay operations. A critical invariant is that a room cannot have two active reservations whose stay intervals overlap. Rates must depend on room type, stay dates and selected services.

The course brief requires Spring Boot for the backend. At midterm, the assessed deliverables are four or more design patterns with justifications, one integrated UML class diagram, two detailed sequence diagrams, a Java proof of concept implementing the selected patterns, and an evaluation of three architecture styles. The brief explicitly permits a limited implementation focused on the interactions among the patterns; this is the scope of the executable prototype. Requirements 7–9 are reserved for the final presentation and are described as the planned extension in Section 8. The source is the course handout, pages 1 and 12–13.

### Requirements traceability

| Requirement | Evidence in this submission |
|---|---|
| 1. Identify four patterns | Strategy, Decorator, State and Observer, Section 3 |
| 2. Explain each pattern | Problem, design role, simpler alternative and advantage, Section 3 |
| 3. One comprehensive class diagram | `uml/01-sinif-diyagrami.puml` (Mermaid edit source: `.mmd`) |
| 4. Two sequence diagrams | `uml/02-rezervasyon-ve-odeme.*` and `uml/03-checkin-checkout.*` |
| 5. Java implementation | Spring Boot `hotel-core/` proof of concept and its tests |
| 6. Three architecture styles | Component and deployment diagrams for each style, Sections 6–7 |

## 2. Domain model and business invariants

The central entities are `RoomType`, `Room`, `Guest`, `Reservation`, `Payment` and `NotificationRecord`. `RoomType` defines capacity and a base nightly rate. A `Room` has an operational status independent of its date-based availability. `Reservation` records one guest, one room, a check-in date, a check-out date, selected extra services, a price snapshot, payment result and lifecycle state. Keeping the quoted total on the reservation ensures later room-rate changes do not alter an existing booking.

We model a stay as a half-open interval `[checkIn, checkOut)`: the guest occupies the room on the check-in date and leaves before the next stay may begin on the check-out date. Two reservations conflict exactly when `existing.checkIn < requested.checkOut` and `requested.checkIn < existing.checkOut`. Therefore 7–9 November and 9–11 November can use the same room. Invalid or empty intervals are rejected. An availability search gives a candidate list; the service checks the room again when it creates the reservation because another request may have booked it in the meantime.

`CONFIRMED` and `CHECKED_IN` reservations block their room. `CANCELLED` and `PAYMENT_FAILED` reservations remain in history but release the room. Check-in changes physical occupancy, while future availability continues to be derived from reservation intervals. Check-out completes the reservation and frees physical occupancy without deleting the history. The prototype uses one-process synchronization for the in-memory repository. It does not claim the same concurrency guarantee across several processes. The final design adds a PostgreSQL exclusion constraint to enforce the invariant at database level.

All amounts use decimal arithmetic and an explicit currency. The prototype's demonstration tariff applies a 25% multiplier to weekend nights, plus 15 per night for breakfast and 10 per night for parking. These numbers are design choices for the demonstration, not course requirements. The simulated payment accepts a controlled success or failure outcome; it never processes a real card.

## 3. Integrated design patterns

The patterns are used together in one booking path: select and calculate the base rate, wrap it with selected service charges, advance the reservation state after payment, and publish a booking event to multiple listeners.

| Pattern | Specific problem and design | Why the simpler approach is weaker | Evidence to present |
|---|---|---|---|
| **Strategy** | `PricingStrategy` defines a rate algorithm. Standard and seasonal strategies calculate the room component from the room type and stay period. | A growing `if/switch` in the booking service mixes pricing policy with transaction flow and makes individual rules harder to change or test. | A weekday and weekend quote differ for the same room type. |
| **Decorator** | A base room price is wrapped by `BreakfastDecorator` and `ParkingDecorator` according to the guest's selection. Each wrapper adds its charge and preserves the same price-component interface. | Creating one method or subclass for every service combination scales poorly as services are added. | The combined price equals base rate plus both selected extras. |
| **State** | `ReservationState` implementations determine whether payment, check-in, check-out or cancellation can advance a booking. The reservation delegates lifecycle behavior to its current state object. | Status checks scattered across controllers and services can admit inconsistent transitions. An enum and a switch alone would not demonstrate this pattern. | A confirmed booking can check in; a completed booking cannot be cancelled. |
| **Observer** | `BookingEventPublisher` distributes a reservation event to notification and audit observers. The booking operation depends on the listener abstraction. | Calling concrete notification and audit classes directly from the booking use case couples it to every future side effect. | One confirmation event creates both a notification record and an audit entry. |

These patterns do not replace the domain rules. In particular, a State object cannot by itself prevent concurrent database writes, and Observer delivery does not guarantee that a remote queue and database commit are atomic. Those concerns are addressed separately in the persistence and messaging design.

### Integrated UML class diagram

The single class diagram is [`uml/01-sinif-diyagrami.puml`](uml/01-sinif-diyagrami.puml). It includes the domain entities, application services and all four pattern roles in one connected model. Its notes identify the classes involved in each pattern. We maintain a [Mermaid source](uml/01-sinif-diyagrami.mmd) for quick edits with the team. Both files represent the same design; they are not four separate class diagrams. The diagram models the target system, so some infrastructure classes shown there, such as persistent repositories and the Rabbit publisher, belong to the final-stage extension. The midterm code is the explicitly limited proof of concept.

### Two main-use-case sequence diagrams

1. [`Reservation, payment and notification`](uml/02-rezervasyon-ve-odeme.puml): availability search, booking-time recheck, Strategy and Decorator price calculation, payment success or failure, State transition and Observer delivery. An alternative branch handles an overlapping booking with HTTP 409.
2. [`Check-in and check-out`](uml/03-checkin-checkout.puml): staff starts from a confirmed booking, State verifies the transition, the room's physical occupancy changes, and checkout completes the booking. Invalid transitions have an explicit alternative branch.

Mermaid edit sources for [sequence 1](uml/02-rezervasyon-ve-odeme.mmd) and [sequence 2](uml/03-checkin-checkout.mmd) are provided. The presentation uses the sequence diagrams to explain the timing of calls; the class diagram explains static responsibility and relationships.

## 4. Prototype architecture and demonstration

The midterm proof of concept is a Spring Boot REST service named `hotel-core`. It keeps repositories in memory to make the design-pattern path executable with little infrastructure. Controllers parse HTTP requests and delegate to an application service; they do not calculate prices or decide lifecycle transitions. The application service checks room and guest validity, serializes booking creation within the process, calculates a price, records the simulated payment, advances the State and publishes an event to the Observer listeners.

The demonstration begins by creating a room type, room and guest. We search the room for a future interval and create a reservation with breakfast and parking. We then repeat the request for the same room and overlapping dates: the second request must receive a conflict response. A reservation beginning on the first reservation's check-out date is allowed. Cancelling the first booking releases its original interval. Check-in and check-out demonstrate the State pattern and physical occupancy changes. Notification and audit queries demonstrate Observer fan-out.

**Prototype limitation:** in-memory data disappears on restart. One-process synchronization is sufficient only for this proof of concept. The final implementation will use PostgreSQL and a database exclusion constraint for two concurrent clients and multiple application instances. Modifying a reservation and sending upcoming-stay reminders are planned final-stage features.

## 5. Evaluation method for architecture alternatives

We compare the styles using the project's central risk and the team's constraints: prevention of double booking, speed of implementation, independence of notifications, operational complexity, and the need to demonstrate requirements 7–9. Each alternative has a **component diagram** for responsibility and data flow and a **deployment diagram** for runtime nodes. The third option is an event-centred distributed architecture with CQRS read projections. Although event-driven systems can also be built from microservices, this alternative differs from our two-service design because events are the primary integration mechanism for pricing, payment, availability projections and notification; reads use separately updated projections.

## 6. Three architecture styles

### 6.1 Modular monolith

One Spring Boot application contains room, guest, booking, payment and notification modules. A single PostgreSQL database holds the authoritative data. A booking request passes through modules using in-process calls and one transaction. This makes consistency, local debugging and deployment straightforward. However, each release and scaling decision affects the whole process, and notification cannot be deployed separately.

- [Component diagram](uml/04-monolit-bilesen.puml)
- [Deployment diagram](uml/05-monolit-dagitim.puml)

### 6.2 Two-service microservice system

`hotel-core` owns room, guest, price, payment simulation and booking data, including the non-overlap invariant. `notification-service` owns notification generation and delivery history. The core service publishes booking events to RabbitMQ. The notification service consumes them and records the result. Each service has an independent build and deployment and does not read the other's tables. Booking correctness remains in one authoritative database, while notifications can arrive later and must handle duplicate events. This design has more container, network and observability work than the monolith.

- [Component diagram](uml/06-mikroservis-bilesen.puml)
- [Deployment diagram](uml/07-mikroservis-dagitim.puml)

### 6.3 Event-centred distributed system with CQRS

A command-side booking authority publishes events to a broker. Separate availability and guest-facing read projections consume these events. Payment and notification consumers react through asynchronous messages; long-running steps may require a saga. New consumers can be attached independently, and read models can be optimized. In return, projections may lag behind the command state, event order and repeated delivery require explicit handling, and debugging a booking across many consumers is harder. The command side still needs a strong non-overlap check: eventual consistency in the read projection cannot decide who gets the last room.

- [Component diagram](uml/08-olay-gudumlu-bilesen.puml)
- [Deployment diagram](uml/09-olay-gudumlu-dagitim.puml)

## 7. Comparison and selected architecture

| Criterion | Modular monolith | Two-service microservices | Event-centred CQRS |
|---|---|---|---|
| Speed for a two-person team | Highest | Moderate | Lowest |
| Booking consistency | One transaction and DB constraint | One authoritative core DB | Strong command side, delayed read side |
| Independent notification release | No | Yes | Yes |
| Operational and test burden | Lowest | Moderate | Highest |
| Direct fit to final course tasks | Insufficient alone | Strong | Possible but more complex |

We selected a staged implementation. The midterm prototype is a modular Spring Boot core. The final target is the two-service microservice architecture because it has a meaningful independent notification responsibility and directly demonstrates inter-service messaging, Docker and CI/CD while preserving one booking authority. A modular monolith would be an economical production choice for a small single hotel, but the assigned final assessment explicitly requires microservices. The full event-centred CQRS option adds failure modes and effort without improving the proof-of-concept outcome enough to justify it here.

## 8. Final-stage implementation plan (requirements 7–9)

1. Replace the in-memory store with PostgreSQL and versioned Flyway migrations. Apply the non-overlap invariant as an exclusion constraint on `room_id` and a half-open `daterange`, filtered to blocking reservation states. Test two simultaneous booking attempts; exactly one must succeed.
2. Create a separate `notification-service` Spring Boot application with its own notification data. Publish `ReservationConfirmed`, `ReservationChanged`, `ReservationCancelled` and `StayReminder` events through RabbitMQ. Give every event an ID; the consumer stores processed IDs to avoid duplicate notifications after redelivery. If time permits, use a transactional outbox so database commit and eventual publication can be reconciled.
3. Package the two applications and their infrastructure with Docker Compose. Add health checks and wait for dependencies to become healthy. Document `docker compose up --build --wait`, ports, demo steps and cleanup.
4. Export a Postman collection and environment covering every implemented endpoint, success and important error cases. Store generated IDs as collection variables so the collection runs from a clean database.
5. Add GitHub Actions for Maven build, automated tests, Docker image builds, Compose deployment to the job's local Docker daemon, and an API smoke test. A GitHub-hosted runner is temporary; this is a reproducible deployment test rather than a permanent hosted system.

## 9. References

- Course handout, *SDT Project Details*, pages 1 and 12–13. Kept outside the Git repository.
- [Spring Boot system requirements](https://docs.spring.io/spring-boot/system-requirements.html)
- [Spring Boot RabbitMQ support](https://docs.spring.io/spring-boot/reference/messaging/amqp.html)
- [PostgreSQL range types and exclusion constraints](https://www.postgresql.org/docs/current/rangetypes.html)
- [Docker Compose startup order](https://docs.docker.com/compose/how-tos/startup-order)
- [GitHub Actions Java/Maven workflow](https://docs.github.com/en/actions/tutorials/build-and-test-code/java-with-maven)
- [GitHub-hosted runner lifecycle](https://docs.github.com/en/actions/how-tos/manage-runners/github-hosted-runners/use-github-hosted-runners)
