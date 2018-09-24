FROM openjdk:9-jdk-slim AS base
WORKDIR /usr/src/app
COPY gradle gradle
COPY gradlew gradlew
COPY settings.gradle settings.gradle
RUN ./gradlew --version

FROM base AS build
WORKDIR /usr/src/app
COPY . .
RUN ./gradlew --no-daemon shadowJar

FROM openjdk:9-jdk-slim
WORKDIR /usr/src/app
COPY --from=build /usr/src/app/build/libs/marquez-all.jar marquez.jar
EXPOSE 5000
ENTRYPOINT ["java", "-jar", "marquez.jar", "server", "$MARQUEZ_CONFG"]
