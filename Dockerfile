FROM openjdk:9-jdk-slim AS builder
WORKDIR /usr/src/app
COPY . /usr/src/app
RUN ./gradlew clean build

FROM openjdk:9-jre-slim
WORKDIR /usr/src/app
COPY --from=builder /usr/src/app/build/libs/marquez-all.jar marquez.jar
COPY config.yml . # Remove and set with env variable instead.
EXPOSE 5000
CMD ["/usr/bin/java", "-jar", "marquez.jar", "server", "config.yml"]
