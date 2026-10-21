# Canonical UML Diagrams

This is the repository's only UML source directory. Every canonical diagram uses PlantUML and an English file name. Generated PNG files are stored in `rendered/`.

| No. | Purpose | PlantUML source | Rendered PNG |
|---:|---|---|---|
| 01 | Integrated class diagram | [01-integrated-class-diagram.puml](01-integrated-class-diagram.puml) | [PNG](rendered/01-integrated-class-diagram.png) |
| 02 | Reservation and payment sequence | [02-reservation-payment-sequence.puml](02-reservation-payment-sequence.puml) | [PNG](rendered/02-reservation-payment-sequence.png) |
| 03 | Check-in and check-out sequence | [03-check-in-check-out-sequence.puml](03-check-in-check-out-sequence.puml) | [PNG](rendered/03-check-in-check-out-sequence.png) |
| 04 | Modular monolith component view | [04-modular-monolith-component.puml](04-modular-monolith-component.puml) | [PNG](rendered/04-modular-monolith-component.png) |
| 05 | Modular monolith deployment view | [05-modular-monolith-deployment.puml](05-modular-monolith-deployment.puml) | [PNG](rendered/05-modular-monolith-deployment.png) |
| 06 | Selected microservices component view | [06-microservices-component.puml](06-microservices-component.puml) | [PNG](rendered/06-microservices-component.png) |
| 07 | Selected microservices deployment view | [07-microservices-deployment.puml](07-microservices-deployment.puml) | [PNG](rendered/07-microservices-deployment.png) |
| 08 | Event-centred CQRS component view | [08-event-driven-cqrs-component.puml](08-event-driven-cqrs-component.puml) | [PNG](rendered/08-event-driven-cqrs-component.png) |
| 09 | Event-centred CQRS deployment view | [09-event-driven-cqrs-deployment.puml](09-event-driven-cqrs-deployment.puml) | [PNG](rendered/09-event-driven-cqrs-deployment.png) |

## Scope rules

- Diagrams 01–03 reflect the current `hotel-core` PoC. Final-only classes are explicitly marked `<<planned>>`.
- The selected patterns are Strategy, Decorator, State and Observer.
- Diagrams 06–07 show the final target: `hotel-core`, `notification-service` and RabbitMQ.
- Diagrams 08–09 are an evaluated alternative, not an implementation claim.
- Check-in/out changes reservation state in the PoC. Physical `OCCUPIED/CLEANING` room status is not implemented.

## Validation and rendering

With PlantUML installed, run from the repository root:

```powershell
plantuml -checkonly "docs/uml/*.puml"
plantuml -tpng -o rendered "docs/uml/*.puml"
```

The PlantUML JAR used during the local audit is not committed. Review every generated diagram visually after rendering; syntax validation does not prove projection readability.
