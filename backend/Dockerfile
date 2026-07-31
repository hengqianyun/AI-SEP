# syntax=docker/dockerfile:1.7

FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml ./
COPY app/data-chain-service/pom.xml app/data-chain-service/pom.xml
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -pl app/data-chain-service -am dependency:go-offline

COPY app/data-chain-service/src app/data-chain-service/src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -pl app/data-chain-service -am -DskipTests package

FROM eclipse-temurin:17-jre-jammy

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system --gid 10001 app \
    && useradd --system --uid 10001 --gid app --home-dir /app app

WORKDIR /app
COPY --from=build --chown=app:app \
    /workspace/app/data-chain-service/target/data-chain-service-*.jar \
    /app/app.jar

USER app

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -Dfile.encoding=UTF-8 -Duser.timezone=Asia/Shanghai"

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
