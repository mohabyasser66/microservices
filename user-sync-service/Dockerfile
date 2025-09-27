FROM openjdk:21-jdk-slim

VOLUME /tmp

# Create app directory
WORKDIR /app

# Copy the jar file
COPY target/user-sync-service-1.0.0.jar app.jar

# Create logs directory
RUN mkdir -p logs

# Expose port
EXPOSE 8086

# Health check
HEALTHCHECK --interval=30s --timeout=30s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8086/api/sync/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
