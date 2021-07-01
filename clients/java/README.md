# Marquez Java Client

Java client for [Marquez](https://github.com/MarquezProject/marquez).

## Installation

Maven:

```xml
<dependency>
    <groupId>io.github.marquezproject</groupId>
    <artifactId>marquez-java</artifactId>
    <version>0.16.0-rc.1</version>
</dependency>
```

or Gradle:

```groovy
implementation 'io.github.marquezproject:marquez-java:0.16.0-rc.1
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

### Supply an SSLContext to enable TLS
```java
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import marquez.client.MarquezClient;
...
KeyManager[] keyManager = setUpKeyManagers();
TrustManager[] trustManager = setUpTrustManagers();
SSLContext sslContext = SSLContext.getInstance("TLS");
sslContext.init(keyManager, trustManager, null);
MarquezClient client = MarquezClient.builder()
        .sslContext(sslContext)
        .baseUrl("https://localhost:5000")
        .build();
```
