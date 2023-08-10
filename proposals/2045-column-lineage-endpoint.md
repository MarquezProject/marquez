# Proposal: Column lineage endpoint proposal

Author(s): @julienledem, @pawel-big-lebowski

Created: 20022-08-18

Dicussion: [column lineage endpoint issue #2045](https://github.com/MarquezProject/marquez/issues/2045)

## Overview

### Use cases
 - Find the current upstream dependencies of a column. A column in a dataset is derived from columns in upstream datasets.
 - See column-level lineage in the dataset level lineage when available.
 - Retrieve point-in-time upstream lineage for a dataset or a column. What did the lineage look like yesterday compared to today?

### Existing elements

- OpenLineage defines a [column-level lineage facet]- (https://github.com/OpenLineage/OpenLineage/blob/main/spec/facets/ColumnLineageDatasetFacet.json).
- Marquez has a lineage endpoint `GET /api/v1/lineage` that returns the current lineage graph connected to a job or a dataset

### Column lineage characteristics and general assumptions

Column level lineage is a different lineage graph due to a different node granularity - kind of zoomed-in view of existing lineage. Instead of datasets and jobs being lineage graph nodes, each dataset field becomes a node. Additionally, there are edges between dataset fields, instead of datasets itself. Thus, enriching existing lineage with column lineage information would not be sufficient. That’s why we propose another API endpoint with column lineage graph.

Upstream and downstream edges do have different characteristics. An output dataset is always produced by a single version of input dataset (one upstream), while a single input datset version can have multiple output dataset versions. Lineage graph can be then easily flooded by downstream subgraph which blurs the overall view. That's why we consider an upstream column lineage as a default one. Downstream lineage will be returned only when requested explicitly.

### New Elements

We propose the following changes:

 - Add column lineage to the dataset resource endpoint. Column lineage will NOT be added to existing `/lineage` endpoint as it may be a heavy database operation run on each lineage graph's node which we want to avoid. Based on that, column level lineage get be requested per dataset in separate requests when required.
 - A new column-lineage endpoint leveraging the column lineage facet to retrieve lineage for a given column.
 - Point-in-time upstream (dataset or column level) lineage given a version of a dataset.

## Proposal

### Add column lineage to existing datasets endpoint

In the `GET /api/v1/namespaces/{namespace}/datasets` api, add column lineage facet to returned dataset resource.

### Add a column-level-lineage endpoint:

New endpoints to retrieve a column lineage of a single field or a whole dataset will be added:
```
GET /column-lineage?nodeId=dataset:{namespace}:{dataset}
GET /column-lineage?nodeId=datasetField:{namespace}:{dataset}:{field}
```
For example:
```
GET /column-lineage?nodeId=dataset:food_delivery:public.delivery_7_days
GET /column-lineage?nodeId=datasetField:food_delivery:public.delivery_7_days:a
```

Although creating a new endpoint, we would like to reuse existing data structures with a new `NodeType.FIELD` introduced.

The logic returns dataset field node:

```
GET /column-lineage?nodeId=datasetField:db1:table1:a
...
{
  graph: [
    {
       "id": "datasetField:db1:table1:a",
       "type": "DATASET_FIELD",
       "data": {
         "namespace": "DB1",
         "name": "table2",
         "field": "a",
         "type": "integer",
         "transformationDescription": "identical",
         "transformationType": "IDENTITY",
         "inputFields": [
            { "namespace": "DBA", "name": "tableA", "field": "columnA"},
            { "namespace": "DBB", "name": "tableB", "field": "columnB"},
            { "namespace": "DBC", "name": "tableC", "field": "columnC"}
         ]
         "inEdges": [
          {
             "origin": "datasetField:db1:table1:a",
             "destination": "datasetField:DBA:tableA:columnA"
          },
          {
             "origin": "datasetField:db1:table1:a",
             "destination": "datasetField:DBB:tableB:columnB"
          },
          {
             "origin": "datasetField:db1:table1:a",
             "destination": "datasetField:DBB:tableB:columnC"
          }
         ],
      },
      ...
      # Input fields, present within "inEdges", can be also returned within a graph due to a `depth` parameter greate than 0.
  }
  ]
}
```

The `depth` parameter controls how many edges, from a given dataset field, shall be returned. The default is set to `0`. In case of default equal `1`, each `inputField` will be returned as a separate node within a response graph with `inputFields` used to produce it. Please note that extending depth may increase the graph size and affect request performance.

The endpoints above fetches upstream column-lineage for given dataset field or all fields within a dataset. Downstream column lineage is turned off by default. However, this can be turned on with an extra `withDownstream` parameter like:

```
GET /column-lineage?nodeId=datasetField:food_delivery:public.delivery_7_days:a&withDownstream=true

```
This will include `outEdges` within the returned node of the graph.


### Point in time upstream lineage

Point in time lineage for newly proposed `/api/v1/column-lineage` endpoint:
```
GET /column-lineage?nodeId=dataset_field:food_delivery:public.delivery_7_days:a&datasetVersion=123e4567-e89b-12d3-a456-426614174000
GET /column-lineage?nodeId=dataset_field:food_delivery:public.delivery_7_days:a&lineageAt=1661846242
```

Point in time can be controlled by:
 * **datasetVersion** - uuid of a specific dataset version,
 * **lineageAt** - which contains a unix timestamp.

When **lineageAt** specified, the latest dataset version before timestamp will be found. Regardles **datasetVersion** or **lineageAt** parameters applied, responses will be the same as below:

```diff
{
  graph: [
    {
<       "id": "datasetField:db1:table1:a",
>       "id": "datasetField:db1:table1:a#{VERSION UUID}",
       "type": "DATASET_FIELD",
       "data": {
        ....
}
```

## Implementation

### columne lineage facet in dataset resource endpoint
Adding the columnLineage facet requires a formatting of existing facet data (work in progress).
### column lineage endpoint
The `/column-lineage` endpoint leverages the `/lineage` endpoint and then filters down the payload to return the expected result.
### point-in-time upstream lineage

The point-in-time upstream lineage leverages the run to dataset version relation to track back the lineage of a given dataset of job version.
Dataset version -> run that produced it -> consumed Dataset Versions.

## Next Steps

Review of this proposal and production of detailed design for the implementation.:

----
SPDX-License-Identifier: Apache-2.0
Copyright 2018-2023 contributors to the Marquez project.
