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
WORKDIR /usr/src/app
COPY --from=build /usr/src/app/build/libs/marquez-all.jar marquez-all.jar
COPY docker/entrypoint.sh entrypoint.sh
COPY docker/wait-for-it.sh wait-for-it.sh
EXPOSE 5000
ENTRYPOINT ["/usr/src/app/entrypoint.sh"]
