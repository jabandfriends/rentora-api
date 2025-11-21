# Stage 1: Build the Spring Boot application
FROM gradle:8.8-jdk21-alpine AS builder
WORKDIR /app
COPY . .
RUN gradle clean bootJar --no-daemon -x test

# Stage 2: Run the application with Tesseract preinstalled
FROM ghcr.io/jitesoft/tesseract:5-5.5.1
WORKDIR /app

# Switch to root to install OpenJDK
USER root
RUN apt-get update && \
    apt-get install -y openjdk-21-jdk && \
    rm -rf /var/lib/apt/lists/*

# Copy tessdata
COPY src/main/resources/tessdata /usr/share/tesseract-ocr/5/tessdata
ENV TESSDATA_PREFIX=/usr/share/tesseract-ocr/5/tessdata

# Copy Spring Boot JAR
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]