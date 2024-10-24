# Marquez Java Client

Java client for [Marquez](https://github.com/MarquezProject/marquez).

## Installation

Maven:

```xml
<dependency>
    <groupId>io.github.marquezproject</groupId>
    <artifactId>marquez-java</artifactId>
    <version>0.50.0</version>
</dependency>
```

or Gradle:

```groovy
implementation 'io.github.marquezproject:marquez-java:0.50.0
```

## Usage

### Reading Metadata
```java
// Connect to http://localhost:5000
MarquezClient client = MarquezClient.builder()
  .baseUrl("http://localhost:5000")
  .build();

// List namespaces
List<Namespace> namespaces = client.listNamespaces();
```

### Customization

The client uses Apache's `httpclient` under the hood. The defaults can be a bit limiting, but you can optionally provide a function that accepts the `HttpClientBuilder` so you can customize it before it's finalised:

```java
SSLContext sslContext = SSLContext.getInstance("TLS");
HttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(...);

MarquezClient client = MarquezClient.builder()
  .baseUrl("http://localhost:5000")
  .httpCustomizer(httpClientBuilder -> {
    httpClientBuilder.setSSLContext(sslContext);
    httpClientBuilder.setConnectionManager(connectionManager);
  })
  .build();
```

### Writing Metadata
To collect OpenLineage events using Marquez, please use the [openlineage-java](https://search.maven.org/artifact/io.openlineage/openlineage-java) library. OpenLineage is an Open Standard for lineage metadata collection designed to collect metadata for a job in execution.

----
SPDX-License-Identifier: Apache-2.0
Copyright 2018-2023 contributors to the Marquez project.
