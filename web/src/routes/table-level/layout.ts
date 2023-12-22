import { Edge, Node as ElkNode } from '../../../libs/graph'
import { LineageGraph } from '../../types/api'

import { JobOrDataset, LineageDataset, LineageJob } from '../../components/lineage/types'
import { TableLevelNodeData } from './nodes'
import { theme } from '../../helpers/theme'

export const createElkNodes = (lineageGraph: LineageGraph, isCompact: boolean) => {
  const nodes: ElkNode<JobOrDataset, TableLevelNodeData>[] = []
  const edges: Edge[] = []

  for (const node of lineageGraph.graph) {
    edges.push(
      ...node.outEdges.map((edge) => {
        return {
          id: `${edge.origin}:${edge.destination}`,
          sourceNodeId: edge.origin,
          targetNodeId: edge.destination,
          color: theme.palette.grey[400],
        }
      })
    )

    if (node.type === 'JOB') {
      nodes.push({
        id: node.id,
        kind: node.type,
        width: 112,
        height: 24,
        data: {
          job: node.data as LineageJob,
        },
      })
    } else if (node.type === 'DATASET') {
      const data = node.data as LineageDataset
      nodes.push({
        id: node.id,
        kind: node.type,
        width: 112,
        height: isCompact ? 24 : 34 + data.fields.length * 10,
        data: {
          dataset: data,
        },
      })
    }
  }
  return { nodes, edges }
}
