// Copyright 2018-2024 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0


import {LineageJob} from "../../types/lineage";

export const lineageJob: LineageJob = {
  "id": {
    "namespace": "food_delivery",
    "name": "etl_orders_7_days"
  },
  "type": "BATCH",
  "name": "etl_orders_7_days",
  "simpleName": "etl_orders_7_days",
  "parentJobName": null,
  "parentJobUuid": null,
  "createdAt": "2020-02-22T22:42:42Z",
  "updatedAt": "2020-02-22T22:44:02Z",
  "namespace": "food_delivery",
  "inputs": [
    {
      "namespace": "food_delivery",
      "name": "public.categories"
    },
    {
      "namespace": "food_delivery",
      "name": "public.menu_items"
    },
    {
      "namespace": "food_delivery",
      "name": "public.orders"
    },
    {
      "namespace": "food_delivery",
      "name": "public.menus"
    }
  ],
  "outputs": [
    {
      "namespace": "food_delivery",
      "name": "public.orders_7_days"
    }
  ],
  "location": null,
  "description": null,
  "latestRun": {
    "id": "ffba2c14-4170-48da-bec3-ab5fd4ec9a3f",
    "createdAt": "2020-02-22T22:42:42Z",
    "updatedAt": "2020-02-22T22:44:02Z",
    "nominalStartTime": null,
    "nominalEndTime": null,
    "state": "COMPLETED",
    "startedAt": "2020-02-22T22:42:42Z",
    "endedAt": "2020-02-22T22:44:02Z",
    "durationMs": 80000,
    "args": {
      "nominal_start_time": "2020-02-22T22:00Z",
      "nominal_end_time": "2020-02-22T22:00Z"
    },
    "jobVersion": {
      "namespace": "food_delivery",
      "name": "etl_orders_7_days",
      "version": "2ced85f2-4274-378d-b9e2-fa4ea6e68c71"
    },
    "facets": {
      "nominalTime": {
        "_producer": "https://github.com/MarquezProject/marquez/blob/main/docker/metadata.json",
        "_schemaURL": "https://openlineage.io/spec/facets/1-0-0/NominalTimeRunFacet.json",
        "nominalEndTime": "2020-02-22T22:00:00Z",
        "nominalStartTime": "2020-02-22T22:00:00Z"
      }
    }
  }
}
