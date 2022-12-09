// SPDX-License-Identifier: Apache-2.0

import { Field, Run, Tag } from '../../types/api'
import { Nullable } from '../../types/util/Nullable'

export type JobOrDataset = 'JOB' | 'DATASET'
export type BatchOrStream = 'BATCH' | 'STREAM' | 'SERVICE'
export type DbTableOrStream = 'DB_TABLE' | 'STREAM'

export interface LineageDataset {
  id: { namespace: string; name: string }
  type: DbTableOrStream
  name: string
  physicalName: string
  createdAt: string
  updatedAt: string
  namespace: string
  sourceName: string
  fields: Field[]
  facets: object
  tags: Tag[]
  lastModifiedAt: string
  description: string
}

export interface LineageJob {
  id: { namespace: string; name: string }
  type: BatchOrStream
  name: string
  createdAt: string
  updatedAt: string
  namespace: string
  inputs: { namespace: string; name: string }[]
  outputs: { namespace: string; name: string }[]
  location: string
  context: {
    [key: string]: string
  }
  description: string
  latestRun: Nullable<Run>
}

export interface LineageEdge {
  origin: string
  destination: string
}

export interface LineageNode {
  id: string
  type: JobOrDataset
  data: LineageDataset | LineageJob
  inEdges: LineageEdge[]
  outEdges: LineageEdge[]
}

export interface MqNode {
  data: LineageDataset | LineageJob
}
