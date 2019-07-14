FROM openjdk:8-jdk AS base
WORKDIR /usr/src/app
COPY gradle gradle
COPY gradle.properties gradle.properties
COPY gradlew gradlew
COPY settings.gradle settings.gradle
RUN ./gradlew --version

FROM base AS build
WORKDIR /usr/src/app
COPY src ./src
COPY build.gradle build.gradle
RUN ./gradlew --no-daemon shadowJar

FROM openjdk:8-jre
RUN apt-get update && apt-get install -y --no-install-recommends postgresql-client-9.6
WORKDIR /usr/src/app
COPY --from=build /usr/src/app/build/libs/marquez-*.jar /usr/src/app
COPY docker/wait-for-db.sh wait-for-db.sh
COPY docker/entrypoint.sh entrypoint.sh
EXPOSE 5000 5001
ENTRYPOINT ["/usr/src/app/entrypoint.sh"]
