import { Edge, Node as ElkNode } from '../../../libs/graph'
import { LineageGraph } from '../../types/api'

import {
  JobOrDataset,
  LineageDataset,
  LineageJob,
  LineageNode,
} from '../../components/lineage/types'
import { Nullable } from '../../types/util/Nullable'
import { TableLevelNodeData } from './nodes'
import { theme } from '../../helpers/theme'

/**
 * Recursively trace the `inEdges` and `outEdges` of the current node to find all connected column nodes
 * @param lineageGraph
 * @param currentGraphNode
 */
export const findDownstreamNodes = (
  lineageGraph: LineageGraph,
  currentGraphNode: Nullable<string>
): LineageNode[] => {
  if (!currentGraphNode) return []
  const currentNode = lineageGraph.graph.find((node) => node.id === currentGraphNode)
  if (!currentNode) return []
  const connectedNodes: LineageNode[] = []
  const visitedNodes: string[] = []
  const queue: LineageNode[] = [currentNode]

  while (queue.length) {
    const currentNode = queue.shift()
    if (!currentNode) continue
    if (visitedNodes.includes(currentNode.id)) continue
    visitedNodes.push(currentNode.id)
    connectedNodes.push(currentNode)
    queue.push(
      ...currentNode.outEdges
        .map((edge) => lineageGraph.graph.find((n) => n.id === edge.destination))
        .filter((item): item is LineageNode => !!item)
    )
  }
  return connectedNodes
}

export const findUpstreamNodes = (
  lineageGraph: LineageGraph,
  currentGraphNode: Nullable<string>
): LineageNode[] => {
  if (!currentGraphNode) return []
  const currentNode = lineageGraph.graph.find((node) => node.id === currentGraphNode)
  if (!currentNode) return []
  const connectedNodes: LineageNode[] = []
  const visitedNodes: string[] = []
  const queue: LineageNode[] = [currentNode]

  while (queue.length) {
    const currentNode = queue.shift()
    if (!currentNode) continue
    if (visitedNodes.includes(currentNode.id)) continue
    visitedNodes.push(currentNode.id)
    connectedNodes.push(currentNode)
    queue.push(
      ...currentNode.inEdges
        .map((edge) => lineageGraph.graph.find((n) => n.id === edge.origin))
        .filter((item): item is LineageNode => !!item)
    )
  }
  return connectedNodes
}

export const createElkNodes = (
  lineageGraph: LineageGraph,
  currentGraphNode: Nullable<string>,
  isCompact: boolean
) => {
  const nodes: ElkNode<JobOrDataset, TableLevelNodeData>[] = []
  const edges: Edge[] = []

  const downstreamNodes = findDownstreamNodes(lineageGraph, currentGraphNode)
  const upstreamNodes = findUpstreamNodes(lineageGraph, currentGraphNode)

  for (const node of lineageGraph.graph) {
    edges.push(
      ...node.outEdges.map((edge) => {
        return {
          id: `${edge.origin}:${edge.destination}`,
          sourceNodeId: edge.origin,
          targetNodeId: edge.destination,
          color:
            downstreamNodes.includes(node) || upstreamNodes.includes(node)
              ? theme.palette.primary.main
              : theme.palette.grey[400],
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
