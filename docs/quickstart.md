---
layout: quickstart
---

## Quickstart

#### PREREQUISITES

Before you begin, make sure you have installed:

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

> **Note:** By default, the HTTP API does not require any form of authentication or authorization.

## Example

In this example, we show how you can record job and dataset metadata using Marquez. We encourage you to familiarize yourself with the [data model](https://marquezproject.github.io/marquez/#data-model) and [APIs](./openapi.html) of Marquez.

> **Note:** The example shows how to record metadata via direct HTTP API calls using `curl`. But, you can also get started using our client library for [Java](https://github.com/MarquezProject/marquez-java) or [Python](https://github.com/MarquezProject/marquez-python).

#### STEP 1: CREATE A NAMESPACE

Before we can begin recording metadata, we must first create a _namespace_. A `namespace` enables the contextual grouping of metadata for related jobs and datasets. Note that jobs and datasets are unique within a namespace, but not across namespaces. In this example, we will use the namespace `wedata`:

##### REQUEST

```bash
$ curl -X PUT http://localhost:5000/api/v1/namespaces/wedata \
  -H 'Content-Type: application/json' \
  -d '{
        "ownerName": "analytics",
        "description": "Contains datasets such as room bookings for each office."
      }'
```

##### RESPONSE

```bash
{
  "name": "wedata",
  "createdAt": "2019-06-08T19:11:59.430Z",
  "updatedAt": "2019-06-08T19:11:59.430Z",
  "ownerName": "analytics",
  "description": "Contains datasets such as room bookings for each office."
}
```

> **Note:** Marquez provides a `default` namespace to record metadata, but we encourage you to create your own.

#### STEP 2: CREATE A SOURCE

Each dataset must be associated with a _source_. A `source` is the physical location of a dataset, such as a table in a database, or a file on cloud storage. A source enables the logical grouping and mapping of physical datasets to their physical source.

##### REQUEST

```bash
$ curl -X PUT http://localhost:5000/api/v1/sources/analytics_db \
  -H 'Content-Type: application/json' \
  -d '{
        "type": "POSTGRESQL",
        "connectionUrl": "jdbc:postgresql://localhost:5431/analytics"
      }'  
```

##### RESPONSE

```bash
{
  "type": "POSTGRESQL",
  "name": "analytics_db",
  "createdAt": "2019-06-08T19:13:00.749Z",
  "updatedAt": "2019-06-08T19:13:00.749Z",
  "connectionUrl": "jdbc:postgresql://localhost:5431/analytics",
  "description": null
}
```

#### STEP 3: ADD DATASET TO NAMESPACE

Next, we need to create a dataset and associate it with an existing source:

##### REQUEST

```bash
$ curl -X PUT http://localhost:5000/api/v1/namespaces/wedata/datasets/wedata.room_bookings \
  -H 'Content-Type: application/json' \
  -d '{ 
        "type": "DB_TABLE",
        "physicalName": "wedata.room_bookings",
        "sourceName": "analytics_db",
        "fields": [
          {"name": "booking_id", "type": "INTEGER", "tags": []},
          {"name": "booked_at", "type": "TIMESTAMP", "tags": []},
          {"name": "office_id", "type": "INTEGER", "tags": []},
          {"name": "room_id", "type": "INTEGER", "tags": []}
        ],
        "tags": ["SENSITIVE"],
        "description": "All global room bookings for each office."
      }'
```

##### RESPONSE

```bash
{
  "type": "DB_TABLE",
  "name": "wedata.room_bookings",
  "physicalName": "wedata.room_bookings",
  "createdAt": "2019-06-08T19:13:34.507Z",
  "updatedAt": "2019-06-08T19:13:34.507Z",
  "sourceName": "analytics_db",
  "fields": [
    {"name": "booking_id", "type": "INTEGER", "tags": [], "description": null},
    {"name": "booked_at", "type": "TIMESTAMP", "tags": [], "description": null},
    {"name": "office_id", "type": "INTEGER", "tags": [], "description": null},
    {"name": "room_id", "type": "INTEGER", "tags": [], "description": null}
  ],
  "tags": ["SENSITIVE"],
  "lastModifiedAt": null,
  "description": "All global room bookings for each office."
}
```

#### STEP 4: ADD JOB TO NAMESPACE

##### REQUEST

```bash
$ curl -X PUT http://localhost:5000/api/v1/namespaces/wedata/jobs/room_bookings_7_days \
  -H 'Content-Type: application/json' \
  -d '{
        "type": "BATCH",
        "inputs": ["wedata.room_bookings"],
        "outputs": [],
        "location": "https://github.com/wework/jobs/commit/124f6089ad4c5fcbb1d7b33cbb5d3a9521c5d32c",
        "description": "Weekly email of room bookings occupancy patterns."
      }'
```

##### RESPONSE

```bash
{
  "type": "BATCH",
  "name": "room_bookings_7_days",
  "createdAt": "2019-06-08T19:13:58.434Z",
  "updatedAt": "2019-06-08T19:13:58.434Z",
  "inputs": ["wedata.room_bookings"],
  "outputs": [],
  "location": "https://github.com/wework/jobs/commit/124f6089ad4c5fcbb1d7b33cbb5d3a9521c5d32c",
  "description": "Weekly email of room bookings occupancy patterns.",
  "latestRun": null
}
```

#### STEP 5: CREATE A RUN

##### REQUEST

```bash
$ curl -X POST http://localhost:5000/api/v1/namespaces/wedata/jobs/room_bookings_7_days/runs \
  -H 'Content-Type: application/json' \
  -d '{
        "runArgs": {
          "email": "data@wework.com",
          "emailOnFailure": false,
          "emailOnRetry": true,
          "retries": 1
        }
      }'
```

##### RESPONSE

```bash
{
  "runId": "099a7574-e518-4e05-877a-6f2749e92791",
  "createdAt": "2019-06-08T19:14:30.679Z",
  "updatedAt": "2019-06-08T19:14:30.694Z",
  "nominalStartTime": null,
  "nominalEndTime": null,
  "runState": "NEW",
  "runArgs": {
    "email": "data@wework.com",
    "emailOnFailure": "false",
    "emailOnRetry": "true",
    "retries": "1"
  }
}
```

#### STEP 6: RECORD A RUN

##### REQUEST

```bash
$ curl -X POST http://localhost:5000/api/v1/jobs/runs/099a7574-e518-4e05-877a-6f2749e92791/start
```

##### RESPONSE

```bash
{
  "runId": "099a7574-e518-4e05-877a-6f2749e92791",
  "createdAt": "2019-06-08T19:14:30.679Z",
  "updatedAt": "2019-06-08T19:24:23.443Z",
  "nominalStartTime": null,
  "nominalEndTime": null,
  "runState": "RUNNING",
  "runArgs": {
    "email": "data@wework.com",
    "emailOnFailure": "false",
    "emailOnRetry": "true",
    "retries": "1"
  }
}
```

#### STEP 7: RECORD A COMPLETE RUN

##### REQUEST

```bash
$ curl -X POST http://localhost:5000/api/v1/jobs/runs/099a7574-e518-4e05-877a-6f2749e92791/complete
```

##### RESPONSE

```bash
{
  "runId": "099a7574-e518-4e05-877a-6f2749e92791",
  "createdAt": "2019-06-08T19:14:30.679Z",
  "updatedAt": "2019-06-08T19:26:31.492Z",
  "nominalStartTime": null,
  "nominalEndTime": null,
  "runState": "COMPLETED",
  "runArgs": {
    "email": "data@wework.com",
    "emailOnFailure": "false",
    "emailOnRetry": "true",
    "retries": "1"
  }
}
```
