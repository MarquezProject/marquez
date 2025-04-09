FROM eclipse-temurin:17 AS base
WORKDIR /usr/src/app
COPY gradle gradle
COPY gradle.properties gradle.properties
COPY gradlew gradlew
COPY settings.gradle settings.gradle

# Make wrapper executable and fix line endings
RUN chmod +x ./gradlew
RUN sed -i 's/\r$//' ./gradlew

FROM base AS build
WORKDIR /usr/src/app
COPY build.gradle build.gradle
COPY api ./api
COPY clients/java ./clients/java
RUN ./gradlew --no-daemon clean :api:shadowJar

FROM eclipse-temurin:17
RUN apt-get update && apt-get install -y postgresql-client bash coreutils
WORKDIR /usr/src/app
COPY --from=build /usr/src/app/api/build/libs/marquez-*.jar /usr/src/app
COPY marquez.dev.yml marquez.dev.yml
COPY docker/entrypoint.sh entrypoint.sh

# Make entrypoint executable
RUN chmod +x entrypoint.sh

EXPOSE 5000 5001
ENTRYPOINT ["/usr/src/app/entrypoint.sh"]
