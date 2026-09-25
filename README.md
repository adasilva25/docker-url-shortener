# Dockerized URL Shortener

A containerized URL shortener API built with Spring Boot and PostgreSQL.

The project demonstrates a practical containerization setup with isolated services, persistent storage, health monitoring, runtime secrets, non-root execution, and reproducible builds using Docker Compose.

## Technology Stack

- Java 21
- Spring Boot
- Spring Data JPA
- PostgreSQL 16
- Flyway
- Docker
- Docker Compose
- Spring Boot Actuator

## Architecture

The application consists of two containerized services:

- **API:** Spring Boot REST application exposed locally on port `8080`.
- **Database:** PostgreSQL instance accessible only through the internal Docker network.

PostgreSQL data is stored in a named Docker volume and remains independent from the database container lifecycle.

The API communicates with PostgreSQL through Docker's internal DNS using the Compose service name `db`.

PostgreSQL is not exposed directly to the host.

## Container Design

### Application Image

The API image uses a multi-stage Docker build.

The build stage contains the JDK, Maven wrapper, project dependencies, and source code required to compile the application.

The runtime stage contains only the JRE, the application JAR, health-check tooling, and the dedicated runtime user.

This keeps build tooling and source code out of the final runtime image.

### Non-root Execution

The application runs under a dedicated Linux user and group instead of `root`.

This follows the principle of least privilege and limits the permissions available to the application process inside the container.

### Networking

Both services belong to an internal Docker network.

The API connects to PostgreSQL using:

```text
db:5432
```

Docker's internal DNS resolves `db` to the corresponding PostgreSQL container.

Only the API port is published to the host:

```text
127.0.0.1:8080
```

The PostgreSQL port is intentionally not published.

### Persistent Storage

PostgreSQL uses the named volume:

```text
url-shortener-db-data
```

Database data therefore remains available when containers are stopped, removed, or recreated.

### Secrets

The database password is supplied at runtime using Docker Compose secrets.

Secrets are not stored in:

- the Dockerfile
- the application image
- source control
- application environment variables

Docker Compose mounts the secret inside the authorized containers under `/run/secrets/`.

Spring Boot loads its datasource password using Config Tree external configuration.

### Health Checks

Both services expose health information to Docker.

PostgreSQL is checked using `pg_isready`.

The API is checked through:

```text
/actuator/health
```

The API depends on PostgreSQL reaching a healthy state before startup.

## Repository Structure

```text
docker-url-shortener/
├── app/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
├── secrets/
├── .env.example
├── .gitignore
├── compose.yaml
└── README.md
```

The `secrets/` directory and local `.env` file are excluded from source control.

## Running Locally

### Requirements

Only the following tools are required:

- Docker
- Docker Compose

Java, Maven, and PostgreSQL do not need to be installed on the host.

### Configure the environment

Create the local environment file:

```bash
cp .env.example .env
```

Create the secrets directory:

```bash
mkdir -p secrets
```

Create the local database password:

```bash
printf "your-local-password" > secrets/db-password.txt
```

The password must remain local and must not be committed to source control.

### Start the application

```bash
docker compose up --build
```

Docker Compose will build the API image and start the required services.

### Verify service status

```bash
docker compose ps
```

### Verify API health

```bash
curl http://localhost:8080/actuator/health
```

A healthy application returns:

```json
{
  "status": "UP"
}
```

### Stop the application

```bash
docker compose down
```

The PostgreSQL volume is preserved.

To inspect existing volumes:

```bash
docker volume ls
```

## Docker Practices Applied

The container configuration applies the following practices:

- trusted and explicitly versioned base images
- multi-stage Docker builds
- dependency-aware build caching
- `.dockerignore` to reduce build context
- dedicated non-root runtime user
- runtime secret injection
- configuration externalization
- internal service networking
- minimal host port exposure
- persistent named volumes
- application and database health checks
- service startup dependencies based on health
- disposable and reproducible containers

## Configuration Strategy

Runtime-specific values are externalized from the application image.

Non-sensitive configuration is provided through environment variables.

Sensitive configuration is provided through Docker secrets.

This allows the same application image to run with different runtime configurations without rebuilding the image.

## Database Management

Database schema changes are managed with Flyway migrations.

Spring Data JPA handles application persistence, while Flyway maintains database schema evolution.

The PostgreSQL container is treated as replaceable infrastructure; persistent database state is maintained independently through Docker-managed storage.

## Security Considerations

The project intentionally limits container privileges and service exposure.

Key measures include:

- API execution as a non-root user
- no database port published to the host
- database credentials excluded from the image and repository
- explicit secret access per service
- isolated application/database networking
- minimal runtime image
- no build tools in the final application image

The local secret file is suitable for local Docker Compose execution. A production deployment should use an appropriate external secret-management solution provided by the target platform.

## Build

The API image can also be built independently:

```bash
docker build -t url-shortener-api:1.0.0 ./app
```

Inspect the resulting image:

```bash
docker image inspect url-shortener-api:1.0.0
```

Inspect its layers:

```bash
docker history url-shortener-api:1.0.0
```

## Scope

This repository focuses on a compact containerized service architecture and the operational concerns around building and running it consistently.

The application intentionally keeps its business domain small so that container lifecycle, networking, persistence, configuration, security, and image construction remain explicit and easy to inspect.

# License

This project is publicly available for reference purposes only.

No permission is granted to use, copy, modify, distribute, or reuse the code or documentation without prior permission from the repository owner.
