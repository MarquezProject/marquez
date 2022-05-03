// SPDX-License-Identifier: Apache-2.0

import { JobOrDataset, LineageNode } from '../components/lineage/types'

export interface Tag {
  name: string
  description: string
}
export interface Runs {
  runs: Run[]
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
}

export interface Datasets {
  datasets: Dataset[]
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
}

export interface DatasetVersions {
  versions: DatasetVersion[]
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
  context: {
    [key: string]: string
  }
  description: string
  latestRun: Run
}

export interface JobId {
  namespace: string
  name: string
}

export type JobType = 'BATCH' | 'STREAM' | 'SERVICE'

export interface Runs {
  runs: Run[]
}

export interface Run {
  id: string
  context: {
    sql?: string
  }
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

export type RunState = 'NEW' | 'COMPLETED' | 'FAILED' | 'ABORTED'

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
