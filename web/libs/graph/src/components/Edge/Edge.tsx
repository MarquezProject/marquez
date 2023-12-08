import React from 'react'

import { ElbowEdge } from './ElbowEdge'
import { StraightEdge } from './StraightEdge'
import type { PositionedEdge } from '../../types'

export interface EdgeProps {
  edge: PositionedEdge
  isMiniMap?: boolean
}

export const Edge = (props: EdgeProps) => {
  const { edge } = props
  switch (edge.type) {
    case 'straight':
      return <StraightEdge {...props} />
    default:
      return <ElbowEdge {...props} />
  }
}
