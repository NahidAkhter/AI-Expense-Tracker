# Multi-stage build for Java 21
FROM gradle:8.5-jdk21-alpine AS build
WORKDIR /app

# Copy gradle files
COPY build.gradle .
COPY settings.gradle .
COPY gradle ./gradle
COPY src ./src

# Build the application
RUN gradle clean build -x test

# Runtime stage with Java 21
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Install Python3 for wait script and curl/wget for health checks
RUN apk add --no-cache python3 curl wget

# Create a non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
# Create logs directory
RUN mkdir -p /app/logs && chown spring:spring /app/logs
USER spring

# Copy the built Spring Boot fat jar explicitly (not the *-plain.jar)
COPY --from=build /app/build/libs/expense-tracker.jar app.jar

# Copy wait script (ensure it's readable by spring user)
COPY --chown=spring:spring wait-for-it.py /wait-for-it.py
RUN chmod +x /wait-for-it.py


EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/expenses/health || exit 1

# Wait for PostgreSQL and start application
CMD ["python3", "/wait-for-it.py", "postgres", "5432", "--command", "java", "-jar", "app.jar"]
