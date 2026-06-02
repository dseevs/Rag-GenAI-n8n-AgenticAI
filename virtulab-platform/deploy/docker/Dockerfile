# Build any VirtuLab microservice JAR into a runtime image.
# Usage: docker build --build-arg SERVICE=auth-service -t virtulab/auth-service:dev .

FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
COPY contracts ./contracts
COPY services ./services
ARG SERVICE=auth-service
RUN mvn -q -pl "services/${SERVICE}" -am package -DskipTests

FROM eclipse-temurin:17-jre-jammy
ARG SERVICE=auth-service
RUN useradd -r -u 1001 virtulab
WORKDIR /app
COPY --from=build /build/services/${SERVICE}/target/${SERVICE}-0.1.0-SNAPSHOT.jar /app/app.jar
USER virtulab
EXPOSE 8080
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
