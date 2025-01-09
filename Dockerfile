# Step 1: Use OpenJDK as the base image
FROM openjdk:8-jdk-slim AS base

# Step 2: Install curl and add MySQL Connector JAR
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
RUN mkdir -p /opt
RUN curl -o /opt/mysql-connector-java-8.0.33.jar https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.0.33/mysql-connector-j-8.0.33.jar

# Step 3: Build the Spring Boot application using Gradle
FROM gradle:6.9.1-jdk8 AS builder
WORKDIR /app
COPY --chown=gradle:gradle . .
RUN gradle clean build -x test

# Step 4: Combine MySQL Connector and Spring Boot application
FROM openjdk:8-jdk-slim
COPY --from=base /opt/mysql-connector-java-8.0.33.jar /opt/
COPY --from=builder /app/build/libs/*.jar /app/app.jar

# Step 5: Specify entrypoint with MySQL connector in classpath
ENTRYPOINT ["java", "-cp", "/opt/mysql-connector-java-8.0.33.jar:/app/app.jar", "org.springframework.boot.loader.JarLauncher"]
