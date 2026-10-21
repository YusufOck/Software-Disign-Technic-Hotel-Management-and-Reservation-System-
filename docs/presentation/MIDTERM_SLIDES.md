# Midterm slide script (English)

This is the editable content plan for the PowerPoint deck. The main talk is designed for about 12–15 minutes, subject to the lecturer's actual time limit. Both students should be ready to explain every slide; the split below is only for rehearsal.

| # | Slide title | On-slide message | Demonstration / speaker cue | Suggested speaker |
|---:|---|---|---|---|
| 1 | Hotel Management and Reservation System | Project 6; team names; Software Design Techniques | State the assigned project and the midterm scope. | A |
| 2 | Midterm assessment scope | Requirements 1–6 total 6 points; four patterns, one class UML, two sequence UML, Java PoC, three architectures | Show the exact evidence map, not an ungraded feature list. | A |
| 3 | The booking invariant | Stay interval `[checkIn, checkOut)`; overlap formula; search is rechecked during booking | Explain why 7–9 and 9–11 November can share one room. | A |
| 4 | Four patterns in one reservation flow | Strategy → Decorator → State → Observer | Show each role and actual code class. Emphasize that State is behavior in classes, not just an enum. | B |
| 5 | One integrated class diagram | All domain and pattern classes share one model | Zoom into Room–Reservation, Pricing, State and Events during the live diagram view. | B |
| 6 | Sequence 1: booking and payment | Search, recheck, price, payment, state, event; 409 and payment-failure alternatives | Walk through the success branch first, then point to conflict/failure branches. | A |
| 7 | Sequence 2: check-in and check-out | Only a confirmed booking checks in; checkout completes it and changes room occupancy | Mention rejected transition branch. | B |
| 8 | Alternative A: modular monolith | One Spring Boot deployable and one DB; easiest consistency/deployment | Explain component and deployment diagrams together. | A |
| 9 | Alternative B: two microservices | `hotel-core` owns bookings; `notification-service` owns messages; RabbitMQ connects them | Explain separate data ownership and that booking correctness remains in core. | B |
| 10 | Alternative C: event-centred CQRS | Broker-led projections and consumers; more flexible but more consistency work | A read projection is not the authority that assigns a room. | B |
| 11 | Architecture decision | Modular PoC now; two-service final target; full CQRS is unnecessary for this scope | Give the comparison criteria and explain the course requirement. | A |
| 12 | Live PoC and final-stage path | Create room/guest, reserve, reject overlap, cancel, check-in/out, inspect notification/audit | Run the prepared API demo and name next milestones: Postgres, RabbitMQ, Docker, Postman, CI/CD. | Both |

## Live demo checklist

1. Start `hotel-core` using its README command; keep a backup terminal screenshot in case a live process fails.
2. Create a room type, room and guest with prepared HTTP requests. A midterm Postman collection is optional; the formal Postman requirement belongs to the final stage.
3. Search a future interval that includes a weekend, then reserve it with both extra services.
4. Show the quoted total and the `CONFIRMED` status. A second overlapping request for the same room should return HTTP 409.
5. Show an adjacent stay that begins 9 November. Cancel the first booking and show its original interval available again.
6. For check-in/out, create a separate reservation that starts on the actual demo day. Show check-in and check-out, then demonstrate a rejected invalid transition. The PoC changes reservation state; it does not yet maintain physical `OCCUPIED/CLEANING` room states.
7. Fetch notification and audit records created from the same booking event.

## Questions to rehearse

- Which classes implement the four patterns, and where do the patterns interact?
- Why is an availability search not enough to prevent double booking?
- Why do you use a half-open date interval?
- What happens to a reservation on payment failure or cancellation?
- Does the prototype's in-memory synchronization work with two server instances? (No; the final database constraint solves that.)
- Why does the class diagram include some final-stage infrastructure that the midterm PoC does not execute?
- How is the third architecture different from the chosen microservice arrangement?
- How will the final pipeline actually deploy, rather than only build and test?

