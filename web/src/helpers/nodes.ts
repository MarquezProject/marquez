// SPDX-License-Identifier: Apache-2.0

import { JobOrDataset, LineageDataset, LineageJob, MqNode } from '../components/lineage/types'
import { Undefinable } from '../types/util/Nullable'

export function isJob(node: MqNode): Undefinable<LineageJob> {
  if (node.data.type === 'BATCH') {
    return node.data as LineageJob
  }
  return undefined
}

export function isDataset(node: MqNode): Undefinable<LineageDataset> {
  if (node.data.type === 'DB_TABLE') {
    return node.data as LineageDataset
  }
  return undefined
}

export function encodeNode(nodeType: JobOrDataset, namespace: string, name: string) {
  return `${encodeURIComponent(nodeType.toLowerCase())}/${encodeURIComponent(
    namespace
  )}/${encodeURIComponent(name)}`
}

export function generateNodeId(type: JobOrDataset, namespace: string, jobName: string) {
  return `${type.toLowerCase()}:${namespace}:${jobName}`
}

export function isLineageJob(
  node: Undefinable<LineageJob | LineageDataset>
): Undefinable<LineageJob> {
  if (node && node.type === 'BATCH') {
    return node as LineageJob
  }
  return undefined
}

export function isLineageDataset(
  node: Undefinable<LineageJob | LineageDataset>
): Undefinable<LineageDataset> {
  if (node && node.type === 'DB_TABLE') {
    return node as LineageDataset
  }
  return undefined
}

const searchDelimiterMap = {
  namespace: 0,
  group: 1
}

type SearchDelimiterMap = typeof searchDelimiterMap

export function parseSearchGroup(nodeId: string, field: keyof SearchDelimiterMap) {
  return nodeId.split(':')[searchDelimiterMap[field]] || ''
}
