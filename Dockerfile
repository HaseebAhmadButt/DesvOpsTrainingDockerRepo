# ---------- Stage 1: Build ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy only pom.xml first so dependency downloads are cached
# and only re-run when pom.xml changes
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Copy source and build the jar (tests skipped for faster image builds)
COPY src ./src
RUN mvn -B clean package -DskipTests

# ---------- Stage 2: Runtime ----------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Run as a non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy only the built jar from the build stage
COPY --from=build /app/target/spring-crud-demo-1.0.0.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]