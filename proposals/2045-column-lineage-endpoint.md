# Proposal: Column lineage endpoint proposal

Author(s): @julienledem

Created: 20022-08-18

Dicussion: [column lineage endpoint issue #2045](https://github.com/MarquezProject/marquez/issues/2045)

## Overview

OpenLineage defines a [column-level lineage facet](https://github.com/OpenLineage/OpenLineage/blob/ff0d87d30ed6c9fe39472788948266a6d3190585/spec/facets/ColumnLineageDatasetFacet.md). 
We propose to add a Marquez endpoint leveraging this facet to filter down lineage for given column.

## Proposal

### add column lineage to existing endpoint
In the GET /lineage api, add column lineage to DATASET nodes' data
```
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
			"name": "public.categories",
			"physicalName": "public.categories",
			"createdAt": "2021-03-09T02:33:18.468719Z",
			"updatedAt": "2022-08-04T05:08:09.190723Z",
			"namespace": "food_delivery",
			"sourceName": "analytics_db",
			"fields": [{
				"name": "id",
				"type": "INTEGER",
				"tags": [],
				"description": "The unique ID of the category."
			}, {
				"name": "name",
				"type": "VARCHAR",
				"tags": [],
				"description": "The name of the category."
			}, {
				"name": "menu_id",
				"type": "INTEGER",
				"tags": [],
				"description": "The ID of the menu related to the category."
			}, {
				"name": "description",
				"type": "TEXT",
				"tags": [],
				"description": "The description of the category."
			}],
>     columnLineage: {
>       "a": {
>         inputFields: [
>            {namespace: "ns", name: "name", "field": "a"},
>            ... other inputs
>         ],
>         transformationDescription: "identical",
>         transformationType: "IDENTITY"
>       },
>       "b": ... other output fields
>     }
			"tags": [],
			"lastModifiedAt": "2022-08-04T05:03:09.190723Z",
			"description": null,
			"lastlifecycleState": null
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
that would be layered on the existing lineage endpoint but filtered down to the datasets that contribute to that column.
It also only returns dataset nodes

```
{
  graph: [
    {
       "id": "dataset:db1:table2",
       "type": "DATASET",
       data: {
         namespace: "DB1",
         name: "table2",
         columnLineage: {
            "a": {
               inputFields: [
                 {namespace: "DB1", name: "table1, "field": "a"}
               ],
               transformationDescription: "identical",
               transformationType: "IDENTITY"
            },
           "b": ... other output fields
         }
      },
      ...
  }
  ]
```

### Point in time upstream lineage
return historical upstream lineage from a given Dataset version. 
```
GET /lineage?nodeId=dataset:food_delivery:public.delivery_7_days:{version}
GET /column-lineage?nodeId=dataset:food_delivery:public.delivery_7_days:{version}&column=a
```
This returns only upstream lineage in this current proposal.
The upstream lineage is well defined to a specific version while downstream lineage is not
The data payload would also add a version field.

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
