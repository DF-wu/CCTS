<p align="center">
  <img src="/imgs/logo.png" width="480" alt="CCTS logo" />
</p>

<h1 align="center">Composite Contract Testing Service (CCTS)</h1>

<p align="center">
  Contract + Event Log validation for event-driven microservices
</p>

<p align="center">
  <img alt="Java" src="https://img.shields.io/badge/Java-17-2ea44f" />
  <img alt="Spring Boot" src="https://img.shields.io/badge/Spring_Boot-2.6.7-6db33f" />
  <img alt="Maven" src="https://img.shields.io/badge/Build-Maven-c71a36" />
  <img alt="RabbitMQ" src="https://img.shields.io/badge/Messaging-RabbitMQ-ff6600" />
  <img alt="MongoDB" src="https://img.shields.io/badge/Database-MongoDB-47a248" />
  <img alt="Pact Broker" src="https://img.shields.io/badge/Contract-Pact_Broker-6f42c1" />
</p>

---

## Table of Contents

- [What is CCTS](#what-is-ccts)
- [Why CCTS](#why-ccts)
- [Architecture](#architecture)
- [Verification Flow](#verification-flow)
- [Quick Start](#quick-start)
- [API](#api)
- [Input Specifications](#input-specifications)
- [Configuration](#configuration)
- [Project Structure](#project-structure)
- [Troubleshooting](#troubleshooting)
- [Related Repositories](#related-repositories)
- [Research Background](#research-background)

---

## What is CCTS

**CCTS** is a verification service for **event-driven microservice systems**.

It combines:

- **Consumer-driven contract testing** (Pact)
- **Event-log sequence validation** (runtime reality)
- **State-machine based flow assertions** (SAGA-style scenarios)

In short, CCTS checks that your services not only agree on message contracts, but also behave correctly in real end-to-end execution.

---

## Why CCTS

Traditional tests usually cover either API contracts or endpoint behavior. In event-driven systems, that is often not enough.

CCTS targets these gaps:

- verifies whether expected cross-service event transitions actually occur,
- detects missing/incomplete contract verification between services,
- validates runtime event ordering against expected saga paths,
- surfaces static scenario modeling issues (isolated states, cyclic references, invalid transitions).

---

## Architecture

<p align="center">
  <img src="/imgs/architecture.svg" width="700" alt="CCTS architecture" />
</p>

### Key components

- **CCTS service**: document parser, verification engine, report exporter
- **RabbitMQ**: event transport + event-log collection source
- **Pact Broker**: contract and verification status source
- **MongoDB**: CCTS documents, event logs, and test result storage

---

## Verification Flow

<p align="center">
  <img src="/imgs/user_process.svg" width="260" alt="CCTS user flow" />
</p>

### 1) Static verification

- parse CCTS documents
- validate schema and required fields
- verify state graph consistency
- construct potential paths
- detect isolated/cyclic states

### 2) Dynamic verification

- retrieve related contracts from Pact Broker
- verify contract-test completion status (`can-i-deploy` style check)
- verify event-log existence for each message delivery
- validate timestamp-ordered event path against expected flow

### 3) Report output

- generate readable test result report (Markdown-based)

---

## Quick Start

### Prerequisites

- Docker + Docker Compose
- Maven
- JDK 17 (for local JVM run)

### Recommended startup (all-in-one)

```bash
sh start-CCTS.sh
```

`start-CCTS.sh` performs:

1. `docker compose down`
2. `sh build.sh`
3. `docker compose up -d`

`build.sh` performs:

1. `mvn -T 1C clean install -Dmaven.test.skip=true`
2. copy `target/CCTS-0.0.1.jar` to `app.jar`
3. build docker image `ccts`
4. `docker compose up -d`

### Default ports (`docker-compose.yml`)

| Service | Port | Purpose |
|---|---:|---|
| CCTS API | `58080` | Main HTTP API |
| RabbitMQ AMQP | `10109` | Messaging |
| RabbitMQ UI | `10110` | Management console |
| Pact Broker | `10141` | Contract broker |
| MongoDB | `27017` | Data storage |

---

## API

> Source: `src/main/java/tw/dfder/ccts/controller/Controller.java`

| Method | Path | Description |
|---|---|---|
| `POST` | `/conductCCTSTest` | Run CCTS verification |
| `POST` | `/cleanDB` | Clean CCTS document/event data |

### Example: run verification

```bash
curl -X POST http://localhost:58080/conductCCTSTest
```

### Example: clean CCTS DB data

```bash
curl -X POST http://localhost:58080/cleanDB
```

---

## Input Specifications

### A) CCTS Message Specification (event metadata)

Each event should include these metadata keys:

| Key | Meaning |
|---|---|
| `provider` | Producer service name |
| `consumer` | Consumer service name |
| `testCaseID` | Contract test case identifier |
| `CCTSTimestamp` | Event timestamp in milliseconds |

### B) CCTS Document Specification (state-flow definition)

Core fields:

- `CCTSVersion`
- `title`
- `startAt`
- `states` (array of state nodes)
- `nextState` or `options` (branching)
- `end` (`True`/`False`)

Minimal example:

```yaml
CCTSVersion: "0.12"
title: demo case
startAt: Top-up Event initialized
states:
  - stateName: Top-up Event initialized
    comment: initial state
    end: False
    nextState:
      stateName: payment
      testCaseId: t-orc-payment-01
      provider: orchestrator
      consumer: paymentService
  - stateName: payment
    comment: make payment
    end: False
    options:
      - stateName: Payment processed
        testCaseId: t-payment-orc-01
        provider: paymentService
        consumer: orchestrator
      - stateName: Payment failed
        testCaseId: t-payment-orc-02
        provider: paymentService
        consumer: orchestrator
```

Detailed handbook:

- `doc/Composite Contract Testing Service Handbook.md`
- HackMD mirror: <https://hackmd.io/HRG1J7HkREqNM2qw0PxB0g>

---

## Configuration

### Container profile

File: `src/main/resources/application-container.yml`

- app port: `58080`
- MongoDB URI:
  `mongodb://soselab:soselab401@local-mongodb:27017/CCTS?authSource=CCTS`
- RabbitMQ: `rabbitmq:5672`
- Pact Broker URL currently set to: `http://pact_broker:9092`

### Local profile

File: `src/main/resources/application.yml`

- app port: `58093`
- RabbitMQ: `140.121.196.23:10109`
- Pact Broker: `http://23.dfder.tw:10141`

> Tune all endpoints and credentials before running in your environment.

---

## Project Structure

<p align="center">
  <img src="/imgs/file_structure.JPG" alt="file structure" />
</p>

| Path | Description |
|---|---|
| `src/main/java` | Core service logic (parser, verifier, report, connectors) |
| `src/main/resources/CCTSDocuments` | Active CCTS case examples |
| `src/main/resources/inactiveDocument` | Inactive / negative-case examples |
| `src/test/java` | Integration test entry |
| `doc` | Handbook and design notes |
| `database_dump` | Sample dump data |
| `docker-compose.yml` | Local integrated stack |
| `build.sh`, `start-CCTS.sh` | Build and bootstrap scripts |

---

## Troubleshooting

### `./mvnw` permission denied

Use:

```bash
sh mvnw test
```

or grant execute permission:

```bash
chmod +x mvnw
./mvnw test
```

### `Failed to load ApplicationContext` / invalid Mongo URI during test

This typically means Mongo configuration is empty or invalid in current profile.

Check:

- `src/main/resources/application.yml`
- `src/main/resources/application-container.yml`

### Pact Broker connectivity issues

Validate:

- service URL (`CCTS.pact_broker`)
- compose/network port mapping
- broker container health

---

## Related Repositories

- CCTS: <https://github.com/DF-wu/CCTS>
- Pact Broker boilerplate used during development: <https://github.com/DF-wu/ContractTestingBoilerplate/tree/master>
- RabbitMQ template used during development: <https://github.com/DF-wu/RabbitMQ_server>

PDVPS-based PoC services:

- Orchestrator: <https://github.com/DF-wu/CCTS_poc_orchestrator>
- Point Service: <https://github.com/DF-wu/CCTS_poc_points>
- Payment Service: <https://github.com/DF-wu/CCTS_poc_payment>
- Logging Service: <https://github.com/DF-wu/CCTS_poc_logging>

---

## Research Background

This repository is based on the thesis:

**Study on Contract Testing and End-to-End Testing for Event-driven Microservice Systems**  
National Taiwan Ocean University, 2022.

The research proposes CCTS as a practical method to combine contract testing with event-log path validation in event-driven microservices.
