# Proposal: Optimize query performance for OpenLineage facets

Author: Willy Lulciuc ([@wslulciuc](https://github.com/wslulciuc))

Created: 2022-08-18

Discussion: [https://github.com/MarquezProject/marquez/issues/2078](https://github.com/MarquezProject/marquez/issues/2078)

## Overview

[OpenLineage](https://openlineage.io) was initially prototyped using Marquez, with the [initial draft](https://github.com/OpenLineage/OpenLineage/blob/main/CHANGELOG.md#010---2021-08-12) of the spec taking inspiration from Marquez's [data model](https://lucid.app/lucidchart/f918ce01-9eb4-4900-b266-49935da271b8/view?page=8xAE.zxyknLQ#). OpenLineage events are collected via [`POST` `/lineage`](https://marquezproject.github.io/marquez/openapi.html#tag/Lineage) calls, and can be queried via the `lineage_events` table using the `runID` associated with the event. The _current_ schema for the `lineage_events` table is defined below:

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

We knew the initial implementation would have to eventually be revisited. That is, OpenLineage events can easily exceed **>** **`10MBs`** resulting in out-of-memory (OOM) errors as facet queries require loading the raw `event` in memory, then filtering for relevant facets. This proposal outlines how we can optimize query performance for OpenLineage facets.



## Proposal

To improve query performance for facets, and avoid querying the `lineage_events` table, we propose adding the following tables to group facets by how they will be accessed:

### Table `dataset_version_facets`

| **COLUMN**           | **TYPE**  |
|----------------------|-----------|
| dataset_version_uuid | `UUID`    |
| name                 | `VARCHAR` |
| facet                | `JSONB`   |

> **Table 1:** Facets for a given dataset version.

### Table `job_version_facets`

| **COLUMN**       | **TYPE**  |
|------------------|-----------|
| job_version_uuid | `UUID`    |
| name             | `VARCHAR` |
| facet            | `JSONB`   |

> **Table 2:** Facets for a given job version.

### Table `run_facets`

| **COLUMN** | **TYPE**  |
|------------|-----------|
| run_uuid   | `UUID`    |
| name       | `VARCHAR` |
| facet      | `JSONB`   |

> **Table 3:** Facets for a given run.

## Implementation

The implementation requires:

1. Schema changes to create the facet tables outlined above.
2. Using the facet tables instead the `lineage_events` table to query for facets.
3. Lazy migration, the facet tables will be queried, and if no facets are returned, then the `lineage_events` table; this approach avoids a backfill, but one will still be needed.
