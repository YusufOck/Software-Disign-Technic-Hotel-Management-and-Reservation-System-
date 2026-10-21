# Hotel Management and Reservation System

This repository contains the two-stage work for Software Design Techniques Project 6. Requirements 1–6 belong to the midterm presentation; requirements 7–9 belong to the final presentation. Confirm the exact presentation date and duration with the lecturer or Moodle.

## Start here

- [Current decisions, status and remaining work](docs/PROJECT_MASTER_PLAN.md)
- [Requirement-to-evidence status](docs/REQUIREMENTS_TRACEABILITY.md)
- [Midterm report](docs/MIDTERM_REPORT.md)
- [Presentation and live demo plan](docs/PRESENTATION_AND_DEMO_PLAN.md)
- [Canonical UML sources](docs/uml/README.md)
- [Executable Spring Boot PoC](hotel-core/README.md)

## Current status

The `hotel-core` module passed a clean build and all 6 tests on 21 October 2026. The selected patterns are Strategy, Decorator, State and Observer. Run the repeatable demo after starting the application:

```powershell
cd hotel-core
.\mvnw.cmd clean test
.\mvnw.cmd spring-boot:run
```

In a second terminal from the repository root:

```powershell
powershell -ExecutionPolicy Bypass -File .\demo\midterm-demo.ps1
```

The midterm PoC is a Spring Boot REST backend with in-memory data. It does not include a frontend, PostgreSQL or Docker because those are not required to demonstrate requirements 1–6. The planned final stage adds PostgreSQL, a separate `notification-service`, RabbitMQ, Docker Compose, Postman/Newman and CI/CD.

