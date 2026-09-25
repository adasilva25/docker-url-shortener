# Build stage
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

RUN ./mvnw dependency:go-offline

COPY src/ src/

RUN ./mvnw clean package -DskipTests


# Runtime stage
FROM eclipse-temurin:21-jre

WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

RUN groupadd --gid 10001 appgroup \
    && useradd --uid 10001 \
       --gid appgroup \
       --no-create-home \
       --shell /usr/sbin/nologin \
       appuser

COPY --from=build --chown=appuser:appgroup /app/target/*.jar app.jar

USER appuser:appgroup

ENTRYPOINT ["java", "-jar", "app.jar"]