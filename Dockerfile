FROM eclipse-temurin:21-jre

# Hardening: Run as non-root container user
RUN groupadd -r appgroup && useradd -r -g appgroup appuser

WORKDIR /app

COPY target/lease-document-approval-workflow-0.0.1-SNAPSHOT.jar /app/app.jar

# Create and grant permissions for documents upload directory
RUN mkdir -p /app/uploads/documents && chown -R appuser:appgroup /app

USER appuser

EXPOSE 8081

HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:8081/actuator/health || exit 1

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/app.jar"]
