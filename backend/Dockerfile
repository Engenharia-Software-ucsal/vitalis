# syntax=docker/dockerfile:1
FROM maven:3.9-eclipse-temurin-21 AS build
ARG MODULE
WORKDIR /src

COPY pom.xml ./
COPY gateway/pom.xml ./gateway/pom.xml
COPY servico-clinico/pom.xml ./servico-clinico/pom.xml
COPY servico-leitos/pom.xml ./servico-leitos/pom.xml
COPY servico-ia/pom.xml ./servico-ia/pom.xml
RUN --mount=type=cache,target=/root/.m2 mvn -B -q -pl ${MODULE} -am dependency:go-offline

COPY ${MODULE}/src ./${MODULE}/src
RUN --mount=type=cache,target=/root/.m2 mvn -B -q -pl ${MODULE} -am package -DskipTests

FROM eclipse-temurin:21-jre
ARG MODULE
WORKDIR /app

RUN groupadd --system appuser && useradd --system --gid appuser appuser
COPY --from=build --chown=appuser:appuser /src/${MODULE}/target/${MODULE}.jar app.jar
USER appuser

ENTRYPOINT ["java", "-jar", "app.jar"]
