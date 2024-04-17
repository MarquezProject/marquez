import React from 'react'

import { Edge } from '../Edge'
import type { NodeRenderer, PositionedEdge, PositionedNode } from '../../types'

interface Props<K, D> {
  node: PositionedNode<K, D>
  nodeRenderers: Map<K, NodeRenderer<K, D>>
  edges?: PositionedEdge[]
  isMiniMap?: boolean
}

export const Node = <K, D>({ node, nodeRenderers, edges, isMiniMap }: Props<K, D>) => {
  const NodeRenderer = nodeRenderers.get(node.kind)
  return (
    <g x={0} y={0} transform={`translate(${node.bottomLeftCorner.x} ${node.bottomLeftCorner.y})`}>
      {NodeRenderer && <NodeRenderer node={node} isMiniMap={isMiniMap} />}
      {node.children?.map((child) => (
        <Node<K, D>
          node={child}
          nodeRenderers={nodeRenderers}
          edges={edges}
          key={child.id}
          isMiniMap={isMiniMap}
        />
      ))}

      {edges
        ?.filter((edge) => edge.container === node.id)
        .map((edge) => (
          <Edge key={edge.id} edge={edge} isMiniMap={isMiniMap} />
        ))}
    </g>
  )
}
