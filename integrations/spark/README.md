# Marquez Spark Agent - `DEPRECATED`

[![Project status](https://img.shields.io/badge/status-deprecated-orange.svg)]()

This repository has been moved to [`OpenLineage/integration/spark`](https://github.com/OpenLineage/OpenLineage/tree/main/integration/spark). **The** `marquez-spark` **library extends the class** [`OpenLineageSparkListener`](https://github.com/OpenLineage/OpenLineage/blob/main/integration/spark/src/main/java/openlineage/spark/agent/OpenLineageSparkListener.java) **for backwards compatibility.**

## Installation

Maven:

```xml
<dependency>
    <groupId>io.github.marquezproject</groupId>
    <artifactId>marquez-spark</artifactId>
    <version>0.16.1</version>
</dependency>
```

or Gradle:

```groovy
implementation 'io.github.marquezproject:marquez-spark:0.16.1
```

## Usage

```python
from pyspark.sql import SparkSession

spark = (SparkSession.builder.master('local')
         .appName('sample_spark')
         .config('spark.jars.packages', 'io.github.marquez:marquez-spark-0.16.1.jar'
         .config('spark.extraListeners', 'marquez.spark.agent.SparkListener')
         .config('spark.openlineage.url', 'http://<marquez-host>/api/v1/namespaces/<marquez-namespace>')
         .getOrCreate())
```
