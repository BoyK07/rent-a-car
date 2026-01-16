# Build stage
FROM gradle:8.14-jdk21 AS builder

WORKDIR /app

# Copy gradle files first for better caching
COPY app/build.gradle.kts app/settings.gradle.kts app/gradle.properties ./
COPY app/gradle ./gradle
COPY app/gradlew ./

# Copy all modules
COPY app/modules ./modules

# Build the application
RUN ./gradlew :server:buildFatJar --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy the built jar from builder stage
COPY --from=builder /app/modules/server/build/libs/*-all.jar ./app.jar

# Set ownership
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose the application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/health || exit 1

# Default to H2 for development, override with environment variables for production
ENV DB_PROVIDER=h2 \
    KTOR_ENV=production

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
