# Proposal: Optimize query performance for OpenLineage facets

Author: Willy Lulciuc ([@wslulciuc](https://github.com/wslulciuc))

Created: 2022-08-18

Discussion: [https://github.com/MarquezProject/marquez/issues/2078](https://github.com/MarquezProject/marquez/issues/2078)

## Overview

[OpenLineage](https://openlineage.io) was initially prototyped using Marquez, with the [initial draft](https://github.com/OpenLineage/OpenLineage/blob/main/CHANGELOG.md#010---2021-08-12) of the spec taking inspiration from Marquez's [data model](https://lucid.app/lucidchart/f918ce01-9eb4-4900-b266-49935da271b8/view?page=8xAE.zxyknLQ#). OpenLineage events are collected via [`POST` `/lineage`](https://marquezproject.github.io/marquez/openapi.html#tag/Lineage) calls, and can be queried via the `lineage_events` table using the `run_uuid` associated with the event. The _current_ schema for the `lineage_events` table is defined below:

### Table `lineage_events`

| **COLUMN**    | **TYPE**     |
|---------------|--------------|
| run_uuid      | `UUID`       |
| event_time    | `TIMESTAMPZ` |
| event_type    | `TEXT`       |
| event         | `JSONB`      |
| job_name      | `TEXT`       |
| job_namespace | `TEXT`       |
| producer      | `TEXT`       |

> **Note:** The table has an index on `run_uuid` and a _multicolumn_ index on `job_name`, `job_namespace`.

The `event` column contains the raw OpenLineage event. When Marquez handles the event, metadata for datasets, jobs, and runs are inserted into their respective tables.

OpenLineage's core model is extensible via _facets_. A `facet` is user-defined metadata and enables entity enrichment. Initially, returning dataset, job, and run facets via the REST API was not supported, but eventually added in release [`0.14.0`](https://github.com/MarquezProject/marquez/compare/0.13.1...0.14.0). The implementation was simple: when querying the `datasets`, `jobs`, or `runs` tables, also query the `lineage_events` table for facets.

We knew the initial implementation would have to be revisited eventually. That is, the `lineage_events` table may have multiple events for a given `run_uuid` that can easily exceed **>** **`10MBs`** per event, resulting in out-of-memory (OOM) errors as facet queries require first loading the raw `event` in memory, then filtering for any relevant facets. For example, the query snippet below is used to query the `datasets` table:

```sql
.
.
LEFT JOIN LATERAL (
  SELECT run_uuid, event_time, event FROM lineage_events
  WHERE run_uuid = dv.run_uuid
) e ON e.run_uuid = dv.run_uuid
.
.
LEFT JOIN (
  SELECT d2.uuid AS dataset_uuid, JSONB_AGG(ds->'facets' ORDER BY event_time ASC) AS facets
  FROM dataset_runs d2,
    jsonb_array_elements(coalesce(d2.event -> 'inputs', '[]'::jsonb) || coalesce(d2.event -> 'outputs', '[]'::jsonb)) AS ds
  WHERE d2.run_uuid = d2.run_uuid
    AND ds -> 'facets' IS NOT NULL
    AND ds ->> 'name' = d2.name
    AND ds ->> 'namespace' = d2.namespace_name
  GROUP BY d2.uuid
) f ON f.dataset_uuid = d.uuid")
```

In the above query, the `inputs` and `outputs` dataset facets for each event are aggregated, then ordered by the event time. This proposal outlines how we can optimize query performance for OpenLineage facets that limit access to the `lineage_events` table.

## Proposal

To improve query performance for facets, and avoid querying the `lineage_events` table, we propose adding the following tables to group facets by how they will be accessed:

### Table `dataset_facets`

| **COLUMN**         | **TYPE**      |
|--------------------|---------------|
| uuid **(PK)**      | `UUID`        |
| created_at         | `TIMESTAMPTZ` |
| run_uuid           | `UUID`        |
| lineage_event_time | `TIMESTAMPTZ` |
| lineage_event_type | `VARCHAR`     |
| type               | `VARCHAR`     |
| name               | `VARCHAR`     |
| facet              | `JSONB`       |

> **Table 1:** Facets for a given dataset.

> **Note:** The `type` used to determine the type of facet: `DATASET`, `INPUT`, `OUTPUT` (see the [_Standard Facets_](https://github.com/OpenLineage/OpenLineage/blob/main/spec/OpenLineage.md#dataset-facets) section in the OpenLineage spec).

### Table `job_facets`

| **COLUMN**         | **TYPE**      |
|--------------------|---------------|
| uuid **(PK)**      | `UUID`        |
| created_at         | `TIMESTAMPTZ` |
| run_uuid           | `UUID`        |
| lineage_event_time | `TIMESTAMPTZ` |
| lineage_event_type | `VARCHAR`     |
| name               | `VARCHAR`     |
| facet              | `JSONB`       |

> **Table 2:** Facets for a given job.

### Table `run_facets`

| **COLUMN**         | **TYPE**      |
|--------------------|---------------|
| uuid **(PK)**      | `UUID`        |
| created_at         | `TIMESTAMPTZ` |
| run_uuid           | `UUID`        |
| lineage_event_time | `TIMESTAMPTZ` |
| lineage_event_type | `VARCHAR`     |
| name               | `VARCHAR`     |
| facet              | `JSONB`       |

> **Table 3:** Facets for a given run.

Note, facet tables will be:

* Append only, mirroring the current insertion pattern of the `lineage_events` table; therefore, avoiding facet conflicts
* Merging facets will follow a _first-to-last_ received order; meaning, facet rows will be merged post query using [`MapperUtils.toFacetsOrNull()`](https://github.com/MarquezProject/marquez/blob/main/api/src/main/java/marquez/db/mappers/MapperUtils.java#L50) mirroring the current logic (i.e. newer facets will be added or override older facet values based on when the OpenLineage event was received)
* Indexed on `run_uuid`

## Implementation

The implementation requires:

1. Schema changes to create the facet tables outlined above.
2. Using the facet tables instead the `lineage_events` table to query for facets.
3. Lazy migration, the facet tables will be queried, and if no facets are returned, then the `lineage_events` table; this approach avoids a backfill, but one will still be needed.
