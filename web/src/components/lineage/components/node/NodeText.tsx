// SPDX-License-Identifier: Apache-2.0

import { Node as GraphNode } from 'dagre'
import { MqNode } from '../../types'
import { theme } from '../../../../helpers/theme'
import React from 'react'

type NodeTextProps = {
  node: GraphNode<MqNode>
}

const TEXT_BOTTOM_SPACING = theme.spacing(3)
const MAX_CHARACTERS = 20
const LEADING_AND_TRAILING_CHARACTERS = 10

export function NodeText({ node }: NodeTextProps) {
  return (
    <text
      x={node.x}
      y={node.y + TEXT_BOTTOM_SPACING}
      fontSize='8'
      textAnchor='middle'
      fill={'white'}
    >
      {node.data.name.length > MAX_CHARACTERS
        ? `${node.data.name.substring(
            0,
            LEADING_AND_TRAILING_CHARACTERS
          )}…${node.data.name.substring(node.data.name.length - LEADING_AND_TRAILING_CHARACTERS)}`
        : node.data.name}
    </text>
  )
}
