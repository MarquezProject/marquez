# Marquez Java Client

Java client for [Marquez](https://github.com/MarquezProject/marquez).

## Installation

Maven:

```xml
<dependency>
    <groupId>io.github.marquezproject</groupId>
    <artifactId>marquez-java</artifactId>
    <version>0.25.0</version>
</dependency>
```

or Gradle:

```groovy
implementation 'io.github.marquezproject:marquez-java:0.25.0
```

## Usage

### Reading Metadata
```java
// Connect to http://localhost:5000
MarquezClient client = MarquezClient().builder()
  .baseUrl("http://localhost:5000")
  .build()

// List namespaces
List<Namespace> namespaces = client.listNamespaces();
```
### Writing Metadata
To collect OpenLineage events using Marquez, please use the [openlineage-java](https://search.maven.org/artifact/io.openlineage/openlineage-java) library. OpenLineage is an Open Standard for lineage metadata collection designed to collect metadata for a job in execution.

## HTTPS

```java
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import marquez.client.MarquezClient;
.
.
KeyManager[] keyManager = setUpKeyManagers();
TrustManager[] trustManager = setUpTrustManagers();

SSLContext sslContext = SSLContext.getInstance("TLS");
sslContext.init(keyManager, trustManager, null);

// Connect to https://localhost:5000
MarquezClient client = MarquezClient.builder()
  .sslContext(sslContext)
  .baseUrl("https://localhost:5000")
  .build();
```
