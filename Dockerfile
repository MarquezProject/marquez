# Eclipse Temurin 17.0.21.1+2 contains the fix for CVE-2026-114938 (Java4Shell).
# Adoptium has published that build for linux/amd64. The linux/aarch64 binary
# is not in the release yet. ARM64 is outside the vulnerable AVX-512 path, so
# that architecture installs the newest published Temurin 17 JDK. The runtime
# still disables UseBASE64Intrinsics.
FROM debian:bookworm-slim AS temurin

ARG TARGETARCH

RUN apt-get update \
 && apt-get install -y --no-install-recommends ca-certificates curl \
 && rm -rf /var/lib/apt/lists/*

RUN set -eux; \
    case "${TARGETARCH}" in \
      amd64) \
        URL="https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.21.1%2B2/OpenJDK17U-jdk_x64_linux_hotspot_17.0.21.1_2.tar.gz"; \
        SHA256="0b0ac5eab6db6abaec68c9913bcddb31321e5da411c45073d520d9c125e6f7b4"; \
        ;; \
      arm64) \
        URL="https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.21%2B9/OpenJDK17U-jdk_aarch64_linux_hotspot_17.0.21_9.tar.gz"; \
        SHA256="3e4eca7ead088f080af1476007decf010a240a1eb4e1e78c54fa32fbcc347084"; \
        ;; \
      *) \
        echo "Unsupported architecture: ${TARGETARCH}" >&2; \
        exit 1; \
        ;; \
    esac; \
    curl -fsSL -o /tmp/jdk.tar.gz "${URL}"; \
    echo "${SHA256}  /tmp/jdk.tar.gz" | sha256sum -c -; \
    mkdir -p /opt/java; \
    tar -xzf /tmp/jdk.tar.gz -C /opt/java --strip-components=1; \
    rm /tmp/jdk.tar.gz

ENV JAVA_HOME=/opt/java
ENV PATH="${JAVA_HOME}/bin:${PATH}"

FROM temurin AS base
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
COPY clients/java ./clients/java
RUN ./gradlew --no-daemon clean :api:shadowJar

FROM temurin
RUN apt-get update && apt-get install -y --no-install-recommends postgresql-client bash coreutils \
 && rm -rf /var/lib/apt/lists/*
WORKDIR /usr/src/app
COPY --from=build /usr/src/app/api/build/libs/marquez-*.jar /usr/src/app
COPY marquez.dev.yml marquez.dev.yml
COPY docker/entrypoint.sh entrypoint.sh
EXPOSE 5000 5001
ENTRYPOINT ["/usr/src/app/entrypoint.sh"]
