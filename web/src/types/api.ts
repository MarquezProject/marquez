// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import { JobOrDataset, LineageNode } from './lineage'
import { Nullable } from './util/Nullable'

export interface Tag {
  name: string
  description: string
}

export interface Tags {
  tags: Tag[]
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
  columnLineage: InputFields[]
}

interface InputField {
  namespace: string
  dataset: string
  field: string
  transformationDescription: string | null
  transformationType: string | null
}

interface InputFields {
  name: string
  inputFields: InputField[]
  transformationDescription: string | null
  transformationType: string | null
}

export interface DatasetVersions {
  totalCount: number
  versions: DatasetVersion[]
}

export interface DataQualityFacets {
  dataQualityAssertions?: {
    assertions?: Assertion[]
  }
}

export interface Assertion {
  assertion: string
  column: string
  success: boolean
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
  type: Nullable<string>
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
  latestRuns: Run[]
  tags: string[]
  parentJobName: Nullable<string>
  parentJobUuid: Nullable<string>
}

export interface JobId {
  namespace: string
  name: string
}

export type JobType = 'BATCH' | 'STREAM' | 'SERVICE'

export interface Runs {
  runs: Run[]
  totalCount: number
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

// OpenSearch

// jobs
interface SourceCodeFacet {
  language: string
  _producer: string
  _schemaURL: string
  sourceCode: string
}

interface OpenSearchFacet {
  sourceCode?: SourceCodeFacet
}

interface JobHit {
  run_id: string
  name: string
  namespace: string
  eventType: EventType
  type: string
  facets?: OpenSearchFacet
  runFacets: OpenSearchRunFacet
}

interface SparkLogicalPlan {
  _producer: string
  _schemaURL: string
  plan: Plan[]
}

interface Plan {
  class: string
  numChildren: number
  ifPartitionNotExists?: boolean
  partitionColumns?: any[]
  query?: number
  outputColumnNames?: string
  output?: AttributeReference[][]
  isStreaming?: boolean
}

interface AttributeReference {
  class: string
  numChildren: number
  name: string
  dataType: string
  nullable: boolean
  metadata: Record<string, any>
  exprId: ExprId
  qualifier: any[]
}

interface ExprId {
  productClass: string
  id: number
  jvmId: string
}

interface SparkVersion {
  _producer: string
  _schemaURL: string
  sparkVersion: string
  openlineageSparkVersion: string
}

interface ProcessingEngine {
  _producer: string
  _schemaURL: string
  version: string
  name: string
  openlineageAdapterVersion: string
}

interface EnvironmentProperties {
  _producer: string
  _schemaURL: string
  environmentProperties: Record<string, any>
}

interface OpenSearchRunFacet {
  'spark.logicalPlan'?: SparkLogicalPlan
  spark_version?: SparkVersion
  processing_engine?: ProcessingEngine
  'environment-properties'?: EnvironmentProperties
}

interface JobHighlight {
  'facets.sourceCode.sourceCode'?: string[]
}

export interface OpenSearchResultJobs {
  hits: JobHit[]
  highlights: JobHighlight[]
}

// datasets
type DatasetHighlight = {
  [key: string]: string[]
}

type SearchInputField = {
  namespace: string
  name: string
  field: string
}

type ColumnLineageField = {
  inputFields: SearchInputField[]
  transformationDescription: string
  transformationType: string
}

type SchemaField = {
  name: string
  type: string
  fields: any[]
}

type SchemaFacet = {
  _producer: string
  _schemaURL: string
  fields: SchemaField[]
}

type ColumnLineageFacet = {
  _producer: string
  _schemaURL: string
  fields: {
    [key: string]: ColumnLineageField
  }
}

type OpenSearchDatasetFacets = {
  schema?: SchemaFacet
  columnLineage?: ColumnLineageFacet
}

type DatasetHit = {
  run_id: string
  name: string
  namespace: string
  eventType: string
  facets?: OpenSearchDatasetFacets
}

export type OpenSearchResultDatasets = {
  hits: DatasetHit[]
  highlights: DatasetHighlight[]
}
