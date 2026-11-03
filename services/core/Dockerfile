# Open Agriculture Core Service — container image (Quarkus JVM fast-jar).
# Runtime only: eclipse-temurin 21 JRE, non-root user, no cloud SDKs.
# Build first: mvn package (from services/core). Not built in this
# workspace (no Docker verified here); see ADR 0003.
FROM eclipse-temurin:21-jre

RUN groupadd -r app && useradd -r -g app app
WORKDIR /work

COPY target/quarkus-app/lib/ /work/lib/
COPY target/quarkus-app/*.jar /work/
COPY target/quarkus-app/app/ /work/app/
COPY target/quarkus-app/quarkus/ /work/quarkus/

USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/work/quarkus-run.jar"]
