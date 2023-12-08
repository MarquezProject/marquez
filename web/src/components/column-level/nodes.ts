import { ColumnLevelDatasetData } from './ColumnLevelDatasetNode'
import { Edge, Node as ElkNode } from '../../../libs/graph'

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
