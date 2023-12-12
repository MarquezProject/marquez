import { ColumnLevelDatasetData } from './ColumnLevelDatasetNode'
import { ColumnLineageColumnNode, ColumnLineageDatasetNode } from './NodeRender'
import { Edge, Node as ElkNode, NodeRendererMap } from '../../../libs/graph'

export interface ColumnLineageColumnNodeData {
  column: string
}

export interface ColumnLineageDatasetNodeData {
  namespace: string
  dataset: string
}

export type MultipleNodeKind = 'dataset' | 'column'
export type MultipleNodeData = ColumnLineageDatasetNodeData | ColumnLineageColumnNodeData

export const columnLevelNodeRenderer: NodeRendererMap<MultipleNodeKind, MultipleNodeData> =
  new Map().set('dataset', ColumnLineageDatasetNode).set('column', ColumnLineageColumnNode)

export const nodes: ElkNode<'simple', ColumnLevelDatasetData>[] = [
  {
    id: 'node1',
    kind: 'simple',
    data: { name: 'Node 1' },
  },

  {
    id: 'node2',
    kind: 'simple',
    data: {
      name: ' Node 2',
    },
  },
]

export const edges: Edge[] = [
  {
    id: '1 to 2',
    sourceNodeId: 'node1',
    targetNodeId: 'node2',
  },
]
