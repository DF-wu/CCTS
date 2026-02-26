<img src="/imgs/logo.png" width="480" alt="CCTS logo" />

# Composite Contract Testing Service (CCTS)

> CCTS is a testing tool for **event-driven microservice systems**.  
> It combines **consumer-driven contract testing** with **end-to-end event-log validation** to verify cross-service behavior in SAGA-like workflows.

---

## Why CCTS

Traditional API-focused tests can confirm request/response contracts, but they usually cannot guarantee that:

- asynchronous events are produced in the expected flow,
- long-running transactions (SAGA) execute in valid state transitions,
- service-level contract checks and runtime event traces stay aligned.

CCTS addresses this gap by validating both **contract completeness** and **runtime execution paths**.

---

## Core Verification Pipeline

<img src="/imgs/user_process.svg" width="240" alt="CCTS user process" />

<img src="/imgs/architecture.svg" width="640" alt="CCTS architecture" />

CCTS executes verification in two layers:

1. **Static Verification**
   - CCTS document parsing and schema checks
   - state definition validation
   - potential path construction
   - isolated state / cyclic path detection

2. **Dynamic Verification**
   - contract retrieval from Pact Broker
   - contract test status checks (`can-i-deploy` logic)
   - event-log existence checks
   - event-log path validation with timestamp ordering

Output is a human-readable test report (Markdown-based rendering).

---

## Project Stack

- **Runtime**: Spring Boot `2.6.7`, Java `17`
- **Messaging**: RabbitMQ (AMQP)
- **Contract Testing**: Pact + Pact Broker
- **Database**: MongoDB
- **Build**: Maven
- **Container**: Docker / Docker Compose

---

## API Endpoints

| Method | Path | Description |
|---|---|---|
| `POST` | `/conductCCTSTest` | Run CCTS verification and return report payload |
| `POST` | `/cleanDB` | Clean CCTS event/document data |

> Endpoint source: `src/main/java/tw/dfder/ccts/controller/Controller.java`

---

## Quick Start (Docker Compose)

### Prerequisites

- Docker + Docker Compose
- Maven (required by `build.sh` before image build)
- JDK 17 (for local non-container runs)

### One-command startup

```bash
sh start-CCTS.sh
```

This script will:

1. stop existing compose stack,
2. build with Maven,
3. create `app.jar`,
4. build Docker image `ccts`,
5. start the compose services.

### Default exposed ports (from `docker-compose.yml`)

- `58080` → CCTS API (`ccts`)
- `10110` → RabbitMQ management UI
- `10109` → RabbitMQ AMQP
- `10141` → Pact Broker
- `27017` → MongoDB

---

## Configuration Reference

### Container profile

File: `src/main/resources/application-container.yml`

- server port: `58080`
- MongoDB URI: `mongodb://soselab:soselab401@local-mongodb:27017/CCTS?authSource=CCTS`
- RabbitMQ host: `rabbitmq:5672`
- Pact Broker URL: `http://pact_broker:9092`

### Local/development profile

File: `src/main/resources/application.yml`

- server port: `58093`
- RabbitMQ host: `140.121.196.23:10109`
- Pact Broker URL: `http://23.dfder.tw:10141`

> Update these values to match your environment before deployment.

---

## CCTS Specifications

### 1) CCTS Message Specification (event metadata)

Each event should include metadata keys:

| Key | Description |
|---|---|
| `provider` | Producer service name |
| `consumer` | Consumer service name |
| `testCaseID` | Contract test case identifier |
| `CCTSTimestamp` | Timestamp in milliseconds |

### 2) CCTS Document Specification

A CCTS document describes a SAGA-like state flow:

- `CCTSVersion`
- `title`
- `startAt`
- `states` (state definitions, transitions, options)
- `end` marker on terminal states

See detailed handbook:

- `doc/Composite Contract Testing Service Handbook.md`
- HackMD mirror: <https://hackmd.io/HRG1J7HkREqNM2qw0PxB0g>

---

## Repository Structure

<img src="/imgs/file_structure.JPG" alt="file structure" />

- `src/main/java` - CCTS core logic (parser, verifier, report exporter, connectors)
- `src/main/resources/CCTSDocuments` - sample CCTS document(s)
- `src/main/resources/inactiveDocument` - archived / negative test case docs
- `doc` - handbook and development notes
- `database_dump` - example DB dump
- `docker-compose.yml` - integrated local stack
- `build.sh` / `start-CCTS.sh` - build & startup scripts

---

## Related Repositories

- CCTS: <https://github.com/DF-wu/CCTS>
- Pact Broker boilerplate used during development:  
  <https://github.com/DF-wu/ContractTestingBoilerplate/tree/master>
- RabbitMQ template used during development:  
  <https://github.com/DF-wu/RabbitMQ_server>

PDVPS-based PoC services:

- Orchestrator: <https://github.com/DF-wu/CCTS_poc_orchestrator>
- Point Service: <https://github.com/DF-wu/CCTS_poc_points>
- Payment Service: <https://github.com/DF-wu/CCTS_poc_payment>
- Logging Service: <https://github.com/DF-wu/CCTS_poc_logging>

---

## Validation & Testing Notes

- Integration tests live under `src/test/java`.
- Default build script currently runs Maven with test skipping (`-Dmaven.test.skip=true`).
- For full verification in CI/manual runs, execute tests explicitly.

Example:

```bash
./mvnw test
```

---

## Research Context

This project is the implementation/prototype line for the thesis:

**Study on Contract Testing and End-to-End Testing for Event-driven Microservice Systems**  
National Taiwan Ocean University, 2022.

The thesis proposes CCTS as a method to combine contract testing with event-log path validation for event-driven microservices.
