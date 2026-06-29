# GymConnect

A gym CRM platform built as a Spring Boot **microservices monorepo**. The main
service manages trainees, trainers and trainings; a dedicated reporting
microservice maintains each trainer's monthly training hours; and a Eureka server
provides service discovery.

## Modules

| Module             | Port | Responsibility                                                                 |
|--------------------|------|--------------------------------------------------------------------------------|
| `eureka-server`    | 8761 | Netflix Eureka service-discovery registry.                                     |
| `main-service`     | 8080 | Gym CRM REST API (trainees, trainers, trainings) + Hibernate/H2 + JWT security.|
| `workload-service` | 8081 | Calculates & serves trainers' monthly training hours from an in-memory store.  |

```
GymConnect/
├── settings.gradle / build.gradle   # monorepo root (shared config)
├── eureka-server/
├── main-service/
└── workload-service/
```

## Tech Stack

- Java 21, Spring Boot 3.3.5, Spring Cloud 2023.0.3
- Spring Cloud Netflix Eureka (discovery), OpenFeign (declarative client),
  Resilience4j (circuit breaker + time limiter)
- Spring Security + JWT (jjwt) for service-to-service authorization
- Hibernate ORM + H2 (main-service); thread-safe in-memory store (workload-service)
- springdoc-openapi (Swagger UI), SLF4J + Logback (two-level logging with transaction id)
- JUnit 5 + Mockito, JaCoCo (≥ 80 % line coverage enforced per module)

## How it works

Whenever a training is **added** (or a trainee — and therefore their trainings — is
**deleted**), `main-service` publishes a workload event to `workload-service`:

```
main-service ──(Feign + Eureka + circuit breaker)──▶ POST /api/workloads
   Authorization: Bearer <service JWT>
   X-Transaction-Id: <propagated id>
```

`workload-service` accrues (ADD) or reverses (DELETE) the training duration in the
trainer's `year → month → total` summary, which can be retrieved via
`GET /api/workloads/{username}`.

The reporting call is **best-effort**: a circuit breaker with a 4s time limiter
guards it, so if `workload-service` is slow or down the training operation still
succeeds and the failure is logged (graceful degradation).

### Workload contract

`POST /api/workloads` (Richardson level 2, JWT-protected)

```json
{
  "username": "Mike.Johnson",
  "firstName": "Mike",
  "lastName": "Johnson",
  "active": true,
  "trainingDate": "2026-05-10",
  "trainingDuration": 90,
  "actionType": "ADD"          // ADD | DELETE
}
```

`GET /api/workloads/{username}` →

```json
{
  "username": "Mike.Johnson", "firstName": "Mike", "lastName": "Johnson", "active": true,
  "years": [ { "year": 2026, "months": [ { "month": 5, "trainingSummaryDuration": 90 } ] } ]
}
```

## Build & Test

```bash
./gradlew build                 # compile + test + JaCoCo 80% verification (all modules)
./gradlew :workload-service:test
./gradlew :main-service:jacocoTestReport   # build/reports/jacoco/test/html/index.html
```

## Run locally

Start the services in this order (each in its own terminal):

```bash
./gradlew :eureka-server:bootRun       # http://localhost:8761
./gradlew :workload-service:bootRun    # http://localhost:8081
./gradlew :main-service:bootRun        # http://localhost:8080
```

Swagger UI: `http://localhost:8080/swagger-ui.html` and
`http://localhost:8081/swagger-ui.html`.

> The two business services share the same `jwt.secret`, so a token issued by
> `main-service` is accepted by `workload-service` without a shared user store.

## Implemented requirements

- ✅ Separate workload microservice with REST endpoint and the required ADD/DELETE contract
- ✅ In-memory monthly summary model (trainer → years → months → duration)
- ✅ Main service calls the workload service on training add **and** on trainee deletion
- ✅ Eureka discovery module
- ✅ Circuit-breaker pattern (Resilience4j) with fallback + timeout handling
- ✅ Bearer-token (JWT) authorization for microservice integration
- ✅ Two-level logging (transaction level with a propagated `transactionId` + operation level)
- ✅ Swagger contract, REST naming best practices, no raw 500s leaked to clients
- ✅ Unit tests ≥ 80 % line coverage per module
