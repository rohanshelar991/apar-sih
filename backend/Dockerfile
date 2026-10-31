FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B -ntp dependency:go-offline
COPY src ./src
# Tests run in the required CI job before deployment.
RUN mvn -B -ntp -DskipTests package

FROM eclipse-temurin:17-jre-jammy
RUN groupadd --system apaar && useradd --system --gid apaar --home-dir /app apaar
WORKDIR /app
COPY --from=build --chown=apaar:apaar /build/target/apaar-connect-1.0.0-SNAPSHOT.jar app.jar
USER apaar
ENV SPRING_PROFILES_ACTIVE=postgres,render
ENV SEED_DEMO_DATA=false
ENV JAVA_TOOL_OPTIONS="-Duser.timezone=UTC -XX:MaxRAMPercentage=65.0"
EXPOSE 10000
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
