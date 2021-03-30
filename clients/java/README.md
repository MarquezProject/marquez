# Marquez Java Client

Java client for [Marquez](https://github.com/MarquezProject/marquez).

## Installation

Maven:

```xml
<dependency>
    <groupId>io.github.marquezproject</groupId>
    <artifactId>marquez-java</artifactId>
    <version>0.13.0</version>
</dependency>
```

or Gradle:

```groovy
implementation 'io.github.marquezproject:marquez-java:0.13.0'
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
