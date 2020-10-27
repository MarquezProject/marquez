# Marquez Java Client

[![CircleCI](https://circleci.com/gh/MarquezProject/marquez-java/tree/main.svg?style=shield)](https://circleci.com/gh/MarquezProject/marquez-java/tree/main) 
[![codecov](https://codecov.io/gh/MarquezProject/marquez-java/branch/main/graph/badge.svg)](https://codecov.io/gh/MarquezProject/marquez-java/branch/main)
[![status](https://img.shields.io/badge/status-WIP-yellow.svg)](#status)
[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/marquez-project/community)
[![license](https://img.shields.io/badge/license-Apache_2.0-blue.svg)](https://raw.githubusercontent.com/MarquezProject/marquez-java/main/LICENSE)
[![Known Vulnerabilities](https://snyk.io/test/github/MarquezProject/marquez-java/badge.svg)](https://snyk.io/test/github/MarquezProject/marquez-java)

Java client for [Marquez](https://github.com/MarquezProject/marquez).

## Status

This library is under active development at [Datakin](https://twitter.com/DatakinHQ). 

## Documentation

See the [API docs](https://marquezproject.github.io/marquez/openapi.html).

## Installation

Maven:

```xml
<dependency>
    <groupId>io.github.marquezproject</groupId>
    <artifactId>marquez-java</artifactId>
    <version>0.4.3</version>
</dependency>
```

or Gradle:

```groovy
implementation 'io.github.marquezproject:marquez-java:0.4.3'
```

## Usage

```java
MarquezClient client = MarquezClient().builder()
    .baseUrl("http://localhost:5000")
    .build()

// Metadata
NamespaceMeta meta = NamespaceMeta().builder()
    .ownerName("me")
    .description("My first namespace!")
    .build()

// Create namespace 
Namespace namespace = client.createNamespace("my-namespace", meta);
```

## Contributing

See [CONTRIBUTING.md](https://github.com/MarquezProject/marquez-java/blob/main/CONTRIBUTING.md) for more details about how to contribute.
