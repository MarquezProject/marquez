# Marquez Java Client

[![CircleCI](https://circleci.com/gh/MarquezProject/marquez-java/tree/master.svg?style=shield)](https://circleci.com/gh/MarquezProject/marquez-java/tree/master) 
[![codecov](https://codecov.io/gh/MarquezProject/marquez-java/branch/master/graph/badge.svg)](https://codecov.io/gh/MarquezProject/marquez-java/branch/master)
[![status](https://img.shields.io/badge/status-WIP-yellow.svg)](#status)
[![license](https://img.shields.io/badge/license-Apache_2.0-blue.svg)](https://raw.githubusercontent.com/MarquezProject/marquez-java/master/LICENSE)

Java client for [Marquez](https://github.com/MarquezProject/marquez).

## Status

This library is under active development at [The We Company](https://www.we.co). 

## Documentation

See the [API docs](https://marquezproject.github.io/marquez/openapi.html).

## Installation

Maven:

```xml
<dependency>
    <groupId>io.github.marquezproject</groupId>
    <artifactId>marquez-java</artifactId>
    <version>0.1.0</version>
</dependency>
```

or Gradle:

```groovy
compile 'io.github.marquezproject:marquez-java:0.1.0'
```
## Usage

```java
MarquezClient client = MarquezClient().builder()
    .baseUrl("http://localhost:5000/api/v1")
    .build()
     
// Metadata
NamespaceMeta meta = NamespaceMeta().builder()
    .name("example-namespace")
    .ownerName("owner@example.com")
    .description("example description")
    .build()

// Create namespace 
Namespace namespace = client.createNamespace(meta);
```

## Contributing

See [CONTRIBUTING.md](https://github.com/MarquezProject/marquez-java/blob/master/CONTRIBUTING.md) for more details about how to contribute.
