FROM eclipse-temurin:17 AS base
WORKDIR /usr/src/app
COPY gradle gradle
COPY gradle.properties gradle.properties
COPY gradlew gradlew
COPY settings.gradle settings.gradle
RUN ./gradlew --version

FROM base AS build
WORKDIR /usr/src/app
COPY build.gradle build.gradle
COPY api ./api
COPY api/build.gradle ./api/build.gradle
COPY clients/java ./clients/java
RUN ./gradlew --no-daemon :api:shadowJar

FROM eclipse-temurin:17
RUN apt-get update && apt-get install -y postgresql-client bash coreutils
WORKDIR /usr/src/app
COPY --from=build /usr/src/app/api/build/libs/marquez-*.jar /usr/src/app
COPY marquez.dev.yml marquez.dev.yml
COPY docker/entrypoint.sh entrypoint.sh
EXPOSE 5000 5001
ENTRYPOINT ["/usr/src/app/entrypoint.sh"]
