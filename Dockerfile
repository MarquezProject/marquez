# Copyright 2018-2023 contributors to the Marquez project
# SPDX-License-Identifier: Apache-2.0

FROM eclipse-temurin:17 AS base
WORKDIR /usr/src/app
COPY gradle gradle
RUN ls -la gradle
COPY gradle.properties gradle.properties
RUN ls -la gradle.properties
COPY gradlew gradlew
RUN ls -la gradlew
COPY settings.gradle settings.gradle
RUN ls -la settings.gradle

# Make wrapper executable and fix line endings
RUN chmod +x ./gradlew
RUN sed -i 's/\r$//' ./gradlew

FROM base AS build
WORKDIR /usr/src/app
COPY build.gradle build.gradle
RUN ls -la build.gradle
COPY api ./api
RUN ls -la api
COPY clients/java ./clients/java
RUN ls -la clients/java
RUN ./gradlew clean :api:shadowJar --no-daemon --refresh-dependencies

FROM eclipse-temurin:17
RUN apt-get update && apt-get install -y postgresql-client bash coreutils dos2unix
WORKDIR /usr/src/app
COPY --from=build /usr/src/app/api/build/libs/marquez-*.jar /usr/src/app
RUN ls -la /usr/src/app/marquez-*.jar
COPY marquez.dev.yml marquez.dev.yml
RUN ls -la marquez.dev.yml
COPY docker/entrypoint.sh entrypoint.sh
RUN dos2unix entrypoint.sh && \
    chmod +x entrypoint.sh && \
    ls -la entrypoint.sh && \
    cat entrypoint.sh

EXPOSE 5000 5001
CMD ["/usr/src/app/entrypoint.sh"]
