# Proposal: Run-level Lineage in Marquez API


Created: 2022-11-22

## Problem

Marquez data model in PostgresSQL allows extracting a snapshot of any lineage content from the past. This may be extremely
useful to track how job, dataset etc. evolved over time. We do already support job and dataset versioning that return requested versions. We want to extend this feature to support `lineage` and `column-lineage` versioning which we call Run-Level Lineage. The purpose of this doc is to agree on API implementation approach.

## Solution

Point in time can be described by a datetime, a dataset version, a job version or  run identifier. Versions are the only reliable parameter as datetime is ambiguous. For example: having different START and COMPLETE datetimes for each run event. We assume the ability to scan over time can be implemented within UI.

We will extend defintion of `nodeId` to contain an optional version of the node:

| **ID**       | **`dataset:{namespace}:{dataset}@{version}`**                     |
|:-------------|:------------------------------------------------------------------|
| **Example**  | **`dataset:food_delivery:public.top_delivery_times@947c0388..`**  |

| **ID**       | **`job:{namespace}:{job}@{version}`**                             |
|:-------------|:------------------------------------------------------------------|
| **Example**  | **`job:food_delivery:orders_popular_day_of_week@947c0388..`**     |

| **ID**       | **`run:{id}`**                                                    |
|:-------------|:------------------------------------------------------------------|
| **Example**  | **`run:a03422cf..`**

Run-level lineage will be implemented for the two lineage endpoints:
 * `/api/v1/namespaces/some-namespace/lineage?nodeId=nodeId=dataset:food_delivery:public.delivery_7_days&@5ca3b37e-4e18-11ed-bdc3-0242ac120002`
 * `/api/v1/namespaces/some-namespace/column-lineage?nodeId=nodeId=datasetField:db1:table1:a@5ca3b37e-4e18-11ed-bdc3-0242ac120002`


Existing API endpoint will use extended `nodeId`. In case of `nodeId` having a version specified, returned graph nodes will also have it. If `nodeId` in query parameter is missing version, `nodeId`s of returned graph will NOT contain version so that no change is done to current behaviour of currently available features.

Future steps

 * Implementing Run-Level Lineage may require database model changes in Marquez and this deserves a separate proposal or design document which is out of scope of this doc.
 * Run-Level column lineage can be implemented within issue #2262.

 ----
SPDX-License-Identifier: Apache-2.0
Copyright 2018-2023 contributors to the Marquez project.
