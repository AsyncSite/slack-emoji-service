# Dockerfile for slack-emoji-service
# Requires JAR to be built locally first: ./gradlew clean build
# This approach avoids GitHub authentication issues during Docker build

FROM eclipse-temurin:21-jre
WORKDIR /app

# Create non-root user for security and logs directory
RUN useradd -m -u 1001 appuser && \
    mkdir -p /app/logs && \
    chown -R appuser:appuser /app && \
    chmod 755 /app/logs

# Copy pre-built JAR file
COPY --chown=appuser:appuser build/libs/*.jar app.jar

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:13084/actuator/health || exit 1

# Switch to non-root user
USER appuser

# Expose application port
EXPOSE 13084

# Set JVM options for container environment
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-local} -jar app.jar"]