# V57 MIGRATION

`V57` database migration introduces significant performance improvements as it splits facets from `lineage_events` table
into: `dataset_facets`, `run_facets` and `job_facets` tables. Migration procedure requires moving existing data
to newly created tables which can be a heavy and time-consuming task.

> **_NOTE:_** For Marquez instances with more than 100K `lineage_events`, an extra manual step is required to upgrade.

## <= 100.000 rows in `lineage_events` table

A standard Flyway migration is run which fills newly created tables into `dataset_facets`, `run_facets` and `job_facets`.
No extra work is required but be prepared for a couple of minutes downtime when performing upgrade.

## \> 100.000  rows in `lineage_events` table

For a heavy users, a standard migration does not copy data to newly created tables. The advantage of such an approach
is that an upgrade take just a moment and after that, one can start API to consume new OpenLineage events while
doing the migration asynchronously. Please note that before finishing the migration, some API calls may return
irrelevant results, especially if the output is based on facets.

To schedule a migration, a command has to be run:
```shell
java -jar api/build/libs/marquez-api-0.30.0-SNAPSHOT.jar db-migrate --version v57 ./marquez.yml
```
Command processes data in chunks, each chunk is run in transaction, and the command stores a state containing information of
chunks processed. Based on that:
* It can be stopped any time,
* It continues automatically with chunks remaining.

A default chunk size is `10000` which is a number of `lineage_events` processed in a single query. A chunk size
may be adjusted as command parameter:
```shell
java -jar api/build/libs/marquez-api-0.30.0-SNAPSHOT.jar db-migrate --version v57 --chunkSize 50000 ./marquez.yml
```

## How long can the migration procedure take?

This depends on size of `lineage_events` but also on a characteristics of each event (how big the events are?, how many
facets they include?).

Performance tests have been implemented in `V57_BackfillFacetsPerformanceTest` class and run
in Docker environment (docker resources: 5cpus, 8GB RAM on Mac M1 PRO). During a test
with 100K `lineage_events`, each event had 48KB and migration resulted
in 500K `job_facets`, 500K `run_facets` and 1.5M `dataset_facets`. Migration took *106 seconds*.
Default `chunkSize` was used.

Table below shows migration time for different amount of `lineage_events` generated the same way.

| `lineage_events` | `job_facets` | `run_facets` | `dataset_facets` | time taken |
|------------------|--------------|--------------|------------------|------------|
| 10K events       | 50K rows     | 50K rows     | 150K rows        | 10sec      |
| 100K events      | 500K rows    | 500K rows    | 150K rows        | 106sec     |
| 500K events      | 2.5M rows    | 2.5M rows    | 7.5M rows        | 612sec     |
| 1M events        | 5M rows      | 5M rows      | 15M rows         | 1473sec    |
