import { Edge, Node as ElkNode } from '../../../libs/graph'
import { LineageGraph } from '../../types/api'

import { JobOrDataset, LineageDataset, LineageJob } from '../../components/lineage/types'
import { TableLevelNodeData } from './nodes'
import { theme } from '../../helpers/theme'

/**
 Node of format dataset:food_delivery:public.categories
 Node of format {type}:{namespace}:{name}
 */
export const parseTableLineageNode = (node: string) => {
  const [type, namespace, dataset] = node.split(':')
  return { type, namespace, dataset }
}

export const createElkNodes = (lineageGraph: LineageGraph) => {
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
        width: 64,
        height: 64,
        data: {
          job: node.data as LineageJob,
        },
      })
    } else if (node.type === 'DATASET') {
      nodes.push({
        id: node.id,
        kind: node.type,
        width: 64,
        height: 64,
        data: {
          dataset: node.data as LineageDataset,
        },
      })
    }
  }
  return { nodes, edges }
}
