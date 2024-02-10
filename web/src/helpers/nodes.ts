// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import { DataQualityFacets, EventType, Run, RunState } from '../types/api'
import { JobOrDataset, LineageDataset, LineageJob, MqNode } from '../components/lineage/types'
import { Undefinable } from '../types/util/Nullable'
import { theme } from './theme'

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
  group: 1,
}

type SearchDelimiterMap = typeof searchDelimiterMap

export function parseSearchGroup(nodeId: string, field: keyof SearchDelimiterMap) {
  return nodeId.split(':')[searchDelimiterMap[field]] || ''
}

export function eventTypeColor(state: EventType) {
  switch (state) {
    case 'START':
      return theme.palette.info.main
    case 'RUNNING':
      return theme.palette.info.main
    case 'COMPLETE':
      return theme.palette.primary.main
    case 'FAIL':
      return theme.palette.error.main
    case 'ABORT':
      return theme.palette.warning.main
    default:
      return theme.palette.secondary.main
  }
}

export function runStateColor(state: RunState) {
  switch (state) {
    case 'NEW':
      return theme.palette.secondary.main
    case 'RUNNING':
      return theme.palette.info.main
    case 'COMPLETED':
      return theme.palette.primary.main
    case 'FAILED':
      return theme.palette.error.main
    case 'ABORTED':
      return theme.palette.warning.main
    default:
      return theme.palette.secondary.main
  }
}

export function jobRunsStatus(runs: Run[], limit = 14) {
  runs = runs.slice(-limit)

  const isAllFailed = runs.every((e) => e.state === 'FAILED')
  const isSomeFailed = runs.some((e) => e.state === 'FAILED')

  if (isAllFailed) {
    return theme.palette.error.main as string
  } else if (isSomeFailed) {
    return theme.palette.info.main as string
  } else {
    return theme.palette.primary.main as string
  }
}

export function datasetFacetsStatus(facets: DataQualityFacets, limit = 14) {
  const assertions = facets?.dataQualityAssertions?.assertions?.slice(-limit)

  if (!assertions?.length) {
    return null
  }

  const isAllFalse = assertions.every((e: any) => e.success === false)
  const isSomeFalse = assertions.some((e: any) => e.success === false)

  if (isAllFalse) {
    return theme.palette.error.main as string
  } else if (isSomeFalse) {
    return theme.palette.info.main as string
  } else {
    return theme.palette.primary.main as string
  }
}
