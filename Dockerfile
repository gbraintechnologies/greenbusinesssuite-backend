# =========================
# Stage 1: Build
# =========================
FROM maven:3.9.9-eclipse-temurin-23 AS build

WORKDIR /app
COPY . .

RUN mvn clean package -DskipTests

# =========================
# Stage 2: Runtime
# =========================
FROM eclipse-temurin:23

WORKDIR /app

# Install curl and netcat for healthcheck
RUN apt-get update && apt-get install -y \
    curl \
    netcat-openbsd \
    && rm -rf /var/lib/apt/lists/*

# Create non-root user
RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser
USER appuser

# Copy JAR
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8081

# Health check - try actuator first, fallback to port check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8081/actuator/health || nc -zv localhost 8081 || exit 1

ENTRYPOINT ["java","-XX:+UseContainerSupport","-XX:MaxRAMPercentage=50.0","-Djava.security.egd=file:/dev/urandom","-jar","app.jar"]