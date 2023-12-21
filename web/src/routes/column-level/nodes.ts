import { ColumnLineageDatasetNode } from './ColumnLineageDatasetNode'
import { NodeRendererMap } from '../../../libs/graph'
import ColumnLineageColumnNode from './ColumnLineageColumnNode'

export interface ColumnLineageColumnNodeData {
  column: string
  namespace: string
  dataset: string
}

export interface ColumnLineageDatasetNodeData {
  namespace: string
  dataset: string
}

export type ColumnLevelNodeKinds = 'dataset' | 'column'
export type ColumnLevelNodeData = ColumnLineageDatasetNodeData | ColumnLineageColumnNodeData

export const columnLevelNodeRenderer: NodeRendererMap<ColumnLevelNodeKinds, ColumnLevelNodeData> =
  new Map().set('dataset', ColumnLineageDatasetNode).set('column', ColumnLineageColumnNode)
