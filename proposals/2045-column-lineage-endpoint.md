# Proposal: Column lineage endpoint proposal

Author(s): @julienledem

Created: 20022-08-18

Dicussion: [column lineage endpoint issue #2045](https://github.com/MarquezProject/marquez/issues/2045)

## Overview

### Use cases
 - Find the current upstream dependencies of a column. A column in a dataset is derived from columns in upstream datasets.
 - See column-level lineage in the dataset level lineage when available.
 - Retrieve point-in-time upstream lineage for a dataset or a column. What did the lineage look like yesterday compared to today?

### Existing elements

- OpenLineage defines a [column-level lineage facet]- (https://github.com/OpenLineage/OpenLineage/blob/ff0d87d30ed6c9fe39472788948266a6d3190585/spec/facets/ColumnLineageDatasetFacet.md). 
- Marquez has a lineage endpoint `GET /api/v1/lineage` that returns the current lineage graph connected to a job or a dataset

### New Elements
We propose to add the following:
- Add column lineage to the lineage endpoint
- A new column-lineage endpoint leveraging the column lineage facet to retrieve lineage for a given column.
- Point-in-time upstream (dataset or column level) lineage given a version of a dataset.

## Proposal

### add column lineage to existing endpoint
In the GET /lineage api, add column lineage to DATASET nodes' data
```diff
{
  "id": "dataset:food_delivery:public.categories",
  "type": "DATASET",
  "data": {
    "type": "DATASET",
    "id": {
      "namespace": "food_delivery",
      "name": "public.categories"
    },
    "type": "DB_TABLE",
    ...
    "fields": [{
		...
    }],
>   columnLineage: {
>     "a": {
>       inputFields: [
>          {namespace: "ns", name: "name", "field": "a"},
>          ... other inputs
>       ],
>       transformationDescription: "identical",
>       transformationType: "IDENTITY"
>     },
>     "b": ... other output fields
>   }
  },
  "inEdges": [{
    "origin": "job:food_delivery:etl_orders.etl_categories",
    "destination": "dataset:food_delivery:public.categories"
  }],
  "outEdges": [{
    "origin": "dataset:food_delivery:public.categories",
    "destination": "job:food_delivery:etl_orders.etl_orders_7_days"
  }]
}
```

### add a column-level-lineage endpoint:

```
GET /column-lineage?nodeId=dataset:food_delivery:public.delivery_7_days&column=a
```
`column` is a ne parameter that must be a column in the schema of the provided dataset `nodeId`.

The logic is layered on the existing lineage endpoint, filtering down to the datasets that contribute to that column.
It only returns dataset nodes.

```diff
{
  graph: [
    {
       "id": "dataset:db1:table2",
       "type": "DATASET",
       data: {
         namespace: "DB1",
         name: "table2",
>       columnLineage: {
>           "a": {
>              inputFields: [
>                {namespace: "DB1", name: "table1, "field": "a"}
>              ],
>              transformationDescription: "identical",
>              transformationType: "IDENTITY"
>           },
>          "b": ... other output fields
>        }
      },
      ...
  }
  ]
}  
```

### Point in time upstream lineage
return historical upstream lineage from a given Dataset version. 
This adds the version element to the nodeId in both the existing `/api/v1/lineage` and newly proposed `/api/v1/column-lineage` endpoint 
```
GET /lineage?nodeId=dataset:food_delivery:public.delivery_7_days:{version}
GET /column-lineage?nodeId=dataset:food_delivery:public.delivery_7_days:{version}&column=a
```
This returns only upstream lineage in this current proposal. This is because upstream lineage is well defined to a specific version while downstream lineage is not. The data payload would add a version field.
```diff
{
  graph: [
    {
<      "id": "dataset:db1:table2",
>      "id": "dataset:db1:table2#{VERSION UUID}",
       "type": "DATASET",
       data: {
         namespace: "DB1",
         name: "table2",
>        version: "{VERSION UUID}"
         ...
       }
    }
  ]
}  
```

## Implementation

### columne lineage facet in lineage
Adding the columnLineage facet requires a formatting of existing facet data. 
### column lineage endpoint
The `/column-lineage` endpoint leverages the `/lineage` endpoint and then filters down the payload to return the expected result.
### point-in-time upstream lineage
The point-in-time upstream lineage leverages the run to dataset version relation to track back the lineage of a given dataset of job version.
Dataset version -> run that produced it -> consumed Dataset Versions.

## Next Steps

Review of this proposal and production of detailed design for the implementation, in particular for the point in time lineage which might affect the dabtabase schema.
