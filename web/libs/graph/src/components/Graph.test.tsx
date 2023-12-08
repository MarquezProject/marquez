import React from 'react'

import '@testing-library/jest-dom'

import { PositionedNode } from '../index'

/* CUSTOM NODES */
export interface SimpleNodeData {
  displayName?: string
}

interface SimpleNodeProps {
  node: PositionedNode<'simple', SimpleNodeData>
}

// a task node has a name and an operator that get displayed
const SimpleNode = ({ node }: SimpleNodeProps) => (
  <rect
    x={10}
    y={20}
    width={node.width}
    height={node.height}
    data-testid={node.id}
    data-custom='true'
  />
)

SimpleNode.getLayoutOptions = (node: SimpleNodeProps['node']) => ({
  ...node,
  width: node.width ?? 100,
  height: node.height ?? 100,
})
