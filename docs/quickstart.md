---
layout: quickstart
---

## Quickstart

#### PREREQUISITES

Before you begin, make sure you have already installed:

* [Docker 17.05](https://docs.docker.com/install)+
* [Docker Compose](https://docs.docker.com/compose/install)

#### SETUP

To checkout the Marquez source code run:

```
$ git clone git@github.com:MarquezProject/marquez.git && cd marquez
```

#### RUNNING WITH [DOCKER](https://github.com/MarquezProject/marquez/blob/master/Dockerfile)

The easiest way to get up and running is with Docker. From the base of the Marquez repository run:

```
$ docker-compose up --build
```

Marquez listens on port `5000` for all API calls and port `5001` for the admin interface. To verify the HTTP API server is running and listening on `localhost` browse to [http://localhost:5001](http://localhost:5001).

> **Note:** By default, the Metadata API does not require any form of authentication or authorization.

## Example

> **Note:** The example shows how to collect metadata via direct HTTP API calls using `curl`. But, you can also get started using our client library for [Python](https://github.com/MarquezProject/marquez-python).

#### STEP 1: CREATE A NAMESPACE

Before we can begin collecting metadata, we must first create a _namespace_. A `namespace` enables the contextual grouping of metadata for related jobs and datasets. Note that jobs and datasets are unique within a namespace, but not across namespaces (please see [data model](https://marquezproject.github.io/marquez/#data-model) for an introduction to the basic concepts of Marquez). In this quickstart, we will use the namespace `wedata`:

##### REQUEST

```bash
$ curl -X PUT http://localhost:5000/api/v1/namespaces/wedata \
  -H 'Content-Type: application/json' \
  -d '{
        "owner": "analytics",
        "description": "Contains datasets such as room bookings for each office."
      }'
```

##### RESPONSE

```bash
{
  "name": "wedata",
  "createdAt": "2019-06-08T19:11:59.430162Z",
  "owner": "analytics",
  "description": "Contains datasets such as room bookings for each office."
}
```

> **Note:** Marquez provides a `default` namespace to record metadata, but we encourage you to create your own.

#### STEP 2: CREATE A DATASOURCE

Each dataset must be associated with a _datasource_. A `datasource` is the physical location of a dataset, such as a table in PostgreSQL, or a file in S3. A datasource enables the grouping of physical datasets to their physical source.

##### REQUEST

```bash
$ curl -X POST http://localhost:5000/api/v1/datasources \
  -H 'Content-Type: application/json' \
  -d '{
        "name": "analytics_db",
        "connectionUrl": "jdbc:postgresql://localhost:5431/analytics"
      }'  
```

##### RESPONSE

```bash
{
  "name": "analytics_db",
  "createdAt": "2019-06-08T19:13:00.749356Z",
  "urn": "urn:datasource:postgresql:analytics_db",
  "connectionUrl": "jdbc:postgresql://localhost:5431/analytics"
}
```

#### STEP 3: ADD DATASET TO NAMESPACE

##### REQUEST

```bash
$ curl -X POST http://localhost:5000/api/v1/namespaces/wedata/datasets \
  -H 'Content-Type: application/json' \
  -d '{ 
        "name": "public.room_bookings",
        "datasourceUrn": "urn:datasource:postgresql:analytics_db",
        "description": "All global room bookings for each office."
      }'
```

##### RESPONSE

```bash
{
  "name": "public.room_bookings",
  "createdAt": "2019-06-08T19:13:34.507414Z",
  "urn": "urn:dataset:analytics_db:public.room_bookings",
  "datasourceUrn": "urn:datasource:postgresql:analytics_db",
  "description": "All global room bookings for each office."
}
```

#### STEP 4: ADD JOB TO NAMESPACE

##### REQUEST

```bash
$ curl -X PUT http://localhost:5000/api/v1/namespaces/wedata/jobs/room_bookings_7_days \
  -H 'Content-Type: application/json' \
  -d '{
        "inputDatasetUrns": ["urn:dataset:analytics_db:public.room_bookings"],
        "outputDatasetUrns": [],
        "location": "https://github.com/wework/jobs/commit/124f6089ad4c5fcbb1d7b33cbb5d3a9521c5d32c",
        "description": "Determine weekly room booking occupancy patterns."
      }'
```

##### RESPONSE

```bash
{
  "name": "room_bookings_7_days",
  "createdAt": "2019-06-08T19:13:58.434995Z",
  "updatedAt": "2019-06-08T19:13:58.434995Z",
  "inputDatasetUrns": [
    "urn:dataset:analytics_db:public.room_bookings"
  ],
  "outputDatasetUrns": [],
  "location": "https://github.com/wework/jobs/commit/124f6089ad4c5fcbb1d7b33cbb5d3a9521c5d32c",
  "description": "Determine weekly room booking occupancy patterns."
}
```

#### STEP 5: CREATE A JOB RUN

##### REQUEST

```bash
$ curl -X POST http://localhost:5000/api/v1/namespaces/wedata/jobs/room_bookings_7_days/runs \
  -H 'Content-Type: application/json' \
  -d '{
        "runArgs": "--email=wedata@wework.com"
      }'
```

##### RESPONSE

```bash
{
  "runId": "0fddd72a-a04f-463e-a0c6-fb5e5ff82fcb",
  "nominalStartTime": null,
  "nominalEndTime": null,
  "runArgs": "--email=wedata@wework.com",
  "runState": "NEW"
}
```

#### STEP 6: RECORD A RUN

```bash
$ curl -X PUT http://localhost:5000/api/v1/jobs/runs/0fddd72a-a04f-463e-a0c6-fb5e5ff82fcb/run
```

#### STEP 7: RECORD A COMPLETE RUN

```bash
$ curl -X PUT http://localhost:5000/api/v1/jobs/runs/0fddd72a-a04f-463e-a0c6-fb5e5ff82fcb/complete
```
