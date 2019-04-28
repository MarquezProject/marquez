---
layout: docs
---

## Quickstart

#### BUILDING

To checkout the Marquez source code:

```
$ git clone git@github.com:MarquezProject/marquez.git && cd marquez
```

Marquez uses [Gradle](https://gradle.org) as the build system. To build the entire project:
```
$ ./gradlew shadowJar
```

The executable can be found under `build/libs/`

#### CONFIGURATION

```
server:
  applicationConnectors:
    - type: http
      port: 8080

db:
  driverClass: org.postgresql.Driver
  url: jdbc:postgresql://localhost:5432/marquez
  user: buendia
  password: macondo
```

#### RUNNING THE [APPLICATION](https://github.com/MarquezProject/marquez/blob/master/src/main/java/marquez/MarquezApp.java)

```
$ ./gradlew run --args 'server config.yml'
```

#### RUNNING WITH [DOCKER](https://github.com/MarquezProject/marquez/blob/master/Dockerfile)

```
$ docker-compose up
```

Marquez listens on TCP port `5000` for all API calls.

## Example

#### CREATE A NAMESPACE

```
$ curl -XPUT http://localhost:5000/api/v1/namespaces/wedata \
  -H 'Content-Type: application/json' \
  -d '{
        "owner": "analytics",
        "description": "Contains datasets such as room bookings for each office."
      }'
```

```sql
> SELECT name, description, current_ownership FROM namespaces;
```
```
    name     |                       description                        | current_ownership
-------------+----------------------------------------------------------+-------------------
 wedata      | Contains datasets such as room bookings for each office. | analytics
```

#### CREATE A DATASOURCE

```
$ curl -XPOST http://localhost:5000/api/v1/datasources \
  -H 'Content-Type: application/json' \
  -d '{
        "name": "analytics_db",
        "connectionUrl": "jdbc:postgresql://localhost:5431/analytics"
      }'
```

```sql
> SELECT name, urn, connection_url FROM datasources;
```
```
     name     |                  urn                   |               connection_url
--------------+----------------------------------------+--------------------------------------------
 analytics_db | urn:datasource:postgresql:analytics_db | jdbc:postgresql://localhost:5431/analytics
```

#### CREATE A DATASET

```
$ curl -XPOST http://localhost:5000/api/v1/namespaces/wedata/datasets \
  -H 'Content-Type: application/json' \
  -d '{ 
        "name": "public.room_bookings",
        "datasourceUrn": "urn:datasource:postgresql:analytics_db",
        "description": "All global room bookings for each office."
      }'
```

```sql
> SELECT name, urn, description FROM datasets;
```
```
         name         |                      urn                      |                description
----------------------+-----------------------------------------------+-------------------------------------------
 public.room_bookings | urn:dataset:analytics_db:public.room_bookings | All global room bookings for each office.
```

#### CREATE A JOB

```
$ curl -XPUT http://localhost:5000/api/v1/namespaces/wedata/jobs/room_bookings_7_days \
  -H 'Content-Type: application/json' \
  -d '{
        "inputDatasetUrns": ["urn:dataset:analytics_db:public.room_bookings"],
        "outputDatasetUrns": ["urn:dataset:analytics_db:public.room_bookings_7_days"],
        "location": "https://github.com/wework/airflow/commit/124f6089ad4c5fcbb1d7b33cbb5d3a9521c5d32c",
        "description": "Determine weekly room booking occupancy patterns."
      }'
```

```sql
> SELECT name, input_dataset_urns, output_dataset_urns, description FROM jobs;
```
```
         name         |               input_dataset_urns                |                  output_dataset_urns                   |                    description
----------------------+-------------------------------------------------+--------------------------------------------------------+---------------------------------------------------
 room_bookings_7_days | {urn:dataset:analytics_db:public.room_bookings} | {urn:dataset:analytics_db:public.room_bookings_7_days} | Determine weekly room booking occupancy patterns.
```

#### CREATE A JOB RUN

```
$ curl -XPOST http://localhost:5000/api/v1/namespaces/wedata/jobs/room_bookings_7_days/runs \
  -H 'Content-Type: application/json' \
  -d '{
        "runArgs": "{}"
      }'
```
```
{
  "runId": "dba53dae-0429-467e-a502-d4c71cd6de79",
  "nominalStartTime": null,
  "nominalEndTime": null,
  "runArgs": "{}",
  "runState": "NEW"
}
```

```sql
>
```

#### RECORD A RUN

```
$ curl -XPUT http://localhost:5000/api/v1/jobs/runs/dba53dae-0429-467e-a502-d4c71cd6de79/run
```

#### RECORD A COMPLETE RUN

```
$ curl -XPUT http://localhost:5000/api/v1/jobs/runs/dba53dae-0429-467e-a502-d4c71cd6de79/complete
```

```sql
>
```
