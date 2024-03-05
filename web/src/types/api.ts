// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import { JobOrDataset, LineageNode } from '../components/lineage/types'

export interface Tag {
  name: string
  description: string
}

export interface Tags {
  tags: Tag[]
}

export interface Namespaces {
  namespaces: Namespace[]
}

export interface Namespace {
  name: string
  createdAt: string
  updatedAt: string
  ownerName: string
  description: string
  isHidden: boolean
}

export interface Events {
  events: Event[]
  totalCount: number
}

export type EventType = 'START' | 'RUNNING' | 'ABORT' | 'FAIL' | 'COMPLETE'

export interface Event {
  eventType: EventType
  eventTime: string
  producer: string
  schemaURL: string
  run: {
    runId: string
    facets: object
  }
  job: {
    name: string
    namespace: string
    facets: object
  }
  inputs: {
    name: string
    namespace: string
    facets: object
  }[]
  outputs: {
    name: string
    namespace: string
    facets: object
  }[]
}

export interface Datasets {
  datasets: Dataset[]
  totalCount: number
}

export interface Dataset {
  id: DatasetId
  type: DatasetType
  name: string
  physicalName: string
  createdAt: string
  updatedAt: string
  namespace: string
  sourceName: string
  fields: Field[]
  tags: string[]
  lastModifiedAt: string
  description: string
  facets: object
  deleted: boolean
  columnLineage: object
}

export interface DatasetVersions {
  versions: DatasetVersion[]
}

export interface DataQualityFacets {
  dataQualityAssertions?: {
    assertions?: {
      assertion: string
      column: string
      success: boolean
    }[]
  }
}

export interface DatasetVersion {
  id: DatasetVersionId
  type: DatasetType
  createdByRun: Run
  name: string
  physicalName: string
  createdAt: string
  version: string
  namespace: string
  sourceName: string
  fields: Field[]
  tags: string[]
  lastModifiedAt: string
  description: string
  lifecycleState: string
  facets: object
}

export interface DatasetId {
  namespace: string
  name: string
}

export interface DatasetVersionId {
  namespace: string
  name: string
  version: string
}

export type DatasetType = 'DB_TABLE' | 'STREAM'

export interface Field {
  name: string
  type: string
  tags: string[]
  description: string
}

export interface Jobs {
  totalCount: number
  jobs: Job[]
}

export interface Job {
  id: JobId
  type: JobType
  name: string
  createdAt: string
  updatedAt: string
  inputs: DatasetId[]
  outputs: DatasetId[]
  namespace: string
  location: string
  description: string
  latestRun: Run
}

export interface JobId {
  namespace: string
  name: string
}

export type JobType = 'BATCH' | 'STREAM' | 'SERVICE'

export interface Runs {
  totalCount: number
  runs: Run[]
}

export interface Run {
  id: string
  createdAt: string
  updatedAt: string
  nominalStartTime: string
  nominalEndTime: string
  state: RunState
  jobVersion: {
    name: string
    namespace: string
    version: string
  }
  startedAt: string
  endedAt: string
  durationMs: number
  args: {
    [key: string]: string
  }
  facets: object
}

export type RunState = 'NEW' | 'RUNNING' | 'COMPLETED' | 'FAILED' | 'ABORTED'

export interface SearchResult {
  name: string
  namespace: string
  nodeId: string
  type: JobOrDataset
  updatedAt: string
}

export type GroupedSearch = { group: string } & SearchResult

export interface LineageGraph {
  graph: LineageNode[]
}

export interface Search {
  totalCount: number
  results: SearchResult[]
}

export interface GroupedSearchResult {
  results: Map<string, GroupedSearch[]>
  rawResults: GroupedSearch[]
}

export interface Facets {
  runId: string
  facets: {
    [key: string]: object
  }
}

export interface ColumnLineageGraph {
  graph: ColumnLineageNode[]
}

export interface ColumnLineageNode {
  id: string
  type: string
  data: ColumnLineageData
  inEdges: ColumnLineageInEdge[]
  outEdges: ColumnLineageOutEdge[]
}

export interface ColumnLineageData {
  namespace: string
  dataset: string
  datasetVersion: string
  field: string
  fieldType: string
  transformationDescription: any
  transformationType: any
  inputFields: ColumnLineage[]
}

export interface ColumnLineage {
  namespace: string
  dataset: string
  datasetVersion: string
  field: string
  transformationDescription: any
  transformationType: any
}

export interface ColumnLineageInEdge {
  origin: string
  destination: string
}

export interface ColumnLineageOutEdge {
  origin: string
  destination: string
}
