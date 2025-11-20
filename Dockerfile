# Stage 1: Build the application
FROM gradle:8.8-jdk21-alpine AS builder
WORKDIR /app
COPY . .
RUN gradle clean bootJar --no-daemon -x test

# Stage 2: Run the application
FROM openjdk:21-ea-21-jdk-slim
WORKDIR /app

# Install Tesseract and dependencies
RUN apt-get update && \
    apt-get install -y tesseract-ocr libtesseract-dev libleptonica-dev && \
    rm -rf /var/lib/apt/lists/*

# Copy tessdata folder from the project into container
COPY src/main/resources/tessdata /tessdata

# Set TESSDATA_PREFIX environment variable
ENV TESSDATA_PREFIX=/tessdata

# Copy built JAR
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
