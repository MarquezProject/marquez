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

#### RUNNING WITH [DOCKER](https://github.com/MarquezProject/marquez/blob/main/Dockerfile)

The easiest way to get up and running is with Docker. From the base of the Marquez repository run:

```
$ docker-compose up
```

> **Tip:** Use the `--build` flag to build images from source, or `--pull` to pull a tagged image.

Marquez listens on port `5000` for all API calls and port `5001` for the admin interface. To verify the HTTP API server is running and listening on `localhost` browse to [http://localhost:5001](http://localhost:5001).

> **Note:** By default, the HTTP API does not require any form of authentication or authorization.

## Example

In this example, we show how you can collect dataset and job metadata using Marquez. We also encourage you to familiarize yourself with the [data model](https://marquezproject.github.io/marquez/#data-model) and [APIs](./openapi.html) of Marquez.

> **Note:** The example shows how to collect metadata via direct HTTP API calls using `curl`. But, you can also get started using our client library for [Java](https://github.com/MarquezProject/marquez-java) or [Python](https://github.com/MarquezProject/marquez-python).

#### STEP 1: CREATE A NAMESPACE

Before we can begin collecting metadata, we must first create a _namespace_. A `namespace` helps you organize related dataset and job metadata. Note that datasets and jobs are unique within a namespace, but not across namespaces. In this example, we will use the namespace `my-namespace`:

##### REQUEST

```bash
$ curl -X PUT http://localhost:5000/api/v1/namespaces/my-namespace \
  -H 'Content-Type: application/json' \
  -d '{
        "ownerName": "me",
        "description": "My first namespace."
      }'
```

##### RESPONSE

```bash
{
  "name": "my-namespace",
  "createdAt": "2019-06-08T19:11:59.430Z",
  "updatedAt": "2019-06-08T19:11:59.430Z",
  "ownerName": "me",
  "description": "My first namespace."
}
```

> **Note:** Marquez provides a `default` namespace to collect metadata, but we encourage you to create your own.

#### STEP 2: CREATE A SOURCE

Each dataset must be associated with a _source_. A `source` is the physical location of a dataset, such as a table in a database, or a file on cloud storage. A source enables the logical grouping and mapping of physical datasets to their physical source. Below, let's create the source `my-source` for the database `mydb`:

##### REQUEST

```bash
$ curl -X PUT http://localhost:5000/api/v1/sources/my-source \
  -H 'Content-Type: application/json' \
  -d '{
        "type": "POSTGRESQL",
        "connectionUrl": "jdbc:postgresql://localhost:5431/mydb",
        "description": "My first source."
      }'  
```

##### RESPONSE

```bash
{
  "type": "POSTGRESQL",
  "name": "my-source",
  "createdAt": "2019-06-08T19:13:00.749Z",
  "updatedAt": "2019-06-08T19:13:00.749Z",
  "connectionUrl": "jdbc:postgresql://localhost:5431/mydb",
  "description": "My first source."
}
```

#### STEP 3: ADD DATASET TO NAMESPACE

Next, we need to create the dataset `mydb.users` and associate it with the existing source `my-source`:

##### REQUEST

```bash
$ curl -X PUT http://localhost:5000/api/v1/namespaces/my-namespace/datasets/mydb.foo \
  -H 'Content-Type: application/json' \
  -d '{ 
        "type": "DB_TABLE",
        "physicalName": "mydb.foo",
        "sourceName": "my-source",
        "fields": [
          {"name": "a", "type": "INTEGER", "tags": []},
          {"name": "b", "type": "TIMESTAMP", "tags": []},
          {"name": "c", "type": "INTEGER", "tags": []},
          {"name": "d", "type": "INTEGER", "tags": []}
        ],
        "tags": [],
        "description": "My first dataset."
      }'
```

##### RESPONSE

```bash
{
  "id": {
    "namespaceName": "my-namespace",
    "name": "mydb.foo"
  },
  "type": "DB_TABLE",
  "name": "mydb.foo",
  "physicalName": "mydb.foo",
  "createdAt": "2019-06-08T19:13:34.507Z",
  "updatedAt": "2019-06-08T19:13:34.507Z",
  "sourceName": "my-source",
  "fields": [
    {"name": "a", "type": "INTEGER", "tags": [], "description": null},
    {"name": "b", "type": "TIMESTAMP", "tags": [], "description": null},
    {"name": "c", "type": "INTEGER", "tags": [], "description": null},
    {"name": "d", "type": "INTEGER", "tags": [], "description": null}
  ],
  "tags": [],
  "lastModifiedAt": null,
  "description": "My first dataset."
}
```

#### STEP 4: ADD JOB TO NAMESPACE

##### REQUEST

```bash
$ curl -X PUT http://localhost:5000/api/v1/namespaces/my-namespace/jobs/my-job \
  -H 'Content-Type: application/json' \
  -d '{
        "type": "BATCH",
        "inputs": [{
          "namespaceName": "my-namespace", 
          "name": "mydb.foo"
        }],
        "outputs": [],
        "location": "https://github.com/my-jobs/blob/124f6089ad4c5fcbb1d7b33cbb5d3a9521c5d32c",
        "description": "My first job!"
      }'
```

##### RESPONSE

```bash
{
  "id": {
    "namespaceName": "my-namespace",
    "name": "my-job"
  },
  "type": "BATCH",
  "name": "room_bookings_7_days",
  "createdAt": "2019-06-08T19:13:58.434Z",
  "updatedAt": "2019-06-08T19:13:58.434Z",
  "inputs": [{
      "namespaceName": "my-namespace",
      "name": "mydb.foo"
  }],
  "outputs": [],
  "location": "https://github.com/my-jobs/blob/124f6089ad4c5fcbb1d7b33cbb5d3a9521c5d32c",
  "context": {},
  "description": "My first job!",
  "latestRun": null
}
```

#### STEP 5: CREATE A RUN

##### REQUEST

```bash
$ curl -X POST http://localhost:5000/api/v1/namespaces/my-namespace/jobs/my-job/runs \
  -H 'Content-Type: application/json' \
  -d '{
        "args": {
          "email": "data@example.com",
          "emailOnFailure": false,
          "emailOnRetry": true,
          "retries": 1
        }
      }'
```

##### RESPONSE

```bash
{
  "id": "e994be10-3833-40e7-83c3-ce778e5f4993",
  "createdAt": "2019-06-08T19:14:30.679Z",
  "updatedAt": "2019-06-08T19:14:30.694Z",
  "nominalStartTime": null,
  "nominalEndTime": null,
  "state": "NEW",
  "startedAt": null,
  "endedAt": null,
  "durationMs": null,
  "args": {
    "email": "data@example.com",
    "emailOnFailure": "false",
    "emailOnRetry": "true",
    "retries": "1"
  }
}
```

#### STEP 6: RECORD A RUN

##### REQUEST

```bash
$ curl -X POST http://localhost:5000/api/v1/jobs/runs/e994be10-3833-40e7-83c3-ce778e5f4993/start
```

##### RESPONSE

```bash
{
  "id": "e994be10-3833-40e7-83c3-ce778e5f4993",
  "createdAt": "2019-06-08T19:14:30.679Z",
  "updatedAt": "2019-06-08T19:24:23.443Z",
  "nominalStartTime": null,
  "nominalEndTime": null,
  "state": "RUNNING",
  "startedAt": "2019-06-08T20:06:45.313Z",
  "endedAt": null,
  "durationMs": null,
  "args": {
    "email": "data@example.com",
    "emailOnFailure": "false",
    "emailOnRetry": "true",
    "retries": "1"
  }
}
```

#### STEP 7: RECORD A COMPLETE RUN

##### REQUEST

```bash
$ curl -X POST http://localhost:5000/api/v1/jobs/runs/e994be10-3833-40e7-83c3-ce778e5f4993/complete
```

##### RESPONSE

```bash
{
  "id": "e994be10-3833-40e7-83c3-ce778e5f4993",
  "createdAt": "2019-06-08T19:14:30.679Z",
  "updatedAt": "2019-06-08T19:26:31.492Z",
  "nominalStartTime": null,
  "nominalEndTime": null,
  "state": "COMPLETED",
  "startedAt": "2019-06-08T20:06:45.313Z",
  "endedAt": "2019-06-08T20:14:40.489Z",
  "durationMs": 475176,
  "args": {
    "email": "data@wework.com",
    "emailOnFailure": "false",
    "emailOnRetry": "true",
    "retries": "1"
  }
}
```
