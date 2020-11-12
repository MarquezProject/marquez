import { LineageDataset, LineageJob, MqNode } from '../components/lineage/types'
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
