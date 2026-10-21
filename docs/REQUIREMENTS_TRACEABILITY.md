# Requirements Traceability and Delivery Status

**Audit date:** 21 October 2026

Status meanings:

- `TODO`: work has not started.
- `DOING`: partial evidence exists.
- `DONE`: evidence exists and was technically checked in this repository audit.
- `VERIFIED`: another team member independently reviewed the `DONE` evidence.

No item is marked `VERIFIED` automatically.

| Req. | Points | Required evidence | Current evidence | Status | Remaining action |
|---:|---:|---|---|---|---|
| 1 | 0.50 | At least four patterns | Strategy, Decorator, State and Observer in code, report and class UML | DONE | Team review |
| 2 | 0.50 | Project-specific justification | Master plan and report pattern comparison | DONE | Rehearse concise explanation |
| 3 | 1.00 | One comprehensive class diagram | `docs/uml/01-integrated-class-diagram.puml` and PNG | DONE | Presentation readability review |
| 4 | 0.75 | Two detailed sequence diagrams | `02-reservation-payment-sequence.puml` and `03-check-in-check-out-sequence.puml` | DONE | Compare messages with live demo |
| 5 | 1.25 | Java PoC with pattern interaction | `hotel-core`; 6/6 tests and REST smoke run passed | DONE | Run on second clean computer |
| 6 | 2.00 | Three styles, data flow, component/deployment, trade-offs and decision | `docs/uml/04`–`09`; report Sections 5–7 | DONE | Review all six rendered views |
| 7 | 2.00 | At least two services, communication, Docker, Postman and README | Target design only | TODO | Implement notification service, Compose and Postman |
| 8 | 1.00 | Async queue between services | Target RabbitMQ design only | TODO | Implement producer, consumer, idempotency and DLQ |
| 9 | 1.00 | CI/CD build, test and deploy | No workflow yet | TODO | Add GitHub Actions pipeline and smoke test |

The **6/10-point midterm portion has technical evidence**, but it is not presentation-ready until team review and rehearsal are complete. The **4/10-point final portion is planned but not implemented**.

## Midterm readiness checklist

- [x] Pattern names match across code, report and UML.
- [x] Every pattern has a problem, design role and simpler alternative.
- [x] One integrated class diagram exists.
- [x] Two sequence diagrams reflect the real PoC behaviour.
- [x] Three architecture alternatives have component and deployment diagrams.
- [x] The clean Maven test run passes 6/6 tests.
- [x] Overlap, adjacent stays and a 16-request concurrency race are tested.
- [x] Payment failure, cancellation and State transitions are tested.
- [x] A repeatable English REST demo script exists.
- [ ] Team names and confirmed presentation details are added.
- [ ] Nine PNG diagrams are approved for projection readability.
- [ ] The live demo is rehearsed twice.
- [ ] The second team member marks reviewed evidence `VERIFIED`.
- [ ] Build and startup work on a second clean computer.

## Deliberately deferred PoC functions

- Reservation date/service modification.
- General room type, room and guest updates.
- Persistent storage and multi-instance overlap safety.
- Upcoming-stay reminders.
- Physical room occupancy/cleaning status.
- Real email and payment providers.

## Final-stage checklist

- [ ] `hotel-core` uses PostgreSQL and Flyway.
- [ ] `notification-service` builds and runs independently.
- [ ] Services do not access each other's tables.
- [ ] `hotel-core -> RabbitMQ -> notification-service` works live.
- [ ] Duplicate events do not create duplicate notifications.
- [ ] Retry and dead-letter behaviour are documented and tested.
- [ ] Dockerfiles and Compose health checks are present.
- [ ] `.env.example` exists and no secret is committed.
- [ ] README documents startup, ports, tests and cleanup.
- [ ] Postman runs setup, happy paths and important error paths from clean data.
- [ ] Newman passes from the command line.
- [ ] GitHub Actions builds, tests and builds images.
- [ ] GitHub Actions starts Compose and runs health and Newman smoke checks.
- [ ] Failure logs are uploaded as workflow artifacts.
