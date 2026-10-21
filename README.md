<a href="https://fireflysoftware.io/openplatform/">
<picture>
  <source media="(max-width: 640px) and (prefers-color-scheme: dark)" srcset="https://raw.githubusercontent.com/fireflysoftwaresolutions/.github/main/profile/assets/banner-narrow-dark.svg">
  <source media="(max-width: 640px)" srcset="https://raw.githubusercontent.com/fireflysoftwaresolutions/.github/main/profile/assets/banner-narrow-light.svg">
  <source media="(prefers-color-scheme: dark)" srcset="https://raw.githubusercontent.com/fireflysoftwaresolutions/.github/main/profile/assets/banner-dark.svg">
  <img src="https://raw.githubusercontent.com/fireflysoftwaresolutions/.github/main/profile/assets/banner-light.svg" alt="Firefly OpenPlatform - Domain Insurance - Reinsurance" width="100%">
</picture>
</a>

# Firefly OpenPlatform - Domain Insurance - Reinsurance

Developed by the Firefly Software Foundation. [dev@fireflysoftware.io](mailto:dev@fireflysoftware.io)

[Website](https://fireflysoftware.io/) · [OpenPlatform](https://fireflysoftware.io/openplatform/) · [Module catalogue](https://fireflysoftware.io/openplatform/modules/)

Domain layer microservice for reinsurance treaties within the Firefly platform. This service orchestrates create, status change, and query against `core-insurance-reinsurance` with compensating saga steps.

Repository: [https://github.com/fireflysoftwaresolutions/domain-insurance-reinsurance](https://github.com/fireflysoftwaresolutions/domain-insurance-reinsurance)

---

## Overview

`domain-insurance-reinsurance` sits between experience services and `core-insurance-reinsurance`. Write operations run through the FireflyFramework saga engine. Queries use the CQRS query bus. The domain depends on the core SDK only. It does not import another core's internal packages. Identifiers that point at parties, contracts, products, or policies stay UUIDs owned by those cores.

It records treaty status through the reinsurance core. It does not post a reinsurance recovery, does not move money, and does not certify IFRS 17 or Solvency II.

### Key features

- **Create** -- register a treaty through `core-insurance-reinsurance`
- **Status change** -- read the current record and update its status through the core This core keeps status on the record itself, so the saga updates that field through the core and does not call a second service.
- **Query** -- read one treaty by identifier
- **Compensation** -- a failed later step asks the core to undo the work already accepted
- **CQRS** -- commands and queries travel on separate buses
- **Reactive I/O** -- Spring WebFlux with virtual threads enabled

## Architecture

### Module structure

| Module | Purpose |
|--------|---------|
| `domain-insurance-reinsurance-core` | Saga workflows, commands, handlers, and the application service |
| `domain-insurance-reinsurance-interfaces` | Request contract for a status change |
| `domain-insurance-reinsurance-infra` | SDK client factory and downstream base-path configuration |
| `domain-insurance-reinsurance-web` | Spring Boot application, REST API, actuator, and OpenAPI |
| `domain-insurance-reinsurance-sdk` | Java client generated from the OpenAPI document |

### Dependency flow

```
web --> interfaces --> core --> infra --> core-insurance-reinsurance-sdk
sdk (generated from openapi.yml)
```

### Saga map

| Saga | Core calls | Compensation |
|------|------------|--------------|
| `RegisterTreatySaga` | `createTreaty` on `TreatiesApi` | Deletes the record through `deleteTreaty` |
| `UpdateTreatyStatusSaga` | Reads the current record, then updates its status through `updateTreaty` | Writes the previous status back through the core |
| Query | `getTreatyById` on `TreatiesApi` | None. A read has nothing to undo. |

Status values accepted by the core: `DRAFT`, `ACTIVE`, `RUN_OFF`, `EXPIRED`.

## Setup

### Prerequisites

- Java 25
- Apache Maven 3.9+
- `core-insurance-reinsurance-sdk` 26.10.3
- Apache Kafka on `localhost:9092` when event publishing is left at the default

### Environment

| Variable | Default | Description |
|----------|---------|-------------|
| `SERVER_ADDRESS` | `localhost` | Bind address |
| `SERVER_PORT` | `8076` | HTTP port |
| `ENDPOINT_CORE_INSURANCE_REINSURANCE` | `http://localhost:8080` | Base URL of `core-insurance-reinsurance` |

### Build and test

```bash
mvn clean verify
```

### Run

```bash
ENDPOINT_CORE_INSURANCE_REINSURANCE=http://localhost:8080 mvn spring-boot:run -pl domain-insurance-reinsurance-web
```

Swagger UI (non-production): [http://localhost:8076/swagger-ui.html](http://localhost:8076/swagger-ui.html)

## API

| Method | Path | Behavior |
|--------|------|----------|
| `POST` | `/api/v1/treaties` | Runs `RegisterTreatySaga` |
| `GET` | `/api/v1/treaties/{treatyId}` | Queries the core |
| `PUT` | `/api/v1/treaties/{treatyId}/status` | Runs `UpdateTreatyStatusSaga` |

The status request body is `status` plus an optional `statusReason`.
