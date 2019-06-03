---
layout: docs
---

## Quickstart

#### SETUP

To checkout the Marquez source code run:

```
$ git clone git@github.com:MarquezProject/marquez.git && cd marquez
```

#### RUNNING WITH [DOCKER](https://github.com/MarquezProject/marquez/blob/master/Dockerfile)

The easiest way to get up and running is with Docker. From the base of the Marquez repository run:

```
$ docker-compose up
```

Marquez listens on port `5000` for all API calls and port `5001` for the admin interface. To verify the HTTP server is running and listening on `localhost` browse to [http://localhost:8081](http://localhost:8081).

> **Note:** By default, the Marquez HTTP API does not require any form of authentication or authorization.

## Example

Before we can begin collecting metadata, we must first create a _namespace_. A `namespace` enables the contextual grouping of metadata for related jobs and datasets. Note that jobs and datasets are unique within a namespace, but not across namespaces (please see [data model](https://marquezproject.github.io/marquez/#data-model) for an introduction to the data model of Marquez).

Marquez provides a `default` namespace to record metadata, but we encourage you to create your own.

> **Note:** The example shows how to collect metadata via direct HTTP API calls using `curl`. But, you can also get started using our client library for [Python](https://github.com/MarquezProject/marquez-python).

#### CREATE A NAMESPACE

```bash
$ curl -X PUT http://localhost:5000/api/v1/namespaces/wedata \
  -H 'Content-Type: application/json' \
  -d '{
        "owner": "analytics",
        "description": "Contains datasets such as room bookings for each office."
      }'
```

#### CREATE A DATASOURCE

```bash
$ curl -X POST http://localhost:5000/api/v1/datasources \
  -H 'Content-Type: application/json' \
  -d '{
        "name": "analytics_db",
        "connectionUrl": "jdbc:postgresql://localhost:5431/analytics"
      }'
```

#### CREATE A DATASET

```bash
$ curl -X POST http://localhost:5000/api/v1/namespaces/wedata/datasets \
  -H 'Content-Type: application/json' \
  -d '{ 
        "name": "public.room_bookings",
        "datasourceUrn": "urn:datasource:postgresql:analytics_db",
        "description": "All global room bookings for each office."
      }'
```

#### ADD JOB TO NAMESPACE

```bash
$ curl -X PUT http://localhost:5000/api/v1/namespaces/wedata/jobs/room_bookings_7_days \
  -H 'Content-Type: application/json' \
  -d '{
        "inputDatasetUrns": ["urn:dataset:analytics_db:public.room_bookings"],
        "outputDatasetUrns": ["urn:dataset:analytics_db:public.room_bookings_7_days"],
        "location": "https://github.com/wework/jobs/commit/124f6089ad4c5fcbb1d7b33cbb5d3a9521c5d32c",
        "description": "Determine weekly room booking occupancy patterns."
      }'
```

#### RECORD A JOB RUN

```bash
$ curl -X POST http://localhost:5000/api/v1/namespaces/wedata/jobs/room_bookings_7_days/runs \
  -H 'Content-Type: application/json' \
  -d '{
        "runArgs": "--output=s3://output/"
      }'
```

#### RECORD A RUN

```bash
$ curl -X PUT http://localhost:5000/api/v1/jobs/runs/dba53dae-0429-467e-a502-d4c71cd6de79/run
```

#### RECORD A COMPLETE RUN

```bash
$ curl -X PUT http://localhost:5000/api/v1/jobs/runs/dba53dae-0429-467e-a502-d4c71cd6de79/complete
```
