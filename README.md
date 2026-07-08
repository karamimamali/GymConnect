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
| `workload-service` | 8081 | Calculates & serves trainers' monthly training summaries, persisted in MongoDB. |

```
GymConnect/
├── settings.gradle / build.gradle   # monorepo root (shared config)
├── eureka-server/
├── main-service/
└── workload-service/
```

## Tech Stack

- Java 21, Spring Boot 3.3.5, Spring Cloud 2023.0.3
- Spring Cloud Netflix Eureka (discovery)
- Spring JMS + ActiveMQ Classic (asynchronous inter-service messaging, JSON payloads)
- Spring Security + JWT (jjwt) for service-to-service authorization
- Hibernate ORM + H2 (main-service); Spring Data MongoDB (workload-service)
- springdoc-openapi (Swagger UI), SLF4J + Logback (two-level logging with transaction id)
- JUnit 5 + Mockito, JaCoCo (≥ 80 % line coverage enforced per module)

## How it works

Whenever a training is **added** (or a trainee — and therefore their trainings — is
**deleted**), `main-service` publishes a workload event **asynchronously** to
ActiveMQ instead of calling `workload-service` over REST:

```
main-service ──(Spring JmsTemplate, JSON TextMessage)──▶ [trainer.workload.queue]
                                                              │
                              @JmsListener (1-10 concurrent consumers, per profile)
                                                              ▼
                                                      workload-service
                          invalid payload (missing required data)
                                                              ▼
                                              [trainer.workload.queue.dlq]
```

`workload-service` accrues (ADD) or reverses (DELETE) the training duration in the
trainer's `year → month → total` summary, which can be retrieved via
`GET /api/workloads/{username}` (JWT-protected REST, read-only).

Key messaging decisions:

- **Managed by Spring, not JMS primitives** — the producer uses `JmsTemplate`
  and the consumer a `@JmsListener` container; no `Session`/`MessageProducer`
  code anywhere.
- **JSON serialization** — messages travel as `TextMessage` JSON via
  `MappingJackson2MessageConverter`; a broker-neutral `_type=TrainerWorkloadMessage`
  property maps to each service's own DTO, so the services share no classes.
- **Error handling & dead letter queue** — the consumer validates every message.
  Payloads missing required information are moved to
  `trainer.workload.queue.dlq` (with a `rejectionReason` property) since
  redelivery can never fix them; unexpected processing failures roll back the
  transacted session so the broker redelivers and eventually dead-letters the
  message. Unreadable (malformed) messages follow the same broker DLQ path.
- **Horizontal scaling of consumers** — the listener container concurrency is
  profile-driven (`app.jms.concurrency`): `1-2` local up to `5-10` in prod.
- **Best-effort publishing** — a broker outage is logged and swallowed, so the
  core training operation still succeeds (graceful degradation).
- **Tracing** — the caller's `transactionId` travels as a JMS message property
  and is restored into the consumer's MDC, so one id correlates the logs of both
  services.

### Workload message contract

JSON body of a `TextMessage` on `trainer.workload.queue`
(`_type=TrainerWorkloadMessage`, optional `transactionId` property):

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

Start MongoDB (for `workload-service`) and an ActiveMQ broker, then the services
(each in its own terminal):

```bash
docker compose up -d mongodb                 # MongoDB on localhost:27017 (workload-service)
docker run -d --name activemq -p 61616:61616 -p 8161:8161 apache/activemq-classic
./gradlew :eureka-server:bootRun       # http://localhost:8761
./gradlew :workload-service:bootRun    # http://localhost:8081
./gradlew :main-service:bootRun        # http://localhost:8080
```

ActiveMQ web console: `http://localhost:8161` (admin/admin) — inspect
`trainer.workload.queue` and `trainer.workload.queue.dlq` there.
MongoDB connection is overridable via `MONGODB_URI` (default
`mongodb://localhost:27017/workload`); the `trainer_workloads` collection and its
`firstName + lastName` index are created automatically on startup.

Profiles (`local` default; `-Dspring.profiles.active=dev|stg|prod`) select the
broker URL (`ACTIVEMQ_URL`/`ACTIVEMQ_USER`/`ACTIVEMQ_PASSWORD` overridable via
environment) and the consumer concurrency per environment.

Swagger UI: `http://localhost:8080/swagger-ui.html` and
`http://localhost:8081/swagger-ui.html`.

> The two business services share the same `jwt.secret`, so a token issued by
> `main-service` is accepted by `workload-service` without a shared user store.

## Implemented requirements

- ✅ Separate workload microservice with the required ADD/DELETE contract
- ✅ MongoDB training-summary document (trainer → years → months → duration), keyed by
  username with a compound `firstName + lastName` index
- ✅ Spring Data MongoDB repository (search/update by username); embedded-Mongo tests
- ✅ REST between microservices replaced with **asynchronous ActiveMQ messaging**
  (on training add **and** on trainee deletion)
- ✅ Dead letter queue for invalid messages (required information missing)
- ✅ JMS integration managed by Spring (`JmsTemplate` / `@JmsListener`), JSON serialization
- ✅ JMS configuration per environment via Spring profiles; consumer concurrency scaling
- ✅ Eureka discovery module
- ✅ Bearer-token (JWT) authorization for the remaining REST surface
- ✅ Two-level logging (transaction level with a propagated `transactionId` + operation level)
- ✅ Swagger contract, REST naming best practices, no raw 500s leaked to clients
- ✅ Unit tests ≥ 80 % line coverage per module
