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

export type MultipleNodeKind = 'dataset' | 'column'
export type MultipleNodeData = ColumnLineageDatasetNodeData | ColumnLineageColumnNodeData

export const columnLevelNodeRenderer: NodeRendererMap<MultipleNodeKind, MultipleNodeData> =
  new Map().set('dataset', ColumnLineageDatasetNode).set('column', ColumnLineageColumnNode)
