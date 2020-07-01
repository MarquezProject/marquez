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

In this example, we show how you can collect dataset and job metadata using Marquez. We encourage you to familiarize yourself with the [data model](https://marquezproject.github.io/marquez/#data-model) and [APIs](./openapi.html) of Marquez.

> **Note:** The example shows how to collect metadata via direct HTTP API calls using `curl`. But, you can also get started using our client library for [Java](https://github.com/MarquezProject/marquez-java) or [Python](https://github.com/MarquezProject/marquez-python).

#### STEP 1: CREATE A NAMESPACE

Before we can begin collecting metadata, we must first create a _namespace_. A `namespace` helps you organize related dataset and job metadata. Note that datasets and jobs are unique within a namespace, but not across namespaces. For example, the job `my-job` may exist in the namespace `this-namespace` and `other-namespace`, but not both. In this example, we'll use the namespace `my-namespace`:

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
  "createdAt": "2020-06-30T20:29:53.521534Z",
  "updatedAt": "2020-06-30T20:29:53.525528Z",
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
  "createdAt": "2020-06-30T20:30:56.535357Z",
  "updatedAt": "2020-06-30T20:30:56.535357Z",
  "connectionUrl": "jdbc:postgresql://localhost:5431/mydb",
  "description": "My first source."
}
```

#### STEP 3: ADD DATASET TO NAMESPACE

Next, we need to create the dataset `my-dataset` and associate it with the existing source `my-source`. In Marquez, datasets have both a _logical_ and _physical_ name. The logical name is how your dataset is known to Marquez, while the physical name how your dataset is known to your source. In this example, we refer to `my-dataset` as the logical name and `public.mytable` (=`schema.table`) as the physical name:

##### REQUEST

```bash
$ curl -X PUT http://localhost:5000/api/v1/namespaces/my-namespace/datasets/my-dataset \
  -H 'Content-Type: application/json' \
  -d '{ 
        "type": "DB_TABLE",
        "physicalName": "public.mytable",
        "sourceName": "my-source",
        "fields": [
          {"name": "a", "type": "INTEGER"},
          {"name": "b", "type": "TIMESTAMP"},
          {"name": "c", "type": "INTEGER"},
          {"name": "d", "type": "INTEGER"}
        ],
        "description": "My first dataset."
      }'
```

##### RESPONSE

```bash
{
  "id": {
    "namespaceName": "my-namespace",
    "name": "my-dataset"
  },
  "type": "DB_TABLE",
  "name": "my-dataset",
  "physicalName": "public.mytable",
  "createdAt": "2020-06-30T20:31:39.129483Z",
  "updatedAt": "2020-06-30T20:31:39.259853Z",
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

With metadata for `my-dataset` in Marquez, let's add the job `my-job`:

##### REQUEST

```bash
$ curl -X PUT http://localhost:5000/api/v1/namespaces/my-namespace/jobs/my-job \
  -H 'Content-Type: application/json' \
  -d '{
        "type": "BATCH",
        "inputs": [{
          "namespaceName": "my-namespace", 
          "name": "my-dataset"
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
  "name": "my-job",
  "createdAt": "2020-06-30T20:32:55.570981Z",
  "updatedAt": "2020-06-30T20:32:55.658594Z",
  "inputs": [{
      "namespaceName": "my-namespace",
      "name": "my-dataset"
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
          "email": "me@example.com",
          "emailOnFailure": false,
          "emailOnRetry": true,
          "retries": 1
        }
      }'
```

##### RESPONSE

```bash
{
  "id": "d46e465b-d358-4d32-83d4-df660ff614dd",
  "createdAt": "2020-06-30T20:34:40.146354Z",
  "updatedAt": "2020-06-30T20:34:40.165768Z",
  "nominalStartTime": null,
  "nominalEndTime": null,
  "state": "NEW",
  "startedAt": null,
  "endedAt": null,
  "durationMs": null,
  "args": {
    "email": "me@example.com",
    "emailOnFailure": "false",
    "emailOnRetry": "true",
    "retries": "1"
  }
}
```

#### STEP 6: START A RUN

Use `d46e465b-d358-4d32-83d4-df660ff614dd` to _start_ the run for `my-job`:

##### REQUEST

```bash
$ curl -X POST http://localhost:5000/api/v1/jobs/runs/d46e465b-d358-4d32-83d4-df660ff614dd/start
```

##### RESPONSE

```bash
{
  "id": "d46e465b-d358-4d32-83d4-df660ff614dd",
  "createdAt": "2020-06-30T20:34:40.146354Z",
  "updatedAt": "2020-06-30T20:37:43.746677Z",
  "nominalStartTime": null,
  "nominalEndTime": null,
  "state": "RUNNING",
  "startedAt": "2020-06-30T20:37:43.746677Z",
  "endedAt": null,
  "durationMs": null,
  "args": {
    "email": "me@example.com",
    "emailOnFailure": "false",
    "emailOnRetry": "true",
    "retries": "1"
  }
}
```

#### STEP 7: COMPLETE A RUN

Use `d46e465b-d358-4d32-83d4-df660ff614dd` to _complete_ the run for `my-job`:

##### REQUEST

```bash
$ curl -X POST http://localhost:5000/api/v1/jobs/runs/d46e465b-d358-4d32-83d4-df660ff614dd/complete
```

##### RESPONSE

```bash
{
  "id": "d46e465b-d358-4d32-83d4-df660ff614dd",
  "createdAt": "2020-06-30T20:34:40.146354Z",
  "updatedAt": "2020-06-30T20:38:25.657449Z",
  "nominalStartTime": null,
  "nominalEndTime": null,
  "state": "COMPLETED",
  "startedAt": "2020-06-30T20:37:43.746677Z",
  "endedAt": "2020-06-30T20:38:25.657449Z",
  "durationMs": 41911,
  "args": {
    "email": "me@example.com",
    "emailOnFailure": "false",
    "emailOnRetry": "true",
    "retries": "1"
  }
}
```